package com.example.yak2.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.ScanFilter;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;
import android.view.Gravity;
import android.widget.RemoteViews;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yak2.MainActivity;
import com.example.yak2.R;
import com.example.yak2.sqllite.DBLoader;
import com.example.yak2.utils.model.Setup;
import com.steerpath.sdk.location.BluetoothServices;
import com.steerpath.sdk.location.Location;
import com.steerpath.sdk.location.LocationListener;
import com.steerpath.sdk.location.LocationRequest;
import com.steerpath.sdk.location.LocationServices;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class LocationForegroundService extends Service implements LocationListener {

    private static final String TAG = LocationForegroundService.class.getSimpleName();
    private final static String FOREGROUND_CHANNEL_ID = "foreground_channel_id";
    private NotificationManager mNotificationManager;
    private Handler handler;
    private int count = 0;
    private static int stateService = Constants.STATE_SERVICE.NOT_CONNECTED;

    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    public List<String> allreq ;

    public ArrayList<Setup> tables;
    public String userName;
    public String userNumber;
    public DBLoader database_loader;

    public LocationForegroundService() {

    }

    private void sendPostdataToAWSYak() {
        for (String s: allreq) {
            sendRequestBodyYak(s);
        }
    }

    private void sendRequestBodyYak(String body)
    {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            final String mRequestBody = body;

            StringRequest stringRequest = new StringRequest(com.android.volley.Request.Method.POST, "https://gdh6jlmrij.execute-api.ap-southeast-1.amazonaws.com/test/add", new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {
                    Log.i("LOG_RESPONSE", response);
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("LOG_RESPONSE", error.toString());
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
                protected Response<String> parseNetworkResponse(NetworkResponse response) {
                    String responseString = "";
                    if (response != null) {
                        responseString = String.valueOf(response.statusCode);
                        VolleyLog.wtf(responseString);
                    }
                    VolleyLog.wtf("Good. welldone");
                    return Response.success(responseString, HttpHeaderParser.parseCacheHeaders(response));
                }
            };

            requestQueue.add(stringRequest);
        } catch (Exception e) {
            VolleyLog.wtf(e.toString());

        }
    }

    private void sendPostdataToAWS(String bufferString) {
        sendRequestBody(bufferString);
    }

    private void sendRequestBody(String body)
    {
        try {
            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put ("userId", userNumber);
            jsonBody.put("locationData", body);
//            final String requestBody = jsonBody.toString();

            JsonObjectRequest jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                    "http://172.20.10.4:8080/api/location", jsonBody, new Response.Listener<JSONObject>() {
                @Override
                public void onResponse(JSONObject response) {
                    Log.i("NOK", String.valueOf(response));
                }
            }, new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e("NOK", error.toString());
                }
            }) {
                @Override
                public String getBodyContentType() {
                    return "application/json; charset=utf-8";
                }
            };

            requestQueue.add(jsonRequest);
        } catch (Exception e) {
            VolleyLog.wtf(e.toString());
        }
    }

    private Toast tt;
    private int counter = 0;
    @Override
    public void onLocationChanged(Location location) {

        // When bluetooth or location services has just been turned off, there might still be
        // a Location update event coming from the pipeline.
        // These checks has no other purpose but to keep infoText reflecting the state of BL or Location Services.
        counter += 1;
        long now = System.currentTimeMillis();
        String somTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000000").format(new java.util.Date (now)).replace(" ","T")+"Z";

        // When bluetooth or location services has just been turned off, there might still be
        // a Location update event coming from the pipeline.
        // These checks has no other purpose but to keep infoText reflecting the state of BL or Location Services.

        if (BluetoothServices.isBluetoothOn() && LocationServices.isLocationOn(this)) {
            String newLine = "\n";
            Log.i ("NOK", "inside updated");

            JSONObject bufferJson = new JSONObject();
            try {
                bufferJson.put("id", now);
                bufferJson.put("userId", userNumber);
                bufferJson.put("longitude", location.getLongitude());
                bufferJson.put("latitude", location.getLatitude());
                bufferJson.put("floorIndex", location.getFloorIndex());
                bufferJson.put("buildingId", location.getBuildingId());
            } catch (JSONException e) {
                e.printStackTrace();
            }

            Log.i ("NOK", bufferJson.toString());
            sendPostdataToAWS(bufferJson.toString());

            // below is Yak's logic
            if (counter % 10 == 0) {
                String buffer = "{\"id\":\"" + now + "\",\"data\":\"" + "" + somTime + "," + location.getProvider() + "," + userName+userNumber + "," + location.getLongitude() + "," + location.getLatitude() + "," + ((int) location.getFloorIndex() + 1) + "," + location.getAccuracy() + "" + "\"" + "}";

                allreq.add(buffer);
            }

            if (counter % 10 == 0)
            {
                sendPostdataToAWSYak();
                allreq.clear();

                Toast.makeText(getApplicationContext(), "data updated from background", Toast.LENGTH_SHORT).show();
            }
            else
            {
                // Toast.makeText(getApplicationContext(), "location updated",Toast.LENGTH_SHORT).show();
                // Toast.makeText(getApplicationContext(), "Long:"+ location.getLongitude() + ", Lat:" + location.getLatitude(),Toast.LENGTH_SHORT).show();
            }

        }
    }

    private static LocationRequest createLocationRequestWithGpsEnabled() {
        LocationRequest request = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // GPS threshold determines the minimum accuracy that GPS must have in order for automatic bluetooth to GPS switch to happen.
        request.setGpsThreshold(8);
        return request;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean checkBluetooth() {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        ScanFilter filter = builder.build();

        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        }

        return true;
    }

    private boolean checkLocationService() {
        if (!LocationServices.isLocationOn(this)) {
            // you may want to show some kind of "Enable Location Services? Yes/No" - dialog before going to Settings
            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
            return false;
        }

        return true;
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void startPositioning() {
        ScanFilter.Builder builder = new ScanFilter.Builder();
        ScanFilter filter = builder.build();

        if (checkBluetooth()) {
            if (checkLocationService()) {
                if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("location_request", false)) {
                    LocationServices.getFusedLocationProviderApi().requestLocationUpdates(createLocationRequestWithGpsEnabled(), this);
                } else {
                    LocationServices.getFusedLocationProviderApi().requestLocationUpdates(this);
                }

                Toast.makeText(getApplicationContext(),getString(R.string.waiting_location), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO: Return the communication channel to the service.
        return null;
    }

    public PowerManager.WakeLock WL;
    PowerManager pM ;

    @Override
    public void onCreate() {
         pM = (PowerManager)getSystemService(Context.POWER_SERVICE);
        WL = pM.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "myapp:mywakeLogTag");

        WL.acquire();
        super.onCreate();

        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        stateService = Constants.STATE_SERVICE.NOT_CONNECTED;

        Toast.makeText(this, "Safety App Foreground started ....", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDestroy() {
        stateService = Constants.STATE_SERVICE.NOT_CONNECTED;

        super.onDestroy();
        // WL.release();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        allreq = new ArrayList<String>();
        database_loader = new DBLoader(this);
        tables = database_loader.loadSetups();
        userName = tables.get(0).getName();
        userNumber = tables.get(0).getUser_num();

        if (intent == null) {
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // if user starts the service
        switch (intent.getAction()) {
            case Constants.ACTION.START_ACTION:
                // Log.d(TAG, "Received user starts foreground intent");
                startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                connect();
                break;
            case Constants.ACTION.STOP_ACTION:
                disconnectFGS();
                stopForeground(true);
                stopSelf();

                break;
            default:
                stopForeground(true);
                stopSelf();
        }

        return START_STICKY;
    }

    // its connected, so change the notification text
    private void connect() {
        // after 10 seconds its connected
        new Handler().postDelayed(
                new Runnable() {
                    public void run() {

                        Toast.makeText(getApplicationContext(),"Connected!", Toast.LENGTH_SHORT).show();
                        stateService = Constants.STATE_SERVICE.CONNECTED;
                        startForeground(Constants.NOTIFICATION_ID_FOREGROUND_SERVICE, prepareNotification());
                        startPositioning();
                    }
                }, 2000);
    }

    private void disconnectFGS() {
        Toast.makeText(getApplicationContext(), "Safety App Foreground Stopped",Toast.LENGTH_LONG).show();
    }

    @SuppressLint("WrongConstant")
    private Notification prepareNotification() {
        // handle build version above android oreo
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
                mNotificationManager.getNotificationChannel(FOREGROUND_CHANNEL_ID) == null) {
            CharSequence name = "Service";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(FOREGROUND_CHANNEL_ID, name, importance);
            channel.enableVibration(false);
            mNotificationManager.createNotificationChannel(channel);
        }

        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setAction(Constants.ACTION.MAIN_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);

        // if min sdk goes below honeycomb
        /* if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.HONEYCOMB) {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        } else {
            notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }*/

        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        // make a stop intent
        Intent stopIntent = new Intent(this, LocationForegroundService.class);
        stopIntent.setAction(Constants.ACTION.STOP_ACTION);
        PendingIntent pendingStopIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        RemoteViews remoteViews = new RemoteViews(getPackageName(), R.layout.notification);
        remoteViews.setOnClickPendingIntent(R.id.btn_stop, pendingStopIntent);

        // if it is connected
        switch(stateService) {
            case Constants.STATE_SERVICE.NOT_CONNECTED:
                remoteViews.setTextViewText(R.id.tv_state, "Safety app disconnected");
                break;
            case Constants.STATE_SERVICE.CONNECTED:
                remoteViews.setTextViewText(R.id.tv_state, "Safety app is running");
                break;
        }

        // notification builder
        NotificationCompat.Builder notificationBuilder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationBuilder = new NotificationCompat.Builder(this, FOREGROUND_CHANNEL_ID);
        } else {
            notificationBuilder = new NotificationCompat.Builder(this);
        }
        notificationBuilder
                .setContent(remoteViews)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setOnlyAlertOnce(true)
                .setOngoing(true)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            notificationBuilder.setVisibility(Notification.VISIBILITY_PUBLIC);
        }

        return notificationBuilder.build();
    }
}