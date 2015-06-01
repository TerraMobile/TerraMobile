package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.util.ResourceUtil;

/**
 * DialogFragment to shows a list of packages availables to download
 *
 * Created by marcelo on 5/25/15.
 */
public class ListPackageFragment extends DialogFragment{
    private ListView lVPackage;

    private ArrayAdapter arrayAdapter;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.getDialog().setTitle(R.string.available_packages);
        return super.onCreateView(inflater, container, savedInstanceState);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        final View v = inflater.inflate(R.layout.fragment_list_package, null);

        lVPackage = (ListView)v.findViewById(R.id.lVPackage);

        this.arrayAdapter = new ArrayAdapter(getActivity(), android.R.layout.simple_list_item_1);
        new PackageListTask((MainActivity)this.getActivity()).execute("http://192.168.3.101/packages.json");

        lVPackage.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                File appPath = ResourceUtil.getDirectory(getResources().getString(R.string.app_workspace_dir));
                String destinationFilePath = appPath.getPath();
                String pkgName = lVPackage.getItemAtPosition(position).toString(); // Package name to concatenate with the url
                new DownloadTask(destinationFilePath, true, (MainActivity) getActivity()).execute(getResources().getString(R.string.gpkg_url));
            }
        });

        return new AlertDialog.Builder(
                getActivity()).
                setNegativeButton(R.string.btnCancel,new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick (DialogInterface dialog,int which){
                        dismiss();
                    }
                }).
                setView(v).
                setCancelable(true).
                create();
    }

    public void setListItems(ArrayList arrayList) {
        arrayAdapter.addAll(arrayList);
        lVPackage.setAdapter(arrayAdapter);
    }
}