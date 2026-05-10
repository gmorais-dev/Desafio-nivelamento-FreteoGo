package DAO;

import Entity.Motorista;
import Entity.CategoriaCnh;
import Entity.StatusFrete;
import Entity.StatusMotorista;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class MotoristaDAO {

    private static final String CAMPOS =
        "id, nome, cpf, data_nascimento, telefone, cnh_numero, cnh_categoria, cnh_validade, tipo_vinculo, status";

    private static final String SQL_INSERT =
        "INSERT INTO motorista (nome, cpf, data_nascimento, telefone, cnh_numero, cnh_categoria, cnh_validade, tipo_vinculo, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE motorista SET nome = ?, cpf = ?, data_nascimento = ?, telefone = ?, cnh_numero = ?, " +
        "cnh_categoria = ?, cnh_validade = ?, tipo_vinculo = ?, status = ? WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM motorista WHERE id = ?";

    private static final String SQL_LISTAR =
        "SELECT " + CAMPOS + " FROM motorista " +
        "WHERE nome ILIKE ? OR cpf ILIKE ? OR cnh_numero ILIKE ? OR cnh_categoria ILIKE ? OR tipo_vinculo ILIKE ? " +
        "ORDER BY id DESC";

    private static final String SQL_LISTAR_PAGINADO = SQL_LISTAR + " LIMIT ? OFFSET ?";

    private static final String SQL_OBTER = "SELECT " + CAMPOS + " FROM motorista WHERE id = ?";

    private static final String SQL_FRETE_ATIVO =
        "SELECT 1 FROM frete WHERE id_motorista = ? AND status IN (?, ?, ?, ?, ?) LIMIT 1";

    private static final String SQL_FRETE_VINCULADO =
        "SELECT 1 FROM frete WHERE id_motorista = ? LIMIT 1";

    private static final String SQL_FRETE_ATIVO_ALT =
        "SELECT 1 FROM frete WHERE motorista_id = ? AND status IN (?, ?, ?, ?, ?) LIMIT 1";

    private static final String SQL_FRETE_VINCULADO_ALT =
        "SELECT 1 FROM frete WHERE motorista_id = ? LIMIT 1";

    public void inserir(Motorista motorista) throws SQLException {
        executarGravacao(motorista, SQL_INSERT, false);
    }

    public boolean atualizar(Motorista motorista) throws SQLException {
        return executarGravacao(motorista, SQL_UPDATE, true);
    }

    private boolean executarGravacao(Motorista m, String sql, boolean update) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            preencherParametros(ps, m);
            if (update) {
                ps.setInt(10, m.getId());
            }
            int linhas = ps.executeUpdate();
            conn.commit();
            return linhas > 0;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public List<Motorista> listar(String filtro) throws SQLException {
        return listar(filtro, 0, 0);
    }

    public List<Motorista> listar(String filtro, int limite, int offset) throws SQLException {
        List<Motorista> motoristas = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(limite > 0 ? SQL_LISTAR_PAGINADO : SQL_LISTAR);
            String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
            for (int i = 1; i <= 5; i++) {
                ps.setString(i, like);
            }
            if (limite > 0) {
                ps.setInt(6, limite);
                ps.setInt(7, Math.max(0, offset));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                motoristas.add(mapear(rs));
            }
            return motoristas;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Motorista obterPorId(int id) throws SQLException {
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

    public boolean possuiFreteAtivo(int id) throws SQLException {
        try {
            return possuiFreteAtivo(id, SQL_FRETE_ATIVO);
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) {
                return false;
            }
            if ("42703".equals(e.getSQLState())) {
                return possuiFreteAtivo(id, SQL_FRETE_ATIVO_ALT);
            }
            throw e;
        }
    }

    private boolean possuiFreteAtivo(int id, String sql) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setString(2, StatusFrete.EMITIDO.getCodigo());
            ps.setString(3, StatusFrete.SAIDA_CONFIRMADA.getCodigo());
            ps.setString(4, "SAÍDA CONFIRMADA");
            ps.setString(5, StatusFrete.EM_TRANSITO.getCodigo());
            ps.setString(6, "EM TRÂNSITO");
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean possuiFreteVinculado(int id) throws SQLException {
        try {
            return existePorSql(SQL_FRETE_VINCULADO, id);
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) {
                return false;
            }
            if ("42703".equals(e.getSQLState())) {
                return existePorSql(SQL_FRETE_VINCULADO_ALT, id);
            }
            throw e;
        }
    }

    public boolean excluir(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(SQL_DELETE);
            ps.setInt(1, id);
            int linhas = ps.executeUpdate();
            conn.commit();
            return linhas > 0;
        } catch (SQLException e) {
            if (conn != null) try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            throw e;
        } finally {
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private boolean existePorSql(String sql, int id) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            rs = ps.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    private void preencherParametros(PreparedStatement ps, Motorista m) throws SQLException {
        ps.setString(1, m.getNome());
        ps.setString(2, m.getCpf());
        if (m.getDataNascimento() != null) {
            ps.setDate(3, Date.valueOf(m.getDataNascimento()));
        } else {
            ps.setNull(3, java.sql.Types.DATE);
        }
        ps.setString(4, m.getTelefone());
        ps.setString(5, m.getCnhNumero());
        ps.setString(6, m.getCnhCategoria() == null ? null : m.getCnhCategoria().getCodigo());
        if (m.getCnhValidade() != null) {
            ps.setDate(7, Date.valueOf(m.getCnhValidade()));
        } else {
            ps.setNull(7, java.sql.Types.DATE);
        }
        ps.setString(8, m.getTipoVinculo());
        ps.setString(9, m.getStatus() == null ? null : m.getStatus().getCodigo());
    }

    private Motorista mapear(ResultSet rs) throws SQLException {
        Motorista m = new Motorista();
        m.setId(rs.getInt("id"));
        m.setNome(rs.getString("nome"));
        m.setCpf(rs.getString("cpf"));
        Date dataNascimento = rs.getDate("data_nascimento");
        m.setDataNascimento(dataNascimento == null ? null : dataNascimento.toLocalDate());
        m.setTelefone(rs.getString("telefone"));
        m.setCnhNumero(rs.getString("cnh_numero"));
        m.setCnhCategoria(CategoriaCnh.fromCodigo(rs.getString("cnh_categoria")));
        Date cnhValidade = rs.getDate("cnh_validade");
        m.setCnhValidade(cnhValidade == null ? null : cnhValidade.toLocalDate());
        m.setTipoVinculo(rs.getString("tipo_vinculo"));
        m.setStatus(StatusMotorista.fromCodigo(rs.getString("status")));
        return m;
    }
}
