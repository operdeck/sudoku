package ottop.sudoku;

import ottop.sudoku.puzzle.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class PuzzleDB {
    public static IPuzzle emptyStandardPuzzle = new Standard9x9Puzzle("Empty standard Sudoku",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........");

    public static IPuzzle emptyNRCPuzzle = new NRCPuzzle("Empty NRC Sudoku",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........");

    public static IPuzzle emptyLetterPuzzle = new SudokuLetterPuzzle("Empty Letter Sudoku", "CFHIPRSTU",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........",
            ".........");

    public static IPuzzle Parool_18nov = new Standard9x9Puzzle("Parool_18nov",
            "........8",
            "..9..2.7.",
            ".64.38...",
            "1.7.6....",
            "..3...8..",
            "....2.7.3",
            "...48.36.",
            ".5.9..2..",
            "9........");

    public static IPuzzle Trouw_535 = new Standard9x9Puzzle("Trouw_535",
            "9 1357   ",
            "3        ",
            " 8   6  1",
            " 26 3 49 ",
            "  96 81  ",
            " 18 2 63 ",
            "1  5   8 ",
            "        3",
            "   1637 5");

    public static IPuzzle puzzelbrein12_2020 = new Sudoku10x10Puzzle("Puzzelbrein 12/2020",
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

    public static IPuzzle www_extremesudoku_info_evil = new Standard9x9Puzzle("www_extremesudoku_info_evil",
            " 4  8 6  ",
            "  84    3",
            "2   1  8 ",
            "       5 ",
            "1 3 2 9 6",
            " 7       ",
            " 6  9   2",
            "9    15  ",
            "  5 3  1 ");

    public static IPuzzle www_extremesudoku_info_evil_271113 = new Standard9x9Puzzle("www.extremesudoku.info 27/11/13",
            "..1.9.5..",
            ".5.4.3.1.",
            "9...8...6",
            ".8.....3.",
            "5.2...9.4",
            ".1.....7.",
            "3...2...1",
            ".2.7.9.5.",
            "..4.1.6..");

    public static IPuzzle extremesudoku_28_nov_2013 = new Standard9x9Puzzle("extremesudoku_28_nov_2013",
            " 1   9   ",
            "  4 7   1",
            "  2   98 ",
            "6  9 3   ",
            " 5  1  7 ",
            "   7 6  5",
            " 71   3  ",
            "5   2 8  ",
            "   3   6 ");

    public static IPuzzle extremesudoku_10_nov_2013 = new Standard9x9Puzzle("extremesudoku_10_nov_2013",
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

    public static IPuzzle NRC_5dec14 = new NRCPuzzle("NRC 5 dec '14",
            "....65...",
            ".......6.",
            "1......78",
            ".........",
            "..27.....",
            ".3..9...1",
            "..6..45..",
            ".8...2...",
            ".........");


    public static IPuzzle unsolvable = new NRCPuzzle("Unsolvable",
            "....652..",
            ".......6.",
            "1......78",
            ".........",
            "..27.....",
            ".3..9...1",
            "..6..45..",
            ".8...2...",
            ".........");

    public static IPuzzle NRC_28dec = new NRCPuzzle("NRC 28 dec 2014",
            ".....2...",
            "..85..1.9",
            ".......6.",
            "..39.....",
            ".........",
            ".....3...",
            ".24..5...",
            ".8.7.....",
            "...1....7");

    public static IPuzzle NRC_17nov = new NRCPuzzle("NRC 17 nov 2014",
            ".86...3..",
            "..95.....",
            "......1.8",
            "1.7.4.5..",
            "2........",
            "........9",
            "..41.....",
            ".....5...",
            ".........");

    public static IPuzzle sudoku_very_hard_1 = new Standard9x9Puzzle("Sudoku Essentials #1",
            ".3.48.6.9",
            "....27...",
            "8..3.....",
            ".19......",
            "78...2.93",
            ".....487.",
            ".....5..6",
            "...13....",
            "9.2.48.1.");

    public static IPuzzle EOC_dec14 = new SudokuLetterPuzzle("Char puzzle", "CFHIPRSTU",
            "SH..R...U",
            "...T.S..F",
            "..T.FHR..",
            ".RI.S..C.",
            "T.HF.IS.R",
            ".S..T.FU.",
            "...UI.P..",
            "P..S.T...",
            "H...P..TS");

    // TODO include some others from the puzzle book

    public static String[] getPuzzles() throws IllegalAccessException {
        Field[] allFields = PuzzleDB.class.getDeclaredFields();
        List<String> puzzleNames = new ArrayList<>();
        PuzzleDB db = new PuzzleDB();
        for (Field f : allFields) {
            puzzleNames.add(((IPuzzle) f.get(db)).getName());
        }
        return puzzleNames.toArray(new String[0]);
    }

    public static IPuzzle getPuzzleByName(String name) throws IllegalAccessException {
        Field[] allFields = PuzzleDB.class.getDeclaredFields();
        PuzzleDB db = new PuzzleDB();
        for (Field f : allFields) {
            IPuzzle p = (IPuzzle) f.get(db);
            if (name.equals(p.getName())) {
                return p;
            }
        }
        return null;
    }
}
