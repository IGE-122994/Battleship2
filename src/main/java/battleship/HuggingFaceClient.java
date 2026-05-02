package battleship;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;

/**
 * Client responsible for communicating with the Hugging Face Inference API.
 *
 * <p>Sends the current game state and move history to a hosted LLM and
 * receives the next move as a JSON string, which is then parsed into
 * a list of shot positions.</p>
 *
 * @author [o teu nome]
 * @version 1.0
 */
public class HuggingFaceClient {

    private static final String HF_TOKEN = "INSERT_TOKEN_HERE";
    private static final String MODEL_URL = "https://router.huggingface.co/v1/chat/completions";

    private static final ObjectMapper mapper = new ObjectMapper();

    private final List<Map<String, String>> history = new ArrayList<>();
    private boolean initialized = false;

    /**
     * Initializes the LLM with the game rules, fleet composition,
     * and the JSON communication protocol via few-shot prompting.
     *
     * @throws Exception if the HTTP request fails
     */
    public void initialize() throws Exception {
        String systemPrompt = """
                És um estratega naval. Tabuleiro: A-J (linhas), 1-10 (colunas).
        
                FROTA (11 navios): 4 Barcas(1), 3 Caravelas(2 retas), 2 Naus(3 retas), 1 Fragata(4 reta), 1 Galeão(5, forma T).
                Navios NUNCA se tocam, nem nas diagonais.
        
                PROTOCOLO — responde SEMPRE e APENAS com JSON de exatamente 3 tiros:
                [ {"row": "A", "column": 5}, {"row": "C", "column": 10}, {"row": "F", "column": 5} ]

                LEI ABSOLUTA — NUNCA VIOLAR:
                - NUNCA repitas coordenadas. Antes de responder, verifica cada tiro contra "Already fired".
                - NUNCA dispares fora do tabuleiro (A-J, 1-10).
                - Sempre exatamente 3 tiros por rajada.
                -Primeira jogada escolhe H10 como uma das coordenadas
                MAPA MENTAL:
                - Cada célula: DESCONHECIDA / ÁGUA / ACERTO
                - Navio afundado → marca halo (8 vizinhos) como ÁGUA imediatamente
                - Diagonais de acerto = ÁGUA garantida (exceto centro do Galeão em T)

                PRIORIDADE (seguir SEMPRE esta ordem):
                1. DESTRUIR — acertos pendentes? TODOS os 3 tiros nesse navio até afundar
                2. ORIENTAR — 1 acerto? Testa N/S/E/O → descobre orientação → segue em linha reta
                3. VARRER — sem alvos? Padrão xadrez, espalhado por zonas com mais células DESCONHECIDAS

                TÁTICAS:
                - Barca: 1 tiro afunda → halo imediato
                - Caravela/Nau/Fragata: testa 2 lados → segue orientação
                - Galeão T: testa 4 direções → só assume orientação após 2 acertos alinhados
                - Fim do jogo: verifica "Already fired" antes de cada tiro e escolhe apenas posições não listadas
        
        """;

        history.add(Map.of("role", "user", "content", systemPrompt));
        String response = sendMessage("Confirma que entendeste todas as regras respondendo apenas com: PRONTO");
        initialized = true;
        System.out.println("LLM inicializado: " + response);
    }

    /**
     * Requests the next move from the LLM given the current game history.
     *
     * @param gameHistory a human-readable summary of all moves played so far
     * @return a list of {@link IPosition} representing the 3 shots to fire
     * @throws Exception if the HTTP request or JSON parsing fails
     */
    public List<IPosition> getNextMove(String gameHistory) throws Exception {
        if (!initialized) initialize();

        String prompt = """
                Histórico do jogo até agora:
                %s

                Gera a próxima rajada de 3 tiros. Responde APENAS com o JSON, sem texto adicional.
                """.formatted(gameHistory);

        String response = sendMessage(prompt);
        return parseShots(response);
    }

    /**
     * Sends a message to the Hugging Face API and returns the model's response.
     *
     * @param userMessage the message to send
     * @return the model's text response
     * @throws Exception if the HTTP request fails
     */
    private String sendMessage(String userMessage) throws Exception {
        history.add(Map.of("role", "user", "content", userMessage));

        Map<String, Object> requestBody = Map.of(
                "model", "meta-llama/Llama-3.3-70B-Instruct:groq",
                "messages", history,
                "max_tokens", 200,
                "temperature", 0.1
        );

        String jsonBody = mapper.writeValueAsString(requestBody);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(MODEL_URL))
                .header("Authorization", "Bearer " + HF_TOKEN)
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        try (HttpClient client = HttpClient.newHttpClient()) {

            HttpResponse<String> response = client.send(
                    request,
                    HttpResponse.BodyHandlers.ofString()
            );

            if (response.statusCode() != 200) {
                throw new RuntimeException(
                        "Erro na API: " + response.statusCode() + " - " + response.body()
                );
            }

            Map<?, ?> responseMap = mapper.readValue(response.body(), Map.class);
            List<?> choices = (List<?>) responseMap.get("choices");
            Map<?, ?> firstChoice = (Map<?, ?>) choices.get(0);
            Map<?, ?> message = (Map<?, ?>) firstChoice.get("message");
            String generatedText = (String) message.get("content");

            history.add(Map.of("role", "assistant", "content", generatedText));
            return generatedText.trim();

        }

    }

    /**
     * Parses a JSON string containing shot positions into a list of {@link IPosition}.
     *
     * @param json the JSON string to parse, expected format:
     *             {@code [{"row":"A","column":1}, ...]}
     * @return a list of positions representing the shots
     * @throws Exception if the JSON is malformed or cannot be parsed
     */
    private List<IPosition> parseShots(String json) throws Exception {
        int start = json.indexOf('[');
        int end = json.lastIndexOf(']') + 1;
        if (start == -1 || end == 0) {
            throw new RuntimeException("Resposta inválida do LLM: " + json);
        }
        String jsonArray = json.substring(start, end);

        List<?> rawList = mapper.readValue(jsonArray, List.class);
        List<IPosition> shots = new ArrayList<>();

        for (Object obj : rawList) {
            Map<?, ?> shot = (Map<?, ?>) obj;
            String row = shot.get("row").toString();
            int column = Integer.parseInt(shot.get("column").toString());
            shots.add(new Position(row.charAt(0), column));
        }

        return shots;
    }

    /**
     * Builds a human-readable summary of all moves played so far.
     *
     * @param moves the list of moves to summarise
     * @return a formatted string describing each move and its outcome
     */
    public static String buildGameHistory(List<IMove> moves) {
        if (moves.isEmpty()) return "Nenhuma jogada ainda.";

        GameHistoryData historyData = new GameHistoryData();
        
        // Process all moves to collect fired positions, pending hits, and sunk ship halos
        for (IMove move : moves) {
            processMoveResults(move, historyData);
        }

        // Calculate available positions for next move
        List<String> availablePositions = calculateAvailablePositions(historyData);

        // Build and return the complete summary
        return buildHistorySummary(moves, historyData, availablePositions);
    }

    /**
     * Inner class to hold game history data (fired positions, pending hits, halo positions).
     */
    private static class GameHistoryData {
        public final List<String> firedList = new ArrayList<>();
        public final List<String> pendingHits = new ArrayList<>();
        public final List<String> haloPositions = new ArrayList<>();
    }

    /**
     * Processes the results of a single move, updating the history data.
     *
     * @param move the move to process
     * @param historyData the game history data to update
     */
    private static void processMoveResults(IMove move, GameHistoryData historyData) {
        List<IPosition> shots = move.getShots();
        List<IGame.ShotResult> results = move.getShotResults();

        for (int i = 0; i < shots.size(); i++) {
            IPosition pos = shots.get(i);
            IGame.ShotResult result = results.get(i);
            
            String coord = positionToCoordinate(pos);
            addFiredPosition(coord, historyData);
            processHitOrMiss(coord, result, historyData);
        }
    }

    /**
     * Converts a Position to a coordinate string (e.g., "A1").
     *
     * @param pos the position to convert
     * @return the coordinate string
     */
    private static String positionToCoordinate(IPosition pos) {
        return String.valueOf((char)('A' + pos.getRow())) + (pos.getColumn() + 1);
    }

    /**
     * Adds a fired position to the list if not already present.
     *
     * @param coord the coordinate to add
     * @param historyData the game history data to update
     */
    private static void addFiredPosition(String coord, GameHistoryData historyData) {
        if (!historyData.firedList.contains(coord)) {
            historyData.firedList.add(coord);
        }
    }

    /**
     * Processes a shot result, updating pending hits and halo positions.
     *
     * @param coord the coordinate that was shot
     * @param result the result of the shot
     * @param historyData the game history data to update
     */
    private static void processHitOrMiss(String coord, IGame.ShotResult result, GameHistoryData historyData) {
        // Pending hit - ship hit but not sunk
        if (result.valid() && !result.repeated() && result.ship() != null && !result.sunk()) {
            if (!historyData.pendingHits.contains(coord)) {
                historyData.pendingHits.add(coord);
            }
        }

        // Ship sunk - remove from pending and add halo positions
        if (result.valid() && result.sunk() && result.ship() != null) {
            removeShipPositionsFromPending(result.ship(), historyData);
            addHaloPositionsForSunkShip(result.ship(), historyData);
        }
    }

    /**
     * Removes all positions of a sunk ship from the pending hits list.
     *
     * @param ship the sunk ship
     * @param historyData the game history data to update
     */
    private static void removeShipPositionsFromPending(IShip ship, GameHistoryData historyData) {
        for (IPosition shipPos : ship.getPositions()) {
            String shipCoord = positionToCoordinate(shipPos);
            historyData.pendingHits.remove(shipCoord);
        }
    }

    /**
     * Adds all halo (adjacent) positions around a sunk ship to the halo positions list.
     *
     * @param ship the sunk ship
     * @param historyData the game history data to update
     */
    private static void addHaloPositionsForSunkShip(IShip ship, GameHistoryData historyData) {
        for (IPosition shipPos : ship.getPositions()) {
            addAdjacentHaloPositions(shipPos, historyData);
        }
    }

    /**
     * Adds all valid adjacent positions around a ship position to the halo list.
     *
     * @param shipPos the position of a ship cell
     * @param historyData the game history data to update
     */
    private static void addAdjacentHaloPositions(IPosition shipPos, GameHistoryData historyData) {
        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr == 0 && dc == 0) continue; // Skip the ship position itself
                
                int nr = shipPos.getRow() + dr;
                int nc = shipPos.getColumn() + dc;
                
                if (isValidBoardPosition(nr, nc)) {
                    String haloCoord = String.valueOf((char)('A' + nr)) + (nc + 1);
                    if (!historyData.haloPositions.contains(haloCoord)) {
                        historyData.haloPositions.add(haloCoord);
                    }
                }
            }
        }
    }

    /**
     * Checks if a position is within board boundaries.
     *
     * @param row the row index
     * @param col the column index
     * @return true if the position is valid, false otherwise
     */
    private static boolean isValidBoardPosition(int row, int col) {
        return row >= 0 && row < 10 && col >= 0 && col < 10;
    }

    /**
     * Calculates the list of available positions for the next move.
     * Available positions are those not yet fired and not in halo zones.
     *
     * @param historyData the game history data
     * @return a list of available position coordinates
     */
    private static List<String> calculateAvailablePositions(GameHistoryData historyData) {
        List<String> available = new ArrayList<>();
        
        for (int r = 0; r < 10; r++) {
            for (int c = 0; c < 10; c++) {
                String coord = String.valueOf((char)('A' + r)) + (c + 1);
                
                if (!historyData.firedList.contains(coord) && !historyData.haloPositions.contains(coord)) {
                    available.add(coord);
                }
            }
        }
        
        return available;
    }

    /**
     * Builds the final game history summary string.
     *
     * @param moves the list of moves
     * @param historyData the game history data
     * @param availablePositions the available positions for next move
     * @return the formatted summary string
     */
    private static String buildHistorySummary(List<IMove> moves, GameHistoryData historyData, List<String> availablePositions) {
        IMove lastMove = moves.get(moves.size() - 1);
        String lastMoveDescription = buildLastMoveDescription(lastMove);

        StringBuilder sb = new StringBuilder();
        sb.append("Rajada número: ").append(moves.size() + 1).append("\n");
        sb.append("POSIÇÕES DISPONÍVEIS (escolhe APENAS destas): ").append(String.join(", ", availablePositions)).append("\n");
        sb.append("ACERTOS PENDENTES (foca aqui PRIMEIRO): ").append(historyData.pendingHits.isEmpty() ? "nenhum" : String.join(", ", historyData.pendingHits)).append("\n");
        sb.append(lastMoveDescription).append("\n");
        sb.append("Escolhe 3 posições da lista DISPONÍVEIS acima. Não uses nenhuma outra.");

        return sb.toString();
    }

    /**
     * Builds a description of the last move for display.
     *
     * @param lastMove the last move to describe
     * @return the formatted description
     */
    private static String buildLastMoveDescription(IMove lastMove) {
        StringBuilder lastMoveDesc = new StringBuilder();
        lastMoveDesc.append("Última rajada (").append(lastMove.getNumber()).append("): ");
        
        for (IPosition pos : lastMove.getShots()) {
            lastMoveDesc.append((char)('A' + pos.getRow())).append(pos.getColumn() + 1).append(" ");
        }
        
        lastMoveDesc.append("-> ").append(lastMove.processEnemyFire(false));
        return lastMoveDesc.toString();
    }

}
