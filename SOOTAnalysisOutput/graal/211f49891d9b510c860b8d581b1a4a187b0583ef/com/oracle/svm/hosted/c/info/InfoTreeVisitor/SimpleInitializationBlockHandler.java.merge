package com.oracle.svm.hosted.c.info;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import com.oracle.svm.core.util.UserError;

public abstract class InfoTreeVisitor {

    static final Comparator<ElementInfo> elementInfoComparator;

    static {
        Comparator<ElementInfo> classNameComparator = Comparator.comparing(e -> e.getClass().getName());
        Comparator<ElementInfo> nameComparator = classNameComparator.thenComparing(e -> e.getName());
        elementInfoComparator = nameComparator.thenComparing(e -> e.getAnnotatedElement().toString());
    }

    protected final void processChildren(ElementInfo info) {
        List<ElementInfo> children = info.getChildren();
        children.sort(elementInfoComparator);
        for (ElementInfo child : children) {
            try {
                child.accept(this);
            } catch (NumberFormatException e) {
                throw UserError.abort("Missing CAP cache value for: %s", child.getUniqueID());
            }
        }
    }

    protected void visitNativeCodeInfo(NativeCodeInfo info) {
        processChildren(info);
    }

    protected void visitStructInfo(StructInfo info) {
        processChildren(info);
    }

    protected void visitRawStructureInfo(RawStructureInfo info) {
        processChildren(info);
    }

    protected void visitStructFieldInfo(StructFieldInfo info) {
        processChildren(info);
    }

    protected void visitStructBitfieldInfo(StructBitfieldInfo info) {
        processChildren(info);
    }

    protected void visitConstantInfo(ConstantInfo info) {
        processChildren(info);
    }

    protected void visitPointerToInfo(PointerToInfo info) {
        processChildren(info);
    }

    protected void visitAccessorInfo(AccessorInfo info) {
        processChildren(info);
    }

    protected void visitElementPropertyInfo(PropertyInfo<?> info) {
        processChildren(info);
    }

    protected void visitEnumInfo(EnumInfo info) {
        processChildren(info);
    }

    protected void visitEnumConstantInfo(EnumConstantInfo info) {
        processChildren(info);
    }

    protected void visitEnumValueInfo(EnumValueInfo info) {
        processChildren(info);
    }

    protected void visitEnumLookupInfo(EnumLookupInfo info) {
        processChildren(info);
    }
}