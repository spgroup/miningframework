package org.springframework.boot.logging.logback;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.ansi.AnsiOutput;

public class ColorConverter extends CompositeConverter<ILoggingEvent> {

    private static final Map<String, AnsiElement> ELEMENTS;

    static {
        Map<String, AnsiElement> elements = new HashMap<String, AnsiElement>();
        elements.put("faint", AnsiElement.FAINT);
        elements.put("red", AnsiElement.RED);
        elements.put("green", AnsiElement.GREEN);
        elements.put("yellow", AnsiElement.YELLOW);
        elements.put("blue", AnsiElement.BLUE);
        elements.put("magenta", AnsiElement.MAGENTA);
        elements.put("cyan", AnsiElement.CYAN);
        ELEMENTS = Collections.unmodifiableMap(elements);
    }

    private static final Map<Integer, AnsiElement> LEVELS;

    static {
        Map<Integer, AnsiElement> levels = new HashMap<Integer, AnsiElement>();
        levels.put(Level.ERROR_INTEGER, AnsiElement.RED);
        levels.put(Level.WARN_INTEGER, AnsiElement.YELLOW);
        LEVELS = Collections.unmodifiableMap(levels);
    }

    @Override
    protected String transform(ILoggingEvent event, String in) {
        AnsiElement element = ELEMENTS.get(getFirstOption());
        if (element == null) {
            element = LEVELS.get(event.getLevel().toInteger());
            element = (element == null ? AnsiElement.GREEN : element);
        }
        return toAnsiString(in, element);
    }

    protected String toAnsiString(String in, AnsiElement element) {
        return AnsiOutput.toString(element, in);
    }
}
