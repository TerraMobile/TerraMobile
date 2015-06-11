package br.org.funcate.terramobile.model;

/**
 * Created by Andre Carvalho on 01/06/15.
 */
public class Project {
    private Integer id;
    private String name;
    private String filePath;

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
}