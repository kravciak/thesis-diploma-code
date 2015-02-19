package cz.vse.startup;

import com.espertech.esper.client.*;
import cz.vse.dao.SchemasDAOImpl;
import cz.vse.dao.StatementsDaoImpl;
import cz.vse.esper.StatementUpdateListener;
import cz.vse.model.SchemaBean;
import cz.vse.model.StatementBean;
import cz.vse.tools.Constants;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;

/**
 * Created by Martin Kravec on 28. 12. 2014.
 */

@Component
public class EsperManager {

    private final Logger log = LoggerFactory.getLogger(EsperManager.class);
    private EPServiceProvider engine;

    @Autowired
    SchemasDAOImpl schemasDao;

    @Autowired
    StatementsDaoImpl statementsDao;

    @Autowired
    private StatementUpdateListener listener;

    @PostConstruct
    public void initialize() {
        Configuration config = new Configuration();
        config.configure(Constants.CONFIG_FILE_ESPER);
        engine = EPServiceProviderManager.getDefaultProvider(config);

        loadSchemas();
        loadStatementBeans();
    }

    @PreDestroy
    public void destroy() {
        engine.destroy();
    }

    //================================================================================
    //Schema Management
    //================================================================================
    public void addSchema(SchemaBean sb) throws ConfigurationException {
        log.info("Adding schema: " + sb.getName());

        ConfigurationEventTypeXMLDOM schema = new ConfigurationEventTypeXMLDOM();
        schema.setRootElementName(sb.getRoot());
        schema.setSchemaText(sb.getXsd());

        engine.getEPAdministrator().getConfiguration().addEventType(sb.getName(), schema);
    }

    public EventType getSchema(String name) {
        return engine.getEPAdministrator().getConfiguration().getEventType(name);
    }

    public boolean isSchema(String name) {
        EventType event = engine.getEPAdministrator().getConfiguration().getEventType(name);
        return event != null;
    }

    public void deleteSchema(SchemaBean sb) throws ConfigurationException {
        engine.getEPAdministrator().getConfiguration().removeEventType(sb.getName(), false);
    }

    public Set<String> getSchemaStatements(SchemaBean sb) {
        return engine.getEPAdministrator().getConfiguration().getEventTypeNameUsedBy(sb.getName());
    }

    public Set<StatementBean> getSchemaStatementBeans(SchemaBean sb) {
        Set<String> statement_names = engine.getEPAdministrator().getConfiguration().getEventTypeNameUsedBy(sb.getName());
        Set<StatementBean> statements = new HashSet<>();
        for (String name : statement_names) {
            statements.add(getStatementBean(name));
        }
        return statements;
    }

    //================================================================================
    // Statement Management
    //================================================================================
    public EPStatement addStatement(StatementBean sb) {
        log.info("Adding statement: " + sb);
        EPStatement eps = engine.getEPAdministrator().createEPL(sb.getEpl(), sb.getName(), sb);

        if (sb.getState().equals("STOPPED")) {
            eps.stop();
        }
        eps.addListener(listener);
        return eps;
    }

    public void deleteStatement(StatementBean sb) {
        log.info("Removing statement: " + sb);
        EPStatement eps = getEPStatement(sb.getName());
        eps.destroy();
    }

    public void startStatement(StatementBean sb) {
        EPStatement eps = getEPStatement(sb.getName());
        if (!eps.isStarted()) {
            eps.start();
        }
    }

    public void stopStatement(StatementBean sb) {
        EPStatement eps = getEPStatement(sb.getName());
        if (eps.isStarted()) {
            eps.stop();
        }
    }

    // TODO: unused
    public List<EPStatement> getAllStatements() {
        String[] names = engine.getEPAdministrator().getStatementNames();
        List <EPStatement> list = new ArrayList<>();
        for (String name:names) {
            list.add(engine.getEPAdministrator().getStatement(name));
        }
        return list;
    }

    //================================================================================
    // Helpers
    //================================================================================

    public StatementBean getStatementBean(String name) {
        return (StatementBean) engine.getEPAdministrator().getStatement(name).getUserObject();
    }

    // TODO: not effective
    public EPStatement getStatement(int id) {
        StatementBean sb = statementsDao.getById(id);
        return getEPStatement(sb.getName());
    }

    public EPStatement getEPStatement(String name) {
        return engine.getEPAdministrator().getStatement(name);
    }

    public EventType[] getEventTypes() {
        return engine.getEPAdministrator().getConfiguration().getEventTypes();
    }

    public void handle(Document doc) {
        engine.getEPRuntime().sendEvent(doc);
    }

    private void loadSchemas() {
        log.info("Loading Schemas...");
        List<SchemaBean> list = schemasDao.getAll();
        for (SchemaBean sb : list) {
            addSchema(sb);
        }
    }

    private void loadStatementBeans() {
        log.info("Loading Statements...");
        List<StatementBean> list = statementsDao.getAll();
        for (StatementBean sb : list) {
            addStatement(sb);
        }
    }

}