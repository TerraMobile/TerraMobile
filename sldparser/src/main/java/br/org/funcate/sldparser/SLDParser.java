package br.org.funcate.sldparser;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;

import org.geotools.styling.FeatureTypeStyle;
import org.geotools.styling.NamedLayer;
import org.geotools.styling.PolygonSymbolizer;
import org.geotools.styling.Rule;
import org.geotools.styling.SLD;
import org.geotools.styling.Style;
import org.geotools.styling.StyledLayerDescriptor;
import org.geotools.xml.Parser;
import org.xml.sax.SAXException;

public class SLDParser {
	
	
	 public static void print() throws FileNotFoundException, IOException, SAXException, ParserConfigurationException
	    {
	    	 Parser parser = new Parser(new org.geotools.sld.v1_1.SLDConfiguration());


	    	 StyledLayerDescriptor sld = (StyledLayerDescriptor) parser.parse(new FileInputStream(new File("terraview-sld.xml")));

	         NamedLayer layer = (NamedLayer) sld.getStyledLayers()[0];
	         
	         Style style = layer.getStyles()[0];
	        
	         FeatureTypeStyle ftStyle = (FeatureTypeStyle) style.featureTypeStyles().get(0);
	         
	         Rule rule = ftStyle.rules().get(0);
	         
	         PolygonSymbolizer ps = (PolygonSymbolizer) rule.getSymbolizers()[0];
	         
	         Color fill = SLD.color(ps.getFill().getColor());
	         Color stroke = SLD.color(ps.getStroke().getColor());
	         
	         System.out.println("Fill Color:  R: " + fill.getRed() + " G: " + fill.getGreen() + " B: " + fill.getBlue());
	         
	         System.out.println("Stroke Color:  R: " + stroke.getRed() + " G: " + stroke.getGreen() + " B: " + stroke.getBlue());
	         
	         
	    }  
	 
	 public static void main(String[] args) throws FileNotFoundException, IOException, SAXException, ParserConfigurationException {
		SLDParser.print();
	}
}
