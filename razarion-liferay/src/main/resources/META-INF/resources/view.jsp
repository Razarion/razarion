<%--
/**
 * Copyright (c) 2000-present Liferay, Inc. All rights reserved.
 *
 * This library is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License as published by the Free
 * Software Foundation; either version 2.1 of the License, or (at your option)
 * any later version.
 *
 * This library is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
 * details.
 */
--%>
<%@ taglib uri="http://java.sun.com/portlet_2_0" prefix="portlet" %>
<%@page import="javax.portlet.PortletURL"%>

<script type="text/javascript">erraiBusRemoteCommunicationEnabled = false;</script>
<script type="text/javascript">erraiJaxRsJacksonMarshallingActive = true;</script>
<script src="<%=request.getContextPath()%>/razarion_client/razarion_client.nocache.js"></script>

<p>
	<b class="redBackground">xxx8xxx</b>

	<portlet:defineObjects />
	<%
		PortletURL actionURL = renderResponse.createActionURL();
		actionURL.setParameter("javax.portlet.action", "gwtlogging");
		actionURL.setParameter("some-parameter", "some-value");
	%>

	<a href="<%=actionURL%>">Call Process Action</a></p>