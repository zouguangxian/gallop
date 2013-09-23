
package com.aipai.gallop;

import com.aipai.gallop.http.client.config.RequestConfig;
import com.aipai.gallop.http.impl.client.cache.CacheConfig;
import com.aipai.gallop.http.impl.client.cache.CachingHttpClientBuilder;

import java.io.IOException;

public class GallopHttpClientBuilder extends CachingHttpClientBuilder {
    private static final String VERSION = "0.6.1.8";

    private static final int DEFAULT_SOCKET_TIMEOUT = 5 * 1000;
    private static final int DEFAULT_MAX_RETRIES = 5;
    private static final int DEFAULT_MAX_OBJECT_SIZE = 200 * 1024;

    public GallopHttpClientBuilder() {
        super();
    }

    public static GallopHttpClientBuilder create(String path) {
        RequestConfig defaultRequestConfig = RequestConfig.custom()
                .setSocketTimeout(DEFAULT_SOCKET_TIMEOUT)
                .setConnectTimeout(DEFAULT_SOCKET_TIMEOUT)
                .setConnectionRequestTimeout(DEFAULT_SOCKET_TIMEOUT)
                .setStaleConnectionCheckEnabled(false)
                .setRedirectsEnabled(true)
                .setExpectContinueEnabled(false)
                .setMaxRedirects(DEFAULT_MAX_RETRIES)
                .build();

        CacheConfig defaultCacheConfig = CacheConfig.custom()
                .setMaxObjectSize(DEFAULT_MAX_OBJECT_SIZE)
                .build();

        GallopHttpClientBuilder builder = new GallopHttpClientBuilder();
        builder.setCacheConfig(defaultCacheConfig)
                .setDefaultRequestConfig(defaultRequestConfig)
                .setUserAgent(String.format("gallop-http/%s", VERSION));

        try {
            HttpDiskCacheStorage defaultDiskCacheStorage = new HttpDiskCacheStorage(path, defaultCacheConfig);
            builder.setHttpCacheStorage(defaultDiskCacheStorage);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return builder;
    }
}
