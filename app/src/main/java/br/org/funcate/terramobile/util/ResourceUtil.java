package br.org.funcate.terramobile.util;

import android.content.res.Resources;
import android.util.TypedValue;

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
}
