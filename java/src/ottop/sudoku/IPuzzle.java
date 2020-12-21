package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;

import java.util.List;

public interface IPuzzle {
    boolean isSolved();
    boolean isInconsistent();
    void resetState();

    // better return String symbol
    char toChar(int n); // TODO iffy conversion from internal representation

    boolean isOccupied(int y, int x); // TODO use Coord
    char getValueAtCell(int y, int x);

    int getWidth();
    int getHeight();

    // doMove interface better be: Coord coord, String symbol
    IPuzzle doMove(int x, int y, char value);
    IPuzzle undoMove();
    boolean canUndo();

    AbstractGroup[] getGroups();
    GroupIntersection[] getIntersections();

    //PossibilitiesContainer getPossibilities(); // TODO by hint level
    AbstractGroup[] getSquareGroups(); // TODO only for UI

    String getName();
}
