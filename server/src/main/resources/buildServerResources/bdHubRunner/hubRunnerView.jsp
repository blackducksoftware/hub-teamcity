<%@ include file="/include.jsp" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>

<!-- script type="text/javascript">

</script> -->

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>

<l:settingsGroup title="Black Duck Hub">

		 <div class="parameter">
            Project Name: <props:displayValue
                name="com.blackducksoftware.integration.hub.projectName" />
        </div>
         <div class="parameter">
            Version: <props:displayValue
                name="com.blackducksoftware.integration.hub.projectVersion" />
        </div>
        <div class="parameter">
            Phase: <props:displayValue
                name="com.blackducksoftware.integration.hub.phase" />
        </div>
        <div class="parameter">
            Distribution: <props:displayValue
                name="com.blackducksoftware.integration.hub.distribution" />
        </div>
        <div class="parameter">
            Match Adjustments: <props:displayValue name="com.blackducksoftware.integration.hub.projectLevelAdjustments" />
        </div>
        <div class="parameter">
            Generate Black Duck Risk Report: <props:displayValue name="com.blackducksoftware.integration.hub.generateRiskReport" />
        </div>
        <div class="parameter">
            Maximum time to wait for BOM update (in minutes): <props:displayValue name="com.blackducksoftware.integration.hub.maxWaitTimeForRiskReport" />
        </div>
        <div class="parameter">
            Hub Scan Memory (in MB's): <props:displayValue
                name="com.blackducksoftware.integration.hub.scanMemory"
                emptyValue="4096"/>
        </div>
        <div class="parameter">
            Code Location Name: <props:displayValue
                name="com.blackducksoftware.integration.hub.codeLocationName" />
        </div>
        <div class="parameter">
            Dry Run: <props:displayValue name="com.blackducksoftware.integration.hub.hubDryRun" />
        </div>
         <div class="parameter">
            Cleanup logs on successful scan: <props:displayValue name="com.blackducksoftware.integration.hub.cleanupOnSuccessfulScan" />
        </div>
        <div class="parameter">
            Unmap Previous Code Locations: <props:displayValue name="com.blackducksoftware.integration.hub.unmapPreviousCodeLocations" />
        </div>
        <div class="parameter">
            Delete Previous Code Locations: <props:displayValue name="com.blackducksoftware.integration.hub.deletePreviousCodeLocations" />
        </div>
        <div class="parameter">
            Scan Targets: <props:displayValue
                name="com.blackducksoftware.integration.hub.targets" />
        </div>
        <div class="parameter">
            Directory Exclusion Patterns: <props:displayValue
                name="com.blackducksoftware.integration.hub.excludePatterns" />
        </div>
</l:settingsGroup>