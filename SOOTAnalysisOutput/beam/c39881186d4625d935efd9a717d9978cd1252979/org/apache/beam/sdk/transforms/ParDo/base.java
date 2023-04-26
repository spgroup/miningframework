package org.apache.beam.sdk.transforms;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.CoderException;
import org.apache.beam.sdk.runners.DirectPipelineRunner;
import org.apache.beam.sdk.runners.PipelineRunner;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.transforms.display.DisplayData.Builder;
import org.apache.beam.sdk.transforms.windowing.WindowFn;
import org.apache.beam.sdk.util.DirectModeExecutionContext;
import org.apache.beam.sdk.util.DirectSideInputReader;
import org.apache.beam.sdk.util.DoFnRunner;
import org.apache.beam.sdk.util.DoFnRunnerBase;
import org.apache.beam.sdk.util.DoFnRunners;
import org.apache.beam.sdk.util.IllegalMutationException;
import org.apache.beam.sdk.util.MutationDetector;
import org.apache.beam.sdk.util.MutationDetectors;
import org.apache.beam.sdk.util.PTuple;
import org.apache.beam.sdk.util.SerializableUtils;
import org.apache.beam.sdk.util.SideInputReader;
import org.apache.beam.sdk.util.StringUtils;
import org.apache.beam.sdk.util.UserCodeException;
import org.apache.beam.sdk.util.WindowedValue;
import org.apache.beam.sdk.values.KV;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.beam.sdk.values.TypedPValue;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;
import javax.annotation.Nullable;

public class ParDo {

    public static Unbound named(String name) {
        return new Unbound().named(name);
    }

    public static Unbound withSideInputs(PCollectionView<?>... sideInputs) {
        return new Unbound().withSideInputs(sideInputs);
    }

    public static Unbound withSideInputs(Iterable<? extends PCollectionView<?>> sideInputs) {
        return new Unbound().withSideInputs(sideInputs);
    }

    public static <OutputT> UnboundMulti<OutputT> withOutputTags(TupleTag<OutputT> mainOutputTag, TupleTagList sideOutputTags) {
        return new Unbound().withOutputTags(mainOutputTag, sideOutputTags);
    }

    public static <InputT, OutputT> Bound<InputT, OutputT> of(DoFn<InputT, OutputT> fn) {
        return of(fn, fn.getClass());
    }

    private static <InputT, OutputT> Bound<InputT, OutputT> of(DoFn<InputT, OutputT> fn, Class<?> fnClass) {
        return new Unbound().of(fn, fnClass);
    }

    private static <InputT, OutputT> DoFn<InputT, OutputT> adapt(DoFnWithContext<InputT, OutputT> fn) {
        return DoFnReflector.of(fn.getClass()).toDoFn(fn);
    }

    @Experimental
    public static <InputT, OutputT> Bound<InputT, OutputT> of(DoFnWithContext<InputT, OutputT> fn) {
        return of(adapt(fn), fn.getClass());
    }

    public static class Unbound {

        private final String name;

        private final List<PCollectionView<?>> sideInputs;

        Unbound() {
            this(null, ImmutableList.<PCollectionView<?>>of());
        }

        Unbound(String name, List<PCollectionView<?>> sideInputs) {
            this.name = name;
            this.sideInputs = sideInputs;
        }

        public Unbound named(String name) {
            return new Unbound(name, sideInputs);
        }

        public Unbound withSideInputs(PCollectionView<?>... sideInputs) {
            return withSideInputs(Arrays.asList(sideInputs));
        }

        public Unbound withSideInputs(Iterable<? extends PCollectionView<?>> sideInputs) {
            ImmutableList.Builder<PCollectionView<?>> builder = ImmutableList.builder();
            builder.addAll(this.sideInputs);
            builder.addAll(sideInputs);
            return new Unbound(name, builder.build());
        }

        public <OutputT> UnboundMulti<OutputT> withOutputTags(TupleTag<OutputT> mainOutputTag, TupleTagList sideOutputTags) {
            return new UnboundMulti<>(name, sideInputs, mainOutputTag, sideOutputTags);
        }

        public <InputT, OutputT> Bound<InputT, OutputT> of(DoFn<InputT, OutputT> fn) {
            return of(fn, fn.getClass());
        }

        private <InputT, OutputT> Bound<InputT, OutputT> of(DoFn<InputT, OutputT> fn, Class<?> fnClass) {
            return new Bound<>(name, sideInputs, fn, fnClass);
        }

        public <InputT, OutputT> Bound<InputT, OutputT> of(DoFnWithContext<InputT, OutputT> fn) {
            return of(adapt(fn), fn.getClass());
        }
    }

    public static class Bound<InputT, OutputT> extends PTransform<PCollection<? extends InputT>, PCollection<OutputT>> {

        private final List<PCollectionView<?>> sideInputs;

        private final DoFn<InputT, OutputT> fn;

        private final Class<?> fnClass;

        Bound(String name, List<PCollectionView<?>> sideInputs, DoFn<InputT, OutputT> fn, Class<?> fnClass) {
            super(name);
            this.sideInputs = sideInputs;
            this.fn = SerializableUtils.clone(fn);
            this.fnClass = fnClass;
        }

        public Bound<InputT, OutputT> named(String name) {
            return new Bound<>(name, sideInputs, fn, fnClass);
        }

        public Bound<InputT, OutputT> withSideInputs(PCollectionView<?>... sideInputs) {
            return withSideInputs(Arrays.asList(sideInputs));
        }

        public Bound<InputT, OutputT> withSideInputs(Iterable<? extends PCollectionView<?>> sideInputs) {
            ImmutableList.Builder<PCollectionView<?>> builder = ImmutableList.builder();
            builder.addAll(this.sideInputs);
            builder.addAll(sideInputs);
            return new Bound<>(name, builder.build(), fn, fnClass);
        }

        public BoundMulti<InputT, OutputT> withOutputTags(TupleTag<OutputT> mainOutputTag, TupleTagList sideOutputTags) {
            return new BoundMulti<>(name, sideInputs, mainOutputTag, sideOutputTags, fn, fnClass);
        }

        @Override
        public PCollection<OutputT> apply(PCollection<? extends InputT> input) {
            return PCollection.<OutputT>createPrimitiveOutputInternal(input.getPipeline(), input.getWindowingStrategy(), input.isBounded()).setTypeDescriptorInternal(fn.getOutputTypeDescriptor());
        }

        @Override
        @SuppressWarnings("unchecked")
        protected Coder<OutputT> getDefaultOutputCoder(PCollection<? extends InputT> input) throws CannotProvideCoderException {
            return input.getPipeline().getCoderRegistry().getDefaultCoder(fn.getOutputTypeDescriptor(), fn.getInputTypeDescriptor(), ((PCollection<InputT>) input).getCoder());
        }

        @Override
        protected String getKindString() {
            Class<?> clazz = DoFnReflector.getDoFnClass(fn);
            if (clazz.isAnonymousClass()) {
                return "AnonymousParDo";
            } else {
                return String.format("ParDo(%s)", StringUtils.approximateSimpleName(clazz));
            }
        }

        @Override
        public void populateDisplayData(Builder builder) {
            super.populateDisplayData(builder);
            ParDo.populateDisplayData(builder, fn, fnClass);
        }

        public DoFn<InputT, OutputT> getFn() {
            return fn;
        }

        public List<PCollectionView<?>> getSideInputs() {
            return sideInputs;
        }
    }

    public static class UnboundMulti<OutputT> {

        private final String name;

        private final List<PCollectionView<?>> sideInputs;

        private final TupleTag<OutputT> mainOutputTag;

        private final TupleTagList sideOutputTags;

        UnboundMulti(String name, List<PCollectionView<?>> sideInputs, TupleTag<OutputT> mainOutputTag, TupleTagList sideOutputTags) {
            this.name = name;
            this.sideInputs = sideInputs;
            this.mainOutputTag = mainOutputTag;
            this.sideOutputTags = sideOutputTags;
        }

        public UnboundMulti<OutputT> named(String name) {
            return new UnboundMulti<>(name, sideInputs, mainOutputTag, sideOutputTags);
        }

        public UnboundMulti<OutputT> withSideInputs(PCollectionView<?>... sideInputs) {
            return withSideInputs(Arrays.asList(sideInputs));
        }

        public UnboundMulti<OutputT> withSideInputs(Iterable<? extends PCollectionView<?>> sideInputs) {
            ImmutableList.Builder<PCollectionView<?>> builder = ImmutableList.builder();
            builder.addAll(this.sideInputs);
            builder.addAll(sideInputs);
            return new UnboundMulti<>(name, builder.build(), mainOutputTag, sideOutputTags);
        }

        public <InputT> BoundMulti<InputT, OutputT> of(DoFn<InputT, OutputT> fn) {
            return of(fn, fn.getClass());
        }

        public <InputT> BoundMulti<InputT, OutputT> of(DoFn<InputT, OutputT> fn, Class<?> fnClass) {
            return new BoundMulti<>(name, sideInputs, mainOutputTag, sideOutputTags, fn, fnClass);
        }

        public <InputT> BoundMulti<InputT, OutputT> of(DoFnWithContext<InputT, OutputT> fn) {
            return of(adapt(fn), fn.getClass());
        }
    }

    public static class BoundMulti<InputT, OutputT> extends PTransform<PCollection<? extends InputT>, PCollectionTuple> {

        private final List<PCollectionView<?>> sideInputs;

        private final TupleTag<OutputT> mainOutputTag;

        private final TupleTagList sideOutputTags;

        private final DoFn<InputT, OutputT> fn;

        private final Class<?> fnClass;

        BoundMulti(String name, List<PCollectionView<?>> sideInputs, TupleTag<OutputT> mainOutputTag, TupleTagList sideOutputTags, DoFn<InputT, OutputT> fn, Class<?> fnClass) {
            super(name);
            this.sideInputs = sideInputs;
            this.mainOutputTag = mainOutputTag;
            this.sideOutputTags = sideOutputTags;
            this.fn = SerializableUtils.clone(fn);
            this.fnClass = fnClass;
        }

        public BoundMulti<InputT, OutputT> named(String name) {
            return new BoundMulti<>(name, sideInputs, mainOutputTag, sideOutputTags, fn, fnClass);
        }

        public BoundMulti<InputT, OutputT> withSideInputs(PCollectionView<?>... sideInputs) {
            return withSideInputs(Arrays.asList(sideInputs));
        }

        public BoundMulti<InputT, OutputT> withSideInputs(Iterable<? extends PCollectionView<?>> sideInputs) {
            ImmutableList.Builder<PCollectionView<?>> builder = ImmutableList.builder();
            builder.addAll(this.sideInputs);
            builder.addAll(sideInputs);
            return new BoundMulti<>(name, builder.build(), mainOutputTag, sideOutputTags, fn, fnClass);
        }

        @Override
        public PCollectionTuple apply(PCollection<? extends InputT> input) {
            PCollectionTuple outputs = PCollectionTuple.ofPrimitiveOutputsInternal(input.getPipeline(), TupleTagList.of(mainOutputTag).and(sideOutputTags.getAll()), input.getWindowingStrategy(), input.isBounded());
            outputs.get(mainOutputTag).setTypeDescriptorInternal(fn.getOutputTypeDescriptor());
            return outputs;
        }

        @Override
        protected Coder<OutputT> getDefaultOutputCoder() {
            throw new RuntimeException("internal error: shouldn't be calling this on a multi-output ParDo");
        }

        @Override
        public <T> Coder<T> getDefaultOutputCoder(PCollection<? extends InputT> input, TypedPValue<T> output) throws CannotProvideCoderException {
            @SuppressWarnings("unchecked")
            Coder<InputT> inputCoder = ((PCollection<InputT>) input).getCoder();
            return input.getPipeline().getCoderRegistry().getDefaultCoder(output.getTypeDescriptor(), fn.getInputTypeDescriptor(), inputCoder);
        }

        @Override
        protected String getKindString() {
            Class<?> clazz = DoFnReflector.getDoFnClass(fn);
            if (fn.getClass().isAnonymousClass()) {
                return "AnonymousParMultiDo";
            } else {
                return String.format("ParMultiDo(%s)", StringUtils.approximateSimpleName(clazz));
            }
        }

        @Override
        public void populateDisplayData(Builder builder) {
            super.populateDisplayData(builder);
            ParDo.populateDisplayData(builder, fn, fnClass);
        }

        public DoFn<InputT, OutputT> getFn() {
            return fn;
        }

        public TupleTag<OutputT> getMainOutputTag() {
            return mainOutputTag;
        }

        public TupleTagList getSideOutputTags() {
            return sideOutputTags;
        }

        public List<PCollectionView<?>> getSideInputs() {
            return sideInputs;
        }
    }

    static {
        DirectPipelineRunner.registerDefaultTransformEvaluator(Bound.class, new DirectPipelineRunner.TransformEvaluator<Bound>() {

            @Override
            public void evaluate(Bound transform, DirectPipelineRunner.EvaluationContext context) {
                evaluateSingleHelper(transform, context);
            }
        });
    }

    private static <InputT, OutputT> void evaluateSingleHelper(Bound<InputT, OutputT> transform, DirectPipelineRunner.EvaluationContext context) {
        TupleTag<OutputT> mainOutputTag = new TupleTag<>("out");
        DirectModeExecutionContext executionContext = DirectModeExecutionContext.create();
        PCollectionTuple outputs = PCollectionTuple.of(mainOutputTag, context.getOutput(transform));
        evaluateHelper(transform.fn, context.getStepName(transform), context.getInput(transform), transform.sideInputs, mainOutputTag, Collections.<TupleTag<?>>emptyList(), outputs, context, executionContext);
        context.setPCollectionValuesWithMetadata(context.getOutput(transform), executionContext.getOutput(mainOutputTag));
    }

    static {
        DirectPipelineRunner.registerDefaultTransformEvaluator(BoundMulti.class, new DirectPipelineRunner.TransformEvaluator<BoundMulti>() {

            @Override
            public void evaluate(BoundMulti transform, DirectPipelineRunner.EvaluationContext context) {
                evaluateMultiHelper(transform, context);
            }
        });
    }

    private static <InputT, OutputT> void evaluateMultiHelper(BoundMulti<InputT, OutputT> transform, DirectPipelineRunner.EvaluationContext context) {
        DirectModeExecutionContext executionContext = DirectModeExecutionContext.create();
        evaluateHelper(transform.fn, context.getStepName(transform), context.getInput(transform), transform.sideInputs, transform.mainOutputTag, transform.sideOutputTags.getAll(), context.getOutput(transform), context, executionContext);
        for (Map.Entry<TupleTag<?>, PCollection<?>> entry : context.getOutput(transform).getAll().entrySet()) {
            @SuppressWarnings("unchecked")
            TupleTag<Object> tag = (TupleTag<Object>) entry.getKey();
            @SuppressWarnings("unchecked")
            PCollection<Object> pc = (PCollection<Object>) entry.getValue();
            context.setPCollectionValuesWithMetadata(pc, (tag == transform.mainOutputTag ? executionContext.getOutput(tag) : executionContext.getSideOutput(tag)));
        }
    }

    private static <InputT, OutputT, ActualInputT extends InputT> void evaluateHelper(DoFn<InputT, OutputT> doFn, String stepName, PCollection<ActualInputT> input, List<PCollectionView<?>> sideInputs, TupleTag<OutputT> mainOutputTag, List<TupleTag<?>> sideOutputTags, PCollectionTuple outputs, DirectPipelineRunner.EvaluationContext context, DirectModeExecutionContext executionContext) {
        DoFn<InputT, OutputT> fn = context.ensureSerializable(doFn);
        SideInputReader sideInputReader = makeSideInputReader(context, sideInputs);
        ImmutabilityCheckingOutputManager<ActualInputT> outputManager = new ImmutabilityCheckingOutputManager<>(fn.getClass().getSimpleName(), new DoFnRunnerBase.ListOutputManager(), outputs);
        DoFnRunner<InputT, OutputT> fnRunner = DoFnRunners.createDefault(context.getPipelineOptions(), fn, sideInputReader, outputManager, mainOutputTag, sideOutputTags, executionContext.getOrCreateStepContext(stepName, stepName), context.getAddCounterMutator(), input.getWindowingStrategy());
        fnRunner.startBundle();
        for (DirectPipelineRunner.ValueWithMetadata<ActualInputT> elem : context.getPCollectionValuesWithMetadata(input)) {
            if (elem.getValue() instanceof KV) {
                @SuppressWarnings("unchecked")
                KV<?, ?> kvElem = (KV<?, ?>) elem.getValue();
                executionContext.setKey(kvElem.getKey());
            } else {
                executionContext.setKey(elem.getKey());
            }
            try {
                MutationDetector inputMutationDetector = MutationDetectors.forValueWithCoder(elem.getWindowedValue().getValue(), input.getCoder());
                @SuppressWarnings("unchecked")
                WindowedValue<InputT> windowedElem = ((WindowedValue<InputT>) elem.getWindowedValue());
                fnRunner.processElement(windowedElem);
                inputMutationDetector.verifyUnmodified();
            } catch (CoderException e) {
                throw UserCodeException.wrap(e);
            } catch (IllegalMutationException exn) {
                throw new IllegalMutationException(String.format("DoFn %s mutated input value %s of class %s (new value was %s)." + " Input values must not be mutated in any way.", fn.getClass().getSimpleName(), exn.getSavedValue(), exn.getSavedValue().getClass(), exn.getNewValue()), exn.getSavedValue(), exn.getNewValue(), exn);
            }
        }
        fnRunner.finishBundle();
        outputManager.verifyLatestOutputsUnmodified();
    }

    private static SideInputReader makeSideInputReader(DirectPipelineRunner.EvaluationContext context, List<PCollectionView<?>> sideInputs) {
        PTuple sideInputValues = PTuple.empty();
        for (PCollectionView<?> view : sideInputs) {
            sideInputValues = sideInputValues.and(view.getTagInternal(), context.getPCollectionView(view));
        }
        return DirectSideInputReader.of(sideInputValues);
    }

    private static void populateDisplayData(DisplayData.Builder builder, DoFn<?, ?> fn, Class<?> fnClass) {
        builder.include(fn).add(DisplayData.item("fn", fnClass).withLabel("Transform Function"));
    }

    private static class ImmutabilityCheckingOutputManager<InputT> implements DoFnRunners.OutputManager, AutoCloseable {

        private final DoFnRunners.OutputManager underlyingOutputManager;

        private final ConcurrentMap<TupleTag<?>, MutationDetector> mutationDetectorForTag;

        private final PCollectionTuple outputs;

        private String doFnName;

        public ImmutabilityCheckingOutputManager(String doFnName, DoFnRunners.OutputManager underlyingOutputManager, PCollectionTuple outputs) {
            this.doFnName = doFnName;
            this.underlyingOutputManager = underlyingOutputManager;
            this.outputs = outputs;
            this.mutationDetectorForTag = Maps.newConcurrentMap();
        }

        @Override
        public <T> void output(TupleTag<T> tag, WindowedValue<T> output) {
            if (outputs.has(tag)) {
                try {
                    MutationDetector newDetector = MutationDetectors.forValueWithCoder(output.getValue(), outputs.get(tag).getCoder());
                    MutationDetector priorDetector = mutationDetectorForTag.put(tag, newDetector);
                    verifyOutputUnmodified(priorDetector);
                } catch (CoderException e) {
                    throw UserCodeException.wrap(e);
                }
            }
            underlyingOutputManager.output(tag, output);
        }

        public void verifyLatestOutputsUnmodified() {
            for (MutationDetector detector : mutationDetectorForTag.values()) {
                verifyOutputUnmodified(detector);
            }
        }

        private <T> void verifyOutputUnmodified(@Nullable MutationDetector detector) {
            if (detector == null) {
                return;
            }
            try {
                detector.verifyUnmodified();
            } catch (IllegalMutationException exn) {
                throw new IllegalMutationException(String.format("DoFn %s mutated value %s after it was output (new value was %s)." + " Values must not be mutated in any way after being output.", doFnName, exn.getSavedValue(), exn.getNewValue()), exn.getSavedValue(), exn.getNewValue(), exn);
            }
        }

        @Override
        public void close() {
            verifyLatestOutputsUnmodified();
        }
    }
}
