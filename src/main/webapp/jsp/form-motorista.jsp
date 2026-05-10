<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Entity.Motorista" %>
<%@ page import="Entity.CategoriaCnh" %>
<%!
    private String valor(Object valor) {
        return valor == null ? "" : String.valueOf(valor);
    }

    private String escaparHtml(Object valor) {
        return valor(valor)
                .replace("&", "&amp;")
                .replace("\"", "&quot;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
%>
<%
    Motorista motorista = (Motorista) request.getAttribute("motorista");
    String modo = (String) request.getAttribute("modo");
    boolean modoVisualizacao = "visualizar".equals(modo);
    boolean modoEdicao = "editar".equals(modo);
    String campoSomenteLeitura = modoVisualizacao ? "readonly" : "";
    String campoDesabilitado = modoVisualizacao ? "disabled" : "";

    String nomeValor = motorista != null ? motorista.getNome() : request.getParameter("nome");
    String cpfValor = motorista != null ? motorista.getCpf() : request.getParameter("cpf");
    String dataNascimentoValor = motorista != null ? valor(motorista.getDataNascimento()) : request.getParameter("dataNascimento");
    String telefoneValor = motorista != null ? motorista.getTelefone() : request.getParameter("telefone");
    String cnhNumeroValor = motorista != null ? motorista.getCnhNumero() : request.getParameter("cnhNumero");
    String cnhCategoriaValor = motorista != null ? valor(motorista.getCnhCategoria()) : request.getParameter("cnhCategoria");
    String cnhValidadeValor = motorista != null ? valor(motorista.getCnhValidade()) : request.getParameter("cnhValidade");
    String tipoVinculoValor = motorista != null ? motorista.getTipoVinculo() : request.getParameter("tipoVinculo");
    String statusValor = motorista != null ? valor(motorista.getStatus()) : request.getParameter("status");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro de Motorista | FreteGo</title>

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form-motorista.css">
</head>
<body>

<main class="motorista-page">

    <header class="motorista-header">
        <button type="button" class="btn-voltar" onclick="history.back()" title="Voltar">
            <span class="material-symbols-outlined">arrow_back</span>
        </button>

        <div class="header-icon">
            <span class="material-symbols-outlined">person</span>
        </div>

        <div class="header-text">
            <h1><%= modoVisualizacao ? "Visualizar Motorista" : (modoEdicao ? "Editar Motorista" : "Cadastro de Motorista") %></h1>
            <p><%= modoVisualizacao ? "Dados cadastrados do motorista selecionado" : (modoEdicao ? "Atualize os dados cadastrados do motorista" : "Registre um novo motorista da equipe") %></p>
        </div>
    </header>

    <div class="motorista-content">

        <%
            String erroServidor = (String) request.getAttribute("erro");
            if (erroServidor != null) {
        %>
        <div class="alert alert-erro" id="alertaErro">
            <span class="material-symbols-outlined alert-icon">error</span>
            <div class="alert-body">
                <strong>Ops! Algo precisa ser corrigido.</strong>
                <p><%= erroServidor %></p>
            </div>
            <button type="button" class="alert-close" onclick="this.parentElement.remove()" title="Fechar">
                <span class="material-symbols-outlined">close</span>
            </button>
        </div>
        <% } %>

        <%
            String msgParam = request.getParameter("msg");
            if ("sucesso".equals(msgParam)) {
        %>
        <div class="alert alert-sucesso" id="alertaSucesso">
            <span class="material-symbols-outlined alert-icon">check_circle</span>
            <div class="alert-body">
                <strong>Motorista cadastrado com sucesso!</strong>
                <p>Os dados foram salvos e o motorista já está disponível no sistema.</p>
            </div>
            <button type="button" class="alert-close" onclick="this.parentElement.remove()" title="Fechar">
                <span class="material-symbols-outlined">close</span>
            </button>
        </div>
        <% } %>

        <form id="formMotorista" action="${pageContext.request.contextPath}/MotoristaController"
              method="post" autocomplete="off" novalidate>

            <input type="hidden" name="acao" value="<%= modoEdicao ? "editar" : "cadastrar" %>">
            <% if (motorista != null) { %>
            <input type="hidden" name="id" value="<%= motorista.getId() %>">
            <% } %>
            <input type="hidden" id="modoVisualizacao" value="<%= modoVisualizacao ? "true" : "false" %>">

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">info</span>
                    <h2>Dados Pessoais</h2>
                </div>

                <div class="form-grid col-dados-pessoais">
                    <div class="form-group">
                        <label for="nome">Nome Completo <span class="obrigatorio">*</span></label>
                        <input type="text" id="nome" name="nome"
                               placeholder="João da Silva" maxlength="200"
                               value="<%= escaparHtml(nomeValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="cpf">CPF <span class="obrigatorio">*</span></label>
                        <input type="text" id="cpf" name="cpf"
                               placeholder="000.000.000-00" maxlength="14"
                               value="<%= escaparHtml(cpfValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="dataNascimento">Data de Nascimento <span class="obrigatorio">*</span></label>
                        <input type="date" id="dataNascimento" name="dataNascimento"
                               value="<%= escaparHtml(dataNascimentoValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>

                <div class="form-grid col-telefone">
                    <div class="form-group">
                        <label for="telefone">Telefone <span class="obrigatorio">*</span></label>
                        <input type="text" id="telefone" name="telefone"
                               placeholder="(11) 98765-4321" maxlength="15"
                               value="<%= escaparHtml(telefoneValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">badge</span>
                    <h2>Carteira Nacional de Habilitação</h2>
                </div>

                <div class="form-grid col-cnh">
                    <div class="form-group">
                        <label for="cnhNumero">Número da CNH <span class="obrigatorio">*</span></label>
                        <input type="text" id="cnhNumero" name="cnhNumero"
                               placeholder="00000000000" maxlength="11" inputmode="numeric"
                               value="<%= escaparHtml(cnhNumeroValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="cnhCategoria">Categoria <span class="obrigatorio">*</span></label>
                        <select id="cnhCategoria" name="cnhCategoria" <%= campoDesabilitado %>>
                            <option value="" disabled <%= valor(cnhCategoriaValor).isEmpty() ? "selected" : "" %>>Selecione a categoria</option>
                            <% for (CategoriaCnh categoria : CategoriaCnh.values()) { %>
                            <option value="<%= categoria.getCodigo() %>" <%= categoria.getCodigo().equals(cnhCategoriaValor) ? "selected" : "" %>><%= categoria.getCodigo() %></option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="cnhValidade">Validade <span class="obrigatorio">*</span></label>
                        <input type="date" id="cnhValidade" name="cnhValidade"
                               value="<%= escaparHtml(cnhValidadeValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">work</span>
                    <h2>Vínculo</h2>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="tipoVinculo">Tipo de Vínculo <span class="obrigatorio">*</span></label>
                        <select id="tipoVinculo" name="tipoVinculo" <%= campoDesabilitado %>>
                            <option value="" disabled <%= valor(tipoVinculoValor).isEmpty() ? "selected" : "" %>>Selecione o vínculo</option>
                            <option value="FUNCIONARIO" <%= "FUNCIONARIO".equals(tipoVinculoValor) || "CLT".equals(tipoVinculoValor) ? "selected" : "" %>>Funcionário</option>
                            <option value="AGREGADO" <%= "AGREGADO".equals(tipoVinculoValor) ? "selected" : "" %>>Agregado</option>
                            <option value="TERCEIRO" <%= "TERCEIRO".equals(tipoVinculoValor) ? "selected" : "" %>>Terceiro</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="status">Status <span class="obrigatorio">*</span></label>
                        <select id="status" name="status" <%= campoDesabilitado %>>
                            <option value="" disabled <%= valor(statusValor).isEmpty() ? "selected" : "" %>>Selecione o status</option>
                            <option value="ATIVO" <%= "ATIVO".equals(statusValor) ? "selected" : "" %>>Ativo</option>
                            <option value="INATIVO" <%= "INATIVO".equals(statusValor) ? "selected" : "" %>>Inativo</option>
                            <option value="SUSPENSO" <%= "SUSPENSO".equals(statusValor) || "AFASTADO".equals(statusValor) ? "selected" : "" %>>Suspenso</option>
                        </select>
                    </div>
                </div>
            </section>

            <div class="form-actions">
                <button type="button" class="btn-cancelar" onclick="history.back()"><%= modoVisualizacao ? "Voltar" : "Cancelar" %></button>

                <% if (!modoVisualizacao) { %>
                <button type="button" class="btn-limpar" id="btnLimpar">
                    <span class="material-symbols-outlined">close</span>
                    Limpar
                </button>

                <button type="submit" class="btn-salvar">
                    <span class="material-symbols-outlined">save</span>
                    <%= modoEdicao ? "Salvar Alterações" : "Salvar Motorista" %>
                </button>
                <% } %>
            </div>
        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/form-motorista.js"></script>
</body>
</html>
