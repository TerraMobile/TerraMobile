package br.org.funcate.terramobile.controller.activity.tasks;

import android.os.AsyncTask;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
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
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceUtil;
import br.org.funcate.terramobile.util.Util;

/**
 * This AsyncTask receives a list of geopackages from the server
 */
public class ProjectListTask extends AsyncTask<String, String, JSONObject> {
    public MainActivity mainActivity;

    public ProjectListTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.showLoadingDialog(mainActivity.getString(R.string.loading_prj_list));
    }

    protected JSONObject doInBackground(String... url) {
        String packagesUrl = url[0];
        JSONObject jsonObject;
        String jsonContent;
        try {
            if(Util.isConnected(mainActivity)){
                HttpParams httpParams = new BasicHttpParams();
                HttpConnectionParams.setConnectionTimeout(httpParams, 5000);
                HttpConnectionParams.setSoTimeout(httpParams, 5000);

                HttpClient httpClient = new DefaultHttpClient(httpParams);
                HttpGet httpGet = new HttpGet(packagesUrl);
                HttpResponse response = httpClient.execute(httpGet);

                StatusLine statusLine = response.getStatusLine();
                int statusCode = statusLine.getStatusCode();
                if (statusCode == 200) {
                    StringBuilder stringBuilder = new StringBuilder();
                    HttpEntity httpEntity = response.getEntity();
                    InputStream content = httpEntity.getContent();
                    BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(content));
                    String line;
                    while ((line = bufferedReader.readLine()) != null)
                        stringBuilder.append(line);

                    jsonContent = stringBuilder.toString();
                    jsonObject = new JSONObject(jsonContent);

                    return jsonObject;
                }
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        try {
            mainActivity.getProgressDialog().dismiss();

            File appPath = ResourceUtil.getDirectory(mainActivity.getString(R.string.app_workspace_dir));

            ArrayList<Project> aLItems = new ArrayList<Project>();
            ArrayList<File> files = ResourceUtil.getGeoPackageFiles(appPath, mainActivity.getString(R.string.geopackage_extension));
            if(!files.isEmpty()) {
                for (File file : files) {
                    String destinationFilePath = appPath.getPath() + "/" + file.getName();

                    Project project = new Project();
                    project.setName(file.getName());
                    project.setFilePath(destinationFilePath);
                    project.setDownloaded(1);

                    aLItems.add(project);
                }
            }

            if (jsonObject != null) {
                JSONArray packages = jsonObject.getJSONArray("packages");
                for (int cont = 0; cont < packages.length(); cont++) {
                    JSONObject json = (JSONObject) packages.get(cont);
                    String pkg = json.getString("pkg");

                    String destinationFilePath = appPath.getPath() + "/" + pkg;

                    File file = ResourceUtil.getGeoPackageByName(appPath, mainActivity.getString(R.string.geopackage_extension), pkg);
                    if (file == null) {
                        Project project = new Project();
                        project.setName(pkg);
                        project.setFilePath(destinationFilePath);
                        project.setDownloaded(0);

                        aLItems.add(project);
                    }
                }
            }
//             else
//                Message.showErrorMessage(mainActivity, R.string.error, R.string.connection_failed);
            if(!aLItems.isEmpty())
                mainActivity.getProjectListFragment().setListItems(aLItems);
            else {
                mainActivity.getProjectListFragment().dismiss();
                Message.showErrorMessage(mainActivity, R.string.error, R.string.projects_not_found);
            }
        } catch (JSONException e) {
            Message.showErrorMessage(mainActivity, R.string.error, R.string.connection_failed);
            e.printStackTrace();
        }
    }
}