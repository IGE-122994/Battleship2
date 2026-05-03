package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import org.joda.time.Duration;

import java.util.*;

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

		char[][] map = initializeMap();
		markShipsOnMap(map, fleet);

		if (show_shots)
			markShotsOnMap(map, moves);

		printBoardGrid(map);

		if (showLegend)
			printLegend();

		System.out.println();
	}

	/**
	 * Initializes the game board map with empty markers.
	 *
	 * @return a 2D char array representing the game board, initialized with empty markers.
	 */
	private static char[][] initializeMap() {
		char[][] map = new char[BOARD_SIZE][BOARD_SIZE];
		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				map[r][c] = EMPTY_MARKER;
		return map;
	}

	/**
	 * Marks ships and their adjacent positions on the game board map.
	 *
	 * @param map   the 2D char array representing the game board.
	 * @param fleet the fleet containing the ships to be marked.
	 */
	private static void markShipsOnMap(char[][] map, IFleet fleet) {
		for (IShip ship : fleet.getShips()) {
			for (IPosition ship_pos : ship.getPositions())
				map[ship_pos.getRow()][ship_pos.getColumn()] = SHIP_MARKER;
			if (!ship.stillFloating())
				for (IPosition adjacent_pos : ship.getAdjacentPositions())
					map[adjacent_pos.getRow()][adjacent_pos.getColumn()] = SHIP_ADJACENT_MARKER;
		}
	}

	/**
	 * Marks shots on the game board map, distinguishing between hits and misses.
	 *
	 * @param map   the 2D char array representing the game board.
	 * @param moves the list of moves containing the shots to be marked.
	 */
	private static void markShotsOnMap(char[][] map, List<IMove> moves) {
		for (IMove move : moves)
			for (IPosition shot : move.getShots()) {
				if (shot.isInside()) {
					int row = shot.getRow();
					int col = shot.getColumn();
					if (map[row][col] == SHIP_MARKER)
						map[row][col] = SHOT_SHIP_MARKER;
					else if (map[row][col] == EMPTY_MARKER || map[row][col] == SHIP_ADJACENT_MARKER)
						map[row][col] = SHOT_WATER_MARKER;
				}
			}
	}

	/**
	 * Prints the game board grid to the console, including column headers, row labels,
	 * and the board borders.
	 *
	 * @param map the 2D char array representing the game board to be printed.
	 */
	private static void printBoardGrid(char[][] map) {
		printColumnHeaders();
		printTopBorder();
		printRows(map);
		printBottomBorder();
	}

	/**
	 * Prints the column headers (1-10) for the board.
	 */
	private static void printColumnHeaders() {
		System.out.println();
		System.out.print("    ");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print(" " + (col + 1));
		}
		System.out.println();
	}

	/**
	 * Prints the top border of the board.
	 */
	private static void printTopBorder() {
		System.out.print("   +-");
		for (int col = 0; col < BOARD_SIZE; col++) {
			System.out.print("--");
		}
		System.out.println("+");
	}

	/**
	 * Prints all rows of the board with row labels and cell contents.
	 *
	 * @param map the 2D char array representing the game board.
	 */
	private static void printRows(char[][] map) {
		for (int row = 0; row < BOARD_SIZE; row++) {
			Position pos = new Position(row, 0);
			char rowLabel = pos.getClassicRow();
			System.out.print(" " + rowLabel + " |");
			for (int col = 0; col < BOARD_SIZE; col++)
				System.out.print(" " + map[row][col]);
			System.out.println(" |");
		}
	}

	/**
	 * Prints the bottom border of the board.
	 */
	private static void printBottomBorder() {
		System.out.print("   +");
		for (int col = 0; col < BOARD_SIZE; col++)
			System.out.print("--");
		System.out.println("-+");
	}

	/**
	 * Prints the legend explaining the symbols used on the board.
	 */
	private static void printLegend() {
		System.out.println(MessageManager.get("game.legend.line1", SHIP_MARKER, SHIP_ADJACENT_MARKER, EMPTY_MARKER));
		System.out.println(MessageManager.get("game.legend.line2", SHOT_SHIP_MARKER, SHOT_WATER_MARKER));
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
			throw new RuntimeException(MessageManager.get("error.jsonShots"), e);
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
	/**
	 * Constructs a new game instance with the player's fleet.
	 *
	 * @param myFleet the fleet of the player containing the ships.
	 */
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

	/**
	 * Returns the player's fleet.
	 *
	 * @return the player's fleet.
	 */
	@Override
	public IFleet getMyFleet()
	{
		return myFleet;
	}

	/**
	 * Returns the list of moves made by the enemy.
	 *
	 * @return list of enemy moves.
	 */
	@Override
	public List<IMove> getAlienMoves()
	{
		return alienMoves;
	}

	/**
	 * Returns the enemy fleet.
	 *
	 * @return the enemy fleet.
	 */
	@Override
	public IFleet getAlienFleet()
	{
		return myFleet;
	}

	/**
	 * Returns the list of moves made by the player.
	 *
	 * @return list of player moves.
	 */
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
		List<IPosition> candidateShots = generateCandidateShots();
		List<IPosition> shots = selectRandomShots(candidateShots);

		printSelectedShots(shots);
		this.fireShots(shots);

		return Game.jsonShots(shots);
	}

	/**
	 * Generates all candidate shot positions by filtering out already struck and adjacent positions.
	 * Excludes positions adjacent to sunk ships and positions that have already been targeted.
	 *
	 * @return a list of valid candidate positions for random shots.
	 */
	private List<IPosition> generateCandidateShots() {
		Set<IPosition> usablePositions = createAllBoardPositions();
		filterSunkShipAdjacencies(usablePositions);
		filterAlreadyShotPositions(usablePositions);
		return new ArrayList<>(usablePositions);
	}

	/**
	 * Creates a set containing all positions on the game board.
	 *
	 * @return a set of all board positions.
	 */
	private Set<IPosition> createAllBoardPositions() {
		Set<IPosition> positions = new HashSet<>();
		for (int r = 0; r < BOARD_SIZE; r++)
			for (int c = 0; c < BOARD_SIZE; c++)
				positions.add(new Position(r, c));
		return positions;
	}

	/**
	 * Removes all positions adjacent to sunk ships from the usable positions set.
	 * This prevents the AI from shooting near already destroyed ships.
	 *
	 * @param usablePositions the set of usable positions to be filtered.
	 */
	private void filterSunkShipAdjacencies(Set<IPosition> usablePositions) {
		for (IShip ship : this.myFleet.getSunkShips())
			usablePositions.removeAll(ship.getAdjacentPositions());
	}

	/**
	 * Removes all positions that have already been shot by previous alien moves
	 * from the usable positions set. This prevents duplicate shots.
	 *
	 * @param usablePositions the set of usable positions to be filtered.
	 */
	private void filterAlreadyShotPositions(Set<IPosition> usablePositions) {
		for (IMove move : this.alienMoves)
			usablePositions.removeAll(move.getShots());
	}

	/**
	 * Selects a list of random unique shot positions from the candidate positions.
	 * If there are fewer candidates than NUMBER_SHOTS, all candidates are selected
	 * and the last position is repeated to fill the remaining slots.
	 *
	 * @param candidateShots the list of candidate positions to select from.
	 * @return a list of random shot positions with size equal to NUMBER_SHOTS.
	 */
	private List<IPosition> selectRandomShots(List<IPosition> candidateShots) {
		Random random = new Random(System.currentTimeMillis());
		List<IPosition> shots = new ArrayList<>();

		if (candidateShots.isEmpty())
			return shots;

		IPosition newShot = null;
		int targetSize = Math.min(candidateShots.size(), Game.NUMBER_SHOTS);

		// Fill with unique random positions
		while (shots.size() < targetSize) {
			newShot = candidateShots.get(random.nextInt(candidateShots.size()));
			if (!shots.contains(newShot))
				shots.add(newShot);
		}

		// If we need more shots, repeat the last position
		while (shots.size() < Game.NUMBER_SHOTS)
			shots.add(newShot);

		return shots;
	}

	/**
	 * Prints the randomly selected shots to the console.
	 *
	 * @param shots the list of shot positions to be printed.
	 */
	private void printSelectedShots(List<IPosition> shots) {
		System.out.println();
		System.out.println(MessageManager.get("game.burst"));
		for (IPosition shot : shots)
			System.out.print(shot + " ");
		System.out.println();
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
					throw new IllegalArgumentException(MessageManager.get("error.incompletePosition", token));
				}
			} else {
				// Caso o token já contenha a coluna e a linha juntas (ex.: "A3")
				Scanner singleScanner = new Scanner(token);
				shots.add(Tasks.readClassicPosition(singleScanner));
			}
		}

		if (shots.size() != NUMBER_SHOTS) {
			throw new IllegalArgumentException(MessageManager.get("error.invalidPositions", NUMBER_SHOTS));
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
			throw new IllegalArgumentException(MessageManager.get("error.invalidShots", NUMBER_SHOTS));
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
		System.out.println(MessageManager.get("game.turnDuration", GameTimer.formatDuration(turnDuration)));
		System.out.println(MessageManager.get("game.accumulatedDuration", GameTimer.formatDuration(accumulated)));
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

		// Check for invalid position
		boolean isOutside = !pos.isInside();
		if (isOutside)
			return handleInvalidShot();

		// Check for repeated shot
		if (isRepeated || repeatedShot(pos))
			return handleRepeatedShot();

		// Attempt to hit a ship at the position
		IShip ship = myFleet.shipAt(pos);
		if (ship == null)
			return handleMiss();

		return handleHit(ship, pos);
	}

	/**
	 * Handles an invalid shot (outside board boundaries).
	 * Increments the invalid shot counter.
	 *
	 * @return a ShotResult indicating an invalid shot.
	 */
	private ShotResult handleInvalidShot() {
		countInvalidShots++;
		return new ShotResult(false, false, null, false);
	}

	/**
	 * Handles a repeated shot (shot at a previously targeted position).
	 * Increments the repeated shot counter.
	 *
	 * @return a ShotResult indicating a repeated shot.
	 */
	private ShotResult handleRepeatedShot() {
		countRepeatedShots++;
		return new ShotResult(true, true, null, false);
	}

	/**
	 * Handles a miss (no ship at the targeted position).
	 *
	 * @return a ShotResult indicating a miss.
	 */
	private ShotResult handleMiss() {
		return new ShotResult(true, false, null, false);
	}

	/**
	 * Handles a hit on a ship. Updates the ship's state, increments hit and sink counters
	 * as appropriate, and determines if the ship was sunk by this shot.
	 *
	 * @param ship the ship that was hit.
	 * @param pos the position where the ship was hit.
	 * @return a ShotResult indicating a successful hit, and whether the ship was sunk.
	 */
	private ShotResult handleHit(IShip ship, IPosition pos) {
		ship.shoot(pos);
		countHits++;

		boolean isSunk = !ship.stillFloating();
		if (isSunk)
			countSinks++;

		return new ShotResult(true, false, ship, isSunk);
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

	/**
	 * Checks if a given position has already been shot by the enemy.
	 *
	 * @param pos position to check.
	 * @return true if the position has already been shot, false otherwise.
	 */
	public boolean repeatedShot(IPosition pos)
	{
		assert pos != null;

		for (IMove move : alienMoves)
			if (move.getShots().contains(pos))
				return true;
		return false;
	}

	/**
	 * Prints the player's board to the console.
	 *
	 * @param show_shots  whether to show shots.
	 * @param show_legend whether to show the legend.
	 */
	public void printMyBoard(boolean show_shots, boolean show_legend)
	{
		Game.printBoard(this.myFleet, this.alienMoves, show_shots, show_legend);
	}

	/**
	 * Prints the enemy's board to the console.
	 *
	 * @param show_shots  whether to show shots.
	 * @param show_legend whether to show the legend.
	 */
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
	 *
	 * <p>
	 * After displaying the final information, this method also generates a PDF
	 * report of the match using {@link GameReportPDF}. The report includes relevant
	 * game data and the total duration, allowing the user to keep a permanent
	 * record of the session.
	 * </p>
	 */
	public void over() {
		System.out.println();
		System.out.println("+--------------------------------------------------------------+");
		System.out.println("| " + MessageManager.get("game.over") + " |");
		System.out.println("+--------------------------------------------------------------+");

		gameTimer.end();
		String totalDuration = GameTimer.formatDuration(gameTimer.getDuration());
		System.out.println(MessageManager.get("game.totalDuration", totalDuration));

		GameReportPDF.generate(this, totalDuration);
	}

}