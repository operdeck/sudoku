package ottop.sudoku.puzzle;

import java.util.ArrayList;
import java.util.Arrays;

public class LetterSudoku extends StandardSudoku {
    public LetterSudoku(String name, String symbols,
                        String row1, String row2, String row3,
                        String row4, String row5, String row6,
                        String row7, String row8, String row9) {

        super(name, stringToSymbolArray(symbols),
                new String[]{row1, row2, row3, row4, row5, row6, row7, row8, row9});
    }

    // For cloning to new puzzle
    private LetterSudoku(String name, String[] symbols, int[][] board) {
        super(name, symbols, board);
    }

    private static String[] stringToSymbolArray(String s) {
        ArrayList<String> result = new ArrayList<>(Arrays.asList(s.split("")));
        if (result.size() != 9) {
            throw new IllegalArgumentException("Letter puzzle symbol list must have " + 9 + " symbols");
        }
        result.add(0, " "); // add first symbol to represent empty cells

        return result.toArray(new String[0]);
    }

    @Override
    protected ISudoku newInstance(String name, int[][] brd) {
        return new LetterSudoku(name, possibleSymbols, brd);
    }
}
