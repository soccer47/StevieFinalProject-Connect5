import java.awt.*;
import java.util.HashMap;

public class Connect5 {
    // Instance variables
    // 2D array representing game board
    public int[][] board;
    // Boolean representing if the game is single player
    public boolean isSinglePlayer;
    // Boolean representing who's turn it is; true = player 1;
    public boolean isTurnP1 = true;
    // Length of streak required to win
    public final int WIN_LENGTH = 3;
    // Boolean representing whether the game is over
    public boolean gameOver = false;
    // Number of rows and columns of available spaces on game board
    public final int BOARD_SIZE = 5;
    // Integer representing the row of the last piece placed
    static int lastRow;
    // Integer representing the column of the last piece placed
    static int lastCol;


    public Connect5(boolean singlePlayer) {
        board = new int[BOARD_SIZE][BOARD_SIZE];
        isSinglePlayer = singlePlayer;
    }

    // Adds a piece of the current to the board in the specified spot
    // Returns false if the spot is already occupied
    public boolean takeTurn(int row, int col) {
        // Make sure the specified spot on the board exists
        if (row < 0 || row > BOARD_SIZE - 1 || col < 0 || col > BOARD_SIZE - 1) {
            // Return false and prompt user to enter another spot if current spot doesn't exist on the board
            return false;
        }
        // Make sure the specified spot on the board is available
        if (board[row][col] != 0) {
            // Return false and prompt user to enter another spot if current spot is occupied already
            return false;
        }
        // Otherwise update the spot on the board with a new integer representing the player who put down the piece
        if (isTurnP1) {board[row][col] = 1;}
        else {board[row][col] = 2;}
        // Update the last row and col variables
        lastRow = row;
        lastCol = col;
        // Check to see if the game has been won
        if (gameWinner(isTurnP1)) {
            // If so, set gameOver to true
            gameOver = true;
            // Then return true to get to exit and get to the gameOver screen
            return true;
        }
        // Otherwise continue with the game
        else {
            // Switch the turn to the other player
            isTurnP1 = !isTurnP1;
        }
        // Return true to show the move and updating of the board was successful
        return true;
    }

    // Return true if the player who just moved has won the game, or false if not
    public boolean gameWinner(boolean isTurnP1) {

        // Get the integer to check for a streak of (1 = player 1, 2 = player 2)
        int validNum;
        if (isTurnP1) {validNum = 1;}
        else {validNum = 2;}

        // Check to see if the previous move resulted in a winning streak
        // Start by looking diagonally down and left from current spot, then proceed clockwise
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // Skip if direction is (0,0) because that’s not a direction
                if (i == 0 && j == 0) {
                    continue;
                }
                // Skip redundant direction checks
                if (i > 0 || (i == 0 && j > 0)) {
                    continue;
                }

                // Count streaks in both forward and backward directions from the last move
                int forward = countStreak(lastRow + i, lastCol + j, i, j, validNum);
                int backward = countStreak(lastRow - i, lastCol - j, -i, -j, validNum);

                // Add 1 to forward and backward streaks to account for last piece placed
                if (forward + backward + 1 >= WIN_LENGTH) {
                    return true;
                }
            }
        }
        // If the last move didn't result in a winning streak, return false
        return false;
    }

    // Helper method for gameWinner
    // Return the number of consecutive validNum pieces in the given direction from (row, col)
    // Takes in the starting row and col, as well as direction of traversal
    // Continues until streak of same number has been broken, or an index is out of bounds
    public int countStreak(int row, int col, int rowIncrement, int colIncrement, int validNum) {
        // The number of current player's pieces in a row (excluding last piece placed)
        int count = 0;
        // Continue while index being checked is in bounds and
        while (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && board[row][col] == validNum) {
            // If the index is valid increment count, as well as the row and col being checked
            count++;
            row += rowIncrement;
            col += colIncrement;
        }
        // Return the number of current player's pieces in a row
        return count;
    }



    // Engine method
    // Return coordinates of the best available move for given game scenario, for the given player
    public Point getBestMove(int playerNum) {
        // HashMap to contain coordinates (keys) and their scores (values) of possible moves
        HashMap<Point, Integer> posMoves = new HashMap<>();

        // Iterate through every available square on the board
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Continue to the next index if the current index is unavailable
                if (board[i][j] != 0) {
                    continue;
                }
                // Get the move score of the current placement
                int givenMove = getMoveScore(i, j, playerNum);
                // If the move results in an immediate win, return those coordinates so the move can be made immediately
                if (givenMove == 2) {
                    return new Point(i, j);
                }
                // Otherwise get the score of the move and add it to the HashMap with the given coordinates
                posMoves.put(new Point(i, j), givenMove);
            }
        }

        // Integer to hold coordinates of highest scored move
        Point bestMove = new Point(-1, -1);

        // Iterate through the HashMap of possible moves to find the one with the highest score
        for (Point placement : posMoves.keySet()) {
            // If bestMove hasn't been altered yet, set it to the first key in the HashMap
            if (bestMove.x == -1) {
                bestMove = placement;
            }
            // Otherwise update bestMove if the current possible move is associated with a higher score
            else if (posMoves.get(placement) >= posMoves.get(bestMove)) {
                bestMove = placement;
            }
        }

        // Return the best available move for the given player
        return bestMove;
    }

    // Helper method for getBestMove()
    // Return the score (0-2) of a given piece placement
    public int getMoveScore(int row, int col, int playerNum) {
        // Create a game state where the piece is placed (set index to playerNum to show that the given player has
        // placed the piece)
        board[row][col] = playerNum;

        // Temporarily store the original position for lastRow and lastCol
        int holdLastRow = lastRow;
        int holdLastCol = lastCol;

        // Update lastRow and lastCol for the current move
        lastRow = row;
        lastCol = col;

        // If the move results in an immediate win, return 2
        if (gameWinner(playerNum == 1)) {
            // Reset lastRow and lastCol to their original values
            lastRow = holdLastRow;
            lastCol = holdLastCol;
            // Reset the index to its original state
            board[row][col] = 0;
            return 2;
        }

        // Otherwise reset lastRow and lastCol to their original values
        lastRow = holdLastRow;
        lastCol = holdLastCol;

        // Get the opponent's player number
        int opponentNum = (playerNum == 1) ? 2 : 1;

        // Now check if the opponent can get an immediate win after the simulated move
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Continue to the next index if the current index is unavailable
                if (board[i][j] != 0) {
                    continue;
                }

                // Temporarily place a piece of the opponent to simulate that move
                board[i][j] = opponentNum;

                // Use local variables to simulate the opponent's move
                int tempRow = lastRow;
                int tempCol = lastCol;
                lastRow = i;
                lastCol = j;

                // If there is a move that can result in an immediate win for the opponent, return 0
                if (gameWinner(opponentNum == 1)) {
                    // Reset lastRow and lastCol to their original values
                    lastRow = tempRow;
                    lastCol = tempCol;
                    // Reset the index to its original state
                    board[i][j] = 0;
                    // Reset the original move
                    board[row][col] = 0;
                    return 0;
                }

                // Otherwise just reset lastRow and lastCol to their original values
                lastRow = tempRow;
                lastCol = tempCol;
                // Also return the index to its original state
                board[i][j] = 0;
            }
        }

        // Reset the original index to its original state
        board[row][col] = 0;
        // Return 1 to show that the move doesn't result in an immediate win, nor does it give the opponent a chance
        // for an immediate win
        return 1;
    }


}
