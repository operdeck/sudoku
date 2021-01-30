package ottop.sudoku.reader;

import java.io.IOException;

// 1M (mostly easy) Sudoku's https://www.kaggle.com/bryanpark/sudoku
class Kaggle1MSudokuReader extends SudokuResourceReader {
    public Kaggle1MSudokuReader() throws IOException {
        super("/sudoku-kaggle.csv");
    }

    protected String getSudokuData(String[] flds) {
        return flds[0];
    }

    protected String getSudokuName(String[] flds) {
        return "Kaggle 1M Sudoku " + puzzleNo;
    }

    protected boolean isValidSudokuLine(String[] flds) {
        return flds.length == 2 && !"quizzes".equals(flds[0]);
    }
}
