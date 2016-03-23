<%@ page import="com.blackducksoftware.integration.hub.teamcity.server.failure.BDPolicyViolationBuildFeature" %>
<%@ include file="/include.jsp" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean"/>
<!-- 
<tr class="noBorder" id="blackDuckPolicyViolationFail">
    <th>
        <label>
            Fail Build if :
        </label>
    </th>
    <td>
            <span id="policyViolationDescription"  class="smallNote">
                There are any Hub Policy violations.
            </span>
    </td>
</tr>
-->
    