/*******************************************************************************
 * Copyright 2021 EPOS ERIC
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/

package org.epos.handler.operations.monitoring;


import java.util.ArrayList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class URLGeneration {

	private static final Logger LOGGER = LoggerFactory.getLogger(URLGeneration.class);

	private static ArrayList<String> blackList = new ArrayList<>();
	static {
		blackList.add("()");
		blackList.add("(,,,)");
		blackList.add("[]");
		blackList.add("[,,,]");
	}

	private URLGeneration() {}

	/**
	 * Generate URL from a template "http://url.org/{id}{?param}" and a parametermap
	 * 
	 * 
	 * @param template
	 * @param map
	 * @return
	 */
	public static String generateURLFromTemplateAndMap(String template, Map<String, Object> map)
	{
		
		System.out.println("URL ---> "+template);
		System.out.println("MAP ---> "+map);

		boolean isSegment = false;
		StringBuilder sb = new StringBuilder();
		sb.ensureCapacity(template.length());

		for(int i = 0; i < template.length(); i++) {
			char c = template.charAt(i);
			switch(c) {
			case '{': {
				isSegment = true;
				sb.append('{'); break;
			}
			case '}': {
				sb.append('}'); 
				isSegment = false;
				String calcTemplate = calculateTemplate(sb.toString(), map);
				if(calcTemplate!=null) {
					template = template.replace(sb.toString(), calcTemplate);
					if(template.contains("{")) {
						template = generateURLFromTemplateAndMap(template, map);
					}
				}else {
					template = template.replace(sb.toString(), "");
				}
				break;
			}
			default: {
				if(isSegment) sb.append(c);
				break;
			}
			}
		}

		return template;
	}


	private static String calculateTemplate(String segment, Map<String, Object> map) {

		segment = segment.replaceAll("\\{", "").replaceAll("\\}", "");
		StringBuilder segmentOutput = new StringBuilder();

		String switchValue = segment.startsWith("%20AND%20")? "%20AND%20" : segment.substring(0, 1);
		
		switch(switchValue) {
		case "?":
			String[] parameters = segment.replace("?","").split(",");
			segmentOutput.append("");
			for(int i=0;i<parameters.length;i++) {
				if(map.containsKey(parameters[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters[i].trim()+"="+map.get(parameters[i].trim()));
					}
					else {
						segmentOutput.append("&"+parameters[i].trim()+"="+map.get(parameters[i].trim()));
					}
				}
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "?");
			break;
		case "&":
			String[] parameters1 = segment.replace("&","").split(",");
			segmentOutput.append("");
			for(int i=0;i<parameters1.length;i++) { 
				if(map.containsKey(parameters1[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters1[i].trim()+"="+map.get(parameters1[i].trim()));
					}
					else {
						segmentOutput.append("&"+parameters1[i].trim()+"="+map.get(parameters1[i].trim()));
					}
				}
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "&");
			break;
		case "/":
			String[] parameters2 = segment.replace("/","").split(",");
			segmentOutput.append("");
			for(int i=0;i<parameters2.length;i++) { 
				if(map.containsKey(parameters2[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters2[i].trim()+"="+map.get(parameters2[i].trim()));
					}
					else {
						segmentOutput.append("&"+parameters2[i].trim()+"="+map.get(parameters2[i].trim()));
					}
				}
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "/");;
			break;
		case "%20AND%20":
			String[] parameters3 = segment.replace("%20AND%20","").split(",");
			segmentOutput.append("");
			for(int i=0;i<parameters3.length;i++) { 
				if(map.containsKey(parameters3[i].trim())) {	
					if(segmentOutput.length()<=1) {
						segmentOutput.append(parameters3[i].trim()+"="+map.get(parameters3[i].trim()));
					}
					else {
						segmentOutput.append("%20AND%20"+parameters3[i].trim()+"="+map.get(parameters3[i].trim()));
					}
				}
			}
			if(!segmentOutput.toString().equals("")) segmentOutput.insert(0, "%20AND%20");
			break;
		default:
			String[] parameters11 = segment.split(",");
			for(int i=0;i<parameters11.length;i++) {
				if(map.containsKey(parameters11[i].trim())) {	
					segmentOutput.append(","+map.get(parameters11[i].trim()));
				}
			}try {
				String aux =  segmentOutput.substring(1, segmentOutput.length());
				segmentOutput = new StringBuilder();
				segmentOutput.append(aux);
			}catch(Exception e) {
				LOGGER.error(e.getMessage());
			}
			break;
		}

		return segmentOutput.toString();
	}

}

