package org.apache.cassandra.gms;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.apache.cassandra.Util;
import org.apache.cassandra.config.DatabaseDescriptor;
import org.apache.cassandra.dht.IPartitioner;
import org.apache.cassandra.dht.RandomPartitioner;
import org.apache.cassandra.dht.Token;
import org.apache.cassandra.locator.TokenMetadata;
import org.apache.cassandra.service.StorageService;
import static org.junit.Assert.assertEquals;

public class GossiperTest {

    static {
        DatabaseDescriptor.daemonInitialization();
    }

    static final IPartitioner partitioner = new RandomPartitioner();

    StorageService ss = StorageService.instance;

    TokenMetadata tmd = StorageService.instance.getTokenMetadata();

    ArrayList<Token> endpointTokens = new ArrayList<>();

    ArrayList<Token> keyTokens = new ArrayList<>();

    List<InetAddress> hosts = new ArrayList<>();

    List<UUID> hostIds = new ArrayList<>();

    @Before
    public void setup() {
        tmd.clearUnsafe();
    }

    @Test
    public void testLargeGenerationJump() throws UnknownHostException, InterruptedException {
        Util.createInitialRing(ss, partitioner, endpointTokens, keyTokens, hosts, hostIds, 2);
        InetAddress remoteHostAddress = hosts.get(1);
        EndpointState initialRemoteState = Gossiper.instance.getEndpointStateForEndpoint(remoteHostAddress);
        HeartBeatState initialRemoteHeartBeat = initialRemoteState.getHeartBeatState();
        assertEquals(initialRemoteHeartBeat.getGeneration(), 1);
        HeartBeatState proposedRemoteHeartBeat = new HeartBeatState(initialRemoteHeartBeat.getGeneration() + Gossiper.MAX_GENERATION_DIFFERENCE + 1);
        EndpointState proposedRemoteState = new EndpointState(proposedRemoteHeartBeat);
        Gossiper.instance.applyStateLocally(ImmutableMap.of(remoteHostAddress, proposedRemoteState));
        HeartBeatState actualRemoteHeartBeat = Gossiper.instance.getEndpointStateForEndpoint(remoteHostAddress).getHeartBeatState();
        assertEquals(proposedRemoteHeartBeat.getGeneration(), actualRemoteHeartBeat.getGeneration());
        HeartBeatState badProposedRemoteHeartBeat = new HeartBeatState((int) (System.currentTimeMillis() / 1000) + Gossiper.MAX_GENERATION_DIFFERENCE * 10);
        EndpointState badProposedRemoteState = new EndpointState(badProposedRemoteHeartBeat);
        Gossiper.instance.applyStateLocally(ImmutableMap.of(remoteHostAddress, badProposedRemoteState));
        actualRemoteHeartBeat = Gossiper.instance.getEndpointStateForEndpoint(remoteHostAddress).getHeartBeatState();
        assertEquals(proposedRemoteHeartBeat.getGeneration(), actualRemoteHeartBeat.getGeneration());
    }
}
