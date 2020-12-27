package ottop.sudoku;

// As computer security expert Ben Laurie has stated, ui.Sudoku is "a denial of service attack on human intellect"

import ottop.sudoku.puzzle.IPuzzle;
import ottop.sudoku.puzzle.NRCPuzzle;

public class Sudoku {

	public static void main(String[] args) {
		IPuzzle p = new NRCPuzzle("NRC_5dec14",
				"....65...",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." );

//		p = new Puzzle("Puzzelbrein", new String[] {
//				"358961274",
//				"642738915",
//				"971425683",
//				"265  9 37",
//				"73    8  ",
//				"   3    2",
//				"    9 5  ",
//				"   6   4 ",
//				"         " });

		SudokuSolver solver = new SudokuSolver(p);
		solver.solve();
		int iterations = 12345;

		if (p.isSolved()) {
			System.out.println("Puzzle " + p + " solved in " + iterations + " iterations.");
		} else {
			System.out.println("Puzzle " + p + " not solved. " + iterations + " iterations done.");
		}
	}
}
