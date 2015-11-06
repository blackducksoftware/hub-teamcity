package com.blackducksoftware.integration.hub.teamcity.mocks;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;

import org.mockito.Mockito;

import com.intellij.util.enumeration.ArrayEnumeration;
import com.intellij.util.enumeration.EmptyEnumeration;

public class MockHttpServletRequest {

    public static HttpServletRequest getMockedHttpServletRequest() {
        HttpServletRequest mockedHttpServletRequest = Mockito.mock(HttpServletRequest.class);

        Mockito.when(mockedHttpServletRequest.getParameter(Mockito.anyString())).thenReturn("");

        return mockedHttpServletRequest;
    }

    public static void addGetParameter(final HttpServletRequest mockedRequest, final String parameterName, final String parameterValue) {
        Mockito.when(mockedRequest.getParameter(parameterName)).thenReturn(parameterValue);
    }

    public static void requestHasParamters(final HttpServletRequest mockedRequest, final boolean hasParameters) {
        if (hasParameters) {
            Enumeration enumeration = new ArrayEnumeration(new String[] { "" });

            // .getParameterNames().hasMoreElements()
            Mockito.when(mockedRequest.getParameterNames()).thenReturn(enumeration);
        } else {
            Enumeration enumeration = EmptyEnumeration.INSTANCE;

            Mockito.when(mockedRequest.getParameterNames()).thenReturn(enumeration);
        }

    }
}
