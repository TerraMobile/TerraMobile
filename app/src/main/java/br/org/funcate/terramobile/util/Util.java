package br.org.funcate.terramobile.util;



import android.content.Context;
import android.graphics.ColorMatrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.HashMap;
import java.util.Random;

/**
 * Created by bogo on 17/06/15.
 */
public class Util {
    public static HashMap<String,Integer> getRandomColor()
    {
        Random rand = new Random();
        int r = rand.nextInt(256);
        int g = rand.nextInt(256);
        int b = rand.nextInt(256);
        HashMap<String, Integer> colorMap = new HashMap<String, Integer>();
        colorMap.put("r", r);
        colorMap.put("g", g);
        colorMap.put("b", b);
        return colorMap;

    }

    /**
     * Ckeck the internet connection
     * @param context
     * @return
     */
    public static boolean isConnected(Context context){
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connMgr.getActiveNetworkInfo();
        return networkInfo != null && networkInfo.isConnected();
    }
}
