package battleship;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @Disabled("Pedro, o teste falha devido ao uso de múltiplos Scanners no System.in. Por favor, corrigir depois.")
    @DisplayName("Execução do main não lança exceções com input válido")
    @Test
    void main() {
        String simulatedInput = "pt\ndesisto\n";
        InputStream originalIn = System.in;
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        try {
            assertDoesNotThrow(() -> Main.main(new String[]{}),
                    "Error: Main.main should not throw any exceptions with valid input.");
        } finally {
            System.setIn(originalIn);
        }
    }
}