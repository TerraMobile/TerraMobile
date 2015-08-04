package br.org.funcate.terramobile.model.domain;

/**
 * Created by marcelo on 5/26/15.
 */
public class Setting {

    private Long id;

    private String key;

    private String value;

    public Setting(String key, String value)
    {
        this.key = key;
        this.value = value;
    }

    public Setting(Long id, String key, String value)
    {
        this.id = id;
        this.key = key;
        this.value = value;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
