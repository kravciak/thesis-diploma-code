package cz.vse.dao;

import cz.vse.model.SchemaBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.util.List;

@Repository
public class SchemasDAOImpl implements SchemasDAO {

    static final Logger log = LoggerFactory.getLogger(SchemasDAOImpl.class);
    static final String tableName = "schemas";

    NamedParameterJdbcTemplate npJdbcTemplate;

    @Autowired
    public SchemasDAOImpl(DataSource dataSource) {
        this.npJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public int save(SchemaBean schema) {
        log.info("Saving: " + schema);
        String query = "insert into " + tableName + " (name, root, xsd) values (:name,:root,:xsd)";

        SqlParameterSource parameters = new BeanPropertySqlParameterSource(schema);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        npJdbcTemplate.update(query, parameters, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public List<SchemaBean> getAll() {
        String query = "select * from " + tableName;
        List schemas = npJdbcTemplate.query(query,
                new BeanPropertyRowMapper(SchemaBean.class));
        return schemas;
    }

    @Override
    public List<SchemaBean> getAll(int offset, int limit, String filter) {
        String query;
        Object[] params;
        if (filter == null || filter.isEmpty()) {
            query = "select * from " + tableName + " limit ? offset ?";
            params = new Object[]{limit, offset};
        } else {
            query = "select * from " + tableName + " where lower(name) like ? limit ? offset ?";
            params = new Object[]{'%'+ filter.toLowerCase() +'%', limit, offset};
        }

        return npJdbcTemplate.getJdbcOperations().query(query, params,
                new BeanPropertyRowMapper(SchemaBean.class));
    }

    @Override
    public int getCount(String filter) {
        String query;
        Object[] params = null;
        if (filter == null || filter.isEmpty()) {
            query = "select count(*) from " + tableName;
        } else {
            query = "select count(*) from " + tableName + " where lower(name) like ?";
            params = new Object[]{'%'+ filter.toLowerCase() +'%'};
        }
        return npJdbcTemplate.getJdbcOperations().queryForObject(query, params, Integer.class);
    }

    @Override
    public SchemaBean getById(int id) {
        String query = "select * from " + tableName + " where id = ?";
        SchemaBean schema = (SchemaBean) npJdbcTemplate.getJdbcOperations().queryForObject(
                query, new Object[]{id},
                new BeanPropertyRowMapper(SchemaBean.class));
        return schema;
    }

    @Override
    public int deleteById(int id) {
        String query = "delete from " + tableName + " where id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource("id", Integer.valueOf(id));
        return npJdbcTemplate.update(query, parameters);
    }

    // TODO: Create tables if not they does not exist
//    public void initialize() {
//        jdbcTemplate.execute(script);
//    }

}
