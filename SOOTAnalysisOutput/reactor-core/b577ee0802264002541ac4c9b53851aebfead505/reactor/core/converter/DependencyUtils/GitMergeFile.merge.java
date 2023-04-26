package reactor.core.converter;

import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;

public final class DependencyUtils {

    static private final boolean HAS_REACTOR_CODEC;

    static private final boolean HAS_REACTOR_NET;

    static private final boolean HAS_REACTOR_BUS;

    static private final FlowPublisherConverter FLOW_PUBLISHER_CONVERTER;

    static private final RxJava1ObservableConverter RX_JAVA_1_OBSERVABLE_CONVERTER;

    static private final RxJava1SingleConverter RX_JAVA_1_SINGLE_CONVERTER;

    static private final RxJava1CompletableConverter RX_JAVA_1_COMPLETABLE_CONVERTER;

    private DependencyUtils() {
    }

    static {
        final int RXJAVA_1_OBSERVABLE = 0b000001;
        final int RXJAVA_1_SINGLE = 0b000010;
        final int RXJAVA_1_COMPLETABLE = 0b000100;
        final int FLOW_PUBLISHER = 0b100000;
        final int REACTOR_CODEC = 0b1000000;
        final int REACTOR_BUS = 0b10000000;
        final int REACTOR_NET = 0b100000000;
        int detected = 0;
        try {
            Flux.class.getClassLoader().loadClass("rx.Observable");
            detected = RXJAVA_1_OBSERVABLE;
            Class.forName("rx.Single");
            detected |= RXJAVA_1_SINGLE;
            Class.forName("rx.Completable");
            detected |= RXJAVA_1_COMPLETABLE;
        } catch (ClassNotFoundException ignore) {
        }
        try {
            Flux.class.getClassLoader().loadClass("reactor.io.codec.Codec");
            detected |= REACTOR_CODEC;
        } catch (ClassNotFoundException ignore) {
        }
        try {
            Flux.class.getClassLoader().loadClass("reactor.io.net.ReactiveChannel");
            detected |= REACTOR_NET;
        } catch (ClassNotFoundException ignore) {
        }
        try {
            Flux.class.getClassLoader().loadClass("reactor.bus.registry.Registry");
            detected |= REACTOR_BUS;
        } catch (ClassNotFoundException ignore) {
        }
        if ((detected & RXJAVA_1_OBSERVABLE) == RXJAVA_1_OBSERVABLE) {
            RX_JAVA_1_OBSERVABLE_CONVERTER = RxJava1ObservableConverter.INSTANCE;
        } else {
            RX_JAVA_1_OBSERVABLE_CONVERTER = null;
        }
        if ((detected & RXJAVA_1_SINGLE) == RXJAVA_1_SINGLE) {
            RX_JAVA_1_SINGLE_CONVERTER = RxJava1SingleConverter.INSTANCE;
        } else {
            RX_JAVA_1_SINGLE_CONVERTER = null;
        }
        if ((detected & RXJAVA_1_COMPLETABLE) == RXJAVA_1_COMPLETABLE) {
            RX_JAVA_1_COMPLETABLE_CONVERTER = RxJava1CompletableConverter.INSTANCE;
        } else {
            RX_JAVA_1_COMPLETABLE_CONVERTER = null;
        }
        if ((detected & FLOW_PUBLISHER) == FLOW_PUBLISHER) {
            FLOW_PUBLISHER_CONVERTER = FlowPublisherConverter.INSTANCE;
        } else {
            FLOW_PUBLISHER_CONVERTER = null;
        }
        HAS_REACTOR_CODEC = (detected & REACTOR_CODEC) == REACTOR_CODEC;
        HAS_REACTOR_BUS = (detected & REACTOR_BUS) == REACTOR_BUS;
        HAS_REACTOR_NET = (detected & REACTOR_NET) == REACTOR_NET;
    }

    public static boolean hasRxJava1() {
        return RX_JAVA_1_OBSERVABLE_CONVERTER != null;
    }

    public static boolean hasRxJava1Single() {
        return RX_JAVA_1_SINGLE_CONVERTER != null;
    }

    public static boolean hasFlowPublisher() {
        return FLOW_PUBLISHER_CONVERTER != null;
    }

    public static boolean hasRxJava1Completable() {
        return RX_JAVA_1_COMPLETABLE_CONVERTER != null;
    }

    public static boolean hasReactorCodec() {
        return HAS_REACTOR_CODEC;
    }

    public static boolean hasReactorBus() {
        return HAS_REACTOR_BUS;
    }

    public static boolean hasReactorNet() {
        return HAS_REACTOR_NET;
    }

    public static Publisher<?> convertToPublisher(Object source) {
        if (source == null) {
            throw new IllegalArgumentException("Cannot convert null sources");
        }
        if (hasRxJava1()) {
            if (hasRxJava1Single() && RX_JAVA_1_SINGLE_CONVERTER.test(source)) {
                return RX_JAVA_1_SINGLE_CONVERTER.apply(source);
            } else if (RX_JAVA_1_OBSERVABLE_CONVERTER.test(source)) {
                return RX_JAVA_1_OBSERVABLE_CONVERTER.apply(source);
<<<<<<< MINE
            } else if (hasRxJava1Completable() && RX_JAVA_1_COMPLETABLE_CONVERTER.test(source)) {
=======
            } else if (RX_JAVA_1_COMPLETABLE_CONVERTER.test(source)) {
>>>>>>> YOURS
                return RX_JAVA_1_COMPLETABLE_CONVERTER.apply(source);
            }
        }
        if (hasFlowPublisher() && FLOW_PUBLISHER_CONVERTER.test(source)) {
            return FLOW_PUBLISHER_CONVERTER.apply(source);
        }
        throw new UnsupportedOperationException("Conversion to Publisher from " + source.getClass());
    }

    @SuppressWarnings("unchecked")
    public static <T> T convertFromPublisher(Publisher<?> source, Class<T> to) {
        if (source == null || to == null) {
            throw new IllegalArgumentException("Cannot convert " + source + " source to " + to + " type");
        }
        if (hasRxJava1()) {
            if (hasRxJava1Single() && RX_JAVA_1_SINGLE_CONVERTER.get().isAssignableFrom(to)) {
                return (T) RX_JAVA_1_SINGLE_CONVERTER.convertTo(source, to);
            } else if (RX_JAVA_1_OBSERVABLE_CONVERTER.get().isAssignableFrom(to)) {
                return (T) RX_JAVA_1_OBSERVABLE_CONVERTER.convertTo(source, to);
            } else if (RX_JAVA_1_COMPLETABLE_CONVERTER.get().isAssignableFrom(to)) {
                return (T) RX_JAVA_1_COMPLETABLE_CONVERTER.convertTo(source, to);
            }
        }
        if (hasFlowPublisher() && FLOW_PUBLISHER_CONVERTER.get().isAssignableFrom(to)) {
            return (T) FLOW_PUBLISHER_CONVERTER.convertTo(source, to);
        }
        throw new UnsupportedOperationException("Cannot convert " + source.getClass() + " source to " + to.getClass() + " type");
    }
}
