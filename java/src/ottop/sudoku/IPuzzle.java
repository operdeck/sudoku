package ottop.sudoku;

import javafx.scene.paint.Paint;
import ottop.sudoku.group.AbstractGroup;

public interface IPuzzle {
    boolean isSolved();
    boolean isInconsistent();
    void resetState();

    String symbolCodeToSymbol(int n);
    boolean isOccupied(Coord coord);
    String getSymbolAtCoordinates(Coord coord);
    int getSymbolCodeAtCoordinates(Coord coord);

    int getWidth();
    int getHeight();

    IPuzzle doMove(Coord coord, String symbol);
    IPuzzle undoMove();
    boolean canUndo();

    AbstractGroup[] getGroups();

    String getName();
    String getSudokuType();

    Paint getCellBackground(int x, int y);
}
