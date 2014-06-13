/*
 * Copyright (c) 2005-2010, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 * 
 * WSO2 Inc. licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License.
 * You may obtain a copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.wso2.carbon.connector;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Iterator;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMAbstractFactory;
import org.apache.axiom.om.OMElement;
import org.apache.axiom.om.OMFactory;
import org.apache.axiom.om.OMNamespace;
import org.apache.axiom.om.util.AXIOMUtil;
import org.apache.axiom.soap.SOAPBody;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;
import org.wso2.carbon.connector.util.FTPSiteUtils;

public class FileCopyInStream extends AbstractConnector implements Connector {

	private static Log log = LogFactory.getLog(FileCreate.class);

	public void connect(MessageContext messageContext) throws ConnectException {

		Object fileLocation = getParameter(messageContext, "filelocation");
		Object filename = getParameter(messageContext, "file");
		Object content = getParameter(messageContext, "content");
		Object newFileLocation = getParameter(messageContext, "newfilelocation");
		int buffSize = 200000;
		/**
		 * if
		 * (Integer.parseInt(messageContext.getProperty("buffersize").toString()
		 * .split(".".toString())[0]) > 0) {
		 * buffSize =
		 * Integer.parseInt(messageContext.getProperty("buffersize").toString()
		 * .split(".".toString())[0]);
		 * }
		 */
		/*
		 * String buffSize1 =
		 * ConnectorUtils.lookupTemplateParamater(messageContext, "buffersize")
		 * .toString();
		 */

		try {

			log.info("File creation started..." + filename.toString());
			log.info("File Location..." + fileLocation.toString());
			log.info("File content..." + content.toString());

			StandardFileSystemManager manager = new StandardFileSystemManager();
			String sftpURL = newFileLocation + filename.toString();
			FileSystemOptions opts = FTPSiteUtils.createDefaultOptions();
			File file = new File(fileLocation.toString(), filename.toString());
			manager.init();
			FileObject localFile = manager.resolveFile(file.getAbsolutePath());
			FileObject remoteFile = manager.resolveFile(sftpURL, opts);
			final int BUFF_SIZE = buffSize;
			final byte[] buffer = new byte[BUFF_SIZE];

			InputStream fin = localFile.getContent().getInputStream();
			OutputStream fout = remoteFile.getContent().getOutputStream();

			IOUtils.copyLarge(fin, fout);
			/*
			 * while (true) {
			 * synchronized (buffer) {
			 * int amountRead = fin.read(buffer);
			 * if (amountRead == -1) {
			 * break;
			 * }
			 * fout.write(buffer, 0, amountRead);
			 * }
			 * }
			 */
			manager.close();
			OMElement element = this.performSearchMessages();

			preparePayload(messageContext, element);
			log.info("File copying completed..." + filename.toString());
			// return dir;

		} catch (Exception e) {
			throw new ConnectException(e);
		}
	}

	private void preparePayload(MessageContext messageContext, OMElement element) {
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

	private OMElement performSearchMessages() throws XMLStreamException,

	IOException, JSONException {
		OMElement resultElement = AXIOMUtil.stringToOM("<jsonObject><Success/></jsonObject>");
		OMElement childElment = resultElement.getFirstElement();

		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("{ \"message\" : ");
		String json = "test";// DataObjectFactory.getRawJSON(message);
		stringBuilder.append(json);
		stringBuilder.append("} ");
		OMElement element = parseJsonToXml(stringBuilder.toString());

		return resultElement;

	}

	public OMElement parseJsonToXml(String sb) throws JSONException, XMLStreamException,
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
