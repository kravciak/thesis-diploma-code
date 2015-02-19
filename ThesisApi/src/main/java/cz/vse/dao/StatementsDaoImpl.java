package cz.vse.dao;

import cz.vse.model.StatementBean;
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
import java.sql.SQLException;
import java.util.List;

/**
 * Created by Martin Kravec on 24. 12. 2014.
 */

@Repository
public class StatementsDaoImpl implements StatementsDAO {

    private Logger log = LoggerFactory.getLogger(StatementsDaoImpl.class);
    NamedParameterJdbcTemplate npJdbcTemplate;
    private static final String tableName = "statements";

    @Autowired
    public StatementsDaoImpl(DataSource dataSource) throws SQLException {
        this.npJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
    }

    @Override
    public int save(StatementBean sb) {
        log.info("Saving: " + sb);
        String query = "INSERT INTO " + tableName + " (name, epl, state, ttl) VALUES (:name, :epl, :state, :ttl)";

        SqlParameterSource parameters = new BeanPropertySqlParameterSource(sb);
        KeyHolder keyHolder = new GeneratedKeyHolder();

        npJdbcTemplate.update(query, parameters, keyHolder);
        return keyHolder.getKey().intValue();
    }

    @Override
    public int update(StatementBean sb) {
        String query = "update " + tableName + " set name=?, epl=?, state=?, ttl=? where id=?";
        Object[] params = new Object[] { sb.getName(), sb.getEpl(), sb.getState(), sb.getTtl(), sb.getId() };
        return npJdbcTemplate.getJdbcOperations().update(query, params);
    }

    @Override
    public List<StatementBean> getAll() {
        String query = "select * from " + tableName;
        List statement = npJdbcTemplate.query(query,
                new BeanPropertyRowMapper(StatementBean.class));
        return statement;
    }

    @Override
    public List<StatementBean> getAll(int limit, int offset, String filter) {
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
                new BeanPropertyRowMapper(StatementBean.class));
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
    public StatementBean getById(int id) {
        String query = "select * from " + tableName + " where id = ?";
        StatementBean statement = (StatementBean) npJdbcTemplate.getJdbcOperations().queryForObject(
                query, new Object[]{id},
                new BeanPropertyRowMapper(StatementBean.class));
        return statement;
    }

    @Override
    public int deleteById(int id) {
        String query = "delete from " + tableName + " where id = :id";
        SqlParameterSource parameters = new MapSqlParameterSource("id", Integer.valueOf(id));
        return npJdbcTemplate.update(query, parameters);
    }

}
