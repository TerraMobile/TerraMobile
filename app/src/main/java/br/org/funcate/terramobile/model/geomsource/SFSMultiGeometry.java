package br.org.funcate.terramobile.model.geomsource;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryCollection;

import org.osmdroid.bonuspack.kml.KmlMultiGeometry;

import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

/**
 * Created by bogo on 15/06/15.
 */
public class SFSMultiGeometry extends KmlMultiGeometry {


    public SFSMultiGeometry(Geometry geometry, GpkgLayer layer){

        if(geometry!=null) {

                GeometryCollection collection=  (GeometryCollection) geometry;
                for (int i = 0; i < collection.getNumGeometries(); i++)
                {
                    Geometry geom = collection.getGeometryN(i);
                    mItems.add(SFSGeometry.parseSFS(geom, layer));
                }
           }
    }
}
