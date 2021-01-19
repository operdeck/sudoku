package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.board.AbstractGroup;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class Explanation {
    protected final Set<String> symbols;

    // TODO: or just one Coord? And then we can have a map of Coord --> Reasons?

    protected final Set<Coord> coords;

    protected Explanation(String symbol, Set<Coord> coords) {
        this.symbols = new HashSet<>();
        this.symbols.add(symbol);
        this.coords = coords;
    }

    protected Explanation(Set<String> symbols, Coord coord) {
        this.symbols = symbols;
        this.coords = new HashSet<>();
        this.coords.add(coord);
    }

    protected Explanation(String symbol, Coord coord) {
        this.symbols = new HashSet<>();
        this.symbols.add(symbol);
        this.coords = new HashSet<>();
        this.coords.add(coord);
    }

    public String toString() {
        StringBuilder result = new StringBuilder("Removed ");
        if (symbols.size() > 1) {
            result.append(symbols);
        } else {
            result.append(symbols.iterator().next());
        }
//        result.append(" from ");
//        if (coords.size() > 1) {
//            result.append(coords);
//        } else {
//            result.append(coords.iterator().next());
//        }
        return result.toString();
    }

    public List<Explanation> combine(Coord coord, List<Explanation> eliminationReasons) {
        if (eliminationReasons == null) {
            eliminationReasons = new ArrayList<>();
        }
        eliminationReasons.add(this);
        return eliminationReasons;
    }

    // TODO: getReasonGroups / getReasonCells

    public List<AbstractGroup> getHighlightGroups() {
        return null;
    }

    public Set<Coord> getHighlightSubArea() {
        return null;
    }

    public abstract int getDifficulty();
}
