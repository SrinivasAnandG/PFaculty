package com.srinivasanand.pfaculty;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.telephony.TelephonyManager;

import com.firebase.client.Firebase;
import com.google.android.gms.common.api.GoogleApiClient;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by srinivasanand on 02/12/17.
 */

public class TrackLocation extends Service {
    public static Intent i;
    public static String s;
    public GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private Handler mHandler = new Handler();

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        i = intent;
        Timer t = new Timer();


        t.scheduleAtFixedRate(new GetLocation(), 0, 15*60*1000);
        //Toast.makeText(getApplicationContext(), "started", Toast.LENGTH_SHORT).show();
        TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
        String uid = tManager.getDeviceId();

        //startService(i);
        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        startService(i);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        startService(i);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        startService(i);
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        startService(i);
    }

    private class GetLocation extends TimerTask  {
        Double lat = 0.0, lon = 0.0, alt = 0.0;
        private LocationListener listener;
        private LocationManager locationManager;
        Firebase ref;
        int k=0;

        @Override
        public void run() {
            // run on another thread
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // display toast at every 10 second
                    //Toast.makeText(getApplicationContext(), "Notify", Toast.LENGTH_SHORT).show();
                    //Toast.makeText(getApplicationContext(), lon + lat + alt + "", Toast.LENGTH_SHORT).show();
                    if (ActivityCompat.checkSelfPermission(getApplication(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    listener = new LocationListener() {
                        @Override
                        public void onLocationChanged(Location location) {

                            float str=location.getAccuracy();


                            if(k==0)
                            {
                                //Toast.makeText(getBaseContext(), "Everything working perfect...)", Toast.LENGTH_SHORT).show();
                            }
                            k=k+1;
                            TelephonyManager tManager = (TelephonyManager)getSystemService(Context.TELEPHONY_SERVICE);
                            String uid = tManager.getDeviceId();
                            //Toast.makeText(getApplicationContext(),uid,Toast.LENGTH_SHORT).show();
                            Firebase.setAndroidContext(getApplicationContext());
                            ref=new Firebase("https://pfaculty-53be5.firebaseio.com/Location/"+uid);
                            ref.setValue(location.getLongitude() + ";" + location.getLatitude());
                            //Toast.makeText(getBaseContext(), location.getLongitude() + ";" + location.getLatitude(), Toast.LENGTH_SHORT).show();
                            Intent i = new Intent("location_update");
                            i.putExtra("coordinates", location.getLongitude() + ";" + location.getLatitude());
                            //Toast.makeText(getApplicationContext(), location.getLongitude() + ";" + location.getLatitude(), Toast.LENGTH_SHORT).show();
                            sendBroadcast(i);
                        }

                        @Override
                        public void onStatusChanged(String s, int i, Bundle bundle) {

                        }

                        @Override
                        public void onProviderEnabled(String s) {

                            // startService(getApplicationContext(),GPS_Service.class);
                            startService(new Intent(getApplicationContext(), TrackLocation.class));

                        }

                        @Override
                        public void onProviderDisabled(String s) {
                            Intent i = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                            i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            startActivity(i);
                        }
                    };

                    locationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);

                    //noinspection MissingPermission
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 15*60*1000, 0, listener);
                    //

                }
            });
        }



    }
}
