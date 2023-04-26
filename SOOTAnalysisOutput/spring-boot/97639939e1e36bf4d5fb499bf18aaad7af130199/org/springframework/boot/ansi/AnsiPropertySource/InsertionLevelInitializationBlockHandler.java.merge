package org.springframework.boot.ansi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.function.IntFunction;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

public class AnsiPropertySource extends PropertySource<AnsiElement> {

    private static final Iterable<Mapping> MAPPINGS;

    static {
        List<Mapping> mappings = new ArrayList<>();
        mappings.add(new EnumMapping<>("AnsiStyle.", AnsiStyle.class));
        mappings.add(new EnumMapping<>("AnsiColor.", AnsiColor.class));
        mappings.add(new Ansi8BitColorMapping("AnsiColor.", Ansi8BitColor::foreground));
        mappings.add(new EnumMapping<>("AnsiBackground.", AnsiBackground.class));
        mappings.add(new Ansi8BitColorMapping("AnsiBackground.", Ansi8BitColor::background));
        mappings.add(new EnumMapping<>("Ansi.", AnsiStyle.class));
        mappings.add(new EnumMapping<>("Ansi.", AnsiColor.class));
        mappings.add(new EnumMapping<>("Ansi.BG_", AnsiBackground.class));
        MAPPINGS = Collections.unmodifiableList(mappings);
    }

    private final boolean encode;

    public AnsiPropertySource(String name, boolean encode) {
        super(name);
        this.encode = encode;
    }

    @Override
    public Object getProperty(String name) {
        if (StringUtils.hasLength(name)) {
            for (Mapping mapping : MAPPINGS) {
                String prefix = mapping.getPrefix();
                if (name.startsWith(prefix)) {
                    String postfix = name.substring(prefix.length());
                    AnsiElement element = mapping.getElement(postfix);
                    if (element != null) {
                        return (this.encode) ? AnsiOutput.encode(element) : element;
                    }
                }
            }
        }
        return null;
    }

    private abstract static class Mapping {

        private final String prefix;

        Mapping(String prefix) {
            this.prefix = prefix;
        }

        String getPrefix() {
            return this.prefix;
        }

        abstract AnsiElement getElement(String postfix);
    }

    private static class EnumMapping<E extends Enum<E> & AnsiElement> extends Mapping {

        private final Set<E> enums;

        EnumMapping(String prefix, Class<E> enumType) {
            super(prefix);
            this.enums = EnumSet.allOf(enumType);
        }

        @Override
        AnsiElement getElement(String postfix) {
            for (Enum<?> candidate : this.enums) {
                if (candidate.name().equals(postfix)) {
                    return (AnsiElement) candidate;
                }
            }
            return null;
        }
    }

    private static class Ansi8BitColorMapping extends Mapping {

        private final IntFunction<Ansi8BitColor> factory;

        Ansi8BitColorMapping(String prefix, IntFunction<Ansi8BitColor> factory) {
            super(prefix);
            this.factory = factory;
        }

        @Override
        AnsiElement getElement(String postfix) {
            if (containsOnlyDigits(postfix)) {
                try {
                    return this.factory.apply(Integer.parseInt(postfix));
                } catch (IllegalArgumentException ex) {
                }
            }
            return null;
        }

        private boolean containsOnlyDigits(String postfix) {
            for (int i = 0; i < postfix.length(); i++) {
                if (!Character.isDigit(postfix.charAt(i))) {
                    return false;
                }
            }
<<<<<<< MINE
            return !postfix.isEmpty();
=======
            return postfix.length() > 0;
>>>>>>> YOURS
        }
    }
}