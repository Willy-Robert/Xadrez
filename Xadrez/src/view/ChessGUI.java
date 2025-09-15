package view;

import controller.Game;
import model.board.Position;
import model.pieces.Pawn;
import model.pieces.Piece;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.util.ArrayList;
import java.util.List;

public class ChessGUI extends JFrame {
    private boolean vsAI = false;
    // Nível da IA
    private int aiLevel = 0; // 0: Fácil, 1: Médio, 2: Difícil

    // IA simples para jogar com as pretas
    private void playBlackAI() {
        if (!game.whiteToMove() && !game.isGameOver()) {
            var moves = new ArrayList<MoveInfo>();
            var captureMoves = new ArrayList<MoveInfo>();
            var checkMoves = new ArrayList<MoveInfo>();
            for (int r = 0; r < 8; r++) {
                for (int c = 0; c < 8; c++) {
                    Position from = new Position(r, c);
                    Piece p = game.board().get(from);
                    if (p != null && !p.isWhite()) {
                        for (Position to : game.legalMovesFrom(from)) {
                            Piece target = game.board().get(to);
                            // Simula o movimento
                            Game sim = game.snapshot();
                            Character promo = null;
                            if (p instanceof Pawn && sim.isPromotion(from, to)) promo = 'Q';
                            sim.move(from, to, promo);
                            if (sim.inCheck(false)) continue; // Não faz se deixa o rei em xeque
                            MoveInfo mi = new MoveInfo(from, to);
                            moves.add(mi);
                            if (target != null && target.isWhite()) captureMoves.add(mi);
                            if (sim.inCheck(true)) checkMoves.add(mi);
                        }
                    }
                }
            }
            MoveInfo move = null;
            if (aiLevel == 0) { // Fácil: aleatório
                if (!moves.isEmpty()) move = moves.get((int)(Math.random() * moves.size()));
            } else if (aiLevel == 1) { // Médio: captura/xeque
                if (!captureMoves.isEmpty()) move = captureMoves.get((int)(Math.random() * captureMoves.size()));
                else if (!checkMoves.isEmpty()) move = checkMoves.get((int)(Math.random() * checkMoves.size()));
                else if (!moves.isEmpty()) move = moves.get((int)(Math.random() * moves.size()));
            } else { // Difícil: xeque/captura/aleatório
                if (!checkMoves.isEmpty()) move = checkMoves.get((int)(Math.random() * checkMoves.size()));
                else if (!captureMoves.isEmpty()) move = captureMoves.get((int)(Math.random() * captureMoves.size()));
                else if (!moves.isEmpty()) move = moves.get((int)(Math.random() * moves.size()));
            }
            if (move != null) {
                Character promo = null;
                Piece moving = game.board().get(move.from);
                if (moving instanceof Pawn && game.isPromotion(move.from, move.to)) promo = 'Q';
                game.move(move.from, move.to, promo);
                refresh();
            }
        }
    }

    // Classe auxiliar para guardar movimentos
    private static class MoveInfo {
        Position from, to;
        MoveInfo(Position f, Position t) { from = f; to = t; }
    }

    private Game game;
    private final JPanel boardPanel;
    private final JButton[][] squares = new JButton[8][8];
    private final JLabel status;
    private final JLabel labelTurn;
    private final JTextArea history;
    private final JScrollPane historyScroll;

    // Placar manual
    private int scoreWhite = 0;
    private int scoreBlack = 0;
    private final JLabel labelScoreWhite = new JLabel();
    private final JLabel labelScoreBlack = new JLabel();

    private final JButton btnNovoJogo = new JButton("Novo");
    private final JButton btnDesfazer = new JButton("Desfazer");

    private final List<Game> undoStack = new ArrayList<>();

    private Position selected = null;
    private List<Position> legalForSelected = new ArrayList<>();

    private static final Border BORDER_SELECTED = BorderFactory.createLineBorder(new Color(0, 0, 255), 3);
    private static final Border BORDER_LEGAL = BorderFactory.createLineBorder(new Color(0, 200, 0), 3);

    // Cores para os botões
    private final Color btnBg = new Color(120, 180, 240);
    private final Color btnFg = Color.WHITE;
    private final Color btnHover = new Color(70, 130, 180);

    public ChessGUI() {
        super("ChessGame");
        // Configuração visual do botão Desfazer
        btnDesfazer.setBackground(new Color(70, 130, 180));
        btnDesfazer.setOpaque(true);
        btnDesfazer.setIcon(null);
        btnDesfazer.setHorizontalTextPosition(SwingConstants.CENTER);
        // Janela moderna de escolha de modo de jogo, SEM opção de imagem
        vsAI = false;
        JDialog dialog = new JDialog((Frame) null, "Escolha o modo de jogo", true);
        dialog.setSize(400, 260);
        dialog.setLayout(new BorderLayout());
        // Painel de fundo com imagem
        JPanel bgPanel = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                // Opção 1: Cor sólida
                g.setColor(new Color(40, 60, 120)); // Altere as cores RGB aqui
                g.fillRect(0, 0, getWidth(), getHeight());
                
                // OU Opção 2: Gradiente
                /*
                Graphics2D g2d = (Graphics2D) g.create();
                GradientPaint gp = new GradientPaint(
                    0, 0, new Color(30, 50, 100),     // Cor do topo
                    0, getHeight(), new Color(10, 20, 40)  // Cor do fundo
                );
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
                g2d.dispose();
                */
                
                // OU Opção 3: Imagem de fundo
                /*
                Image img = new ImageIcon("resources/fundo.png").getImage();
                Graphics2D g2d = (Graphics2D) g.create();
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.60f));
                g2d.drawImage(img, 0, 0, getWidth(), getHeight(), this);
                g2d.dispose();
                */
            }
        };
        bgPanel.setLayout(new BoxLayout(bgPanel, BoxLayout.Y_AXIS));
        bgPanel.setOpaque(false);
        JLabel title = new JLabel("Escolha o modo de jogo");
        title.setFont(new Font("Segoe UI", Font.BOLD, 22));
        title.setForeground(new Color(255, 255, 255));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        bgPanel.add(Box.createVerticalStrut(28));
        bgPanel.add(title);
        bgPanel.add(Box.createVerticalStrut(18));
        JButton btnManual = new JButton("Manual");
        JButton btnIA = new JButton("Contra IA");
        Font btnFont = new Font("Segoe UI", Font.BOLD, 16);
        Dimension btnSize = new Dimension(140, 36);
        JButton[] btns = {btnManual, btnIA};
        for (JButton btn : btns) {
            btn.setFont(btnFont);
            btn.setPreferredSize(btnSize);
            btn.setBackground(btnBg);
            btn.setForeground(btnFg);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(btnHover); }
                public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(btnBg); }
            });
        }
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.add(btnManual);
        btnPanel.add(Box.createHorizontalStrut(16));
        btnPanel.add(btnIA);
        bgPanel.add(btnPanel);
        dialog.setContentPane(bgPanel);
        final int[] escolha = {0};
    btnManual.addActionListener(evt -> { escolha[0] = 0; dialog.dispose(); });
    btnIA.addActionListener(evt -> {
        JDialog nivelDialog = new JDialog((Frame) null, "Escolha o nível da IA", true);
        nivelDialog.setSize(340, 160);
        nivelDialog.setLayout(new BorderLayout());
        JPanel nivelPanel = new JPanel();
        nivelPanel.setLayout(new BoxLayout(nivelPanel, BoxLayout.Y_AXIS));
        nivelPanel.setOpaque(false);
        JLabel lbl = new JLabel("Escolha o nível da IA:");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        nivelPanel.add(Box.createVerticalStrut(18));
        nivelPanel.add(lbl);
        nivelPanel.add(Box.createVerticalStrut(18));
        JPanel btnNivelPanel = new JPanel();
        btnNivelPanel.setLayout(new BoxLayout(btnNivelPanel, BoxLayout.X_AXIS));
        btnNivelPanel.setOpaque(false);
        String[] niveis = {"Fácil", "Médio", "Difícil"};
        Font btnFontNivel = new Font("Segoe UI", Font.BOLD, 15);
        for (int i = 0; i < niveis.length; i++) {
            JButton btnNivel = new JButton(niveis[i]);
            btnNivel.setFont(btnFontNivel);
            btnNivel.setBackground(btnBg);
            btnNivel.setForeground(btnFg);
            btnNivel.setFocusPainted(false);
            btnNivel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 220), 1, true),
                BorderFactory.createEmptyBorder(8, 16, 8, 16)));
            btnNivel.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btnNivel.setOpaque(true);
            final int nivelEscolhido = i;
            btnNivel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { btnNivel.setBackground(btnHover); }
                public void mouseExited(java.awt.event.MouseEvent evt) { btnNivel.setBackground(btnBg); }
            });
            btnNivel.addActionListener(nivelEvt -> {
                aiLevel = nivelEscolhido;
                nivelDialog.dispose();
                dialog.dispose();
                escolha[0] = 1;
            });
            btnNivelPanel.add(btnNivel);
            if (i < niveis.length - 1) btnNivelPanel.add(Box.createHorizontalStrut(16));
        }
        nivelPanel.add(btnNivelPanel);
        nivelDialog.setContentPane(nivelPanel);
        nivelDialog.setLocationRelativeTo(dialog);
        nivelDialog.setVisible(true);
    });
        dialog.setLocationRelativeTo(null);
        dialog.setVisible(true);
        vsAI = (escolha[0] == 1);
        this.game = new Game();
        // Imagem de fundo fixa
    // Imagem de fundo fixa (caso queira usar depois):
    // Image backgroundImage = new ImageIcon("resources/fundo.png").getImage();

        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        boardPanel = new JPanel(new GridLayout(8, 8, 0, 0));
        boardPanel.setBackground(Color.DARK_GRAY);

        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                final int rr = r;
                final int cc = c;
                JButton b = new JButton();
                b.setMargin(new Insets(0, 0, 0, 0));
                b.setFocusPainted(false);
                b.setOpaque(true);
                b.setBorderPainted(true);
                b.setContentAreaFilled(true);
                b.setFont(b.getFont().deriveFont(Font.BOLD, 24f));
                b.addActionListener(evt -> handleClick(new Position(rr, cc)));
                squares[r][c] = b;
                boardPanel.add(b);
            }
        }

        labelTurn = new JLabel("Turno atual: Brancas");
        labelTurn.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        status = new JLabel("Vez: Brancas");
        status.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        history = new JTextArea(10, 20);
        history.setEditable(false);
        history.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
        historyScroll = new JScrollPane(history);

    JPanel rightPanel = new JPanel(new BorderLayout(6, 6));
    rightPanel.setPreferredSize(new Dimension(180, 0)); // largura ainda menor
    rightPanel.add(new JLabel("Histórico de lances:"), BorderLayout.NORTH);
    historyScroll.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 220), 2, true));
    rightPanel.add(historyScroll, BorderLayout.CENTER);

    // Painel do placar manual corrigido
    JPanel scorePanel = new JPanel(new GridBagLayout());
        // Visual moderno: borda arredondada e sombra
    // Sem cor de fundo no placar
    scorePanel.setOpaque(false);
    scorePanel.setBorder(BorderFactory.createLineBorder(new Color(180, 190, 220), 2, true));
    GridBagConstraints gbc = new GridBagConstraints();
    Font scoreFont = new Font(Font.SANS_SERIF, Font.BOLD, 18);
        labelScoreWhite.setFont(scoreFont.deriveFont(Font.BOLD, 22f));
        labelScoreBlack.setFont(scoreFont.deriveFont(Font.BOLD, 22f));
        labelScoreWhite.setForeground(new Color(40, 40, 60));
        labelScoreBlack.setForeground(new Color(40, 40, 60));
    labelScoreWhite.setOpaque(false);
    labelScoreBlack.setOpaque(false);
    labelScoreWhite.setBackground(new Color(0,0,0,0));
    labelScoreBlack.setBackground(new Color(0,0,0,0));
        labelScoreWhite.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 220), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        labelScoreBlack.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(180, 190, 220), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
    // Ícones dos reis
    int iconSize = 38;
    ImageIcon whiteKingIcon = ImageUtil.getPieceIcon(true, 'K', iconSize);
    ImageIcon blackKingIcon = ImageUtil.getPieceIcon(false, 'K', iconSize);
    labelScoreWhite.setIcon(whiteKingIcon);
    labelScoreBlack.setIcon(blackKingIcon);
    labelScoreWhite.setHorizontalTextPosition(SwingConstants.RIGHT);
    labelScoreBlack.setHorizontalTextPosition(SwingConstants.RIGHT);
    labelScoreWhite.setIconTextGap(16);
    labelScoreBlack.setIconTextGap(16);
    JButton btnAddWhite = new JButton("+");
    JButton btnSubWhite = new JButton("-");
    JButton btnAddBlack = new JButton("+");
    JButton btnSubBlack = new JButton("-");
    JButton btnResetWhite = new JButton("Zerar");
    JButton btnResetBlack = new JButton("Zerar");
        Font btnScoreFont = scoreFont.deriveFont(Font.BOLD, 18f);
        btnAddWhite.setFont(btnScoreFont);
        btnSubWhite.setFont(btnScoreFont);
        btnAddBlack.setFont(btnScoreFont);
        btnSubBlack.setFont(btnScoreFont);
        btnResetWhite.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        btnResetBlack.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
        Color btnScoreBg = new Color(200, 210, 240);
    // Color btnScoreHover = new Color(180, 190, 220); // removido, não utilizado
        JButton[] allBtns = {btnAddWhite, btnSubWhite, btnAddBlack, btnSubBlack, btnResetWhite, btnResetBlack};
        for (JButton btn : allBtns) {
            btn.setBackground(btnScoreBg);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createEmptyBorder());
            btn.setContentAreaFilled(false);
            btn.setOpaque(false);
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setForeground(new Color(60, 80, 180)); }
                public void mouseExited(java.awt.event.MouseEvent evt) { btn.setForeground(Color.BLACK); }
            });
        }
    // Linha 1: Brancas
    gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(10, 12, 10, 8); gbc.anchor = GridBagConstraints.CENTER;
    scorePanel.add(labelScoreWhite, gbc);
    gbc.gridx = 1; gbc.insets = new Insets(10, 4, 10, 4);
    scorePanel.add(btnAddWhite, gbc);
    gbc.gridx = 2; gbc.insets = new Insets(10, 4, 10, 4);
    scorePanel.add(btnSubWhite, gbc);
    gbc.gridx = 3; gbc.insets = new Insets(10, 4, 10, 12);
    scorePanel.add(btnResetWhite, gbc);
    // Linha 2: Pretas
    gbc.gridx = 0; gbc.gridy = 1; gbc.insets = new Insets(10, 12, 16, 8);
    scorePanel.add(labelScoreBlack, gbc);
    gbc.gridx = 1; gbc.insets = new Insets(10, 4, 16, 4);
    scorePanel.add(btnAddBlack, gbc);
    gbc.gridx = 2; gbc.insets = new Insets(10, 4, 16, 4);
    scorePanel.add(btnSubBlack, gbc);
    gbc.gridx = 3; gbc.insets = new Insets(10, 4, 16, 12);
    scorePanel.add(btnResetBlack, gbc);
    rightPanel.add(scorePanel, BorderLayout.SOUTH);

    // Adiciona os painéis de peças capturadas abaixo do placar
    // rightPanel.add(capturedPanelWhite, BorderLayout.AFTER_LAST_LINE);
    // rightPanel.add(capturedPanelBlack, BorderLayout.PAGE_END);

    // Listeners do placar
    btnAddWhite.addActionListener(evt -> { scoreWhite++; updateScore(); });
    btnSubWhite.addActionListener(evt -> { if (scoreWhite > 0) scoreWhite--; updateScore(); });
    btnAddBlack.addActionListener(evt -> { scoreBlack++; updateScore(); });
    btnSubBlack.addActionListener(evt -> { if (scoreBlack > 0) scoreBlack--; updateScore(); });
    btnResetWhite.addActionListener(evt -> { scoreWhite = 0; updateScore(); });
    btnResetBlack.addActionListener(evt -> { scoreBlack = 0; updateScore(); });

        JPanel buttonPanel = new JPanel(new GridLayout(1, 3, 4, 4));
        btnDesfazer.setIconTextGap(6);
        Font btnMainFont = new Font("Segoe UI", Font.BOLD, 11);
        Dimension btnMainSize = new Dimension(90, 28);
        Color btnMainBg = new Color(120, 180, 240);
        Color btnMainFg = Color.WHITE;
        Color btnMainHover = new Color(70, 130, 180);

        JButton[] mainBtns = {btnNovoJogo, btnDesfazer};
        for (JButton btn : mainBtns) {
            btn.setFont(btnMainFont);
            btn.setPreferredSize(btnMainSize);
            btn.setBackground(btnMainBg);
            btn.setForeground(btnMainFg);
            btn.setFocusPainted(false);
            btn.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(180, 190, 220), 1, true),
                BorderFactory.createEmptyBorder(4, 8, 4, 8)));
            btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
            btn.setOpaque(true);
            btn.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseEntered(java.awt.event.MouseEvent evt) { btn.setBackground(btnMainHover); }
                public void mouseExited(java.awt.event.MouseEvent evt) { btn.setBackground(btnMainBg); }
            });
        }
        // Fundo azul para o botão Desfazer
        btnDesfazer.setBackground(new Color(70, 130, 180));
        btnDesfazer.setIcon(null);
        btnDesfazer.setHorizontalTextPosition(SwingConstants.CENTER);
        buttonPanel.setBackground(new Color(240, 245, 255));
    buttonPanel.setBorder(BorderFactory.createEmptyBorder(8, 4, 8, 4));
    buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.Y_AXIS));
    JPanel topBtnsPanel = new JPanel(new GridLayout(1, 3, 4, 4));
    btnDesfazer.setIcon(null);
    btnDesfazer.setBackground(new Color(70, 130, 180));
    btnDesfazer.setOpaque(true);
    topBtnsPanel.setOpaque(false);
    topBtnsPanel.add(btnNovoJogo);
    topBtnsPanel.add(btnDesfazer);
    buttonPanel.add(topBtnsPanel);
        rightPanel.add(buttonPanel, BorderLayout.NORTH);

        add(labelTurn, BorderLayout.NORTH);
        add(boardPanel, BorderLayout.CENTER);
        add(status, BorderLayout.SOUTH);
        add(rightPanel, BorderLayout.EAST);

        btnNovoJogo.addActionListener(evt -> {
            // Pergunta novamente o modo ao iniciar novo jogo
            String[] optionsNovo = {"Manual", "Contra IA"};
            int modeNovo = JOptionPane.showOptionDialog(
                null,
                "Escolha o modo de jogo:",
                "Modo de Jogo",
                JOptionPane.DEFAULT_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                optionsNovo,
                optionsNovo[0]
            );
            vsAI = (modeNovo == 1);
            game = new Game();
            undoStack.clear();
            selected = null;
            legalForSelected.clear();
            btnNovoJogo.setText("Novo Jogo");
            refresh();
        });

        btnDesfazer.addActionListener(evt -> {
            if (!undoStack.isEmpty()) {
                game = undoStack.remove(undoStack.size() - 1);
                selected = null;
                legalForSelected.clear();
                refresh();
            } else {
                JOptionPane.showMessageDialog(this, "Nada para desfazer.");
            }
        });

        boardPanel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refresh();
            }
        });

        setMinimumSize(new Dimension(800, 600));
        setLocationRelativeTo(null);
        setVisible(true);

        refresh();
    }

    private void updateScore() {
        labelScoreWhite.setText(" " + scoreWhite);
        labelScoreBlack.setText(" " + scoreBlack);
    }

    private void handleClick(Position clicked) {
        if (game.isGameOver()) return;

        Piece p = game.board().get(clicked);

        if (selected == null) {
            if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
                refresh(); // Atualiza para mostrar a seleção
            }
        } else {
            if (game.legalMovesFrom(selected).contains(clicked)) {
                undoStack.add(game.snapshot());
                Character promo = null;
                Piece moving = game.board().get(selected);
                if (moving instanceof Pawn && game.isPromotion(selected, clicked)) {
                    promo = askPromotion();
                }
                game.move(selected, clicked, promo);
                selected = null;
                legalForSelected.clear();
                refresh(); // Atualiza após o movimento
            } else if (p != null && p.isWhite() == game.whiteToMove()) {
                selected = clicked;
                legalForSelected = game.legalMovesFrom(selected);
                refresh(); // Atualiza para mostrar a nova seleção
            } else {
                selected = null;
                legalForSelected.clear();
                refresh(); // Atualiza para limpar a seleção
            }
        }

        // Se for vez das pretas e IA está ativa
        if (vsAI && !game.whiteToMove() && !game.isGameOver()) {
            new javax.swing.Timer(800, e -> {
                playBlackAI();
                ((javax.swing.Timer)e.getSource()).stop();
            }).start();
        }

        if (game.isGameOver()) {
            String winnerMessage = game.getWinnerMessage();
            int choice = JOptionPane.showOptionDialog(this,
                    winnerMessage,
                    "Fim de Jogo",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.INFORMATION_MESSAGE,
                    null,
                    new String[]{"Novo Jogo", "Sair"},
                    "Novo Jogo");

            if (choice == JOptionPane.YES_OPTION) {
                game = new Game();
                undoStack.clear();
                selected = null;
                legalForSelected.clear();
                refresh();
            } else if (choice == JOptionPane.NO_OPTION) {
                System.exit(0);
            }
        }
    }

    private Character askPromotion() {
    String[] opts = {"Rainha", "Torre", "Bispo", "Cavalo"};
    int ch = JOptionPane.showOptionDialog(
        this,
        "Escolha a peça para promoção:",
        "Promoção",
        JOptionPane.DEFAULT_OPTION,
        JOptionPane.QUESTION_MESSAGE,
        null,
        opts,
        opts[0]
    );
    // Se o usuário cancelar, retorna 'Q' (Rainha) por padrão
    if (ch < 0) ch = 0;
    return switch (ch) {
        case 1 -> 'R';
        case 2 -> 'B';
        case 3 -> 'N';
        default -> 'Q';
    };
    }

    private void updateTurnLabel() {
        String side = game.whiteToMove() ? "Brancas" : "Pretas";
        labelTurn.setText("Turno atual: " + side);
    }

    private void refresh() {
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                boolean light = (r + c) % 2 == 0;
                // Altere estas cores para customizar o tabuleiro
                Color corClaras = new Color(240, 217, 181);   // Bege claro
                Color corEscuras = new Color(181, 136, 99);   // Marrom
                Color base = light ? corClaras : corEscuras;
                JButton b = squares[r][c];
                b.setBackground(base);
                b.setBorder(null);
            }
        }

        if (selected != null) {
            squares[selected.getRow()][selected.getColumn()].setBorder(BORDER_SELECTED);
            for (Position d : legalForSelected) {
                squares[d.getRow()][d.getColumn()].setBorder(BORDER_LEGAL);
            }
        }

        int iconSize = computeSquareIconSize();
        for (int r = 0; r < 8; r++) {
            for (int c = 0; c < 8; c++) {
                Position pos = new Position(r, c);
                Piece p = game.board().get(pos);
                JButton b = squares[r][c];

                b.setIcon(null);
                b.setText("");

                if (p != null) {
                                        char sym = p.getSymbol().charAt(0);
                    ImageIcon icon = ImageUtil.getPieceIcon(p.isWhite(), sym, iconSize);

                    if (icon != null) {
                        b.setIcon(icon);
                    } else {
                        b.setText(toUnicode(p.getSymbol(), p.isWhite()));
                    }

                    b.revalidate();
                    b.repaint();
                }
            }
        }

        String side = game.whiteToMove() ? "Brancas" : "Pretas";
        String chk = game.inCheck(game.whiteToMove()) ? " — Xeque!" : "";
        status.setText("Vez: " + side + chk);
        updateTurnLabel();

        StringBuilder sb = new StringBuilder();
        var hist = game.history();
        for (int i = 0; i < hist.size(); i++) {
            if (i % 2 == 0) sb.append((i / 2) + 1).append(". ");
            sb.append(hist.get(i)).append(" ");
            if (i % 2 == 1) sb.append("\n");
        }
        history.setText(sb.toString());
        history.setCaretPosition(history.getDocument().getLength());
    }

    private String toUnicode(String sym, boolean white) {
        return switch (sym) {
            case "K" -> white ? "\u2654" : "\u265A";
            case "Q" -> white ? "\u2655" : "\u265B";
            case "R" -> white ? "\u2656" : "\u265C";
            case "B" -> white ? "\u2657" : "\u265D";
            case "N" -> white ? "\u2658" : "\u265E";
            case "P" -> white ? "\u2659" : "\u265F";
            default -> "";
        };
    }

    private int computeSquareIconSize() {
        JButton b = squares[0][0];
        int w = Math.max(1, b.getWidth());
        int h = Math.max(1, b.getHeight());
        int side = Math.min(w, h);
        return side <= 1 ? 64 : Math.max(24, side - 6);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(ChessGUI::new);
    }
}