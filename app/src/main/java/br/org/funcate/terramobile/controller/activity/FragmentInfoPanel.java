package br.org.funcate.terramobile.controller.activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Iterator;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.service.FeatureService;

/**
 * Created by Andre Carvalho on 28/08/15.
 */
public class FragmentInfoPanel extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.info_panel, container, false);
        TableLayout tableLayout = (TableLayout) view.findViewById(R.id.info_panel_table);

        FeatureInfoPanelActivity activity = (FeatureInfoPanelActivity)getActivity();
        Intent intent = activity.getIntent();
        Bundle extras = intent.getExtras();

        if(extras.containsKey(FeatureService.FEATURE_DATA_CONTENT)) {
            extras = extras.getBundle(FeatureService.FEATURE_DATA_CONTENT);
            ArrayList<String> keys = new ArrayList<String>();
            if(extras!=null && extras.containsKey(FeatureService.FEATURE_DATA_KEYS)) {
                keys = extras.getStringArrayList(FeatureService.FEATURE_DATA_KEYS);
            }

            Iterator<String> itKeys = (keys!=null)?(keys.iterator()):(null);
            boolean backgroundControl=true;

            if(itKeys!=null) {
                while (itKeys.hasNext()) {

                    if(extras==null) continue;

                    String key = itKeys.next();
                    Object o = extras.get(key);
                    String typeClass = o.getClass().getName();

                    TableRow tableRow = new TableRow(tableLayout.getContext());

                    backgroundControl = !backgroundControl;
                    if (backgroundControl)
                        tableRow.setBackgroundColor(getResources().getColor(R.color.info_panel_background));

                    TextView label = getTextViewComponent(tableLayout.getContext(), key, getResources().getInteger(R.integer.label_size));
                    label.setLayoutParams(getTableLayoutParams(3));
                    tableRow.addView(label);

                    if (String.class.getName().equals(typeClass)) {
                        String s = (String) o;
                        TextView value = getTextViewComponent(tableLayout.getContext(), s, getResources().getInteger(R.integer.value_size));
                        value.setLayoutParams(getTableLayoutParams(3));
                        tableRow.addView(value);
                    }

                    tableLayout.addView(tableRow);
                }
            }
        }else {
            TextView defaultText = (TextView) tableLayout.findViewById(R.id.default_text_info);
            if(defaultText!=null) defaultText.setVisibility(View.VISIBLE);
        }

        view.invalidate();
        return view;
    }

    private TextView getTextViewComponent(Context context, String text, float textSize) {

        TextView textView = new TextView(context);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        textView.setText(text);
        textView.setTextColor(Color.BLACK);
        textView.setVisibility(View.VISIBLE);

        return textView;
    }

    private TableRow.LayoutParams getTableLayoutParams(int weight) {
        int width = 0;
        int height = ViewGroup.LayoutParams.MATCH_PARENT;

        return getTableLayoutParams(width, height, weight);
    }

    private TableRow.LayoutParams getTableLayoutParams(int width, int height, int weight) {

        return new TableRow.LayoutParams( width, height, weight );
    }
}
