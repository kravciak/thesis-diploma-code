package cz.vse.sample;

import com.espertech.esper.client.*;
import com.espertech.esper.client.soda.EPStatementObjectModel;
import com.espertech.esper.client.soda.View;
import com.espertech.esper.client.util.XMLRenderingOptions;
import com.fasterxml.jackson.databind.ObjectMapper;
import cz.vse.esper.StatementUpdateListener;
import cz.vse.esper.XMLRenderer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import cz.vse.tools.Helpers;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Iterator;
import java.util.List;

public class RunMe {

    private StatementUpdateListener listener;
    private final static String ENGINE_URI = "CEP-SERVICE-0000";
    private static Logger log = LoggerFactory.getLogger(RunMe.class);
    public ObjectMapper mapper = new ObjectMapper();
    public static EPServiceProvider engine;

    public RunMe() throws Exception {
        Configuration config = new Configuration();
        config.configure("esper.cfg.xml");
        engine = EPServiceProviderManager.getDefaultProvider(config);

        addEventTypes();
    }

    public static void main(String[] args) throws Exception {
        RunMe instance = new RunMe();
        instance.sendXmlEvent();
//        engine.getEPAdministrator().createEPL("select * from TweetEvent where message like '%happy%'", "name");
//        EPStatementObjectModel cepl =  engine.getEPAdministrator().compileEPL("select * from TweetEvessnt where message like '%happy%'");

//        instance.schema();
//        instance.cassandra();
//        instance.waitForExit();
//        instance.sendXmlEvent();
    }

    private String printDesc(EventPropertyDescriptor desc, int level) {
        String prepend = "";
        for (int i = 0; i < level; i++) {
            prepend = prepend + "  ";
        }
        return prepend + desc.getPropertyName() + ":" + desc.getPropertyType().toString();
    }


    private void addEventTypes() {
        ConfigurationEventTypeXMLDOM eventcfg;

        eventcfg = new ConfigurationEventTypeXMLDOM();
        eventcfg.setRootElementName("TweetEvent");
        eventcfg.setSchemaResource("TweetEvent.xsd");
        engine.getEPAdministrator().getConfiguration().addEventType("TweetEvent", eventcfg);

        eventcfg = new ConfigurationEventTypeXMLDOM();
        eventcfg.setRootElementName("StockEvent");
        eventcfg.setSchemaResource("StockEvent.xsd");
        engine.getEPAdministrator().getConfiguration().addEventType("StockEvent", eventcfg);

        eventcfg = new ConfigurationEventTypeXMLDOM();
        eventcfg.setRootElementName("Sensor");
        eventcfg.setSchemaResource("EsperEvent.xsd");
        engine.getEPAdministrator().getConfiguration().addEventType("EsperEvent", eventcfg);

//        Map<String, Object> def = new HashMap();
//        def.put("username", String.class);
//        def.put("message", String.class);
//        engine.getEPAdministrator().getConfiguration().addEventType("TweetEvent", def);

//        String[] propertyNames = {"username", "message"};   // order is important
//        Object[] propertyTypes = {String.class, String.class};  // type order matches name order
//        engine.getEPAdministrator().getConfiguration().addEventType("TweetEvent", propertyNames, propertyTypes);

//        SupportHTTPClient client = new SupportHTTPClient();
//        client.request(8083, "sendevent", "stream", "AccessLogEvent", "date", "mydate");
    }

    private void sendXmlEvent() throws ParserConfigurationException, SAXException, IOException {
        engine.getEPAdministrator().createEPL("select * from TweetEvent where message like '%happy%'").addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                EventBean event = newEvents[0];
                String xml = new XMLRenderer(event.getEventType(), new XMLRenderingOptions()).render("event", event);
                log.info(xml);
            }
        });

        String xmlstr = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                "<TweetEvent>" +
                "<username>martin</username>" +
                "<message>ping-happy</message>" +
                "</TweetEvent>";
        Document doc = Helpers.convertXmlToDocument(xmlstr);
        engine.getEPRuntime().sendEvent(doc);
    }

    private void waitForExit() throws IOException {
        BufferedReader stdin = new BufferedReader (new InputStreamReader(System.in));
        boolean isShutdown = false;
        do
        {
            if(stdin.ready());
            {
                String line  = stdin.readLine();
                isShutdown = line.trim().equalsIgnoreCase("exit");
            }
        }
        while(!isShutdown);
        engine.destroy();
    }

    public static void mainEplError(String[] args) {
        String epl;

        epl = "create window win22 as select * from TweetEvent";
        EPStatementObjectModel cepl;
        try {
            cepl = engine.getEPAdministrator().compileEPL(epl);
            engine.getEPAdministrator().create(cepl);

            System.out.println("OK");
        } catch (EPException e) {
            System.out.println("ERROR1: " + e.getMessage());
        } catch (Exception e) {
            System.out.println("ERROR2: " + e.getMessage());
        }
    }

    private void listWindows() {
        log.info(":::WINDOWS:::");
        EventType[] ets = engine.getEPAdministrator().getConfiguration().getEventTypes();
        for (EventType et : ets) {
            log.info("> Event: " + et.getName());
            String[] ns = et.getPropertyNames();
            for (String n : ns) {
                log.info(n + ":" + et.getPropertyType(n).getSimpleName());
            }
        }
    }

    private void listStatements() {
        log.info(":::STATEMENTS:::");
        String[] statements = engine.getEPAdministrator().getStatementNames();
        for (String s : statements) {
            log.info(s);
        }
    }

    private void listEventTypes() {
        log.info(":::EventTypes:::");
        EventType[] ets = engine.getEPAdministrator().getConfiguration().getEventTypes();
        for (EventType et : ets) {
            log.info("> EventName: " + et.getName());
            String[] pns = et.getPropertyNames();

            for (String pn : pns) {
                log.info(pn + " : " + et.getPropertyType(pn).getSimpleName());
            }
        }
    }

    private void printWindow(String window) {
        EPOnDemandQueryResult results = engine.getEPRuntime().executeQuery("select * from " + window);
        Iterator<EventBean> iterator = results.iterator();
        while (iterator.hasNext()) {
            String out = iterator.next().getUnderlying().toString();
            log.info(out);
        }
    }

    private void printSelect(String select) {
        engine.getEPAdministrator().createEPL(select).addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                for(EventBean event : newEvents) {
                    TweetEvent tw = (TweetEvent) event.getFragment("tweet");
                    log.info("tw: " + tw.toString());
                }
            }
        });
        (new TweetGeneratorSimulator(engine)).start();
        (new StockGeneratorSimulator(engine)).start();
    }

    private void schema() {
        String WINDOW = "create window qwindow.win:keepall() as qschema";

//        String SCHEMA = "create schema qschema as (tweet TweetEvent, stock StockEvent)";
//        String INSERT = "insert into qwindow select * from TweetEvent.std:lastevent() as tweet, StockEvent.std:lastevent() as stock";

//        String SCHEMA = "create schema qschema as (tweet.username string)";
//        String INSERT = "insert into qwindow select tweet.username from TweetEvent.std:lastevent() as tweet, StockEvent.std:lastevent() as stock";

//        String SCHEMA = "create schema qschema as (tweet.username string, stock.open float)";
//        String INSERT = "insert into qwindow select tweet.username, stock.open from TweetEvent.std:lastevent() as tweet, StockEvent.std:lastevent() as stock";

//        engine.getEPAdministrator().createEPL(SCHEMA);
//        engine.getEPAdministrator().createEPL(WINDOW);
//        engine.getEPAdministrator().createEPL(INSERT);

//        String EPL = "select tweet.username, stock.open from TweetEvent.std:lastevent() as tweet, StockEvent.std:lastevent() as stock";
//        String schema = new SchemaParser(engine).parse(EPL);
    }

    private void cassandra() {

/*
        Cluster cluster;
        Session session;

        cluster = Cluster.builder().addContactPoint("127.0.0.1").build();
        session = cluster.connect("demo");

        System.out.println("-------------------------------");
        // Use select to get the user we just entered
        ResultSet results = session.execute("SELECT * FROM users");
        for (Row row : results) {
            System.out.format("%s %d\n", row.getString("firstname"), row.getInt("age"));
        }
        System.out.println("-------------------------------");

        cluster.close();
*/



//        engine.getEPAdministrator().createEPL("select * from TweetEvent.std:lastevent() as tweet, StockEvent.std:lastevent() as stock").addListener(new UpdateListener() {
        engine.getEPAdministrator().createEPL("select * from TweetEvent").addListener(new UpdateListener() {
            @Override
            public void update(EventBean[] newEvents, EventBean[] oldEvents) {
                log.info("Received");
//                if (newEvents != null) {
//                    for (int i = 0; i < newEvents.length; i++) {
//                        EventBean event = newEvents[i];
//                        String[] properties = event.getEventType().getPropertyNames();
//                        for (String property : properties) {
//                            log.info("Property: " + event.get(property));
//                            ObjectNode node = mapper.valueToTree(event.getUnderlying());
//                            log.info(node.toString());
//                        }
//                    }
//                }
            }
        });
        engine.getEPRuntime().sendEvent(new TweetEvent("kravciak", "wtf"));
//        Helpers.runGenerators(engine);
    }

    private List<View> getCompiledView(String eplPart) {
        EPStatementObjectModel cepl = engine.getEPAdministrator().compileEPL("create window " + eplPart + " as w");
        return cepl.getCreateWindow().getViews();
    }
}
