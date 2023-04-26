package org.apache.beam.runners.dataflow;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.apache.beam.runners.core.construction.resources.PipelineResources.detectClassPathResourcesToStage;
import static org.apache.beam.sdk.util.CoderUtils.encodeToByteArray;
import static org.apache.beam.sdk.util.SerializableUtils.serializeToByteArray;
import static org.apache.beam.sdk.util.StringUtils.byteArrayToJsonString;
import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.MoreObjects.firstNonNull;
import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions.checkArgument;
import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions.checkState;
import static org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Strings.isNullOrEmpty;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.json.GoogleJsonResponseException;
import com.google.api.services.clouddebugger.v2.CloudDebugger;
import com.google.api.services.clouddebugger.v2.model.Debuggee;
import com.google.api.services.clouddebugger.v2.model.RegisterDebuggeeRequest;
import com.google.api.services.clouddebugger.v2.model.RegisterDebuggeeResponse;
import com.google.api.services.dataflow.model.DataflowPackage;
import com.google.api.services.dataflow.model.Job;
import com.google.api.services.dataflow.model.ListJobsResponse;
import com.google.api.services.dataflow.model.SdkHarnessContainerImage;
import com.google.api.services.dataflow.model.WorkerPool;
import com.google.auto.value.AutoValue;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.channels.Channels;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import org.apache.beam.model.pipeline.v1.RunnerApi;
import org.apache.beam.runners.core.construction.BeamUrns;
import org.apache.beam.runners.core.construction.DeduplicatedFlattenFactory;
import org.apache.beam.runners.core.construction.EmptyFlattenAsCreateFactory;
import org.apache.beam.runners.core.construction.Environments;
import org.apache.beam.runners.core.construction.External;
import org.apache.beam.runners.core.construction.PTransformMatchers;
import org.apache.beam.runners.core.construction.PTransformReplacements;
import org.apache.beam.runners.core.construction.PipelineTranslation;
import org.apache.beam.runners.core.construction.ReplacementOutputs;
import org.apache.beam.runners.core.construction.SdkComponents;
import org.apache.beam.runners.core.construction.SingleInputOutputOverrideFactory;
import org.apache.beam.runners.core.construction.SplittableParDo;
import org.apache.beam.runners.core.construction.SplittableParDoNaiveBounded;
import org.apache.beam.runners.core.construction.UnboundedReadFromBoundedSource;
import org.apache.beam.runners.core.construction.UnconsumedReads;
import org.apache.beam.runners.core.construction.WriteFilesTranslation;
import org.apache.beam.runners.core.construction.graph.ProjectionPushdownOptimizer;
import org.apache.beam.runners.dataflow.DataflowPipelineTranslator.JobSpecification;
import org.apache.beam.runners.dataflow.StreamingViewOverrides.StreamingCreatePCollectionViewFactory;
import org.apache.beam.runners.dataflow.TransformTranslator.StepTranslationContext;
import org.apache.beam.runners.dataflow.options.DataflowPipelineDebugOptions;
import org.apache.beam.runners.dataflow.options.DataflowPipelineOptions;
import org.apache.beam.runners.dataflow.options.DataflowPipelineWorkerPoolOptions;
import org.apache.beam.runners.dataflow.util.DataflowTemplateJob;
import org.apache.beam.runners.dataflow.util.DataflowTransport;
import org.apache.beam.runners.dataflow.util.MonitoringUtil;
import org.apache.beam.runners.dataflow.util.PackageUtil.StagedFile;
import org.apache.beam.runners.dataflow.util.PropertyNames;
import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.Pipeline.PipelineVisitor;
import org.apache.beam.sdk.PipelineResult.State;
import org.apache.beam.sdk.PipelineRunner;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.Coder.NonDeterministicException;
import org.apache.beam.sdk.coders.KvCoder;
import org.apache.beam.sdk.coders.VoidCoder;
import org.apache.beam.sdk.extensions.gcp.options.GcpOptions;
import org.apache.beam.sdk.extensions.gcp.storage.PathValidator;
import org.apache.beam.sdk.io.BoundedSource;
import org.apache.beam.sdk.io.FileBasedSink;
import org.apache.beam.sdk.io.FileSystems;
import org.apache.beam.sdk.io.Read;
import org.apache.beam.sdk.io.UnboundedSource;
import org.apache.beam.sdk.io.WriteFiles;
import org.apache.beam.sdk.io.WriteFilesResult;
import org.apache.beam.sdk.io.fs.ResolveOptions;
import org.apache.beam.sdk.io.fs.ResourceId;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessage;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessageWithAttributesAndMessageIdCoder;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubMessageWithAttributesCoder;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubUnboundedSink;
import org.apache.beam.sdk.io.gcp.pubsub.PubsubUnboundedSource;
import org.apache.beam.sdk.io.kafka.KafkaIO;
import org.apache.beam.sdk.options.ExperimentalOptions;
import org.apache.beam.sdk.options.PipelineOptions;
import org.apache.beam.sdk.options.PipelineOptionsValidator;
import org.apache.beam.sdk.options.ValueProvider.NestedValueProvider;
import org.apache.beam.sdk.runners.AppliedPTransform;
import org.apache.beam.sdk.runners.PTransformOverride;
import org.apache.beam.sdk.runners.PTransformOverrideFactory;
import org.apache.beam.sdk.runners.TransformHierarchy;
import org.apache.beam.sdk.runners.TransformHierarchy.Node;
import org.apache.beam.sdk.state.MapState;
import org.apache.beam.sdk.state.SetState;
import org.apache.beam.sdk.transforms.Combine;
import org.apache.beam.sdk.transforms.Combine.CombineFn;
import org.apache.beam.sdk.transforms.Combine.GroupedValues;
import org.apache.beam.sdk.transforms.DoFn;
import org.apache.beam.sdk.transforms.GroupIntoBatches;
import org.apache.beam.sdk.transforms.Impulse;
import org.apache.beam.sdk.transforms.PTransform;
import org.apache.beam.sdk.transforms.ParDo;
import org.apache.beam.sdk.transforms.Reshuffle;
import org.apache.beam.sdk.transforms.SimpleFunction;
import org.apache.beam.sdk.transforms.View;
import org.apache.beam.sdk.transforms.View.CreatePCollectionView;
import org.apache.beam.sdk.transforms.WithKeys;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.transforms.reflect.DoFnSignatures;
import org.apache.beam.sdk.transforms.windowing.BoundedWindow;
import org.apache.beam.sdk.transforms.windowing.GlobalWindow;
import org.apache.beam.sdk.util.InstanceBuilder;
import org.apache.beam.sdk.util.MimeTypes;
import org.apache.beam.sdk.util.NameUtils;
import org.apache.beam.sdk.util.ReleaseInfo;
import org.apache.beam.sdk.util.WindowedValue;
import org.apache.beam.sdk.util.common.ReflectHelpers;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PBegin;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollection.IsBounded;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.PDone;
import org.apache.beam.sdk.values.PInput;
import org.apache.beam.sdk.values.PValue;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TypeDescriptors;
import org.apache.beam.sdk.values.ValueWithRecordId;
import org.apache.beam.sdk.values.WindowingStrategy;
import org.apache.beam.vendor.grpc.v1p43p2.com.google.protobuf.InvalidProtocolBufferException;
import org.apache.beam.vendor.grpc.v1p43p2.com.google.protobuf.TextFormat;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.annotations.VisibleForTesting;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Joiner;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Preconditions;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Strings;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.base.Utf8;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.collect.ImmutableList;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.collect.ImmutableMap;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.collect.Iterables;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.hash.HashCode;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.hash.Hashing;
import org.apache.beam.vendor.guava.v26_0_jre.com.google.common.io.Files;
import org.joda.time.DateTimeUtils;
import org.joda.time.DateTimeZone;
import org.joda.time.format.DateTimeFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "nullness" })
public class DataflowRunner extends PipelineRunner<DataflowPipelineJob> {

    private static final Logger LOG = LoggerFactory.getLogger(DataflowRunner.class);

    private final DataflowPipelineOptions options;

    private final DataflowClient dataflowClient;

    private final DataflowPipelineTranslator translator;

    private DataflowRunnerHooks hooks;

    private static final int CREATE_JOB_REQUEST_LIMIT_BYTES = 10 * 1024 * 1024;

    @VisibleForTesting
    static final int GCS_UPLOAD_BUFFER_SIZE_BYTES_DEFAULT = 1024 * 1024;

    @VisibleForTesting
    static final String PIPELINE_FILE_NAME = "pipeline.pb";

    @VisibleForTesting
    static final String DATAFLOW_GRAPH_FILE_NAME = "dataflow_graph.json";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final ObjectMapper MAPPER_WITH_MODULES = new ObjectMapper().registerModules(ObjectMapper.findModules(ReflectHelpers.findClassLoader()));

    private final Set<PCollection<?>> pcollectionsRequiringIndexedFormat;

    private final Set<PCollection<?>> pCollectionsPreservedKeys;

    private final Set<PCollection<?>> pcollectionsRequiringAutoSharding;

    public static final String PROJECT_ID_REGEXP = "[a-z][-a-z0-9:.]+[a-z0-9]";

    static final String ENDPOINT_REGEXP = "https://[\\S]*googleapis\\.com[/]?";

    public static DataflowRunner fromOptions(PipelineOptions options) {
        DataflowPipelineOptions dataflowOptions = PipelineOptionsValidator.validate(DataflowPipelineOptions.class, options);
        ArrayList<String> missing = new ArrayList<>();
        if (dataflowOptions.getAppName() == null) {
            missing.add("appName");
        }
        if (Strings.isNullOrEmpty(dataflowOptions.getRegion()) && isServiceEndpoint(dataflowOptions.getDataflowEndpoint())) {
            missing.add("region");
        }
        if (missing.size() > 0) {
            throw new IllegalArgumentException("Missing required pipeline options: " + Joiner.on(',').join(missing));
        }
        validateWorkerSettings(PipelineOptionsValidator.validate(DataflowPipelineWorkerPoolOptions.class, options));
        PathValidator validator = dataflowOptions.getPathValidator();
        String gcpTempLocation;
        try {
            gcpTempLocation = dataflowOptions.getGcpTempLocation();
        } catch (Exception e) {
            throw new IllegalArgumentException("DataflowRunner requires gcpTempLocation, " + "but failed to retrieve a value from PipelineOptions", e);
        }
        validator.validateOutputFilePrefixSupported(gcpTempLocation);
        String stagingLocation;
        try {
            stagingLocation = dataflowOptions.getStagingLocation();
        } catch (Exception e) {
            throw new IllegalArgumentException("DataflowRunner requires stagingLocation, " + "but failed to retrieve a value from PipelineOptions", e);
        }
        validator.validateOutputFilePrefixSupported(stagingLocation);
        if (!isNullOrEmpty(dataflowOptions.getSaveProfilesToGcs())) {
            validator.validateOutputFilePrefixSupported(dataflowOptions.getSaveProfilesToGcs());
        }
        if (dataflowOptions.getFilesToStage() != null) {
            dataflowOptions.getFilesToStage().stream().forEach(stagedFileSpec -> {
                File localFile;
                if (stagedFileSpec.contains("=")) {
                    String[] components = stagedFileSpec.split("=", 2);
                    localFile = new File(components[1]);
                } else {
                    localFile = new File(stagedFileSpec);
                }
                if (!localFile.exists()) {
                    throw new RuntimeException(String.format("Non-existent files specified in filesToStage: %s", localFile));
                }
            });
        } else {
            dataflowOptions.setFilesToStage(detectClassPathResourcesToStage(DataflowRunner.class.getClassLoader(), options));
            if (dataflowOptions.getFilesToStage().isEmpty()) {
                throw new IllegalArgumentException("No files to stage has been found.");
            } else {
                LOG.info("PipelineOptions.filesToStage was not specified. " + "Defaulting to files from the classpath: will stage {} files. " + "Enable logging at DEBUG level to see which files will be staged.", dataflowOptions.getFilesToStage().size());
                LOG.debug("Classpath elements: {}", dataflowOptions.getFilesToStage());
            }
        }
        String jobName = dataflowOptions.getJobName().toLowerCase();
        checkArgument(jobName.matches("[a-z]([-a-z0-9]*[a-z0-9])?"), "JobName invalid; the name must consist of only the characters " + "[-a-z0-9], starting with a letter and ending with a letter " + "or number");
        if (!jobName.equals(dataflowOptions.getJobName())) {
            LOG.info("PipelineOptions.jobName did not match the service requirements. " + "Using {} instead of {}.", jobName, dataflowOptions.getJobName());
        }
        dataflowOptions.setJobName(jobName);
        String project = dataflowOptions.getProject();
        if (project.matches("[0-9]*")) {
            throw new IllegalArgumentException("Project ID '" + project + "' invalid. Please make sure you specified the Project ID, not project number.");
        } else if (!project.matches(PROJECT_ID_REGEXP)) {
            throw new IllegalArgumentException("Project ID '" + project + "' invalid. Please make sure you specified the Project ID, not project" + " description.");
        }
        DataflowPipelineDebugOptions debugOptions = dataflowOptions.as(DataflowPipelineDebugOptions.class);
        if (debugOptions.getNumberOfWorkerHarnessThreads() < 0) {
            throw new IllegalArgumentException("Number of worker harness threads '" + debugOptions.getNumberOfWorkerHarnessThreads() + "' invalid. Please make sure the value is non-negative.");
        }
        if (dataflowOptions.getRecordJfrOnGcThrashing() && Environments.getJavaVersion() == Environments.JavaVersion.java8) {
            throw new IllegalArgumentException("recordJfrOnGcThrashing is only supported on java 9 and up.");
        }
        if (dataflowOptions.isStreaming() && dataflowOptions.getGcsUploadBufferSizeBytes() == null) {
            dataflowOptions.setGcsUploadBufferSizeBytes(GCS_UPLOAD_BUFFER_SIZE_BYTES_DEFAULT);
        }
        String agentJavaVer = "(JRE 8 environment)";
        if (Environments.getJavaVersion() == Environments.JavaVersion.java17) {
            agentJavaVer = "(JRE 17 environment)";
        } else if (Environments.getJavaVersion() == Environments.JavaVersion.java11) {
            agentJavaVer = "(JRE 11 environment)";
        }
        DataflowRunnerInfo dataflowRunnerInfo = DataflowRunnerInfo.getDataflowRunnerInfo();
        String userAgent = String.format("%s/%s%s", dataflowRunnerInfo.getName(), dataflowRunnerInfo.getVersion(), agentJavaVer).replace(" ", "_");
        dataflowOptions.setUserAgent(userAgent);
        return new DataflowRunner(dataflowOptions);
    }

    static boolean isServiceEndpoint(String endpoint) {
        return Strings.isNullOrEmpty(endpoint) || Pattern.matches(ENDPOINT_REGEXP, endpoint);
    }

    static void validateSdkContainerImageOptions(DataflowPipelineWorkerPoolOptions workerOptions) {
        String sdkContainerOption = workerOptions.getSdkContainerImage();
        String workerHarnessOption = workerOptions.getWorkerHarnessContainerImage();
        Preconditions.checkArgument(sdkContainerOption == null || workerHarnessOption == null || sdkContainerOption.equals(workerHarnessOption), "Cannot use legacy option workerHarnessContainerImage with sdkContainerImage. Prefer sdkContainerImage.");
        String containerImage = workerOptions.getSdkContainerImage();
        if (workerOptions.getWorkerHarnessContainerImage() != null && workerOptions.getSdkContainerImage() == null) {
            LOG.warn("Prefer --sdkContainerImage over deprecated legacy option --workerHarnessContainerImage.");
            containerImage = workerOptions.getWorkerHarnessContainerImage();
        }
        workerOptions.setSdkContainerImage(containerImage);
        workerOptions.setWorkerHarnessContainerImage(containerImage);
    }

    @VisibleForTesting
    static void validateWorkerSettings(DataflowPipelineWorkerPoolOptions workerOptions) {
        DataflowPipelineOptions dataflowOptions = workerOptions.as(DataflowPipelineOptions.class);
        validateSdkContainerImageOptions(workerOptions);
        GcpOptions gcpOptions = workerOptions.as(GcpOptions.class);
        Preconditions.checkArgument(gcpOptions.getZone() == null || gcpOptions.getWorkerRegion() == null, "Cannot use option zone with workerRegion. Prefer either workerZone or workerRegion.");
        Preconditions.checkArgument(gcpOptions.getZone() == null || gcpOptions.getWorkerZone() == null, "Cannot use option zone with workerZone. Prefer workerZone.");
        Preconditions.checkArgument(gcpOptions.getWorkerRegion() == null || gcpOptions.getWorkerZone() == null, "workerRegion and workerZone options are mutually exclusive.");
        boolean hasExperimentWorkerRegion = false;
        if (dataflowOptions.getExperiments() != null) {
            for (String experiment : dataflowOptions.getExperiments()) {
                if (experiment.startsWith("worker_region")) {
                    hasExperimentWorkerRegion = true;
                    break;
                }
            }
        }
        Preconditions.checkArgument(!hasExperimentWorkerRegion || gcpOptions.getWorkerRegion() == null, "Experiment worker_region and option workerRegion are mutually exclusive.");
        Preconditions.checkArgument(!hasExperimentWorkerRegion || gcpOptions.getWorkerZone() == null, "Experiment worker_region and option workerZone are mutually exclusive.");
        if (gcpOptions.getZone() != null) {
            LOG.warn("Option --zone is deprecated. Please use --workerZone instead.");
            gcpOptions.setWorkerZone(gcpOptions.getZone());
            gcpOptions.setZone(null);
        }
    }

    @VisibleForTesting
    protected DataflowRunner(DataflowPipelineOptions options) {
        this.options = options;
        this.dataflowClient = DataflowClient.create(options);
        this.translator = DataflowPipelineTranslator.fromOptions(options);
        this.pcollectionsRequiringIndexedFormat = new HashSet<>();
        this.pCollectionsPreservedKeys = new HashSet<>();
        this.pcollectionsRequiringAutoSharding = new HashSet<>();
        this.ptransformViewsWithNonDeterministicKeyCoders = new HashSet<>();
    }

    private List<PTransformOverride> getOverrides(boolean streaming) {
        ImmutableList.Builder<PTransformOverride> overridesBuilder = ImmutableList.builder();
        overridesBuilder.add(PTransformOverride.of(PTransformMatchers.flattenWithDuplicateInputs(), DeduplicatedFlattenFactory.create())).add(PTransformOverride.of(PTransformMatchers.emptyFlatten(), EmptyFlattenAsCreateFactory.instance()));
        overridesBuilder.add(PTransformOverride.of(PTransformMatchers.splittableParDoSingle(), new ReflectiveOneToOneOverrideFactory(SplittableParDoOverrides.ParDoSingleViaMulti.class, this))).add(PTransformOverride.of(PTransformMatchers.splittableParDoMulti(), new SplittableParDoOverrides.SplittableParDoOverrideFactory()));
        if (streaming) {
            if (!hasExperiment(options, "enable_custom_pubsub_source")) {
                overridesBuilder.add(PTransformOverride.of(PTransformMatchers.classEqualTo(PubsubUnboundedSource.class), new StreamingPubsubIOReadOverrideFactory()));
            }
            if (!hasExperiment(options, "enable_custom_pubsub_sink")) {
                overridesBuilder.add(PTransformOverride.of(PTransformMatchers.classEqualTo(PubsubUnboundedSink.class), new StreamingPubsubIOWriteOverrideFactory(this)));
            }
            overridesBuilder.add(KafkaIO.Read.KAFKA_READ_OVERRIDE);
            if (!hasExperiment(options, "enable_file_dynamic_sharding")) {
                overridesBuilder.add(PTransformOverride.of(PTransformMatchers.writeWithRunnerDeterminedSharding(), new StreamingShardedWriteFactory(options)));
            }
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.groupIntoBatches(), new GroupIntoBatchesOverride.StreamingGroupIntoBatchesOverrideFactory(this)));
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.groupWithShardableStates(), new GroupIntoBatchesOverride.StreamingGroupIntoBatchesWithShardedKeyOverrideFactory(this)));
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.classEqualTo(Read.Bounded.class), new StreamingBoundedReadOverrideFactory())).add(PTransformOverride.of(PTransformMatchers.classEqualTo(Read.Unbounded.class), new StreamingUnboundedReadOverrideFactory()));
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.classEqualTo(View.CreatePCollectionView.class), new StreamingCreatePCollectionViewFactory()));
        } else {
            overridesBuilder.add(SplittableParDo.PRIMITIVE_BOUNDED_READ_OVERRIDE);
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.classEqualTo(GroupIntoBatches.class), new GroupIntoBatchesOverride.BatchGroupIntoBatchesOverrideFactory<>(this))).add(PTransformOverride.of(PTransformMatchers.classEqualTo(GroupIntoBatches.WithShardedKey.class), new GroupIntoBatchesOverride.BatchGroupIntoBatchesWithShardedKeyOverrideFactory<>(this)));
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.stateOrTimerParDoMulti(), BatchStatefulParDoOverrides.multiOutputOverrideFactory(options))).add(PTransformOverride.of(PTransformMatchers.stateOrTimerParDoSingle(), BatchStatefulParDoOverrides.singleOutputOverrideFactory()));
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.splittableProcessKeyedBounded(), new SplittableParDoNaiveBounded.OverrideFactory()));
            overridesBuilder.add(PTransformOverride.of(PTransformMatchers.classEqualTo(View.AsMap.class), new ReflectiveViewOverrideFactory(BatchViewOverrides.BatchViewAsMap.class, this))).add(PTransformOverride.of(PTransformMatchers.classEqualTo(View.AsMultimap.class), new ReflectiveViewOverrideFactory(BatchViewOverrides.BatchViewAsMultimap.class, this))).add(PTransformOverride.of(PTransformMatchers.classEqualTo(Combine.GloballyAsSingletonView.class), new CombineGloballyAsSingletonViewOverrideFactory(this))).add(PTransformOverride.of(PTransformMatchers.classEqualTo(View.AsList.class), new ReflectiveViewOverrideFactory(BatchViewOverrides.BatchViewAsList.class, this))).add(PTransformOverride.of(PTransformMatchers.classEqualTo(View.AsIterable.class), new ReflectiveViewOverrideFactory(BatchViewOverrides.BatchViewAsIterable.class, this)));
        }
        overridesBuilder.add(PTransformOverride.of(PTransformMatchers.classEqualTo(Reshuffle.class), new ReshuffleOverrideFactory())).add(PTransformOverride.of(new DataflowPTransformMatchers.CombineValuesWithoutSideInputsPTransformMatcher(), new PrimitiveCombineGroupedValuesOverrideFactory())).add(PTransformOverride.of(PTransformMatchers.classEqualTo(ParDo.SingleOutput.class), new PrimitiveParDoSingleFactory()));
        return overridesBuilder.build();
    }

    private static class CombineGloballyAsSingletonViewOverrideFactory<InputT, ViewT> extends ReflectiveViewOverrideFactory<InputT, ViewT> {

        private CombineGloballyAsSingletonViewOverrideFactory(DataflowRunner runner) {
            super((Class) BatchViewOverrides.BatchViewAsSingleton.class, runner);
        }

        @Override
        public PTransformReplacement<PCollection<InputT>, PCollectionView<ViewT>> getReplacementTransform(AppliedPTransform<PCollection<InputT>, PCollectionView<ViewT>, PTransform<PCollection<InputT>, PCollectionView<ViewT>>> transform) {
            Combine.GloballyAsSingletonView<?, ?> combineTransform = (Combine.GloballyAsSingletonView) transform.getTransform();
            return PTransformReplacement.of(PTransformReplacements.getSingletonMainInput(transform), new BatchViewOverrides.BatchViewAsSingleton(runner, findCreatePCollectionView(transform), (CombineFn) combineTransform.getCombineFn(), combineTransform.getFanout()));
        }
    }

    private static class ReflectiveViewOverrideFactory<InputT, ViewT> implements PTransformOverrideFactory<PCollection<InputT>, PCollectionView<ViewT>, PTransform<PCollection<InputT>, PCollectionView<ViewT>>> {

        final Class<PTransform<PCollection<InputT>, PCollectionView<ViewT>>> replacement;

        final DataflowRunner runner;

        private ReflectiveViewOverrideFactory(Class<PTransform<PCollection<InputT>, PCollectionView<ViewT>>> replacement, DataflowRunner runner) {
            this.replacement = replacement;
            this.runner = runner;
        }

        CreatePCollectionView<ViewT, ViewT> findCreatePCollectionView(final AppliedPTransform<PCollection<InputT>, PCollectionView<ViewT>, PTransform<PCollection<InputT>, PCollectionView<ViewT>>> transform) {
            final AtomicReference<CreatePCollectionView> viewTransformRef = new AtomicReference<>();
            transform.getPipeline().traverseTopologically(new PipelineVisitor.Defaults() {

                private boolean tracking = false;

                @Override
                public CompositeBehavior enterCompositeTransform(Node node) {
                    if (transform.getTransform() == node.getTransform()) {
                        tracking = true;
                    }
                    return super.enterCompositeTransform(node);
                }

                @Override
                public void visitPrimitiveTransform(Node node) {
                    if (tracking && node.getTransform() instanceof CreatePCollectionView) {
                        checkState(viewTransformRef.compareAndSet(null, (CreatePCollectionView) node.getTransform()), "Found more than one instance of a CreatePCollectionView when " + "attempting to replace %s, found [%s, %s]", replacement, viewTransformRef.get(), node.getTransform());
                    }
                }

                @Override
                public void leaveCompositeTransform(Node node) {
                    if (transform.getTransform() == node.getTransform()) {
                        tracking = false;
                    }
                }
            });
            checkState(viewTransformRef.get() != null, "Expected to find CreatePCollectionView contained within %s", transform.getTransform());
            return viewTransformRef.get();
        }

        @Override
        public PTransformReplacement<PCollection<InputT>, PCollectionView<ViewT>> getReplacementTransform(final AppliedPTransform<PCollection<InputT>, PCollectionView<ViewT>, PTransform<PCollection<InputT>, PCollectionView<ViewT>>> transform) {
            PTransform<PCollection<InputT>, PCollectionView<ViewT>> rep = InstanceBuilder.ofType(replacement).withArg(DataflowRunner.class, runner).withArg(CreatePCollectionView.class, findCreatePCollectionView(transform)).build();
            return PTransformReplacement.of(PTransformReplacements.getSingletonMainInput(transform), (PTransform) rep);
        }

        @Override
        public Map<PCollection<?>, ReplacementOutput> mapOutputs(Map<TupleTag<?>, PCollection<?>> outputs, PCollectionView<ViewT> newOutput) {
            return ReplacementOutputs.singleton(outputs, newOutput);
        }
    }

    private static class ReflectiveOneToOneOverrideFactory<InputT, OutputT, TransformT extends PTransform<PCollection<InputT>, PCollection<OutputT>>> extends SingleInputOutputOverrideFactory<PCollection<InputT>, PCollection<OutputT>, TransformT> {

        private final Class<PTransform<PCollection<InputT>, PCollection<OutputT>>> replacement;

        private final DataflowRunner runner;

        private ReflectiveOneToOneOverrideFactory(Class<PTransform<PCollection<InputT>, PCollection<OutputT>>> replacement, DataflowRunner runner) {
            this.replacement = replacement;
            this.runner = runner;
        }

        @Override
        public PTransformReplacement<PCollection<InputT>, PCollection<OutputT>> getReplacementTransform(AppliedPTransform<PCollection<InputT>, PCollection<OutputT>, TransformT> transform) {
            PTransform<PCollection<InputT>, PCollection<OutputT>> rep = InstanceBuilder.ofType(replacement).withArg(DataflowRunner.class, runner).withArg((Class<TransformT>) transform.getTransform().getClass(), transform.getTransform()).build();
            return PTransformReplacement.of(PTransformReplacements.getSingletonMainInput(transform), rep);
        }
    }

    private String debuggerMessage(String projectId, String uniquifier) {
        return String.format("To debug your job, visit Google Cloud Debugger at: " + "https://console.developers.google.com/debug?project=%s&dbgee=%s", projectId, uniquifier);
    }

    private void maybeRegisterDebuggee(DataflowPipelineOptions options, String uniquifier) {
        if (!options.getEnableCloudDebugger()) {
            return;
        }
        if (options.getDebuggee() != null) {
            throw new RuntimeException("Should not specify the debuggee");
        }
        CloudDebugger debuggerClient = DataflowTransport.newClouddebuggerClient(options).build();
        Debuggee debuggee = registerDebuggee(debuggerClient, uniquifier);
        options.setDebuggee(debuggee);
        System.out.println(debuggerMessage(options.getProject(), debuggee.getUniquifier()));
    }

    private Debuggee registerDebuggee(CloudDebugger debuggerClient, String uniquifier) {
        RegisterDebuggeeRequest registerReq = new RegisterDebuggeeRequest();
        registerReq.setDebuggee(new Debuggee().setProject(options.getProject()).setUniquifier(uniquifier).setDescription(uniquifier).setAgentVersion("google.com/cloud-dataflow-java/v1"));
        try {
            RegisterDebuggeeResponse registerResponse = debuggerClient.controller().debuggees().register(registerReq).execute();
            Debuggee debuggee = registerResponse.getDebuggee();
            if (debuggee.getStatus() != null && debuggee.getStatus().getIsError()) {
                throw new RuntimeException("Unable to register with the debugger: " + debuggee.getStatus().getDescription().getFormat());
            }
            return debuggee;
        } catch (IOException e) {
            throw new RuntimeException("Unable to register with the debugger: ", e);
        }
    }

    protected RunnerApi.Pipeline applySdkEnvironmentOverrides(RunnerApi.Pipeline pipeline, DataflowPipelineDebugOptions options) {
        String sdkHarnessContainerImageOverrides = options.getSdkHarnessContainerImageOverrides();
        if (Strings.isNullOrEmpty(sdkHarnessContainerImageOverrides)) {
            return pipeline;
        }
        String[] overrides = sdkHarnessContainerImageOverrides.split(",", -1);
        if (overrides.length % 2 != 0) {
            throw new RuntimeException("invalid syntax for SdkHarnessContainerImageOverrides: " + options.getSdkHarnessContainerImageOverrides());
        }
        RunnerApi.Pipeline.Builder pipelineBuilder = pipeline.toBuilder();
        RunnerApi.Components.Builder componentsBuilder = pipelineBuilder.getComponentsBuilder();
        componentsBuilder.clearEnvironments();
        for (Map.Entry<String, RunnerApi.Environment> entry : pipeline.getComponents().getEnvironmentsMap().entrySet()) {
            RunnerApi.Environment.Builder environmentBuilder = entry.getValue().toBuilder();
            if (BeamUrns.getUrn(RunnerApi.StandardEnvironments.Environments.DOCKER).equals(environmentBuilder.getUrn())) {
                RunnerApi.DockerPayload dockerPayload;
                try {
                    dockerPayload = RunnerApi.DockerPayload.parseFrom(environmentBuilder.getPayload());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException("Error parsing environment docker payload.", e);
                }
                String containerImage = dockerPayload.getContainerImage();
                for (int i = 0; i < overrides.length; i += 2) {
                    containerImage = containerImage.replaceAll(overrides[i], overrides[i + 1]);
                }
                environmentBuilder.setPayload(RunnerApi.DockerPayload.newBuilder().setContainerImage(containerImage).build().toByteString());
            }
            componentsBuilder.putEnvironments(entry.getKey(), environmentBuilder.build());
        }
        return pipelineBuilder.build();
    }

    @VisibleForTesting
    protected RunnerApi.Pipeline resolveArtifacts(RunnerApi.Pipeline pipeline) {
        RunnerApi.Pipeline.Builder pipelineBuilder = pipeline.toBuilder();
        RunnerApi.Components.Builder componentsBuilder = pipelineBuilder.getComponentsBuilder();
        componentsBuilder.clearEnvironments();
        for (Map.Entry<String, RunnerApi.Environment> entry : pipeline.getComponents().getEnvironmentsMap().entrySet()) {
            RunnerApi.Environment.Builder environmentBuilder = entry.getValue().toBuilder();
            environmentBuilder.clearDependencies();
            for (RunnerApi.ArtifactInformation info : entry.getValue().getDependenciesList()) {
                if (!BeamUrns.getUrn(RunnerApi.StandardArtifacts.Types.FILE).equals(info.getTypeUrn())) {
                    throw new RuntimeException(String.format("unsupported artifact type %s", info.getTypeUrn()));
                }
                RunnerApi.ArtifactFilePayload filePayload;
                try {
                    filePayload = RunnerApi.ArtifactFilePayload.parseFrom(info.getTypePayload());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException("Error parsing artifact file payload.", e);
                }
                String stagedName;
                if (BeamUrns.getUrn(RunnerApi.StandardArtifacts.Roles.STAGING_TO).equals(info.getRoleUrn())) {
                    try {
                        RunnerApi.ArtifactStagingToRolePayload stagingPayload = RunnerApi.ArtifactStagingToRolePayload.parseFrom(info.getRolePayload());
                        stagedName = stagingPayload.getStagedName();
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException("Error parsing artifact staging_to role payload.", e);
                    }
                } else {
                    try {
                        File source = new File(filePayload.getPath());
                        HashCode hashCode = Files.asByteSource(source).hash(Hashing.sha256());
                        stagedName = Environments.createStagingFileName(source, hashCode);
                    } catch (IOException e) {
                        throw new RuntimeException(String.format("Error creating staged name for artifact %s", filePayload.getPath()), e);
                    }
                }
                environmentBuilder.addDependencies(info.toBuilder().setTypeUrn(BeamUrns.getUrn(RunnerApi.StandardArtifacts.Types.URL)).setTypePayload(RunnerApi.ArtifactUrlPayload.newBuilder().setUrl(FileSystems.matchNewResource(options.getStagingLocation(), true).resolve(stagedName, ResolveOptions.StandardResolveOptions.RESOLVE_FILE).toString()).setSha256(filePayload.getSha256()).build().toByteString()));
            }
            componentsBuilder.putEnvironments(entry.getKey(), environmentBuilder.build());
        }
        return pipelineBuilder.build();
    }

    protected List<DataflowPackage> stageArtifacts(RunnerApi.Pipeline pipeline) {
        ImmutableList.Builder<StagedFile> filesToStageBuilder = ImmutableList.builder();
        Set<String> stagedNames = new HashSet<>();
        for (Map.Entry<String, RunnerApi.Environment> entry : pipeline.getComponents().getEnvironmentsMap().entrySet()) {
            for (RunnerApi.ArtifactInformation info : entry.getValue().getDependenciesList()) {
                if (!BeamUrns.getUrn(RunnerApi.StandardArtifacts.Types.FILE).equals(info.getTypeUrn())) {
                    throw new RuntimeException(String.format("unsupported artifact type %s", info.getTypeUrn()));
                }
                RunnerApi.ArtifactFilePayload filePayload;
                try {
                    filePayload = RunnerApi.ArtifactFilePayload.parseFrom(info.getTypePayload());
                } catch (InvalidProtocolBufferException e) {
                    throw new RuntimeException("Error parsing artifact file payload.", e);
                }
                String stagedName;
                if (BeamUrns.getUrn(RunnerApi.StandardArtifacts.Roles.STAGING_TO).equals(info.getRoleUrn())) {
                    try {
                        RunnerApi.ArtifactStagingToRolePayload stagingPayload = RunnerApi.ArtifactStagingToRolePayload.parseFrom(info.getRolePayload());
                        stagedName = stagingPayload.getStagedName();
                    } catch (InvalidProtocolBufferException e) {
                        throw new RuntimeException("Error parsing artifact staging_to role payload.", e);
                    }
                } else {
                    try {
                        File source = new File(filePayload.getPath());
                        HashCode hashCode = Files.asByteSource(source).hash(Hashing.sha256());
                        stagedName = Environments.createStagingFileName(source, hashCode);
                    } catch (IOException e) {
                        throw new RuntimeException(String.format("Error creating staged name for artifact %s", filePayload.getPath()), e);
                    }
                }
                if (stagedNames.contains(stagedName)) {
                    continue;
                } else {
                    stagedNames.add(stagedName);
                }
                filesToStageBuilder.add(StagedFile.of(filePayload.getPath(), filePayload.getSha256(), stagedName));
            }
        }
        return options.getStager().stageFiles(filesToStageBuilder.build());
    }

    private List<RunnerApi.ArtifactInformation> getDefaultArtifacts() {
        ImmutableList.Builder<String> pathsToStageBuilder = ImmutableList.builder();
        String windmillBinary = options.as(DataflowPipelineDebugOptions.class).getOverrideWindmillBinary();
        String dataflowWorkerJar = options.getDataflowWorkerJar();
        if (dataflowWorkerJar != null && !dataflowWorkerJar.isEmpty() && !useUnifiedWorker(options)) {
            pathsToStageBuilder.add("dataflow-worker.jar=" + dataflowWorkerJar);
        }
        pathsToStageBuilder.addAll(options.getFilesToStage());
        if (windmillBinary != null) {
            pathsToStageBuilder.add("windmill_main=" + windmillBinary);
        }
        return Environments.getArtifacts(pathsToStageBuilder.build());
    }

    @Override
    public DataflowPipelineJob run(Pipeline pipeline) {
        if (useUnifiedWorker(options)) {
            List<String> experiments = options.getExperiments();
            if (!experiments.contains("use_runner_v2")) {
                experiments.add("use_runner_v2");
            }
            if (!experiments.contains("use_unified_worker")) {
                experiments.add("use_unified_worker");
            }
            if (!experiments.contains("beam_fn_api")) {
                experiments.add("beam_fn_api");
            }
            if (!experiments.contains("use_portable_job_submission")) {
                experiments.add("use_portable_job_submission");
            }
            options.setExperiments(ImmutableList.copyOf(experiments));
        }
        logWarningIfPCollectionViewHasNonDeterministicKeyCoder(pipeline);
        if (containsUnboundedPCollection(pipeline)) {
            options.setStreaming(true);
        }
        if (!options.isStreaming() && !ExperimentalOptions.hasExperiment(options, "disable_projection_pushdown")) {
            ProjectionPushdownOptimizer.optimize(pipeline);
        }
        LOG.info("Executing pipeline on the Dataflow Service, which will have billing implications " + "related to Google Compute Engine usage and other Google Cloud Services.");
        DataflowPipelineOptions dataflowOptions = options.as(DataflowPipelineOptions.class);
        String workerHarnessContainerImageURL = DataflowRunner.getContainerImageForJob(dataflowOptions);
        RunnerApi.Environment defaultEnvironmentForDataflow = Environments.createDockerEnvironment(workerHarnessContainerImageURL);
        SdkComponents portableComponents = SdkComponents.create();
        portableComponents.registerEnvironment(defaultEnvironmentForDataflow.toBuilder().addAllDependencies(getDefaultArtifacts()).addAllCapabilities(Environments.getJavaCapabilities()).build());
        RunnerApi.Pipeline portablePipelineProto = PipelineTranslation.toProto(pipeline, portableComponents, false);
        List<DataflowPackage> packages = stageArtifacts(portablePipelineProto);
        portablePipelineProto = resolveArtifacts(portablePipelineProto);
        portablePipelineProto = applySdkEnvironmentOverrides(portablePipelineProto, options);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Portable pipeline proto:\n{}", TextFormat.printer().printToString(portablePipelineProto));
        }
        LOG.info("Staging portable pipeline proto to {}", options.getStagingLocation());
        byte[] serializedProtoPipeline = portablePipelineProto.toByteArray();
        DataflowPackage stagedPipeline = options.getStager().stageToFile(serializedProtoPipeline, PIPELINE_FILE_NAME);
        dataflowOptions.setPipelineUrl(stagedPipeline.getLocation());
        replaceV1Transforms(pipeline);
        SdkComponents dataflowV1Components = SdkComponents.create();
        dataflowV1Components.registerEnvironment(defaultEnvironmentForDataflow.toBuilder().addAllDependencies(getDefaultArtifacts()).addAllCapabilities(Environments.getJavaCapabilities()).build());
        RunnerApi.Pipeline dataflowV1PipelineProto = PipelineTranslation.toProto(pipeline, dataflowV1Components, true);
        if (LOG.isDebugEnabled()) {
            LOG.debug("Dataflow v1 pipeline proto:\n{}", TextFormat.printer().printToString(dataflowV1PipelineProto));
        }
        int randomNum = new Random().nextInt(9000) + 1000;
        String requestId = DateTimeFormat.forPattern("YYYYMMddHHmmssmmm").withZone(DateTimeZone.UTC).print(DateTimeUtils.currentTimeMillis()) + "_" + randomNum;
        maybeRegisterDebuggee(dataflowOptions, requestId);
        JobSpecification jobSpecification = translator.translate(pipeline, dataflowV1PipelineProto, dataflowV1Components, this, packages);
        if (!isNullOrEmpty(dataflowOptions.getDataflowWorkerJar()) && !useUnifiedWorker(options)) {
            List<String> experiments = firstNonNull(dataflowOptions.getExperiments(), Collections.emptyList());
            if (!experiments.contains("use_staged_dataflow_worker_jar")) {
                dataflowOptions.setExperiments(ImmutableList.<String>builder().addAll(experiments).add("use_staged_dataflow_worker_jar").build());
            }
        }
        Job newJob = jobSpecification.getJob();
        try {
            newJob.getEnvironment().setSdkPipelineOptions(MAPPER.readValue(MAPPER_WITH_MODULES.writeValueAsBytes(options), Map.class));
        } catch (IOException e) {
            throw new IllegalArgumentException("PipelineOptions specified failed to serialize to JSON.", e);
        }
        newJob.setClientRequestId(requestId);
        DataflowRunnerInfo dataflowRunnerInfo = DataflowRunnerInfo.getDataflowRunnerInfo();
        String version = dataflowRunnerInfo.getVersion();
        checkState(!"${pom.version}".equals(version), "Unable to submit a job to the Dataflow service with unset version ${pom.version}");
        LOG.info("Dataflow SDK version: {}", version);
        newJob.getEnvironment().setUserAgent((Map) dataflowRunnerInfo.getProperties());
        if (!isNullOrEmpty(options.getGcpTempLocation())) {
            newJob.getEnvironment().setTempStoragePrefix(dataflowOptions.getPathValidator().verifyPath(options.getGcpTempLocation()));
        }
        newJob.getEnvironment().setDataset(options.getTempDatasetId());
        if (options.getWorkerRegion() != null) {
            newJob.getEnvironment().setWorkerRegion(options.getWorkerRegion());
        }
        if (options.getWorkerZone() != null) {
            newJob.getEnvironment().setWorkerZone(options.getWorkerZone());
        }
        if (options.getFlexRSGoal() == DataflowPipelineOptions.FlexResourceSchedulingGoal.COST_OPTIMIZED) {
            newJob.getEnvironment().setFlexResourceSchedulingGoal("FLEXRS_COST_OPTIMIZED");
        } else if (options.getFlexRSGoal() == DataflowPipelineOptions.FlexResourceSchedulingGoal.SPEED_OPTIMIZED) {
            newJob.getEnvironment().setFlexResourceSchedulingGoal("FLEXRS_SPEED_OPTIMIZED");
        }
        if (!isNullOrEmpty(dataflowOptions.getMinCpuPlatform())) {
            List<String> experiments = firstNonNull(dataflowOptions.getExperiments(), Collections.emptyList());
            List<String> minCpuFlags = experiments.stream().filter(p -> p.startsWith("min_cpu_platform")).collect(Collectors.toList());
            if (minCpuFlags.isEmpty()) {
                dataflowOptions.setExperiments(ImmutableList.<String>builder().addAll(experiments).add("min_cpu_platform=" + dataflowOptions.getMinCpuPlatform()).build());
            } else {
                LOG.warn("Flag min_cpu_platform is defined in both top level PipelineOption, " + "as well as under experiments. Proceed using {}.", minCpuFlags.get(0));
            }
        }
        newJob.getEnvironment().setExperiments(ImmutableList.copyOf(firstNonNull(dataflowOptions.getExperiments(), Collections.emptyList())));
        String containerImage = getContainerImageForJob(options);
        for (WorkerPool workerPool : newJob.getEnvironment().getWorkerPools()) {
            workerPool.setWorkerHarnessContainerImage(containerImage);
        }
        configureSdkHarnessContainerImages(options, portablePipelineProto, newJob);
        newJob.getEnvironment().setVersion(getEnvironmentVersion(options));
        if (hooks != null) {
            hooks.modifyEnvironmentBeforeSubmission(newJob.getEnvironment());
        }
        if (hasExperiment(options, "upload_graph")) {
            DataflowPackage stagedGraph = options.getStager().stageToFile(DataflowPipelineTranslator.jobToString(newJob).getBytes(UTF_8), DATAFLOW_GRAPH_FILE_NAME);
            newJob.getSteps().clear();
            newJob.setStepsLocation(stagedGraph.getLocation());
        }
        if (!isNullOrEmpty(options.getDataflowJobFile()) || !isNullOrEmpty(options.getTemplateLocation())) {
            boolean isTemplate = !isNullOrEmpty(options.getTemplateLocation());
            if (isTemplate) {
                checkArgument(isNullOrEmpty(options.getDataflowJobFile()), "--dataflowJobFile and --templateLocation are mutually exclusive.");
            }
            String fileLocation = firstNonNull(options.getTemplateLocation(), options.getDataflowJobFile());
            checkArgument(fileLocation.startsWith("/") || fileLocation.startsWith("gs://"), "Location must be local or on Cloud Storage, got %s.", fileLocation);
            ResourceId fileResource = FileSystems.matchNewResource(fileLocation, false);
            String workSpecJson = DataflowPipelineTranslator.jobToString(newJob);
            try (PrintWriter printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(Channels.newOutputStream(FileSystems.create(fileResource, MimeTypes.TEXT)), UTF_8)))) {
                printWriter.print(workSpecJson);
                LOG.info("Printed job specification to {}", fileLocation);
            } catch (IOException ex) {
                String error = String.format("Cannot create output file at %s", fileLocation);
                if (isTemplate) {
                    throw new RuntimeException(error, ex);
                } else {
                    LOG.warn(error, ex);
                }
            }
            if (isTemplate) {
                LOG.info("Template successfully created.");
                return new DataflowTemplateJob();
            }
        }
        String jobIdToUpdate = null;
        if (options.isUpdate()) {
            jobIdToUpdate = getJobIdFromName(options.getJobName());
            newJob.setTransformNameMapping(options.getTransformNameMapping());
            newJob.setReplaceJobId(jobIdToUpdate);
        }
        if (options.getCreateFromSnapshot() != null && !options.getCreateFromSnapshot().isEmpty()) {
            newJob.setCreatedFromSnapshotId(options.getCreateFromSnapshot());
        }
        Job jobResult;
        try {
            jobResult = dataflowClient.createJob(newJob);
        } catch (GoogleJsonResponseException e) {
            String errorMessages = "Unexpected errors";
            if (e.getDetails() != null) {
                if (Utf8.encodedLength(newJob.toString()) >= CREATE_JOB_REQUEST_LIMIT_BYTES) {
                    errorMessages = "The size of the serialized JSON representation of the pipeline " + "exceeds the allowable limit. " + "For more information, please see the documentation on job submission:\n" + "https://cloud.google.com/dataflow/docs/guides/deploying-a-pipeline#jobs";
                } else {
                    errorMessages = e.getDetails().getMessage();
                }
            }
            throw new RuntimeException("Failed to create a workflow job: " + errorMessages, e);
        } catch (IOException e) {
            throw new RuntimeException("Failed to create a workflow job", e);
        }
        DataflowPipelineJob dataflowPipelineJob = new DataflowPipelineJob(DataflowClient.create(options), jobResult.getId(), options, jobSpecification != null ? jobSpecification.getStepNames() : Collections.emptyMap(), portablePipelineProto);
        if (jobResult.getClientRequestId() != null && !jobResult.getClientRequestId().isEmpty() && !jobResult.getClientRequestId().equals(requestId)) {
            if (options.isUpdate()) {
                throw new DataflowJobAlreadyUpdatedException(dataflowPipelineJob, String.format("The job named %s with id: %s has already been updated into job id: %s " + "and cannot be updated again.", newJob.getName(), jobIdToUpdate, jobResult.getId()));
            } else {
                throw new DataflowJobAlreadyExistsException(dataflowPipelineJob, String.format("There is already an active job named %s with id: %s. If you want to submit a" + " second job, try again by setting a different name using --jobName.", newJob.getName(), jobResult.getId()));
            }
        }
        LOG.info("To access the Dataflow monitoring console, please navigate to {}", MonitoringUtil.getJobMonitoringPageURL(options.getProject(), options.getRegion(), jobResult.getId()));
        LOG.info("Submitted job: {}", jobResult.getId());
        LOG.info("To cancel the job using the 'gcloud' tool, run:\n> {}", MonitoringUtil.getGcloudCancelCommand(options, jobResult.getId()));
        return dataflowPipelineJob;
    }

    private static String getContainerImageFromEnvironmentId(String environmentId, RunnerApi.Pipeline pipelineProto) {
        RunnerApi.Environment environment = pipelineProto.getComponents().getEnvironmentsMap().get(environmentId);
        if (!BeamUrns.getUrn(RunnerApi.StandardEnvironments.Environments.DOCKER).equals(environment.getUrn())) {
            throw new RuntimeException("Dataflow can only execute pipeline steps in Docker environments: " + environment.getUrn());
        }
        RunnerApi.DockerPayload dockerPayload;
        try {
            dockerPayload = RunnerApi.DockerPayload.parseFrom(environment.getPayload());
        } catch (InvalidProtocolBufferException e) {
            throw new RuntimeException("Error parsing docker payload.", e);
        }
        return dockerPayload.getContainerImage();
    }

    @AutoValue
    abstract static class EnvironmentInfo {

        static EnvironmentInfo create(String environmentId, String containerUrl) {
            return new AutoValue_DataflowRunner_EnvironmentInfo(environmentId, containerUrl);
        }

        abstract String environmentId();

        abstract String containerUrl();
    }

    private static List<EnvironmentInfo> getAllEnvironmentInfo(RunnerApi.Pipeline pipelineProto) {
        return pipelineProto.getComponents().getTransformsMap().values().stream().map(transform -> transform.getEnvironmentId()).filter(environmentId -> !environmentId.isEmpty()).distinct().map(environmentId -> EnvironmentInfo.create(environmentId, getContainerImageFromEnvironmentId(environmentId, pipelineProto))).collect(Collectors.toList());
    }

    static void configureSdkHarnessContainerImages(DataflowPipelineOptions options, RunnerApi.Pipeline pipelineProto, Job newJob) {
        if (useUnifiedWorker(options)) {
            List<SdkHarnessContainerImage> sdkContainerList = getAllEnvironmentInfo(pipelineProto).stream().map(environmentInfo -> {
                SdkHarnessContainerImage image = new SdkHarnessContainerImage();
                image.setEnvironmentId(environmentInfo.environmentId());
                image.setContainerImage(environmentInfo.containerUrl());
                if (environmentInfo.containerUrl().toLowerCase().contains("python")) {
                    image.setUseSingleCorePerContainer(true);
                }
                return image;
            }).collect(Collectors.toList());
            for (WorkerPool workerPool : newJob.getEnvironment().getWorkerPools()) {
                workerPool.setSdkHarnessContainerImages(sdkContainerList);
            }
        }
    }

    public static boolean hasExperiment(DataflowPipelineDebugOptions options, String experiment) {
        List<String> experiments = firstNonNull(options.getExperiments(), Collections.<String>emptyList());
        return experiments.contains(experiment);
    }

    private static Map<String, Object> getEnvironmentVersion(DataflowPipelineOptions options) {
        DataflowRunnerInfo runnerInfo = DataflowRunnerInfo.getDataflowRunnerInfo();
        String majorVersion;
        String jobType;
        if (useUnifiedWorker(options)) {
            majorVersion = runnerInfo.getFnApiEnvironmentMajorVersion();
            jobType = options.isStreaming() ? "FNAPI_STREAMING" : "FNAPI_BATCH";
        } else {
            majorVersion = runnerInfo.getLegacyEnvironmentMajorVersion();
            jobType = options.isStreaming() ? "STREAMING" : "JAVA_BATCH_AUTOSCALING";
        }
        return ImmutableMap.of(PropertyNames.ENVIRONMENT_VERSION_MAJOR_KEY, majorVersion, PropertyNames.ENVIRONMENT_VERSION_JOB_TYPE_KEY, jobType);
    }

    @VisibleForTesting
    protected void replaceV1Transforms(Pipeline pipeline) {
        boolean streaming = options.isStreaming() || containsUnboundedPCollection(pipeline);
        UnconsumedReads.ensureAllReadsConsumed(pipeline);
        pipeline.replaceAll(getOverrides(streaming));
    }

    private boolean containsUnboundedPCollection(Pipeline p) {
        class BoundednessVisitor extends PipelineVisitor.Defaults {

            IsBounded boundedness = IsBounded.BOUNDED;

            @Override
            public void visitValue(PValue value, Node producer) {
                if (value instanceof PCollection) {
                    boundedness = boundedness.and(((PCollection) value).isBounded());
                }
            }
        }
        BoundednessVisitor visitor = new BoundednessVisitor();
        p.traverseTopologically(visitor);
        return visitor.boundedness == IsBounded.UNBOUNDED;
    }

    public DataflowPipelineTranslator getTranslator() {
        return translator;
    }

    @Experimental
    public void setHooks(DataflowRunnerHooks hooks) {
        this.hooks = hooks;
    }

    private void logWarningIfPCollectionViewHasNonDeterministicKeyCoder(Pipeline pipeline) {
        if (!ptransformViewsWithNonDeterministicKeyCoders.isEmpty()) {
            final SortedSet<String> ptransformViewNamesWithNonDeterministicKeyCoders = new TreeSet<>();
            pipeline.traverseTopologically(new PipelineVisitor.Defaults() {

                @Override
                public void visitValue(PValue value, TransformHierarchy.Node producer) {
                }

                @Override
                public void visitPrimitiveTransform(TransformHierarchy.Node node) {
                    if (ptransformViewsWithNonDeterministicKeyCoders.contains(node.getTransform())) {
                        ptransformViewNamesWithNonDeterministicKeyCoders.add(node.getFullName());
                    }
                }

                @Override
                public CompositeBehavior enterCompositeTransform(TransformHierarchy.Node node) {
                    if (node.getTransform() instanceof View.AsMap || node.getTransform() instanceof View.AsMultimap) {
                        PCollection<KV<?, ?>> input = (PCollection<KV<?, ?>>) Iterables.getOnlyElement(node.getInputs().values());
                        KvCoder<?, ?> inputCoder = (KvCoder) input.getCoder();
                        try {
                            inputCoder.getKeyCoder().verifyDeterministic();
                        } catch (NonDeterministicException e) {
                            ptransformViewNamesWithNonDeterministicKeyCoders.add(node.getFullName());
                        }
                    }
                    if (ptransformViewsWithNonDeterministicKeyCoders.contains(node.getTransform())) {
                        ptransformViewNamesWithNonDeterministicKeyCoders.add(node.getFullName());
                    }
                    return CompositeBehavior.ENTER_TRANSFORM;
                }

                @Override
                public void leaveCompositeTransform(TransformHierarchy.Node node) {
                }
            });
            LOG.warn("Unable to use indexed implementation for View.AsMap and View.AsMultimap for {} because" + " the key coder is not deterministic. Falling back to singleton implementation" + " which may cause memory and/or performance problems. Future major versions of" + " Dataflow will require deterministic key coders.", ptransformViewNamesWithNonDeterministicKeyCoders);
        }
    }

    boolean doesPCollectionRequireIndexedFormat(PCollection<?> pcol) {
        return pcollectionsRequiringIndexedFormat.contains(pcol);
    }

    void addPCollectionRequiringIndexedFormat(PCollection<?> pcol) {
        pcollectionsRequiringIndexedFormat.add(pcol);
    }

    void maybeRecordPCollectionPreservedKeys(PCollection<?> pcol) {
        pCollectionsPreservedKeys.add(pcol);
    }

    void maybeRecordPCollectionWithAutoSharding(PCollection<?> pcol) {
        checkArgument(options.isEnableStreamingEngine(), "Runner determined sharding not available in Dataflow for GroupIntoBatches for" + " non-Streaming-Engine jobs. In order to use runner determined sharding, please use" + " --streaming --enable_streaming_engine");
        pCollectionsPreservedKeys.add(pcol);
        pcollectionsRequiringAutoSharding.add(pcol);
    }

    boolean doesPCollectionPreserveKeys(PCollection<?> pcol) {
        return pCollectionsPreservedKeys.contains(pcol);
    }

    boolean doesPCollectionRequireAutoSharding(PCollection<?> pcol) {
        return pcollectionsRequiringAutoSharding.contains(pcol);
    }

    private Set<PTransform<?, ?>> ptransformViewsWithNonDeterministicKeyCoders;

    void recordViewUsesNonDeterministicKeyCoder(PTransform<?, ?> ptransform) {
        ptransformViewsWithNonDeterministicKeyCoders.add(ptransform);
    }

    private static class StreamingPubsubIOReadOverrideFactory implements PTransformOverrideFactory<PBegin, PCollection<PubsubMessage>, PubsubUnboundedSource> {

        @Override
        public PTransformReplacement<PBegin, PCollection<PubsubMessage>> getReplacementTransform(AppliedPTransform<PBegin, PCollection<PubsubMessage>, PubsubUnboundedSource> transform) {
            return PTransformReplacement.of(transform.getPipeline().begin(), new StreamingPubsubIORead(transform.getTransform()));
        }

        @Override
        public Map<PCollection<?>, ReplacementOutput> mapOutputs(Map<TupleTag<?>, PCollection<?>> outputs, PCollection<PubsubMessage> newOutput) {
            return ReplacementOutputs.singleton(outputs, newOutput);
        }
    }

    private static class StreamingPubsubIORead extends PTransform<PBegin, PCollection<PubsubMessage>> {

        private final PubsubUnboundedSource transform;

        public StreamingPubsubIORead(PubsubUnboundedSource transform) {
            this.transform = transform;
        }

        public PubsubUnboundedSource getOverriddenTransform() {
            return transform;
        }

        @Override
        public PCollection<PubsubMessage> expand(PBegin input) {
            Coder coder = transform.getNeedsMessageId() ? new PubsubMessageWithAttributesAndMessageIdCoder() : new PubsubMessageWithAttributesCoder();
            return PCollection.createPrimitiveOutputInternal(input.getPipeline(), WindowingStrategy.globalDefault(), IsBounded.UNBOUNDED, coder);
        }

        @Override
        protected String getKindString() {
            return "StreamingPubsubIORead";
        }

        static {
            DataflowPipelineTranslator.registerTransformTranslator(StreamingPubsubIORead.class, new StreamingPubsubIOReadTranslator());
        }
    }

    private static void translateOverriddenPubsubSourceStep(PubsubUnboundedSource overriddenTransform, StepTranslationContext stepTranslationContext) {
        stepTranslationContext.addInput(PropertyNames.FORMAT, "pubsub");
        if (overriddenTransform.getTopicProvider() != null) {
            if (overriddenTransform.getTopicProvider().isAccessible()) {
                stepTranslationContext.addInput(PropertyNames.PUBSUB_TOPIC, overriddenTransform.getTopic().getFullPath());
            } else {
                stepTranslationContext.addInput(PropertyNames.PUBSUB_TOPIC_OVERRIDE, ((NestedValueProvider) overriddenTransform.getTopicProvider()).propertyName());
            }
        }
        if (overriddenTransform.getSubscriptionProvider() != null) {
            if (overriddenTransform.getSubscriptionProvider().isAccessible()) {
                stepTranslationContext.addInput(PropertyNames.PUBSUB_SUBSCRIPTION, overriddenTransform.getSubscription().getFullPath());
            } else {
                stepTranslationContext.addInput(PropertyNames.PUBSUB_SUBSCRIPTION_OVERRIDE, ((NestedValueProvider) overriddenTransform.getSubscriptionProvider()).propertyName());
            }
        }
        if (overriddenTransform.getTimestampAttribute() != null) {
            stepTranslationContext.addInput(PropertyNames.PUBSUB_TIMESTAMP_ATTRIBUTE, overriddenTransform.getTimestampAttribute());
        }
        if (overriddenTransform.getIdAttribute() != null) {
            stepTranslationContext.addInput(PropertyNames.PUBSUB_ID_ATTRIBUTE, overriddenTransform.getIdAttribute());
        }
        if (overriddenTransform.getNeedsAttributes() || overriddenTransform.getNeedsMessageId()) {
            stepTranslationContext.addInput(PropertyNames.PUBSUB_SERIALIZED_ATTRIBUTES_FN, byteArrayToJsonString(serializeToByteArray(new IdentityMessageFn())));
        }
    }

    private static class StreamingPubsubIOReadTranslator implements TransformTranslator<StreamingPubsubIORead> {

        @Override
        public void translate(StreamingPubsubIORead transform, TransformTranslator.TranslationContext context) {
            checkArgument(context.getPipelineOptions().isStreaming(), "StreamingPubsubIORead is only for streaming pipelines.");
            StepTranslationContext stepContext = context.addStep(transform, "ParallelRead");
            translateOverriddenPubsubSourceStep(transform.getOverriddenTransform(), stepContext);
            stepContext.addOutput(PropertyNames.OUTPUT, context.getOutput(transform));
        }
    }

    private static class IdentityMessageFn extends SimpleFunction<PubsubMessage, PubsubMessage> {

        @Override
        public PubsubMessage apply(PubsubMessage input) {
            return input;
        }
    }

    private static class StreamingPubsubIOWrite extends PTransform<PCollection<PubsubMessage>, PDone> {

        private final PubsubUnboundedSink transform;

        public StreamingPubsubIOWrite(DataflowRunner runner, PubsubUnboundedSink transform) {
            this.transform = transform;
        }

        PubsubUnboundedSink getOverriddenTransform() {
            return transform;
        }

        @Override
        public PDone expand(PCollection<PubsubMessage> input) {
            return PDone.in(input.getPipeline());
        }

        @Override
        protected String getKindString() {
            return "StreamingPubsubIOWrite";
        }

        static {
            DataflowPipelineTranslator.registerTransformTranslator(StreamingPubsubIOWrite.class, new StreamingPubsubSinkTranslators.StreamingPubsubIOWriteTranslator());
        }
    }

    private static class StreamingPubsubSinkTranslators {

        static class StreamingPubsubIOWriteTranslator implements TransformTranslator<StreamingPubsubIOWrite> {

            @Override
            public void translate(StreamingPubsubIOWrite transform, TransformTranslator.TranslationContext context) {
                checkArgument(context.getPipelineOptions().isStreaming(), "StreamingPubsubIOWrite is only for streaming pipelines.");
                StepTranslationContext stepContext = context.addStep(transform, "ParallelWrite");
                StreamingPubsubSinkTranslators.translate(transform.getOverriddenTransform(), stepContext, context.getInput(transform));
            }
        }

        private static void translate(PubsubUnboundedSink overriddenTransform, StepTranslationContext stepContext, PCollection input) {
            stepContext.addInput(PropertyNames.FORMAT, "pubsub");
            if (overriddenTransform.getTopicProvider().isAccessible()) {
                stepContext.addInput(PropertyNames.PUBSUB_TOPIC, overriddenTransform.getTopic().getFullPath());
            } else {
                stepContext.addInput(PropertyNames.PUBSUB_TOPIC_OVERRIDE, ((NestedValueProvider) overriddenTransform.getTopicProvider()).propertyName());
            }
            if (overriddenTransform.getTimestampAttribute() != null) {
                stepContext.addInput(PropertyNames.PUBSUB_TIMESTAMP_ATTRIBUTE, overriddenTransform.getTimestampAttribute());
            }
            if (overriddenTransform.getIdAttribute() != null) {
                stepContext.addInput(PropertyNames.PUBSUB_ID_ATTRIBUTE, overriddenTransform.getIdAttribute());
            }
            stepContext.addInput(PropertyNames.PUBSUB_SERIALIZED_ATTRIBUTES_FN, byteArrayToJsonString(serializeToByteArray(new IdentityMessageFn())));
            stepContext.addEncodingInput(WindowedValue.getFullCoder(VoidCoder.of(), GlobalWindow.Coder.INSTANCE));
            stepContext.addInput(PropertyNames.PARALLEL_INPUT, input);
        }
    }

    private static class SingleOutputExpandableTransformTranslator implements TransformTranslator<External.SingleOutputExpandableTransform> {

        @Override
        public void translate(External.SingleOutputExpandableTransform transform, TranslationContext context) {
            StepTranslationContext stepContext = context.addStep(transform, "ExternalTransform");
            PCollection<?> output = (PCollection<?>) context.getOutput(transform);
            stepContext.addOutput(PropertyNames.OUTPUT, output);
        }
    }

    static {
        DataflowPipelineTranslator.registerTransformTranslator(External.SingleOutputExpandableTransform.class, new SingleOutputExpandableTransformTranslator());
    }

    private static class MultiOutputExpandableTransformTranslator implements TransformTranslator<External.MultiOutputExpandableTransform> {

        @Override
        public void translate(External.MultiOutputExpandableTransform transform, TranslationContext context) {
            StepTranslationContext stepContext = context.addStep(transform, "ExternalTransform");
            Map<TupleTag<?>, PCollection<?>> outputs = context.getOutputs(transform);
            for (Map.Entry<TupleTag<?>, PCollection<?>> taggedOutput : outputs.entrySet()) {
                TupleTag<?> tag = taggedOutput.getKey();
                stepContext.addOutput(tag.getId(), taggedOutput.getValue());
            }
        }
    }

    static {
        DataflowPipelineTranslator.registerTransformTranslator(External.MultiOutputExpandableTransform.class, new MultiOutputExpandableTransformTranslator());
    }

    private static class ImpulseTranslator implements TransformTranslator<Impulse> {

        @Override
        public void translate(Impulse transform, TransformTranslator.TranslationContext context) {
            if (context.getPipelineOptions().isStreaming()) {
                StepTranslationContext stepContext = context.addStep(transform, "ParallelRead");
                stepContext.addInput(PropertyNames.FORMAT, "pubsub");
                stepContext.addInput(PropertyNames.PUBSUB_SUBSCRIPTION, "_starting_signal/");
                stepContext.addOutput(PropertyNames.OUTPUT, context.getOutput(transform));
            } else {
                StepTranslationContext stepContext = context.addStep(transform, "ParallelRead");
                stepContext.addInput(PropertyNames.FORMAT, "impulse");
                WindowedValue.FullWindowedValueCoder<byte[]> coder = WindowedValue.getFullCoder(context.getOutput(transform).getCoder(), GlobalWindow.Coder.INSTANCE);
                byte[] encodedImpulse;
                try {
                    encodedImpulse = encodeToByteArray(coder, WindowedValue.valueInGlobalWindow(new byte[0]));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                stepContext.addInput(PropertyNames.IMPULSE_ELEMENT, byteArrayToJsonString(encodedImpulse));
                stepContext.addOutput(PropertyNames.OUTPUT, context.getOutput(transform));
            }
        }
    }

    static {
        DataflowPipelineTranslator.registerTransformTranslator(Impulse.class, new ImpulseTranslator());
    }

    private static class StreamingUnboundedReadOverrideFactory<T> implements PTransformOverrideFactory<PBegin, PCollection<T>, Read.Unbounded<T>> {

        @Override
        public PTransformReplacement<PBegin, PCollection<T>> getReplacementTransform(AppliedPTransform<PBegin, PCollection<T>, Read.Unbounded<T>> transform) {
            return PTransformReplacement.of(transform.getPipeline().begin(), new StreamingUnboundedRead<>(transform.getTransform()));
        }

        @Override
        public Map<PCollection<?>, ReplacementOutput> mapOutputs(Map<TupleTag<?>, PCollection<?>> outputs, PCollection<T> newOutput) {
            return ReplacementOutputs.singleton(outputs, newOutput);
        }
    }

    private static class StreamingUnboundedRead<T> extends PTransform<PBegin, PCollection<T>> {

        private final UnboundedSource<T, ?> source;

        public StreamingUnboundedRead(Read.Unbounded<T> transform) {
            this.source = transform.getSource();
        }

        @Override
        public final PCollection<T> expand(PBegin input) {
            source.validate();
            if (source.requiresDeduping()) {
                return Pipeline.applyTransform(input, new ReadWithIds<>(source)).apply(new Deduplicate<>());
            } else {
                return Pipeline.applyTransform(input, new ReadWithIds<>(source)).apply("StripIds", ParDo.of(new ValueWithRecordId.StripIdsDoFn<>()));
            }
        }

        private static class ReadWithIds<T> extends PTransform<PInput, PCollection<ValueWithRecordId<T>>> {

            private final UnboundedSource<T, ?> source;

            private ReadWithIds(UnboundedSource<T, ?> source) {
                this.source = source;
            }

            @Override
            public final PCollection<ValueWithRecordId<T>> expand(PInput input) {
                return PCollection.createPrimitiveOutputInternal(input.getPipeline(), WindowingStrategy.globalDefault(), IsBounded.UNBOUNDED, ValueWithRecordId.ValueWithRecordIdCoder.of(source.getOutputCoder()));
            }

            @Override
            public void populateDisplayData(DisplayData.Builder builder) {
                builder.delegate(source);
            }

            public UnboundedSource<T, ?> getSource() {
                return source;
            }
        }

        @Override
        public String getKindString() {
            return String.format("Read(%s)", NameUtils.approximateSimpleName(source));
        }

        static {
            DataflowPipelineTranslator.registerTransformTranslator(ReadWithIds.class, new ReadWithIdsTranslator());
        }

        private static class ReadWithIdsTranslator implements TransformTranslator<ReadWithIds<?>> {

            @Override
            public void translate(ReadWithIds<?> transform, TransformTranslator.TranslationContext context) {
                ReadTranslator.translateReadHelper(transform.getSource(), transform, context);
            }
        }
    }

    private static class Deduplicate<T> extends PTransform<PCollection<ValueWithRecordId<T>>, PCollection<T>> {

        private static final int NUM_RESHARD_KEYS = 10000;

        @Override
        public PCollection<T> expand(PCollection<ValueWithRecordId<T>> input) {
            return input.apply(WithKeys.of((ValueWithRecordId<T> value) -> Arrays.hashCode(value.getId()) % NUM_RESHARD_KEYS).withKeyType(TypeDescriptors.integers())).apply(Reshuffle.of()).apply("StripIds", ParDo.of(new DoFn<KV<Integer, ValueWithRecordId<T>>, T>() {

                @ProcessElement
                public void processElement(ProcessContext c) {
                    c.output(c.element().getValue().getValue());
                }
            }));
        }
    }

    private static class StreamingBoundedReadOverrideFactory<T> implements PTransformOverrideFactory<PBegin, PCollection<T>, Read.Bounded<T>> {

        @Override
        public PTransformReplacement<PBegin, PCollection<T>> getReplacementTransform(AppliedPTransform<PBegin, PCollection<T>, Read.Bounded<T>> transform) {
            return PTransformReplacement.of(transform.getPipeline().begin(), new StreamingBoundedRead<>(transform.getTransform()));
        }

        @Override
        public Map<PCollection<?>, ReplacementOutput> mapOutputs(Map<TupleTag<?>, PCollection<?>> outputs, PCollection<T> newOutput) {
            return ReplacementOutputs.singleton(outputs, newOutput);
        }
    }

    private static class StreamingBoundedRead<T> extends PTransform<PBegin, PCollection<T>> {

        private final BoundedSource<T> source;

        public StreamingBoundedRead(Read.Bounded<T> transform) {
            this.source = transform.getSource();
        }

        @Override
        public final PCollection<T> expand(PBegin input) {
            source.validate();
            return Pipeline.applyTransform(input, new UnboundedReadFromBoundedSource<>(source)).setIsBoundedInternal(IsBounded.BOUNDED);
        }
    }

    public static class StreamingPCollectionViewWriterFn<T> extends DoFn<Iterable<T>, T> {

        private final PCollectionView<?> view;

        private final Coder<T> dataCoder;

        public static <T> StreamingPCollectionViewWriterFn<T> create(PCollectionView<?> view, Coder<T> dataCoder) {
            return new StreamingPCollectionViewWriterFn<>(view, dataCoder);
        }

        private StreamingPCollectionViewWriterFn(PCollectionView<?> view, Coder<T> dataCoder) {
            this.view = view;
            this.dataCoder = dataCoder;
        }

        public PCollectionView<?> getView() {
            return view;
        }

        public Coder<T> getDataCoder() {
            return dataCoder;
        }

        @ProcessElement
        public void processElement(ProcessContext c, BoundedWindow w) throws Exception {
            throw new UnsupportedOperationException(String.format("%s is a marker class only and should never be executed.", getClass().getName()));
        }
    }

    @Override
    public String toString() {
        return "DataflowRunner#" + options.getJobName();
    }

    private String getJobIdFromName(String jobName) {
        try {
            ListJobsResponse listResult;
            String token = null;
            do {
                listResult = dataflowClient.listJobs(token);
                token = listResult.getNextPageToken();
                for (Job job : listResult.getJobs()) {
                    if (job.getName().equals(jobName) && MonitoringUtil.toState(job.getCurrentState()).equals(State.RUNNING)) {
                        return job.getId();
                    }
                }
            } while (token != null);
        } catch (GoogleJsonResponseException e) {
            throw new RuntimeException("Got error while looking up jobs: " + (e.getDetails() != null ? e.getDetails().getMessage() : e), e);
        } catch (IOException e) {
            throw new RuntimeException("Got error while looking up jobs: ", e);
        }
        throw new IllegalArgumentException("Could not find running job named " + jobName);
    }

    static class CombineGroupedValues<K, InputT, OutputT> extends PTransform<PCollection<KV<K, Iterable<InputT>>>, PCollection<KV<K, OutputT>>> {

        private final Combine.GroupedValues<K, InputT, OutputT> original;

        private final Coder<KV<K, OutputT>> outputCoder;

        CombineGroupedValues(GroupedValues<K, InputT, OutputT> original, Coder<KV<K, OutputT>> outputCoder) {
            this.original = original;
            this.outputCoder = outputCoder;
        }

        @Override
        public PCollection<KV<K, OutputT>> expand(PCollection<KV<K, Iterable<InputT>>> input) {
            return PCollection.createPrimitiveOutputInternal(input.getPipeline(), input.getWindowingStrategy(), input.isBounded(), outputCoder);
        }

        public Combine.GroupedValues<K, InputT, OutputT> getOriginalCombine() {
            return original;
        }
    }

    private static class PrimitiveCombineGroupedValuesOverrideFactory<K, InputT, OutputT> implements PTransformOverrideFactory<PCollection<KV<K, Iterable<InputT>>>, PCollection<KV<K, OutputT>>, Combine.GroupedValues<K, InputT, OutputT>> {

        @Override
        public PTransformReplacement<PCollection<KV<K, Iterable<InputT>>>, PCollection<KV<K, OutputT>>> getReplacementTransform(AppliedPTransform<PCollection<KV<K, Iterable<InputT>>>, PCollection<KV<K, OutputT>>, GroupedValues<K, InputT, OutputT>> transform) {
            return PTransformReplacement.of(PTransformReplacements.getSingletonMainInput(transform), new CombineGroupedValues<>(transform.getTransform(), PTransformReplacements.getSingletonMainOutput(transform).getCoder()));
        }

        @Override
        public Map<PCollection<?>, ReplacementOutput> mapOutputs(Map<TupleTag<?>, PCollection<?>> outputs, PCollection<KV<K, OutputT>> newOutput) {
            return ReplacementOutputs.singleton(outputs, newOutput);
        }
    }

    private static class StreamingPubsubIOWriteOverrideFactory implements PTransformOverrideFactory<PCollection<PubsubMessage>, PDone, PubsubUnboundedSink> {

        private final DataflowRunner runner;

        private StreamingPubsubIOWriteOverrideFactory(DataflowRunner runner) {
            this.runner = runner;
        }

        @Override
        public PTransformReplacement<PCollection<PubsubMessage>, PDone> getReplacementTransform(AppliedPTransform<PCollection<PubsubMessage>, PDone, PubsubUnboundedSink> transform) {
            return PTransformReplacement.of(PTransformReplacements.getSingletonMainInput(transform), new StreamingPubsubIOWrite(runner, transform.getTransform()));
        }

        @Override
        public Map<PCollection<?>, ReplacementOutput> mapOutputs(Map<TupleTag<?>, PCollection<?>> outputs, PDone newOutput) {
            return Collections.emptyMap();
        }
    }

    @VisibleForTesting
    static class StreamingShardedWriteFactory<UserT, DestinationT, OutputT> implements PTransformOverrideFactory<PCollection<UserT>, WriteFilesResult<DestinationT>, WriteFiles<UserT, DestinationT, OutputT>> {

        static final int DEFAULT_NUM_SHARDS = 10;

        DataflowPipelineWorkerPoolOptions options;

        StreamingShardedWriteFactory(PipelineOptions options) {
            this.options = options.as(DataflowPipelineWorkerPoolOptions.class);
        }

        @Override
        public PTransformReplacement<PCollection<UserT>, WriteFilesResult<DestinationT>> getReplacementTransform(AppliedPTransform<PCollection<UserT>, WriteFilesResult<DestinationT>, WriteFiles<UserT, DestinationT, OutputT>> transform) {
            int numShards;
            if (options.getMaxNumWorkers() > 0) {
                numShards = options.getMaxNumWorkers() * 2;
            } else if (options.getNumWorkers() > 0) {
                numShards = options.getNumWorkers() * 2;
            } else {
                numShards = DEFAULT_NUM_SHARDS;
            }
            try {
                List<PCollectionView<?>> sideInputs = WriteFilesTranslation.getDynamicDestinationSideInputs(transform);
                FileBasedSink sink = WriteFilesTranslation.getSink(transform);
                WriteFiles<UserT, DestinationT, OutputT> replacement = WriteFiles.to(sink).withSideInputs(sideInputs);
                if (WriteFilesTranslation.isWindowedWrites(transform)) {
                    replacement = replacement.withWindowedWrites();
                }
                return PTransformReplacement.of(PTransformReplacements.getSingletonMainInput(transform), replacement.withNumShards(numShards));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        @Override
        public Map<PCollection<?>, ReplacementOutput> mapOutputs(Map<TupleTag<?>, PCollection<?>> outputs, WriteFilesResult<DestinationT> newOutput) {
            return ReplacementOutputs.tagged(outputs, newOutput);
        }
    }

    @VisibleForTesting
    static String getContainerImageForJob(DataflowPipelineOptions options) {
        String containerImage = options.getSdkContainerImage();
        if (containerImage == null) {
            return getDefaultContainerImageUrl(options);
        } else if (containerImage.contains("IMAGE")) {
            return containerImage.replace("IMAGE", getDefaultContainerImageNameForJob(options));
        } else {
            return containerImage;
        }
    }

    static String getDefaultContainerImageUrl(DataflowPipelineOptions options) {
        DataflowRunnerInfo dataflowRunnerInfo = DataflowRunnerInfo.getDataflowRunnerInfo();
        return String.format("%s/%s:%s", dataflowRunnerInfo.getContainerImageBaseRepository(), getDefaultContainerImageNameForJob(options), getDefaultContainerVersion(options));
    }

    static String getDefaultContainerImageNameForJob(DataflowPipelineOptions options) {
        Environments.JavaVersion javaVersion = Environments.getJavaVersion();
        if (useUnifiedWorker(options)) {
            return String.format("beam_%s_sdk", javaVersion.name());
        } else if (options.isStreaming()) {
            return String.format("beam-%s-streaming", javaVersion.legacyName());
        } else {
            return String.format("beam-%s-batch", javaVersion.legacyName());
        }
    }

    static String getDefaultContainerVersion(DataflowPipelineOptions options) {
        DataflowRunnerInfo dataflowRunnerInfo = DataflowRunnerInfo.getDataflowRunnerInfo();
        ReleaseInfo releaseInfo = ReleaseInfo.getReleaseInfo();
        if (releaseInfo.isDevSdkVersion()) {
            if (useUnifiedWorker(options)) {
                return dataflowRunnerInfo.getFnApiDevContainerVersion();
            }
            return dataflowRunnerInfo.getLegacyDevContainerVersion();
        }
        return releaseInfo.getSdkVersion();
    }

    static boolean useUnifiedWorker(DataflowPipelineOptions options) {
        return hasExperiment(options, "beam_fn_api") || hasExperiment(options, "use_runner_v2") || hasExperiment(options, "use_unified_worker") || ((hasExperiment(options, "enable_prime") || firstNonNull(options.getDataflowServiceOptions(), new ArrayList<>()).contains("enable_prime")) && !hasExperiment(options, "disable_prime_runner_v2"));
    }

    static boolean useStreamingEngine(DataflowPipelineOptions options) {
        return hasExperiment(options, GcpOptions.STREAMING_ENGINE_EXPERIMENT) || hasExperiment(options, GcpOptions.WINDMILL_SERVICE_EXPERIMENT);
    }

    static void verifyDoFnSupported(DoFn<?, ?> fn, boolean streaming, DataflowPipelineOptions options) {
        if (streaming && DoFnSignatures.requiresTimeSortedInput(fn)) {
            throw new UnsupportedOperationException(String.format("%s does not currently support @RequiresTimeSortedInput in streaming mode.", DataflowRunner.class.getSimpleName()));
        }
        boolean streamingEngine = useStreamingEngine(options);
        boolean isUnifiedWorker = useUnifiedWorker(options);
        if (DoFnSignatures.usesSetState(fn)) {
            if (streaming && (isUnifiedWorker || streamingEngine)) {
                throw new UnsupportedOperationException(String.format("%s does not currently support %s when using %s", DataflowRunner.class.getSimpleName(), SetState.class.getSimpleName(), isUnifiedWorker ? "streaming on unified worker" : "streaming engine"));
            }
        }
        if (DoFnSignatures.usesMapState(fn)) {
            if (streaming && (isUnifiedWorker || streamingEngine)) {
                throw new UnsupportedOperationException(String.format("%s does not currently support %s when using %s", DataflowRunner.class.getSimpleName(), MapState.class.getSimpleName(), isUnifiedWorker ? "streaming on unified worker" : "streaming engine"));
            }
        }
        if (DoFnSignatures.usesBundleFinalizer(fn) && !isUnifiedWorker) {
            throw new UnsupportedOperationException(String.format("%s does not currently support %s when not using unified worker because it uses " + "BundleFinalizers in its implementation. Set the `--experiments=use_runner_v2` " + "option to use this DoFn.", DataflowRunner.class.getSimpleName(), fn.getClass().getSimpleName()));
        }
    }

    static void verifyStateSupportForWindowingStrategy(WindowingStrategy strategy) {
        if (strategy.needsMerge()) {
            throw new UnsupportedOperationException(String.format("%s does not currently support state or timers with merging windows", DataflowRunner.class.getSimpleName()));
        }
    }
}
