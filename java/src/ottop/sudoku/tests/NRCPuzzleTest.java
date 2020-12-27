package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.*;
import ottop.sudoku.puzzle.IPuzzle;

import static org.junit.Assert.*;

public class NRCPuzzleTest {
	@Test
	public void checkNRCGroups()
	{
		IPuzzle p = PuzzleDB.NRC_17nov;
		assertEquals(31, p.getGroups().length);
	}
}
