package org.springframework.boot.ansi;

import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import org.springframework.core.env.PropertyResolver;
import org.springframework.core.env.PropertySource;
import org.springframework.util.StringUtils;

public class AnsiPropertySource extends PropertySource<AnsiElement> {

    private static final Iterable<MappedEnum<?>> MAPPED_ENUMS;

    static {
        List<MappedEnum<?>> enums = new ArrayList<>();
        enums.add(new MappedEnum<>("AnsiStyle.", AnsiStyle.class));
        enums.add(new MappedEnum<>("AnsiColor.", AnsiColor.class));
        enums.add(new MappedEnum<>("AnsiBackground.", AnsiBackground.class));
        enums.add(new MappedEnum<>("Ansi.", AnsiStyle.class));
        enums.add(new MappedEnum<>("Ansi.", AnsiColor.class));
        enums.add(new MappedEnum<>("Ansi.BG_", AnsiBackground.class));
        MAPPED_ENUMS = Collections.unmodifiableList(enums);
    }

    private final boolean encode;

    public AnsiPropertySource(String name, boolean encode) {
        super(name);
        this.encode = encode;
    }

    @Override
    public Object getProperty(String name) {
        if (StringUtils.hasLength(name)) {
            for (MappedEnum<?> mappedEnum : MAPPED_ENUMS) {
                if (name.startsWith(mappedEnum.getPrefix())) {
                    String enumName = name.substring(mappedEnum.getPrefix().length());
                    for (Enum<?> ansiEnum : mappedEnum.getEnums()) {
                        if (ansiEnum.name().equals(enumName)) {
                            if (this.encode) {
                                return AnsiOutput.encode((AnsiElement) ansiEnum);
                            }
                            return ansiEnum;
                        }
                    }
                }
            }
        }
        return null;
    }

    private static class MappedEnum<E extends Enum<E>> {

        private final String prefix;

        private final Set<E> enums;

        MappedEnum(String prefix, Class<E> enumType) {
            this.prefix = prefix;
            this.enums = EnumSet.allOf(enumType);
        }

        public String getPrefix() {
            return this.prefix;
        }

        public Set<E> getEnums() {
            return this.enums;
        }
    }
}
