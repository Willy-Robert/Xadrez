package model.pieces;

import model.board.Board;
import model.board.Position;

import java.util.ArrayList;
import java.util.List;

public class Bishop extends Piece {

    public Bishop(Board b, boolean w) {
        super(b, w);
    }

    @Override
    public String getSymbol() {
        return "B";
    }

    @Override
    public Piece copyFor(Board newBoard) {
        Bishop copy = new Bishop(newBoard, isWhite());
        copy.moved = this.moved;
        if (this.position != null) {
            copy.position = this.position.copy();
        }
        return copy;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        ray(moves, -1, -1);
        ray(moves, -1, 1);
        ray(moves, 1, -1);
        ray(moves, 1, 1);
        return moves;
    }

    private void ray(List<Position> moves, int dr, int dc) {
        int r = position.getRow() + dr;
        int c = position.getColumn() + dc;
        Position p = new Position(r, c);

        while (p.isValid()) {
            Piece q = board.get(p);

            if (q == null) {
                moves.add(p);
            } else {
                if (q.isWhite() != this.isWhite()) {
                    moves.add(p);
                }
                break;
            }

            r += dr;
            c += dc;
            p = new Position(r, c);
        }
    }
}