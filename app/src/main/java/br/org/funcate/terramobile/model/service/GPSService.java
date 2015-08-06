package br.org.funcate.terramobile.model.service;

import android.content.Context;
import android.location.LocationListener;
import android.location.LocationManager;

/**
 * Created by Andre Carvalho on 04/08/15.
 */
public class GPSService {

    // The minimum distance to change Updates in meters
    private static long MIN_DISTANCE_CHANGE_FOR_UPDATES = 0; // 0 meters

    // The minimum time between updates in milliseconds
    private static long MIN_TIME_BW_UPDATES = 0; // 0 minute (ex.: to 1 minute use (1000 * 60 * 1))

    public static boolean isGPSProviderEnable(Context context) {
        return GPSService.getLocationManager(context).isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    public static boolean isNetworkProviderEnable(Context context) {
        return GPSService.getLocationManager(context).isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    public static boolean registerListener(Context context, LocationListener locationListener) {
        if(GPSService.isNetworkProviderEnable(context)){
            GPSService.getLocationManager(context).requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    GPSService.MIN_TIME_BW_UPDATES,
                    GPSService.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);
            return true;
        }else if (GPSService.isGPSProviderEnable(context)) {
            GPSService.getLocationManager(context).requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    GPSService.MIN_TIME_BW_UPDATES,
                    GPSService.MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    locationListener);
            return true;
        }else {
            return false;
        }
    }

    public static void unregisterListener(Context context, LocationListener locationListener){
        // Remove the listener you previously added
        GPSService.getLocationManager(context).removeUpdates(locationListener);
    }

    private static LocationManager getLocationManager(Context context) {
        return (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }
}
