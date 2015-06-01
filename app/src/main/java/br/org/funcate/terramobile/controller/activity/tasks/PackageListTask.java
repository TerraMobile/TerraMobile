package br.org.funcate.terramobile.controller.activity.tasks;

import android.net.ConnectivityManager;
import android.os.AsyncTask;

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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.util.Message;

/**
 * This AsyncTask receives the data from the server
 */
public class PackageListTask extends AsyncTask<String, String, JSONObject> {
    public MainActivity mainActivity;

    public PackageListTask(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.showLoadingDialog("Loading project list...");
    }

    protected JSONObject doInBackground(String... url) {
        ConnectivityManager cm = (ConnectivityManager)mainActivity.getSystemService(mainActivity.CONNECTIVITY_SERVICE);
        int wifi = ConnectivityManager.TYPE_WIFI;
        int mobile = ConnectivityManager.TYPE_MOBILE;
        if (cm.getNetworkInfo(mobile).isConnected() ||
                cm.getNetworkInfo(wifi).isConnected()) {
//            String packagesUrl = getResources().getString(R.string.gpkg_url); // URL of the service that provides the packages TODO: Change the url when the service is ready
            String packagesUrl = url[0];
            JSONObject jsonObject;
            String jsonContent;
            try {
                HttpClient httpClient = new DefaultHttpClient();
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
            } catch (ClientProtocolException e) {
                e.printStackTrace();
            } catch (JSONException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    @Override
    protected void onPostExecute(JSONObject jsonObject) {
        try {
            mainActivity.getProgressDialog().dismiss();
            if (jsonObject != null) {
                JSONArray packages = jsonObject.getJSONArray("packages"); // TODO: Change the array name
                ArrayList<String> aLItems = new ArrayList();
                for (int cont = 0; cont < packages.length(); cont++) {
                    JSONObject json = (JSONObject) packages.get(cont);
                    String pkg = json.getString("pkg"); // TODO: Change the array item name
                    aLItems.add(pkg);
                }
                if(!aLItems.isEmpty())
                    mainActivity.getListPackageFragment().setListItems(aLItems);
                else
                    Message.showErrorMessage(mainActivity, R.string.error, R.string.projects_not_found);
            } else
                Message.showErrorMessage(mainActivity, R.string.error, R.string.projects_not_found);
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onProgressUpdate(String... values) {
    }
}