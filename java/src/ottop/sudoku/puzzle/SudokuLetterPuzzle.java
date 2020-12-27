package ottop.sudoku.puzzle;

import ottop.sudoku.puzzle.Standard9x9Puzzle;

public class SudokuLetterPuzzle extends Standard9x9Puzzle {
    public static String TYPE = "Letters";

    public SudokuLetterPuzzle(String name, String[] sudokuRows) {
        super(name, sudokuRows);
    }
}
