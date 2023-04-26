package org.apache.beam.sdk.transforms;

import org.apache.beam.sdk.coders.CannotProvideCoderException;
import org.apache.beam.sdk.coders.Coder;
import org.apache.beam.sdk.coders.IterableLikeCoder;
import org.apache.beam.sdk.transforms.windowing.WindowFn;
import org.apache.beam.sdk.util.WindowingStrategy;
import org.apache.beam.sdk.values.PCollection;
import org.apache.beam.sdk.values.PCollection.IsBounded;
import org.apache.beam.sdk.values.PCollectionList;

public class Flatten {

    public static <T> FlattenPCollectionList<T> pCollections() {
        return new FlattenPCollectionList<>();
    }

    public static <T> FlattenIterables<T> iterables() {
        return new FlattenIterables<>();
    }

    public static class FlattenPCollectionList<T> extends PTransform<PCollectionList<T>, PCollection<T>> {

        private FlattenPCollectionList() {
        }

        @Override
        public PCollection<T> apply(PCollectionList<T> inputs) {
            WindowingStrategy<?, ?> windowingStrategy;
            IsBounded isBounded = IsBounded.BOUNDED;
            if (!inputs.getAll().isEmpty()) {
                windowingStrategy = inputs.get(0).getWindowingStrategy();
                for (PCollection<?> input : inputs.getAll()) {
                    WindowingStrategy<?, ?> other = input.getWindowingStrategy();
                    if (!windowingStrategy.getWindowFn().isCompatible(other.getWindowFn())) {
                        throw new IllegalStateException("Inputs to Flatten had incompatible window windowFns: " + windowingStrategy.getWindowFn() + ", " + other.getWindowFn());
                    }
                    if (!windowingStrategy.getTrigger().getSpec().isCompatible(other.getTrigger().getSpec())) {
                        throw new IllegalStateException("Inputs to Flatten had incompatible triggers: " + windowingStrategy.getTrigger() + ", " + other.getTrigger());
                    }
                    isBounded = isBounded.and(input.isBounded());
                }
            } else {
                windowingStrategy = WindowingStrategy.globalDefault();
            }
            return PCollection.<T>createPrimitiveOutputInternal(inputs.getPipeline(), windowingStrategy, isBounded);
        }

        @Override
        protected Coder<?> getDefaultOutputCoder(PCollectionList<T> input) throws CannotProvideCoderException {
            for (PCollection<T> pCollection : input.getAll()) {
                return pCollection.getCoder();
            }
            throw new CannotProvideCoderException(this.getClass().getSimpleName() + " cannot provide a Coder for" + " empty " + PCollectionList.class.getSimpleName());
        }
    }

    public static class FlattenIterables<T> extends PTransform<PCollection<? extends Iterable<T>>, PCollection<T>> {

        @Override
        public PCollection<T> apply(PCollection<? extends Iterable<T>> in) {
            Coder<? extends Iterable<T>> inCoder = in.getCoder();
            if (!(inCoder instanceof IterableLikeCoder)) {
                throw new IllegalArgumentException("expecting the input Coder<Iterable> to be an IterableLikeCoder");
            }
            @SuppressWarnings("unchecked")
            Coder<T> elemCoder = ((IterableLikeCoder<T, ?>) inCoder).getElemCoder();
            return in.apply(ParDo.named("FlattenIterables").of(new DoFn<Iterable<T>, T>() {

                @Override
                public void processElement(ProcessContext c) {
                    for (T i : c.element()) {
                        c.output(i);
                    }
                }
            })).setCoder(elemCoder);
        }
    }
}
