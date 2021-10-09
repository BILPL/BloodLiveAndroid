package com.blood.live;

import android.app.Activity;
import android.content.Intent;
import android.util.Log;
import android.webkit.ValueCallback;

import com.google.gson.Gson;

import static android.content.ContentValues.TAG;

public class JavaScriptResult implements ValueCallback<String> {
    String methodType = "";

    Activity activity;
    public JavaScriptResult(String methodType, Activity context) {
        this.methodType = methodType;
        this.activity = context;
    }

    @Override
    public void onReceiveValue(String data) {
        try{
            if (this.methodType.toLowerCase().equals("blshare"))
            {
                Utility.ShareData(this.activity, data);
            }

        }catch(Exception ex){
            Log.e(TAG, "onReceiveValue: " );
        }

    }
}


