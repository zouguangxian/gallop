package com.aipai.gallop;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import com.aipai.gallop.http.HttpEntity;
import com.aipai.gallop.http.HttpResponse;
import com.aipai.gallop.http.StatusLine;
import com.aipai.gallop.http.util.EntityUtils;

import java.io.IOException;

public class ImageHttpResponseHandler extends DefaultHttpResponseHandler {

    private static String[] mAllowedContentTypes = new String[] {
        "image/jpeg",
        "image/png"
    };
    
    public ImageHttpResponseHandler() {
    }
    
    @Override
    public void onSuccess(HttpResponse response) {
        StatusLine statusLine = response.getStatusLine();
        int statusCode = statusLine.getStatusCode();
        if (statusCode == 200) {
            HttpEntity entity = response.getEntity();            
            try {
                byte[] bytes = EntityUtils.toByteArray(entity);
                Bitmap bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                this.onSuccess(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public String[] getAllowedContentTypes() {
        return mAllowedContentTypes;
    }
    
    
    public void onSuccess(Bitmap bitmap) {
        
    }

}
