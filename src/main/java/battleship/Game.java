package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.joda.time.Duration;

import java.util.*;

/**
 * Represents the Battleship game, managing player and enemy boards,
 * shots, ships, and game state.
 *
 * <p>This class provides methods to print boards, handle player and enemy shots,
 * generate random enemy shots, serialize positions to JSON, and track
 * game statistics such as valid shots, repeated shots, hits, and sunk ships.</p>
 */
public class Game implements IGame {

    //------------------------------------------------------------------
    /** Size of the game board (number of rows and columns). */
    public static final int BOARD_SIZE = 10;

    /** Number of shots per move. */
    public static final int NUMBER_SHOTS = 3;

    /** Character representing empty water on the board. */
    private static final char EMPTY_MARKER = '.';

    /** Character representing an intact ship on the board. */
    private static final char SHIP_MARKER = '#';

    /** Character representing a successful hit on a ship. */
    private static final char SHOT_SHIP_MARKER = '*';

    /** Character representing a shot that hit water. */
    private static final char SHOT_WATER_MARKER = 'o';

    /** Character representing a tile adjacent to a ship. */
    private static final char SHIP_ADJACENT_MARKER = '-';

    //------------------------------------------------------------------
    private final IFleet myFleet;
    private final List<IMove> alienMoves;

    private final IFleet alienFleet;
    private final List<IMove> myMoves;

    private Integer countInvalidShots;
    private Integer countRepeatedShots;
    private Integer countHits;
    private Integer countSinks;
    private int moveNumber;

    private GameTimer gameTimer = new GameTimer();
    private Duration previousAccumulated = Duration.ZERO;

    //------------------------------------------------------------------
    /**
     * Constructs a new game instance with the player's fleet.
     *
     * @param myFleet the fleet of the player containing the ships.
     */
    public Game(IFleet myFleet) {
        this.moveNumber = 1;
        this.alienMoves = new ArrayList<>();
        this.myMoves = new ArrayList<>();
        this.alienFleet = new Fleet();
        this.myFleet = myFleet;
        this.countInvalidShots = 0;
        this.countRepeatedShots = 0;
        this.countHits = 0;
        this.countSinks = 0;
        gameTimer.begin();
    }

    //------------------------------------------------------------------
    /**
     * Returns the player's fleet.
     *
     * @return the player's fleet.
     */
    @Override
    public IFleet getMyFleet() { return myFleet; }

    /**
     * Returns the list of moves made by the enemy.
     *
     * @return list of enemy moves.
     */
    @Override
    public List<IMove> getAlienMoves() { return alienMoves; }

    /**
     * Returns the enemy fleet.
     *
     * @return the enemy fleet.
     */
    @Override
    public IFleet getAlienFleet() { return alienFleet; }

    /**
     * Returns the list of moves made by the player.
     *
     * @return list of player moves.
     */
    @Override
    public List<IMove> getMyMoves() { return myMoves; }

    //------------------------------------------------------------------
    /**
     * Prints a representation of the game board to the console.
     *
     * @param fleet       the fleet to display on the board.
     * @param moves       list of moves that include shots to be displayed.
     * @param show_shots  if true, marks shots on the board.
     * @param showLegend  if true, displays a legend explaining symbols.
     */
    public static void printBoard(IFleet fleet, List<IMove> moves, boolean show_shots, boolean showLegend) {
        assert fleet != null;
        assert moves != null;

        char[][] map = new char[BOARD_SIZE][BOARD_SIZE];

        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                map[r][c] = EMPTY_MARKER;

        for (IShip ship : fleet.getShips()) {
            for (IPosition ship_pos : ship.getPositions())
                map[ship_pos.getRow()][ship_pos.getColumn()] = SHIP_MARKER;
            if (!ship.stillFloating())
                for (IPosition adjacent_pos : ship.getAdjacentPositions())
                    map[adjacent_pos.getRow()][adjacent_pos.getColumn()] = SHIP_ADJACENT_MARKER;
        }

        if (show_shots)
            for (IMove move : moves)
                for (IPosition shot : move.getShots()) {
                    if (shot.isInside()) {
                        int row = shot.getRow();
                        int col = shot.getColumn();
                        if (map[row][col] == SHIP_MARKER)
                            map[row][col] = SHOT_SHIP_MARKER;
                        if (map[row][col] == EMPTY_MARKER || map[row][col] == SHIP_ADJACENT_MARKER)
                            map[row][col] = SHOT_WATER_MARKER;
                    }
                }

        System.out.println();
        System.out.print("    ");
        for (int col = 0; col < BOARD_SIZE; col++) System.out.print(" " + (col + 1));
        System.out.println();

        System.out.print("   +-");
        for (int col = 0; col < BOARD_SIZE; col++) System.out.print("--");
        System.out.println("+");

        for (int row = 0; row < BOARD_SIZE; row++) {
            Position pos = new Position(row, 0);
            System.out.print(" " + pos.getClassicRow() + " |");
            for (int col = 0; col < BOARD_SIZE; col++)
                System.out.print(" " + map[row][col]);
            System.out.println(" |");
        }

        System.out.print("   +");
        for (int col = 0; col < BOARD_SIZE; col++) System.out.print("--");
        System.out.println("-+");

        if (showLegend) {
            System.out.println(MessageManager.get("game.legend.line1", SHIP_MARKER, SHIP_ADJACENT_MARKER, EMPTY_MARKER));
            System.out.println(MessageManager.get("game.legend.line2", SHOT_SHIP_MARKER, SHOT_WATER_MARKER));
        }
        System.out.println();
    }

    /**
     * Serializes a list of shot positions to a formatted JSON string.
     *
     * @param shots list of positions to serialize.
     * @return JSON string representing the shot positions.
     * @throws RuntimeException if JSON serialization fails.
     */
    public static String jsonShots(List<IPosition> shots) {
        assert shots != null;

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        List<Map<String, Object>> simplifiedShots = new ArrayList<>();
        for (IPosition shot : shots) {
            Map<String, Object> simplePos = new LinkedHashMap<>();
            simplePos.put("row", String.valueOf(shot.getClassicRow()));
            simplePos.put("column", shot.getClassicColumn());
            simplifiedShots.add(simplePos);
        }

        try {
            return objectMapper.writeValueAsString(simplifiedShots);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(MessageManager.get("error.jsonShots"), e);
        }
    }

    //------------------------------------------------------------------
    /**
     * Generates random shots for the enemy, ensuring they are valid and unique.
     * Applies the shots to the player's fleet and returns the JSON representation.
     *
     * @return JSON string representing enemy shots.
     * @throws RuntimeException if serialization fails.
     */
    public String randomEnemyFire() {
        Random random = new Random(System.currentTimeMillis());

        Set<IPosition> usablePositions = new HashSet<>();
        for (int r = 0; r < BOARD_SIZE; r++)
            for (int c = 0; c < BOARD_SIZE; c++)
                usablePositions.add(new Position(r, c));

        this.myFleet.getSunkShips().forEach(ship -> usablePositions.removeAll(ship.getAdjacentPositions()));
        this.alienMoves.forEach(move -> usablePositions.removeAll(move.getShots()));

        List<IPosition> candidateShots = new ArrayList<>(usablePositions);
        List<IPosition> shots = new ArrayList<>();

        IPosition newShot = null;
        if (candidateShots.size() >= NUMBER_SHOTS)
            while (shots.size() < NUMBER_SHOTS) {
                newShot = candidateShots.get(random.nextInt(candidateShots.size()));
                if (!shots.contains(newShot)) shots.add(newShot);
            }
        else {
            while (shots.size() < candidateShots.size()) {
                newShot = candidateShots.get(random.nextInt(candidateShots.size()));
                if (!shots.contains(newShot)) shots.add(newShot);
            }
            while (shots.size() < NUMBER_SHOTS) shots.add(newShot);
        }

        System.out.println(MessageManager.get("game.burst"));
        for (IPosition shot : shots) System.out.print(shot + " ");
        System.out.println();

        this.fireShots(shots);
        return jsonShots(shots);
    }

    /**
     * Reads enemy fire input from a scanner, validates positions, applies the shots,
     * and returns their JSON representation.
     *
     * @param in Scanner to read enemy input.
     * @return JSON string of enemy shots.
     * @throws IllegalArgumentException if input is invalid or incomplete.
     */
    public String readEnemyFire(Scanner in) {
        assert in != null;

        String input = in.nextLine().trim();
        List<IPosition> shots = new ArrayList<>();
        Scanner inputScanner = new Scanner(input);

        while (shots.size() < NUMBER_SHOTS && inputScanner.hasNext()) {
            String token = inputScanner.next();
            if (token.matches("[A-Za-z]")) {
                if (inputScanner.hasNextInt()) {
                    int row = inputScanner.nextInt();
                    shots.add(new Position(token.toUpperCase().charAt(0), row));
                } else {
                    throw new IllegalArgumentException(MessageManager.get("error.incompletePosition", token));
                }
            } else {
                shots.add(Tasks.readClassicPosition(new Scanner(token)));
            }
        }

        if (shots.size() != NUMBER_SHOTS)
            throw new IllegalArgumentException(MessageManager.get("error.invalidPositions", NUMBER_SHOTS));

        this.fireShots(shots);
        return jsonShots(shots);
    }

    /**
     * Fires a list of shots, updates game statistics, and prints turn and total durations.
     *
     * @param shots list of positions to fire.
     * @throws IllegalArgumentException if the number of shots is invalid.
     */
    public void fireShots(List<IPosition> shots) {
        assert shots != null;

        if (shots.size() != NUMBER_SHOTS)
            throw new IllegalArgumentException(MessageManager.get("error.invalidShots", NUMBER_SHOTS));

        List<ShotResult> shotResults = new ArrayList<>();
        List<IPosition> alreadyShot = new ArrayList<>();
        for (IPosition pos : shots) {
            shotResults.add(fireSingleShot(pos, alreadyShot.contains(pos)));
            alreadyShot.add(pos);
        }

        Move move = new Move(moveNumber, shots, shotResults);
        move.processEnemyFire(true);
        alienMoves.add(move);
        moveNumber++;

        Duration accumulated = gameTimer.getDuration();
        Duration turnDuration = accumulated.minus(previousAccumulated);
        previousAccumulated = accumulated;
        System.out.println(MessageManager.get("game.turnDuration", GameTimer.formatDuration(turnDuration)));
        System.out.println(MessageManager.get("game.totalDuration", GameTimer.formatDuration(accumulated)));
    }

    /**
     * Fires a single shot at a specified position.
     *
     * @param pos the position to shoot.
     * @param isRepeated true if the shot is a repeat, false otherwise.
     * @return ShotResult containing the result of the shot.
     */
    public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {
        assert pos != null;

        if (!pos.isInside()) { countInvalidShots++; return new ShotResult(false, false, null, false); }
        if (isRepeated || repeatedShot(pos)) { countRepeatedShots++; return new ShotResult(true, true, null, false); }

        IShip ship = myFleet.shipAt(pos);
        if (ship == null) return new ShotResult(true, false, null, false);

        ship.shoot(pos);
        countHits++;
        if (!ship.stillFloating()) countSinks++;
        return new ShotResult(true, false, ship, !ship.stillFloating());
    }

    //------------------------------------------------------------------
    @Override public int getRepeatedShots() { return countRepeatedShots; }
    @Override public int getInvalidShots() { return countInvalidShots; }
    @Override public int getHits() { return countHits; }
    @Override public int getSunkShips() { return countSinks; }
    @Override public int getRemainingShips() { return myFleet.getFloatingShips().size(); }

    /**
     * Checks if a given position has already been shot by the enemy.
     *
     * @param pos position to check.
     * @return true if the position has already been shot, false otherwise.
     */
    public boolean repeatedShot(IPosition pos) {
        assert pos != null;
        for (IMove move : alienMoves) if (move.getShots().contains(pos)) return true;
        return false;
    }

    /**
     * Prints the player's board to the console.
     *
     * @param show_shots  whether to show shots.
     * @param show_legend whether to show the legend.
     */
    public void printMyBoard(boolean show_shots, boolean show_legend) {
        printBoard(myFleet, alienMoves, show_shots, show_legend);
    }

    /**
     * Prints the enemy's board to the console.
     *
     * @param show_shots  whether to show shots.
     * @param show_legend whether to show the legend.
     */
    public void printAlienBoard(boolean show_shots, boolean show_legend) {
        printBoard(alienFleet, myMoves, show_shots, show_legend);
    }

    /**
     * Ends the game, prints a closing message, generates a PDF report,
     * and displays the total game duration.
     */
    public void over() {
        System.out.println();
        System.out.println("+--------------------------------------------------------------+");
        System.out.println("| " + MessageManager.get("game.over") + " |");
        System.out.println("+--------------------------------------------------------------+");

        GameReportPDF.generate(this);
        gameTimer.end();
        System.out.println(MessageManager.get("game.totalDuration", GameTimer.formatDuration(gameTimer.getDuration())));
    }
}