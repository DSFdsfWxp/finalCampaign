package finalCampaign.launch;

public class desktopClassLoader extends shareClassLoader {
    protected Class<?> platformDefineClass(String name, byte[] bytecode) {
        return defineClass(name, bytecode, 0, bytecode.length);
    }
}
