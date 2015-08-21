package br.org.funcate.sldparser;

import org.geotools.filter.FilterFactoryImpl;
import org.geotools.styling.FillImpl;

/**
 * Created by bogo on 12/08/15.
 */
public class FillOurImpl extends FillImpl {
    public FillOurImpl() {
        super(new FilterFactoryImpl());
    }
}
