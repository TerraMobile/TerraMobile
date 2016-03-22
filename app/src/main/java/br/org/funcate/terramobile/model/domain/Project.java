package br.org.funcate.terramobile.model.domain;

import java.io.File;

import br.org.funcate.terramobile.R;
import br.org.funcate.terramobile.model.exception.InvalidAppConfigException;
import br.org.funcate.terramobile.util.ResourceHelper;
import br.org.funcate.terramobile.util.Util;

/**
 * Created by Andre Carvalho on 01/06/15.
 */
public class Project {

    /**
     * These attributes refers to states of the GeoPackage.
     */
    public static final int NEW=0;// New packages downloaded from server.
    public static final int UPLOAD=1;// Packages gathering data to sending to server.
    //public static final int SYNC=2;// Packages whose data was synchronized with the server (never exist in app).

    private Integer id;
    private String UUID;
    private String name;
    private String filePath;
    private int downloaded;
    private int updated;
    private String description;
    private int status;
    private boolean modified;
    private boolean onTheAppOnly;

    public Project()
    {

    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name=name;
    }

    public String getFilePath() {
        return filePath;
    }

    public String getUploadTempDir() throws InvalidAppConfigException {

        File tempPath = Util.getDirectory(ResourceHelper.getStringResource(R.string.app_workspace_temp_dir));
        String applicationTempDir = tempPath.getAbsolutePath();

        return applicationTempDir;
    }

    public String nextUploadFileName(String fileUploadSuffix) throws InvalidAppConfigException {

        String fileExtension = ResourceHelper.getStringResource(R.string.geopackage_extension);

        return this.getName().replace(fileExtension, fileUploadSuffix + fileExtension);
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int isUpdated() {
        return updated;
    }

    public void setUpdated(int updated) {
        this.updated = updated;
    }

    @Override
    public String toString() {
        if(name.indexOf('.')<0) return name;
        else return name.substring(0, name.indexOf('.'));
    }

    public int isDownloaded() {
        return downloaded;
    }

    public void setDownloaded(int downloaded) {
        this.downloaded = downloaded;
    }


    public String getUUID() {
        return UUID;
    }

    public void setUUID(String UUID) {
        this.UUID = UUID;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public boolean isModified() {
        return modified;
    }

    public void setModified(boolean modified) {
        this.modified = modified;
    }

    public boolean isOnTheAppOnly() {
        return onTheAppOnly;
    }

    public void setOnTheAppOnly(boolean onTheAppOnly) {
        this.onTheAppOnly = onTheAppOnly;
    }

}