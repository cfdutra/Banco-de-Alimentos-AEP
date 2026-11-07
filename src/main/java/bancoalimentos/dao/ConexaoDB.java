package bancoalimentos.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Abre a conexão com o MySQL. Os valores padrão servem para o XAMPP/WAMP
 * (usuário root, sem senha). Para mudar, defina as variáveis de ambiente
 * DB_URL, DB_USER e DB_PASSWORD.
 */
public final class ConexaoDB {

    private static final String URL = System.getenv().getOrDefault("DB_URL",
            "jdbc:mysql://localhost:3306/banco_alimentos?useSSL=false&allowPublicKeyRetrieval=true");
    private static final String USUARIO = System.getenv().getOrDefault("DB_USER", "root");
    private static final String SENHA = System.getenv().getOrDefault("DB_PASSWORD", "");

    private ConexaoDB() {
    }

    public static Connection getConexao() throws SQLException {
        return DriverManager.getConnection(URL, USUARIO, SENHA);
    }
}
