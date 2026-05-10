package DAO;

import Entity.EventoSistema;
import Entity.Frete;
import Entity.OcorrenciaFrete;
import Entity.StatusFrete;
import Entity.StatusVeiculo;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class FreteDAO {

    private final OcorrenciaFreteDAO ocorrenciaFreteDAO = new OcorrenciaFreteDAO();

    private static final String CAMPOS_FRETE =
        "f.id, f.numero, f.id_remetente, f.id_destinatario, f.id_motorista, f.id_veiculo, " +
        "f.municipio_origem, f.uf_origem, f.municipio_destino, f.uf_destino, f.descricao_carga, " +
        "f.peso_kg, f.volumes, f.valor_frete, f.aliquota_icms, f.valor_icms, f.valor_total, " +
        "f.status, f.data_emissao, f.data_previsao_entrega, f.data_saida, f.data_entrega, " +
        "cr.razao_social AS remetente_nome, cd.razao_social AS destinatario_nome, " +
        "m.nome AS motorista_nome, v.placa AS veiculo_placa";

    private static final String FROM_FRETE =
        " FROM frete f " +
        "JOIN cliente cr ON cr.id = f.id_remetente " +
        "JOIN cliente cd ON cd.id = f.id_destinatario " +
        "JOIN motorista m ON m.id = f.id_motorista " +
        "JOIN veiculo v ON v.id = f.id_veiculo ";

    private static final String SQL_INSERT_FRETE =
        "INSERT INTO frete (numero, id_remetente, id_destinatario, id_motorista, id_veiculo, " +
        "municipio_origem, uf_origem, municipio_destino, uf_destino, descricao_carga, peso_kg, volumes, " +
        "valor_frete, aliquota_icms, valor_icms, valor_total, status, data_emissao, data_previsao_entrega) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_INSERT_EVENTO =
        "INSERT INTO evento_sistema (tipo, entidade, entidade_id, payload, status, tentativas) " +
        "VALUES (?, ?, ?, ?, ?, ?)";

    private static final String SQL_OBTER =
        "SELECT " + CAMPOS_FRETE + FROM_FRETE + "WHERE f.id = ?";

    private static final String SQL_LISTAR =
        "SELECT " + CAMPOS_FRETE + FROM_FRETE +
        "WHERE f.numero ILIKE ? OR cr.razao_social ILIKE ? OR cd.razao_social ILIKE ? OR " +
        "m.nome ILIKE ? OR v.placa ILIKE ? OR f.status ILIKE ? " +
        "ORDER BY f.id DESC";

    private static final String SQL_PROXIMO_NUMERO =
        "SELECT COALESCE(MAX(id), 0) + 1 FROM frete";

    public int inserir(Frete frete, EventoSistema evento) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet chaves = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(SQL_INSERT_FRETE, Statement.RETURN_GENERATED_KEYS);
            preencherFrete(ps, frete);
            ps.executeUpdate();
            chaves = ps.getGeneratedKeys();
            if (!chaves.next()) {
                throw new SQLException("Nao foi possivel recuperar o ID do frete criado.");
            }
            int idFrete = chaves.getInt(1);
            inserirEvento(conn, evento, idFrete);
            conn.commit();
            return idFrete;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            if (chaves != null) try { chaves.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean atualizarStatus(int id, StatusFrete statusAtual, StatusFrete novoStatus, EventoSistema evento,
                                   OcorrenciaFrete ocorrencia) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(
                "UPDATE frete SET status = ?, " +
                "data_saida = CASE WHEN ? THEN COALESCE(data_saida, ?) WHEN ? THEN NULL ELSE data_saida END, " +
                "data_entrega = ? WHERE id = ? AND status = ?");
            ps.setString(1, novoStatus.getCodigo());
            boolean registraSaida = novoStatus == StatusFrete.SAIDA_CONFIRMADA || novoStatus == StatusFrete.EM_TRANSITO;
            ps.setBoolean(2, registraSaida);
            ps.setTimestamp(3, Timestamp.valueOf(obterReferenciaDataHora(novoStatus, ocorrencia)));
            ps.setBoolean(4, novoStatus == StatusFrete.CANCELADO);
            if (novoStatus == StatusFrete.ENTREGUE || novoStatus == StatusFrete.NAO_ENTREGUE) {
                ps.setTimestamp(5, Timestamp.valueOf(obterReferenciaDataHora(novoStatus, ocorrencia)));
            } else {
                ps.setNull(5, java.sql.Types.TIMESTAMP);
            }
            ps.setInt(6, id);
            ps.setString(7, statusAtual.getCodigo());
            int linhas = ps.executeUpdate();
            if (linhas == 0) {
                conn.rollback();
                return false;
            }

            atualizarVeiculoPorStatus(conn, id, novoStatus);
            if (ocorrencia != null) {
                ocorrenciaFreteDAO.inserir(conn, ocorrencia);
            }
            inserirEvento(conn, evento, id);
            conn.commit();
            return true;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void registrarOcorrencia(int idFrete, OcorrenciaFrete ocorrencia, EventoSistema evento) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);
            ocorrenciaFreteDAO.inserir(conn, ocorrencia);
            inserirEvento(conn, evento, idFrete);
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Timestamp obterUltimaDataHoraOcorrencia(int idFrete) throws SQLException {
        Connection conn = null;
        try {
            conn = Conexao.getConexao();
            return ocorrenciaFreteDAO.obterUltimaDataHora(conn, idFrete);
        } finally {
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Frete obterPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(SQL_OBTER);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next() ? mapear(rs) : null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public List<Frete> listar(String filtro) throws SQLException {
        List<Frete> fretes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(SQL_LISTAR);
            String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
            for (int i = 1; i <= 6; i++) {
                ps.setString(i, like);
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                fretes.add(mapear(rs));
            }
            return fretes;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public int obterProximoSequencial() throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(SQL_PROXIMO_NUMERO);
            rs = ps.executeQuery();
            return rs.next() ? rs.getInt(1) : 1;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void preencherFrete(PreparedStatement ps, Frete f) throws SQLException {
        ps.setString(1, f.getNumero());
        ps.setInt(2, f.getRemetenteId());
        ps.setInt(3, f.getDestinatarioId());
        ps.setInt(4, f.getMotoristaId());
        ps.setInt(5, f.getVeiculoId());
        ps.setString(6, f.getMunicipioOrigem());
        ps.setString(7, f.getUfOrigem());
        ps.setString(8, f.getMunicipioDestino());
        ps.setString(9, f.getUfDestino());
        ps.setString(10, f.getDescricaoCarga());
        ps.setDouble(11, f.getPesoKg());
        ps.setInt(12, f.getVolumes());
        ps.setDouble(13, f.getValorFrete());
        ps.setDouble(14, f.getAliquotaIcms());
        ps.setDouble(15, f.getValorIcms());
        ps.setDouble(16, f.getValorTotal());
        ps.setString(17, f.getStatus().getCodigo());
        ps.setTimestamp(18, Timestamp.valueOf(f.getDataEmissao()));
        ps.setDate(19, Date.valueOf(f.getDataPrevisaoEntrega()));
    }

    private void inserirEvento(Connection conn, EventoSistema evento, int entidadeId) throws SQLException {
        if (evento == null) {
            return;
        }
        evento.setEntidadeId(entidadeId);
        if (evento.getPayload() != null) {
            evento.setPayload(evento.getPayload().replace("\"id\":null", "\"id\":" + entidadeId));
        }
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(SQL_INSERT_EVENTO);
            ps.setString(1, evento.getTipo().getCodigo());
            ps.setString(2, evento.getEntidade());
            ps.setInt(3, entidadeId);
            ps.setString(4, evento.getPayload());
            ps.setString(5, evento.getStatus());
            ps.setInt(6, evento.getTentativas() == null ? 0 : evento.getTentativas());
            ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void atualizarVeiculoPorStatus(Connection conn, int idFrete, StatusFrete novoStatus) throws SQLException {
        String statusVeiculo = null;
        if (novoStatus == StatusFrete.SAIDA_CONFIRMADA || novoStatus == StatusFrete.EM_TRANSITO) {
            statusVeiculo = StatusVeiculo.EM_VIAGEM.getCodigo();
        }
        if (novoStatus == StatusFrete.ENTREGUE || novoStatus == StatusFrete.NAO_ENTREGUE || novoStatus == StatusFrete.CANCELADO) {
            statusVeiculo = StatusVeiculo.DISPONIVEL.getCodigo();
        }
        if (statusVeiculo == null) {
            return;
        }

        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement("UPDATE veiculo SET status = ? WHERE id = (SELECT id_veiculo FROM frete WHERE id = ?)");
            ps.setString(1, statusVeiculo);
            ps.setInt(2, idFrete);
            ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private java.time.LocalDateTime obterReferenciaDataHora(StatusFrete novoStatus, OcorrenciaFrete ocorrencia) {
        if (ocorrencia != null && ocorrencia.getDataHora() != null) {
            return ocorrencia.getDataHora();
        }
        return java.time.LocalDateTime.now();
    }

    private Frete mapear(ResultSet rs) throws SQLException {
        Frete f = new Frete();
        f.setId(rs.getInt("id"));
        f.setNumero(rs.getString("numero"));
        f.setRemetenteId(rs.getInt("id_remetente"));
        f.setDestinatarioId(rs.getInt("id_destinatario"));
        f.setMotoristaId(rs.getInt("id_motorista"));
        f.setVeiculoId(rs.getInt("id_veiculo"));
        f.setMunicipioOrigem(rs.getString("municipio_origem"));
        f.setUfOrigem(rs.getString("uf_origem"));
        f.setMunicipioDestino(rs.getString("municipio_destino"));
        f.setUfDestino(rs.getString("uf_destino"));
        f.setDescricaoCarga(rs.getString("descricao_carga"));
        f.setPesoKg(rs.getObject("peso_kg") == null ? null : rs.getDouble("peso_kg"));
        f.setVolumes(rs.getObject("volumes") == null ? null : rs.getInt("volumes"));
        f.setValorFrete(rs.getObject("valor_frete") == null ? null : rs.getDouble("valor_frete"));
        f.setAliquotaIcms(rs.getObject("aliquota_icms") == null ? null : rs.getDouble("aliquota_icms"));
        f.setValorIcms(rs.getObject("valor_icms") == null ? null : rs.getDouble("valor_icms"));
        f.setValorTotal(rs.getObject("valor_total") == null ? null : rs.getDouble("valor_total"));
        f.setStatus(StatusFrete.fromCodigo(rs.getString("status")));
        Timestamp dataEmissao = rs.getTimestamp("data_emissao");
        Date previsao = rs.getDate("data_previsao_entrega");
        Timestamp dataSaida = rs.getTimestamp("data_saida");
        Timestamp dataEntrega = rs.getTimestamp("data_entrega");
        f.setDataEmissao(dataEmissao == null ? null : dataEmissao.toLocalDateTime());
        f.setDataPrevisaoEntrega(previsao == null ? null : previsao.toLocalDate());
        f.setDataSaida(dataSaida == null ? null : dataSaida.toLocalDateTime());
        f.setDataEntrega(dataEntrega == null ? null : dataEntrega.toLocalDateTime());
        f.setRemetenteNome(rs.getString("remetente_nome"));
        f.setDestinatarioNome(rs.getString("destinatario_nome"));
        f.setMotoristaNome(rs.getString("motorista_nome"));
        f.setVeiculoPlaca(rs.getString("veiculo_placa"));
        return f;
    }
}
