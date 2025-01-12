package finalCampaign.annotation.util;

import java.io.*;
import javax.annotation.processing.*;
import javax.lang.model.element.*;
import javax.tools.Diagnostic.*;

public class codeWriter {
    private static Messager messager;

    public static void setMessager(Messager messager) {
        codeWriter.messager = messager;
    }

    public static enum modifier {
        m_public,
        m_private,
        m_protected,
        m_public_static,
        m_private_static,
        m_protected_static
        ;

        public String build() {
            return name().substring(2).replace("_", " ");
        }
    }

    private Writer writer;
    private Element element;
    private int tabs;
    private boolean newMethodHeadBegan;
    private boolean newAnnotationWithArgBegan;
    private boolean newAnnotationWritten;

    public codeWriter(Writer writer, Element e) {
        this.writer = writer;
        element = e;
        tabs = 0;
        newMethodHeadBegan = false;
        newAnnotationWithArgBegan = false;
        newAnnotationWritten = false;
    }

    private void printError(Exception e) {
        if (element == null)
            messager.printMessage(Kind.ERROR, "Failed to write to source: " + e.getLocalizedMessage());
        else
            messager.printMessage(Kind.ERROR, "Failed to write to source: " + e.getLocalizedMessage(), element);
    }

    private void writeString(String fmt, Object ...args) {
        try {
            writer.write(String.format(fmt, args));
        } catch(Exception e) {
            printError(e);
        }
        newAnnotationWritten = false;
    }

    private void writeLine(String fmt, Object ...args) {
        writeString("    ".repeat(tabs) + fmt + "\n", args);
    }

    public void close() {
        try {
            writer.close();
        } catch(Exception e) {
            printError(e);
        }
    }

    public void row() {
        writeString("\n");
    }

    public void packageName(String name) {
        writeString("package %s;\n\n", name);
    }

    public void importPackage(String name) {
        writeString("import %s;\n", name);
    }

    public void beginBlock(String fmt, Object ...args) {
        writeLine(fmt + " {", args);
        tabs ++;
    }

    public void endBlock() {
        tabs --;
        writeLine("}");
    }

    public void annotation(String name) {
        if (!newAnnotationWritten)
            row();
        writeLine("@%s", name);
        newAnnotationWritten = true;
    }

    public void annotation(String name, String singleArg) {
        if (!newAnnotationWritten)
            row();
        writeLine("@%s(%s)", name, singleArg);
        newAnnotationWritten = true;
    }

    public void beginAnnotationWithArg(String name) {
        writeString("    ".repeat(tabs) + "%s@%s(", newAnnotationWritten ? "" : "\n", name);
        newAnnotationWithArgBegan = true;
    }

    public void annotationArg(String name, String val) {
        writeString("%s%s = %s", newAnnotationWithArgBegan ? "" : ", ", name, val);
        newAnnotationWithArgBegan = false;
    }

    public void endAnnotationWithArg() {
        writeString(")\n");
        newAnnotationWritten = true;
    }

    public void beginClass(modifier mod, String name) {
        if (!newAnnotationWritten)
            row();
        beginBlock("%s class %s", mod.build(), name);
    }

    public void beginClass(modifier mod, String name, String superClass) {
        if (!newAnnotationWritten)
            row();
        beginBlock("%s class %s extends %s", mod.build(), name, superClass);
    }

    public void endClass() {
        endBlock();
        row();
    }

    public void beginMethodHead(modifier mod, String name, String returnType) {
        if (!newAnnotationWritten)
            row();
        writeString("    ".repeat(tabs) + "%s %s %s(", mod.build(), returnType, name);
        newMethodHeadBegan = true;
    }

    public void methodArg(String type, String name) {
        writeString("%s%s %s", newMethodHeadBegan ? "" : ", ", type, name);
        newMethodHeadBegan = false;
    }

    public void endMethodHead() {
        writeString(") {\n");
        tabs ++;
    }

    public void endMethod() {
        endBlock();
    }

    public void classVariable(modifier mod, String type, String name) {
        writeLine("%s %s %s;", mod.build(), type, name);
    }

    public void statement(String fmt, Object ...args) {
        writeLine(fmt + ";", args);
    }

    public void comment(String fmt, Object ...args) {
        writeLine("// " + fmt, args);
    }
}
