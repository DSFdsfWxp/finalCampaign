package finalCampaign.tool;

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.lang.annotation.*;
import finalCampaign.net.*;
import finalCampaign.net.fcNet.*;
import finalCampaign.util.*;
import mindustry.io.*;
import arc.files.*;
import arc.struct.*;
import arc.util.io.*;

public class genNet {
    private static Seq<String> fcCall = new Seq<>();
    private static ObjectMap<String, String> generatedPacket = new ObjectMap<>();

    public static void main(String[] args) {
        Class<?> actions = fcAction.class;

        for (Method action : actions.getDeclaredMethods()) {
            CallFrom callFrom = null;
            for (Annotation annotation : action.getDeclaredAnnotations())
                if (annotation instanceof CallFrom cf) callFrom = cf;

            if (!Modifier.isPublic(action.getModifiers())) continue;
            if (!Modifier.isStatic(action.getModifiers())) throw new RuntimeException("Not a valid action: " + action.getName());
            if (callFrom == null) throw new RuntimeException("Missing CallFrom Annotation in action: " + action.getName());
            if (!action.getReturnType().equals(boolean.class)) throw new RuntimeException("Not a valid action: " + action.getName()); 
            if (action.getParameterCount() < 1) throw new RuntimeException("Not a valid action: " + action.getName()); 

            generateActionCall(action, callFrom);
            generatePacket(action, callFrom);
        }

        Fi cwd = new Fi(new File("."));
        Fi dir = cwd.child("src").child("finalCampaign").child("net");

        dir.child("fcCall.java").writeString(generateFcCall());

        dir = dir.child("packet");
        if (!dir.exists()) dir.mkdirs();
        for (String packet : generatedPacket.keys()) dir.child(packet + ".java").writeString(generatedPacket.get(packet));
    }

    private static void generatePacket(Method action, CallFrom callFrom) {
        Seq<String> para = new Seq<>();
        Parameter[] paras = Arrays.copyOfRange(action.getParameters(), 1, action.getParameters().length);
        for (Parameter parameter : paras)
            para.add("public " + getTypeExpression(parameter.getType().getName()) + " " + parameter.getName() + ";");

        String annotation = "PacketSource." + callFrom.value().name();
        if (!callFrom.reliable()) annotation = "value = " + annotation + ", rereliable = false";

        String code = "";
        code += "package finalCampaign.net.packet;\n\n";
        code += "import mindustry.*;\n";
        code += "import finalCampaign.net.*;\n";
        code += "import finalCampaign.util.*;\n";
        code += "import finalCampaign.net.fcNet.*;\n";
        code += "import arc.util.io.*;\n";
        code += "import mindustry.gen.*;\n";
        code += "import mindustry.io.*;\n";
        code += "import mindustry.type.*;\n\n";
        code += "// Automatic generated, do not modify.\n\n";
        code += "@SuppressWarnings(\"all\")\n";
        code += String.format("@CallFrom(%s)\n", annotation);
        code += String.format("public class %sPacket extends fcPacket {\n", action.getName());
        for (String p : para) code += "    " + p + "\n";
        code += "\n";

        code += "    @Override\n";
        code += "    public void read(Reads reads) {\n";
        code += "        super.read(reads);\n";
        for (Parameter p : paras) code += "        " + generateReadStatement(p) + "\n";
        code += "    }\n\n";

        code += "    @Override\n";
        code += "    public void write(Writes writes) {\n";
        code += "        super.write(writes);\n";
        for (Parameter p : paras) code += "        " + generateWriteStatement(p) + "\n";
        code += "    }\n\n";

        para = new Seq<>();
        para.add("this.__caller");
        for (Parameter parameter : paras)
            para.add("this." + parameter.getName());
        String handleCode = String.format("fcAction.%s(%s)", action.getName(), String.join(", ", para));

        boolean handleServer = true;
        boolean handleClient = true;
        if (callFrom.value() == PacketSource.client) handleClient = false;
        if (callFrom.value() == PacketSource.server) handleServer = false;

        if (handleClient) {
            code += "    @Override\n";
            code += "    public void handleClient() {\n";
            code += "        " + handleCode + ";\n";
            code += "    }\n\n";
        }

        para = new Seq<>();
        para.add("player");
        for (Parameter parameter : paras)
            para.add("this." + parameter.getName());
        handleCode = String.format("fcAction.%s(%s)", action.getName(), String.join(", ", para));

        if (handleServer) {
            code += "    @Override\n";
            code += "    public void handleServer(Player player) {\n";
            code += "        super.handleServer(player);\n";
            code += "        if (" + handleCode + ") fcNet.send(this);\n";
            code += "    }\n\n";
        }

        code += "}";

        generatedPacket.put(action.getName() + "Packet", code);
    }

    private static String generateReadStatement(Parameter parameter) {
        Class<?>[] lst = {Reads.class, TypeIO.class, typeIO.class};
        boolean isArray = parameter.getType().getName().startsWith("[");
        for (Class<?> c : lst) {
            for (Method m : c.getMethods()) {
                if (m.getReturnType().equals(parameter.getType())) {
                    if (c.equals(Reads.class)) {
                        if (m.getName().equals("checkEOF")) continue;
                        if (m.getName().startsWith("u")) continue;
                        if (isArray) continue;
                        if (m.getParameterCount() > 0) continue;

                        return "this." + parameter.getName() + " = reads." + m.getName() + "();";
                    } else {
                        if (!m.getName().startsWith("read") || m.getParameterCount() != 1) continue;
                        if (!m.getParameterTypes()[0].equals(Reads.class)) continue;

                        return "this." + parameter.getName() + " = " + c.getSimpleName() + "." + m.getName() + "(reads);";
                    }
                }
            }
        }
        throw new RuntimeException("Could not find a method to read such a type: " + parameter.getType().getName());
    }

    private static String generateWriteStatement(Parameter parameter) {
        Class<?>[] lst = {Reads.class, TypeIO.class, typeIO.class};
        boolean isArray = parameter.getType().getName().startsWith("[");
        for (Class<?> c : lst) {
            for (Method m : c.getMethods()) {
                if (m.getReturnType().equals(parameter.getType())) {
                    if (c.equals(Reads.class)) {
                        if (m.getName().equals("checkEOF")) continue;
                        if (m.getName().startsWith("u")) continue;
                        if (isArray) continue;
                        if (m.getParameterCount() > 1) continue;

                        return "writes." + m.getName() + "(" + "this." + parameter.getName() + ");";
                    } else {
                        if (!m.getName().startsWith("read") || m.getParameterCount() != 1) continue;
                        if (!m.getParameterTypes()[0].equals(Reads.class)) continue;

                        return c.getSimpleName() + "." + m.getName().replace("read", "write") + "(writes, " + "this." + parameter.getName() + ");";
                    }
                }
            }
        }
        throw new RuntimeException("Could not find a method to write such a type: " + parameter.getType().getName());
    }

    private static String generateFcCall() {
        String code = "";
        code += "package finalCampaign.net;\n\n";
        code += "import mindustry.*;\n";
        code += "import finalCampaign.net.packet.*;\n\n";
        code += "// Automatic generated, do not modify.\n\n";
        code += "@SuppressWarnings(\"all\")\n";
        code += "public class fcCall {\n";
        code += String.join("\n\n", fcCall) + "\n\n";
        code += "    public static void register() {\n";
        for (String packet : generatedPacket.keys()) code += "        packets.registerPacket(" + packet + "::new);\n";
        code += "    }\n";
        code += "}";

        return code;
    }

    private static void generateActionCall(Method action, CallFrom callFrom) {
        Seq<String> para = new Seq<>();
        Parameter[] paras = Arrays.copyOfRange(action.getParameters(), 1, action.getParameters().length);
        for (Parameter parameter : paras)
            para.add(getTypeExpression(parameter.getType().getName()) + " " + parameter.getName());

        String code = "";
        Seq<String> args = new Seq<>(new String[] {action.getName(), String.join(", ", para), action.getName(), action.getName()});
        code += "    public static void %s(%s) {\n";
        code += "        %sPacket packet = new %sPacket();\n";

        for (Parameter parameter : paras) {
            code += "        packet.%s = %s;\n";
            args.add(new String[] {parameter.getName(), parameter.getName()});
        }
        code += "\n";

        switch (callFrom.value()) {
            case both: {
                code += "        if (!Vars.net.active() || Vars.net.server())\n";
                code += "            packet.handleServer(Vars.player);\n";
            }
            case client: {
                code += "        if (Vars.net.client())\n";
                code += "            fcNet.send(packet);\n";
                break;
            }
            case server: {
                code += "        if (Vars.net.server())\n";
                code += "            fcNet.send(packet);\n";
                break;
            }
        }

        code += "    }";

        fcCall.add(String.format(code, args.toArray(String.class)));
    }

    private static String getTypeExpression(String rawTypeName) {
        if (!rawTypeName.startsWith("[")) return rawTypeName;

        int arrayNum = 0;
        String[] splited = rawTypeName.split("");
        for (String t : splited) {
            if (!t.equals("[")) break;
            arrayNum ++;
        }

        String typeName = rawTypeName.substring(arrayNum + 1);
        switch (rawTypeName.substring(arrayNum - 1, arrayNum + 1)) {
            case "[I":
                typeName = "int";
                break;
            case "[Z":
                typeName = "boolean";
                break;
            case "[B":
                typeName = "byte";
                break;
            case "[S":
                typeName = "short";
                break;
            case "[J":
                typeName = "long";
                break;
            case "[F":
                typeName = "float";
                break;
            case "[D":
                typeName = "double";
                break;
            case "[C":
                typeName = "char";
                break;
            case "[L":
                typeName = typeName.substring(0, typeName.length() - 1);
                break;
            default:
                throw new RuntimeException("Should not reach here.");
        }

        return typeName + repeatString("[]", arrayNum);
    }

    private static String repeatString(String txt, int count) {
        String out = "";
        for (int i=0; i<count; i++) out += txt;
        return out;
    }
}
