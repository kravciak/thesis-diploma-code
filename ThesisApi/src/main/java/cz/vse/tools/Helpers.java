package cz.vse.tools;

import com.espertech.esper.client.EventPropertyDescriptor;
import com.espertech.esper.client.EventType;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import org.json.JSONObject;
import org.json.XML;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/**
 *
 * @author Martin Kravec
 */
public class Helpers {

    public static ObjectMapper mapper = new ObjectMapper();

    public static ObjectNode eventTypeToJSON(EventType event) {
        ObjectNode node = mapper.createObjectNode();
        for (EventPropertyDescriptor desc : event.getPropertyDescriptors()) {
            if (desc.isFragment()) {
                EventType inner = event.getFragmentType(desc.getPropertyName()).getFragmentType();
                node.set(desc.getPropertyName(), eventTypeToJSON(inner));
            } else {
                node.put(desc.getPropertyName(), desc.getPropertyType().getSimpleName());
            }
        }
        return node;
    }

    public static String convertDocumentToString(Document doc) {
        TransformerFactory tf = TransformerFactory.newInstance();
        Transformer transformer;
        try {
            transformer = tf.newTransformer();
            // below code to remove XML declaration
            // transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
            StringWriter writer = new StringWriter();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));
            String output = writer.getBuffer().toString();
            return output;
        } catch (TransformerException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static Document convertJsonToDocument(String root, String jsonStr) throws ParserConfigurationException, SAXException, IOException {
        JSONObject json = new JSONObject("{\""+root+"\":" + jsonStr + "}");
        String xml = XML.toString(json);
        return Helpers.convertXmlToDocument(xml);
    }

    public static Document convertXmlToDocument(String xmlStr) throws IOException, SAXException, ParserConfigurationException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder;

        builder = factory.newDocumentBuilder();
        return builder.parse(new InputSource(new StringReader(xmlStr)));
    }

}
