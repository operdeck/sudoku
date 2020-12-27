package ottop.sudoku.puzzle;

import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RowGroup;
import ottop.sudoku.group.SquareGroup;

import java.util.*;

public class Standard9x9Puzzle extends AbtractPuzzle {
    public static String TYPE = "Standard";
    protected static String[] symbols = {" ","1","2","3","4","5","6","7","8","9"};

    public Standard9x9Puzzle(String name,
                             String row1, String row2, String row3,
                             String row4, String row5, String row6,
                             String row7, String row8, String row9) {
        this(name, new String[]{row1,row2,row3,row4,row5,row6,row7,row8,row9});
    }

    protected Standard9x9Puzzle(String name, int[][] board) {
        super(name);
        possibleSymbols = Arrays.asList(symbols);
        this.board = board;
        resetState();
    }

    protected Standard9x9Puzzle(String name, String[] sudokuRows) {
        super(name);
        possibleSymbols = Arrays.asList(symbols);
        this.board = readSingleCharBoard(sudokuRows);
        resetState();
    }

    protected void initGroups() {
        groups = new ArrayList<>();

        int cnt = 0;
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                groups.add(new SquareGroup(x * 3, y * 3, this, "Group " + (++cnt)));
            }
        }
        for (int i = 0; i < 9; i++) {
            groups.add(new RowGroup(0, i, this, "Row " + (1 + i)));
            groups.add(new ColumnGroup(i, 0, this, "Column " + (1 + i)));
        }
    }

    protected int[][] readSingleCharBoard(String[] sudokuRows) {
        if (sudokuRows.length != getHeight())
            throw new IllegalArgumentException("Initialization must have " + getHeight() + " rows");

        int[][] brd = new int[getWidth()][getHeight()];

        for (int y=0; y<getHeight(); y++) {
            String s = sudokuRows[y];
            if (s.length() != getWidth())
                throw new IllegalArgumentException("Initialization must have " + getWidth() + " chars for each row");
            for (int x = 0; x<s.length(); x++) {
                String symbol = s.substring(x, x+1);
                brd[x][y] = symbolToSymbolCode(symbol);
            }
        }

        return brd;
    }

    @Override
    public int getSymbolCodeRange() {
        return 10;
    }

    @Override
    public int getWidth() { return 9; }

    @Override
    public int getHeight() { return 9; }

    protected IPuzzle newInstance(String name, int[][] brd) {
        return new Standard9x9Puzzle(name, brd);
    }

    @Override
    public String getSudokuType() { return TYPE; }
}
