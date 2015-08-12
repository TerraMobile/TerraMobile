package br.org.funcate.terramobile.model.service;

import android.content.Context;

import org.geotools.styling.AbstractSymbolizer;
import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.Rule;
import org.geotools.styling.Symbolizer;
import org.opengis.feature.simple.SimpleFeatureType;
import org.opengis.feature.type.GeometryType;
import org.osmdroid.bonuspack.kml.Style;

import java.util.List;
import java.util.Map;

import br.org.funcate.mobile.sld.SLDParser;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.StyleDAO;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;

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
        //TODO
        return new Style();
    }
}
