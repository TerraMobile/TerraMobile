package br.org.funcate.terramobile.model.service;

import android.os.Bundle;

import com.augtech.geoapi.geopackage.DateUtil;

import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.type.AttributeType;
import org.opengis.feature.type.Name;

import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * Created by Andre Carvalho on 27/08/15.
 */
public class FeatureService {

    /**
     * Use to indicate the exists feature data into a Bundle.
     */
    public static String FEATURE_DATA_CONTENT = "feature_data_content";
    public static String FEATURE_DATA_KEYS = "feature_data_keys";

    /**
     * Read all attributes of the feature and put into Bundle object.
     * @param feature, a feature
     * @return a Bundle with feature data attributes.
     */
    public static Bundle featureAttrsToBundle(SimpleFeature feature) {

        List<Object> attrs = feature.getAttributes();
        if(attrs.size()<=0) return null;
        List<AttributeType> featureTypes = feature.getFeatureType().getTypes();
        ArrayList<String> featureKeys = new ArrayList<String>();

        Iterator<AttributeType> itTypes = featureTypes.iterator();
        Bundle bundle = new Bundle(attrs.size());

        while (itTypes.hasNext()) {
            AttributeType attributeType = itTypes.next();
            Name typeName = attributeType.getName();
            String typeClass = attributeType.getBinding().getName();
            Object o = feature.getAttribute(typeName);
            String s=null;
            if(String.class.getName().equals(typeClass)){
                s = (String)o;
                bundle.putString(typeName.toString(),s);
            }else if(Double.class.getName().equals(typeClass)){
                Double d = (Double)o;
                s = d.toString();
            }else if(Integer.class.getName().equals(typeClass)){
                Integer i = (Integer)o;
                s = i.toString();
            }else if(Boolean.class.getName().equals(typeClass)){
                Boolean b = (Boolean)o;
                s = b.toString();
            }else if(Date.class.getName().equals(typeClass)){
                Date date = (Date)o;
                s = DateUtil.serializeDate(date);
            }else if(typeClass.equals("[Ljava.lang.Byte;")){
                byte[] photo=(byte[])o;
                bundle.putByteArray(typeName.toString(), photo);
                featureKeys.add(typeName.toString());
            }

            if(s!=null) {
                bundle.putString(typeName.toString(), s);
                featureKeys.add(typeName.toString());
            }
        }
        bundle.putStringArrayList(FEATURE_DATA_KEYS, featureKeys);
        return bundle;
    }
}
