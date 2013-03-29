package jenkins.plugins.p4;

import java.io.FileOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import javanet.staxutils.IndentingXMLEventWriter;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;

import com.perforce.p4java.core.IChangelistSummary;

/**
 * Changelist objects as {@link IChangelistSummary} are saved to XML document
 * with the help of Stax (Streaming API for XML) API.
 * 
 * @author mitapani
 *
 */
public class ChangelistToXML {
    
    /**
     * Save a list of changelist elements to XML format.
     * 
     * @param XMLFile Path to the xml file
     * @param changelists List of {@link IChangelistSummary} elements
     * @throws Exception
     */
    public static void saveChangelist(String XMLFile, List<IChangelistSummary> changelists) throws Exception {
        // Setup StaX
        XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(XMLFile));
        eventWriter = new IndentingXMLEventWriter(eventWriter);
        XMLEventFactory eventFactory = XMLEventFactory.newInstance();

        // Create and write start tag
        eventWriter.add(eventFactory.createStartDocument());

        // Create changelist open tag
        eventWriter.add(eventFactory.createStartElement("", "", "changelist"));
        
        // Create an instance of SimpleDateFormat used for formatting 
        // the string representation of date
        DateFormat df = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        
        if (changelists != null) {
            // Write the different nodes
            for (IChangelistSummary changelist : changelists) {
                // Create entry open tag
                eventWriter.add(eventFactory.createStartElement("", "", "entry"));
                createNode(eventWriter, "changenumber", Integer.toString(changelist.getId()));
                createNode(eventWriter, "date", df.format(changelist.getDate()));
                createNode(eventWriter, "description", changelist.getDescription());
                createNode(eventWriter, "user", changelist.getUsername());
                eventWriter.add(eventFactory.createEndElement("", "", "entry"));
            }
        }

        // Create end tag
        eventWriter.add(eventFactory.createEndElement("", "", "changelist"));
        eventWriter.add(eventFactory.createEndDocument());
        eventWriter.close();
    }

    /**
     * Create a new element to the xml.
     * 
     * @param eventWriter Handle to the xml writer
     * @param name Name of the new element
     * @param value Value of the new element
     * @throws XMLStreamException
     */
    private static void createNode(XMLEventWriter eventWriter, String name, String value) 
            throws XMLStreamException {
    
    XMLEventFactory eventFactory = XMLEventFactory.newInstance();
        
    // Create start node
    eventWriter.add(eventFactory.createStartElement("", "", name));
    
    // Create content
    eventWriter.add(eventFactory.createCharacters(value));
    
    // Create end node
    eventWriter.add(eventFactory.createEndElement("", "", name));
    }
}
