package com.aipai.gallop;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import com.aipai.gallop.http.client.cache.HttpCacheEntry;
import com.aipai.gallop.http.client.cache.HttpCacheEntrySerializer;
import com.aipai.gallop.http.client.cache.HttpCacheStorage;
import com.aipai.gallop.http.client.cache.HttpCacheUpdateCallback;
import com.aipai.gallop.http.client.cache.HttpCacheUpdateException;
import com.aipai.gallop.http.impl.client.cache.CacheConfig;
import com.aipai.gallop.http.impl.client.cache.DefaultHttpCacheEntrySerializer;

public class HttpDiskCacheStorage implements HttpCacheStorage, Closeable {
    private DiskLruCache cache;
    private final int APP_VERSION = 618;
    private final HttpCacheEntrySerializer serializer;
    private MessageDigest md;
    
    public HttpDiskCacheStorage(String path, CacheConfig config) throws IOException {
        File cacheDir;
        cacheDir = new File(path);
        cacheDir.mkdir();
        
        cache = DiskLruCache.open(cacheDir, APP_VERSION, 1, Integer.MAX_VALUE);
        serializer = new DefaultHttpCacheEntrySerializer(); 
        try {
            md = MessageDigest.getInstance("MD5");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }
    
    // refer: http://stackoverflow.com/questions/9655181/convert-from-byte-array-to-hex-string-in-java
    final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        int v;
        for ( int j = 0; j < bytes.length; j++ ) {
            v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }
    
    protected String hash(String value) {
        String retval;
        md.reset();
        md.update(value.getBytes());
        byte[] digest = md.digest();
        retval = bytesToHex(digest);
        return retval;
    }
    
    @Override
    public HttpCacheEntry getEntry(String key) throws IOException {
        key = hash(key);
        
        DiskLruCache.Snapshot snapshot = cache.get(key);
        if(snapshot == null)
            return null;
        
        InputStream strm = snapshot.getInputStream(0);
        return serializer.readFrom(strm);
    }

    @Override
    public void putEntry(String key, HttpCacheEntry entry) throws IOException {
        key = hash(key);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        serializer.writeTo(entry, bos);
        DiskLruCache.Editor editor = cache.edit(key);
        OutputStream strm = editor.newOutputStream(0);
        strm.write(bos.toByteArray());
        editor.commit();
    }

    @Override
    public void removeEntry(String key) throws IOException {
        key = hash(key);
        cache.remove(key);
    }

    @Override
    public void updateEntry(String key, HttpCacheUpdateCallback callback) throws IOException,
            HttpCacheUpdateException {
        key = hash(key);
        HttpCacheEntry existingEntry = getEntry(key);
        HttpCacheEntry updatedEntry = callback.update(existingEntry);
        putEntry(key, updatedEntry);
    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub
        
    }

}
