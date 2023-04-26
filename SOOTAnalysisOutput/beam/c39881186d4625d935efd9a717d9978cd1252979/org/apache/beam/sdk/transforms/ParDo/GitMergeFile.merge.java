package org.apache.beam.sdk.transforms;

import org.apache.beam.sdk.Pipeline;
import org.apache.beam.sdk.annotations.Experimental;
import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.runners.PipelineRunner;
import org.apache.beam.sdk.transforms.display.DisplayData;
import org.apache.beam.sdk.transforms.display.DisplayData.Builder;
import org.apache.beam.sdk.transforms.windowing.WindowFn;
import org.apache.beam.sdk.util.SerializableUtils;
import org.apache.beam.sdk.util.StringUtils;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollectionTuple;
import org.apache.beam.sdk.values.PCollectionView;
import org.apache.beam.sdk.values.TupleTag;
import org.apache.beam.sdk.values.TupleTagList;
import org.apache.beam.sdk.values.TypedPValue;
import com.google.common.collect.ImmutableList;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

public class ParDo {

    public static Unbound withSideInputs(PCollectionView<?>... sideInputs) {
        return new Unbound().withSideInputs(sideInputs);
    }

    public static Unbound withSideInputs(Iterable<? extends PCollectionView<?>> sideInputs) {
        return new Unbound().withSideInputs(sideInputs);
    }

    public static <OutputT> UnboundMulti<OutputT> withOutputTags(TupleTag<OutputT> mainOutputTag, TupleTagList sideOutputTags) {
        return new Unbound().withOutputTags(mainOutputTag, sideOutputTags);
    }

    public static <InputT, OutputT> Bound<InputT, OutputT> of(OldDoFn<InputT, OutputT> fn) {
        return of(fn, fn.getClass());
    }

    private static <InputT, OutputT> Bound<InputT, OutputT> of(OldDoFn<InputT, OutputT> fn, Class<?> fnClass) {
        return new Unbound().of(fn, fnClass);
    }

    private static <InputT, OutputT> OldDoFn<InputT, OutputT> adapt(DoFn<InputT, OutputT> fn) {
        return DoFnReflector.of(fn.getClass()).toDoFn(fn);
    }

    @Experimental
    public static <InputT, OutputT> Bound<InputT, OutputT> of(DoFn<InputT, OutputT> fn) {
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

        public <InputT, OutputT> Bound<InputT, OutputT> of(OldDoFn<InputT, OutputT> fn) {
            return of(fn, fn.getClass());
        }

        private <InputT, OutputT> Bound<InputT, OutputT> of(OldDoFn<InputT, OutputT> fn, Class<?> fnClass) {
            return new Bound<>(name, sideInputs, fn, fnClass);
        }

        public <InputT, OutputT> Bound<InputT, OutputT> of(DoFn<InputT, OutputT> fn) {
            return of(adapt(fn), fn.getClass());
        }
    }

    public static class Bound<InputT, OutputT> extends PTransform<PCollection<? extends InputT>, PCollection<OutputT>> {

        private final List<PCollectionView<?>> sideInputs;

        private final OldDoFn<InputT, OutputT> fn;

        private final Class<?> fnClass;

        Bound(String name, List<PCollectionView<?>> sideInputs, OldDoFn<InputT, OutputT> fn, Class<?> fnClass) {
            super(name);
            this.sideInputs = sideInputs;
            this.fn = SerializableUtils.clone(fn);
            this.fnClass = fnClass;
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

        public OldDoFn<InputT, OutputT> getFn() {
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

        public UnboundMulti<OutputT> withSideInputs(PCollectionView<?>... sideInputs) {
            return withSideInputs(Arrays.asList(sideInputs));
        }

        public UnboundMulti<OutputT> withSideInputs(Iterable<? extends PCollectionView<?>> sideInputs) {
            ImmutableList.Builder<PCollectionView<?>> builder = ImmutableList.builder();
            builder.addAll(this.sideInputs);
            builder.addAll(sideInputs);
            return new UnboundMulti<>(name, builder.build(), mainOutputTag, sideOutputTags);
        }

        public <InputT> BoundMulti<InputT, OutputT> of(OldDoFn<InputT, OutputT> fn) {
            return of(fn, fn.getClass());
        }

        public <InputT> BoundMulti<InputT, OutputT> of(OldDoFn<InputT, OutputT> fn, Class<?> fnClass) {
            return new BoundMulti<>(name, sideInputs, mainOutputTag, sideOutputTags, fn, fnClass);
        }

        public <InputT> BoundMulti<InputT, OutputT> of(DoFn<InputT, OutputT> fn) {
            return of(adapt(fn), fn.getClass());
        }
    }

    public static class BoundMulti<InputT, OutputT> extends PTransform<PCollection<? extends InputT>, PCollectionTuple> {

        private final List<PCollectionView<?>> sideInputs;

        private final TupleTag<OutputT> mainOutputTag;

        private final TupleTagList sideOutputTags;

        private final OldDoFn<InputT, OutputT> fn;

        private final Class<?> fnClass;

        BoundMulti(String name, List<PCollectionView<?>> sideInputs, TupleTag<OutputT> mainOutputTag, TupleTagList sideOutputTags, OldDoFn<InputT, OutputT> fn, Class<?> fnClass) {
            super(name);
            this.sideInputs = sideInputs;
            this.mainOutputTag = mainOutputTag;
            this.sideOutputTags = sideOutputTags;
            this.fn = SerializableUtils.clone(fn);
            this.fnClass = fnClass;
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
            if (clazz.isAnonymousClass()) {
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

        public OldDoFn<InputT, OutputT> getFn() {
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

<<<<<<< MINE
    private static void populateDisplayData(DisplayData.Builder builder, DoFn<?, ?> fn, Class<?> fnClass) {
=======
    private static void populateDisplayData(DisplayData.Builder builder, OldDoFn<?, ?> fn, Class<?> fnClass) {
>>>>>>> YOURS
        builder.include(fn).add(DisplayData.item("fn", fnClass).withLabel("Transform Function"));
    }
}
