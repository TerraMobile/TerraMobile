package br.org.funcate.terramobile.service;

import android.content.Context;
import android.location.LocationManager;

/**
 * Created by Andre Carvalho on 04/08/15.
 */
public class GPSService {

    public static boolean isEnable(Context context) {
        LocationManager mlocManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return mlocManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }
}
