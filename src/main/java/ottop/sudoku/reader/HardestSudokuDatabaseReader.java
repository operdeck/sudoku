package ottop.sudoku.reader;

import java.io.IOException;

// Very hard puzzles - my solver currently does not crack any of them
// http://www.mediafire.com/?9ypndha1zadpwaw
// http://forum.enjoysudoku.com/the-hardest-sudokus-new-thread-t6539.html
// The latest Hardest sudokus Database can be downloaded by following
// this link: HardestDatabase110626.txt
class HardestSudokuDatabaseReader extends SudokuResourceReader {
    public HardestSudokuDatabaseReader() throws IOException {
        super("/HardestDatabase110626.txt");
    }

    protected String getSudokuData(String[] flds) {
        return flds[0];
    }

    protected String getSudokuName(String[] flds) {
        return flds[1] + ";" + flds[2] + " " + puzzleNo;
    }

    protected boolean isValidSudokuLine(String[] flds) {
        return flds.length == 10;
    }
}
