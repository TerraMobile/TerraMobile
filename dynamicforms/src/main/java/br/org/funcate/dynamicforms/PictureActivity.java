package br.org.funcate.dynamicforms;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.Button;

import br.org.funcate.dynamicforms.util.LibraryConstants;
import br.org.funcate.dynamicforms.views.GPictureView;

/**
 * Created by Andre Carvalho on 20/08/15.
 */
public class PictureActivity extends FragmentActivity {

    private String _picturePath;
    private String _pictureTmpPath;
    private String _bitmapID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getIntent().hasExtra(FormUtilities.PICTURE_PATH_VIEW)) {
            // get data from extras
            _picturePath = getIntent().getStringExtra(FormUtilities.PICTURE_PATH_VIEW);
        }
        if (getIntent().hasExtra(FormUtilities.PICTURE_DB_VIEW)) {
            _pictureTmpPath = getIntent().getStringExtra(FormUtilities.PICTURE_DB_VIEW);
        }

        _bitmapID = getIntent().getStringExtra(FormUtilities.PICTURE_BITMAP_ID);

        setContentView(R.layout.fragment_picture);
    }

    public void removePictureClicked(View view) {

        Intent result = getIntent();
        result.putExtra(FormUtilities.PICTURE_BITMAP_ID, getBitmapID());
        result.putExtra(FormUtilities.PICTURE_RESPONSE_REMOVE_VIEW, true);
        setResult(GPictureView.PICTURE_VIEW_RESULT, result);
        finish();
    }

    public void cancelPictureClicked(View view) {
        Intent result = getIntent();
        result.putExtra(FormUtilities.PICTURE_BITMAP_ID, getBitmapID());
        result.putExtra(FormUtilities.PICTURE_RESPONSE_REMOVE_VIEW, false);
        setResult(GPictureView.PICTURE_VIEW_RESULT, result);
        finish();
    }

    public String getBitmapID() {
        return _bitmapID;
    }

    public String getPicturePath() {
        return _picturePath;
    }

    public String getPictureTmpPath() {
        return _pictureTmpPath;
    }
}
