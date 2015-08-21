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
package br.org.funcate.dynamicforms.views;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.FragmentDetail;
import br.org.funcate.dynamicforms.PictureActivity;
import br.org.funcate.dynamicforms.R;
import br.org.funcate.dynamicforms.camera.CameraActivity;
import br.org.funcate.dynamicforms.exceptions.CollectFormException;
import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.dynamicforms.markers.MarkersUtilities;
import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.dynamicforms.util.PositionUtilities;
import br.org.funcate.dynamicforms.util.ResourcesManager;

import static br.org.funcate.dynamicforms.FormUtilities.COLON;
import static br.org.funcate.dynamicforms.FormUtilities.UNDERSCORE;

/**
 * A custom pictures view.
 *
 * @author Andrea Antonello (www.hydrologis.com)
 */
public class GPictureView extends View implements GView {

    /**
     * The ids of the pictures.
     */
    private Map<String, Object> _pictures;

    private List<String> addedImages = new ArrayList<String>();

    private Map<Integer, String> addedIdsToImageViews = new HashMap<Integer, String>();

    private LinearLayout imageLayout;

    private FragmentDetail mFragmentDetail;

    public static int PICTURE_VIEW_RESULT;

    /**
     * @param context  the context to use.
     * @param attrs    attributes.
     * @param defStyle def style.
     */
    public GPictureView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    /**
     * @param context the context to use.
     * @param attrs   attributes.
     */
    public GPictureView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    /**
     * @param noteId                the id of the note this image belows to.
     * @param fragmentDetail        the fragment detail  to use.
     * @param attrs                 attributes.
     * @param requestCode           the code for starting the activity with result.
     * @param parentView            parent
     * @param label                 label
     * @param pictures              the value are the ids and binary data of the images.
     * @param constraintDescription constraints
     */
    public GPictureView(final long noteId, final FragmentDetail fragmentDetail, AttributeSet attrs, final int requestCode, LinearLayout parentView, String label, Map<String, Object> pictures,
                        String constraintDescription) {
        super(fragmentDetail.getActivity(), attrs);

        mFragmentDetail=fragmentDetail;

        _pictures = pictures;

        PICTURE_VIEW_RESULT = requestCode;

        final FragmentActivity activity = fragmentDetail.getActivity();
        LinearLayout textLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);
        textLayout.setLayoutParams(layoutParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        parentView.addView(textLayout);

        TextView textView = new TextView(activity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textView.setPadding(2, 2, 2, 2);
        textView.setText(label.replace(UNDERSCORE, " ").replace(COLON, " ") + " " + constraintDescription);
        textView.setTextColor(activity.getResources().getColor(R.color.formcolor));
        textLayout.addView(textView);

        final Button button = new Button(activity);
        button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        button.setPadding(15, 5, 15, 5);
        button.setText(R.string.take_picture);
        textLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(activity);
                //double[] gpsLocation = PositionUtilities.getGpsLocationFromPreferences(preferences);

                String imageName = ImageUtilities.getCameraImageName(null);
                Intent cameraIntent = new Intent(activity, CameraActivity.class);
                cameraIntent.putExtra(LibraryConstants.PREFS_KEY_CAMERA_IMAGENAME, imageName);
                cameraIntent.putExtra(LibraryConstants.SELECTED_POINT_ID, noteId);
                cameraIntent.putExtra(FormUtilities.MAIN_APP_WORKING_DIRECTORY, fragmentDetail.getWorkingDirectory());
               /* if (gpsLocation != null) {
                    cameraIntent.putExtra(LibraryConstants.LATITUDE, gpsLocation[1]);
                    cameraIntent.putExtra(LibraryConstants.LONGITUDE, gpsLocation[0]);
                    cameraIntent.putExtra(LibraryConstants.ELEVATION, gpsLocation[2]);
                }*/
                fragmentDetail.startActivityForResult(cameraIntent, requestCode);
            }
        });

        ScrollView scrollView = new ScrollView(activity);
        ScrollView.LayoutParams scrollLayoutParams = new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(scrollLayoutParams);
        parentView.addView(scrollView);

        imageLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT) ;
        imageLayoutParams.setMargins(10, 10, 10, 10);
        imageLayoutParams.leftMargin=20;
        imageLayout.setLayoutParams(imageLayoutParams);
        imageLayout.setOrientation(LinearLayout.HORIZONTAL);
        scrollView.addView(imageLayout);

        updateValueForm();
        try {
            refresh(activity);
        } catch (Exception e) {
            //GPLog.error(this, null, e);
            e.printStackTrace();
            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public void refresh(final Context context) throws Exception {

        if (_pictures != null && _pictures.size() > 0) {

            Set<String> imageKeys = _pictures.keySet();
            Iterator<String> itKeys = imageKeys.iterator();
            while (itKeys.hasNext()) {

                String imageId = itKeys.next();
                if(!_pictures.containsKey(imageId)) {
                    // TODO: Here, write a log in logfile
                    continue;
                }

                byte[] image = (byte[])_pictures.get(imageId);
                Bitmap imageBitmap = ImageUtilities.getBitmapFromBlob(image);
                //Bitmap thumbnail = ImageUtilities.makeThumbnail(imageBitmap);
                int viewID = new Integer(imageId).intValue();
                View v = imageLayout.findViewById(viewID);
                if(v==null) {
                    imageLayout.addView(getImageView(context, imageBitmap, viewID));
                    imageLayout.invalidate();
                }
            }
        }
        if (addedImages.size() > 0) {
            for (String imagePath : addedImages) {
                if ( ImageUtilities.isImagePath(imagePath) && !addedIdsToImageViews.containsValue(imagePath) ) {
                    byte[] image = ImageUtilities.getImageFromPath(imagePath, 2);
                    Bitmap imageBitmap = ImageUtilities.getBitmapFromBlob(image);
                    Bitmap thumbnail = ImageUtilities.makeThumbnail(imageBitmap);
                    imageLayout.addView(getImageView(context, thumbnail, thumbnail.getGenerationId()));
                    addedIdsToImageViews.put(thumbnail.getGenerationId(), imagePath);
                    imageLayout.invalidate();
                }
            }
        }

        if ( (_pictures == null || _pictures.size() == 0) && (addedImages == null || addedImages.size() == 0) ) {
            imageLayout.removeAllViewsInLayout();
        }

    }

    public ImageView getImageView(final Context context, final Bitmap thumbnail, int ID) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(102, 102));
        imageView.setPadding(5, 5, 5, 5);
        imageView.setImageBitmap(thumbnail);
        imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_black_1px));
        imageView.setId(ID);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                int photoId = v.getId();

                FragmentActivity activity = mFragmentDetail.getActivity();
                Intent intent = new Intent(activity, PictureActivity.class);

                if(addedIdsToImageViews.containsKey(photoId)){// pictures on session
                    intent.putExtra(FormUtilities.PICTURE_PATH_VIEW, addedIdsToImageViews.get(photoId));
                }else if(_pictures.containsKey(photoId)) {// pictures from database
                    intent.putExtra(FormUtilities.PICTURE_DB_VIEW, String.valueOf(photoId));
                }
                if(intent.hasExtra(FormUtilities.PICTURE_PATH_VIEW) || intent.hasExtra(FormUtilities.PICTURE_DB_VIEW)) {
                    intent.putExtra(FormUtilities.PICTURE_BITMAP_ID, photoId);
                    activity.startActivityForResult(intent, PICTURE_VIEW_RESULT);
                }
                else
                    Toast.makeText(getContext(), "Fail, the picture not found.", Toast.LENGTH_LONG).show();


                /*
                if(addedIdsToImageViews.containsKey(photoId)){// pictures on session
                    String imagePath = addedIdsToImageViews.remove(photoId);
                    addedImages.remove(imagePath);
                }else if(_pictures.containsKey(photoId)) {// pictures from database
                    _pictures.remove(photoId);
                }

                JSONArray form = null;
                try {
                    form = mFragmentDetail.getSelectedForm();
                } catch (JSONException jse) {
                    jse.printStackTrace();
                    Toast.makeText(getContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
                }

                if (form != null) {
                    try {
                        String json = encodeToJson();
                        FormUtilities.updatePicture(form, json);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                try {
                    refresh(getContext());
                } catch (Exception e) {
                    e.printStackTrace();
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                }*/
                /**
                 * open in markers to edit it
                 */
                // MarkersUtilities.launchOnImage(context, image);
                       /* try {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            Image image = imagesDbHelper.getImage(imageIdLong);
                            File tempDir = ResourcesManager.getInstance(context).getTempDir();
                            String ext = ".jpg";
                            if (image.getName().endsWith(".png"))
                                ext = ".png";
                            File imageFile = new File(tempDir, ImageUtilities.getTempImageName(ext));
                            byte[] imageData = imagesDbHelper.getImageData(image.getId());
                            ImageUtilities.writeImageDataToFile(imageData, imageFile.getAbsolutePath());

                            intent.setDataAndType(Uri.fromFile(imageFile), "image*//**//*"); //$NON-NLS-1$
                            context.startActivity(intent);
                        } catch (Exception e) {
                            //GPLog.error(this, null, e);
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                        }*/
                //Toast.makeText(getContext(), "Implement this action", Toast.LENGTH_LONG).show();

            }
        });
        return imageView;
    }

    public String getValue() {
        return "";
    }

    /**
     * Encode from the picture control variables to json format.
     * Using addedImages and _pictures to create the json:
     *
     * {added_paths:'path1,path2,path3', removed_ids:'10,32'}
     *
     * @return the json string
     */
    private String encodeToJson() {

        String imagePaths = "";
        if(addedImages!=null) {
            Iterator<String> iterator = addedImages.iterator();
            while (iterator.hasNext())
                imagePaths += (imagePaths.isEmpty() ? "" : FormUtilities.SEMICOLON) + iterator.next();
        }

        imagePaths = "'"+FormUtilities.TAG_ADDED_IMG+"'"+FormUtilities.COLON+"'"+imagePaths+"'";

        String imageIds = "";
        if(_pictures!=null) {
            Set<String> keySet = _pictures.keySet();
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext())
                imageIds += (imageIds.isEmpty() ? "" : FormUtilities.SEMICOLON) + iterator.next();
        }

        imageIds = "'" + FormUtilities.TAG_DATABASE_IMG + "'"+FormUtilities.COLON+"'" + imageIds + "'";
        String jsonImgs = "{"+imagePaths+ FormUtilities.COMMA +imageIds+"}";

        return jsonImgs;
    }

    /**
     * Make json with image information and put in selected form in memory.
     */
    private void updateValueForm() {
        JSONArray form = null;
        try {
            form = mFragmentDetail.getSelectedForm();
        } catch (JSONException jse) {
            jse.printStackTrace();
            Toast.makeText(getContext(), jse.getMessage(), Toast.LENGTH_LONG).show();
        }

        if (form != null) {
            try {
                String json = encodeToJson();
                FormUtilities.updatePicture(form, json);
            } catch (JSONException e) {
                e.printStackTrace();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void setOnActivityResult(Intent data) {

        Boolean hasPhoto = data.getBooleanExtra(LibraryConstants.OBJECT_EXISTS, false);
        String imgPath = "";
        if(hasPhoto) {

            imgPath = data.getStringExtra(FormUtilities.PHOTO_COMPLETE_PATH);
            addedImages.add(imgPath);
            updateValueForm();

            try {
                refresh(getContext());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
        if(data.hasExtra(FormUtilities.PICTURE_RESPONSE_REMOVE_VIEW)) {
            Boolean hasPictureRemoved = data.getBooleanExtra(FormUtilities.PICTURE_RESPONSE_REMOVE_VIEW, false);
            if(!hasPictureRemoved) {
                return;
            }

            Integer photoId = data.getIntExtra(FormUtilities.PICTURE_BITMAP_ID, -1);

            if(addedIdsToImageViews.containsKey(photoId)){// pictures on session
                String imagePath = addedIdsToImageViews.remove(photoId);
                addedImages.remove(imagePath);
            }else if(_pictures.containsKey(photoId.toString())) {// pictures from database
                _pictures.remove(photoId);
            }

            updateValueForm();

            try {
                refresh(getContext());
            } catch (Exception e) {
                e.printStackTrace();
                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }
    }
}

