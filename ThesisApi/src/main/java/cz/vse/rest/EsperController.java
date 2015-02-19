package cz.vse.rest;

import com.espertech.esper.event.EventAdapterException;
import cz.vse.startup.EsperManager;
import cz.vse.tools.Helpers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.xml.sax.SAXException;

import javax.servlet.http.HttpServletRequest;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.*;
import java.io.IOException;
import java.io.StringWriter;

/**
 * REST Web Service
 *
 * @author Martin Kravec
 */

@RestController
@RequestMapping(value = "esper", produces = "application/json")
public class EsperController {

    final Logger log = LoggerFactory.getLogger(EsperController.class);

    @Autowired
    EsperManager esper;

    @RequestMapping("ping")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ping() {
    }

    @RequestMapping(value = "events", method = RequestMethod.POST, consumes = "application/xml")
    public void processStream(HttpServletRequest request) throws IOException, XMLStreamException, ParserConfigurationException, SAXException {
        log.info("Receiving XML stream.");

        // Initialize XML Input - StAX
        XMLInputFactory factory = XMLInputFactory.newInstance();
        XMLStreamReader reader = factory.createXMLStreamReader(
                request.getInputStream());

        // Initialize XML Output
        XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
        StringWriter stringWriter = new StringWriter();
        XMLStreamWriter writer = xmlOutputFactory.createXMLStreamWriter(stringWriter);

        String elementName;
        String rootName = null;

        try {
            while (reader.hasNext()) {
                int event = reader.next();
                switch (event) {
                    case XMLStreamConstants.START_ELEMENT:
                        elementName = reader.getLocalName();
                        if (elementName.equals("events")) {
                            break;
                        }
                        if (rootName == null) {
                            rootName = elementName;
                        }
                        writer.writeStartElement(elementName);
                        break;

                    case XMLStreamConstants.CHARACTERS:
                        writer.writeCharacters(reader.getText().trim());
                        break;

                    case XMLStreamConstants.END_ELEMENT:
                        elementName = reader.getLocalName();
                        if (elementName.equals("events")) {
                            break;
                        }
                        writer.writeEndElement();

                        if (elementName.equals(rootName)) {
                            // Process XML
                            writer.flush();
                            String xmlString = stringWriter.getBuffer().toString();
                            esper.handle(Helpers.convertXmlToDocument(xmlString));

                            // Reset
                            stringWriter.getBuffer().setLength(0);
                            rootName = null;
                        }
                        break;
                }
            }
        } finally {
            writer.close();
            stringWriter.close();
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({XMLStreamException.class, EventAdapterException.class})
    public String conflict(Exception e) {
        log.info("Exception: " + e.getMessage());
        return e.getMessage();
    }

}
