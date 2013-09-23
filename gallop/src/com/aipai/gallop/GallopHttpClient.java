package com.aipai.gallop;

import android.content.Context;
import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import com.aipai.gallop.http.Header;
import com.aipai.gallop.http.HttpEntity;
import com.aipai.gallop.http.HttpResponse;
import com.aipai.gallop.http.NameValuePair;
import com.aipai.gallop.http.client.ClientProtocolException;
import com.aipai.gallop.http.client.HttpClient;
import com.aipai.gallop.http.client.ResponseHandler;
import com.aipai.gallop.http.client.entity.UrlEncodedFormEntity;
import com.aipai.gallop.http.client.methods.HttpDelete;
import com.aipai.gallop.http.client.methods.HttpGet;
import com.aipai.gallop.http.client.methods.HttpPost;
import com.aipai.gallop.http.client.methods.HttpPut;
import com.aipai.gallop.http.client.methods.HttpUriRequest;
import com.aipai.gallop.http.client.protocol.HttpClientContext;
import com.aipai.gallop.http.client.utils.URIBuilder;
import com.aipai.gallop.http.concurrent.FutureCallback;
import com.aipai.gallop.http.impl.client.FutureRequestExecutionService;
import com.aipai.gallop.http.impl.client.HttpRequestFutureTask;
import com.aipai.gallop.http.protocol.HttpContext;


public class GallopHttpClient implements Closeable {
    private static final int MAX_THREADS = 10;
    protected HttpClient httpClient; 
    private final HttpClientContext httpContext;
    
    private ExecutorService executorService;
    private FutureRequestExecutionService futureRequestExecutionService;
    private final Map<Context, List<WeakReference<Future<?>>>> requestMap;
    private HttpResponseMessageHandler messageHandler;
        
    public GallopHttpClient(HttpClient client) {
        httpClient = client;
        httpContext = HttpClientContext.create();
        executorService = Executors.newFixedThreadPool(MAX_THREADS);
        futureRequestExecutionService = new FutureRequestExecutionService(httpClient, executorService);
        messageHandler = new HttpResponseMessageHandler();
        requestMap = new WeakHashMap<Context, List<WeakReference<Future<?>>>>();
    }
    
    /**
     * Get the underlying HttpContext instance.
     */
    public HttpContext getHttpContext() {
        return httpContext;
    }
        
    //
    // HTTP GET Requests
    //

    /**
     * Perform a HTTP GET request, without any parameters.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(String url, HttpResponseHandler responseHandler) {
        get(null, url, null, responseHandler);
    }

    /**
     * Perform a HTTP GET request with parameters.
     * @param url the URL to send the request to.
     * @param params additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(String url, List<NameValuePair> params, HttpResponseHandler responseHandler) {
        get(null, url, params, responseHandler);
    }

    /**
     * Perform a HTTP GET request without any parameters and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(Context context, String url, HttpResponseHandler responseHandler) {
        get(context, url, null, responseHandler);
    }

    /**
     * Perform a HTTP GET request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param params additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void get(Context context, String url, List<NameValuePair>  params, HttpResponseHandler responseHandler) {
        get(context, url, null, params, responseHandler);
    }
    
    /**
     * Perform a HTTP GET request and track the Android Context which initiated
     * the request with customized headers
     * 
     * @param url the URL to send the request to.
     * @param headers set headers only for this request
     * @param params additional GET parameters to send with the request.
     * @param responseHandler the response handler instance that should handle
     *        the response.
     */
    public void get(Context context, String url, Header[] headers, List<NameValuePair> params, HttpResponseHandler responseHandler) {
        try {
            URIBuilder builder = new URIBuilder(url);
            if (params != null) {
                for(NameValuePair item : params) {
                    builder.setParameter(item.getName(), item.getValue());
                }
            }
            
            HttpUriRequest request = new HttpGet(builder.build());
            if(headers != null) request.setHeaders(headers);
            sendRequest(httpClient, httpContext, request, null, responseHandler, context);
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }        
    }

    //
    // HTTP POST Requests
    //

    /**
     * Perform a HTTP POST request, without any parameters.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(String url, HttpResponseHandler responseHandler) {
        post(null, url, null, responseHandler);
    }

    /**
     * Perform a HTTP POST request with parameters.
     * @param url the URL to send the request to.
     * @param params additional POST parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(String url, List<NameValuePair> params, HttpResponseHandler responseHandler) {
        post(null, url, params, responseHandler);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param params additional POST parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(Context context, String url, List<NameValuePair> params, HttpResponseHandler responseHandler) {        
        try {
            HttpEntity entity = new UrlEncodedFormEntity(params);
            post(context, url, entity, null, responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param entity a raw {@link HttpEntity} to send with the request, for example, use this to send string/json/xml payloads to a server by passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void post(Context context, String url, HttpEntity entity, String contentType, HttpResponseHandler responseHandler) {
        post(context, url, null, entity, contentType, responseHandler);
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request. Set headers only for this request
     * 
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set headers only for this request
     * @param params additional POST parameters to send with the request.
     * @param contentType the content type of the payload you are sending, for
     *        example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle
     *        the response.
     */
    public void post(Context context, String url, Header[] headers, List<NameValuePair> params, String contentType,
            HttpResponseHandler responseHandler) {
        try {
            HttpEntity entity = new UrlEncodedFormEntity(params);
            post(context, url, headers, entity, contentType, responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform a HTTP POST request and track the Android Context which initiated
     * the request. Set headers only for this request
     *
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set headers only for this request
     * @param entity a raw {@link HttpEntity} to send with the request, for
     *        example, use this to send string/json/xml payloads to a server by
     *        passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for
     *        example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle
     *        the response.
     */
    public void post(Context context, String url, Header[] headers, HttpEntity entity, String contentType,
            HttpResponseHandler responseHandler) {
        HttpPost request = new HttpPost(url);
        request.setEntity(entity);
        if(headers != null) request.setHeaders(headers);
        sendRequest(httpClient, httpContext, request, contentType, responseHandler, context);
    }

    //
    // HTTP PUT Requests
    //

    /**
     * Perform a HTTP PUT request, without any parameters.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(String url, HttpResponseHandler responseHandler) {
        put(null, url, null, responseHandler);
    }

    /**
     * Perform a HTTP PUT request with parameters.
     * @param url the URL to send the request to.
     * @param params additional PUT parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(String url, List<NameValuePair> params, HttpResponseHandler responseHandler) {
        put(null, url, params, responseHandler);
    }

    /**
     * Perform a HTTP PUT request and track the Android Context which initiated the request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param params additional PUT parameters or files to send with the request.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(Context context, String url, List<NameValuePair> params, HttpResponseHandler responseHandler) {
        try {
            HttpEntity entity = new UrlEncodedFormEntity(params);
            put(context, url, entity, null, responseHandler);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * Perform a HTTP PUT request and track the Android Context which initiated the request.
     * And set one-time headers for the request
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param entity a raw {@link HttpEntity} to send with the request, for example, use this to send string/json/xml payloads to a server by passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(Context context, String url, HttpEntity entity, String contentType, HttpResponseHandler responseHandler) {
        put(context, url, null, entity, contentType, responseHandler);
    }
    
    /**
     * Perform a HTTP PUT request and track the Android Context which initiated the request.
     * And set one-time headers for the request
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set one-time headers for this request
     * @param entity a raw {@link HttpEntity} to send with the request, for example, use this to send string/json/xml payloads to a server by passing a {@link org.apache.http.entity.StringEntity}.
     * @param contentType the content type of the payload you are sending, for example application/json if sending a json payload.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void put(Context context, String url,Header[] headers, HttpEntity entity, String contentType, HttpResponseHandler responseHandler) {
        HttpPut request = new HttpPut(url);
        request.setEntity(entity);
        if(headers != null) request.setHeaders(headers);
        sendRequest(httpClient, httpContext, request, contentType, responseHandler, context);
    }

    //
    // HTTP DELETE Requests
    //

    /**
     * Perform a HTTP DELETE request.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void delete(String url, HttpResponseHandler responseHandler) {
        delete(null, url, responseHandler);
    }

    /**
     * Perform a HTTP DELETE request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void delete(Context context, String url, HttpResponseHandler responseHandler) {
        delete(context, url, null, responseHandler);
    }
    
    /**
     * Perform a HTTP DELETE request.
     * @param context the Android Context which initiated the request.
     * @param url the URL to send the request to.
     * @param headers set one-time headers for this request
     * @param responseHandler the response handler instance that should handle the response.
     */
    public void delete(Context context, String url, Header[] headers, HttpResponseHandler responseHandler) {
        final HttpDelete delete = new HttpDelete(url);
        if(headers != null) delete.setHeaders(headers);
        sendRequest(httpClient, httpContext, delete, null, responseHandler, context);
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

        private HttpResponseHandler handler;
        public WrapCallback(HttpResponseHandler responseHandler) {
            super();
            handler = responseHandler;
        }

        @Override
        public void cancelled() {
            GallopHttpClient.this.messageHandler.sendCancelMessage(handler);
        }

        @Override
        public void completed(HttpResponse response) {
            GallopHttpClient.this.messageHandler.sendSuccessMessage(handler, response);
        }

        @Override
        public void failed(Exception e) {
            GallopHttpClient.this.messageHandler.sendFailureMessage(handler, e);
        }
    }

    protected void sendRequest(HttpClient client, HttpContext httpContext, HttpUriRequest uriRequest, String contentType, HttpResponseHandler responseHandler, Context context) {
        if(contentType != null) {
            uriRequest.addHeader("Content-Type", contentType);
        }

        HttpRequestFutureTask<HttpResponse> task = futureRequestExecutionService.execute(uriRequest, httpContext, new WrapResponseHandler(responseHandler), new WrapCallback(responseHandler));

        if(context != null) {
            // Add request to request map
            List<WeakReference<Future<?>>> requestList = requestMap.get(context);
            if(requestList == null) {
                requestList = new LinkedList<WeakReference<Future<?>>>();
                requestMap.put(context, requestList);
            }

            requestList.add(new WeakReference<Future<?>>(task));
        }
    }


    @Override
    public void close() throws IOException {
    }
}
