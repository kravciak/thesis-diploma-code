package cz.vse.rest;

import com.espertech.esper.client.ConfigurationException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.vse.dao.SchemasDAO;
import cz.vse.exceptions.BadRequestException;
import cz.vse.exceptions.ConflictException;
import cz.vse.model.SchemaBean;
import cz.vse.model.StatementBean;
import cz.vse.startup.EsperManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.List;
import java.util.Set;

/**
 * Created by Martin Kravec on 27. 12. 2014.
 */

@RestController
@RequestMapping(value = "schemas", produces = "application/json")
public class SchemaController {
    private final Logger log = LoggerFactory.getLogger(SchemaController.class);
    ObjectMapper mapper = new ObjectMapper();

    @Autowired
    SchemasDAO dao;

    @Autowired
    EsperManager esper;

    @RequestMapping(value="{id}", method = RequestMethod.GET )
    public JsonNode get(@PathVariable("id") int id) {
        SchemaBean schema = dao.getById(id);
        Set<StatementBean> statements = esper.getSchemaStatementBeans(schema);

        ObjectNode node = mapper.createObjectNode();
        ObjectNode onode = mapper.valueToTree(schema);

        ObjectNode metadata;
        metadata = onode.putObject("metadata");
        metadata.set("statements", mapper.valueToTree(statements));

        return node.set("schema", onode);
    }

    @RequestMapping(method = RequestMethod.GET)
    public JsonNode getAll(@RequestParam("limit") int limit,
                           @RequestParam("offset") int offset,
                           @RequestParam(value = "filter", required = false) String filter) {
        ObjectNode tmp;
        ObjectNode node = mapper.createObjectNode();
        ArrayNode anode = mapper.createArrayNode();

        List<SchemaBean> schemas = dao.getAll(offset, limit, filter);
        for (SchemaBean schema : schemas) {
            tmp = mapper.valueToTree(schema);
            tmp.putObject("metadata")
                    .put("usage_count", esper.getSchemaStatements(schema).size());
            anode.add(tmp);
        }
        node.putObject("metadata").put("total_count", dao.getCount(filter));
        return node.set("schemas", anode);
    }

    @Transactional
    @RequestMapping(method = RequestMethod.POST)
    public SchemaBean create(@RequestParam("name") String name,
                             @RequestParam("root") String root,
                             @RequestParam("xsd") String xsd) {
        SchemaBean sb = new SchemaBean(name, root, xsd);
        if (esper.isSchema(sb.getName())) {
            throw new BadRequestException("Event type named '"+ sb.getName() + "' has already been declared.");
        }

        try {
            esper.addSchema(sb);
            sb.setId(dao.save(sb));
        } catch (ConfigurationException e) {
            throw new BadRequestException(e.getMessage());
        }
        return sb;
    }

    @Transactional
    @RequestMapping(value = "{id}", method = RequestMethod.DELETE)
    public void delete(@PathVariable("id") int id) {
        SchemaBean sb = dao.getById(id);
        try {
            esper.deleteSchema(sb);
            dao.deleteById(sb.getId());
        } catch (ConfigurationException e) {
            throw new ConflictException(e.getMessage());
        }
    }

}
