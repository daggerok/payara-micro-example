<%--
  Created by IntelliJ IDEA.
  User: mak
  Date: 11/6/18
  Time: 01:32
  To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<html>
<head>
  <title>Payara Micro</title>
</head>
<body>
<div id="app"></div>
<script>
  (function ready() {
    document.addEventListener('DOMContentLoaded', function (evt) {
      fetch('/v1')
        .then(function (data) {
          document.querySelector('#app').textContent = data.text();
        });
    }, false);
  }());
</script>
</body>
</html>
