package com.twitter.ambrose.util;

import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;
import org.codehaus.jackson.type.TypeReference;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

public class JSONUtil {

    public static void writeJson(Writer writer, Object object) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.configure(SerializationConfig.Feature.INDENT_OUTPUT, true);
        om.configure(SerializationConfig.Feature.FAIL_ON_EMPTY_BEANS, false);
        writer.write(om.writeValueAsString(object));
        writer.write("\n");
        writer.flush();
    }

    public static void writeJson(String fileName, Object object) throws IOException {
        JSONUtil.writeJson(new PrintWriter(fileName), object);
    }

    public static Object readJson(String json, TypeReference<?> type) throws IOException {
        ObjectMapper om = new ObjectMapper();
        om.getDeserializationConfig().set(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        return om.readValue(json, type);
    }

    public static String readFile(String path) throws IOException {
        FileInputStream stream = new FileInputStream(new File(path));
        try {
            FileChannel fc = stream.getChannel();
            MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
            return Charset.defaultCharset().decode(bb).toString();
        } finally {
            stream.close();
        }
    }
}
