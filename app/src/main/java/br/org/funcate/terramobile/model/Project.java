package br.org.funcate.terramobile.model;

/**
 * Created by Andre Carvalho on 01/06/15.
 */
public class Project {
    private Integer id;
    private String current;
    private String filePath;

    public Project() {
        this.current="";
        this.filePath="";
    }

    public Project(Integer id, String name,String path) {
        this.id = id;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }
}