package BO;

import DAO.ClienteDAO;
import Entity.Cliente;
import Exception.CadastroException;
import util.Validador;
import java.util.List;

import java.sql.SQLException;

public class ClienteBO {

    private final ClienteDAO clienteDAO = new ClienteDAO();

    

    public void cadastrar(Cliente cliente) throws CadastroException {
        validarCamposObrigatorios(cliente);
        validarCnpj(cliente.getCnpj());
        validarTipo(cliente.getTipo());
        if (Validador.isVazio(cliente.getStatus())) {
            cliente.setStatus("ATIVO");
        }
        validarStatus(cliente.getStatus());
        try {
            clienteDAO.inserir(cliente);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao cadastrar cliente. Tente novamente.", e);
        }
    }

    public void editar(Cliente cliente) throws CadastroException {
        if (cliente.getId() == null || cliente.getId() <= 0) {
            throw new CadastroException("Cliente inválido para edição.");
        }
        validarCamposObrigatorios(cliente);
        validarCnpj(cliente.getCnpj());
        validarTipo(cliente.getTipo());
        if (Validador.isVazio(cliente.getStatus())) {
            cliente.setStatus("ATIVO");
        }
        validarStatus(cliente.getStatus());
        try {
            clienteDAO.atualizar(cliente);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao editar cliente. Tente novamente.", e);
        }
    }

    public void excluir(int id) throws CadastroException {
        if (id <= 0) {
            throw new CadastroException("Cliente inválido para exclusão.");
        }
        try {
            if (clienteDAO.existeFreteVinculado(id)) {
                throw new CadastroException("Não é possível excluir cliente com frete em aberto. A exclusão só é permitida quando os fretes vinculados estiverem entregues, cancelados ou não entregues.");
            }
            boolean excluiu = clienteDAO.excluir(id);
            if (!excluiu) {
                throw new CadastroException("Cliente não encontrado para exclusão.");
            }
        } catch (CadastroException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroExclusao("cliente", e);
        }
    }

    public List<Cliente> listar(String filtro) throws CadastroException {
        if (filtro == null) filtro = "";
        try {
            return clienteDAO.listar(filtro);
        } catch (Exception e) {
            throw new CadastroException("Erro ao buscar clientes.", e);
        }
    }

    public List<Cliente> listarPaginado(String filtro, int pagina, int tamanhoPagina) throws CadastroException {
        int tamanho = tamanhoPagina <= 0 ? 10 : tamanhoPagina;
        int offset = Math.max(0, pagina - 1) * tamanho;
        try {
            return clienteDAO.listar(filtro == null ? "" : filtro, tamanho, offset);
        } catch (Exception e) {
            throw new CadastroException("Erro ao buscar clientes.", e);
        }
    }

    public Cliente obterPorId(int id) throws CadastroException {
        try {
            return clienteDAO.obterPorId(id);
        } catch (Exception e) {
            throw new CadastroException("Erro ao recuperar cliente.", e);
        }
    }

    private void validarCamposObrigatorios(Cliente cliente) throws CadastroException {
        if (Validador.isVazio(cliente.getRazaoSocial())) {
            throw new CadastroException("Informe a Razão Social da empresa antes de continuar.");
        }
        if (Validador.isVazio(cliente.getNomeFantasia())) {
            throw new CadastroException("O Nome Fantasia é obrigatório. Por favor, preencha esse campo.");
        }
        if (Validador.isVazio(cliente.getCnpj())) {
            throw new CadastroException("O CNPJ é obrigatório. Por favor, informe um CNPJ válido.");
        }
        if (Validador.isVazio(cliente.getTipo())) {
            throw new CadastroException("Selecione o Tipo de cliente (Remetente, Destinatário ou Ambos).");
        }
        if (Validador.isVazio(cliente.getLogradouro())) {
            throw new CadastroException("Informe o Logradouro (rua, avenida, etc.) do endereço.");
        }
        if (cliente.getNumero() == null) {
            throw new CadastroException("Informe o Número do endereço.");
        }
        if (Validador.isVazio(cliente.getBairro())) {
            throw new CadastroException("Informe o Bairro do endereço.");
        }
        if (Validador.isVazio(cliente.getMunicipio())) {
            throw new CadastroException("Informe o Município do endereço.");
        }
        if (Validador.isVazio(cliente.getUf())) {
            throw new CadastroException("Informe a UF (estado) do endereço.");
        }
        if (!Validador.ufValida(cliente.getUf())) {
            throw new CadastroException("A UF informada não é válida. Use a sigla do estado (ex: SP, RJ, MG).");
        }
        if (Validador.isVazio(cliente.getCep())) {
            throw new CadastroException("Informe o CEP do endereço.");
        }
        if (Validador.isVazio(cliente.getTelefone())) {
            throw new CadastroException("Informe um telefone para contato com a empresa.");
        }
        if (Validador.isVazio(cliente.getEmail())) {
            throw new CadastroException("Informe um e-mail válido para contato com a empresa.");
        }
    }

    private void validarCnpj(String cnpj) throws CadastroException {
        if (!Validador.cnpjValido(cnpj)) {
            throw new CadastroException("O CNPJ informado não é válido. Verifique os dígitos e tente novamente.");
        }
    }

    private void validarTipo(String tipo) throws CadastroException {
        if (!tipo.equals("REMETENTE") && !tipo.equals("DESTINATARIO") && !tipo.equals("AMBOS")) {
            throw new CadastroException("Tipo de cliente inválido. Selecione: Remetente, Destinatário ou Ambos.");
        }
    }

    private void validarStatus(String status) throws CadastroException {
        if (!"ATIVO".equals(status) && !"INATIVO".equals(status)) {
            throw new CadastroException("Status de cliente inválido. Selecione: Ativo ou Inativo.");
        }
    }

    private CadastroException traduzirErroExclusao(String entidade, SQLException e) {
        if ("23503".equals(e.getSQLState())) {
            return new CadastroException("Não é possível excluir " + entidade + " enquanto existirem fretes vinculados no histórico. Para permitir exclusão física após o encerramento dos fretes, a modelagem do relacionamento com frete precisa ser revista.", e);
        }
        if ("42P01".equals(e.getSQLState())) {
            return new CadastroException("Erro ao excluir " + entidade + ": tabela necessária não existe no banco (" + e.getMessage() + ").", e);
        }
        if ("42703".equals(e.getSQLState())) {
            return new CadastroException("Erro ao excluir " + entidade + ": coluna esperada não existe no banco (" + e.getMessage() + ").", e);
        }
        return new CadastroException("Erro ao excluir " + entidade + ": " + e.getMessage(), e);
    }
}
