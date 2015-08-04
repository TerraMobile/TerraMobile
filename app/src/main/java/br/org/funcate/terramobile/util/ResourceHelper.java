package br.org.funcate.terramobile.util;

import android.content.res.Resources;
import android.os.Environment;
import android.util.Log;
import android.util.TypedValue;

import com.vividsolutions.jts.util.CollectionUtil;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.MissingResourceException;

import br.org.funcate.dynamicforms.FormActivity;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;

/**
 * Created by bogo on 23/04/15.
 */
public class ResourceHelper {

    private static Resources resources;

    public static double getDoubleResource(int resourceId) throws InvalidAppConfigException
    {

        Resources resources = getResources();

        TypedValue typedValue = new TypedValue();

        resources.getValue(resourceId, typedValue, true);

        return typedValue.getFloat();
    }

    public static int getIntResource(int resourceId) throws InvalidAppConfigException
    {
        Resources resources = getResources();

        return resources.getInteger(resourceId);
    }

    public static boolean getBooleanResource(int resourceId) throws InvalidAppConfigException
    {
        Resources resources = getResources();

        return resources.getBoolean(resourceId);
    }

    public static String getStringResource(int resourceId) throws InvalidAppConfigException
    {
        Resources resources = getResources();

        return resources.getString(resourceId);
    }

    public static String[] getStringArrayResource(int resourceId) throws InvalidAppConfigException
    {
        Resources resources = getResources();

        return resources.getStringArray(resourceId);
    }

    public static ArrayList getArrayListResource(int resourceId) throws InvalidAppConfigException {

        Resources resources = getResources();

        String s[] = resources.getStringArray(resourceId);

        ArrayList<String> l = new ArrayList<String>();
        //l.addAll(s);
        return l;
       // return resources.getStringArray(resourceId);
    }
    private static Resources getResources() throws InvalidAppConfigException {
        if(resources!=null)
        {
            return resources;
        }
        else
        {
            throw new InvalidAppConfigException("Missing application configuration.");
        }

    }

    public static void setResources(Resources res)
    {
        resources =  res;
    }
}