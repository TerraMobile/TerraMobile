package br.org.funcate.terramobile.model.tilesource;

import android.graphics.drawable.Drawable;

import com.augtech.geoapi.geopackage.GeoPackage;
import com.augtech.geoapi.geopackage.GpkgTable;

import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import br.org.funcate.jgpkg.service.GeoPackageService;


/**
 * Implements a file system cache and provides cached tiles. This functions as a tile provider by
 * serving cached tiles for the supplied tile source.
 *
 * @author Marc Kurtz
 * @author Nicolas Gramlich
 *
 */
public class MapTileGeoPackageProvider extends MapTileModuleProviderBase {

    // ===========================================================
    // Constants
    // ===========================================================

    private static final Logger logger = LoggerFactory.getLogger(MapTileGeoPackageProvider.class);

    // ===========================================================
    // Fields
    // ===========================================================


    private final AtomicReference<ITileSource> mTileSource = new AtomicReference<ITileSource>();
    private String mLayerName = null;
    private GeoPackage mGeoPackage = null;
    private Map<Integer, Map<String, Integer>> tilesBoundsByZoomLevel = null;

    // ===========================================================
    // Constructors
    // ===========================================================



    public MapTileGeoPackageProvider(final ITileSource pTileSource, String layerName, GeoPackage geoPackage) {
        super(OpenStreetMapTileProviderConstants.NUMBER_OF_TILE_FILESYSTEM_THREADS, OpenStreetMapTileProviderConstants.TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);

        mTileSource.set(pTileSource);
        mLayerName=layerName;
        mGeoPackage=geoPackage;
    }

    // ===========================================================
    // Getter & Setter
    // ===========================================================

    // ===========================================================
    // Methods from SuperClass/Interfaces
    // ===========================================================

    @Override
    public boolean getUsesDataConnection() {
        return false;
    }

    @Override
    protected String getName() {
        return "GeoPackage Tiles";
    }

    @Override
    protected String getThreadGroupName() {
        return "geopackagetiles";
    }

    @Override
    protected Runnable getTileLoader() {
        return new TileLoader();
    }

    @Override
    public int getMinimumZoomLevel() {
        ITileSource tileSource = mTileSource.get();
        return tileSource != null ? tileSource.getMinimumZoomLevel() : MINIMUM_ZOOMLEVEL;
    }

    @Override
    public int getMaximumZoomLevel() {
        ITileSource tileSource = mTileSource.get();
        return tileSource != null ? tileSource.getMaximumZoomLevel()
                : microsoft.mappoint.TileSystem.getMaximumZoomLevel();
    }

    @Override
    public void setTileSource(final ITileSource pTileSource) {
        mTileSource.set(pTileSource);
    }

    // ===========================================================
    // Inner and Anonymous Classes
    // ===========================================================

    protected class TileLoader extends MapTileModuleProviderBase.TileLoader {

        @Override
        public Drawable loadTile(final MapTileRequestState pState) throws CantContinueException {

            ITileSource tileSource = mTileSource.get();
            if (tileSource == null) {
                return null;
            }


            final MapTile tile = pState.getMapTile();



                try {

                    if(!isValidTileForTileSource(tile.getX(), tile.getY(), tile.getZoomLevel()))
                    {
                        return null;
                    }

                    System.out.println("Reaching tile X: " + tile.getX() + " Y: " + tile.getY() + " Z: " +  tile.getZoomLevel());
                    byte[] b = getTileFromGPKG(tile.getX(), tile.getY(), tile.getZoomLevel());

                    if(b==null)
                    {
                        System.out.println("FAILED to reach tile X: " + tile.getX() + " Y: " + tile.getY() + " Z: " +  tile.getZoomLevel());
                        return null;
                    }
                    else
                    {
                        System.out.println("Reached tile X: " + tile.getX() + " Y: " + tile.getY() + " Z: " +  tile.getZoomLevel() + " == " +b.length);

                    }
                    InputStream is = new ByteArrayInputStream(b);
                    final Drawable drawable = tileSource.getDrawable(is);

                    return drawable;

                } catch (final BitmapTileSourceBase.LowMemoryException e) {
                    // low memory so empty the queue
                    logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
                    throw new CantContinueException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }
          //  }

            // If we get here then there is no file in the file cache
            return null;
        }

        private boolean isValidTileForTileSource(int col, int row, int level)
        {
            if(tilesBoundsByZoomLevel==null)
            {
                tilesBoundsByZoomLevel = new HashMap<Integer, Map<String, Integer>>();
            }

            Map<String, Integer> currentZoomLevelBounds = tilesBoundsByZoomLevel.get(level);
            if(currentZoomLevelBounds==null)
            {
                try {
                    //currentZoomLevelBounds=GeoPackageService.getTilesBounds(mGeoPackage, mLayerName, GpkgTable.TABLE_TYPE_TILES,level);
                    tilesBoundsByZoomLevel.put(level,currentZoomLevelBounds);
                } catch (Exception e) {
                    e.printStackTrace();
                    return false;
                }
            }
            int minTileRow = currentZoomLevelBounds.get("minTileRow");
            int maxTileRow = currentZoomLevelBounds.get("maxTileRow");

            int minTileCol = currentZoomLevelBounds.get("minTileCol");
            int maxTileCol = currentZoomLevelBounds.get("maxTileCol");


            if((row>=minTileRow)
                    &&(row<=maxTileRow)
                    &&(col>=minTileCol)
                    &&(col<=maxTileCol))
            {

                System.out.println("VALID TILE: col: " + col + " row: " + row + " lv: " + level);
                return true;
            }
            else
            {
                System.out.println("INVALID TILE: col: " + col + " row: " + row + " lv: " + level);
                return false;
            }
        }

        private byte[] getTileFromGPKG(int col, int row, int level) throws Exception {
            return GeoPackageService.getTile(mGeoPackage, mLayerName, col, row, level);
        }
        /**
         * A tile has loaded.
         */
        protected void tileLoaded(final MapTileRequestState pState, final Drawable pDrawable) {
            if (DEBUG_TILE_PROVIDERS) {
                logger.debug("TileLoader.tileLoaded() on provider: " + getName() + " with tile: "
                        + pState.getMapTile());
            }
            removeTileFromQueues(pState.getMapTile());
            pState.getCallback().mapTileRequestCompleted(pState, pDrawable);
        }

        void removeTileFromQueues(final MapTile mapTile) {
            synchronized (mQueueLockObject) {
                if (DEBUG_TILE_PROVIDERS) {
                    logger.debug("MapTileModuleProviderBase.removeTileFromQueues() on provider: "
                            + getName() + " for tile: " + mapTile);
                }
                mPending.remove(mapTile);
                mWorking.remove(mapTile);
            }
        }
    }
}

