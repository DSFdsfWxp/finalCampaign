package finalCampaign.annotation;

import java.util.*;
import javax.annotation.processing.*;
import javax.lang.model.*;
import javax.lang.model.element.*;
import javax.lang.model.type.*;
import javax.tools.*;
import javax.tools.Diagnostic.*;
import finalCampaign.annotation.util.*;
import finalCampaign.annotation.util.annotationsUtil.*;
import finalCampaign.annotation.util.codeWriter.*;

@SupportedAnnotationTypes({"finalCampaign.annotation.net.netCall"})
@SupportedSourceVersion(SourceVersion.RELEASE_17)
public class netProcessor extends baseProcessor {

    private JavaFileObject fcCallObj;
    private codeWriter fcCallWriter;
    private ArrayList<String> packets;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        codeWriter.setMessager(messager);

        try {
            fcCallObj = filer.createSourceFile("finalCampaign.net.fcCall");
            fcCallWriter = new codeWriter(fcCallObj.openWriter(), null);

            fcCallWriter.packageName("finalCampaign.net");
            fcCallWriter.importPackage("arc.*");
            fcCallWriter.importPackage("mindustry.*");
            fcCallWriter.importPackage("finalCampaign.net.packet.*");

            fcCallWriter.row();
            fcCallWriter.comment("Automatic generated.");

            fcCallWriter.annotation("SuppressWarnings", "\"all\"");
            fcCallWriter.beginClass(modifier.m_public, "fcCall");

            packets = new ArrayList<>();
        } catch (Exception e) {
            messager.printMessage(Kind.ERROR, "Failed to create source file: " + e.getLocalizedMessage());
        }
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (fcCallObj == null)
            return true;

        for (TypeElement annotation : annotations) {
            Set<? extends Element> annotationElems = roundEnv.getElementsAnnotatedWith(annotation);

            for (Element e : annotationElems) {
                if (e.getKind() != ElementKind.METHOD)
                    continue;
                
                ExecutableElement ee = (ExecutableElement) e;
                String name = ee.getSimpleName().toString();
                JavaFileObject packetObj = null;
                codeWriter packetWriter = null;
                boolean errHappened = false;

                try {
                    packetObj = filer.createClassFile("finalCampaign.net.packet." + name + "Packet", e);
                    packetWriter = new codeWriter(packetObj.openWriter(), e);
                } catch(Exception err) {
                    messager.printMessage(Kind.ERROR, "Failed to create source file: " + err.getLocalizedMessage());
                    continue;
                }

                packetWriter.packageName("finalCampaign.net.packet");
                packetWriter.importPackage("arc.util.io.*");
                packetWriter.importPackage("mindustry.*");
                packetWriter.importPackage("mindustry.net.*");
                packetWriter.importPackage("mindustry.io.*");
                packetWriter.importPackage("mindustry.type.*");
                packetWriter.importPackage("finalCampaign.annotation.net.*");
                packetWriter.importPackage("finalCampaign.net.*");
                packetWriter.importPackage("finalCampaign.net.fcNet.*");
                packetWriter.importPackage("finalCampaign.util.*");

                packetWriter.annotation("SuppressWarnings", "\"all\"");

                annotationsUtil methodAnnotations = new annotationsUtil(ee.getAnnotationMirrors());
                annotationUtil netCall = methodAnnotations.getAnnotation("finalCampaign.annotation.net.netCall");

                if (netCall == null) {
                    messager.printMessage(Kind.ERROR, "A method without netCall annotation: " + name, e);
                    continue;
                }

                packetWriter.beginAnnotationWithArg("netCall");
                Enum<?> packetSrc = netCall.getValue("src");
                packetWriter.annotationArg("src", "packetSource." + packetSrc.name());
                packetWriter.endAnnotationWithArg();

                packetWriter.beginClass(modifier.m_public, name + "Packet");

                List<? extends VariableElement> paras = ee.getParameters();
                for (VariableElement ve : paras)
                    packetWriter.classVariable(modifier.m_public, ve.asType().toString(), ve.getSimpleName().toString());

                packetWriter.row();

                packetWriter.annotation("Override");
                packetWriter.beginMethodHead(modifier.m_public, "read", "void");
                packetWriter.methodArg("Reads", "reads");
                packetWriter.endMethodHead();
                packetWriter.statement("super.read(reads)");

                for (VariableElement ve : paras) {
                    String veName = ve.getSimpleName().toString();
                    boolean done = false;

                    TypeElement readsType = elements.getTypeElement("arc.util.io.Reads");
                    for (Element rte : readsType.getEnclosedElements()) {
                        if (rte instanceof ExecutableElement rtee) {
                            String rteeName = rtee.getSimpleName().toString();
                            if (rteeName.equals("checkEOF") || rteeName.startsWith("u"))
                                continue;
                            if (ve.asType().getKind().equals(TypeKind.ARRAY))
                                continue;
                            if (rtee.getParameters().size() > 0)
                                continue;
                            if (!rtee.getReturnType().equals(ve.asType()))
                                continue;
                            
                            packetWriter.statement("this.%s = reads.%s()", veName, rteeName);
                            done = true;
                            break;
                        }
                    }

                    if (done)
                        continue;

                    String[] readerLst = {"mindustry.io.TypeIO", "finalCampaign.util.typeIO"};
                    for (String reader : readerLst) {
                        TypeElement te = elements.getTypeElement(reader);
                        for (Element re : te.getEnclosedElements()) {
                            if (re instanceof ExecutableElement ree) {
                                String reeName = ree.getSimpleName().toString();
                                if (!reeName.startsWith("read") || ree.getParameters().size() != 1)
                                    continue;
                                if (!ree.getReturnType().equals(ve.asType()))
                                    continue;
                                
                                packetWriter.statement("this.%s = %s.%s(reads)", veName, reader, reeName);
                                done = true;
                                break;
                            }
                        }

                        if (done)
                            break;
                    }

                    if (!done) {
                        messager.printMessage(Kind.ERROR, "Could not find a method to read such a type: " + veName, e);
                        errHappened = true;
                        break;
                    }
                }

                packetWriter.endMethod();

                packetWriter.annotation("Override");
                packetWriter.beginMethodHead(modifier.m_public, "write", "void");
                packetWriter.methodArg("Writes", "writes");
                packetWriter.endMethodHead();
                packetWriter.statement("super.write(write)");

                for (VariableElement ve : paras) {
                    String veName = ve.getSimpleName().toString();
                    boolean done = false;

                    TypeElement writesType = elements.getTypeElement("arc.util.io.Writes");
                    for (Element wte : writesType.getEnclosedElements()) {
                        if (wte instanceof ExecutableElement wtee) {
                            String wteeName = wtee.getSimpleName().toString();
                            if (wteeName.equals("checkEOF") || wteeName.startsWith("u"))
                                continue;
                            if (ve.asType().getKind().equals(TypeKind.ARRAY))
                                continue;
                            if (wtee.getParameters().size() > 1)
                                continue;
                            if (!wtee.getParameters().get(0).asType().equals(ve.asType()))
                                continue;
                            
                            packetWriter.statement("writes.%s(this.%s)", wteeName, veName);
                            done = true;
                            break;
                        }
                    }

                    if (done)
                        continue;

                    String[] writerLst = {"mindustry.io.TypeIO", "finalCampaign.util.typeIO"};
                    for (String writer : writerLst) {
                        TypeElement te = elements.getTypeElement(writer);
                        for (Element we : te.getEnclosedElements()) {
                            if (we instanceof ExecutableElement wee) {
                                String weeName = wee.getSimpleName().toString();
                                if (!weeName.startsWith("write") || wee.getParameters().size() != 2)
                                    continue;
                                if (!wee.getParameters().get(0).asType().equals(te.asType()) ||
                                    !wee.getParameters().get(1).asType().equals(ve.asType()))
                                    continue;
                                
                                packetWriter.statement("%s.%s(writes, this.%s)", veName, writer, weeName);
                                done = true;
                                break;
                            }
                        }

                        if (done)
                            break;
                    }

                    if (!done) {
                        messager.printMessage(Kind.ERROR, "Could not find a method to write such a type: " + veName, e);
                        errHappened = true;
                        break;
                    }
                }
                
                packetWriter.endMethod();

                if (packetSrc.name().equals("both") ||
                    packetSrc.name().equals("server")) {
                    packetWriter.annotation("Override");
                    packetWriter.beginMethodHead(modifier.m_public, "handleClient", "void");
                    generateChecks("this.__caller", ee, methodAnnotations, packetWriter);
                    packetWriter.row();

                    ArrayList<String> parasToPass = new ArrayList<>();
                    for (VariableElement ve : paras)
                        parasToPass.add("this." + ve.getSimpleName().toString());
                    packetWriter.statement("fcAction.%s(this.__caller, %s)", name, String.join(", ", parasToPass));

                    packetWriter.endMethod();
                }

                if (packetSrc.name().equals("both") ||
                    packetSrc.name().equals("client")) {
                    packetWriter.annotation("Override");
                    packetWriter.beginMethodHead(modifier.m_public, "handleServer", "void");
                    generateChecks("player", ee, methodAnnotations, packetWriter);
                    packetWriter.row();
                    packetWriter.statement("super.handleServer(player)");

                    ArrayList<String> parasToPass = new ArrayList<>();
                    for (VariableElement ve : paras)
                        parasToPass.add("this." + ve.getSimpleName().toString());
                    packetWriter.beginBlock("if (fcAction.%s(player, %s))", name, String.join(", ", parasToPass));
                    packetWriter.statement("fcNet.send(this)");

                    packetWriter.endMethod();
                }

                packetWriter.endClass();
                packetWriter.close();

                if (errHappened)
                    packetObj.delete();
                else
                    packets.add(name + "Packet");
            }
        }

        return true;
    }

    private void generateChecks(String playerParaName, ExecutableElement method, annotationsUtil annotations, codeWriter writer) {

    }
    
}
