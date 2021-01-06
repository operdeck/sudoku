package ottop.sudoku.puzzle;

// The X-Sudoku has the two main diagonals as extra regions. These two main
// diagonals must contain all of the digits from 1 to 9 only once too.

public class XSudoku extends StandardSudoku {
    public XSudoku(String name, String row1, String row2, String row3, String row4, String row5, String row6, String row7, String row8, String row9) {
        super(name, row1, row2, row3, row4, row5, row6, row7, row8, row9);
    }
}
