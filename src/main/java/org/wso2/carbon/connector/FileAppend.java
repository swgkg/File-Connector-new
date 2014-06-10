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

import java.io.OutputStream;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemException;
import org.apache.commons.vfs.FileSystemManager;
import org.apache.commons.vfs.VFS;
import org.apache.commons.vfs.impl.DefaultFileSystemManager;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;

/**
 * 
 * @author gayan
 * 
 */
public class FileAppend extends AbstractConnector implements Connector {

	private static Log log = LogFactory.getLog(FileAppend.class);


	
	public void connect(MessageContext messageContext) throws ConnectException {

		Object fileLocation = getParameter(messageContext, "filelocation");
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
		String ftpFileLocation =
		                         getParameter(messageContext, "ftpfilelocation") == null
		                                                                                ? ""
		                                                                                : getParameter(
		                                                                                               messageContext,
		                                                                                               "ftpfilelocation").toString();
		log.info("File append start with" + filename.toString());

		FileSystemManager fsManager;
		PrintWriter pw = null;
		OutputStream out = null;

		try {
			fsManager = VFS.getManager();
			if (fsManager != null) {

				FileObject fileObj = fsManager.resolveFile(ftpFileLocation + filename);

				// if the file does not exist, this method creates it, and the
				// parent folder, if necessary
				// if the file does exist, it appends whatever is written to the
				// output stream
				out = fileObj.getContent().getOutputStream(true);

				pw = new PrintWriter(out);
				pw.write(content);
				pw.flush();

				if (fileObj != null) {
					fileObj.close();
				}
				((DefaultFileSystemManager) fsManager).close();
			}

		} catch (FileSystemException e) {
			e.printStackTrace();
		} finally {
			if (pw != null) {
				pw.close();
			}
		}

	}
}
