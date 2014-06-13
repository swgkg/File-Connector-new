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
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;
import org.wso2.carbon.connector.util.FTPSiteUtils;
import org.wso2.carbon.connector.util.ResultPayloadCreater;

public class FileRename extends AbstractConnector implements Connector {

	public void connect(MessageContext messageContext) throws ConnectException {
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
		String content =
		                 getParameter(messageContext, "content") == null
		                                                                ? ""
		                                                                : getParameter(
		                                                                               messageContext,
		                                                                               "content").toString();
		String newFileName =
		                     getParameter(messageContext, "newfilename") == null
		                                                                        ? ""
		                                                                        : getParameter(
		                                                                                       messageContext,
		                                                                                       "newfilename").toString();

		String filebeforepprocess =
		                            getParameter(messageContext, "filebeforepprocess") == null
		                                                                                      ? ""
		                                                                                      : getParameter(
		                                                                                                     messageContext,
		                                                                                                     "filebeforepprocess").toString();

		try {

			System.out.println("File creation started..." + filename.toString());
			System.out.println("File Location..." + fileLocation.toString());
			System.out.println("File content..." + content.toString());

			StandardFileSystemManager manager = new StandardFileSystemManager();

			try {
				manager.init();

				// Create remote object
				FileObject remoteFile =
				                        manager.resolveFile(fileLocation.toString() +
				                                                    filename.toString(),
				                                            FTPSiteUtils.createDefaultOptions());

				FileObject reNameFile =
				                        manager.resolveFile(fileLocation.toString() +
				                                                    newFileName.toString(),
				                                            FTPSiteUtils.createDefaultOptions());
				if (remoteFile.exists()) {
					if (!filebeforepprocess.equals("")) {
						FileObject fBeforeProcess =
						                            manager.resolveFile(filebeforepprocess +
						                                                filename);
						fBeforeProcess.copyFrom(remoteFile, Selectors.SELECT_SELF);
					}

					remoteFile.moveTo(reNameFile);
					ResultPayloadCreater resultPayload = new ResultPayloadCreater();
					String responce = "<result>Suceess</result>";
					OMElement element = resultPayload.performSearchMessages(responce);
					resultPayload.preparePayload(messageContext, element);
					log.info("Rename remote file success");
				}
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				manager.close();
			}
			// return dir;

		} catch (Exception e) {
			throw new ConnectException(e);
		}
	}
}
