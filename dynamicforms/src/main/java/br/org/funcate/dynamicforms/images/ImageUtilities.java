/*
 * Geopaparazzi - Digital field mapping on Android based devices
 * Copyright (C) 2010  HydroloGIS (www.hydrologis.com)
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package br.org.funcate.dynamicforms.images;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;

import br.org.funcate.dynamicforms.util.FileUtilities;
import br.org.funcate.dynamicforms.util.TimeUtilities;

/**
 * Images helper utilities.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class ImageUtilities {
    public static final int MAX_IMAGE_FILE_SIZE = 2097152; //2097152=(2*1024*1024) 2MB
    public static final int THUMBNAILWIDTH = 100;

    public static String getSketchImageName(Date date) {
        if (date == null)
            date = new Date();
        String currentDatestring = TimeUtilities.INSTANCE.TIMESTAMPFORMATTER_UTC.format(date);
        return "SKETCH_" + currentDatestring + ".png";
    }

    public static String getCameraImageName(Date date) {
        if (date == null)
            date = new Date();
        String currentDatestring = TimeUtilities.INSTANCE.TIMESTAMPFORMATTER_UTC.format(date);
        return "IMG_" + currentDatestring + ".jpg";
    }

    public static String getMapImageName(Date date) {
        if (date == null)
            date = new Date();
        String currentDatestring = TimeUtilities.INSTANCE.TIMESTAMPFORMATTER_UTC.format(date);
        return "MAP_" + currentDatestring + ".png";
    }

    public static boolean isImagePath(String path) {
        return path.toLowerCase().endsWith("jpg") || path.toLowerCase().endsWith("png");
    }

    /**
     * Get the default temporary image file name.
     *
     * @param ext and optional dot+extension to add. If null, '.jpg' is used.
     * @return the image name.
     */
    public static String getTempImageName(String ext) {
        if (ext == null) ext = ".jpg";
        return "tmp_gp_image" + ext;
    }

    /**
     * Get an image from a file by its path.
     *
     * @param imageFilePath the image path.
     * @param tryCount      times to try in 300 millis loop, in case the image is
     *                      not yet on disk. (ugly but no other way right now)
     * @return the image data or null.
     */
    public static byte[] getImageFromPath(String imageFilePath, int tryCount) {
        Bitmap image = BitmapFactory.decodeFile(imageFilePath);
        int count = 0;
        while (image == null && ++count < tryCount) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            image = BitmapFactory.decodeFile(imageFilePath);
        }
        if (image == null) return null;
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        return stream.toByteArray();
    }

    /**
     * Get an image and thumbnail from a file by its path.
     *
     * @param imageFilePath the image path.
     * @param tryCount      times to try in 300 millis loop, in case the image is
     *                      not yet on disk. (ugly but no other way right now)
     * @return the image and thumbnail data or null.
     */
    public static byte[][] getImageAndThumbnailFromPath(String imageFilePath, int tryCount) {
        byte[][] imageAndThumbNail = new byte[2][];

        // first read full image and check existence
        Bitmap image = BitmapFactory.decodeFile(imageFilePath);
        int count = 0;
        while (image == null && ++count < tryCount) {
            try {
                Thread.sleep(300);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            image = BitmapFactory.decodeFile(imageFilePath);
        }
        if (image == null) return null;

        int width = image.getWidth();
        int height = image.getHeight();

        // define sampling for thumbnail
        float sampleSizeF = (float) width / (float) THUMBNAILWIDTH;
        float newHeight = height/sampleSizeF;
        Bitmap thumbnail =  Bitmap.createScaledBitmap(image, THUMBNAILWIDTH, (int) newHeight, false);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();

        stream = new ByteArrayOutputStream();
        thumbnail.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] thumbnailBytes = stream.toByteArray();

        imageAndThumbNail[0] = imageBytes;
        imageAndThumbNail[1] = thumbnailBytes;
        return imageAndThumbNail;
    }

    /**
     * This method overwrites the original file.
     * @param targetW, the output width
     * @param targetH, the output height
     * @param imagePath, the path to input file image
     * @return true in sucess or false otherwise
     */
    public static boolean resampleImage(int targetW, int targetH, String imagePath) {
        Bitmap outputBitmap = getScaledBitmap(targetW, targetH, imagePath);
        if(outputBitmap==null) return false;
        byte[] blob = getBlobFromBitmap(outputBitmap);
        if(blob.length<=0) return false;
        try {

            writeImageDataToFile(blob, imagePath);
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    public static Bitmap getScaledBitmap(int targetW, int targetH, String imagePath) {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

        // Determine how much to scale down the image
        int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

        // Decode the image file into a Bitmap sized to fill the View
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

        Bitmap bitmap = BitmapFactory.decodeFile(imagePath, bmOptions);
        return bitmap;
    }

    public static Bitmap makeThumbnail(Bitmap image) {
        if (image == null) return null;

        int width = image.getWidth();
        int height = image.getHeight();

        // define sampling for thumbnail
        float sampleSizeF = (float) width / (float) THUMBNAILWIDTH;
        float newHeight = height/sampleSizeF;
        Bitmap thumbnail =  Bitmap.createScaledBitmap(image, THUMBNAILWIDTH, (int) newHeight, false);
        return thumbnail;
    }

    public static Bitmap getBitmapFromBlob(byte[] image) {
        Bitmap bitmap = BitmapFactory.decodeByteArray(image, 0, image.length);
        return bitmap;
    }

    public static byte[] getBlobFromBitmap(Bitmap image) {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        image.compress(Bitmap.CompressFormat.JPEG, 90, stream);
        byte[] imageBytes = stream.toByteArray();

        return imageBytes;
    }

    /**
     * Write am image to disk. If exists File into the path, this will remove.
     *
     * @param imageData the data to write.
     * @param imagePath the path to write to.
     * @throws IOException
     */
    public static void writeImageDataToFile(byte[] imageData, String imagePath) throws IOException {
        File img = new File(imagePath);
        if(img.exists() && img.delete()) {
            FileOutputStream fout = new FileOutputStream(imagePath);
            try {
                fout.write(imageData);
            } finally {
                fout.close();
            }
        }
    }
}
