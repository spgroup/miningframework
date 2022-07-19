package com.sun.xml.internal.ws.streaming;

import com.sun.xml.internal.ws.api.streaming.XMLStreamReaderFactory;
import com.sun.xml.internal.ws.util.FastInfosetUtil;
import com.sun.xml.internal.ws.util.xml.XmlUtil;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamSource;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.reflect.Method;
import java.net.URL;

public class SourceReaderFactory {

    static Class fastInfosetSourceClass;

    static Method fastInfosetSource_getInputStream;

    static {
        try {
            fastInfosetSourceClass = Class.forName("com.sun.xml.internal.org.jvnet.fastinfoset.FastInfosetSource");
            fastInfosetSource_getInputStream = fastInfosetSourceClass.getMethod("getInputStream");
        } catch (Exception e) {
            fastInfosetSourceClass = null;
        }
    }

    public static XMLStreamReader createSourceReader(Source source, boolean rejectDTDs) {
        return createSourceReader(source, rejectDTDs, null);
    }

    public static XMLStreamReader createSourceReader(Source source, boolean rejectDTDs, String charsetName) {
        try {
            if (source instanceof StreamSource) {
                StreamSource streamSource = (StreamSource) source;
                InputStream is = streamSource.getInputStream();
                if (is != null) {
                    if (charsetName != null) {
                        return XMLStreamReaderFactory.create(source.getSystemId(), new InputStreamReader(is, charsetName), rejectDTDs);
                    } else {
                        return XMLStreamReaderFactory.create(source.getSystemId(), is, rejectDTDs);
                    }
                } else {
                    Reader reader = streamSource.getReader();
                    if (reader != null) {
                        return XMLStreamReaderFactory.create(source.getSystemId(), reader, rejectDTDs);
                    } else {
                        return XMLStreamReaderFactory.create(source.getSystemId(), new URL(source.getSystemId()).openStream(), rejectDTDs);
                    }
                }
            } else if (source.getClass() == fastInfosetSourceClass) {
                return FastInfosetUtil.createFIStreamReader((InputStream) fastInfosetSource_getInputStream.invoke(source));
            } else if (source instanceof DOMSource) {
                DOMStreamReader dsr = new DOMStreamReader();
                dsr.setCurrentNode(((DOMSource) source).getNode());
                return dsr;
            } else if (source instanceof SAXSource) {
                Transformer tx = XmlUtil.newTransformer();
                DOMResult domResult = new DOMResult();
                tx.transform(source, domResult);
                return createSourceReader(new DOMSource(domResult.getNode()), rejectDTDs);
            } else {
                throw new XMLReaderException("sourceReader.invalidSource", source.getClass().getName());
            }
        } catch (Exception e) {
            throw new XMLReaderException(e);
        }
    }
}
