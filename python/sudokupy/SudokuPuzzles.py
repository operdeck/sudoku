from sudokupy.Sudoku import NRCSudoku, Sudoku

# https://www.nrc.nl/nieuws/2018/02/03/sudoku-a1590454
# NRC saturday 3 feb 2018
# solvable by simple elimination only :(
nrc3feb2018 = \
    NRCSudoku((" 9   4 7 ",
               " 7 3  9 8",
               "         ",
               "      5  ",
               "   8     ",
               "         ",
               "5    6  9",
               " 3  41   ",
               "    7    ")
              )

# NRC saturday 29 feb 2020
# even this one requires nothing fancy
nrc29feb2018 = \
    NRCSudoku((" 1   2   ",
               "  8      ",
               "      5 3",
               "   9    2",
               "       9 ",
               "4 52     ",
               "    1 38 ",
               "         ",
               "  6      "))
puzzelbreinweek48_2020 = \
    Sudoku(("358961274",
            "642738915",
            "971425683",
            "265  9 37",
            "73    8  ",
            "   3    2",
            "    9 5  ",
            "   6   4 ",
            "         "))
