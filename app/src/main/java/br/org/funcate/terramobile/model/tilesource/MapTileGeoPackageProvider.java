package br.org.funcate.terramobile.model.tilesource;

import android.graphics.drawable.Drawable;
import android.os.Environment;

import com.augtech.geoapi.geopackage.GeoPackage;

import org.osmdroid.tileprovider.ExpirableBitmapDrawable;
import org.osmdroid.tileprovider.IRegisterReceiver;
import org.osmdroid.tileprovider.MapTile;
import org.osmdroid.tileprovider.MapTileRequestState;
import org.osmdroid.tileprovider.constants.OpenStreetMapTileProviderConstants;
import org.osmdroid.tileprovider.modules.MapTileFileStorageProviderBase;
import org.osmdroid.tileprovider.modules.MapTileModuleProviderBase;
import org.osmdroid.tileprovider.tilesource.BitmapTileSourceBase;
import org.osmdroid.tileprovider.tilesource.ITileSource;
import org.osmdroid.tileprovider.tilesource.TileSourceFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
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

    // ===========================================================
    // Constructors
    // ===========================================================



    public MapTileGeoPackageProvider(final ITileSource pTileSource) {
        super(OpenStreetMapTileProviderConstants.NUMBER_OF_TILE_FILESYSTEM_THREADS, OpenStreetMapTileProviderConstants.TILE_FILESYSTEM_MAXIMUM_QUEUE_SIZE);

        mTileSource.set(pTileSource);
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

            // if there's no sdcard then don't do anything
/*            if (!getSdCardAvailable()) {
                if (DEBUGMODE) {
                    logger.debug("No sdcard - do nothing for tile: " + tile);
                }
                return null;
            }*/

            // Check the tile source to see if its file is available and if so, then render the
            // drawable and return the tile
            final File file = new File(TILE_PATH_BASE,
                    tileSource.getTileRelativeFilenameString(tile) + TILE_PATH_EXTENSION);
            if (file.exists()) {

                try {

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


                    // Check to see if file has expired
                    final long now = System.currentTimeMillis();
                    final long lastModified = file.lastModified();
/*                    final boolean fileExpired = lastModified < now - mMaximumCachedFileAge;

                    if (fileExpired && drawable != null) {
                        if (DEBUGMODE) {
                            logger.debug("Tile expired: " + tile);
                        }
                        drawable.setState(new int[] {ExpirableBitmapDrawable.EXPIRED });
                    }*/
                    return drawable;

                } catch (final BitmapTileSourceBase.LowMemoryException e) {
                    // low memory so empty the queue
                    logger.warn("LowMemoryException downloading MapTile: " + tile + " : " + e);
                    throw new CantContinueException(e);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            // If we get here then there is no file in the file cache
            return null;
        }

        private byte[] getTileFromGPKG(int col, int row, int level) throws Exception {
            return GeoPackageService.getTile(GeoPackageService.geoPackage, "landsat2012_tiles", col, row, level);
        }
    }
}

