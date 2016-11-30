# XML custom gatherer

Custom gatherer for use with a custom collection for download of XML files.

Add URLs for XML to fetch to collection.cfg.start.urls file. Format is 1 URL per line. Each URL is fetched and the XML is processed using the set of options defined. Note: only 1 set of xml options is supported so the same settings will be applied to all the XML files downloaded.

Note: you need to set the following collection.cfg option otherwise cache copies will not work:

store.record.type=XmlRecord

Supports the following collection.cfg settings:

## Collection.cfg options

### xml.node

*Values:* path to a element in GPath style (dot-notation) / * (all children, default)

eg. 
feed.xml
```
<books>
	<book>
		<info>
			<title> The Adventures Of Sherlock Holmes </title>
			<author> Arthur Conan Doyle </author>
		</info>

		<contents>
			<chapter>A Scandal in Bohemia</chapter>
			<chapter>The Red-headed League</chapter>
			...
			<chapter>The Adventure of the Copper Beeches</chapter>
		</contents>
	</book>

	<book>
		...
	</book>
</books>
```

xml.node=book // store \<book\> records

xml.node=book.contents // store only \<contents\> records from \<book\> node

### xml.debug

*Values:* true (print out some additional information to the logs) / false (default)
