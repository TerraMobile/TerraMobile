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
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import br.org.funcate.dynamicforms.FormUtilities;
import br.org.funcate.dynamicforms.FragmentDetail;
import br.org.funcate.dynamicforms.PictureActivity;
import br.org.funcate.dynamicforms.R;
import br.org.funcate.dynamicforms.camera.CameraActivity;
import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.dynamicforms.util.LibraryConstants;

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
    private Map<String, Map<String, String>> _pictures;

    private Map<String, String> newImagesFromCamera = new HashMap<String, String>();

    private Map<String, String> addedIdsToImageViews = new HashMap<String, String>();

    private LinearLayout imageLayout;

    private FragmentDetail mFragmentDetail;

    /* define dimensions to thumbnail imageView  */
    private int thumbnailWidth;
    private int thumbnailHeight;

    public static int PICTURE_VIEW_RESULT;

    /**
     * @param context  the context to use.
     * @param attrs    attributes.
     * @param defStyle def style.
     */
    public GPictureView(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
        thumbnailWidth=context.getResources().getInteger(R.integer.thumbnail_width);
        thumbnailHeight=context.getResources().getInteger(R.integer.thumbnail_height);

    }

    /**
     * @param context the context to use.
     * @param attrs   attributes.
     */
    public GPictureView(Context context, AttributeSet attrs) {

        super(context, attrs);
        thumbnailWidth=context.getResources().getInteger(R.integer.thumbnail_width);
        thumbnailHeight=context.getResources().getInteger(R.integer.thumbnail_height);

    }

    /**
     * @param fragmentDetail        the fragment detail  to use.
     * @param attrs                 attributes.
     * @param requestCode           the code for starting the activity with result.
     * @param parentView            parent
     * @param label                 label
     * @param pictures              the value are the ids and binary data of the images.
     * @param constraintDescription constraints
     */
    public GPictureView(final FragmentDetail fragmentDetail, AttributeSet attrs, final int requestCode, LinearLayout parentView, String label, Map<String, Map<String, String>> pictures,
                        String constraintDescription) {
        super(fragmentDetail.getActivity(), attrs);

        thumbnailWidth=fragmentDetail.getActivity().getResources().getInteger(R.integer.thumbnail_width);
        thumbnailHeight=fragmentDetail.getActivity().getResources().getInteger(R.integer.thumbnail_height);

        mFragmentDetail=fragmentDetail;

        _pictures = pictures;

        PICTURE_VIEW_RESULT = requestCode;

        final FragmentActivity activity = fragmentDetail.getActivity();
        LinearLayout textLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(10, 10, 10, 10);
        textLayout.setPadding(10, 5, 10, 5);
        textLayout.setLayoutParams(layoutParams);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        parentView.addView(textLayout);

        TextView textView = new TextView(activity);
        textView.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        textView.setPadding(2, 2, 2, 2);
        String t = label.replace(UNDERSCORE, " ").replace(COLON, " ") + " " + constraintDescription;
        textView.setText(t);
        textView.setTextColor(activity.getResources().getColor(R.color.formcolor));
        textLayout.addView(textView);

        final Button button = new Button(activity);
        button.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
        button.setPadding(15, 5, 15, 5);
        button.setText(R.string.take_picture);
        textLayout.addView(button);

        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                Intent cameraIntent = new Intent(activity, CameraActivity.class);

                cameraIntent.putExtra(FormUtilities.MAIN_APP_WORKING_DIRECTORY, fragmentDetail.getWorkingDirectory());

                fragmentDetail.startActivityForResult(cameraIntent, requestCode);
            }
        });

        ScrollView scrollView = new ScrollView(activity);
        ScrollView.LayoutParams scrollLayoutParams = new ScrollView.LayoutParams(LayoutParams.MATCH_PARENT,
                LayoutParams.WRAP_CONTENT);
        scrollView.setLayoutParams(scrollLayoutParams);
        scrollView.setHorizontalScrollBarEnabled(true);
        scrollView.setOverScrollMode(HorizontalScrollView.OVER_SCROLL_ALWAYS);
        parentView.addView(scrollView);

        imageLayout = new LinearLayout(activity);
        LinearLayout.LayoutParams imageLayoutParams = new LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT,
                LayoutParams.WRAP_CONTENT);
        imageLayout.setLayoutParams(imageLayoutParams);
        imageLayout.setPadding(15, 5, 15, 5);
        imageLayout.setOrientation(LinearLayout.HORIZONTAL);
        imageLayout.setDescendantFocusability(ViewGroup.FOCUS_BLOCK_DESCENDANTS);
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

        imageLayout.removeAllViewsInLayout();
        int increaseTime = 0;// using on runnable

        if (_pictures != null && _pictures.size() > 0) {

            Set<String> imageKeys = _pictures.keySet();
            for (final String imageId : imageKeys) {

                if (!_pictures.containsKey(imageId)) {
                    // TODO: Here, write a log in logfile
                    continue;
                } else {
                    Map<String, String> imagePaths = _pictures.get(imageId);
                    final String imagePath = imagePaths.get("thumbnail");
                    final ProgressBar pgBar = getProgressBar(context);
                    imageLayout.addView(pgBar);
                    increaseTime++;

                    Handler handler = new Handler();
                    handler.postDelayed(new Runnable() {
                        public void run() {
/*
                            byte[] image = ImageUtilities.getImageFromPath(imagePath, 2);
                            if (image != null && image.length > 0) {
                                Bitmap imageBitmap = ImageUtilities.getBitmapFromBlob(image);
                                try {
                                    image = null;
                                } catch (Throwable t) {
                                    t.printStackTrace();
                                    getDefaultBitmap();
                                    return;
                                }

                                Bitmap thumbnail = ImageUtilities.makeThumbnail(imageBitmap);
                                if (imageBitmap.isRecycled()) imageBitmap.recycle();
                                */
                            if(imagePath!=null && !imagePath.isEmpty() && (new File(imagePath)).exists()) {
                                pgBar.setVisibility(View.GONE);
                                imageLayout.addView(getImageView(context, imagePath, imageId));
                                imageLayout.invalidate();
                                //if (thumbnail.isRecycled()) thumbnail.recycle();
                            } else {
                                getDefaultBitmap();
                            }
                        }

                        public void getDefaultBitmap() {
                            pgBar.setVisibility(View.GONE);
                            Bitmap bitmapError = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_action_highlight_remove);
                            imageLayout.addView(getImageView(context, bitmapError, imageId));
                            imageLayout.invalidate();
                            // if (bitmapError.isRecycled()) bitmapError.recycle();
                        }
                    }, increaseTime * 1000 + 1000);
                }
            }
        }
        if(newImagesFromCamera != null && newImagesFromCamera.size() > 0) {
            Set<String> imageKeys = newImagesFromCamera.keySet();
            for (final String imageId : imageKeys) {

                final String imagePath = newImagesFromCamera.get(imageId);
                final ProgressBar pgBar = getProgressBar(context);
                imageLayout.addView(pgBar);
                increaseTime++;

                Handler handler = new Handler();
                handler.postDelayed(new Runnable() {
                    public void run() {
                        try {

                            if(imagePath!=null && !imagePath.isEmpty() && (new File(imagePath)).exists()) {

                                pgBar.setVisibility(View.GONE);
                                imageLayout.addView(getImageView(context, imagePath, imageId));
                                imageLayout.invalidate();

                                /*
                                if (!ImageUtilities.resampleImage(imagePath)) {
                                    getDefaultBitmap();
                                    return;
                                }
                                byte[] image = ImageUtilities.getImageFromPath(imagePath, 2);
                                if (image != null) {
                                    Bitmap imageBitmap = ImageUtilities.getBitmapFromBlob(image);
                                    image = null;
                                    Bitmap thumbnail = ImageUtilities.makeThumbnail(imageBitmap);
                                    imageBitmap = null;
                                    pgBar.setVisibility(View.GONE);
                                    imageLayout.addView(getImageView(context, thumbnail, imageId));
                                    thumbnail = null;
                                    imageLayout.invalidate();
                                } else {
                                    getDefaultBitmap();
                                }
                                */
                            }
                        } catch (Throwable e) {
                            e.printStackTrace();
                            getDefaultBitmap();
                        }
                    }

                    public void getDefaultBitmap() {
                        pgBar.setVisibility(View.GONE);
                        Bitmap bitmapError = BitmapFactory.decodeResource(getResources(), R.drawable.ic_stat_action_highlight_remove);
                        imageLayout.addView(getImageView(context, bitmapError, imageId));
                        imageLayout.invalidate();
                    }
                }, increaseTime * 1000 + 1000);

                if (!addedIdsToImageViews.containsValue(imagePath))
                    addedIdsToImageViews.put(imageId, imagePath);
            }
        }

        imageLayout.invalidate();

    }

    public ProgressBar getProgressBar(final Context context) {
        ProgressBar progressBar = new ProgressBar(context);
        // Get the Drawable custom_progressbar
        Drawable draw=getResources().getDrawable(R.drawable.customprogressbar);
        // set the drawable as progress drawable
        progressBar.setProgressDrawable(draw);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setPadding(5, 5, 5, 5);
        progressBar.setLayoutParams(new LinearLayout.LayoutParams(thumbnailWidth, thumbnailHeight));
        return progressBar;
    }

    private ImageView getImageView(final Context context, String photoPath, String uuid) {
        Bitmap bitmap = ImageUtilities.getPic(thumbnailWidth, thumbnailHeight, photoPath);
        return this.getImageView(context, bitmap, uuid);
    }

    private ImageView getImageView(final Context context, Bitmap photo, String uuid) {
        ImageView imageView = new ImageView(context);
        imageView.setLayoutParams(new LinearLayout.LayoutParams(thumbnailWidth, thumbnailHeight));
        imageView.setPadding(5, 5, 5, 5);
        imageView.setImageBitmap(photo);
        imageView.setBackgroundDrawable(getResources().getDrawable(R.drawable.border_black_1px));
        imageView.setTag(uuid);
        imageView.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String photoId = (String)v.getTag();

                FragmentActivity activity = mFragmentDetail.getActivity();
                Intent intent = new Intent(activity, PictureActivity.class);

                if(addedIdsToImageViews.containsKey(photoId)){// pictures on session
                    intent.putExtra(FormUtilities.PICTURE_PATH_VIEW, addedIdsToImageViews.get(photoId));
                }else if(_pictures.containsKey(photoId)) {// pictures from database

                    Map<String, String> imagePaths = _pictures.get(photoId);
                    String imagePath = imagePaths.get("display");

                    intent.putExtra(FormUtilities.PICTURE_DB_VIEW, imagePath);// Image temporary path
                }
                if(intent.hasExtra(FormUtilities.PICTURE_PATH_VIEW) || intent.hasExtra(FormUtilities.PICTURE_DB_VIEW)) {
                    intent.putExtra(FormUtilities.PICTURE_BITMAP_ID, photoId);
                    activity.startActivityForResult(intent, PICTURE_VIEW_RESULT);
                }
                else
                    Toast.makeText(getContext(), "Fail, the picture not found.", Toast.LENGTH_LONG).show();


                /*
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
     * Using newImagesFromCamera and _pictures to create the json:
     *
     * {added_paths:'path1,path2,path3', removed_ids:'10,32'}
     *
     * @return the json string
     */
    private String encodeToJson() {

        String imagePaths = "";
        if(newImagesFromCamera != null) {
            Set<String> imageKeys = newImagesFromCamera.keySet();
            for (String imageKey : imageKeys) {
                imagePaths += (imagePaths.isEmpty() ? "" : FormUtilities.SEMICOLON) + newImagesFromCamera.get(imageKey);
            }
        }

        imagePaths = "'"+FormUtilities.TAG_ADDED_IMG+"'"+FormUtilities.COLON+"'"+imagePaths+"'";

        String imageIds = "";
        if(_pictures!=null) {
            Set<String> keySet = _pictures.keySet();
            for (String aKey : keySet)
                imageIds += (imageIds.isEmpty() ? "" : FormUtilities.SEMICOLON) + aKey;
        }

        imageIds = "'" + FormUtilities.TAG_DATABASE_IMG + "'"+FormUtilities.COLON+"'" + imageIds + "'";

        return "{"+imagePaths+ FormUtilities.COMMA +imageIds+"}";
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
        String imgPath;

        if (hasPhoto) {// response of the CameraActivity with one image

            imgPath = data.getStringExtra(FormUtilities.PHOTO_COMPLETE_PATH);

            // TODO: save azimuth on database together with the picture.
            double azimuth = data.getDoubleExtra(LibraryConstants.AZIMUTH, 0);

            String uuid = java.util.UUID.randomUUID().toString();

            newImagesFromCamera.put(uuid, imgPath);
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
            if(!hasPictureRemoved) {// response of the PictureActivity, used to remove image
                return;
            }

            String photoId = data.getStringExtra(FormUtilities.PICTURE_BITMAP_ID);

            if(addedIdsToImageViews.containsKey(photoId)){// pictures on session
                addedIdsToImageViews.remove(photoId);
                newImagesFromCamera.remove(photoId);
            }else if(_pictures.containsKey(photoId)) {// pictures from database
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

