package br.org.funcate.terramobile.model;

/**
 * Created by Andre Carvalho on 01/06/15.
 */
public class Project {

    private String current;
    private String filePath;

    public Project() {
        this.current="";
        this.filePath="";
    }

    public Project(String name,String path) {
        this.current=name;
        this.filePath=path;
    }

    public String getCurrent() {
        return this.current;
    }

    public void setCurrent(String name) {
        this.current=name;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }


}
