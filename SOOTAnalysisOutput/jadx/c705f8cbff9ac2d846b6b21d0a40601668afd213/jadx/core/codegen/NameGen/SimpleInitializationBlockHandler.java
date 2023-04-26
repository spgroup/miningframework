package jadx.core.codegen;

import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import jadx.core.Consts;
import jadx.core.deobf.NameMapper;
import jadx.core.dex.attributes.nodes.LoopLabelAttr;
import jadx.core.dex.info.ClassInfo;
import jadx.core.dex.info.MethodInfo;
import jadx.core.dex.instructions.InvokeNode;
import jadx.core.dex.instructions.args.ArgType;
import jadx.core.dex.instructions.args.InsnArg;
import jadx.core.dex.instructions.args.InsnWrapArg;
import jadx.core.dex.instructions.args.NamedArg;
import jadx.core.dex.instructions.args.RegisterArg;
import jadx.core.dex.instructions.args.SSAVar;
import jadx.core.dex.instructions.mods.ConstructorInsn;
import jadx.core.dex.nodes.InsnNode;
import jadx.core.dex.nodes.MethodNode;
import jadx.core.utils.StringUtils;
import jadx.core.utils.Utils;

public class NameGen {

    private static final Map<String, String> OBJ_ALIAS;

    private final Set<String> varNames = new LinkedHashSet<>();

    private final MethodNode mth;

    private final boolean fallback;

    static {
<<<<<<< MINE
        OBJ_ALIAS = new HashMap<>();
        OBJ_ALIAS.put(Consts.CLASS_STRING, "str");
        OBJ_ALIAS.put(Consts.CLASS_CLASS, "cls");
        OBJ_ALIAS.put(Consts.CLASS_THROWABLE, "th");
        OBJ_ALIAS.put(Consts.CLASS_OBJECT, "obj");
        OBJ_ALIAS.put("java.util.Iterator", "it");
        OBJ_ALIAS.put("java.lang.Boolean", "bool");
        OBJ_ALIAS.put("java.lang.Short", "sh");
        OBJ_ALIAS.put("java.lang.Integer", "num");
        OBJ_ALIAS.put("java.lang.Character", "ch");
        OBJ_ALIAS.put("java.lang.Byte", "b");
        OBJ_ALIAS.put("java.lang.Float", "f");
        OBJ_ALIAS.put("java.lang.Long", "l");
        OBJ_ALIAS.put("java.lang.Double", "d");
        OBJ_ALIAS.put("java.lang.StringBuilder", "sb");
=======
        OBJ_ALIAS = Utils.newConstStringMap(Consts.CLASS_STRING, "str", Consts.CLASS_CLASS, "cls", Consts.CLASS_THROWABLE, "th", Consts.CLASS_OBJECT, "obj", "java.util.Iterator", "it", "java.lang.Boolean", "bool", "java.lang.Short", "sh", "java.lang.Integer", "num", "java.lang.Character", "ch", "java.lang.Byte", "b", "java.lang.Float", "f", "java.lang.Long", "l", "java.lang.Double", "d");
>>>>>>> YOURS
    }

    public NameGen(MethodNode mth, boolean fallback) {
        this.mth = mth;
        this.fallback = fallback;
    }

    public String assignArg(RegisterArg arg) {
        String name = makeArgName(arg);
        if (fallback) {
            return name;
        }
        name = getUniqueVarName(name);
        arg.setName(name);
        return name;
    }

    public String assignNamedArg(NamedArg arg) {
        String name = arg.getName();
        if (fallback) {
            return name;
        }
        name = getUniqueVarName(name);
        arg.setName(name);
        return name;
    }

    public String useArg(RegisterArg arg) {
        String name = arg.getName();
        if (name == null || fallback) {
            return getFallbackName(arg);
        }
        return name;
    }

    public String getLoopLabel(LoopLabelAttr attr) {
        String name = "loop" + attr.getLoop().getId();
        varNames.add(name);
        return name;
    }

    private String getUniqueVarName(String name) {
        String r = name;
        int i = 2;
        while (varNames.contains(r)) {
            r = name + i;
            i++;
        }
        varNames.add(r);
        return r;
    }

    private String makeArgName(RegisterArg arg) {
        if (fallback) {
            return getFallbackName(arg);
        }
        if (arg.isThis()) {
            return RegisterArg.THIS_ARG_NAME;
        }
        String name = arg.getName();
        String varName = name != null ? name : guessName(arg);
        if (NameMapper.isReserved(varName)) {
            varName = varName + "R";
        }
        if (!NameMapper.isValidIdentifier(varName)) {
            varName = getFallbackName(arg);
        }
        return varName;
    }

    private String getFallbackName(RegisterArg arg) {
        StringBuilder sb = new StringBuilder();
        sb.append('r').append(arg.getRegNum());
        SSAVar sVar = arg.getSVar();
        if (sVar != null) {
            sb.append('v').append(sVar.getVersion());
        }
        return sb.toString();
    }

    private String guessName(RegisterArg arg) {
        SSAVar sVar = arg.getSVar();
        if (sVar != null && sVar.getName() == null) {
            RegisterArg assignArg = sVar.getAssign();
            InsnNode assignInsn = assignArg.getParentInsn();
            if (assignInsn != null) {
                String name = makeNameFromInsn(assignInsn);
                if (name != null && !NameMapper.isReserved(name)) {
                    assignArg.setName(name);
                    return name;
                }
            }
        }
        ArgType type = arg.getType();
        if (!type.isTypeKnown() && arg.getInitType().isTypeKnown()) {
            type = arg.getInitType();
        }
        return makeNameForType(type);
    }

    private String makeNameForType(ArgType type) {
        if (type.isPrimitive()) {
            return makeNameForPrimitive(type);
        }
        if (type.isArray()) {
            return makeNameForType(type.getArrayRootElement()) + "Arr";
        }
        return makeNameForObject(type);
    }

    private static String makeNameForPrimitive(ArgType type) {
        return type.getPrimitiveType().getShortName().toLowerCase();
    }

    private String makeNameForObject(ArgType type) {
        if (type.isObject()) {
            String alias = getAliasForObject(type.getObject());
            if (alias != null) {
                return alias;
            }
            ClassInfo extClsInfo = ClassInfo.extCls(mth.root(), type);
            String shortName = extClsInfo.getShortName();
            String vName = fromName(shortName);
            if (vName != null) {
                return vName;
            }
            if (shortName != null) {
                return StringUtils.escape(shortName.toLowerCase());
            }
        }
        return StringUtils.escape(type.toString());
    }

    private static String fromName(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        if (name.toUpperCase().equals(name)) {
            return name.toLowerCase();
        }
        String v1 = Character.toLowerCase(name.charAt(0)) + name.substring(1);
        if (!v1.equals(name)) {
            return v1;
        }
        if (name.length() < 3) {
            return name + "Var";
        }
        return null;
    }

    private static String getAliasForObject(String name) {
        return OBJ_ALIAS.get(name);
    }

    private String makeNameFromInsn(InsnNode insn) {
        switch(insn.getType()) {
            case INVOKE:
                InvokeNode inv = (InvokeNode) insn;
                return makeNameFromInvoke(inv.getCallMth());
            case CONSTRUCTOR:
                ConstructorInsn co = (ConstructorInsn) insn;
                return makeNameForObject(co.getClassType().getType());
            case ARRAY_LENGTH:
                return "length";
            case ARITH:
            case TERNARY:
            case CAST:
                for (InsnArg arg : insn.getArguments()) {
                    if (arg.isInsnWrap()) {
                        InsnNode wrapInsn = ((InsnWrapArg) arg).getWrapInsn();
                        String wName = makeNameFromInsn(wrapInsn);
                        if (wName != null) {
                            return wName;
                        }
                    }
                }
                break;
            default:
                break;
        }
        return null;
    }

    private String makeNameFromInvoke(MethodInfo callMth) {
        String name = callMth.getName();
        if (name.startsWith("get") || name.startsWith("set")) {
            return fromName(name.substring(3));
        }
        ArgType declType = callMth.getDeclClass().getAlias().getType();
        if ("iterator".equals(name)) {
            return "it";
        }
        if ("toString".equals(name)) {
            return makeNameForType(declType);
        }
        if ("forName".equals(name) && declType.equals(ArgType.CLASS)) {
            return OBJ_ALIAS.get(Consts.CLASS_CLASS);
        }
        if (name.startsWith("to")) {
            return fromName(name.substring(2));
        }
        return name;
    }
}