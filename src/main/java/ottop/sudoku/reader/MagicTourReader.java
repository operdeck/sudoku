package ottop.sudoku.reader;

import java.io.IOException;

// From http://magictour.free.fr/top95. The main focus of the site is some chess problem
// but this collection of Sudoku's seems to contain a nice mix of difficult but not
// impossible puzzles.
public class MagicTourReader extends SudokuResourceReader {
    public MagicTourReader() throws IOException {
        super("/top95.txt");
    }

    protected String[] split(String line) {
        return new String[]{line};
    }

    protected String getSudokuData(String[] flds) {
        return flds[0];
    }

    protected String getSudokuName(String[] flds) {
        return "Magic tour " + puzzleNo;
    }

    protected boolean isValidSudokuLine(String[] flds) {
        return flds.length == 1 && flds[0].length() == 81;
    }
}
