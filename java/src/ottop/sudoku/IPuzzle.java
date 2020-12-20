package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;

import java.util.List;

public interface IPuzzle {
    boolean isSolved();
    boolean isInconsistent();

    char toChar(int n); // TODO iffy conversion from internal representation
    boolean isOccupied(int y, int x);
    char getValueAtCell(int y, int x);

    int getWidth();
    int getHeight();

    IPuzzle doMove(int x, int y, char value);
    IPuzzle undoMove();
    boolean canUndo();

    AbstractGroup[] getGroups();
    GroupIntersection[] getIntersections();

    //PossibilitiesContainer getPossibilities(); // TODO by hint level
    List<AbstractGroup> getSquareGroups(); // TODO only for UI
}
