package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.*;

import java.util.Map;

import static org.junit.Assert.*;

public class StandardPuzzleTest {
	@Test
	public void checkBoard() {
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

		assertTrue(p.isOccupied(new Coord("r6c2")));
		assertTrue(p.isOccupied(new Coord("r1c6")));
		assertTrue(p.isOccupied(new Coord("r3c9")));
		assertEquals("1", p.getSymbolAtCoordinates(new Coord("r3c1")));
		assertFalse(p.isOccupied(new Coord("r9c9")));
		assertEquals(" ", p.getSymbolAtCoordinates(new Coord("r9c9")));
	}

	@Test
	public void checkState() {
		StandardPuzzle p = new StandardPuzzle( "Bad puzzle",
				"123456789",
				"123456789",
				"123456789",
				"123456789",
				"123456789",
				"123456789",
				"123456789",
				"123456789",
				"123456789" );
		assertFalse(p.isSolved());
		assertTrue(p.isInconsistent());

		StandardPuzzle p2 = new StandardPuzzle( "Solved puzzle",
				"827154396",
				"965327148",
				"341689752",
				"593468271",
				"472513689",
				"618972435",
				"786235914",
				"154796823",
				"239841567" );
		assertTrue(p2.isSolved());
		assertFalse(p2.isInconsistent());
	}

	@Test
	public void checkGroups()
	{
		IPuzzle p = PuzzleDB.Trouw_535;
		assertEquals(27, p.getGroups().length);
	}

	@Test
	public void checkDoMove()
	{
		IPuzzle p = PuzzleDB.Trouw_535;
		SudokuSolver s = new SudokuSolver(p, false);

		assertFalse(p.isOccupied(new Coord(1,7)));
		assertFalse(p.canUndo());

		Map.Entry<Coord, Integer> move = s.nextMove();
		assertNotNull(move);

		assertEquals("5", p.symbolCodeToSymbol(move.getValue()));
		assertEquals("r8c2", move.getKey().toString());

		IPuzzle nextPuzzle = p.doMove(move.getKey(), "5");
		assertTrue(nextPuzzle.canUndo());
		assertTrue(nextPuzzle.isOccupied(new Coord(1,7)));
	}
}
