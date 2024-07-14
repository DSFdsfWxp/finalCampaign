package finalCampaign.launch;

import java.io.*;

import arc.Core;
import arc.Files.*;
import arc.files.*;
import arc.util.*;

public class bothIOUtil {
    public static byte[] readAllBytes(InputStream inputStream) throws IOException {
        final int bufLen = 4 * 0x400; // 4KB
        byte[] buf = new byte[bufLen];
        int readLen;
        IOException exception = null;
   
        try {
            try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
                while ((readLen = inputStream.read(buf, 0, bufLen)) != -1)
                    outputStream.write(buf, 0, readLen);
   
                return outputStream.toByteArray();
            }
        } catch (IOException e) {
            exception = e;
            throw e;
        } finally {
            if (exception == null) inputStream.close();
            else try {
                inputStream.close();
            } catch (IOException e) {
                exception.addSuppressed(e);
            }
        }
    }

    public static InputStream readFileInternalAsStream(String path) {
        Fi file = OS.isAndroid ? Core.files.internal(path) : new Fi(path, FileType.internal);
        InputStream stream = null;
        try {
            stream = file.read();
        } catch(Exception ignore) {}
        return stream;
    }

    public static byte[] readFileInternalAsByte(String path) throws IOException {
        return readAllBytes(readFileInternalAsStream(path));
    }

}
