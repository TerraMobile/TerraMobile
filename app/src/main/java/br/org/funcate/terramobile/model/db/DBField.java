package br.org.funcate.terramobile.model.db;

/**
 * Created by Andre Carvalho on 20/11/15.
 */
public class DBField {

    /** The internal field name */
    protected String fieldName = "";
    /** The SQLite data type */
    protected String fieldType = "";
    /** A value associated with this field */
    protected Object value = null;
    /** Is this field the table primary key? */
    protected boolean primaryKey = false;

    public DBField() {
        this.fieldName="";
        this.fieldType="";
        this.value=null;
        this.primaryKey=false;
    }

    public DBField(String fieldName, String fieldType) {
        this.fieldName=fieldName;
        this.fieldType=fieldType;
        this.value=null;
        this.primaryKey=false;
    }

    public DBField(String fieldName, String fieldType, boolean primaryKey) {
        this.fieldName=fieldName;
        this.fieldType=fieldType;
        this.value=null;
        this.primaryKey=primaryKey;
    }
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
    }

    public String getFieldType() {
        return fieldType;
    }

    public void setFieldType(String fieldType) {
        this.fieldType = fieldType;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public boolean isPrimaryKey() {
        return primaryKey;
    }

    public void setPrimaryKey(boolean primaryKey) {
        this.primaryKey = primaryKey;
    }
}
