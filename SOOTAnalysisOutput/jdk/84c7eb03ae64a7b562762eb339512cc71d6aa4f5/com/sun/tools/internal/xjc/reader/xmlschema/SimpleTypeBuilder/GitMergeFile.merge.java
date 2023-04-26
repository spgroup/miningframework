package com.sun.tools.internal.xjc.reader.xmlschema;

import java.io.StringWriter;
import java.math.BigInteger;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;
import javax.activation.MimeTypeParseException;
import javax.xml.bind.DatatypeConverter;
import com.sun.codemodel.internal.JJavaName;
import com.sun.codemodel.internal.util.JavadocEscapeWriter;
import com.sun.xml.internal.bind.v2.WellKnownNamespace;
import com.sun.tools.internal.xjc.ErrorReceiver;
import com.sun.tools.internal.xjc.model.CBuiltinLeafInfo;
import com.sun.tools.internal.xjc.model.CClassInfo;
import com.sun.tools.internal.xjc.model.CClassInfoParent;
import com.sun.tools.internal.xjc.model.CClassRef;
import com.sun.tools.internal.xjc.model.CEnumConstant;
import com.sun.tools.internal.xjc.model.CEnumLeafInfo;
import com.sun.tools.internal.xjc.model.CNonElement;
import com.sun.tools.internal.xjc.model.Model;
import com.sun.tools.internal.xjc.model.TypeUse;
import com.sun.tools.internal.xjc.model.TypeUseFactory;
import com.sun.tools.internal.xjc.reader.Const;
import com.sun.tools.internal.xjc.reader.Ring;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIEnum;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIEnumMember;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIProperty;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BindInfo;
import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.EnumMemberMode;
import com.sun.tools.internal.xjc.util.MimeTypeRange;
import static com.sun.xml.internal.bind.v2.WellKnownNamespace.XML_MIME_URI;
import com.sun.xml.internal.bind.v2.runtime.SwaRefAdapterMarker;
import com.sun.xml.internal.xsom.XSAttributeDecl;
import com.sun.xml.internal.xsom.XSComplexType;
import com.sun.xml.internal.xsom.XSComponent;
import com.sun.xml.internal.xsom.XSElementDecl;
import com.sun.xml.internal.xsom.XSFacet;
import com.sun.xml.internal.xsom.XSListSimpleType;
import com.sun.xml.internal.xsom.XSRestrictionSimpleType;
import com.sun.xml.internal.xsom.XSSimpleType;
import com.sun.xml.internal.xsom.XSUnionSimpleType;
import com.sun.xml.internal.xsom.XSVariety;
import com.sun.xml.internal.xsom.impl.util.SchemaWriter;
import com.sun.xml.internal.xsom.visitor.XSSimpleTypeFunction;
import com.sun.xml.internal.xsom.visitor.XSVisitor;
import org.xml.sax.Locator;

public final class SimpleTypeBuilder extends BindingComponent {

    protected final BGMBuilder builder = Ring.get(BGMBuilder.class);

    private final Model model = Ring.get(Model.class);

    public final Stack<XSComponent> refererStack = new Stack<XSComponent>();

    private final Set<XSComponent> acknowledgedXmimeContentTypes = new HashSet<XSComponent>();

    private XSSimpleType initiatingType;

    public static final Map<String, TypeUse> builtinConversions = new HashMap<String, TypeUse>();

    public TypeUse build(XSSimpleType type) {
        XSSimpleType oldi = initiatingType;
        this.initiatingType = type;
        TypeUse e = checkRefererCustomization(type);
        if (e == null)
            e = compose(type);
        initiatingType = oldi;
        return e;
    }

    public TypeUse buildDef(XSSimpleType type) {
        XSSimpleType oldi = initiatingType;
        this.initiatingType = type;
        TypeUse e = type.apply(composer);
        initiatingType = oldi;
        return e;
    }

    private BIConversion getRefererCustomization() {
        BindInfo info = builder.getBindInfo(getReferer());
        BIProperty prop = info.get(BIProperty.class);
        if (prop == null)
            return null;
        return prop.getConv();
    }

    public XSComponent getReferer() {
        return refererStack.peek();
    }

    private TypeUse checkRefererCustomization(XSSimpleType type) {
        XSComponent top = getReferer();
        if (top instanceof XSElementDecl) {
            XSElementDecl eref = (XSElementDecl) top;
            assert eref.getType() == type;
            BindInfo info = builder.getBindInfo(top);
            BIConversion conv = info.get(BIConversion.class);
            if (conv != null) {
                conv.markAsAcknowledged();
                return conv.getTypeUse(type);
            }
            detectJavaTypeCustomization();
        } else if (top instanceof XSAttributeDecl) {
            XSAttributeDecl aref = (XSAttributeDecl) top;
            assert aref.getType() == type;
            detectJavaTypeCustomization();
        } else if (top instanceof XSComplexType) {
            XSComplexType tref = (XSComplexType) top;
            assert tref.getBaseType() == type || tref.getContentType() == type;
            detectJavaTypeCustomization();
        } else if (top == type) {
        } else
            assert false;
        BIConversion conv = getRefererCustomization();
        if (conv != null) {
            conv.markAsAcknowledged();
            return conv.getTypeUse(type);
        } else
            return null;
    }

    private void detectJavaTypeCustomization() {
        BindInfo info = builder.getBindInfo(getReferer());
        BIConversion conv = info.get(BIConversion.class);
        if (conv != null) {
            conv.markAsAcknowledged();
            getErrorReporter().error(conv.getLocation(), Messages.ERR_UNNESTED_JAVATYPE_CUSTOMIZATION_ON_SIMPLETYPE);
        }
    }

    TypeUse compose(XSSimpleType t) {
        TypeUse e = find(t);
        if (e != null)
            return e;
        return t.apply(composer);
    }

    public final XSSimpleTypeFunction<TypeUse> composer = new XSSimpleTypeFunction<TypeUse>() {

        public TypeUse listSimpleType(XSListSimpleType type) {
            XSSimpleType itemType = type.getItemType();
            refererStack.push(itemType);
            TypeUse tu = TypeUseFactory.makeCollection(build(type.getItemType()));
            refererStack.pop();
            return tu;
        }

        public TypeUse unionSimpleType(XSUnionSimpleType type) {
            boolean isCollection = false;
            for (int i = 0; i < type.getMemberSize(); i++) if (type.getMember(i).getVariety() == XSVariety.LIST || type.getMember(i).getVariety() == XSVariety.UNION) {
                isCollection = true;
                break;
            }
            TypeUse r = CBuiltinLeafInfo.STRING;
            if (isCollection)
                r = TypeUseFactory.makeCollection(r);
            return r;
        }

        public TypeUse restrictionSimpleType(XSRestrictionSimpleType type) {
            return compose(type.getSimpleBaseType());
        }
    };

    private TypeUse find(XSSimpleType type) {
        TypeUse r;
        boolean noAutoEnum = false;
        BindInfo info = builder.getBindInfo(type);
        BIConversion conv = info.get(BIConversion.class);
        if (conv != null) {
            conv.markAsAcknowledged();
            return conv.getTypeUse(type);
        }
        BIEnum en = info.get(BIEnum.class);
        if (en != null) {
            en.markAsAcknowledged();
            if (!en.isMapped()) {
                noAutoEnum = true;
            } else {
                if (!canBeMappedToTypeSafeEnum(type)) {
                    getErrorReporter().error(en.getLocation(), Messages.ERR_CANNOT_BE_TYPE_SAFE_ENUM);
                    getErrorReporter().error(type.getLocator(), Messages.ERR_CANNOT_BE_TYPE_SAFE_ENUM_LOCATION);
                    return null;
                }
                if (en.ref != null) {
                    if (!JJavaName.isFullyQualifiedClassName(en.ref)) {
                        Ring.get(ErrorReceiver.class).error(en.getLocation(), Messages.format(Messages.ERR_INCORRECT_CLASS_NAME, en.ref));
                        return null;
                    }
                    return new CClassRef(model, type, en, info.toCustomizationList());
                }
                return bindToTypeSafeEnum((XSRestrictionSimpleType) type, en.className, en.javadoc, en.members, getEnumMemberMode().getModeWithEnum(), en.getLocation());
            }
        }
        if (type.getTargetNamespace().equals(WellKnownNamespace.XML_SCHEMA)) {
            String name = type.getName();
            if (name != null) {
                r = lookupBuiltin(name);
                if (r != null)
                    return r;
            }
        }
        if (type.getTargetNamespace().equals(WellKnownNamespace.SWA_URI)) {
            String name = type.getName();
            if (name != null && name.equals("swaRef"))
                return CBuiltinLeafInfo.STRING.makeAdapted(SwaRefAdapterMarker.class, false);
        }
        if (type.isRestriction() && !noAutoEnum) {
            XSRestrictionSimpleType rst = type.asRestriction();
            if (shouldBeMappedToTypeSafeEnumByDefault(rst)) {
                r = bindToTypeSafeEnum(rst, null, null, Collections.<String, BIEnumMember>emptyMap(), getEnumMemberMode(), null);
                if (r != null)
                    return r;
            }
        }
        return (CNonElement) getClassSelector()._bindToClass(type, null, false);
    }

    private static Set<XSRestrictionSimpleType> reportedEnumMemberSizeWarnings;

    private boolean shouldBeMappedToTypeSafeEnumByDefault(XSRestrictionSimpleType type) {
        if (type.isLocal())
            return false;
        if (type.getRedefinedBy() != null)
            return false;
        List<XSFacet> facets = type.getDeclaredFacets(XSFacet.FACET_ENUMERATION);
        if (facets.isEmpty())
            return false;
        if (facets.size() > builder.getGlobalBinding().getDefaultEnumMemberSizeCap()) {
            if (reportedEnumMemberSizeWarnings == null)
                reportedEnumMemberSizeWarnings = new HashSet<XSRestrictionSimpleType>();
            if (!reportedEnumMemberSizeWarnings.contains(type)) {
                getErrorReporter().warning(type.getLocator(), Messages.WARN_ENUM_MEMBER_SIZE_CAP, type.getName(), facets.size(), builder.getGlobalBinding().getDefaultEnumMemberSizeCap());
                reportedEnumMemberSizeWarnings.add(type);
            }
            return false;
        }
        if (!canBeMappedToTypeSafeEnum(type))
            return false;
        for (XSSimpleType t = type; t != null; t = t.getSimpleBaseType()) if (t.isGlobal() && builder.getGlobalBinding().canBeMappedToTypeSafeEnum(t))
            return true;
        return false;
    }

    private static final Set<String> builtinTypeSafeEnumCapableTypes;

    static {
        Set<String> s = new HashSet<String>();
        String[] typeNames = new String[] { "string", "boolean", "float", "decimal", "double", "anyURI" };
        s.addAll(Arrays.asList(typeNames));
        builtinTypeSafeEnumCapableTypes = Collections.unmodifiableSet(s);
    }

    public static boolean canBeMappedToTypeSafeEnum(XSSimpleType type) {
        do {
            if (WellKnownNamespace.XML_SCHEMA.equals(type.getTargetNamespace())) {
                String localName = type.getName();
                if (localName != null) {
                    if (localName.equals("anySimpleType"))
                        return false;
                    if (localName.equals("ID") || localName.equals("IDREF"))
                        return false;
                    if (builtinTypeSafeEnumCapableTypes.contains(localName))
                        return true;
                }
            }
            type = type.getSimpleBaseType();
        } while (type != null);
        return false;
    }

    private TypeUse bindToTypeSafeEnum(XSRestrictionSimpleType type, String className, String javadoc, Map<String, BIEnumMember> members, EnumMemberMode mode, Locator loc) {
        if (loc == null)
            loc = type.getLocator();
        if (className == null) {
            if (!type.isGlobal()) {
                getErrorReporter().error(loc, Messages.ERR_NO_ENUM_NAME_AVAILABLE);
                return CBuiltinLeafInfo.STRING;
            }
            className = type.getName();
        }
        className = builder.deriveName(className, type);
        {
            StringWriter out = new StringWriter();
            SchemaWriter sw = new SchemaWriter(new JavadocEscapeWriter(out));
            type.visit((XSVisitor) sw);
            if (javadoc != null)
                javadoc += "\n\n";
            else
                javadoc = "";
            javadoc += Messages.format(Messages.JAVADOC_HEADING, type.getName()) + "\n<p>\n<pre>\n" + out.getBuffer() + "</pre>";
        }
        refererStack.push(type.getSimpleBaseType());
        TypeUse use = build(type.getSimpleBaseType());
        refererStack.pop();
        if (use.isCollection())
            return null;
        CNonElement baseDt = use.getInfo();
        if (baseDt instanceof CClassInfo)
            return null;
        XSFacet[] errorRef = new XSFacet[1];
        List<CEnumConstant> memberList = buildCEnumConstants(type, false, members, errorRef);
        if (memberList == null || checkMemberNameCollision(memberList) != null) {
            switch(mode) {
                case SKIP:
                    return null;
                case ERROR:
                    if (memberList == null) {
                        getErrorReporter().error(errorRef[0].getLocator(), Messages.ERR_CANNOT_GENERATE_ENUM_NAME, errorRef[0].getValue());
                    } else {
                        CEnumConstant[] collision = checkMemberNameCollision(memberList);
                        getErrorReporter().error(collision[0].getLocator(), Messages.ERR_ENUM_MEMBER_NAME_COLLISION, collision[0].getName());
                        getErrorReporter().error(collision[1].getLocator(), Messages.ERR_ENUM_MEMBER_NAME_COLLISION_RELATED);
                    }
                    return null;
                case GENERATE:
                    memberList = buildCEnumConstants(type, true, members, null);
                    break;
            }
        }
        if (memberList.isEmpty()) {
            getErrorReporter().error(loc, Messages.ERR_NO_ENUM_FACET);
            return null;
        }
        CClassInfoParent scope;
        if (type.isGlobal())
            scope = new CClassInfoParent.Package(getClassSelector().getPackage(type.getTargetNamespace()));
        else
            scope = getClassSelector().getClassScope();
        CEnumLeafInfo xducer = new CEnumLeafInfo(model, BGMBuilder.getName(type), scope, className, baseDt, memberList, type, builder.getBindInfo(type).toCustomizationList(), loc);
        xducer.javadoc = javadoc;
        BIConversion conv = new BIConversion.Static(type.getLocator(), xducer);
        conv.markAsAcknowledged();
        builder.getOrCreateBindInfo(type).addDecl(conv);
        return conv.getTypeUse(type);
    }

    private List<CEnumConstant> buildCEnumConstants(XSRestrictionSimpleType type, boolean needsToGenerateMemberName, Map<String, BIEnumMember> members, XSFacet[] errorRef) {
        List<CEnumConstant> memberList = new ArrayList<CEnumConstant>();
        int idx = 1;
        Set<String> enums = new HashSet<String>();
        for (XSFacet facet : type.getDeclaredFacets(XSFacet.FACET_ENUMERATION)) {
            String name = null;
            String mdoc = builder.getBindInfo(facet).getDocumentation();
            if (!enums.add(facet.getValue().value))
                continue;
            if (needsToGenerateMemberName) {
                name = "VALUE_" + (idx++);
            } else {
                String facetValue = facet.getValue().value;
                BIEnumMember mem = members.get(facetValue);
                if (mem == null)
                    mem = builder.getBindInfo(facet).get(BIEnumMember.class);
                if (mem != null) {
                    name = mem.name;
                    if (mdoc == null) {
                        mdoc = mem.javadoc;
                    }
                }
                if (name == null) {
                    StringBuilder sb = new StringBuilder();
                    for (int i = 0; i < facetValue.length(); i++) {
                        char ch = facetValue.charAt(i);
                        if (Character.isJavaIdentifierPart(ch))
                            sb.append(ch);
                        else
                            sb.append('_');
                    }
                    name = model.getNameConverter().toConstantName(sb.toString());
                }
            }
            if (!JJavaName.isJavaIdentifier(name)) {
                if (errorRef != null)
                    errorRef[0] = facet;
                return null;
            }
            memberList.add(new CEnumConstant(name, mdoc, facet.getValue().value, facet, builder.getBindInfo(facet).toCustomizationList(), facet.getLocator()));
        }
        return memberList;
    }

    private CEnumConstant[] checkMemberNameCollision(List<CEnumConstant> memberList) {
        Map<String, CEnumConstant> names = new HashMap<String, CEnumConstant>();
        for (CEnumConstant c : memberList) {
            CEnumConstant old = names.put(c.getName(), c);
            if (old != null)
                return new CEnumConstant[] { old, c };
        }
        return null;
    }

    private EnumMemberMode getEnumMemberMode() {
        return builder.getGlobalBinding().getEnumMemberMode();
    }

    private TypeUse lookupBuiltin(String typeLocalName) {
        if (typeLocalName.equals("integer") || typeLocalName.equals("long")) {
            BigInteger xe = readFacet(XSFacet.FACET_MAXEXCLUSIVE, -1);
            BigInteger xi = readFacet(XSFacet.FACET_MAXINCLUSIVE, 0);
            BigInteger max = min(xe, xi);
            if (max != null) {
                BigInteger ne = readFacet(XSFacet.FACET_MINEXCLUSIVE, +1);
                BigInteger ni = readFacet(XSFacet.FACET_MININCLUSIVE, 0);
                BigInteger min = max(ne, ni);
                if (min != null) {
                    if (min.compareTo(INT_MIN) >= 0 && max.compareTo(INT_MAX) <= 0)
                        typeLocalName = "int";
                    else if (min.compareTo(LONG_MIN) >= 0 && max.compareTo(LONG_MAX) <= 0)
                        typeLocalName = "long";
                }
            }
        } else if (typeLocalName.equals("boolean") && isRestrictedTo0And1()) {
            return CBuiltinLeafInfo.BOOLEAN_ZERO_OR_ONE;
        } else if (typeLocalName.equals("base64Binary")) {
            return lookupBinaryTypeBinding();
        } else if (typeLocalName.equals("anySimpleType")) {
            if (getReferer() instanceof XSAttributeDecl || getReferer() instanceof XSSimpleType)
                return CBuiltinLeafInfo.STRING;
            else
                return CBuiltinLeafInfo.ANYTYPE;
        }
        return builtinConversions.get(typeLocalName);
    }

    private TypeUse lookupBinaryTypeBinding() {
        XSComponent referer = getReferer();
        String emt = referer.getForeignAttribute(XML_MIME_URI, Const.EXPECTED_CONTENT_TYPES);
        if (emt != null) {
            acknowledgedXmimeContentTypes.add(referer);
            try {
                List<MimeTypeRange> types = MimeTypeRange.parseRanges(emt);
                MimeTypeRange mt = MimeTypeRange.merge(types);
                if (mt.majorType.equalsIgnoreCase("image"))
                    return CBuiltinLeafInfo.IMAGE.makeMimeTyped(mt.toMimeType());
                if ((mt.majorType.equalsIgnoreCase("application") || mt.majorType.equalsIgnoreCase("text")) && isXml(mt.subType))
                    return CBuiltinLeafInfo.XML_SOURCE.makeMimeTyped(mt.toMimeType());
                if ((mt.majorType.equalsIgnoreCase("text") && (mt.subType.equalsIgnoreCase("plain")))) {
                    return CBuiltinLeafInfo.STRING.makeMimeTyped(mt.toMimeType());
                }
                return CBuiltinLeafInfo.DATA_HANDLER.makeMimeTyped(mt.toMimeType());
            } catch (ParseException e) {
                getErrorReporter().error(referer.getLocator(), Messages.format(Messages.ERR_ILLEGAL_EXPECTED_MIME_TYPE, emt, e.getMessage()));
            } catch (MimeTypeParseException e) {
                getErrorReporter().error(referer.getLocator(), Messages.format(Messages.ERR_ILLEGAL_EXPECTED_MIME_TYPE, emt, e.getMessage()));
            }
        }
        return CBuiltinLeafInfo.BASE64_BYTE_ARRAY;
    }

    public boolean isAcknowledgedXmimeContentTypes(XSComponent c) {
        return acknowledgedXmimeContentTypes.contains(c);
    }

    private boolean isXml(String subType) {
        return subType.equals("xml") || subType.endsWith("+xml");
    }

    private boolean isRestrictedTo0And1() {
        XSFacet pattern = initiatingType.getFacet(XSFacet.FACET_PATTERN);
        if (pattern != null) {
            String v = pattern.getValue().value;
            if (v.equals("0|1") || v.equals("1|0") || v.equals("\\d"))
                return true;
        }
        XSFacet enumf = initiatingType.getFacet(XSFacet.FACET_ENUMERATION);
        if (enumf != null) {
            String v = enumf.getValue().value;
            if (v.equals("0") || v.equals("1"))
                return true;
        }
        return false;
    }

    private BigInteger readFacet(String facetName, int offset) {
        XSFacet me = initiatingType.getFacet(facetName);
        if (me == null)
            return null;
        BigInteger bi = DatatypeConverter.parseInteger(me.getValue().value);
        if (offset != 0)
            bi = bi.add(BigInteger.valueOf(offset));
        return bi;
    }

    private BigInteger min(BigInteger a, BigInteger b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a.min(b);
    }

    private BigInteger max(BigInteger a, BigInteger b) {
        if (a == null)
            return b;
        if (b == null)
            return a;
        return a.max(b);
    }

    private static final BigInteger LONG_MIN = BigInteger.valueOf(Long.MIN_VALUE);

    private static final BigInteger LONG_MAX = BigInteger.valueOf(Long.MAX_VALUE);

    private static final BigInteger INT_MIN = BigInteger.valueOf(Integer.MIN_VALUE);

    private static final BigInteger INT_MAX = BigInteger.valueOf(Integer.MAX_VALUE);

    static {
        Map<String, TypeUse> m = builtinConversions;
        m.put("string", CBuiltinLeafInfo.STRING);
        m.put("anyURI", CBuiltinLeafInfo.STRING);
        m.put("boolean", CBuiltinLeafInfo.BOOLEAN);
        m.put("hexBinary", CBuiltinLeafInfo.HEXBIN_BYTE_ARRAY);
        m.put("float", CBuiltinLeafInfo.FLOAT);
        m.put("decimal", CBuiltinLeafInfo.BIG_DECIMAL);
        m.put("integer", CBuiltinLeafInfo.BIG_INTEGER);
        m.put("long", CBuiltinLeafInfo.LONG);
        m.put("unsignedInt", CBuiltinLeafInfo.LONG);
        m.put("int", CBuiltinLeafInfo.INT);
        m.put("unsignedShort", CBuiltinLeafInfo.INT);
        m.put("short", CBuiltinLeafInfo.SHORT);
        m.put("unsignedByte", CBuiltinLeafInfo.SHORT);
        m.put("byte", CBuiltinLeafInfo.BYTE);
        m.put("double", CBuiltinLeafInfo.DOUBLE);
        m.put("QName", CBuiltinLeafInfo.QNAME);
        m.put("NOTATION", CBuiltinLeafInfo.QNAME);
        m.put("dateTime", CBuiltinLeafInfo.CALENDAR);
        m.put("date", CBuiltinLeafInfo.CALENDAR);
        m.put("time", CBuiltinLeafInfo.CALENDAR);
        m.put("gYearMonth", CBuiltinLeafInfo.CALENDAR);
        m.put("gYear", CBuiltinLeafInfo.CALENDAR);
        m.put("gMonthDay", CBuiltinLeafInfo.CALENDAR);
        m.put("gDay", CBuiltinLeafInfo.CALENDAR);
        m.put("gMonth", CBuiltinLeafInfo.CALENDAR);
        m.put("duration", CBuiltinLeafInfo.DURATION);
        m.put("token", CBuiltinLeafInfo.TOKEN);
        m.put("normalizedString", CBuiltinLeafInfo.NORMALIZED_STRING);
        m.put("ID", CBuiltinLeafInfo.ID);
        m.put("IDREF", CBuiltinLeafInfo.IDREF);
    }
}
