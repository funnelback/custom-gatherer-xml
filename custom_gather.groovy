import com.funnelback.common.*
import com.funnelback.common.config.*
import com.funnelback.common.io.store.*
import com.funnelback.common.io.store.xml.*
import com.funnelback.common.utils.*
import java.net.URL

// XML imports
import groovy.xml.*

// Read $SEARCH_HOME
def searchHome = Environment.getValidSearchHome().getCanonicalPath()

// Create a configuration object to read collection.cfg
def config = new NoOptionsConfig(new File(args[0]), args[1])

// Create a Store instance to store gathered data
def store = new XmlStoreFactory(config).newStore()

// Define default values
def xmlNode  = '*'
def xmlDebug = false

// Read config from collection.cfg
if (config.value("xml.node") != null)  xmlNode  = config.value("xml.node")
if (config.value("xml.debug") != null) xmlDebug = config.value("xml.debug")

// Open the XML store
store.open()
// Open the start URLs file
File file = new File(searchHome + File.separatorChar + "conf" + File.separatorChar+config.value("collection") + File.separatorChar + "collection.cfg.start.urls")

def line
file.withReader { reader ->
	while ((line = reader.readLine()) != null) {
		println "Gathering XML for " + line

		// Fetch the XML file
		String xmlText = new URL(line).getText();
		Node rootNode  = new XmlParser().parseText(stripNonValidXMLCharacters(xmlText))
	    // Figure out the Node to use to split the document
	    def Node = Eval.x(rootNode, 'x.'+xmlNode)
		
		def i = 0

		Node.each { node ->
			// Check to see if the update has been stopped
			if (i % 100 == 0) {
				if (config.isUpdateStopped()) {
					store.close()
					throw new RuntimeException("Update stop requested by user.")
				}
				config.setProgressMessage("Processed " + i + " records")
			}
		
			def xmlString = XmlUtil.serialize(node)
			if (xmlDebug) println xmlString
			
			def xmlContent = XMLUtils.fromString(xmlString)
			store.add(new XmlRecord(xmlContent, line + '/doc' + (i++)))
		}
	}
}
// close() required for the store to be flushed
store.close()

/**
 * This method ensures that the output String has only
 * valid XML unicode characters as specified by the
 * XML 1.0 standard. For reference, please see
 * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
 * standard</a>. This method will return an empty
 * String if the input is null or empty.
 *
 * @param in The String whose non-valid characters we want to remove.
 * @return The in String, stripped of non-valid characters.
 */
public String stripNonValidXMLCharacters(String input) {
    StringBuffer out = new StringBuffer(); // Used to hold the output.
    char current; // Used to reference the current character.

    def c=0

    if (input == null || ("".equals(input))) return ""; // vacancy test.
    for (int i = 0; i < input.length(); i++) {
        current = input.charAt(i); // NOTE: No IndexOutOfBoundsException caught here; it should not happen.
        if ((current == 0x9) ||
            (current == 0xA) ||
            (current == 0xD) ||
            ((current >= 0x20) && (current <= 0xD7FF)) ||
            ((current >= 0xE000) && (current <= 0xFFFD)) ||
            ((current >= 0x10000) && (current <= 0x10FFFF))) {
                out.append(current);
        }
        else {c++}
    }
    println ("Stripped "+c+" illegal chars")
    return out.toString();
}

