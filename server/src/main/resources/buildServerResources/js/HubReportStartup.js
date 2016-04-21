/*******************************************************************************
 * Black Duck Software Suite SDK
 * Copyright (C) 2016 Black Duck Software, Inc.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
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
