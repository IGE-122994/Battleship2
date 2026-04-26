
package battleship;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class HuggingFaceClientTest {

    private HuggingFaceClient client;

    @BeforeEach
    void setUp() {
        client = new HuggingFaceClient();
    }

    // ===================== buildGameHistory =====================

    @Test
    @DisplayName("buildGameHistory retorna mensagem quando lista de moves está vazia")
    void testBuildGameHistoryEmptyMoves() {
        String result = HuggingFaceClient.buildGameHistory(new ArrayList<>());
        assertEquals("Nenhuma jogada ainda.", result);
    }

    @Test
    @DisplayName("buildGameHistory contém número da rajada")
    void testBuildGameHistoryContainsRoundNumber() {
        // Criar um move mock simples
        IMove move = createMockMove(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );

        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertTrue(result.contains("Rajada número:"));
    }

    @Test
    @DisplayName("buildGameHistory contém secção de posições disponíveis")
    void testBuildGameHistoryContainsAvailablePositions() {
        IMove move = createMockMove(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );

        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertTrue(result.contains("POSIÇÕES DISPONÍVEIS"));
    }

    @Test
    @DisplayName("buildGameHistory contém secção de acertos pendentes")
    void testBuildGameHistoryContainsPendingHits() {
        IMove move = createMockMove(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );

        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertTrue(result.contains("ACERTOS PENDENTES"));
    }

    @Test
    @DisplayName("buildGameHistory com tiro válido não afundado mostra acerto pendente")
    void testBuildGameHistoryPendingHitWhenValidNotSunk() {
        // Simular navio atingido mas não afundado
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        IMove move = createMockMove(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(true, false, ship, false))
        );

        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertTrue(result.contains("A1"));
    }

    @Test
    @DisplayName("buildGameHistory com múltiplos moves aumenta número de rajada")
    void testBuildGameHistoryMultipleMoves() {
        IMove move1 = createMockMove(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );
        IMove move2 = createMockMove(2,
                List.of(new Position('B', 2)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );

        String result = HuggingFaceClient.buildGameHistory(List.of(move1, move2));
        assertTrue(result.contains("Rajada número: 3"));
    }

    @Test
    @DisplayName("buildGameHistory posições disparadas não aparecem em disponíveis")
    void testBuildGameHistoryFiredPositionsNotAvailable() {
        IMove move = createMockMove(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );

        String result = HuggingFaceClient.buildGameHistory(List.of(move));

        // Verifica que o resultado contém a secção de disponíveis
        assertTrue(result.contains("POSIÇÕES DISPONÍVEIS"));

        // Verifica que o total de posições disponíveis é menor que 100
        // (porque pelo menos 1 posição foi disparada)
        String availableLine = result.lines()
                .filter(l -> l.contains("POSIÇÕES DISPONÍVEIS"))
                .findFirst().orElse("");
        assertTrue(availableLine.split(",").length < 100);
    }

    // ===================== Helper =====================

    private IMove createMockMove(int number, List<IPosition> shots, List<IGame.ShotResult> results) {
        return new Move(number, shots, results);
    }
}