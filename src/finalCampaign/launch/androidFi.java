package finalCampaign.launch;

import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import arc.util.io.Streams;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

/**
 * (Modified from {@link arc.backend.android.AndroidFi})
 * <p>
 * @author mzechner
 * @author Nathan Sweet
 */
public class androidFi extends bothFi{
    // The asset manager, or null if this is not an internal file.
    private final AssetManager assets;

    public androidFi(AssetManager assets, String fileName, FileType type){
        super(fileName.replace('\\', '/'), type);
        this.assets = assets;
    }

    public androidFi(AssetManager assets, File file, FileType type){
        super(file, type);
        this.assets = assets;
    }

    @Override
    public bothFi child(String name){
        name = name.replace('\\', '/');
        if(file.getPath().length() == 0) return new androidFi(assets, new File(name), type);
        return new androidFi(assets, new File(file, name), type);
    }

    @Override
    public bothFi sibling(String name){
        throw new RuntimeException("Not support");
    }

    @Override
    public bothFi parent(){
        File parent = file.getParentFile();
        if(parent == null){
            if(type == FileType.absolute)
                parent = new File("/");
            else
                parent = new File("");
        }
        return new androidFi(assets, parent, type);
    }

    @Override
    public InputStream read(){
        if(type == FileType.internal){
            try{
                return assets.open(file.getPath());
            }catch(IOException ex){
                throw new RuntimeException("Error reading file: " + file + " (" + type + ")", ex);
            }
        }
        return super.read();
    }

    @Override
    public ByteBuffer map(FileChannel.MapMode mode){
        if(type == FileType.internal){
            FileInputStream input = null;
            try{
                AssetFileDescriptor fd = getAssetFileDescriptor();
                long startOffset = fd.getStartOffset();
                long declaredLength = fd.getDeclaredLength();
                input = new FileInputStream(fd.getFileDescriptor());
                ByteBuffer map = input.getChannel().map(mode, startOffset, declaredLength);
                map.order(ByteOrder.nativeOrder());
                return map;
            }catch(Exception ex){
                throw new RuntimeException("Error memory mapping file: " + this + " (" + type + ")", ex);
            }finally{
                Streams.close(input);
            }
        }
        return super.map(mode);
    }

    @Override
    public bothFi[] list(){
        if(type == FileType.internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                bothFi[] handles = new bothFi[relativePaths.length];
                for(int i = 0, n = handles.length; i < n; i++)
                    handles[i] = new androidFi(assets, new File(file, relativePaths[i]), type);
                return handles;
            }catch(Exception ex){
                throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list();
    }

    @Override
    public bothFi[] list(FileFilter filter){
        if(type == FileType.internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                bothFi[] handles = new bothFi[relativePaths.length];
                int count = 0;
                for(int i = 0, n = handles.length; i < n; i++){
                    String path = relativePaths[i];
                    bothFi child = new androidFi(assets, new File(file, path), type);
                    if(!filter.accept(child.file())) continue;
                    handles[count] = child;
                    count++;
                }
                if(count < relativePaths.length){
                    bothFi[] newHandles = new bothFi[count];
                    System.arraycopy(handles, 0, newHandles, 0, count);
                    handles = newHandles;
                }
                return handles;
            }catch(Exception ex){
                throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list(filter);
    }

    @Override
    public bothFi[] list(FilenameFilter filter){
        if(type == FileType.internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                bothFi[] handles = new bothFi[relativePaths.length];
                int count = 0;
                for(int i = 0, n = handles.length; i < n; i++){
                    String path = relativePaths[i];
                    if(!filter.accept(file, path)) continue;
                    handles[count] = new androidFi(assets, new File(file, path), type);
                    count++;
                }
                if(count < relativePaths.length){
                    bothFi[] newHandles = new bothFi[count];
                    System.arraycopy(handles, 0, newHandles, 0, count);
                    handles = newHandles;
                }
                return handles;
            }catch(Exception ex){
                throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list(filter);
    }

    @Override
    public bothFi[] list(String suffix){
        if(type == FileType.internal){
            try{
                String[] relativePaths = assets.list(file.getPath());
                bothFi[] handles = new bothFi[relativePaths.length];
                int count = 0;
                for(int i = 0, n = handles.length; i < n; i++){
                    String path = relativePaths[i];
                    if(!path.endsWith(suffix)) continue;
                    handles[count] = new androidFi(assets, new File(file, path), type);
                    count++;
                }
                if(count < relativePaths.length){
                    bothFi[] newHandles = new bothFi[count];
                    System.arraycopy(handles, 0, newHandles, 0, count);
                    handles = newHandles;
                }
                return handles;
            }catch(Exception ex){
                throw new RuntimeException("Error listing children: " + file + " (" + type + ")", ex);
            }
        }
        return super.list(suffix);
    }

    @Override
    public boolean isDirectory(){
        if(type == FileType.internal){
            try{
                return assets.list(file.getPath()).length > 0;
            }catch(IOException ex){
                return false;
            }
        }
        return super.isDirectory();
    }

    @Override
    public boolean exists(){
        if(type == FileType.internal){
            String fileName = file.getPath();
            try{
                assets.open(fileName).close(); // Check if file exists.
                return true;
            }catch(Exception ex){
                // This is SUPER slow! but we need it for directories.
                try{
                    return assets.list(fileName).length > 0;
                }catch(Exception ignored){
                }
                return false;
            }
        }
        return super.exists();
    }

    @Override
    public long length(){
        if(type == FileType.internal){
            try(AssetFileDescriptor fileDescriptor = assets.openFd(file.getPath())){
                return fileDescriptor.getLength();
            }catch(IOException ignored){
            }
        }
        return super.length();
    }

    @Override
    public File file(){
        if(type == FileType.local) return new File(bothFiles.LocalStoragePath, file.getPath());
        return super.file();
    }

    /**
     * @return an AssetFileDescriptor for this file or null if the file is not of type Internal
     * @throws IOException - thrown by AssetManager.openFd()
     */
    public AssetFileDescriptor getAssetFileDescriptor() throws IOException{
        return assets != null ? assets.openFd(path()) : null;
    }
}
