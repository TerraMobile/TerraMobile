package br.org.funcate.terramobile.controller.activity.tasks.httpclient;

import java.io.IOException;
import java.io.OutputStream;

import br.org.funcate.terramobile.controller.activity.tasks.UploadTask;

public class OutputStreamUploadProgress extends OutputStream {

    private final OutputStream outstream;
    private volatile long bytesWritten=0;
    private FileBodyUploadProgress fileBody;


    public OutputStreamUploadProgress(OutputStream outstream, FileBodyUploadProgress fileBody) {
        this.outstream = outstream;
        this.fileBody = fileBody;
    }

    @Override
    public void write(int b) throws IOException {
        outstream.write(b);
        bytesWritten++;
        updateProgress();
    }

    @Override
    public void write(byte[] b) throws IOException {
        outstream.write(b);
        bytesWritten += b.length;
        updateProgress();
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        outstream.write(b, off, len);
        bytesWritten += len;
        updateProgress();
    }

    @Override
    public void flush() throws IOException {
        outstream.flush();
    }

    @Override
    public void close() throws IOException {
        outstream.close();
    }

    public void updateProgress() {
        if(fileBody!=null)
        {
            fileBody.updateProgress(bytesWritten);
        }
    }
}