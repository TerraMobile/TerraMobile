package br.org.funcate.sldparser;

import org.geotools.filter.FilterFactoryImpl;
import org.geotools.styling.StrokeImpl;

/**
 * Created by bogo on 12/08/15.
 */
public class StrokeOurImpl extends StrokeImpl {
    public StrokeOurImpl() {
        super(new FilterFactoryImpl());
    }
}
