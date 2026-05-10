package Controller;

import BO.VeiculoBO;
import Entity.Veiculo;
import Exception.NegocioException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

public class VeiculoController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final VeiculoBO veiculoBO = new VeiculoBO();

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

        request.getRequestDispatcher("/jsp/form--veiculo.jsp").forward(request, response);
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
            List<Veiculo> lista = veiculoBO.listarPaginado(request.getParameter("filtro"), parsePagina(request.getParameter("pagina")), 10);
            response.setContentType("application/json;charset=UTF-8");
            StringBuilder json = new StringBuilder("[");
            boolean primeira = true;
            for (Veiculo v : lista) {
                if (!primeira) json.append(',');
                primeira = false;
                json.append("{\"id\":").append(v.getId())
                    .append(",\"placa\":\"").append(escaparJson(v.getPlaca())).append("\"")
                    .append(",\"rntrc\":\"").append(escaparJson(v.getRntrc())).append("\"")
                    .append(",\"anoFabricacao\":").append(v.getAnoFabricacao() == null ? "null" : v.getAnoFabricacao())
                    .append(",\"tipo\":\"").append(escaparJson(v.getTipo())).append("\"")
                    .append(",\"taraKg\":").append(v.getTaraKg() == null ? "null" : v.getTaraKg())
                    .append(",\"capacidadeKg\":").append(v.getCapacidadeKg() == null ? "null" : v.getCapacidadeKg())
                    .append(",\"volumeM3\":").append(v.getVolumeM3() == null ? "null" : v.getVolumeM3())
                    .append(",\"status\":\"").append(escaparJson(v.getStatus())).append("\"}");
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
            Veiculo veiculo = veiculoBO.obterPorId(Integer.parseInt(request.getParameter("id")));
            if (veiculo == null) {
                response.sendRedirect(request.getContextPath() + "/jsp/consulta-veiculo.jsp");
                return;
            }
            request.setAttribute("veiculo", veiculo);
            request.setAttribute("modo", "editar".equals(request.getParameter("acao")) ? "editar" : "visualizar");
            request.getRequestDispatcher("/jsp/form--veiculo.jsp").forward(request, response);
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jsp/consulta-veiculo.jsp");
        }
    }

    private void cadastrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Veiculo veiculo = montarVeiculo(request);
        try {
            veiculoBO.cadastrar(veiculo);
            response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp?msg=veiculo_cadastrado");
        } catch (NegocioException e) {
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("veiculo", veiculo);
            request.getRequestDispatcher("/jsp/form--veiculo.jsp").forward(request, response);
        }
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        Veiculo veiculo = montarVeiculo(request);
        try {
            veiculoBO.editar(veiculo);
            response.sendRedirect(request.getContextPath() + "/jsp/consulta-veiculo.jsp?msg=veiculo_editado");
        } catch (NegocioException e) {
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("veiculo", veiculo);
            request.setAttribute("modo", "editar");
            request.getRequestDispatcher("/jsp/form--veiculo.jsp").forward(request, response);
        }
    }

    private void excluir(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        try {
            veiculoBO.excluir(Integer.parseInt(request.getParameter("id")));
            response.getWriter().print("{\"sucesso\":true,\"mensagem\":\"Veículo excluído com sucesso.\"}");
        } catch (NegocioException e) {
            response.setStatus(400);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"" + escaparJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            response.setStatus(500);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"Erro inesperado ao excluir veículo.\"}");
        }
    }

    private Veiculo montarVeiculo(HttpServletRequest request) {
        Veiculo veiculo = new Veiculo();
        veiculo.setId(parseInteger(request.getParameter("id")));
        veiculo.setPlaca(request.getParameter("placa"));
        veiculo.setRntrc(request.getParameter("rntrc"));
        veiculo.setAnoFabricacao(parseInteger(request.getParameter("anoFabricacao")));
        veiculo.setTipo(request.getParameter("tipo"));
        veiculo.setTaraKg(parseDouble(request.getParameter("taraKg")));
        veiculo.setCapacidadeKg(parseDouble(request.getParameter("capacidadeKg")));
        veiculo.setVolumeM3(parseDouble(request.getParameter("volumeM3")));
        veiculo.setStatus(request.getParameter("status"));
        return veiculo;
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
            return valor == null || valor.trim().isEmpty() ? null : Double.parseDouble(valor.trim().replace(',', '.'));
        } catch (NumberFormatException e) {
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
}
