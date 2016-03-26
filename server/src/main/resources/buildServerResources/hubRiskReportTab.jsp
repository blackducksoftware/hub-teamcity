<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="hubRiskReportData" scope="request"
	type="com.blackducksoftware.integration.hub.report.api.HubRiskReportData" />

<link href="${teamcityPluginResourcesPath}css/HubBomReport.css"
	rel="stylesheet" type="text/css" />
<link
	href="${teamcityPluginResourcesPath}font-awesome-4.5.0/css/font-awesome.min.css"
	rel="stylesheet" type="text/css" />
<script type="text/javascript"
	src="${teamcityPluginResourcesPath}js/HubBomReportFunctions.js"></script>
<script type="text/javascript"
	src="${teamcityPluginResourcesPath}js/Sortable.js"></script>

<div class="riskReportBackgroundColor">
	<div class="reportHeader">
		<div class="h1 reportHeaderTitle">Black Duck Risk Report</div>

		<div style="float: right;">
			<img class="reportHeaderIcon"
				src="${teamcityPluginResourcesPath}images/Hub_BD_logo.png" />
		</div>
	</div>

	<div class="versionSummaryTable">
		<div>
			<div class="clickable linkText versionSummaryLargeLabel"
				onclick="window.open('${hubRiskReportData.getReport().getReportProjectUrl()}', '_blank');">
				${hubRiskReportData.htmlEscape(hubRiskReportData.getReport().getDetailedReleaseSummary().getProjectName())}</div>
			<div class="versionSummaryLargeLabel">
				<i class="fa fa-caret-right"></i>
			</div>

			<div class="clickable linkText versionSummaryLargeLabel"
				onclick="window.open('${hubRiskReportData.getReport().getReportVersionUrl()}', '_blank');">
				${hubRiskReportData.htmlEscape(hubRiskReportData.getReport().getDetailedReleaseSummary().getVersion())}</div>

			<div style="float: right;"
				class="linkText riskReportText clickable evenPadding"
				onclick="window.open('${hubRiskReportData.getReport().getReportVersionUrl()}', '_blank');">
				See more detail...</div>
		</div>
		<div>
			<div class="versionSummaryLabel">Phase:</div>
			<div class="versionSummaryLabel">${hubRiskReportData.htmlEscape(hubRiskReportData.getReport().getDetailedReleaseSummary().getPhaseDisplayValue())}</div>
			<div class="versionSummaryLabel">|</div>
			<div class="versionSummaryLabel">Distribution:</div>
			<div class="versionSummaryLabel">${hubRiskReportData.htmlEscape(hubRiskReportData.getReport().getDetailedReleaseSummary().getDistributionDisplayValue())}</div>
		</div>
	</div>

	<!-- SECURITY RISK SUMMARY -->
	<div class="riskSummaryContainer horizontal rounded">
		<div class="riskSummaryContainerLabel">
			Security Risk <i id="securityDescriptionIcon"
				class="fa fa-info-circle infoIcon"
				title="Calculated risk on number of component versions based on known vulnerabilities."></i>
		</div>

		<div class="progress-bar horizontal">
			<div id="highSecurityRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByVulnerabilityRisk(this)">High</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getVulnerabilityRiskHighCount()}</div>
			<div class="progress-track">
				<div id="highVulnerabilityRiskBar" class="progress-fill-high">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getVulnerabilityRiskHighCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="mediumSecurityRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByVulnerabilityRisk(this)">Medium</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getVulnerabilityRiskMediumCount()}</div>
			<div class="progress-track">
				<div id="mediumVulnerabilityRiskBar" class="progress-fill-medium">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getVulnerabilityRiskMediumCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="lowSecurityRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByVulnerabilityRisk(this)">Low</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getVulnerabilityRiskLowCount()}</div>
			<div class="progress-track">
				<div id="lowVulnerabilityRiskBar" class="progress-fill-low">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getVulnerabilityRiskLowCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="noneSecurityRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByVulnerabilityRisk(this)">None</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getVulnerabilityRiskNoneCount()}</div>
			<div class="progress-track">
				<div id="noVulnerabilityRiskBar" class="progress-fill-none">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getVulnerabilityRiskNoneCount())}%</span>
				</div>
			</div>
		</div>
	</div>
	<!-- SECURITY RISK SUMMARY END -->

	<!-- LICENSE  RISK SUMMARY -->
	<div class="riskSummaryContainer horizontal rounded">
		<div class="riskSummaryContainerLabel">
			License Risk <i id="licenseDescriptionIcon"
				class="fa fa-info-circle infoIcon"
				title="Calculated risk based on open source software (OSS) license use in your projects."></i>
		</div>

		<div class="progress-bar horizontal">
			<div id="highLicenseRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByLicenseRisk(this)">High</div>
			<div class="riskSummaryCount">${hubRiskReportData.getLicenseRiskHighCount()}</div>
			<div class="progress-track">
				<div id="highLicenseRiskBar" class="progress-fill-high">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getLicenseRiskHighCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="mediumLicenseRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByLicenseRisk(this)">Medium</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getLicenseRiskMediumCount()}</div>
			<div class="progress-track">
				<div id="mediumLicenseRiskBar" class="progress-fill-medium">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getLicenseRiskMediumCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="lowLicenseRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByLicenseRisk(this)">Low</div>
			<div class="riskSummaryCount">${hubRiskReportData.getLicenseRiskLowCount()}</div>
			<div class="progress-track">
				<div id="lowLicenseRiskBar" class="progress-fill-low">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getLicenseRiskLowCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="noneLicenseRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByLicenseRisk(this)">None</div>
			<div class="riskSummaryCount">${hubRiskReportData.getLicenseRiskNoneCount()}</div>
			<div class="progress-track">
				<div id="noLicenseRiskBar" class="progress-fill-none">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getLicenseRiskNoneCount())}%</span>
				</div>
			</div>
		</div>
	</div>
	<!-- LICENSE RISK SUMMARY END -->

	<!-- OPERATIONAL RISK SUMMARY -->
	<div class="riskSummaryContainer horizontal rounded">
		<div class="riskSummaryContainerLabel">
			Operational Risk <i id="operationalDescriptionIcon"
				class="fa fa-info-circle infoIcon"
				title="Calculated risk based on tracking overall open source software (OSS) component activity."></i>
		</div>

		<div class="progress-bar horizontal">
			<div id="highOperationalRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByOperationalRisk(this)">High</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getOperationalRiskHighCount()}</div>
			<div class="progress-track">
				<div id="highOperationalRiskBar" class="progress-fill-high">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getOperationalRiskHighCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="mediumOperationalRiskLabel"
				class="clickable riskSummaryLabel"
				onclick="filterTableByOperationalRisk(this)">Medium</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getOperationalRiskMediumCount()}</div>
			<div class="progress-track">
				<div id="mediumOperationalRiskBar" class="progress-fill-medium">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getOperationalRiskMediumCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="lowOperationalRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByOperationalRisk(this)">Low</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getOperationalRiskLowCount()}</div>
			<div class="progress-track">
				<div id="lowOperationalRiskBar" class="progress-fill-low">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getOperationalRiskLowCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="noneOperationalRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByOperationalRisk(this)">None</div>
			<div class="riskSummaryCount">
				${hubRiskReportData.getOperationalRiskNoneCount()}</div>
			<div class="progress-track">
				<div id="noOperationalRiskBar" class="progress-fill-none">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getOperationalRiskNoneCount())}%</span>
				</div>
			</div>
		</div>
	</div>
	<!-- OPERATIONAL RISK SUMMARY END -->

	<table class="table-summary horizontal">
		<tbody>
			<tr>
				<td class="summaryLabel" style="font-weight: bold;">BOM Entries
					:</td>
				<td class="summaryValue">${hubRiskReportData.getBomEntries().size()}</td>
			</tr>
		</tbody>
	</table>
	<table id="hubBomReport" class="table sortable">
		<thead>
			<tr>
				<th class="clickable componentColumn columnLabel evenPadding">Component</th>
				<th class="clickable componentColumn columnLabel evenPadding">Version</th>
				<th class="clickable columnLabel evenPadding">License</th>
				<th class="clickable riskColumnLabel evenPadding">H</th>
				<th class="clickable riskColumnLabel evenPadding">M</th>
				<th class="clickable riskColumnLabel evenPadding">L</th>
				<th class="clickable riskColumnLabel evenPadding"
					title="License Risk">Lic R</th>
				<th class="clickable riskColumnLabel evenPadding"
					title="Operational Risk">Opt R</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="entry" items="${hubRiskReportData.getBomEntries()}">
				<tr>
					<td class="clickable componentColumn evenPadding"
						onclick="window.open('${hubRiskReportData.getReport().getComponentUrl(entry)}', '_blank');">
						${hubRiskReportData.htmlEscape(entry.getProducerProject().getName())}</td>
					<td class="clickable componentColumn evenPadding"
						onclick="window.open('${hubRiskReportData.getReport().getVersionUrl(entry)}', '_blank');">
						${hubRiskReportData.htmlEscape(entry.getProducerReleasesDisplay())}</td>
					<td class="licenseColumn evenPadding"
						title="${entry.getLicensesDisplay()}">${entry.getLicensesDisplay()}</td>
					<td class="riskColumn"><div
							class="risk-span riskColumn risk-count">${entry.getVulnerabilityRisk().getHIGH()}</div></td>
					<td class="riskColumn"><div
							class="risk-span riskColumn risk-count">${entry.getVulnerabilityRisk().getMEDIUM()}</div></td>
					<td class="riskColumn"><div
							class="risk-span riskColumn risk-count">${entry.getVulnerabilityRisk().getLOW()}</div></td>
					<td class="riskColumn"><div
							class="risk-span riskColumn risk-count">${entry.getLicenseRiskString()}</div></td>
					<td class="riskColumn"><div
							class="risk-span riskColumn risk-count">${entry.getOperationalRiskString()}</div></td>
				</tr>
			</c:forEach>
		</tbody>
	</table>
</div>

<!-- load this script after the tables otherwise the tables wont exist yet when this script runs -->
<script type="text/javascript"
	src="${teamcityPluginResourcesPath}js/HubReportStartup.js"></script>
