package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.SolutionContainer;
import ottop.sudoku.StandardPuzzle;
import ottop.sudoku.SudokuSolver;

import static org.junit.Assert.assertTrue;

public class StandardPuzzleTest {

	@Test
	public void Parool_18nov() {
		StandardPuzzle p = new StandardPuzzle(new String[] {
				"........8",
				"..9..2.7.",
				".64.38...",
				"1.7.6....",
				"..3...8..",
				"....2.7.3",
				"...48.36.",
				".5.9..2..",
				"9........" });
		SudokuSolver solver = new SudokuSolver(p);
		solver.solve();
		assertTrue(p.isSolved());
	}
		
	@Test
	public void Trouw_535() {
		StandardPuzzle p = new StandardPuzzle(new String[] {
				"9 1357   ",
				"3        ",
				" 8   6  1",
				" 26 3 49 ",
				"  96 81  ",
				" 18 2 63 ",
				"1  5   8 ",
				"        3",
				"   1637 5" });
		SudokuSolver solver = new SudokuSolver(p);
		solver.solve();
		assertTrue(p.isSolved());
	}
		
	@Test
	public void www_extremesudoku_info_evil() {
		StandardPuzzle p = new StandardPuzzle(new String[] {
				" 4  8 6  ",
				"  84    3",
				"2   1  8 ",
				"       5 ",
				"1 3 2 9 6",
				" 7       ",
				" 6  9   2",
				"9    15  ",
				"  5 3  1 "});
		SudokuSolver solver = new SudokuSolver(p);
		solver.solve();
		assertTrue(p.isSolved());
	}
		
	@Test
	public void www_extremesudoku_info_evil_271113() {
		StandardPuzzle p = new StandardPuzzle(new String[] {
				"..1.9.5..",
				".5.4.3.1.",
				"9...8...6",
				".8.....3.",
				"5.2...9.4",
				".1.....7.",
				"3...2...1",
				".2.7.9.5.",
				"..4.1.6.."});
		SudokuSolver solver = new SudokuSolver(p);
		solver.solve();
		assertTrue(p.isSolved());
	}
		
	@Test
	public void extremesudoku_28_nov_2013() {
		StandardPuzzle p = new StandardPuzzle(new String[] {
				" 1   9   ",
				"  4 7   1",
				"  2   98 ",
				"6  9 3   ",
				" 5  1  7 ",
				"   7 6  5",
				" 71   3  ",
				"5   2 8  ",
				"   3   6 "
		});
		SudokuSolver solver = new SudokuSolver(p);
		solver.solve();
		assertTrue(p.isSolved());
	}
		
	@Test
	public void extremesudoku_10_nov_2013() {
		StandardPuzzle p = new StandardPuzzle(new String[] {
				"  89    2",
				" 2  7  8 ",
				"3    41  ",
				"6    92  ",
				" 5  4  9 ",
				"  25    7",
				"  56    3",
				" 1  3  6 ",
				"8    74  "
		});
		SudokuSolver solver = new SudokuSolver(p);
		assertTrue(p.isSolved());
	}
}
