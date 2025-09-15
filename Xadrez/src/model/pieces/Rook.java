package model.pieces;

import model.board.Board;
import model.board.Position;

import java.util.ArrayList;
import java.util.List;

public class Rook extends Piece {

    public Rook(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public String getSymbol() {
        return "R";
    }

    @Override
    public Piece copyFor(Board newBoard) {
        Rook copy = new Rook(newBoard, this.isWhite());
        copy.moved = this.moved;
        copy.position = this.position.copy(); // garante posição correta na cópia
        return copy;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        Position from = getPosition();
        if (from == null) return moves;

        addRay(moves, from, -1,  0); // cima
        addRay(moves, from,  1,  0); // baixo
        addRay(moves, from,  0, -1); // esquerda
        addRay(moves, from,  0,  1); // direita

        return moves;
    }

    private void addRay(List<Position> acc, Position from, int dRow, int dCol) {
        int r = from.getRow();
        int c = from.getColumn();

        while (true) {
            r += dRow;
            c += dCol;

            if (r < 0 || r > 7 || c < 0 || c > 7) break;

            Position to = new Position(r, c);
            Piece occ = board.get(to);

            if (occ == null) {
                acc.add(to);
            } else {
                if (occ.isWhite() != this.isWhite()) {
                    acc.add(to); // pode capturar
                }
                break; // bloqueia o raio
            }
        }
    }
}