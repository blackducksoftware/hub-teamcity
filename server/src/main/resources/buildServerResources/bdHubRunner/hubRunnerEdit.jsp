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

</style>

<script type="text/javascript">

</script>

<!-- Have to do this because the textProperty doesnt accept a default value -->
<c:set var="scanMemoryValue"
       value="${not empty propertiesBean.properties['com.blackducksoftware.integration.hub.hubScanMemory'] ? propertiesBean.properties['com.blackducksoftware.integration.hub.hubScanMemory'] : 4096}"/>
       

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
				
				<tr class="noBorder" id="blackDuckHubVersionPhase"
					style="">
					<th><label
						for="com.blackducksoftware.integration.hub.phase">
							Phase: <bs:helpIcon
								iconTitle="Choose the Phase at which this Version is in its life cycle. This Phase will be used when creating the Version, changing this will NOT update the Version's Phase" />
					</label></th>
					<td><props:textProperty
							name="com.blackducksoftware.integration.hub.phase"
							className="longField" /> 
							<span class="smallNote"> Phase at which this Version is in. </span>
					</td>
				</tr>
				
				<tr class="noBorder" id="blackDuckHubVersionDistribution"
					style="">
					<th><label
						for="com.blackducksoftware.integration.hub.distribution">
							Distribution: <bs:helpIcon
								iconTitle="Choose how this Version is planned to be distributed. This will be used when creating the Version, changing this will NOT update the Version's Distribution" />
					</label></th>
					<td><props:textProperty
							name="com.blackducksoftware.integration.hub.distribution"
							className="longField" /> 
							<span class="smallNote"> Distribution type for this Version. </span>
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
				
				<tr class="noBorder" id="blackDuckHubScanTargets" style="">
					<th><label
						for="com.blackducksoftware.integration.hub.targets">
							Scan Targets: <bs:helpIcon
								iconTitle="Provide the path to the target that you would like to scan within this workspace. If no target is provided then the entire workspace will be scanned." />
					</label></th>
					
					<td> <props:textProperty
							name="com.blackducksoftware.integration.hub.targets"
							className="longField" /> 
							<span class="smallNote"> Path of the target, within the workspace, to be scanned. </span>
					</td>
				</tr>

			</l:settingsGroup>

