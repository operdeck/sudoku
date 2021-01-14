package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;

import java.util.List;
import java.util.Set;

public class SimpleEliminationReason extends Explanation {

    private final AbstractGroup removedByGroup;

    public SimpleEliminationReason(String symbol, Coord removedFromCell, AbstractGroup removedByGroup) {
        super(symbol, removedFromCell);
        this.removedByGroup = removedByGroup;
    }

    public SimpleEliminationReason(Set<String> symbols, Coord removedFromCell, AbstractGroup removedByGroup) {
        super(symbols, removedFromCell);
        this.removedByGroup = removedByGroup;
    }

    // TODO: seems to be fairly expensive. We could introduce an EliminationReasons class that
    // keeps an internal cache of coord --> reason just for the simple reasons.
    // If there is another symbol at the same coord and by the same group, combine the symbols
    public List<Explanation> combine(List<Explanation> eliminationReasons) {
        if (eliminationReasons != null) {
            for (Explanation e : eliminationReasons) {
                if (e instanceof SimpleEliminationReason) {
                    if (e.coords.equals(this.coords) && ((SimpleEliminationReason) e).removedByGroup.equals(this.removedByGroup)) {
                        e.symbols.addAll(this.symbols);
                        return eliminationReasons;
                    }
                }
            }
        }

        return super.combine(eliminationReasons);
    }

    public String toString() {
        StringBuilder result = new StringBuilder(super.toString());
        result.append(" because of ").append(removedByGroup).append(" (Simple Elimination)");
        return result.toString();
    }

    public List<AbstractGroup> getHighlightGroups() {
        return List.of(removedByGroup);
    }

    @Override
    public int getDifficulty() {
        return 1;
    }

}
