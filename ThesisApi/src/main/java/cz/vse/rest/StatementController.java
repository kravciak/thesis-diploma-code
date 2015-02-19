package cz.vse.rest;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.espertech.esper.client.EPException;
import com.espertech.esper.client.EPStatement;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.JacksonXmlModule;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import cz.vse.dao.ResultsDAOImpl;
import cz.vse.dao.StatementsDaoImpl;
import cz.vse.exceptions.BadRequestException;
import cz.vse.exceptions.NotFoundException;
import cz.vse.model.StatementBean;
import cz.vse.startup.EsperManager;
import cz.vse.tools.Helpers;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * REST Web Service
 *
 * @author Martin Kravec
 */

@RestController
@RequestMapping(value = "statements", produces = "application/json")
public class StatementController {

    private final Logger log = LoggerFactory.getLogger(StatementController.class);
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    EsperManager esper;

    @Autowired
    StatementsDaoImpl dao;

    @Autowired
    ResultsDAOImpl resultsDAO;

    @RequestMapping(value = "{id}/control")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void control (@PathVariable("id") int id,
                         @RequestParam("cmd") String cmd) {
        StatementBean sb = dao.getById(id);
        switch (cmd) {
            case "start":
                esper.startStatement(sb);
                sb.setState("STARTED");
                break;
            case "stop":
                esper.stopStatement(sb);
                sb.setState("STOPPED");
                break;
            default:
                throw new NotFoundException("Requested command is not supported");
        }
        dao.update(sb);
    }

    @RequestMapping(value="{id}", method = RequestMethod.GET )
    public JsonNode get(@PathVariable("id") int id) {
        ObjectNode tmp;
        ObjectNode node = mapper.createObjectNode();

        StatementBean statement = dao.getById(id);
        tmp = mapper.valueToTree(statement);
        tmp.putObject("metadata")
                .put("result_count", resultsDAO.getCount(statement.getId()))
                .set("description", Helpers.eventTypeToJSON(
                    esper.getEPStatement(statement.getName()).getEventType()));

        return node.set("statement", tmp);
    }

    @RequestMapping(method = RequestMethod.GET)
    public ObjectNode getAll(@RequestParam("limit") int limit,
                             @RequestParam("offset") int offset,
                             @RequestParam(value = "filter", required = false) String filter) {

        ObjectNode tmp;
        ObjectNode node = mapper.createObjectNode();
        ArrayNode anode = mapper.createArrayNode();

        List<StatementBean> statements = dao.getAll(limit, offset, filter);
        for (StatementBean statement : statements) {
            tmp = mapper.valueToTree(statement);
            tmp.putObject("metadata")
                    // Not so easy in cassandra..need to create counter table
                    // TODO: Count in loop - http://stackoverflow.com/questions/2957269/counting-multiple-rows-in-mysql-in-one-query
                    .put("result_count", resultsDAO.getCount(statement.getId()));
            anode.add(tmp);
        }
        node.set("statements", anode);
        node.putObject("metadata").put("total_count", dao.getCount(filter));
        return node;
    }

    @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public StatementBean create(@RequestParam("name") String name,
                                @RequestParam("epl") String epl,
                                @RequestParam(value = "state", defaultValue = "STARTED") String state,
                                @RequestParam(value = "ttl", defaultValue = "0") int ttl) {

        if (ttl < 0) throw new BadRequestException("TTL must be positive");

        // Persist - get id
        StatementBean sb = new StatementBean(name, epl, state, ttl);
        sb.setId(dao.save(sb));

        // Change name if duplicity
        try {
            EPStatement eps = esper.addStatement(sb);
            if (!eps.getName().equals(sb.getName())) {
                sb.setName(eps.getName());
                dao.update(sb);
            }
        } catch (EPException e) {
            throw new BadRequestException(e.getMessage());
        }
        return sb;
    }

    @Transactional
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        StatementBean sb = dao.getById(id);
        esper.deleteStatement(sb);
        resultsDAO.deleteAll(id);
        dao.deleteById(id);
    }

}
