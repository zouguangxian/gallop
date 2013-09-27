package com.aipai.gallop;

import com.aipai.gallop.http.HttpResponse;
import com.aipai.gallop.http.client.ClientProtocolException;
import com.aipai.gallop.http.client.HttpClient;
import com.aipai.gallop.http.client.ResponseHandler;
import com.aipai.gallop.http.client.methods.HttpUriRequest;
import com.aipai.gallop.http.concurrent.FutureCallback;
import com.aipai.gallop.http.impl.client.FutureRequestExecutionService;
import com.aipai.gallop.http.impl.client.HttpRequestFutureTask;
import com.aipai.gallop.http.protocol.HttpContext;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HttpRequestManager {
    private static final int MAX_THREADS = 10;
    
    private ExecutorService executorService;
    private HttpResponseMessageHandler messageHandler;
    private final Map<HttpClient, FutureRequestExecutionService> futureRequestExecutionServices;
    

    public HttpRequestManager() {
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
        messageHandler = new HttpResponseMessageHandler();
        futureRequestExecutionServices = new HashMap<HttpClient, FutureRequestExecutionService>();
    }
    
    private final class WrapResponseHandler implements ResponseHandler<HttpResponse> {

        public WrapResponseHandler(HttpResponseHandler responseHandler) {
        }
        
        @Override
        public HttpResponse handleResponse(HttpResponse response) throws ClientProtocolException, IOException {
            return response;
        }
    }
    
    private final class WrapCallback implements FutureCallback<HttpResponse> {

        private WeakReference<HttpResponseHandler> handlerRef;
        public WrapCallback(HttpResponseHandler responseHandler) {
            super();
            handlerRef = new WeakReference<HttpResponseHandler>(responseHandler);
        }

        @Override
        public void cancelled() {
            HttpResponseHandler handler = handlerRef.get();
            if (handler != null) {
                HttpRequestManager.this.messageHandler.sendCancelMessage(handler);
            }
        }

        @Override
        public void completed(HttpResponse response) {
            HttpResponseHandler handler = handlerRef.get();
            if (handler != null) {
                HttpRequestManager.this.messageHandler.sendSuccessMessage(handler, response);
            }
        }

        @Override
        public void failed(Exception e) {
            HttpResponseHandler handler = handlerRef.get();
            if (handler != null) {
                HttpRequestManager.this.messageHandler.sendFailureMessage(handler, e);
            }
        }
    }
    
    public HttpRequestFutureTask<HttpResponse> enqueue(HttpClient httpClient, HttpContext httpContext, HttpUriRequest uriRequest, HttpResponseHandler responseHandler) {
        FutureRequestExecutionService futureRequestExecutionService = futureRequestExecutionServices.get(httpClient);
        if(futureRequestExecutionService == null) {
            futureRequestExecutionService = new FutureRequestExecutionService(httpClient, executorService);
            futureRequestExecutionServices.put(httpClient, futureRequestExecutionService);
        }
                
        HttpRequestFutureTask<HttpResponse> task = futureRequestExecutionService.execute(uriRequest, httpContext, new WrapResponseHandler(responseHandler), new WrapCallback(responseHandler));
        return task;
    }

}
