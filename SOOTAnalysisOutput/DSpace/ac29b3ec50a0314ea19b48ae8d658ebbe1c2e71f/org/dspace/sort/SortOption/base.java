package org.dspace.sort;

import java.io.IOException;
import java.util.Comparator;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Logger;
import org.dspace.core.ConfigurationManager;

public class SortOption {

    private static final Logger log = org.apache.logging.log4j.LogManager.getLogger(SortOption.class);

    public static final String ASCENDING = "ASC";

    public static final String DESCENDING = "DESC";

    private int number;

    private String name;

    private String metadata;

    private String type;

    private String[] mdBits;

    private boolean visible;

    private static Set<SortOption> sortOptionsSet = null;

    static {
        try {
            Set<SortOption> newSortOptionsSet = new TreeSet<SortOption>(new Comparator<SortOption>() {

                @Override
                public int compare(SortOption sortOption, SortOption sortOption1) {
                    return Integer.valueOf(sortOption.getNumber()).compareTo(Integer.valueOf(sortOption1.getNumber()));
                }
            });
            int idx = 1;
            String option;
            while (((option = ConfigurationManager.getProperty("webui.itemlist.sort-option." + idx))) != null) {
                SortOption so = new SortOption(idx, option);
                newSortOptionsSet.add(so);
                idx++;
            }
            SortOption.sortOptionsSet = newSortOptionsSet;
        } catch (SortException se) {
            log.fatal("Unable to load SortOptions", se);
        }
    }

    public SortOption(int number, String name, String md, String type) throws SortException {
        this.name = name;
        this.type = type;
        this.metadata = md;
        this.number = number;
        this.visible = true;
        generateMdBits();
    }

    public SortOption(int number, String definition) throws SortException {
        this.number = number;
        String rx = "(\\w+):([\\w\\.\\*]+):(\\w+):?(\\w*)";
        Pattern pattern = Pattern.compile(rx);
        Matcher matcher = pattern.matcher(definition);
        if (!matcher.matches()) {
            throw new SortException("Sort Order configuration is not valid: webui.itemlist.sort-option." + number + " = " + definition);
        }
        name = matcher.group(1);
        metadata = matcher.group(2);
        type = matcher.group(3);
        if (matcher.groupCount() > 3 && "hide".equalsIgnoreCase(matcher.group(4))) {
            visible = false;
        } else {
            visible = true;
        }
        generateMdBits();
    }

    public String getMetadata() {
        return metadata;
    }

    public void setMetadata(String metadata) {
        this.metadata = metadata;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getNumber() {
        return number;
    }

    public void setNumber(int number) {
        this.number = number;
    }

    public boolean isVisible() {
        return visible;
    }

    public String[] getMdBits() {
        return (String[]) ArrayUtils.clone(mdBits);
    }

    private void generateMdBits() throws SortException {
        try {
            mdBits = interpretField(metadata, null);
        } catch (IOException e) {
            throw new SortException(e);
        }
    }

    public final String[] interpretField(String mfield, String init) throws IOException {
        StringTokenizer sta = new StringTokenizer(mfield, ".");
        String[] field = { init, init, init };
        int i = 0;
        while (sta.hasMoreTokens()) {
            field[i++] = sta.nextToken();
        }
        if (field[0] == null || field[1] == null) {
            throw new IOException("at least a schema and element be " + "specified in configuration.  You supplied: " + mfield);
        }
        return field;
    }

    public boolean isDate() {
        if ("date".equals(type)) {
            return true;
        }
        return false;
    }

    public boolean isDefault() {
        if (number == 0) {
            return true;
        }
        return false;
    }

    public static Set<SortOption> getSortOptions() throws SortException {
        if (SortOption.sortOptionsSet == null) {
            throw new SortException("Sort options not loaded");
        }
        return SortOption.sortOptionsSet;
    }

    public static SortOption getSortOption(String name) throws SortException {
        for (SortOption so : SortOption.getSortOptions()) {
            if (StringUtils.equals(name, so.getName())) {
                return so;
            }
        }
        return null;
    }

    public static SortOption getSortOption(int number) throws SortException {
        for (SortOption so : SortOption.getSortOptions()) {
            if (so.getNumber() == number) {
                return so;
            }
        }
        return null;
    }

    public static SortOption getDefaultSortOption() throws SortException {
        for (SortOption so : getSortOptions()) {
            return so;
        }
        return null;
    }
}
