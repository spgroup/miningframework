package com.google.cloud.dataflow.sdk.transforms;

import static com.google.cloud.dataflow.sdk.util.CoderUtils.encodeToByteArray;
import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.coders.Coder.NonDeterministicException;
import com.google.cloud.dataflow.sdk.coders.CoderException;
import com.google.cloud.dataflow.sdk.coders.IterableCoder;
import com.google.cloud.dataflow.sdk.coders.KvCoder;
import com.google.cloud.dataflow.sdk.runners.DirectPipelineRunner;
import com.google.cloud.dataflow.sdk.runners.DirectPipelineRunner.ValueWithMetadata;
import com.google.cloud.dataflow.sdk.transforms.windowing.BoundedWindow;
import com.google.cloud.dataflow.sdk.transforms.windowing.DefaultTrigger;
import com.google.cloud.dataflow.sdk.transforms.windowing.GlobalWindows;
import com.google.cloud.dataflow.sdk.transforms.windowing.InvalidWindows;
import com.google.cloud.dataflow.sdk.transforms.windowing.Window;
import com.google.cloud.dataflow.sdk.transforms.windowing.WindowFn;
import com.google.cloud.dataflow.sdk.util.GroupAlsoByWindowsViaOutputBufferDoFn;
import com.google.cloud.dataflow.sdk.util.ReifyTimestampAndWindowsDoFn;
import com.google.cloud.dataflow.sdk.util.SystemReduceFn;
import com.google.cloud.dataflow.sdk.util.WindowedValue;
import com.google.cloud.dataflow.sdk.util.WindowedValue.FullWindowedValueCoder;
import com.google.cloud.dataflow.sdk.util.WindowedValue.WindowedValueCoder;
import com.google.cloud.dataflow.sdk.util.WindowingStrategy;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.PCollection.IsBounded;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        WindowingStrategy<?, ?> windowingStrategy = input.getWindowingStrategy();
        return input.apply(new ReifyTimestampsAndWindows<K, V>()).apply(new GroupByKeyOnly<K, WindowedValue<V>>()).apply(new SortValuesByTimestamp<K, V>()).apply(new GroupAlsoByWindow<K, V>(windowingStrategy)).setWindowingStrategyInternal(updateWindowingStrategy(windowingStrategy));
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

    static <K, V> Coder<K> getKeyCoder(Coder<KV<K, V>> inputCoder) {
        return getInputKvCoder(inputCoder).getKeyCoder();
    }

    public static <K, V> Coder<V> getInputValueCoder(Coder<KV<K, V>> inputCoder) {
        return getInputKvCoder(inputCoder).getValueCoder();
    }

    static <K, V> Coder<Iterable<V>> getOutputValueCoder(Coder<KV<K, V>> inputCoder) {
        return IterableCoder.of(getInputValueCoder(inputCoder));
    }

    static <K, V> KvCoder<K, Iterable<V>> getOutputKvCoder(Coder<KV<K, V>> inputCoder) {
        return KvCoder.of(getKeyCoder(inputCoder), getOutputValueCoder(inputCoder));
    }

    public static class ReifyTimestampsAndWindows<K, V> extends PTransform<PCollection<KV<K, V>>, PCollection<KV<K, WindowedValue<V>>>> {

        @Override
        public PCollection<KV<K, WindowedValue<V>>> apply(PCollection<KV<K, V>> input) {
            @SuppressWarnings("unchecked")
            KvCoder<K, V> inputKvCoder = (KvCoder<K, V>) input.getCoder();
            Coder<K> keyCoder = inputKvCoder.getKeyCoder();
            Coder<V> inputValueCoder = inputKvCoder.getValueCoder();
            Coder<WindowedValue<V>> outputValueCoder = FullWindowedValueCoder.of(inputValueCoder, input.getWindowingStrategy().getWindowFn().windowCoder());
            Coder<KV<K, WindowedValue<V>>> outputKvCoder = KvCoder.of(keyCoder, outputValueCoder);
            return input.apply(ParDo.of(new ReifyTimestampAndWindowsDoFn<K, V>())).setCoder(outputKvCoder);
        }
    }

    public static class SortValuesByTimestamp<K, V> extends PTransform<PCollection<KV<K, Iterable<WindowedValue<V>>>>, PCollection<KV<K, Iterable<WindowedValue<V>>>>> {

        @Override
        public PCollection<KV<K, Iterable<WindowedValue<V>>>> apply(PCollection<KV<K, Iterable<WindowedValue<V>>>> input) {
            return input.apply(ParDo.of(new DoFn<KV<K, Iterable<WindowedValue<V>>>, KV<K, Iterable<WindowedValue<V>>>>() {

                @Override
                public void processElement(ProcessContext c) {
                    KV<K, Iterable<WindowedValue<V>>> kvs = c.element();
                    K key = kvs.getKey();
                    Iterable<WindowedValue<V>> unsortedValues = kvs.getValue();
                    List<WindowedValue<V>> sortedValues = new ArrayList<>();
                    for (WindowedValue<V> value : unsortedValues) {
                        sortedValues.add(value);
                    }
                    Collections.sort(sortedValues, new Comparator<WindowedValue<V>>() {

                        @Override
                        public int compare(WindowedValue<V> e1, WindowedValue<V> e2) {
                            return e1.getTimestamp().compareTo(e2.getTimestamp());
                        }
                    });
                    c.output(KV.<K, Iterable<WindowedValue<V>>>of(key, sortedValues));
                }
            })).setCoder(input.getCoder());
        }
    }

    public static class GroupAlsoByWindow<K, V> extends PTransform<PCollection<KV<K, Iterable<WindowedValue<V>>>>, PCollection<KV<K, Iterable<V>>>> {

        private final WindowingStrategy<?, ?> windowingStrategy;

        public GroupAlsoByWindow(WindowingStrategy<?, ?> windowingStrategy) {
            this.windowingStrategy = windowingStrategy;
        }

        @Override
        @SuppressWarnings("unchecked")
        public PCollection<KV<K, Iterable<V>>> apply(PCollection<KV<K, Iterable<WindowedValue<V>>>> input) {
            @SuppressWarnings("unchecked")
            KvCoder<K, Iterable<WindowedValue<V>>> inputKvCoder = (KvCoder<K, Iterable<WindowedValue<V>>>) input.getCoder();
            Coder<K> keyCoder = inputKvCoder.getKeyCoder();
            Coder<Iterable<WindowedValue<V>>> inputValueCoder = inputKvCoder.getValueCoder();
            IterableCoder<WindowedValue<V>> inputIterableValueCoder = (IterableCoder<WindowedValue<V>>) inputValueCoder;
            Coder<WindowedValue<V>> inputIterableElementCoder = inputIterableValueCoder.getElemCoder();
            WindowedValueCoder<V> inputIterableWindowedValueCoder = (WindowedValueCoder<V>) inputIterableElementCoder;
            Coder<V> inputIterableElementValueCoder = inputIterableWindowedValueCoder.getValueCoder();
            Coder<Iterable<V>> outputValueCoder = IterableCoder.of(inputIterableElementValueCoder);
            Coder<KV<K, Iterable<V>>> outputKvCoder = KvCoder.of(keyCoder, outputValueCoder);
            return input.apply(ParDo.of(groupAlsoByWindowsFn(windowingStrategy, inputIterableElementValueCoder))).setCoder(outputKvCoder);
        }

        private <W extends BoundedWindow> GroupAlsoByWindowsViaOutputBufferDoFn<K, V, Iterable<V>, W> groupAlsoByWindowsFn(WindowingStrategy<?, W> strategy, Coder<V> inputIterableElementValueCoder) {
            return new GroupAlsoByWindowsViaOutputBufferDoFn<K, V, Iterable<V>, W>(strategy, SystemReduceFn.<K, V, W>buffering(inputIterableElementValueCoder));
        }
    }

    public static class GroupByKeyOnly<K, V> extends PTransform<PCollection<KV<K, V>>, PCollection<KV<K, Iterable<V>>>> {

        @SuppressWarnings({ "rawtypes", "unchecked" })
        @Override
        public PCollection<KV<K, Iterable<V>>> apply(PCollection<KV<K, V>> input) {
            return PCollection.<KV<K, Iterable<V>>>createPrimitiveOutputInternal(input.getPipeline(), input.getWindowingStrategy(), input.isBounded());
        }

        @SuppressWarnings("unchecked")
        KvCoder<K, V> getInputKvCoder(Coder<KV<K, V>> inputCoder) {
            if (!(inputCoder instanceof KvCoder)) {
                throw new IllegalStateException("GroupByKey requires its input to use KvCoder");
            }
            return (KvCoder<K, V>) inputCoder;
        }

        @Override
        protected Coder<KV<K, Iterable<V>>> getDefaultOutputCoder(PCollection<KV<K, V>> input) {
            return GroupByKey.getOutputKvCoder(input.getCoder());
        }
    }

    static {
        registerWithDirectPipelineRunner();
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    private static <K, V> void registerWithDirectPipelineRunner() {
        DirectPipelineRunner.registerDefaultTransformEvaluator(GroupByKeyOnly.class, new DirectPipelineRunner.TransformEvaluator<GroupByKeyOnly>() {

            @Override
            public void evaluate(GroupByKeyOnly transform, DirectPipelineRunner.EvaluationContext context) {
                evaluateHelper(transform, context);
            }
        });
    }

    private static <K, V> void evaluateHelper(GroupByKeyOnly<K, V> transform, DirectPipelineRunner.EvaluationContext context) {
        PCollection<KV<K, V>> input = context.getInput(transform);
        List<ValueWithMetadata<KV<K, V>>> inputElems = context.getPCollectionValuesWithMetadata(input);
        Coder<K> keyCoder = GroupByKey.getKeyCoder(input.getCoder());
        Map<GroupingKey<K>, List<V>> groupingMap = new HashMap<>();
        for (ValueWithMetadata<KV<K, V>> elem : inputElems) {
            K key = elem.getValue().getKey();
            V value = elem.getValue().getValue();
            byte[] encodedKey;
            try {
                encodedKey = encodeToByteArray(keyCoder, key);
            } catch (CoderException exn) {
                throw new IllegalArgumentException("unable to encode key " + key + " of input to " + transform + " using " + keyCoder, exn);
            }
            GroupingKey<K> groupingKey = new GroupingKey<>(key, encodedKey);
            List<V> values = groupingMap.get(groupingKey);
            if (values == null) {
                values = new ArrayList<V>();
                groupingMap.put(groupingKey, values);
            }
            values.add(value);
        }
        List<ValueWithMetadata<KV<K, Iterable<V>>>> outputElems = new ArrayList<>();
        for (Map.Entry<GroupingKey<K>, List<V>> entry : groupingMap.entrySet()) {
            GroupingKey<K> groupingKey = entry.getKey();
            K key = groupingKey.getKey();
            List<V> values = entry.getValue();
            values = context.randomizeIfUnordered(values, true);
            outputElems.add(ValueWithMetadata.of(WindowedValue.valueInEmptyWindows(KV.<K, Iterable<V>>of(key, values))).withKey(key));
        }
        context.setPCollectionValuesWithMetadata(context.getOutput(transform), outputElems);
    }

    private static class GroupingKey<K> {

        private K key;

        private byte[] encodedKey;

        public GroupingKey(K key, byte[] encodedKey) {
            this.key = key;
            this.encodedKey = encodedKey;
        }

        public K getKey() {
            return key;
        }

        @Override
        public boolean equals(Object o) {
            if (o instanceof GroupingKey) {
                GroupingKey<?> that = (GroupingKey<?>) o;
                return Arrays.equals(this.encodedKey, that.encodedKey);
            } else {
                return false;
            }
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(encodedKey);
        }
    }
}
