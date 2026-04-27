package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for HuggingFaceClient.
 * Author: Francisco Silva
 * Date: 2026-04-27
 * Cyclomatic Complexity:
 * - HuggingFaceClient(): 1
 * - initialize(): 1
 * - getNextMove(): 2
 * - sendMessage(): 3
 * - parseShots(): 2
 * - buildGameHistory(): 8
 */
class HuggingFaceClientTest {

    private HuggingFaceClient client;

    @BeforeEach
    void setUp() {
        client = new HuggingFaceClient();
    }

    @AfterEach
    void tearDown() {
        client = null;
    }

    // ===================== HuggingFaceClient() - CC=1 =====================

    @Test
    void huggingFaceClient() {
        assertNotNull(client, "Error: HuggingFaceClient instance should not be null.");
    }

    // ===================== buildGameHistory() - CC=8 =====================

    @Test
    void buildGameHistory1() {
        // Path: moves.isEmpty() == true
        String result = HuggingFaceClient.buildGameHistory(new ArrayList<>());
        assertEquals("Nenhuma jogada ainda.", result,
                "Error: expected 'Nenhuma jogada ainda.' but got different result.");
    }

    @Test
    void buildGameHistory2() {
        // Path: moves não vazia, tiro inválido (not valid, not sunk)
        IMove move = new Move(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );
        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertAll(
                () -> assertTrue(result.contains("Rajada número:"),
                        "Error: result should contain 'Rajada número:'."),
                () -> assertTrue(result.contains("POSIÇÕES DISPONÍVEIS"),
                        "Error: result should contain 'POSIÇÕES DISPONÍVEIS'."),
                () -> assertTrue(result.contains("ACERTOS PENDENTES"),
                        "Error: result should contain 'ACERTOS PENDENTES'.")
        );
    }

    @Test
    void buildGameHistory3() {
        // Path: tiro válido, não repetido, navio atingido mas não afundado
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        IMove move = new Move(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(true, false, ship, false))
        );
        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertTrue(result.contains("ACERTOS PENDENTES (foca aqui PRIMEIRO): A1"),
                "Error: result should contain pending hit 'A1'.");
    }

    @Test
    void buildGameHistory4() {
        // Path: tiro válido, navio afundado — remove dos pendentes e calcula halo
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        IMove move = new Move(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(true, false, ship, true))
        );
        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertAll(
                () -> assertTrue(result.contains("POSIÇÕES DISPONÍVEIS"),
                        "Error: result should contain 'POSIÇÕES DISPONÍVEIS'."),
                () -> assertTrue(result.contains("ACERTOS PENDENTES (foca aqui PRIMEIRO): nenhum"),
                        "Error: result should show no pending hits after sinking.")
        );
    }

    @Test
    void buildGameHistory5() {
        // Path: halo calculado — posições adjacentes ao navio afundado não aparecem em disponíveis
        IShip ship = new Barge(Compass.NORTH, new Position('E', 5));
        IMove move = new Move(1,
                List.of(new Position('E', 5)),
                List.of(new IGame.ShotResult(true, false, ship, true))
        );
        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        String availableLine = result.lines()
                .filter(l -> l.contains("POSIÇÕES DISPONÍVEIS"))
                .findFirst().orElse("");
        assertFalse(availableLine.contains("D4"),
                "Error: D4 should not be available as it is in the halo of the sunk ship.");
    }

    @Test
    void buildGameHistory6() {
        // Path: múltiplos moves — número de rajada incrementa
        IMove move1 = new Move(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );
        IMove move2 = new Move(2,
                List.of(new Position('B', 2)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );
        String result = HuggingFaceClient.buildGameHistory(List.of(move1, move2));
        assertTrue(result.contains("Rajada número: 3"),
                "Error: expected 'Rajada número: 3' but got different result.");
    }

    @Test
    void buildGameHistory7() {
        // Path: posição disparada não aparece em disponíveis
        IMove move = new Move(1,
                List.of(new Position('A', 1)),
                List.of(new IGame.ShotResult(false, false, null, false))
        );
        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        String availableLine = result.lines()
                .filter(l -> l.contains("POSIÇÕES DISPONÍVEIS"))
                .findFirst().orElse("");
        assertTrue(availableLine.split(",").length < 100,
                "Error: available positions should be less than 100 after one shot.");
    }

    @Test
    void buildGameHistory8() {
        // Path: pendingHits não vazio — aparece na string resultado
        IShip ship = new Barge(Compass.NORTH, new Position('C', 3));
        IMove move = new Move(1,
                List.of(new Position('C', 3)),
                List.of(new IGame.ShotResult(true, false, ship, false))
        );
        String result = HuggingFaceClient.buildGameHistory(List.of(move));
        assertFalse(result.contains("ACERTOS PENDENTES (foca aqui PRIMEIRO): nenhum"),
                "Error: pending hits should not be 'nenhum' when there is a hit.");
    }
}