package ottop.sudoku.puzzle;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.ColumnGroup;
import ottop.sudoku.board.RectangularGroup;
import ottop.sudoku.board.RowGroup;

import java.util.ArrayList;

public class Sudoku10x10 extends AbstractSudoku {
    public Sudoku10x10(String name,
                       String row1, String row2,
                       String row3, String row4,
                       String row5, String row6,
                       String row7, String row8,
                       String row9, String row10) {
        this(name,
                new String[]{" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"},
                new String[]{row1, row2, row3, row4, row5, row6, row7, row8, row9, row10});
    }

    protected Sudoku10x10(String name, String[] symbols, String[] sudokuRows) {
        super(name, symbols, readCommaSeparatedBoard(sudokuRows, 10, 10, symbols));
    }

    @Override
    void initGroups() {
        int cnt = 0;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 2; x++) {
                AbstractGroup g = new RectangularGroup(x * 5, y * 2, this, "Rect " + (++cnt));
                groups.add(g);
                groupsWithBoundaries.add(g);
            }
        }
        for (int i = 0; i < 10; i++) {
            groups.add(new RowGroup(0, i, this));
            groups.add(new ColumnGroup(i, 0, this));
        }
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        if (name != null && name.length() > 0) result.append(name).append(":\n");
        for (int y = 0; y < getHeight(); y++) {
            if (y > 0 && ((y % 2) == 0)) {
                for (int x = 0; x < getWidth(); x++) {
                    result.append("==");
                    if (x < getWidth() - 1) result.append("+");
                    if (x == 5) result.append("+");
                }
                result.append("\n");
            }
            for (int x = 0; x < getWidth(); x++) {
                if (x > 0) result.append("|");
                if (x == 5) result.append("|"); // group sep
                String symbol = getSymbolAtCoordinates(new Coord(x, y));
                if (symbol.length() == 1) {
                    result.append(" ").append(getSymbolAtCoordinates(new Coord(x, y)));
                } else {
                    result.append(getSymbolAtCoordinates(new Coord(x, y)));
                }
            }
            result.append("\n");
        }
        return result.toString();
    }

    @Override
    public int getWidth() {
        return 10;
    }

    @Override
    public int getHeight() {
        return 10;
    }
}
