
package com.aipai.gallop;

import com.aipai.gallop.http.HttpRequest;
import com.aipai.gallop.http.HttpResponse;

public interface HttpResponseHandler {

    /**
     * onReceived
     * @param response
     */
    public void onReceived(HttpResponse response);

    /**
     * onSuccess
     * @param response
     */
    public void onSuccess(HttpResponse response);
    
    /**
     * onFailure
     * @param e
     * @param response
     */
    public void onFailure(Throwable e, HttpResponse response);

    /**
     * onCancel
     * @param request
     */
    public void onCancel(HttpRequest request);

}
