package DAO;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;


public class Conexao {

    private static final String URL = obterUrl();
    private static final String USUARIO = obter("DB_USER", "postgres");
    private static final String SENHA = obter("DB_PASSWORD", "1234");

    static {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException("Driver PostgreSQL não encontrado.", e);
        }
    }

    /**
        
    
     * @return 
     * @throws SQLException
     */
    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }

    private static String obterUrl() {
        String url = obter("DB_URL", "");
        if (!url.isEmpty()) {
            return url;
        }
        String host = obter("DB_HOST", "localhost");
        String porta = obter("DB_PORT", "5432");
        String banco = obter("DB_NAME", "postgres");
        return "jdbc:postgresql://" + host + ":" + porta + "/" + banco;
    }

    private static String obter(String chave, String valorPadrao) {
        String valorSistema = System.getProperty(chave);
        if (valorSistema != null && !valorSistema.trim().isEmpty()) {
            return valorSistema.trim();
        }
        String valorAmbiente = System.getenv(chave);
        if (valorAmbiente != null && !valorAmbiente.trim().isEmpty()) {
            return valorAmbiente.trim();
        }
        return valorPadrao;
    }
}
