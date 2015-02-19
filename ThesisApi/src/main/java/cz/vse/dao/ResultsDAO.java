package cz.vse.dao;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import cz.vse.model.StatementBean;
import cz.vse.model.StatementResultBean;

import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Martin Kravec on 24. 12. 2014.
 */
public interface ResultsDAO {

    Row exportOne(int statementID, UUID uuid);

    ResultSet exportAll(int statementID);

    ResultSet exportRange(int statementID, Date start, Date stop);

    StatementResultBean getOne(int statementID, UUID uuid);

    List<StatementResultBean> getAll(int statementID, UUID offset, int limit, boolean reversed);

    long getCount(int statementID);

    long getCount(int statementID, Date start, Date stop);

    void save(StatementBean sb, String json);

    void delete(int statementID, UUID uuid);

    void deleteAll(int statementID);

}
