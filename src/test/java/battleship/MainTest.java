package battleship;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MainTest {

    @DisplayName("Execução do main não lança exceções com input válido")
    @Test
    void testMainDoesNotThrowException() {
        String simulatedInput = "pt\n";
        System.setIn(new ByteArrayInputStream(simulatedInput.getBytes()));

        assertDoesNotThrow(() -> Main.main(new String[]{}));
    }
}