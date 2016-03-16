package br.org.funcate.terramobile.model.geomsource;

import org.opengis.feature.simple.SimpleFeature;
import org.osmdroid.bonuspack.kml.KmlPlacemark;

import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSPlacemark extends KmlPlacemark {


    public SFSPlacemark(SimpleFeature feature, GpkgLayer layer)
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
                mGeometry = SFSGeometry.parseSFS(feature, layer);
                mGeometry.mId = mId;
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
