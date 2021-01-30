package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NakedGroupEliminationReason extends Explanation {

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
        String groupLabel = "Group";
        if (nakedPairCells.size()==2) groupLabel = "Pair";
        if (nakedPairCells.size()==3) groupLabel = "Trio";
        if (nakedPairCells.size()==4) groupLabel = "Quad";

        if (!isExtended) result.append(" (Simple Naked ").append(groupLabel).append(")");
        if (isExtended) result.append(" (Extended Naked ").append(groupLabel).append(")");
        return result.toString();
    }

    public List<AbstractGroup> getHighlightGroups() {
        return List.of(removedFromGroup);
    }

    public Map<String, Set<Coord>> getHighlightCells() {
        return getHighlightCells(nakedPairCells);
    }

    @Override
    public int getDifficulty() {
        return isExtended ? 4 : 3;
    }
}