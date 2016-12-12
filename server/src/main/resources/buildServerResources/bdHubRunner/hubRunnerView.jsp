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
            Dry Run: <props:displayValue name="com.blackducksoftware.integration.hub.hubDryRun" />
        </div>
        <div class="parameter">
            Scan Targets: <props:displayValue
                name="com.blackducksoftware.integration.hub.targets" />
        </div>
</l:settingsGroup>