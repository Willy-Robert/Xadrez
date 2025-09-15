package model.pieces;

import model.board.Board;
import model.board.Position;
import java.util.ArrayList;
import java.util.List;

public class Knight extends Piece {
    public Knight(Board board, boolean isWhite) {
        super(board, isWhite);
    }

    @Override
    public List<Position> getPossibleMoves() {
        List<Position> moves = new ArrayList<>();
        
        // Movimentos em L do cavalo
        addIfFreeOrEnemy(moves, position.getRow()-2, position.getColumn()-1);
        addIfFreeOrEnemy(moves, position.getRow()-2, position.getColumn()+1);
        addIfFreeOrEnemy(moves, position.getRow()-1, position.getColumn()-2);
        addIfFreeOrEnemy(moves, position.getRow()-1, position.getColumn()+2);
        addIfFreeOrEnemy(moves, position.getRow()+1, position.getColumn()-2);
        addIfFreeOrEnemy(moves, position.getRow()+1, position.getColumn()+2);
        addIfFreeOrEnemy(moves, position.getRow()+2, position.getColumn()-1);
        addIfFreeOrEnemy(moves, position.getRow()+2, position.getColumn()+1);

        return moves;
    }

    @Override
    public String getSymbol() {
        return "N";
    }

    @Override
    public Piece copyFor(Board newBoard) {
        Knight copy = new Knight(newBoard, isWhite);
        copy.position = this.position;
        copy.moved = this.moved;
        return copy;
    }
}
