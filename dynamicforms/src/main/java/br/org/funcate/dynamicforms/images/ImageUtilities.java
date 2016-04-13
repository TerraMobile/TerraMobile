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
     * This method will reduce the original image to the predefined dimensions.
     * If size this file is greater than the max size defined by MAX_IMAGE_FILE_SIZE,
     * then the reduce factor is applied until this file be reduced to minor size.
     * This overwrites the original file.
     * @param imagePath, the path to input file image
     * @return true in success or false otherwise
     */
    public static boolean resampleImage(String imagePath) throws Throwable {
        File imageFile = new File(imagePath);
        int reductionFactor=90;

        while (imageFile.length() > ImageUtilities.MAX_IMAGE_FILE_SIZE) {

            try {
                Bitmap outputBitmap = ImageUtilities.getScaledBitmap(imagePath, reductionFactor);
                if (outputBitmap == null) return false;
                byte[] blob = ImageUtilities.getBlobFromBitmap(outputBitmap, reductionFactor);
                if (blob==null || blob.length <= 0) return false;
                writeImageDataToFile(blob, imagePath);
                blob=null;
            } catch (IOException e) {
                e.printStackTrace();
                return false;
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
            imageFile = new File(imagePath);
            reductionFactor -= 10;
        }

        return true;
    }


    /**
     * Rescale one image to reduce the size.
     * @param imagePath, the path to JPEG image file
     * @return The smaller Bitmap.
     */
    public static Bitmap getScaledBitmap(String imagePath, int reductionFactor) throws Throwable {

        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = getImageDimensions(imagePath);
        int targetW = Math.round(bmOptions.outWidth * reductionFactor / 100);
        int targetH = Math.round(bmOptions.outHeight * reductionFactor / 100);

        return Bitmap.createScaledBitmap(BitmapFactory.decodeFile(imagePath), targetW, targetH, false);
    }

    public static BitmapFactory.Options getImageDimensions(String imagePath) {
        // Get the dimensions of the bitmap
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(imagePath, bmOptions);
        //String imageType = bmOptions.outMimeType;
        return bmOptions;
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

    public static byte[] getBlobFromBitmap(Bitmap image, int jpegQuality) throws IOException {

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        if(!image.compress(Bitmap.CompressFormat.JPEG, jpegQuality, stream)) {
            stream.close();
            return null;
        }
        byte[] imageBytes = stream.toByteArray();
        stream.close();
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
        if(imageData!=null) {
            if (img.exists() && img.delete()) {
                FileOutputStream fout = new FileOutputStream(imagePath);
                try {
                    fout.write(imageData);
                } finally {
                    fout.close();
                }
            }
        }
    }

    public static Bitmap getPic(int outputWidth, int outputHeight, String photoPath) {

        if(photoPath==null || photoPath.isEmpty()) {
            throw new IllegalArgumentException("Invalid parameter.");
        }
		/* There isn't enough memory to open up more than a couple camera photos */
		/* So pre-scale the target bitmap into which the file is decoded */

		/* Get the size of the image */
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        bmOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(photoPath, bmOptions);
        int photoW = bmOptions.outWidth;
        int photoH = bmOptions.outHeight;

		/* Figure out which way needs to be reduced less */
        int scaleFactor = 1;
        if ((outputWidth > 0) || (outputHeight > 0)) {
            scaleFactor = Math.min(photoW/outputWidth, photoH/outputHeight);
        }

		/* Set bitmap options to scale the image decode target */
        bmOptions.inJustDecodeBounds = false;
        bmOptions.inSampleSize = scaleFactor;
        bmOptions.inPurgeable = true;

		/* Decode the JPEG file into a Bitmap */
        return BitmapFactory.decodeFile(photoPath, bmOptions);
    }

}
