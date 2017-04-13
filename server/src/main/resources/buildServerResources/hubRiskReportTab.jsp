<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core"%>

<jsp:useBean id="bundle" scope="request"
	type="com.blackducksoftware.integration.hub.util.HubResourceBundleHelper" />
<jsp:useBean id="teamcityBaseUrl" type="java.lang.String" scope="request"/>
     
    <iframe id="reportFrame" width="100%" ></iframe>
	<script type="text/javascript">
	    var frame = document.getElementById("reportFrame");
	    frame.onload = function() {
	      setTimeout(function () {
	         var frame = document.getElementById("reportFrame");
	         frame.height = frame.contentWindow.document.body.scrollHeight;
	      }, 200);
	    };
	    frame.src="${hubRiskReportUrl}";
	</script> 