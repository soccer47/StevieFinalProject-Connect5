import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import javax.sound.sampled.*;
import java.io.File;
import java.io.IOException;

public class Connect5Viewer extends JPanel implements MouseListener, KeyListener {
    enum GameState {
        START, PLAYER1_TURN, PLAYER2_TURN, PLAYER1_WIN, PLAYER2_WIN, ENGINE_WIN, DRAW
    }

    private JFrame frame;
    private ImageIcon startingScreen, turnP1, turnP2, winP1, winP2, winEngine, winDraw;
    private GameState state;
    private boolean isSinglePlayer;
    private Connect5 game; // Ensure this is initialized
    private final int GRID_SIZE = 5;
    private final int CIRCLE_RADIUS = 30;
    private Point[][] gridPoints = new Point[GRID_SIZE][GRID_SIZE];
    private AudioPlayer audioPlayer;
    private static final int DEPTH = 5;

    // Pulse effect fields for animating the last move
    private boolean isPulsing = false;
    private long pulseStartTime = 0;
    private Timer pulseTimer;

    // Constructor to initialize the game
    public Connect5Viewer(boolean isSinglePlayer) {
        this.isSinglePlayer = isSinglePlayer;
        this.game = new Connect5(isSinglePlayer);  // Ensure game is initialized here
        this.state = GameState.START; // Game starts at the "Start" state

        loadImages(); // Load image assets
        setupFrame(); // Set up JFrame
        calculateGridPoints(); // Calculate grid layout
        addMouseListener(this); // Add mouse listener for interaction
        addKeyListener(this); // Add key listener for reset functionality
        setFocusable(true);
        requestFocusInWindow();

        audioPlayer = new AudioPlayer(); // Initialize audio player
        playStartingMusic(); // Play the starting music
    }

    // Method to load images for different game states
    private void loadImages() {
        startingScreen = new ImageIcon("Resources/startingScreen.png");
        turnP1 = new ImageIcon("Resources/turnP1.png");
        turnP2 = new ImageIcon("Resources/turnP2.png");
        winP1 = new ImageIcon("Resources/winP1.png");
        winP2 = new ImageIcon("Resources/winP2.png");
        winEngine = new ImageIcon("Resources/winEngine.png");
        winDraw = new ImageIcon("Resources/winDraw.png");
    }

    // Setup the main game frame
    private void setupFrame() {
        frame = new JFrame("Connect 5");
        frame.setSize(1200, 850);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(this); // Add the game panel
        frame.setVisible(true); // Make frame visible
    }

    // Calculate grid points for the 5x5 grid
    private void calculateGridPoints() {
        int xStart = 200;
        int xEnd = 580;
        int yStart = 190;
        int yEnd = 615;

        int xStep = (xEnd - xStart) / (GRID_SIZE - 1);
        int yStep = (yEnd - yStart) / (GRID_SIZE - 1);

        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int x = xStart + c * xStep;
                int y = yStart + r * yStep;
                gridPoints[r][c] = new Point(x, y); // Store grid points
            }
        }
    }

    // Play starting music
    private void playStartingMusic() {
        audioPlayer.playMusic("Resources/startingMusic.wav");
    }

    // Play game music during gameplay
    private void playGameMusic() {
        audioPlayer.playMusic("Resources/gameMusic.wav");
    }

    // Play win music when the game ends
    private void playWinMusic() {
        audioPlayer.playMusic("Resources/winMusic.wav");
    }

    // Method to draw game components on the screen
    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw the appropriate screen based on the game state
        switch (state) {
            case START -> g.drawImage(startingScreen.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER1_TURN -> g.drawImage(turnP1.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER2_TURN -> g.drawImage(turnP2.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER1_WIN -> g.drawImage(winP1.getImage(), 0, 0, getWidth(), getHeight(), null);
            case PLAYER2_WIN -> g.drawImage(winP2.getImage(), 0, 0, getWidth(), getHeight(), null);
            case ENGINE_WIN -> g.drawImage(winEngine.getImage(), 0, 0, getWidth(), getHeight(), null);
            case DRAW -> g.drawImage(winDraw.getImage(), 0, 0, getWidth(), getHeight(), null);
        }

        // Only draw the grid and pieces when in the game state or pulsing effect
        if (state == GameState.PLAYER1_TURN || state == GameState.PLAYER2_TURN || isPulsing) {
            drawAllGridCircles(g);  // Draw grid circles
            drawPieces(g);           // Draw the player pieces on the grid
        }
    }

    // Draw all the grid circles on the board
    private void drawAllGridCircles(Graphics g) {
        g.setColor(Color.GRAY); // Set the grid color
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Point p = gridPoints[r][c];
                g.drawOval(p.x - CIRCLE_RADIUS, p.y - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2); // Draw circle at grid point
            }
        }
    }

    // Draw the player pieces (P1 and P2) on the grid
    private void drawPieces(Graphics g) {
        int[][] board = game.board; // Get the current board state
        long elapsed = System.currentTimeMillis() - pulseStartTime; // Track pulse animation

        // Iterate through each grid point and draw a piece if it's occupied
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                int val = board[r][c];
                if (val != 0) {
                    Point p = gridPoints[r][c];
                    boolean isLastPiece = isPulsing && r == game.lastRow && c == game.lastCol;

                    // Apply pulsing effect for the last placed piece
                    if (isLastPiece) {
                        double pulseFactor = 1.0 + 0.2 * Math.sin((elapsed / 100.0) * Math.PI);
                        int radius = (int)(CIRCLE_RADIUS * pulseFactor);
                        g.setColor(val == 1 ? Color.PINK : Color.CYAN);
                        g.fillOval(p.x - radius, p.y - radius, radius * 2, radius * 2);
                    } else {
                        g.setColor(val == 1 ? Color.PINK : Color.CYAN);
                        g.fillOval(p.x - CIRCLE_RADIUS, p.y - CIRCLE_RADIUS, CIRCLE_RADIUS * 2, CIRCLE_RADIUS * 2);
                    }
                }
            }
        }
    }

    // Handle mouse click events to process player moves
    @Override
    public void mouseClicked(MouseEvent e) {
        int x = e.getX();
        int y = e.getY();

        // Handle start screen interactions
        if (state == GameState.START) {
            if (x >= 330 && x <= 577 && y >= 465 && y <= 540) {
                isSinglePlayer = true;
                game = new Connect5(isSinglePlayer);  // Initialize the game for single-player
                calculateGridPoints();
                state = GameState.PLAYER1_TURN;
                playGameMusic();
                repaint();
                return;
            } else if (x >= 630 && x <= 880 && y >= 465 && y <= 540) {
                isSinglePlayer = false;
                game = new Connect5(isSinglePlayer);  // Initialize the game for multi-player
                calculateGridPoints();
                state = GameState.PLAYER1_TURN;
                playGameMusic();
                repaint();
                return;
            }
            return;
        }

        // Ensure game is not null and only process valid moves
        if (game == null || game.gameOver || (state != GameState.PLAYER1_TURN && state != GameState.PLAYER2_TURN)) return;

        // Check if click was inside a grid circle and process the move
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                Point p = gridPoints[r][c];
                if (Math.hypot(x - p.x, y - p.y) <= CIRCLE_RADIUS) {
                    if (game.board[r][c] == 0) {
                        boolean currentPlayerIsP1 = game.isTurnP1;
                        boolean moveValid = game.takeTurn(r, c);

                        if (moveValid) {
                            // Check if the game is over and handle win/draw
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
                                                if (isSinglePlayer) {
                                                    state = currentPlayerIsP1 ? GameState.PLAYER1_WIN : GameState.ENGINE_WIN;
                                                } else {
                                                    state = currentPlayerIsP1 ? GameState.PLAYER1_WIN : GameState.PLAYER2_WIN;
                                                }
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
                                    Move bestMove = game.minimax(boardCopy, DEPTH, true, -1, -1); // AI move
                                    if (bestMove != null) {
                                        Timer aiMoveTimer = new Timer(500, evt -> {
                                            game.takeTurn(bestMove.row, bestMove.col);
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

    // Helper methods to check if the board is full and if there's a winner
    private boolean isBoardFull() {
        for (int r = 0; r < GRID_SIZE; r++) {
            for (int c = 0; c < GRID_SIZE; c++) {
                if (game.board[r][c] == 0) return false;
            }
        }
        return true;
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
            game = null; // Reset the game
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
        new Connect5Viewer(true);
    }

    // Audio player class to handle playing music
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
