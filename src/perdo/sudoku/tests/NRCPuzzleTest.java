package perdo.sudoku.tests;

import static org.junit.Assert.*;

import org.junit.Test;

import perdo.sudoku.NRCPuzzle;
import perdo.sudoku.Puzzle;

public class NRCPuzzleTest {

	@Test
	public void NRC_5dec14() {
		Puzzle p = new NRCPuzzle(new String[] {
				"....65...",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." });
		p.solve();
		assertTrue(p.solved());
	}

	@Test
	public void unsolvable() {
		Puzzle p = new NRCPuzzle(new String[] {
				"....652..",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." });
		p.solve();
		assertFalse(p.solved());
	}

	@Test
	public void NRC_28dec() {
		Puzzle p = new NRCPuzzle(new String[] {
				".....2...",
				"..85..1.9",
				".......6.",
				"..39.....",
				".........",
				".....3...",
				".24..5...",
				".8.7.....",
				"...1....7" });
		p.solve();
		assertTrue(p.solved());
	}

	@Test
	public void NRC_17nov() {
		Puzzle p = new NRCPuzzle(new String[] {
				".86...3..",
				"..95.....",
				"......1.8",
				"1.7.4.5..",
				"2........",
				"........9",
				"..41.....",
				".....5...",
				"........." });
		p.solve();
		assertTrue(p.solved());
	}

}
