package model.pieces;

import model.board.Board;
import model.board.Position;

import java.util.ArrayList;
import java.util.List;

public class Pawn extends Piece {

    public Pawn(Board b, boolean w) {
        super(b, w);
    }

    @Override
    public String getSymbol() {
        return "P";
    }

    @Override
    public Piece copyFor(Board newBoard) {
        Pawn copy = new Pawn(newBoard, isWhite);
        copy.moved = this.moved;
        copy.position = this.position.copy(); // garante posição correta na cópia
        return copy;
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        int dir = isWhite ? -1 : 1;

        // Avanço simples
        Position oneStep = new Position(position.getRow() + dir, position.getColumn());
        if (oneStep.isValid() && board.get(oneStep) == null) {
            moves.add(oneStep);

            // Avanço duplo
            Position twoStep = new Position(position.getRow() + 2 * dir, position.getColumn());
            if (!moved && twoStep.isValid() && board.get(twoStep) == null) {
                moves.add(twoStep);
            }
        }

        // Capturas diagonais
        Position left = new Position(position.getRow() + dir, position.getColumn() - 1);
        Position right = new Position(position.getRow() + dir, position.getColumn() + 1);

        if (left.isValid()) {
            Piece target = board.get(left);
            if (target != null && target.isWhite() != isWhite) {
                moves.add(left);
            }
        }

        if (right.isValid()) {
            Piece target = board.get(right);
            if (target != null && target.isWhite() != isWhite) {
                moves.add(right);
            }
        }

        // En passant será tratado no controller
        return moves;
    }

    @Override
    public List<Position> getAttacks() {
        List<Position> attacks = new ArrayList<>();
        int dir = isWhite ? -1 : 1;

        Position left = new Position(position.getRow() + dir, position.getColumn() - 1);
        Position right = new Position(position.getRow() + dir, position.getColumn() + 1);

        if (left.isValid()) attacks.add(left);
        if (right.isValid()) attacks.add(right);

        return attacks;
    }
}