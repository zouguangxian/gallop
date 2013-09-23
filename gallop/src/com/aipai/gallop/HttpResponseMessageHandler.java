package com.aipai.gallop;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

import com.aipai.gallop.http.HttpResponse;

public class HttpResponseMessageHandler extends Handler {
    protected static final int SUCCESS_MESSAGE = 0;
    protected static final int FAILURE_MESSAGE = 1;
    protected static final int CANCEL_MESSAGE = 2;
    
    public HttpResponseMessageHandler() {
    }

    public HttpResponseMessageHandler(Callback callback) {
        super(callback);
    }

    public HttpResponseMessageHandler(Looper looper) {
        super(looper);
    }

    public HttpResponseMessageHandler(Looper looper, Callback callback) {
        super(looper, callback);
    }

    protected void sendSuccessMessage(HttpResponseHandler handler, HttpResponse response) {
        sendMessage(obtainMessage(SUCCESS_MESSAGE, new Object[]{handler, response}));
    }

    protected void sendFailureMessage(HttpResponseHandler handler, Throwable e) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{handler, e}));
    }
    
    protected void sendCancelMessage(HttpResponseHandler handler) {
        sendMessage(obtainMessage(FAILURE_MESSAGE, new Object[]{handler}));
    }
    
    protected void handleSuccessMessage(HttpResponseHandler handler, HttpResponse response) {
        handler.onReceived(response);
    }

    protected void handleFailureMessage(HttpResponseHandler handler, Throwable e) {
        handler.onFailure(e, null);
    }
    
    protected void handleCancelMessage(HttpResponseHandler handler) {
        handler.onCancel(null);
    }
    
    @Override
    // Methods which emulate android's Handler and Message methods
    public void handleMessage(Message msg) {
        HttpResponseHandler handler;
        Object[] objs = (Object[])msg.obj;
        handler = (HttpResponseHandler)objs[0];
        switch(msg.what) {
            case SUCCESS_MESSAGE:
                HttpResponse response;
                response = (HttpResponse)objs[1];
                handleSuccessMessage(handler, response);
                break;
            case FAILURE_MESSAGE:
                Throwable exception;
                exception = (Throwable)objs[1];
                handleFailureMessage(handler, exception);
                break;
            case CANCEL_MESSAGE:
                handleCancelMessage(handler);
                break;
        }
    }

}