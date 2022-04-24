package com.example.yak2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanFilter;
import android.content.BroadcastReceiver;
import android.Manifest;

import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import android.view.Gravity;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.JsonRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.example.yak2.examples.PositioningActivity;
import com.example.yak2.services.Constants;
import com.example.yak2.services.LocationForegroundService;
import com.example.yak2.sqllite.DBLoader;
import com.example.yak2.utils.PermissionUtils;
import com.example.yak2.utils.SettingsHelper;
import com.example.yak2.utils.model.Setup;
//import com.squareup.haha.perflib.Main;
//import com.squareup.haha.perflib.Main;
import com.google.gson.JsonObject;
import com.steerpath.sdk.assettracking.AssetGateway;
import com.steerpath.sdk.location.BluetoothServices;
import com.steerpath.sdk.location.FusedLocationProviderApi;
import com.steerpath.sdk.location.Location;
import com.steerpath.sdk.location.LocationListener;
import com.steerpath.sdk.location.LocationRequest;
import com.steerpath.sdk.location.LocationServices;
import com.steerpath.sdk.meta.MetaFeature;
import com.steerpath.sdk.meta.MetaLoader;
import com.steerpath.sdk.meta.MetaQuery;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.DataOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.altbeacon.bluetooth.BluetoothMedic;

public class MainActivity extends AppCompatActivity implements LocationListener {

    WebView webpage;

    private boolean isBluetoothEnabled = false;
    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;

    Button buttonStartLocation;
    Button buttonChangeId;
    Button ModeBtn;

    private Intent startIntent2;

    EditText experimentName;
    EditText experimentNumber;
    String expNameString ;
    String experimentNumberString = "00";

    String customHTML;
    String testString;
    String full_user_id;

    String locationDataHTMLString;
    WebView locationDataWebView;

    JSONObject obj_getuserinfo;

    private ArrayList<Setup> apikeys;
    private DBLoader dbLoader;
    private static Setup currentKey;
    private SharedPreferences sp;
    private static ArrayList<MetaFeature> newBuildings;
    private boolean isLoading = false;
    private static boolean clientChanged = false;

    private String key;
    private String region;

    BluetoothMedic medic;

    Handler mHandler;
    private String androidId;

    private RequestQueue mRequestQueue;
    private StringRequest stringRequest;
    public List<String> allreq ;

    private Toast tt;
    private int counter = 0;
    private String buffer;

    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_ENABLE_BT = 2;

    private boolean location_permission_enabled = false;

    private String info;
    private String user_info_repsonse_string = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
            webpage = findViewById(R.id.mainWebPageView);
            webpage.getSettings().setJavaScriptEnabled(true);
            webpage.setWebViewClient(new WebViewClient());

            buttonStartLocation = (Button) findViewById(R.id.buttonStartLocation);
            buttonChangeId = findViewById(R.id.buttonChangeId);
            ModeBtn = findViewById(R.id.modeBtn);

            experimentName = (EditText) findViewById(R.id.editTextTextPersonName);
            expNameString = experimentName.getText().toString();
            testString = getString(R.string.testString);

            experimentNumber = (EditText) findViewById(R.id.editTextTextPersonNumber);
            experimentNumberString = experimentNumber.getText().toString();
            locationDataWebView = findViewById(R.id.locationDataWebView);
            locationDataHTMLString = "<html><body><pstyle=\"color:black; font-size:10pt\"> Press Change ID to change your id <br> Press start to start location </p></body></html>";
            locationDataWebView.loadData(locationDataHTMLString, "text/html", "UTF-8");

            key = "eyJhbGciOiJSUzI1NiJ9.eyJpYXQ6IjoxNTUyMzk2NDk1LCJqdGkiOiI2NDU4OGM5Zi1lYTA4LTQ5NGMtYjgwMi04N2ZmNzcyNjg3YzAiLCJzY29wZXMiOiJ2Mi1lYWY2ODc3OC05ZGEzLTRhNWUtYWQ4NC05ZDUwNDNhMDQ4YWYtcHVibGlzaGVkOnIiLCJzdWIiOiJ2Mi1lYWY2ODc3OC05ZGEzLTRhNWUtYWQ4NC05ZDUwNDNhMDQ4YWYifQ.lWGDm-gZda54YItEtzZEuxv8vVy24FfRBzY25sWyqPtA3J3vBFCTz1E-8bam1-WAR2MEkGRRNCfyV1ZQ1NHU3i2mDDLi2NcogGm3ESO1kcXmwj-LiyWUHN3e0IZji0CRCtHHIS6Z4uavQPGpNXzOFq2yX40KoGQuvupHxqFarCb4XGpYiiws3H08cIIaoC70dKNCGthnWajBvXjhENXSG4QzoUutj8TV3OEaohFx2xfS_i7jBYxnauhB86GduVNkCjvXwM5bhknrf6OgFrHMXhaaf2nk82GSElM8y0nGQ2Y7G9KqhCyU2_8ifzeqqno8gvWQiedGLm0uTpL0bZMeNg";
            region = "AP1";

            locationDataHTMLString = "<html><body style='font-size:xx-small; font-family: monospace, monospace;'>START DEBUGGING :: <br>";

            try {
                dbLoader = new DBLoader(this);
                apikeys = dbLoader.loadSetups();
                currentKey = apikeys.get(0);
                experimentName.setText(currentKey.getName());
                experimentNumber.setText(currentKey.getUser_num());
                full_user_id = currentKey.getName() + currentKey.getUser_num();

                Toast.makeText(this, "User Id - " + currentKey.getUser_num() + "", Toast.LENGTH_LONG).show();

                sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                DemoApplication.configureClient(getApplicationContext(), currentKey, sp.getBoolean("monitor", false));
                LocalBroadcastManager.getInstance(this).registerReceiver(sdkReadyBroadcastReceiver,
                        new IntentFilter(DemoApplication.BROADCAST_SDK_READY));

            } catch (Exception ex) {
                try {
                    String name_ = "test";

                    dbLoader = new DBLoader(this);
                    dbLoader.deleteAll();
                    Setup newSetup = new Setup()
                            .accesToken(key)
                            .name(name_)
                            .region(region)
                            .userNumber("example@gmail.com");
                    dbLoader.addSetup(newSetup);
                    apikeys = dbLoader.loadSetups();
                    currentKey = apikeys.get(0);

                    experimentName.setText(currentKey.getName());
                    experimentNumber.setText(currentKey.getUser_num());

                    full_user_id = currentKey.getUser_num();
                    Toast.makeText(this, full_user_id + " -- 2", Toast.LENGTH_LONG).show();

                    webpage.clearCache(true);
                    webpage.clearHistory();
                    locationDataHTMLString += ex.toString() + "<br> p---- >>>";
                    webpage.loadData(locationDataHTMLString, "text/html", "UTF-8");
                    full_user_id = currentKey.getUser_num();

                    sp = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                    DemoApplication.configureClient(getApplicationContext(), currentKey, sp.getBoolean("monitor", false));
                    LocalBroadcastManager.getInstance(this).registerReceiver(sdkReadyBroadcastReceiver,
                            new IntentFilter(DemoApplication.BROADCAST_SDK_READY));

                }
                catch (Exception exxx)
                {
                    webpage.clearCache(true);
                    Log.e("Gella", exxx.toString(),exxx.fillInStackTrace());

                    webpage.clearHistory();
                    locationDataHTMLString +="<span style'color:green'>"+exxx.toString()+"</span></body></html>";
                    webpage.loadData(locationDataHTMLString, "text/html", "UTF-8");
                }
        }
    }

    private BroadcastReceiver sdkReadyBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            isLoading = false;
            MainActivity.this.loadBuildings();
            clientChanged = true;
            if (DemoApplication.USES_DEFAULT_CONFIG) DemoApplication.USES_DEFAULT_CONFIG = false;
        }
    };

    public void loadBuildings() {
        MetaQuery.Builder query = new MetaQuery.Builder(this, MetaQuery.DataType.BUILDINGS);
        MetaLoader.load(query.build(), result -> {
            if (newBuildings != null) {
                newBuildings.clear();
            }
            newBuildings = result.getMetaFeatures();
            isLoading = false;

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        IntentFilter filter = new IntentFilter();
        filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        filter.addAction("android.location.PROVIDERS_CHANGED");
        registerReceiver(receiver, filter);

        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            String[] permissions = PermissionUtils.getMissingPermissions(this);
            if (permissions.length > 0) {
                PermissionUtils.requestPermissions(this, permissions, REQUEST_PERMISSIONS);

                location_permission_enabled = false;

            } else {
                location_permission_enabled = true;
            }

        } else {
            location_permission_enabled = false;
        }

        // set the UI text
        setOperatorModeText();

        ModeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i ("NOK", "click the mode button");
                String modeStatus = "ON";

                if (ModeBtn.getText().equals("ON")) {
                    modeStatus = "ON";
                } else {
                    modeStatus = "OFF";
                }

                SharedPreferences sharedPreferences = getSharedPreferences("DataSharedPref", MODE_PRIVATE);
                SharedPreferences.Editor dataEditor = sharedPreferences.edit();

                dataEditor.putString("modeManageBtn", modeStatus.toString());
                dataEditor.commit();

                setOperatorModeText();
            }
        });

        buttonStartLocation.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (checkLocationPermission())
                {
                    Log.i ("NOK", "click start the button");
                    expNameString= experimentName.getText().toString();
                    experimentNumberString = experimentNumber.getText().toString();

                    Log.i ("NOK", expNameString);
                    Log.i ("NOK", String.valueOf(buttonStartLocation.getText()));

                    if (buttonStartLocation.getText().equals(getString(R.string.startLocation)))
                    {
                        Log.i ("NOK", "after clicked the start button");
                        buttonStartLocation.setText(R.string.stopLocation);
                        buttonChangeId.setEnabled(false);
                        checkBluetooth();
                        checkLocationService();

                        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
                            String[] permissions = PermissionUtils.getMissingPermissions(getApplicationContext());
                            if (permissions.length > 0) {
                                PermissionUtils.requestPermissions(getParent(), permissions, REQUEST_PERMISSIONS);
                            } else {
                                // I think this is the default (should run location)
                                try {
                                    Thread.sleep(000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                DemoApplication.USES_DEFAULT_CONFIG = true;
                                startPositioning();

                                startIntent2 = new Intent(MainActivity.this, LocationForegroundService.class);
                                startIntent2.setAction(Constants.ACTION.START_ACTION);
                                startService(startIntent2);

                                // save temporary data


                            }

                        } else {
                            webpage.clearCache(true);
                            webpage.clearHistory();
                            customHTML = "<html><body style='color:red; font-size:10pt'> BLE not supported</body></html>";
                            webpage.loadData(customHTML, "text/html", "UTF-8");
                        }

                    }
                    else if  (buttonStartLocation.getText().equals(getString(R.string.stopLocation)))
                    {
                        buttonStartLocation.setText(R.string.startLocation);
                        buttonChangeId.setEnabled(true);

                        stopService(startIntent2);
                        // unregisterReceiver(receiver);

                        // When FusedLocationProviderApi has any registered LocationListener on it, positioning engine remains alive.
                        // Meaning it will keep bluetooth scanner alive and will drain battery.
                        // Therefore, when app is backgrounded, it is recommended to call FusedLocationProviderApi.Api.get().removeLocationUpdates()
                        // for each LocationListener you have previously registered. Unless you want to track user's movements even if when app has backgrounded.
                        LocationServices.getFusedLocationProviderApi().removeLocationUpdates(MainActivity.this);
                        locationDataWebView.clearCache(true);
                        locationDataWebView.clearHistory();
                        locationDataHTMLString = "Location stopped <br> Press Change ID to change your id <br> Press start to start location again.";
                        locationDataWebView.loadData(locationDataHTMLString, "text/html", "UTF-8");
                    }
                }
                else
                {
                    locationDataWebView.clearCache(true);
                    locationDataWebView.clearHistory();
                    locationDataHTMLString = "Location stopped <br> Press Change ID to change your id <br> Press start to start location again.";
                    locationDataWebView.loadData(locationDataHTMLString, "text/html", "UTF-8");
                }
            }
        });

        buttonChangeId.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                if (buttonChangeId.getText().equals(getString(R.string.changeId)))
                {
                    buttonChangeId.setText(R.string.saveId);
                    buttonStartLocation.setEnabled(false);

                    experimentName.setEnabled(true);
                    experimentNumber.setEnabled(true);
                }
                else if (buttonChangeId.getText().equals(getString(R.string.saveId)))
                {
                    buttonChangeId.setText(R.string.changeId);
                    buttonStartLocation.setEnabled(true);

                    experimentName.setEnabled(false);
                    experimentNumber.setEnabled(false);

                    expNameString= experimentName.getText().toString();
                    experimentNumberString = experimentNumber.getText().toString();

                    full_user_id = expNameString+experimentNumberString;

                    validateExperimentName(expNameString);

                    locationDataWebView.loadData(full_user_id, "text/html", "UTF-8");

                    dbLoader.deleteAll();
                    Setup newSetup = new Setup()
                            .accesToken(key)
                            .name(expNameString)
                            .region(region)
                            .userNumber(experimentNumberString);
                    dbLoader.addSetup(newSetup);
                }
            }
        });
    }

    private void setOperatorModeText() {
        String currentMode = getModeFromSharedPrefer();

        if (currentMode.equals("ON")) {
            ModeBtn.setText("OFF");
        } else {
            ModeBtn.setText("ON");
        }
    }

    private String getModeFromSharedPrefer() {
        SharedPreferences existingSharedData = getSharedPreferences("DataSharedPref", MODE_PRIVATE);
        String modeData = existingSharedData.getString("modeManageBtn", "");

        return modeData;
    }

    private void sendPostdataToAWS(String bufferString) {
        sendRequestBody(bufferString);
    }

    private void sendRequestBody(String body)
    {
        try {
            String currentMode = getModeFromSharedPrefer();
            currentMode = (currentMode.equals("ON") ? "operator" : "default");

            RequestQueue requestQueue = Volley.newRequestQueue(this);
            JSONObject jsonBody = new JSONObject();
            jsonBody.put ("userId", experimentNumber.getText().toString());
            jsonBody.put("locationData", body);
            jsonBody.put ("userMode", currentMode);
//            final String requestBody = jsonBody.toString();

            JsonObjectRequest jsonRequest = new JsonObjectRequest(com.android.volley.Request.Method.POST,
                    "https://safety-backend.herokuapp.com/api/location/", jsonBody, new Response.Listener<JSONObject>() {
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

    @Override
    public void onLocationChanged(Location location) {
        Log.i("NOK", "this is debug");
        // When bluetooth or location services has just been turned off, there might still be
        // a Location update event coming from the pipeline.
        // These checks has no other purpose but to keep infoText reflecting the state of BL or Location Services.

        if (BluetoothServices.isBluetoothOn() && LocationServices.isLocationOn(this)) {
            locationDataHTMLString = "<p style=\"font-size:12pt; line-height:0.2em; text-align:left\"><b>Safety App id</b>: " +
                    "<br><br><br><br><br><br><br><br>" + experimentNumberString + "</p><hr>" +
                    "<p style=\"font-size:12pt; line-height:0.2em; text-align:left\"><b>Current Location</b>: </p>" +
                    "<p style=\"font-size:10pt\"> <b>Long:</b>" +
                    location.getLongitude() + ",<br><b>Lat:</b> " +
                    location.getLatitude() +
                     "<br><b>Accuracy: </b>" +
                    String.valueOf(location.getAccuracy()) +
                    "<br><b>Building: </b>" + String.valueOf(location.getBuildingId()) +
                    "<br><b>Floor: </b>" + String.valueOf(location.getFloorIndex()) +
                    "</p>";

            locationDataWebView.clearCache(true);
            locationDataWebView.clearHistory();
            locationDataWebView.loadData(locationDataHTMLString, "text/html", "UTF-8");

            counter += 1;
            long now = System.currentTimeMillis();
            String somTime = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss.000000000").format(new java.util.Date(now)).replace(" ", "T") + "Z";

            // send to cloud
            if (0 == 0) {
                String userId = experimentNumber.getText().toString();
                // buffer = "{\"id\":\"" + now + "\",\"data\":\"" + "" + somTime + "," + location.getProvider() + "," + userId + "," +
                //        location.getLongitude() + "," + location.getLatitude() + "," + ((int) location.getFloorIndex() + 1) + "," +
                //        location.getAccuracy() + "" + "\"" + "}";

                JSONObject bufferJson = new JSONObject();
                try {
                    bufferJson.put("id", now);
                    bufferJson.put("userId", userId);
                    bufferJson.put("longitude", location.getLongitude());
                    bufferJson.put("latitude", location.getLatitude());
                    bufferJson.put("floorIndex", location.getFloorIndex());
                    bufferJson.put("buildingId", location.getBuildingId());
                } catch (JSONException e) {
                    e.printStackTrace();
                }

                Log.i ("NOK", bufferJson.toString());
                sendPostdataToAWS(bufferJson.toString());
                counter = 0;
            }
        }
    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                boolean allPermissionsGranted = true;
                for (int result : grantResults) {
                    if (result == PackageManager.PERMISSION_DENIED) {
                        allPermissionsGranted = false;
                    }
                }

                if (allPermissionsGranted) {
                    //startPositioning();
                }
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_ENABLE_BT:
                if (resultCode == RESULT_OK) {
                    // startPositioning();
                } else {
                    //TODO:  User did not enable Bluetooth or an error occurred
                }
                break;

            default:
                super.onActivityResult(requestCode, resultCode, data);
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    public void onStop() {
        super.onStop();

        // unregisterReceiver(receiver);

        // When FusedLocationProviderApi has any registered LocationListener on it, positioning engine remains alive.
        // Meaning it will keep bluetooth scanner alive and will drain battery.
        // Therefore, when app is backgrounded, it is recommended to call FusedLocationProviderApi.Api.get().removeLocationUpdates()
        // for each LocationListener you have previously registered. Unless you want to track user's movements even if when app has backgrounded.
        // LocationServices.getFusedLocationProviderApi().removeLocationUpdates(this);
    }

    private BroadcastReceiver receiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

        }
    };

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
            }
        }
    }

    /**
     * By default, SDK has disabled GPS (priority is PRIORITY_STEERPATH_ONLY).
     * With LocationRequest, you may enable GPS and also define paramaters such as how accurate or how frequently positioning is collected.
     * Usually Steerpath advices against of enabling GPS, but this is the way you can do it.
     *
     * @return request
     */
    private static LocationRequest createLocationRequestWithGpsEnabled() {
        LocationRequest request = new LocationRequest().setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // GPS threshold determines the minimum accuracy that GPS must have in order for automatic bluetooth to GPS switch to happen.
        request.setGpsThreshold(3);
        return request;
    }

    private boolean checkBluetooth() {
        final BluetoothManager bluetoothManager = (BluetoothManager) getSystemService(BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bluetoothManager.getAdapter();

        // Ensures Bluetooth is available on the device and it is enabled. If not,
        // displays a dialog requesting user permission to enable Bluetooth.
        if (adapter == null || !adapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
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

    private boolean validateExperimentName(String newid)
    {
        if(newid.isEmpty())
        {
            Log.i ("NOK", "Experiemnt id is empty");
            webpage.clearCache(true);
            webpage.clearHistory();
            customHTML = "<html><body style='color:red'> You must add your experiment id and user number first example: enth and 12</body></html>";
            webpage.loadData(customHTML, "text/html", "UTF-8");
            return false;
        }
        else if(newid.equals("test"))
        {
            Log.i ("NOK", "Experiemnt is test");
            webpage.clearCache(true);
            webpage.clearHistory();
            customHTML = "<html><body style='color:orange'> Please choose another valid name other than 'test' e.g. enth_22</body></html>";
            webpage.loadData(customHTML, "text/html", "UTF-8");
            return false;
        }
        else
        {
            buttonStartLocation.setText(R.string.startLocation);

            customHTML = "<html><body style='color:blue'><h2>ESK experiment</h2><b>"+full_user_id+"</b></body></html>";
            webpage.clearCache(true);
            webpage.clearHistory();
            webpage.getSettings().setJavaScriptEnabled(true);
            webpage.setWebChromeClient(new WebChromeClient());
            webpage.loadUrl("file:///android_asset/www/index.html");
            return true;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (SettingsHelper.useAssetGateway()) {
            AssetGateway.stopAssetGatewayService(this);

        }

        medic.enablePowerCycleOnFailures(getApplicationContext());
        medic.enablePeriodicTests(getApplicationContext(), BluetoothMedic.SCAN_TEST |
                BluetoothMedic.TRANSMIT_TEST);

        unregisterReceiver(receiver);

        // When FusedLocationProviderApi has any registered LocationListener on it, positioning engine remains alive.
        // Meaning it will keep bluetooth scanner alive and will drain battery.
        // Therefore, when app is backgrounded, it is recommended to call FusedLocationProviderApi.Api.get().removeLocationUpdates()
        // for each LocationListener you have previously registered. Unless you want to track user's movements even if when app has backgrounded.
        LocationServices.getFusedLocationProviderApi().removeLocationUpdates(this);

    }

    public boolean checkLocationPermission() {
        return true;
    }


}