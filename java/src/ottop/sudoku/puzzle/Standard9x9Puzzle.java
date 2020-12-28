package ottop.sudoku.puzzle;

import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RowGroup;
import ottop.sudoku.group.SquareGroup;

import java.util.ArrayList;

public class Standard9x9Puzzle extends AbstractPuzzle {
    public Standard9x9Puzzle(String name,
                             String row1, String row2, String row3,
                             String row4, String row5, String row6,
                             String row7, String row8, String row9) {
        this(name,
                new String[]{" ", "1", "2", "3", "4", "5", "6", "7", "8", "9"},
                new String[]{row1,row2,row3,row4,row5,row6,row7,row8,row9});
    }

    protected Standard9x9Puzzle(String name, String[] symbols, int[][] board) {
        super(name);
        possibleSymbols = symbols;
        this.board = board;
        resetState();
    }

    protected Standard9x9Puzzle(String name, String[] symbols, String[] sudokuRows) {
        super(name);
        possibleSymbols = symbols;
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

    protected IPuzzle newInstance(String name, int[][] brd) {
        return new Standard9x9Puzzle(name, possibleSymbols, brd);
    }

    @Override
    public int getWidth() { return 9; }

    @Override
    public int getHeight() { return 9; }
}
