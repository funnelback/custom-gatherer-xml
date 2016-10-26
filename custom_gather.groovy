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
		Node rootNode  = new XmlParser().parseText(xmlText)
		def i = 0

		rootNode."${xmlNode}".each { node ->
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

