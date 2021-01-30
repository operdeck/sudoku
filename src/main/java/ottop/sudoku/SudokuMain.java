package ottop.sudoku;

// As computer security expert Ben Laurie has stated, ui.SudokuMain is "a denial of service attack on human intellect"

import ottop.sudoku.puzzle.ISudoku;
import ottop.sudoku.reader.MagicTourReader;
import ottop.sudoku.reader.SudokuReader;
import ottop.sudoku.reader.SudokuResourceReader;
import ottop.sudoku.solver.SudokuSolver;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

// Massive collections of Sudoku's here. Not currently used but perhaps
// can pick the ones with higher ratings.
// For the latest compiled list of potential hardest puzzles (Sep 30 2019) please visit
// champagne's https://drive.google.com/drive/folders/0B5lH6mGXxWzXTDFRMnVTbGNlZU0 and download ph_1910.zip
// https://drive.google.com/drive/folders/0B5lH6mGXxWzXTDFRMnVTbGNlZU0

// Sudoku nerd forum: http://forum.enjoysudoku.com/#

// OCR on sudoku's plus data
// https://github.com/wichtounet/sudoku_dataset

// Brute force solving every Sudoku
// http://norvig.com/sudoku.html
// plus some links to datasets including the MagicTour data

public class SudokuMain {

    public static void main(String[] args) throws IOException {

        //SudokuReader sr = new Kaggle1MSudokuReader();
        //SudokuReader sr = new HardestSudokuDatabaseReader();


        while (true) {
            SudokuReader sr = new MagicTourReader();

            Instant start = Instant.now();
            int nSolved = 0;
            int nUnsolved = 0;

            while (sr.hasNext()) {
                ISudoku p = sr.next();
                //System.out.println(p);

                //SudokuSolver solver = new SudokuSolver(p);
                //ISudoku solvedPuzzle = solver.setSmartest().solve();
                //if (solvedPuzzle != null && solvedPuzzle.isSolved()) {
                int result = SudokuSolver.assessDifficulty(p);
                if (result < 0) System.out.println("Not solved: " + p.getName());
                if (result < 0) nUnsolved++; else nSolved++;

//                System.out.println("Puzzle " + p.getName() +
//                        " level " + SudokuSolver.assessDifficulty(p)); // + solvedPuzzle);
            }

            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("Solved " + nSolved + "/" + (nSolved+nUnsolved) + " in " + timeElapsed/1000.0 + " secs");
            System.exit(0);
        }
    }
}
