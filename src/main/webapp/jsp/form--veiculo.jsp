<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Entity.Veiculo" %>
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
    Veiculo veiculo = (Veiculo) request.getAttribute("veiculo");
    String modo = (String) request.getAttribute("modo");
    boolean modoVisualizacao = "visualizar".equals(modo);
    boolean modoEdicao = "editar".equals(modo);
    String campoSomenteLeitura = modoVisualizacao ? "readonly" : "";
    String campoDesabilitado = modoVisualizacao ? "disabled" : "";

    String placaValor = veiculo != null ? veiculo.getPlaca() : request.getParameter("placa");
    String rntrcValor = veiculo != null ? veiculo.getRntrc() : request.getParameter("rntrc");
    String anoFabricacaoValor = veiculo != null ? valor(veiculo.getAnoFabricacao()) : request.getParameter("anoFabricacao");
    String tipoValor = veiculo != null ? veiculo.getTipo() : request.getParameter("tipo");
    String statusValor = veiculo != null ? veiculo.getStatus() : request.getParameter("status");
    String taraKgValor = veiculo != null ? valor(veiculo.getTaraKg()) : request.getParameter("taraKg");
    String capacidadeKgValor = veiculo != null ? valor(veiculo.getCapacidadeKg()) : request.getParameter("capacidadeKg");
    String volumeM3Valor = veiculo != null ? valor(veiculo.getVolumeM3()) : request.getParameter("volumeM3");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro de Veículo | FreteGo</title>

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form-veiculo.css">
</head>
<body>

<main class="veiculo-page">

    <header class="veiculo-header">
        <button type="button" class="btn-voltar" onclick="history.back()" title="Voltar">
            <span class="material-symbols-outlined">arrow_back</span>
        </button>

        <div class="header-icon">
            <span class="material-symbols-outlined">local_shipping</span>
        </div>

        <div class="header-text">
            <h1><%= modoVisualizacao ? "Visualizar Veículo" : (modoEdicao ? "Editar Veículo" : "Cadastro de Veículo") %></h1>
            <p><%= modoVisualizacao ? "Dados cadastrados do veículo selecionado" : (modoEdicao ? "Atualize os dados cadastrados do veículo" : "Registre um novo veículo da frota") %></p>
        </div>
    </header>

    <div class="veiculo-content">

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
                <strong>Veículo cadastrado com sucesso!</strong>
                <p>Os dados foram salvos e o veículo já está disponível no sistema.</p>
            </div>
            <button type="button" class="alert-close" onclick="this.parentElement.remove()" title="Fechar">
                <span class="material-symbols-outlined">close</span>
            </button>
        </div>
        <% } %>

        <form id="formVeiculo" action="${pageContext.request.contextPath}/VeiculoController"
              method="post" autocomplete="off" novalidate>

            <input type="hidden" name="acao" value="<%= modoEdicao ? "editar" : "cadastrar" %>">
            <% if (veiculo != null) { %>
            <input type="hidden" name="id" value="<%= veiculo.getId() %>">
            <% } %>
            <input type="hidden" id="modoVisualizacao" value="<%= modoVisualizacao ? "true" : "false" %>">

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">info</span>
                    <h2>Identificação</h2>
                </div>

                <div class="form-grid col-3">
                    <div class="form-group">
                        <label for="placa">Placa <span class="obrigatorio">*</span></label>
                        <input type="text" id="placa" name="placa"
                               placeholder="ABC1D23  OU  ABC-1234" maxlength="8"
                               value="<%= escaparHtml(placaValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="rntrc">RNTRC <span class="obrigatorio">*</span></label>
                        <input type="text" id="rntrc" name="rntrc"
                               placeholder="00000000" maxlength="8" inputmode="numeric"
                               value="<%= escaparHtml(rntrcValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="anoFabricacao">Ano de Fabricação <span class="obrigatorio">*</span></label>
                        <input type="text" id="anoFabricacao" name="anoFabricacao"
                               placeholder="2024" maxlength="4" inputmode="numeric"
                               value="<%= escaparHtml(anoFabricacaoValor) %>" <%= campoSomenteLeitura %>>
                    </div>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="tipo">Tipo <span class="obrigatorio">*</span></label>
                        <select id="tipo" name="tipo" <%= campoDesabilitado %>>
                            <option value="" disabled <%= valor(tipoValor).isEmpty() ? "selected" : "" %>>Selecione o tipo</option>
                            <option value="VAN" <%= "VAN".equals(tipoValor) ? "selected" : "" %>>Van</option>
                            <option value="TRUCK" <%= "TRUCK".equals(tipoValor) ? "selected" : "" %>>Truck</option>
                            <option value="CARRETA" <%= "CARRETA".equals(tipoValor) ? "selected" : "" %>>Carreta</option>
                            <option value="UTILITARIO" <%= "UTILITARIO".equals(tipoValor) ? "selected" : "" %>>Utilitário</option>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="status">Status <span class="obrigatorio">*</span></label>
                        <select id="status" name="status" <%= campoDesabilitado %>>
                            <option value="" disabled <%= valor(statusValor).isEmpty() ? "selected" : "" %>>Selecione o status</option>
                            <option value="DISPONIVEL" <%= "DISPONIVEL".equals(statusValor) ? "selected" : "" %>>Disponível</option>
                            <option value="EM_VIAGEM" <%= "EM_VIAGEM".equals(statusValor) ? "selected" : "" %>>Em viagem</option>
                            <option value="MANUTENCAO" <%= "MANUTENCAO".equals(statusValor) ? "selected" : "" %>>Manutenção</option>
                        </select>
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">speed</span>
                    <h2>Capacidades</h2>
                </div>

                <div class="form-grid col-3">
                    <div class="form-group">
                        <label for="taraKg">Tara (kg) <span class="obrigatorio">*</span></label>
                        <input type="text" id="taraKg" name="taraKg"
                               placeholder="8500" inputmode="decimal"
                               value="<%= escaparHtml(taraKgValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="capacidadeKg">Capacidade de Carga (kg) <span class="obrigatorio">*</span></label>
                        <input type="text" id="capacidadeKg" name="capacidadeKg"
                               placeholder="15000" inputmode="decimal"
                               value="<%= escaparHtml(capacidadeKgValor) %>" <%= campoSomenteLeitura %>>
                    </div>

                    <div class="form-group">
                        <label for="volumeM3">Volume (m³) <span class="obrigatorio">*</span></label>
                        <input type="text" id="volumeM3" name="volumeM3"
                               placeholder="42.5" inputmode="decimal"
                               value="<%= escaparHtml(volumeM3Valor) %>" <%= campoSomenteLeitura %>>
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
                    <%= modoEdicao ? "Salvar Alterações" : "Salvar Veículo" %>
                </button>
                <% } %>
            </div>
        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/form-veiculo.js"></script>
</body>
</html>
