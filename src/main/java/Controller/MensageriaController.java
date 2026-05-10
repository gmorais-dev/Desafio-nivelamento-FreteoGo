package Controller;

import BO.MensageriaBO;
import Exception.MensageriaException;
import Mensageria.ResultadoEnvioMensageria;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLEncoder;

public class MensageriaController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final MensageriaBO mensageriaBO = new MensageriaBO();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("text/html;charset=UTF-8");

        String acao = request.getParameter("acao");
        if ("enviarPendentes".equals(acao)) {
            enviarPendentesComRedirect(request, response);
            return;
        }

        response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        request.setCharacterEncoding("UTF-8");
        response.setContentType("application/json;charset=UTF-8");

        String acao = request.getParameter("acao");
        if ("enviarPendentes".equals(acao)) {
            enviarPendentesJson(response);
            return;
        }

        response.setStatus(400);
        response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"Ação de mensageria inválida.\"}");
    }

    private void enviarPendentesComRedirect(HttpServletRequest request, HttpServletResponse response) throws IOException {
        try {
            ResultadoEnvioMensageria resultado = mensageriaBO.enviarPendentes();
            String destino = request.getContextPath()
                + "/jsp/menu.jsp?msg=mensageria_processada"
                + "&enviados=" + resultado.getEnviados()
                + "&erros=" + resultado.getErros()
                + "&total=" + resultado.getTotalEncontrados();
            response.sendRedirect(destino);
        } catch (MensageriaException e) {
            String destino = request.getContextPath()
                + "/jsp/menu.jsp?msg=mensageria_falha&detalhe=" + encode(e.getMessage());
            response.sendRedirect(destino);
        }
    }

    private void enviarPendentesJson(HttpServletResponse response) throws IOException {
        try {
            ResultadoEnvioMensageria resultado = mensageriaBO.enviarPendentes();
            response.getWriter().print("{\"sucesso\":true,\"total\":" + resultado.getTotalEncontrados()
                + ",\"enviados\":" + resultado.getEnviados()
                + ",\"erros\":" + resultado.getErros()
                + ",\"mensagem\":\"Envio de mensageria concluído.\"}");
        } catch (MensageriaException e) {
            response.setStatus(400);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    private String encode(String valor) throws IOException {
        return URLEncoder.encode(valor == null ? "" : valor, "UTF-8");
    }

    private String escaparJson(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
