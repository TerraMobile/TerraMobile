package br.org.funcate.terramobile.controller.activity.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.entity.mime.content.StringBody;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.db.ApplicationDatabase;
import br.org.funcate.terramobile.model.db.DatabaseFactory;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.domain.Project;
import br.org.funcate.terramobile.model.exception.DAOException;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.Message;
import br.org.funcate.terramobile.util.Util;

/**
 * This AsyncTask downloads a geopackage from the server
 */
public class DownloadTask extends AsyncTask<String, String, Boolean> {
    private String unzipDestinationFilePath;
    private String downloadDestinationFilePath;
    private ArrayList<String> mFiles;

    private MainActivity mainActivity;

    private File destinationFile;

    private String projectFileName;

    private String projectUUID;

    private int projectStatus;

    private boolean error;

    public DownloadTask(String downloadDestinationFilePath, String unzipDestinationFilePath, String projectFileName, String projectUUID, int projectStatus, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.unzipDestinationFilePath = unzipDestinationFilePath;
        this.downloadDestinationFilePath = downloadDestinationFilePath;
        this.projectFileName = projectFileName;
        this.projectUUID = projectUUID;
        this.projectStatus = projectStatus;
        mFiles = new ArrayList<String>();
    }

    @Override
    protected void onPreExecute() {
        mainActivity.showProgressDialog(mainActivity.getString(R.string.downloading));
    }

    protected Boolean doInBackground(String... urlToDownload) {
        if(android.os.Debug.isDebuggerConnected()) android.os.Debug.waitForDebugger(); // Para debugar é preciso colocar um breakpoint nessa linha

        if (urlToDownload[0].isEmpty()) {
            Log.e("URL missing", "Variable urlToDownload[0] is empty");
            return false;
        }

        if (downloadDestinationFilePath.isEmpty()) {
            Log.e("Path missing", "Variable downloadDestinationFilePath is empty");
            return false;
        }
        destinationFile = new File(downloadDestinationFilePath);
        try {
            if (!destinationFile.exists()) {
                if (!destinationFile.createNewFile()) {
                    return null;
                }
            }else {
                if(destinationFile.delete()) {
                    if(!destinationFile.createNewFile())
                        return null;
                }
                else return null;
            }

            HttpParams httpParams = new BasicHttpParams();

            HttpConnectionParams.setConnectionTimeout(httpParams, 5000);

            HttpConnectionParams.setSoTimeout(httpParams, 5000);

            HttpClient httpClient = new DefaultHttpClient(httpParams);

            HttpPost httpPost = new HttpPost(urlToDownload[0]);

            ArrayList<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);

            MultipartEntityBuilder entityBuilder = MultipartEntityBuilder.create().setMode(HttpMultipartMode.BROWSER_COMPATIBLE);

            entityBuilder.addPart("projectStatus", new StringBody(Integer.toString(projectStatus), ContentType.TEXT_PLAIN));

            entityBuilder.addPart("projectId", new StringBody(projectUUID, ContentType.TEXT_PLAIN));

            entityBuilder.addPart("projectName", new StringBody(projectFileName, ContentType.TEXT_PLAIN));

            entityBuilder.addPart("user", new StringBody("userName", ContentType.TEXT_PLAIN));

            entityBuilder.addPart("password", new StringBody("password", ContentType.TEXT_PLAIN));

            httpPost.setEntity(entityBuilder.build());

            HttpResponse response = httpClient.execute(httpPost);

            StatusLine statusLine = response.getStatusLine();

            int statusCode = statusLine.getStatusCode();

            if (statusCode == 200) {

                StringBuilder stringBuilder = new StringBuilder();

                HttpEntity httpEntity = response.getEntity();

                InputStream content = httpEntity.getContent();


                long totalSize = httpEntity.getContentLength();
                if(totalSize == -1){
                    this.cancel(true);
                    error = true;
                    return false;
                }

                if(content == null) {
                    this.cancel(true);
                    error = true;
                    return false;
                }

                InputStream inputStream = new BufferedInputStream(content);
                OutputStream fileOutput = new FileOutputStream(destinationFile);

                byte buffer[] = new byte[1024];

                int bufferLength;
                long total = 0;
                while ((bufferLength = inputStream.read(buffer)) != -1) {
                    if(isCancelled()) {
                        fileOutput.flush();
                        fileOutput.close();
                        inputStream.close();
                        return false;
                    }
                    total += bufferLength;
                    publishProgress("" + (int) ((total * 100) / totalSize), mainActivity.getString(R.string.downloading));
                    fileOutput.write(buffer, 0, bufferLength);
                }
                fileOutput.flush();
                fileOutput.close();

                String ext = mainActivity.getString(R.string.geopackage_extension);

                if(downloadDestinationFilePath.endsWith(ext))
                    mFiles.add(downloadDestinationFilePath.substring(downloadDestinationFilePath.lastIndexOf(File.separatorChar)+1, downloadDestinationFilePath.length()));
                else
                    mFiles = this.unzip(new File(downloadDestinationFilePath), new File(unzipDestinationFilePath));

                unzipDestinationFilePath += "/" +projectFileName;

                if(destinationFile.exists()) {
                    publishProgress("99", mainActivity.getString(R.string.copying_file));
                    Util.copyFile(downloadDestinationFilePath, unzipDestinationFilePath);
                    destinationFile.delete();
                }

                return true;

            }
            else
            {
                this.cancel(true);
                error = true;
                if(destinationFile.exists())
                    destinationFile.delete();
                return false;
            }

        }catch (IOException e) {
            e.printStackTrace();
            this.cancel(true);
            error = true;
            if(destinationFile.exists())
                destinationFile.delete();
            return false;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            this.cancel(true);
            if(destinationFile.exists())
                destinationFile.delete();
            error = true;
            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        try {
            if(android.os.Debug.isDebuggerConnected()) android.os.Debug.waitForDebugger(); // Para debugar é preciso colocar um breakpoint nessa linha

                mainActivity.getMainController().getTreeViewController().refreshTreeView();

            String projectName = mFiles.get(0);// The project is the last not_downloaded geopackage file.

            ProjectDAO projectDAO = new ProjectDAO(DatabaseFactory.getDatabase(mainActivity, ApplicationDatabase.DATABASE_NAME));

            Project project = new Project();
            project.setId(null);
            project.setName(projectName);
            project.setFilePath(unzipDestinationFilePath);
            project.setDownloaded(1);
            projectDAO.insert(project);

            mainActivity.getMainController().setCurrentProject(projectDAO.getByName(projectName));

            if(mainActivity.getProgressDialog() != null && mainActivity.getProgressDialog().isShowing()) {
                if (aBoolean) {
                    mainActivity.getProgressDialog().dismiss();
                    mainActivity.getProjectListFragment().dismiss();
                    Message.showSuccessMessage(mainActivity, R.string.success, R.string.download_success);
                } else {
                    mainActivity.getProgressDialog().dismiss();
                    Message.showErrorMessage(mainActivity, R.string.error, R.string.download_failed);
                }
            }
            else {
                Message.showErrorMessage(mainActivity, R.string.error, R.string.download_failed);
            }
        } catch (InvalidAppConfigException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        } catch (DAOException e) {
            e.printStackTrace();
            Message.showErrorMessage(mainActivity, R.string.error, e.getMessage());
        }

    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(mainActivity.getProgressDialog() != null && mainActivity.getProgressDialog().isShowing()) {
            mainActivity.getProgressDialog().setProgress(Integer.parseInt(values[0]));
            mainActivity.getProgressDialog().setMessage(values[1]);
        }
    }

    /**
     * Called when the cancel button of the ProgressDialog is touched
     * @param aBoolean
     */
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        if(destinationFile.exists()) {
            if(destinationFile.delete())
                if(mainActivity.getProgressDialog().isShowing())
                    mainActivity.getProgressDialog().dismiss();
                if(error)
                    Message.showErrorMessage(mainActivity, R.string.error, R.string.download_failed);
                else
                    Message.showSuccessMessage(mainActivity, R.string.success, R.string.download_cancelled);
        }
    }

    /**
     * Count the number of files on a zip
     * @param zipFile Zip file
     * @return The number of files on the zip archive
     */
    private long countZipFiles(File zipFile) {
        long totalFiles = 0;
        try {
            ZipInputStream zipInputStream = new ZipInputStream(new BufferedInputStream(new FileInputStream(zipFile)));
            while (zipInputStream.getNextEntry() != null)
                totalFiles++;
            zipInputStream.close();
            return totalFiles;
        }catch (FileNotFoundException e) {
            Log.e("countZipFiles", "File not found");
            e.printStackTrace();
            return totalFiles;
        } catch (IOException e) {
            Log.e("countZipFiles", "input/output error");
            e.printStackTrace();
            return totalFiles;
        }
    }

    /**
     * Unzip an archive
     * @param zipFile Zip archive
     * @param targetDirectory Directory to unzip the files
     * @throws IOException
     */
    public ArrayList<String> unzip(File zipFile, File targetDirectory) throws IOException {
        ZipInputStream zipInputStream = new ZipInputStream(
                new BufferedInputStream(new FileInputStream(zipFile)));

        ArrayList<String> files=new ArrayList<String>();

        try {
            ZipEntry zipEntry;
            int count;
            byte[] buffer = new byte[8192];
            int numFiles = 0;
            long totalFiles = countZipFiles(zipFile);

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (zipEntry.isDirectory())
                    continue;

                numFiles++;

                File file = new File(targetDirectory, zipEntry.getName());
                FileOutputStream fileOutputStream = new FileOutputStream(file);

                files.add(zipEntry.getName()+mainActivity.getString(R.string.geopackage_extension));

                long total = 0;
                long totalZipSize = zipEntry.getCompressedSize();
                while ((count = zipInputStream.read(buffer)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / totalZipSize), mainActivity.getResources().getString(R.string.decompressing) + "\n" + mainActivity.getResources().getString(R.string.file) + " " + numFiles + "/" + totalFiles);
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();
                zipFile.delete();
            }
        }catch (FileNotFoundException e) {
            Log.e("unzip", "File not found");
            e.printStackTrace();
            return null;
        } catch (IOException e) {
            Log.e("unzip", "input/output error");
            e.printStackTrace();
            return null;
        } finally {
            zipInputStream.close();
        }
        return files;
    }
}