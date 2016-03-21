package br.org.funcate.terramobile.controller.activity.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.FileBody;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import java.io.File;
import java.io.IOException;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

/**
 * This AsyncTask upload a geopackage from the server
 */
public class UploadTask extends AsyncTask<String, String, Boolean> {
    private String uploadOriginFilePath;

    private static final String LINE_FEED = "\r\n";

    private MainActivity mainActivity;

    private File originFile;

    public UploadTask(String uploadOriginFilePath, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.uploadOriginFilePath = uploadOriginFilePath;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.showLoadingDialog(mainActivity.getString(R.string.send_project_upload));
    }

    @Override
    protected void onPostExecute(Boolean result) {
        try {
            if(result && originFile.exists()) {
                File uploadedDir = Util.getDirectory(ResourceHelper.getStringResource(R.string.app_workspace_uploaded_dir));
                Util.moveFile(originFile.getAbsolutePath(), uploadedDir.getAbsolutePath());
                // and moving the journal file too
                File originJournalFile = new File(originFile.getAbsolutePath() + "-journal");
                if(originJournalFile.exists()) {
                    Util.moveFile(originJournalFile.getAbsolutePath(), uploadedDir.getAbsolutePath());
                }
            }
        }catch (Exception e) {
            e.printStackTrace();
            Log.e("onPostExecute Exception", e.getMessage());
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Log.e("onPostExecute Exception", e.getMessage());
        }finally {
            mainActivity.getProgressDialog().dismiss();
        }
    }

    protected Boolean doInBackground(String... urlToUpload) {
        if (android.os.Debug.isDebuggerConnected())
            android.os.Debug.waitForDebugger(); // Para debugar é preciso colocar um breakpoint nessa linha

        if (urlToUpload[0].isEmpty()) {
            Log.e("URL missing", "Variable urlToDownload[0] is empty");
            return false;
        }

        if (uploadOriginFilePath.isEmpty()) {
            Log.e("Path missing", "Variable uploadOriginFilePath is empty");
            return false;
        }
        originFile = new File(uploadOriginFilePath);
        try {
            if (!originFile.exists()) {
                Log.e("Path missing", "Variable uploadOriginFilePath is empty");
                return false;
            }


            HttpParams httpParams = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);

            HttpConnectionParams.setSoTimeout(httpParams, 5000);

            HttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpPost httpPost = new HttpPost(urlToUpload[0]);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addPart("user", new StringBody("userName", ContentType.TEXT_PLAIN));

            entityBuilder.addPart("password", new StringBody("password", ContentType.TEXT_PLAIN));

            entityBuilder.addPart("fileName", new StringBody(originFile.getName(), ContentType.TEXT_PLAIN));

            entityBuilder.addPart("file", new FileBody(originFile));

            httpPost.setEntity(entityBuilder.build());

            HttpResponse response = httpClient.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();

            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {
                return true;
            }
            else
            {
                return false;
            }

        } catch (ClientProtocolException e) {
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

    }

    /**
     * Called when the cancel button of the ProgressDialog is touched
     * @param aBoolean
     */
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        mainActivity.getProgressDialog().dismiss();
        Message.showSuccessMessage(mainActivity, R.string.success, R.string.download_cancelled);
    }
}