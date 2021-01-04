package ottop.sudoku;

// As computer security expert Ben Laurie has stated, ui.SudokuMain is "a denial of service attack on human intellect"

import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.puzzle.NRCSudoku;
import ottop.sudoku.solve.SudokuSolver;

public class SudokuMain {

    public static void main(String[] args) {
        ISudoku p = new NRCSudoku("NRC_5dec14",
                "....65...",
                ".......6.",
                "1......78",
                ".........",
                "..27.....",
                ".3..9...1",
                "..6..45..",
                ".8...2...",
                ".........");

        SudokuSolver solver = new SudokuSolver(p);
        ISudoku solvedPuzzle = solver.solve();

        if (solvedPuzzle.isSolved()) {
            System.out.println("Puzzle " + p + " solved:\n" + solvedPuzzle);
        } else {
            System.out.println("Puzzle " + p + " not solved.");
        }
    }
}
