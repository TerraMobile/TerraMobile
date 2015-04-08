package br.org.funcate.terramobile.model.task;

import android.os.AsyncTask;
import android.os.Environment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.ProtocolException;
import java.net.URL;

import br.org.funcate.terramobile.model.exception.DownloadException;
import br.org.funcate.terramobile.model.service.DownloadService;

/**
 * Created by bogo on 08/04/15.
 */
public class DownloadTask extends AsyncTask<Void, Void, Boolean> {

    private String destinationFilePath;

    private String urlToDownload;

    private DownloadException exception;

    private boolean overwrite;

    public DownloadTask(String urlToDownload, String destinationFilePath, boolean overwrite)
    {
        this.destinationFilePath=destinationFilePath;
        this.urlToDownload=urlToDownload;
        this.overwrite=overwrite;
    }

    protected Boolean doInBackground(Void... voidParam) {

        if(urlToDownload.isEmpty())
        {
            exception= new DownloadException("Missing URL to be downloaded.");
            return false;
        }

        if(destinationFilePath.isEmpty())
        {
            exception= new DownloadException("Missing destination path to download to.");
            return false;
        }

        try {

            boolean success=DownloadService.downloadFile(urlToDownload,destinationFilePath, overwrite);

            return success;

        } catch (DownloadException e) {
            exception = e;
        }

        return false;
    }

    public DownloadException getException()
    {
        return exception;
    }
}
