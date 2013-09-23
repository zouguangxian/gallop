package com.aipai.gallop;

import com.aipai.gallop.http.client.methods.HttpUriRequest;

public class HttpRequestWrapper implements Comparable<HttpUriRequest>{
    protected HttpUriRequest mRequest;
    public HttpRequestWrapper(HttpUriRequest request) {
        mRequest = request;
    }

    @Override
    public int compareTo(HttpUriRequest another) {
        return 0;
    }
}
