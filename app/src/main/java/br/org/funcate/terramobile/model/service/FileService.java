package br.org.funcate.terramobile.model.service;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import br.org.funcate.terramobile.model.exception.DownloadException;
import br.org.funcate.terramobile.model.exception.FileException;

/**
 * Created by bogo on 08/04/15.
 */
public class FileService {

    public static boolean unzip(String file, String destinationPath) throws FileException {
        InputStream is;
        ZipInputStream zis;
        try
        {
            String filename;
            is = new FileInputStream(file);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null)
            {
                filename = ze.getName();

                if (ze.isDirectory()) {
                    File fmd = new File(destinationPath + filename);
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(destinationPath + filename);

                while ((count = zis.read(buffer)) != -1)
                {
                    fout.write(buffer, 0, count);
                }

                fout.close();
                zis.closeEntry();
            }

            zis.close();
            return true;
        }
        catch(IOException e)
        {
           throw new FileException("Error while unzipping file: " + file,e);
        }
    }
}
