package ottop.sudoku.explain;

import ottop.sudoku.Coord;
import ottop.sudoku.group.AbstractGroup;

import java.util.Set;

public class NakedPairEliminationReason extends EliminationReason {

    private final AbstractGroup removedFromGroup;
    private final boolean isExtendedNakedPairElimination;
    private final Set<String> nakedPairSymbols;
    private final Set<Coord> nakedPairCells;

    public NakedPairEliminationReason(Set<String> symbols,
                                      Coord removedFromCell,
                                      AbstractGroup removedFromGroup,
                                      Set<String> nakedPairSymbols,
                                      Set<Coord> nakedPairCells,
                                      boolean isExtendedNakedPairElimination) {
        super(symbols, removedFromCell);
        this.removedFromGroup = removedFromGroup;
        this.nakedPairSymbols = nakedPairSymbols;
        this.nakedPairCells = nakedPairCells;
        this.isExtendedNakedPairElimination = isExtendedNakedPairElimination;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(" in ").append(removedFromGroup);
        result.append(" because ").append(nakedPairSymbols).append(" have to be in ").append(nakedPairCells);
        if (!isExtendedNakedPairElimination) result.append(" (Simple Naked Pairs)");
        if (isExtendedNakedPairElimination) result.append(" (Extended Naked Pairs)");
        return result.toString();
    }

}