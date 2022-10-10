package com.sun.tools.internal.xjc.reader.relaxng;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import com.sun.tools.internal.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.internal.xjc.model.TypeUse;
import com.sun.tools.internal.xjc.reader.xmlschema.SimpleTypeBuilder;
import com.sun.xml.internal.rngom.xml.util.WellKnownNamespaces;

final class DatatypeLib {

    public final String nsUri;

    private final Map<String, TypeUse> types;

    public DatatypeLib(String nsUri, Map<String, TypeUse> types) {
        this.nsUri = nsUri;
        this.types = Collections.unmodifiableMap(types);
    }

    TypeUse get(String name) {
        return types.get(name);
    }

    public static final DatatypeLib BUILTIN;

    public static final DatatypeLib XMLSCHEMA = new DatatypeLib(WellKnownNamespaces.XML_SCHEMA_DATATYPES, SimpleTypeBuilder.builtinConversions);

    static {
        Map<String, TypeUse> builtinTypes = new HashMap<>();
        builtinTypes.put("token", CBuiltinLeafInfo.TOKEN);
        builtinTypes.put("string", CBuiltinLeafInfo.STRING);
        BUILTIN = new DatatypeLib("", builtinTypes);
    }
}
