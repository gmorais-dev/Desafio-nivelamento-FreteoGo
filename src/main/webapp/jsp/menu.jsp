<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Menu</title>

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
</head>
<body>


<nav class="navbar">

 
    <a href="${pageContext.request.contextPath}/jsp/menu.jsp" class="navbar-brand">
        <div class="logo-icon">
            <span class="material-symbols-outlined">local_shipping</span>
        </div>
        <span class="logo-text">Frete<span>Go</span></span>
    </a>

    <div class="navbar-user">

        <div class="user-info">
            <span class="material-symbols-outlined">account_circle</span>
            <span id="nome-usuario">Usuário</span>
        </div>

        <a href="${pageContext.request.contextPath}/jsp/auth.jsp" class="btn-sair">
            <span class="material-symbols-outlined">logout</span>
            Sair
        </a>

    </div>
</nav>


<main class="main-content">

    
    <%
        String msg = request.getParameter("msg");
        String toastTitulo = null, toastTexto = null;
        if ("cliente_cadastrado".equals(msg)) {
            toastTitulo = "Cliente cadastrado com sucesso!";
            toastTexto  = "Os dados da empresa foram salvos e já estão disponíveis no sistema.";
        } else if ("motorista_cadastrado".equals(msg)) {
            toastTitulo = "Motorista cadastrado com sucesso!";
            toastTexto  = "Os dados do motorista foram salvos e já estão disponíveis na equipe.";
        } else if ("veiculo_cadastrado".equals(msg)) {
            toastTitulo = "Veículo cadastrado com sucesso!";
            toastTexto  = "Os dados do veículo foram salvos e já estão disponíveis na frota.";
        } else if ("frete_cadastrado".equals(msg)) {
            toastTitulo = "Frete cadastrado com sucesso!";
            toastTexto  = "O contrato foi emitido e já está disponível para acompanhamento.";
        } else if ("mensageria_processada".equals(msg)) {
            String enviados = request.getParameter("enviados");
            String erros = request.getParameter("erros");
            String total = request.getParameter("total");
            toastTitulo = "Mensageria processada!";
            toastTexto  = "Eventos encontrados: " + (total == null ? "0" : total)
                + " | enviados: " + (enviados == null ? "0" : enviados)
                + " | com erro: " + (erros == null ? "0" : erros) + ".";
        } else if ("mensageria_falha".equals(msg)) {
            toastTitulo = "Falha ao publicar mensageria";
            toastTexto  = request.getParameter("detalhe");
        }
    %>
    <% if (toastTitulo != null && !"mensageria_falha".equals(msg)) { %>
    <div class="toast toast-sucesso" id="toastSucesso">
        <span class="material-symbols-outlined toast-icon">check_circle</span>
        <div class="toast-body">
            <strong><%= toastTitulo %></strong>
            <p><%= toastTexto %></p>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()" title="Fechar">
            <span class="material-symbols-outlined">close</span>
        </button>
    </div>
    <% } %>
    <%
        if ("mensageria_falha".equals(msg)) {
    %>
    <div class="toast" id="toastFalha" style="border-left: 4px solid #f87171; background: #2d1111;">
        <span class="material-symbols-outlined toast-icon" style="color:#fca5a5;">error</span>
        <div class="toast-body">
            <strong><%= toastTitulo %></strong>
            <p><%= toastTexto == null ? "Erro ao enviar eventos para a API de mensageria." : toastTexto %></p>
        </div>
        <button class="toast-close" onclick="this.parentElement.remove()" title="Fechar">
            <span class="material-symbols-outlined">close</span>
        </button>
    </div>
    <%
        }
    %>

    <!-- Boas-vindas -->
    <div class="welcome-block">
        <h1>Bem-vindo de volta</h1>
        <p>O que você quer fazer hoje?</p>
    </div>

    <section class="menu-section">

        <div class="section-label">
            <div class="bar"></div>
            <div>
                <h2>Cadastros</h2>
                <p>Adicione e gerencie informações do sistema</p>
            </div>
        </div>

        <div class="cards-grid">

            <a href="${pageContext.request.contextPath}/jsp/form-cliente.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">apartment</span>
                </div>
                <div class="card-text">
                    <h3>Clientes</h3>
                    <p>Cadastre empresas e contatos</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/jsp/form-motorista.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">group</span>
                </div>
                <div class="card-text">
                    <h3>Motoristas</h3>
                    <p>Gerencie sua equipe</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/jsp/form--veiculo.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">local_shipping</span>
                </div>
                <div class="card-text">
                    <h3>Veículos</h3>
                    <p>Frota e documentação</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/FreteController" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">receipt_long</span>
                </div>
                <div class="card-text">
                    <h3>Frete</h3>
                    <p>Cadastre e acompanhe operações de frete</p>
                </div>
            </a>

        </div>
    </section>

    <section class="menu-section">

        <div class="section-label">
            <div class="bar"></div>
            <div>
                <h2>Consultas</h2>
                <p>Visualize e analise seus dados</p>
            </div>
        </div>

        <div class="cards-grid">

            <a href="${pageContext.request.contextPath}/jsp/consulta-clientes.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">apartment</span>
                </div>
                <div class="card-text">
                    <h3>Clientes</h3>
                    <p>Pesquisar por CNPJ, razão social ou empresa</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/jsp/consulta-motorista.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">group</span>
                </div>
                <div class="card-text">
                    <h3>Motoristas</h3>
                    <p>Pesquisar por CPF, CNH ou nome</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/jsp/consulta-veiculo.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">local_shipping</span>
                </div>
                <div class="card-text">
                    <h3>Veículos</h3>
                    <p>Pesquisar por placa, RNTRC ou status</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/jsp/dashboard-fretes.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">receipt_long</span>
                </div>
                <div class="card-text">
                    <h3>Dashboard de Fretes</h3>
                    <p>Tempo real, mensageria e status</p>
                </div>
            </a>

        </div>
    </section>

    <!-- ?? SEÇÃO: Operações ????????????????????????????????? -->
    <section class="menu-section">

        <div class="section-label">
            <div class="bar"></div>
            <div>
                <h2>Operações</h2>
                <p>Gerencie o fluxo de entregas</p>
            </div>
        </div>

        <div class="cards-grid">

            <a href="${pageContext.request.contextPath}/FreteController" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">add_circle</span>
                </div>
                <div class="card-text">
                    <h3>Novo Frete</h3>
                    <p>Emita um novo conhecimento</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/jsp/dashboard-fretes.jsp" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">move_to_inbox</span>
                </div>
                <div class="card-text">
                    <h3>Painel em Tempo Real</h3>
                    <p>Acompanhe fila e atualizações</p>
                </div>
            </a>

            <a href="${pageContext.request.contextPath}/MensageriaController?acao=enviarPendentes" class="menu-card">
                <div class="card-icon">
                    <span class="material-symbols-outlined">publish</span>
                </div>
                <div class="card-text">
                    <h3>Publicar Mensageria</h3>
                    <p>Envia eventos pendentes para a API</p>
                </div>
            </a>

        </div>
    </section>

</main>

</body>
<script>
    // Lê o nome do usuário salvo no login
    var usuario = JSON.parse(localStorage.getItem('usuario') || '{}');
    var nome    = usuario.nome || 'Usuário';
    document.getElementById('nome-usuario').textContent = nome;

    // Auto-fechar toast de sucesso após 6 segundos
    var toast = document.getElementById('toastSucesso');
    if (toast) {
        setTimeout(function () {
            toast.style.transition = 'opacity 0.5s';
            toast.style.opacity = '0';
            setTimeout(function () { toast.remove(); }, 500);
        }, 6000);
    }
</script>
</html>
