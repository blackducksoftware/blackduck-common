/**
 * Hub Common
 *
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
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
package com.blackducksoftware.integration.hub.api.component.version;

import java.util.List;

import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.commons.lang3.builder.RecursiveToStringStyle;
import org.apache.commons.lang3.builder.ReflectionToStringBuilder;

import com.blackducksoftware.integration.hub.api.item.HubResponse;

public class LicenseInfo extends HubResponse {
	
	private final String type;
	private final List<License> licenses;
	
	public LicenseInfo(final String licensesType, final List<License> licenses){
		this.type = licensesType;
		this.licenses = licenses;
	}

	public String getType() {
		return type;
	}

	public List<License> getLicenses() {
		return licenses;
	}
	
	@Override
	public String toString() {
		return ReflectionToStringBuilder.toString(this, RecursiveToStringStyle.JSON_STYLE);
	}
	
	//http://stackoverflow.com/questions/8180430/how-to-override-equals-method-in-java
	@Override
	public boolean equals(Object obj){
		if(obj == null){
			return false;
		}
		
		if(!LicenseInfo.class.isAssignableFrom(obj.getClass())){
			return false;
		}
		final LicenseInfo licInfoObj = (LicenseInfo)obj;
		
		if((this.type==null) ? (licInfoObj.getType() != null) : (!this.type.equals(licInfoObj.getType()))){
			return false;
		}
		if((this.licenses==null) ? (licInfoObj.getLicenses() != null) : (!this.licenses.equals(licInfoObj.getLicenses()))){
			return false;
		}
		
		return true;
	}
	
	//https://commons.apache.org/proper/commons-lang/apidocs/org/apache/commons/lang3/builder/HashCodeBuilder.html
	@Override
	public int hashCode(){
		HashCodeBuilder builder = new HashCodeBuilder(13, 23).
				append(type);
		for(License l : licenses){
			builder.append(l);
		}
		return builder.toHashCode();
	}
	
}
