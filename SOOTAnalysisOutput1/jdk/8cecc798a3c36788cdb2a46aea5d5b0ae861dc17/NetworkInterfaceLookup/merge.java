package org.openjdk.bench.java.net;

import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.BenchmarkMode;
import org.openjdk.jmh.annotations.Fork;
import org.openjdk.jmh.annotations.Measurement;
import org.openjdk.jmh.annotations.Mode;
import org.openjdk.jmh.annotations.OutputTimeUnit;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.Warmup;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.concurrent.TimeUnit;

@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.SECONDS)
@State(Scope.Thread)
@Fork(2)
@Warmup(iterations = 5, time = 2)
@Measurement(iterations = 10, time = 2)
public class NetworkInterfaceLookup {

    static final InetAddress address = InetAddress.getLoopbackAddress();

    static final Method isBoundInetAddress_method;

    static final Method getByInetAddress_method;

    static {
        Method isBound = null;
        Method getByInet = null;
        try {
            isBound = NetworkInterface.class.getDeclaredMethod("isBoundInetAddress", InetAddress.class);
            isBound.setAccessible(true);
        } catch (Exception e) {
            System.out.println("NetworkInterface.isBoundInetAddress not found");
        }
        try {
            getByInet = NetworkInterface.class.getDeclaredMethod("getByInetAddress", InetAddress.class);
        } catch (Exception e) {
            System.out.println("NetworkInterface.getByInetAddress not found");
        }
        isBoundInetAddress_method = isBound;
        getByInetAddress_method = getByInet;
    }

    @Benchmark
    public boolean bound() throws Exception {
        return (boolean) isBoundInetAddress_method.invoke(null, address);
    }

    @Benchmark
    public NetworkInterface getByInetAddress() throws Exception {
        return (NetworkInterface) getByInetAddress_method.invoke(null, address);
    }
}
