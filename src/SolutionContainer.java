import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Container of an array of digits placed in cells, as a solution
 * for the next iteration of the puzzle solver.
 */
public class SolutionContainer {
	private Map<Coord, Integer> sols;
	
	public SolutionContainer() {
		sols = new HashMap<Coord, Integer>();
	}
	
	public void addSolution(Coord c, int n, String source) {
		if (sols.containsKey(c)) {
			if (sols.get(c) != n) {
				throw new RuntimeException("ERROR: trying to place " + n + " at " + c + " which is occupied by " + sols.get(c));
			}
		} else {
			System.out.println("Put " + n + " at " + c + " (" + source + ")");
			sols.put(c, n);
		}
	}
	
	public String toString(String[] originalPuzzle) {
		StringBuffer buf = new StringBuffer();
		
		for (int y=0; y<9; y++) {
			for (int x=0; x<9; x++) {
				char original = originalPuzzle[y].charAt(x);
				if (original >= '1' && original <= '9') {
					buf.append(' ').append(original).append(' ');
				} else {
					Integer sol = sols.get(new Coord(x,y));
					if (sol != null) {
						buf.append('[').append(sol).append(']');
					} else {
						buf.append(" . ");
					}
				}
				buf.append(' ');
			}
			buf.append(System.getProperty("line.separator"));
		}
		
		return buf.toString();				
	}

	// Generate a string representation of the current sodoku merged with the solutions
	// NOTE: will be very similar to the 'toString'
	public String[] merge(String[] originalPuzzle) {
		List<String> rowsList = new ArrayList<String>();
		
		for (int y=0; y<9; y++) {
			StringBuffer buf = new StringBuffer();
			for (int x=0; x<9; x++) {
				char original = originalPuzzle[y].charAt(x);
				if (original >= '1' && original <= '9') {
					buf.append(original);
				} else {
					Integer sol = sols.get(new Coord(x,y));
					if (sol != null) {
						buf.append(sol);
					} else {
						buf.append(".");
					}
				}
			}
			rowsList.add(buf.toString());
		}
		String[] result = new String[rowsList.size()];
		for (int i=0; i<rowsList.size(); i++) result[i] = rowsList.get(i);
		return result;
	}
}
