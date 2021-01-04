package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.puzzle.ISudoku;

import static org.junit.Assert.assertEquals;

public class Sudoku10x10Test {
    @Test
    public void checkSymbolCodeMapping() {
        ISudoku p = PuzzleDB.puzzelbrein12_2020;

        System.out.println(p);
        assertEquals(11, p.getSymbolCodeRange());
        assertEquals("3", p.symbolCodeToSymbol(3));
        assertEquals(" ", p.symbolCodeToSymbol(0));
    }
}
