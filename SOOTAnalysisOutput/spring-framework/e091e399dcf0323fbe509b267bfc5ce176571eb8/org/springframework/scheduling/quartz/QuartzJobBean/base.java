package org.springframework.scheduling.quartz;

import java.lang.reflect.Method;
import java.util.Map;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.MutablePropertyValues;
import org.springframework.beans.PropertyAccessorFactory;
import org.springframework.util.ReflectionUtils;

public abstract class QuartzJobBean implements Job {

    private static final Method getSchedulerMethod;

    private static final Method getMergedJobDataMapMethod;

    static {
        try {
            getSchedulerMethod = JobExecutionContext.class.getMethod("getScheduler");
            getMergedJobDataMapMethod = JobExecutionContext.class.getMethod("getMergedJobDataMap");
        } catch (NoSuchMethodException ex) {
            throw new IllegalStateException("Incompatible Quartz API: " + ex);
        }
    }

    public final void execute(JobExecutionContext context) throws JobExecutionException {
        try {
            Scheduler scheduler = (Scheduler) ReflectionUtils.invokeMethod(getSchedulerMethod, context);
            Map mergedJobDataMap = (Map) ReflectionUtils.invokeMethod(getMergedJobDataMapMethod, context);
            BeanWrapper bw = PropertyAccessorFactory.forBeanPropertyAccess(this);
            MutablePropertyValues pvs = new MutablePropertyValues();
            pvs.addPropertyValues(scheduler.getContext());
            pvs.addPropertyValues(mergedJobDataMap);
            bw.setPropertyValues(pvs, true);
        } catch (SchedulerException ex) {
            throw new JobExecutionException(ex);
        }
        executeInternal(context);
    }

    protected abstract void executeInternal(JobExecutionContext context) throws JobExecutionException;
}
