package ottop.sudoku.tests;

import org.testng.annotations.Test;
import ottop.sudoku.IPuzzle;

public class NRCPuzzleTest {

	@Test
	public void NRC_5dec14() {
		IPuzzle p = new ottop.sudoku.NRCPuzzle(
				"NRC 5 dec '14",
				"....65...",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." );
		p.solve();
		assert(p.solved());
	}

	@Test
	public void unsolvable() {
		IPuzzle p = new ottop.sudoku.NRCPuzzle(
				"Unsolvable",
				"....652..",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." );
		p.solve();
		assert(p.solved());
	}

	@Test
	public void NRC_28dec() {
		IPuzzle p = new ottop.sudoku.NRCPuzzle(
				"NRC 28 dec 2014",
				".....2...",
				"..85..1.9",
				".......6.",
				"..39.....",
				".........",
				".....3...",
				".24..5...",
				".8.7.....",
				"...1....7" );
		p.solve();
		assert(p.solved());
	}

	@Test
	public void NRC_17nov() {
		IPuzzle p = new ottop.sudoku.NRCPuzzle(
				"NRC 17 nov 2014",
				".86...3..",
				"..95.....",
				"......1.8",
				"1.7.4.5..",
				"2........",
				"........9",
				"..41.....",
				".....5...",
				"........." );
		p.solve();
		assert(p.solved());
	}

}
