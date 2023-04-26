package com.sun.org.apache.xerces.internal.impl.xs.traversers;

import java.util.Iterator;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.Vector;
import com.sun.org.apache.xerces.internal.impl.dv.InvalidDatatypeValueException;
import com.sun.org.apache.xerces.internal.impl.dv.XSSimpleType;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaGrammar;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaNamespaceSupport;
import com.sun.org.apache.xerces.internal.impl.xs.SchemaSymbols;
import com.sun.org.apache.xerces.internal.impl.xs.XSAttributeDecl;
import com.sun.org.apache.xerces.internal.impl.xs.XSGrammarBucket;
import com.sun.org.apache.xerces.internal.impl.xs.XSWildcardDecl;
import com.sun.org.apache.xerces.internal.impl.xs.util.XInt;
import com.sun.org.apache.xerces.internal.impl.xs.util.XIntPool;
import com.sun.org.apache.xerces.internal.util.DOMUtil;
import com.sun.org.apache.xerces.internal.util.SymbolTable;
import com.sun.org.apache.xerces.internal.util.XMLChar;
import com.sun.org.apache.xerces.internal.util.XMLSymbols;
import com.sun.org.apache.xerces.internal.utils.XMLSecurityManager;
import com.sun.org.apache.xerces.internal.xni.QName;
import com.sun.org.apache.xerces.internal.xs.XSConstants;
import java.util.HashMap;
import org.w3c.dom.Attr;
import org.w3c.dom.Element;

public class XSAttributeChecker {

    private static final String ELEMENT_N = "element_n";

    private static final String ELEMENT_R = "element_r";

    private static final String ATTRIBUTE_N = "attribute_n";

    private static final String ATTRIBUTE_R = "attribute_r";

    private static int ATTIDX_COUNT = 0;

    public static final int ATTIDX_ABSTRACT = ATTIDX_COUNT++;

    public static final int ATTIDX_AFORMDEFAULT = ATTIDX_COUNT++;

    public static final int ATTIDX_BASE = ATTIDX_COUNT++;

    public static final int ATTIDX_BLOCK = ATTIDX_COUNT++;

    public static final int ATTIDX_BLOCKDEFAULT = ATTIDX_COUNT++;

    public static final int ATTIDX_DEFAULT = ATTIDX_COUNT++;

    public static final int ATTIDX_EFORMDEFAULT = ATTIDX_COUNT++;

    public static final int ATTIDX_FINAL = ATTIDX_COUNT++;

    public static final int ATTIDX_FINALDEFAULT = ATTIDX_COUNT++;

    public static final int ATTIDX_FIXED = ATTIDX_COUNT++;

    public static final int ATTIDX_FORM = ATTIDX_COUNT++;

    public static final int ATTIDX_ID = ATTIDX_COUNT++;

    public static final int ATTIDX_ITEMTYPE = ATTIDX_COUNT++;

    public static final int ATTIDX_MAXOCCURS = ATTIDX_COUNT++;

    public static final int ATTIDX_MEMBERTYPES = ATTIDX_COUNT++;

    public static final int ATTIDX_MINOCCURS = ATTIDX_COUNT++;

    public static final int ATTIDX_MIXED = ATTIDX_COUNT++;

    public static final int ATTIDX_NAME = ATTIDX_COUNT++;

    public static final int ATTIDX_NAMESPACE = ATTIDX_COUNT++;

    public static final int ATTIDX_NAMESPACE_LIST = ATTIDX_COUNT++;

    public static final int ATTIDX_NILLABLE = ATTIDX_COUNT++;

    public static final int ATTIDX_NONSCHEMA = ATTIDX_COUNT++;

    public static final int ATTIDX_PROCESSCONTENTS = ATTIDX_COUNT++;

    public static final int ATTIDX_PUBLIC = ATTIDX_COUNT++;

    public static final int ATTIDX_REF = ATTIDX_COUNT++;

    public static final int ATTIDX_REFER = ATTIDX_COUNT++;

    public static final int ATTIDX_SCHEMALOCATION = ATTIDX_COUNT++;

    public static final int ATTIDX_SOURCE = ATTIDX_COUNT++;

    public static final int ATTIDX_SUBSGROUP = ATTIDX_COUNT++;

    public static final int ATTIDX_SYSTEM = ATTIDX_COUNT++;

    public static final int ATTIDX_TARGETNAMESPACE = ATTIDX_COUNT++;

    public static final int ATTIDX_TYPE = ATTIDX_COUNT++;

    public static final int ATTIDX_USE = ATTIDX_COUNT++;

    public static final int ATTIDX_VALUE = ATTIDX_COUNT++;

    public static final int ATTIDX_ENUMNSDECLS = ATTIDX_COUNT++;

    public static final int ATTIDX_VERSION = ATTIDX_COUNT++;

    public static final int ATTIDX_XML_LANG = ATTIDX_COUNT++;

    public static final int ATTIDX_XPATH = ATTIDX_COUNT++;

    public static final int ATTIDX_FROMDEFAULT = ATTIDX_COUNT++;

    public static final int ATTIDX_ISRETURNED = ATTIDX_COUNT++;

    private static final XIntPool fXIntPool = new XIntPool();

    private static final XInt INT_QUALIFIED = fXIntPool.getXInt(SchemaSymbols.FORM_QUALIFIED);

    private static final XInt INT_UNQUALIFIED = fXIntPool.getXInt(SchemaSymbols.FORM_UNQUALIFIED);

    private static final XInt INT_EMPTY_SET = fXIntPool.getXInt(XSConstants.DERIVATION_NONE);

    private static final XInt INT_ANY_STRICT = fXIntPool.getXInt(XSWildcardDecl.PC_STRICT);

    private static final XInt INT_ANY_LAX = fXIntPool.getXInt(XSWildcardDecl.PC_LAX);

    private static final XInt INT_ANY_SKIP = fXIntPool.getXInt(XSWildcardDecl.PC_SKIP);

    private static final XInt INT_ANY_ANY = fXIntPool.getXInt(XSWildcardDecl.NSCONSTRAINT_ANY);

    private static final XInt INT_ANY_LIST = fXIntPool.getXInt(XSWildcardDecl.NSCONSTRAINT_LIST);

    private static final XInt INT_ANY_NOT = fXIntPool.getXInt(XSWildcardDecl.NSCONSTRAINT_NOT);

    private static final XInt INT_USE_OPTIONAL = fXIntPool.getXInt(SchemaSymbols.USE_OPTIONAL);

    private static final XInt INT_USE_REQUIRED = fXIntPool.getXInt(SchemaSymbols.USE_REQUIRED);

    private static final XInt INT_USE_PROHIBITED = fXIntPool.getXInt(SchemaSymbols.USE_PROHIBITED);

    private static final XInt INT_WS_PRESERVE = fXIntPool.getXInt(XSSimpleType.WS_PRESERVE);

    private static final XInt INT_WS_REPLACE = fXIntPool.getXInt(XSSimpleType.WS_REPLACE);

    private static final XInt INT_WS_COLLAPSE = fXIntPool.getXInt(XSSimpleType.WS_COLLAPSE);

    private static final XInt INT_UNBOUNDED = fXIntPool.getXInt(SchemaSymbols.OCCURRENCE_UNBOUNDED);

    private static final Map fEleAttrsMapG = new HashMap(29);

    private static final Map fEleAttrsMapL = new HashMap(79);

    protected static final int DT_ANYURI = 0;

    protected static final int DT_ID = 1;

    protected static final int DT_QNAME = 2;

    protected static final int DT_STRING = 3;

    protected static final int DT_TOKEN = 4;

    protected static final int DT_NCNAME = 5;

    protected static final int DT_XPATH = 6;

    protected static final int DT_XPATH1 = 7;

    protected static final int DT_LANGUAGE = 8;

    protected static final int DT_COUNT = DT_LANGUAGE + 1;

    private static final XSSimpleType[] fExtraDVs = new XSSimpleType[DT_COUNT];

    static {
        SchemaGrammar grammar = SchemaGrammar.SG_SchemaNS;
        fExtraDVs[DT_ANYURI] = (XSSimpleType) grammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_ANYURI);
        fExtraDVs[DT_ID] = (XSSimpleType) grammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_ID);
        fExtraDVs[DT_QNAME] = (XSSimpleType) grammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_QNAME);
        fExtraDVs[DT_STRING] = (XSSimpleType) grammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_STRING);
        fExtraDVs[DT_TOKEN] = (XSSimpleType) grammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_TOKEN);
        fExtraDVs[DT_NCNAME] = (XSSimpleType) grammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_NCNAME);
        fExtraDVs[DT_XPATH] = fExtraDVs[DT_STRING];
        fExtraDVs[DT_XPATH] = fExtraDVs[DT_STRING];
        fExtraDVs[DT_LANGUAGE] = (XSSimpleType) grammar.getGlobalTypeDecl(SchemaSymbols.ATTVAL_LANGUAGE);
    }

    protected static final int DT_BLOCK = -1;

    protected static final int DT_BLOCK1 = -2;

    protected static final int DT_FINAL = -3;

    protected static final int DT_FINAL1 = -4;

    protected static final int DT_FINAL2 = -5;

    protected static final int DT_FORM = -6;

    protected static final int DT_MAXOCCURS = -7;

    protected static final int DT_MAXOCCURS1 = -8;

    protected static final int DT_MEMBERTYPES = -9;

    protected static final int DT_MINOCCURS1 = -10;

    protected static final int DT_NAMESPACE = -11;

    protected static final int DT_PROCESSCONTENTS = -12;

    protected static final int DT_USE = -13;

    protected static final int DT_WHITESPACE = -14;

    protected static final int DT_BOOLEAN = -15;

    protected static final int DT_NONNEGINT = -16;

    protected static final int DT_POSINT = -17;

    static {
        int attCount = 0;
        int ATT_ABSTRACT_D = attCount++;
        int ATT_ATTRIBUTE_FD_D = attCount++;
        int ATT_BASE_R = attCount++;
        int ATT_BASE_N = attCount++;
        int ATT_BLOCK_N = attCount++;
        int ATT_BLOCK1_N = attCount++;
        int ATT_BLOCK_D_D = attCount++;
        int ATT_DEFAULT_N = attCount++;
        int ATT_ELEMENT_FD_D = attCount++;
        int ATT_FINAL_N = attCount++;
        int ATT_FINAL1_N = attCount++;
        int ATT_FINAL_D_D = attCount++;
        int ATT_FIXED_N = attCount++;
        int ATT_FIXED_D = attCount++;
        int ATT_FORM_N = attCount++;
        int ATT_ID_N = attCount++;
        int ATT_ITEMTYPE_N = attCount++;
        int ATT_MAXOCCURS_D = attCount++;
        int ATT_MAXOCCURS1_D = attCount++;
        int ATT_MEMBER_T_N = attCount++;
        int ATT_MINOCCURS_D = attCount++;
        int ATT_MINOCCURS1_D = attCount++;
        int ATT_MIXED_D = attCount++;
        int ATT_MIXED_N = attCount++;
        int ATT_NAME_R = attCount++;
        int ATT_NAMESPACE_D = attCount++;
        int ATT_NAMESPACE_N = attCount++;
        int ATT_NILLABLE_D = attCount++;
        int ATT_PROCESS_C_D = attCount++;
        int ATT_PUBLIC_R = attCount++;
        int ATT_REF_R = attCount++;
        int ATT_REFER_R = attCount++;
        int ATT_SCHEMA_L_R = attCount++;
        int ATT_SCHEMA_L_N = attCount++;
        int ATT_SOURCE_N = attCount++;
        int ATT_SUBSTITUTION_G_N = attCount++;
        int ATT_SYSTEM_N = attCount++;
        int ATT_TARGET_N_N = attCount++;
        int ATT_TYPE_N = attCount++;
        int ATT_USE_D = attCount++;
        int ATT_VALUE_NNI_N = attCount++;
        int ATT_VALUE_PI_N = attCount++;
        int ATT_VALUE_STR_N = attCount++;
        int ATT_VALUE_WS_N = attCount++;
        int ATT_VERSION_N = attCount++;
        int ATT_XML_LANG = attCount++;
        int ATT_XPATH_R = attCount++;
        int ATT_XPATH1_R = attCount++;
        OneAttr[] allAttrs = new OneAttr[attCount];
        allAttrs[ATT_ABSTRACT_D] = new OneAttr(SchemaSymbols.ATT_ABSTRACT, DT_BOOLEAN, ATTIDX_ABSTRACT, Boolean.FALSE);
        allAttrs[ATT_ATTRIBUTE_FD_D] = new OneAttr(SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT, DT_FORM, ATTIDX_AFORMDEFAULT, INT_UNQUALIFIED);
        allAttrs[ATT_BASE_R] = new OneAttr(SchemaSymbols.ATT_BASE, DT_QNAME, ATTIDX_BASE, null);
        allAttrs[ATT_BASE_N] = new OneAttr(SchemaSymbols.ATT_BASE, DT_QNAME, ATTIDX_BASE, null);
        allAttrs[ATT_BLOCK_N] = new OneAttr(SchemaSymbols.ATT_BLOCK, DT_BLOCK, ATTIDX_BLOCK, null);
        allAttrs[ATT_BLOCK1_N] = new OneAttr(SchemaSymbols.ATT_BLOCK, DT_BLOCK1, ATTIDX_BLOCK, null);
        allAttrs[ATT_BLOCK_D_D] = new OneAttr(SchemaSymbols.ATT_BLOCKDEFAULT, DT_BLOCK, ATTIDX_BLOCKDEFAULT, INT_EMPTY_SET);
        allAttrs[ATT_DEFAULT_N] = new OneAttr(SchemaSymbols.ATT_DEFAULT, DT_STRING, ATTIDX_DEFAULT, null);
        allAttrs[ATT_ELEMENT_FD_D] = new OneAttr(SchemaSymbols.ATT_ELEMENTFORMDEFAULT, DT_FORM, ATTIDX_EFORMDEFAULT, INT_UNQUALIFIED);
        allAttrs[ATT_FINAL_N] = new OneAttr(SchemaSymbols.ATT_FINAL, DT_FINAL, ATTIDX_FINAL, null);
        allAttrs[ATT_FINAL1_N] = new OneAttr(SchemaSymbols.ATT_FINAL, DT_FINAL1, ATTIDX_FINAL, null);
        allAttrs[ATT_FINAL_D_D] = new OneAttr(SchemaSymbols.ATT_FINALDEFAULT, DT_FINAL2, ATTIDX_FINALDEFAULT, INT_EMPTY_SET);
        allAttrs[ATT_FIXED_N] = new OneAttr(SchemaSymbols.ATT_FIXED, DT_STRING, ATTIDX_FIXED, null);
        allAttrs[ATT_FIXED_D] = new OneAttr(SchemaSymbols.ATT_FIXED, DT_BOOLEAN, ATTIDX_FIXED, Boolean.FALSE);
        allAttrs[ATT_FORM_N] = new OneAttr(SchemaSymbols.ATT_FORM, DT_FORM, ATTIDX_FORM, null);
        allAttrs[ATT_ID_N] = new OneAttr(SchemaSymbols.ATT_ID, DT_ID, ATTIDX_ID, null);
        allAttrs[ATT_ITEMTYPE_N] = new OneAttr(SchemaSymbols.ATT_ITEMTYPE, DT_QNAME, ATTIDX_ITEMTYPE, null);
        allAttrs[ATT_MAXOCCURS_D] = new OneAttr(SchemaSymbols.ATT_MAXOCCURS, DT_MAXOCCURS, ATTIDX_MAXOCCURS, fXIntPool.getXInt(1));
        allAttrs[ATT_MAXOCCURS1_D] = new OneAttr(SchemaSymbols.ATT_MAXOCCURS, DT_MAXOCCURS1, ATTIDX_MAXOCCURS, fXIntPool.getXInt(1));
        allAttrs[ATT_MEMBER_T_N] = new OneAttr(SchemaSymbols.ATT_MEMBERTYPES, DT_MEMBERTYPES, ATTIDX_MEMBERTYPES, null);
        allAttrs[ATT_MINOCCURS_D] = new OneAttr(SchemaSymbols.ATT_MINOCCURS, DT_NONNEGINT, ATTIDX_MINOCCURS, fXIntPool.getXInt(1));
        allAttrs[ATT_MINOCCURS1_D] = new OneAttr(SchemaSymbols.ATT_MINOCCURS, DT_MINOCCURS1, ATTIDX_MINOCCURS, fXIntPool.getXInt(1));
        allAttrs[ATT_MIXED_D] = new OneAttr(SchemaSymbols.ATT_MIXED, DT_BOOLEAN, ATTIDX_MIXED, Boolean.FALSE);
        allAttrs[ATT_MIXED_N] = new OneAttr(SchemaSymbols.ATT_MIXED, DT_BOOLEAN, ATTIDX_MIXED, null);
        allAttrs[ATT_NAME_R] = new OneAttr(SchemaSymbols.ATT_NAME, DT_NCNAME, ATTIDX_NAME, null);
        allAttrs[ATT_NAMESPACE_D] = new OneAttr(SchemaSymbols.ATT_NAMESPACE, DT_NAMESPACE, ATTIDX_NAMESPACE, INT_ANY_ANY);
        allAttrs[ATT_NAMESPACE_N] = new OneAttr(SchemaSymbols.ATT_NAMESPACE, DT_ANYURI, ATTIDX_NAMESPACE, null);
        allAttrs[ATT_NILLABLE_D] = new OneAttr(SchemaSymbols.ATT_NILLABLE, DT_BOOLEAN, ATTIDX_NILLABLE, Boolean.FALSE);
        allAttrs[ATT_PROCESS_C_D] = new OneAttr(SchemaSymbols.ATT_PROCESSCONTENTS, DT_PROCESSCONTENTS, ATTIDX_PROCESSCONTENTS, INT_ANY_STRICT);
        allAttrs[ATT_PUBLIC_R] = new OneAttr(SchemaSymbols.ATT_PUBLIC, DT_TOKEN, ATTIDX_PUBLIC, null);
        allAttrs[ATT_REF_R] = new OneAttr(SchemaSymbols.ATT_REF, DT_QNAME, ATTIDX_REF, null);
        allAttrs[ATT_REFER_R] = new OneAttr(SchemaSymbols.ATT_REFER, DT_QNAME, ATTIDX_REFER, null);
        allAttrs[ATT_SCHEMA_L_R] = new OneAttr(SchemaSymbols.ATT_SCHEMALOCATION, DT_ANYURI, ATTIDX_SCHEMALOCATION, null);
        allAttrs[ATT_SCHEMA_L_N] = new OneAttr(SchemaSymbols.ATT_SCHEMALOCATION, DT_ANYURI, ATTIDX_SCHEMALOCATION, null);
        allAttrs[ATT_SOURCE_N] = new OneAttr(SchemaSymbols.ATT_SOURCE, DT_ANYURI, ATTIDX_SOURCE, null);
        allAttrs[ATT_SUBSTITUTION_G_N] = new OneAttr(SchemaSymbols.ATT_SUBSTITUTIONGROUP, DT_QNAME, ATTIDX_SUBSGROUP, null);
        allAttrs[ATT_SYSTEM_N] = new OneAttr(SchemaSymbols.ATT_SYSTEM, DT_ANYURI, ATTIDX_SYSTEM, null);
        allAttrs[ATT_TARGET_N_N] = new OneAttr(SchemaSymbols.ATT_TARGETNAMESPACE, DT_ANYURI, ATTIDX_TARGETNAMESPACE, null);
        allAttrs[ATT_TYPE_N] = new OneAttr(SchemaSymbols.ATT_TYPE, DT_QNAME, ATTIDX_TYPE, null);
        allAttrs[ATT_USE_D] = new OneAttr(SchemaSymbols.ATT_USE, DT_USE, ATTIDX_USE, INT_USE_OPTIONAL);
        allAttrs[ATT_VALUE_NNI_N] = new OneAttr(SchemaSymbols.ATT_VALUE, DT_NONNEGINT, ATTIDX_VALUE, null);
        allAttrs[ATT_VALUE_PI_N] = new OneAttr(SchemaSymbols.ATT_VALUE, DT_POSINT, ATTIDX_VALUE, null);
        allAttrs[ATT_VALUE_STR_N] = new OneAttr(SchemaSymbols.ATT_VALUE, DT_STRING, ATTIDX_VALUE, null);
        allAttrs[ATT_VALUE_WS_N] = new OneAttr(SchemaSymbols.ATT_VALUE, DT_WHITESPACE, ATTIDX_VALUE, null);
        allAttrs[ATT_VERSION_N] = new OneAttr(SchemaSymbols.ATT_VERSION, DT_TOKEN, ATTIDX_VERSION, null);
        allAttrs[ATT_XML_LANG] = new OneAttr(SchemaSymbols.ATT_XML_LANG, DT_LANGUAGE, ATTIDX_XML_LANG, null);
        allAttrs[ATT_XPATH_R] = new OneAttr(SchemaSymbols.ATT_XPATH, DT_XPATH, ATTIDX_XPATH, null);
        allAttrs[ATT_XPATH1_R] = new OneAttr(SchemaSymbols.ATT_XPATH, DT_XPATH1, ATTIDX_XPATH, null);
        Container attrList;
        attrList = Container.getContainer(5);
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ATTRIBUTE, attrList);
        attrList = Container.getContainer(7);
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        attrList.put(SchemaSymbols.ATT_FORM, allAttrs[ATT_FORM_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        attrList.put(SchemaSymbols.ATT_USE, allAttrs[ATT_USE_D]);
        fEleAttrsMapL.put(ATTRIBUTE_N, attrList);
        attrList = Container.getContainer(5);
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        attrList.put(SchemaSymbols.ATT_USE, allAttrs[ATT_USE_D]);
        fEleAttrsMapL.put(ATTRIBUTE_R, attrList);
        attrList = Container.getContainer(10);
        attrList.put(SchemaSymbols.ATT_ABSTRACT, allAttrs[ATT_ABSTRACT_D]);
        attrList.put(SchemaSymbols.ATT_BLOCK, allAttrs[ATT_BLOCK_N]);
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        attrList.put(SchemaSymbols.ATT_NILLABLE, allAttrs[ATT_NILLABLE_D]);
        attrList.put(SchemaSymbols.ATT_SUBSTITUTIONGROUP, allAttrs[ATT_SUBSTITUTION_G_N]);
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ELEMENT, attrList);
        attrList = Container.getContainer(10);
        attrList.put(SchemaSymbols.ATT_BLOCK, allAttrs[ATT_BLOCK_N]);
        attrList.put(SchemaSymbols.ATT_DEFAULT, allAttrs[ATT_DEFAULT_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_N]);
        attrList.put(SchemaSymbols.ATT_FORM, allAttrs[ATT_FORM_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        attrList.put(SchemaSymbols.ATT_NILLABLE, allAttrs[ATT_NILLABLE_D]);
        attrList.put(SchemaSymbols.ATT_TYPE, allAttrs[ATT_TYPE_N]);
        fEleAttrsMapL.put(ELEMENT_N, attrList);
        attrList = Container.getContainer(4);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        fEleAttrsMapL.put(ELEMENT_R, attrList);
        attrList = Container.getContainer(6);
        attrList.put(SchemaSymbols.ATT_ABSTRACT, allAttrs[ATT_ABSTRACT_D]);
        attrList.put(SchemaSymbols.ATT_BLOCK, allAttrs[ATT_BLOCK1_N]);
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MIXED, allAttrs[ATT_MIXED_D]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_COMPLEXTYPE, attrList);
        attrList = Container.getContainer(4);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        attrList.put(SchemaSymbols.ATT_PUBLIC, allAttrs[ATT_PUBLIC_R]);
        attrList.put(SchemaSymbols.ATT_SYSTEM, allAttrs[ATT_SYSTEM_N]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_NOTATION, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MIXED, allAttrs[ATT_MIXED_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_COMPLEXTYPE, attrList);
        attrList = Container.getContainer(1);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SIMPLECONTENT, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_BASE, allAttrs[ATT_BASE_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_RESTRICTION, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_BASE, allAttrs[ATT_BASE_R]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_EXTENSION, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ATTRIBUTEGROUP, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAMESPACE, allAttrs[ATT_NAMESPACE_D]);
        attrList.put(SchemaSymbols.ATT_PROCESSCONTENTS, allAttrs[ATT_PROCESS_C_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ANYATTRIBUTE, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MIXED, allAttrs[ATT_MIXED_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_COMPLEXCONTENT, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ATTRIBUTEGROUP, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_GROUP, attrList);
        attrList = Container.getContainer(4);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_REF, allAttrs[ATT_REF_R]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_GROUP, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS1_D]);
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS1_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ALL, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_CHOICE, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SEQUENCE, attrList);
        attrList = Container.getContainer(5);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MAXOCCURS, allAttrs[ATT_MAXOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_MINOCCURS, allAttrs[ATT_MINOCCURS_D]);
        attrList.put(SchemaSymbols.ATT_NAMESPACE, allAttrs[ATT_NAMESPACE_D]);
        attrList.put(SchemaSymbols.ATT_PROCESSCONTENTS, allAttrs[ATT_PROCESS_C_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ANY, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_UNIQUE, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_KEY, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        attrList.put(SchemaSymbols.ATT_REFER, allAttrs[ATT_REFER_R]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_KEYREF, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_XPATH, allAttrs[ATT_XPATH_R]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SELECTOR, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_XPATH, allAttrs[ATT_XPATH1_R]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_FIELD, attrList);
        attrList = Container.getContainer(1);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_ANNOTATION, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ANNOTATION, attrList);
        attrList = Container.getContainer(1);
        attrList.put(SchemaSymbols.ATT_SOURCE, allAttrs[ATT_SOURCE_N]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_APPINFO, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_APPINFO, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_SOURCE, allAttrs[ATT_SOURCE_N]);
        attrList.put(SchemaSymbols.ATT_XML_LANG, allAttrs[ATT_XML_LANG]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_DOCUMENTATION, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_DOCUMENTATION, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL1_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAME, allAttrs[ATT_NAME_R]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_SIMPLETYPE, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_FINAL, allAttrs[ATT_FINAL1_N]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_SIMPLETYPE, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_ITEMTYPE, allAttrs[ATT_ITEMTYPE_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_LIST, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_MEMBERTYPES, allAttrs[ATT_MEMBER_T_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_UNION, attrList);
        attrList = Container.getContainer(8);
        attrList.put(SchemaSymbols.ATT_ATTRIBUTEFORMDEFAULT, allAttrs[ATT_ATTRIBUTE_FD_D]);
        attrList.put(SchemaSymbols.ATT_BLOCKDEFAULT, allAttrs[ATT_BLOCK_D_D]);
        attrList.put(SchemaSymbols.ATT_ELEMENTFORMDEFAULT, allAttrs[ATT_ELEMENT_FD_D]);
        attrList.put(SchemaSymbols.ATT_FINALDEFAULT, allAttrs[ATT_FINAL_D_D]);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_TARGETNAMESPACE, allAttrs[ATT_TARGET_N_N]);
        attrList.put(SchemaSymbols.ATT_VERSION, allAttrs[ATT_VERSION_N]);
        attrList.put(SchemaSymbols.ATT_XML_LANG, allAttrs[ATT_XML_LANG]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_SCHEMA, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_SCHEMALOCATION, allAttrs[ATT_SCHEMA_L_R]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_INCLUDE, attrList);
        fEleAttrsMapG.put(SchemaSymbols.ELT_REDEFINE, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_NAMESPACE, allAttrs[ATT_NAMESPACE_N]);
        attrList.put(SchemaSymbols.ATT_SCHEMALOCATION, allAttrs[ATT_SCHEMA_L_N]);
        fEleAttrsMapG.put(SchemaSymbols.ELT_IMPORT, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_NNI_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_LENGTH, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MINLENGTH, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MAXLENGTH, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_FRACTIONDIGITS, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_PI_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_TOTALDIGITS, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_STR_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_PATTERN, attrList);
        attrList = Container.getContainer(2);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_STR_N]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_ENUMERATION, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_WS_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_WHITESPACE, attrList);
        attrList = Container.getContainer(3);
        attrList.put(SchemaSymbols.ATT_ID, allAttrs[ATT_ID_N]);
        attrList.put(SchemaSymbols.ATT_VALUE, allAttrs[ATT_VALUE_STR_N]);
        attrList.put(SchemaSymbols.ATT_FIXED, allAttrs[ATT_FIXED_D]);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MAXINCLUSIVE, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MAXEXCLUSIVE, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MININCLUSIVE, attrList);
        fEleAttrsMapL.put(SchemaSymbols.ELT_MINEXCLUSIVE, attrList);
    }

    protected XSDHandler fSchemaHandler = null;

    protected SymbolTable fSymbolTable = null;

    protected Map fNonSchemaAttrs = new HashMap();

    protected Vector fNamespaceList = new Vector();

    protected boolean[] fSeen = new boolean[ATTIDX_COUNT];

    private static boolean[] fSeenTemp = new boolean[ATTIDX_COUNT];

    public XSAttributeChecker(XSDHandler schemaHandler) {
        fSchemaHandler = schemaHandler;
    }

    public void reset(SymbolTable symbolTable) {
        fSymbolTable = symbolTable;
        fNonSchemaAttrs.clear();
    }

    public Object[] checkAttributes(Element element, boolean isGlobal, XSDocumentInfo schemaDoc) {
        return checkAttributes(element, isGlobal, schemaDoc, false);
    }

    public Object[] checkAttributes(Element element, boolean isGlobal, XSDocumentInfo schemaDoc, boolean enumAsQName) {
        if (element == null)
            return null;
        Attr[] attrs = DOMUtil.getAttrs(element);
        resolveNamespace(element, attrs, schemaDoc.fNamespaceSupport);
        String uri = DOMUtil.getNamespaceURI(element);
        String elName = DOMUtil.getLocalName(element);
        if (!SchemaSymbols.URI_SCHEMAFORSCHEMA.equals(uri)) {
            reportSchemaError("s4s-elt-schema-ns", new Object[] { elName }, element);
        }
        Map eleAttrsMap = fEleAttrsMapG;
        String lookupName = elName;
        if (!isGlobal) {
            eleAttrsMap = fEleAttrsMapL;
            if (elName.equals(SchemaSymbols.ELT_ELEMENT)) {
                if (DOMUtil.getAttr(element, SchemaSymbols.ATT_REF) != null)
                    lookupName = ELEMENT_R;
                else
                    lookupName = ELEMENT_N;
            } else if (elName.equals(SchemaSymbols.ELT_ATTRIBUTE)) {
                if (DOMUtil.getAttr(element, SchemaSymbols.ATT_REF) != null)
                    lookupName = ATTRIBUTE_R;
                else
                    lookupName = ATTRIBUTE_N;
            }
        }
        Container attrList = (Container) eleAttrsMap.get(lookupName);
        if (attrList == null) {
            reportSchemaError("s4s-elt-invalid", new Object[] { elName }, element);
            return null;
        }
        Object[] attrValues = getAvailableArray();
        long fromDefault = 0;
        System.arraycopy(fSeenTemp, 0, fSeen, 0, ATTIDX_COUNT);
        int length = attrs.length;
        Attr sattr = null;
        for (int i = 0; i < length; i++) {
            sattr = attrs[i];
            String attrName = sattr.getName();
            String attrURI = DOMUtil.getNamespaceURI(sattr);
            String attrVal = DOMUtil.getValue(sattr);
            if (attrName.startsWith("xml")) {
                String attrPrefix = DOMUtil.getPrefix(sattr);
                if ("xmlns".equals(attrPrefix) || "xmlns".equals(attrName)) {
                    continue;
                } else if (SchemaSymbols.ATT_XML_LANG.equals(attrName) && (SchemaSymbols.ELT_SCHEMA.equals(elName) || SchemaSymbols.ELT_DOCUMENTATION.equals(elName))) {
                    attrURI = null;
                }
            }
            if (attrURI != null && attrURI.length() != 0) {
                if (attrURI.equals(SchemaSymbols.URI_SCHEMAFORSCHEMA)) {
                    reportSchemaError("s4s-att-not-allowed", new Object[] { elName, attrName }, element);
                } else {
                    if (attrValues[ATTIDX_NONSCHEMA] == null) {
                        attrValues[ATTIDX_NONSCHEMA] = new Vector(4, 2);
                    }
                    ((Vector) attrValues[ATTIDX_NONSCHEMA]).addElement(attrName);
                    ((Vector) attrValues[ATTIDX_NONSCHEMA]).addElement(attrVal);
                }
                continue;
            }
            OneAttr oneAttr = attrList.get(attrName);
            if (oneAttr == null) {
                reportSchemaError("s4s-att-not-allowed", new Object[] { elName, attrName }, element);
                continue;
            }
            fSeen[oneAttr.valueIndex] = true;
            try {
                if (oneAttr.dvIndex >= 0) {
                    if (oneAttr.dvIndex != DT_STRING && oneAttr.dvIndex != DT_XPATH && oneAttr.dvIndex != DT_XPATH1) {
                        XSSimpleType dv = fExtraDVs[oneAttr.dvIndex];
                        Object avalue = dv.validate(attrVal, schemaDoc.fValidationContext, null);
                        if (oneAttr.dvIndex == DT_QNAME) {
                            QName qname = (QName) avalue;
                            if (qname.prefix == XMLSymbols.EMPTY_STRING && qname.uri == null && schemaDoc.fIsChameleonSchema)
                                qname.uri = schemaDoc.fTargetNamespace;
                        }
                        attrValues[oneAttr.valueIndex] = avalue;
                    } else {
                        attrValues[oneAttr.valueIndex] = attrVal;
                    }
                } else {
                    attrValues[oneAttr.valueIndex] = validate(attrValues, attrName, attrVal, oneAttr.dvIndex, schemaDoc);
                }
            } catch (InvalidDatatypeValueException ide) {
                reportSchemaError("s4s-att-invalid-value", new Object[] { elName, attrName, ide.getMessage() }, element);
                if (oneAttr.dfltValue != null)
                    attrValues[oneAttr.valueIndex] = oneAttr.dfltValue;
            }
            if (elName.equals(SchemaSymbols.ELT_ENUMERATION) && enumAsQName) {
                attrValues[ATTIDX_ENUMNSDECLS] = new SchemaNamespaceSupport(schemaDoc.fNamespaceSupport);
            }
        }
        OneAttr[] reqAttrs = attrList.values;
        for (int i = 0; i < reqAttrs.length; i++) {
            OneAttr oneAttr = reqAttrs[i];
            if (oneAttr.dfltValue != null && !fSeen[oneAttr.valueIndex]) {
                attrValues[oneAttr.valueIndex] = oneAttr.dfltValue;
                fromDefault |= (1 << oneAttr.valueIndex);
            }
        }
        attrValues[ATTIDX_FROMDEFAULT] = new Long(fromDefault);
        if (attrValues[ATTIDX_MAXOCCURS] != null) {
            int min = ((XInt) attrValues[ATTIDX_MINOCCURS]).intValue();
            int max = ((XInt) attrValues[ATTIDX_MAXOCCURS]).intValue();
            if (max != SchemaSymbols.OCCURRENCE_UNBOUNDED) {
                if (fSchemaHandler.fSecureProcessing != null) {
                    String localName = element.getLocalName();
                    final boolean optimize = (localName.equals("element") || localName.equals("any")) && (element.getNextSibling() == null) && (element.getPreviousSibling() == null) && (element.getParentNode().getLocalName().equals("sequence"));
                    if (!optimize) {
                        int maxOccurNodeLimit = fSchemaHandler.fSecureProcessing.getLimit(XMLSecurityManager.Limit.MAX_OCCUR_NODE_LIMIT);
                        if (max > maxOccurNodeLimit && !fSchemaHandler.fSecureProcessing.isNoLimit(maxOccurNodeLimit)) {
                            reportSchemaFatalError("MaxOccurLimit", new Object[] { new Integer(maxOccurNodeLimit) }, element);
                            attrValues[ATTIDX_MAXOCCURS] = fXIntPool.getXInt(maxOccurNodeLimit);
                            max = maxOccurNodeLimit;
                        }
                    }
                }
                if (min > max) {
                    reportSchemaError("p-props-correct.2.1", new Object[] { elName, attrValues[ATTIDX_MINOCCURS], attrValues[ATTIDX_MAXOCCURS] }, element);
                    attrValues[ATTIDX_MINOCCURS] = attrValues[ATTIDX_MAXOCCURS];
                }
            }
        }
        return attrValues;
    }

    private Object validate(Object[] attrValues, String attr, String ivalue, int dvIndex, XSDocumentInfo schemaDoc) throws InvalidDatatypeValueException {
        if (ivalue == null)
            return null;
        String value = XMLChar.trim(ivalue);
        Object retValue = null;
        Vector memberType;
        int choice;
        switch(dvIndex) {
            case DT_BOOLEAN:
                if (value.equals(SchemaSymbols.ATTVAL_FALSE) || value.equals(SchemaSymbols.ATTVAL_FALSE_0)) {
                    retValue = Boolean.FALSE;
                } else if (value.equals(SchemaSymbols.ATTVAL_TRUE) || value.equals(SchemaSymbols.ATTVAL_TRUE_1)) {
                    retValue = Boolean.TRUE;
                } else {
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[] { value, "boolean" });
                }
                break;
            case DT_NONNEGINT:
                try {
                    if (value.length() > 0 && value.charAt(0) == '+')
                        value = value.substring(1);
                    retValue = fXIntPool.getXInt(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[] { value, "nonNegativeInteger" });
                }
                if (((XInt) retValue).intValue() < 0)
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[] { value, "nonNegativeInteger" });
                break;
            case DT_POSINT:
                try {
                    if (value.length() > 0 && value.charAt(0) == '+')
                        value = value.substring(1);
                    retValue = fXIntPool.getXInt(Integer.parseInt(value));
                } catch (NumberFormatException e) {
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[] { value, "positiveInteger" });
                }
                if (((XInt) retValue).intValue() <= 0)
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.1", new Object[] { value, "positiveInteger" });
                break;
            case DT_BLOCK:
                choice = 0;
                if (value.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    choice = XSConstants.DERIVATION_SUBSTITUTION | XSConstants.DERIVATION_EXTENSION | XSConstants.DERIVATION_RESTRICTION;
                } else {
                    StringTokenizer t = new StringTokenizer(value, " \n\t\r");
                    while (t.hasMoreTokens()) {
                        String token = t.nextToken();
                        if (token.equals(SchemaSymbols.ATTVAL_EXTENSION)) {
                            choice |= XSConstants.DERIVATION_EXTENSION;
                        } else if (token.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            choice |= XSConstants.DERIVATION_RESTRICTION;
                        } else if (token.equals(SchemaSymbols.ATTVAL_SUBSTITUTION)) {
                            choice |= XSConstants.DERIVATION_SUBSTITUTION;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[] { value, "(#all | List of (extension | restriction | substitution))" });
                        }
                    }
                }
                retValue = fXIntPool.getXInt(choice);
                break;
            case DT_BLOCK1:
            case DT_FINAL:
                choice = 0;
                if (value.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    choice = XSConstants.DERIVATION_SUBSTITUTION | XSConstants.DERIVATION_EXTENSION | XSConstants.DERIVATION_RESTRICTION | XSConstants.DERIVATION_LIST | XSConstants.DERIVATION_UNION;
                } else {
                    StringTokenizer t = new StringTokenizer(value, " \n\t\r");
                    while (t.hasMoreTokens()) {
                        String token = t.nextToken();
                        if (token.equals(SchemaSymbols.ATTVAL_EXTENSION)) {
                            choice |= XSConstants.DERIVATION_EXTENSION;
                        } else if (token.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            choice |= XSConstants.DERIVATION_RESTRICTION;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[] { value, "(#all | List of (extension | restriction))" });
                        }
                    }
                }
                retValue = fXIntPool.getXInt(choice);
                break;
            case DT_FINAL1:
                choice = 0;
                if (value.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    choice = XSConstants.DERIVATION_SUBSTITUTION | XSConstants.DERIVATION_EXTENSION | XSConstants.DERIVATION_RESTRICTION | XSConstants.DERIVATION_LIST | XSConstants.DERIVATION_UNION;
                } else {
                    StringTokenizer t = new StringTokenizer(value, " \n\t\r");
                    while (t.hasMoreTokens()) {
                        String token = t.nextToken();
                        if (token.equals(SchemaSymbols.ATTVAL_LIST)) {
                            choice |= XSConstants.DERIVATION_LIST;
                        } else if (token.equals(SchemaSymbols.ATTVAL_UNION)) {
                            choice |= XSConstants.DERIVATION_UNION;
                        } else if (token.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            choice |= XSConstants.DERIVATION_RESTRICTION;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[] { value, "(#all | List of (list | union | restriction))" });
                        }
                    }
                }
                retValue = fXIntPool.getXInt(choice);
                break;
            case DT_FINAL2:
                choice = 0;
                if (value.equals(SchemaSymbols.ATTVAL_POUNDALL)) {
                    choice = XSConstants.DERIVATION_SUBSTITUTION | XSConstants.DERIVATION_EXTENSION | XSConstants.DERIVATION_RESTRICTION | XSConstants.DERIVATION_LIST | XSConstants.DERIVATION_UNION;
                } else {
                    StringTokenizer t = new StringTokenizer(value, " \n\t\r");
                    while (t.hasMoreTokens()) {
                        String token = t.nextToken();
                        if (token.equals(SchemaSymbols.ATTVAL_EXTENSION)) {
                            choice |= XSConstants.DERIVATION_EXTENSION;
                        } else if (token.equals(SchemaSymbols.ATTVAL_RESTRICTION)) {
                            choice |= XSConstants.DERIVATION_RESTRICTION;
                        } else if (token.equals(SchemaSymbols.ATTVAL_LIST)) {
                            choice |= XSConstants.DERIVATION_LIST;
                        } else if (token.equals(SchemaSymbols.ATTVAL_UNION)) {
                            choice |= XSConstants.DERIVATION_UNION;
                        } else {
                            throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[] { value, "(#all | List of (extension | restriction | list | union))" });
                        }
                    }
                }
                retValue = fXIntPool.getXInt(choice);
                break;
            case DT_FORM:
                if (value.equals(SchemaSymbols.ATTVAL_QUALIFIED))
                    retValue = INT_QUALIFIED;
                else if (value.equals(SchemaSymbols.ATTVAL_UNQUALIFIED))
                    retValue = INT_UNQUALIFIED;
                else
                    throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[] { value, "(qualified | unqualified)" });
                break;
            case DT_MAXOCCURS:
                if (value.equals(SchemaSymbols.ATTVAL_UNBOUNDED)) {
                    retValue = INT_UNBOUNDED;
                } else {
                    try {
                        retValue = validate(attrValues, attr, value, DT_NONNEGINT, schemaDoc);
                    } catch (NumberFormatException e) {
                        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[] { value, "(nonNegativeInteger | unbounded)" });
                    }
                }
                break;
            case DT_MAXOCCURS1:
                if (value.equals("1"))
                    retValue = fXIntPool.getXInt(1);
                else
                    throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[] { value, "(1)" });
                break;
            case DT_MEMBERTYPES:
                memberType = new Vector();
                try {
                    StringTokenizer t = new StringTokenizer(value, " \n\t\r");
                    while (t.hasMoreTokens()) {
                        String token = t.nextToken();
                        QName qname = (QName) fExtraDVs[DT_QNAME].validate(token, schemaDoc.fValidationContext, null);
                        if (qname.prefix == XMLSymbols.EMPTY_STRING && qname.uri == null && schemaDoc.fIsChameleonSchema)
                            qname.uri = schemaDoc.fTargetNamespace;
                        memberType.addElement(qname);
                    }
                    retValue = memberType;
                } catch (InvalidDatatypeValueException ide) {
                    throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.2", new Object[] { value, "(List of QName)" });
                }
                break;
            case DT_MINOCCURS1:
                if (value.equals("0"))
                    retValue = fXIntPool.getXInt(0);
                else if (value.equals("1"))
                    retValue = fXIntPool.getXInt(1);
                else
                    throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[] { value, "(0 | 1)" });
                break;
            case DT_NAMESPACE:
                if (value.equals(SchemaSymbols.ATTVAL_TWOPOUNDANY)) {
                    retValue = INT_ANY_ANY;
                } else if (value.equals(SchemaSymbols.ATTVAL_TWOPOUNDOTHER)) {
                    retValue = INT_ANY_NOT;
                    String[] list = new String[2];
                    list[0] = schemaDoc.fTargetNamespace;
                    list[1] = null;
                    attrValues[ATTIDX_NAMESPACE_LIST] = list;
                } else {
                    retValue = INT_ANY_LIST;
                    fNamespaceList.removeAllElements();
                    StringTokenizer tokens = new StringTokenizer(value, " \n\t\r");
                    String token;
                    String tempNamespace;
                    try {
                        while (tokens.hasMoreTokens()) {
                            token = tokens.nextToken();
                            if (token.equals(SchemaSymbols.ATTVAL_TWOPOUNDLOCAL)) {
                                tempNamespace = null;
                            } else if (token.equals(SchemaSymbols.ATTVAL_TWOPOUNDTARGETNS)) {
                                tempNamespace = schemaDoc.fTargetNamespace;
                            } else {
                                fExtraDVs[DT_ANYURI].validate(token, schemaDoc.fValidationContext, null);
                                tempNamespace = fSymbolTable.addSymbol(token);
                            }
                            if (!fNamespaceList.contains(tempNamespace)) {
                                fNamespaceList.addElement(tempNamespace);
                            }
                        }
                    } catch (InvalidDatatypeValueException ide) {
                        throw new InvalidDatatypeValueException("cvc-datatype-valid.1.2.3", new Object[] { value, "((##any | ##other) | List of (anyURI | (##targetNamespace | ##local)) )" });
                    }
                    int num = fNamespaceList.size();
                    String[] list = new String[num];
                    fNamespaceList.copyInto(list);
                    attrValues[ATTIDX_NAMESPACE_LIST] = list;
                }
                break;
            case DT_PROCESSCONTENTS:
                if (value.equals(SchemaSymbols.ATTVAL_STRICT))
                    retValue = INT_ANY_STRICT;
                else if (value.equals(SchemaSymbols.ATTVAL_LAX))
                    retValue = INT_ANY_LAX;
                else if (value.equals(SchemaSymbols.ATTVAL_SKIP))
                    retValue = INT_ANY_SKIP;
                else
                    throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[] { value, "(lax | skip | strict)" });
                break;
            case DT_USE:
                if (value.equals(SchemaSymbols.ATTVAL_OPTIONAL))
                    retValue = INT_USE_OPTIONAL;
                else if (value.equals(SchemaSymbols.ATTVAL_REQUIRED))
                    retValue = INT_USE_REQUIRED;
                else if (value.equals(SchemaSymbols.ATTVAL_PROHIBITED))
                    retValue = INT_USE_PROHIBITED;
                else
                    throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[] { value, "(optional | prohibited | required)" });
                break;
            case DT_WHITESPACE:
                if (value.equals(SchemaSymbols.ATTVAL_PRESERVE))
                    retValue = INT_WS_PRESERVE;
                else if (value.equals(SchemaSymbols.ATTVAL_REPLACE))
                    retValue = INT_WS_REPLACE;
                else if (value.equals(SchemaSymbols.ATTVAL_COLLAPSE))
                    retValue = INT_WS_COLLAPSE;
                else
                    throw new InvalidDatatypeValueException("cvc-enumeration-valid", new Object[] { value, "(preserve | replace | collapse)" });
                break;
        }
        return retValue;
    }

    void reportSchemaFatalError(String key, Object[] args, Element ele) {
        fSchemaHandler.reportSchemaFatalError(key, args, ele);
    }

    void reportSchemaError(String key, Object[] args, Element ele) {
        fSchemaHandler.reportSchemaError(key, args, ele);
    }

    public void checkNonSchemaAttributes(XSGrammarBucket grammarBucket) {
        Iterator entries = fNonSchemaAttrs.entrySet().iterator();
        XSAttributeDecl attrDecl;
        while (entries.hasNext()) {
            Map.Entry entry = (Map.Entry) entries.next();
            String attrRName = (String) entry.getKey();
            String attrURI = attrRName.substring(0, attrRName.indexOf(','));
            String attrLocal = attrRName.substring(attrRName.indexOf(',') + 1);
            SchemaGrammar sGrammar = grammarBucket.getGrammar(attrURI);
            if (sGrammar == null) {
                continue;
            }
            attrDecl = sGrammar.getGlobalAttributeDecl(attrLocal);
            if (attrDecl == null) {
                continue;
            }
            XSSimpleType dv = (XSSimpleType) attrDecl.getTypeDefinition();
            if (dv == null) {
                continue;
            }
            Vector values = (Vector) entry.getValue();
            String elName;
            String attrName = (String) values.elementAt(0);
            int count = values.size();
            for (int i = 1; i < count; i += 2) {
                elName = (String) values.elementAt(i);
                try {
                    dv.validate((String) values.elementAt(i + 1), null, null);
                } catch (InvalidDatatypeValueException ide) {
                    reportSchemaError("s4s-att-invalid-value", new Object[] { elName, attrName, ide.getMessage() }, null);
                }
            }
        }
    }

    public static String normalize(String content, short ws) {
        int len = content == null ? 0 : content.length();
        if (len == 0 || ws == XSSimpleType.WS_PRESERVE)
            return content;
        StringBuffer sb = new StringBuffer();
        if (ws == XSSimpleType.WS_REPLACE) {
            char ch;
            for (int i = 0; i < len; i++) {
                ch = content.charAt(i);
                if (ch != 0x9 && ch != 0xa && ch != 0xd)
                    sb.append(ch);
                else
                    sb.append((char) 0x20);
            }
        } else {
            char ch;
            int i;
            boolean isLeading = true;
            for (i = 0; i < len; i++) {
                ch = content.charAt(i);
                if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20) {
                    sb.append(ch);
                    isLeading = false;
                } else {
                    for (; i < len - 1; i++) {
                        ch = content.charAt(i + 1);
                        if (ch != 0x9 && ch != 0xa && ch != 0xd && ch != 0x20)
                            break;
                    }
                    if (i < len - 1 && !isLeading)
                        sb.append((char) 0x20);
                }
            }
        }
        return sb.toString();
    }

    static final int INIT_POOL_SIZE = 10;

    static final int INC_POOL_SIZE = 10;

    Object[][] fArrayPool = new Object[INIT_POOL_SIZE][ATTIDX_COUNT];

    private static Object[] fTempArray = new Object[ATTIDX_COUNT];

    int fPoolPos = 0;

    protected Object[] getAvailableArray() {
        if (fArrayPool.length == fPoolPos) {
            fArrayPool = new Object[fPoolPos + INC_POOL_SIZE][];
            for (int i = fPoolPos; i < fArrayPool.length; i++) fArrayPool[i] = new Object[ATTIDX_COUNT];
        }
        Object[] retArray = fArrayPool[fPoolPos];
        fArrayPool[fPoolPos++] = null;
        System.arraycopy(fTempArray, 0, retArray, 0, ATTIDX_COUNT - 1);
        retArray[ATTIDX_ISRETURNED] = Boolean.FALSE;
        return retArray;
    }

    public void returnAttrArray(Object[] attrArray, XSDocumentInfo schemaDoc) {
        if (schemaDoc != null)
            schemaDoc.fNamespaceSupport.popContext();
        if (fPoolPos == 0 || attrArray == null || attrArray.length != ATTIDX_COUNT || ((Boolean) attrArray[ATTIDX_ISRETURNED]).booleanValue()) {
            return;
        }
        attrArray[ATTIDX_ISRETURNED] = Boolean.TRUE;
        if (attrArray[ATTIDX_NONSCHEMA] != null)
            ((Vector) attrArray[ATTIDX_NONSCHEMA]).clear();
        fArrayPool[--fPoolPos] = attrArray;
    }

    public void resolveNamespace(Element element, Attr[] attrs, SchemaNamespaceSupport nsSupport) {
        nsSupport.pushContext();
        int length = attrs.length;
        Attr sattr = null;
        String rawname, prefix, uri;
        for (int i = 0; i < length; i++) {
            sattr = attrs[i];
            rawname = DOMUtil.getName(sattr);
            prefix = null;
            if (rawname.equals(XMLSymbols.PREFIX_XMLNS))
                prefix = XMLSymbols.EMPTY_STRING;
            else if (rawname.startsWith("xmlns:"))
                prefix = fSymbolTable.addSymbol(DOMUtil.getLocalName(sattr));
            if (prefix != null) {
                uri = fSymbolTable.addSymbol(DOMUtil.getValue(sattr));
                nsSupport.declarePrefix(prefix, uri.length() != 0 ? uri : null);
            }
        }
    }
}

class OneAttr {

    public String name;

    public int dvIndex;

    public int valueIndex;

    public Object dfltValue;

    public OneAttr(String name, int dvIndex, int valueIndex, Object dfltValue) {
        this.name = name;
        this.dvIndex = dvIndex;
        this.valueIndex = valueIndex;
        this.dfltValue = dfltValue;
    }
}

abstract class Container {

    static final int THRESHOLD = 5;

    static Container getContainer(int size) {
        if (size > THRESHOLD)
            return new LargeContainer(size);
        else
            return new SmallContainer(size);
    }

    abstract void put(String key, OneAttr value);

    abstract OneAttr get(String key);

    OneAttr[] values;

    int pos = 0;
}

class SmallContainer extends Container {

    String[] keys;

    SmallContainer(int size) {
        keys = new String[size];
        values = new OneAttr[size];
    }

    void put(String key, OneAttr value) {
        keys[pos] = key;
        values[pos++] = value;
    }

    OneAttr get(String key) {
        for (int i = 0; i < pos; i++) {
            if (keys[i].equals(key)) {
                return values[i];
            }
        }
        return null;
    }
}

class LargeContainer extends Container {

    Map items;

    LargeContainer(int size) {
        items = new HashMap(size * 2 + 1);
        values = new OneAttr[size];
    }

    void put(String key, OneAttr value) {
        items.put(key, value);
        values[pos++] = value;
    }

    OneAttr get(String key) {
        OneAttr ret = (OneAttr) items.get(key);
        return ret;
    }
}
