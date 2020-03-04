import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

class Sudoku:
    """Base Sudoku class"""
    def __init__(self, sudoku):
        self.puzzle = self.fromTextTuple(sudoku)
        self.initGroups()
        self.initPossibilities()

    def fromTextTuple(self, sudoku):
        puzzle = dict()
        if (len(sudoku) != 9):
            raise Exception('Need 9 rows')
        for r in range(9):
            arow = sudoku[r]
            if (len(arow) != 9):
                raise Exception('Need 9 cells in row '+ str(r+1) + ': \"' + arow + '"')
            for c in range(9):
                if arow[c] == ' ' or arow[c] == '.':
                    pass
                elif arow[c] >= '1' and arow[c] <= '9':
                    puzzle[(r,c)] = int(arow[c])
                else:
                    raise Exception('Unrecognized character ' + arow[c] + ' at row ' + str(r + 1) + " col " + str(c+1) + ': \"' + arow + '"')
        return puzzle

    def initGroups(self):
        allrows = list()
        allcols = list()
        allsquares = list()
        self.allcells = list()
        for x in range(9):
            allrows.append({ "name" : "row " + str(1+x), "cells" : list()})
            allcols.append({ "name" : "col " + str(1+x), "cells" : list()})
            allsquares.append({"name": "sqr " + str(1 + x), "cells": list()})
        for i in range(9):
            for j in range(9):
                cell = (i,j)
                self.allcells.append(cell)
                allrows[i]["cells"].append(cell)
                allcols[j]["cells"].append(cell)
                allsquares[3*(i//3)+(j//3)]["cells"].append(cell)
        self.groups = allrows + allcols + allsquares

    def initPossibilities(self):
        self.possibilities = dict()
        self.explanations = dict()
        for cell in self.allcells:
            self.possibilities[cell] = set(range(1,10))
            self.explanations[cell] = []

    def place(self, digit, cell):
        self.puzzle[cell] = digit
        self.initPossibilities()

    def print(self):
        print(self.puzzle) # rather useless
        print( self.groups )
        print(str(len(self.groups)) + " groups")

    def getShowRaster(self):
        mx = np.zeros((9,9))
        # color some squares to highlight the structure
        for x in range(0,3):
            for y in range(0,3):
                mx[x,y] = 1
                mx[x+6,y] = 1
                mx[x, y+6] = 1
                mx[x+6, y+6] = 1
                mx[x+3, y+3] = 1
        return mx

    def show(self):
        fig, ax = plt.subplots()
        im = ax.imshow(self.getShowRaster(), cmap="Purples", alpha=0.6) # strange way to plot a grid - need something simpler...
        # https://matplotlib.org/3.1.0/tutorials/colors/colormaps.html
        # All ticks and label them with the respective list entries
        ax.set_xticks(np.arange(9))
        ax.set_yticks(np.arange(9))
        ax.set_xticklabels(range(1,10))
        ax.set_yticklabels(range(1,10))
        for cell in self.allcells:
            if cell in self.puzzle:
                ax.text(cell[1], cell[0], self.puzzle[cell], ha="center", va="center", color="black", size=15)
            else:
                p = self.possibilities[(cell[0], cell[1])]
                txt = ""
                for i in range(1, 10):
                    if i in p:
                        txt = txt + str(i)
                    else:
                        txt = txt + "."
                    if i < 9 and i % 3 == 0:
                        txt = txt + "\n"
                ax.text(cell[1], cell[0], txt, ha="center", va="center", color="black", size=9, alpha=0.6, family='monospace')
        for x in range(8):
            if x==2 or x==5:
                pass
            else:
                plt.axhline(0.5+x, color="grey")
                plt.axvline(0.5 + x, color="grey")
        for x in range(8):
            if x==2 or x==5:
                plt.axhline(0.5+x, color="black")
                plt.axvline(0.5 + x, color="black")
        ax.set_title("Sudoku")
        fig.tight_layout()
        plt.show()

    def celltostr(self, pair):
        return str(1+pair[0])+","+str(1+pair[1])

    # TODO: keep candidates per cell
    # Filters the existing possibilities per cell by applying elimination
    # from all groups that this cell is part of.
    def groupElimination(self):
        print('Simple elimination per cell')
        for cell in set(self.allcells) - set(self.puzzle):
            candidates = self.possibilities[cell]
            for agroup in self.groups:
                if cell in agroup["cells"]:
                    digitsingroup = set([self.puzzle[c] for c in agroup["cells"] if c in self.puzzle])
                    commonelements = candidates.intersection(digitsingroup)
                    if (len(commonelements) > 0):
                        candidates = candidates - digitsingroup
                        self.explanations[cell].append(agroup["name"] + ": " + str(sorted(commonelements)))
            self.possibilities[cell] = candidates
            # TODO below belongs elsewhere
            if len(candidates) == 1:
                print("Elimination results in 1 possibility at " + self.celltostr(cell) + ": " + str(list(candidates)[0]))
                print("because: " + "; ".join(self.explanations[cell]))

    def solver2(self):
        # Second simple solver:
        #   for all groups C
        #       M = digits missing in C
        #
        #     look what is missing per group
        moves = list()
        return moves

class NRCSudoku(Sudoku):
    """ Adds rectangular areas """
    def initGroups(self):
        super().initGroups()
        r1 = list()
        r2 = list()
        r3 = list()
        r4 = list()
        for i in range(9):
            for j in range(9):
                if (i >= 1 and i <= 3 and j >= 1 and j <= 3):
                    r1.append( (i,j) )
                if (i >= 1 and i <= 3 and j >= 5 and j <= 7):
                    r2.append((i, j))
                if (i >= 5 and i <= 7 and j >= 1 and j <= 3):
                    r3.append((i, j))
                if (i >= 5 and i <= 7 and j >= 5 and j <= 7):
                    r4.append((i, j))
        self.groups.append({ "name" : "nrc 1", "cells" : r1})
        self.groups.append({ "name" : "nrc 2", "cells" : r2})
        self.groups.append({ "name" : "nrc 3", "cells" : r3})
        self.groups.append({ "name" : "nrc 4", "cells" : r4})

    def getShowRaster(self):
        mx = super().getShowRaster()
        for x in range(0,3):
            for y in range(0,3):
                mx[x+1,y+1] = 2
                mx[x+5,y+1] = 2
                mx[x+1,y+5] = 2
                mx[x+5,y+5] = 2
        return mx

def main():
    # https://www.nrc.nl/nieuws/2018/02/03/sudoku-a1590454
    # NRC saturday 3 feb 2018
    s = NRCSudoku((" 9   4 7 ",
                   " 7 3  9 8",
                   "         ",
                   "      5  ",
                   "   8     ",
                   "         ",
                   "5    6  9",
                   " 3  41   ",
                   "    7    ")
                  )

    s1 = NRCSudoku((" 1   2   ",
                   "  8      ",
                   "      5 3",
                   "   9    2",
                   "       9 ",
                   "4 52     ",
                   "    1 38 ",
                   "         ",
                   "  6      "))
    s.print()
    #s.show()
    mvz = s.groupElimination()
    print(mvz)
    # if len(mvz) > 0:
    #     s.place( mvz[0]["value"], mvz[0]["cell"] )
    # s.print()
    s.show()
    # mvz = s.groupElimination()
    # print(mvz)


if __name__ == "__main__":
    main()
