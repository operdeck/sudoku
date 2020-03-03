import pandas as pd
import numpy as np
import matplotlib.pyplot as plt

class Sudoku:
    """Base Sudoku class"""
    def __init__(self, sudoku):
        self.current = self.fromTextTuple(sudoku)
        self.initConstraints()

    def fromTextTuple(self, sudoku):
        allrows = list()
        if (len(sudoku) != 9):
            raise Exception('Need 9 rows')
        for r in range(9):
            arow = sudoku[r]
            allcells = list()
            if (len(arow) != 9):
                raise Exception('Need 9 cells in row '+ str(r+1) + ': \"' + arow + '"')
            for c in range(9):
                if arow[c] == ' ' or arow[c] == '.':
                    allcells.append(0)
                elif arow[c] >= '1' and arow[c] <= '9':
                    allcells.append(int(arow[c]))
                else:
                    raise Exception('Unrecognized character ' + arow[c] + ' at row ' + str(r + 1) + " col " + str(c+1) + ': \"' + arow + '"')
            allrows.append(allcells)
        return np.asmatrix(allrows)

    def initConstraints(self):
        allrows = list()
        allcols = list()
        self.allcells = list()
        for x in range(9):
            allrows.append({ "name" : "row " + str(1+x), "cells" : list()})
            allcols.append({ "name" : "col " + str(1+x), "cells" : list()})
        for i in range(9):
            for j in range(9):
                cell = (i,j)
                self.allcells.append(cell)
                allrows[i]["cells"].append(cell)
                allcols[j]["cells"].append(cell)
        self.constraints = allrows + allcols

    def place(self, digit, cell):
        self.current[cell[0], cell[1]] = digit

    def print(self):
        print(self.current)
        print( self.constraints )
        print(str(len(self.constraints)) + " constraints")

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
            if self.current[cell[0], cell[1]] == 0:
                # TODO: show cell candidates
                text = ax.text(cell[1], cell[0], "123\n456\n789",
                               ha="center", va="center", color="black", size=9, alpha=0.6)
            else:
                text = ax.text(cell[1], cell[0], self.current[cell[0], cell[1]],
                               ha="center", va="center", color="black", size=15)
        for x in range(9):
            plt.axhline(0.5+x)
            plt.axvline(0.5 + x)
        ax.set_title("Sudoku")
        fig.tight_layout()
        plt.show()

    def celltostr(self, pair):
        return str(1+pair[0])+","+str(1+pair[1])

    # TODO: keep candidates per cell
    def solver1(self):
        print('Simple elimination per cell')
        # First simple solver:
        # apply constraints per cell
        # for all cells (i,j)
        #     if not occupied (i,j)
        #           candidates = 1..9
        #           for all constraints C that (i,j) is part of
        #                  O = current values for all cells in C
        #                  remove all of O from the list of candidates
        moves = list()
        for cell in self.allcells:
            if self.current[cell[0], cell[1]] == 0:
                candidates = set(range(1,10))
                #print( cell )
                for aconstraint in self.constraints:
                    if cell in aconstraint["cells"]:
                        #print(aconstraint)
                        cellsinconstraint = list()
                        for pair in aconstraint["cells"]:
                            if self.current[pair[0], pair[1]] != 0:
                                cellsinconstraint.append(self.current[pair[0], pair[1]])
                        #print(cellsinconstraint)
                        candidates = candidates - set(cellsinconstraint)
                #print(candidates)
                if len(candidates) == 1:
                    print("Elimination results in 1 possibility at " + self.celltostr(cell) + ": " + str(list(candidates)[0]))
                    moves.append({"value" : list(candidates)[0], "cell" : cell})
        return moves

    def solver2(self):
        # Second simple solver:
        #   for all constraints C
        #       M = digits missing in C
        #
        #     look what is missing per constraint
        moves = list()
        return moves

class NRCSudoku(Sudoku):
    """ Adds rectangular areas """
    def initConstraints(self):
        super().initConstraints()
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
        self.constraints.append({ "name" : "rect 1", "cells" : r1})
        self.constraints.append({ "name" : "rect 2", "cells" : r2})
        self.constraints.append({ "name" : "rect 3", "cells" : r3})
        self.constraints.append({ "name" : "rect 4", "cells" : r4})

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
    s.print()
    #s.show()
    mvz = s.solver1()
    print(mvz)
    # if len(mvz) > 0:
    #     s.place( mvz[0]["value"], mvz[0]["cell"] )
    # s.print()
    s.show()
    # mvz = s.solver1()
    # print(mvz)


if __name__ == "__main__":
    main()
