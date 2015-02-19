package cz.vse.model;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import cz.vse.tools.Constants;

/**
 *
 * @author Martin Kravec
 */

public class StatementBean {

    Logger log = LoggerFactory.getLogger(StatementBean.class);

    int id;
    int ttl;
    String epl;
    String name;
    String state;

    public StatementBean() {
    }

    public StatementBean(String name, String epl) {
        this(name, epl, "STARTED", Constants.HOUR);
    }

    public StatementBean(String name, String epl, String state, int ttl) {
        this.epl = epl;
        this.ttl = ttl;
        this.name = name;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEpl() {
        return epl;
    }

    public int getTtl() {
        return ttl;
    }

    public String getState() {
        return state;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEpl(String epl) {
        this.epl = epl;
    }

    public void setTtl(int ttl) {
        this.ttl = ttl;
    }

    public void setState(String state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "StatementBean: " + id + ": " + name + " - " + epl;
    }
}
