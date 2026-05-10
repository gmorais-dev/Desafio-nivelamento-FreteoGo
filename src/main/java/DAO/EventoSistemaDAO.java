package DAO;

import Entity.EventoSistema;
import Entity.TipoEvento;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class EventoSistemaDAO {

    private static final String SQL_LISTAR_PARA_ENVIO =
        "SELECT id, tipo, entidade, entidade_id, payload, status, tentativas, data_criacao, data_publicacao, mensagem_erro " +
        "FROM evento_sistema WHERE status IN ('PENDENTE', 'ERRO') ORDER BY id ASC LIMIT ?";

    private static final String SQL_MARCAR_ENVIADO =
        "UPDATE evento_sistema SET status = 'ENVIADO', data_publicacao = CURRENT_TIMESTAMP, mensagem_erro = NULL WHERE id = ?";

    private static final String SQL_MARCAR_ERRO =
        "UPDATE evento_sistema SET status = 'ERRO', tentativas = COALESCE(tentativas, 0) + 1, mensagem_erro = ?, data_publicacao = NULL WHERE id = ?";

    public List<EventoSistema> listarParaEnvio(int limite) throws SQLException {
        List<EventoSistema> eventos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(SQL_LISTAR_PARA_ENVIO);
            ps.setInt(1, limite);
            rs = ps.executeQuery();
            while (rs.next()) {
                eventos.add(mapear(rs));
            }
            return eventos;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public void marcarComoEnviado(int idEvento) throws SQLException {
        executarAtualizacaoSimples(SQL_MARCAR_ENVIADO, null, idEvento);
    }

    public void marcarComoErro(int idEvento, String mensagemErro) throws SQLException {
        executarAtualizacaoSimples(SQL_MARCAR_ERRO, limitarMensagem(mensagemErro), idEvento);
    }

    private void executarAtualizacaoSimples(String sql, String mensagemErro, int idEvento) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(sql);
            if (mensagemErro != null) {
                ps.setString(1, mensagemErro);
                ps.setInt(2, idEvento);
            } else {
                ps.setInt(1, idEvento);
            }
            ps.executeUpdate();
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private EventoSistema mapear(ResultSet rs) throws SQLException {
        EventoSistema evento = new EventoSistema();
        evento.setId(rs.getInt("id"));
        evento.setTipo(parseTipo(rs.getString("tipo")));
        evento.setEntidade(rs.getString("entidade"));
        evento.setEntidadeId(rs.getInt("entidade_id"));
        evento.setPayload(rs.getString("payload"));
        evento.setStatus(rs.getString("status"));
        evento.setTentativas(rs.getInt("tentativas"));
        evento.setDataCriacao(toLocalDateTime(rs.getTimestamp("data_criacao")));
        evento.setDataPublicacao(toLocalDateTime(rs.getTimestamp("data_publicacao")));
        evento.setMensagemErro(rs.getString("mensagem_erro"));
        return evento;
    }

    private TipoEvento parseTipo(String codigo) {
        if (codigo == null) {
            return null;
        }
        for (TipoEvento tipo : TipoEvento.values()) {
            if (tipo.getCodigo().equals(codigo)) {
                return tipo;
            }
        }
        return null;
    }

    private LocalDateTime toLocalDateTime(Timestamp timestamp) {
        return timestamp == null ? null : timestamp.toLocalDateTime();
    }

    private String limitarMensagem(String mensagemErro) {
        if (mensagemErro == null || mensagemErro.length() <= 500) {
            return mensagemErro;
        }
        return mensagemErro.substring(0, 500);
    }
}
