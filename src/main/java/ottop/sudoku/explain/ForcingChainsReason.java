package ottop.sudoku.explain;

import ottop.sudoku.board.Coord;
import ottop.sudoku.solver.ForcingChainsEliminator;

import java.util.*;

public class ForcingChainsReason extends Explanation {
    private Coord startCell;
    private List<String> startCellSymbols = new ArrayList<>();
    private List<List<Coord>> chains = new ArrayList<>();
    private int depth; // indication of length of chain size

    public ForcingChainsReason(String symbol,
                               Coord startCell,
                               Coord removedFromCell) {
        super(symbol, removedFromCell);
        this.startCell = startCell;
    }

    public String getSymbol() { return symbols.iterator().next(); }
    public Set<Coord> getRemovedFrom() { return coords; }
    public void setChainDepth(int depth) { this.depth = depth; }
    public int getChainDepth() { return depth; }

    public int getTotalChainLength()
    {
        int l = 0;
        for (List<Coord> aChain : chains) {
            l += aChain.size();
        }
        return l;
    }

    @Override
    public int getDifficulty() {
        return 999;
    } // TODO!

    public void addChain(String symbol, List<Coord> stepChain) {
        startCellSymbols.add(symbol);
        chains.add(stepChain);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder(super.toString());
        sb.append(" from ");
        if (coords.size() > 1) {
            sb.append(coords);
        } else {
            sb.append(coords.iterator().next());
        }
        sb.append(" with ").append(chains.size()).append(" chains starting at ").append(startCell);
        for (int i=0; i<startCellSymbols.size(); i++) {
            sb.append(" {").append(startCellSymbols.get(i)).append(": ").append(chains.get(i)).append("}");
        }
        String kind = "Forcing Chains";
        // XYWing http://www.sadmansoftware.com/sudoku/xywing.php
        if (startCellSymbols.size() == 2 && chains.size() == 2 && chains.get(0).size() == 1 && chains.get(1).size() == 1) {
            Coord c1 = chains.get(0).get(0);
            Coord c2 = chains.get(1).get(0);
            // c1 must be buddy of start
            // c2 must be buddy of start
            // c1 and c2 must have had only 2 candidates
            // start should share one candidate with c1 and one with c2
            // c1 and c2 should share one candidate which is the removed one
            kind = "XY-Wing";
        }
        sb.append(" (").append(kind).append(")");
        return sb.toString();
    }

    public Map<String, Set<Coord>> getHighlightCells() {
        Map<String, Set<Coord>> result = new HashMap<>();

        result.put("", Collections.singleton(startCell));
        for (int i=0; i<chains.size(); i++) {
            result.put(startCellSymbols.get(i), new HashSet<>(chains.get(i)));
        }

        return result;
    }

    public int compareTo(ForcingChainsReason other)
    {
        if (other == null) return -1;
        int result = getChainDepth() - other.getChainDepth();
        if (result == 0) result = getTotalChainLength() - other.getTotalChainLength();
        return result;
    }
}
