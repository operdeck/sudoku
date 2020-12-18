package ottop.sudoku.tests;

import org.testng.annotations.Test;
import ottop.sudoku.Puzzle;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class CharTest {

	@Test
	public void EOC_dec14() {
		Puzzle p = new Puzzle(new String[] {
				"SH..R...U",
				"...T.S..F",
				"..T.FHR..",
				".RI.S..C.",
				"T.HF.IS.R",
				".S..T.FU.",
				"...UI.P..",
				"P..S.T...",
				"H...P..TS" });
		p.solve();
		assertTrue(p.isSolved());
	}

}
