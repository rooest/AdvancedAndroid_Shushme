package com.example.android.shushme;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Result;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.Geofence;
import com.google.android.gms.location.GeofencingRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;

import java.util.ArrayList;
import java.util.List;

public class GeoFencing implements ResultCallback {

    private static final String TAG = GeoFencing.class.getSimpleName();

    private static int EXP_TIME = 24 * 60 * 60 * 1000;
    private static int RADIUS = 50;


    private GoogleApiClient mGoogleApiClient;
    private Context mContext;
    private PendingIntent mPendingIntent;
    private List<Geofence> mGeofenceList;

    public GeoFencing(GoogleApiClient mGoogleApiClient, Context mContext) {
        this.mGoogleApiClient = mGoogleApiClient;
        this.mContext = mContext;
        mPendingIntent = null;
        mGeofenceList = new ArrayList<>();
    }

    public void registerAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()
                || mGeofenceList == null || mGeofenceList.size() == 0) {
            return;
        }

        if (ActivityCompat.checkSelfPermission(mContext, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // : Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.GeofencingApi.addGeofences(
                mGoogleApiClient,
                getGeofencingREquest(),
                getGeofencePendingIntent()
        ).setResultCallback(this);

    }

    public void unRegisterAllGeofences() {
        if (mGoogleApiClient == null || !mGoogleApiClient.isConnected()) {
            return;
        }

        try {
            LocationServices.GeofencingApi.removeGeofences(
                    mGoogleApiClient,
                    getGeofencePendingIntent()
            ).setResultCallback(this);
        } catch (SecurityException e) {
            e.printStackTrace();
        }


    }


    public void updateGeofencesList(PlaceBuffer places) {
        /*
          Why? we  have aready  crate this  field in  constructor
         */
        mGeofenceList = new ArrayList<>();
        if (places == null || places.getCount() == 0) return;
        for (Place place : places) {

            String placeId = place.getId();
            Double placeLatitude = place.getLatLng().latitude;
            Double placeLongtitude = place.getLatLng().longitude;

            Geofence geofence = new Geofence.Builder()
                    .setRequestId(placeId)
                    .setExpirationDuration(EXP_TIME)
                    .setCircularRegion(placeLatitude, placeLongtitude, RADIUS)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER | Geofence.GEOFENCE_TRANSITION_EXIT)
                    .build();

            mGeofenceList.add(geofence);
        }
    }

    private GeofencingRequest getGeofencingREquest() {
        GeofencingRequest.Builder geofencingRequestBuilder = new GeofencingRequest.Builder();
        geofencingRequestBuilder.setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER);
        geofencingRequestBuilder.addGeofences(mGeofenceList);
        return geofencingRequestBuilder.build();
    }

    private PendingIntent getGeofencePendingIntent() {
        if (mPendingIntent != null) {
            return mPendingIntent;
        }
        Intent intent = new Intent(mContext, GeofenceBroadcastReceiver.class);
        mPendingIntent = PendingIntent.getBroadcast(mContext, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT);
        return mPendingIntent;
    }

    @Override
    public void onResult(@NonNull Result result) {
        Log.d(TAG, " onREsult " + result.getStatus().toString());
    }
}
