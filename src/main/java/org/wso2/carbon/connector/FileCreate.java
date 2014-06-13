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

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.xml.stream.XMLStreamException;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileContent;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.Selectors;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.synapse.MessageContext;
import org.codehaus.jettison.json.JSONException;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;
import org.wso2.carbon.connector.util.FTPSiteUtils;
import org.wso2.carbon.connector.util.ResultPayloadCreater;

/**
 * 
 * @author gayan
 * @description Handle the file creation functionality
 */
public class FileCreate extends AbstractConnector implements Connector {

	private static Log log = LogFactory.getLog(FileCreate.class);

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

		String contentType =
		                     getParameter(messageContext, "contenttype") == null
		                                                                        ? ""
		                                                                        : getParameter(
		                                                                                       messageContext,
		                                                                                       "contenttype").toString();

		String encoding =
		                  getParameter(messageContext, "encoding") == null
		                                                                  ? ""
		                                                                  : getParameter(
		                                                                                 messageContext,
		                                                                                 "encoding").toString();
		String filebeforepprocess =
		                            getParameter(messageContext, "filebeforepprocess") == null
		                                                                                      ? ""
		                                                                                      : getParameter(
		                                                                                                     messageContext,
		                                                                                                     "filebeforepprocess").toString();
		String fileafterprocsess =
		                           getParameter(messageContext, "fileafterprocsess") == null
		                                                                                    ? ""
		                                                                                    : getParameter(
		                                                                                                   messageContext,
		                                                                                                   "fileafterprocsess").toString();
		Object ftpFileLocation = getParameter(messageContext, "ftpfilelocation");// "ftp://gayan:ws02@localhost:21/home/gayan/ftp/"
		PrintWriter pw = null;

		log.info("File creation started..." + filename);
		log.info("File Location..." + fileLocation);
		log.info("File content..." + content);

		String sftpURL = fileLocation + filename;

		boolean resultStatus = false;
		try {
			FileSystemOptions opts = FTPSiteUtils.createDefaultOptions();
			StandardFileSystemManager manager = new StandardFileSystemManager();

			if (content.toString().equals("")) {

				manager.init();

				FileObject remoteFile = manager.resolveFile(sftpURL, opts);

				// remoteFile.getContent().setAttribute("ContentType",
				// contentType);

				remoteFile.createFile();

				FileObject fileAfterProcess = manager.resolveFile(sftpURL, opts);
				fileAfterProcess.copyFrom(remoteFile, Selectors.SELECT_SELF);
				manager.close();
			} else {

				FileSystemManager fsManager;

				OutputStream out = null;

				fsManager = VFS.getManager();
				if (fsManager != null) {

					FileObject fileObj = fsManager.resolveFile(sftpURL);

					// if the file does not exist, this method creates it,
					// and the
					// parent folder, if necessary
					// if the file does exist, it appends whatever is
					// written to the
					// output stream

					FileContent fileContent = fileObj.getContent();

					// fileContent.setAttribute("Content-Type", contentType);

					out = fileContent.getOutputStream(true);

					if (encoding.equals("")) {
						pw = new PrintWriter(out);
						pw.write(content.toString());
						pw.flush();
					} else {
						IOUtils.write(content, out, encoding);
					}
					if (fileObj != null) {
						fileObj.close();
					}

					FileObject setContentFile = fsManager.resolveFile(sftpURL);
					if (setContentFile.exists()) {

						// fileObj.getContent().setAttribute("ContentType",
						// contentType);
					}

					if (setContentFile != null) {
						setContentFile.close();
					}
					((DefaultFileSystemManager) fsManager).close();
				}
				resultStatus = true;
			}
		} catch (FileSystemException e) {
			e.printStackTrace();
			log.info(e.getMessage());
			resultStatus = false;

		} catch (IOException e) {
			e.printStackTrace();
			log.info(e.getMessage());
			resultStatus = false;

		}

		finally {
			if (pw != null) {
				pw.close();
			}

		}

		generateOutput(messageContext, resultStatus);

		log.info("File create completed....");

	}

	/**
	 * 
	 * @param messageContext
	 * @param resultStatus
	 */
	private void generateOutput(MessageContext messageContext, boolean resultStatus) {
		ResultPayloadCreater resultPayload = new ResultPayloadCreater();
		String responce = "<result>Creation status : " + resultStatus + "</result>";

		try {
			OMElement element = resultPayload.performSearchMessages(responce);
			resultPayload.preparePayload(messageContext, element);
		} catch (XMLStreamException | IOException | JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
