package org.springframework.boot.logging.logback;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.CompositeConverter;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;

public class ColorConverter extends CompositeConverter<ILoggingEvent> {

    static {
<<<<<<< MINE
        Map<String, AnsiElement> elements = new HashMap<>();
        elements.put("faint", AnsiStyle.FAINT);
        elements.put("red", AnsiColor.RED);
        elements.put("green", AnsiColor.GREEN);
        elements.put("yellow", AnsiColor.YELLOW);
        elements.put("blue", AnsiColor.BLUE);
        elements.put("magenta", AnsiColor.MAGENTA);
        elements.put("cyan", AnsiColor.CYAN);
        ELEMENTS = Collections.unmodifiableMap(elements);
=======
        Map<String, AnsiElement> ansiElements = new HashMap<String, AnsiElement>();
        ansiElements.put("faint", AnsiStyle.FAINT);
        ansiElements.put("red", AnsiColor.RED);
        ansiElements.put("green", AnsiColor.GREEN);
        ansiElements.put("yellow", AnsiColor.YELLOW);
        ansiElements.put("blue", AnsiColor.BLUE);
        ansiElements.put("magenta", AnsiColor.MAGENTA);
        ansiElements.put("cyan", AnsiColor.CYAN);
        elements = Collections.unmodifiableMap(ansiElements);
>>>>>>> YOURS
    }

    static {
<<<<<<< MINE
        Map<Integer, AnsiElement> levels = new HashMap<>();
        levels.put(Level.ERROR_INTEGER, AnsiColor.RED);
        levels.put(Level.WARN_INTEGER, AnsiColor.YELLOW);
        LEVELS = Collections.unmodifiableMap(levels);
=======
        Map<Integer, AnsiElement> ansiLevels = new HashMap<Integer, AnsiElement>();
        ansiLevels.put(Level.ERROR_INTEGER, AnsiColor.RED);
        ansiLevels.put(Level.WARN_INTEGER, AnsiColor.YELLOW);
        levels = Collections.unmodifiableMap(ansiLevels);
>>>>>>> YOURS
    }

    private static final Map<String, AnsiElement> elements;

    private static final Map<Integer, AnsiElement> levels;

    @Override
    protected String transform(ILoggingEvent event, String in) {
        AnsiElement element = elements.get(getFirstOption());
        if (element == null) {
            element = levels.get(event.getLevel().toInteger());
            element = (element == null ? AnsiColor.GREEN : element);
        }
        return toAnsiString(in, element);
    }

    protected String toAnsiString(String in, AnsiElement element) {
        return AnsiOutput.toString(element, in);
    }
}