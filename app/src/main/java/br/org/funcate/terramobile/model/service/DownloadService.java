package br.org.funcate.terramobile.model.service;

import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutionException;

import br.org.funcate.terramobile.model.exception.DownloadException;
import br.org.funcate.terramobile.model.task.DownloadTask;

/**
 * Created by bogo on 08/04/15.
 */
public class DownloadService {

    public static boolean downloadFile(String urlStr, String destinationFilePath, boolean overwrite) throws DownloadException {


        try {

            URL url = new URL(urlStr);

            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

            urlConnection.setRequestMethod("GET");

            urlConnection.setDoOutput(true);

            urlConnection.connect();

            File SDCardRoot = Environment.getExternalStorageDirectory();

            File file = new File(destinationFilePath);

            if(!file.exists())
            {
                file.createNewFile();
            }
            else {
                if (overwrite) {
                    file.delete();
                } else {
                    return true;
                }
            }

            FileOutputStream fileOutput = new FileOutputStream(file);

            InputStream inputStream = urlConnection.getInputStream();

            int totalSize = urlConnection.getContentLength();

            int downloadedSize = 0;

            byte[] buffer = new byte[1024];

            int bufferLength = 0;

            while ( (bufferLength = inputStream.read(buffer)) > 0 ) {

                fileOutput.write(buffer, 0, bufferLength);

                downloadedSize += bufferLength;
                /*    updateProgress(downloadedSize, totalSize);*/
                System.out.println(downloadedSize + "/"  + totalSize);

            }
            fileOutput.close();

            return true;

        } catch (IOException e) {
            throw new DownloadException("Error downloading file: " + urlStr,e);
        }



    }
}
