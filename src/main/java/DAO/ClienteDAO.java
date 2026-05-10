package DAO;

import Entity.Cliente;
import Entity.StatusFrete;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.sql.ResultSet;


public class ClienteDAO {

    private static final String SQL_INSERT =
        "INSERT INTO cliente " +
        "(razao_social, nome_fantasia, cnpj, inscricao_estadual, tipo, " +
        " logradouro, numero, complemento, bairro, municipio, uf, cep, " +
        " telefone, email, status) " +
        "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

    private static final String SQL_UPDATE =
        "UPDATE cliente SET " +
        "razao_social = ?, nome_fantasia = ?, cnpj = ?, inscricao_estadual = ?, tipo = ?, " +
        "logradouro = ?, numero = ?, complemento = ?, bairro = ?, municipio = ?, uf = ?, cep = ?, " +
        "telefone = ?, email = ?, status = ? " +
        "WHERE id = ?";

    private static final String SQL_DELETE =
        "DELETE FROM cliente WHERE id = ?";

    private static final String SQL_EXISTE_FRETE_VINCULADO =
        "SELECT 1 FROM frete " +
        "WHERE (id_remetente = ? OR id_destinatario = ?) " +
        "AND UPPER(REPLACE(TRANSLATE(COALESCE(status, ''), 'ÁÀÃÂÉÊÍÓÔÕÚÇ', 'AAAAEEIOOOUC'), ' ', '_')) NOT IN (?, ?, ?) " +
        "LIMIT 1";

    private static final String SQL_EXISTE_FRETE_VINCULADO_ALT =
        "SELECT 1 FROM frete " +
        "WHERE (remetente_id = ? OR destinatario_id = ?) " +
        "AND UPPER(REPLACE(TRANSLATE(COALESCE(status, ''), 'ÁÀÃÂÉÊÍÓÔÕÚÇ', 'AAAAEEIOOOUC'), ' ', '_')) NOT IN (?, ?, ?) " +
        "LIMIT 1";

    private static final String SQL_LISTAR  = 
        "SELECT id, razao_social, nome_fantasia, cnpj, inscricao_estadual, tipo, " +
        "logradouro, numero, complemento, bairro, municipio, uf, cep, telefone, email, status " +
        "FROM cliente " +
        "WHERE razao_social ILIKE ? OR nome_fantasia ILIKE ? OR cnpj ILIKE ? " +
        "ORDER BY id DESC";

    private static final String SQL_LISTAR_PAGINADO  =
        SQL_LISTAR + " LIMIT ? OFFSET ?";

    public void inserir(Cliente C) throws SQLException {
        Connection        conn = null;
        PreparedStatement prepSt = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false); 

            prepSt = conn.prepareStatement(SQL_INSERT);

            prepSt.setString(1,  C.getRazaoSocial());
            prepSt.setString(2,  C.getNomeFantasia());
            prepSt.setString(3,  C.getCnpj());
            prepSt.setString(4,  C.getInscricaoEstadual());
            prepSt.setString(5,  C.getTipo());
            prepSt.setString(6,  C.getLogradouro());
           
            if (C.getNumero() != null) {
                prepSt.setInt(7, C.getNumero());
            } else {
                prepSt.setNull(7, java.sql.Types.INTEGER);
            }

            prepSt.setString(8,  C.getComplemento());
            prepSt.setString(9,  C.getBairro());
            prepSt.setString(10, C.getMunicipio());
            prepSt.setString(11, C.getUf());
            prepSt.setString(12, C.getCep());
            prepSt.setString(13, C.getTelefone());
            prepSt.setString(14, C.getEmail());
            prepSt.setString(15, C.getStatus());

            prepSt.executeUpdate();
            conn.commit();

        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e; 

        } finally {
            if (prepSt != null) {
                try { prepSt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public void atualizar(Cliente C) throws SQLException {
        Connection conn = null;
        PreparedStatement prepSt = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);

            prepSt = conn.prepareStatement(SQL_UPDATE);

            prepSt.setString(1, C.getRazaoSocial());
            prepSt.setString(2, C.getNomeFantasia());
            prepSt.setString(3, C.getCnpj());
            prepSt.setString(4, C.getInscricaoEstadual());
            prepSt.setString(5, C.getTipo());
            prepSt.setString(6, C.getLogradouro());

            if (C.getNumero() != null) {
                prepSt.setInt(7, C.getNumero());
            } else {
                prepSt.setNull(7, java.sql.Types.INTEGER);
            }

            prepSt.setString(8, C.getComplemento());
            prepSt.setString(9, C.getBairro());
            prepSt.setString(10, C.getMunicipio());
            prepSt.setString(11, C.getUf());
            prepSt.setString(12, C.getCep());
            prepSt.setString(13, C.getTelefone());
            prepSt.setString(14, C.getEmail());
            prepSt.setString(15, C.getStatus());
            prepSt.setInt(16, C.getId());

            prepSt.executeUpdate();
            conn.commit();
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (prepSt != null) {
                try { prepSt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public boolean existeFreteVinculado(int id) throws SQLException {
        try {
            return existeFreteVinculado(id, SQL_EXISTE_FRETE_VINCULADO);
        } catch (SQLException e) {
            if ("42P01".equals(e.getSQLState())) {
                return false;
            }
            if ("42703".equals(e.getSQLState())) {
                return existeFreteVinculado(id, SQL_EXISTE_FRETE_VINCULADO_ALT);
            }
            throw e;
        }
    }

    private boolean existeFreteVinculado(int id, String sql) throws SQLException {
        Connection conn = null;
        PreparedStatement prepSt = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            prepSt = conn.prepareStatement(sql);
            prepSt.setInt(1, id);
            prepSt.setInt(2, id);
            prepSt.setString(3, StatusFrete.ENTREGUE.getCodigo());
            prepSt.setString(4, StatusFrete.CANCELADO.getCodigo());
            prepSt.setString(5, StatusFrete.NAO_ENTREGUE.getCodigo());
            rs = prepSt.executeQuery();
            return rs.next();
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (prepSt != null) try { prepSt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
    }

    public boolean excluir(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement prepSt = null;
        try {
            conn = Conexao.getConexao();
            conn.setAutoCommit(false);

            prepSt = conn.prepareStatement(SQL_DELETE);
            prepSt.setInt(1, id);
            int linhasAfetadas = prepSt.executeUpdate();

            conn.commit();
            return linhasAfetadas > 0;
        } catch (SQLException e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) { ex.printStackTrace(); }
            }
            throw e;
        } finally {
            if (prepSt != null) {
                try { prepSt.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
            if (conn != null) {
                try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
            }
        }
    }

    public List<Cliente> listar(String filtro) throws SQLException {
        return listar(filtro, 0, 0);
    }

    public List<Cliente> listar(String filtro, int limite, int offset) throws SQLException {
        List<Cliente> clientes = new ArrayList<>();
        Connection conn = null;
        PreparedStatement prepSt = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            prepSt = conn.prepareStatement(limite > 0 ? SQL_LISTAR_PAGINADO : SQL_LISTAR);
            String likeFiltro = "%" + (filtro == null ? "" : filtro.trim()) + "%";
            prepSt.setString(1, likeFiltro);
            prepSt.setString(2, likeFiltro);
            prepSt.setString(3, likeFiltro);
            if (limite > 0) {
                prepSt.setInt(4, limite);
                prepSt.setInt(5, Math.max(0, offset));
            }
            rs = prepSt.executeQuery();
            while (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setRazaoSocial(rs.getString("razao_social"));
                c.setNomeFantasia(rs.getString("nome_fantasia"));
                c.setCnpj(rs.getString("cnpj"));
                c.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                c.setTipo(rs.getString("tipo"));
                c.setLogradouro(rs.getString("logradouro"));
                c.setNumero(rs.getObject("numero") != null ? rs.getInt("numero") : null);
                c.setComplemento(rs.getString("complemento"));
                c.setBairro(rs.getString("bairro"));
                c.setMunicipio(rs.getString("municipio"));
                c.setUf(rs.getString("uf"));
                c.setCep(rs.getString("cep"));
                c.setTelefone(rs.getString("telefone"));
                c.setEmail(rs.getString("email"));
                c.setStatus(rs.getString("status"));
                clientes.add(c);
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (prepSt != null) try { prepSt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return clientes;
    }

    public Cliente obterPorId(int id) throws SQLException {
        Connection conn = null;
        PreparedStatement prepSt = null;
        ResultSet rs = null;
        try {
            conn = Conexao.getConexao();
            String sql = "SELECT id, razao_social, nome_fantasia, cnpj, inscricao_estadual, tipo, " +
                    "logradouro, numero, complemento, bairro, municipio, uf, cep, telefone, email, status " +
                    "FROM cliente WHERE id = ?";
            prepSt = conn.prepareStatement(sql);
            prepSt.setInt(1, id);
            rs = prepSt.executeQuery();
            
            if (rs.next()) {
                Cliente c = new Cliente();
                c.setId(rs.getInt("id"));
                c.setRazaoSocial(rs.getString("razao_social"));
                c.setNomeFantasia(rs.getString("nome_fantasia"));
                c.setCnpj(rs.getString("cnpj"));
                c.setInscricaoEstadual(rs.getString("inscricao_estadual"));
                c.setTipo(rs.getString("tipo"));
                c.setLogradouro(rs.getString("logradouro"));
                c.setNumero(rs.getObject("numero") != null ? rs.getInt("numero") : null);
                c.setComplemento(rs.getString("complemento"));
                c.setBairro(rs.getString("bairro"));
                c.setMunicipio(rs.getString("municipio"));
                c.setUf(rs.getString("uf"));
                c.setCep(rs.getString("cep"));
                c.setTelefone(rs.getString("telefone"));
                c.setEmail(rs.getString("email"));
                c.setStatus(rs.getString("status"));
                return c;
            }
        } finally {
            if (rs != null) try { rs.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (prepSt != null) try { prepSt.close(); } catch (SQLException e) { e.printStackTrace(); }
            if (conn != null) try { conn.close(); } catch (SQLException e) { e.printStackTrace(); }
        }
        return null;
    }
}
