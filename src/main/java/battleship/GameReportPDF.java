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
 * Generates a PDF report of a completed Battleship game.
 * The report includes game statistics, the final board state,
 * and a summary of all moves played.
 */
public class GameReportPDF {

    private static final String OUTPUT_DIR = "data/";
    private static final DateTimeFormatter FILE_FORMATTER    = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
    private static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // Fonts
    private static final Font FONT_TITLE   = new Font(Font.HELVETICA, 18, Font.BOLD);
    private static final Font FONT_SECTION = new Font(Font.HELVETICA, 13, Font.BOLD);
    private static final Font FONT_NORMAL  = new Font(Font.HELVETICA, 11, Font.NORMAL);
    private static final Font FONT_MONO    = new Font(Font.COURIER,   10, Font.NORMAL);
    private static final Font FONT_SMALL   = new Font(Font.HELVETICA,  9, Font.ITALIC);

    /**
     * Generates and saves the PDF report for the given game.
     *
     * @param game the completed game instance
     * @return the path of the generated PDF file
     */
    public static String generate(Game game) {

        LocalDateTime now = LocalDateTime.now();
        String filename = OUTPUT_DIR + "relatorio_" + now.format(FILE_FORMATTER) + ".pdf";

        try {
            boolean ignored = new java.io.File(OUTPUT_DIR).mkdirs();

            Document document = new Document(PageSize.A4, 50, 50, 60, 60);
            PdfWriter.getInstance(document, new FileOutputStream(filename));
            document.open();

            addTitle(document, now);
            addStatistics(document, game);
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

    private static void addStatistics(Document doc, Game game) throws DocumentException {
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

        doc.add(table);
        doc.add(new LineSeparator());
        doc.add(Chunk.NEWLINE);
    }

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

    private static void addFooter(Document doc) throws DocumentException {
        doc.add(Chunk.NEWLINE);
        Paragraph footer = new Paragraph("Gerado por Battleship2 - ISCTE-IUL", FONT_SMALL);
        footer.setAlignment(Element.ALIGN_CENTER);
        doc.add(footer);
    }

    // -----------------------------------------------------------------------
    // Helpers

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

    private static void addTableHeader(PdfPTable table, String text) {
        Font headerFont = new Font(Font.HELVETICA, 11, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(new Color(50, 90, 140));
        cell.setPadding(5);
        table.addCell(cell);
    }

    private static PdfPCell cell(String text, Font font) {
        PdfPCell c = new PdfPCell(new Phrase(text, font));
        c.setPadding(4);
        return c;
    }

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