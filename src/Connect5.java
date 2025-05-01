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
            return false;
        }
        // Make sure the specified spot on the board is available
        if (board[row][col] != 0) {
            return false;
        }
        // Otherwise update the spot on the board with a new integer representing the player who put down the piece
        if (isTurnP1) {board[row][col] = 1;}
        else {board[row][col] = 2;}
        // Update the last row and col variables
        lastRow = row;
        lastCol = col;

        // Check to see if the game was won
        if (gameWinner(isTurnP1)) {
            gameOver = true;
            return true;
        }

        // Switch turn only if no win
        isTurnP1 = !isTurnP1;
        return true;
    }

    // Return true if the player who just moved has won the game, or false if not
    public boolean gameWinner(boolean isTurnP1) {
        // Get the integer to check for a streak of (1 = player 1, 2 = player 2)
        int validNum;
        if (isTurnP1) {
            validNum = 1;
        }
        else {
            validNum = 2;
        }

        // Check to see if the previous move resulted in a winning streak
        // Start by looking diagonally down and left from current spot, then proceed clockwise
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // Skip if direction is (0,0) because that’s not a direction
                if (i == 0 && j == 0) {continue;}
                // Skip redundant direction checks
                if (i > 0 || (i == 0 && j > 0)) {continue;}

                // Count streaks in both forward and backward directions from the last move
                int forward = countStreak(board, lastRow + i, lastCol + j, i, j, validNum);
                int backward = countStreak(board, lastRow - i, lastCol - j, -i, -j, validNum);

                // Add 1 to forward and backward streaks to account for last piece placed
                if (forward + backward + 1 >= WIN_LENGTH) {
                    return true;
                }
            }
        }
        return false;
    }

    // Helper method for gameWinner
    // Return the number of consecutive validNum pieces in the given direction from (row, col)
    // Takes in the starting row and col, as well as direction of traversal
    // Continues until streak of same number has been broken, or an index is out of bounds
    public int countStreak(int[][] theBoard, int row, int col, int rowIncrement, int colIncrement, int validNum) {
        int count = 0;
        // While the next index matches the validNum, increment count, row, and col accordingly
        while (row >= 0 && row < BOARD_SIZE && col >= 0 && col < BOARD_SIZE && theBoard[row][col] == validNum) {
            count++;
            row += rowIncrement;
            col += colIncrement;
        }
        return count;
    }

    // Engine method
    // Return coordinates of the best available move for given game scenario, for the given player
    // Recursively return the move with the highest guaranteed score up to the given depth
    public Move minimax(int[][] board, int depth, boolean isMaxing, int OGRow, int OGCol) {
        // Base Case
        // Find out if the game has been won or lost
        int gameState = evaluate(board);
        // If this version of the game is over, or depth limit reached, return the score associated with this outcome
        // along with the coordinates of the first move
        if (gameState == 100 || gameState == -100 || depth == 0) {
            return new Move(OGRow, OGCol, gameState);
        }

        // Initialize best move depending on whether maximizing or minimizing
        Move bestMove;
        if (isMaxing) {
            bestMove = new Move(-1, -1, Integer.MIN_VALUE);
        }
        else {
            bestMove = new Move(-1, -1, Integer.MAX_VALUE);
        }

        // Loop through all possible valid moves
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Only consider empty spots on the board (0 means empty)
                if (board[i][j] == 0) {
                    // Simulate making this move
                    // Maxing player uses 2, minimizing player uses 1
                    if (isMaxing) {
                        board[i][j] = 2;
                    }
                    else {
                        board[i][j] = 1;
                    }

                    // If the first row and col haven't been initialized yet, make the original coordinates this index
                    // Set the row and column for the first move in the sequence
                    int moveRow;
                    int moveCol;
                    // Set moveRow and moveCol to the current index if this is the first move being taken in the tree
                    if (OGRow == -1) {
                        moveRow = i;
                        moveCol = j;
                    }
                    // Otherwise set them to the coordinates of the original move
                    else {
                        moveRow = OGRow;
                        moveCol = OGCol;
                    }

                    // Recurse, switch to the other player's turn
                    Move currentMove = minimax(board, depth - 1, !isMaxing, moveRow, moveCol);

                    // Pick the first move if bestMove hasn't been changed yet
                    if (currentMove == null) {
                        continue;
                    }
                    // Choose the best move based on current player
                    if (isMaxing && currentMove.score > bestMove.score) {
                        // Maximizing player's best move
                        bestMove = currentMove;
                    } else if (!isMaxing && currentMove.score < bestMove.score) {
                        // Minimizing player's best move
                        bestMove = currentMove;
                    }

                    // Reset the index on the board to its original state for the next iteration
                    board[i][j] = 0;
                }
            }
        }

        // Return the best move found
        return bestMove;
    }


    // Helper method for minimax
    // Return state of game (-100 = P1 wins, 100 = P2 wins)
    public int evaluate(int[][] board) {
        // Iterate through every cell on the board, checking for winning streaks
        for (int i = 0; i < BOARD_SIZE; i++) {
            for (int j = 0; j < BOARD_SIZE; j++) {
                // Get the value at the current index
                int index = board[i][j];
                // Skip the current index if it's empty
                if (index == 0) {continue;}

                // Check all four directions for a winning streak
                if (
                        // Horizontal
                        countStreak(board, i, j, 0, 1, index) >= WIN_LENGTH ||
                                // Vertical
                                countStreak(board, i, j, 1, 0, index) >= WIN_LENGTH ||
                                // Down-right diagonal
                                countStreak(board, i, j, 1, 1, index) >= WIN_LENGTH ||
                                // Up-right diagonal
                                countStreak(board, i, j, -1, 1, index) >= WIN_LENGTH
                ) {
                    // If the streak is of 1s, return -100 to show that the human has won
                    if (index == 1) {
                        return -100;
                    }
                    // Otherwise return 100 to show that the computer has won
                    else {
                        return 100;
                    }
                }
            }
        }

        // If no winning streak is found, iterate through the board again, checking for shorter streaks
        int subScore = 0;
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                // Get the value at the current index
                int index = board[i][j];
                // Skip the current index if it's empty
                if (index == 0) {continue;}

                // Check all four directions for a streak of 1 less than a winning streak
                if (
                    // Horizontal
                        countStreak(board, i, j, 0, 1, index) == WIN_LENGTH - 1 ||
                                // Vertical
                                countStreak(board, i, j, 1, 0, index) == WIN_LENGTH - 1 ||
                                // Down-right diagonal
                                countStreak(board, i, j, 1, 1, index) == WIN_LENGTH - 1 ||
                                // Up-right diagonal
                                countStreak(board, i, j, -1, 1, index) == WIN_LENGTH - 1
                ) {
                    // If the streak is of 1s, subtract 5 from subScore
                    if (index == 1) {
                        subScore -= 1;
                    }
                    // Otherwise add 5 to subScore
                    else {
                        subScore += 1;
                    }
                }
            }
        }

        // If there's no winner, return the subScore
        return subScore;
    }
}
