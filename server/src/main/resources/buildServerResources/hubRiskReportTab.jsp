<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="bundle" scope="request"
	type="com.blackducksoftware.integration.hub.util.HubResourceBundleHelper" />
<jsp:useBean id="teamcityBaseUrl" type="java.lang.String" scope="request"/>
     
     <link href="${teamcityBaseUrl}${teamcityPluginResourcesPath}plugin/blackduck-hub/css/HubBomReport.css"  rel="stylesheet" type="text/css"  />
     <link href="${teamcityBaseUrl}${teamcityPluginResourcesPath}plugin/blackduck-hub/font-awesome-4.5.0/css/font-awesome.min.css"  rel="stylesheet" type="text/css"  />
     <script type="text/javascript"
             src="${teamcityBaseUrl}${teamcityPluginResourcesPath}plugin/blackduck-hub/js/jquery-3.1.1.min.js" />
     <script type="text/javascript"
             src="${teamcityBaseUrl}${teamcityPluginResourcesPath}plugin/blackduck-hub/js/Sortable.js" />
     <script type="text/javascript"
             src="${teamcityBaseUrl}${teamcityPluginResourcesPath}plugin/blackduck-hub/js/HubBomReportFunctions.js" />
     <script type="text/javascript"
             src="${teamcityBaseUrl}${teamcityPluginResourcesPath}plugin/blackduck-hub/js/HubRiskReport.js" />
     
    <div id="riskReportDiv"></div>
    <script type="text/javascript">
      var myJQuery = jQuery.noConflict();
      myJQuery(document).ready(function () {
          console.log("Risk Report Document ready.");
          var data = ${teamcityBaseUrl}${hubRiskReportJsonUrl};
          var riskReport = new RiskReport(myJQuery,data);
          riskReport.createReport();
      });
</script>