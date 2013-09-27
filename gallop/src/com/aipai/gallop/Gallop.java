package com.aipai.gallop;

import android.content.Context;
import android.os.Environment;

import com.aipai.gallop.http.impl.client.CloseableHttpClient;

import java.io.File;

public class Gallop {

    private static CloseableHttpClient defaultHttpClient;
    private static HttpRequestManager defaultHttpRequestManager;
    
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
    
    public static CloseableHttpClient createDefaultHttpClient(Context context) {
        if (context == null && defaultHttpClient == null) {
            throw new IllegalArgumentException("context is null");
        }

        if (defaultHttpClient != null) {
            throw new RuntimeException("defaultHttpClient has been created");
        }

        File cacheDir = getDiskCacheDir(context, "http-cache");
        String path = null;
        if(cacheDir != null) {
            path = cacheDir.getPath();
        }
        defaultHttpClient = GallopHttpClientBuilder.create(path).build();

        return defaultHttpClient;
    }
    
    public static CloseableHttpClient getDefaultHttpClient()
    {
        return defaultHttpClient;
    }
    
    public static HttpRequestManager createDefaultHttpRequestManager(Context context) {
        if(defaultHttpRequestManager != null) {
            throw new RuntimeException("defaultHttpRequestManager has been created.");
        }
        defaultHttpRequestManager = new HttpRequestManager();
        return defaultHttpRequestManager;
    }
    
    public static HttpRequestManager getDefaultHttpRequestManager()
    {
        return defaultHttpRequestManager;
    }
    
    public static void createDefault(Context context)
    {
        createDefaultHttpClient(context);
        createDefaultHttpRequestManager(context);
    }
}
