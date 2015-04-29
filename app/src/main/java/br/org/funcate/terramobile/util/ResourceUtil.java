package br.org.funcate.terramobile.util;

import android.content.res.Resources;
import android.util.TypedValue;

import com.vividsolutions.jts.util.CollectionUtil;

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
}
