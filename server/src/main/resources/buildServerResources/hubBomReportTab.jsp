<%@ include file="/include.jsp" %>

<jsp:useBean id="hubBomReportData" scope="request" type="com.blackducksoftware.integration.hub.report.api.HubBomReportData" />

<div>
  <p>I'm a very happy monkey.</p>
</div>

vRHC: ${hubBomReportData.getLicenseRiskNoneCount()}
???