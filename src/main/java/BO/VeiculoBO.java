package BO;

import DAO.VeiculoDAO;
import Entity.StatusVeiculo;
import Entity.Veiculo;
import Exception.CadastroException;
import util.Validador;

import java.sql.SQLException;
import java.time.Year;
import java.util.List;

public class VeiculoBO {

    private final VeiculoDAO veiculoDAO = new VeiculoDAO();

    public void cadastrar(Veiculo veiculo) throws CadastroException {
        validarCamposObrigatorios(veiculo);
        try {
            veiculoDAO.inserir(veiculo);
        } catch (SQLException e) {
            throw traduzirErroSql("Erro ao cadastrar veículo. Tente novamente.", e);
        }
    }

    public void editar(Veiculo veiculo) throws CadastroException {
        if (veiculo.getId() == null || veiculo.getId() <= 0) {
            throw new CadastroException("Veículo inválido para edição.");
        }
        validarCamposObrigatorios(veiculo);
        validarDisponivelManualComFreteEmTransito(veiculo);
        try {
            if (!veiculoDAO.atualizar(veiculo)) {
                throw new CadastroException("Veículo não encontrado para edição.");
            }
        } catch (CadastroException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroSql("Erro ao editar veículo. Tente novamente.", e);
        }
    }

    public void excluir(int id) throws CadastroException {
        if (id <= 0) {
            throw new CadastroException("Veículo inválido para exclusão.");
        }
        try {
            if (veiculoDAO.possuiFreteVinculado(id)) {
                throw new CadastroException("Não é possível excluir veículo com frete vinculado.");
            }
            if (!veiculoDAO.excluir(id)) {
                throw new CadastroException("Veículo não encontrado para exclusão.");
            }
        } catch (CadastroException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroExclusao("veículo", e);
        }
    }

    public List<Veiculo> listar(String filtro) throws CadastroException {
        try {
            return veiculoDAO.listar(filtro == null ? "" : filtro);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao buscar veículos.", e);
        }
    }

    public List<Veiculo> listarPaginado(String filtro, int pagina, int tamanhoPagina) throws CadastroException {
        int tamanho = tamanhoPagina <= 0 ? 10 : tamanhoPagina;
        int offset = Math.max(0, pagina - 1) * tamanho;
        try {
            return veiculoDAO.listar(filtro == null ? "" : filtro, tamanho, offset);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao buscar veículos.", e);
        }
    }

    public Veiculo obterPorId(int id) throws CadastroException {
        try {
            return veiculoDAO.obterPorId(id);
        } catch (SQLException e) {
            throw new CadastroException("Erro ao recuperar veículo.", e);
        }
    }

    private void validarCamposObrigatorios(Veiculo veiculo) throws CadastroException {
        if (Validador.isVazio(veiculo.getPlaca())) {
            throw new CadastroException("Informe a placa do veículo.");
        }
        validarPlaca(veiculo.getPlaca());
        if (Validador.isVazio(veiculo.getRntrc())) {
            throw new CadastroException("Informe o RNTRC do veículo.");
        }
        validarRntrc(veiculo.getRntrc());
        if (!Validador.isPositivo(veiculo.getAnoFabricacao())) {
            throw new CadastroException("Informe o ano de fabricação do veículo.");
        }
        int anoMaximo = Year.now().getValue() + 1;
        if (veiculo.getAnoFabricacao() < 1950 || veiculo.getAnoFabricacao() > anoMaximo) {
            throw new CadastroException("Ano de fabricação inválido.");
        }
        if (Validador.isVazio(veiculo.getTipo())) {
            throw new CadastroException("Informe o tipo do veículo.");
        }
        validarTipo(veiculo.getTipo());
        if (!Validador.isPositivo(veiculo.getTaraKg())) {
            throw new CadastroException("Informe a tara do veículo.");
        }
        if (!Validador.isPositivo(veiculo.getCapacidadeKg())) {
            throw new CadastroException("Informe a capacidade do veículo.");
        }
        if (!Validador.isPositivo(veiculo.getVolumeM3())) {
            throw new CadastroException("Informe o volume do veículo.");
        }
        if (Validador.isVazio(veiculo.getStatus())) {
            throw new CadastroException("Informe o status do veículo.");
        }
        validarStatus(veiculo.getStatus());
    }

    private void validarPlaca(String placa) throws CadastroException {
        String normalizada = placa == null ? "" : placa.trim().toUpperCase();
        boolean antiga = normalizada.matches("^[A-Z]{3}-?[0-9]{4}$");
        boolean mercosul = normalizada.matches("^[A-Z]{3}[0-9][A-Z][0-9]{2}$");
        if (!antiga && !mercosul) {
            throw new CadastroException("Informe uma placa válida no formato antigo (ABC-1234) ou Mercosul (ABC1D23).");
        }
    }

    private void validarRntrc(String rntrc) throws CadastroException {
        String normalizado = rntrc == null ? "" : rntrc.trim();
        if (!normalizado.matches("^\\d{8}$")) {
            throw new CadastroException("O RNTRC deve conter exatamente 8 dígitos numéricos.");
        }
    }

    private void validarTipo(String tipo) throws CadastroException {
        if (!"TRUCK".equals(tipo) && !"CARRETA".equals(tipo) && !"VAN".equals(tipo) && !"UTILITARIO".equals(tipo)) {
            throw new CadastroException("Tipo de veículo inválido. Selecione: Truck, Carreta, Van ou Utilitário.");
        }
    }

    private void validarStatus(String status) throws CadastroException {
        if (!StatusVeiculo.DISPONIVEL.getCodigo().equals(status)
                && !StatusVeiculo.EM_VIAGEM.getCodigo().equals(status)
                && !StatusVeiculo.MANUTENCAO.getCodigo().equals(status)) {
            throw new CadastroException("Status de veículo inválido. Selecione: Disponível, Em Viagem ou Em Manutenção.");
        }
    }

    private void validarDisponivelManualComFreteEmTransito(Veiculo veiculo) throws CadastroException {
        if (!"DISPONIVEL".equals(veiculo.getStatus())) {
            return;
        }
        try {
            if (veiculoDAO.possuiFreteEmTransito(veiculo.getId())) {
                throw new CadastroException("Não é permitido marcar veículo como disponível manualmente enquanto há frete com saída confirmada ou em trânsito.");
            }
        } catch (SQLException e) {
            throw new CadastroException("Erro ao validar status do veículo.", e);
        }
    }

    private CadastroException traduzirErroSql(String mensagemPadrao, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new CadastroException("Já existe veículo cadastrado com a placa informada.", e);
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
