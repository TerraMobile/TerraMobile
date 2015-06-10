package br.org.funcate.terramobile.controller.activity.tasks;

import android.os.AsyncTask;
import android.util.Log;

import org.opengis.context.Resource;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.controller.activity.MainActivity;
import br.org.funcate.terramobile.model.Project;
import br.org.funcate.terramobile.model.db.dao.ProjectDAO;
import br.org.funcate.terramobile.model.exception.DownloadException;
import br.org.funcate.terramobile.util.Message;

/**
 * This AsyncTask downloads a geopackage from the server
 */
public class DownloadTask extends AsyncTask<String, String, Boolean> {
    private String unzipDestinationFilePath;
    private String downloadDestinationFilePath;
    private ArrayList<String> mFiles;

    private boolean overwrite;

    private MainActivity mainActivity;

    private File destinationFile;

    public DownloadTask(String downloadDestinationFilePath, String unzipDestinationFilePath, boolean overwrite, MainActivity mainActivity) {
        this.mainActivity = mainActivity;
        this.unzipDestinationFilePath = unzipDestinationFilePath;
        this.downloadDestinationFilePath = downloadDestinationFilePath;
        this.overwrite = overwrite;
        this.mFiles = new ArrayList<String>();
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
            if (!destinationFile.exists())
                destinationFile.createNewFile();
            else {
                if (overwrite)
                    destinationFile.delete();
                else
                    return true;
            }
            URL url = new URL(urlToDownload[0]);
            URLConnection urlConnection = url.openConnection();
            urlConnection.connect();

            int totalSize = urlConnection.getContentLength();

            InputStream inputStream = new BufferedInputStream(url.openStream());
            OutputStream fileOutput = new FileOutputStream(destinationFile);

            byte buffer[] = new byte[1024];

            int bufferLength;
            long total = 0;
            while ((bufferLength = inputStream.read(buffer)) != -1) {
                if (isCancelled()) {
                    fileOutput.flush();
                    fileOutput.close();
                    inputStream.close();
                    return false;
                }
                fileOutput.flush();
                fileOutput.close();

                String ext = mainActivity.getString(R.string.geopackage_extension);

                if (downloadDestinationFilePath.endsWith(ext))
                    mFiles.add(downloadDestinationFilePath.substring(downloadDestinationFilePath.lastIndexOf(File.separatorChar) + 1, downloadDestinationFilePath.lastIndexOf(ext)));
                else
                    mFiles = this.unzip(new File(downloadDestinationFilePath), new File(unzipDestinationFilePath));

                return true;
            }
            fileOutput.flush();
            fileOutput.close();
            inputStream.close();

            mFiles = this.unzip(new File(downloadDestinationFilePath), new File(unzipDestinationFilePath));
            return true;
        }catch (IOException e) {
            e.printStackTrace();
            if(destinationFile.exists())
                destinationFile.delete();
            return false;
        }catch (IllegalArgumentException e){
            e.printStackTrace();
            if(destinationFile.exists())
                destinationFile.delete();
            return false;
        }
    }

    /**
     * Called when the cancel button of the ProgressDialog is touched
     * @param aBoolean
     */
    protected void onCancelled(Boolean aBoolean) {
        super.onCancelled(aBoolean);
        if(destinationFile.exists())
            destinationFile.delete();
        Message.showSuccessMessage(mainActivity, R.string.success, R.string.download_cancelled);
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
            if (zipInputStream != null) {
                while (zipInputStream.getNextEntry() != null)
                    totalFiles++;
                zipInputStream.close();
            }
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

                files.add(zipEntry.getName());

                long total = 0;
                long totalZipSize = zipEntry.getCompressedSize();
                while ((count = zipInputStream.read(buffer)) != -1) {
                    total += count;
                    publishProgress("" + (int) ((total * 100) / totalZipSize), mainActivity.getResources().getString(R.string.decompressing) + "\n" + mainActivity.getResources().getString(R.string.file) + " " + numFiles + "/" + totalFiles);
                    fileOutputStream.write(buffer, 0, count);
                }
                fileOutputStream.close();
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

    @Override
    protected void onPostExecute(Boolean aBoolean) {
        mainActivity.getTreeView().refreshTreeView();
        // The project is the last downloaded geopackage file.
        try {
            mainActivity.getProject().setCurrent(mFiles.get(0));
        }catch (Exception e) {
            Message.showErrorMessage(mainActivity, R.string.error, R.string.download_failed);
        }

        String fileName = mFiles.get(0);// The project is the last downloaded geopackage file.

        ProjectDAO projectDAO = new ProjectDAO(mainActivity);

        Project project = new Project();
        project.setId(null);
        project.setCurrent(fileName);
        project.setFilePath(downloadDestinationFilePath);
        projectDAO.insert(project);

        mainActivity.setProject(project);

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
        else
            Message.showErrorMessage(mainActivity, R.string.error, R.string.download_failed);
    }

    @Override
    protected void onProgressUpdate(String... values) {
        if(mainActivity.getProgressDialog() != null && mainActivity.getProgressDialog().isShowing()) {
            mainActivity.getProgressDialog().setProgress(Integer.parseInt(values[0]));
            mainActivity.getProgressDialog().setMessage(values[1]);
        }
    }
}