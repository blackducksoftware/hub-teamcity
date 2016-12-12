<%@ include file="/include.jsp"%>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props"%>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms"%>


<jsp:useBean id="propertiesBean" scope="request"
	type="jetbrains.buildServer.controllers.BasePropertiesBean" />
<jsp:useBean id="hubConfigPersistenceManager"
	type="com.blackducksoftware.integration.hub.teamcity.server.global.ServerHubConfigPersistenceManager"
	scope="request" />


<style type="text/css">
.bdLongTextArea{
	min-width: 40em !important;
	min-height: 8em !important;
	overflow: scroll !important;
	white-space: nowrap !important;
	resize: vertical !important;
	
}
</style>

<script type="text/javascript">

</script>

<!-- Have to do this because the textProperty doesnt accept a default value -->
<c:set var="scanMemoryValue" value="${not empty propertiesBean.properties['com.blackducksoftware.integration.hub.scanMemory'] ? propertiesBean.properties['com.blackducksoftware.integration.hub.scanMemory'] : 4096}"/>
<c:set var="maxWaitTimeForRiskReportValue" value="${not empty propertiesBean.properties['com.blackducksoftware.integration.hub.maxWaitTimeForRiskReport'] ? propertiesBean.properties['com.blackducksoftware.integration.hub.maxWaitTimeForRiskReport'] : 5}"/>

			<l:settingsGroup title="Black Duck Hub">
				<tr class="noBorder" id="blackDuckHubProjectName" style="">
					<th><label
						for="com.blackducksoftware.integration.hub.projectName">
							Project Name: <bs:helpIcon
								iconTitle="Name of the Hub Project." />
					</label></th>
					<td><props:textProperty
							name="com.blackducksoftware.integration.hub.projectName"
							className="longField" /> <span class="smallNote"> Name of
							the Hub Project. </span></td>
				</tr>

				<tr class="noBorder" id="blackDuckHubProjectVersion"
					style="">
					<th><label
						for="com.blackducksoftware.integration.hub.projectVersion">
							Version: <bs:helpIcon
								iconTitle="Version of the Hub Project" />
					</label></th>
					<td><props:textProperty
							name="com.blackducksoftware.integration.hub.projectVersion"
							className="longField" /> 
							<span class="smallNote"> Version of the Hub Project. </span>
					</td>
				</tr>
				
				<tr class="noBorder" id="blackDuckHubGenerateRiskReport" style="">
					<th>
						<label for="com.blackducksoftware.integration.hub.generateRiskReport">
							Generate Black Duck Risk Report:
						</label>
					</th>
					<td>
						<props:checkboxProperty name="com.blackducksoftware.integration.hub.generateRiskReport" className="longField" />
					</td>
				</tr>

				<tr class="noBorder" id="blackDuckHubMaxWaitTimeForRiskReport">
					<th>
						<label for="com.blackducksoftware.integration.hub.maxWaitTimeForRiskReport">
							Maximum time to wait for BOM update (in minutes):
							<bs:helpIcon iconTitle="Maximum time to wait (in minutes) for the BOM to be updated with the scan results. This also gets used as the maximum time to wait for the Report to be generated. Default: 5 minutes" />
						</label>
					</th>
					<td>
						<props:textProperty name="com.blackducksoftware.integration.hub.maxWaitTimeForRiskReport" className="longField" value="${maxWaitTimeForRiskReportValue}" />
					</td>
				</tr>

				<tr class="noBorder" id="blackDuckHubScanMemory" style="">
					<th><label
						for="com.blackducksoftware.integration.hub.scanMemory">
							Hub Scan Memory (in MB's): <span class="mandatoryAsterix"
							title="Mandatory field">*</span> <bs:helpIcon
								iconTitle="Specify the amount of memory for the Hub scan to use in MB's. Default : 4096" />
					</label></th>
					
					<td> <props:textProperty
							name="com.blackducksoftware.integration.hub.scanMemory"
							className="longField" value="${scanMemoryValue}"/> 
							<span class="smallNote"> Must provide at least 4096 MB of memory. </span>
					</td>
				</tr>
				
				<tr class="noBorder" id="blackDuckHubDryRun" style="">
					<th>
						<label for="com.blackducksoftware.integration.hub.hubDryRun">
							Dry Run:
						</label>
					</th>
					<td>
						<props:checkboxProperty name="com.blackducksoftware.integration.hub.hubDryRun" className="longField" />
					</td>
				</tr>
				
				<tr class="noBorder" id="blackDuckHubScanTargets" style="">
					<th><label
						for="com.blackducksoftware.integration.hub.targets">
							Scan Targets: <bs:helpIcon
								iconTitle="If no target is provided then the entire workspace will be scanned. Please only provide one target per line." />
					</label></th>
					
					<td> <props:textProperty
							name="com.blackducksoftware.integration.hub.targets"
							className="bdLongTextArea" expandable="true"/> 
							<span class="smallNote"> Path of the target, within the workspace, to be scanned. One target per line. </span>
					</td>
				</tr>

			</l:settingsGroup>

