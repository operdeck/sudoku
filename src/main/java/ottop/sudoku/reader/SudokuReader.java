package ottop.sudoku.reader;

import ottop.sudoku.puzzle.ISudoku;

import java.io.IOException;

public interface SudokuReader {
    boolean hasNext() throws IOException;
    ISudoku next();
}
