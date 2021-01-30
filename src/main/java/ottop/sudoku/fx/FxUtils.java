package ottop.sudoku.fx;

import javafx.geometry.VPos;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import ottop.sudoku.board.AbstractGroup;
import ottop.sudoku.board.Coord;
import ottop.sudoku.puzzle.ISudoku;

import java.util.Collection;
import java.util.List;
import java.util.Set;

public class FxUtils {
    public static void drawGroup(Canvas canvas, ISudoku p, AbstractGroup g) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        Set<Coord> coords = g.getCoords();

        // for all coords c
        // if not cell left of c in g draw left border
        for (Coord c: coords) {
            int x = c.getX();
            int y = c.getY();
            double posXLeft = getCellX(canvas, p, x);
            double posXRight = getCellX(canvas, p, x+1);
            double posYTop = getCellY(canvas, p, y);
            double posYBottom = getCellY(canvas, p, y+1);
            if (x == 0 || !g.isInGroup(new Coord(x-1, y))) {
                gc.strokeLine(posXLeft, posYBottom, posXLeft, posYTop);
            }
            if (x > p.getWidth() || !g.isInGroup(new Coord(x+1, y))) {
                gc.strokeLine(posXRight, posYBottom, posXRight, posYTop);
            }
            if (y == 0 || !g.isInGroup(new Coord(x, y-1))) {
                gc.strokeLine(posXLeft, posYTop, posXRight, posYTop);
            }
            if (y > p.getHeight() || !g.isInGroup(new Coord(x, y+1))) {
                gc.strokeLine(posXLeft, posYBottom, posXRight, posYBottom);
            }
        }
//        int xMin=Integer.MAX_VALUE, yMin=Integer.MAX_VALUE;
//        int xMax=Integer.MIN_VALUE, yMax= Integer.MIN_VALUE;
//        for (Coord c: coords) {
//            xMin = Math.min(xMin, c.getX());
//            xMax = Math.max(xMax, c.getX());
//            yMin = Math.min(yMin, c.getY());
//            yMax = Math.max(yMax, c.getY());
//        }
//        gc.strokeRect(getCellX(canvas, p, xMin), getCellY(canvas, p, yMin),
//                (xMax-xMin+1)*getCellWidth(canvas, p), (yMax-yMin+1)*getCellHeight(canvas, p));
//
    }

    public static void drawPuzzleOnCanvas(Canvas canvas, ISudoku p, Coord currentPosition,
                                          Collection<Set<Coord>> highlightedCells,
                                          List<AbstractGroup> highlightedGroups) {
        // Canvas
        GraphicsContext gc = canvas.getGraphicsContext2D();
        double canvasHeight = canvas.getHeight();
        double canvasWidth = canvas.getWidth();

        // Big white background
        gc.setFill(Color.WHITE);
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(1);
        gc.fillRect(0, 0, canvasWidth, canvasHeight);

        // For buddy cells
        Set<Coord> buddies = p.getBuddies(currentPosition);

        // The highlighted cells may come in multiple groups each with different colors
        Color[] colors = {Color.BLACK, Color.DARKGREEN, Color.DARKORANGE, Color.MEDIUMVIOLETRED,
        Color.SADDLEBROWN, Color.FIREBRICK, Color.LIME, Color.AQUA, Color.SLATEBLUE};

        // Cell backgrounds
        for (Coord c: p.getAllCells()) {
            Color background = Color.WHITE;
            // Find the highlight group of this cell to determine its color
            int highlightGroupIdx = getHighlightGroupIdx(highlightedCells, c);
            if (highlightGroupIdx != -1) {
                background = Color.GOLD;
            } else {
                if (p.isAtOverlay(c)) {
                    background = Color.PALEGREEN;
                    if (currentPosition != null && p.getBuddies(currentPosition).contains(c))
                        background = Color.MEDIUMSEAGREEN;
                } else {
                    if (currentPosition != null && p.getBuddies(currentPosition).contains(c))
                        background = Color.LIGHTGRAY;
                }
            }
            gc.setFill(background);
            gc.fillRect(getCellX(canvas, p, c.getX()), getCellY(canvas, p, c.getY()),
                    getCellWidth(canvas, p), getCellHeight(canvas, p));
        }

        // Text in cells
        Font cellText = Font.font("Helvetica", 15);
        gc.setFont(cellText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);
        gc.setLineWidth(1);
        for (Coord c : p.getAllCells()) {
            gc.setStroke(Color.BLUE);
            gc.strokeRect(getCellX(canvas, p, c.getX()), getCellY(canvas, p, c.getY()),
                    getCellWidth(canvas, p), getCellHeight(canvas, p));
            if (p.isOccupied(c)) {
                if (c.equals(currentPosition)) {
                    gc.setStroke(Color.DARKGRAY);
                } else {
                    gc.setStroke(Color.BLACK);
                }
                gc.strokeText(p.getSymbolAtCoordinates(c),
                        getCellX(canvas, p, c.getX() + 0.5), getCellY(canvas, p, c.getY() + 0.5));
            }
        }

        // Group boundaries
        gc.setStroke(Color.BLUE);
        gc.setLineWidth(3);
        for (AbstractGroup g: p.getGroupsWithVisualBoundary()) {
            drawGroup(canvas, p, g);
        }

        // Highlighted groups
        if (highlightedGroups != null) {
            gc.setStroke(Color.ORANGE);
            gc.setLineWidth(3);
            for (AbstractGroup g : highlightedGroups) {
                drawGroup(canvas, p, g);
            }
        }

        // Cell borders for highlighted cells
        gc.setLineWidth(3);
        for (Coord c : p.getAllCells()) {
            int highlightGroupIdx = getHighlightGroupIdx(highlightedCells, c);
            if (highlightGroupIdx >= 0) {
                Color border = colors[highlightGroupIdx % colors.length];
                gc.setStroke(border);
//                if (highlightGroupIdx == 0 && highlightedCells.size()>1) {
//                    gc.strokeOval(getCellX(canvas, p, c.getX()), getCellY(canvas, p, c.getY()),
//                            getCellWidth(canvas, p), getCellHeight(canvas, p));
//                }
                gc.strokeRect(getCellX(canvas, p, c.getX()), getCellY(canvas, p, c.getY()),
                        getCellWidth(canvas, p), getCellHeight(canvas, p));
            }
        }

    }

    private static int getHighlightGroupIdx(Collection<Set<Coord>> highlightedCells, Coord c) {
        int highlightGroupIdx = -1;
        if (highlightedCells != null) {
            int idx = 0;
            for (Set<Coord> cs : highlightedCells) {
                if (cs.contains(c)) {
                    highlightGroupIdx = idx;
                    break;
                }
                idx++;
            }
        }
        return highlightGroupIdx;
    }

    public static void drawPossibilities(Canvas canvas, ISudoku p, Coord c,
                                         Set<Integer> candidatesAtCell,
                                         Set<Integer> candidatesAtCellAfterSimpleElimination) {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        gc.setStroke(Color.DIMGRAY);
        gc.setLineWidth(0.5);
        Font smallText = Font.font("Helvetica", 8);
        gc.setFont(smallText);
        gc.setTextAlign(TextAlignment.CENTER);
        gc.setTextBaseline(VPos.CENTER);

        int n = p.getSymbolCodeRange() - 1; // minus empty cell code
        int nmarkerrows = (int) Math.sqrt(n);
        int nmarkercols = (int) Math.ceil(n / (double) nmarkerrows);

        for (int symbolCode : candidatesAtCellAfterSimpleElimination) {
            int subrow = (symbolCode - 1) / nmarkercols;
            int subcol = (symbolCode - 1) % nmarkercols;
            double relx = c.getX() + 0.15 + 0.7 * subcol / (double) (nmarkercols - 1);
            double rely = c.getY() + 0.15 + 0.7 * subrow / (double) (nmarkerrows - 1);
            gc.strokeText(p.symbolCodeToSymbol(symbolCode), getCellX(canvas, p, relx), getCellY(canvas, p, rely));

            // Strike through if eliminated
            if (!candidatesAtCell.contains(symbolCode)) {
                double relsizex = 0.7 * 0.4 / (double) (nmarkercols - 1);
                double relsizey = 0.7 * 0.4 / (double) (nmarkerrows - 1);
                gc.strokeLine(getCellX(canvas, p, relx - relsizex), getCellY(canvas, p, rely),
                        getCellX(canvas, p, relx + relsizex), getCellY(canvas, p, rely));
            }
        }
    }



    static double getCellX(Canvas canvas, ISudoku p, double x) {
        return (5 + x * getCellWidth(canvas, p));
    }

    static double getCellY(Canvas canvas, ISudoku p, double y) {
        return (5 + y * getCellHeight(canvas, p));
    }

    static double getCellWidth(Canvas canvas, ISudoku p) {
        double canvasWidth = canvas.getWidth();
        return (canvasWidth - 10) / p.getWidth();
    }

    static double getCellHeight(Canvas canvas, ISudoku p) {
        double canvasHeight = canvas.getHeight();
        return (canvasHeight - 10) / p.getHeight();
    }


}
