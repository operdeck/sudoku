package ottop.sudoku.puzzle;

import javafx.scene.canvas.Canvas;
import ottop.sudoku.Coord;
import ottop.sudoku.group.AbstractGroup;

import java.util.List;
import java.util.Set;

public interface IPuzzle {
    boolean isSolved();

    boolean isInconsistent();

    void resetState(); // a puzzle is a board with a set of occupied cells

    String symbolCodeToSymbol(int n);

    int symbolToSymbolCode(String symbol);

    boolean isOccupied(Coord coord);

    String getSymbolAtCoordinates(Coord coord);

    int getSymbolCodeAtCoordinates(Coord coord);

    int getSymbolCodeRange(); // for standard 9x9 puzzle will return 10 as 0 is always for empty cells

    int getWidth(); // will be 9 for standard puzzle

    int getHeight();

    Coord[] getAllCells();

    IPuzzle doMove(Coord coord, String symbol);

    IPuzzle undoMove();

    boolean canUndo();

    AbstractGroup[] getGroups();

    List<AbstractGroup> getGroups(Coord coord);

    String getName();

    void drawPuzzleOnCanvas(Canvas canvas, Coord highlight, Set<Coord> currentHighlightedSubArea);

    void drawGroup(Canvas canvas, AbstractGroup g);

    void drawPossibilities(Canvas canvas, Coord c, Set<Integer> values);

}
