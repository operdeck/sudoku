package ottop.sudoku;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class PuzzleDB {
    public static IPuzzle Parool_18nov = new StandardPuzzle("Parool_18nov",
            "........8",
            "..9..2.7.",
            ".64.38...",
            "1.7.6....",
            "..3...8..",
            "....2.7.3",
            "...48.36.",
            ".5.9..2..",
            "9........");

    public static IPuzzle Trouw_535 = new StandardPuzzle("Trouw_535",
            "9 1357   ",
            "3        ",
            " 8   6  1",
            " 26 3 49 ",
            "  96 81  ",
            " 18 2 63 ",
            "1  5   8 ",
            "        3",
            "   1637 5");

    public static IPuzzle www_extremesudoku_info_evil = new StandardPuzzle("www_extremesudoku_info_evil",
            " 4  8 6  ",
            "  84    3",
            "2   1  8 ",
            "       5 ",
            "1 3 2 9 6",
            " 7       ",
            " 6  9   2",
            "9    15  ",
            "  5 3  1 ");

    public static IPuzzle www_extremesudoku_info_evil_271113 = new StandardPuzzle("www_extremesudoku_info_evil_271113",
            "..1.9.5..",
            ".5.4.3.1.",
            "9...8...6",
            ".8.....3.",
            "5.2...9.4",
            ".1.....7.",
            "3...2...1",
            ".2.7.9.5.",
            "..4.1.6..");

    public static IPuzzle extremesudoku_28_nov_2013 = new StandardPuzzle("extremesudoku_28_nov_2013",
            " 1   9   ",
            "  4 7   1",
            "  2   98 ",
            "6  9 3   ",
            " 5  1  7 ",
            "   7 6  5",
            " 71   3  ",
            "5   2 8  ",
            "   3   6 ");

    public static IPuzzle extremesudoku_10_nov_2013 = new StandardPuzzle("extremesudoku_10_nov_2013",
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

    public static IPuzzle NRC_5dec14 = new ottop.sudoku.NRCPuzzle("NRC 5 dec '14",
            "....65...",
            ".......6.",
            "1......78",
            ".........",
            "..27.....",
            ".3..9...1",
            "..6..45..",
            ".8...2...",
            ".........");


    public static IPuzzle unsolvable = new ottop.sudoku.NRCPuzzle("Unsolvable",
            "....652..",
            ".......6.",
            "1......78",
            ".........",
            "..27.....",
            ".3..9...1",
            "..6..45..",
            ".8...2...",
            ".........");

    public static IPuzzle NRC_28dec = new ottop.sudoku.NRCPuzzle("NRC 28 dec 2014",
            ".....2...",
            "..85..1.9",
            ".......6.",
            "..39.....",
            ".........",
            ".....3...",
            ".24..5...",
            ".8.7.....",
            "...1....7");

    public static IPuzzle NRC_17nov = new ottop.sudoku.NRCPuzzle("NRC 17 nov 2014",
            ".86...3..",
            "..95.....",
            "......1.8",
            "1.7.4.5..",
            "2........",
            "........9",
            "..41.....",
            ".....5...",
            ".........");

    // TODO: there is nothing standard about this one
    public static IPuzzle EOC_dec14 = new StandardPuzzle("Char puzzle",
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

}
