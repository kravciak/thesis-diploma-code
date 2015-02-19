package cz.vse.model;

import com.datastax.driver.core.Row;
import com.datastax.driver.core.utils.UUIDs;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import javax.swing.plaf.nimbus.State;
import java.io.IOException;
import java.util.UUID;

/**
 * Created by Martin Kravec on 19. 12. 2014.
 */
public class StatementResultBean {
    UUID timeID;
    int statementID;
    String event;

    private static ObjectMapper mapper = new ObjectMapper();

    public StatementResultBean(UUID timeID, int statementID, String event) {
        this.timeID = timeID;
        this.event = event;
        this.statementID = statementID;
    }

    public StatementResultBean(Row row) {
        this(row.getUUID("time_id"), row.getInt("statement_id"), row.getString("event"));
    }

    public UUID getTimeID() {
        return timeID;
    }

    public int getStatementID() {
        return statementID;
    }

    public String getEvent() {
        return event;
    }

    public long getUnixTimestamp() {
        return UUIDs.unixTimestamp(timeID) / 1000;
    }

    public ObjectNode toJSON() throws IOException {
        ObjectNode node = mapper.createObjectNode();
        node.put("time_id", timeID.toString());
        node.put("statement_id", statementID);
        node.set("event", mapper.readTree(event));
        return node;
    }
}
