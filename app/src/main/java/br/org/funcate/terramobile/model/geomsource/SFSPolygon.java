package br.org.funcate.terramobile.model.geomsource;

import com.vividsolutions.jts.geom.Polygon;

import org.osmdroid.bonuspack.kml.KmlPolygon;
import org.osmdroid.util.GeoPoint;

import java.util.ArrayList;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSPolygon extends KmlPolygon {

    public SFSPolygon(Polygon polygon){
        super();

        if(polygon!=null)
        {
            mCoordinates = SFSGeometry.parseSFSLineString(polygon.getExteriorRing());

            if(polygon.getNumInteriorRing()>0)
            {
                mHoles = new ArrayList<ArrayList<GeoPoint>>(polygon.getNumInteriorRing());
                for (int i=1; i<polygon.getNumInteriorRing(); i++)
                {
                   ArrayList<GeoPoint> pList = SFSGeometry.parseSFSLineString(polygon.getInteriorRingN(i));
                    mHoles.add(pList);
                }
            }
        }
    }
}
