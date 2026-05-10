<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="Entity.Frete" %>
<%@ page import="Entity.OcorrenciaFrete" %>
<%@ page import="Entity.StatusFrete" %>
<%@ page import="Entity.TipoOcorrenciaFrete" %>
<%@ page import="java.time.LocalDateTime" %>
<%@ page import="java.time.format.DateTimeFormatter" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
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

    private String formatarDataHora(LocalDateTime valor) {
        if (valor == null) {
            return "";
        }
        return valor.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
    }

    private String formatarDataHoraInput(LocalDateTime valor) {
        if (valor == null) {
            return "";
        }
        return valor.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
    }
%>
<%
    Frete frete = (Frete) request.getAttribute("frete");
    OcorrenciaFrete ocorrenciaForm = (OcorrenciaFrete) request.getAttribute("ocorrenciaForm");
    List<OcorrenciaFrete> ocorrencias = (List<OcorrenciaFrete>) request.getAttribute("ocorrencias");
    TipoOcorrenciaFrete[] tiposOcorrencia = (TipoOcorrenciaFrete[]) request.getAttribute("tiposOcorrencia");
    if (ocorrencias == null) ocorrencias = Collections.emptyList();
    if (tiposOcorrencia == null) tiposOcorrencia = new TipoOcorrenciaFrete[0];

    boolean freteFinalizado = frete != null && (frete.getStatus() == StatusFrete.ENTREGUE
            || frete.getStatus() == StatusFrete.NAO_ENTREGUE
            || frete.getStatus() == StatusFrete.CANCELADO);

    String tipoSelecionado = ocorrenciaForm != null && ocorrenciaForm.getTipo() != null
            ? ocorrenciaForm.getTipo().getCodigo() : "";
    String dataHoraOcorrenciaValor = ocorrenciaForm != null ? formatarDataHoraInput(ocorrenciaForm.getDataHora()) : "";
    String municipioOcorrenciaValor = ocorrenciaForm != null ? ocorrenciaForm.getMunicipio() : "";
    String ufOcorrenciaValor = ocorrenciaForm != null ? ocorrenciaForm.getUf() : "";
    String descricaoOcorrenciaValor = ocorrenciaForm != null ? ocorrenciaForm.getDescricao() : "";
    String nomeRecebedorValor = ocorrenciaForm != null ? ocorrenciaForm.getNomeRecebedor() : "";
    String documentoRecebedorValor = ocorrenciaForm != null ? ocorrenciaForm.getDocumentoRecebedor() : "";
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Detalhes do Frete | FreteGo</title>

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/detalhe-frete.css">
</head>
<body>

<main class="detalhe-page">
    <header class="detalhe-header">
        <div class="header-left">
            <button type="button" class="btn-voltar" onclick="history.back()" title="Voltar">
                <span class="material-symbols-outlined">arrow_back</span>
            </button>

            <a href="${pageContext.request.contextPath}/jsp/menu.jsp" class="detalhe-brand" title="Voltar ao menu">
                <div class="header-icon">
                    <span class="material-symbols-outlined">local_shipping</span>
                </div>

                <div class="header-text">
                    <h1>Detalhes do Frete</h1>
                    <p>Histórico cronológico e registro operacional da carga</p>
                </div>
            </a>
        </div>

        <div class="frete-badge">
            <span>Status atual</span>
            <strong><%= frete == null || frete.getStatus() == null ? "" : escaparHtml(frete.getStatus().getCodigo()) %></strong>
        </div>
    </header>

    <div class="detalhe-content">
        <%
            String erroServidor = (String) request.getAttribute("erro");
            if (erroServidor != null) {
        %>
        <div class="alert alert-erro">
            <span class="material-symbols-outlined alert-icon">error</span>
            <div class="alert-body">
                <strong>Não foi possível concluir a operação.</strong>
                <p><%= erroServidor %></p>
            </div>
        </div>
        <% } %>

        <%
            String msgParam = request.getParameter("msg");
            if ("frete_cadastrado".equals(msgParam) || "ocorrencia_registrada".equals(msgParam)
                    || "entrega_registrada".equals(msgParam) || "status_atualizado".equals(msgParam)) {
        %>
        <div class="alert alert-sucesso">
            <span class="material-symbols-outlined alert-icon">check_circle</span>
            <div class="alert-body">
                <strong>Atualização concluída.</strong>
                <p>
                    <%= "frete_cadastrado".equals(msgParam) ? "O frete foi cadastrado e já está pronto para acompanhamento."
                            : ("entrega_registrada".equals(msgParam) ? "A entrega foi concluída com a ocorrência registrada na mesma transação."
                            : ("ocorrencia_registrada".equals(msgParam) ? "A ocorrência foi adicionada ao histórico do frete."
                            : "O status do frete foi atualizado com sucesso.")) %>
                </p>
            </div>
        </div>
        <% } %>

        <section class="card resumo-card">
            <div class="card-title">
                <span class="material-symbols-outlined">receipt_long</span>
                <h2>Resumo do Frete</h2>
            </div>

            <div class="resumo-grid">
                <div class="resumo-item">
                    <span>Número</span>
                    <strong><%= frete == null ? "" : escaparHtml(frete.getNumero()) %></strong>
                </div>
                <div class="resumo-item">
                    <span>Remetente</span>
                    <strong><%= frete == null ? "" : escaparHtml(frete.getRemetenteNome()) %></strong>
                </div>
                <div class="resumo-item">
                    <span>Destinatário</span>
                    <strong><%= frete == null ? "" : escaparHtml(frete.getDestinatarioNome()) %></strong>
                </div>
                <div class="resumo-item">
                    <span>Motorista</span>
                    <strong><%= frete == null ? "" : escaparHtml(frete.getMotoristaNome()) %></strong>
                </div>
                <div class="resumo-item">
                    <span>Veículo</span>
                    <strong><%= frete == null ? "" : escaparHtml(frete.getVeiculoPlaca()) %></strong>
                </div>
                <div class="resumo-item">
                    <span>Rota</span>
                    <strong><%= frete == null ? "" : escaparHtml(frete.getMunicipioOrigem()) %>/<%= frete == null ? "" : escaparHtml(frete.getUfOrigem()) %> → <%= frete == null ? "" : escaparHtml(frete.getMunicipioDestino()) %>/<%= frete == null ? "" : escaparHtml(frete.getUfDestino()) %></strong>
                </div>
            </div>

            <div class="acoes-status">
                <% if (frete != null && frete.getStatus() == StatusFrete.EMITIDO) { %>
                <form method="post" action="${pageContext.request.contextPath}/FreteController">
                    <input type="hidden" name="acao" value="confirmarSaida">
                    <input type="hidden" name="id" value="<%= frete.getId() %>">
                    <input type="hidden" name="origem" value="detalhe">
                    <button type="submit" class="btn-acao">
                        <span class="material-symbols-outlined">exit_to_app</span>
                        Confirmar saída
                    </button>
                </form>
                <% } %>

                <% if (frete != null && frete.getStatus() == StatusFrete.SAIDA_CONFIRMADA) { %>
                <form method="post" action="${pageContext.request.contextPath}/FreteController">
                    <input type="hidden" name="acao" value="iniciarTransito">
                    <input type="hidden" name="id" value="<%= frete.getId() %>">
                    <input type="hidden" name="origem" value="detalhe">
                    <button type="submit" class="btn-acao">
                        <span class="material-symbols-outlined">route</span>
                        Iniciar trânsito
                    </button>
                </form>
                <% } %>

                <% if (frete != null && frete.getStatus() == StatusFrete.EM_TRANSITO) { %>
                <form method="post" action="${pageContext.request.contextPath}/FreteController">
                    <input type="hidden" name="acao" value="naoEntregar">
                    <input type="hidden" name="id" value="<%= frete.getId() %>">
                    <input type="hidden" name="origem" value="detalhe">
                    <button type="submit" class="btn-acao btn-secundaria">
                        <span class="material-symbols-outlined">warning</span>
                        Marcar não entregue
                    </button>
                </form>
                <% } %>
            </div>
        </section>

        <div class="detalhe-grid">
            <section class="card">
                <div class="card-title">
                    <span class="material-symbols-outlined">history</span>
                    <h2>Ocorrências da Entrega</h2>
                </div>

                <div class="timeline">
                    <% if (ocorrencias.isEmpty()) { %>
                    <div class="timeline-vazio">
                        Nenhuma ocorrência registrada para este frete até o momento.
                    </div>
                    <% } %>

                    <% for (OcorrenciaFrete ocorrencia : ocorrencias) { %>
                    <article class="timeline-item">
                        <div class="timeline-ponto"></div>
                        <div class="timeline-card">
                            <div class="timeline-topo">
                                <strong><%= escaparHtml(ocorrencia.getTipo().getDescricao()) %></strong>
                                <span><%= formatarDataHora(ocorrencia.getDataHora()) %></span>
                            </div>
                            <div class="timeline-local">
                                <span class="material-symbols-outlined">location_on</span>
                                <span><%= escaparHtml(ocorrencia.getMunicipio()) %>/<%= escaparHtml(ocorrencia.getUf()) %></span>
                            </div>
                            <% if (valor(ocorrencia.getDescricao()).length() > 0) { %>
                            <p class="timeline-texto"><%= escaparHtml(ocorrencia.getDescricao()) %></p>
                            <% } %>
                            <% if (valor(ocorrencia.getNomeRecebedor()).length() > 0 || valor(ocorrencia.getDocumentoRecebedor()).length() > 0) { %>
                            <div class="timeline-recebedor">
                                Recebedor: <strong><%= escaparHtml(ocorrencia.getNomeRecebedor()) %></strong>
                                (<%= escaparHtml(ocorrencia.getDocumentoRecebedor()) %>)
                            </div>
                            <% } %>
                        </div>
                    </article>
                    <% } %>
                </div>
            </section>

            <section class="card">
                <div class="card-title">
                    <span class="material-symbols-outlined">playlist_add</span>
                    <h2>Nova Ocorrência</h2>
                </div>

                <% if (freteFinalizado) { %>
                <div class="form-bloqueado">
                    Este frete já está finalizado. O histórico continua visível, mas não aceita novas ocorrências.
                </div>
                <% } else { %>
                <form id="formOcorrencia" method="post" action="${pageContext.request.contextPath}/FreteController" novalidate>
                    <input type="hidden" name="acao" value="registrarOcorrencia">
                    <input type="hidden" name="freteId" value="<%= frete == null ? "" : frete.getId() %>">

                    <div class="form-group">
                        <label for="tipoOcorrencia">Tipo de ocorrência <span class="obrigatorio">*</span></label>
                        <select id="tipoOcorrencia" name="tipoOcorrencia">
                            <option value="" disabled <%= tipoSelecionado.isEmpty() ? "selected" : "" %>>Selecione</option>
                            <% for (TipoOcorrenciaFrete tipo : tiposOcorrencia) { %>
                            <option value="<%= tipo.getCodigo() %>" <%= tipo.getCodigo().equals(tipoSelecionado) ? "selected" : "" %>>
                                <%= escaparHtml(tipo.getDescricao()) %>
                            </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-grid col-2">
                        <div class="form-group">
                            <label for="dataHoraOcorrencia">Data e hora <span class="obrigatorio">*</span></label>
                            <input type="datetime-local" id="dataHoraOcorrencia" name="dataHoraOcorrencia"
                                   value="<%= escaparHtml(dataHoraOcorrenciaValor) %>">
                        </div>
                        <div class="form-group">
                            <label for="municipioOcorrencia">Município <span class="obrigatorio">*</span></label>
                            <input type="text" id="municipioOcorrencia" name="municipioOcorrencia" maxlength="100"
                                   value="<%= escaparHtml(municipioOcorrenciaValor) %>">
                        </div>
                    </div>

                    <div class="form-grid col-2">
                        <div class="form-group">
                            <label for="ufOcorrencia">UF <span class="obrigatorio">*</span></label>
                            <input type="text" id="ufOcorrencia" name="ufOcorrencia" maxlength="2"
                                   value="<%= escaparHtml(ufOcorrenciaValor) %>">
                        </div>
                    </div>

                    <div class="form-group" id="grupoDescricao">
                        <label for="descricaoOcorrencia">Descrição <span class="obrigatorio" id="obrigatorioDescricao" hidden>*</span></label>
                        <textarea id="descricaoOcorrencia" name="descricaoOcorrencia" maxlength="500"
                                  placeholder="Detalhe o que ocorreu no trajeto."><%= escaparHtml(descricaoOcorrenciaValor) %></textarea>
                        <span class="field-hint">Obrigatória para Avaria, Extravio e Outros.</span>
                    </div>

                    <div class="form-grid col-2 grupo-recebedor" id="grupoRecebedor">
                        <div class="form-group">
                            <label for="nomeRecebedor">Nome do recebedor <span class="obrigatorio" id="obrigatorioRecebedorNome" hidden>*</span></label>
                            <input type="text" id="nomeRecebedor" name="nomeRecebedor" maxlength="200"
                                   value="<%= escaparHtml(nomeRecebedorValor) %>">
                        </div>
                        <div class="form-group">
                            <label for="documentoRecebedor">Documento do recebedor <span class="obrigatorio" id="obrigatorioRecebedorDoc" hidden>*</span></label>
                            <input type="text" id="documentoRecebedor" name="documentoRecebedor" maxlength="30"
                                   value="<%= escaparHtml(documentoRecebedorValor) %>">
                        </div>
                    </div>

                    <div class="form-actions">
                        <button type="submit" class="btn-registrar">
                            <span class="material-symbols-outlined">save</span>
                            Registrar ocorrência
                        </button>
                    </div>
                </form>
                <% } %>
            </section>
        </div>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/detalhe-frete.js"></script>
</body>
</html>
