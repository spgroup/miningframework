package com.google.cloud.dataflow.sdk.runners;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;
import com.google.cloud.dataflow.sdk.Pipeline;
import com.google.cloud.dataflow.sdk.Pipeline.PipelineVisitor;
import com.google.cloud.dataflow.sdk.PipelineResult;
import com.google.cloud.dataflow.sdk.coders.CannotProvideCoderException;
import com.google.cloud.dataflow.sdk.coders.Coder;
import com.google.cloud.dataflow.sdk.coders.ListCoder;
import com.google.cloud.dataflow.sdk.io.AvroIO;
import com.google.cloud.dataflow.sdk.io.FileBasedSink;
import com.google.cloud.dataflow.sdk.io.TextIO;
import com.google.cloud.dataflow.sdk.options.DirectPipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptions;
import com.google.cloud.dataflow.sdk.options.PipelineOptions.CheckEnabled;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsFactory;
import com.google.cloud.dataflow.sdk.options.PipelineOptionsValidator;
import com.google.cloud.dataflow.sdk.transforms.Aggregator;
import com.google.cloud.dataflow.sdk.transforms.AppliedPTransform;
import com.google.cloud.dataflow.sdk.transforms.Combine;
import com.google.cloud.dataflow.sdk.transforms.Combine.KeyedCombineFn;
import com.google.cloud.dataflow.sdk.transforms.DoFn;
import com.google.cloud.dataflow.sdk.transforms.PTransform;
import com.google.cloud.dataflow.sdk.transforms.ParDo;
import com.google.cloud.dataflow.sdk.transforms.Partition;
import com.google.cloud.dataflow.sdk.transforms.Partition.PartitionFn;
import com.google.cloud.dataflow.sdk.transforms.windowing.BoundedWindow;
import com.google.cloud.dataflow.sdk.util.AppliedCombineFn;
import com.google.cloud.dataflow.sdk.util.IOChannelUtils;
import com.google.cloud.dataflow.sdk.util.MapAggregatorValues;
import com.google.cloud.dataflow.sdk.util.PerKeyCombineFnRunner;
import com.google.cloud.dataflow.sdk.util.PerKeyCombineFnRunners;
import com.google.cloud.dataflow.sdk.util.SerializableUtils;
import com.google.cloud.dataflow.sdk.util.TestCredential;
import com.google.cloud.dataflow.sdk.util.WindowedValue;
import com.google.cloud.dataflow.sdk.util.common.Counter;
import com.google.cloud.dataflow.sdk.util.common.CounterSet;
import com.google.cloud.dataflow.sdk.values.KV;
import com.google.cloud.dataflow.sdk.values.PCollection;
import com.google.cloud.dataflow.sdk.values.PCollectionList;
import com.google.cloud.dataflow.sdk.values.PCollectionView;
import com.google.cloud.dataflow.sdk.values.PDone;
import com.google.cloud.dataflow.sdk.values.PInput;
import com.google.cloud.dataflow.sdk.values.POutput;
import com.google.cloud.dataflow.sdk.values.PValue;
import com.google.cloud.dataflow.sdk.values.TypedPValue;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.joda.time.Instant;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class DirectPipelineRunner extends PipelineRunner<DirectPipelineRunner.EvaluationResults> {

    private static final Logger LOG = LoggerFactory.getLogger(DirectPipelineRunner.class);

    private Random rand;

    private static Map<Class, TransformEvaluator> defaultTransformEvaluators = new HashMap<>();

    private Map<Class, TransformEvaluator> localTransformEvaluators = new HashMap<>();

    public static <TransformT extends PTransform<?, ?>> void registerDefaultTransformEvaluator(Class<TransformT> transformClass, TransformEvaluator<? super TransformT> transformEvaluator) {
        if (defaultTransformEvaluators.put(transformClass, transformEvaluator) != null) {
            throw new IllegalArgumentException("defining multiple evaluators for " + transformClass);
        }
    }

    public <TransformT extends PTransform<?, ?>> void registerTransformEvaluator(Class<TransformT> transformClass, TransformEvaluator<TransformT> transformEvaluator) {
        if (localTransformEvaluators.put(transformClass, transformEvaluator) != null) {
            throw new IllegalArgumentException("defining multiple evaluators for " + transformClass);
        }
    }

    public <TransformT extends PTransform<?, ?>> TransformEvaluator<TransformT> getTransformEvaluator(Class<TransformT> transformClass) {
        TransformEvaluator<TransformT> transformEvaluator = localTransformEvaluators.get(transformClass);
        if (transformEvaluator == null) {
            transformEvaluator = defaultTransformEvaluators.get(transformClass);
        }
        return transformEvaluator;
    }

    public static DirectPipelineRunner fromOptions(PipelineOptions options) {
        DirectPipelineOptions directOptions = PipelineOptionsValidator.validate(DirectPipelineOptions.class, options);
        LOG.debug("Creating DirectPipelineRunner");
        return new DirectPipelineRunner(directOptions);
    }

    public static DirectPipelineRunner createForTest() {
        DirectPipelineOptions options = PipelineOptionsFactory.as(DirectPipelineOptions.class);
        options.setStableUniqueNames(CheckEnabled.ERROR);
        options.setGcpCredential(new TestCredential());
        return new DirectPipelineRunner(options);
    }

    public DirectPipelineRunner withSerializabilityTesting(boolean enable) {
        this.testSerializability = enable;
        return this;
    }

    public DirectPipelineRunner withEncodabilityTesting(boolean enable) {
        this.testEncodability = enable;
        return this;
    }

    public DirectPipelineRunner withUnorderednessTesting(boolean enable) {
        this.testUnorderedness = enable;
        return this;
    }

    @Override
    public <OutputT extends POutput, InputT extends PInput> OutputT apply(PTransform<InputT, OutputT> transform, InputT input) {
        if (transform instanceof Combine.GroupedValues) {
            return (OutputT) applyTestCombine((Combine.GroupedValues) transform, (PCollection) input);
        } else if (transform instanceof TextIO.Write.Bound) {
            return (OutputT) applyTextIOWrite((TextIO.Write.Bound) transform, (PCollection<?>) input);
        } else if (transform instanceof AvroIO.Write.Bound) {
            return (OutputT) applyAvroIOWrite((AvroIO.Write.Bound) transform, (PCollection<?>) input);
        } else {
            return super.apply(transform, input);
        }
    }

    private <K, InputT, AccumT, OutputT> PCollection<KV<K, OutputT>> applyTestCombine(Combine.GroupedValues<K, InputT, OutputT> transform, PCollection<KV<K, Iterable<InputT>>> input) {
        PCollection<KV<K, OutputT>> output = input.apply(ParDo.of(TestCombineDoFn.create(transform, input, testSerializability, rand)).withSideInputs(transform.getSideInputs()));
        try {
            output.setCoder(transform.getDefaultOutputCoder(input));
        } catch (CannotProvideCoderException exc) {
        }
        return output;
    }

    private static class ElementProcessingOrderPartitionFn<T> implements PartitionFn<T> {

        private int elementNumber;

        @Override
        public int partitionFor(T elem, int numPartitions) {
            return elementNumber++ % numPartitions;
        }
    }

    private static class DirectTextIOWrite<T> extends PTransform<PCollection<T>, PDone> {

        private final TextIO.Write.Bound<T> transform;

        private DirectTextIOWrite(TextIO.Write.Bound<T> transform) {
            this.transform = transform;
        }

        @Override
        public PDone apply(PCollection<T> input) {
            checkState(transform.getNumShards() > 1, "DirectTextIOWrite is expected to only be used when sharding controls are required.");
            PCollectionList<T> partitionedElements = input.apply(Partition.of(transform.getNumShards(), new ElementProcessingOrderPartitionFn<T>()));
            for (int i = 0; i < transform.getNumShards(); ++i) {
                String outputFilename = IOChannelUtils.constructName(transform.getFilenamePrefix(), transform.getShardNameTemplate(), getFileExtension(transform.getFilenameSuffix()), i, transform.getNumShards());
                String transformName = String.format("%s(Shard:%s)", transform.getName(), i);
                partitionedElements.get(i).apply(transformName, transform.withNumShards(1).withShardNameTemplate("").withSuffix("").to(outputFilename));
            }
            return PDone.in(input.getPipeline());
        }
    }

    private static String getFileExtension(String usersExtension) {
        if (usersExtension == null || usersExtension.isEmpty()) {
            return "";
        }
        if (usersExtension.startsWith(".")) {
            return usersExtension;
        }
        return "." + usersExtension;
    }

    private <T> PDone applyTextIOWrite(TextIO.Write.Bound<T> transform, PCollection<T> input) {
        if (transform.getNumShards() <= 1) {
            return super.apply(transform.withNumShards(1), input);
        }
        return input.apply(new DirectTextIOWrite<>(transform));
    }

    private static class DirectAvroIOWrite<T> extends PTransform<PCollection<T>, PDone> {

        private final AvroIO.Write.Bound<T> transform;

        private DirectAvroIOWrite(AvroIO.Write.Bound<T> transform) {
            this.transform = transform;
        }

        @Override
        public PDone apply(PCollection<T> input) {
            checkState(transform.getNumShards() > 1, "DirectAvroIOWrite is expected to only be used when sharding controls are required.");
            PCollectionList<T> partitionedElements = input.apply(Partition.of(transform.getNumShards(), new ElementProcessingOrderPartitionFn<T>()));
            for (int i = 0; i < transform.getNumShards(); ++i) {
                String outputFilename = IOChannelUtils.constructName(transform.getFilenamePrefix(), transform.getShardNameTemplate(), getFileExtension(transform.getFilenameSuffix()), i, transform.getNumShards());
                String transformName = String.format("%s(Shard:%s)", transform.getName(), i);
                partitionedElements.get(i).apply(transformName, transform.withNumShards(1).withShardNameTemplate("").withSuffix("").to(outputFilename));
            }
            return PDone.in(input.getPipeline());
        }
    }

    private <T> PDone applyAvroIOWrite(AvroIO.Write.Bound<T> transform, PCollection<T> input) {
        if (transform.getNumShards() <= 1) {
            return super.apply(transform.withNumShards(1), input);
        }
        return input.apply(new DirectAvroIOWrite<>(transform));
    }

    public static class TestCombineDoFn<K, InputT, AccumT, OutputT> extends DoFn<KV<K, Iterable<InputT>>, KV<K, OutputT>> {

        private final PerKeyCombineFnRunner<? super K, ? super InputT, AccumT, OutputT> fnRunner;

        private final Coder<AccumT> accumCoder;

        private final boolean testSerializability;

        private final Random rand;

        public static <K, InputT, AccumT, OutputT> TestCombineDoFn<K, InputT, AccumT, OutputT> create(Combine.GroupedValues<K, InputT, OutputT> transform, PCollection<KV<K, Iterable<InputT>>> input, boolean testSerializability, Random rand) {
            AppliedCombineFn<? super K, ? super InputT, ?, OutputT> fn = transform.getAppliedFn(input.getPipeline().getCoderRegistry(), input.getCoder(), input.getWindowingStrategy());
            return new TestCombineDoFn(PerKeyCombineFnRunners.create(fn.getFn()), fn.getAccumulatorCoder(), testSerializability, rand);
        }

        public TestCombineDoFn(PerKeyCombineFnRunner<? super K, ? super InputT, AccumT, OutputT> fnRunner, Coder<AccumT> accumCoder, boolean testSerializability, Random rand) {
            this.fnRunner = fnRunner;
            this.accumCoder = accumCoder;
            this.testSerializability = testSerializability;
            this.rand = rand;
            this.accumCoder.getEncodingId();
        }

        @Override
        public void processElement(ProcessContext c) throws Exception {
            K key = c.element().getKey();
            Iterable<InputT> values = c.element().getValue();
            List<AccumT> groupedPostShuffle = ensureSerializableByCoder(ListCoder.of(accumCoder), addInputsRandomly(fnRunner, key, values, rand, c), "After addInputs of KeyedCombineFn " + fnRunner.fn().toString());
            AccumT merged = ensureSerializableByCoder(accumCoder, fnRunner.mergeAccumulators(key, groupedPostShuffle, c), "After mergeAccumulators of KeyedCombineFn " + fnRunner.fn().toString());
            c.output(KV.of(key, fnRunner.extractOutput(key, merged, c)));
        }

        public static <K, AccumT, InputT> List<AccumT> addInputsRandomly(PerKeyCombineFnRunner<? super K, ? super InputT, AccumT, ?> fnRunner, K key, Iterable<InputT> values, Random random, DoFn<?, ?>.ProcessContext c) {
            List<AccumT> out = new ArrayList<AccumT>();
            int i = 0;
            AccumT accumulator = fnRunner.createAccumulator(key, c);
            boolean hasInput = false;
            for (InputT value : values) {
                accumulator = fnRunner.addInput(key, accumulator, value, c);
                hasInput = true;
                if (i == 0 || random.nextInt(1 << Math.min(i, 30)) == 0) {
                    if (i % 2 == 0) {
                        accumulator = fnRunner.compact(key, accumulator, c);
                    }
                    out.add(accumulator);
                    accumulator = fnRunner.createAccumulator(key, c);
                    hasInput = false;
                }
                i++;
            }
            if (hasInput) {
                out.add(accumulator);
            }
            Collections.shuffle(out, random);
            return out;
        }

        public <T> T ensureSerializableByCoder(Coder<T> coder, T value, String errorContext) {
            if (testSerializability) {
                return SerializableUtils.ensureSerializableByCoder(coder, value, errorContext);
            }
            return value;
        }
    }

    @Override
    public EvaluationResults run(Pipeline pipeline) {
        LOG.info("Executing pipeline using the DirectPipelineRunner.");
        Evaluator evaluator = new Evaluator(rand);
        evaluator.run(pipeline);
        for (Counter counter : evaluator.getCounters()) {
            LOG.info("Final aggregator value: {}", counter);
        }
        LOG.info("Pipeline execution complete.");
        return evaluator;
    }

    public interface TransformEvaluator<TransformT extends PTransform> {

        public void evaluate(TransformT transform, EvaluationContext context);
    }

    public interface EvaluationResults extends PipelineResult {

        <T> List<T> getPCollection(PCollection<T> pc);

        <T> List<WindowedValue<T>> getPCollectionWindowedValues(PCollection<T> pc);

        <T> List<List<T>> getPCollectionList(PCollectionList<T> pcs);

        <T, WindowedT> Iterable<WindowedValue<?>> getPCollectionView(PCollectionView<T> view);
    }

    public static class ValueWithMetadata<V> {

        public static <V> ValueWithMetadata<V> of(WindowedValue<V> windowedValue) {
            return new ValueWithMetadata<>(windowedValue, null);
        }

        public ValueWithMetadata<V> withKey(Object key) {
            return new ValueWithMetadata<>(windowedValue, key);
        }

        public <T> ValueWithMetadata<T> withValue(T value) {
            return new ValueWithMetadata(windowedValue.withValue(value), getKey());
        }

        public WindowedValue<V> getWindowedValue() {
            return windowedValue;
        }

        public V getValue() {
            return windowedValue.getValue();
        }

        public Instant getTimestamp() {
            return windowedValue.getTimestamp();
        }

        public Collection<? extends BoundedWindow> getWindows() {
            return windowedValue.getWindows();
        }

        public Object getKey() {
            return key;
        }

        private final Object key;

        private final WindowedValue<V> windowedValue;

        private ValueWithMetadata(WindowedValue<V> windowedValue, Object key) {
            this.windowedValue = windowedValue;
            this.key = key;
        }
    }

    public interface EvaluationContext extends EvaluationResults {

        DirectPipelineOptions getPipelineOptions();

        <InputT extends PInput> InputT getInput(PTransform<InputT, ?> transform);

        <OutputT extends POutput> OutputT getOutput(PTransform<?, OutputT> transform);

        <T> void setPCollectionValuesWithMetadata(PCollection<T> pc, List<ValueWithMetadata<T>> elements);

        <T> void setPCollectionWindowedValue(PCollection<T> pc, List<WindowedValue<T>> elements);

        <T> void setPCollection(PCollection<T> pc, List<T> elements);

        <T> List<ValueWithMetadata<T>> getPCollectionValuesWithMetadata(PCollection<T> pc);

        <ElemT, T, WindowedT> void setPCollectionView(PCollectionView<T> pc, Iterable<WindowedValue<ElemT>> value);

        <T> T ensureElementEncodable(TypedPValue<T> pvalue, T element);

        <T> List<T> randomizeIfUnordered(List<T> elements, boolean inPlaceAllowed);

        <FunctionT extends Serializable> FunctionT ensureSerializable(FunctionT fn);

        <T> Coder<T> ensureCoderSerializable(Coder<T> coder);

        <T> T ensureSerializableByCoder(Coder<T> coder, T data, String errorContext);

        CounterSet.AddCounterMutator getAddCounterMutator();

        public String getStepName(PTransform<?, ?> transform);
    }

    class Evaluator implements PipelineVisitor, EvaluationContext {

        private final Map<PTransform<?, ?>, String> stepNames = new HashMap<>();

        private final Map<PValue, Object> store = new HashMap<>();

        private final CounterSet counters = new CounterSet();

        private AppliedPTransform<?, ?, ?> currentTransform;

        private Map<Aggregator<?, ?>, Collection<PTransform<?, ?>>> aggregatorSteps = null;

        private final Map<PTransform<?, ?>, String> fullNames = new HashMap<>();

        private Random rand;

        public Evaluator() {
            this(new Random());
        }

        public Evaluator(Random rand) {
            this.rand = rand;
        }

        public void run(Pipeline pipeline) {
            pipeline.traverseTopologically(this);
            aggregatorSteps = new AggregatorPipelineExtractor(pipeline).getAggregatorSteps();
        }

        @Override
        public DirectPipelineOptions getPipelineOptions() {
            return options;
        }

        @Override
        public <InputT extends PInput> InputT getInput(PTransform<InputT, ?> transform) {
            checkArgument(currentTransform != null && currentTransform.getTransform() == transform, "can only be called with current transform");
            return (InputT) currentTransform.getInput();
        }

        @Override
        public <OutputT extends POutput> OutputT getOutput(PTransform<?, OutputT> transform) {
            checkArgument(currentTransform != null && currentTransform.getTransform() == transform, "can only be called with current transform");
            return (OutputT) currentTransform.getOutput();
        }

        @Override
        public void enterCompositeTransform(TransformTreeNode node) {
        }

        @Override
        public void leaveCompositeTransform(TransformTreeNode node) {
        }

        @Override
        public void visitTransform(TransformTreeNode node) {
            PTransform<?, ?> transform = node.getTransform();
            fullNames.put(transform, node.getFullName());
            TransformEvaluator evaluator = getTransformEvaluator(transform.getClass());
            if (evaluator == null) {
                throw new IllegalStateException("no evaluator registered for " + transform);
            }
            LOG.debug("Evaluating {}", transform);
            currentTransform = AppliedPTransform.of(node.getFullName(), node.getInput(), node.getOutput(), (PTransform) transform);
            evaluator.evaluate(transform, this);
            currentTransform = null;
        }

        @Override
        public void visitValue(PValue value, TransformTreeNode producer) {
            LOG.debug("Checking evaluation of {}", value);
            if (value.getProducingTransformInternal() == null) {
                throw new RuntimeException("internal error: expecting a PValue " + "to have a producingTransform");
            }
            if (!producer.isCompositeNode()) {
                getPValue(value);
            }
        }

        void setPValue(PValue pvalue, Object contents) {
            if (store.containsKey(pvalue)) {
                throw new IllegalStateException("internal error: setting the value of " + pvalue + " more than once");
            }
            store.put(pvalue, contents);
        }

        Object getPValue(PValue pvalue) {
            if (!store.containsKey(pvalue)) {
                throw new IllegalStateException("internal error: getting the value of " + pvalue + " before it has been computed");
            }
            return store.get(pvalue);
        }

        <T> List<ValueWithMetadata<T>> toValueWithMetadata(List<T> values) {
            List<ValueWithMetadata<T>> result = new ArrayList<>(values.size());
            for (T value : values) {
                result.add(ValueWithMetadata.of(WindowedValue.valueInGlobalWindow(value)));
            }
            return result;
        }

        <T> List<ValueWithMetadata<T>> toValueWithMetadataFromWindowedValue(List<WindowedValue<T>> values) {
            List<ValueWithMetadata<T>> result = new ArrayList<>(values.size());
            for (WindowedValue<T> value : values) {
                result.add(ValueWithMetadata.of(value));
            }
            return result;
        }

        @Override
        public <T> void setPCollection(PCollection<T> pc, List<T> elements) {
            setPCollectionValuesWithMetadata(pc, toValueWithMetadata(elements));
        }

        @Override
        public <T> void setPCollectionWindowedValue(PCollection<T> pc, List<WindowedValue<T>> elements) {
            setPCollectionValuesWithMetadata(pc, toValueWithMetadataFromWindowedValue(elements));
        }

        @Override
        public <T> void setPCollectionValuesWithMetadata(PCollection<T> pc, List<ValueWithMetadata<T>> elements) {
            LOG.debug("Setting {} = {}", pc, elements);
            ensurePCollectionEncodable(pc, elements);
            setPValue(pc, elements);
        }

        @Override
        public <ElemT, T, WindowedT> void setPCollectionView(PCollectionView<T> view, Iterable<WindowedValue<ElemT>> value) {
            LOG.debug("Setting {} = {}", view, value);
            setPValue(view, value);
        }

        @Override
        public <T> List<T> getPCollection(PCollection<T> pc) {
            List<T> result = new ArrayList<>();
            for (ValueWithMetadata<T> elem : getPCollectionValuesWithMetadata(pc)) {
                result.add(elem.getValue());
            }
            return result;
        }

        @Override
        public <T> List<WindowedValue<T>> getPCollectionWindowedValues(PCollection<T> pc) {
            return Lists.transform(getPCollectionValuesWithMetadata(pc), new Function<ValueWithMetadata<T>, WindowedValue<T>>() {

                @Override
                public WindowedValue<T> apply(ValueWithMetadata<T> input) {
                    return input.getWindowedValue();
                }
            });
        }

        @Override
        public <T> List<ValueWithMetadata<T>> getPCollectionValuesWithMetadata(PCollection<T> pc) {
            List<ValueWithMetadata<T>> elements = (List<ValueWithMetadata<T>>) getPValue(pc);
            elements = randomizeIfUnordered(elements, false);
            LOG.debug("Getting {} = {}", pc, elements);
            return elements;
        }

        @Override
        public <T> List<List<T>> getPCollectionList(PCollectionList<T> pcs) {
            List<List<T>> elementsList = new ArrayList<>();
            for (PCollection<T> pc : pcs.getAll()) {
                elementsList.add(getPCollection(pc));
            }
            return elementsList;
        }

        @Override
        public <T, WindowedT> Iterable<WindowedValue<?>> getPCollectionView(PCollectionView<T> view) {
            Iterable<WindowedValue<?>> value = (Iterable<WindowedValue<?>>) getPValue(view);
            LOG.debug("Getting {} = {}", view, value);
            return value;
        }

        <T> List<ValueWithMetadata<T>> ensurePCollectionEncodable(PCollection<T> pc, List<ValueWithMetadata<T>> elements) {
            ensureCoderSerializable(pc.getCoder());
            if (!testEncodability) {
                return elements;
            }
            List<ValueWithMetadata<T>> elementsCopy = new ArrayList<>(elements.size());
            for (ValueWithMetadata<T> element : elements) {
                elementsCopy.add(element.withValue(ensureElementEncodable(pc, element.getValue())));
            }
            return elementsCopy;
        }

        @Override
        public <T> T ensureElementEncodable(TypedPValue<T> pvalue, T element) {
            return ensureSerializableByCoder(pvalue.getCoder(), element, "Within " + pvalue.toString());
        }

        @Override
        public <T> List<T> randomizeIfUnordered(List<T> elements, boolean inPlaceAllowed) {
            if (!testUnorderedness) {
                return elements;
            }
            List<T> elementsCopy = new ArrayList<>(elements);
            Collections.shuffle(elementsCopy, rand);
            return elementsCopy;
        }

        @Override
        public <FunctionT extends Serializable> FunctionT ensureSerializable(FunctionT fn) {
            if (!testSerializability) {
                return fn;
            }
            return SerializableUtils.ensureSerializable(fn);
        }

        @Override
        public <T> Coder<T> ensureCoderSerializable(Coder<T> coder) {
            if (testSerializability) {
                SerializableUtils.ensureSerializable(coder);
            }
            return coder;
        }

        @Override
        public <T> T ensureSerializableByCoder(Coder<T> coder, T value, String errorContext) {
            if (testSerializability) {
                return SerializableUtils.ensureSerializableByCoder(coder, value, errorContext);
            }
            return value;
        }

        @Override
        public CounterSet.AddCounterMutator getAddCounterMutator() {
            return counters.getAddCounterMutator();
        }

        @Override
        public String getStepName(PTransform<?, ?> transform) {
            String stepName = stepNames.get(transform);
            if (stepName == null) {
                stepName = "s" + (stepNames.size() + 1);
                stepNames.put(transform, stepName);
            }
            return stepName;
        }

        public CounterSet getCounters() {
            return counters;
        }

        @Override
        public State getState() {
            return State.DONE;
        }

        @Override
        public <T> AggregatorValues<T> getAggregatorValues(Aggregator<?, T> aggregator) {
            Map<String, T> stepValues = new HashMap<>();
            for (PTransform<?, ?> step : aggregatorSteps.get(aggregator)) {
                String stepName = String.format("user-%s-%s", stepNames.get(step), aggregator.getName());
                String fullName = fullNames.get(step);
                Counter<?> counter = counters.getExistingCounter(stepName);
                if (counter == null) {
                    throw new IllegalArgumentException("Aggregator " + aggregator + " is not used in this pipeline");
                }
                stepValues.put(fullName, (T) counter.getAggregate());
            }
            return new MapAggregatorValues<>(stepValues);
        }
    }

    private final DirectPipelineOptions options;

    private boolean testSerializability;

    private boolean testEncodability;

    private boolean testUnorderedness;

    private DirectPipelineRunner(DirectPipelineOptions options) {
        this.options = options;
        IOChannelUtils.registerStandardIOFactories(options);
        long randomSeed;
        if (options.getDirectPipelineRunnerRandomSeed() != null) {
            randomSeed = options.getDirectPipelineRunnerRandomSeed();
        } else {
            randomSeed = new Random().nextLong();
        }
        LOG.debug("DirectPipelineRunner using random seed {}.", randomSeed);
        rand = new Random(randomSeed);
        testSerializability = options.isTestSerializability();
        testEncodability = options.isTestEncodability();
        testUnorderedness = options.isTestUnorderedness();
    }

    public DirectPipelineOptions getPipelineOptions() {
        return options;
    }

    @Override
    public String toString() {
        return "DirectPipelineRunner#" + hashCode();
    }
}
