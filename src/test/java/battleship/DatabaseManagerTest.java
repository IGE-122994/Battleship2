package battleship;

import org.junit.jupiter.api.*;
import java.io.File;
import java.sql.*;
import static org.junit.jupiter.api.Assertions.*;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseManagerTest {

    private static final String DB_FILE = "battleship_history.db";

    @BeforeAll
    static void setUp() {
        // Opcional: Apagar a base de dados antiga para começar do zero nos testes
        File dbFile = new File(DB_FILE);
        if (dbFile.exists()) {
            dbFile.delete();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Deve inicializar a base de dados e criar a tabela")
    void testInitializeDatabase() {
        // Executa a inicialização
        assertDoesNotThrow(DatabaseManager::initializeDatabase,
                "A inicialização não deve lançar exceções");

        // Verifica se o ficheiro foi criado
        File dbFile = new File(DB_FILE);
        assertTrue(dbFile.exists(), "O ficheiro da base de dados deve existir");

        // Verifica se a tabela existe mesmo lá dentro
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
             ResultSet rs = conn.getMetaData().getTables(null, null, "game_history", null)) {

            assertTrue(rs.next(), "A tabela 'game_history' deve ter sido criada");

        } catch (SQLException e) {
            fail("Erro ao verificar a tabela: " + e.getMessage());
        }
    }

    @Test
    @Order(2)
    @DisplayName("Deve guardar um resultado de jogo com sucesso")
    void testSaveGameResult() {
        String winner = "Player Test";
        int totalShots = 42;

        // Tenta guardar
        assertDoesNotThrow(() -> DatabaseManager.saveGameResult(winner, totalShots),
                "Guardar o resultado não deve lançar exceções");

        // Verifica se o dado foi realmente inserido
        String sql = "SELECT winner, total_shots FROM game_history WHERE winner = ?";
        try (Connection conn = DriverManager.getConnection("jdbc:sqlite:" + DB_FILE);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, winner);
            ResultSet rs = pstmt.executeQuery();

            assertTrue(rs.next(), "Deve encontrar um registo para o vencedor");
            assertEquals(totalShots, rs.getInt("total_shots"), "O número de tiros deve coincidir");

        } catch (SQLException e) {
            fail("Erro ao verificar o registo guardado: " + e.getMessage());
        }
    }
}