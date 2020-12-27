package ottop.sudoku.puzzle;

import ottop.sudoku.puzzle.Standard9x9Puzzle;

public class Sudoku10x10Puzzle extends Standard9x9Puzzle {
    public static String TYPE = "10x10";
    protected static String[] symbols = {" ","1","2","3","4","5","6","7","8","9","10"};

    public Sudoku10x10Puzzle(String name, String[] sudokuRows) {
        super(name, sudokuRows);
    }
}
