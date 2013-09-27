
package com.aipai.gallop;

import com.aipai.gallop.http.HttpResponse;
import com.aipai.gallop.http.client.HttpClient;
import com.aipai.gallop.http.client.methods.HttpUriRequest;
import com.aipai.gallop.http.client.protocol.HttpClientContext;
import com.aipai.gallop.http.impl.client.HttpRequestFutureTask;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.Future;

public class HttpRequestQueue {

    private HttpClient httpClient;
    private HttpRequestManager httpRequestManager;
    private final Map<Integer, WeakReference<Future<?>>> requestMap;
    private int nextid = 1;

    public HttpRequestQueue() {
        this(Gallop.getDefaultHttpClient(), Gallop.getDefaultHttpRequestManager());
    }

    public HttpRequestQueue(HttpClient client, HttpRequestManager manager) {
        httpClient = client;
        httpRequestManager = manager;
        requestMap = new WeakHashMap<Integer, WeakReference<Future<?>>>();
    }

    public int enqueue(HttpUriRequest uriRequest, HttpResponseHandler responseHandler) {

        HttpRequestFutureTask<HttpResponse> task = httpRequestManager.enqueue(httpClient, HttpClientContext.create(), uriRequest, responseHandler);
        int retval = nextid;
        requestMap.put(nextid++, new WeakReference<Future<?>>(task));
        return retval;
    }

    public void cancel() {
        for (Map.Entry<Integer, WeakReference<Future<?>>> entry : requestMap.entrySet()) {
            WeakReference<Future<?>> future = entry.getValue();
            Future<?> request = future.get();
            if (request != null) {
                request.cancel(true);
            }
        }
        requestMap.clear();
    }

    public void cancel(int id) {
        WeakReference<Future<?>> future = requestMap.get(id);
        if (future != null) {
            Future<?> request = future.get();
            if (request != null) {
                request.cancel(true);
            }
            requestMap.remove(id);
        }
    }
}
