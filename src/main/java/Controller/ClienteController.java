package Controller;

import BO.ClienteBO;
import Entity.Cliente;
import Exception.NegocioException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

public class ClienteController extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private final ClienteBO clienteBO = new ClienteBO();

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
        
        request.getRequestDispatcher("/jsp/form-cliente.jsp").forward(request, response);
    }
    private void buscar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String filtro = request.getParameter("filtro");
        String status = request.getParameter("status");
        try {
            List<Cliente> lista = clienteBO.listarPaginado(filtro, parsePagina(request.getParameter("pagina")), 10);
            
            // Filtra por status se foi informado
            if (status != null && !status.isEmpty()) {
                List<Cliente> listaFiltrada = new ArrayList<>();
                for (Cliente c : lista) {
                    if (status.equals(c.getStatus())) {
                        listaFiltrada.add(c);
                    }
                }
                lista = listaFiltrada;
            }
            
            response.setContentType("application/json;charset=UTF-8");
            java.io.PrintWriter out = response.getWriter();
            out.print("[");
            boolean primeira = true;
            for (Cliente c : lista) {
                if (!primeira) out.print(',');
                primeira = false;
                out.print("{\"id\":" + c.getId()
                        + ",\"empresa\":\"" + (c.getNomeFantasia()!=null?c.getNomeFantasia().replace("\"","\\\"") : "") + "\""
                        + ",\"razao\":\"" + (c.getRazaoSocial()!=null?c.getRazaoSocial().replace("\"","\\\"") : "") + "\""
                        + ",\"cnpj\":\"" + (c.getCnpj()!=null?c.getCnpj():"") + "\""
                        + ",\"ie\":\"" + (c.getInscricaoEstadual()!=null?c.getInscricaoEstadual():"") + "\""
                        + ",\"municipio\":\"" + (c.getMunicipio()!=null?c.getMunicipio():"") + "\""
                        + ",\"uf\":\"" + (c.getUf()!=null?c.getUf():"") + "\""
                        + ",\"status\":\"" + (c.getStatus()!=null?c.getStatus():"") + "\"}");
            }
            out.print("]");
            out.flush();
        } catch (Exception e) {
            response.setStatus(e instanceof NegocioException ? 400 : 500);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().print("{\"message\":\"" + escaparJson(e.getMessage()) + "\"}");
        }
    }

    private void carregarFormulario(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String id = request.getParameter("id");
        String acao = request.getParameter("acao");
        try {
            int clienteId = Integer.parseInt(id);
            Cliente cliente = clienteBO.obterPorId(clienteId);
            
            if (cliente != null) {
                request.setAttribute("cliente", cliente);
                request.setAttribute("modo", "editar".equals(acao) ? "editar" : "visualizar");
                request.getRequestDispatcher("/jsp/form-cliente.jsp").forward(request, response);
            } else {
                response.sendRedirect(request.getContextPath() + "/jsp/consulta-clientes.jsp");
            }
        } catch (NumberFormatException e) {
            response.sendRedirect(request.getContextPath() + "/jsp/consulta-clientes.jsp");
        } catch (Exception e) {
            response.sendRedirect(request.getContextPath() + "/jsp/consulta-clientes.jsp");
        }
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


    private void cadastrar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cliente cliente = montarCliente(request);

        try {
            clienteBO.cadastrar(cliente);
            response.sendRedirect(request.getContextPath() + "/jsp/menu.jsp?msg=cliente_cadastrado");

        } catch (NegocioException e) {
            // Captura CadastroException e qualquer subclasse de NegocioException
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("cliente", cliente);
            request.getRequestDispatcher("/jsp/form-cliente.jsp").forward(request, response);
        }

        
    }

    private void editar(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        Cliente cliente = montarCliente(request);

        try {
            clienteBO.editar(cliente);
            response.sendRedirect(request.getContextPath() + "/jsp/consulta-clientes.jsp?msg=cliente_editado");

        } catch (NegocioException e) {
            request.setAttribute("erro", e.getMessage());
            request.setAttribute("cliente", cliente);
            request.setAttribute("modo", "editar");
            request.getRequestDispatcher("/jsp/form-cliente.jsp").forward(request, response);
        }
    }

    private void excluir(HttpServletRequest request, HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json;charset=UTF-8");

        String idStr = request.getParameter("id");
        try {
            int id = Integer.parseInt(idStr);
            clienteBO.excluir(id);
            response.getWriter().print("{\"sucesso\":true,\"mensagem\":\"Cliente excluído com sucesso.\"}");
        } catch (NegocioException e) {
            response.setStatus(400);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"" + escaparJson(e.getMessage()) + "\"}");
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatus(500);
            response.getWriter().print("{\"sucesso\":false,\"mensagem\":\"Erro inesperado ao excluir cliente.\"}");
        }
    }

    private Cliente montarCliente(HttpServletRequest request) {
        String idStr             = request.getParameter("id");
        String razaoSocial       = request.getParameter("razaoSocial");
        String nomeFantasia      = request.getParameter("nomeFantasia");
        String cnpj              = request.getParameter("cnpj");
        String inscricaoEstadual = request.getParameter("inscricaoEstadual");
        String tipo              = request.getParameter("tipo");
        String logradouro        = request.getParameter("logradouro");
        String numeroStr         = request.getParameter("numero");
        String complemento       = request.getParameter("complemento");
        String bairro            = request.getParameter("bairro");
        String municipio         = request.getParameter("municipio");
        String uf                = request.getParameter("uf");
        String cep               = request.getParameter("cep");
        String telefone          = request.getParameter("telefone");
        String email             = request.getParameter("email");
        String status            = request.getParameter("status");

        Integer id = null;
        if (idStr != null && !idStr.trim().isEmpty()) {
            try {
                id = Integer.parseInt(idStr.trim());
            } catch (NumberFormatException e) {
            
            }
        }

        Integer numero = null;
        if (numeroStr != null && !numeroStr.trim().isEmpty()) {
            try {
                numero = Integer.parseInt(numeroStr.trim());
            } catch (NumberFormatException e) {
            
            }
        }

        Cliente cliente = new Cliente();
        cliente.setId(id);
        cliente.setRazaoSocial(razaoSocial);
        cliente.setNomeFantasia(nomeFantasia);
        cliente.setCnpj(cnpj);
        cliente.setInscricaoEstadual(inscricaoEstadual);
        cliente.setTipo(tipo);
        cliente.setLogradouro(logradouro);
        cliente.setNumero(numero);
        cliente.setComplemento(complemento);
        cliente.setBairro(bairro);
        cliente.setMunicipio(municipio);
        cliente.setUf(uf);
        cliente.setCep(cep);
        cliente.setTelefone(telefone);
        cliente.setEmail(email);
        cliente.setStatus(status);

        return cliente;
    }

    private String escaparJson(String valor) {
        if (valor == null) {
            return "";
        }
        return valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private int parsePagina(String valor) {
        try {
            return valor == null || valor.trim().isEmpty() ? 1 : Integer.parseInt(valor.trim());
        } catch (NumberFormatException e) {
            return 1;
        }
    }
}
