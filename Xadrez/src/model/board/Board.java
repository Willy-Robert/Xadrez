package model.board;

import model.pieces.Piece;
import java.util.ArrayList;
import java.util.List;

public class Board {
    private final Piece[][] pieces;

    public Board() {
        pieces = new Piece[8][8];
    }

    public Piece get(Position pos) {
        if (!isInside(pos)) return null;
        return pieces[pos.getRow()][pos.getColumn()];
    }

    public void set(Position pos, Piece piece) {
        if (!isInside(pos)) return;
        pieces[pos.getRow()][pos.getColumn()] = piece;
        if (piece != null) {
            piece.setPosition(pos);
        }
    }

    public void placePiece(Piece piece, Position pos) {
        if (!isInside(pos)) return;
        pieces[pos.getRow()][pos.getColumn()] = piece;
        if (piece != null) {
            piece.setPosition(pos);
        }
    }

    public boolean isInside(Position pos) {
        if (pos == null) return false;
        return pos.getRow() >= 0 && pos.getRow() < 8 && 
               pos.getColumn() >= 0 && pos.getColumn() < 8;
    }

    /** Lista todas as peças de uma cor. */
    public List<Piece> pieces(boolean white) {
        List<Piece> list = new ArrayList<>();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = pieces[r][c];
                if (p != null && p.isWhite() == white) {
                    list.add(p);
                }
            }
        }
        return list;
    }

    /**
     * Encontra a posição do rei de uma cor específica.
     * @param isWhite true se for o rei branco, false se for o rei preto.
     * @return a posição do rei ou null se não for encontrado.
     */
    public Position findKing(boolean isWhite) {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = pieces[r][c];
                if (p != null && p.isWhite() == isWhite && p.getSymbol().equals("K")) {
                    return new Position(r, c);
                }
            }
        }
        return null;
    }

    /** Cópia profunda do tabuleiro (clona peças para o novo Board). */
    public Board copy() {
        Board newBoard = new Board();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Piece p = pieces[r][c];
                if (p != null) {
                    newBoard.pieces[r][c] = p.copyFor(newBoard);
                }
            }
        }
        return newBoard;
    }
}