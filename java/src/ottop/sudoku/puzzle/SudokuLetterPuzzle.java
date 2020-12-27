package ottop.sudoku.puzzle;

import java.util.ArrayList;
import java.util.Arrays;

public class SudokuLetterPuzzle extends Standard9x9Puzzle {
    public SudokuLetterPuzzle(String name, String symbols,
                             String row1, String row2, String row3,
                             String row4, String row5, String row6,
                             String row7, String row8, String row9) {
        this(name, symbols, new String[]{row1,row2,row3,row4,row5,row6,row7,row8,row9});
    }
    protected SudokuLetterPuzzle(String name, String symbols, String[] sudokuRows) {
        super(name, stringToSymbolArray(symbols), sudokuRows);
    }
    private SudokuLetterPuzzle(String name, String[] symbols, int[][] board) {
        super(name, symbols, board);
    }
    private static String[] stringToSymbolArray(String s) {
        ArrayList<String> result = new ArrayList<>(Arrays.asList(s.split("")));
        result.add(0, " "); // add first symbol to represent empty cells

        return result.toArray(new String[0]);
    }
    @Override
    protected IPuzzle newInstance(String name, int[][] brd) {
        return new SudokuLetterPuzzle(name, possibleSymbols.toArray(new String[0]), brd);
    }
}
