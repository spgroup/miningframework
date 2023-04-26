package com.fasterxml.jackson.databind.ext;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdScalarDeserializer;

public class NioPathDeserializer extends StdScalarDeserializer<Path> {

    private static final long serialVersionUID = 1;

    public NioPathDeserializer() {
        super(Path.class);
    }

    @Override
    public Path deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        if (!p.hasToken(JsonToken.VALUE_STRING)) {
            return (Path) ctxt.handleUnexpectedToken(Path.class, p);
        }
        final String value = p.getText();
        if (value.indexOf(':') < 0) {
            return Paths.get(value);
        }
        try {
            URI uri = new URI(value);
            return Paths.get(uri);
        } catch (URISyntaxException e) {
            return (Path) ctxt.handleInstantiationProblem(handledType(), value, e);
        }
    }
}
