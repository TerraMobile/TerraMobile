package br.org.funcate.terramobile.model.geomsource;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.vividsolutions.jts.geom.Geometry;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.bonuspack.kml.KmlGeometry;
import org.osmdroid.bonuspack.kml.KmlLineString;
import org.osmdroid.bonuspack.kml.KmlMultiGeometry;
import org.osmdroid.bonuspack.kml.KmlPlacemark;
import org.osmdroid.bonuspack.kml.KmlPoint;
import org.osmdroid.bonuspack.kml.KmlPolygon;

import java.util.Map;
import java.util.Set;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSPlacemark extends KmlPlacemark {


    public SFSPlacemark(SimpleFeature feature)
    {
        super();
        if(feature!=null)
        {
            if(feature.getID()!=null)
            {
                mId = feature.getID();
            }
            if(feature.getDefaultGeometry()!=null)
            {
                mGeometry = SFSGeometry.parseSFS(feature);
            }

        }
/*        if (json.has("properties")){

            for (int i = 0; i < feature.getAttributeCount(); i++) {
                feature.getAttribute(i)
            }


            for (Map.Entry<String,JsonElement> entry:entrySet){

                String key = entry.getKey();
                JsonElement je = entry.getValue();
                String value;
                try {
                    value = je.getAsString();
                } catch (Exception e){
                    value = je.toString();
                }
                if (key!=null && value!=null)
                    setExtendedData(key, value);
            }
            //Put "name" property in standard KML format:
            if (mExtendedData!=null && mExtendedData.containsKey("name")){
                mName = mExtendedData.get("name");
                mExtendedData.remove("name");
            }
        }*/
    }



}
