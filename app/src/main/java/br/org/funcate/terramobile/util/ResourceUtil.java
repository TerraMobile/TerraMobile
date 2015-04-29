package br.org.funcate.terramobile.util;

import android.content.res.Resources;
import android.os.Environment;
import android.util.TypedValue;

import com.vividsolutions.jts.util.CollectionUtil;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import br.org.funcate.terramobile.R;

/**
 * Created by bogo on 23/04/15.
 */
public class ResourceUtil {

    public static double getDoubleResource(Resources resources, int resourceId)
    {
        TypedValue typedValue = new TypedValue();
        resources.getValue(resourceId, typedValue, true);
        return typedValue.getFloat();
    }

    public static int getIntResource(Resources resources, int resourceId)
    {
        return resources.getInteger(resourceId);
    }

    public static boolean getBooleanResource(Resources resources, int resourceId)
    {
        return resources.getBoolean(resourceId);
    }

    public static String getStringResource(Resources resources, int resourceId)
    {
        return resources.getString(resourceId);
    }

    public static String[] getStringArrayResource(Resources resources, int resourceId)
    {
        return resources.getStringArray(resourceId);
    }

    public static ArrayList getArrayListResource(Resources resources, int resourceId)
    {
        String s[] = resources.getStringArray(resourceId);

        ArrayList<String> l = new ArrayList<String>();
        //l.addAll(s);
        return l;
       // return resources.getStringArray(resourceId);
    }

    /** Get a directory on extenal storage (SD card etc), ensuring it exists
     *
     * @return a new File representing the chosen directory
     */
    public static File getDirectory(String directory) {
        if (directory==null) return null;
        String path = Environment.getExternalStorageDirectory().toString();
        path += directory.startsWith("/") ? "" : "/";
        path += directory.endsWith("/") ? directory : directory + "/";
        File file = new File(path);
        file.mkdirs();
        return file;
    }
}
