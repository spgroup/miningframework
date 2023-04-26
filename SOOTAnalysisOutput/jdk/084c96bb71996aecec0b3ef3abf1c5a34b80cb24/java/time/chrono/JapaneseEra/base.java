package java.time.chrono;

import static java.time.temporal.ChronoField.ERA;
import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalField;
import java.time.temporal.UnsupportedTemporalTypeException;
import java.time.temporal.ValueRange;
import java.util.Arrays;
import java.util.Objects;
import sun.util.calendar.CalendarDate;

public final class JapaneseEra implements Era, Serializable {

    static final int ERA_OFFSET = 2;

    static final sun.util.calendar.Era[] ERA_CONFIG;

    public static final JapaneseEra SEIREKI = new JapaneseEra(-999, LocalDate.MIN);

    public static final JapaneseEra MEIJI = new JapaneseEra(-1, LocalDate.of(1868, 9, 8));

    public static final JapaneseEra TAISHO = new JapaneseEra(0, LocalDate.of(1912, 7, 30));

    public static final JapaneseEra SHOWA = new JapaneseEra(1, LocalDate.of(1926, 12, 25));

    public static final JapaneseEra HEISEI = new JapaneseEra(2, LocalDate.of(1989, 1, 8));

    private static final int N_ERA_CONSTANTS = HEISEI.getValue() + ERA_OFFSET + 1;

    private static final long serialVersionUID = 1466499369062886794L;

    private static final JapaneseEra[] KNOWN_ERAS;

    static {
        sun.util.calendar.Era[] sunEras = JapaneseChronology.JCAL.getEras();
        ERA_CONFIG = new sun.util.calendar.Era[sunEras.length + 1];
        for (int i = 1; i < ERA_CONFIG.length; i++) {
            ERA_CONFIG[i] = sunEras[i - 1];
        }
        KNOWN_ERAS = new JapaneseEra[ERA_CONFIG.length];
        KNOWN_ERAS[0] = SEIREKI;
        KNOWN_ERAS[1] = MEIJI;
        KNOWN_ERAS[2] = TAISHO;
        KNOWN_ERAS[3] = SHOWA;
        KNOWN_ERAS[4] = HEISEI;
        for (int i = N_ERA_CONSTANTS; i < ERA_CONFIG.length; i++) {
            CalendarDate date = ERA_CONFIG[i].getSinceDate();
            LocalDate isoDate = LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth());
            KNOWN_ERAS[i] = new JapaneseEra(i - ERA_OFFSET, isoDate);
        }
    }

    private final int eraValue;

    private final transient LocalDate since;

    private JapaneseEra(int eraValue, LocalDate since) {
        this.eraValue = eraValue;
        this.since = since;
    }

    private Object readResolve() throws ObjectStreamException {
        try {
            return of(eraValue);
        } catch (DateTimeException e) {
            InvalidObjectException ex = new InvalidObjectException("Invalid era");
            ex.initCause(e);
            throw ex;
        }
    }

    sun.util.calendar.Era getPrivateEra() {
        return ERA_CONFIG[ordinal(eraValue)];
    }

    public static JapaneseEra of(int japaneseEra) {
        if (japaneseEra != SEIREKI.eraValue && (japaneseEra < MEIJI.eraValue || japaneseEra > HEISEI.eraValue)) {
            throw new DateTimeException("Invalid era: " + japaneseEra);
        }
        return KNOWN_ERAS[ordinal(japaneseEra)];
    }

    public static JapaneseEra valueOf(String japaneseEra) {
        Objects.requireNonNull(japaneseEra, "japaneseEra");
        for (JapaneseEra era : KNOWN_ERAS) {
            if (era.getName().equals(japaneseEra)) {
                return era;
            }
        }
        throw new IllegalArgumentException("japaneseEra is invalid");
    }

    public static JapaneseEra[] values() {
        return Arrays.copyOf(KNOWN_ERAS, KNOWN_ERAS.length);
    }

    static JapaneseEra from(LocalDate date) {
        for (int i = KNOWN_ERAS.length - 1; i > 0; i--) {
            JapaneseEra era = KNOWN_ERAS[i];
            if (date.compareTo(era.since) >= 0) {
                return era;
            }
        }
        return SEIREKI;
    }

    static JapaneseEra toJapaneseEra(sun.util.calendar.Era privateEra) {
        for (int i = ERA_CONFIG.length - 1; i > 0; i--) {
            if (ERA_CONFIG[i].equals(privateEra)) {
                return KNOWN_ERAS[i];
            }
        }
        return SEIREKI;
    }

    static sun.util.calendar.Era privateEraFrom(LocalDate isoDate) {
        for (int i = KNOWN_ERAS.length - 1; i > 0; i--) {
            JapaneseEra era = KNOWN_ERAS[i];
            if (isoDate.compareTo(era.since) >= 0) {
                return ERA_CONFIG[i];
            }
        }
        return null;
    }

    private static int ordinal(int eraValue) {
        return (eraValue == SEIREKI.eraValue) ? 0 : eraValue + ERA_OFFSET;
    }

    @Override
    public int getValue() {
        return eraValue;
    }

    @Override
    public ValueRange range(TemporalField field) {
        if (field == ERA) {
            return JapaneseChronology.INSTANCE.range(ERA);
        }
        return Era.super.range(field);
    }

    String getAbbreviation() {
        int index = ordinal(getValue());
        if (index == 0) {
            return "";
        }
        return ERA_CONFIG[index].getAbbreviation();
    }

    String getName() {
        int index = ordinal(getValue());
        if (index == 0) {
            return "Seireki";
        }
        return ERA_CONFIG[index].getName();
    }

    @Override
    public String toString() {
        return getName();
    }

    private Object writeReplace() {
        return new Ser(Ser.JAPANESE_ERA_TYPE, this);
    }

    void writeExternal(DataOutput out) throws IOException {
        out.writeByte(this.getValue());
    }

    static JapaneseEra readExternal(DataInput in) throws IOException {
        byte eraValue = in.readByte();
        return JapaneseEra.of(eraValue);
    }
}
