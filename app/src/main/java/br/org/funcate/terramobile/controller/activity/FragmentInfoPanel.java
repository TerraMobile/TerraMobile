package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.dynamicforms.images.ImageUtilities;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.service.FeatureService;

/**
 * Created by Andre Carvalho on 28/08/15.
 */
public class FragmentInfoPanel extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.info_panel, container, false);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.info_panel_linear);

        FeatureInfoPanelActivity activity = (FeatureInfoPanelActivity)getActivity();

        Intent intent = activity.getIntent();
        Bundle extras = intent.getExtras();
        GridLayout table = (GridLayout) layout.findViewById(R.id.table_values);
        table.removeAllViews();

        if(extras.containsKey(FeatureService.FEATURE_DATA_CONTENT)) {
            extras = extras.getBundle(FeatureService.FEATURE_DATA_CONTENT);
            ArrayList<String> keys = new ArrayList<String>();
            if(extras.containsKey(FeatureService.FEATURE_DATA_KEYS)) {
                keys = extras.getStringArrayList(FeatureService.FEATURE_DATA_KEYS);
            }else{
                TextView defaultText = (TextView) layout.findViewById(R.id.default_text_info);
                if(defaultText!=null) defaultText.setVisibility(View.VISIBLE);
                return view;
            }

            if(keys.size()<=0) {
                TextView defaultText = (TextView) layout.findViewById(R.id.default_text_info);
                if(defaultText!=null) defaultText.setVisibility(View.VISIBLE);
                return view;
            }

            table.setColumnCount(2);
            table.setRowCount(keys.size());

            Iterator<String> itKeys = keys.iterator();
            int row = 0;
            while (itKeys.hasNext()) {

                TextView label = getTextViewComponent(layout.getContext(), 0, row, R.dimen.label_size);
                TextView value = getTextViewComponent(layout.getContext(), 1, row, R.dimen.text_size);

                String key = itKeys.next();
                label.setText(key);
                label.setTextColor(Color.BLACK);
                value.setTextColor(Color.BLACK);

                Object o = extras.get(key);
                String typeClass = o.getClass().getName();
                String s=null;

                if(String.class.getName().equals(typeClass)) {
                    s = (String) o;
                    value.setText(s);

                    table.addView(label);
                    table.addView(value);

                }else if(typeClass.equals("[Ljava.lang.Byte;")){

                    byte[] photo=(byte[])o;
                    ImageView img = new ImageView(activity);
                    img.setLayoutParams(getGridLayoutParams(1, row));
                    img.setImageBitmap(ImageUtilities.getBitmapFromBlob(photo));
                    table.addView(img);
                }
                row++;
            }
        }else{
            TextView defaultText = (TextView) layout.findViewById(R.id.default_text_info);
            if(defaultText!=null) defaultText.setVisibility(View.VISIBLE);
        }
        view.invalidate();
        return view;
    }

    private TextView getTextViewComponent(Context context, int column, int row, float textSize) {

        TextView textView = new TextView(context);
        textView.setTextSize(textSize);
        textView.setVisibility(View.VISIBLE);
        textView.setLayoutParams(getGridLayoutParams(column, row));

        return textView;
    }

    private GridLayout.LayoutParams getGridLayoutParams(int column, int row) {

        GridLayout.LayoutParams valueParams;
        valueParams = new GridLayout.LayoutParams();
        valueParams.height = GridLayout.LayoutParams.WRAP_CONTENT;
        valueParams.width = GridLayout.LayoutParams.WRAP_CONTENT;
        valueParams.rightMargin = 5;
        valueParams.topMargin = 5;
        valueParams.setGravity(Gravity.CENTER);
        valueParams.columnSpec = GridLayout.spec(column);
        valueParams.rowSpec = GridLayout.spec(row);
        return valueParams;
    }
}
