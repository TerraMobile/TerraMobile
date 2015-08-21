package br.org.funcate.terramobile.model.service;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;

import com.augtech.geoapi.feature.type.GeometryTypeImpl;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import org.geotools.styling.AbstractSymbolizer;
import org.geotools.styling.BasicPolygonStyle;
import org.geotools.styling.ExternalGraphic;
import org.geotools.styling.ExternalGraphicImpl;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.LineSymbolizer;
import org.geotools.styling.LineSymbolizerImpl;
import org.geotools.styling.MarkImpl;
import org.geotools.styling.PointSymbolizer;
import org.geotools.styling.PointSymbolizerImpl;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.PolygonSymbolizerImpl;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.opengis.geometry.Geometry;
import org.opengis.style.GraphicalSymbol;
import org.osmdroid.bonuspack.kml.KmlFeature;
import org.osmdroid.bonuspack.kml.Style;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import br.org.funcate.mobile.sld.SLDParser;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.StyleDAO;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.osmbonuspack.overlays.MyKmlStyler;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by bogo on 12/08/15.
 */
public class StyleService {

    public static Style loadStyle(Context context, String databasePath, GpkgLayer layer) throws InvalidAppConfigException, StyleException {
        try {
            StyleDAO dao = new StyleDAO(DatabaseFactory.getDatabase(context, databasePath));

            String sldXML = dao.get(layer.getName());

            org.geotools.styling.Style[] gtStyle = SLDParser.parse(sldXML);

            Style osmStyle = convertToOSMStyle(gtStyle[0], layer.getFeatureType().getGeometryDescriptor().getType());

            return osmStyle;

        } catch (DAOException e) {
            throw new StyleException(e.getMessage(), e);
        }

    }
    private static Style convertToOSMStyle(org.geotools.styling.Style style, GeometryType geometryType)
    {
        List<FeatureTypeStyle> featureTypeStyleList = style.featureTypeStyles();
        FeatureTypeStyle featureTypeStyle = featureTypeStyleList.get(0);
        Rule rule = featureTypeStyle.rules().get(0);
        Symbolizer sym = rule.symbolizers().get(0);



        int fillColor =0;
        double fillOpacity =0;
        int contourColor =0;
        double contourOpacity =0;
        float strokeWidth =0;
        String wellNowName=null;
        int pointSize=0;
        Bitmap img = null;

        if(sym instanceof PolygonSymbolizerImpl)
        {
            String fillColorHex = ((PolygonSymbolizer)sym).getFill().getColor().toString();

            String contourColorHex = ((PolygonSymbolizer)sym).getStroke().getColor().toString();

            fillColor = Color.parseColor(fillColorHex);

            contourColor = Color.parseColor(contourColorHex);

            strokeWidth = Float.parseFloat(((PolygonSymbolizer)sym).getStroke().getWidth().toString());

            fillOpacity = Double.parseDouble(((PolygonSymbolizer)sym).getFill().getOpacity().toString());

            contourOpacity = Double.parseDouble(((PolygonSymbolizer)sym).getStroke().getOpacity().toString());

        } else if(sym instanceof LineSymbolizerImpl)
        {
            String contourColorHex = ((LineSymbolizer)sym).getStroke().getColor().toString();

            contourColor = Color.parseColor(contourColorHex);

            strokeWidth = Float.parseFloat(((LineSymbolizer)sym).getStroke().getWidth().toString());

            contourOpacity = Double.parseDouble(((LineSymbolizer)sym).getStroke().getOpacity().toString());

        } else if(sym instanceof PointSymbolizerImpl)
        {

            List<GraphicalSymbol> graphicsSymbol = ((PointSymbolizer)sym).getGraphic().graphicalSymbols();

            if(graphicsSymbol.size()>0)
            {
                GraphicalSymbol gs = graphicsSymbol.get(0);
                if(gs instanceof MarkImpl)
                {

                    String fillColorHex = ((MarkImpl)gs).getFill().getColor().toString();

                    String contourColorHex = ((MarkImpl)gs).getStroke().getColor().toString();

                    fillColor = Color.parseColor(fillColorHex);

                    contourColor = Color.parseColor(contourColorHex);

                    strokeWidth = Float.parseFloat(((MarkImpl)gs).getStroke().getWidth().toString());

                    wellNowName = ((MarkImpl)gs).getWellKnownName().toString();

                    fillOpacity = Double.parseDouble(((MarkImpl)gs).getFill().getOpacity().toString());

                    contourOpacity = Double.parseDouble(((MarkImpl)gs).getStroke().getOpacity().toString());

                    pointSize = Integer.parseInt(((PointSymbolizer)sym).getGraphic().getSize().toString());

                } else if(gs instanceof ExternalGraphicImpl)
                {
                    if(((ExternalGraphicImpl)gs).getInlineContent()!=null)
                    {
                        img = BitmapFactory.decodeStream(((ExternalGraphicImpl)gs).getInlineContent());
                    }

                }
            }


       } else
        {
            //Create default style
            HashMap<String, Integer> colorMap = Util.getRandomColor();
            contourColor = Color.rgb(colorMap.get("r"), colorMap.get("g"), colorMap.get("b"));
            fillColor = Color.argb(80, colorMap.get("r"), colorMap.get("g"), colorMap.get("b"));
            strokeWidth = 2.0f;

        }

        Style osmStyle = new Style(img, contourColor, strokeWidth, fillColor);


      /*  if(geometryType.getBinding() == Point.class)
        {

        } else if(geometryType.getBinding()== LineString.class)
        {

        } else if(geometryType.getBinding()== Polygon.class)
        {

        }*/

        return osmStyle;
    }
}
