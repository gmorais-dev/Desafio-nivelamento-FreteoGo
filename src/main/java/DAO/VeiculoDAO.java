package DAO;

import Entity.StatusFrete;
import Entity.Veiculo;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VeiculoDAO {

    private static final String CAMPOS =
        "id, placa, rntrc, ano_fabricacao, tipo, tara_kg, capacidade_kg, volume_m3, status";

    private static final String SQL_INSERT =
        "INSERT INTO veiculo (placa, rntrc, ano_fabricacao, tipo, tara_kg, capacidade_kg, volume_m3, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE veiculo SET placa = ?, rntrc = ?, ano_fabricacao = ?, tipo = ?, tara_kg = ?, " +
        "capacidade_kg = ?, volume_m3 = ?, status = ? WHERE id = ?";

    private static final String SQL_DELETE = "DELETE FROM veiculo WHERE id = ?";
    private static final String SQL_OBTER = "SELECT " + CAMPOS + " FROM veiculo WHERE id = ?";

    private static final String SQL_LISTAR =
        "SELECT " + CAMPOS + " FROM veiculo " +
        "WHERE placa ILIKE ? OR rntrc ILIKE ? OR tipo ILIKE ? " +
        "ORDER BY id DESC";

    private static final String SQL_LISTAR_PAGINADO = SQL_LISTAR + " LIMIT ? OFFSET ?";

    private static final String SQL_FRETE_EM_TRANSITO =
        "SELECT 1 FROM frete WHERE id_veiculo = ? AND status IN (?, ?, ?, ?) LIMIT 1";

    private static final String SQL_FRETE_VINCULADO =
        "SELECT 1 FROM frete WHERE id_veiculo = ? LIMIT 1";

    private static final String SQL_FRETE_EM_TRANSITO_ALT =
        "SELECT 1 FROM frete WHERE veiculo_id = ? AND status IN (?, ?, ?, ?) LIMIT 1";

    private static final String SQL_FRETE_VINCULADO_ALT =
        "SELECT 1 FROM frete WHERE veiculo_id = ? LIMIT 1";

    public void inserir(Veiculo veiculo) throws SQLException {
        executarGravacao(veiculo, SQL_INSERT, false);
    }

    public boolean atualizar(Veiculo veiculo) throws SQLException {
        return executarGravacao(veiculo, SQL_UPDATE, true);
    }

    private boolean executarGravacao(Veiculo v, String sql, boolean update) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);
            ps = conn.prepareStatement(sql);
            preencherParametros(ps, v);
            if (update) {
                ps.setInt(9, v.getId());
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

    public List<Veiculo> listar(String filtro) throws SQLException {
        return listar(filtro, 0, 0);
    }

    public List<Veiculo> listar(String filtro, int limite, int offset) throws SQLException {
        List<Veiculo> veiculos = new ArrayList<>();
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(limite > 0 ? SQL_LISTAR_PAGINADO : SQL_LISTAR);
            String like = "%" + (filtro == null ? "" : filtro.trim()) + "%";
            ps.setString(1, like);
            ps.setString(2, like);
            ps.setString(3, like);
            if (limite > 0) {
                ps.setInt(4, limite);
                ps.setInt(5, Math.max(0, offset));
            }
            rs = ps.executeQuery();
            while (rs.next()) {
                veiculos.add(mapear(rs));
            }
            return veiculos;
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (ps != null) try { ps.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public Veiculo obterPorId(int id) throws SQLException {
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

    public boolean possuiFreteEmTransito(int id) throws SQLException {
        try {
            return possuiFreteEmTransito(id, SQL_FRETE_EM_TRANSITO);
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) {
                return false;
            }
            if ("42703".equals(e.getSQLState())) {
                return possuiFreteEmTransito(id, SQL_FRETE_EM_TRANSITO_ALT);
            }
            throw e;
        }
    }

    private boolean possuiFreteEmTransito(int id, String sql) throws SQLException {
        Connection conn = null;
        PreparedStatement ps = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            ps = conn.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setString(2, StatusFrete.SAIDA_CONFIRMADA.getCodigo());
            ps.setString(3, "SAÍDA CONFIRMADA");
            ps.setString(4, StatusFrete.EM_TRANSITO.getCodigo());
            ps.setString(5, "EM TRÂNSITO");
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
            return possuiFreteVinculado(id, SQL_FRETE_VINCULADO);
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) {
                return false;
            }
            if ("42703".equals(e.getSQLState())) {
                return possuiFreteVinculado(id, SQL_FRETE_VINCULADO_ALT);
            }
            throw e;
        }
    }

    private boolean possuiFreteVinculado(int id, String sql) throws SQLException {
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

    private void preencherParametros(PreparedStatement ps, Veiculo v) throws SQLException {
        ps.setString(1, v.getPlaca());
        ps.setString(2, v.getRntrc());
        if (v.getAnoFabricacao() != null) {
            ps.setInt(3, v.getAnoFabricacao());
        } else {
            ps.setNull(3, java.sql.Types.INTEGER);
        }
        ps.setString(4, v.getTipo());
        setDouble(ps, 5, v.getTaraKg());
        setDouble(ps, 6, v.getCapacidadeKg());
        setDouble(ps, 7, v.getVolumeM3());
        ps.setString(8, v.getStatus());
    }

    private void setDouble(PreparedStatement ps, int index, Double valor) throws SQLException {
        if (valor != null) {
            ps.setDouble(index, valor);
        } else {
            ps.setNull(index, java.sql.Types.DOUBLE);
        }
    }

    private Veiculo mapear(ResultSet rs) throws SQLException {
        Veiculo v = new Veiculo();
        v.setId(rs.getInt("id"));
        v.setPlaca(rs.getString("placa"));
        v.setRntrc(rs.getString("rntrc"));
        v.setAnoFabricacao(rs.getObject("ano_fabricacao") == null ? null : rs.getInt("ano_fabricacao"));
        v.setTipo(rs.getString("tipo"));
        v.setTaraKg(rs.getObject("tara_kg") == null ? null : rs.getDouble("tara_kg"));
        v.setCapacidadeKg(rs.getObject("capacidade_kg") == null ? null : rs.getDouble("capacidade_kg"));
        v.setVolumeM3(rs.getObject("volume_m3") == null ? null : rs.getDouble("volume_m3"));
        v.setStatus(rs.getString("status"));
        return v;
    }
}
