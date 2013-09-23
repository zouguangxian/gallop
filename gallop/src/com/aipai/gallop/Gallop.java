package com.aipai.gallop;

import android.content.Context;
import android.os.Environment;

import java.io.File;

public class Gallop {

    public Gallop() {
        // TODO Auto-generated constructor stub
    }
    
    // Creates a unique subdirectory of the designated app cache directory.
    // Tries to use external
    // but if not mounted, falls back on internal storage.
    public static File getDiskCacheDir(Context context, String uniqueName) {
        // Check if media is mounted or storage is built-in, if so, try and use
        // external cache dir
        // otherwise use internal cache dir
        String cachePath;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState()) ||      
                !Environment.isExternalStorageRemovable()) {
            cachePath = context.getExternalCacheDir().getPath();
        } else {
            cachePath = context.getCacheDir().getPath();
        }       

        return new File(cachePath + File.separator + uniqueName);
    }       
    
    public static GallopHttpClient createDefault(Context context) {
        File cacheDir = getDiskCacheDir(context, "http-cache");
        String path = null;
        if(cacheDir != null) {
            path = cacheDir.getPath();
        }

        GallopHttpClient retval = new GallopHttpClient(GallopHttpClientBuilder.create(path).build());
        return retval;
    }
}
