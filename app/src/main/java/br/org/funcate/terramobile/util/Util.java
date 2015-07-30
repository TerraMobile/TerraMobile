package br.org.funcate.terramobile.util;



import android.content.Context;
import android.graphics.ColorMatrix;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
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

    public static void copyFile(String fileOrigin, String fileDestination) {
        InputStream inStream;
        OutputStream outStream;
        try {
            File origin = new File(fileOrigin);
            File destination = new File(fileDestination);

            inStream = new FileInputStream(origin);
            outStream = new FileOutputStream(destination);

            byte[] buffer = new byte[1024];

            int length;
            while ((length = inStream.read(buffer)) > 0)
                outStream.write(buffer, 0, length);
            inStream.close();
            outStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
