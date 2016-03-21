package br.org.funcate.terramobile.controller.activity.tasks.httpclient;

import org.apache.http.entity.mime.content.FileBody;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import br.org.funcate.terramobile.controller.activity.tasks.UploadTask;

public class FileBodyUploadProgress extends FileBody {

    private OutputStreamUploadProgress outstream;
    private UploadTask uploadTask;

    public FileBodyUploadProgress(File file, UploadTask uploadTask) {
        super(file);
        this.uploadTask=uploadTask;
    }

    @Override
    public void writeTo(OutputStream out) throws IOException {
        this.outstream = new OutputStreamUploadProgress(out, this);
        super.writeTo(this.outstream);
    }

    public void updateProgress(long writtenLength) {
        if (outstream == null) {
            uploadTask.updateProgress(0);
            return;
        }
        long contentLength = getContentLength();
        if (contentLength <= 0) { // Prevent division by zero and negative values
            uploadTask.updateProgress(0);
            return;
        }
        int percent = new Long(new Long(100)*writtenLength/new Long(contentLength)).intValue();
        uploadTask.updateProgress(percent);
    }
}