package br.org.funcate.terramobile.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import org.opengis.geometry.BoundingBox;
import org.osmdroid.util.BoundingBoxE6;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by bogo on 30/07/15.
 */
public class GeoUtil {
        /**
     * Ckeck the internet connection
     * @param context
     * @return
     */
    public static BoundingBoxE6 convertToBoundingBoxE6(BoundingBox bb){
        BoundingBoxE6 bbe6 = new BoundingBoxE6(bb.getMaxY(), bb.getMaxX(), bb.getMinY(), bb.getMinX());
        return bbe6;
    }
}
