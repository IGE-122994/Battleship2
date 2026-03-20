package battleship;

import com.lowagie.text.Chunk;
import com.lowagie.text.Document;
import com.lowagie.text.DocumentException;
import com.lowagie.text.Element;
import com.lowagie.text.Font;
import com.lowagie.text.PageSize;
import com.lowagie.text.Paragraph;
import com.lowagie.text.Phrase;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.lowagie.text.pdf.draw.LineSeparator;

import java.awt.Color;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Utility class responsible for generating a PDF report at the end of a Battleship game.
 *
 * <p>The report is automatically created when the game ends and includes:
 * <ul>
 *   <li>Game title and date/time of the session</li>
 *   <li>Overall game statistics (total moves, hits, sunk ships, total duration, etc.)</li>
 *   <li>A detailed log of every move played</li>
 *   <li>The final state of the board</li>
 * </ul>
 *
 * <p>The output file is saved to the {@code data/} directory with a timestamp-based filename,
 * e.g. {@code data/relatorio_20260318_1430.pdf}.
 *
 * <p>This class uses the OpenPDF library (version 1.3.32) for PDF generation.
 *
 * @author [o teu nome]
 * @version 1.0
 * @see Game
 * @see GameTimer
 */
public class GameReportPDF {

    /**
     * Directory where the generated PDF reports are saved.
     */
    private static final String OUTPUT_DIR = "data/";

    /**
     * Date/time formatter used for the PDF filename (e.g. {@code 20260318_1430}).
     */
    private static final DateTimeFormatter FILE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");

    /**
     * Date/time formatter used for the human-readable date inside the PDF
     * (e.g. {@code 18/03/2026 14:30}).
     */
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private static final Font FONT_TITLE   = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font FONT_NORMAL  = new Font(Font.HELVETICA, 11, Font.NORMAL);
    private static final Font FONT_MONO    = new Font(Font.COURIER,   10, Font.NORMAL);
    private static final Font FONT_SMALL   = new Font(Font.HELVETICA,  9, Font.ITALIC);

    /**
     * Generates a PDF report for the given completed game and saves it to the {@code data/} directory.
     *
     * <p>The report includes the game statistics (including total duration), a move-by-move log,
     * and the final board state. If the output directory does not exist, it is created automatically.
     * If an error occurs during generation, a message is printed to {@code stderr} and the method
     * returns the intended filename regardless.
     *
     * @param game          the completed {@link Game} instance to report on; must not be {@code null}
     * @param totalDuration a human-readable string representing the total duration of the game
     *                      (e.g. {@code "2m 36s 469ms"}), as produced by
     *                      {@link GameTimer#formatDuration(org.joda.time.Duration)}
     * @return the path of the generated PDF file (e.g. {@code data/relatorio_20260318_1430.pdf})
     */
    public static String generate(Game game, String totalDuration) {

        LocalDateTime now = LocalDateTime.now();
        String filename = OUTPUT_DIR + "relatorio_" + now.format(FILE_FORMATTER) + ".pdf";

        try {
            boolean ignored = new java.io.File(OUTPUT_DIR).mkdirs();

            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            addTitle(document, now);
            addStatistics(document, game, totalDuration);
            addMovesSummary(document, game);
            addFinalBoard(document, game);
            addFooter(document);

            document.close();
            System.out.println("Relatorio PDF gerado: " + filename);

        } catch (DocumentException | IOException e) {
            System.err.println("Erro ao gerar o PDF: " + e.getMessage());
        }

        return filename;
    }

    // -----------------------------------------------------------------------

    /**
     * Adds the title section to the document, including the game title and the
     * date/time of the session.
     *
     * @param doc the PDF {@link Document} to write to
     * @param now the date and time of the game session
     * @throws DocumentException if an error occurs while adding content to the document
     */
    private static void addTitle(Document doc, LocalDateTime now) throws DocumentException {
        Paragraph title = new Paragraph("Batalha Naval - Relatorio de Jogo", FONT_TITLE);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(4);
        doc.add(title);

        Paragraph date = new Paragraph("Data: " + now.format(DISPLAY_FORMATTER), FONT_SMALL);
        date.setAlignment(Element.ALIGN_CENTER);
        date.setSpacingAfter(16);
        doc.add(date);

        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Adds the game statistics section to the document.
     *
     * <p>Includes total moves, sunk ships, floating ships, hits, shots outside
     * the board, repeated shots and the total duration of the game.
     *
     * @param doc           the PDF {@link Document} to write to
     * @param game          the {@link Game} instance containing the statistics
     * @param totalDuration a human-readable string representing the total game duration
     * @throws DocumentException if an error occurs while adding content to the document
     */
    private static void addStatistics(Document doc, Game game, String totalDuration) throws DocumentException {
        doc.add(new Paragraph("Estatisticas do Jogo", FONT_SECTION));
        doc.add(Chunk.NEWLINE);

        IFleet fleet = game.getMyFleet();
        List<IMove> moves = game.getAlienMoves();

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(60);
        table.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.setSpacingAfter(16);

        addTableRow(table, "Total de rajadas",       String.valueOf(moves.size()));
        addTableRow(table, "Navios afundados",        String.valueOf(fleet.getSunkShips().size()));
        addTableRow(table, "Navios a flutuar",        String.valueOf(fleet.getFloatingShips().size()));
        addTableRow(table, "Total de acertos",        String.valueOf(game.getHits()));
        addTableRow(table, "Tiros fora do tabuleiro", String.valueOf(game.getInvalidShots()));
        addTableRow(table, "Tiros repetidos",         String.valueOf(game.getRepeatedShots()));
        addTableRow(table, "Duracao total do jogo",   totalDuration);

        doc.add(table);
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Adds a move-by-move log to the document.
     *
     * <p>Each row in the table shows the move number, the shot positions in classic
     * notation (e.g. A1, J10), and a summary of the outcome (hits, sunk ships,
     * missed shots, etc.). If no moves were played, a placeholder message is shown instead.
     *
     * @param doc  the PDF {@link Document} to write to
     * @param game the {@link Game} instance containing the move history
     * @throws DocumentException if an error occurs while adding content to the document
     */
    private static void addMovesSummary(Document doc, Game game) throws DocumentException {
        doc.add(new Paragraph("Registo de Jogadas", FONT_SECTION));
        doc.add(Chunk.NEWLINE);

        List<IMove> moves = game.getAlienMoves();

        if (moves.isEmpty()) {
            doc.add(new Paragraph("Nenhuma jogada registada.", FONT_NORMAL));
        } else {
            PdfPTable table = new PdfPTable(3);
            table.setWidthPercentage(85);
            table.setSpacingAfter(16);

            addTableHeader(table, "Rajada");
            addTableHeader(table, "Tiros");
            addTableHeader(table, "Resultado");

            for (IMove move : moves) {
                table.addCell(cell(String.valueOf(move.getNumber()), FONT_NORMAL));

                StringBuilder positions = new StringBuilder();
                for (IPosition pos : move.getShots()) {
                    if (!positions.isEmpty()) positions.append(", ");
                    positions.append((char)('A' + pos.getRow())).append(pos.getColumn() + 1);
                }
                table.addCell(cell(positions.toString(), FONT_MONO));
                table.addCell(cell(summariseMove(move), FONT_NORMAL));
            }
            doc.add(table);
        }

        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);
    }

    /**
     * Adds the final board state to the document in ASCII representation.
     *
     * <p>The board uses the following symbols:
     * <ul>
     *   <li>{@code #} — ship position</li>
     *   <li>{@code *} — successful hit</li>
     *   <li>{@code o} — shot in water (miss)</li>
     *   <li>{@code -} — position adjacent to a sunk ship</li>
     *   <li>{@code .} — empty water</li>
     * </ul>
     *
     * @param doc  the PDF {@link Document} to write to
     * @param game the {@link Game} instance containing the fleet and move history
     * @throws DocumentException if an error occurs while adding content to the document
     */
    private static void addFinalBoard(Document doc, Game game) throws DocumentException {
        doc.add(new Paragraph("Estado Final do Tabuleiro", FONT_SECTION));
        doc.add(Chunk.NEWLINE);

        IFleet fleet = game.getMyFleet();
        List<IMove> moves = game.getAlienMoves();

        int size = Game.BOARD_SIZE;
        char[][] map = new char[size][size];
        for (int r = 0; r < size; r++)
            for (int c = 0; c < size; c++)
                map[r][c] = '.';

        for (IShip ship : fleet.getShips()) {
            for (IPosition p : ship.getPositions())
                map[p.getRow()][p.getColumn()] = '#';
            if (!ship.stillFloating())
                for (IPosition p : ship.getAdjacentPositions())
                    map[p.getRow()][p.getColumn()] = '-';
        }

        for (IMove move : moves)
            for (IPosition shot : move.getShots())
                if (shot.isInside()) {
                    int r = shot.getRow(), c = shot.getColumn();
                    if (map[r][c] == '#') map[r][c] = '*';
                    else if (map[r][c] == '.' || map[r][c] == '-') map[r][c] = 'o';
                }

        StringBuilder sb = new StringBuilder();
        sb.append("      1  2  3  4  5  6  7  8  9  10\n");
        sb.append("   +--------------------------------+\n");
        for (int r = 0; r < size; r++) {
            sb.append(" ").append((char)('A' + r)).append(" |");
            for (int c = 0; c < size; c++)
                sb.append("  ").append(map[r][c]);
            sb.append("  |\n");
        }
        sb.append("   +--------------------------------+\n");
        sb.append("\n  # navio   * tiro certeiro   o tiro na agua   - adjacente");

        Paragraph board = new Paragraph(sb.toString(), FONT_MONO);
        board.setSpacingAfter(16);
        doc.add(board);
    }

    /**
     * Adds a footer to the document identifying the application that generated the report.
     *
     * @param doc the PDF {@link Document} to write to
     * @throws DocumentException if an error occurs while adding content to the document
     */
    private static void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("Gerado por Battleship2 - ISCTE-IUL", FONT_SMALL);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    // -----------------------------------------------------------------------
    // Helpers

    /**
     * Adds a label-value row to a two-column table, without borders.
     *
     * @param table the {@link PdfPTable} to add the row to
     * @param label the left-column label
     * @param value the right-column value
     */
    private static void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, FONT_NORMAL));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingBottom(4);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, FONT_NORMAL));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(4);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    /**
     * Adds a styled header cell to a table with a dark blue background and white text.
     *
     * @param table the {@link PdfPTable} to add the header cell to
     * @param text  the header text to display
     */
    private static void addTableHeader(PdfPTable table, String text) {
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new Color(50, 90, 140));
        cell.setPadding(5);
        table.addCell(cell);
    }

    /**
     * Creates a styled table cell with the given text and font.
     *
     * @param text the cell content
     * @param font the {@link Font} to apply
     * @return a configured {@link PdfPCell}
     */
    private static PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(4);
        return c;
    }

    /**
     * Produces a human-readable summary of a single move's outcome.
     *
     * <p>Counts hits, sunk ships, missed shots, repeated shots and shots outside
     * the board, and returns them as a compact string
     * (e.g. {@code "2 acerto(s) (1 afundado(s)) 1 agua"}).
     * Returns {@code "-"} if no results are available.
     *
     * @param move the {@link IMove} to summarise
     * @return a string describing the outcome of the move
     */
    private static String summariseMove(IMove move) {
        int hits = 0, misses = 0, sunk = 0, repeated = 0, outside = 0;
        for (IGame.ShotResult r : move.getShotResults()) {
            if (!r.valid())            outside++;
            else if (r.repeated())     repeated++;
            else if (r.ship() == null) misses++;
            else {
                hits++;
                if (r.sunk()) sunk++;
            }
        }
        StringBuilder s = new StringBuilder();
        if (hits     > 0) s.append(hits).append(" acerto(s) ");
        if (sunk     > 0) s.append("(").append(sunk).append(" afundado(s)) ");
        if (misses   > 0) s.append(misses).append(" agua ");
        if (repeated > 0) s.append(repeated).append(" repetido(s) ");
        if (outside  > 0) s.append(outside).append(" exterior ");
        return s.isEmpty() ? "-" : s.toString().trim();
    }
}