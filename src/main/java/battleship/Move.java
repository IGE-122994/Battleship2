package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Shot
 *
 * @author Your Name
 * Date: 20/02/2026
 * Time: 19:39
 */
public class Move implements IMove {

	//-------------------------------------------------------------------
	private final int number;
	private final List<IPosition> shots;
	private final List<IGame.ShotResult> shotResults;

	//-------------------------------------------------------------------
	public Move(int moveNumber, List<IPosition> moveShots, List<IGame.ShotResult> moveResults) {
		this.number = moveNumber;
		this.shots = moveShots;
		this.shotResults = moveResults;
	}

	@Override
	public String toString() {
		return "Move{" +
				"number=" + number +
				", shots=" + shots.size() +
				", results=" + shotResults.size() +
				'}';
	}

	@Override
	public int getNumber() {
		return this.number;
	}

	@Override
	public List<IPosition> getShots() {
		return this.shots;
	}

	@Override
	public List<IGame.ShotResult> getShotResults() {
		return this.shotResults;
	}

	/**
	 * Processes the results of enemy fire on the game board, analyzing the outcomes of shots,
	 * such as valid shots, repeated shots, missed shots, hits on ships, and sunk ships. It can
	 * also display a detailed summary of the shot results if verbose mode is activated.
	 *
	 * @param verbose a boolean indicating whether a detailed summary should be printed to the console
	 *                for the processed enemy fire data.
	 * @return a JSON-formatted string that encapsulates the results, including counts of valid shots,
	 *         repeated shots, missed shots, shots outside the game board, and details of hits and
	 *         sunk ships.
	 */
	@Override
    public String processEnemyFire(boolean verbose) {

        Map<String, Integer> sunkBoatsCount = new HashMap<>(); // Rastrear quantos navios de cada tipo afundaram
        Map<String, Integer> hitsPerBoat = new HashMap<>();

        ShotStats stats = computeShotStats(sunkBoatsCount, hitsPerBoat);

        if (verbose) {
            printVerboseSummary(stats.validShots, sunkBoatsCount, hitsPerBoat, stats.missedShots, stats.repeatedShots, stats.outsideShots);
        }

        return buildJsonSummary(stats.validShots, stats.outsideShots, stats.repeatedShots, stats.missedShots, sunkBoatsCount, hitsPerBoat);
    }

    private ShotStats computeShotStats(Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {

        ShotStats stats = new ShotStats();

        for (IGame.ShotResult result : this.shotResults) {
            if (!result.valid()) {
                stats.outsideShots++;
                continue;
            }

            if (result.repeated()) {
                stats.repeatedShots++;
            } else {
                stats.validShots++;
                if (result.ship() == null) {
                    stats.missedShots++;
                } else {
                    String boatName = result.ship().getCategory();
                    hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
                    if (result.sunk()) {
                        sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
                    }
                }
            }
        }

        return stats;
    }

    private void printVerboseSummary(int validShots, Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat, int missedShots, int repeatedShots, int outsideShots) {
        // Construção da mensagem de saída
        StringBuilder output = new StringBuilder();

        // VALID SHOTS
        if (validShots > 0) {
            output.append(String.format(
                    MessageManager.get("move.validShots"),
                    validShots,
                    validShots > 1 ? "s" : "",
                    validShots > 1 ? "s" : ""
            ));
        }

        // SUNK BOATS
        for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
            int count = entry.getValue();
            String boatName = entry.getKey();
            if (output.length() > 0) output.append(" + ");
            output.append(String.format(
                    MessageManager.get("move.sunkBoat"),
                    count,
                    boatName,
                    count > 1 ? "s" : ""
            ));
        }

        // HITS ON BOATS (not sunk)
        for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
            if (!sunkBoatsCount.containsKey(entry.getKey())) {
                int hits = entry.getValue();
                String boatName = entry.getKey();
                if (output.length() > 0) output.append(" + ");
                output.append(String.format(
                        MessageManager.get("move.hitBoat"),
                        hits,
                        hits > 1 ? "s" : "",
                        boatName
                ));
            }
        }

        // MISSED SHOTS
        if (missedShots > 0) {
            if (output.length() > 0) output.append(" + ");
            output.append(String.format(
                    MessageManager.get("move.waterShots"),
                    missedShots,
                    missedShots > 1 ? "s" : ""
            ));
        }

        // REPEATED SHOTS
        if (repeatedShots > 0) {
            if (output.length() > 0) output.append(" + ");
            output.append(String.format(
                    MessageManager.get("move.repeatedShots"),
                    repeatedShots,
                    repeatedShots > 1 ? "s" : "",
                    repeatedShots > 1 ? "s" : ""
            ));
        }
        // OUTSIDE SHOTS
        if (outsideShots > 0) {
            if (output.length() > 0) output.append(" + ");
            output.append(String.format(
                    MessageManager.get("move.outsideShots"),
                    outsideShots,
                    outsideShots > 1 ? "s" : "",
                    outsideShots > 1 ? "es" : ""
            ));
        }

        System.out.println(String.format(
                MessageManager.get("move.summary"),
                this.number,
                output
        ));
    }

    private static String buildJsonSummary(int validShots, int outsideShots, int repeatedShots, int missedShots, Map<String, Integer> sunkBoatsCount, Map<String, Integer> hitsPerBoat) {
        // Criar o mapa para o JSON
        Map<String, Object> response = new HashMap<>();
        response.put("validShots", validShots);
        response.put("outsideShots", outsideShots);
        response.put("repeatedShots", repeatedShots);
        response.put("missedShots", missedShots);

        // Criar a lista de barcos afundados
        List<Map<String, Object>> sunkBoats = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
            Map<String, Object> boat = new HashMap<>();
            boat.put("type", entry.getKey());
            boat.put("count", entry.getValue());
            sunkBoats.add(boat);
        }
        response.put("sunkBoats", sunkBoats);

        // Criar a lista de acertos em barcos que não foram afundados
        List<Map<String, Object>> boatHits = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
            if (!sunkBoatsCount.containsKey(entry.getKey())) {
                Map<String, Object> boat = new HashMap<>();
                boat.put("type", entry.getKey());
                boat.put("hits", entry.getValue());
                boatHits.add(boat);
            }
        }
        response.put("hitsOnBoats", boatHits);

        // Serializar o JSON utilizando Jackson
        String jsonString;

        // Serializar os tiros gerados em JSON usando a biblioteca Jackson
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);

        try {
            jsonString = objectMapper.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(MessageManager.get("error.jsonMove"), e);
        }

        System.out.println(jsonString);
//		System.out.println();

        // Retornar o JSON
        return jsonString;
    }

    private static class ShotStats {
        int validShots;
        int repeatedShots;
        int missedShots;
        int outsideShots;
    }


}