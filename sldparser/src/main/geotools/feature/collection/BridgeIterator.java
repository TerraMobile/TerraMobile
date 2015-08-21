package org.geotools.feature.collection;

import java.io.Closeable;
import java.io.IOException;
import java.util.Iterator;

import org.geotools.feature.FeatureIterator;
import org.opengis.feature.Feature;

/**
 * A closeable iterator wrapped around a provided feature iterator. 
 * 
 * @see org.geotools.data.DataUtilities#iterator(org.geotools.feature.FeatureIterator)
 * @see org.geotools.data.DataUtilities#close(java.util.Iterator)
 * @author Jody Garnett (LISAsoft)
 */
public class BridgeIterator<F extends Feature> implements Iterator<F>, Closeable {
    FeatureIterator<F> delegate;
    public BridgeIterator(FeatureIterator<F> features) {
        this.delegate = features;
    }

    @Override
    public boolean hasNext() {
        return delegate.hasNext();
    }

    @Override
    public F next() {
        return delegate.next();
    }

    @Override
    public void remove() {
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}