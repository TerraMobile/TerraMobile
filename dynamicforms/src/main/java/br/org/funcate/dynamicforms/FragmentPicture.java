package br.org.funcate.dynamicforms;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import br.org.funcate.dynamicforms.images.ImageUtilities;

/**
 * Created by Andre Carvalho on 20/08/15.
 */
public class FragmentPicture extends Fragment{

    private PictureActivity activity;

    @Override
    public void onCreate( Bundle savedInstanceState ) {
        super.onCreate(savedInstanceState);

        activity = (PictureActivity) getActivity();
    }

    @Override
    public void onActivityCreated( Bundle savedInstanceState ) {
        super.onActivityCreated(savedInstanceState);

    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.picture, container, false);
        ImageView toRemovePicture = (ImageView) view.findViewById(R.id.to_remove_picture);
        Button removeButton = (Button) view.findViewById(R.id.removeButton);
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);

        toRemovePicture.setImageBitmap(getPicture());

        /*removeButton.setOnClickListener(this);
        cancelButton.setOnClickListener(this);*/

        return view;
    }

    private Bitmap getPicture() {
        String picturePath = activity.getPicturePath();
        String pictureID = activity.getPictureID();
        Bitmap image = null;

        if(picturePath!=null && !picturePath.isEmpty()) {
            image = getPictureFromPath(picturePath);
        }

        if(pictureID!=null && !pictureID.isEmpty()) {
            image = getPictureFromDB(pictureID);
        }

        return image;
    }

    private Bitmap getPictureFromPath(String path) {
        Bitmap image = null;
        if(ImageUtilities.isImagePath(path)) {
            byte[] blob = ImageUtilities.getImageFromPath(path, 2);
            image = ImageUtilities.getBitmapFromBlob(blob);
        }
        return image;
    }

    private Bitmap getPictureFromDB(String id) {
        Bitmap image = null;
        return image;
    }

    @Override
    public void onAttach( Activity activity ) {
        super.onAttach(activity);

    }

    /**
     * Called when a view has been clicked.
     *
     * @param v The view that was clicked.
     */
    /*
    @Override
    public void onClick(View v) {
        Button button = (Button)v;
        Intent result = new Intent();
        result.putExtra(FormUtilities.PICTURE_BITMAP_ID, activity.getBitmapID());
        if(button.getId() == R.id.removeButton ) {
            result.putExtra(FormUtilities.PICTURE_RESPONSE_REMOVE_VIEW, true);
            activity.setResult(FormUtilities.PICTURE_VIEW_RESULT_CODE, result);
        }else if(button.getId() == R.id.cancelButton ) {
            result.putExtra(FormUtilities.PICTURE_RESPONSE_REMOVE_VIEW, false);
            activity.setResult(FormUtilities.PICTURE_VIEW_RESULT_CODE, result);
        }
        //activity.finish();
    }*/
}
