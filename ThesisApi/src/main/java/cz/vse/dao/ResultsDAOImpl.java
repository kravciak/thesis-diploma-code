package cz.vse.dao;

import com.datastax.driver.core.*;
import com.datastax.driver.core.policies.DCAwareRoundRobinPolicy;
import com.datastax.driver.core.policies.DefaultRetryPolicy;
import com.datastax.driver.core.policies.TokenAwarePolicy;
import cz.vse.model.StatementBean;
import cz.vse.model.StatementResultBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import cz.vse.tools.Constants;

import javax.annotation.PreDestroy;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Created by Martin Kravec on 14. 12. 2014.
 */

@Repository
public class ResultsDAOImpl implements ResultsDAO {

    static Logger log = LoggerFactory.getLogger(ResultsDAOImpl.class);

    private Cluster cluster;
    private Session session;
    private static String tableName = "statement_results";

    private static final String INSERT = "Insert into " + tableName + " (time_id, statement_id, event) values (now(), ?, ?)";
    private static final String INSERT_TTL = "Insert into " + tableName + " (time_id, statement_id, event) values (now(), ?, ?) USING TTL ?";
    private static final String COUNT = "Select count(*) from " + tableName + " where statement_id = ?";
    private static final String COUNT_RANGE = "Select count(*) from " + tableName + " where statement_id = ? and time_id > maxTimeuuid(?) and time_id < minTimeuuid(?)";
    private static final String DELETE_ONE = "Delete from " + tableName + " where statement_id = ? and time_id = ?";
    private static final String DELETE_ALL = "Delete from " + tableName + " where statement_id = ?";
    private static final String SELECT_ONE = "Select * from " + tableName + " where statement_id = ? and time_id = ?";
    private static final String SELECT_ALL = "Select * from " + tableName + " where statement_id = ? and time_id <= ? limit ?";
    private static final String SELECT_ALL_REVERSED = "Select * from " + tableName + " where statement_id = ? and time_id >= ? " + " order by time_id asc limit ?";
    private static final String EXPORT_ONE = "Select * from " + tableName + " where statement_id = ? and time_id = ?";
    private static final String EXPORT_ALL = "Select * from " + tableName + " where statement_id = ?";
    private static final String EXPORT_RANGE = "Select * from " + tableName + " where statement_id = ? and time_id > maxTimeuuid(?) and time_id < minTimeuuid(?)";

    private PreparedStatement insertPS;
    private PreparedStatement insertTtlPS;
    private PreparedStatement countPS;
    private PreparedStatement countRangePS;
    private PreparedStatement deleteOnePS;
    private PreparedStatement deleteAllPS;
    private PreparedStatement selectOnePS;
    private PreparedStatement selectAllPS;
    private PreparedStatement selectAllReversedPS;
    private PreparedStatement exportOnePS;
    private PreparedStatement exportAllPS;
    private PreparedStatement exportRangePS;

    public ResultsDAOImpl() {
        this.cluster = Cluster.builder()
                .addContactPoint(Constants.CASSANDRA_HOST)
                .withRetryPolicy(DefaultRetryPolicy.INSTANCE)
                .withLoadBalancingPolicy(
                        new TokenAwarePolicy(new DCAwareRoundRobinPolicy("datacenter1")))
                .build();
        this.session = cluster.connect(Constants.CASSANDRA_KEYSPACE);

        this.insertPS = session.prepare(INSERT);
        this.insertTtlPS = session.prepare(INSERT_TTL);
        this.countPS = session.prepare(COUNT);
        this.countRangePS = session.prepare(COUNT_RANGE);
        this.deleteOnePS = session.prepare(DELETE_ONE);
        this.deleteAllPS = session.prepare(DELETE_ALL);
        this.selectOnePS = session.prepare(SELECT_ONE);
        this.selectAllPS = session.prepare(SELECT_ALL);
        this.selectAllReversedPS = session.prepare(SELECT_ALL_REVERSED);
        this.exportOnePS = session.prepare(EXPORT_ONE);
        this.exportAllPS = session.prepare(EXPORT_ALL);
        this.exportRangePS = session.prepare(EXPORT_RANGE);
    }

    @Override
    public void save(StatementBean sb, String json) {
        BoundStatement boundStatement;
        if (sb.getTtl() == 0) {
            boundStatement = new BoundStatement(insertPS)
                    .bind(sb.getId(), json);
        } else {
            boundStatement = new BoundStatement(insertTtlPS)
                    .bind(sb.getId(), json, sb.getTtl());
        }
        session.execute(boundStatement);
    }

    @Override
    public void delete(int statementID, UUID uuid) {
        BoundStatement boundStatement = new BoundStatement(deleteOnePS)
                .bind(statementID, uuid);
        session.execute(boundStatement);
    }

    @Override
    public void deleteAll(int statementID) {
        BoundStatement boundStatement = new BoundStatement(deleteAllPS)
                .bind(statementID);
        session.execute(boundStatement);
    }

    @Override
    public Row exportOne(int statementID, UUID uuid) {
        BoundStatement boundStatement = new BoundStatement(exportOnePS)
                .bind(statementID, uuid);
        return session.execute(boundStatement).one();
    }

    @Override
    public ResultSet exportAll(int statementID) {
        BoundStatement boundStatement = new BoundStatement(exportAllPS)
                .bind(statementID);
        return session.execute(boundStatement);
    }

    @Override
    public ResultSet exportRange(int statementID, Date start, Date stop) {
        fixStopDate(stop);
        BoundStatement boundStatement = new BoundStatement(exportRangePS)
                .bind(statementID, start, stop);
        return session.execute(boundStatement);
    }

    @Override
    public StatementResultBean getOne(int statementID, UUID uuid) {
        BoundStatement boundStatement = new BoundStatement(selectOnePS)
                .bind(statementID, uuid);
        Row row = session.execute(boundStatement).one();

        StatementResultBean srb = null;
        if (row != null) {
            srb = new StatementResultBean(row);
        }
        return srb;
    }

    @Override
    public List<StatementResultBean> getAll(int statementID, UUID offset, int limit, boolean reversed) {
        PreparedStatement query = reversed ? selectAllReversedPS : selectAllPS;
        BoundStatement boundStatement = new BoundStatement(query)
                .bind(statementID, offset, limit);
        ResultSet results_raw = session.execute(boundStatement);

        StatementResultBean srb;
        List<StatementResultBean> results = new ArrayList<>();
        for (Row row : results_raw) {
            srb = new StatementResultBean(row);
            if (reversed) {
                results.add(0, srb);
            } else {
                results.add(srb);
            }
        }
        return results;
    }

    // TODO: move into separate table for performance
    @Override
    public long getCount(int statementID) {
        BoundStatement boundStatement = new BoundStatement(countPS)
                .bind(statementID);
        ResultSet results_raw = session.execute(boundStatement);
        return results_raw.one().getLong("count");
    }

    @Override
    public long getCount(int statementID, Date start, Date stop) {
        fixStopDate(stop);
        BoundStatement boundStatement = new BoundStatement(countRangePS)
                .bind(statementID, start, stop);
        ResultSet results_raw = session.execute(boundStatement);
        return results_raw.one().getLong("count");
    }

    @PreDestroy
    private void terminate() {
        this.session.close();
        this.cluster.close();
    }

    private void fixStopDate(Date date) {
        date.setTime(date.getTime() + 999);
    }

}
