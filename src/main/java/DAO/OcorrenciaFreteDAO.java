package DAO;

import Entity.OcorrenciaFrete;
import Entity.TipoOcorrenciaFrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class OcorrenciaFreteDAO {

    private static final String SQL_LISTAR_POR_FRETE =
        "SELECT id, id_frete, tipo, data_hora, municipio, uf, descricao, nome_recebedor, documento_recebedor " +
        "FROM ocorrencia_frete WHERE id_frete = ? ORDER BY data_hora ASC, id ASC";

    private static final String SQL_ULTIMA_DATA_HORA =
        "SELECT MAX(data_hora) FROM ocorrencia_frete WHERE id_frete = ?";

    private static final String SQL_INSERIR =
        "INSERT INTO ocorrencia_frete (id_frete, tipo, data_hora, municipio, uf, descricao, nome_recebedor, documento_recebedor) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    public List<OcorrenciaFrete> listarPorFrete(int freteId) throws SQLException {
        List<OcorrenciaFrete> lista = new ArrayList<OcorrenciaFrete>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(SQL_LISTAR_POR_FRETE);
            ps.setInt(1, freteId);
            rs = ps.executeQuery();
            while (rs.next()) {
                lista.add(mapear(rs));
            }
            return lista;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Timestamp obterUltimaDataHora(Connection conn, int freteId) throws SQLException {
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(SQL_ULTIMA_DATA_HORA);
            ps.setInt(1, freteId);
            rs = ps.executeQuery();
            return rs.next() ? rs.getTimestamp(1) : null;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void inserir(Connection conn, OcorrenciaFrete ocorrencia) throws SQLException {
        PreparedStatement ps = null;
        try {
            ps = conn.prepareStatement(SQL_INSERIR);
            ps.setInt(1, ocorrencia.getFreteId());
            ps.setString(2, ocorrencia.getTipo().getCodigo());
            ps.setTimestamp(3, Timestamp.valueOf(ocorrencia.getDataHora()));
            ps.setString(4, ocorrencia.getMunicipio());
            ps.setString(5, ocorrencia.getUf());
            ps.setString(6, ocorrencia.getDescricao());
            ps.setString(7, ocorrencia.getNomeRecebedor());
            ps.setString(8, ocorrencia.getDocumentoRecebedor());
            ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private OcorrenciaFrete mapear(ResultSet rs) throws SQLException {
        OcorrenciaFrete ocorrencia = new OcorrenciaFrete();
        ocorrencia.setId(rs.getInt("id"));
        ocorrencia.setFreteId(rs.getInt("id_frete"));
        ocorrencia.setTipo(TipoOcorrenciaFrete.fromCodigo(rs.getString("tipo")));
        Timestamp dataHora = rs.getTimestamp("data_hora");
        ocorrencia.setDataHora(dataHora == null ? null : dataHora.toLocalDateTime());
        ocorrencia.setMunicipio(rs.getString("municipio"));
        ocorrencia.setUf(rs.getString("uf"));
        ocorrencia.setDescricao(rs.getString("descricao"));
        ocorrencia.setNomeRecebedor(rs.getString("nome_recebedor"));
        ocorrencia.setDocumentoRecebedor(rs.getString("documento_recebedor"));
        return ocorrencia;
    }
}
