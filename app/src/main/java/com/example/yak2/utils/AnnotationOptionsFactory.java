//package com.example.yak2.utils;
//
//import android.content.Context;
//import android.graphics.Color;
//import androidx.core.content.ContextCompat;
//
//import com.google.gson.JsonObject;
//import com.google.gson.JsonPrimitive;
//import com.mapbox.mapboxsdk.annotations.MarkerViewOptions;
//import com.mapbox.mapboxsdk.annotations.PolygonOptions;
//import com.mapbox.mapboxsdk.geometry.LatLng;
//import com.mapbox.mapboxsdk.style.layers.Property;
//import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
//import com.mapbox.services.commons.geojson.Feature;
//import com.mapbox.services.commons.geojson.FeatureCollection;
//import com.mapbox.services.commons.geojson.Point;
////import com.steerpath.demo.R;
//import com.steerpath.sdk.maps.MapUtils;
//import com.steerpath.sdk.maps.SteerpathAnnotationOptions;
//import com.steerpath.sdk.maps.SteerpathLayerOptions;
//import com.steerpath.sdk.meta.MetaFeature;
//import com.steerpath.sdk.meta.internal.JSONContract;
//import com.steerpath.sdk.meta.internal.K;
//
//import org.json.JSONArray;
//import org.json.JSONException;
//import org.json.JSONObject;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconAllowOverlap;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAllowOverlap;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textAnchor;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textColor;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textField;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textFont;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloBlur;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloColor;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textHaloWidth;
//import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.textSize;
//
//public class AnnotationOptionsFactory {
//
//    private AnnotationOptionsFactory() {}
//
//    public static SteerpathAnnotationOptions createAnnotationOptions(Context context, MetaFeature feature) {
//        SteerpathAnnotationOptions.Builder builder = new SteerpathAnnotationOptions.Builder();
//        builder.withBaseMarkerViewOptions(createMarkerViewOptions(context, feature));
//        builder.userId(feature.getId());
//        builder.floor(feature.getFloor());
//        return builder.build();
//    }
//
//    public static List<SteerpathAnnotationOptions> createAnnotationOptions(Context context, List<MetaFeature> features) {
//        ArrayList<SteerpathAnnotationOptions> options = new ArrayList<>();
//        for (MetaFeature feature : features) {
//            options.add(createAnnotationOptions(context, feature));
//        }
//
//        return options;
//    }
//
//    /**
//     * MarkerViewOptions builds MarkerView.
//     */
//    private static MarkerViewOptions createMarkerViewOptions(Context context, MetaFeature feature) {
//
//        String title = feature.getTitle();
//        if (title.isEmpty()) title = feature.getTags().toString();
//
//        return new MarkerViewOptions()
//                .position(new LatLng(feature.getLatitude(), feature.getLongitude()))
//                .title(title);
//    }
//
//    public static SteerpathAnnotationOptions createGeofenceOptions(Context context, JSONObject featureJson) {
//        SteerpathAnnotationOptions.Builder builder = new SteerpathAnnotationOptions.Builder();
//        builder.withOptions(createPolygonOptions(context, featureJson));
//        builder.floor(JSONContract.getFloor(featureJson, 0));
//        return builder.build();
//    }
//
//    private static PolygonOptions createPolygonOptions(Context context, JSONObject featureJson) {
//        List<LatLng> polygons = new ArrayList<>();
//        if (featureJson.has(K.geometry)) {
//            try {
//                JSONObject geometry = featureJson.getJSONObject(K.geometry);
//                if (geometry.has(K.coordinates)) {
//                    JSONArray coordinates = geometry.getJSONArray(K.coordinates);
//                    for (int i=0; i<coordinates.length(); i++) {
//                        JSONArray coordinate = coordinates.getJSONArray(i);
//                        for (int j=0; j<coordinate.length(); j++) {
//                            JSONArray c = coordinate.getJSONArray(j);
//                            polygons.add(new LatLng(c.getDouble(1), c.getDouble(0))); // GeoJson brings LatLon in reverse order: LonLat
//                        }
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return new PolygonOptions()
//                .addAll(polygons)
//                .alpha(0.5f)
//                .strokeColor(ContextCompat.getColor(context, R.color.geofence_area))
//                .fillColor(ContextCompat.getColor(context, R.color.geofence_area));
//    }
//
//    /*private static PolygonOptions createMultiPolygonOptions(Context context, JSONObject featureJSON) {
//        List<LatLng> polygons = new ArrayList<>();
//        if (featureJSON.has(K.geometry)) {
//            try {
//                JSONObject geometry = featureJSON.getJSONObject(K.geometry);
//                if (geometry.has(K.coordinates)) {
//                    JSONArray coordinates = geometry.getJSONArray(K.coordinates);
//                    for (int i = 0; i < coordinates.length(); i++) {
//                        JSONArray coordinate = coordinates.getJSONArray(i);
//                        for (int j = 0; j < coordinate.length(); j++) {
//                            JSONArray c = coordinate.getJSONArray(j);
//                            polygons.add(new LatLng(c.getDouble(1), c.getDouble(0)));
//                        }
//                    }
//                }
//            } catch (JSONException e) {
//                e.printStackTrace();
//            }
//        }
//
//        return new PolygonOptions()
//                .addAll(polygons)
//                .alpha(0.5f)
//    }*/
//
//    public static SteerpathAnnotationOptions createSimpleMarker(LatLng latLng, int floorIndex) {
//        Object opts = new MarkerViewOptions().position(new LatLng(latLng.getLatitude(), latLng.getLongitude()));
//        SteerpathAnnotationOptions.Builder builder = new SteerpathAnnotationOptions.Builder();
//        builder.withOptions(opts);
//        builder.floor(floorIndex);
//        return builder.build();
//    }
//
//    public static SteerpathAnnotationOptions createSymbolLayerOption(String sourceId, String layerId) {
//        SteerpathAnnotationOptions.Builder builder = new SteerpathAnnotationOptions.Builder();
//        builder.withSteerpathLayerOptions(new SteerpathLayerOptions()
//                .withSymbolLayer(new SymbolLayer(layerId, sourceId)
//                                .withProperties(
//                                        iconImage("my-marker-image"),
//                                        iconAllowOverlap(true),
//
//                                        textField("{title}"),
//                                        textColor(Color.BLACK),
//                                        textSize(20f),
//                                        textAnchor(Property.TEXT_ANCHOR_TOP),
//                                        textAllowOverlap(false),
//                                        textHaloColor(Color.WHITE),
//                                        textHaloWidth(0.5f),
//                                        textHaloBlur(0.5f),
//                                        // If you are not seeing text at all, check the font!
//                                        textFont(new String[] {"arial"})
//
//                                        // this is funny. You should try it.
////                                iconRotate(
////                                        zoom(
////                                                exponential(
////                                                        stop(17, iconRotate(0f)),
////                                                        stop(18, iconRotate(30f)),
////                                                        stop(19, iconRotate(60f)),
////                                                        stop(20, iconRotate(90f))
////                                                ).withBase(0.8f)
////                                        )
////                                )
//                                )
//                ));
//        return builder.build();
//    }
//
//    public static FeatureCollection toFeatureCollection(List<MetaFeature> metaFeatures) {
//        Feature[] featureList = new Feature[metaFeatures.size()];
//        for (int i=0; i<metaFeatures.size(); i++) {
//            MetaFeature metaFeature = metaFeatures.get(i);
//            featureList[i] = Feature.fromGeometry(Point.fromCoordinates(new double[] {metaFeature.getLongitude(), metaFeature.getLatitude()}), featureProperties(metaFeature));
//        }
//        return FeatureCollection.fromFeatures(featureList);
//    }
//
//    private static JsonObject featureProperties(MetaFeature metaFeature) {
//        JsonObject object = new JsonObject();
//        object.add("title", new JsonPrimitive(metaFeature.getTitle()));
//        object.add("type", new JsonPrimitive("marker"));
//        object.add("localRef", new JsonPrimitive(metaFeature.getLocalRef()));
//        object.add(SteerpathLayerOptions.LAYER_INDEX, new JsonPrimitive(metaFeature.getFloor())); // required for floor switching
//        object.add(SteerpathLayerOptions.BUILDING_REF, new JsonPrimitive(metaFeature.getBuildingReference())); // required for floor switching
//        object.add("id", new JsonPrimitive(metaFeature.getId()));
//        return object;
//    }
//
//    public static JsonObject featureProperties(String title, int floor, String buildingRf, String id) {
//        JsonObject object = new JsonObject();
//        object.add("title", new JsonPrimitive(title));
//        object.add("type", new JsonPrimitive("marker"));
//        object.add(SteerpathLayerOptions.LAYER_INDEX, new JsonPrimitive(floor));
//        object.add(SteerpathLayerOptions.BUILDING_REF, new JsonPrimitive(buildingRf));
//        object.add("id", new JsonPrimitive(id));
//        return object;
//    }
//
//    public static SteerpathAnnotationOptions createFillExtrusionForLatLng(Feature feature) {
//        SteerpathAnnotationOptions.Builder builder = new SteerpathAnnotationOptions.Builder();
//        builder.withSteerpathLayerOptions(new SteerpathLayerOptions()
//                .withFilterStatement(MapUtils.toFilterStatement(feature))
//                .withFillExtrusionLayer(MapUtils.createFillExtrusionLayer(getRandomHeight(), getRandomColor(), feature)));
//        return builder.build();
//    }
//
//    public static SteerpathAnnotationOptions createFillExtrusionForTags(String... tags) {
//        SteerpathAnnotationOptions.Builder builder = new SteerpathAnnotationOptions.Builder();
//        builder.withSteerpathLayerOptions(new SteerpathLayerOptions()
//                .withFilterStatement(MapUtils.toFilterStatement(tags))
//                .withFillExtrusionLayer(MapUtils.createFillExtrusionLayer(getRandomHeight(), getRandomColor(), tags)));
//        return builder.build();
//    }
//
//    private static float getRandomHeight() {
//        int min = 5;
//        int max = 40;
//        Random r = new Random();
//        return (float) (r.nextInt(max - min + 1) + min);
//    }
//
//    private static int getRandomColor() {
//        Random rnd = new Random();
//        return Color.argb(255, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256));
//    }
//}