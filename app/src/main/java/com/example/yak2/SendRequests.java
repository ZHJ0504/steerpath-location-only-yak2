package com.example.yak2;

import android.content.Context;
import android.util.Base64;
import android.util.Log;
import android.webkit.WebChromeClient;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;




import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;


public class SendRequests {

    private Context context;

    public String url;
    public String returnString;
    public JSONObject returnJson;

    private int requestMethod;


    public SendRequests(Context context, String url, String method) {
        this.context = context;
        this.url = url;

        if(method=="POST") requestMethod = Request.Method.POST;
        else requestMethod = Request.Method.GET;
    }


    public void sendRequest(String body_string_get_request)
    {

        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this.context);

            final String mRequestBody = body_string_get_request;

            VolleyLog.wtf(body_string_get_request);


            StringRequest stringRequest = new StringRequest(requestMethod,
                    url,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("LOG_RESPONSE", response);

                            try {
                                returnJson = new JSONObject(response);
                                returnString = returnJson.toString();

                                Toast.makeText(context, "Data added successfully!", Toast.LENGTH_SHORT).show();
                            } catch (JSONException e) {
                                returnString = response.toString();

                                Toast.makeText(context, returnString, Toast.LENGTH_LONG).show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.e("LOG_RESPONSE", error.toString());

                            Toast.makeText(context, error.toString(), Toast.LENGTH_LONG).show();

                        }
                    }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }

                @Override
                public byte[] getBody() throws AuthFailureError {
                    try {
                        return mRequestBody == null ? null : mRequestBody.getBytes("utf-8");
                    } catch (UnsupportedEncodingException uee) {
                        VolleyLog.wtf("Unsupported Encoding while trying to get the bytes of %s using %s", mRequestBody, "utf-8");
                        return null;
                    }
                }

                @Override
                protected Map<String, String> getParams() throws AuthFailureError {
                    Map<String, String> params = new HashMap<>();

                    return params;
                }



            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            VolleyLog.wtf(e.toString());
        }
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
