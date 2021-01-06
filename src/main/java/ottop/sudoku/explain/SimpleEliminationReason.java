package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;

import java.util.List;

public class SimpleEliminationReason extends EliminationReason {

    private final AbstractGroup removedByGroup;

    public SimpleEliminationReason(String symbol, Coord removedFromCell, AbstractGroup removedByGroup) {
        super(symbol, removedFromCell);
        this.removedByGroup = removedByGroup;
    }

    // If there is another symbol at the same coord and by the same group, combine the symbols
    public List<EliminationReason> combine(List<EliminationReason> eliminationReasons) {
        if (eliminationReasons != null) {
            for (EliminationReason e : eliminationReasons) {
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
