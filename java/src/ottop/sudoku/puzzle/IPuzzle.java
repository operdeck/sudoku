package ottop.sudoku.puzzle;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Paint;
import ottop.sudoku.Coord;
import ottop.sudoku.group.AbstractGroup;

import java.util.Set;

public interface IPuzzle {
    boolean isSolved();
    boolean isInconsistent();
    void resetState();

    String symbolCodeToSymbol(int n);
    int symbolToSymbolCode(String symbol);
    boolean isOccupied(Coord coord);
    String getSymbolAtCoordinates(Coord coord);
    int getSymbolCodeAtCoordinates(Coord coord);
    int getSymbolCodeRange(); // for standard 9x9 puzzle will return 10 as 0 is always for empty cells

    int getWidth();
    int getHeight();

    IPuzzle doMove(Coord coord, String symbol);
    IPuzzle undoMove();
    boolean canUndo();

    AbstractGroup[] getGroups();

    String getName();
    String getSudokuType();

    void drawPuzzleOnCanvas(Canvas canvas, Coord highlight);
    void drawPossibilities(Canvas canvas, Coord c, Set<Integer> values);
}
