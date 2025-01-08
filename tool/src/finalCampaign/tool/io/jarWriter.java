package finalCampaign.tool.io;

import java.io.*;
import java.util.*;
import java.util.zip.*;
import arc.struct.*;
import finalCampaign.launch.*;

public class jarWriter {
    private countableOutputStream countableStream;
    private ZipOutputStream stream;
    private Seq<String> addedEntry;
    private Seq<String> excludePath;
    private Deflater def;
    private boolean isApk;

    public jarWriter(fi jarFi, boolean apk) {
        try {
            countableStream = new countableOutputStream();
            countableStream.underlayStream = jarFi.write();

            stream = new ZipOutputStream(countableStream);
            stream.setLevel(Deflater.DEFAULT_COMPRESSION);

            def = new Deflater(Deflater.DEFAULT_COMPRESSION, true);

            addedEntry = new Seq<>();
            isApk = apk;
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void mkdirs(String path) {
        if (path.length() == 0) return;
        if (isApk) return;
        
        if (path.endsWith("/")) path = path.substring(0, path.length() - 1);
        if (path.startsWith("/")) path = path.substring(1);
        String[] paths = path.split("/");
        String currentPath = "";

        for (String sub : paths) {
            currentPath += sub;
            addEntry(String.join("/", currentPath) + "/", true, null, ZipOutputStream.DEFLATED);
            currentPath += "/";
        }
    }

    private String getParentPath(String path) {
        if (path.length() == 0) return "";
        String[] paths = path.split("/");
        if (paths.length == 1) return "";
        return String.join("/", Arrays.copyOf(paths, paths.length - 1));
    }

    public void add(String path, byte[] file, int method) {
        mkdirs(getParentPath(path));
        addEntry(path, false, file, method);
    }

    public void add(String path, byte[] file) {
        add(path, file, ZipOutputStream.DEFLATED);
    }

    public void add(String path, fi directory, int method) {
        if (!directory.isDirectory()) throw new RuntimeException("Not a directory: " + path);
        mkdirs(path);
        for (fi file : directory.list()) {
            String filePath = (path.length() > 0 ? path + "/" : "") + file.name();
            if (file.isDirectory()) {
                add(filePath, file, method);
            } else {
                add(filePath, file.readBytes(), method);
            }
        }
    }

    public void add(String path, fi directory) {
        add(path, directory, ZipOutputStream.DEFLATED);
    }

    public void exclude(String path) {
        if (!path.endsWith("/"))
            path += "/";
        if (!path.startsWith("/"))
            path = "/" + path;
        excludePath.add(path);
    }

    private void addEntry(String entry, boolean dir, byte[] data, int method) {
        for (String excludes : excludePath)
            if (entry.startsWith(excludes))
                return;

        if (addedEntry.contains(entry)) return;

        try {
            ZipEntry e = new ZipEntry(entry);

            if (!dir) {
                e.setMethod(method);
                e.setSize(data.length);

                // i know it's time wasting, but
                // no more good ideas without a zip output stream by myself
                if (method != ZipOutputStream.STORED) {
                    byte[] buffer = new byte[data.length];

                    def.setInput(data);
                    def.finish();

                    while (!def.finished()) {
                        def.deflate(buffer, 0, data.length);
                    }

                    e.setCompressedSize(def.getBytesWritten());
                    def.reset();
                } else {
                    if (isApk && entry.endsWith("resources.arsc")) {
                        // 4 bytes align. Since the header size of this file will be 44, not add it here.
                        e.setExtra(new byte[4 - (countableStream.bytesWritten % 4)]);
                    }
                    e.setCompressedSize(data.length);
                }

                CRC32 crc32 = new CRC32();
                crc32.update(data, 0, data.length);
                e.setCrc(crc32.getValue());
            } else {
                e.setSize(0);
                e.setCompressedSize(2);
                e.setCrc(0);
            }

            stream.putNextEntry(e);
            if (!dir) stream.write(data);
            stream.closeEntry();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
        addedEntry.add(entry);
    }

    public void close() {
        try {
            stream.close();
            def.end();
        } catch(Exception e) {
            throw new RuntimeException(e);
        }
    }

    private static class countableOutputStream extends OutputStream {
        int bytesWritten = 0;
        OutputStream underlayStream;
        
        @Override
        public void write(byte b[], int off, int len) throws IOException {
            underlayStream.write(b, off, len);
            bytesWritten += len;
        }

        @Override
        public void write(int b) throws IOException {
            underlayStream.write(b);
            bytesWritten ++;
        }

        @Override
        public void write(byte b[]) throws IOException {
            underlayStream.write(b);
            bytesWritten += b.length;
        }

        @Override
        public void flush() throws IOException {
            underlayStream.flush();
        }

        @Override
        public void close() throws IOException {
            underlayStream.close();
        }
    }
}