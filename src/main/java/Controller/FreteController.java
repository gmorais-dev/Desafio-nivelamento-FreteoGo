package Controller;

import BO.ClienteBO;
import BO.FreteBO;
import BO.MotoristaBO;
import BO.OcorrenciaFreteBO;
import BO.VeiculoBO;
import Entity.Frete;
import Entity.OcorrenciaFrete;
import Entity.StatusFrete;
import Entity.TipoOcorrenciaFrete;
import Exception.NegocioException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;

public class FreteController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final FreteBO freteBO = new FreteBO();
    private final ClienteBO clienteBO = new ClienteBO();
    private final MotoristaBO motoristaBO = new MotoristaBO();
    private final VeiculoBO veiculoBO = new VeiculoBO();
    private final OcorrenciaFreteBO ocorrenciaFreteBO = new OcorrenciaFreteBO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String acao = request.getParameter("acao");
        if ("buscar".equals(acao)) {
            buscar(request, response);
            return;
        }
        if ("obter".equals(acao)) {
            obter(request, response);
            return;
        }
        if ("detalhar".equals(acao)) {
            detalhar(request, response);
            return;
        }

        carregarCombos(request);
        request.getRequestDispatcher("/jsp/form-frete.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String acao = request.getParameter("acao");
        if ("cadastrar".equals(acao)) {
            cadastrar(request, response);
            return;
        }
        if ("registrarOcorrencia".equals(acao)) {
            registrarOcorrencia(request, response);
            return;
        }
        if ("confirmarSaida".equals(acao) || "iniciarTransito".equals(acao) || "entregar".equals(acao)
                || "naoEntregar".equals(acao) || "cancelar".equals(acao)) {
            alterarStatus(request, response, acao);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp");
    }

    private void cadastrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Frete frete = montarFrete(request);
        try {
            int idFrete = freteBO.cadastrar(frete);
            response.sendRedirect(request.getContextPath() + "/FreteController?acao=detalhar&id=" + idFrete + "&msg=frete_cadastrado");
        } catch (NegocioException e) {
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("frete", frete);
            carregarCombos(request);
            request.getRequestDispatcher("/jsp/form-frete.jsp").forward(request, response);
        }
    }

    private void buscar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            List<Frete> lista = freteBO.listar(request.getParameter("filtro"));
            response.setContentType("application/json;charset=UTF-8");
            StringBuilder json = new StringBuilder("[");
            boolean primeira = true;
            for (Frete f : lista) {
                if (!primeira) json.append(',');
                primeira = false;
                json.append(jsonFrete(f));
            }
            json.append(']');
            response.getWriter().print(json.toString());
        } catch (Exception e) {
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"mensagem\":\"Erro ao buscar fretes.\"}");
        }
    }

    private void obter(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            Frete frete = freteBO.obterPorId(parseInteger(request.getParameter("id")));
            if (frete == null) {
                response.setStatus(404);
                response.getWriter().print("{\"mensagem\":\"Frete não encontrado.\"}");
                return;
            }
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print(jsonFrete(frete));
        } catch (Exception e) {
            response.setStatus(500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"mensagem\":\"Erro ao recuperar frete.\"}");
        }
    }

    private void detalhar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer id = parseInteger(request.getParameter("id"));
        if (id == null || id <= 0) {
            response.sendRedirect(request.getContextPath() + "/jsp/dashboard-fretes.jsp");
            return;
        }

        try {
            carregarDetalheFrete(request, id, (OcorrenciaFrete) request.getAttribute("ocorrenciaForm"));
            request.getRequestDispatcher("/jsp/detalhe-frete.jsp").forward(request, response);
        } catch (NegocioException e) {
            response.sendRedirect(request.getContextPath() + "/jsp/dashboard-fretes.jsp");
        }
    }

    private void registrarOcorrencia(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Integer idFrete = parseInteger(request.getParameter("freteId"));
        OcorrenciaFrete ocorrencia = montarOcorrencia(request);
        try {
            if (ocorrencia.getTipo() == TipoOcorrenciaFrete.ENTREGA_REALIZADA) {
                freteBO.entregar(idFrete == null ? 0 : idFrete, ocorrencia);
                response.sendRedirect(request.getContextPath() + "/FreteController?acao=detalhar&id=" + idFrete + "&msg=entrega_registrada");
                return;
            }
            freteBO.registrarOcorrencia(idFrete == null ? 0 : idFrete, ocorrencia);
            response.sendRedirect(request.getContextPath() + "/FreteController?acao=detalhar&id=" + idFrete + "&msg=ocorrencia_registrada");
        } catch (NegocioException e) {
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("ocorrenciaForm", ocorrencia);
            try {
                carregarDetalheFrete(request, idFrete == null ? 0 : idFrete, ocorrencia);
                request.getRequestDispatcher("/jsp/detalhe-frete.jsp").forward(request, response);
            } catch (NegocioException ex) {
                response.sendRedirect(request.getContextPath() + "/jsp/dashboard-fretes.jsp");
            }
        }
    }

    private void alterarStatus(HttpServletRequest request, HttpServletResponse response, String acao) throws IOException {
        boolean origemDetalhe = "detalhe".equals(request.getParameter("origem"));
        try {
            int id = parseInteger(request.getParameter("id"));
            if ("confirmarSaida".equals(acao)) {
                freteBO.confirmarSaida(id);
            } else if ("iniciarTransito".equals(acao)) {
                freteBO.iniciarTransito(id);
            } else if ("entregar".equals(acao)) {
                OcorrenciaFrete ocorrenciaEntrega = montarOcorrencia(request);
                if (ocorrenciaEntrega.getTipo() == null) {
                    ocorrenciaEntrega.setTipo(TipoOcorrenciaFrete.ENTREGA_REALIZADA);
                }
                freteBO.entregar(id, ocorrenciaEntrega);
            } else if ("naoEntregar".equals(acao)) {
                freteBO.naoEntregar(id);
            } else {
                freteBO.cancelar(id);
            }
            if (origemDetalhe) {
                response.sendRedirect(request.getContextPath() + "/FreteController?acao=detalhar&id=" + id + "&msg=status_atualizado");
                return;
            }
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"sucesso\":true,\"mensagem\":\"Status do frete atualizado com sucesso.\"}");
        } catch (NegocioException e) {
            if (origemDetalhe) {
                try {
                    Integer id = parseInteger(request.getParameter("id"));
                    request.setAttribute("erro", e.getMessage());
                    request.setAttribute("ocorrenciaForm", montarOcorrencia(request));
                    carregarDetalheFrete(request, id == null ? 0 : id, (OcorrenciaFrete) request.getAttribute("ocorrenciaForm"));
                    request.getRequestDispatcher("/jsp/detalhe-frete.jsp").forward(request, response);
                    return;
                } catch (Exception ex) {
                    throw new IOException(ex);
                }
            }
            response.setStatus(400);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"" + escaparJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            if (origemDetalhe) {
                response.sendRedirect(request.getContextPath() + "/FreteController?acao=detalhar&id=" + request.getParameter("id") + "&msg=erro_status");
                return;
            }
            response.setStatus(500);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"Erro inesperado ao atualizar status do frete.\"}");
        }
    }

    private void carregarDetalheFrete(HttpServletRequest request, int idFrete, OcorrenciaFrete ocorrenciaForm)
            throws NegocioException {
        Frete frete = freteBO.obterPorId(idFrete);
        if (frete == null) {
            throw new NegocioException("Frete não encontrado.");
        }
        request.setAttribute("frete", frete);
        request.setAttribute("ocorrencias", ocorrenciaFreteBO.listarPorFrete(idFrete));
        request.setAttribute("tiposOcorrencia", TipoOcorrenciaFrete.values());
        if (ocorrenciaForm != null) {
            request.setAttribute("ocorrenciaForm", ocorrenciaForm);
        }
    }

    private void carregarCombos(HttpServletRequest request) {
        try {
            request.setAttribute("clientes", clienteBO.listar(""));
            request.setAttribute("motoristas", motoristaBO.listar(""));
            request.setAttribute("veiculos", veiculoBO.listar(""));
        } catch (Exception e) {
            request.setAttribute("erroCombos", "Não foi possível carregar clientes, motoristas e veículos.");
        }
    }

    private Frete montarFrete(HttpServletRequest request) {
        Frete frete = new Frete();
        frete.setRemetenteId(parseInteger(request.getParameter("remetenteId")));
        frete.setDestinatarioId(parseInteger(request.getParameter("destinatarioId")));
        frete.setMotoristaId(parseInteger(request.getParameter("motoristaId")));
        frete.setVeiculoId(parseInteger(request.getParameter("veiculoId")));
        frete.setMunicipioOrigem(request.getParameter("municipioOrigem"));
        frete.setUfOrigem(request.getParameter("ufOrigem"));
        frete.setMunicipioDestino(request.getParameter("municipioDestino"));
        frete.setUfDestino(request.getParameter("ufDestino"));
        frete.setDescricaoCarga(request.getParameter("descricaoCarga"));
        frete.setPesoKg(parseDouble(request.getParameter("pesoKg")));
        frete.setVolumes(parseInteger(request.getParameter("volumes")));
        frete.setValorFrete(parseDouble(request.getParameter("valorFrete")));
        frete.setAliquotaIcms(parseDouble(request.getParameter("aliquotaIcms")));
        frete.setValorIcms(parseDouble(request.getParameter("valorIcms")));
        frete.setValorTotal(parseDouble(request.getParameter("valorTotal")));
        frete.setStatus(parseStatus(request.getParameter("status")));
        frete.setDataEmissao(parseDataHora(request.getParameter("dataEmissao")));
        frete.setDataPrevisaoEntrega(parseData(request.getParameter("dataPrevisaoEntrega")));
        return frete;
    }

    private OcorrenciaFrete montarOcorrencia(HttpServletRequest request) {
        OcorrenciaFrete ocorrencia = new OcorrenciaFrete();
        ocorrencia.setFreteId(parseInteger(request.getParameter("freteId")));
        ocorrencia.setTipo(parseTipoOcorrencia(request.getParameter("tipoOcorrencia")));
        ocorrencia.setDataHora(parseDataHoraCompleta(request.getParameter("dataHoraOcorrencia")));
        ocorrencia.setMunicipio(request.getParameter("municipioOcorrencia"));
        ocorrencia.setUf(request.getParameter("ufOcorrencia"));
        ocorrencia.setDescricao(request.getParameter("descricaoOcorrencia"));
        ocorrencia.setNomeRecebedor(request.getParameter("nomeRecebedor"));
        ocorrencia.setDocumentoRecebedor(request.getParameter("documentoRecebedor"));
        return ocorrencia;
    }

    private StatusFrete parseStatus(String valor) {
        try {
            StatusFrete status = StatusFrete.fromCodigo(valor);
            return status == null ? StatusFrete.EMITIDO : status;
        } catch (IllegalArgumentException e) {
            return StatusFrete.EMITIDO;
        }
    }

    private TipoOcorrenciaFrete parseTipoOcorrencia(String valor) {
        try {
            return TipoOcorrenciaFrete.fromCodigo(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private Integer parseInteger(String valor) {
        try {
            return valor == null || valor.trim().isEmpty() ? null : Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private Double parseDouble(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) return null;
            String normalizado = valor.replace("R$", "").trim();
            if (normalizado.contains(",") && normalizado.contains(".")) {
                normalizado = normalizado.replace(".", "").replace(',', '.');
            } else {
                normalizado = normalizado.replace(',', '.');
            }
            return Double.parseDouble(normalizado);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private LocalDate parseData(String valor) {
        try {
            return valor == null || valor.trim().isEmpty() ? null : LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private LocalDateTime parseDataHora(String valor) {
        LocalDate data = parseData(valor);
        return data == null ? null : LocalDateTime.of(data, LocalTime.now());
    }

    private LocalDateTime parseDataHoraCompleta(String valor) {
        try {
            if (valor == null || valor.trim().isEmpty()) {
                return null;
            }
            return LocalDateTime.parse(valor.trim(), DateTimeFormatter.ISO_LOCAL_DATE_TIME);
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String jsonFrete(Frete f) {
        return "{\"id\":" + f.getId()
                + ",\"numero\":\"" + escaparJson(f.getNumero()) + "\""
                + ",\"status\":\"" + (f.getStatus() == null ? "" : f.getStatus().getCodigo()) + "\""
                + ",\"remetente\":\"" + escaparJson(f.getRemetenteNome()) + "\""
                + ",\"destinatario\":\"" + escaparJson(f.getDestinatarioNome()) + "\""
                + ",\"motorista\":\"" + escaparJson(f.getMotoristaNome()) + "\""
                + ",\"veiculo\":\"" + escaparJson(f.getVeiculoPlaca()) + "\""
                + ",\"origem\":\"" + escaparJson(f.getMunicipioOrigem()) + "/" + escaparJson(f.getUfOrigem()) + "\""
                + ",\"destino\":\"" + escaparJson(f.getMunicipioDestino()) + "/" + escaparJson(f.getUfDestino()) + "\""
                + ",\"descricaoCarga\":\"" + escaparJson(f.getDescricaoCarga()) + "\""
                + ",\"pesoKg\":" + valorJson(f.getPesoKg())
                + ",\"volumes\":" + valorJson(f.getVolumes())
                + ",\"valorFrete\":" + valorJson(f.getValorFrete())
                + ",\"aliquotaIcms\":" + valorJson(f.getAliquotaIcms())
                + ",\"valorIcms\":" + valorJson(f.getValorIcms())
                + ",\"valorTotal\":" + valorJson(f.getValorTotal())
                + ",\"dataEmissao\":\"" + (f.getDataEmissao() == null ? "" : f.getDataEmissao()) + "\""
                + ",\"dataPrevisaoEntrega\":\"" + (f.getDataPrevisaoEntrega() == null ? "" : f.getDataPrevisaoEntrega()) + "\""
                + ",\"dataSaida\":\"" + (f.getDataSaida() == null ? "" : f.getDataSaida()) + "\""
                + ",\"dataEntrega\":\"" + (f.getDataEntrega() == null ? "" : f.getDataEntrega()) + "\"}";
    }

    private String valorJson(Number valor) {
        return valor == null ? "null" : String.valueOf(valor);
    }

    private String escaparJson(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
