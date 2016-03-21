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
