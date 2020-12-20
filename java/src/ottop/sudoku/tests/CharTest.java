package ottop.sudoku.tests;

import org.testng.annotations.Test;
import ottop.sudoku.SolutionContainer;
import ottop.sudoku.StandardPuzzle;
import ottop.sudoku.SudokuSolver;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharTest {

	@Test
	public void EOC_dec14() {
		StandardPuzzle p = new StandardPuzzle(new String[] {
				"SH..R...U",
				"...T.S..F",
				"..T.FHR..",
				".RI.S..C.",
				"T.HF.IS.R",
				".S..T.FU.",
				"...UI.P..",
				"P..S.T...",
				"H...P..TS" });
		SudokuSolver solver = new SudokuSolver(p);
		solver.solve();
		assertTrue(p.isSolved());
	}

}
