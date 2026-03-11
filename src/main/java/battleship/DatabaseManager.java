package battleship;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.Statement;
import java.sql.SQLException;
public class DatabaseManager {
    // O caminho para o ficheiro da base de dados (será criado automaticamente na raiz do projeto)
    private static final String URL = "jdbc:sqlite:battleship_history.db";

    /**
     * Liga à base de dados e cria a tabela se ela ainda não existir.
     */

    public static void initializeDatabase() {
        // Instrução SQL para criar a tabela com 3 colunas: ID, Vencedor e Total de Tiros
        String sql = "CREATE TABLE IF NOT EXISTS game_history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "winner TEXT NOT NULL, " +
                "total_shots INTEGER NOT NULL" +
                ");";

        try (Connection conn = DriverManager.getConnection(URL);
             Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            System.out.println("[Base de Dados] Tabela de histórico inicializada com sucesso!");

        } catch (SQLException e) {
            System.out.println("[Erro Base de Dados] Falha ao criar a tabela: " + e.getMessage());
        }
    }

    /**
     * Guarda o resultado final de uma partida na base de dados.
     */
    public static void saveGameResult(String winner, int totalShots) {
        // Instrução SQL para inserir uma nova linha (?) que protege contra "SQL Injection"
        String sql = "INSERT INTO game_history(winner, total_shots) VALUES(?, ?)";

        try (Connection conn = DriverManager.getConnection(URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, winner);
            pstmt.setInt(2, totalShots);
            pstmt.executeUpdate();

            System.out.println("[Base de Dados] Resultado guardado! (" + winner + " venceu com " + totalShots + " tiros)");

        } catch (SQLException e) {
            System.out.println("[Erro Base de Dados] Falha ao guardar o resultado: " + e.getMessage());
        }
    }
}
