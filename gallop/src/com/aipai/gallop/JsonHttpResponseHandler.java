
package com.aipai.gallop;

import com.aipai.gallop.http.HttpRequest;
import com.aipai.gallop.http.HttpResponse;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class JsonHttpResponseHandler extends DefaultHttpResponseHandler {
    private static String[] mAllowedContentTypes = new String[] {
            "application/json",
    };

    public JsonHttpResponseHandler() {
    }

    @Override
    public void onFailure(Throwable e, HttpResponse response) {
    }

    @Override
    public void onCancel(HttpRequest request) {
    }

    @Override
    public void onSuccess(HttpResponse response) {
        // refer:
        // http://stackoverflow.com/questions/2845599/how-do-i-parse-json-from-a-java-httpresponse/2845612#2845612
        try {
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
            StringBuilder builder = new StringBuilder();
            for (String line = null; (line = reader.readLine()) != null;) {
                builder.append(line).append("\n");
            }
            JSONTokener tokener = new JSONTokener(builder.toString());
            JSONObject retval = new JSONObject(tokener);
            this.onSuccess(retval);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalStateException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    public void onSuccess(JSONObject json)
    {
    }

    @Override
    public String[] getAllowedContentTypes()
    {
        return mAllowedContentTypes;
    }
}
