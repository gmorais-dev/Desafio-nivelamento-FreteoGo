package BO;

import DAO.MotoristaDAO;
import Entity.Motorista;
import Entity.StatusMotorista;
import Exception.CadastroException;
import util.Validador;

import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class MotoristaBO {

    private final MotoristaDAO motoristaDAO = new MotoristaDAO();

    public void cadastrar(Motorista motorista) throws CadastroException {
        validarCamposObrigatorios(motorista);
        validarCpf(motorista.getCpf());
        if (motorista.getStatus() == null) {
            motorista.setStatus(StatusMotorista.ATIVO);
        }
        try {
            motoristaDAO.inserir(motorista);
        } catch (SQLException e) {
            throw traduzirErroSql("Erro ao cadastrar motorista. Tente novamente.", e);
        }
    }

    public void editar(Motorista motorista) throws CadastroException {
        if (motorista.getId() == null || motorista.getId() <= 0) {
            throw new CadastroException("Motorista inválido para edição.");
        }
        validarCamposObrigatorios(motorista);
        validarCpf(motorista.getCpf());
        try {
            validarInativacaoComFreteAtivo(motorista);
            if (!motoristaDAO.atualizar(motorista)) {
                throw new CadastroException("Motorista não encontrado para edição.");
            }
        } catch (CadastroException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroSql("Erro ao editar motorista. Tente novamente.", e);
        }
    }

    public void excluir(int id) throws CadastroException {
        if (id <= 0) {
            throw new CadastroException("Motorista inválido para exclusão.");
        }
        try {
            if (motoristaDAO.possuiFreteVinculado(id)) {
                throw new CadastroException("Não é possível excluir motorista com frete vinculado.");
            }
            if (!motoristaDAO.excluir(id)) {
                throw new CadastroException("Motorista não encontrado para exclusão.");
            }
        } catch (CadastroException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroExclusao("motorista", e);
        }
    }

    public List<Motorista> listar(String filtro) throws CadastroException {
        try {
            return motoristaDAO.listar(filtro == null ? "" : filtro);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao buscar motoristas.", e);
        }
    }

    public List<Motorista> listarPaginado(String filtro, int pagina, int tamanhoPagina) throws CadastroException {
        int tamanho = tamanhoPagina <= 0 ? 10 : tamanhoPagina;
        int offset = Math.max(0, pagina - 1) * tamanho;
        try {
            return motoristaDAO.listar(filtro == null ? "" : filtro, tamanho, offset);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao buscar motoristas.", e);
        }
    }

    public Motorista obterPorId(int id) throws CadastroException {
        try {
            return motoristaDAO.obterPorId(id);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao recuperar motorista.", e);
        }
    }

    private void validarCamposObrigatorios(Motorista motorista) throws CadastroException {
        if (Validador.isVazio(motorista.getNome())) {
            throw new CadastroException("Informe o nome do motorista.");
        }
        if (Validador.isVazio(motorista.getCpf())) {
            throw new CadastroException("Informe o CPF do motorista.");
        }
        if (motorista.getDataNascimento() == null) {
            throw new CadastroException("Informe a data de nascimento do motorista.");
        }
        if (Validador.isVazio(motorista.getTelefone())) {
            throw new CadastroException("Informe o telefone do motorista.");
        }
        if (Validador.isVazio(motorista.getCnhNumero())) {
            throw new CadastroException("Informe o número da CNH.");
        }
        if (motorista.getCnhCategoria() == null) {
            throw new CadastroException("Informe a categoria da CNH.");
        }
        if (motorista.getCnhValidade() == null) {
            throw new CadastroException("Informe a validade da CNH.");
        }
        if (motorista.getCnhValidade().isBefore(LocalDate.now())) {
            throw new CadastroException("A CNH deve estar válida na data atual.");
        }
        if (Validador.isVazio(motorista.getTipoVinculo())) {
            throw new CadastroException("Informe o tipo de vínculo do motorista.");
        }
        validarTipoVinculo(motorista.getTipoVinculo());
        if (motorista.getStatus() == null) {
            throw new CadastroException("Informe o status do motorista.");
        }
    }

    private void validarTipoVinculo(String tipoVinculo) throws CadastroException {
        if (!"FUNCIONARIO".equals(tipoVinculo) && !"AGREGADO".equals(tipoVinculo) && !"TERCEIRO".equals(tipoVinculo)) {
            throw new CadastroException("Tipo de vínculo inválido. Selecione: Funcionário, Agregado ou Terceiro.");
        }
    }

    private void validarCpf(String cpf) throws CadastroException {
        if (!Validador.cpfValido(cpf)) {
            throw new CadastroException("O CPF informado não é válido.");
        }
    }

    private void validarInativacaoComFreteAtivo(Motorista motorista) throws CadastroException, SQLException {
        if (motorista.getStatus() != StatusMotorista.INATIVO) {
            return;
        }
        if (motoristaDAO.possuiFreteAtivo(motorista.getId())) {
            throw new CadastroException("Não é permitido inativar motorista com frete emitido, saída confirmada ou em trânsito.");
        }
    }

    private CadastroException traduzirErroSql(String mensagemPadrao, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new CadastroException("Já existe motorista cadastrado com o CPF ou CNH informados.", e);
        }
        return new CadastroException(mensagemPadrao, e);
    }

    private CadastroException traduzirErroExclusao(String entidade, SQLException e) {
        if ("23503".equals(e.getSQLState())) {
            return new CadastroException("Não é possível excluir " + entidade + " com registros vinculados.", e);
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
