<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="hubRiskReportData" scope="request"
	type="com.blackducksoftware.integration.hub.api.report.HubRiskReportData" />

<jsp:useBean id="bundle" scope="request"
	type="com.blackducksoftware.integration.hub.util.HubResourceBundleHelper" />

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
		<div class="h1 reportHeaderTitle">${bundle.getString("title")}</div>

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
				${bundle.getString("hub.report.link")}</div>
		</div>
		<div>
			<div class="versionSummaryLabel">${bundle.getString("phase")}:</div>
			<div class="versionSummaryLabel">${hubRiskReportData.htmlEscape(hubRiskReportData.getReport().getDetailedReleaseSummary().getPhaseDisplayValue())}</div>
			<div class="versionSummaryLabel">|</div>
			<div class="versionSummaryLabel">${bundle.getString("distribution")}:</div>
			<div class="versionSummaryLabel">${hubRiskReportData.htmlEscape(hubRiskReportData.getReport().getDetailedReleaseSummary().getDistributionDisplayValue())}</div>
		</div>
	</div>

	<!-- SECURITY RISK SUMMARY -->
	<div class="riskSummaryContainer horizontal rounded">
		<div class="riskSummaryContainerLabel">
			${bundle.getString("vulnerability.risk.title")} <i id="securityDescriptionIcon"
				class="fa fa-info-circle infoIcon"
				title="${bundle.getString('vulnerability.risk.description')}"></i>
		</div>

		<div class="progress-bar horizontal">
			<div id="highSecurityRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.high")}</div>
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
				onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.medium")}</div>
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
				onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.low")}</div>
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
				onclick="filterTableByVulnerabilityRisk(this)">${bundle.getString("entry.none")}</div>
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
			${bundle.getString("license.risk.title")} <i id="licenseDescriptionIcon"
				class="fa fa-info-circle infoIcon"
				title="${bundle.getString('license.risk.description')}"></i>
		</div>

		<div class="progress-bar horizontal">
			<div id="highLicenseRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.high")}</div>
			<div class="riskSummaryCount">${hubRiskReportData.getLicenseRiskHighCount()}</div>
			<div class="progress-track">
				<div id="highLicenseRiskBar" class="progress-fill-high">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getLicenseRiskHighCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="mediumLicenseRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.medium")}</div>
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
				onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.low")}</div>
			<div class="riskSummaryCount">${hubRiskReportData.getLicenseRiskLowCount()}</div>
			<div class="progress-track">
				<div id="lowLicenseRiskBar" class="progress-fill-low">
					<span>${hubRiskReportData.getPercentage(hubRiskReportData.getLicenseRiskLowCount())}%</span>
				</div>
			</div>
		</div>

		<div class="progress-bar horizontal">
			<div id="noneLicenseRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByLicenseRisk(this)">${bundle.getString("entry.none")}</div>
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
			${bundle.getString("operational.risk.title")} <i id="operationalDescriptionIcon"
				class="fa fa-info-circle infoIcon"
				title="${bundle.getString('operational.risk.description')}"></i>
		</div>

		<div class="progress-bar horizontal">
			<div id="highOperationalRiskLabel" class="clickable riskSummaryLabel"
				onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.high")}</div>
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
				onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.medium")}</div>
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
				onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.low")}</div>
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
				onclick="filterTableByOperationalRisk(this)">${bundle.getString("entry.none")}</div>
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
				<td class="summaryLabel" style="font-weight: bold;">${bundle.getString("entries")}:</td>
				<td class="summaryValue">${hubRiskReportData.getBomEntries().size()}</td>
			</tr>
		</tbody>
	</table>
	<table id="hubBomReport" class="table sortable">
		<thead>
			<tr>
				<th/>
				<th class="clickable componentColumn columnLabel evenPadding">${bundle.getString("component")}</th>
				<th class="clickable componentColumn columnLabel evenPadding">${bundle.getString("version")}</th>
				<th class="clickable columnLabel evenPadding">${bundle.getString("license")}</th>
				<th class="clickable riskColumnLabel evenPadding">${bundle.getString("entry.high.short")}</th>
				<th class="clickable riskColumnLabel evenPadding">${bundle.getString("entry.medium.short")}</th>
				<th class="clickable riskColumnLabel evenPadding">${bundle.getString("entry.low.short")}</th>
				<th class="clickable riskColumnLabel evenPadding"
					title="${bundle.getString('license.risk.title')}">${bundle.getString("license.risk.title.short")}</th>
				<th class="clickable riskColumnLabel evenPadding"
					title="${bundle.getString('operational.risk.title')}">${bundle.getString("operational.risk.title.short")}</th>
			</tr>
		</thead>
		<tbody>
			<c:forEach var="entry" items="${hubRiskReportData.getBomEntries()}">
				<tr>
					<td class="evenPadding violation"><i class="fa fa-ban"></i><div>${entry.getPolicyApprovalStatus()}</div></td>
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
