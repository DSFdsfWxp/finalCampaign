package finalCampaign.launch;

import arc.struct.*;
import arc.util.*;

import java.io.*;
import java.util.*;
import java.util.zip.*;

/** 
 * (Modified from {@link arc.files.ZipFi})
 * The ZipFi to used in both mod side and launcher side.
 * <p>
 * A FileHandle meant for easily representing and reading the contents of a zip/jar file.*/
public class bothZipFi extends bothFi{
    private @Nullable bothZipFi[] children;
    private @Nullable bothZipFi parent;
    private String path;

    private Seq<bothZipFi> allFiles, allDirectories;

    private final @Nullable ZipEntry entry;
    private final ZipFile zip;

    @SuppressWarnings("unchecked")
    public bothZipFi(bothFi zipFileLoc){
        super(new File(""), FileType.absolute);
        entry = null;

        try{
            zip = new ZipFile(zipFileLoc.file());
            path = "";

            Seq<ZipEntry> entries = (Seq<ZipEntry>)Seq.with(Collections.list(zip.entries()));
            ObjectMap<String, ZipEntry> byName = new ObjectMap<>();
            entries.each(e -> byName.put(e.getName(), e));

            Seq<String> names = entries.map(z -> z.getName().replace('\\', '/'));
            ObjectSet<String> paths = new ObjectSet<>();

            for(String path : names){
                paths.add(path);
                while(path.contains("/") && !path.equals("/") && path.substring(0, path.length() - 1).contains("/")){
                    int index = path.endsWith("/") ? path.substring(0, path.length() - 1).lastIndexOf('/') : path.lastIndexOf('/');
                    path = path.substring(0, index);
                    paths.add(path.endsWith("/") ? path : path + "/");
                }
            }

            if(paths.contains("/")){
                file = new File("/");
                paths.remove("/");
            }

            allFiles = new Seq<>();
            allDirectories = new Seq<>();

            for(String s : paths){
                ZipEntry entry = byName.get(s);

                bothZipFi file =  entry != null ? new bothZipFi(entry, zip, allFiles, allDirectories) : new bothZipFi(s, zip, allFiles, allDirectories);
                allFiles.add(file);

                if(file.isDirectory()){
                    allDirectories.add(file);
                }
            }

            allFiles.add(this);
            allDirectories.add(this);

            parent = null;
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    private static int countSlashes(String str){
        int sum = 0;
        for(int i = 0; i < str.length(); i++){
            if(str.charAt(i) == '/') sum ++;
        }
        return sum;
    }

    private bothZipFi(ZipEntry entry, ZipFile file, Seq<bothZipFi> allFiles, Seq<bothZipFi> allDirectories){
        super(new File(entry.getName()), FileType.absolute);
        this.allDirectories = allDirectories;
        this.allFiles = allFiles;
        this.path = entry.getName().replace('\\', '/');
        this.entry = entry;
        this.zip = file;
    }

    private bothZipFi(String path, ZipFile file, Seq<bothZipFi> allFiles, Seq<bothZipFi> allDirectories){
        super(new File(path), FileType.absolute);
        this.allDirectories = allDirectories;
        this.allFiles = allFiles;
        this.path = path.replace('\\', '/');
        this.entry = null;
        this.zip = file;
    }

    @Override
    public boolean delete(){
        try{
            zip.close();
            return true;
        }catch(IOException e){
            Log.err(e);
            return false;
        }
    }

    @Override
    public boolean exists(){
        return true;
    }

    @Override
    public bothFi child(String name){
        //trigger cache
        list();

        for(bothZipFi child : children){
            if(child.name().equals(name)){
                return child;
            }
        }

        return new bothFi(new File(file, name)){
            @Override
            public boolean exists(){
                return false;
            }
        };
    }

    @Override
    public String name(){
        return file.getName();
    }

    @Override
    public String path(){
        return path;
    }

    private static boolean isChild(bothZipFi file, bothZipFi dir){
        return dir != file
            && file.path().startsWith(dir.path())
            && (file.path().substring(1 + dir.path().length()).indexOf('/') == -1 || //do not allow extra slashes in the path
            (file.path().endsWith("/") && countSlashes(file.path().substring(1 + dir.path().length())) == 1));
    }

    @Override
    public bothFi parent(){
        //root
        if(path.length() == 0) return null;

        if(parent == null){
            parent = allDirectories.find(other -> isChild(this, other));
        }

        return parent;
    }

    @Override
    public bothFi[] list(){
        if(children == null){
            children = allFiles.select(f -> f.parent == this || isChild(f, this)).toArray(bothZipFi.class);
        }

        return children;
    }

    @Override
    public boolean isDirectory(){
        return entry == null || entry.isDirectory();
    }

    @Override
    public InputStream read(){
        if(entry == null) throw new RuntimeException("Not permitted.");
        try{
            return zip.getInputStream(entry);
        }catch(IOException e){
            throw new RuntimeException(e);
        }
    }

    @Override
    public long length(){
        return isDirectory() ? 0 : entry.getSize();
    }

    @Override
    public String toString(){
        return path();
    }
}