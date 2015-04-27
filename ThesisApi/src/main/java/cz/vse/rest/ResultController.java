package cz.vse.rest;

import com.datastax.driver.core.ResultSet;
import com.datastax.driver.core.Row;
import com.espertech.esper.core.service.EPRuntimeImpl;
import com.espertech.esper.event.EventAdapterException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import cz.vse.dao.ResultsDAOImpl;
import cz.vse.exceptions.BadRequestException;
import cz.vse.exceptions.NotFoundException;
import cz.vse.model.StatementResultBean;
import cz.vse.startup.EsperManager;
import cz.vse.tools.Constants;
import cz.vse.tools.Helpers;
import org.json.JSONObject;
import org.json.XML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping(value = "/statements/{statement_id}/results", produces = "application/json")
public class ResultController {

    private final Logger log = LoggerFactory.getLogger(ResultController.class);
    private static ObjectMapper mapper = new ObjectMapper();

    @Autowired
    EsperManager esper;

    @Autowired
    ResultsDAOImpl dao;

    @RequestMapping(method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAll(@PathVariable("statement_id") int statementID) {
        dao.deleteAll(statementID);
    }

    @RequestMapping(value = "{uuid}", method = RequestMethod.DELETE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOne(@PathVariable("statement_id") int statement_id,
                          @PathVariable("uuid") UUID uuid) {
        dao.delete(statement_id, uuid);
    }

    @RequestMapping(value = "{uuid}/export", method = RequestMethod.GET)
    public void exportOne(@PathVariable("statement_id") int statementID,
                          @PathVariable("uuid") UUID uuid,
                          HttpServletResponse response) throws IOException {

        Row row = dao.exportOne(statementID, uuid);
        if (row == null) throw new NotFoundException();

        StatementResultBean srb = new StatementResultBean(row);

        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=\"export.json\"");

        OutputStream outputStream = response.getOutputStream();
        outputStream.write(srb.toJSON().toString().getBytes());
        outputStream.flush();
        outputStream.close();
    }

    @RequestMapping(value = "export", method = RequestMethod.GET)
    public void exportAll(
            @PathVariable("statement_id") int statementID,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        response.setHeader("Content-Disposition", "attachment; filename=\"export.json\"");

        OutputStream outputStream = response.getOutputStream();

        StatementResultBean srb;
        ResultSet results_raw = dao.exportAll(statementID);
        for (Row row : results_raw) {
            srb = new StatementResultBean(row);
            outputStream.write(srb.toJSON().toString().getBytes());
        }
        outputStream.flush();
        outputStream.close();
    }

    @RequestMapping(value = "{uuid}", method = RequestMethod.GET)
    public JsonNode getOne(@PathVariable("statement_id") int statementID,
                           @PathVariable("uuid") UUID uuid) throws IOException {

        StatementResultBean srb = dao.getOne(statementID, uuid);
        if (srb == null) throw new NotFoundException();

        ObjectNode node = mapper.createObjectNode();
        return node.set("result", srb.toJSON());
    }

    @RequestMapping(method = RequestMethod.GET)
    public ObjectNode getAll(
            @PathVariable("statement_id") int statementID,
            @RequestParam(value = "offset", defaultValue = "first") String offset,
            @RequestParam(value = "reverse", defaultValue = "false") boolean reverse,
            @RequestParam(value = "limit", defaultValue = "100") int limit) throws IOException {

        /**
         * Parse parameters and load DB data
         */
        long resultsCount = dao.getCount(statementID);

        int buffer = 2;
        if (limit > 1000) {
            limit = 1000;
        }

        if (offset == null || offset.equals("first")) {
            offset = Constants.UUID_MAX;
        } else {
            if (offset.equals("last")) {
                offset = Constants.UUID_MIN;
                reverse = true;
                int lastPageSize = (int) (resultsCount % limit);
                if (0 < lastPageSize && lastPageSize < limit) {
                    limit = lastPageSize;
                }
                buffer = 1;
            }
        }

        List<StatementResultBean> results_raw = dao.getAll(
                statementID, UUID.fromString(offset), limit + buffer, reverse);

        /**
         * Allow pagination using metadata
         */
        ObjectNode node = mapper.createObjectNode();
        ObjectNode pagination = mapper.createObjectNode();
        ArrayNode results = mapper.createArrayNode();

        if (reverse) {
            if (results_raw.size() == limit + buffer) {
                results_raw.remove(0);
                pagination.put("offset_previous", results_raw.get(0).getTimeID().toString());
            }
            if (!offset.equals(Constants.UUID_MIN)) {
                pagination.put("offset_next", results_raw.remove(limit).getTimeID().toString());
            }
        } else {
            if (results_raw.size() == limit + buffer) {
                results_raw.remove(limit + buffer - 1);
            }
            if (results_raw.size() == limit + buffer - 1) {
                pagination.put("offset_next", results_raw.remove(limit).getTimeID().toString());
            }
            if (!offset.equals(Constants.UUID_MAX)) {
                pagination.put("offset_previous", results_raw.get(0).getTimeID().toString());
            }
        }

        /**
         * Parse results to JSON Object
         */
        for (StatementResultBean result : results_raw) {
            results.add(result.toJSON());
        }
        node.set("results", results);
        node.putObject("metadata").set("pagination", pagination);

        return node;
    }

    @RequestMapping(value = "count", method = RequestMethod.GET)
    public long count(@PathVariable("statement_id") int statementID,
                      @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                      @RequestParam("stop") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date stop) {

        if (start == null || stop == null) throw new BadRequestException("Invalid date format");
        return dao.getCount(statementID, start, stop);
    }

    // TODO: async - do not wait till end
    @RequestMapping(value = "replay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replayAll(@PathVariable("statement_id") int statementID,
                       @RequestParam("as") String as,
                       @RequestParam("start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date start,
                       @RequestParam("stop") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") Date stop) throws IOException, SAXException, ParserConfigurationException {
        if (as == null || as.isEmpty()) throw new BadRequestException("You have to specify Root Element");
        Document doc;
        ResultSet results_raw = dao.exportRange(statementID, start, stop);
        for (Row row : results_raw) {
            doc = Helpers.convertJsonToDocument(as, row.getString("event"));
            esper.handle(doc);
        }
        log.info("Events " + statementID + " replayed as \"" + as + "\"");
    }

    @RequestMapping(value = "{uuid}/replay")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replayOne(@PathVariable("statement_id") int statementID,
                       @PathVariable("uuid") UUID uuid,
                       @RequestParam("as") String as) throws IOException, SAXException, ParserConfigurationException {
        if (as == null || as.isEmpty()) throw new BadRequestException("You have to specify Root Element");
        Row row = dao.exportOne(statementID, uuid);
        Document doc = Helpers.convertJsonToDocument(as, row.getString("event"));
        esper.handle(doc);

        log.info("Event " + uuid + " replayed as \"" + as + "\"");
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({SAXParseException.class, EventAdapterException.class})
    public ObjectNode conflict(Exception e) {
        log.info("Exception: " + e.getMessage());
        return mapper.createObjectNode().put("message",e.getMessage());
    }

}