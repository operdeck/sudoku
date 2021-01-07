package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.solver.GroupIntersection;
import ottop.sudoku.board.AbstractGroup;

import java.util.List;
import java.util.Set;

public class IntersectionRadiationEliminationReason extends EliminationReason {

    private final AbstractGroup mustBeInGroup;
    private final AbstractGroup removedFromGroup;
    private final GroupIntersection intersection;

    public IntersectionRadiationEliminationReason(String symbol,
                                                  Set<Coord> removedFromCells,
                                                  AbstractGroup mustBeInGroup,
                                                  AbstractGroup removedFromGroup,
                                                  GroupIntersection intersection) {
        super(symbol, removedFromCells);
        this.mustBeInGroup = mustBeInGroup;
        this.removedFromGroup = removedFromGroup;
        this.intersection = intersection;
    }
//                                    " (in " + intersection.getIntersectionGroup(1 - i) + ") because " +
//                                            myPuzzle.symbolCodeToSymbol(symbolCode) + " has to be in " +
//                                            intersection.getIntersectionGroup(i) + " in one of " +
//                                            intersection + " (Intersection Radiation)")) updated = true;

    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(" because it has to be in the intersection of ")
                .append(removedFromGroup).append(" with ")
                .append(mustBeInGroup).append(" (Intersection Radiation)");
        return result.toString();
    }

    public List<AbstractGroup> getHighlightGroups() {
        return List.of(mustBeInGroup, removedFromGroup);
    }

    public Set<Coord> getHighlightSubArea() {
        return intersection.getIntersection();
    }

    @Override
    public int getDifficulty() {
        return 2;
    }
}
