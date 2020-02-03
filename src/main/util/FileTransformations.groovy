package main.util

@Grab(group='org.eclipse.jdt', module='org.eclipse.jdt.core', version='3.12.2')
@Grab(group='commons-io', module='commons-io', version='2.4')
import main.project.*

import java.io.*;
import java.util.*;

import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.ASTRewrite;
import org.eclipse.text.edits.TextEdit;

import static org.eclipse.jdt.core.dom.Modifier.*;

final class FileTransformations {

    private static boolean hasEmptyConstructor = false;
    private static String className = "";
    private static AnonymousClassDeclaration classNode = null;

    public static void parse(String str, final ASTParser parser, final CompilationUnit cu, final String modifiedMethod) {
        hasEmptyConstructor = false
        try{ 
            cu.accept(new ASTVisitor() {
                Set names = new HashSet();

                public boolean visit(FieldDeclaration node) {
                    removeModifiersFields(node);
                    return true;
                }

                public boolean visit(MethodDeclaration node) {
                    if(node.isConstructor()){
                        if(node.parameters().isEmpty()){
                            hasEmptyConstructor = true;
                        }
                        className = node.getName().toString();
                    }
                    if (node.getName().toString().equals(modifiedMethod)){
                        removeModifiersMethods(node, cu);
                    }
                    return true;
                }

            });
        }catch(Exception e1){
            print(e1)
        }
    }

    public static String readFileToString(String filePath) throws IOException {
        StringBuilder fileData = new StringBuilder(1000);
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        char[] buf = new char[10];
        int numRead = 0;
        while ((numRead = reader.read(buf)) != -1) {
            String readData = String.valueOf(buf, 0, numRead);
            fileData.append(readData);
            buf = new char[1024];
        }
        reader.close();
        return fileData.toString();
    }

    public static void ParseFilesInDir() throws IOException {
        File dirs = new File(".");
        String dirPath = dirs.getCanonicalPath() + File.separator+"src"+File.separator;
        File root = new File(dirPath);
        File[] files = root.listFiles ( );
        String filePath = null;
        for (File f : files ) {
            filePath = f.getAbsolutePath();
            if(f.isFile()){
                //parse(readFileToString(filePath));
            }
        }
    }

    private static void addEmptyConstructor(String str, ASTParser parser, CompilationUnit cu) {
        AST ast = cu.getAST();

        MethodDeclaration newConstructor = ast.newMethodDeclaration();

        newConstructor.setName(ast.newSimpleName(className));
        newConstructor.setConstructor(true);
        newConstructor.setBody(ast.newBlock());
        ModifierKeyword amp = ModifierKeyword.PUBLIC_KEYWORD;
        newConstructor.modifiers().add(ast.newModifier(amp));
        TypeDeclaration typeDeclaration = ( TypeDeclaration )cu.types().get( 0 );
        typeDeclaration.bodyDeclarations().add(newConstructor);

    }

    private static void removeModifiersFields(FieldDeclaration node) {
        List<Modifier> modifiersToRemove = new ArrayList<Modifier>();
        int i = 0;

        while(i < node.modifiers().size()) {
            if (node.modifiers().get(i) instanceof Modifier) {
                Modifier mod = (Modifier) node.modifiers().get(i);
                if(mod.isFinal()|| mod.isProtected() ){
                    modifiersToRemove.add(mod);
                }
            }
            i++;
        }

        for(Modifier mod : modifiersToRemove){
            node.modifiers().remove(mod);
        }
    }

    private static void removeModifiersMethods(MethodDeclaration node, CompilationUnit cu) {
        AST ast = cu.getAST();
        List<Modifier> modifiersToRemove = new ArrayList<Modifier>();

        int i = 0;
        while(i < node.modifiers().size()){
            if (node.modifiers().get(i) instanceof Modifier){
                Modifier mod = (Modifier) node.modifiers().get(i);
                if(mod.isFinal()|| mod.isProtected() ){
                    modifiersToRemove.add(mod);
                }else if (mod.isPrivate()){
                    mod.setKeyword(ModifierKeyword.PUBLIC_KEYWORD);
                }
            }
            i++;
        }

        for(Modifier mod : modifiersToRemove){
            node.modifiers().remove(mod);
        }
        if(node.modifiers().size() > 0) {
            if (node.modifiers().get(0) instanceof Modifier) {
                Modifier a = (Modifier) node.modifiers().get(0);
                if (!a.isPublic()) {
                    node.modifiers().add(0, ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
                }
            }
        }else{
            node.modifiers().add(0, ast.newModifier(ModifierKeyword.PUBLIC_KEYWORD));
        }
    }

    public static void saveChanges(File file, CompilationUnit cu) throws FileNotFoundException {
        FileWriter fooWriter = new FileWriter(file, false);
        fooWriter.write(cu.toString());
        fooWriter.close();
    }

    public static final void runTransformation(String fileName, String modifiedMethod) throws IOException {
        File file = new File(fileName);
        final String str = FileUtils.readFileToString(file);
        org.eclipse.jface.text.Document document = new org.eclipse.jface.text.Document(str);

        ASTParser parser = ASTParser.newParser(AST.JLS8);
        Map options = JavaCore.getOptions();
        JavaCore.setComplianceOptions(JavaCore.VERSION_1_8, options);
        parser.setCompilerOptions(options);

        parser.setSource(document.get().toCharArray());
        parser.setKind(ASTParser.K_COMPILATION_UNIT);

        final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
        List types = cu.types();
        TypeDeclaration typeDec = (TypeDeclaration) types.get(0);
        className = typeDec.getName().toString();

        parse(str, parser, cu, modifiedMethod);
        if(!hasEmptyConstructor) {
            addEmptyConstructor(str,parser,cu);
            this.hasEmptyConstructor = true
        }
        saveChanges(file, cu)
        
    }

}