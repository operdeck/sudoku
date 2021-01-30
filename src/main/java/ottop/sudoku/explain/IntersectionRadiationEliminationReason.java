package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class IntersectionRadiationEliminationReason extends Explanation {

    private final AbstractGroup mustBeInGroup;
    private final AbstractGroup removedFromGroup;
    private final Set<Coord> intersection;

    public IntersectionRadiationEliminationReason(String symbol,
                                                  Set<Coord> removedFromCells,
                                                  AbstractGroup mustBeInGroup,
                                                  AbstractGroup removedFromGroup,
                                                  Set<Coord> intersection) {
        super(symbol, removedFromCells);
        this.mustBeInGroup = mustBeInGroup;
        this.removedFromGroup = removedFromGroup;
        this.intersection = intersection;
    }
//                                    " (in " + intersection.getIntersectionGroup(1 - i) + ") because " +
//                                            myPuzzle.symbolCodeToSymbol(symbolCode) + " has to be in " +
//                                            intersection.getIntersectionGroup(i) + " in one of " +
//                                            intersection + " (Intersection Radiation)")) updated = true;

    // TODO: doesnt work well, combines too many reasons

//    public List<Explanation> combine(Coord coord, List<Explanation> eliminationReasons) {
//        if (eliminationReasons != null) {
//            for (Explanation e : eliminationReasons) {
//                if (e instanceof IntersectionRadiationEliminationReason) {
//                    IntersectionRadiationEliminationReason re = (IntersectionRadiationEliminationReason) e;
//                    if (e.coords.equals(this.coords) &&
//                            re.mustBeInGroup.equals(this.mustBeInGroup) &&
//                            re.removedFromGroup.equals(this.removedFromGroup)) {
//                        e.symbols.addAll(this.symbols);
//                        return eliminationReasons;
//                    }
//                }
//            }
//        }
//
//        return super.combine(coord, eliminationReasons);
//    }

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

    public Map<String, Set<Coord>> getHighlightCells() {
        return getHighlightCells(intersection);
    }

    @Override
    public int getDifficulty() {
        return 2;
    }
}
