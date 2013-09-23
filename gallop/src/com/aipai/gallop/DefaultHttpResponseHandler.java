package com.aipai.gallop;

import com.aipai.gallop.http.Header;
import com.aipai.gallop.http.HttpRequest;
import com.aipai.gallop.http.HttpResponse;
import com.aipai.gallop.http.client.HttpResponseException;

import java.util.regex.Pattern;

public class DefaultHttpResponseHandler implements HttpResponseHandler {

    public DefaultHttpResponseHandler() {
    }

    public String[] getAllowedContentTypes()
    {
        return new String[]{};
    }
    
    @Override
    public void onReceived(HttpResponse response) {
        boolean foundAllowedContentType = isAllowedContentType(response, getAllowedContentTypes());
        if (!foundAllowedContentType) {
            onFailure(new HttpResponseException(response.getStatusLine().getStatusCode(), "Content-Type not allowed!"), response); 
        } else {
            onSuccess(response);
        }
    }

    @Override
    public void onSuccess(HttpResponse response) {
        
    }

    @Override
    public void onFailure(Throwable e, HttpResponse response) {
        
    }

    @Override
    public void onCancel(HttpRequest request) {

    }

    public static boolean isAllowedContentType(HttpResponse response, String[] allowedContentTypes) {
        Header[] contentTypeHeaders = response.getHeaders("Content-Type");
        if(contentTypeHeaders.length != 1) {
            // malformed/ambiguous HTTP Header, ABORT!
            return false;
        }
        Header contentTypeHeader = contentTypeHeaders[0];
        boolean foundAllowedContentType = false;
        for(String anAllowedContentType : allowedContentTypes) {
            if(Pattern.matches(anAllowedContentType, contentTypeHeader.getValue())) {
                foundAllowedContentType = true;
            }
        }
        if(!foundAllowedContentType) {
            //Content-Type not in allowed list, ABORT!
            return false;
        }
        
        return true;
    }    
}
