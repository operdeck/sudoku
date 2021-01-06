package ottop.sudoku.reader;

import ottop.sudoku.SudokuMain;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.StandardSudoku;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public abstract class SudokuResourceReader implements  SudokuReader {
    ISudoku next = null;
    private final BufferedReader reader;
    public int puzzleNo = 0;

    public SudokuResourceReader(String path) {
        InputStream contentStream = SudokuMain.class.getResourceAsStream(path);
        reader = new BufferedReader(new InputStreamReader(contentStream));
    }

    @Override
    public boolean hasNext() throws IOException {
        String line;
        next = null;
        while ((line = reader.readLine()) != null) {
            String[] flds = split(line);
            if (isValidSudokuLine(flds)) {
                puzzleNo++;
                next = new StandardSudoku(getSudokuName(flds), getSudokuData(flds));
                break;
            }
        }
        if (next == null) {
            reader.close();
        }
        return next != null;
    }

    protected String[] split(String line) {
        return line.split(",");
    }

    protected abstract String getSudokuData(String[] flds);

    protected abstract String getSudokuName(String[] flds);

    protected abstract boolean isValidSudokuLine(String[] flds);

    @Override
    public ISudoku next() {
        return next;
    }
}

