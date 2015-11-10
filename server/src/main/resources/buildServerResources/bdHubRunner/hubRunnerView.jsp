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
            Distribtion: <props:displayValue
                name="com.blackducksoftware.integration.hub.distribution" />
        </div>
        <div class="parameter">
            CLI Path: <props:displayValue
                name="com.blackducksoftware.integration.hub.cliPath" />
        </div>
        <div class="parameter">
            Hub Scan Memory (in MB's): <props:displayValue
                name="com.blackducksoftware.integration.hub.scanMemory"
                emptyValue="4096"/>
        </div>
        <div class="parameter">
            Scan Targets: <props:displayValue
                name="com.blackducksoftware.integration.hub.targets" />
        </div>
</l:settingsGroup>