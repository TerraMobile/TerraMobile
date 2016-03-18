package br.org.funcate.terramobile.controller.activity.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
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

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.List;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.Util;

/**
 * This AsyncTask upload a geopackage from the server
 */
public class UploadTask extends AsyncTask<String, String, Boolean> {
    private String uploadOriginFilePath;

    private static final String LINE_FEED = "\r\n";

    private MainActivity mainActivity;

    private File originFile;

    // creates a unique boundary based on time stamp
    String boundary = "===" + System.currentTimeMillis() + "===";

    public UploadTask(String uploadOriginFilePath, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.uploadOriginFilePath = uploadOriginFilePath;
    }

    @Override
    protected void onPreExecute() {
        mainActivity.showLoadingDialog(mainActivity.getString(R.string.send_project_upload));
    }

    public void onPostExecute() {
        try {
            mainActivity.getProgressDialog().dismiss();
        }catch (Exception e) {
            e.printStackTrace();
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

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addPart("user", new StringBody("userName", ContentType.TEXT_PLAIN));

            entityBuilder.addPart("password", new StringBody("password", ContentType.TEXT_PLAIN));

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

           /* URL url = new URL(urlToUpload[0]);
            HttpURLConnection urlConnection = (HttpURLConnection)url.openConnection();
            urlConnection.setConnectTimeout(5000);
            urlConnection.setUseCaches(false);
            urlConnection.setDoOutput(true); // indicates POST method
            urlConnection.setDoInput(true);        return false;
            urlConnection.setReadTimeout(5000);
            urlConnection.setRequestProperty("Content-Type",
                    "multipart/form-data; boundary=" + boundary);
            urlConnection.setRequestProperty("User-Agent", "TerraMobileApp");
            urlConnection.setRequestProperty("Test", "Bonjour");

            OutputStream outputStream = urlConnection.getOutputStream();
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(outputStream), true);

            addFormField("fileName",originFile.getName(), writer, outputStream);

            addFilePart("file", originFile, writer, outputStream);

            finish(writer, urlConnection);*/
/*
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            return false;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            return false;
        }*/
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

    private List<String> finish(PrintWriter writer, HttpURLConnection conn) throws IOException {
        List<String> response = new ArrayList<String>();

        writer.append(LINE_FEED).flush();
        writer.append("--" + boundary + "--").append(LINE_FEED);
        writer.close();

        // checks server's status code first
        int status = conn.getResponseCode();
        if (status == HttpURLConnection.HTTP_OK) {
            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    conn.getInputStream()));
            String line = null;
            while ((line = reader.readLine()) != null) {
                response.add(line);
            }
            reader.close();
            conn.disconnect();

        } else {
            throw new IOException("Server returned non-OK status: " + status);
        }
        onPostExecute();
        return response;
    }

}