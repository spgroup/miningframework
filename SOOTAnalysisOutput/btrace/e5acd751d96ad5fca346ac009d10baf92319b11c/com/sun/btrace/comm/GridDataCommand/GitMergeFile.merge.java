package com.sun.btrace.comm;

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.List;
import com.sun.btrace.aggregation.HistogramData;
import java.util.regex.Pattern;

public class GridDataCommand extends DataCommand {

    private static final Pattern INDEX_PATTERN = Pattern.compile("%(\\d)+\\$");

    private List<Object[]> data;

    private String format;

    public GridDataCommand() {
        this(null, null);
    }

    public GridDataCommand(String name, List<Object[]> data) {
        this(name, data, null);
    }

    public GridDataCommand(String name, List<Object[]> data, String format) {
        super(GRID_DATA, name);
        this.data = data;
        this.format = format;
    }

    public List<Object[]> getData() {
        return data;
    }

    private Map<Integer, Integer> getColumnWidth(List<Object[]> objects) {
        Map<Integer, Integer> columnWidth = new LinkedHashMap<>();
        for (Object[] obj : objects) {
            for (int column = 0; column < obj.length; ++column) {
                int length = obj[column].toString().length();
                Integer width = 0;
                if (columnWidth.containsKey(column)) {
                    width = columnWidth.get(column);
                }
                if (length > width) {
                    columnWidth.put(column, length);
                }
            }
        }
        return columnWidth;
    }

    public void print(PrintWriter out) {
        if (data != null) {
            if (name != null && !name.equals("")) {
                out.println(name);
            }
            Map<Integer, Integer> columnWidth = getColumnWidth(data);
            for (Object[] dataRow : data) {
                Object[] printRow = dataRow.clone();
                for (int i = 0; i < printRow.length; i++) {
                    if (printRow[i] == null) {
                        printRow[i] = "<null>";
                    }
                    if (printRow[i] instanceof HistogramData) {
                        StringWriter buffer = new StringWriter();
                        PrintWriter writer = new PrintWriter(buffer);
                        ((HistogramData) printRow[i]).print(writer);
                        writer.flush();
                        printRow[i] = buffer.toString();
                    }
                    if (printRow[i] instanceof String) {
                        String value = (String) printRow[i];
                        if (value.contains("\n")) {
                            printRow[i] = reformatMultilineValue(value);
                        }
                    }
                }
                String usedFormat = this.format;
                if (usedFormat == null || usedFormat.length() == 0) {
                    StringBuilder buffer = new StringBuilder();
                    for (int i = 0; i < printRow.length; i++) {
                        buffer.append("  ");
                        buffer.append(getFormat(printRow[i], columnWidth, i));
                    }
                    usedFormat = buffer.toString();
                }
                String line = String.format(usedFormat, printRow);
                out.println(line);
            }
        }
        out.flush();
    }

    private static final HashMap<Class<?>, String> typeFormats = new HashMap<Class<?>, String>();

    static {
        typeFormats.put(Integer.class, "%15d");
        typeFormats.put(Short.class, "%15d");
        typeFormats.put(Byte.class, "%15d");
        typeFormats.put(Long.class, "%15d");
        typeFormats.put(BigInteger.class, "%15d");
        typeFormats.put(Double.class, "%15f");
        typeFormats.put(Float.class, "%15f");
        typeFormats.put(BigDecimal.class, "%15f");
        typeFormats.put(String.class, "%-50s");
    }

    private String getFormat(Object object, Map<Integer, Integer> columnWidth, Integer column) {
        if (object == null) {
            return "%-15s";
        }
        String usedFormat = typeFormats.get(object.getClass());
        if (usedFormat == null) {
            return "%-15s";
        }
        if (columnWidth != null && column != null && columnWidth.containsKey(column)) {
            usedFormat = usedFormat.replaceFirst("\\d+", String.valueOf(columnWidth.get(column)));
        }
        return usedFormat;
    }

    private String reformatMultilineValue(String value) {
        StringBuilder result = new StringBuilder();
        result.append("\n");
        for (String line : value.split("\n")) {
            result.append("\t").append(line);
            result.append("\n");
        }
        return result.toString();
    }

    protected void write(ObjectOutput out) throws IOException {
        out.writeUTF(name != null ? name : "");
        if (data != null) {
            out.writeUTF(format != null ? format : "");
            out.writeInt(data.size());
            for (Object[] row : data) {
                out.writeInt(row.length);
                for (Object cell : row) {
                    out.writeObject(cell);
                }
            }
        } else {
            out.writeInt(0);
        }
    }

    protected void read(ObjectInput in) throws IOException, ClassNotFoundException {
        name = in.readUTF();
        format = in.readUTF();
        if (format.length() == 0)
            format = null;
        int rowCount = in.readInt();
        data = new ArrayList<Object[]>(rowCount);
        for (int i = 0; i < rowCount; i++) {
            int cellCount = in.readInt();
            Object[] row = new Object[cellCount];
            for (int j = 0; j < cellCount; j++) {
                row[j] = in.readObject();
            }
            data.add(row);
        }
    }
}
