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

import org.apache.axiom.om.OMElement;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
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
public class FileDelete extends AbstractConnector implements Connector {

	private static Log log = LogFactory.getLog(FileCreate.class);

	public void connect(MessageContext messageContext) throws ConnectException {
		System.out.println("File deletion started...");
		String fileLocation =
		                      getParameter(messageContext, "filelocation") == null
		                                                                          ? ""
		                                                                          : getParameter(
		                                                                                         messageContext,
		                                                                                         "filelocation").toString();
		String filename =
		                  getParameter(messageContext, "file") == null
		                                                              ? ""
		                                                              : getParameter(
		                                                                             messageContext,
		                                                                             "file").toString();

		String filebeforepprocess =
		                            getParameter(messageContext, "filebeforepprocess") == null
		                                                                                      ? ""
		                                                                                      : getParameter(
		                                                                                                     messageContext,
		                                                                                                     "filebeforepprocess").toString();

		try {

			log.info("File deletion started..." + filename.toString());
			log.info("File Location..." + fileLocation);

			StandardFileSystemManager manager = new StandardFileSystemManager();

			try {
				manager.init();

				// Create remote object
				FileObject remoteFile =
				                        manager.resolveFile(fileLocation + filename,
				                                            FTPSiteUtils.createDefaultOptions());
				if (!filebeforepprocess.equals("")) {
					FileObject fBeforeProcess = manager.resolveFile(filebeforepprocess + filename);
					fBeforeProcess.copyFrom(remoteFile, Selectors.SELECT_SELF);
				}

				if (remoteFile.exists()) {
					remoteFile.delete();
					ResultPayloadCreater resultPayload = new ResultPayloadCreater();
					String responce = "<result>Suceess</result>";
					OMElement element = resultPayload.performSearchMessages(responce);
					resultPayload.preparePayload(messageContext, element);
					log.info("Delete remote file success");
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				manager.close();
			}

		} catch (Exception e) {
			throw new ConnectException(e);
		}
	}
}
