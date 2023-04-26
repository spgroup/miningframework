package com.oracle.svm.test;

import org.junit.Assert;
import org.junit.Test;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

public class AbstractClassSerializationTest implements Serializable {

    private static final long serialVersionUID = 1L;

    private static final byte[] serializedObject;

    private static final Map<Integer, String> map;

    static {
        map = new HashMap<>();
        map.put(1, "Test");
        map.put(2, "This is a test");
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        ObjectOutputStream objectOutputStream;
        try {
            objectOutputStream = new ObjectOutputStream(byteArrayOutputStream);
            objectOutputStream.writeObject(map);
            objectOutputStream.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
        serializedObject = byteArrayOutputStream.toByteArray();
    }

    @Test
    public void testAbstractClassSerialization() {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(serializedObject);
        try {
            ObjectInputStream objectInputStream = new ObjectInputStream(byteArrayInputStream);
            Object deserializedObject = objectInputStream.readObject();
            Assert.assertEquals(deserializedObject.toString(), map.toString());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}