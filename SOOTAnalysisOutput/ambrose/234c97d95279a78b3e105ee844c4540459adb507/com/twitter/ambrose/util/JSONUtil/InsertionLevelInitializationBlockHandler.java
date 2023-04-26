package com.twitter.ambrose.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

public class JSONUtil {

    public static void writeJson(Writer writer, Object object) throws IOException {
        mapper.writeValue(writer, object);
    }

    public static void writeJson(String fileName, Object object) throws IOException {
        Writer writer = new PrintWriter(fileName);
        try {
            JSONUtil.writeJson(writer, object);
        } finally {
            writer.close();
        }
    }

    public static String toJson(Object object) throws IOException {
        StringWriter writer = new StringWriter();
        writeJson(writer, object);
        return writer.toString();
    }

    public static <T> T toObject(String json, TypeReference<T> type) throws IOException {
        return mapper.readValue(json, type);
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

    private static final ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);
        mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT, false);
        mapper.disable(SerializationFeature.FLUSH_AFTER_WRITE_VALUE);
        mapper.disable(SerializationFeature.CLOSE_CLOSEABLE);
        mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

    public static void mixinAnnotatons(Class<?> target, Class<?> mixinSource) {
        mapper.addMixInAnnotations(target, mixinSource);
    }
}