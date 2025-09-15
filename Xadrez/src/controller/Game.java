package controller;

import model.board.Board;
import model.board.Position;
import model.pieces.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Game {
    private int whiteChecks = 0;
    private int blackChecks = 0;
    private boolean whiteLostByChecks = false;
    private boolean blackLostByChecks = false;

    private Board board;
    private boolean whiteToMove = true;
    private boolean gameOver = false;
    private Position enPassantTarget = null;
    private final List<String> history = new ArrayList<>();
    private boolean suppressEndCheck = false;

    public Game() {
        this.board = new Board();
        setupPieces();
    }

    public Board board() { return board; }

    public boolean whiteToMove() { return whiteToMove; }

    public boolean isGameOver() { return gameOver; }

    public List<String> history() { return Collections.unmodifiableList(history); }

    public List<Position> legalMovesFrom(Position from) {
        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return List.of();

        List<Position> pseudoMoves = p.getPossibleMoves();
        List<Position> legalMoves = new ArrayList<>();

        for (Position to : pseudoMoves) {
            Game copy = snapshot();
            copy.suppressEndCheck = true;
            copy.move(from, to, null);
            if (!copy.inCheck(whiteToMove)) {
                legalMoves.add(to);
            }
        }

        return legalMoves;
    }

    public boolean isPromotion(Position from, Position to) {
        Piece p = board.get(from);
        if (!(p instanceof Pawn)) return false;
        return p.isWhite() ? to.getRow() == 0 : to.getRow() == 7;
    }

    public void move(Position from, Position to, Character promotion) {
        if (gameOver) return;
        if (from == null || to == null || !board.isInside(from) || !board.isInside(to)) return;

        Piece p = board.get(from);
        if (p == null || p.isWhite() != whiteToMove) return;

        boolean isKing = (p instanceof King);
        int dCol = Math.abs(to.getColumn() - from.getColumn());

        // Roque
        if (isKing && dCol == 2) {
            int row = from.getRow();
            board.set(to, p);
            board.set(from, null);

            if (to.getColumn() == 6) {
                Piece rook = board.get(new Position(row, 7));
                board.set(new Position(row, 5), rook);
                board.set(new Position(row, 7), null);
                if (rook != null) rook.setMoved(true);
                addHistory("O-O");
            } else {
                Piece rook = board.get(new Position(row, 0));
                board.set(new Position(row, 3), rook);
                board.set(new Position(row, 0), null);
                if (rook != null) rook.setMoved(true);
                addHistory("O-O-O");
            }

            p.setMoved(true);
            enPassantTarget = null;
            whiteToMove = !whiteToMove;
            if (!suppressEndCheck) checkEndConditions();
            return;
        }

        // En passant
        boolean isPawn = (p instanceof Pawn);
        boolean diagonal = from.getColumn() != to.getColumn();
        boolean toIsEmpty = board.get(to) == null;
        boolean isEnPassant = isPawn && diagonal && toIsEmpty && to.equals(enPassantTarget);
        if (isEnPassant) {
            board.set(to, p);
            board.set(from, null);
            int dir = p.isWhite() ? 1 : -1;
            board.set(new Position(to.getRow() + dir, to.getColumn()), null);
            p.setMoved(true);
            addHistory(coord(from) + "x" + coord(to) + " e.p.");
            enPassantTarget = null;
            whiteToMove = !whiteToMove;
            if (!suppressEndCheck) checkEndConditions();
            return;
        }

        // Trata promoção antes do movimento normal
        if (promotion != null && isPawn && isPromotion(from, to)) {
            // Remove o peão da posição antiga
            board.set(from, null);
            // Cria a peça promovida
            Piece promoted = switch (Character.toUpperCase(promotion)) {
                case 'R' -> new Rook(board, p.isWhite());
                case 'B' -> new Bishop(board, p.isWhite());
                case 'N' -> new Knight(board, p.isWhite());
                default  -> new Queen(board, p.isWhite());
            };
            promoted.setMoved(true);
            board.set(to, promoted);
            enPassantTarget = null;
            addHistory(coord(from) + "=" + promoted.getSymbol());
            whiteToMove = !whiteToMove;
            if (!suppressEndCheck) checkEndConditions();
            return;
        }

        // Lance normal
        Piece capturedBefore = board.get(to);
        board.set(to, p);
        board.set(from, null);
        p.setMoved(true);

        // Trata en passant
        if (isPawn && Math.abs(to.getRow() - from.getRow()) == 2) {
            int mid = (to.getRow() + from.getRow()) / 2;
            enPassantTarget = new Position(mid, from.getColumn());
        } else {
            enPassantTarget = null;
        }

        addHistory(coord(from) + (capturedBefore != null ? "x" : "-") + coord(to));

        whiteToMove = !whiteToMove;
        if (!suppressEndCheck) checkEndConditions();
    }

    public boolean inCheck(boolean whiteSide) {
        Position kingPos = board.findKing(whiteSide);
        if (kingPos == null) return false;

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                Position pos = new Position(row, col);
                Piece p = board.get(pos);
                if (p != null && p.isWhite() != whiteSide) {
                    List<Position> moves = p.getPossibleMoves();
                    if (moves.contains(kingPos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean isCheckmate(boolean whiteSide) {
        if (!inCheck(whiteSide)) return false;

        for (Piece p : board.pieces(whiteSide)) {
            for (Position to : p.getPossibleMoves()) {
                Position from = p.getPosition();
                Game copy = snapshot();
                copy.suppressEndCheck = true;
                copy.move(from, to, null);
                if (!copy.inCheck(whiteSide)) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isStalemate(boolean whiteSide) {
        if (inCheck(whiteSide)) return false;

        for (Piece p : board.pieces(whiteSide)) {
            for (Position to : p.getPossibleMoves()) {
                Position from = p.getPosition();
                Game copy = snapshot();
                copy.suppressEndCheck = true;
                copy.move(from, to, null);
                if (!copy.inCheck(whiteSide)) {
                    return false;
                }
            }
        }
        return true;
    }

    private void checkEndConditions() {
        // Verifica xeque-mate/afogamento para ambos os lados
        if (isCheckmate(true)) {
            gameOver = true;
            addHistory("Checkmate");
            return;
        }
        if (isCheckmate(false)) {
            gameOver = true;
            addHistory("Checkmate");
            return;
        }
        if (isStalemate(true)) {
            gameOver = true;
            addHistory("Stalemate");
            return;
        }
        if (isStalemate(false)) {
            gameOver = true;
            addHistory("Stalemate");
            return;
        }

        // Regra dos 3 xeques para ambos os lados
        if (inCheck(true)) {
            addHistory("Check");
            whiteChecks++;
            if (whiteChecks >= 3) {
                gameOver = true;
                whiteLostByChecks = true;
                return;
            }
        }
        if (inCheck(false)) {
            addHistory("Check");
            blackChecks++;
            if (blackChecks >= 3) {
                gameOver = true;
                blackLostByChecks = true;
                return;
            }
        }
    }

    public Game snapshot() {
        Game g = new Game();
        g.board = this.board.copy();
        g.whiteToMove = this.whiteToMove;
        g.gameOver = this.gameOver;
        g.enPassantTarget = (this.enPassantTarget == null)
                ? null
                : new Position(enPassantTarget.getRow(), enPassantTarget.getColumn());
        g.history.clear();
        g.history.addAll(this.history);
        return g;
    }

    private void addHistory(String moveStr) {
        history.add(moveStr);
    }

    private String coord(Position p) {
        char file = (char) ('a' + p.getColumn());
        int rank = 8 - p.getRow();
        return "" + file + rank;
    }

    public String getWinnerMessage() {
        if (!gameOver) return "O jogo ainda está em andamento.";

        if (whiteLostByChecks) {
            return "Brancas levaram 3 xeques. Pretas venceram!";
        } else if (blackLostByChecks) {
            return "Pretas levaram 3 xeques. Brancas venceram!";
        }

        String last = history.get(history.size() - 1);
        if (last.equals("Checkmate")) {
            return whiteToMove ? "Xeque-mate! Pretas venceram." : "Xeque-mate! Brancas venceram.";
        } else if (last.equals("Stalemate")) {
            return "Empate por afogamento!";
        } else {
            return "Empate!";
        }
    }

    public void reset() {
    board = new Board();
    whiteToMove = true;
    gameOver = false;
    whiteChecks = 0;
    blackChecks = 0;
    whiteLostByChecks = false;
    blackLostByChecks = false;
        enPassantTarget = null;
        history.clear();
        setupPieces();
    }

        private void setupPieces() {
        board.placePiece(new Rook(board, true),   new Position(7, 0));
        board.placePiece(new Knight(board, true), new Position(7, 1));
        board.placePiece(new Bishop(board, true), new Position(7, 2));
        board.placePiece(new Queen(board, true),  new Position(7, 3));
        board.placePiece(new King(board, true),   new Position(7, 4));
        board.placePiece(new Bishop(board, true), new Position(7, 5));
        board.placePiece(new Knight(board, true), new Position(7, 6));
        board.placePiece(new Rook(board, true),   new Position(7, 7));
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, true), new Position(6, c));
        }

        board.placePiece(new Rook(board, false),   new Position(0, 0));
        board.placePiece(new Knight(board, false), new Position(0, 1));
        board.placePiece(new Bishop(board, false), new Position(0, 2));
        board.placePiece(new Queen(board, false),  new Position(0, 3));
        board.placePiece(new King(board, false),   new Position(0, 4));
        board.placePiece(new Bishop(board, false), new Position(0, 5));
        board.placePiece(new Knight(board, false), new Position(0, 6));
        board.placePiece(new Rook(board, false),   new Position(0, 7));
        for (int c = 0; c < 8; c++) {
            board.placePiece(new Pawn(board, false), new Position(1, c));
        }
    }
}
