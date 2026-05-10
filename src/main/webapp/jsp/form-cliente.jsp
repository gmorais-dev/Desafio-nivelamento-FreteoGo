<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Entity.Cliente" %>
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
    Cliente cliente = (Cliente) request.getAttribute("cliente");
    String modo = (String) request.getAttribute("modo");
    boolean modoVisualizacao = "visualizar".equals(modo);
    boolean modoEdicao = "editar".equals(modo);
    String campoSomenteLeitura = modoVisualizacao ? "readonly" : "";
    String campoDesabilitado = modoVisualizacao ? "disabled" : "";

    String razaoSocialValor = cliente != null ? cliente.getRazaoSocial() : request.getParameter("razaoSocial");
    String nomeFantasiaValor = cliente != null ? cliente.getNomeFantasia() : request.getParameter("nomeFantasia");
    String cnpjValor = cliente != null ? cliente.getCnpj() : request.getParameter("cnpj");
    String inscricaoEstadualValor = cliente != null ? cliente.getInscricaoEstadual() : request.getParameter("inscricaoEstadual");
    String tipoValor = cliente != null ? cliente.getTipo() : request.getParameter("tipo");
    String logradouroValor = cliente != null ? cliente.getLogradouro() : request.getParameter("logradouro");
    String numeroValor = cliente != null ? valor(cliente.getNumero()) : request.getParameter("numero");
    String complementoValor = cliente != null ? cliente.getComplemento() : request.getParameter("complemento");
    String bairroValor = cliente != null ? cliente.getBairro() : request.getParameter("bairro");
    String municipioValor = cliente != null ? cliente.getMunicipio() : request.getParameter("municipio");
    String ufValor = cliente != null ? cliente.getUf() : request.getParameter("uf");
    String cepValor = cliente != null ? cliente.getCep() : request.getParameter("cep");
    String telefoneValor = cliente != null ? cliente.getTelefone() : request.getParameter("telefone");
    String emailValor = cliente != null ? cliente.getEmail() : request.getParameter("email");
    String statusValor = cliente != null ? cliente.getStatus() : request.getParameter("status");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro de Cliente | FreteGo</title>

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form-cliente.css">
</head>
<body>

<main class="cliente-page">

    <header class="cliente-header">
        <button type="button" class="btn-voltar" onclick="history.back()" title="Voltar">
            <span class="material-symbols-outlined">arrow_back</span>
        </button>

        <div class="header-icon">
            <span class="material-symbols-outlined">apartment</span>
        </div>

        <div class="header-text">
            <h1><%= modoVisualizacao ? "Visualizar Cliente" : (modoEdicao ? "Editar Cliente" : "Cadastro de Cliente") %></h1>
            <p><%= modoVisualizacao ? "Dados cadastrados do cliente selecionado" : (modoEdicao ? "Atualize os dados cadastrados do cliente" : "Registre uma nova empresa para operações de frete") %></p>
        </div>
    </header>

    <div class="cliente-content">

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
                <strong>Cliente cadastrado com sucesso!</strong>
                <p>Os dados foram salvos e o cadastro já está disponível no sistema.</p>
            </div>
            <button type="button" class="alert-close" onclick="this.parentElement.remove()" title="Fechar">
                <span class="material-symbols-outlined">close</span>
            </button>
        </div>
        <% } %>

        <form id="formCliente" action="${pageContext.request.contextPath}/ClienteController"
              method="post" autocomplete="off" novalidate>

            <input type="hidden" name="acao" value="<%= modoEdicao ? "editar" : "cadastrar" %>">
            <% if (cliente != null) { %>
            <input type="hidden" name="id" value="<%= cliente.getId() %>">
            <% } %>
            <input type="hidden" id="modoVisualizacao" value="<%= modoVisualizacao ? "true" : "false" %>">

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">apartment</span>
                    <h2>Dados da empresa</h2>
                </div>

                <div class="form-grid col-1">
                    <div class="form-group">
                        <label for="razaoSocial">Razão Social <span class="obrigatorio">*</span></label>
                        <input type="text" id="razaoSocial" name="razaoSocial"
                               placeholder="Transportes Silva LTDA" maxlength="200"
                               value="<%= escaparHtml(razaoSocialValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="nomeFantasia">Nome Fantasia <span class="obrigatorio">*</span></label>
                        <input type="text" id="nomeFantasia" name="nomeFantasia"
                               placeholder="Silva Fretes" maxlength="200"
                               value="<%= escaparHtml(nomeFantasiaValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="cnpj">CNPJ <span class="obrigatorio">*</span></label>
                        <input type="text" id="cnpj" name="cnpj"
                               placeholder="00.000.000/0000-00" maxlength="18"
                               value="<%= escaparHtml(cnpjValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="inscricaoEstadual">Inscrição Estadual</label>
                        <input type="text" id="inscricaoEstadual" name="inscricaoEstadual"
                               placeholder="Isento ou número da IE" maxlength="20"
                               value="<%= escaparHtml(inscricaoEstadualValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="tipo">Tipo <span class="obrigatorio">*</span></label>
                        <select id="tipo" name="tipo" <%= campoDesabilitado %>>
                            <option value="" disabled <%= valor(tipoValor).isEmpty() ? "selected" : "" %>>Selecione o tipo</option>
                            <option value="REMETENTE" <%= "REMETENTE".equals(tipoValor) ? "selected" : "" %>>Remetente</option>
                            <option value="DESTINATARIO" <%= "DESTINATARIO".equals(tipoValor) ? "selected" : "" %>>Destinatário</option>
                            <option value="AMBOS" <%= "AMBOS".equals(tipoValor) ? "selected" : "" %>>Ambos</option>
                        </select>
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">location_on</span>
                    <h2>Endereço</h2>
                </div>

                <div class="form-grid col-2-1">
                    <div class="form-group">
                        <label for="logradouro">Logradouro <span class="obrigatorio">*</span></label>
                        <input type="text" id="logradouro" name="logradouro"
                               placeholder="Av. Brasil" maxlength="200"
                               value="<%= escaparHtml(logradouroValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="numero">Número <span class="obrigatorio">*</span></label>
                        <input type="number" id="numero" name="numero"
                               placeholder="1000" min="1"
                               value="<%= escaparHtml(numeroValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="complemento">Complemento</label>
                        <input type="text" id="complemento" name="complemento"
                               placeholder="Sala 12, Bloco B" maxlength="100"
                               value="<%= escaparHtml(complementoValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="bairro">Bairro <span class="obrigatorio">*</span></label>
                        <input type="text" id="bairro" name="bairro"
                               placeholder="Centro" maxlength="100"
                               value="<%= escaparHtml(bairroValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>

                <div class="form-grid col-num">
                    <div class="form-group">
                        <label for="municipio">Município <span class="obrigatorio">*</span></label>
                        <input type="text" id="municipio" name="municipio"
                               placeholder="São Paulo" maxlength="100"
                               value="<%= escaparHtml(municipioValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="uf">UF <span class="obrigatorio">*</span></label>
                        <input type="text" id="uf" name="uf"
                               placeholder="SP" maxlength="2"
                               value="<%= escaparHtml(ufValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="cep">CEP <span class="obrigatorio">*</span></label>
                        <input type="text" id="cep" name="cep"
                               placeholder="00000-000" maxlength="10"
                               value="<%= escaparHtml(cepValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">call</span>
                    <h2>Contato</h2>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="telefone">Telefone <span class="obrigatorio">*</span></label>
                        <input type="text" id="telefone" name="telefone"
                               placeholder="(11) 99999-0000" maxlength="20"
                               value="<%= escaparHtml(telefoneValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="email">E-mail <span class="obrigatorio">*</span></label>
                        <input type="email" id="email" name="email"
                               placeholder="contato@empresa.com" maxlength="150"
                               value="<%= escaparHtml(emailValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="status">Status <span class="obrigatorio">*</span></label>
                        <select id="status" name="status" <%= campoDesabilitado %>>
                            <option value="ATIVO" <%= "ATIVO".equals(statusValor) || valor(statusValor).isEmpty() ? "selected" : "" %>>Ativo</option>
                            <option value="INATIVO" <%= "INATIVO".equals(statusValor) ? "selected" : "" %>>Inativo</option>
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
                    <%= modoEdicao ? "Salvar Alterações" : "Salvar Cliente" %>
                </button>
                <% } %>
            </div>
        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/form-cliente.js"></script>
</body>
</html>
