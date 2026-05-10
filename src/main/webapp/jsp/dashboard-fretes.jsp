<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Mensageria.MensageriaConfig" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Dashboard de Fretes | FreteGo</title>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/dashboard-fretes/dashboard-fretes.css">
</head>
<body>
    <div
        id="dashboard-fretes-root"
        data-mensageria-ws-url="<%= MensageriaConfig.websocketUrl() %>"
        data-mensageria-topic="<%= MensageriaConfig.websocketTopic() %>"
    ></div>
    <script type="module" src="${pageContext.request.contextPath}/dashboard-fretes/dashboard-fretes.js"></script>
</body>
</html>
