package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

class IMoveTest {

    // ===================== readMove =====================

    @Test
    @DisplayName("readMove cria move com número correto")
    void testReadMoveNumber() {
        Scanner sc = new Scanner("3 0 0 0 1 0 2");
        Move move = IMove.readMove(1, sc);
        assertEquals(1, move.getNumber());
    }

    @Test
    @DisplayName("readMove cria move com número correto de tiros")
    void testReadMoveShotsCount() {
        Scanner sc = new Scanner("3 0 0 0 1 0 2");
        Move move = IMove.readMove(1, sc);
        assertEquals(3, move.getShots().size());
    }

    @Test
    @DisplayName("readMove cria move com posições corretas")
    void testReadMoveShotsPositions() {
        Scanner sc = new Scanner("3 0 0 0 1 0 2");
        Move move = IMove.readMove(1, sc);
        assertEquals(0, move.getShots().get(0).getRow());
        assertEquals(0, move.getShots().get(0).getColumn());
        assertEquals(0, move.getShots().get(1).getRow());
        assertEquals(1, move.getShots().get(1).getColumn());
        assertEquals(0, move.getShots().get(2).getRow());
        assertEquals(2, move.getShots().get(2).getColumn());
    }

    @Test
    @DisplayName("readMove cria move com lista de resultados vazia")
    void testReadMoveShotResultsEmpty() {
        Scanner sc = new Scanner("3 0 0 0 1 0 2");
        Move move = IMove.readMove(1, sc);
        assertTrue(move.getShotResults().isEmpty());
    }

    @Test
    @DisplayName("readMove com zero tiros cria move vazio")
    void testReadMoveZeroShots() {
        Scanner sc = new Scanner("0");
        Move move = IMove.readMove(1, sc);
        assertEquals(0, move.getShots().size());
    }

    @Test
    @DisplayName("readMove com número de move diferente")
    void testReadMoveDifferentMoveNumber() {
        Scanner sc = new Scanner("1 0 0");
        Move move = IMove.readMove(5, sc);
        assertEquals(5, move.getNumber());
    }

    // ===================== Move via IMove =====================

    @Test
    @DisplayName("toString de Move não retorna null")
    void testMoveToStringNotNull() {
        IMove move = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), new ArrayList<>());
        assertNotNull(move.toString());
    }

    @Test
    @DisplayName("getNumber retorna número correto")
    void testGetNumber() {
        IMove move = new Move(3, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), new ArrayList<>());
        assertEquals(3, move.getNumber());
    }

    @Test
    @DisplayName("getShots retorna lista de tiros correta")
    void testGetShots() {
        List<IPosition> shots = List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        );
        IMove move = new Move(1, shots, new ArrayList<>());
        assertEquals(3, move.getShots().size());
    }

    @Test
    @DisplayName("getShotResults retorna lista de resultados correta")
    void testGetShotResults() {
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, true, null, false)
        );
        IMove move = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), results);
        assertEquals(3, move.getShotResults().size());
    }

    @Test
    @DisplayName("processEnemyFire com verbose false não lança exceção")
    void testProcessEnemyFireNotVerbose() {
        IMove move = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        ));
        assertDoesNotThrow(() -> move.processEnemyFire(false));
    }

    @Test
    @DisplayName("processEnemyFire com verbose true não lança exceção")
    void testProcessEnemyFireVerbose() {
        IMove move = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        ));
        assertDoesNotThrow(() -> move.processEnemyFire(true));
    }
}