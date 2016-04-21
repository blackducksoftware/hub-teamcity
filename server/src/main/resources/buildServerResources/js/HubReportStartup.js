/*******************************************************************************
 * Copyright (C) 2016 Black Duck Software, Inc.
 * http://www.blackducksoftware.com/
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version 2 only
 * as published by the Free Software Foundation.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License version 2
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
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
