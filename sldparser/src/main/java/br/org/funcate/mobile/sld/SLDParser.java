
package br.org.funcate.mobile.sld;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.geotools.styling.Style;
import org.geotools.styling.StyleFactoryImpl;

public class SLDParser {
	
	 public static Style[] parse(String sldXml)
     {
        InputStream is = new ByteArrayInputStream(sldXml.getBytes());

        return SLDParser.parse(is);
    }
    public static Style[] parse(InputStream is) {

        org.geotools.styling.SLDParser parser = new org.geotools.styling.SLDParser(new StyleFactoryImpl(), is);

        Style[] styles = parser.readXML();

        return styles;
    }
}
