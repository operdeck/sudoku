package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.puzzle.IPuzzle;

import static org.junit.Assert.assertEquals;

public class LetterPuzzleTest {
    @Test
    public void checkSymbolCodeMapping()
    {
        IPuzzle p = PuzzleDB.EOC_dec14;

        assertEquals(10, p.getSymbolCodeRange());
        assertEquals( "H", p.symbolCodeToSymbol(3));
        assertEquals(" ", p.symbolCodeToSymbol(0));
    }
}
