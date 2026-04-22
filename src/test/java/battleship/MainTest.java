package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

/**
 * Test class for Main.
 * Author: Pedro Vicêncio
 * Date: 2026-04-23
 * Time: 14:00
 * Cyclomatic Complexity for each method:
 * - main: 1
 */
class MainTest {

    @DisplayName("Execução do main não lança exceções com input válido")
    @Test
    void main() {
        String simulatedInput = "pt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}), "Error: Main.main should not throw any exceptions with valid input.");
    }
}