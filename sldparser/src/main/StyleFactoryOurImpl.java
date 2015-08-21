package br.org.funcate.sldparser;

import org.geotools.filter.FilterFactoryImpl;
import org.geotools.styling.StyleFactoryImpl;

/**
 * Created by bogo on 11/08/15.
 */
public class StyleFactoryOurImpl extends StyleFactoryImpl {

    public StyleFactoryOurImpl() {
        super(new FilterFactoryImpl());
    }
}
