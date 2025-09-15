package model.pieces;

import model.board.Board;
import model.board.Position;

import java.util.ArrayList;
import java.util.List;

public class Queen extends Piece {

    public Queen(Board b, boolean w) {
        super(b, w);
    }

    @Override
    public String getSymbol() {
        return "Q";
    }

    @Override
    public Piece copyFor(Board newBoard) {
        Queen copy = new Queen(newBoard, isWhite);
        copy.moved = this.moved;
        copy.position = this.position != null ? this.position.copy() : null;
        return copy;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        ray(moves, -1, 0);  // cima
        ray(moves, 1, 0);   // baixo
        ray(moves, 0, -1);  // esquerda
        ray(moves, 0, 1);   // direita
        ray(moves, -1, -1); // diagonal superior esquerda
        ray(moves, -1, 1);  // diagonal superior direita
        ray(moves, 1, -1);  // diagonal inferior esquerda
        ray(moves, 1, 1);   // diagonal inferior direita
        return moves;
    }

    private void ray(List<Position> moves, int dr, int dc) {
        int r = position.getRow() + dr;
        int c = position.getColumn() + dc;

        while (new Position(r, c).isValid()) {
            Position p = new Position(r, c);
            Piece q = board.get(p);

            if (q == null) {
                moves.add(p);
            } else {
                if (q.isWhite() != this.isWhite) {
                    moves.add(p); // pode capturar
                }
                break; // bloqueia o raio
            }

            r += dr;
            c += dc;
        }
    }
}