package ottop.sudoku.explain;

import ottop.sudoku.Coord;
import ottop.sudoku.group.AbstractGroup;

import java.util.Set;

public class XWingEliminationReason extends EliminationReason {

    private final AbstractGroup removedFromGroup;
    private final Set<AbstractGroup> groups1;
    private final Set<AbstractGroup> groups2;

    public XWingEliminationReason(String symbol,
                                  Set<Coord> removedFromCells,
                                  AbstractGroup removedFromGroup,
                                  Set<AbstractGroup> groups1,
                                  Set<AbstractGroup> groups2) {
        super(symbol, removedFromCells);
        this.removedFromGroup = removedFromGroup;
        this.groups1 = groups1;
        this.groups2 = groups2;
    }

//                                " of " + g + " because " + myPuzzle.symbolCodeToSymbol(symbolCode) + " has to be in " +
//                                        entry.getKey() + " X " + entry.getValue() + " (X-Wing)")) updated = true;

    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(" of ").append(removedFromGroup).append(" because it has to be in the intersections of ");
        result.append(groups1).append(" X ").append(groups2).append(" (X-Wing)");
        return result.toString();
    }
}
