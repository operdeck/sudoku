package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.*;

import static org.junit.Assert.*;

public class StandardPuzzleTest {
	@Test
	public void checkBasicPuzzleAdmin() {
		StandardPuzzle p = new StandardPuzzle(
				"....65...",
				".......6.",
				"1......78",
				".........",
				"..27.....",
				".3..9...1",
				"..6..45..",
				".8...2...",
				"........." );
		assertFalse(p.isSolved());
		assertEquals(9, p.getHeight());
		assertEquals(9, p.getWidth());
		assertFalse(p.canUndo());
		assertTrue(p.isOccupied(0,5));
		assertTrue(p.isOccupied(2, 8));
		assertEquals('1', p.getValueAtCell(2,0));
		assertFalse(p.isOccupied(8, 8));
		assertEquals(' ', p.getValueAtCell(8,8));
	}

	@Test
	public void checkGroups()
	{
		IPuzzle p = PuzzleDB.Trouw_535;
		assertEquals(27, p.getGroups().length);
		assertEquals(9, p.getSquareGroups().length);
	}
}
