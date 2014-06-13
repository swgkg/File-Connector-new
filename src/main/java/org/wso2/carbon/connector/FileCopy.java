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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;
import org.wso2.carbon.connector.util.FTPSiteUtils;
import org.wso2.carbon.connector.util.ResultPayloadCreater;

/**
 * 
 * @author gayan
 * 
 */
public class FileCopy extends AbstractConnector implements Connector {

	private static Log log = LogFactory.getLog(FileCreate.class);

	public void connect(MessageContext messageContext) throws ConnectException {

		Object fileLocation = getParameter(messageContext, "filelocation");
		Object filename = getParameter(messageContext, "file");
		Object content = getParameter(messageContext, "content");
		Object newFileLocation = getParameter(messageContext, "newfilelocation");
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
			remoteFile.copyFrom(localFile, Selectors.SELECT_SELF);

			manager.close();
			ResultPayloadCreater resultPayload = new ResultPayloadCreater();

			String responce = "<result>Suceess</result>";
			OMElement element = resultPayload.performSearchMessages(responce);
			resultPayload.preparePayload(messageContext, element);
			log.info("File copying completed..." + filename.toString());
			// return dir;

		} catch (Exception e) {
			throw new ConnectException(e);
		}
	}
}
