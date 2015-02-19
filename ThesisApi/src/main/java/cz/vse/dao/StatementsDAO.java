package cz.vse.dao;

import cz.vse.model.StatementBean;

import java.util.List;

/**
 * Created by Martin Kravec on 24. 12. 2014.
 */
public interface StatementsDAO {

    int save(StatementBean sb);

    int update(StatementBean sb);

    List<StatementBean> getAll();

    List<StatementBean> getAll(int offset, int limit, String filter);

    int getCount(String filter);

    StatementBean getById(int rid);

    int deleteById(int rid);

}
