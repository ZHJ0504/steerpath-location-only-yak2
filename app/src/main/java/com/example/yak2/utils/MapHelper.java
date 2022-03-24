package com.example.yak2.utils;

import android.location.Location;

import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.steerpath.sdk.maps.SteerpathMap;
import com.steerpath.sdk.meta.MetaFeature;

/*
* Map helper class to move camera into given location or building
*/
public class MapHelper {

    public static void moveCameraTo(SteerpathMap map, MetaFeature feature) {
        if (feature != null) {
            map.setCameraPosition(new CameraPosition.Builder()
                    .target(new LatLng(feature.getLatitude(), feature.getLongitude()))
                    .zoom(18)
                    .build());
        }
    }

    public static void moveCameraTo(SteerpathMap map, LatLng latLng) {
        map.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(latLng))
                .zoom(18)
                .build());
    }

    public static void moveCameraTo(SteerpathMap map, Location location) {
        LatLng latLng = new LatLng();
        latLng.setLongitude(location.getLongitude());
        latLng.setLatitude(location.getLatitude());
        map.setCameraPosition(new CameraPosition.Builder()
                .target(new LatLng(latLng))
                .zoom(18)
                .build());
    }


}
