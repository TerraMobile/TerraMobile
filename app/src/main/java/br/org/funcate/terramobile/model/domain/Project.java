package br.org.funcate.terramobile.model.domain;

/**
 * Created by Andre Carvalho on 01/06/15.
 */
public class Project {
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
        return name.substring(0, name.indexOf('.'));
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