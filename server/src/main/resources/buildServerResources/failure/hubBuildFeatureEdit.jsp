<%@ page import="com.blackducksoftware.integration.hub.teamcity.server.failure.HubFailureType" %>
<%@ include file="/include.jsp" %>

<%@ taglib prefix="props" tagdir="/WEB-INF/tags/props" %>
<%@ taglib prefix="l" tagdir="/WEB-INF/tags/layout" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="forms" tagdir="/WEB-INF/tags/forms" %>

<jsp:useBean id="propertiesBean" scope="request" type="jetbrains.buildServer.controllers.BasePropertiesBean" />

<script type="text/javascript">
    BS.hub = {
        var descriptions = {},

        var addDescription: fuction(failureConditionString, failureConditionDescription) {
            descriptions[failureConditionString] = failureConditionDescription;
        },

        var toggleDescription: function(select) {
            var options = select.options;
            var index = select.selectedIndex;
            var selectedValue = options[index].value;
            $('failureTypeDescription').html(descriptions[selectedValue]);
        }
    };
</script>

<tr class="noBorder">
    <th>
        <label for="com.blackducksoftware.integration.hub.hubFailureType">
            Fail Build if :
        </label>
    </th>
    <td>
        <props:selectProperty name="com.blackducksoftware.integration.hub.hubFailureType" className="tabIndex" multiple="false" onchange="BS.hub.toggleDescription(this)">
            <c:set var="failureConditions" value="<%= HubFailureType.values() %>" />
            <c:forEach var="failureCondition" items="${failureConditions}">
                <c:set var="selected" value="false" />
                <c:set var="failureConditionString" value="${failureCondition}" />
                <c:set var="failureConditionDisplayName" value="${failureCondition.displayName}" />
                <c:set var="failureConditionDescription" value="${failureCondition.description}" />

                <script>BS.hub.addDescription(failureConditionString, failureConditionDescription);</script>

                <c:if test="${failureConditionString == propertiesBean.properties['com.blackducksoftware.integration.hub.hubFailureType']}">
                    <c:set var="selected" value="true" />
                    <c:set var="initialDescriptionToShow" value="${failureConditionDescription}" />
                </c:if>

                <props:option value="${failureConditionString}" selected="${selected}">
                    <c:out value="${failureConditionDisplayName}" />
                </props:option>
            </c:forEach>
        </props:selectProperty>

        <span id="failureTypeDescription" class="smallNote">
            <c:out value="${initialDescriptionToShow}" />
        </span>
    </td>
</tr>
