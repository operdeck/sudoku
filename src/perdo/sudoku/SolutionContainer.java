package perdo.sudoku;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Container of an array of digits placed in cells, as a solution
 * for the next iteration of the puzzle solver.
 */
public class SolutionContainer {
	private Map<Coord, Integer> sols;
	private Puzzle myPuzzle;
	
	public SolutionContainer(Puzzle p) {
		sols = new HashMap<Coord, Integer>();
		myPuzzle = p;
	}
	
	public void addSolution(Coord c, int n, String source) {
		if (sols.containsKey(c)) {
			if (sols.get(c) != n) {
				throw new RuntimeException("ERROR: trying to place " + myPuzzle.toChar(n) + " at " + c + " which is occupied by " + myPuzzle.toChar(sols.get(c)));
			}
		} else {
			System.out.println("Put " + myPuzzle.toChar(n) + " at " + c + " (" + source + ")");
			sols.put(c, n);
		}
	}
	
// TODO represent final solution more compact:
//	
//	 S H F | C R P | T I U  
//	 I P R | T U S | C H F  
//	 U C T | I F H | R S P  
//   ------+-------+------
//	 F R I | P S U | H C T  
//	 T U H | F C I | S P R  
//	 C S P | H T R | F U I  
//  -------+-------+------
//	 R T S | U I C | P F H  
//	 P F U | S H T | I R C  
//	 H I C | R P F | U T S  

	
	public String toString(Puzzle originalPuzzle) {
		StringBuffer buf = new StringBuffer();

		boolean isAllOccupied = true;
		for (int y=0; y<9; y++) {
			for (int x=0; x<9; x++) {
				boolean isOccupied = originalPuzzle.isOccupied(y, x);
				if (isAllOccupied && !isOccupied) isAllOccupied = false;
			}
		}
		
		if (isAllOccupied) {
			for (int y=0; y<9; y++) {
				if (y > 0 && y % 3 == 0) {
					buf.append("------+-------+------");
					buf.append(System.getProperty("line.separator"));
				}
				for (int x=0; x<9; x++) {
					if (x % 3 == 0 && x > 0) {
						buf.append(" |");
					}
					if (x > 0) buf.append(' ');
					buf.append(originalPuzzle.getOriginalCharacter(y, x));
				}
				buf.append(System.getProperty("line.separator"));
			}
		} else {
			for (int y=0; y<9; y++) {
				for (int x=0; x<9; x++) {
					boolean isOccupied = originalPuzzle.isOccupied(y, x);
					if (isOccupied) {
						buf.append(' ').append(originalPuzzle.getOriginalCharacter(y, x)).append(' ');
					} else {
						Integer sol = sols.get(new Coord(x,y));
						if (sol != null) {
							buf.append('[').append(originalPuzzle.toChar(sol)).append(']');
						} else {
							buf.append(" . ");
						}
					}
					buf.append(' ');
				}
				buf.append(System.getProperty("line.separator"));
			}
		}
		
		return buf.toString();				
	}

	// Generate a string representation of the current sodoku merged with the solutions
	// NOTE: will be very similar to the 'toString'
	public String[] merge(Puzzle puzzle) {
		List<String> rowsList = new ArrayList<String>();
		
		for (int y=0; y<9; y++) {
			StringBuffer buf = new StringBuffer();
			for (int x=0; x<9; x++) {
				boolean isOccupied = puzzle.isOccupied(y, x);
				if (isOccupied) {
					buf.append(puzzle.getOriginalCharacter(y,x));
				} else {
					Integer sol = sols.get(new Coord(x,y));
					if (sol != null) {
						buf.append(puzzle.toChar(sol));
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
