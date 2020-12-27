package ottop.sudoku;

import ottop.sudoku.puzzle.IPuzzle;

import java.util.*;

/** Container of an array of digits placed in cells, as a solution
 * for the next iteration of the puzzle solver.
 */
public class SolutionContainer {
	private final Map<Coord, String> sols = new HashMap<>();;
	private final Map<Coord, List<String>> reasons = new HashMap<>();
	private final IPuzzle myPuzzle;
	private final boolean trace;

	public SolutionContainer(IPuzzle p, boolean trace) {
		myPuzzle = p;
		this.trace = trace;
	}

	public void addSolution(Coord c, int symbolCode, String reason) {
		String symbol = myPuzzle.symbolCodeToSymbol(symbolCode);
		if (sols.containsKey(c)) {
			if (sols.get(c) != symbol) {
				throw new RuntimeException("ERROR: trying to place " +
						symbol + " at " + c +
						" which is occupied by " + sols.get(c));
			} else {
				// Same solution but another reason for it
				if (reason != null) {
					if (reasons.containsKey(c)) {
						reasons.get(c).add(reason);
					} else {
						reasons.put(c, new ArrayList<>(Collections.singleton(reason)));
					}
				}
			}
		} else {
			if (trace) {
				System.out.println("Put " + symbol + " at " + c + " (" + reason + ")");
			}
			sols.put(c, symbol);
			if (reason != null) {
				reasons.put(c, new ArrayList<>(Collections.singleton(reason)));
			}
		}
	}

	public int size() {
		return sols.keySet().size();
	}

	public Map.Entry<Coord, String> getFirstMove() {
		Map.Entry<Coord, String> result = null;
		Iterator<Map.Entry<Coord, String>> it = sols.entrySet().iterator();
		if (it.hasNext()) {
			result = it.next();
		}
		return result;
	}

	public String toString() {
		StringBuilder result = new StringBuilder();
		boolean isFirst = true;
		result.append("(");
		for (Coord c: sols.keySet()) {
			if (!isFirst) result.append(", ");
			result.append(c).append(":").append(sols.get(c));
			if (reasons.containsKey(c)) {
				result.append(" ").append(reasons.get(c));
			}
			isFirst = false;
		}
		result.append(")");
		return result.toString();
	}
}
