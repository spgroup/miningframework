package com.alibaba.csp.sentinel.slots.block.flow;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import com.alibaba.csp.sentinel.concurrent.NamedThreadFactory;
import com.alibaba.csp.sentinel.log.RecordLog;
import com.alibaba.csp.sentinel.util.StringUtil;
import com.alibaba.csp.sentinel.node.metric.MetricTimerListener;
import com.alibaba.csp.sentinel.property.DynamicSentinelProperty;
import com.alibaba.csp.sentinel.property.PropertyListener;
import com.alibaba.csp.sentinel.property.SentinelProperty;
import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.flow.controller.DefaultController;
import com.alibaba.csp.sentinel.slots.block.flow.controller.RateLimiterController;
import com.alibaba.csp.sentinel.slots.block.flow.controller.WarmUpController;
import com.alibaba.csp.sentinel.slots.block.flow.controller.WarmUpRateLimiterController;

public class FlowRuleManager {

    private static final Map<String, List<FlowRule>> flowRules = new ConcurrentHashMap<String, List<FlowRule>>();

    private final static ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1, new NamedThreadFactory("sentinel-metrics-record-task", true));

    private final static FlowPropertyListener listener = new FlowPropertyListener();

    private static SentinelProperty<List<FlowRule>> currentProperty = new DynamicSentinelProperty<List<FlowRule>>();

    static {
        currentProperty.addListener(listener);
        scheduler.scheduleAtFixedRate(new MetricTimerListener(), 0, 1, TimeUnit.SECONDS);
    }

    public static void register2Property(SentinelProperty<List<FlowRule>> property) {
        synchronized (listener) {
            RecordLog.info("[FlowRuleManager] Registering new property to flow rule manager");
            currentProperty.removeListener(listener);
            property.addListener(listener);
            currentProperty = property;
        }
    }

    public static List<FlowRule> getRules() {
        List<FlowRule> rules = new ArrayList<FlowRule>();
        for (Map.Entry<String, List<FlowRule>> entry : flowRules.entrySet()) {
            rules.addAll(entry.getValue());
        }
        return rules;
    }

    public static void loadRules(List<FlowRule> rules) {
        currentProperty.updateValue(rules);
    }

    private static Map<String, List<FlowRule>> loadFlowConf(List<FlowRule> list) {
        Map<String, List<FlowRule>> newRuleMap = new ConcurrentHashMap<String, List<FlowRule>>();
        if (list == null || list.isEmpty()) {
            return newRuleMap;
        }
        for (FlowRule rule : list) {
            if (!isValidRule(rule)) {
                RecordLog.warn("[FlowRuleManager] Ignoring invalid flow rule when loading new flow rules: " + rule);
                continue;
            }
            if (StringUtil.isBlank(rule.getLimitApp())) {
                rule.setLimitApp(RuleConstant.LIMIT_APP_DEFAULT);
            }
            TrafficShapingController rater = new DefaultController(rule.getCount(), rule.getGrade());
            if (rule.getGrade() == RuleConstant.FLOW_GRADE_QPS && rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_WARM_UP && rule.getWarmUpPeriodSec() > 0) {
                rater = new WarmUpController(rule.getCount(), rule.getWarmUpPeriodSec(), ColdFactorProperty.coldFactor);
            } else if (rule.getGrade() == RuleConstant.FLOW_GRADE_QPS && rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER && rule.getMaxQueueingTimeMs() > 0) {
                rater = new RateLimiterController(rule.getMaxQueueingTimeMs(), rule.getCount());
            } else if (rule.getGrade() == RuleConstant.FLOW_GRADE_QPS && rule.getControlBehavior() == RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER && rule.getMaxQueueingTimeMs() > 0 && rule.getWarmUpPeriodSec() > 0) {
                rater = new WarmUpRateLimiterController(rule.getCount(), rule.getWarmUpPeriodSec(), rule.getMaxQueueingTimeMs(), ColdFactorProperty.coldFactor);
            }
            rule.setRater(rater);
            String identity = rule.getResource();
            List<FlowRule> ruleM = newRuleMap.get(identity);
            if (ruleM == null) {
                ruleM = new ArrayList<FlowRule>();
                newRuleMap.put(identity, ruleM);
            }
            ruleM.add(rule);
        }
        if (!newRuleMap.isEmpty()) {
            Comparator<FlowRule> comparator = new FlowRuleComparator();
            for (List<FlowRule> rules : newRuleMap.values()) {
                Collections.sort(rules, comparator);
            }
        }
        return newRuleMap;
    }

    static Map<String, List<FlowRule>> getFlowRules() {
        return flowRules;
    }

    public static boolean hasConfig(String resource) {
        return flowRules.containsKey(resource);
    }

    public static boolean isOtherOrigin(String origin, String resourceName) {
        if (StringUtil.isEmpty(origin)) {
            return false;
        }
        List<FlowRule> rules = flowRules.get(resourceName);
        if (rules != null) {
            for (FlowRule rule : rules) {
                if (origin.equals(rule.getLimitApp())) {
                    return false;
                }
            }
        }
        return true;
    }

    private static final class FlowPropertyListener implements PropertyListener<List<FlowRule>> {

        @Override
        public void configUpdate(List<FlowRule> value) {
            Map<String, List<FlowRule>> rules = loadFlowConf(value);
            if (rules != null) {
                flowRules.clear();
                flowRules.putAll(rules);
            }
            RecordLog.info("[FlowRuleManager] Flow rules received: " + flowRules);
        }

        @Override
        public void configLoad(List<FlowRule> conf) {
            Map<String, List<FlowRule>> rules = loadFlowConf(conf);
            if (rules != null) {
                flowRules.clear();
                flowRules.putAll(rules);
            }
            RecordLog.info("[FlowRuleManager] Flow rules loaded: " + flowRules);
        }
    }

    public static boolean isValidRule(FlowRule rule) {
        boolean baseValid = rule != null && !StringUtil.isBlank(rule.getResource()) && rule.getCount() >= 0 && rule.getGrade() >= 0 && rule.getStrategy() >= 0 && rule.getControlBehavior() >= 0;
        if (!baseValid) {
            return false;
        }
        return checkStrategyField(rule) && checkControlBehaviorField(rule);
    }

    private static boolean checkStrategyField(FlowRule rule) {
        if (rule.getStrategy() == RuleConstant.STRATEGY_RELATE || rule.getStrategy() == RuleConstant.STRATEGY_CHAIN) {
            return StringUtil.isNotBlank(rule.getRefResource());
        }
        return true;
    }

    private static boolean checkControlBehaviorField(FlowRule rule) {
        switch(rule.getControlBehavior()) {
            case RuleConstant.CONTROL_BEHAVIOR_WARM_UP:
                return rule.getWarmUpPeriodSec() > 0;
            case RuleConstant.CONTROL_BEHAVIOR_RATE_LIMITER:
                return rule.getMaxQueueingTimeMs() > 0;
            case RuleConstant.CONTROL_BEHAVIOR_WARM_UP_RATE_LIMITER:
                return rule.getWarmUpPeriodSec() > 0 && rule.getMaxQueueingTimeMs() > 0;
            default:
                return true;
        }
    }
}
