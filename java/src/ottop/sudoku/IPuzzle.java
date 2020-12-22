package ottop.sudoku;

import javafx.scene.paint.Paint;
import ottop.sudoku.group.AbstractGroup;

public interface IPuzzle {
    boolean isSolved();
    boolean isInconsistent();
    void resetState();

    String symbolCodeToSymbol(int n); // TODO iffy conversion from internal representation
    boolean isOccupied(int y, int x); // TODO use Coord
    String getSymbolAtCoordinates(int y, int x);
    int getSymbolCodeAtCoordinates(int y, int x);

    int getWidth();
    int getHeight();

    // doMove interface better be: Coord coord, String symbol
    IPuzzle doMove(int x, int y, String symbol);
    IPuzzle undoMove();
    boolean canUndo();

    AbstractGroup[] getGroups();
    GroupIntersection[] getIntersections(); // Used in some elimination algorithms

    String getName();
    String getSudokuType();

    Paint getCellBackground(int x, int y);
}
