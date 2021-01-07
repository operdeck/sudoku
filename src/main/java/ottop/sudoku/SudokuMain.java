package ottop.sudoku;

// As computer security expert Ben Laurie has stated, ui.SudokuMain is "a denial of service attack on human intellect"

import ottop.sudoku.puzzle.ISudoku;
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

// 1M (mostly easy) Sudoku's https://www.kaggle.com/bryanpark/sudoku
class Kaggle1MSudokuReader extends SudokuResourceReader {
    public Kaggle1MSudokuReader() throws IOException {
        super("/sudoku-kaggle.csv");
    }
    protected String getSudokuData(String[] flds) {
        return flds[0];
    }
    protected String getSudokuName(String[] flds) {
        return "Kaggle 1M Sudoku " + puzzleNo;
    }
    protected boolean isValidSudokuLine(String[] flds) {
        return flds.length == 2 && !"quizzes".equals(flds[0]);
    }
}

// Very hard puzzles - my solver currently does not crack any of them
// http://www.mediafire.com/?9ypndha1zadpwaw
// http://forum.enjoysudoku.com/the-hardest-sudokus-new-thread-t6539.html
// The latest Hardest sudokus Database can be downloaded by following
// this link: HardestDatabase110626.txt
class HardestSudokuDatabaseReader extends SudokuResourceReader {
    public HardestSudokuDatabaseReader() throws IOException {
        super("/HardestDatabase110626.txt");
    }
    protected String getSudokuData(String[] flds) {
        return flds[0];
    }
    protected String getSudokuName(String[] flds) {
        return flds[1] + ";" + flds[2] + " " + puzzleNo;
    }
    protected boolean isValidSudokuLine(String[] flds) {
        return flds.length == 10;
    }
}

// From http://magictour.free.fr/top95. The main focus of the site is some chess problem
// but this collection of Sudoku's seems to contain a nice mix of difficult but not
// impossible puzzles.
class MagicTourReader extends SudokuResourceReader {
    public MagicTourReader() throws IOException {
        super("/top95.txt");
    }
    protected String[] split(String line) {
        return new String[] {line};
    }
    protected String getSudokuData(String[] flds) {
        return flds[0];
    }
    protected String getSudokuName(String[] flds) {
        return "Magic tour " + puzzleNo;
    }
    protected boolean isValidSudokuLine(String[] flds) {
        return flds.length == 1 && flds[0].length() == 81;
    }
}

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
                if (result < 0) nSolved++; else nUnsolved++;

//                System.out.println("Puzzle " + p.getName() +
//                        " level " + SudokuSolver.assessDifficulty(p)); // + solvedPuzzle);
            }

            Instant finish = Instant.now();
            long timeElapsed = Duration.between(start, finish).toMillis();
            System.out.println("Solved " + nSolved + "/" + (nSolved+nUnsolved) + " in " + timeElapsed/1000.0 + " secs");
        }
    }
}
