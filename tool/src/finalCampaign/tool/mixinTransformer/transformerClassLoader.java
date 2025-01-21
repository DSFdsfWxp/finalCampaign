package finalCampaign.tool.mixinTransformer;

import arc.func.*;
import arc.struct.*;
import finalCampaign.launch.*;
import finalCampaign.tool.patcher.*;

public class transformerClassLoader extends shareClassLoader {
    private Seq<String> generatedClasses = new Seq<>();

    @Override
    protected Class<?> tryLoadClass(String name) {
        throw new RuntimeException("Never goes here.");
    }

    public void eachClassFile(Cons<fi> cons) {
        Seq<fi> stack = new Seq<>();
        jars.each(jar -> stack.add(jar.rootDir));

        while (stack.size > 0) {
            for (fi f : stack.first().list()) {
                if (f.isDirectory())
                    stack.add(f);
                else if (f.extension().equals("class"))
                    cons.get(f);
            }
            stack.remove(0);
        }
    }

    public Seq<patchedClass> patchClass(byte[] bytecode) {
        Seq<patchedClass> res = new Seq<>();

        classPatcher reader = new classPatcher(bytecode);
        patchedClass thisClass = new patchedClass();
        thisClass.name = reader.getClassName();
        thisClass.bytecode = transformer.transform(thisClass.name.replace("/", "."), bytecode);
        res.add(thisClass);

        Seq<byte[]> stack = new Seq<>();
        stack.add(thisClass.bytecode);

        while (stack.size > 0) {
            reader = new classPatcher(stack.first());
            Seq<String> classesNeedToParse = new Seq<>();
    
            for (classPatcher.constentPoolItem item : reader.constentItems) {
                if (item.tag == 7) { // class ref
                    classesNeedToParse.add(reader.constentItems[item.pos1].string);
                } else if (item.tag == 12) { // name and type descriptor
                    String tmp = null;
                    String src = reader.constentItems[item.pos2].string;
                    for (int ii=0; ii<src.length(); ii++) {
                        switch (src.charAt(ii)) {
                            case 'L': {
                                if (tmp == null) {
                                    tmp = "";
                                } else {
                                    tmp += "L";
                                }
                                break;
                            }
                            case ';': {
                                if (tmp != null) {
                                    classesNeedToParse.add(tmp);
                                    tmp = null;
                                }
                                break;
                            }
                            default: {
                                if (tmp != null) tmp += src.charAt(ii);
                            }
                        }
                    }
                }
            }
    
            for (String name : classesNeedToParse) {
                if (name.startsWith(reader.getClassName() + "$Anonymous$") && getResource(name + ".class") == null && !generatedClasses.contains(name)) {
                    patchedClass generatedClass = new patchedClass();
                    generatedClass.name = name;
                    generatedClass.bytecode = transformer.transform(name.replace("/", "."), null);

                    res.add(generatedClass);
                    stack.add(generatedClass.bytecode);
                    generatedClasses.add(name);
                }
            }

            stack.remove(0);
        }

        return res;
    }

    public static class patchedClass {
        String name;
        byte[] bytecode;
    }
    
}
