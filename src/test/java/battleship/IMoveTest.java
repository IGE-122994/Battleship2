package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test class for IMove.
 * Author: Francisco Silva
 * Date: 2026-04-27
 * Cyclomatic Complexity:
 * - readMove(): 2
 * - toString(): 1
 * - getNumber(): 1
 * - getShots(): 1
 * - getShotResults(): 1
 * - processEnemyFire(): 8
 */
class IMoveTest {

    private IMove move;

    @BeforeEach
    void setUp() {
        move = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), new ArrayList<>());
    }

    @AfterEach
    void tearDown() {
        move = null;
    }

    // ===================== readMove - CC=2 =====================

    @Test
    void readMove1() {
        // Path: numShots > 0 → lê posições e cria move
        Scanner sc = new Scanner("3 0 0 0 1 0 2");
        Move result = IMove.readMove(1, sc);
        assertAll(
                () -> assertEquals(1, result.getNumber(), "Error: expected move number 1"),
                () -> assertEquals(3, result.getShots().size(), "Error: expected 3 shots"),
                () -> assertTrue(result.getShotResults().isEmpty(), "Error: expected empty shot results"),
                () -> assertEquals(0, result.getShots().get(0).getRow(), "Error: expected row 0 for first shot"),
                () -> assertEquals(0, result.getShots().get(0).getColumn(), "Error: expected column 0 for first shot"),
                () -> assertEquals(0, result.getShots().get(1).getRow(), "Error: expected row 0 for second shot"),
                () -> assertEquals(1, result.getShots().get(1).getColumn(), "Error: expected column 1 for second shot"),
                () -> assertEquals(0, result.getShots().get(2).getRow(), "Error: expected row 0 for third shot"),
                () -> assertEquals(2, result.getShots().get(2).getColumn(), "Error: expected column 2 for third shot")
        );
    }

    @Test
    void readMove2() {
        // Path: numShots = 0 → cria move vazio
        Scanner sc = new Scanner("0");
        Move result = IMove.readMove(1, sc);
        assertAll(
                () -> assertEquals(1, result.getNumber(), "Error: expected move number 1"),
                () -> assertEquals(0, result.getShots().size(), "Error: expected 0 shots"),
                () -> assertTrue(result.getShotResults().isEmpty(), "Error: expected empty shot results")
        );
    }

    @Test
    void readMove3() {
        // Path: número de move diferente
        Scanner sc = new Scanner("1 0 0");
        Move result = IMove.readMove(5, sc);
        assertEquals(5, result.getNumber(), "Error: expected move number 5");
    }

    // ===================== toString - CC=1 =====================

    @Test
    void toString1() {
        assertNotNull(move.toString(), "Error: expected non-null toString result");
    }

    // ===================== getNumber - CC=1 =====================

    @Test
    void getNumber() {
        assertEquals(1, move.getNumber(), "Error: expected move number 1");
    }

    // ===================== getShots - CC=1 =====================

    @Test
    void getShots() {
        assertEquals(3, move.getShots().size(), "Error: expected 3 shots");
    }

    // ===================== getShotResults - CC=1 =====================

    @Test
    void getShotResults() {
        List<IGame.ShotResult> results = List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, true, null, false)
        );
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), results);
        assertEquals(3, m.getShotResults().size(), "Error: expected 3 shot results");
    }

    // ===================== processEnemyFire - CC=8 =====================

    @Test
    void processEnemyFire1() {
        // Path: verbose false, tiro inválido (outside)
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(false),
                "Error: expected no exception with outside shots verbose=false");
    }

    @Test
    void processEnemyFire2() {
        // Path: verbose false, tiro repetido
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(false),
                "Error: expected no exception with repeated shots verbose=false");
    }

    @Test
    void processEnemyFire3() {
        // Path: verbose false, tiro na água
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(false),
                "Error: expected no exception with water shots verbose=false");
    }

    @Test
    void processEnemyFire4() {
        // Path: verbose false, acerto em navio não afundado
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(true, false, ship, false),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(false),
                "Error: expected no exception with hit on ship verbose=false");
    }

    @Test
    void processEnemyFire5() {
        // Path: verbose false, navio afundado
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(true, false, ship, true),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(false),
                "Error: expected no exception with sunk ship verbose=false");
    }

    @Test
    void processEnemyFire6() {
        // Path: verbose true, tiro inválido
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false),
                new IGame.ShotResult(false, false, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(true),
                "Error: expected no exception with outside shots verbose=true");
    }

    @Test
    void processEnemyFire7() {
        // Path: verbose true, tiro repetido
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false),
                new IGame.ShotResult(true, true, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(true),
                "Error: expected no exception with repeated shots verbose=true");
    }

    @Test
    void processEnemyFire8() {
        // Path: verbose true, navio afundado
        IShip ship = new Barge(Compass.NORTH, new Position('A', 1));
        IMove m = new Move(1, List.of(
                new Position('A', 1),
                new Position('A', 2),
                new Position('A', 3)
        ), List.of(
                new IGame.ShotResult(true, false, ship, true),
                new IGame.ShotResult(true, false, null, false),
                new IGame.ShotResult(true, false, null, false)
        ));
        assertDoesNotThrow(() -> m.processEnemyFire(true),
                "Error: expected no exception with sunk ship verbose=true");
    }
}