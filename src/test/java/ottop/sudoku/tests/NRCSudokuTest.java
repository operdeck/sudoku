package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.puzzle.ISudoku;

import static org.junit.Assert.assertEquals;

public class NRCSudokuTest {
    @Test
    public void checkNRCGroups() {
        ISudoku p = PuzzleDB.NRC_17nov;
        assertEquals(31, p.getGroups().size());
    }
}
