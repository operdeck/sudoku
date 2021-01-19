package ottop.sudoku.tests;

import org.junit.Test;
import ottop.sudoku.PuzzleDB;
import ottop.sudoku.board.Coord;
import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.solver.SudokuSolver;

import static junit.framework.TestCase.*;

public class UndoRedoTest {
    @Test
    public void testUndoRedo() {
        ISudoku p = PuzzleDB.extremesudoku_info_excessive_4jan2021;
        SudokuSolver s1 = new SudokuSolver(p);

        assertFalse(p.canUndo());
        assertFalse(p.canRedo());

        // Do some moves
        p.doMove(new Coord("r1c1"), "1");
        p.doMove(new Coord("r3c5"), "2");
        p.doMove(new Coord("r3c6"), "1");
        p.doMove(new Coord("r4c6"), "8");
        p.doMove(new Coord("r8c6"), "5");

        // Assert we have these in the undo stack
        assertTrue(p.canUndo());
        assertFalse(p.canRedo());

        // Undo one
        Coord lastMove = p.undoMove().getKey();
        assertEquals("r8c6", String.valueOf(lastMove));
        assertTrue(p.canUndo());
        assertTrue(p.canRedo());
        assertEquals("Extreme Sudoku Excessive 4/1/21:\n" +
                "1.6..7..2\n" +
                ".7..5..1.\n" +
                "3..6219..\n" +
                "4..5.81..\n" +
                ".1..4..8.\n" +
                "..8..2..9\n" +
                "..1..6..5\n" +
                ".4..8..3.\n" +
                "5..2..7..\n", String.valueOf(p));

        // Undo all
        p.undoMove();
        p.undoMove();
        p.undoMove();
        p.undoMove();
        assertFalse(p.canUndo());
        assertTrue(p.canRedo());
        assertEquals("Extreme Sudoku Excessive 4/1/21:\n" +
                "..6..7..2\n" +
                ".7..5..1.\n" +
                "3..6..9..\n" +
                "4..5..1..\n" +
                ".1..4..8.\n" +
                "..8..2..9\n" +
                "..1..6..5\n" +
                ".4..8..3.\n" +
                "5..2..7..\n", String.valueOf(p));

        // Now wind back again
        assertEquals("r1c1=1", String.valueOf(p.redoMove()));
        assertEquals("1", p.getSymbolAtCoordinates(new Coord("r1c1")));
        p.redoMove();
        p.redoMove();
        p.redoMove();
        p.redoMove();
        assertEquals("Extreme Sudoku Excessive 4/1/21:\n" +
                "1.6..7..2\n" +
                ".7..5..1.\n" +
                "3..6219..\n" +
                "4..5.81..\n" +
                ".1..4..8.\n" +
                "..8..2..9\n" +
                "..1..6..5\n" +
                ".4..85.3.\n" +
                "5..2..7..\n", String.valueOf(p));
    }

    @Test
    public void testNoStateLeftBehind() {
        ISudoku p = PuzzleDB.extremesudoku_info_excessive_4jan2021;
        SudokuSolver s1 = new SudokuSolver(p);

        // Do some moves

        p.doMove(new Coord("r1c1"), "1");
        p.doMove(new Coord("r3c5"), "2");
        p.doMove(new Coord("r3c6"), "1");
        p.doMove(new Coord("r4c6"), "8");
        p.doMove(new Coord("r8c6"), "5");

        assertEquals("2", p.getSymbolAtCoordinates(new Coord("r3c5")));

        // Create another solver for the same puzzle
        // This should reset the puzzle state

        SudokuSolver s2 = new SudokuSolver(p);

        // Assert we see the correct moves / i.e. number of possibilities / groups initialized

        assertEquals(" ", p.getSymbolAtCoordinates(new Coord("r3c5")));
    }

    @Test
    public void testDB() {
//        assertEquals(19, PuzzleDB.getPuzzleNames().length);
        assertEquals("Parool_18nov:\n" +
                "........8\n" +
                "..9..2.7.\n" +
                ".64.38...\n" +
                "1.7.6....\n" +
                "..3...8..\n" +
                "....2.7.3\n" +
                "...48.36.\n" +
                ".5.9..2..\n" +
                "9........\n", String.valueOf(PuzzleDB.getPuzzleByName("Parool_18nov")));
    }
}
