package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.*;

import static org.junit.Assert.*;

public class NRCPuzzleTest {
	@Test
	public void checkNRCGroups()
	{
		IPuzzle p = PuzzleDB.NRC_17nov;
		assertEquals(31, p.getGroups().length);
		assertEquals(13, p.getSquareGroups().length);
	}
}
