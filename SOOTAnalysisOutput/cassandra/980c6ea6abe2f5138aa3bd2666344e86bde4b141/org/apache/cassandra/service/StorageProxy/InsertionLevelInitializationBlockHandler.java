package org.apache.cassandra.service;

import java.nio.ByteBuffer;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.cache.CacheLoader;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.PeekingIterator;
import com.google.common.primitives.Ints;
import com.google.common.util.concurrent.Uninterruptibles;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.cassandra.batchlog.Batch;
import org.apache.cassandra.batchlog.BatchlogManager;
import org.apache.cassandra.concurrent.Stage;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.db.ColumnFamilyStore;
import org.apache.cassandra.db.ConsistencyLevel;
import org.apache.cassandra.db.CounterMutation;
import org.apache.cassandra.db.DecoratedKey;
import org.apache.cassandra.db.IMutation;
import org.apache.cassandra.db.Keyspace;
import org.apache.cassandra.db.Mutation;
import org.apache.cassandra.db.PartitionPosition;
import org.apache.cassandra.db.PartitionRangeReadCommand;
import org.apache.cassandra.db.ReadCommand;
import org.apache.cassandra.db.ReadExecutionController;
import org.apache.cassandra.db.ReadResponse;
import org.apache.cassandra.db.SinglePartitionReadCommand;
import org.apache.cassandra.db.TruncateRequest;
import org.apache.cassandra.db.WriteType;
import org.apache.cassandra.config.SchemaConstants;
import org.apache.cassandra.db.filter.DataLimits;
import org.apache.cassandra.db.filter.TombstoneOverwhelmingException;
import org.apache.cassandra.db.partitions.FilteredPartition;
import org.apache.cassandra.db.partitions.PartitionIterator;
import org.apache.cassandra.db.partitions.PartitionIterators;
import org.apache.cassandra.db.partitions.PartitionUpdate;
import org.apache.cassandra.db.partitions.UnfilteredPartitionIterator;
import org.apache.cassandra.db.rows.RowIterator;
import org.apache.cassandra.db.view.ViewUtils;
import org.apache.cassandra.dht.AbstractBounds;
import org.apache.cassandra.dht.Bounds;
import org.apache.cassandra.dht.RingPosition;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.exceptions.CasWriteTimeoutException;
import org.apache.cassandra.exceptions.CasWriteUnknownResultException;
import org.apache.cassandra.exceptions.InvalidRequestException;
import org.apache.cassandra.exceptions.IsBootstrappingException;
import org.apache.cassandra.exceptions.OverloadedException;
import org.apache.cassandra.exceptions.ReadFailureException;
import org.apache.cassandra.exceptions.ReadTimeoutException;
import org.apache.cassandra.exceptions.RequestFailureException;
import org.apache.cassandra.exceptions.RequestFailureReason;
import org.apache.cassandra.exceptions.RequestTimeoutException;
import org.apache.cassandra.exceptions.UnavailableException;
import org.apache.cassandra.exceptions.WriteFailureException;
import org.apache.cassandra.exceptions.WriteTimeoutException;
import org.apache.cassandra.gms.Gossiper;
import org.apache.cassandra.hints.Hint;
import org.apache.cassandra.hints.HintsService;
import org.apache.cassandra.index.Index;
import org.apache.cassandra.locator.AbstractReplicationStrategy;
import org.apache.cassandra.locator.EndpointsForRange;
import org.apache.cassandra.locator.EndpointsForToken;
import org.apache.cassandra.locator.IEndpointSnitch;
import org.apache.cassandra.locator.InetAddressAndPort;
import org.apache.cassandra.locator.LocalStrategy;
import org.apache.cassandra.locator.Replica;
import org.apache.cassandra.locator.ReplicaLayout;
import org.apache.cassandra.locator.ReplicaPlan;
import org.apache.cassandra.locator.ReplicaPlans;
import org.apache.cassandra.locator.Replicas;
import org.apache.cassandra.locator.TokenMetadata;
import org.apache.cassandra.metrics.CASClientRequestMetrics;
import org.apache.cassandra.metrics.CASClientWriteRequestMetrics;
import org.apache.cassandra.metrics.ClientRequestMetrics;
import org.apache.cassandra.metrics.ClientWriteRequestMetrics;
import org.apache.cassandra.metrics.ReadRepairMetrics;
import org.apache.cassandra.metrics.StorageMetrics;
import org.apache.cassandra.metrics.ViewWriteMetrics;
import org.apache.cassandra.net.ForwardingInfo;
import org.apache.cassandra.net.Message;
import org.apache.cassandra.net.MessageFlag;
import org.apache.cassandra.net.MessagingService;
import org.apache.cassandra.net.RequestCallback;
import org.apache.cassandra.net.Verb;
import org.apache.cassandra.schema.Schema;
import org.apache.cassandra.schema.SchemaConstants;
import org.apache.cassandra.schema.TableMetadata;
import org.apache.cassandra.service.paxos.Commit;
import org.apache.cassandra.service.paxos.PaxosState;
import org.apache.cassandra.service.paxos.PrepareCallback;
import org.apache.cassandra.service.paxos.ProposeCallback;
import org.apache.cassandra.service.reads.AbstractReadExecutor;
import org.apache.cassandra.service.reads.DataResolver;
import org.apache.cassandra.service.reads.ReadCallback;
import org.apache.cassandra.service.reads.repair.ReadRepair;
import org.apache.cassandra.tracing.Tracing;
import org.apache.cassandra.triggers.TriggerExecutor;
import org.apache.cassandra.utils.AbstractIterator;
import org.apache.cassandra.utils.FBUtilities;
import org.apache.cassandra.utils.MBeanWrapper;
import org.apache.cassandra.utils.MonotonicClock;
import org.apache.cassandra.utils.Pair;
import org.apache.cassandra.utils.UUIDGen;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.NANOSECONDS;
import static org.apache.cassandra.net.NoPayload.noPayload;
import static org.apache.cassandra.net.Verb.BATCH_STORE_REQ;
import static org.apache.cassandra.net.Verb.MUTATION_REQ;
import static org.apache.cassandra.net.Verb.PAXOS_COMMIT_REQ;
import static org.apache.cassandra.net.Verb.PAXOS_PREPARE_REQ;
import static org.apache.cassandra.net.Verb.PAXOS_PROPOSE_REQ;
import static org.apache.cassandra.net.Verb.TRUNCATE_REQ;
import static org.apache.cassandra.service.BatchlogResponseHandler.BatchlogCleanup;
import static org.apache.cassandra.service.paxos.PrepareVerbHandler.doPrepare;
import static org.apache.cassandra.service.paxos.ProposeVerbHandler.doPropose;

public class StorageProxy implements StorageProxyMBean {<<<<<<< MINE

=======


  private static class SinglePartitionReadLifecycle {

    private final SinglePartitionReadCommand command;

    private final AbstractReadExecutor executor;

    private final ConsistencyLevel consistency;

    private final long queryStartNanoTime;

    private PartitionIterator result;

    private ReadCallback repairHandler;

    SinglePartitionReadLifecycle(SinglePartitionReadCommand command, ConsistencyLevel consistency, long queryStartNanoTime) {
      this.command = command;
      this.executor = AbstractReadExecutor.getReadExecutor(command, consistency, queryStartNanoTime);
      this.consistency = consistency;
      this.queryStartNanoTime = queryStartNanoTime;
    }

    boolean isDone() {
      return result != null;
    }

    void doInitialQueries() {
      executor.executeAsync();
    }

    void maybeTryAdditionalReplicas() {
      executor.maybeTryAdditionalReplicas();
    }

    void awaitResultsAndRetryOnDigestMismatch() throws ReadFailureException, ReadTimeoutException {
      try {
        result = executor.get();
      } catch (DigestMismatchException ex) {
        Tracing.trace("Digest mismatch: {}", ex);
        ReadRepairMetrics.repairedBlocking.mark();
        Keyspace keyspace = Keyspace.open(command.metadata().ksName);
        DataResolver resolver = new DataResolver(keyspace, command, ConsistencyLevel.ALL, executor.handler.endpoints.size(), queryStartNanoTime);
        repairHandler = new ReadCallback(resolver, ConsistencyLevel.ALL, executor.getContactedReplicas().size(), command, keyspace, executor.handler.endpoints, queryStartNanoTime);
        for (InetAddress endpoint : executor.getContactedReplicas()) {
          MessageOut<ReadCommand> message = command.createMessage(MessagingService.instance().getVersion(endpoint));
          Tracing.trace("Enqueuing full data read to {}", endpoint);
          MessagingService.instance().sendRRWithFailure(message, endpoint, repairHandler);
        }
      }
    }

    void maybeAwaitFullDataRead() throws ReadTimeoutException {
      if (repairHandler == null)
        return;
      try {
        result = repairHandler.get();
      } catch (DigestMismatchException e) {
        throw new AssertionError(e);
      } catch (ReadTimeoutException e) {
        if (Tracing.isTracing())
          Tracing.trace("Timed out waiting on digest mismatch repair requests");
        else
          logger.trace("Timed out waiting on digest mismatch repair requests");
        int blockFor = consistency.blockFor(Keyspace.open(command.metadata().ksName));
        throw new ReadTimeoutException(consistency, blockFor - 1, blockFor, true);
      }
    }

    PartitionIterator getResult() {
      assert result != null;
      return result;
    }
  }
>>>>>>> YOURS

    public static final String MBEAN_NAME = "org.apache.cassandra.db:type=StorageProxy";

    private static final Logger logger = LoggerFactory.getLogger(StorageProxy.class);

    public static final String UNREACHABLE = "UNREACHABLE";

    private static final WritePerformer standardWritePerformer;

    private static final WritePerformer counterWritePerformer;

    private static final WritePerformer counterWriteOnCoordinatorPerformer;

    public static final StorageProxy instance = new StorageProxy();

    private static volatile int maxHintsInProgress = 128 * FBUtilities.getAvailableProcessors();

    private static final CacheLoader<InetAddressAndPort, AtomicInteger> hintsInProgress = new CacheLoader<InetAddressAndPort, AtomicInteger>() {

        public AtomicInteger load(InetAddressAndPort inetAddress) {
            return new AtomicInteger(0);
        }
    };

    private static final ClientRequestMetrics readMetrics = new ClientRequestMetrics("Read");

    private static final ClientRequestMetrics rangeMetrics = new ClientRequestMetrics("RangeSlice");

    private static final ClientWriteRequestMetrics writeMetrics = new ClientWriteRequestMetrics("Write");

    private static final CASClientWriteRequestMetrics casWriteMetrics = new CASClientWriteRequestMetrics("CASWrite");

    private static final CASClientRequestMetrics casReadMetrics = new CASClientRequestMetrics("CASRead");

    private static final ViewWriteMetrics viewWriteMetrics = new ViewWriteMetrics("ViewWrite");

    private static final Map<ConsistencyLevel, ClientRequestMetrics> readMetricsMap = new EnumMap<>(ConsistencyLevel.class);

<<<<<<< MINE
private static final Map<ConsistencyLevel, ClientWriteRequestMetrics> writeMetricsMap = new EnumMap<>(ConsistencyLevel.class);
=======
private static final Map<ConsistencyLevel, ClientRequestMetrics> writeMetricsMap = new EnumMap<>(ConsistencyLevel.class);
>>>>>>> YOURS


    private static final double CONCURRENT_SUBREQUESTS_MARGIN = 0.10;

    private static final int MAX_CONCURRENT_RANGE_REQUESTS = Math.max(1, Integer.getInteger("cassandra.max_concurrent_range_requests", FBUtilities.getAvailableProcessors() * 10));

<<<<<<< MINE
    private StorageProxy() {
    }
=======
        private SingleRangeResponse query(RangeForQuery toQuery, boolean isFirst) {
            PartitionRangeReadCommand rangeCommand = command.forSubRange(toQuery.range, isFirst);
            DataResolver resolver = new DataResolver(keyspace, rangeCommand, consistency, toQuery.filteredEndpoints.size(), queryStartNanoTime);
            int blockFor = consistency.blockFor(keyspace);
            int minResponses = Math.min(toQuery.filteredEndpoints.size(), blockFor);
            List<InetAddress> minimalEndpoints = toQuery.filteredEndpoints.subList(0, minResponses);
            ReadCallback handler = new ReadCallback(resolver, consistency, rangeCommand, minimalEndpoints, queryStartNanoTime);
            handler.assureSufficientLiveNodes();
            if (toQuery.filteredEndpoints.size() == 1 && canDoLocalRequest(toQuery.filteredEndpoints.get(0))) {
                StageManager.getStage(Stage.READ).execute(new LocalReadRunnable(rangeCommand, handler));
            } else {
                for (InetAddress endpoint : toQuery.filteredEndpoints) {
                    MessageOut<ReadCommand> message = rangeCommand.createMessage(MessagingService.instance().getVersion(endpoint));
                    Tracing.trace("Enqueuing request to {}", endpoint);
                    MessagingService.instance().sendRRWithFailure(message, endpoint, handler);
                }
            }
            return new SingleRangeResponse(handler);
        }
>>>>>>> YOURS

    static {
        MBeanWrapper.instance.registerMBean(instance, MBEAN_NAME);
        HintsService.instance.registerMBean();
        standardWritePerformer = (mutation, targets, responseHandler, localDataCenter) -> {
            assert mutation instanceof Mutation;
            sendToHintedReplicas((Mutation) mutation, targets, responseHandler, localDataCenter, Stage.MUTATION);
        };
        counterWritePerformer = (mutation, targets, responseHandler, localDataCenter) -> {
            EndpointsForToken selected = targets.contacts().withoutSelf();
            Replicas.temporaryAssertFull(selected);
            counterWriteTask(mutation, targets.withContact(selected), responseHandler, localDataCenter).run();
        };
        counterWriteOnCoordinatorPerformer = (mutation, targets, responseHandler, localDataCenter) -> {
            EndpointsForToken selected = targets.contacts().withoutSelf();
            Replicas.temporaryAssertFull(selected);
            Stage.COUNTER_MUTATION.executor().execute(counterWriteTask(mutation, targets.withContact(selected), responseHandler, localDataCenter));
        };
        for (ConsistencyLevel level : ConsistencyLevel.values()) {
            readMetricsMap.put(level, new ClientRequestMetrics("Read-" + level.name()));
            writeMetricsMap.put(level, new ClientWriteRequestMetrics("Write-" + level.name()));
        }
        ReadRepairMetrics.init();
    }

    public static RowIterator cas(String keyspaceName, String cfName, DecoratedKey key, CASRequest request, ConsistencyLevel consistencyForPaxos, ConsistencyLevel consistencyForCommit, ClientState state, int nowInSeconds, long queryStartNanoTime) throws UnavailableException, IsBootstrappingException, RequestFailureException, RequestTimeoutException, InvalidRequestException, CasWriteUnknownResultException {
        final long startTimeForMetrics = System.nanoTime();
        TableMetadata metadata = Schema.instance.getTableMetadata(keyspaceName, cfName);
        int contentions = 0;
        try {
            consistencyForPaxos.validateForCas();
            consistencyForCommit.validateForCasCommit(keyspaceName);
            long timeoutNanos = DatabaseDescriptor.getCasContentionTimeout(NANOSECONDS);
            while (System.nanoTime() - queryStartNanoTime < timeoutNanos) {
                ReplicaPlan.ForPaxosWrite replicaPlan = ReplicaPlans.forPaxos(Keyspace.open(keyspaceName), key, consistencyForPaxos);
                final PaxosBallotAndContention pair = beginAndRepairPaxos(queryStartNanoTime, key, metadata, replicaPlan, consistencyForPaxos, consistencyForCommit, true, state);
                final UUID ballot = pair.ballot;
                contentions += pair.contentions;
                Tracing.trace("Reading existing values for CAS precondition");
                SinglePartitionReadCommand readCommand = (SinglePartitionReadCommand) request.readCommand(nowInSeconds);
                ConsistencyLevel readConsistency = consistencyForPaxos == ConsistencyLevel.LOCAL_SERIAL ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
                FilteredPartition current;
                try (RowIterator rowIter = readOne(readCommand, readConsistency, queryStartNanoTime)) {
                    current = FilteredPartition.create(rowIter);
                }
                if (!request.appliesTo(current)) {
                    Tracing.trace("CAS precondition does not match current values {}", current);
                    casWriteMetrics.conditionNotMet.inc();
                    return current.rowIterator();
                }
                PartitionUpdate updates = request.makeUpdates(current);
                long size = updates.dataSize();
                casWriteMetrics.mutationSize.update(size);
                writeMetricsMap.get(consistencyForPaxos).mutationSize.update(size);
                updates = TriggerExecutor.instance.execute(updates);
                Commit proposal = Commit.newProposal(ballot, updates);
                Tracing.trace("CAS precondition is met; proposing client-requested updates for {}", ballot);
                if (proposePaxos(proposal, replicaPlan, true, queryStartNanoTime)) {
                    commitPaxos(proposal, consistencyForCommit, true, queryStartNanoTime);
                    Tracing.trace("CAS successful");
                    return null;
                }
                Tracing.trace("Paxos proposal not accepted (pre-empted by a higher ballot)");
                contentions++;
                Uninterruptibles.sleepUninterruptibly(ThreadLocalRandom.current().nextInt(100), MILLISECONDS);
            }
            throw new WriteTimeoutException(WriteType.CAS, consistencyForPaxos, 0, consistencyForPaxos.blockFor(Keyspace.open(keyspaceName)));
        } catch (CasWriteUnknownResultException e) {
            casWriteMetrics.unknownResult.mark();
            throw e;
        } catch (WriteTimeoutException wte) {
            casWriteMetrics.timeouts.mark();
            writeMetricsMap.get(consistencyForPaxos).timeouts.mark();
            throw new CasWriteTimeoutException(wte.writeType, wte.consistency, wte.received, wte.blockFor, contentions);
        } catch (ReadTimeoutException e) {
            casWriteMetrics.timeouts.mark();
            writeMetricsMap.get(consistencyForPaxos).timeouts.mark();
            throw e;
        } catch (WriteFailureException | ReadFailureException e) {
            casWriteMetrics.failures.mark();
            writeMetricsMap.get(consistencyForPaxos).failures.mark();
            throw e;
        } catch (UnavailableException e) {
            casWriteMetrics.unavailables.mark();
            writeMetricsMap.get(consistencyForPaxos).unavailables.mark();
            throw e;
        } finally {
            recordCasContention(contentions);
            Keyspace.open(keyspaceName).getColumnFamilyStore(cfName).metric.topCasPartitionContention.addSample(key.getKey(), contentions);
            final long latency = System.nanoTime() - startTimeForMetrics;
            casWriteMetrics.addNano(latency);
            writeMetricsMap.get(consistencyForPaxos).addNano(latency);
        }
    }

<<<<<<< MINE
=======
static {
        MBeanWrapper.instance.registerMBean(instance, MBEAN_NAME);
        HintsService.instance.registerMBean();
        HintedHandOffManager.instance.registerMBean();
        standardWritePerformer = new WritePerformer() {

            public void apply(IMutation mutation, Iterable<InetAddress> targets, AbstractWriteResponseHandler<IMutation> responseHandler, String localDataCenter, ConsistencyLevel consistency_level) throws OverloadedException {
                assert mutation instanceof Mutation;
                sendToHintedEndpoints((Mutation) mutation, targets, responseHandler, localDataCenter, Stage.MUTATION);
            }
        };
        counterWritePerformer = new WritePerformer() {

            public void apply(IMutation mutation, Iterable<InetAddress> targets, AbstractWriteResponseHandler<IMutation> responseHandler, String localDataCenter, ConsistencyLevel consistencyLevel) {
                counterWriteTask(mutation, targets, responseHandler, localDataCenter).run();
            }
        };
        counterWriteOnCoordinatorPerformer = new WritePerformer() {

            public void apply(IMutation mutation, Iterable<InetAddress> targets, AbstractWriteResponseHandler<IMutation> responseHandler, String localDataCenter, ConsistencyLevel consistencyLevel) {
                StageManager.getStage(Stage.COUNTER_MUTATION).execute(counterWriteTask(mutation, targets, responseHandler, localDataCenter));
            }
        };
        for (ConsistencyLevel level : ConsistencyLevel.values()) {
            readMetricsMap.put(level, new ClientRequestMetrics("Read-" + level.name()));
            writeMetricsMap.put(level, new ClientRequestMetrics("Write-" + level.name()));
        }
    }
>>>>>>> YOURS


    private static void recordCasContention(int contentions) {
        if (contentions > 0)
            casWriteMetrics.contention.update(contentions);
    }

<<<<<<< MINE
private static PaxosBallotAndContention beginAndRepairPaxos(long queryStartNanoTime, DecoratedKey key, TableMetadata metadata, ReplicaPlan.ForPaxosWrite paxosPlan, ConsistencyLevel consistencyForPaxos, ConsistencyLevel consistencyForCommit, final boolean isWrite, ClientState state) throws WriteTimeoutException, WriteFailureException {
        long timeoutNanos = DatabaseDescriptor.getCasContentionTimeout(NANOSECONDS);
=======
private static Pair<UUID, Integer> beginAndRepairPaxos(long queryStartNanoTime, DecoratedKey key, CFMetaData metadata, List<InetAddress> liveEndpoints, int requiredParticipants, ConsistencyLevel consistencyForPaxos, ConsistencyLevel consistencyForCommit, final boolean isWrite, ClientState state) throws WriteTimeoutException, WriteFailureException {
        long timeout = TimeUnit.MILLISECONDS.toNanos(DatabaseDescriptor.getCasContentionTimeout());
>>>>>>> YOURS
        PrepareCallback summary = null;
        int contentions = 0;
<<<<<<< MINE
        while (System.nanoTime() - queryStartNanoTime < timeoutNanos) {
=======
        while (System.nanoTime() - queryStartNanoTime < timeout) {
>>>>>>> YOURS
            long minTimestampMicrosToUse = summary == null ? Long.MIN_VALUE : 1 + UUIDGen.microsTimestamp(summary.mostRecentInProgressCommit.ballot);
            long ballotMicros = state.getTimestampForPaxos(minTimestampMicrosToUse);
            UUID ballot = UUIDGen.getRandomTimeUUIDFromMicros(ballotMicros);
            Tracing.trace("Preparing {}", ballot);
            Commit toPrepare = Commit.newPrepare(key, metadata, ballot);
<<<<<<< MINE
            summary = preparePaxos(toPrepare, paxosPlan, queryStartNanoTime);
=======
            summary = preparePaxos(toPrepare, liveEndpoints, requiredParticipants, consistencyForPaxos, queryStartNanoTime);
>>>>>>> YOURS
            if (!summary.promised) {
                Tracing.trace("Some replicas have already promised a higher ballot than ours; aborting");
                contentions++;
                Uninterruptibles.sleepUninterruptibly(ThreadLocalRandom.current().nextInt(100), MILLISECONDS);
                continue;
            }
            Commit inProgress = summary.mostRecentInProgressCommitWithUpdate;
            Commit mostRecent = summary.mostRecentCommit;
            if (!inProgress.update.isEmpty() && inProgress.isAfter(mostRecent)) {
                Tracing.trace("Finishing incomplete paxos round {}", inProgress);
                if (isWrite)
                    casWriteMetrics.unfinishedCommit.inc();
                else
                    casReadMetrics.unfinishedCommit.inc();
                Commit refreshedInProgress = Commit.newProposal(ballot, inProgress.update);
<<<<<<< MINE
                if (proposePaxos(refreshedInProgress, paxosPlan, false, queryStartNanoTime)) {
=======
                if (proposePaxos(refreshedInProgress, liveEndpoints, requiredParticipants, false, consistencyForPaxos, queryStartNanoTime)) {
>>>>>>> YOURS
                    try {
                        commitPaxos(refreshedInProgress, consistencyForCommit, false, queryStartNanoTime);
                    } catch (WriteTimeoutException e) {
                        recordCasContention(contentions);
                        throw new WriteTimeoutException(WriteType.CAS, e.consistency, e.received, e.blockFor);
                    }
                } else {
                    Tracing.trace("Some replicas have already promised a higher ballot than ours; aborting");
                    contentions++;
                    Uninterruptibles.sleepUninterruptibly(ThreadLocalRandom.current().nextInt(100), MILLISECONDS);
                }
                continue;
            }
            int nowInSec = Ints.checkedCast(TimeUnit.MICROSECONDS.toSeconds(ballotMicros));
            Iterable<InetAddressAndPort> missingMRC = summary.replicasMissingMostRecentCommit(metadata, nowInSec);
            if (Iterables.size(missingMRC) > 0) {
                Tracing.trace("Repairing replicas that missed the most recent commit");
                sendCommit(mostRecent, missingMRC);
                continue;
            }
            return new PaxosBallotAndContention(ballot, contentions);
        }
        recordCasContention(contentions);
<<<<<<< MINE
        throw new WriteTimeoutException(WriteType.CAS, consistencyForPaxos, 0, consistencyForPaxos.blockFor(Keyspace.open(metadata.keyspace)));
=======
        throw new WriteTimeoutException(WriteType.CAS, consistencyForPaxos, 0, consistencyForPaxos.blockFor(Keyspace.open(metadata.ksName)));
>>>>>>> YOURS
    }

    private static void sendCommit(Commit commit, Iterable<InetAddressAndPort> replicas) {
        Message<Commit> message = Message.out(PAXOS_COMMIT_REQ, commit);
        for (InetAddressAndPort target : replicas) MessagingService.instance().send(message, target);
    }

    private static PrepareCallback preparePaxos(Commit toPrepare, ReplicaPlan.ForPaxosWrite replicaPlan, long queryStartNanoTime) throws WriteTimeoutException {
        PrepareCallback callback = new PrepareCallback(toPrepare.update.partitionKey(), toPrepare.update.metadata(), replicaPlan.requiredParticipants(), replicaPlan.consistencyLevel(), queryStartNanoTime);
        Message<Commit> message = Message.out(PAXOS_PREPARE_REQ, toPrepare);
        for (Replica replica : replicaPlan.contacts()) {
            if (replica.isSelf()) {
                PAXOS_PREPARE_REQ.stage.execute(() -> {
                    try {
                        callback.onResponse(message.responseWith(doPrepare(toPrepare)));
                    } catch (Exception ex) {
                        logger.error("Failed paxos prepare locally", ex);
                    }
                });
            } else {
                MessagingService.instance().sendWithCallback(message, replica.endpoint(), callback);
            }
        }
        callback.await();
        return callback;
    }

    private static boolean proposePaxos(Commit proposal, ReplicaPlan.ForPaxosWrite replicaPlan, boolean backoffIfPartial, long queryStartNanoTime) throws WriteTimeoutException, CasWriteUnknownResultException {
        ProposeCallback callback = new ProposeCallback(replicaPlan.contacts().size(), replicaPlan.requiredParticipants(), !backoffIfPartial, replicaPlan.consistencyLevel(), queryStartNanoTime);
        Message<Commit> message = Message.out(PAXOS_PROPOSE_REQ, proposal);
        for (Replica replica : replicaPlan.contacts()) {
            if (replica.isSelf()) {
                PAXOS_PROPOSE_REQ.stage.execute(() -> {
                    try {
                        Message<Boolean> response = message.responseWith(doPropose(proposal));
                        callback.onResponse(response);
                    } catch (Exception ex) {
                        logger.error("Failed paxos propose locally", ex);
                    }
                });
            } else {
                MessagingService.instance().sendWithCallback(message, replica.endpoint(), callback);
            }
        }
        callback.await();
        if (callback.isSuccessful())
            return true;
        if (backoffIfPartial && !callback.isFullyRefused())
            throw new CasWriteUnknownResultException(replicaPlan.consistencyLevel(), callback.getAcceptCount(), replicaPlan.requiredParticipants());
        return false;
    }

<<<<<<< MINE

=======
    private static PrepareCallback preparePaxos(Commit toPrepare, List<InetAddress> endpoints, int requiredParticipants, ConsistencyLevel consistencyForPaxos, long queryStartNanoTime) throws WriteTimeoutException {
        PrepareCallback callback = new PrepareCallback(toPrepare.update.partitionKey(), toPrepare.update.metadata(), requiredParticipants, consistencyForPaxos, queryStartNanoTime);
        MessageOut<Commit> message = new MessageOut<Commit>(MessagingService.Verb.PAXOS_PREPARE, toPrepare, Commit.serializer);
        for (InetAddress target : endpoints) MessagingService.instance().sendRR(message, target, callback);
        callback.await();
        return callback;
    }
>>>>>>> YOURS

<<<<<<< MINE

=======
    private static boolean proposePaxos(Commit proposal, List<InetAddress> endpoints, int requiredParticipants, boolean timeoutIfPartial, ConsistencyLevel consistencyLevel, long queryStartNanoTime) throws WriteTimeoutException {
        ProposeCallback callback = new ProposeCallback(endpoints.size(), requiredParticipants, !timeoutIfPartial, consistencyLevel, queryStartNanoTime);
        MessageOut<Commit> message = new MessageOut<Commit>(MessagingService.Verb.PAXOS_PROPOSE, proposal, Commit.serializer);
        for (InetAddress target : endpoints) MessagingService.instance().sendRR(message, target, callback);
        callback.await();
        if (callback.isSuccessful())
            return true;
        if (timeoutIfPartial && !callback.isFullyRefused())
            throw new WriteTimeoutException(WriteType.CAS, consistencyLevel, callback.getAcceptCount(), requiredParticipants);
        return false;
    }
>>>>>>> YOURS

<<<<<<< MINE

=======
    private static void commitPaxos(Commit proposal, ConsistencyLevel consistencyLevel, boolean allowHints, long queryStartNanoTime) throws WriteTimeoutException {
        boolean shouldBlock = consistencyLevel != ConsistencyLevel.ANY;
        Keyspace keyspace = Keyspace.open(proposal.update.metadata().ksName);
        Token tk = proposal.update.partitionKey().getToken();
        List<InetAddress> naturalEndpoints = StorageService.instance.getNaturalEndpoints(keyspace.getName(), tk);
        Collection<InetAddress> pendingEndpoints = StorageService.instance.getTokenMetadata().pendingEndpointsFor(tk, keyspace.getName());
        AbstractWriteResponseHandler<Commit> responseHandler = null;
        if (shouldBlock) {
            AbstractReplicationStrategy rs = keyspace.getReplicationStrategy();
            responseHandler = rs.getWriteResponseHandler(naturalEndpoints, pendingEndpoints, consistencyLevel, null, WriteType.SIMPLE, queryStartNanoTime);
            responseHandler.setSupportsBackPressure(false);
        }
        MessageOut<Commit> message = new MessageOut<Commit>(MessagingService.Verb.PAXOS_COMMIT, proposal, Commit.serializer);
        for (InetAddress destination : Iterables.concat(naturalEndpoints, pendingEndpoints)) {
            checkHintOverload(destination);
            if (FailureDetector.instance.isAlive(destination)) {
                if (shouldBlock) {
                    if (canDoLocalRequest(destination))
                        commitPaxosLocal(message, responseHandler);
                    else
                        MessagingService.instance().sendRR(message, destination, responseHandler, allowHints && shouldHint(destination));
                } else {
                    MessagingService.instance().sendOneWay(message, destination);
                }
            } else if (allowHints && shouldHint(destination)) {
                submitHint(proposal.makeMutation(), destination, null);
            }
        }
        if (shouldBlock)
            responseHandler.get();
    }
>>>>>>> YOURS

    private static void commitPaxosLocal(Replica localReplica, final Message<Commit> message, final AbstractWriteResponseHandler<?> responseHandler) {
        PAXOS_COMMIT_REQ.stage.maybeExecuteImmediately(new LocalMutationRunnable(localReplica) {

            public void runMayThrow() {
                try {
                    PaxosState.commit(message.payload);
                    if (responseHandler != null)
                        responseHandler.onResponse(null);
                } catch (Exception ex) {
                    if (!(ex instanceof WriteTimeoutException))
                        logger.error("Failed to apply paxos commit locally : ", ex);
                    responseHandler.onFailure(FBUtilities.getBroadcastAddressAndPort(), RequestFailureReason.forException(ex));
                }
            }

            @Override
            protected Verb verb() {
                return PAXOS_COMMIT_REQ;
            }
        });
    }

    public static void mutate(List<? extends IMutation> mutations, ConsistencyLevel consistencyLevel, long queryStartNanoTime) throws UnavailableException, OverloadedException, WriteTimeoutException, WriteFailureException {
        Tracing.trace("Determining replicas for mutation");
        final String localDataCenter = DatabaseDescriptor.getEndpointSnitch().getLocalDatacenter();
        long startTime = System.nanoTime();
        List<AbstractWriteResponseHandler<IMutation>> responseHandlers = new ArrayList<>(mutations.size());
        WriteType plainWriteType = mutations.size() <= 1 ? WriteType.SIMPLE : WriteType.UNLOGGED_BATCH;
        try {
            for (IMutation mutation : mutations) {
                if (mutation instanceof CounterMutation)
                    responseHandlers.add(mutateCounter((CounterMutation) mutation, localDataCenter, queryStartNanoTime));
                else
                    responseHandlers.add(performWrite(mutation, consistencyLevel, localDataCenter, standardWritePerformer, null, plainWriteType, queryStartNanoTime));
            }
            for (int i = 0; i < mutations.size(); ++i) {
                if (!(mutations.get(i) instanceof CounterMutation))
                    responseHandlers.get(i).maybeTryAdditionalReplicas(mutations.get(i), standardWritePerformer, localDataCenter);
            }
            for (AbstractWriteResponseHandler<IMutation> responseHandler : responseHandlers) responseHandler.get();
        } catch (WriteTimeoutException | WriteFailureException ex) {
            if (consistencyLevel == ConsistencyLevel.ANY) {
                hintMutations(mutations);
            } else {
                if (ex instanceof WriteFailureException) {
                    writeMetrics.failures.mark();
                    writeMetricsMap.get(consistencyLevel).failures.mark();
                    WriteFailureException fe = (WriteFailureException) ex;
                    Tracing.trace("Write failure; received {} of {} required replies, failed {} requests", fe.received, fe.blockFor, fe.failureReasonByEndpoint.size());
                } else {
                    writeMetrics.timeouts.mark();
                    writeMetricsMap.get(consistencyLevel).timeouts.mark();
                    WriteTimeoutException te = (WriteTimeoutException) ex;
                    Tracing.trace("Write timeout; received {} of {} required replies", te.received, te.blockFor);
                }
                throw ex;
            }
        } catch (UnavailableException e) {
            writeMetrics.unavailables.mark();
            writeMetricsMap.get(consistencyLevel).unavailables.mark();
            Tracing.trace("Unavailable");
            throw e;
        } catch (OverloadedException e) {
            writeMetrics.unavailables.mark();
            writeMetricsMap.get(consistencyLevel).unavailables.mark();
            Tracing.trace("Overloaded");
            throw e;
        } finally {
            long latency = System.nanoTime() - startTime;
            writeMetrics.addNano(latency);
            writeMetricsMap.get(consistencyLevel).addNano(latency);
            updateCoordinatorWriteLatencyTableMetric(mutations, latency);
        }
    }

    private static void hintMutations(Collection<? extends IMutation> mutations) {
        for (IMutation mutation : mutations) if (!(mutation instanceof CounterMutation))
            hintMutation((Mutation) mutation);
        Tracing.trace("Wrote hints to satisfy CL.ANY after no replicas acknowledged the write");
    }

    private static void hintMutation(Mutation mutation) {
        String keyspaceName = mutation.getKeyspaceName();
        Token token = mutation.key().getToken();
        EndpointsForToken replicasToHint = ReplicaLayout.forTokenWriteLiveAndDown(Keyspace.open(keyspaceName), token).all().filter(StorageProxy::shouldHint);
        submitHint(mutation, replicasToHint, null);
    }

    public boolean appliesLocally(Mutation mutation) {
        String keyspaceName = mutation.getKeyspaceName();
        Token token = mutation.key().getToken();
        InetAddressAndPort local = FBUtilities.getBroadcastAddressAndPort();
        return ReplicaLayout.forTokenWriteLiveAndDown(Keyspace.open(keyspaceName), token).all().endpoints().contains(local);
    }

    public static void mutateMV(ByteBuffer dataKey, Collection<Mutation> mutations, boolean writeCommitLog, AtomicLong baseComplete, long queryStartNanoTime) throws UnavailableException, OverloadedException, WriteTimeoutException {
        Tracing.trace("Determining replicas for mutation");
        final String localDataCenter = DatabaseDescriptor.getEndpointSnitch().getLocalDatacenter();
        long startTime = System.nanoTime();
        try {
            final UUID batchUUID = UUIDGen.getTimeUUID();
            if (StorageService.instance.isStarting() || StorageService.instance.isJoining() || StorageService.instance.isMoving()) {
                BatchlogManager.store(Batch.createLocal(batchUUID, FBUtilities.timestampMicros(), mutations), writeCommitLog);
            } else {
                List<WriteResponseHandlerWrapper> wrappers = new ArrayList<>(mutations.size());
                Set<Mutation> nonLocalMutations = new HashSet<>(mutations);
                Token baseToken = StorageService.instance.getTokenMetadata().partitioner.getToken(dataKey);
                ConsistencyLevel consistencyLevel = ConsistencyLevel.ONE;
                ReplicaPlan.ForTokenWrite replicaPlan = ReplicaPlans.forLocalBatchlogWrite();
                BatchlogCleanup cleanup = new BatchlogCleanup(mutations.size(), () -> asyncRemoveFromBatchlog(replicaPlan, batchUUID));
                for (Mutation mutation : mutations) {
                    String keyspaceName = mutation.getKeyspaceName();
                    Token tk = mutation.key().getToken();
                    Optional<Replica> pairedEndpoint = ViewUtils.getViewNaturalEndpoint(keyspaceName, baseToken, tk);
                    EndpointsForToken pendingReplicas = StorageService.instance.getTokenMetadata().pendingEndpointsForToken(tk, keyspaceName);
                    if (!pairedEndpoint.isPresent()) {
                        if (pendingReplicas.isEmpty())
                            logger.warn("Received base materialized view mutation for key {} that does not belong " + "to this node. There is probably a range movement happening (move or decommission)," + "but this node hasn't updated its ring metadata yet. Adding mutation to " + "local batchlog to be replayed later.", mutation.key());
                        continue;
                    }
<<<<<<< MINE
                    if (pairedEndpoint.get().isSelf() && StorageService.instance.isJoined() && pendingReplicas.isEmpty()) {
=======
                    if (pairedEndpoint.get().equals(FBUtilities.getBroadcastAddress()) && StorageService.instance.isJoined() && pendingEndpoints.isEmpty()) {
>>>>>>> YOURS
                        try {
                            mutation.apply(writeCommitLog);
                            nonLocalMutations.remove(mutation);
                            cleanup.ackMutation();
                        } catch (Exception exc) {
                            logger.error("Error applying local view update to keyspace {}: {}", mutation.getKeyspaceName(), mutation);
                            throw exc;
                        }
                    } else {
<<<<<<< MINE
                        wrappers.add(wrapViewBatchResponseHandler(mutation, consistencyLevel, consistencyLevel, EndpointsForToken.of(tk, pairedEndpoint.get()), pendingReplicas, baseComplete, WriteType.BATCH, cleanup, queryStartNanoTime));
=======
                        wrappers.add(wrapViewBatchResponseHandler(mutation, consistencyLevel, consistencyLevel, Collections.singletonList(pairedEndpoint.get()), baseComplete, WriteType.BATCH, cleanup, queryStartNanoTime));
>>>>>>> YOURS
                    }
                }
                if (!nonLocalMutations.isEmpty())
                    BatchlogManager.store(Batch.createLocal(batchUUID, FBUtilities.timestampMicros(), nonLocalMutations), writeCommitLog);
                if (!wrappers.isEmpty())
                    asyncWriteBatchedMutations(wrappers, localDataCenter, Stage.VIEW_MUTATION);
            }
        } finally {
            viewWriteMetrics.addNano(System.nanoTime() - startTime);
        }
    }

    @SuppressWarnings("unchecked")
    public static void mutateWithTriggers(List<? extends IMutation> mutations, ConsistencyLevel consistencyLevel, boolean mutateAtomically, long queryStartNanoTime) throws WriteTimeoutException, WriteFailureException, UnavailableException, OverloadedException, InvalidRequestException {
        Collection<Mutation> augmented = TriggerExecutor.instance.execute(mutations);
        boolean updatesView = Keyspace.open(mutations.iterator().next().getKeyspaceName()).viewManager.updatesAffectView(mutations, true);
        long size = IMutation.dataSize(mutations);
        writeMetrics.mutationSize.update(size);
        writeMetricsMap.get(consistencyLevel).mutationSize.update(size);
        if (augmented != null)
            mutateAtomically(augmented, consistencyLevel, updatesView, queryStartNanoTime);
        else {
            if (mutateAtomically || updatesView)
                mutateAtomically((Collection<Mutation>) mutations, consistencyLevel, updatesView, queryStartNanoTime);
            else
                mutate(mutations, consistencyLevel, queryStartNanoTime);
        }
    }

<<<<<<< MINE

=======
    @SuppressWarnings("unchecked")
    public static void mutateWithTriggers(Collection<? extends IMutation> mutations, ConsistencyLevel consistencyLevel, boolean mutateAtomically, long queryStartNanoTime) throws WriteTimeoutException, WriteFailureException, UnavailableException, OverloadedException, InvalidRequestException {
        Collection<Mutation> augmented = TriggerExecutor.instance.execute(mutations);
        boolean updatesView = Keyspace.open(mutations.iterator().next().getKeyspaceName()).viewManager.updatesAffectView(mutations, true);
        if (augmented != null)
            mutateAtomically(augmented, consistencyLevel, updatesView, queryStartNanoTime);
        else {
            if (mutateAtomically || updatesView)
                mutateAtomically((Collection<Mutation>) mutations, consistencyLevel, updatesView, queryStartNanoTime);
            else
                mutate(mutations, consistencyLevel, queryStartNanoTime);
        }
    }
>>>>>>> YOURS

    private static void updateCoordinatorWriteLatencyTableMetric(Collection<? extends IMutation> mutations, long latency) {
        if (null == mutations) {
            return;
        }
        try {
            mutations.stream().flatMap(m -> m.getTableIds().stream().map(tableId -> Keyspace.open(m.getKeyspaceName()).getColumnFamilyStore(tableId))).distinct().forEach(store -> store.metric.coordinatorWriteLatency.update(latency, TimeUnit.NANOSECONDS));
        } catch (Exception ex) {
            logger.warn("Exception occurred updating coordinatorWriteLatency metric", ex);
        }
    }

    private static void syncWriteToBatchlog(Collection<Mutation> mutations, ReplicaPlan.ForTokenWrite replicaPlan, UUID uuid, long queryStartNanoTime) throws WriteTimeoutException, WriteFailureException {
        WriteResponseHandler<?> handler = new WriteResponseHandler(replicaPlan, WriteType.BATCH_LOG, queryStartNanoTime);
        Batch batch = Batch.createLocal(uuid, FBUtilities.timestampMicros(), mutations);
        Message<Batch> message = Message.out(BATCH_STORE_REQ, batch);
        for (Replica replica : replicaPlan.liveAndDown()) {
            logger.trace("Sending batchlog store request {} to {} for {} mutations", batch.id, replica, batch.size());
            if (replica.isSelf())
                performLocally(Stage.MUTATION, replica, () -> BatchlogManager.store(batch), handler);
            else
                MessagingService.instance().sendWithCallback(message, replica.endpoint(), handler);
        }
        handler.get();
    }

    private static void asyncRemoveFromBatchlog(ReplicaPlan.ForTokenWrite replicaPlan, UUID uuid) {
        Message<UUID> message = Message.out(Verb.BATCH_REMOVE_REQ, uuid);
        for (Replica target : replicaPlan.contacts()) {
            if (logger.isTraceEnabled())
                logger.trace("Sending batchlog remove request {} to {}", uuid, target);
            if (target.isSelf())
                performLocally(Stage.MUTATION, target, () -> BatchlogManager.remove(uuid));
            else
                MessagingService.instance().send(message, target.endpoint());
        }
    }

<<<<<<< MINE

=======
    private static void syncWriteToBatchlog(Collection<Mutation> mutations, BatchlogEndpoints endpoints, UUID uuid, long queryStartNanoTime) throws WriteTimeoutException, WriteFailureException {
        WriteResponseHandler<?> handler = new WriteResponseHandler<>(endpoints.all, Collections.<InetAddress>emptyList(), endpoints.all.size() == 1 ? ConsistencyLevel.ONE : ConsistencyLevel.TWO, Keyspace.open(SchemaConstants.SYSTEM_KEYSPACE_NAME), null, WriteType.BATCH_LOG, queryStartNanoTime);
        Batch batch = Batch.createLocal(uuid, FBUtilities.timestampMicros(), mutations);
        if (!endpoints.current.isEmpty())
            syncWriteToBatchlog(handler, batch, endpoints.current);
        if (!endpoints.legacy.isEmpty())
            LegacyBatchlogMigrator.syncWriteToBatchlog(handler, batch, endpoints.legacy);
        handler.get();
    }
>>>>>>> YOURS

<<<<<<< MINE

=======
    private static void asyncRemoveFromBatchlog(BatchlogEndpoints endpoints, UUID uuid, long queryStartNanoTime) {
        if (!endpoints.current.isEmpty())
            asyncRemoveFromBatchlog(endpoints.current, uuid);
        if (!endpoints.legacy.isEmpty())
            LegacyBatchlogMigrator.asyncRemoveFromBatchlog(endpoints.legacy, uuid, queryStartNanoTime);
    }
>>>>>>> YOURS

    private static void asyncWriteBatchedMutations(List<WriteResponseHandlerWrapper> wrappers, String localDataCenter, Stage stage) {
        for (WriteResponseHandlerWrapper wrapper : wrappers) {
            Replicas.temporaryAssertFull(wrapper.handler.replicaPlan.liveAndDown());
            ReplicaPlan.ForTokenWrite replicas = wrapper.handler.replicaPlan.withContact(wrapper.handler.replicaPlan.liveAndDown());
            try {
                sendToHintedReplicas(wrapper.mutation, replicas, wrapper.handler, localDataCenter, stage);
            } catch (OverloadedException | WriteTimeoutException e) {
<<<<<<< MINE
                wrapper.handler.onFailure(FBUtilities.getBroadcastAddressAndPort(), RequestFailureReason.forException(e));
=======
                wrapper.handler.onFailure(FBUtilities.getBroadcastAddress(), RequestFailureReason.UNKNOWN);
>>>>>>> YOURS
            }
        }
    }

    private static void syncWriteBatchedMutations(List<WriteResponseHandlerWrapper> wrappers, Stage stage) throws WriteTimeoutException, OverloadedException {
        String localDataCenter = DatabaseDescriptor.getEndpointSnitch().getLocalDatacenter();
        for (WriteResponseHandlerWrapper wrapper : wrappers) {
            EndpointsForToken sendTo = wrapper.handler.replicaPlan.liveAndDown();
            Replicas.temporaryAssertFull(sendTo);
            sendToHintedReplicas(wrapper.mutation, wrapper.handler.replicaPlan.withContact(sendTo), wrapper.handler, localDataCenter, stage);
        }
        for (WriteResponseHandlerWrapper wrapper : wrappers) wrapper.handler.get();
    }

<<<<<<< MINE

=======
    public static AbstractWriteResponseHandler<IMutation> performWrite(IMutation mutation, ConsistencyLevel consistency_level, String localDataCenter, WritePerformer performer, Runnable callback, WriteType writeType, long queryStartNanoTime) throws UnavailableException, OverloadedException {
        String keyspaceName = mutation.getKeyspaceName();
        AbstractReplicationStrategy rs = Keyspace.open(keyspaceName).getReplicationStrategy();
        Token tk = mutation.key().getToken();
        List<InetAddress> naturalEndpoints = StorageService.instance.getNaturalEndpoints(keyspaceName, tk);
        Collection<InetAddress> pendingEndpoints = StorageService.instance.getTokenMetadata().pendingEndpointsFor(tk, keyspaceName);
        AbstractWriteResponseHandler<IMutation> responseHandler = rs.getWriteResponseHandler(naturalEndpoints, pendingEndpoints, consistency_level, callback, writeType, queryStartNanoTime);
        responseHandler.assureSufficientLiveNodes();
        performer.apply(mutation, Iterables.concat(naturalEndpoints, pendingEndpoints), responseHandler, localDataCenter, consistency_level);
        return responseHandler;
    }
>>>>>>> YOURS

<<<<<<< MINE

=======
    private static WriteResponseHandlerWrapper wrapBatchResponseHandler(Mutation mutation, ConsistencyLevel consistency_level, ConsistencyLevel batchConsistencyLevel, WriteType writeType, BatchlogResponseHandler.BatchlogCleanup cleanup, long queryStartNanoTime) {
        Keyspace keyspace = Keyspace.open(mutation.getKeyspaceName());
        AbstractReplicationStrategy rs = keyspace.getReplicationStrategy();
        String keyspaceName = mutation.getKeyspaceName();
        Token tk = mutation.key().getToken();
        List<InetAddress> naturalEndpoints = StorageService.instance.getNaturalEndpoints(keyspaceName, tk);
        Collection<InetAddress> pendingEndpoints = StorageService.instance.getTokenMetadata().pendingEndpointsFor(tk, keyspaceName);
        AbstractWriteResponseHandler<IMutation> writeHandler = rs.getWriteResponseHandler(naturalEndpoints, pendingEndpoints, consistency_level, null, writeType, queryStartNanoTime);
        BatchlogResponseHandler<IMutation> batchHandler = new BatchlogResponseHandler<>(writeHandler, batchConsistencyLevel.blockFor(keyspace), cleanup, queryStartNanoTime);
        return new WriteResponseHandlerWrapper(batchHandler, mutation);
    }
>>>>>>> YOURS

    private static WriteResponseHandlerWrapper wrapViewBatchResponseHandler(Mutation mutation, ConsistencyLevel consistencyLevel, ConsistencyLevel batchConsistencyLevel, EndpointsForToken naturalEndpoints, EndpointsForToken pendingEndpoints, AtomicLong baseComplete, WriteType writeType, BatchlogResponseHandler.BatchlogCleanup cleanup, long queryStartNanoTime) {
        Keyspace keyspace = Keyspace.open(mutation.getKeyspaceName());
        AbstractReplicationStrategy rs = keyspace.getReplicationStrategy();
        ReplicaLayout.ForTokenWrite liveAndDown = ReplicaLayout.forTokenWrite(naturalEndpoints, pendingEndpoints);
        ReplicaPlan.ForTokenWrite replicaPlan = ReplicaPlans.forWrite(keyspace, consistencyLevel, liveAndDown, ReplicaPlans.writeAll);
        AbstractWriteResponseHandler<IMutation> writeHandler = rs.getWriteResponseHandler(replicaPlan, () -> {
            long delay = Math.max(0, System.currentTimeMillis() - baseComplete.get());
            viewWriteMetrics.viewWriteLatency.update(delay, MILLISECONDS);
        }, writeType, queryStartNanoTime);
        BatchlogResponseHandler<IMutation> batchHandler = new ViewWriteMetricsWrapped(writeHandler, batchConsistencyLevel.blockFor(keyspace), cleanup, queryStartNanoTime);
        return new WriteResponseHandlerWrapper(batchHandler, mutation);
    }

<<<<<<< MINE

=======
    private static WriteResponseHandlerWrapper wrapViewBatchResponseHandler(Mutation mutation, ConsistencyLevel consistency_level, ConsistencyLevel batchConsistencyLevel, List<InetAddress> naturalEndpoints, AtomicLong baseComplete, WriteType writeType, BatchlogResponseHandler.BatchlogCleanup cleanup, long queryStartNanoTime) {
        Keyspace keyspace = Keyspace.open(mutation.getKeyspaceName());
        AbstractReplicationStrategy rs = keyspace.getReplicationStrategy();
        String keyspaceName = mutation.getKeyspaceName();
        Token tk = mutation.key().getToken();
        Collection<InetAddress> pendingEndpoints = StorageService.instance.getTokenMetadata().pendingEndpointsFor(tk, keyspaceName);
        AbstractWriteResponseHandler<IMutation> writeHandler = rs.getWriteResponseHandler(naturalEndpoints, pendingEndpoints, consistency_level, () -> {
            long delay = Math.max(0, System.currentTimeMillis() - baseComplete.get());
            viewWriteMetrics.viewWriteLatency.update(delay, TimeUnit.MILLISECONDS);
        }, writeType, queryStartNanoTime);
        BatchlogResponseHandler<IMutation> batchHandler = new ViewWriteMetricsWrapped(writeHandler, batchConsistencyLevel.blockFor(keyspace), cleanup, queryStartNanoTime);
        return new WriteResponseHandlerWrapper(batchHandler, mutation);
    }
>>>>>>> YOURS

    private static class WriteResponseHandlerWrapper {

        final BatchlogResponseHandler<IMutation> handler;

        final Mutation mutation;

        WriteResponseHandlerWrapper(BatchlogResponseHandler<IMutation> handler, Mutation mutation) {
            this.handler = handler;
            this.mutation = mutation;
        }
    }

    public static void sendToHintedReplicas(final Mutation mutation, ReplicaPlan.ForTokenWrite plan, AbstractWriteResponseHandler<IMutation> responseHandler, String localDataCenter, Stage stage) throws OverloadedException {
        Collection<Replica> localDc = null;
        Map<String, Collection<Replica>> dcGroups = null;
        Message<Mutation> message = null;
        boolean insertLocal = false;
        Replica localReplica = null;
        Collection<Replica> endpointsToHint = null;
        List<InetAddressAndPort> backPressureHosts = null;
        for (Replica destination : plan.contacts()) {
            checkHintOverload(destination);
            if (plan.isAlive(destination)) {
                if (destination.isSelf()) {
                    insertLocal = true;
                    localReplica = destination;
                } else {
                    if (message == null)
                        message = Message.outWithFlag(MUTATION_REQ, mutation, MessageFlag.CALL_BACK_ON_FAILURE);
                    String dc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(destination);
                    if (localDataCenter.equals(dc)) {
                        if (localDc == null)
                            localDc = new ArrayList<>(plan.contacts().size());
                        localDc.add(destination);
                    } else {
                        if (dcGroups == null)
                            dcGroups = new HashMap<>();
                        Collection<Replica> messages = dcGroups.get(dc);
                        if (messages == null)
                            messages = dcGroups.computeIfAbsent(dc, (v) -> new ArrayList<>(3));
                        messages.add(destination);
                    }
                    if (backPressureHosts == null)
                        backPressureHosts = new ArrayList<>(plan.contacts().size());
                    backPressureHosts.add(destination.endpoint());
                }
            } else {
                responseHandler.expired();
                if (shouldHint(destination)) {
                    if (endpointsToHint == null)
                        endpointsToHint = new ArrayList<>();
                    endpointsToHint.add(destination);
                }
            }
        }
        if (endpointsToHint != null)
            submitHint(mutation, EndpointsForToken.copyOf(mutation.key().getToken(), endpointsToHint), responseHandler);
        if (insertLocal) {
            Preconditions.checkNotNull(localReplica);
            performLocally(stage, localReplica, mutation::apply, responseHandler);
        }
        if (localDc != null) {
            for (Replica destination : localDc) MessagingService.instance().sendWriteWithCallback(message, destination, responseHandler, true);
        }
        if (dcGroups != null) {
            for (Collection<Replica> dcTargets : dcGroups.values()) sendMessagesToNonlocalDC(message, EndpointsForToken.copyOf(mutation.key().getToken(), dcTargets), responseHandler);
        }
    }

    private static void checkHintOverload(Replica destination) {
        if (StorageMetrics.totalHintsInProgress.getCount() > maxHintsInProgress && (getHintsInProgressFor(destination.endpoint()).get() > 0 && shouldHint(destination))) {
            throw new OverloadedException("Too many in flight hints: " + StorageMetrics.totalHintsInProgress.getCount() + " destination: " + destination + " destination hints: " + getHintsInProgressFor(destination.endpoint()).get());
        }
    }

    private static void sendMessagesToNonlocalDC(Message<? extends IMutation> message, EndpointsForToken targets, AbstractWriteResponseHandler<IMutation> handler) {
        final Replica target;
        if (targets.size() > 1) {
            target = targets.get(ThreadLocalRandom.current().nextInt(0, targets.size()));
            EndpointsForToken forwardToReplicas = targets.filter(r -> r != target, targets.size());
            for (Replica replica : forwardToReplicas) {
                MessagingService.instance().callbacks.addWithExpiration(handler, message, replica, handler.replicaPlan.consistencyLevel(), true);
                logger.trace("Adding FWD message to {}@{}", message.id(), replica);
            }
            long[] messageIds = new long[forwardToReplicas.size()];
            Arrays.fill(messageIds, message.id());
            message = message.withForwardTo(new ForwardingInfo(forwardToReplicas.endpointList(), messageIds));
        } else {
            target = targets.get(0);
        }
        MessagingService.instance().sendWriteWithCallback(message, target, handler, true);
        logger.trace("Sending message to {}@{}", message.id(), target);
    }

    private static void performLocally(Stage stage, Replica localReplica, final Runnable runnable) {
        stage.maybeExecuteImmediately(new LocalMutationRunnable(localReplica) {

            public void runMayThrow() {
                try {
                    runnable.run();
                } catch (Exception ex) {
                    logger.error("Failed to apply mutation locally : ", ex);
                }
            }

            @Override
            protected Verb verb() {
                return Verb.MUTATION_REQ;
            }
        });
    }

<<<<<<< MINE
    private static void performLocally(Stage stage, Replica localReplica, final Runnable runnable, final RequestCallback<?> handler) {
        stage.maybeExecuteImmediately(new LocalMutationRunnable(localReplica) {

            public void runMayThrow() {
                try {
                    runnable.run();
                    handler.onResponse(null);
                } catch (Exception ex) {
                    if (!(ex instanceof WriteTimeoutException))
                        logger.error("Failed to apply mutation locally : ", ex);
                    handler.onFailure(FBUtilities.getBroadcastAddressAndPort(), RequestFailureReason.forException(ex));
                }
            }

            @Override
            protected Verb verb() {
                return Verb.MUTATION_REQ;
            }
        });
    }
=======
    private static void performLocally(Stage stage, Optional<IMutation> mutation, final Runnable runnable, final IAsyncCallbackWithFailure<?> handler) {
        StageManager.getStage(stage).maybeExecuteImmediately(new LocalMutationRunnable(mutation) {

            public void runMayThrow() {
                try {
                    runnable.run();
                    handler.response(null);
                } catch (Exception ex) {
                    if (!(ex instanceof WriteTimeoutException))
                        logger.error("Failed to apply mutation locally : {}", ex);
                    handler.onFailure(FBUtilities.getBroadcastAddress(), RequestFailureReason.UNKNOWN);
                }
            }

            @Override
            protected Verb verb() {
                return MessagingService.Verb.MUTATION;
            }
        });
    }
>>>>>>> YOURS<<<<<<< MINE
=======
public static void sendToHintedEndpoints(final Mutation mutation, Iterable<InetAddress> targets, AbstractWriteResponseHandler<IMutation> responseHandler, String localDataCenter, Stage stage) throws OverloadedException {
        int targetsSize = Iterables.size(targets);
        Collection<InetAddress> localDc = null;
        Map<String, Collection<InetAddress>> dcGroups = null;
        MessageOut<Mutation> message = null;
        boolean insertLocal = false;
        ArrayList<InetAddress> endpointsToHint = null;
        List<InetAddress> backPressureHosts = null;
        for (InetAddress destination : targets) {
            checkHintOverload(destination);
            if (FailureDetector.instance.isAlive(destination)) {
                if (canDoLocalRequest(destination)) {
                    insertLocal = true;
                } else {
                    if (message == null)
                        message = mutation.createMessage();
                    String dc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(destination);
                    if (localDataCenter.equals(dc)) {
                        if (localDc == null)
                            localDc = new ArrayList<>(targetsSize);
                        localDc.add(destination);
                    } else {
                        Collection<InetAddress> messages = (dcGroups != null) ? dcGroups.get(dc) : null;
                        if (messages == null) {
                            messages = new ArrayList<>(3);
                            if (dcGroups == null)
                                dcGroups = new HashMap<>();
                            dcGroups.put(dc, messages);
                        }
                        messages.add(destination);
                    }
                    if (backPressureHosts == null)
                        backPressureHosts = new ArrayList<>(targetsSize);
                    backPressureHosts.add(destination);
                }
            } else {
                if (shouldHint(destination)) {
                    if (endpointsToHint == null)
                        endpointsToHint = new ArrayList<>(targetsSize);
                    endpointsToHint.add(destination);
                }
            }
        }
        if (backPressureHosts != null)
            MessagingService.instance().applyBackPressure(backPressureHosts, responseHandler.currentTimeout());
        if (endpointsToHint != null)
            submitHint(mutation, endpointsToHint, responseHandler);
        if (insertLocal)
            performLocally(stage, Optional.of(mutation), mutation::apply, responseHandler);
        if (localDc != null) {
            for (InetAddress destination : localDc) MessagingService.instance().sendRR(message, destination, responseHandler, true);
        }
        if (dcGroups != null) {
            for (Collection<InetAddress> dcTargets : dcGroups.values()) sendMessagesToNonlocalDC(message, dcTargets, responseHandler);
        }
    }
>>>>>>> YOURS


    private static Replica findSuitableReplica(String keyspaceName, DecoratedKey key, String localDataCenter, ConsistencyLevel cl) throws UnavailableException {
        Keyspace keyspace = Keyspace.open(keyspaceName);
        IEndpointSnitch snitch = DatabaseDescriptor.getEndpointSnitch();
        EndpointsForToken replicas = keyspace.getReplicationStrategy().getNaturalReplicasForToken(key);
        replicas = replicas.filter(replica -> StorageService.instance.isRpcReady(replica.endpoint()));
        if (replicas.isEmpty())
            throw UnavailableException.create(cl, cl.blockFor(keyspace), 0);
        List<Replica> localReplicas = new ArrayList<>(replicas.size());
        for (Replica replica : replicas) if (snitch.getDatacenter(replica).equals(localDataCenter))
            localReplicas.add(replica);
        if (localReplicas.isEmpty()) {
            if (cl.isDatacenterLocal())
                throw UnavailableException.create(cl, cl.blockFor(keyspace), 0);
            replicas = snitch.sortedByProximity(FBUtilities.getBroadcastAddressAndPort(), replicas);
            return replicas.get(0);
        }
        return localReplicas.get(ThreadLocalRandom.current().nextInt(localReplicas.size()));
    }

    public static AbstractWriteResponseHandler<IMutation> applyCounterMutationOnLeader(CounterMutation cm, String localDataCenter, Runnable callback, long queryStartNanoTime) throws UnavailableException, OverloadedException {
        return performWrite(cm, cm.consistency(), localDataCenter, counterWritePerformer, callback, WriteType.COUNTER, queryStartNanoTime);
    }

    public static AbstractWriteResponseHandler<IMutation> applyCounterMutationOnCoordinator(CounterMutation cm, String localDataCenter, long queryStartNanoTime) throws UnavailableException, OverloadedException {
        return performWrite(cm, cm.consistency(), localDataCenter, counterWriteOnCoordinatorPerformer, null, WriteType.COUNTER, queryStartNanoTime);
    }

    private static Runnable counterWriteTask(final IMutation mutation, final ReplicaPlan.ForTokenWrite replicaPlan, final AbstractWriteResponseHandler<IMutation> responseHandler, final String localDataCenter) {
        return new DroppableRunnable(Verb.COUNTER_MUTATION_REQ) {

            @Override
            public void runMayThrow() throws OverloadedException, WriteTimeoutException {
                assert mutation instanceof CounterMutation;
                Mutation result = ((CounterMutation) mutation).applyCounterMutation();
                responseHandler.onResponse(null);
                sendToHintedReplicas(result, replicaPlan, responseHandler, localDataCenter, Stage.COUNTER_MUTATION);
            }
        };
    }<<<<<<< MINE
=======
private static Runnable counterWriteTask(final IMutation mutation, final Iterable<InetAddress> targets, final AbstractWriteResponseHandler<IMutation> responseHandler, final String localDataCenter) {
        return new DroppableRunnable(MessagingService.Verb.COUNTER_MUTATION) {

            @Override
            public void runMayThrow() throws OverloadedException, WriteTimeoutException {
                assert mutation instanceof CounterMutation;
                Mutation result = ((CounterMutation) mutation).applyCounterMutation();
                responseHandler.response(null);
                Set<InetAddress> remotes = Sets.difference(ImmutableSet.copyOf(targets), ImmutableSet.of(FBUtilities.getBroadcastAddress()));
                if (!remotes.isEmpty())
                    sendToHintedEndpoints(result, remotes, responseHandler, localDataCenter, Stage.COUNTER_MUTATION);
            }
        };
    }
>>>>>>> YOURS


    private static boolean systemKeyspaceQuery(List<? extends ReadCommand> cmds) {
<<<<<<< MINE
        for (ReadCommand cmd : cmds) if (!SchemaConstants.isLocalSystemKeyspace(cmd.metadata().keyspace))
=======
        for (ReadCommand cmd : cmds) if (!SchemaConstants.isLocalSystemKeyspace(cmd.metadata().ksName))
>>>>>>> YOURS
            return false;
        return true;
    }

    public static RowIterator readOne(SinglePartitionReadCommand command, ConsistencyLevel consistencyLevel, long queryStartNanoTime) throws UnavailableException, IsBootstrappingException, ReadFailureException, ReadTimeoutException, InvalidRequestException {
        return readOne(command, consistencyLevel, null, queryStartNanoTime);
    }

    public static RowIterator readOne(SinglePartitionReadCommand command, ConsistencyLevel consistencyLevel, ClientState state, long queryStartNanoTime) throws UnavailableException, IsBootstrappingException, ReadFailureException, ReadTimeoutException, InvalidRequestException {
        return PartitionIterators.getOnlyElement(read(SinglePartitionReadCommand.Group.one(command), consistencyLevel, state, queryStartNanoTime), command);
    }

    public static PartitionIterator read(SinglePartitionReadCommand.Group group, ConsistencyLevel consistencyLevel, long queryStartNanoTime) throws UnavailableException, IsBootstrappingException, ReadFailureException, ReadTimeoutException, InvalidRequestException {
        assert !consistencyLevel.isSerialConsistency();
        return read(group, consistencyLevel, null, queryStartNanoTime);
    }

    public static PartitionIterator read(SinglePartitionReadCommand.Group group, ConsistencyLevel consistencyLevel, ClientState state, long queryStartNanoTime) throws UnavailableException, IsBootstrappingException, ReadFailureException, ReadTimeoutException, InvalidRequestException {
<<<<<<< MINE
        if (StorageService.instance.isBootstrapMode() && !systemKeyspaceQuery(group.queries)) {
=======
        if (StorageService.instance.isBootstrapMode() && !systemKeyspaceQuery(group.commands)) {
>>>>>>> YOURS
            readMetrics.unavailables.mark();
            readMetricsMap.get(consistencyLevel).unavailables.mark();
            throw new IsBootstrappingException();
        }
        return consistencyLevel.isSerialConsistency() ? readWithPaxos(group, consistencyLevel, state, queryStartNanoTime) : readRegular(group, consistencyLevel, queryStartNanoTime);
    }

    private static PartitionIterator readWithPaxos(SinglePartitionReadCommand.Group group, ConsistencyLevel consistencyLevel, ClientState state, long queryStartNanoTime) throws InvalidRequestException, UnavailableException, ReadFailureException, ReadTimeoutException {
        assert state != null;
        if (group.queries.size() > 1)
            throw new InvalidRequestException("SERIAL/LOCAL_SERIAL consistency may only be requested for one partition at a time");
        long start = System.nanoTime();
        SinglePartitionReadCommand command = group.queries.get(0);
        TableMetadata metadata = command.metadata();
        DecoratedKey key = command.partitionKey();
        PartitionIterator result = null;
        try {
            ReplicaPlan.ForPaxosWrite replicaPlan = ReplicaPlans.forPaxos(Keyspace.open(metadata.keyspace), key, consistencyLevel);
            final ConsistencyLevel consistencyForCommitOrFetch = consistencyLevel == ConsistencyLevel.LOCAL_SERIAL ? ConsistencyLevel.LOCAL_QUORUM : ConsistencyLevel.QUORUM;
            try {
                final PaxosBallotAndContention pair = beginAndRepairPaxos(start, key, metadata, replicaPlan, consistencyLevel, consistencyForCommitOrFetch, false, state);
                if (pair.contentions > 0)
                    casReadMetrics.contention.update(pair.contentions);
            } catch (WriteTimeoutException e) {
                throw new ReadTimeoutException(consistencyLevel, 0, consistencyLevel.blockFor(Keyspace.open(metadata.keyspace)), false);
            } catch (WriteFailureException e) {
                throw new ReadFailureException(consistencyLevel, e.received, e.blockFor, false, e.failureReasonByEndpoint);
            }
<<<<<<< MINE
            result = fetchRows(group.queries, consistencyForCommitOrFetch, queryStartNanoTime);
=======
            result = fetchRows(group.commands, consistencyForCommitOrFetch, queryStartNanoTime);
>>>>>>> YOURS
        } catch (UnavailableException e) {
            readMetrics.unavailables.mark();
            casReadMetrics.unavailables.mark();
            readMetricsMap.get(consistencyLevel).unavailables.mark();
            throw e;
        } catch (ReadTimeoutException e) {
            readMetrics.timeouts.mark();
            casReadMetrics.timeouts.mark();
            readMetricsMap.get(consistencyLevel).timeouts.mark();
            throw e;
        } catch (ReadFailureException e) {
            readMetrics.failures.mark();
            casReadMetrics.failures.mark();
            readMetricsMap.get(consistencyLevel).failures.mark();
            throw e;
        } finally {
            long latency = System.nanoTime() - start;
            readMetrics.addNano(latency);
            casReadMetrics.addNano(latency);
            readMetricsMap.get(consistencyLevel).addNano(latency);
<<<<<<< MINE
            Keyspace.open(metadata.keyspace).getColumnFamilyStore(metadata.name).metric.coordinatorReadLatency.update(latency, TimeUnit.NANOSECONDS);
=======
            Keyspace.open(metadata.ksName).getColumnFamilyStore(metadata.cfName).metric.coordinatorReadLatency.update(latency, TimeUnit.NANOSECONDS);
>>>>>>> YOURS
        }
        return result;
    }

    @SuppressWarnings("resource")
    private static PartitionIterator readRegular(SinglePartitionReadCommand.Group group, ConsistencyLevel consistencyLevel, long queryStartNanoTime) throws UnavailableException, ReadFailureException, ReadTimeoutException {
        long start = System.nanoTime();
        try {
<<<<<<< MINE
            PartitionIterator result = fetchRows(group.queries, consistencyLevel, queryStartNanoTime);
            boolean enforceStrictLiveness = group.queries.get(0).metadata().enforceStrictLiveness();
            if (group.queries.size() > 1)
=======
            PartitionIterator result = fetchRows(group.commands, consistencyLevel, queryStartNanoTime);
            boolean enforceStrictLiveness = group.commands.get(0).metadata().enforceStrictLiveness();
            if (group.commands.size() > 1)
>>>>>>> YOURS
                result = group.limits().filter(result, group.nowInSec(), group.selectsFullPartition(), enforceStrictLiveness);
            return result;
        } catch (UnavailableException e) {
            readMetrics.unavailables.mark();
            readMetricsMap.get(consistencyLevel).unavailables.mark();
            throw e;
        } catch (ReadTimeoutException e) {
            readMetrics.timeouts.mark();
            readMetricsMap.get(consistencyLevel).timeouts.mark();
            throw e;
        } catch (ReadFailureException e) {
            readMetrics.failures.mark();
            readMetricsMap.get(consistencyLevel).failures.mark();
            throw e;
        } finally {
            long latency = System.nanoTime() - start;
            readMetrics.addNano(latency);
            readMetricsMap.get(consistencyLevel).addNano(latency);
<<<<<<< MINE
            for (ReadCommand command : group.queries) Keyspace.openAndGetStore(command.metadata()).metric.coordinatorReadLatency.update(latency, TimeUnit.NANOSECONDS);
=======
            for (ReadCommand command : group.commands) Keyspace.openAndGetStore(command.metadata()).metric.coordinatorReadLatency.update(latency, TimeUnit.NANOSECONDS);
>>>>>>> YOURS
        }
    }

    private static PartitionIterator concatAndBlockOnRepair(List<PartitionIterator> iterators, List<ReadRepair> repairs) {
        PartitionIterator concatenated = PartitionIterators.concat(iterators);
        if (repairs.isEmpty())
            return concatenated;
        return new PartitionIterator() {

            public void close() {
                concatenated.close();
                repairs.forEach(ReadRepair::maybeSendAdditionalWrites);
                repairs.forEach(ReadRepair::awaitWrites);
            }

            public boolean hasNext() {
                return concatenated.hasNext();
            }

            public RowIterator next() {
                return concatenated.next();
            }
        };
    }

<<<<<<< MINE

=======
    private static PartitionIterator fetchRows(List<SinglePartitionReadCommand> commands, ConsistencyLevel consistencyLevel, long queryStartNanoTime) throws UnavailableException, ReadFailureException, ReadTimeoutException {
        int cmdCount = commands.size();
        SinglePartitionReadLifecycle[] reads = new SinglePartitionReadLifecycle[cmdCount];
        for (int i = 0; i < cmdCount; i++) reads[i] = new SinglePartitionReadLifecycle(commands.get(i), consistencyLevel, queryStartNanoTime);
        for (int i = 0; i < cmdCount; i++) reads[i].doInitialQueries();
        for (int i = 0; i < cmdCount; i++) reads[i].maybeTryAdditionalReplicas();
        for (int i = 0; i < cmdCount; i++) reads[i].awaitResultsAndRetryOnDigestMismatch();
        for (int i = 0; i < cmdCount; i++) if (!reads[i].isDone())
            reads[i].maybeAwaitFullDataRead();
        List<PartitionIterator> results = new ArrayList<>(cmdCount);
        for (int i = 0; i < cmdCount; i++) {
            assert reads[i].isDone();
            results.add(reads[i].getResult());
        }
        return PartitionIterators.concat(results);
    }
>>>>>>> YOURS

    public static class LocalReadRunnable extends DroppableRunnable {

        private final ReadCommand command;

        private final ReadCallback handler;

        public LocalReadRunnable(ReadCommand command, ReadCallback handler) {
            super(Verb.READ_REQ);
            this.command = command;
            this.handler = handler;
        }

        protected void runMayThrow() {
            try {
<<<<<<< MINE
                command.setMonitoringTime(approxCreationTimeNanos, false, verb.expiresAfterNanos(), DatabaseDescriptor.getSlowQueryTimeout(NANOSECONDS));
                ReadResponse response;
                try (ReadExecutionController executionController = command.executionController();
                    UnfilteredPartitionIterator iterator = command.executeLocally(executionController)) {
                    response = command.createResponse(iterator);
=======
                command.setMonitoringTime(constructionTime, false, verb.getTimeout(), DatabaseDescriptor.getSlowQueryTimeout());
                ReadResponse response;
                try (ReadExecutionController executionController = command.executionController();
                    UnfilteredPartitionIterator iterator = command.executeLocally(executionController)) {
                    response = command.createResponse(iterator);
                }
                if (command.complete()) {
                    handler.response(response);
                } else {
                    MessagingService.instance().incrementDroppedMessages(verb, System.currentTimeMillis() - constructionTime);
                    handler.onFailure(FBUtilities.getBroadcastAddress(), RequestFailureReason.UNKNOWN);
>>>>>>> YOURS
                }
                if (command.complete()) {
                    handler.response(response);
                } else {
                    MessagingService.instance().metrics.recordSelfDroppedMessage(verb, MonotonicClock.approxTime.now() - approxCreationTimeNanos, NANOSECONDS);
                    handler.onFailure(FBUtilities.getBroadcastAddressAndPort(), RequestFailureReason.UNKNOWN);
                }
                MessagingService.instance().latencySubscribers.add(FBUtilities.getBroadcastAddressAndPort(), MonotonicClock.approxTime.now() - approxCreationTimeNanos, NANOSECONDS);
            } catch (Throwable t) {
                if (t instanceof TombstoneOverwhelmingException) {
<<<<<<< MINE
                    handler.onFailure(FBUtilities.getBroadcastAddressAndPort(), RequestFailureReason.READ_TOO_MANY_TOMBSTONES);
=======
                    handler.onFailure(FBUtilities.getBroadcastAddress(), RequestFailureReason.READ_TOO_MANY_TOMBSTONES);
>>>>>>> YOURS
                    logger.error(t.getMessage());
                } else {
<<<<<<< MINE
                    handler.onFailure(FBUtilities.getBroadcastAddressAndPort(), RequestFailureReason.UNKNOWN);
=======
                    handler.onFailure(FBUtilities.getBroadcastAddress(), RequestFailureReason.UNKNOWN);
>>>>>>> YOURS
                    throw t;
            }
        }
        }
    }

    private static float estimateResultsPerRange(PartitionRangeReadCommand command, Keyspace keyspace) {
        ColumnFamilyStore cfs = keyspace.getColumnFamilyStore(command.metadata().id);
        Index index = command.getIndex(cfs);
        float maxExpectedResults = index == null ? command.limits().estimateTotalResults(cfs) : index.getEstimatedResultRows();
        return (maxExpectedResults / DatabaseDescriptor.getNumTokens()) / keyspace.getReplicationStrategy().getReplicationFactor().allReplicas;
    }

    @VisibleForTesting
    public static class RangeIterator extends AbstractIterator<ReplicaPlan.ForRangeRead> {

        private final Keyspace keyspace;

        private final ConsistencyLevel consistency;

        private final Iterator<? extends AbstractBounds<PartitionPosition>> ranges;

        private final int rangeCount;

        public RangeIterator(PartitionRangeReadCommand command, Keyspace keyspace, ConsistencyLevel consistency) {
            this.keyspace = keyspace;
            this.consistency = consistency;
            List<? extends AbstractBounds<PartitionPosition>> l = keyspace.getReplicationStrategy() instanceof LocalStrategy ? command.dataRange().keyRange().unwrap() : getRestrictedRanges(command.dataRange().keyRange());
            this.ranges = l.iterator();
            this.rangeCount = l.size();
        }

        public int rangeCount() {
            return rangeCount;
        }

        protected ReplicaPlan.ForRangeRead computeNext() {
            if (!ranges.hasNext())
                return endOfData();
            return ReplicaPlans.forRangeRead(keyspace, consistency, ranges.next(), 1);
        }
    }

        public static class RangeMerger extends AbstractIterator<ReplicaPlan.ForRangeRead> {

        private final Keyspace keyspace;

        private final ConsistencyLevel consistency;

        private final PeekingIterator<ReplicaPlan.ForRangeRead> ranges;

        public RangeMerger(Iterator<ReplicaPlan.ForRangeRead> iterator, Keyspace keyspace, ConsistencyLevel consistency) {
            this.keyspace = keyspace;
            this.consistency = consistency;
            this.ranges = Iterators.peekingIterator(iterator);
        }

        protected ReplicaPlan.ForRangeRead computeNext() {
            if (!ranges.hasNext())
                return endOfData();
            ReplicaPlan.ForRangeRead current = ranges.next();
            while (ranges.hasNext()) {
                if (current.range().right.isMinimum())
                    break;
                ReplicaPlan.ForRangeRead next = ranges.peek();
                ReplicaPlan.ForRangeRead merged = ReplicaPlans.maybeMerge(keyspace, consistency, current, next);
                if (merged == null)
                    break;
                current = merged;
                ranges.next();
            }
            return current;
        }
    }

    private static class SingleRangeResponse extends AbstractIterator<RowIterator> implements PartitionIterator {

        private final DataResolver resolver;

        private final ReadCallback handler;

        private final ReadRepair readRepair;

        private PartitionIterator result;

        private SingleRangeResponse(DataResolver resolver, ReadCallback handler, ReadRepair readRepair) {
            this.resolver = resolver;
            this.handler = handler;
            this.readRepair = readRepair;
        }

        private void waitForResponse() throws ReadTimeoutException {
            if (result != null)
                return;
            handler.awaitResults();
            result = resolver.resolve();
        }

        protected RowIterator computeNext() {
            waitForResponse();
            return result.hasNext() ? result.next() : endOfData();
        }

        public void close() {
            if (result != null)
                result.close();
        }
    }

    public static class RangeCommandIterator extends AbstractIterator<RowIterator> implements PartitionIterator {

        private final Iterator<ReplicaPlan.ForRangeRead> ranges;

        private final int totalRangeCount;

        private final PartitionRangeReadCommand command;

        private final boolean enforceStrictLiveness;

        private final long startTime;

        private final long queryStartNanoTime;

        private DataLimits.Counter counter;

        private PartitionIterator sentQueryIterator;

        private final int maxConcurrencyFactor;

        private int concurrencyFactor;

        private int liveReturned;

        private int rangesQueried;

        private int batchesRequested = 0;

<<<<<<< MINE
        public RangeCommandIterator(Iterator<ReplicaPlan.ForRangeRead> ranges, PartitionRangeReadCommand command, int concurrencyFactor, int maxConcurrencyFactor, int totalRangeCount, long queryStartNanoTime) {
            this.command = command;
            this.concurrencyFactor = concurrencyFactor;
            this.maxConcurrencyFactor = maxConcurrencyFactor;
            this.startTime = System.nanoTime();
            this.ranges = ranges;
            this.totalRangeCount = totalRangeCount;
            this.queryStartNanoTime = queryStartNanoTime;
            this.enforceStrictLiveness = command.metadata().enforceStrictLiveness();
        }
=======
        public RangeCommandIterator(Iterator<RangeForQuery> ranges, PartitionRangeReadCommand command, int concurrencyFactor, int maxConcurrencyFactor, int totalRangeCount, Keyspace keyspace, ConsistencyLevel consistency, long queryStartNanoTime) {
            this.command = command;
            this.concurrencyFactor = concurrencyFactor;
            this.maxConcurrencyFactor = maxConcurrencyFactor;
            this.startTime = System.nanoTime();
            this.ranges = ranges;
            this.totalRangeCount = totalRangeCount;
            this.consistency = consistency;
            this.keyspace = keyspace;
            this.queryStartNanoTime = queryStartNanoTime;
            this.enforceStrictLiveness = command.metadata().enforceStrictLiveness();
        }
>>>>>>> YOURS

        public RowIterator computeNext() {
            try {
                while (sentQueryIterator == null || !sentQueryIterator.hasNext()) {
                    if (!ranges.hasNext())
                        return endOfData();
                    if (sentQueryIterator != null) {
                        liveReturned += counter.counted();
                        sentQueryIterator.close();
                        updateConcurrencyFactor();
                    }
                    sentQueryIterator = sendNextRequests();
                }
                return sentQueryIterator.next();
            } catch (UnavailableException e) {
                rangeMetrics.unavailables.mark();
                throw e;
            } catch (ReadTimeoutException e) {
                rangeMetrics.timeouts.mark();
                throw e;
            } catch (ReadFailureException e) {
                rangeMetrics.failures.mark();
                throw e;
            }
        }

        private void updateConcurrencyFactor() {
            liveReturned += counter.counted();
            concurrencyFactor = computeConcurrencyFactor(totalRangeCount, rangesQueried, maxConcurrencyFactor, command.limits().count(), liveReturned);
        }

        @VisibleForTesting
        public static int computeConcurrencyFactor(int totalRangeCount, int rangesQueried, int maxConcurrencyFactor, int limit, int liveReturned) {
            maxConcurrencyFactor = Math.max(1, Math.min(maxConcurrencyFactor, totalRangeCount - rangesQueried));
            if (liveReturned == 0) {
                Tracing.trace("Didn't get any response rows; new concurrent requests: {}", maxConcurrencyFactor);
                return maxConcurrencyFactor;
            }
            int remainingRows = limit - liveReturned;
            float rowsPerRange = (float) liveReturned / (float) rangesQueried;
            int concurrencyFactor = Math.max(1, Math.min(maxConcurrencyFactor, Math.round(remainingRows / rowsPerRange)));
            logger.trace("Didn't get enough response rows; actual rows per range: {}; remaining rows: {}, new concurrent requests: {}", rowsPerRange, remainingRows, concurrencyFactor);
            return concurrencyFactor;
        }

        private SingleRangeResponse query(ReplicaPlan.ForRangeRead replicaPlan, boolean isFirst) {
            PartitionRangeReadCommand rangeCommand = command.forSubRange(replicaPlan.range(), isFirst);
            if (DatabaseDescriptor.getRepairedDataTrackingForRangeReadsEnabled() && replicaPlan.contacts().filter(Replica::isFull).size() > 1) {
                command.trackRepairedStatus();
                rangeCommand.trackRepairedStatus();
            }
            ReplicaPlan.SharedForRangeRead sharedReplicaPlan = ReplicaPlan.shared(replicaPlan);
            ReadRepair<EndpointsForRange, ReplicaPlan.ForRangeRead> readRepair = ReadRepair.create(command, sharedReplicaPlan, queryStartNanoTime);
            DataResolver<EndpointsForRange, ReplicaPlan.ForRangeRead> resolver = new DataResolver<>(rangeCommand, sharedReplicaPlan, readRepair, queryStartNanoTime);
            ReadCallback<EndpointsForRange, ReplicaPlan.ForRangeRead> handler = new ReadCallback<>(resolver, rangeCommand, sharedReplicaPlan, queryStartNanoTime);
            if (replicaPlan.contacts().size() == 1 && replicaPlan.contacts().get(0).isSelf()) {
                Stage.READ.execute(new LocalReadRunnable(rangeCommand, handler));
            } else {
                for (Replica replica : replicaPlan.contacts()) {
                    Tracing.trace("Enqueuing request to {}", replica);
                    ReadCommand command = replica.isFull() ? rangeCommand : rangeCommand.copyAsTransientQuery(replica);
                    Message<ReadCommand> message = command.createMessage(command.isTrackingRepairedStatus() && replica.isFull());
                    MessagingService.instance().sendWithCallback(message, replica.endpoint(), handler);
                }
            }
            return new SingleRangeResponse(resolver, handler, readRepair);
        }

        private PartitionIterator sendNextRequests() {
            List<PartitionIterator> concurrentQueries = new ArrayList<>(concurrencyFactor);
            List<ReadRepair> readRepairs = new ArrayList<>(concurrencyFactor);
            try {
            for (int i = 0; i < concurrencyFactor && ranges.hasNext(); ) {
<<<<<<< MINE
                    ReplicaPlan.ForRangeRead range = ranges.next();
                    @SuppressWarnings("resource")
                    SingleRangeResponse response = query(range, i == 0);
                    concurrentQueries.add(response);
                    readRepairs.add(response.readRepair);
=======
                RangeForQuery range = ranges.next();
                concurrentQueries.add(query(range, i == 0));
>>>>>>> YOURS
                rangesQueried += range.vnodeCount();
                i += range.vnodeCount();
            }
            batchesRequested++;
            } catch (Throwable t) {
                for (PartitionIterator response : concurrentQueries) response.close();
                throw t;
            }
            Tracing.trace("Submitted {} concurrent range requests", concurrentQueries.size());
            counter = DataLimits.NONE.newCounter(command.nowInSec(), true, command.selectsFullPartition(), enforceStrictLiveness);
            return counter.applyTo(concatAndBlockOnRepair(concurrentQueries, readRepairs));
        }

        public void close() {
            try {
                if (sentQueryIterator != null)
                    sentQueryIterator.close();
            } finally {
                long latency = System.nanoTime() - startTime;
                rangeMetrics.addNano(latency);
                Keyspace.openAndGetStore(command.metadata()).metric.coordinatorScanLatency.update(latency, TimeUnit.NANOSECONDS);
            }
        }

        @VisibleForTesting
        public int rangesQueried() {
            return rangesQueried;
        }

        @VisibleForTesting
        public int batchesRequested() {
            return batchesRequested;
        }
    }

    @SuppressWarnings("resource")
    public static PartitionIterator getRangeSlice(PartitionRangeReadCommand command, ConsistencyLevel consistencyLevel, long queryStartNanoTime) {
        Tracing.trace("Computing ranges to query");
        Keyspace keyspace = Keyspace.open(command.metadata().keyspace);
        RangeIterator ranges = new RangeIterator(command, keyspace, consistencyLevel);
        float resultsPerRange = estimateResultsPerRange(command, keyspace);
        resultsPerRange -= resultsPerRange * CONCURRENT_SUBREQUESTS_MARGIN;
        int maxConcurrencyFactor = Math.min(ranges.rangeCount(), MAX_CONCURRENT_RANGE_REQUESTS);
        int concurrencyFactor = resultsPerRange == 0.0 ? 1 : Math.max(1, Math.min(maxConcurrencyFactor, (int) Math.ceil(command.limits().count() / resultsPerRange)));
        logger.trace("Estimated result rows per range: {}; requested rows: {}, ranges.size(): {}; concurrent range requests: {}", resultsPerRange, command.limits().count(), ranges.rangeCount(), concurrencyFactor);
        Tracing.trace("Submitting range requests on {} ranges with a concurrency of {} ({} rows per range expected)", ranges.rangeCount(), concurrencyFactor, resultsPerRange);
        RangeMerger mergedRanges = new RangeMerger(ranges, keyspace, consistencyLevel);
<<<<<<< MINE
        RangeCommandIterator rangeCommandIterator = new RangeCommandIterator(mergedRanges, command, concurrencyFactor, maxConcurrencyFactor, ranges.rangeCount(), queryStartNanoTime);
=======
        RangeCommandIterator rangeCommandIterator = new RangeCommandIterator(mergedRanges, command, concurrencyFactor, maxConcurrencyFactor, ranges.rangeCount(), keyspace, consistencyLevel, queryStartNanoTime);
>>>>>>> YOURS
        return command.limits().filter(command.postReconciliationProcessing(rangeCommandIterator), command.nowInSec(), command.selectsFullPartition(), command.metadata().enforceStrictLiveness());
    }

    public Map<String, List<String>> getSchemaVersions() {
        return describeSchemaVersions(false);
    }

    public Map<String, List<String>> getSchemaVersionsWithPort() {
        return describeSchemaVersions(true);
    }

    public static Map<String, List<String>> describeSchemaVersions(boolean withPort) {
        final String myVersion = Schema.instance.getVersion().toString();
        final Map<InetAddressAndPort, UUID> versions = new ConcurrentHashMap<>();
        final Set<InetAddressAndPort> liveHosts = Gossiper.instance.getLiveMembers();
        final CountDownLatch latch = new CountDownLatch(liveHosts.size());
        RequestCallback<UUID> cb = message -> {
            versions.put(message.from(), message.payload);
                latch.countDown();
        };
        Message message = Message.out(Verb.SCHEMA_VERSION_REQ, noPayload);
        for (InetAddressAndPort endpoint : liveHosts) MessagingService.instance().sendWithCallback(message, endpoint, cb);
        try {
            latch.await(DatabaseDescriptor.getRpcTimeout(NANOSECONDS), NANOSECONDS);
        } catch (InterruptedException ex) {
            throw new AssertionError("This latch shouldn't have been interrupted.");
        }
        Map<String, List<String>> results = new HashMap<String, List<String>>();
        Iterable<InetAddressAndPort> allHosts = Iterables.concat(Gossiper.instance.getLiveMembers(), Gossiper.instance.getUnreachableMembers());
        for (InetAddressAndPort host : allHosts) {
            UUID version = versions.get(host);
            String stringVersion = version == null ? UNREACHABLE : version.toString();
            List<String> hosts = results.get(stringVersion);
            if (hosts == null) {
                hosts = new ArrayList<String>();
                results.put(stringVersion, hosts);
            }
            hosts.add(host.getHostAddress(withPort));
        }
        if (results.get(UNREACHABLE) != null)
            logger.debug("Hosts not in agreement. Didn't get a response from everybody: {}", StringUtils.join(results.get(UNREACHABLE), ","));
        for (Map.Entry<String, List<String>> entry : results.entrySet()) {
            if (entry.getKey().equals(UNREACHABLE) || entry.getKey().equals(myVersion))
                continue;
            for (String host : entry.getValue()) logger.debug("{} disagrees ({})", host, entry.getKey());
        }
        if (results.size() == 1)
            logger.debug("Schemas are in agreement.");
        return results;
    }

    static <T extends RingPosition<T>> List<AbstractBounds<T>> getRestrictedRanges(final AbstractBounds<T> queryRange) {
        if (queryRange instanceof Bounds && queryRange.left.equals(queryRange.right) && !queryRange.left.isMinimum()) {
            return Collections.singletonList(queryRange);
        }
        TokenMetadata tokenMetadata = StorageService.instance.getTokenMetadata();
        List<AbstractBounds<T>> ranges = new ArrayList<AbstractBounds<T>>();
        Iterator<Token> ringIter = TokenMetadata.ringIterator(tokenMetadata.sortedTokens(), queryRange.left.getToken(), true);
        AbstractBounds<T> remainder = queryRange;
        while (ringIter.hasNext()) {
            Token upperBoundToken = ringIter.next();
            T upperBound = (T) upperBoundToken.upperBound(queryRange.left.getClass());
            if (!remainder.left.equals(upperBound) && !remainder.contains(upperBound))
                break;
            Pair<AbstractBounds<T>, AbstractBounds<T>> splits = remainder.split(upperBound);
            if (splits == null)
                continue;
            ranges.add(splits.left);
            remainder = splits.right;
        }
        ranges.add(remainder);
        return ranges;
    }

    public boolean getHintedHandoffEnabled() {
        return DatabaseDescriptor.hintedHandoffEnabled();
    }

    public void setHintedHandoffEnabled(boolean b) {
        synchronized (StorageService.instance) {
            if (b)
                StorageService.instance.checkServiceAllowedToStart("hinted handoff");
            DatabaseDescriptor.setHintedHandoffEnabled(b);
        }
    }

    public void enableHintsForDC(String dc) {
        DatabaseDescriptor.enableHintsForDC(dc);
    }

    public void disableHintsForDC(String dc) {
        DatabaseDescriptor.disableHintsForDC(dc);
    }

    public Set<String> getHintedHandoffDisabledDCs() {
        return DatabaseDescriptor.hintedHandoffDisabledDCs();
    }

    public int getMaxHintWindow() {
        return DatabaseDescriptor.getMaxHintWindow();
    }

    public void setMaxHintWindow(int ms) {
        DatabaseDescriptor.setMaxHintWindow(ms);
    }

    public static boolean shouldHint(Replica replica) {
        if (!DatabaseDescriptor.hintedHandoffEnabled())
            return false;
        if (replica.isTransient() || replica.isSelf())
            return false;
            Set<String> disabledDCs = DatabaseDescriptor.hintedHandoffDisabledDCs();
            if (!disabledDCs.isEmpty()) {
            final String dc = DatabaseDescriptor.getEndpointSnitch().getDatacenter(replica);
                if (disabledDCs.contains(dc)) {
                Tracing.trace("Not hinting {} since its data center {} has been disabled {}", replica, dc, disabledDCs);
                    return false;
                }
            }
        boolean hintWindowExpired = Gossiper.instance.getEndpointDowntime(replica.endpoint()) > DatabaseDescriptor.getMaxHintWindow();
            if (hintWindowExpired) {
            HintsService.instance.metrics.incrPastWindow(replica.endpoint());
            Tracing.trace("Not hinting {} which has been down {} ms", replica, Gossiper.instance.getEndpointDowntime(replica.endpoint()));
            }
            return !hintWindowExpired;
    }

    public static void truncateBlocking(String keyspace, String cfname) throws UnavailableException, TimeoutException {
        logger.debug("Starting a blocking truncate operation on keyspace {}, CF {}", keyspace, cfname);
        if (isAnyStorageHostDown()) {
            logger.info("Cannot perform truncate, some hosts are down");
            int liveMembers = Gossiper.instance.getLiveMembers().size();
            throw UnavailableException.create(ConsistencyLevel.ALL, liveMembers + Gossiper.instance.getUnreachableMembers().size(), liveMembers);
        }
        Set<InetAddressAndPort> allEndpoints = StorageService.instance.getLiveRingMembers(true);
        int blockFor = allEndpoints.size();
        final TruncateResponseHandler responseHandler = new TruncateResponseHandler(blockFor);
        Tracing.trace("Enqueuing truncate messages to hosts {}", allEndpoints);
        Message<TruncateRequest> message = Message.out(TRUNCATE_REQ, new TruncateRequest(keyspace, cfname));
        for (InetAddressAndPort endpoint : allEndpoints) MessagingService.instance().sendWithCallback(message, endpoint, responseHandler);
        try {
            responseHandler.get();
        } catch (TimeoutException e) {
            Tracing.trace("Timed out");
            throw e;
        }
    }

    private static boolean isAnyStorageHostDown() {
        return !Gossiper.instance.getUnreachableTokenOwners().isEmpty();
    }

    public interface WritePerformer {

        public void apply(IMutation mutation, ReplicaPlan.ForTokenWrite targets, AbstractWriteResponseHandler<IMutation> responseHandler, String localDataCenter) throws OverloadedException;
    }

    private static class ViewWriteMetricsWrapped extends BatchlogResponseHandler<IMutation> {

<<<<<<< MINE

=======
        public ViewWriteMetricsWrapped(AbstractWriteResponseHandler<IMutation> writeHandler, int i, BatchlogCleanup cleanup, long queryStartNanoTime) {
            super(writeHandler, i, cleanup, queryStartNanoTime);
            viewWriteMetrics.viewReplicasAttempted.inc(totalEndpoints());
        }
>>>>>>> YOURS

        public void onResponse(Message<IMutation> msg) {
            super.onResponse(msg);
            viewWriteMetrics.viewReplicasSuccess.inc();
        }
    }

    private static abstract class DroppableRunnable implements Runnable {

        final long approxCreationTimeNanos;<<<<<<< MINE
=======
final long constructionTime;
>>>>>>> YOURS


<<<<<<< MINE
final Verb verb;
=======
final MessagingService.Verb verb;
>>>>>>> YOURS


        public DroppableRunnable(Verb verb) {
            this.approxCreationTimeNanos = MonotonicClock.approxTime.now();
            this.verb = verb;
        }<<<<<<< MINE
=======
public DroppableRunnable(MessagingService.Verb verb) {
            this.constructionTime = System.currentTimeMillis();
            this.verb = verb;
        }
>>>>>>> YOURS


        public final void run() {
<<<<<<< MINE
            long approxCurrentTimeNanos = MonotonicClock.approxTime.now();
            long expirationTimeNanos = verb.expiresAtNanos(approxCreationTimeNanos);
            if (approxCurrentTimeNanos > expirationTimeNanos) {
                long timeTakenNanos = approxCurrentTimeNanos - approxCreationTimeNanos;
                MessagingService.instance().metrics.recordSelfDroppedMessage(verb, timeTakenNanos, NANOSECONDS);
=======
            long timeTaken = System.currentTimeMillis() - constructionTime;
            if (timeTaken > verb.getTimeout()) {
                MessagingService.instance().incrementDroppedMessages(verb, timeTaken);
>>>>>>> YOURS
                return;
            }
            try {
                runMayThrow();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        abstract protected void runMayThrow() throws Exception;
    }

    private static abstract class LocalMutationRunnable implements Runnable {

        private final long approxCreationTimeNanos = MonotonicClock.approxTime.now();

        private final Replica localReplica;

        LocalMutationRunnable(Replica localReplica) {
            this.localReplica = localReplica;
        }

        private final Optional<IMutation> mutationOpt;

        public LocalMutationRunnable(Optional<IMutation> mutationOpt) {
            this.mutationOpt = mutationOpt;
        }

        public LocalMutationRunnable() {
            this.mutationOpt = Optional.empty();
        }

        public final void run() {
<<<<<<< MINE
            final Verb verb = verb();
            long nowNanos = MonotonicClock.approxTime.now();
            long expirationTimeNanos = verb.expiresAtNanos(approxCreationTimeNanos);
            if (nowNanos > expirationTimeNanos) {
                long timeTakenNanos = nowNanos - approxCreationTimeNanos;
                MessagingService.instance().metrics.recordSelfDroppedMessage(Verb.MUTATION_REQ, timeTakenNanos, NANOSECONDS);
                HintRunnable runnable = new HintRunnable(EndpointsForToken.of(localReplica.range().right, localReplica)) {
=======
            final MessagingService.Verb verb = verb();
            long mutationTimeout = verb.getTimeout();
            long timeTaken = System.currentTimeMillis() - constructionTime;
            if (timeTaken > mutationTimeout) {
                if (MessagingService.DROPPABLE_VERBS.contains(verb))
                    MessagingService.instance().incrementDroppedMutations(mutationOpt, timeTaken);
                HintRunnable runnable = new HintRunnable(Collections.singleton(FBUtilities.getBroadcastAddress())) {
>>>>>>> YOURS

                    protected void runMayThrow() throws Exception {
                        LocalMutationRunnable.this.runMayThrow();
                    }
                };
                submitHint(runnable);
                return;
            }
            try {
                runMayThrow();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        abstract protected Verb verb();

        abstract protected void runMayThrow() throws Exception;
    }

    private abstract static class HintRunnable implements Runnable {

        public final EndpointsForToken targets;

        protected HintRunnable(EndpointsForToken targets) {
            this.targets = targets;
        }

        public void run() {
            try {
                runMayThrow();
            } catch (Exception e) {
                throw new RuntimeException(e);
            } finally {
                StorageMetrics.totalHintsInProgress.dec(targets.size());
                for (InetAddressAndPort target : targets.endpoints()) getHintsInProgressFor(target).decrementAndGet();
            }
        }

        abstract protected void runMayThrow() throws Exception;
    }

    public long getTotalHints() {
        return StorageMetrics.totalHints.getCount();
    }

    public int getMaxHintsInProgress() {
        return maxHintsInProgress;
    }

    public void setMaxHintsInProgress(int qs) {
        maxHintsInProgress = qs;
    }

    public int getHintsInProgress() {
        return (int) StorageMetrics.totalHintsInProgress.getCount();
    }

    public void verifyNoHintsInProgress() {
        if (getHintsInProgress() > 0)
            logger.warn("Some hints were not written before shutdown.  This is not supposed to happen.  You should (a) run repair, and (b) file a bug report");
    }

    private static AtomicInteger getHintsInProgressFor(InetAddressAndPort destination) {
        try {
            return hintsInProgress.load(destination);
        } catch (Exception e) {
            throw new AssertionError(e);
        }
    }

    public static Future<Void> submitHint(Mutation mutation, Replica target, AbstractWriteResponseHandler<IMutation> responseHandler) {
        return submitHint(mutation, EndpointsForToken.of(target.range().right, target), responseHandler);
    }

    public static Future<Void> submitHint(Mutation mutation, EndpointsForToken targets, AbstractWriteResponseHandler<IMutation> responseHandler) {
        Replicas.assertFull(targets);
        HintRunnable runnable = new HintRunnable(targets) {

            public void runMayThrow() {
                Set<InetAddressAndPort> validTargets = new HashSet<>(targets.size());
                Set<UUID> hostIds = new HashSet<>(targets.size());
                for (InetAddressAndPort target : targets.endpoints()) {
                    UUID hostId = StorageService.instance.getHostIdForEndpoint(target);
                    if (hostId != null) {
                        hostIds.add(hostId);
                        validTargets.add(target);
                    } else
                        logger.debug("Discarding hint for endpoint not part of ring: {}", target);
                }
                logger.trace("Adding hints for {}", validTargets);
                HintsService.instance.write(hostIds, Hint.create(mutation, System.currentTimeMillis()));
                validTargets.forEach(HintsService.instance.metrics::incrCreatedHints);
                if (responseHandler != null && responseHandler.replicaPlan.consistencyLevel() == ConsistencyLevel.ANY)
                    responseHandler.onResponse(null);
            }
        };
        return submitHint(runnable);
    }

    private static Future<Void> submitHint(HintRunnable runnable) {
        StorageMetrics.totalHintsInProgress.inc(runnable.targets.size());
        for (Replica target : runnable.targets) getHintsInProgressFor(target.endpoint()).incrementAndGet();
        return (Future<Void>) Stage.MUTATION.submit(runnable);
    }

    public Long getRpcTimeout() {
        return DatabaseDescriptor.getRpcTimeout(MILLISECONDS);
    }

    public void setRpcTimeout(Long timeoutInMillis) {
        DatabaseDescriptor.setRpcTimeout(timeoutInMillis);
    }

    public Long getReadRpcTimeout() {
        return DatabaseDescriptor.getReadRpcTimeout(MILLISECONDS);
    }

    public void setReadRpcTimeout(Long timeoutInMillis) {
        DatabaseDescriptor.setReadRpcTimeout(timeoutInMillis);
    }

    public Long getWriteRpcTimeout() {
        return DatabaseDescriptor.getWriteRpcTimeout(MILLISECONDS);
    }

    public void setWriteRpcTimeout(Long timeoutInMillis) {
        DatabaseDescriptor.setWriteRpcTimeout(timeoutInMillis);
    }

    public Long getCounterWriteRpcTimeout() {
        return DatabaseDescriptor.getCounterWriteRpcTimeout(MILLISECONDS);
    }

    public void setCounterWriteRpcTimeout(Long timeoutInMillis) {
        DatabaseDescriptor.setCounterWriteRpcTimeout(timeoutInMillis);
    }

    public Long getCasContentionTimeout() {
        return DatabaseDescriptor.getCasContentionTimeout(MILLISECONDS);
    }

    public void setCasContentionTimeout(Long timeoutInMillis) {
        DatabaseDescriptor.setCasContentionTimeout(timeoutInMillis);
    }

    public Long getRangeRpcTimeout() {
        return DatabaseDescriptor.getRangeRpcTimeout(MILLISECONDS);
    }

    public void setRangeRpcTimeout(Long timeoutInMillis) {
        DatabaseDescriptor.setRangeRpcTimeout(timeoutInMillis);
    }

    public Long getTruncateRpcTimeout() {
        return DatabaseDescriptor.getTruncateRpcTimeout(MILLISECONDS);
    }

    public void setTruncateRpcTimeout(Long timeoutInMillis) {
        DatabaseDescriptor.setTruncateRpcTimeout(timeoutInMillis);
    }

    public Long getNativeTransportMaxConcurrentConnections() {
        return DatabaseDescriptor.getNativeTransportMaxConcurrentConnections();
    }

    public void setNativeTransportMaxConcurrentConnections(Long nativeTransportMaxConcurrentConnections) {
        DatabaseDescriptor.setNativeTransportMaxConcurrentConnections(nativeTransportMaxConcurrentConnections);
    }

    public Long getNativeTransportMaxConcurrentConnectionsPerIp() {
        return DatabaseDescriptor.getNativeTransportMaxConcurrentConnectionsPerIp();
    }

    public void setNativeTransportMaxConcurrentConnectionsPerIp(Long nativeTransportMaxConcurrentConnections) {
        DatabaseDescriptor.setNativeTransportMaxConcurrentConnectionsPerIp(nativeTransportMaxConcurrentConnections);
    }

    public void reloadTriggerClasses() {
        TriggerExecutor.instance.reloadClasses();
    }

    public long getReadRepairAttempted() {
        return ReadRepairMetrics.attempted.getCount();
    }

    public long getReadRepairRepairedBlocking() {
        return ReadRepairMetrics.repairedBlocking.getCount();
    }

    public long getReadRepairRepairedBackground() {
        return ReadRepairMetrics.repairedBackground.getCount();
    }

    public int getNumberOfTables() {
        return Schema.instance.getNumberOfTables();
    }

    public String getIdealConsistencyLevel() {
        return Objects.toString(DatabaseDescriptor.getIdealConsistencyLevel(), "");
    }

    public String setIdealConsistencyLevel(String cl) {
        ConsistencyLevel original = DatabaseDescriptor.getIdealConsistencyLevel();
        ConsistencyLevel newCL = ConsistencyLevel.valueOf(cl.trim().toUpperCase());
        DatabaseDescriptor.setIdealConsistencyLevel(newCL);
        return String.format("Updating ideal consistency level new value: %s old value %s", newCL, original.toString());
    }

    @Deprecated
public int getOtcBacklogExpirationInterval() {
        return 0;
    }

    @Deprecated
public void setOtcBacklogExpirationInterval(int intervalInMillis) {
    }

    @Override
    public void enableRepairedDataTrackingForRangeReads() {
        DatabaseDescriptor.setRepairedDataTrackingForRangeReadsEnabled(true);
    }

    @Override
    public void disableRepairedDataTrackingForRangeReads() {
        DatabaseDescriptor.setRepairedDataTrackingForRangeReadsEnabled(false);
    }

    @Override
    public boolean getRepairedDataTrackingEnabledForRangeReads() {
        return DatabaseDescriptor.getRepairedDataTrackingForRangeReadsEnabled();
    }

    @Override
    public void enableRepairedDataTrackingForPartitionReads() {
        DatabaseDescriptor.setRepairedDataTrackingForPartitionReadsEnabled(true);
    }

    @Override
    public void disableRepairedDataTrackingForPartitionReads() {
        DatabaseDescriptor.setRepairedDataTrackingForPartitionReadsEnabled(false);
    }

    @Override
    public boolean getRepairedDataTrackingEnabledForPartitionReads() {
        return DatabaseDescriptor.getRepairedDataTrackingForPartitionReadsEnabled();
    }

    @Override
    public void enableReportingUnconfirmedRepairedDataMismatches() {
        DatabaseDescriptor.reportUnconfirmedRepairedDataMismatches(true);
    }

    @Override
    public void disableReportingUnconfirmedRepairedDataMismatches() {
        DatabaseDescriptor.reportUnconfirmedRepairedDataMismatches(false);
    }

    @Override
    public boolean getReportingUnconfirmedRepairedDataMismatchesEnabled() {
        return DatabaseDescriptor.reportUnconfirmedRepairedDataMismatches();
    }

    @Override
    public boolean getSnapshotOnRepairedDataMismatchEnabled() {
        return DatabaseDescriptor.snapshotOnRepairedDataMismatch();
    }

    @Override
    public void enableSnapshotOnRepairedDataMismatch() {
        DatabaseDescriptor.setSnapshotOnRepairedDataMismatch(true);
    }

    @Override
    public void disableSnapshotOnRepairedDataMismatch() {
        DatabaseDescriptor.setSnapshotOnRepairedDataMismatch(false);
    }

    static class PaxosBallotAndContention {

        final UUID ballot;

        final int contentions;

        PaxosBallotAndContention(UUID ballot, int contentions) {
            this.ballot = ballot;
            this.contentions = contentions;
        }

        @Override
        public final int hashCode() {
            int hashCode = 31 + (ballot == null ? 0 : ballot.hashCode());
            return 31 * hashCode * this.contentions;
        }

        @Override
        public final boolean equals(Object o) {
            if (!(o instanceof PaxosBallotAndContention))
                return false;
            PaxosBallotAndContention that = (PaxosBallotAndContention) o;
            return Objects.equals(ballot, that.ballot) && contentions == that.contentions;
        }
    }

    @Override
    public boolean getSnapshotOnDuplicateRowDetectionEnabled() {
        return DatabaseDescriptor.snapshotOnDuplicateRowDetection();
    }

    @Override
    public void enableSnapshotOnDuplicateRowDetection() {
        DatabaseDescriptor.setSnapshotOnDuplicateRowDetection(true);
    }

    @Override
    public void disableSnapshotOnDuplicateRowDetection() {
        DatabaseDescriptor.setSnapshotOnDuplicateRowDetection(false);
    }

    @Override
    public boolean getCheckForDuplicateRowsDuringReads() {
        return DatabaseDescriptor.checkForDuplicateRowsDuringReads();
    }

    @Override
    public void enableCheckForDuplicateRowsDuringReads() {
        DatabaseDescriptor.setCheckForDuplicateRowsDuringReads(true);
    }

    @Override
    public void disableCheckForDuplicateRowsDuringReads() {
        DatabaseDescriptor.setCheckForDuplicateRowsDuringReads(false);
    }

    @Override
    public boolean getCheckForDuplicateRowsDuringCompaction() {
        return DatabaseDescriptor.checkForDuplicateRowsDuringCompaction();
    }

    @Override
    public void enableCheckForDuplicateRowsDuringCompaction() {
        DatabaseDescriptor.setCheckForDuplicateRowsDuringCompaction(true);
    }

    @Override
    public void disableCheckForDuplicateRowsDuringCompaction() {
        DatabaseDescriptor.setCheckForDuplicateRowsDuringCompaction(false);
    }
}