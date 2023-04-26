package org.springframework.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.Flow;
import java.util.function.Function;
import kotlinx.coroutines.CompletableDeferredKt;
import kotlinx.coroutines.Deferred;
import org.reactivestreams.Publisher;
import reactor.adapter.JdkFlowAdapter;
import reactor.blockhound.BlockHound;
import reactor.blockhound.integration.BlockHoundIntegration;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;
import org.springframework.util.ConcurrentReferenceHashMap;

public class ReactiveAdapterRegistry {

    @Nullable
    private static volatile ReactiveAdapterRegistry sharedInstance;

    private static final boolean reactorPresent;

    private static final boolean rxjava2Present;

    private static final boolean rxjava3Present;

    private static final boolean kotlinCoroutinesPresent;

    private static final boolean mutinyPresent;

    static {
        ClassLoader classLoader = ReactiveAdapterRegistry.class.getClassLoader();
        reactorPresent = ClassUtils.isPresent("reactor.core.publisher.Flux", classLoader);
<<<<<<< MINE
=======
        flowPublisherPresent = ClassUtils.isPresent("java.util.concurrent.Flow.Publisher", classLoader);
        rxjava1Present = ClassUtils.isPresent("rx.Observable", classLoader) && ClassUtils.isPresent("rx.RxReactiveStreams", classLoader);
>>>>>>> YOURS
        rxjava2Present = ClassUtils.isPresent("io.reactivex.Flowable", classLoader);
        rxjava3Present = ClassUtils.isPresent("io.reactivex.rxjava3.core.Flowable", classLoader);
        kotlinCoroutinesPresent = ClassUtils.isPresent("kotlinx.coroutines.reactor.MonoKt", classLoader);
        mutinyPresent = ClassUtils.isPresent("io.smallrye.mutiny.Multi", classLoader);
    }

    private final List<ReactiveAdapter> adapters = new ArrayList<>();

    public ReactiveAdapterRegistry() {
        if (reactorPresent) {
            new ReactorRegistrar().registerAdapters(this);
<<<<<<< MINE
            new ReactorJdkFlowAdapterRegistrar().registerAdapter(this);
=======
            if (flowPublisherPresent) {
                new ReactorJdkFlowAdapterRegistrar().registerAdapter(this);
            }
        }
        if (rxjava1Present) {
            new RxJava1Registrar().registerAdapters(this);
>>>>>>> YOURS
        }
        if (rxjava2Present) {
            new RxJava2Registrar().registerAdapters(this);
        }
        if (rxjava3Present) {
            new RxJava3Registrar().registerAdapters(this);
        }
        if (reactorPresent && kotlinCoroutinesPresent) {
            new CoroutinesRegistrar().registerAdapters(this);
        }
        if (mutinyPresent) {
            new MutinyRegistrar().registerAdapters(this);
        }
    }

    public boolean hasAdapters() {
        return !this.adapters.isEmpty();
    }

    public void registerReactiveType(ReactiveTypeDescriptor descriptor, Function<Object, Publisher<?>> toAdapter, Function<Publisher<?>, Object> fromAdapter) {
        if (reactorPresent) {
            this.adapters.add(new ReactorAdapter(descriptor, toAdapter, fromAdapter));
        } else {
            this.adapters.add(new ReactiveAdapter(descriptor, toAdapter, fromAdapter));
        }
    }

    @Nullable
    public ReactiveAdapter getAdapter(Class<?> reactiveType) {
        return getAdapter(reactiveType, null);
    }

    @Nullable
    public ReactiveAdapter getAdapter(@Nullable Class<?> reactiveType, @Nullable Object source) {
        if (this.adapters.isEmpty()) {
            return null;
        }
        Object sourceToUse = (source instanceof Optional ? ((Optional<?>) source).orElse(null) : source);
        Class<?> clazz = (sourceToUse != null ? sourceToUse.getClass() : reactiveType);
        if (clazz == null) {
            return null;
        }
        for (ReactiveAdapter adapter : this.adapters) {
            if (adapter.getReactiveType() == clazz) {
                return adapter;
            }
        }
        for (ReactiveAdapter adapter : this.adapters) {
            if (adapter.getReactiveType().isAssignableFrom(clazz)) {
                return adapter;
            }
        }
        return null;
    }

    public static ReactiveAdapterRegistry getSharedInstance() {
        ReactiveAdapterRegistry registry = sharedInstance;
        if (registry == null) {
            synchronized (ReactiveAdapterRegistry.class) {
                registry = sharedInstance;
                if (registry == null) {
                    registry = new ReactiveAdapterRegistry();
                    sharedInstance = registry;
                }
            }
        }
        return registry;
    }

    private static class ReactorRegistrar {

        void registerAdapters(ReactiveAdapterRegistry registry) {
            registry.registerReactiveType(ReactiveTypeDescriptor.singleOptionalValue(Mono.class, Mono::empty), source -> (Mono<?>) source, Mono::from);
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(Flux.class, Flux::empty), source -> (Flux<?>) source, Flux::from);
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(Publisher.class, Flux::empty), source -> (Publisher<?>) source, source -> source);
            registry.registerReactiveType(ReactiveTypeDescriptor.nonDeferredAsyncValue(CompletionStage.class, EmptyCompletableFuture::new), source -> Mono.fromCompletionStage((CompletionStage<?>) source), source -> Mono.from(source).toFuture());
        }
    }

    private static class ReactorJdkFlowAdapterRegistrar {
<<<<<<< MINE
=======

        void registerAdapter(ReactiveAdapterRegistry registry) {
            try {
                String publisherName = "java.util.concurrent.Flow.Publisher";
                Class<?> publisherClass = ClassUtils.forName(publisherName, getClass().getClassLoader());
                String adapterName = "reactor.adapter.JdkFlowAdapter";
                Class<?> flowAdapterClass = ClassUtils.forName(adapterName, getClass().getClassLoader());
                Method toFluxMethod = flowAdapterClass.getMethod("flowPublisherToFlux", publisherClass);
                Method toFlowMethod = flowAdapterClass.getMethod("publisherToFlowPublisher", Publisher.class);
                Object emptyFlow = ReflectionUtils.invokeMethod(toFlowMethod, null, Flux.empty());
                registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(publisherClass, () -> emptyFlow), source -> (Publisher<?>) ReflectionUtils.invokeMethod(toFluxMethod, null, source), publisher -> ReflectionUtils.invokeMethod(toFlowMethod, null, publisher));
            } catch (Throwable ex) {
            }
        }
    }

    private static class RxJava1Registrar {
>>>>>>> YOURS

        void registerAdapter(ReactiveAdapterRegistry registry) {
            Flow.Publisher<?> emptyFlow = JdkFlowAdapter.publisherToFlowPublisher(Flux.empty());
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(Flow.Publisher.class, () -> emptyFlow), source -> JdkFlowAdapter.flowPublisherToFlux((Flow.Publisher<?>) source), JdkFlowAdapter::publisherToFlowPublisher);
        }
    }

    private static class RxJava2Registrar {

        void registerAdapters(ReactiveAdapterRegistry registry) {
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(io.reactivex.Flowable.class, io.reactivex.Flowable::empty), source -> (io.reactivex.Flowable<?>) source, io.reactivex.Flowable::fromPublisher);
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(io.reactivex.Observable.class, io.reactivex.Observable::empty), source -> ((io.reactivex.Observable<?>) source).toFlowable(io.reactivex.BackpressureStrategy.BUFFER), io.reactivex.Observable::fromPublisher);
            registry.registerReactiveType(ReactiveTypeDescriptor.singleRequiredValue(io.reactivex.Single.class), source -> ((io.reactivex.Single<?>) source).toFlowable(), io.reactivex.Single::fromPublisher);
            registry.registerReactiveType(ReactiveTypeDescriptor.singleOptionalValue(io.reactivex.Maybe.class, io.reactivex.Maybe::empty), source -> ((io.reactivex.Maybe<?>) source).toFlowable(), source -> io.reactivex.Flowable.fromPublisher(source).toObservable().singleElement());
            registry.registerReactiveType(ReactiveTypeDescriptor.noValue(io.reactivex.Completable.class, io.reactivex.Completable::complete), source -> ((io.reactivex.Completable) source).toFlowable(), io.reactivex.Completable::fromPublisher);
        }
    }

    private static class RxJava3Registrar {

        void registerAdapters(ReactiveAdapterRegistry registry) {
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(io.reactivex.rxjava3.core.Flowable.class, io.reactivex.rxjava3.core.Flowable::empty), source -> (io.reactivex.rxjava3.core.Flowable<?>) source, io.reactivex.rxjava3.core.Flowable::fromPublisher);
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(io.reactivex.rxjava3.core.Observable.class, io.reactivex.rxjava3.core.Observable::empty), source -> ((io.reactivex.rxjava3.core.Observable<?>) source).toFlowable(io.reactivex.rxjava3.core.BackpressureStrategy.BUFFER), io.reactivex.rxjava3.core.Observable::fromPublisher);
            registry.registerReactiveType(ReactiveTypeDescriptor.singleRequiredValue(io.reactivex.rxjava3.core.Single.class), source -> ((io.reactivex.rxjava3.core.Single<?>) source).toFlowable(), io.reactivex.rxjava3.core.Single::fromPublisher);
            registry.registerReactiveType(ReactiveTypeDescriptor.singleOptionalValue(io.reactivex.rxjava3.core.Maybe.class, io.reactivex.rxjava3.core.Maybe::empty), source -> ((io.reactivex.rxjava3.core.Maybe<?>) source).toFlowable(), io.reactivex.rxjava3.core.Maybe::fromPublisher);
            registry.registerReactiveType(ReactiveTypeDescriptor.noValue(io.reactivex.rxjava3.core.Completable.class, io.reactivex.rxjava3.core.Completable::complete), source -> ((io.reactivex.rxjava3.core.Completable) source).toFlowable(), io.reactivex.rxjava3.core.Completable::fromPublisher);
        }
    }

    private static class ReactorAdapter extends ReactiveAdapter {

        ReactorAdapter(ReactiveTypeDescriptor descriptor, Function<Object, Publisher<?>> toPublisherFunction, Function<Publisher<?>, Object> fromPublisherFunction) {
            super(descriptor, toPublisherFunction, fromPublisherFunction);
        }

        @Override
        public <T> Publisher<T> toPublisher(@Nullable Object source) {
            Publisher<T> publisher = super.toPublisher(source);
            return (isMultiValue() ? Flux.from(publisher) : Mono.from(publisher));
        }
    }

    private static class EmptyCompletableFuture<T> extends CompletableFuture<T> {

        EmptyCompletableFuture() {
            complete(null);
        }
    }

    private static class CoroutinesRegistrar {

        @SuppressWarnings("KotlinInternalInJava")
        void registerAdapters(ReactiveAdapterRegistry registry) {
            registry.registerReactiveType(ReactiveTypeDescriptor.singleOptionalValue(Deferred.class, () -> CompletableDeferredKt.CompletableDeferred(null)), source -> CoroutinesUtils.deferredToMono((Deferred<?>) source), source -> CoroutinesUtils.monoToDeferred(Mono.from(source)));
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(kotlinx.coroutines.flow.Flow.class, kotlinx.coroutines.flow.FlowKt::emptyFlow), source -> kotlinx.coroutines.reactor.ReactorFlowKt.asFlux((kotlinx.coroutines.flow.Flow<?>) source), kotlinx.coroutines.reactive.ReactiveFlowKt::asFlow);
        }
    }

    private static class MutinyRegistrar {

        void registerAdapters(ReactiveAdapterRegistry registry) {
            registry.registerReactiveType(ReactiveTypeDescriptor.singleOptionalValue(io.smallrye.mutiny.Uni.class, () -> io.smallrye.mutiny.Uni.createFrom().nothing()), uni -> ((io.smallrye.mutiny.Uni<?>) uni).convert().toPublisher(), publisher -> io.smallrye.mutiny.Uni.createFrom().publisher(publisher));
            registry.registerReactiveType(ReactiveTypeDescriptor.multiValue(io.smallrye.mutiny.Multi.class, () -> io.smallrye.mutiny.Multi.createFrom().empty()), multi -> (io.smallrye.mutiny.Multi<?>) multi, publisher -> io.smallrye.mutiny.Multi.createFrom().publisher(publisher));
        }
    }

    public static class SpringCoreBlockHoundIntegration implements BlockHoundIntegration {

        @Override
        public void applyTo(BlockHound.Builder builder) {
            builder.allowBlockingCallsInside("org.springframework.core.LocalVariableTableParameterNameDiscoverer", "inspectClass");
            String className = "org.springframework.util.ConcurrentReferenceHashMap$Segment";
            builder.allowBlockingCallsInside(className, "doTask");
            builder.allowBlockingCallsInside(className, "clear");
            builder.allowBlockingCallsInside(className, "restructure");
        }
    }
}
