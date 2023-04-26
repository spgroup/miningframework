package org.springframework.boot.validation;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import javax.validation.MessageInterpolator;
import javax.validation.Validation;
import javax.validation.ValidationException;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.util.ClassUtils;

public class MessageInterpolatorFactory implements ObjectFactory<MessageInterpolator> {

    private static final Set<String> FALLBACKS;

    static {
        Set<String> fallbacks = new LinkedHashSet<>();
        fallbacks.add("org.hibernate.validator.messageinterpolation.ParameterMessageInterpolator");
        FALLBACKS = Collections.unmodifiableSet(fallbacks);
    }

    @Override
    public MessageInterpolator getObject() throws BeansException {
        try {
            return Validation.byDefaultProvider().configure().getDefaultMessageInterpolator();
        } catch (ValidationException ex) {
            MessageInterpolator fallback = getFallback();
            if (fallback != null) {
                return fallback;
            }
            throw ex;
        }
    }

    private MessageInterpolator getFallback() {
        for (String fallback : FALLBACKS) {
            try {
                return getFallback(fallback);
            } catch (Exception ex) {
            }
        }
        return null;
    }

    private MessageInterpolator getFallback(String fallback) {
        Class<?> interpolatorClass = ClassUtils.resolveClassName(fallback, null);
        Object interpolator = BeanUtils.instantiateClass(interpolatorClass);
        return (MessageInterpolator) interpolator;
    }
}