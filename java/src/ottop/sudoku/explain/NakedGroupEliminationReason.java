package ottop.sudoku.explain;

import ottop.sudoku.Coord;
import ottop.sudoku.group.AbstractGroup;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

public class NakedGroupEliminationReason extends EliminationReason {

    private final AbstractGroup removedFromGroup;
    private final boolean isExtended;
    private final Set<String> nakedPairSymbols;
    private final Set<Coord> nakedPairCells;

    public NakedGroupEliminationReason(Set<String> symbols,
                                       Coord removedFromCell,
                                       AbstractGroup removedFromGroup,
                                       Set<String> nakedPairSymbols,
                                       Set<Coord> nakedPairCells,
                                       boolean isExtended) {
        super(symbols, removedFromCell);
        this.removedFromGroup = removedFromGroup;
        this.nakedPairSymbols = nakedPairSymbols;
        this.nakedPairCells = nakedPairCells;
        this.isExtended = isExtended;
    }

    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(" in ").append(removedFromGroup);
        result.append(" because ").append(nakedPairSymbols).append(" have to be in ").append(nakedPairCells);
        if (!isExtended) result.append(" (Simple Naked Groups)");
        if (isExtended) result.append(" (Extended Naked Groups)");
        return result.toString();
    }

    public List<AbstractGroup> getHighlightGroups() {
        return List.of(removedFromGroup);
    }

    public Set<Coord> getHighlightSubArea() {
        return nakedPairCells;
    }
}