<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Consulta de Motoristas</title>

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/consulta/consulta-motorista.css">
</head>
<body data-context-path="${pageContext.request.contextPath}">
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
    <div class="consulta-container">
        <div class="section-label">
            <div class="bar"></div>
            <div>
                <h2>Consulta de Motoristas</h2>
                <p>Pesquise por CPF, CNH, nome, vínculo ou status</p>
            </div>
        </div>

        <div class="filtros">
            <div class="filtros-header">
                <div class="filtros-title">
                    <span class="material-symbols-outlined">filter_list</span>
                    <div>
                        <strong>FILTROS</strong>
                        <div class="filtros-sub">Buscar por</div>
                    </div>
                </div>
                <div class="filtros-actions">
                    <button id="btnLimpar" class="btn-clear" title="Limpar filtros">✕</button>
                </div>
            </div>

            <div class="row filtros-row">
                <select class="select-field" id="selCampo">
                    <option value="todos">Todos os campos</option>
                    <option value="nome">Nome</option>
                    <option value="cpf">CPF</option>
                    <option value="cnh">CNH</option>
                    <option value="categoria">Categoria</option>
                    <option value="vinculo">Vínculo</option>
                </select>

                <div class="search-wrap">
                    <span class="material-symbols-outlined search-icon">search</span>
                    <input class="search-input" type="text"
                           placeholder="Buscar por nome, CPF, CNH, categoria ou vínculo"
                           id="txtFiltro">
                </div>

                <select class="select-field" id="selStatus">
                    <option value="">Todos</option>
                    <option value="ATIVO">Ativo</option>
                    <option value="INATIVO">Inativo</option>
                    <option value="SUSPENSO">Suspenso</option>
                </select>

                <button id="btnBuscar" class="btn-primary">Buscar</button>
            </div>
        </div>

        <div class="resultado">
            <div id="lblResultados">0 resultados encontrados</div>
            <table class="tabela-motoristas" id="tblMotoristas">
                <thead>
                <tr>
                    <th>Nome</th>
                    <th>CPF</th>
                    <th>Telefone</th>
                    <th>CNH</th>
                    <th>Categoria</th>
                    <th>Validade</th>
                    <th>Vínculo</th>
                    <th>Status</th>
                    <th class="acoes">Ações</th>
                </tr>
                </thead>
                <tbody>
                </tbody>
            </table>
        </div>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/consulta-motorista.js"></script>
</body>
</html>
