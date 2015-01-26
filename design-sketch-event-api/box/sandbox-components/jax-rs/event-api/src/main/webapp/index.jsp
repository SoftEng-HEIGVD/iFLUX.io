<%-- 
    Document   : index
    Created on : Jan 12, 2015, 10:15:06 AM
    Author     : Olivier Liechti
--%>

<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>

<!DOCTYPE html>
<html>
  <head>
    <title>iFLUX</title>
    <meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap.min.css">
    <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/css/bootstrap-theme.min.css">
    <script src="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.1/js/bootstrap.min.js"></script>
    <script src="https://code.jquery.com/jquery-2.1.3.min.js"></script>
    <script src="scripts/client.js"></script>
  </head>
  <body>
    <div class="container">
      <div class="page-header">
        <h1>iFLUX Event API</h1>
        <p class="lead">Minimal implementation of the API specified <a href="http://www.iflux.io/api/">here</a>.</p>
      </div>
      <p>
        You can use the following root URL to access the API: <a href="api/">
          <c:out value="${pageContext.servletContext.contextPath}/api/" />
        </a>
      </p>
      <p>
        <button id="bSendEvents" type="button" class="btn btn-default">Send event to API</button>
        <button id="bGetLoggedEvents" type="button" class="btn btn-default">Get logged events (debug)</button>
      </p>

    </div>
  </body>
</html>
