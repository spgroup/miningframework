package org.springframework.boot.logging.log4j2;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.layout.PatternLayout;
import org.apache.logging.log4j.core.pattern.ConverterKeys;
import org.apache.logging.log4j.core.pattern.LogEventPatternConverter;
import org.apache.logging.log4j.core.pattern.PatternConverter;
import org.apache.logging.log4j.core.pattern.PatternFormatter;
import org.apache.logging.log4j.core.pattern.PatternParser;
import org.springframework.boot.ansi.AnsiColor;
import org.springframework.boot.ansi.AnsiElement;
import org.springframework.boot.ansi.AnsiOutput;
import org.springframework.boot.ansi.AnsiStyle;

@Plugin(name = "color", category = PatternConverter.CATEGORY)
@ConverterKeys({ "clr", "color" })
public final class ColorConverter extends LogEventPatternConverter {

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
        levels.put(Level.FATAL.intLevel(), AnsiColor.RED);
        levels.put(Level.ERROR.intLevel(), AnsiColor.RED);
        levels.put(Level.WARN.intLevel(), AnsiColor.YELLOW);
        LEVELS = Collections.unmodifiableMap(levels);
=======
        Map<Integer, AnsiElement> ansiLevels = new HashMap<Integer, AnsiElement>();
        ansiLevels.put(Level.FATAL.intLevel(), AnsiColor.RED);
        ansiLevels.put(Level.ERROR.intLevel(), AnsiColor.RED);
        ansiLevels.put(Level.WARN.intLevel(), AnsiColor.YELLOW);
        levels = Collections.unmodifiableMap(ansiLevels);
>>>>>>> YOURS
    }

    private static final Map<String, AnsiElement> elements;

    private static final Map<Integer, AnsiElement> levels;

    private final List<PatternFormatter> formatters;

    private final AnsiElement styling;

    private ColorConverter(List<PatternFormatter> formatters, AnsiElement styling) {
        super("style", "style");
        this.formatters = formatters;
        this.styling = styling;
    }

    public static ColorConverter newInstance(Configuration config, String[] options) {
        if (options.length < 1) {
            LOGGER.error("Incorrect number of options on style. " + "Expected at least 1, received {}", options.length);
            return null;
        }
        if (options[0] == null) {
            LOGGER.error("No pattern supplied on style");
            return null;
        }
        PatternParser parser = PatternLayout.createPatternParser(config);
        List<PatternFormatter> formatters = parser.parse(options[0]);
        AnsiElement element = (options.length == 1 ? null : elements.get(options[1]));
        return new ColorConverter(formatters, element);
    }

    @Override
    public boolean handlesThrowable() {
        for (PatternFormatter formatter : this.formatters) {
            if (formatter.handlesThrowable()) {
                return true;
            }
        }
        return super.handlesThrowable();
    }

    @Override
    public void format(LogEvent event, StringBuilder toAppendTo) {
        StringBuilder buf = new StringBuilder();
        for (PatternFormatter formatter : this.formatters) {
            formatter.format(event, buf);
        }
        if (buf.length() > 0) {
            AnsiElement element = this.styling;
            if (element == null) {
                element = levels.get(event.getLevel().intLevel());
                element = (element == null ? AnsiColor.GREEN : element);
            }
            appendAnsiString(toAppendTo, buf.toString(), element);
        }
    }

    protected void appendAnsiString(StringBuilder toAppendTo, String in, AnsiElement element) {
        toAppendTo.append(AnsiOutput.toString(element, in));
    }
}