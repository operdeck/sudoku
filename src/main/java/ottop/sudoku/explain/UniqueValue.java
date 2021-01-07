package ottop.sudoku.explain;

import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;

import java.util.List;
import java.util.Set;

public class UniqueValue extends EliminationReason {
    List<AbstractGroup> uniqueInGroups;

    public UniqueValue(String s, Coord coord, List<AbstractGroup> uniqueInGroups) {
        super(s, coord);
        this.uniqueInGroups = uniqueInGroups;
    }

    @Override
    public int getDifficulty() {
        return 2;
    }

    public String toString() {
        StringBuilder result = new StringBuilder("Unique value: ").append(symbols.iterator().next());
        result.append(" in groups ").append(uniqueInGroups);
        return result.toString();
    }

    public List<AbstractGroup> getHighlightGroups() {
        return uniqueInGroups;
    }
    public Set<Coord> getHighlightSubArea() {
        return coords;
    }
}
