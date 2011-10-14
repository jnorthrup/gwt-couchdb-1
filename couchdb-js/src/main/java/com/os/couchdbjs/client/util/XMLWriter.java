package com.os.couchdbjs.client.util;

import com.google.gwt.core.client.JavaScriptObject;

public class XMLWriter {
	private static final native void initJs() /*-{
    $wnd.XMLWriter = function(encoding, version) {
        if (encoding)
            {
                this.encoding = encoding;
            }
        if (version)
            {
                this.version = version;
            }
    };
    (function() {
        $wnd.XMLWriter.prototype = {
            encoding:'ISO-8859-1',// what is the encoding
            version:'1.0', //what xml version to use
            formatting: 'indented', //how to format the output (indented/none)  ?
            indentChar:'\t', //char to use for indent
            indentation: 1, //how many indentChar to add per level
            newLine: '\n', //character to separate nodes when formatting
            //start a new document, cleanup if we are reusing
            writeStartDocument:writeStartDocument,
            //get back to the root
            writeEndDocument:writeEndDocument,
            //start a new fragment, cleanup if we are reusing
            writeStartFragment:writeStartFragment,
            //set the text of the doctype
            writeDocType:writeDocType,
            //start a new node with this name, and an optional namespace
            writeStartElement:writeStartElement,
            //go up one node, if we are in the root, ignore it
            writeEndElement:writeEndElement,
            //add an attribute to the active node
            writeAttributeString:writeAttributeString,
            //add a text node to the active node
            writeString:writeString,
            //shortcut, open an element, write the text and close
            writeElementString:writeElementString,
            //add a text node wrapped with CDATA
            writeCDATA:writeCDATA,
            //add a text node wrapped in a comment
            writeComment:writeComment,
            //generate the xml string, you can skip closing the last nodes
            flush:flush,
            //cleanup, don't use again without calling startDocument
            close:close
        };

        function close() {
            if (this.root)
                {
                    clean(this.root);
                }
            this.active = this.root = this.stack = null;
        }

        function flush() {
            if (this.stack && this.stack[0])//ensure it's closed
                {
                    this.writeEndDocument();
                }
            var
                    chr = '', indent = '', num = this.indentation,
                    formatting = 'indented' == this.formatting.toLowerCase(),
                    buffer = '<?xml version="' + this.version + '" encoding="' + this.encoding + '"';
            if (this.standalone !== undefined)
                {
                    if (this.standalone) {
                        buffer += ' standalone="yes"';
                    }
                    else {
                        buffer += ' standalone="no"';
                    }
                }
            buffer += ' ?>';
            buffer = [buffer];
            if (this.doctype && this.root)
                {
                    buffer.push('<!DOCTYPE ' + this.root.n + ' ' + this.doctype + '>');
                }
            if (this.fragment)
                {
                    buffer = [];
                }
            if (formatting) {
                while (num--)
                    {
                        chr += this.indentChar;
                    }
            }
            if (this.root)//skip if no element was added
                {
                    format(this.root, indent, chr, buffer);
                }
            return buffer.join(formatting ? this.newLine : '');
        }

        function writeComment(text) {
            this.writeString('<!-- ' + text + ' -->');
        }

        function writeCDATA(text) {
            this.writeString('<![CDATA[' + text + ']]>');
        }

        function writeElementString(name, text, ns) {
            writeStartElement(name, ns);
            writeString(text);
            writeEndElement();
        }

        function writeString(text) {
            if (this.active)
                {
                    this.active.c.push(text);
                }
        }

        function writeAttributeString(name, value) {
            if (this.active)
                {
                    this.active.a[name] = value;
                }
        }

        function writeEndElement() {
            this.active = this.stack.pop() || this.root;
        }

        function writeStartElement(name, ns) {
            if (ns)//namespace
                {
                    name = ns + ':' + name;
                }

            var node = { n:name, a:{ }, c: [ ] };//(n)ame, (a)ttributes, (c)hildren

            if (this.active) {
                this.active.c.push(node);
                this.stack.push(this.active);
            } else
                {
                    this.root = node;
                }
            this.active = node;
        }

        function writeDocType(dt) {
            this.doctype = dt;
        }

        function writeStartFragment() {
            this.close();//cleanup
            this.stack = [ ];
            this.standalone = standalone;
            this.fragment = true;
        }

        function writeEndDocument() {
            this.active = this.root;
            this.stack = [ ];
        }

        function writeStartDocument(standalone) {
            this.close();//cleanup
            this.stack = [ ];
            this.standalone = standalone;
            this.fragment = false;
        }

        //utility, you don't need it
        function clean(node) {
            var l = node.c.length;
            while (l--) {
                if ('object' == typeof node.c[l])
                    {
                        clean(node.c[l]);
                    }
            }
            node.n = node.a = node.c = null;
        }

      //utility, you don't need it
        function format(node, indent, chr, buffer) {
            var
                    xml = indent + '<' + node.n,
                    nc = node.c.length,
                    attr, child, i = 0;
            for (attr in node.a)
                {
                    xml += ' ' + attr + '="' + node.a[attr] + '"';
                }
            xml += nc ? '>' : ' />';
            buffer.push(xml);
            if (nc) {
                do{
                    child = node.c[i++];
                    if ('string' == typeof child) {
                        if (1 == nc)//single text node
                            {
                                return buffer.push(buffer.pop() + child + '</' + node.n + '>');
                            }
                        else //regular text node
                            {
                                buffer.push(indent + chr + child);
                            }
                    } else if ('object' == typeof child) //element node
                        {
                            format(child, indent + chr, chr, buffer);
                        }
                } while (i < nc);
                buffer.push(indent + '</' + node.n + '>');
            }
        }

    })();
}-*/;

	private static final JavaScriptObject createNativeWriter() {
		return createNativeWriter("UTF-8");
	}

	private static final native JavaScriptObject createNativeWriter(String pEncoding) /*-{
		return new $wnd.XMLWriter(pEncoding,'1.0');
	}-*/;

	private static boolean _INIT_JS;
	
	public static XMLWriter createWriter() {
		if(!_INIT_JS) {
			initJs();
			_INIT_JS = true;
		}
		return new XMLWriter(createNativeWriter());
	}

	private JavaScriptObject m_javaObj;

	private XMLWriter(JavaScriptObject pWriter) {
		m_javaObj = pWriter;
	}

	public void writeStartDocument(boolean pStandalone) {
		nativeWriteStartDocument(m_javaObj, pStandalone);
	}

	public void writeStartFragment() {
		nativeWriteStartFragment(m_javaObj);
	}

	public void writeEndDocument() {
		nativeWriteEndDocument(m_javaObj);
	}

	public void writeDocType(String pDeclarations) {
		nativeWriteDocType(m_javaObj, pDeclarations);
	}

	public void writeStartElement(String pName) {
		nativeWriteStartElement(m_javaObj, pName);
	}

	public void writeStartElement(String pName, String pNS) {
		nativeWriteStartElement(m_javaObj, pName, pNS);
	}

	public void writeEndElement() {
		nativeWriteEndElement(m_javaObj);
	}

	public void writeAttributeString(String pName, String pValue) {
		nativeWriteAttributeString(m_javaObj, pName, pValue);
	}

	public void writeString(String pText) {
		nativeWriteString(m_javaObj, pText);
	}

	public void writeElementString(String pName, String pText) {
		nativeWriteElementString(m_javaObj, pName, pText);
	}

	public void writeElementString(String pName, String pText, String pNS) {
		nativeWriteElementString(m_javaObj, pName, pText, pNS);
	}

	public void writeCDATA(String pData) {
		nativeWriteCDATA(m_javaObj, pData);
	}

	public void writeComment(String pText) {
		nativeWriteComment(m_javaObj, pText);
	}

	public String flush() {
		return nativeFlush(m_javaObj);
	}

	public void close() {
		nativeClose(m_javaObj);
	}

	private final native void nativeWriteStartDocument(JavaScriptObject pObj, boolean pStandalone) /*-{
		pObj.writeStartDocument(pStandalone);
	}-*/;

	private final native void nativeWriteEndDocument(JavaScriptObject pObj) /*-{
		pObj.writeEndDocument();
	}-*/;

	private final native void nativeWriteDocType(JavaScriptObject pObj, String pDeclarations) /*-{
		pObj.writeDocType(pDeclarations);
	}-*/;

	private final native void nativeWriteStartElement(JavaScriptObject pObj, String pName) /*-{
		pObj.writeStartElement(pName);
	}-*/;

	private final native void nativeWriteStartElement(JavaScriptObject pObj, String pName, String pNS) /*-{
		pObj.writeStartElement(pName,pNS);
	}-*/;

	private final native void nativeWriteEndElement(JavaScriptObject pObj) /*-{
		pObj.writeEndElement();
	}-*/;

	private final native void nativeWriteAttributeString(JavaScriptObject pObj, String pName, String pValue) /*-{
		pObj.writeAttributeString(pName,pValue);
	}-*/;

	private final native void nativeWriteString(JavaScriptObject pObj, String pText) /*-{
		pObj.writeString(pText);
	}-*/;

	private final native void nativeWriteElementString(JavaScriptObject pObj, String pName, String pText) /*-{
		pObj.writeElementString(pName,pText);
	}-*/;

	private final native void nativeWriteElementString(JavaScriptObject pObj, String pName, String pText, String pNS) /*-{
		pObj.writeElementString(pName,pText,pNS);
	}-*/;

	private final native void nativeWriteCDATA(JavaScriptObject pObj, String pData) /*-{
		pObj.writeCDATA(pData);
	}-*/;

	private final native void nativeWriteComment(JavaScriptObject pObj, String pText) /*-{
		pObj.writeComment(pText);
	}-*/;

	private final native String nativeFlush(JavaScriptObject pObj) /*-{
		return pObj.flush();
	}-*/;

	private final native String nativeClose(JavaScriptObject pObj) /*-{
		return pObj.close();
	}-*/;

	private final native void nativeWriteStartFragment(JavaScriptObject pObj) /*-{
		pObj.writeStartFragment();
	}-*/;
}