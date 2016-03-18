package br.org.funcate.terramobile.controller.activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;

import java.util.ArrayList;

import br.org.funcate.jgpkg.exception.QueryException;
import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.tasks.UploadTask;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.model.exception.InvalidGeopackageException;
import br.org.funcate.terramobile.model.exception.StyleException;
import br.org.funcate.terramobile.model.exception.TerraMobileException;
import br.org.funcate.terramobile.model.gpkg.objects.GpkgLayer;
import br.org.funcate.terramobile.model.service.AppGeoPackageService;
import br.org.funcate.terramobile.model.service.LayersService;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.view.UploadLayerListAdapter;

/**
 * DialogFragment to show the user's credentials form on the settings menu
 *
 * Created by marcelo on 5/25/15.
 */
public class UploadProjectFragment extends DialogFragment{
    Project project;
//    private EditText eTServerURL;
    private UploadProjectController controller;

    private UploadLayerListAdapter uploadListAdapter;

    View view=null;
    public static UploadProjectFragment newInstance() {
        UploadProjectFragment fragment = new UploadProjectFragment();

        return fragment;
    }

    public Dialog onCreateDialog(Bundle savedInstanceState) {

        LayoutInflater inflater = LayoutInflater.from(getActivity());
        view = inflater.inflate(R.layout.fragment_upload_project, null);
        controller=new UploadProjectController();

       /* if(this.project!=null)
        {
            return null;
        }*/



  //      eTServerURL = (EditText) v.findViewById(R.id.serverURL);

/*        Button btnSave = (Button) v.findViewById(R.id.btnSave);
        Button btnCancel = (Button) v.findViewById(R.id.btnCancel);*/

/*
        String serverUrl = ((SettingsActivity)this.getActivity()).getController().getServerURL();
        if (serverUrl != null)
            eTServerURL.setText(serverUrl);

        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()) {
                    urlController.save(eTServerURL.getText().toString());
                    dismiss();
                }
            }
        });
*/

/*        btnCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });*/

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        builder.setNegativeButton(R.string.close, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dismiss();
            }
        });
        builder.setView(view);
        builder.setTitle(R.string.project_upload);
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.upload, null);
        final AlertDialog dialog = builder.create();


        dialog.show();

        Button b = dialog.getButton(AlertDialog.BUTTON_POSITIVE);
        b.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (uploadProject()) {
                    dismiss();
                }
            }
        });

        return dialog;
    }

    public void setProject(Project project)
    {
        this.project=project;
    }

    private void buildLayersList()
    {
        if(this.project!=null)
        {
            try {

                ArrayList<GpkgLayer> layers= AppGeoPackageService.getLayers(this.project, getActivity());

                ArrayList<GpkgLayer> editableLayers = LayersService.getEditableLayers(layers);

                ListView layersList = (ListView)view.findViewById(R.id.layersListView);

                uploadListAdapter = new UploadLayerListAdapter(getActivity(), R.id.layersListView, editableLayers);

                layersList.setAdapter(uploadListAdapter);

            } catch (InvalidGeopackageException e) {
                e.printStackTrace();
            } catch (QueryException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onActivityCreated(Bundle arg0) {
        if (getDialog() == null) {  // Returns mDialog
            // Tells DialogFragment to not use the fragment as a dialog, and so won't try to use mDialog
            setShowsDialog(false);
        }
        buildLayersList();
        super.onActivityCreated(arg0);  // Will now complete and not crash
    }

    private boolean uploadProject()
    {
        ListView listView = (ListView)view.findViewById(R.id.layersListView);
        ArrayList<GpkgLayer> layers = new ArrayList<GpkgLayer>();
        for (int i = 0; i < listView.getCount(); i++) {
            GpkgLayer layer = (GpkgLayer) listView.getChildAt(i).getTag();
            CheckBox cb = (CheckBox) listView.getChildAt(i).findViewById(R.id.cbUploadLayer);
            if(cb.isChecked())
            {
                layers.add(layer);
            }
        }

        if(layers.size()==0)
        {
            Message.showErrorMessage(getActivity(), R.string.fail, R.string.error_uploding_missing_layers);
            return false;
        }

        String fileName = null;
        try {
            fileName = AppGeoPackageService.createGeopackageForUpload(getActivity(), this.project, layers);
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(getActivity(), R.string.fail, e.getMessage());
        } catch (TerraMobileException e) {
            e.printStackTrace();
            Message.showErrorMessage(getActivity(), R.string.fail, e.getMessage());
        } catch (StyleException e) {
            e.printStackTrace();
            Message.showErrorMessage(getActivity(), R.string.fail, e.getMessage());
        }

        if(fileName!=null) {

            final String serverURL = ((MainActivity) getActivity()).getMainController().getServerURL();
            //UploadTask uploadTask = (UploadTask) new UploadTask(fileName, (MainActivity)getActivity()).execute(serverURL + "projectupload/");
            UploadTask uploadTask = (UploadTask) new UploadTask(fileName, (MainActivity) getActivity()).execute(serverURL + "/addprojects/userName/" + fileName);
            return true;
        }
        return false;

    }
}