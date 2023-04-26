package com.google.cloud.dataflow.sdk.transforms;

import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.coders.Coder.NonDeterministicException;
import com.google.cloud.dataflow.sdk.coders.IterableCoder;
import com.google.cloud.dataflow.sdk.coders.KvCoder;
import com.google.cloud.dataflow.sdk.transforms.windowing.DefaultTrigger;
import com.google.cloud.dataflow.sdk.transforms.windowing.GlobalWindows;
import com.google.cloud.dataflow.sdk.transforms.windowing.InvalidWindows;
import com.google.cloud.dataflow.sdk.transforms.windowing.Window;
import com.google.cloud.dataflow.sdk.transforms.windowing.WindowFn;
import com.google.cloud.dataflow.sdk.util.WindowingStrategy;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.PCollection.IsBounded;

public class GroupByKey<K, V> extends PTransform<PCollection<KV<K, V>>, PCollection<KV<K, Iterable<V>>>> {

    private final boolean fewKeys;

    private GroupByKey(boolean fewKeys) {
        this.fewKeys = fewKeys;
    }

    public static <K, V> GroupByKey<K, V> create() {
        return new GroupByKey<>(false);
    }

    static <K, V> GroupByKey<K, V> create(boolean fewKeys) {
        return new GroupByKey<>(fewKeys);
    }

    public boolean fewKeys() {
        return fewKeys;
    }

    public static void applicableTo(PCollection<?> input) {
        WindowingStrategy<?, ?> windowingStrategy = input.getWindowingStrategy();
        if (windowingStrategy.getWindowFn() instanceof GlobalWindows && windowingStrategy.getTrigger().getSpec() instanceof DefaultTrigger && input.isBounded() != IsBounded.BOUNDED) {
            throw new IllegalStateException("GroupByKey cannot be applied to non-bounded PCollection in " + "the GlobalWindow without a trigger. Use a Window.into or Window.triggering transform " + "prior to GroupByKey.");
        }
        if (windowingStrategy.getWindowFn() instanceof InvalidWindows) {
            String cause = ((InvalidWindows<?>) windowingStrategy.getWindowFn()).getCause();
            throw new IllegalStateException("GroupByKey must have a valid Window merge function.  " + "Invalid because: " + cause);
        }
    }

    @Override
    public void validate(PCollection<KV<K, V>> input) {
        applicableTo(input);
        Coder<K> keyCoder = getKeyCoder(input.getCoder());
        try {
            keyCoder.verifyDeterministic();
        } catch (NonDeterministicException e) {
            throw new IllegalStateException("the keyCoder of a GroupByKey must be deterministic", e);
        }
    }

    public WindowingStrategy<?, ?> updateWindowingStrategy(WindowingStrategy<?, ?> inputStrategy) {
        WindowFn<?, ?> inputWindowFn = inputStrategy.getWindowFn();
        if (!inputWindowFn.isNonMerging()) {
            inputWindowFn = new InvalidWindows<>("WindowFn has already been consumed by previous GroupByKey", inputWindowFn);
        }
        return inputStrategy.withWindowFn(inputWindowFn).withTrigger(inputStrategy.getTrigger().getSpec().getContinuationTrigger());
    }

    @Override
    public PCollection<KV<K, Iterable<V>>> apply(PCollection<KV<K, V>> input) {
        return PCollection.createPrimitiveOutputInternal(input.getPipeline(), updateWindowingStrategy(input.getWindowingStrategy()), input.isBounded());
    }

    @Override
    protected Coder<KV<K, Iterable<V>>> getDefaultOutputCoder(PCollection<KV<K, V>> input) {
        return getOutputKvCoder(input.getCoder());
    }

    @SuppressWarnings("unchecked")
    static <K, V> KvCoder<K, V> getInputKvCoder(Coder<KV<K, V>> inputCoder) {
        if (!(inputCoder instanceof KvCoder)) {
            throw new IllegalStateException("GroupByKey requires its input to use KvCoder");
        }
        return (KvCoder<K, V>) inputCoder;
    }

    public static <K, V> Coder<K> getKeyCoder(Coder<KV<K, V>> inputCoder) {
        return getInputKvCoder(inputCoder).getKeyCoder();
    }

    public static <K, V> Coder<V> getInputValueCoder(Coder<KV<K, V>> inputCoder) {
        return getInputKvCoder(inputCoder).getValueCoder();
    }

    static <K, V> Coder<Iterable<V>> getOutputValueCoder(Coder<KV<K, V>> inputCoder) {
        return IterableCoder.of(getInputValueCoder(inputCoder));
    }

    public static <K, V> KvCoder<K, Iterable<V>> getOutputKvCoder(Coder<KV<K, V>> inputCoder) {
        return KvCoder.of(getKeyCoder(inputCoder), getOutputValueCoder(inputCoder));
    }
}
