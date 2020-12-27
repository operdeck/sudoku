package ottop.sudoku.puzzle;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import ottop.sudoku.Coord;
import ottop.sudoku.group.ColumnGroup;
import ottop.sudoku.group.RectangularGroup;
import ottop.sudoku.group.RowGroup;
import ottop.sudoku.group.SquareGroup;

import java.util.ArrayList;
import java.util.Arrays;

public class Sudoku10x10Puzzle extends AbtractPuzzle {
    public Sudoku10x10Puzzle(String name,
                             String row1, String row2,
                             String row3, String row4,
                             String row5, String row6,
                             String row7, String row8,
                             String row9, String row10) {
       this(name,
                new String[]{" ", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"},
                new String[]{row1,row2,row3,row4,row5,row6,row7,row8,row9,row10});
    }

    protected Sudoku10x10Puzzle(String name, String[] symbols, int[][] board) {
        super(name);
        possibleSymbols = Arrays.asList(symbols);
        this.board = board;
        resetState();
    }

    protected Sudoku10x10Puzzle(String name, String[] symbols, String[] sudokuRows) {
        super(name);
        possibleSymbols = Arrays.asList(symbols);
        this.board = new int[10][10];
        this.board = readCommaSeparatedBoard(sudokuRows);
        resetState();
    }

    @Override
    void initGroups() {
        groups = new ArrayList<>();

        int cnt = 0;
        for (int y = 0; y < 5; y++) {
            for (int x = 0; x < 2; x++) {
                groups.add(new RectangularGroup(x * 5, y * 2, this, "Rect " + (++cnt)));
            }
        }
        for (int i = 0; i < 10; i++) {
            groups.add(new RowGroup(0, i, this, "Row " + (1 + i)));
            groups.add(new ColumnGroup(i, 0, this, "Column " + (1 + i)));
        }
    }

    @Override
    protected IPuzzle newInstance(String name, int[][] brd) {
        return new Sudoku10x10Puzzle(name, possibleSymbols.toArray(new String[0]), brd);
    }

    @Override
    public int getSymbolCodeRange() {
        return 11;
    }

    @Override
    public int getWidth() {
        return 10;
    }

    @Override
    public int getHeight() {
        return 10;
    }

    // TODO: this can be fairly generic based on the Square or Rectangular groups
    @Override
    protected void drawGroupBoundaries(Canvas canvas, GraphicsContext gc) {
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        for (int x=0; x<2; x++) {
            for (int y = 0; y<5; y++) {
                gc.strokeRect(getCellX(canvas,x*5), getCellY(canvas,y*2), 5*getCellWidth(canvas), 2*getCellHeight(canvas));
            }
        }
    }

    @Override
    public String toString() {
        StringBuffer result = new StringBuffer();

        if (name != null && name.length() > 0) result.append(name).append(":\n");
        for (int y = 0; y < getHeight(); y++) {
            if (y > 0 && ((y % 2) == 0)) {
                for (int x = 0; x < getWidth(); x++) {
                    result.append("==");
                    if (x < getWidth()-1) result.append("+");
                    if (x==5) result.append("+");
                }
                result.append("\n");
            }
            for (int x = 0; x < getWidth(); x++) {
                if (x>0) result.append("|");
                if (x==5) result.append("|"); // group sep
                String symbol = getSymbolAtCoordinates(new Coord(x,y));
                if (symbol.length() == 1) {
                    result.append(" ").append(getSymbolAtCoordinates(new Coord(x,y)));
                } else {
                    result.append(getSymbolAtCoordinates(new Coord(x,y)));
                }
            }
            result.append("\n");
        }
        return result.toString();
    }
}
