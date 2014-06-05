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
import java.util.Arrays;

import org.apache.commons.io.FileUtils;
import org.apache.synapse.MessageContext;
import org.wso2.carbon.connector.core.AbstractConnector;
import org.wso2.carbon.connector.core.ConnectException;
import org.wso2.carbon.connector.core.Connector;

public class FileRead extends AbstractConnector implements Connector {

	public void connect(MessageContext messageContext) throws ConnectException {
		Object fileLocation = getParameter(messageContext, "filelocation");
		Object filename = getParameter(messageContext, "file");
		Object content = getParameter(messageContext, "content");
		try {

			System.out.println("File creation started..." + filename.toString());
			System.out.println("File Location..." + fileLocation.toString());
			System.out.println("File content..." + content.toString());

			File file = new File(fileLocation.toString(), filename.toString());
			File newFile = new File("/home/gayan/", filename.toString());
			byte[] buf = FileUtils.readFileToByteArray(file);
			System.out.println("Reading contents form file : " + Arrays.toString(buf));
			String str = FileUtils.readFileToString(newFile, "US_ASCII");
			System.out.println("Reading contents form file : " + str);
			// return dir;

		} catch (Exception e) {
			throw new ConnectException(e);
		}
	}
}