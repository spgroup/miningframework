package org.springframework.boot.actuate.endpoint;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Sanitizer {

    private final List<SanitizingFunction> sanitizingFunctions = new ArrayList<>();

    public Sanitizer() {
        this(Collections.emptyList());
    }

    public Sanitizer(Iterable<SanitizingFunction> sanitizingFunctions) {
        sanitizingFunctions.forEach(this.sanitizingFunctions::add);
    }

    public Object sanitize(SanitizableData data, boolean showUnsanitized) {
        Object value = data.getValue();
        if (value == null) {
            return null;
        }
        if (!showUnsanitized) {
            return SanitizableData.SANITIZED_VALUE;
        }
        for (SanitizingFunction sanitizingFunction : this.sanitizingFunctions) {
            data = sanitizingFunction.apply(data);
            Object sanitizedValue = data.getValue();
            if (!value.equals(sanitizedValue)) {
                return sanitizedValue;
            }
        }
        return value;
    }
}