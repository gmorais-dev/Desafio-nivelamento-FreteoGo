<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="java.util.Collections" %>
<%@ page import="java.util.List" %>
<%@ page import="Entity.Cliente" %>
<%@ page import="Entity.Frete" %>
<%@ page import="Entity.Motorista" %>
<%@ page import="Entity.StatusFrete" %>
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

    private boolean selecionado(Object esperado, Object atual) {
        return valor(esperado).equals(valor(atual));
    }
%>
<%
    Frete frete = (Frete) request.getAttribute("frete");
    List<Cliente> clientes = (List<Cliente>) request.getAttribute("clientes");
    List<Motorista> motoristas = (List<Motorista>) request.getAttribute("motoristas");
    List<Veiculo> veiculos = (List<Veiculo>) request.getAttribute("veiculos");
    if (clientes == null) clientes = Collections.emptyList();
    if (motoristas == null) motoristas = Collections.emptyList();
    if (veiculos == null) veiculos = Collections.emptyList();

    String numeroFreteValor = frete != null ? frete.getNumero() : "";
    String dataEmissaoValor = frete != null && frete.getDataEmissao() != null ? valor(frete.getDataEmissao().toLocalDate()) : request.getParameter("dataEmissao");
    String dataPrevisaoValor = frete != null ? valor(frete.getDataPrevisaoEntrega()) : request.getParameter("dataPrevisaoEntrega");
    String remetenteValor = frete != null ? valor(frete.getRemetenteId()) : request.getParameter("remetenteId");
    String destinatarioValor = frete != null ? valor(frete.getDestinatarioId()) : request.getParameter("destinatarioId");
    String motoristaValor = frete != null ? valor(frete.getMotoristaId()) : request.getParameter("motoristaId");
    String veiculoValor = frete != null ? valor(frete.getVeiculoId()) : request.getParameter("veiculoId");
    String municipioOrigemValor = frete != null ? frete.getMunicipioOrigem() : request.getParameter("municipioOrigem");
    String ufOrigemValor = frete != null ? frete.getUfOrigem() : request.getParameter("ufOrigem");
    String municipioDestinoValor = frete != null ? frete.getMunicipioDestino() : request.getParameter("municipioDestino");
    String ufDestinoValor = frete != null ? frete.getUfDestino() : request.getParameter("ufDestino");
    String descricaoCargaValor = frete != null ? frete.getDescricaoCarga() : request.getParameter("descricaoCarga");
    String pesoKgValor = frete != null ? valor(frete.getPesoKg()) : request.getParameter("pesoKg");
    String volumesValor = frete != null ? valor(frete.getVolumes()) : request.getParameter("volumes");
    String valorFreteValor = frete != null ? valor(frete.getValorFrete()) : request.getParameter("valorFrete");
    String aliquotaIcmsValor = frete != null ? valor(frete.getAliquotaIcms()) : request.getParameter("aliquotaIcms");
    String valorIcmsValor = frete != null ? valor(frete.getValorIcms()) : request.getParameter("valorIcms");
    String valorTotalValor = frete != null ? valor(frete.getValorTotal()) : request.getParameter("valorTotal");
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Cadastro de Frete | FreteGo</title>

    <link rel="stylesheet"
          href="https://fonts.googleapis.com/css2?family=Material+Symbols+Outlined:opsz,wght,FILL,GRAD@20..48,100..700,0..1,-50..200"/>
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/menu.css">
    <link rel="stylesheet" href="${pageContext.request.contextPath}/css/form-frete.css">
</head>
<body>

<main class="frete-page">

    <header class="frete-header">
        <div class="header-left">
            <button type="button" class="btn-voltar" onclick="history.back()" title="Voltar">
                <span class="material-symbols-outlined">arrow_back</span>
            </button>

            <div class="header-icon">
                <span class="material-symbols-outlined">receipt_long</span>
            </div>

            <div class="header-text">
                <h1>Cadastro de Frete</h1>
                <p>Registre um novo contrato de transporte</p>
            </div>
        </div>

        <div class="frete-badge">
            <span>Nº do Frete</span>
            <strong>FRT-AAAA-NNNNN</strong>
        </div>
    </header>

    <div class="frete-content">

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
                <strong>Frete cadastrado com sucesso!</strong>
                <p>O contrato foi salvo e já está disponível para acompanhamento.</p>
            </div>
            <button type="button" class="alert-close" onclick="this.parentElement.remove()" title="Fechar">
                <span class="material-symbols-outlined">close</span>
            </button>
        </div>
        <% } %>

        <form id="formFrete" action="${pageContext.request.contextPath}/FreteController"
              method="post" autocomplete="off" novalidate>

            <input type="hidden" name="acao" value="cadastrar">
            <input type="hidden" id="status" name="status" value="<%= StatusFrete.EMITIDO.getCodigo() %>">

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">receipt_long</span>
                    <h2>Identificação</h2>
                </div>

                <div class="form-grid col-3">
                    <div class="form-group">
                        <label for="numeroFrete">Número do Frete <span class="obrigatorio">*</span></label>
                        <input type="text" id="numeroFrete"
                               placeholder="FRT-AAAA-NNNNN" value="<%= escaparHtml(numeroFreteValor) %>" readonly>
                        <span class="field-hint">Gerado automaticamente pelo BO</span>
                    </div>

                    <div class="form-group">
                        <label for="dataEmissao">Data de Emissão <span class="obrigatorio">*</span></label>
                        <input type="date" id="dataEmissao" name="dataEmissao"
                               value="<%= escaparHtml(dataEmissaoValor) %>">
                    </div>

                    <div class="form-group">
                        <label for="dataPrevisaoEntrega">Previsão de Entrega <span class="obrigatorio">*</span></label>
                        <input type="date" id="dataPrevisaoEntrega" name="dataPrevisaoEntrega"
                               value="<%= escaparHtml(dataPrevisaoValor) %>">
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">groups</span>
                    <h2>Partes Envolvidas</h2>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="remetenteId">Remetente <span class="obrigatorio">*</span></label>
                        <select id="remetenteId" name="remetenteId">
                            <option value="" disabled <%= valor(remetenteValor).isEmpty() ? "selected" : "" %>>Selecione um cliente</option>
                            <% for (Cliente cliente : clientes) {
                                boolean aceitaRemetente = "ATIVO".equals(cliente.getStatus()) && ("REMETENTE".equals(cliente.getTipo()) || "AMBOS".equals(cliente.getTipo()));
                                if (!aceitaRemetente) continue;
                            %>
                            <option value="<%= cliente.getId() %>" <%= selecionado(cliente.getId(), remetenteValor) ? "selected" : "" %>>
                                <%= escaparHtml(cliente.getRazaoSocial()) %> - <%= escaparHtml(cliente.getCnpj()) %>
                            </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="destinatarioId">Destinatário <span class="obrigatorio">*</span></label>
                        <select id="destinatarioId" name="destinatarioId">
                            <option value="" disabled <%= valor(destinatarioValor).isEmpty() ? "selected" : "" %>>Selecione um cliente</option>
                            <% for (Cliente cliente : clientes) {
                                boolean aceitaDestinatario = "ATIVO".equals(cliente.getStatus()) && ("DESTINATARIO".equals(cliente.getTipo()) || "AMBOS".equals(cliente.getTipo()));
                                if (!aceitaDestinatario) continue;
                            %>
                            <option value="<%= cliente.getId() %>" <%= selecionado(cliente.getId(), destinatarioValor) ? "selected" : "" %>>
                                <%= escaparHtml(cliente.getRazaoSocial()) %> - <%= escaparHtml(cliente.getCnpj()) %>
                            </option>
                            <% } %>
                        </select>
                    </div>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="motoristaId">Motorista <span class="obrigatorio">*</span></label>
                        <select id="motoristaId" name="motoristaId">
                            <option value="" disabled <%= valor(motoristaValor).isEmpty() ? "selected" : "" %>>Selecione um motorista</option>
                            <% for (Motorista motorista : motoristas) { %>
                            <option value="<%= motorista.getId() %>"
                                    data-status="<%= motorista.getStatus() == null ? "" : motorista.getStatus().getCodigo() %>"
                                    data-frete-status=""
                                    data-cnh-validade="<%= escaparHtml(motorista.getCnhValidade()) %>"
                                    <%= selecionado(motorista.getId(), motoristaValor) ? "selected" : "" %>>
                                <%= escaparHtml(motorista.getNome()) %> - CNH <%= escaparHtml(motorista.getCnhNumero()) %>
                            </option>
                            <% } %>
                        </select>
                    </div>

                    <div class="form-group">
                        <label for="veiculoId">Veículo <span class="obrigatorio">*</span></label>
                        <select id="veiculoId" name="veiculoId">
                            <option value="" disabled <%= valor(veiculoValor).isEmpty() ? "selected" : "" %>>Selecione um veículo</option>
                            <% for (Veiculo veiculo : veiculos) { %>
                            <option value="<%= veiculo.getId() %>"
                                    data-status="<%= escaparHtml(veiculo.getStatus()) %>"
                                    data-capacidade-kg="<%= veiculo.getCapacidadeKg() == null ? "" : veiculo.getCapacidadeKg() %>"
                                    <%= selecionado(veiculo.getId(), veiculoValor) ? "selected" : "" %>>
                                <%= escaparHtml(veiculo.getPlaca()) %> - <%= escaparHtml(veiculo.getTipo()) %> - <%= escaparHtml(veiculo.getStatus()) %>
                            </option>
                            <% } %>
                        </select>
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">pin_drop</span>
                    <h2>Rota</h2>
                </div>

                <div class="form-grid col-rota">
                    <div class="form-group">
                        <label for="municipioOrigem">Origem - Município <span class="obrigatorio">*</span></label>
                        <input type="text" id="municipioOrigem" name="municipioOrigem"
                               placeholder="Ex: São Paulo" maxlength="100"
                               value="<%= escaparHtml(municipioOrigemValor) %>">
                    </div>

                    <div class="form-group">
                        <label for="ufOrigem">UF <span class="obrigatorio">*</span></label>
                        <input type="text" id="ufOrigem" name="ufOrigem"
                               placeholder="UF" maxlength="2"
                               value="<%= escaparHtml(ufOrigemValor) %>">
                    </div>

                    <div class="form-group">
                        <label for="municipioDestino">Destino - Município <span class="obrigatorio">*</span></label>
                        <input type="text" id="municipioDestino" name="municipioDestino"
                               placeholder="Ex: Rio de Janeiro" maxlength="100"
                               value="<%= escaparHtml(municipioDestinoValor) %>">
                    </div>

                    <div class="form-group">
                        <label for="ufDestino">UF <span class="obrigatorio">*</span></label>
                        <input type="text" id="ufDestino" name="ufDestino"
                               placeholder="UF" maxlength="2"
                               value="<%= escaparHtml(ufDestinoValor) %>">
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">package_2</span>
                    <h2>Natureza da Carga</h2>
                </div>

                <div class="form-grid col-1">
                    <div class="form-group">
                        <label for="descricaoCarga">Descrição <span class="obrigatorio">*</span></label>
                        <textarea id="descricaoCarga" name="descricaoCarga"
                                  placeholder="Ex: Eletrodomésticos embalados em caixas de papelão"
                                  maxlength="500"><%= escaparHtml(descricaoCargaValor) %></textarea>
                    </div>
                </div>

                <div class="form-grid col-2">
                    <div class="form-group">
                        <label for="pesoKg">Peso Bruto (kg) <span class="obrigatorio">*</span></label>
                        <input type="text" id="pesoKg" name="pesoKg"
                               placeholder="12500" inputmode="decimal"
                               value="<%= escaparHtml(pesoKgValor) %>">
                    </div>

                    <div class="form-group">
                        <label for="volumes">Volumes <span class="obrigatorio">*</span></label>
                        <input type="text" id="volumes" name="volumes"
                               placeholder="120" inputmode="numeric"
                               value="<%= escaparHtml(volumesValor) %>">
                    </div>
                </div>
            </section>

            <section class="form-card">
                <div class="card-title">
                    <span class="material-symbols-outlined">attach_money</span>
                    <h2>Valores</h2>
                </div>

                <div class="form-grid col-3">
                    <div class="form-group">
                        <label for="valorFrete">Valor do Frete (R$) <span class="obrigatorio">*</span></label>
                        <input type="text" id="valorFrete" name="valorFrete"
                               placeholder="3500.00" inputmode="decimal"
                               value="<%= escaparHtml(valorFreteValor) %>">
                    </div>

                    <div class="form-group">
                        <label for="aliquotaIcms">ICMS - Alíquota (%) <span class="obrigatorio">*</span></label>
                        <input type="text" id="aliquotaIcms" name="aliquotaIcms"
                               placeholder="12" inputmode="decimal"
                               value="<%= escaparHtml(aliquotaIcmsValor) %>">
                    </div>

                    <div class="form-group">
                        <label for="valorIcms">ICMS - Valor</label>
                        <input type="text" id="valorIcms" name="valorIcms"
                               placeholder="R$ 0,00" readonly
                               value="<%= escaparHtml(valorIcmsValor) %>">
                    </div>
                </div>

                <div class="total-box">
                    <div class="total-label">
                        <span class="material-symbols-outlined">calendar_month</span>
                        <span>Valor Total do Frete</span>
                    </div>
                    <strong id="valorTotalTexto">R$ 0,00</strong>
                    <input type="hidden" id="valorTotal" name="valorTotal" value="<%= escaparHtml(valorTotalValor) %>">
                </div>
            </section>

            <div class="form-actions">
                <button type="button" class="btn-cancelar" onclick="history.back()">Cancelar</button>

                <button type="button" class="btn-limpar" id="btnLimpar">
                    <span class="material-symbols-outlined">close</span>
                    Limpar
                </button>

                <button type="submit" class="btn-salvar">
                    <span class="material-symbols-outlined">save</span>
                    Salvar Frete
                </button>
            </div>
        </form>
    </div>
</main>

<script src="${pageContext.request.contextPath}/js/form-frete.js"></script>
</body>
</html>
