package model.pieces;

import model.board.Board;
import model.board.Position;

import java.util.List;

public abstract class Piece {
    protected Position position;
    protected final boolean isWhite;
    protected final Board board;
    protected boolean moved = false;

    public Piece(Board board, boolean isWhite) {
        this.board = board;
        this.isWhite = isWhite;
    }

    public boolean isWhite() {
        return isWhite;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean hasMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    /**
     * Retorna os movimentos possíveis da peça (sem considerar xeque).
     */
    public abstract List<Position> getPossibleMoves();

    /**
     * Retorna as casas atacadas pela peça.
     * Para peões, difere dos movimentos possíveis.
     */
    public List<Position> getAttacks() {
        return getPossibleMoves();
    }

    /**
     * Retorna o símbolo da peça: K, Q, R, B, N, P
     */
    public abstract String getSymbol();

    /**
     * Cria uma cópia da peça para outro tabuleiro.
     */
    public abstract Piece copyFor(Board newBoard);

    /**
     * Verifica se a casa está vazia.
     */
    protected boolean empty(int r, int c) {
        Position p = new Position(r, c);
        return p.isValid() && board.get(p) == null;
    }

    /**
     * Verifica se há uma peça inimiga na casa.
     */
    protected boolean enemy(int r, int c) {
        Position p = new Position(r, c);
        if (!p.isValid()) return false;
        Piece q = board.get(p);
        return q != null && q.isWhite() != this.isWhite;
    }

    /**
     * Adiciona a posição à lista se estiver vazia ou ocupada por inimigo.
     */
    protected void addIfFreeOrEnemy(List<Position> list, int r, int c) {
        Position p = new Position(r, c);
        if (!p.isValid()) return;
        Piece q = board.get(p);
        if (q == null || q.isWhite() != this.isWhite) {
            list.add(p);
        }
    }
}
