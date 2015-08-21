package br.org.funcate.terramobile.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.augtech.geoapi.geometry.BoundingBoxImpl;

import org.opengis.geometry.BoundingBox;
import org.osmdroid.util.BoundingBoxE6;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by bogo on 30/07/15.
 */
public class GeoUtil {

    public static BoundingBoxE6 convertToBoundingBoxE6(BoundingBox bb){
        BoundingBoxE6 bbe6 = new BoundingBoxE6(bb.getMaxY(), bb.getMaxX(), bb.getMinY(), bb.getMinX());
        return bbe6;
    }

    public static BoundingBox convertToBoundingBox(BoundingBoxE6 bb){

        BoundingBox bbe = new BoundingBoxImpl(bb.getLonWestE6() / 1E6, bb.getLonEastE6() / 1E6, bb.getLatSouthE6() / 1E6, bb.getLatNorthE6() / 1E6);
        return bbe;
    }
}
