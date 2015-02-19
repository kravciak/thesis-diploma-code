package cz.vse.model;

/**
 * Created by Martin Kravec on 23. 12. 2014.
 */
public class SchemaBean {
    int id;
    String name;
    String root;
    String xsd;

    public SchemaBean() {
    }

    public SchemaBean(String name, String root, String xsd) {
        this.name = name;
        this.root = root;
        this.xsd = xsd;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        this.root = root;
    }

    public String getXsd() {
        return xsd;
    }

    public void setXsd(String xsd) {
        this.xsd = xsd;
    }

    @Override
    public String toString() {
        return "Schema [id:" + id + ", name:" + name + "]";
    }
}
