package battleship;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.util.*;

/**
 * Move
 * Representa uma jogada com os tiros realizados e resultados.
 */
public class Move implements IMove {

    private final int number;
    private final List<IPosition> shots;
    private final List<IGame.ShotResult> shotResults;

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

    @Override
    public String processEnemyFire(boolean verbose) {

        int validShots = 0;
        int repeatedShots = 0;
        int missedShots = 0;
        int outsideShots = 0;

        Map<String, Integer> sunkBoatsCount = new HashMap<>();
        Map<String, Integer> hitsPerBoat = new HashMap<>();

        for (IGame.ShotResult result : this.shotResults) {
            if (!result.valid()) {
                outsideShots++;
                continue;
            }

            if (result.repeated()) {
                repeatedShots++;
            } else {
                validShots++;
                if (result.ship() == null) {
                    missedShots++;
                } else {
                    String boatName = result.ship().getCategory();
                    hitsPerBoat.put(boatName, hitsPerBoat.getOrDefault(boatName, 0) + 1);
                    if (result.sunk()) {
                        sunkBoatsCount.put(boatName, sunkBoatsCount.getOrDefault(boatName, 0) + 1);
                    }
                }
            }
        }

        if (verbose) {
            StringBuilder output = new StringBuilder();

            if (validShots > 0) {
                output.append(String.format(MessageManager.get("move.validShots"), validShots, validShots > 1 ? "s" : "", validShots > 1 ? "s" : ""));
            }

            for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
                int count = entry.getValue();
                String boatName = entry.getKey();
                output.append(count > 0 ? (output.length() > 0 ? " + " : "") : "");
                output.append(String.format(MessageManager.get("move.sunkBoat"), count, boatName, count > 1 ? "s" : ""));
            }

            for (Map.Entry<String, Integer> entry : hitsPerBoat.entrySet()) {
                if (!sunkBoatsCount.containsKey(entry.getKey())) {
                    int hits = entry.getValue();
                    String boatName = entry.getKey();
                    output.append(hits > 0 ? (output.length() > 0 ? " + " : "") : "");
                    output.append(String.format(MessageManager.get("move.hitBoat"), hits, hits > 1 ? "s" : "", boatName));
                }
            }

            if (missedShots > 0) {
                output.append(missedShots > 0 ? (output.length() > 0 ? " + " : "") : "");
                output.append(String.format(MessageManager.get("move.waterShots"), missedShots, missedShots > 1 ? "s" : ""));
            }

            if (repeatedShots > 0) {
                output.append(repeatedShots > 0 ? (output.length() > 0 ? " + " : "") : "");
                output.append(String.format(MessageManager.get("move.repeatedShots"), repeatedShots, repeatedShots > 1 ? "s" : "", repeatedShots > 1 ? "s" : ""));
            }

            if (outsideShots > 0) {
                output.append(outsideShots > 0 ? (output.length() > 0 ? " + " : "") : "");
                output.append(String.format(MessageManager.get("move.outsideShots"), outsideShots, outsideShots > 1 ? "s" : "", outsideShots > 1 ? "es" : ""));
            }

            System.out.println(String.format(MessageManager.get("move.summary"), this.number, output));
        }

        // Construir JSON de resposta
        Map<String, Object> response = new HashMap<>();
        response.put("validShots", validShots);
        response.put("repeatedShots", repeatedShots);
        response.put("missedShots", missedShots);
        response.put("outsideShots", outsideShots);

        List<Map<String, Object>> sunkBoats = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : sunkBoatsCount.entrySet()) {
            Map<String, Object> boat = new HashMap<>();
            boat.put("type", entry.getKey());
            boat.put("count", entry.getValue());
            sunkBoats.add(boat);
        }
        response.put("sunkBoats", sunkBoats);

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

        try {
            return OBJECT_MAPPER.writeValueAsString(response);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(MessageManager.get("error.jsonMove"), e);
        }
    }

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper()
            .enable(SerializationFeature.INDENT_OUTPUT);
}