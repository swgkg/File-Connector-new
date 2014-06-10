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
import java.util.Collection;

import org.apache.axiom.om.OMElement;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.vfs.FileObject;
import org.apache.commons.vfs.FileSystemOptions;
import org.apache.commons.vfs.impl.StandardFileSystemManager;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;
import org.wso2.carbon.connector.util.FTPSiteUtils;
import org.wso2.carbon.connector.util.FilePattenMatcher;
import org.wso2.carbon.connector.util.ResultPayloadCreater;

public class FileSearch extends AbstractConnector implements Connector {

	private static Log log = LogFactory.getLog(FileSearch.class);

	public void connect(MessageContext messageContext) throws ConnectException {
		Object fileLocation = getParameter(messageContext, "filelocation");
		Object filename = getParameter(messageContext, "file");
		Object content = getParameter(messageContext, "content");
		String filepattern =
		                     getParameter(messageContext, "filepattern") == null
		                                                                        ? ""
		                                                                        : getParameter(
		                                                                                       messageContext,
		                                                                                       "filepattern").toString();
		String dirpattern =
		                    getParameter(messageContext, "dirpattern") == null
		                                                                      ? ""
		                                                                      : getParameter(
		                                                                                     messageContext,
		                                                                                     "dirpattern").toString();
		Object ftpFileLocation = getParameter(messageContext, "ftpfilelocation");
		try {

			log.info("File creation started..." + filename.toString());
			// System.out.println("File creation started..." +
			// filename.toString());
			log.info("File Location..." + fileLocation.toString());
			log.info("File content..." + content.toString());

			if (ftpFileLocation != null && !ftpFileLocation.toString().equals("")) {

				StandardFileSystemManager manager = new StandardFileSystemManager();
				String sftpURL = ftpFileLocation.toString();
				FileSystemOptions opts = FTPSiteUtils.createDefaultOptions();

				manager.init();

				FileObject remoteFile = manager.resolveFile(sftpURL, opts);
				FileObject[] children = remoteFile.getChildren();
				final String FILE_PATTERN = filepattern;
				final String DIR_PATTERN = dirpattern;
				StringBuffer sb = new StringBuffer();
				sb.append("<result>");
				for (int i = 0; i < children.length; i++) {
					if (new FilePattenMatcher(FILE_PATTERN).validate(children[i].getName()
					                                                            .getBaseName()
					                                                            .toLowerCase())) {
						sb.append("<file>" + children[i].getName().getBaseName() + "</file>");
						System.out.println(children[i].getName().getBaseName());
					} else if (new FilePattenMatcher(DIR_PATTERN).validate(children[i].getName()
					                                                                  .getBaseName()
					                                                                  .toLowerCase())) {
						sb.append("<file>" + children[i].getName().getBaseName() + "</file>");
						System.out.println(children[i].getName().getBaseName());
					}
				}
				sb.append("</result>");
				// remoteFile.findFiles(arg0)
				ResultPayloadCreater resultPayload = new ResultPayloadCreater();

				OMElement element = resultPayload.performSearchMessages(sb.toString());
				resultPayload.preparePayload(messageContext, element);
				manager.close();

				log.info("File searching completed..." + filename.toString());
			} else {
				File file = new File(fileLocation.toString(), filename.toString());
				File inputDirectory = new File(fileLocation.toString());
				final String FILE_PATTERN = filepattern;
				final String DIR_PATTERN = dirpattern;
				final IOFileFilter filesFilter = new IOFileFilter() {
					@Override
					public boolean accept(File file, String s) {
						return file.isFile();
					}

					@Override
					public boolean accept(File file) {
						return new FilePattenMatcher(FILE_PATTERN).validate(file.getName()
						                                                        .toLowerCase());
					}

				};

				final IOFileFilter dirsFilter = new IOFileFilter() {
					@Override
					public boolean accept(File file, String s) {
						return file.isDirectory();
					}

					@Override
					public boolean accept(File file) {
						return new FilePattenMatcher(DIR_PATTERN).validate(file.getName()
						                                                       .toLowerCase());
					}

				};

				Collection<File> fileList =
				                            FileUtils.listFiles(inputDirectory, filesFilter,
				                                                dirsFilter);
				StringBuffer sb = new StringBuffer();
				sb.append("<result>");
				for (File f : fileList) {
					sb.append("<file>" + f.getName() + "</file>");

				}
				sb.append("</result>");
				log.info(sb.toString());
				ResultPayloadCreater resultPayload = new ResultPayloadCreater();

				OMElement element = resultPayload.performSearchMessages(sb.toString());
				resultPayload.preparePayload(messageContext, element);
			}

		} catch (Exception e) {
			throw new ConnectException(e);
		}
	}
	/*
	 * private OMElement performSearchMessages(Collection<File> files) throws
	 * XMLStreamException,
	 * IOException {
	 * OMElement resultElement =
	 * AXIOMUtil.stringToOM("<jsonObject><files/></jsonObject>");
	 * OMElement childElment = resultElement.getFirstElement();
	 * 
	 * for (File f : files) {
	 * StringBuilder stringBuilder = new StringBuilder();
	 * stringBuilder.append("{ \"message\" : ");
	 * // String json = DataObjectFactory.getRawJSON(message);
	 * stringBuilder.append(f.getName());
	 * stringBuilder.append("} ");
	 * // OMElement element =stringBuilder.toString());
	 * // childElment.addChild(element);
	 * }
	 * 
	 * if (files.size() == 0) {
	 * StringBuilder stringBuilder = new StringBuilder();
	 * stringBuilder.append("{ \"message\" : {}");
	 * stringBuilder.append("} ");
	 * // OMElement element =
	 * // super.parseJsonToXml(stringBuilder.toString());
	 * // resultElement.addChild(element);
	 * }
	 * return resultElement;
	 * 
	 * }
	 */
}
