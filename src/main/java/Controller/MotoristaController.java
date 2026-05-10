package Controller;

import BO.MotoristaBO;
import Entity.CategoriaCnh;
import Entity.Motorista;
import Entity.StatusMotorista;
import Exception.NegocioException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;

public class MotoristaController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MotoristaBO motoristaBO = new MotoristaBO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String acao = request.getParameter("acao");
        String id = request.getParameter("id");

        if ("buscar".equals(acao)) {
            buscar(request, response);
            return;
        }

        if (("visualizar".equals(acao) || "editar".equals(acao)) && id != null && !id.isEmpty()) {
            carregarFormulario(request, response);
            return;
        }

        request.getRequestDispatcher("/jsp/form-motorista.jsp").forward(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String acao = request.getParameter("acao");

        if ("cadastrar".equals(acao)) {
            cadastrar(request, response);
        } else if ("editar".equals(acao)) {
            editar(request, response);
        } else if ("excluir".equals(acao)) {
            excluir(request, response);
        } else {
            response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp");
        }
    }

    private void buscar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            String filtro = request.getParameter("filtro");
            String status = request.getParameter("status");
            String filtroConsulta = (filtro == null || filtro.trim().isEmpty()) ? status : filtro;
            List<Motorista> lista = motoristaBO.listarPaginado(filtroConsulta, parsePagina(request.getParameter("pagina")), 10);
            response.setContentType("application/json;charset=UTF-8");
            StringBuilder json = new StringBuilder("[");
            boolean primeira = true;
            for (Motorista m : lista) {
                if (!primeira) json.append(',');
                primeira = false;
                json.append("{\"id\":").append(m.getId())
                    .append(",\"nome\":\"").append(escaparJson(m.getNome())).append("\"")
                    .append(",\"cpf\":\"").append(escaparJson(m.getCpf())).append("\"")
                    .append(",\"dataNascimento\":\"").append(m.getDataNascimento() == null ? "" : m.getDataNascimento()).append("\"")
                    .append(",\"telefone\":\"").append(escaparJson(m.getTelefone())).append("\"")
                    .append(",\"cnhNumero\":\"").append(escaparJson(m.getCnhNumero())).append("\"")
                    .append(",\"cnhCategoria\":\"").append(escaparJson(codigo(m.getCnhCategoria()))).append("\"")
                    .append(",\"cnhValidade\":\"").append(m.getCnhValidade() == null ? "" : m.getCnhValidade()).append("\"")
                    .append(",\"tipoVinculo\":\"").append(escaparJson(m.getTipoVinculo())).append("\"")
                    .append(",\"status\":\"").append(escaparJson(codigo(m.getStatus()))).append("\"}");
            }
            json.append(']');
            response.getWriter().print(json.toString());
        } catch (Exception e) {
            response.setStatus(e instanceof NegocioException ? 400 : 500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"mensagem\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    private void carregarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        try {
            Motorista motorista = motoristaBO.obterPorId(Integer.parseInt(request.getParameter("id")));
            if (motorista == null) {
                response.sendRedirect(request.getContextPath() + "/jsp/consulta-motorista.jsp");
                return;
            }
            request.setAttribute("motorista", motorista);
            request.setAttribute("modo", "editar".equals(request.getParameter("acao")) ? "editar" : "visualizar");
            request.getRequestDispatcher("/jsp/form-motorista.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jsp/consulta-motorista.jsp");
        }
    }

    private void cadastrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Motorista motorista = montarMotorista(request);
        try {
            motoristaBO.cadastrar(motorista);
            response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp?msg=motorista_cadastrado");
        } catch (NegocioException e) {
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("motorista", motorista);
            request.getRequestDispatcher("/jsp/form-motorista.jsp").forward(request, response);
        }
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Motorista motorista = montarMotorista(request);
        try {
            motoristaBO.editar(motorista);
            response.sendRedirect(request.getContextPath() + "/jsp/consulta-motorista.jsp?msg=motorista_editado");
        } catch (NegocioException e) {
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("motorista", motorista);
            request.setAttribute("modo", "editar");
            request.getRequestDispatcher("/jsp/form-motorista.jsp").forward(request, response);
        }
    }

    private void excluir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            motoristaBO.excluir(Integer.parseInt(request.getParameter("id")));
            response.getWriter().print("{\"sucesso\":true,\"mensagem\":\"Motorista excluído com sucesso.\"}");
        } catch (NegocioException e) {
            response.setStatus(400);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"" + escaparJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"Erro inesperado ao excluir motorista.\"}");
        }
    }

    private Motorista montarMotorista(HttpServletRequest request) {
        Motorista motorista = new Motorista();
        motorista.setId(parseInteger(request.getParameter("id")));
        motorista.setNome(request.getParameter("nome"));
        motorista.setCpf(request.getParameter("cpf"));
        motorista.setDataNascimento(parseData(request.getParameter("dataNascimento")));
        motorista.setTelefone(request.getParameter("telefone"));
        motorista.setCnhNumero(request.getParameter("cnhNumero"));
        motorista.setCnhCategoria(parseCategoriaCnh(request.getParameter("cnhCategoria")));
        motorista.setCnhValidade(parseData(request.getParameter("cnhValidade")));
        motorista.setTipoVinculo(request.getParameter("tipoVinculo"));
        motorista.setStatus(parseStatusMotorista(request.getParameter("status")));
        return motorista;
    }

    private CategoriaCnh parseCategoriaCnh(String valor) {
        try {
            return CategoriaCnh.fromCodigo(valor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private StatusMotorista parseStatusMotorista(String valor) {
        try {
            return StatusMotorista.fromCodigo(valor);
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

    private LocalDate parseData(String valor) {
        try {
            return valor == null || valor.trim().isEmpty() ? null : LocalDate.parse(valor.trim());
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private String escaparJson(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private int parsePagina(String valor) {
        try {
            return valor == null || valor.trim().isEmpty() ? 1 : Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }

    private String codigo(Enum<?> valor) {
        return valor == null ? "" : valor.toString();
    }
}
