package org.wso2.carbon.connector.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;

public class ResultPayloadCreater {

	public void preparePayload(MessageContext messageContext, OMElement element) {
		SOAPBody soapBody = messageContext.getEnvelope().getBody();
		for (Iterator itr = soapBody.getChildElements(); itr.hasNext();) {
			OMElement child = (OMElement) itr.next();
			child.detach();
		}
		for (Iterator itr = element.getChildElements(); itr.hasNext();) {
			OMElement child = (OMElement) itr.next();
			soapBody.addChild(child);
		}
	}

	public OMElement performSearchMessages(String output) throws XMLStreamException,

	IOException, JSONException {
		OMElement resultElement;
		if (!output.equals("")) {
			resultElement = AXIOMUtil.stringToOM(output);
		} else {
			resultElement = AXIOMUtil.stringToOM("<result></></result>");
		}

		return resultElement;

	}

	private OMElement parseJsonToXml(String sb) throws JSONException, XMLStreamException,
	                                           IOException {
		StringWriter sw = new StringWriter(5120);
		// OMElement elm = JsonBuilder.toXml(new
		// ByteArrayInputStream(sb.getBytes()), false);
		OMFactory factory = OMAbstractFactory.getOMFactory();
		OMNamespace ns1 = factory.createOMNamespace("wso2", "www.wso2.com");
		OMElement elm = factory.createOMElement("successful", ns1);// ;AXIOMUtil.stringToOM("Succeessful");
		return elm;
	}
}
