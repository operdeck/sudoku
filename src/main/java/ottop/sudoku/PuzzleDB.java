package ottop.sudoku;

import ottop.sudoku.puzzle.*;
import ottop.sudoku.reader.MagicTourReader;
import ottop.sudoku.reader.SudokuReader;

import java.io.IOException;
import java.lang.reflect.Field;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PuzzleDB {
    private static ISudoku emptyStandardPuzzle = new StandardSudoku("Empty standard Sudoku",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........");

    private static ISudoku emptyNRCPuzzle = new NRCSudoku("Empty NRC Sudoku",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........");

    private static ISudoku emptySDoku = new SDoku("Empty S-Doku",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........");

    public static ISudoku emptyLetterPuzzle = new LetterSudoku("Empty Letter SudokuMain", "CFHIPRSTU",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........");

    private static ISudoku Parool_18nov = new StandardSudoku("Parool_18nov",
            "........8",
            "..9..2.7.",
            ".64.38...",
            "1.7.6....",
            "..3...8..",
            "....2.7.3",
            "...48.36.",
            ".5.9..2..",
            "9........");

    public static ISudoku Trouw_535 = new StandardSudoku("Trouw_535",
            "9 1357   ",
            "3        ",
            " 8   6  1",
            " 26 3 49 ",
            "  96 81  ",
            " 18 2 63 ",
            "1  5   8 ",
            "        3",
            "   1637 5");

    public static ISudoku puzzelbrein12_2020 = new Sudoku10x10("Puzzelbrein 12/2020",
            "10, , , ,6, , ,2,7, ",
            " , , , , , , ,5, , ,",
            " , , ,9, , , , ,3, ",
            " , ,5,8, ,1, , , , ,",
            " , , ,2, , , ,7, , 4",
            " , , , ,10,8, ,1, ,3",
            " , ,6, , , ,3, , ,10",
            "3,8, , ,5,2,1, , , ,",
            " , , , , , ,4, , ,8",
            " ,2, ,4,, 6, ,9,5, ");

    public static ISudoku www_extremesudoku_info_evil = new StandardSudoku("www_extremesudoku_info_evil",
            " 4  8 6  ",
            "  84    3",
            "2   1  8 ",
            "       5 ",
            "1 3 2 9 6",
            " 7       ",
            " 6  9   2",
            "9    15  ",
            "  5 3  1 ");

    public static ISudoku www_extremesudoku_info_evil_271113 = new StandardSudoku("www.extremesudoku.info 27/11/13",
            "..1.9.5..",
            ".5.4.3.1.",
            "9...8...6",
            ".8.....3.",
            "5.2...9.4",
            ".1.....7.",
            "3...2...1",
            ".2.7.9.5.",
            "..4.1.6..");

    public static ISudoku extremesudoku_28_nov_2013 = new StandardSudoku("extremesudoku_28_nov_2013",
            " 1   9   ",
            "  4 7   1",
            "  2   98 ",
            "6  9 3   ",
            " 5  1  7 ",
            "   7 6  5",
            " 71   3  ",
            "5   2 8  ",
            "   3   6 ");

    public static ISudoku extremesudoku_10_nov_2013 = new StandardSudoku("extremesudoku_10_nov_2013",
            "  89    2",
            " 2  7  8 ",
            "3    41  ",
            "6    92  ",
            " 5  4  9 ",
            "  25    7",
            "  56    3",
            " 1  3  6 ",
            "8    74  "
    );

    public static ISudoku NRC_5dec14 = new NRCSudoku("NRC 5 dec '14",
            "....65...",
            ".......6.",
            "1......78",
            ".........",
            "..27.....",
            ".3..9...1",
            "..6..45..",
            ".8...2...",
            ".........");


    public static ISudoku unsolvable = new NRCSudoku("Unsolvable",
            "....652..",
            ".......6.",
            "1......78",
            ".........",
            "..27.....",
            ".3..9...1",
            "..6..45..",
            ".8...2...",
            ".........");

    public static ISudoku NRC_28dec = new NRCSudoku("NRC 28 dec 2014",
            ".....2...",
            "..85..1.9",
            ".......6.",
            "..39.....",
            ".........",
            ".....3...",
            ".24..5...",
            ".8.7.....",
            "...1....7");

    public static ISudoku NRC_17nov = new NRCSudoku("NRC 17 nov 2014",
            ".86...3..",
            "..95.....",
            "......1.8",
            "1.7.4.5..",
            "2........",
            "........9",
            "..41.....",
            ".....5...",
            ".........");

    private static ISudoku brainHouse = new SDoku("Fresh Brainhouse",
            "4........",
            ".........",
            "6.9......",
            "...2.....",
            "...4.....",
            "...7.....",
            "....9....",
            "2.73.8...",
            ".5...6...");

    public static ISudoku sudoku_very_hard_1 = new StandardSudoku("SudokuMain Essentials #1",
            ".3.48.6.9",
            "....27...",
            "8..3.....",
            ".19......",
            "78...2.93",
            ".....487.",
            ".....5..6",
            "...13....",
            "9.2.48.1.");

    public static ISudoku EOC_dec14 = new LetterSudoku("Char puzzle", "CFHIPRSTU",
            "SH..R...U",
            "...T.S..F",
            "..T.FHR..",
            ".RI.S..C.",
            "T.HF.IS.R",
            ".S..T.FU.",
            "...UI.P..",
            "P..S.T...",
            "H...P..TS");

    public static ISudoku extremesudoku_info_evil_4jan2021 = new StandardSudoku("Extreme Sudoku Evil 4/1/21",
            "6.13.57.9",
            ".........",
            "3..7.4..6",
            "2.8...3.5",
            "....9....",
            "9.7...6.8",
            "7..9.2..4",
            ".........",
            "5.61.89.3");

    public static ISudoku extremesudoku_info_excessive_4jan2021 = new StandardSudoku("Extreme Sudoku Excessive 4/1/21",
            "..6..7..2",
            ".7..5..1.",
            "3..6..9..",
            "4..5..1..",
            ".1..4..8.",
            "..8..2..9",
            "..1..6..5",
            ".4..8..3.",
            "5..2..7..");

    private static ISudoku intersection_rad_sudokuessentials = new StandardSudoku("Intersection Rad Example",
            "  6  35  ",
            "1  7  3 6",
            "8 7 5  24",
            "4  6 79 2",
            "   1 9   ",
            "6 85 2  7",
            "36  7 2 1",
            "7 4  1  5",
            "  12  7  ");

    private static ISudoku forcing_chains_sadman = new StandardSudoku("Forcing chains example",
            "8 6495  1",
            " 3 87659 ",
            "59    86 ",
            "453168279",
            " 69 4  58",
            "7 85 94 6",
            "34 6  985",
            "68 953 4 ",
            "9 5 846 3");

    private static List<ISudoku> getDeclaredPuzzles(PuzzleDB db) {
        Field[] allFields = PuzzleDB.class.getDeclaredFields();
        List<ISudoku> puzzles = new ArrayList<>();
        for (Field f : allFields) {
            try {
                puzzles.add(((ISudoku) f.get(db)).clone());
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        }
        return puzzles;
    }

    private static List<ISudoku> getMagicTourPuzzles() {
        List<ISudoku> puzzles = new ArrayList<>();
        try {
            SudokuReader sr = new MagicTourReader();
            while (sr.hasNext()) {
                puzzles.add(sr.next());
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return puzzles;
    }

    public static String[] getPuzzleNames() {
        PuzzleDB db = new PuzzleDB();
        List<String> puzzleNames = new ArrayList<>();
        getDeclaredPuzzles(db).forEach((p) -> puzzleNames.add(p.getName()));
        getMagicTourPuzzles().forEach((p) -> puzzleNames.add(p.getName()));
        return puzzleNames.toArray(new String[0]);
    }

    public static ISudoku getPuzzleByName(String name) {
        AtomicReference<ISudoku> result = new AtomicReference<>();
        PuzzleDB db = new PuzzleDB();
        getDeclaredPuzzles(db).forEach((p) -> {
            if (p.getName().equals(name)) result.set(p);
        });
        if (result.get() == null) {
            getMagicTourPuzzles().forEach((p) -> {
                if (p.getName().equals(name)) result.set(p);
            });
        }
        return result.get();
    }
}
