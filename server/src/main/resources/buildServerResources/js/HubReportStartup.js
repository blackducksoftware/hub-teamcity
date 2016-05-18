/*******************************************************************************
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
 *******************************************************************************/
var pageBody = document.getElementById("page-body");
if (pageBody) {
	// If the element was found set the class name
	pageBody.className += " pageBody";
}

adjustWidth(document.getElementById("highVulnerabilityRiskBar"));
adjustWidth(document.getElementById("mediumVulnerabilityRiskBar"));
adjustWidth(document.getElementById("lowVulnerabilityRiskBar"));
adjustWidth(document.getElementById("noVulnerabilityRiskBar"));

adjustWidth(document.getElementById("highLicenseRiskBar"));
adjustWidth(document.getElementById("mediumLicenseRiskBar"));
adjustWidth(document.getElementById("lowLicenseRiskBar"));
adjustWidth(document.getElementById("noLicenseRiskBar"));

adjustWidth(document.getElementById("highOperationalRiskBar"));
adjustWidth(document.getElementById("mediumOperationalRiskBar"));
adjustWidth(document.getElementById("lowOperationalRiskBar"));
adjustWidth(document.getElementById("noOperationalRiskBar"));

adjustTable();
