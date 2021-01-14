package ottop.sudoku.puzzle;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.solver.Updateable;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface ISudoku extends Cloneable {
    ISudoku clone();

    boolean isComplete();

    boolean isInconsistent();

    default boolean isSolved() {
        return isComplete() && !isInconsistent();
    }

    String symbolCodeToSymbol(int n);

    int symbolToSymbolCode(String symbol);

    boolean isOccupied(Coord coord);

    String getSymbolAtCoordinates(Coord coord);

    int getSymbolCodeAtCoordinates(Coord coord);

    int getSymbolCodeRange(); // for standard 9x9 puzzle will return 10 as 0 is always for empty cells

    int getWidth(); // will be 9 for standard puzzle

    int getHeight();

    Coord[] getAllCells();

    boolean doMove(Coord coord, String symbol);

    Map.Entry<Coord, String> undoMove();

    Map.Entry<Coord, String> redoMove();

    boolean canUndo();

    boolean canRedo();

    AbstractGroup[] getGroups();

    AbstractGroup[] getBuddyGroups(Coord coord);

    Set<Coord> getBuddies(Coord coord);

    String getName();
    
    boolean isAtOverlay(Coord c);

    List<AbstractGroup> getGroupsWithVisualBoundary();

    void setSolver(Updateable solver);
}
