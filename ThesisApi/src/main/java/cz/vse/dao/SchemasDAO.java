package cz.vse.dao;

import cz.vse.model.SchemaBean;

import java.util.List;

/**
 * Created by Martin Kravec on 22. 12. 2014.
 */
public interface SchemasDAO {

    int save(SchemaBean schema);

    List<SchemaBean> getAll();

    List<SchemaBean> getAll(int offset, int limit, String filter);

    int getCount(String filter);

    SchemaBean getById(int id);

    int deleteById(int id);

}
