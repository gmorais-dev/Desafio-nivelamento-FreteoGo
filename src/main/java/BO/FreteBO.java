package BO;

import DAO.ClienteDAO;
import DAO.FreteDAO;
import DAO.MotoristaDAO;
import DAO.VeiculoDAO;
import Entity.Cliente;
import Entity.EventoSistema;
import Entity.Frete;
import Entity.Motorista;
import Entity.OcorrenciaFrete;
import Entity.StatusFrete;
import Entity.StatusMotorista;
import Entity.StatusVeiculo;
import Entity.TipoEvento;
import Entity.TipoOcorrenciaFrete;
import Entity.Veiculo;
import Exception.FreteException;
import Mensageria.MensageriaConfig;
import util.Validador;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;

public class FreteBO {

    private final FreteDAO freteDAO = new FreteDAO();
    private final ClienteDAO clienteDAO = new ClienteDAO();
    private final MotoristaDAO motoristaDAO = new MotoristaDAO();
    private final VeiculoDAO veiculoDAO = new VeiculoDAO();

    public int cadastrar(Frete frete) throws FreteException {
        try {
            prepararFrete(frete);
            validarCriacao(frete);
            EventoSistema evento = criarEvento(TipoEvento.FRETE_CRIADO, frete, frete.getStatus(), null);
            return freteDAO.inserir(frete, evento);
        } catch (FreteException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroSql("Erro ao cadastrar frete. Tente novamente.", e);
        }
    }

    public List<Frete> listar(String filtro) throws FreteException {
        try {
            return freteDAO.listar(filtro == null ? "" : filtro);
        } catch (SQLException e) {
            throw new FreteException("Erro ao buscar fretes.", e);
        }
    }

    public Frete obterPorId(int id) throws FreteException {
        try {
            return freteDAO.obterPorId(id);
        } catch (SQLException e) {
            throw new FreteException("Erro ao recuperar frete.", e);
        }
    }

    public void confirmarSaida(int id) throws FreteException {
        alterarStatus(id, StatusFrete.EMITIDO, StatusFrete.SAIDA_CONFIRMADA, TipoEvento.FRETE_SAIDA_CONFIRMADA,
            criarOcorrenciaAutomatica(TipoOcorrenciaFrete.SAIDA_PATIO, "Saída do pátio confirmada."));
    }

    public void iniciarTransito(int id) throws FreteException {
        alterarStatus(id, StatusFrete.SAIDA_CONFIRMADA, StatusFrete.EM_TRANSITO, TipoEvento.FRETE_EM_TRANSITO,
            criarOcorrenciaAutomatica(TipoOcorrenciaFrete.EM_ROTA, "Frete em rota."));
    }

    public void entregar(int id, OcorrenciaFrete ocorrenciaEntrega) throws FreteException {
        alterarStatus(id, StatusFrete.EM_TRANSITO, StatusFrete.ENTREGUE, TipoEvento.FRETE_ENTREGUE, ocorrenciaEntrega);
    }

    public void naoEntregar(int id) throws FreteException {
        alterarStatus(id, StatusFrete.EM_TRANSITO, StatusFrete.NAO_ENTREGUE, TipoEvento.FRETE_NAO_ENTREGUE, null);
    }

    public void cancelar(int id) throws FreteException {
        alterarStatus(id, StatusFrete.EMITIDO, StatusFrete.CANCELADO, TipoEvento.FRETE_CANCELADO, null);
    }

    public void registrarOcorrencia(int idFrete, OcorrenciaFrete ocorrencia) throws FreteException {
        if (idFrete <= 0) {
            throw new FreteException("Frete inválido.");
        }
        try {
            Frete frete = freteDAO.obterPorId(idFrete);
            if (frete == null) {
                throw new FreteException("Frete não encontrado.");
            }
            ocorrencia.setFreteId(idFrete);
            completarOcorrenciaComFrete(frete, ocorrencia);
            validarOcorrencia(frete, ocorrencia, false);
            EventoSistema evento = criarEventoOcorrencia(frete, ocorrencia);
            freteDAO.registrarOcorrencia(idFrete, ocorrencia, evento);
        } catch (FreteException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroSql("Erro ao registrar ocorrência do frete.", e);
        }
    }

    private void alterarStatus(int id, StatusFrete statusAtualEsperado, StatusFrete novoStatus, TipoEvento tipoEvento,
                               OcorrenciaFrete ocorrencia) throws FreteException {
        if (id <= 0) {
            throw new FreteException("Frete inválido.");
        }
        try {
            Frete frete = freteDAO.obterPorId(id);
            if (frete == null) {
                throw new FreteException("Frete não encontrado.");
            }
            if (frete.getStatus() != statusAtualEsperado) {
                throw new FreteException("Transição de status inválida. Status atual: " + frete.getStatus().getCodigo() + ".");
            }
            if (ocorrencia != null) {
                completarOcorrenciaComFrete(frete, ocorrencia);
                validarOcorrencia(frete, ocorrencia, novoStatus == StatusFrete.ENTREGUE);
            }
            frete.setStatus(novoStatus);
            EventoSistema evento = criarEvento(tipoEvento, frete, novoStatus, ocorrencia);
            if (!freteDAO.atualizarStatus(id, statusAtualEsperado, novoStatus, evento, ocorrencia)) {
                throw new FreteException("Frete não encontrado ou status alterado por outro processo.");
            }
        } catch (FreteException e) {
            throw e;
        } catch (SQLException e) {
            throw traduzirErroSql("Erro ao atualizar status do frete.", e);
        }
    }

    private void prepararFrete(Frete frete) throws SQLException {
        if (frete.getDataEmissao() == null) {
            frete.setDataEmissao(LocalDateTime.now());
        }
        if (frete.getStatus() == null) {
            frete.setStatus(StatusFrete.EMITIDO);
        }
        frete.setNumero(gerarNumeroFrete());
        double valorFrete = valorOuZero(frete.getValorFrete());
        double aliquota = valorOuZero(frete.getAliquotaIcms());
        double valorIcms = valorFrete * aliquota / 100;
        frete.setValorIcms(valorIcms);
        frete.setValorTotal(valorFrete + valorIcms);
    }

    private String gerarNumeroFrete() throws SQLException {
        int sequencial = freteDAO.obterProximoSequencial();
        return String.format("FRT-%d-%05d", Year.now().getValue(), sequencial);
    }

    private void validarCriacao(Frete frete) throws FreteException, SQLException {
        if (frete.getRemetenteId() == null || frete.getRemetenteId() <= 0) {
            throw new FreteException("Informe o remetente do frete.");
        }
        if (frete.getDestinatarioId() == null || frete.getDestinatarioId() <= 0) {
            throw new FreteException("Informe o destinatário do frete.");
        }
        if (frete.getRemetenteId().equals(frete.getDestinatarioId())) {
            throw new FreteException("O destinatário deve ser diferente do remetente.");
        }
        if (frete.getMotoristaId() == null || frete.getMotoristaId() <= 0) {
            throw new FreteException("Informe o motorista do frete.");
        }
        if (frete.getVeiculoId() == null || frete.getVeiculoId() <= 0) {
            throw new FreteException("Informe o veículo do frete.");
        }
        validarCamposRotaCargaValores(frete);
        validarPartes(frete);
    }

    private void validarCamposRotaCargaValores(Frete frete) throws FreteException {
        if (Validador.isVazio(frete.getMunicipioOrigem())) {
            throw new FreteException("Informe o município de origem.");
        }
        if (!Validador.ufValida(frete.getUfOrigem())) {
            throw new FreteException("Informe uma UF de origem válida.");
        }
        if (Validador.isVazio(frete.getMunicipioDestino())) {
            throw new FreteException("Informe o município de destino.");
        }
        if (!Validador.ufValida(frete.getUfDestino())) {
            throw new FreteException("Informe uma UF de destino válida.");
        }
        if (Validador.isVazio(frete.getDescricaoCarga())) {
            throw new FreteException("Informe a descrição da carga.");
        }
        if (!Validador.isPositivo(frete.getPesoKg())) {
            throw new FreteException("Informe um peso maior que zero.");
        }
        if (!Validador.isPositivo(frete.getVolumes())) {
            throw new FreteException("Informe uma quantidade de volumes maior que zero.");
        }
        if (!Validador.isPositivo(frete.getValorFrete())) {
            throw new FreteException("Informe um valor de frete maior que zero.");
        }
        if (frete.getAliquotaIcms() == null || frete.getAliquotaIcms() < 0 || frete.getAliquotaIcms() > 100) {
            throw new FreteException("Informe uma alíquota de ICMS entre 0 e 100.");
        }
        if (frete.getDataPrevisaoEntrega() == null) {
            throw new FreteException("Informe a previsão de entrega.");
        }
        LocalDate emissao = frete.getDataEmissao() == null ? LocalDate.now() : frete.getDataEmissao().toLocalDate();
        if (!frete.getDataPrevisaoEntrega().isAfter(emissao)) {
            throw new FreteException("A previsão de entrega deve ser posterior à data de emissão.");
        }
    }

    private void validarPartes(Frete frete) throws FreteException, SQLException {
        Cliente remetente = clienteDAO.obterPorId(frete.getRemetenteId());
        Cliente destinatario = clienteDAO.obterPorId(frete.getDestinatarioId());
        Motorista motorista = motoristaDAO.obterPorId(frete.getMotoristaId());
        Veiculo veiculo = veiculoDAO.obterPorId(frete.getVeiculoId());

        if (remetente == null || !"ATIVO".equals(remetente.getStatus())) {
            throw new FreteException("Remetente não encontrado ou inativo.");
        }
        if (destinatario == null || !"ATIVO".equals(destinatario.getStatus())) {
            throw new FreteException("Destinatário não encontrado ou inativo.");
        }
        if (motorista == null || motorista.getStatus() != StatusMotorista.ATIVO) {
            throw new FreteException("Motorista não encontrado ou inativo.");
        }
        if (motorista.getCnhValidade() == null || motorista.getCnhValidade().isBefore(frete.getDataEmissao().toLocalDate())) {
            throw new FreteException("A CNH do motorista deve estar válida na data de emissão.");
        }
        if (motoristaDAO.possuiFreteAtivo(frete.getMotoristaId())) {
            throw new FreteException("O motorista já possui frete emitido, com saída confirmada ou em trânsito.");
        }
        if (veiculo == null || !StatusVeiculo.DISPONIVEL.getCodigo().equals(veiculo.getStatus())) {
            throw new FreteException("Veículo não encontrado ou indisponível.");
        }
        if (veiculo.getCapacidadeKg() == null || frete.getPesoKg() > veiculo.getCapacidadeKg()) {
            throw new FreteException("O peso da carga excede a capacidade do veículo.");
        }
    }

    private void validarOcorrencia(Frete frete, OcorrenciaFrete ocorrencia, boolean entrega) throws FreteException, SQLException {
        if (ocorrencia == null) {
            throw new FreteException("Informe os dados da ocorrência.");
        }
        if (ocorrencia.getTipo() == null) {
            throw new FreteException("Informe o tipo da ocorrência.");
        }
        if (entrega && ocorrencia.getTipo() != TipoOcorrenciaFrete.ENTREGA_REALIZADA) {
            throw new FreteException("A entrega deve registrar uma ocorrência do tipo Entrega Realizada.");
        }
        if (!entrega && ocorrencia.getTipo() == TipoOcorrenciaFrete.ENTREGA_REALIZADA) {
            throw new FreteException("Use a rotina de entrega para registrar Entrega Realizada.");
        }
        if (frete.getStatus() == StatusFrete.ENTREGUE || frete.getStatus() == StatusFrete.NAO_ENTREGUE
                || frete.getStatus() == StatusFrete.CANCELADO) {
            throw new FreteException("Não é permitido registrar ocorrência em frete finalizado.");
        }
        if (ocorrencia.getDataHora() == null) {
            throw new FreteException("Informe a data e hora da ocorrência.");
        }
        if (frete.getDataEmissao() != null && ocorrencia.getDataHora().isBefore(frete.getDataEmissao())) {
            throw new FreteException("A ocorrência não pode ser anterior à emissão do frete.");
        }
        Timestamp ultimaDataHora = freteDAO.obterUltimaDataHoraOcorrencia(frete.getId());
        if (ultimaDataHora != null && ocorrencia.getDataHora().isBefore(ultimaDataHora.toLocalDateTime())) {
            throw new FreteException("A ocorrência deve respeitar a ordem cronológica do frete.");
        }
        if (Validador.isVazio(ocorrencia.getMunicipio())) {
            throw new FreteException("Informe o município da ocorrência.");
        }
        if (!Validador.ufValida(ocorrencia.getUf())) {
            throw new FreteException("Informe uma UF válida para a ocorrência.");
        }
        if (ocorrencia.getTipo() == TipoOcorrenciaFrete.AVARIA
                || ocorrencia.getTipo() == TipoOcorrenciaFrete.EXTRAVIO
                || ocorrencia.getTipo() == TipoOcorrenciaFrete.OUTROS) {
            if (Validador.isVazio(ocorrencia.getDescricao())) {
                throw new FreteException("A descrição é obrigatória para avaria, extravio e outros.");
            }
        }
        if (ocorrencia.getTipo() == TipoOcorrenciaFrete.ENTREGA_REALIZADA) {
            if (Validador.isVazio(ocorrencia.getNomeRecebedor())) {
                throw new FreteException("Informe o nome do recebedor para concluir a entrega.");
            }
            if (Validador.isVazio(ocorrencia.getDocumentoRecebedor())) {
                throw new FreteException("Informe o documento do recebedor para concluir a entrega.");
            }
        }
    }

    private void completarOcorrenciaComFrete(Frete frete, OcorrenciaFrete ocorrencia) {
        ocorrencia.setFreteId(frete.getId());
        if (ocorrencia.getDataHora() == null) {
            ocorrencia.setDataHora(LocalDateTime.now());
        }
        if (Validador.isVazio(ocorrencia.getMunicipio())) {
            ocorrencia.setMunicipio(frete.getMunicipioOrigem());
        }
        if (Validador.isVazio(ocorrencia.getUf())) {
            ocorrencia.setUf(frete.getUfOrigem());
        }
        ocorrencia.setMunicipio(ocorrencia.getMunicipio() == null ? null : ocorrencia.getMunicipio().trim());
        ocorrencia.setUf(ocorrencia.getUf() == null ? null : ocorrencia.getUf().trim().toUpperCase());
        ocorrencia.setDescricao(normalizarTexto(ocorrencia.getDescricao()));
        ocorrencia.setNomeRecebedor(normalizarTexto(ocorrencia.getNomeRecebedor()));
        ocorrencia.setDocumentoRecebedor(normalizarTexto(ocorrencia.getDocumentoRecebedor()));
    }

    private OcorrenciaFrete criarOcorrenciaAutomatica(TipoOcorrenciaFrete tipo, String descricao) {
        OcorrenciaFrete ocorrencia = new OcorrenciaFrete();
        ocorrencia.setTipo(tipo);
        ocorrencia.setDescricao(descricao);
        ocorrencia.setDataHora(LocalDateTime.now());
        return ocorrencia;
    }

    private EventoSistema criarEvento(TipoEvento tipo, Frete frete, StatusFrete status, OcorrenciaFrete ocorrencia) {
        EventoSistema evento = new EventoSistema();
        evento.setTipo(tipo);
        evento.setEntidade("FRETE");
        evento.setPayload(criarPayloadEvento(tipo, frete, status, ocorrencia));
        evento.setStatus("PENDENTE");
        evento.setTentativas(0);
        return evento;
    }

    private EventoSistema criarEventoOcorrencia(Frete frete, OcorrenciaFrete ocorrencia) {
        EventoSistema evento = new EventoSistema();
        evento.setTipo(TipoEvento.OCORRENCIA_FRETE_REGISTRADA);
        evento.setEntidade("FRETE");
        evento.setPayload(criarPayloadOcorrencia(frete, ocorrencia));
        evento.setStatus("PENDENTE");
        evento.setTentativas(0);
        return evento;
    }

    private String criarPayloadEvento(TipoEvento tipo, Frete frete, StatusFrete status, OcorrenciaFrete ocorrencia) {
        StringBuilder json = new StringBuilder();
        json.append('{')
            .append("\"versao\":\"1.0\",")
            .append("\"evento\":\"").append(tipo.getCodigo()).append("\",")
            .append("\"origem\":\"").append(escaparJson(MensageriaConfig.origem())).append("\",")
            .append("\"endpointMensageria\":\"").append(escaparJson(MensageriaConfig.endpointEventos())).append("\",")
            .append("\"mensageriaHabilitada\":").append(MensageriaConfig.habilitada()).append(',')
            .append("\"dataEvento\":\"").append(LocalDateTime.now()).append("\",")
            .append("\"frete\":{")
            .append("\"id\":").append(frete.getId() == null ? "null" : frete.getId()).append(',')
            .append("\"numero\":\"").append(escaparJson(frete.getNumero())).append("\",")
            .append("\"status\":\"").append(status.getCodigo()).append("\",")
            .append("\"idRemetente\":").append(frete.getRemetenteId()).append(',')
            .append("\"idDestinatario\":").append(frete.getDestinatarioId()).append(',')
            .append("\"idMotorista\":").append(frete.getMotoristaId()).append(',')
            .append("\"idVeiculo\":").append(frete.getVeiculoId()).append(',')
            .append("\"origem\":\"").append(escaparJson(frete.getMunicipioOrigem())).append('/').append(escaparJson(frete.getUfOrigem())).append("\",")
            .append("\"destino\":\"").append(escaparJson(frete.getMunicipioDestino())).append('/').append(escaparJson(frete.getUfDestino())).append("\",")
            .append("\"pesoKg\":").append(valorOuZero(frete.getPesoKg())).append(',')
            .append("\"valorTotal\":").append(valorOuZero(frete.getValorTotal()))
            .append('}');
        if (ocorrencia != null) {
            appendOcorrenciaJson(json, ocorrencia);
        }
        json.append('}');
        return json.toString();
    }

    private String criarPayloadOcorrencia(Frete frete, OcorrenciaFrete ocorrencia) {
        StringBuilder json = new StringBuilder();
        json.append('{')
            .append("\"versao\":\"1.0\",")
            .append("\"evento\":\"").append(TipoEvento.OCORRENCIA_FRETE_REGISTRADA.getCodigo()).append("\",")
            .append("\"origem\":\"").append(escaparJson(MensageriaConfig.origem())).append("\",")
            .append("\"endpointMensageria\":\"").append(escaparJson(MensageriaConfig.endpointEventos())).append("\",")
            .append("\"mensageriaHabilitada\":").append(MensageriaConfig.habilitada()).append(',')
            .append("\"dataEvento\":\"").append(LocalDateTime.now()).append("\",")
            .append("\"frete\":{")
            .append("\"id\":").append(frete.getId()).append(',')
            .append("\"numero\":\"").append(escaparJson(frete.getNumero())).append("\",")
            .append("\"status\":\"").append(frete.getStatus().getCodigo()).append("\"")
            .append('}');
        appendOcorrenciaJson(json, ocorrencia);
        json.append('}');
        return json.toString();
    }

    private void appendOcorrenciaJson(StringBuilder json, OcorrenciaFrete ocorrencia) {
        json.append(",\"ocorrencia\":{")
            .append("\"tipo\":\"").append(ocorrencia.getTipo().getCodigo()).append("\",")
            .append("\"descricaoTipo\":\"").append(escaparJson(ocorrencia.getTipo().getDescricao())).append("\",")
            .append("\"dataHora\":\"").append(ocorrencia.getDataHora()).append("\",")
            .append("\"municipio\":\"").append(escaparJson(ocorrencia.getMunicipio())).append("\",")
            .append("\"uf\":\"").append(escaparJson(ocorrencia.getUf())).append("\",")
            .append("\"descricao\":\"").append(escaparJson(ocorrencia.getDescricao())).append("\",")
            .append("\"nomeRecebedor\":\"").append(escaparJson(ocorrencia.getNomeRecebedor())).append("\",")
            .append("\"documentoRecebedor\":\"").append(escaparJson(ocorrencia.getDocumentoRecebedor())).append("\"")
            .append('}');
    }

    private double valorOuZero(Double valor) {
        return valor == null ? 0D : valor;
    }

    private String normalizarTexto(String valor) {
        return Validador.isVazio(valor) ? null : valor.trim();
    }

    private String escaparJson(String valor) {
        return valor == null ? "" : valor.replace("\\", "\\\\").replace("\"", "\\\"");
    }

    private FreteException traduzirErroSql(String mensagemPadrao, SQLException e) {
        if ("23505".equals(e.getSQLState())) {
            return new FreteException("Já existe frete cadastrado com o número informado.", e);
        }
        if ("42P01".equals(e.getSQLState())) {
            return new FreteException("Erro ao processar frete: tabela necessária não existe no banco (" + e.getMessage() + ").", e);
        }
        if ("42703".equals(e.getSQLState())) {
            return new FreteException("Erro ao processar frete: coluna esperada não existe no banco (" + e.getMessage() + ").", e);
        }
        return new FreteException(mensagemPadrao, e);
    }
}
