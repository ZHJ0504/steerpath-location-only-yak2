package com.example.yak2;

import android.content.Context;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONObject;

public class WebAppInterface {
    private Context context;
    public JSONObject input_data;
    public String userid;
    public String experimentid;
    public String userfeedbackData;

    public WebAppInterface(Context context, JSONObject obj, String userid, String experimentid) {
        this.context = context;
        this.input_data = obj;
        this.experimentid = experimentid;
        this.userid = userid;
    }

    @JavascriptInterface
    public String alert_data(){
//        Toast.makeText(context, input_data.toString(), Toast.LENGTH_LONG).show();
        return input_data.toString();

    }

    @JavascriptInterface
    public String getExperimentData(){

        String experimentData = "{\"res_body\":{\"user_name\":\""+userid+"\",\"experiment_id\":\""+experimentid+"\"}}";
        return experimentData;

    }

    @JavascriptInterface
    public void Toast_userfeedback(String userFeedbackString)
    {
        Toast.makeText(context, userFeedbackString, Toast.LENGTH_LONG).show();
//        ٍsend to influxdb

        SendRequests thisreq = new SendRequests(context,"https://gdh6jlmrij.execute-api.ap-southeast-1.amazonaws.com/getUserInputs/res-yak-getuserinfo", "POST");
        thisreq.sendRequest(userFeedbackString);
    }

    @Override
    public int hashCode() {
        return super.hashCode();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        return super.equals(obj);
    }

    @NonNull
    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    @NonNull
    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    protected void finalize() throws Throwable {
        super.finalize();
    }
}
