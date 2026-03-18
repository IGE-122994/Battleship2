package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.joda.time.Duration;

import java.util.*;

<<<<<<< feature/internationalization-messages
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
=======
public class Game implements IGame
{
	/**
	 * Prints the game board by representing the positions of ships, adjacent tiles,
	 * shots, and other game elements onto the console. The method also optionally
	 * displays shot positions and a legend explaining the symbols used on the board.
	 *
	 * @param fleet       the fleet of ships to be displayed on the board. Ships are marked
	 *                    and their positions are shown according to their placement.
	 * @param moves       the list of moves containing shots. If shot positions are shown,
	 *                    they will be rendered based on their outcome (hit, miss, etc.).
	 * @param show_shots  if true, displays the shots taken during the game and marks
	 *                    their result (hit or miss) on the board.
	 * @param showLegend  if true, displays an explanatory legend of the symbols used
	 *                    to represent various elements such as ships, misses, hits, etc.
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
					if (shot.isInside()){
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
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print(" " + (col + 1));
		}
		System.out.println();

		System.out.print("   +-");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("+");

		for (int row = 0; row < BOARD_SIZE; row++) {
			Position pos = new Position(row, 0);
			char rowLabel = pos.getClassicRow();
			System.out.print(" " + rowLabel + " |");
			for (int col = 0; col < BOARD_SIZE; col++)
				System.out.print(" " + map[row][col]);
			System.out.println(" |");
		}

		System.out.print("   +");
		for (int col = 0; col < BOARD_SIZE; col++)
			System.out.print("--");
		System.out.println("-+");

		if (showLegend) {
			System.out.println("          LEGENDA");
			System.out.println("'" + SHIP_MARKER + "'->navio, '" + SHIP_ADJACENT_MARKER + "'->adjacente a navio, '" + EMPTY_MARKER + "'->água");
			System.out.println("'" + SHOT_SHIP_MARKER + "'->Tiro certeiro, '" + SHOT_WATER_MARKER + "'->Tiro na água");
		}
		System.out.println();
	}

	/**
	 * Serializes a list of shot positions into a JSON string. Each shot is represented
	 * with its classic row and column values. The method uses the Jackson library for
	 * JSON serialization.
	 *
	 * @param shots a list of shot positions to be serialized. Each position is represented
	 *              by an implementation of the {@code IPosition} interface. The list must
	 *              not be null.
	 * @return a formatted JSON string containing the shot positions. Each shot includes
	 *         its classic row and column.
	 * @throws RuntimeException if an error occurs during JSON serialization.
	 */
	public static String jsonShots(List<IPosition> shots) {

		assert shots != null;

		// Serializar os tiros gerados em JSON usando a biblioteca Jackson
		ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

		// 1. Create a simplified list containing only the desired data
		List<Map<String, Object>> simplifiedShots = new ArrayList<>();
		for (IPosition shot : shots) {
			Map<String, Object> simplePos = new LinkedHashMap<>();
			// We use getClassicRow() and getClassicColumn() based on your current JSON output
			simplePos.put("row", String.valueOf(shot.getClassicRow()));
			simplePos.put("column", shot.getClassicColumn());
			simplifiedShots.add(simplePos);
		}

		String jsonString = null;
		try {
			// 2. Serialize the simplified list instead of the raw 'shots' list
			jsonString = objectMapper.writeValueAsString(simplifiedShots);
		} catch (JsonProcessingException e) {
			throw new RuntimeException("Erro ao serializar o JSON", e);
		}

//		System.out.println(jsonString);
//		System.out.println();

		// Retornar o JSON
		return jsonString;
	}

	//------------------------------------------------------------------
	public static final int BOARD_SIZE = 10;
	public static final int NUMBER_SHOTS = 3;

	private static final char EMPTY_MARKER = '.';
	private static final char SHIP_MARKER = '#';
	private static final char SHOT_SHIP_MARKER = '*';
	private static final char SHOT_WATER_MARKER = 'o';
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
	public Game(IFleet myFleet)
	{
		this.moveNumber = 1;

		this.alienMoves = new ArrayList<IMove>();
		this.myMoves = new ArrayList<IMove>();

		this.alienFleet = new Fleet();
		this.myFleet = myFleet;

		this.countInvalidShots = 0;
		this.countRepeatedShots = 0;
		this.countHits = 0;
		this.countSinks = 0;

		gameTimer.begin();
	}

	@Override
	public IFleet getMyFleet()
	{
		return myFleet;
	}

	@Override
	public List<IMove> getAlienMoves()
	{
		return alienMoves;
	}

	@Override
	public IFleet getAlienFleet()
	{
		return myFleet;
	}

	@Override
	public List<IMove> getMyMoves()
	{
		return myMoves;
	}

	/**
	 *  Simulates a random firing action by the enemy, generating a set of unique shot coordinates
	 * and serializing them into a JSON string. The method ensures that the random shots are valid
	 * and do not duplicate existing shots in the game or previous enemy moves. After generating
	 * the shots, it applies the firing logic and serializes the result for further processing.
	 *
	 * @return A JSON string representing the list of randomly generated enemy shots.
	 * @throws RuntimeException if there is an error during the JSON serialization of the shots.
	 */
	public String randomEnemyFire() {

		// Criar uma instância de Random com uma seed baseada no timestamp atual
		Random random = new Random(System.currentTimeMillis());

		Set<IPosition> usablePositions = new HashSet<IPosition>();
		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				usablePositions.add(new Position(r, c));

		this.myFleet.getSunkShips().forEach(ship -> usablePositions.removeAll(ship.getAdjacentPositions()));
		this.alienMoves.forEach(move ->  usablePositions.removeAll(move.getShots()));

		List<IPosition> candidateShots = new ArrayList<>(usablePositions);

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<IPosition>();

		System.out.println();
		// Gerar coordenadas únicas até atingir o número definido por NUMBER_SHOTS

		IPosition newShot = null;
		if (candidateShots.size() >= Game.NUMBER_SHOTS)
			while (shots.size() < Game.NUMBER_SHOTS) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
		else {
			while (shots.size() < candidateShots.size()) {
				newShot = candidateShots.get(random.nextInt(candidateShots.size()));
				if (!shots.contains(newShot))
					shots.add(newShot);
			}
			while (shots.size() < Game.NUMBER_SHOTS)
				shots.add(newShot);
		}

		System.out.print("rajada ");
		for (IPosition shot : shots)
			System.out.print(shot + " ");
		System.out.println();

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}


	/**
	 * Reads and processes the enemy fire input from the specified scanner.
	 * The method expects input describing positions for enemy shots. It verifies
	 * the format, ensures the correct number of positions are provided, and then fires
	 * on those positions.
	 *
	 * @param in the scanner object to read the enemy fire positions from, input must
	 *           be formatted either as a single token combining the column and row
	 *           (e.g., "A3") or as separate tokens (e.g., "A" followed by "3").
	 * @throws IllegalArgumentException if the provided positions are incomplete,
	 *                                  incorrectly formatted, or do not match the
	 *                                  required number of shots (NUMBER_SHOTS).
	 */
	public String readEnemyFire(Scanner in) {

		assert in != null;

		String input = in.nextLine().trim();

		// Criar lista para armazenar os tiros
		List<IPosition> shots = new ArrayList<>();

		Scanner inputScanner = new Scanner(input);
		while (shots.size() < NUMBER_SHOTS && inputScanner.hasNext()) {
			// Lê a próxima parte e constrói uma posição
			String token = inputScanner.next();

			if (token.matches("[A-Za-z]")) {
				// Caso seja somente uma coluna ("A", "B", etc.), esperar o próximo número
				if (inputScanner.hasNextInt()) {
					int row = inputScanner.nextInt();
					shots.add(new Position(token.toUpperCase().charAt(0), row));
				} else {
					throw new IllegalArgumentException("Posição incompleta! A coluna '" + token + "' não é seguida por uma linha.");
				}
			} else {
				// Caso o token já contenha a coluna e a linha juntas (ex.: "A3")
				Scanner singleScanner = new Scanner(token);
				shots.add(Tasks.readClassicPosition(singleScanner));
			}
		}

		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Você deve inserir exatamente " + NUMBER_SHOTS + " posições!");
		}

		this.fireShots(shots);

		return Game.jsonShots(shots);
	}

	/**
	 * Fires a set of shots during a player's move. Each shot is resolved and
	 * consolidated into a move, which is processed and added to the list of alien moves.
	 * The method ensures exactly {@code NUMBER_SHOTS} shots are fired, validates
	 * each shot's position, and increments the move counter after completing the operation.
	 *
	 * <p>
	 * This method also updates the game's timing information. After resolving the
	 * move, the method:
	 * <ul>
	 *     <li>Retrieves the total accumulated game duration from the {@code GameTimer}</li>
	 *     <li>Computes the duration of the current turn by subtracting the previously
	 *         accumulated duration</li>
	 *     <li>Prints both the turn duration and the updated accumulated duration</li>
	 * </ul>
	 * </p>
	 *
	 * @param shots a list of positions representing the locations to fire shots at.
	 *              The positions should be unique and valid within the bounds of the game board.
	 *              The size of the list must be equal to {@code NUMBER_SHOTS}.
	 * @throws IllegalArgumentException if the list of shots is null, contains an invalid
	 *                                  number of positions, or includes duplicate positions.
	 */
	public void fireShots(List<IPosition> shots)
	{
		assert shots != null;

		List<ShotResult> shotResults = new ArrayList<>();
		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException("Você deve atirar exatamente " + NUMBER_SHOTS + " tiros por jogada.");
		}

		List<IPosition> alreadyShot = new ArrayList<>();
		for (IPosition pos : shots) {
			shotResults.add(fireSingleShot(pos, alreadyShot.contains(pos)));
			alreadyShot.add(pos);
		}

		Move move = new Move(moveNumber, shots, shotResults);

//		System.out.println(move);

		move.processEnemyFire(true);

		alienMoves.add(move);

		moveNumber++;

		Duration accumulated = gameTimer.getDuration();
		Duration turnDuration = accumulated.minus(previousAccumulated);
		previousAccumulated = accumulated;
		System.out.println("Duração da jogada: " + GameTimer.formatDuration(turnDuration));
		System.out.println("Duração acumulada: " + GameTimer.formatDuration(accumulated));
	}

	/**
	 * Fires a single shot at the specified position, handling scenarios such as invalid positions,
	 * repeated shots, hits, misses, and sinking a ship. The method updates the necessary counters
	 * for invalid shots, repeated shots, hits, and sunk ships.
	 *
	 * @param pos the position to fire the shot at; must be valid and within the game board boundaries.
	 * @param isRepeated true if the shot is marked as a repeat attempt, false otherwise.
	 * @return a ShotResult object containing the result of the shot, including whether the shot was
	 *         valid, repeated, a hit, and whether a ship was sunk.
	 */
	public ShotResult fireSingleShot(IPosition pos, boolean isRepeated) {

		assert pos != null;

		if (!pos.isInside()) {
			countInvalidShots++;
			return new ShotResult(false, false, null, false);
		}

		if (isRepeated || repeatedShot(pos)) {
			countRepeatedShots++;
			return new ShotResult(true, true, null, false);
		}

		IShip ship = myFleet.shipAt(pos);
		if (ship == null)
			return new ShotResult(true, false, null, false);
		else
		{
			ship.shoot(pos);
			countHits++;
			if (!ship.stillFloating()) {
				countSinks++;
			}
			return new ShotResult(true, false, ship, !ship.stillFloating());
		}
	}

	@Override
	public int getRepeatedShots()
	{
		return this.countRepeatedShots;
	}

	@Override
	public int getInvalidShots()
	{
		return this.countInvalidShots;
	}

	@Override
	public int getHits()
	{
		return this.countHits;
	}

	@Override
	public int getSunkShips()
	{
		return this.countSinks;
	}

	@Override
	public int getRemainingShips()
	{
		List<IShip> floatingShips = myFleet.getFloatingShips();
		return floatingShips.size();
	}

	public boolean repeatedShot(IPosition pos)
	{
		assert pos != null;

		for (IMove move : alienMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	public void printMyBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.myFleet, this.alienMoves, show_shots, show_legend);
	}

	public void printAlienBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.alienFleet, this.myMoves, show_shots, show_legend);
	}

	/**
	 * Finalizes the game, prints a closing message and displays the total duration
	 * of the match.
	 *
	 * <p>
	 * This method stops the {@link GameTimer}, retrieves the total elapsed time
	 * since the beginning of the game, and prints it in a human-readable format.
	 * </p>
	 */
	public void over() {
		System.out.println();
		System.out.println("+--------------------------------------------------------------+");
		System.out.println("| Maldito sejas, Java Sparrow, eu voltarei, glub glub glub ... |");
		System.out.println("+--------------------------------------------------------------+");

		gameTimer.end();
		String totalDuration = GameTimer.formatDuration(gameTimer.getDuration());
		System.out.println("Duração da partida: " + totalDuration);

		GameReportPDF.generate(this, totalDuration);
	}

>>>>>>> main
}