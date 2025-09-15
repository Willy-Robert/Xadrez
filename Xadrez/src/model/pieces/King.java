package model.pieces;

import model.board.Board;
import model.board.Position;

import java.util.ArrayList;
import java.util.List;

public class King extends Piece {

    public King(Board b, boolean w) {
        super(b, w);
    }

    @Override
    public String getSymbol() {
        return "K";
    }

    @Override
    public Piece copyFor(Board newBoard) {
        King copy = new King(newBoard, isWhite);
        copy.moved = this.moved;
        copy.position = this.position.copy(); // garante posição correta na cópia
        return copy;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                if (dr != 0 || dc != 0) {
                    int r = position.getRow() + dr;
                    int c = position.getColumn() + dc;
                    addIfFreeOrEnemy(moves, r, c);
                }
            }
        }

        // Roques tratados no controller.Game
        return moves;
    }
}