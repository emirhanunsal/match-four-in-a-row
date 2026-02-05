package me.github.emirhanunsal.matchfourinarow.domain;

import lombok.Getter;

@Getter
public class Board {
    private static final int ROWS = 6;
    private static final int COLS = 7;
    private static final int WIN_LENGTH = 4;

    private final Disc[][] grid;

    public Board() {
        this.grid = new Disc[ROWS][COLS];
        for (int i = 0; i < ROWS; i++) {
            for (int j = 0; j < COLS; j++) {
                grid[i][j] = Disc.EMPTY;
            }
        }
    }

    public int dropDisc(int column, Disc disc) {
        if (column < 0 || column >= COLS) {
            throw new IllegalArgumentException("Invalid column: " + column);
        }

        if (disc == null || disc == Disc.EMPTY) {
            throw new IllegalArgumentException("Cannot drop EMPTY or null disc");
        }

        for (int row = ROWS - 1; row >= 0; row--) {
            if (grid[row][column] == Disc.EMPTY) {
                grid[row][column] = disc;
                return row;
            }
        }

        throw new IllegalStateException("Column " + column + " is full");
    }

    public boolean isFull() {
        for (int col = 0; col < COLS; col++) {
            if (grid[0][col] == Disc.EMPTY) {
                return false;
            }
        }
        return true;
    }

    public Disc checkWinner(int row, int col) {
        Disc disc = grid[row][col];
        if (disc == Disc.EMPTY) {
            return null;
        }

        if (checkDirection(row, col, 0, 1, disc)) return disc;
        if (checkDirection(row, col, 1, 0, disc)) return disc;
        if (checkDirection(row, col, 1, 1, disc)) return disc;
        if (checkDirection(row, col, -1, 1, disc)) return disc;

        return null;
    }

    private boolean checkDirection(int row, int col, int deltaRow, int deltaCol, Disc disc) {
        int count = 0;

        for (int direction = -1; direction <= 1; direction += 2) {
            int r = row;
            int c = col;

            while (isValid(r, c) && grid[r][c] == disc) {
                count++;
                r += direction * deltaRow;
                c += direction * deltaCol;
            }
        }

        count--;
        return count >= WIN_LENGTH;
    }

    private boolean isValid(int row, int col) {
        return row >= 0 && row < ROWS && col >= 0 && col < COLS;
    }

    public Disc getDisc(int row, int col) {
        if (!isValid(row, col)) {
            throw new IllegalArgumentException("Invalid position: (" + row + ", " + col + ")");
        }
        return grid[row][col];
    }

    public boolean isColumnFull(int column) {
        if (column < 0 || column >= COLS) {
            return true;
        }
        return grid[0][column] != Disc.EMPTY;
    }
}
