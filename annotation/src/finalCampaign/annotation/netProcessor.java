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

                if (!ee.getModifiers().contains(Modifier.PUBLIC) ||
                    !ee.getModifiers().contains(Modifier.STATIC))
                    continue;
                
                if (ee.getReturnType().getKind() != TypeKind.BOOLEAN) {
                    messager.printMessage(Kind.ERROR, "Net action must return a boolean type.", ee);
                    continue;
                }
                
                try {
                    generatePacket(ee);
                } catch (Exception err) {
                    err.printStackTrace();
                    throw err;
                }
            }
        }

        if (roundEnv.processingOver()) {
            generateCallRegister();
            fcCallWriter.endClass();
            fcCallWriter.close();
        }

        return true;
    }

    private void generatePacket(ExecutableElement method) {
        String name = method.getSimpleName().toString();
        JavaFileObject packetObj = null;
        codeWriter packetWriter = null;
        boolean errHappened = false;

        try {
            packetObj = filer.createSourceFile("finalCampaign.net.packet." + name + "Packet", method);
            packetWriter = new codeWriter(packetObj.openWriter(), method);
        } catch(Exception err) {
            messager.printMessage(Kind.ERROR, "Failed to create source file: " + err.getLocalizedMessage());
            return;
        }

        packetWriter.packageName("finalCampaign.net.packet");
        packetWriter.importPackage("arc.util.io.*");
        packetWriter.importPackage("mindustry.*");
        packetWriter.importPackage("mindustry.gen.*");
        packetWriter.importPackage("mindustry.net.*");
        packetWriter.importPackage("mindustry.io.*");
        packetWriter.importPackage("mindustry.type.*");
        packetWriter.importPackage("finalCampaign.annotation.net.*");
        packetWriter.importPackage("finalCampaign.map.*");
        packetWriter.importPackage("finalCampaign.net.*");
        packetWriter.importPackage("finalCampaign.net.fcNet.*");
        packetWriter.importPackage("finalCampaign.util.io.*");

        packetWriter.annotation("SuppressWarnings", "\"all\"");

        annotationsUtil methodAnnotations = new annotationsUtil(method.getAnnotationMirrors());
        annotationUtil netCall = methodAnnotations.getAnnotation("finalCampaign.annotation.net.netCall");

        if (netCall == null) {
            messager.printMessage(Kind.ERROR, "A method without netCall annotation: " + name, method);
            return;
        }

        VariableElement packetSrc = netCall.getValue("src");
        String packetSrcName = packetSrc.getSimpleName().toString();
        boolean packetReliable = netCall.getValue("reliable", true);
        packetWriter.beginAnnotationWithArg("netCall");
        packetWriter.annotationArg("src", "packetSource." + packetSrcName);
        if (!packetReliable)
            packetWriter.annotationArg("reliable", "false");
        packetWriter.endAnnotationWithArg();

        packetWriter.beginClass(modifier.m_public, name + "Packet", "fcPacket");

        List<? extends VariableElement> methodParas = method.getParameters();
        if (methodParas.size() == 0) {
            messager.printMessage(Kind.ERROR, "Action should have player arg at least.", method);
            errHappened = true;
        } else {
            ArrayList<VariableElement> lst = new ArrayList<>();
            for (int i=1; i<methodParas.size(); i++)
                lst.add((VariableElement) methodParas.get(i));
            methodParas = lst;
        }

        for (VariableElement ve : methodParas)
            packetWriter.classVariable(modifier.m_public, ve.asType().toString(), ve.getSimpleName().toString());

        packetWriter.row();

        errHappened &= generatePacketRead(method, methodParas, packetWriter);
        errHappened &= generatePacketWrite(method, methodParas, packetWriter);

        generateChecks(method, methodParas, methodAnnotations, packetWriter);

        if (packetSrcName.equals("both") || packetSrcName.equals("server"))
            generatePacketHandle(false, method, methodParas, methodAnnotations, packetWriter);

        if (packetSrcName.equals("both") || packetSrcName.equals("client"))
            generatePacketHandle(true, method, methodParas, methodAnnotations, packetWriter);

        packetWriter.endClass();
        packetWriter.close();

        if (errHappened) {
            packetObj.delete();
        } else {
            packets.add(name + "Packet");
            generatePacketCall(method, methodParas, packetSrcName);
        }
    }

    private boolean generatePacketRead(ExecutableElement method, List<? extends VariableElement> methodParas, codeWriter packetWriter) {
        packetWriter.annotation("Override");
        packetWriter.beginMethodHead(modifier.m_public, "read", "void");
        packetWriter.methodArg("Reads", "reads");
        packetWriter.endMethodHead();
        packetWriter.statement("super.read(reads)");

        for (VariableElement ve : methodParas) {
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
                    if (!types.isSameType(rtee.getReturnType(), ve.asType()))
                        continue;
                    
                    packetWriter.statement("this.%s = reads.%s()", veName, rteeName);
                    done = true;
                    break;
                }
            }

            if (done)
                continue;

            String[] readerLst = {"mindustry.io.TypeIO", "finalCampaign.util.io.typeIO"};
            for (String reader : readerLst) {
                TypeElement te = elements.getTypeElement(reader);
                for (Element re : te.getEnclosedElements()) {
                    if (re instanceof ExecutableElement ree) {
                        String reeName = ree.getSimpleName().toString();
                        if (!reeName.startsWith("read") || ree.getParameters().size() != 1)
                            continue;
                        if (!types.isSameType(ree.getReturnType(), ve.asType()))
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
                messager.printMessage(Kind.ERROR, "Could not find a method to read such a type: " + ve.asType().toString(), method);
                return false;
            }
        }

        packetWriter.endMethod();
        return true;
    }

    private boolean generatePacketWrite(ExecutableElement method, List<? extends VariableElement> methodParas, codeWriter packetWriter) {
        packetWriter.annotation("Override");
        packetWriter.beginMethodHead(modifier.m_public, "write", "void");
        packetWriter.methodArg("Writes", "writes");
        packetWriter.endMethodHead();
        packetWriter.statement("super.write(writes)");

        for (VariableElement ve : methodParas) {
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
                    if (!types.isSameType(rtee.getReturnType(), ve.asType()))
                        continue;
                    
                    packetWriter.statement("writes.%s(this.%s)", rteeName, veName);
                    done = true;
                    break;
                }
            }

            if (done)
                continue;

            TypeElement writesType = elements.getTypeElement("arc.util.io.Writes");
            String[] writerLst = {"mindustry.io.TypeIO", "finalCampaign.util.io.typeIO"};
            for (String writer : writerLst) {
                TypeElement te = elements.getTypeElement(writer);
                for (Element we : te.getEnclosedElements()) {
                    if (we instanceof ExecutableElement wee) {
                        String weeName = wee.getSimpleName().toString();
                        if (!weeName.startsWith("write") || wee.getParameters().size() != 2)
                            continue;
                        if (!types.isSameType(wee.getParameters().get(0).asType(), writesType.asType()) ||
                            !types.isSameType(wee.getParameters().get(1).asType(), ve.asType()))
                            continue;
                        
                        packetWriter.statement("%s.%s(writes, this.%s)", te.getSimpleName().toString(), weeName, veName);
                        done = true;
                        break;
                    }
                }

                if (done)
                    break;
            }

            if (!done) {
                messager.printMessage(Kind.ERROR, "Could not find a method to write such a type: " + ve.asType().toString(), method);
                return false;
            }
        }
        
        packetWriter.endMethod();
        return true;
    }

    private void generatePacketHandle(boolean serverSide, ExecutableElement method, List<? extends VariableElement> methodParas, annotationsUtil methodAnnotations, codeWriter packetWriter) {
        String methodName = method.getSimpleName().toString();
        
        packetWriter.annotation("Override");
        packetWriter.beginMethodHead(modifier.m_public, serverSide ? "handleServer" : "handleClient", "void");
        if (serverSide)
            packetWriter.methodArg("Player", "player");
        packetWriter.endMethodHead();

        if (serverSide)
            packetWriter.statement("super.handleServer(player)");

        packetWriter.statement("if (!check()) return");
        packetWriter.row();

        ArrayList<String> parasToPass = new ArrayList<>();
        for (VariableElement ve : methodParas)
            parasToPass.add("this." + ve.getSimpleName().toString());
        String actionCall =  String.format("fcAction.%s(this.__caller, %s)", methodName, String.join(", ", parasToPass));
        if (serverSide) {
            packetWriter.beginBlock("if (%s)", actionCall);
            packetWriter.statement("fcNet.send(this)");
            packetWriter.endBlock();
        } else {
            packetWriter.statement(actionCall);
        }

        packetWriter.endMethod();
    }

    private void generateChecks(ExecutableElement method, List<? extends VariableElement> methodParas, annotationsUtil methodAnnotations, codeWriter packetWriter) {
        String playerPara = "this.__caller";

        annotationUtil netCall = methodAnnotations.getAnnotation("finalCampaign.annotation.net.netCall");
        annotationUtil nullCheckExcludeAnnotation = methodAnnotations.getAnnotation("finalCampaign.annotation.net.nullCheckExclude");
        annotationUtil deadCheckExcludeAnnotation = methodAnnotations.getAnnotation("finalCampaign.annotation.net.deadCheckExclude");
        annotationUtil teamCheckOptAnnotation = methodAnnotations.getAnnotation("finalCampaign.annotation.net.teamCheckOpt");
        annotationUtil buildingTargetAnnotation = methodAnnotations.getAnnotation("finalCampaign.annotation.net.buildingTarget");

        boolean nullCheck = netCall.getValue("nullCheck", true);
        boolean deadCheck = netCall.getValue("deadCheck", true);
        boolean teamCheck = netCall.getValue("teamCheck", true);
        boolean buildingModuleCheck = netCall.getValue("buildingModuleCheck", true);
        boolean teamCheckSkipInSandbox = true;
        boolean sandboxOnly = methodAnnotations.getAnnotation("finalCampaign.annotation.net.sandboxOnly") != null;
        ArrayList<String> nullCheckExclude = new ArrayList<>();
        ArrayList<String> deadCheckExclude = new ArrayList<>();
        ArrayList<String> teamCheckExclude = new ArrayList<>();
        ArrayList<TypeMirror> buildingTarget = new ArrayList<>();

        if (nullCheckExcludeAnnotation != null)
            nullCheckExclude = nullCheckExcludeAnnotation.getValueAsArray("value");
        if (deadCheckExcludeAnnotation != null)
            deadCheckExclude = deadCheckExcludeAnnotation.getValueAsArray("value");
        if (teamCheckOptAnnotation != null) {
            teamCheckExclude = teamCheckOptAnnotation.getValueAsArray("exclude");
            teamCheckSkipInSandbox = teamCheckOptAnnotation.getValue("skipInSandbox", true);
        }
        if (buildingTargetAnnotation != null)
            buildingTarget = buildingTargetAnnotation.getValueAsArray("value");
        
        ArrayList<VariableElement> methodObjectParas = new ArrayList<>();
        for (VariableElement ve : methodParas) {
            switch (ve.asType().getKind()) {
                case INT:
                case SHORT:
                case LONG:
                case CHAR:
                case BOOLEAN:
                case BYTE:
                case FLOAT:
                case DOUBLE:
                    continue;
                default:
                    methodObjectParas.add(ve);
            }
        }

        ArrayList<VariableElement> methodBuildingParas = new ArrayList<>();
        TypeMirror buildingType = elements.getTypeElement("mindustry.gen.Building").asType();
        for (VariableElement ve : methodObjectParas) {
            if (types.isAssignable(ve.asType(), buildingType))
                methodBuildingParas.add(ve);
        }

        packetWriter.beginMethodHead(modifier.m_private, "check", "boolean");
        packetWriter.endMethodHead();

        if (nullCheck) {
            packetWriter.statement("if (%s == null) return false", playerPara);
            for (VariableElement ve : methodObjectParas) {
                String paraName = ve.getSimpleName().toString();
                if (nullCheckExclude.contains(paraName))
                    continue;
                
                packetWriter.statement("if (this.%s == null) return false", paraName);
            }
        }

        if (deadCheck) {
            TypeMirror healthcInterface = elements.getTypeElement("mindustry.gen.Healthc").asType();
            for (VariableElement ve : methodObjectParas) {
                String paraName = ve.getSimpleName().toString();
                if (deadCheckExclude.contains(paraName))
                    continue;
                if (!types.isAssignable(ve.asType(), healthcInterface))
                    continue;
                
                packetWriter.statement("if (this.%s != null && this.%s.dead()) return false", paraName, paraName);
            }
        }

        if (teamCheck) {
            TypeMirror teamcInterface = elements.getTypeElement("mindustry.gen.Teamc").asType();
            if (teamCheckSkipInSandbox)
                packetWriter.beginBlock("if (!fcMap.sandbox())");

            for (VariableElement ve : methodObjectParas) {
                String paraName = ve.getSimpleName().toString();
                if (teamCheckExclude.contains(paraName))
                    continue;
                if (!types.isAssignable(ve.asType(), teamcInterface))
                    continue;

                packetWriter.statement("if (%s != null && this.%s != null && !this.%s.team().equals(%s.team())) return false", playerPara, paraName, paraName, playerPara);
            }

            if (teamCheckSkipInSandbox)
                packetWriter.endBlock();
        }

        if (buildingModuleCheck) {
            TypeMirror itemType = elements.getTypeElement("mindustry.type.Item").asType();
            TypeMirror liquidType = elements.getTypeElement("mindustry.type.Liquid").asType();
            boolean checkItem = false;
            boolean checkLiquid = false;

            for (VariableElement ve : methodObjectParas) {
                TypeMirror paraType = ve.asType();

                if (types.isAssignable(paraType, itemType))
                    checkItem = true;
                if (types.isAssignable(paraType, liquidType))
                    checkLiquid = true;
            }

            if (checkItem)
                for (VariableElement buildingPara : methodBuildingParas) {
                    String buildingParaName = buildingPara.getSimpleName().toString();
                    packetWriter.statement("if (this.%s != null && this.%s.items == null) return false", buildingParaName, buildingParaName);
                }

            if (checkLiquid)
                for (VariableElement buildingPara : methodBuildingParas) {
                    String buildingParaName = buildingPara.getSimpleName().toString();
                    packetWriter.statement("if (this.%s != null && this.%s.liquids == null) return false", buildingParaName, buildingParaName);
                }
        }

        {
            ArrayList<String> ifConditionLst = new ArrayList<>();
            for (TypeMirror buildingTargetType : buildingTarget) 
                ifConditionLst.add(String.format("!(this.%%s instanceof %s)", ((TypeElement) types.asElement(buildingTargetType)).getQualifiedName()));
            String ifConditionFmt = String.join(" && ", ifConditionLst);

            if (ifConditionLst.size() > 0)
                for (VariableElement ve : methodBuildingParas)
                    packetWriter.statement(("if (" + ifConditionFmt + ") return false").replace("%s", ve.getSimpleName().toString()));
        }

        if (sandboxOnly) {
            packetWriter.statement("if (!fcMap.sandbox()) return false");
        }

        packetWriter.row();
        packetWriter.statement("return true");
        packetWriter.endMethod();
    }

    private void generatePacketCall(ExecutableElement method, List<? extends VariableElement> methodParas, String packetSrc) {
        String methodName = method.getSimpleName().toString();

        fcCallWriter.beginMethodHead(modifier.m_public_static, methodName, "void");
        for (VariableElement ve : methodParas)
            fcCallWriter.methodArg(ve.asType().toString(), ve.getSimpleName().toString());
        fcCallWriter.endMethodHead();

        fcCallWriter.statement("%sPacket packet = new %sPacket()", methodName, methodName);
        for (VariableElement ve : methodParas) {
            String veName = ve.getSimpleName().toString();
            fcCallWriter.statement("packet.%s = %s", veName, veName);
        }
        fcCallWriter.row();

        if (packetSrc.equals("both")) {
            fcCallWriter.beginBlock("if (!Vars.net.active() || Vars.net.server())");
            fcCallWriter.statement("Core.app.post(() -> packet.handleServer(Vars.player))");
            fcCallWriter.endBlock();

            fcCallWriter.beginBlock("if (Vars.net.client())");
            fcCallWriter.statement("fcNet.send(packet)");
            fcCallWriter.endBlock();
        } else if (packetSrc.equals("client")) {
            fcCallWriter.beginBlock("if (Vars.net.client())");
            fcCallWriter.statement("fcNet.send(packet)");
            fcCallWriter.endBlock();
        } else if (packetSrc.equals("server")) {
            fcCallWriter.beginBlock("if (Vars.net.server())");
            fcCallWriter.statement("fcNet.send(packet)");
            fcCallWriter.endBlock();
        }

        fcCallWriter.endMethod();
    }

    private void generateCallRegister() {
        fcCallWriter.beginMethodHead(modifier.m_public_static, "register", "void");
        fcCallWriter.endMethodHead();
        for (String packet : packets)
            fcCallWriter.statement("packets.registerPacket(%s::new)", packet);
        fcCallWriter.endMethod();
    }
    
}
