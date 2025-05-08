import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Connect4Viewer extends JPanel implements MouseListener, KeyListener {
    enum GameState {
        START, PLAYER1_TURN, PLAYER2_TURN, PLAYER1_WIN, PLAYER2_WIN, ENGINE_WIN, DRAW
    }

    private JFrame frame;
    private ImageIcon startingScreen, turnP1, turnP2, winP1, winP2, winEngine, winDraw;
    private GameState state;
    private boolean isSinglePlayer;
    private Connect4 game;
    private final int GRID_SIZE = 7;
    private Point[][] gridPoints = new Point[GRID_SIZE][GRID_SIZE];
    private int cellWidth, cellHeight;
    private AudioPlayer audioPlayer;
    private Color hotPink = new Color(255, 95, 150);
    private static final int DEPTH = 4;

    private boolean isPulsing = false;
    private long pulseStartTime = 0;
    private Timer pulseTimer;

    public Connect4Viewer(boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        this.game = new Connect4(isSinglePlayer);
        this.state = GameState.START;

        loadImages();
        setupFrame();
        calculateGridPoints();
        addMouseListener(this);
        addKeyListener(this);
        setFocusable(true);
        requestFocusInWindow();

        audioPlayer = new AudioPlayer();
        playStartingMusic();
    }

    private void loadImages() {
        startingScreen = new ImageIcon("Resources/startingScreen.png");
        turnP1 = new ImageIcon("Resources/turnP1.png");
        turnP2 = new ImageIcon("Resources/turnP2.png");
        winP1 = new ImageIcon("Resources/winP1.png");
        winP2 = new ImageIcon("Resources/winP2.png");
        winEngine = new ImageIcon("Resources/winEngine.png");
        winDraw = new ImageIcon("Resources/winDraw.png");
    }

    private void setupFrame() {
        frame = new JFrame("Connect 5");
        frame.setSize(1200, 850);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this);
        frame.setVisible(true);
    }

    private void calculateGridPoints() {
        int xStart = 128;
        int yStart = 115;

        cellWidth = 78;
        cellHeight = 84;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int x = xStart + c * cellWidth + cellWidth / 2;
                int y = yStart + r * cellHeight + cellHeight / 2;
                gridPoints[r][c] = new Point(x, y);
            }
        }
    }

    private void playStartingMusic() {
        audioPlayer.playMusic("Resources/startingMusic.wav");
    }

    private void playGameMusic() {
        audioPlayer.playMusic("Resources/gameMusic.wav");
    }

    private void playWinMusic() {
        audioPlayer.playMusic("Resources/winMusic.wav");
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        switch (state) {
            case START -> g.drawImage(startingScreen.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER1_TURN -> g.drawImage(turnP1.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER2_TURN -> g.drawImage(turnP2.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER1_WIN -> g.drawImage(winP1.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER2_WIN -> g.drawImage(winP2.getImage(), 0, 0, getWidth(), getHeight(), null);
            case ENGINE_WIN -> g.drawImage(winEngine.getImage(), 0, 0, getWidth(), getHeight(), null);
            case DRAW -> g.drawImage(winDraw.getImage(), 0, 0, getWidth(), getHeight(), null);
        }

        if (state == GameState.PLAYER1_TURN || state == GameState.PLAYER2_TURN || isPulsing) {
            drawPieces(g);
            drawAllGridSquares(g);
        }
    }


    private void drawAllGridSquares(Graphics g) {
        g.setColor(Color.BLACK);
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Point p = gridPoints[r][c];
                int x = p.x - cellWidth / 2;
                int y = p.y - cellHeight / 2;
                g.drawRect(x, y, cellWidth, cellHeight);
            }
        }
    }

    private void drawPieces(Graphics g) {
        int[][] board = game.board;
        long elapsed = System.currentTimeMillis() - pulseStartTime;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int val = board[r][c];
                if (val != 0) {
                    Point p = gridPoints[r][c];
                    boolean isLast = isPulsing && r == game.lastRow && c == game.lastCol;
                    int drawWidth = cellWidth;
                    int drawHeight = cellHeight;

                    if (isLast) {
                        double pulseFactor = 1.0 + 0.2 * Math.sin((elapsed / 100.0) * Math.PI);
                        drawWidth = (int)(cellWidth * pulseFactor);
                        drawHeight = (int)(cellHeight * pulseFactor);
                    }

                    int x = p.x - drawWidth / 2;
                    int y = p.y - drawHeight / 2;

                    g.setColor(val == 1 ? hotPink : Color.CYAN);
                    g.fillRect(x, y, drawWidth, drawHeight);
                }
            }
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX(), y = e.getY();

        if (state == GameState.START) {
            if (x >= 305 && x <= 570 && y >= 470 && y <= 545) {
                isSinglePlayer = true;
                game = new Connect4(isSinglePlayer);
                calculateGridPoints();
                state = GameState.PLAYER1_TURN;
                playGameMusic();
                repaint();
                return;
            } else if (x >= 635 && x <= 900 && y >= 470 && y <= 545) {
                isSinglePlayer = false;
                game = new Connect4(isSinglePlayer);
                calculateGridPoints();
                state = GameState.PLAYER1_TURN;
                playGameMusic();
                repaint();
                return;
            }
            return;
        }

        if (game == null || game.gameOver || (state != GameState.PLAYER1_TURN && state != GameState.PLAYER2_TURN)) return;

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Point p = gridPoints[r][c];
                int left = p.x - cellWidth / 2;
                int top = p.y - cellHeight / 2;
                Rectangle cellBounds = new Rectangle(left, top, cellWidth, cellHeight);

                if (cellBounds.contains(x, y)) {
                    if (game.board[r][c] == 0) {
                        boolean currentPlayerIsP1 = game.isTurnP1;
                        boolean moveValid = game.takeTurn(r, c);

                        if (moveValid) {
                            if (game.gameOver) {
                                if (isBoardFull() && !hasWinner()) {
                                    state = GameState.DRAW;
                                    playWinMusic();
                                } else {
                                    isPulsing = true;
                                    pulseStartTime = System.currentTimeMillis();
                                    repaint();

                                    pulseTimer = new Timer(50, new ActionListener() {
                                        @Override
                                        public void actionPerformed(ActionEvent e) {
                                            long elapsed = System.currentTimeMillis() - pulseStartTime;
                                            if (elapsed >= 3000) {
                                                isPulsing = false;
                                                pulseTimer.stop();
                                                state = isSinglePlayer ?
                                                        (currentPlayerIsP1 ? GameState.PLAYER1_WIN : GameState.ENGINE_WIN) :
                                                        (currentPlayerIsP1 ? GameState.PLAYER1_WIN : GameState.PLAYER2_WIN);
                                                playWinMusic();
                                            }
                                            repaint();
                                        }
                                    });
                                    pulseTimer.start();
                                }
                            } else {
                                state = game.isTurnP1 ? GameState.PLAYER1_TURN : GameState.PLAYER2_TURN;
                                repaint();

                                if (isSinglePlayer && !game.isTurnP1) {
                                    int[][] boardCopy = deepCopyBoard(game.board);
                                    Move bestMove = game.minimax(boardCopy, DEPTH, true, -1, -1);
                                    if (bestMove == null) bestMove = getNextAvailableSpot();

                                    if (bestMove != null) {
                                        Move finalBestMove = bestMove;
                                        Timer aiMoveTimer = new Timer(500, evt -> {
                                            game.takeTurn(finalBestMove.row, finalBestMove.col);
                                            if (game.gameOver) {
                                                if (isBoardFull() && !hasWinner()) {
                                                    state = GameState.DRAW;
                                                } else {
                                                    isPulsing = true;
                                                    pulseStartTime = System.currentTimeMillis();
                                                    pulseTimer = new Timer(50, new ActionListener() {
                                                        @Override
                                                        public void actionPerformed(ActionEvent e2) {
                                                            long elapsed = System.currentTimeMillis() - pulseStartTime;
                                                            if (elapsed >= 3000) {
                                                                isPulsing = false;
                                                                pulseTimer.stop();
                                                                state = GameState.ENGINE_WIN;
                                                                playWinMusic();
                                                            }
                                                            repaint();
                                                        }
                                                    });
                                                    pulseTimer.start();
                                                }
                                            } else {
                                                state = GameState.PLAYER1_TURN;
                                            }
                                            repaint();
                                        });
                                        aiMoveTimer.setRepeats(false);
                                        aiMoveTimer.start();
                                    }
                                }
                            }
                        }
                    }
                    return;
                }
            }
        }
    }

    private boolean isBoardFull() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (game.board[r][c] == 0) return false;
            }
        }
        return true;
    }

    public Move getNextAvailableSpot() {
        for (int i = 0; i < game.board.length; i++) {
            for (int j = 0; j < GRID_SIZE; j++) {
                if (game.board[i][j] == 0) return new Move(i, j, 0);
            }
        }
        return new Move(0, 0, 0);
    }

    private int[][] deepCopyBoard(int[][] original) {
        if (original == null) return null;
        int[][] copy = new int[original.length][];
        for (int i = 0; i < original.length; i++) {
            copy[i] = java.util.Arrays.copyOf(original[i], original[i].length);
        }
        return copy;
    }

    private boolean hasWinner() {
        return state == GameState.PLAYER1_WIN || state == GameState.PLAYER2_WIN || state == GameState.ENGINE_WIN;
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            game = null;
            state = GameState.START;
            calculateGridPoints();
            playStartingMusic();
            repaint();
        }
    }

    public void mousePressed(MouseEvent e) {}
    public void mouseReleased(MouseEvent e) {}
    public void mouseEntered(MouseEvent e) {}
    public void mouseExited(MouseEvent e) {}
    public void keyReleased(KeyEvent e) {}
    public void keyTyped(KeyEvent e) {}

    public static void main(String[] args) {
        new Connect4Viewer(true);
    }

    static class AudioPlayer {
        private Clip clip;

        public void playMusic(String filePath) {
            try {
                if (clip != null && clip.isRunning()) {
                    clip.stop();
                    clip.close();
                }

                File audioFile = new File(filePath);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(audioFile);
                clip = AudioSystem.getClip();
                clip.open(audioStream);
                clip.start();
                clip.loop(Clip.LOOP_CONTINUOUSLY);
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        }
    }
}
