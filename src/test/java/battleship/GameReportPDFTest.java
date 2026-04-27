package battleship;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/**
 * Test class for GameReportPDF.
 * Author: Francisco Silva
 * Date: 2026-04-27
 * Cyclomatic Complexity:
 * - generate(): 2
 * - addTitle(): 1
 * - addStatistics(): 1
 * - addMovesSummary(): 2
 * - addFinalBoard(): 6
 * - addTableRow(): 1
 * - addTableHeader(): 1
 * - cell(): 1
 * - summariseMove(): 9
 * - addFooter(): 1
 */
class GameReportPDFTest {

    private Game game;
    private Fleet fleet;

    @BeforeEach
    void setUp() {
        fleet = new Fleet();
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 1)));
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 3)));
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 5)));
        fleet.addShip(new Barge(Compass.NORTH, new Position('A', 7)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position('C', 1)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position('C', 4)));
        fleet.addShip(new Caravel(Compass.NORTH, new Position('C', 7)));
        fleet.addShip(new Frigate(Compass.NORTH, new Position('E', 1)));
        fleet.addShip(new Carrack(Compass.NORTH, new Position('E', 4)));
        fleet.addShip(new Carrack(Compass.NORTH, new Position('E', 7)));
        fleet.addShip(new Galleon(Compass.NORTH, new Position('G', 1)));
        game = new Game(fleet);
    }

    @AfterEach
    void tearDown() {
        game = null;
        fleet = null;
    }

    // ===================== generate - CC=2 =====================

    @Test
    void generate1() {
        // Path: geração bem sucedida → retorna caminho com prefixo correto
        String path = GameReportPDF.generate(game, "1m 30s 500ms");
        assertAll(
                () -> assertNotNull(path, "Error: expected non-null path"),
                () -> assertTrue(path.endsWith(".pdf"), "Error: expected path ending with .pdf"),
                () -> assertTrue(path.startsWith("data/relatorio_"), "Error: expected path starting with 'data/relatorio_'"),
                () -> assertTrue(new File(path).exists(), "Error: expected PDF file to exist on disk")
        );
        new File(path).delete();
    }

    @Test
    void generate2() throws Exception {
        // Path: erro de IO → retorna caminho mesmo sem criar ficheiro
        File dataDir = new File("data");
        if (dataDir.exists() && dataDir.isDirectory()) {
            for (File f : dataDir.listFiles()) f.delete();
            dataDir.delete();
        }
        File dataFile = new File("data");
        dataFile.createNewFile();
        try {
            String path = GameReportPDF.generate(game, "0m 5s 0ms");
            assertNotNull(path, "Error: expected non-null path even on IO error");
        } finally {
            dataFile.delete();
        }
    }

    // ===================== addMovesSummary - CC=2 =====================

    @Test
    void addMovesSummary1() {
        // Path: lista de moves vazia → "Nenhuma jogada registada."
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 0s 0ms"),
                "Error: expected no exception with empty moves list");
    }

    @Test
    void addMovesSummary2() {
        // Path: lista de moves não vazia → tabela com jogadas
        List<IPosition> shots = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with non-empty moves list");
    }

    // ===================== summariseMove - CC=9 =====================

    @Test
    void summariseMove1() {
        // Path: tiro fora do tabuleiro → outside++
        List<IPosition> shots = List.of(
                new Position(-1, 0),
                new Position(-1, 1),
                new Position(-1, 2)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with outside shots");
    }

    @Test
    void summariseMove2() {
        // Path: tiro repetido → repeated++
        List<IPosition> shots1 = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots1);
        List<IPosition> shots2 = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots2);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 10s 0ms"),
                "Error: expected no exception with repeated shots");
    }

    @Test
    void summariseMove3() {
        // Path: tiro na água → misses++
        List<IPosition> shots = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with water shots");
    }

    @Test
    void summariseMove4() {
        // Path: acerto em navio não afundado → hits++
        List<IShip> ships = game.getMyFleet().getShips();
        assumeTrue(!ships.isEmpty(), "Frota sem navios — ignorar teste");
        IPosition shipPos = ships.get(0).getPositions().get(0);
        List<IPosition> shots = List.of(
                shipPos,
                new Position('B', 8),
                new Position('B', 9)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with hit on ship");
    }

    @Test
    void summariseMove5() {
        // Path: navio afundado → hits++ e sunk++
        List<IShip> ships = game.getMyFleet().getShips();
        assumeTrue(!ships.isEmpty(), "Frota sem navios — ignorar teste");
        List<IPosition> shots = List.of(
                new Position('A', 1),
                new Position('B', 8),
                new Position('B', 9)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with sunk ship");
    }

    @Test
    void summariseMove6() {
        // Path: resultado vazio → retorna "-"
        Move move = new Move(1, List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        ), List.of());
        game.getAlienMoves().add(move);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with empty shot results");
    }

    @Test
    void summariseMove7() {
        // Path: múltiplos moves → tabela completa
        List<IPosition> shots1 = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots1);
        List<IPosition> shots2 = List.of(
                new Position('B', 4),
                new Position('B', 5),
                new Position('B', 6)
        );
        game.fireShots(shots2);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "1m 0s 0ms"),
                "Error: expected no exception with multiple moves");
    }

    // ===================== addFinalBoard - CC=6 =====================

    @Test
    void addFinalBoard1() {
        // Path: tabuleiro vazio sem moves
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 0s 0ms"),
                "Error: expected no exception with empty board");
    }

    @Test
    void addFinalBoard2() {
        // Path: posição com navio marcada como '#'
        List<IShip> ships = game.getMyFleet().getShips();
        assumeTrue(!ships.isEmpty(), "Frota sem navios — ignorar teste");
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 0s 0ms"),
                "Error: expected no exception with ships on board");
    }

    @Test
    void addFinalBoard3() {
        // Path: tiro acerta navio → marcado como '*'
        List<IShip> ships = game.getMyFleet().getShips();
        assumeTrue(!ships.isEmpty(), "Frota sem navios — ignorar teste");
        IPosition shipPos = ships.get(0).getPositions().get(0);
        List<IPosition> shots = List.of(
                shipPos,
                new Position('B', 8),
                new Position('B', 9)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with hit marker on board");
    }

    @Test
    void addFinalBoard4() {
        // Path: tiro na água → marcado como 'o'
        List<IPosition> shots = List.of(
                new Position('B', 1),
                new Position('B', 2),
                new Position('B', 3)
        );
        game.fireShots(shots);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with water marker on board");
    }

    @Test
    void addFinalBoard5() {
        // Path: navio afundado → adjacentes marcados como '-'
        List<IPosition> shots1 = List.of(
                new Position('A', 1),
                new Position('J', 8),
                new Position('J', 9)
        );
        game.fireShots(shots1);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 5s 0ms"),
                "Error: expected no exception with adjacent marker on board");
    }

    @Test
    void addFinalBoard6() {
        // Path: tiro em posição adjacente '-' → marcado como 'o'
        List<IPosition> shots1 = List.of(
                new Position('A', 1),
                new Position('J', 8),
                new Position('J', 9)
        );
        game.fireShots(shots1);
        List<IPosition> shots2 = List.of(
                new Position('A', 2),
                new Position('J', 6),
                new Position('J', 7)
        );
        game.fireShots(shots2);
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "0m 10s 0ms"),
                "Error: expected no exception with shot on adjacent position");
    }

    @Test
    void addFinalBoard7() {
        // Path: todos os navios afundados → board completo
        IFleet myFleet = game.getMyFleet();
        assumeTrue(!myFleet.getShips().isEmpty(), "Frota sem navios — ignorar teste");
        for (IShip ship : myFleet.getShips()) {
            for (IPosition pos : ship.getPositions()) {
                try {
                    List<IPosition> shots = List.of(
                            pos,
                            new Position('J', 8),
                            new Position('J', 9)
                    );
                    game.fireShots(shots);
                } catch (Exception e) {
                    // ignorar duplicados
                }
            }
        }
        assertDoesNotThrow(() -> GameReportPDF.generate(game, "5m 0s 0ms"),
                "Error: expected no exception with all ships sunk");
    }
}