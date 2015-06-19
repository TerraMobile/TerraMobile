package br.org.funcate.terramobile.model;

/**
 * Created by marcelo on 5/26/15.
 */
public class Settings {
    private long id;
    private String userName;
    private String password;
    private String url;
    private String currentProject;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getCurrentProject() {
        return currentProject;
    }

    public void setCurrentProject(String currentProject) {
        this.currentProject = currentProject;
    }

    public String getCurrentProjectName(){
        return currentProject.substring(0, currentProject.indexOf('.'));
    }
}
