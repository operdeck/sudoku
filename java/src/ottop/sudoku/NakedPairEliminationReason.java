package ottop.sudoku;

import ottop.sudoku.group.AbstractGroup;

import java.util.Set;

public class NakedPairEliminationReason extends EliminationReason {

    private final AbstractGroup removedFromGroup;
    private final boolean isExtendedNakedPairElimination;
    private final Set<Coord> nakedPairCells;

    public NakedPairEliminationReason(Set<String> symbols,
                                                  Set<Coord> removedFromCells,
                                                  AbstractGroup removedFromGroup,
                                      Set<Coord> nakedPairCells,
                                      boolean isExtendedNakedPairElimination)
    {
        super(symbols, removedFromCells);
        this.removedFromGroup = removedFromGroup;
        this.nakedPairCells = nakedPairCells;
        this.isExtendedNakedPairElimination = isExtendedNakedPairElimination;
    }
//                    if (cache.removePossibilities(possibilities, c, " in " + groupID +
//            " because " + possibilities +
//            " have to be in one of " + coordinates + " (" + reason + ")")) {

    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(" of ").append(removedFromGroup);
        result.append(" because they have to be in ").append(nakedPairCells);
        if (!isExtendedNakedPairElimination) result.append(" (Simple Naked Pairs)");
        if (isExtendedNakedPairElimination) result.append(" (Extended Naked Pairs)");
        return result.toString();
    }

}