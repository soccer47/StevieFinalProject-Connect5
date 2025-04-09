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
        // Switch the turn to the other player
        isTurnP1 = !isTurnP1;
        // Return true to show the move and updating of the board was successful
        return true;
    }

    // Return the player who has won the game, or 0 if neither has won yet
    public boolean gameWinner(boolean isTurnP1) {

        // Get the integer to check for a streak of (1 = player 1, 2 = player 2)
        int validNum;
        if (isTurnP1) {validNum = 1;}
        else {validNum = 2;}

        // Check to see if the previous move resulted in a winning streak
        // Start by looking diagonally down and left from current spot, then proceed clockwise
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                // If the last move resulted in a streak of 5 in the given direction, return true
                if (isStreak(lastRow + i, lastCol + j, i, j, 1, validNum)) {
                    return true;
                }
            }
        }
        // If the last move didn't result in a winning streak, return false
        return false;
    }

    // Helper method for gameWinner
    // Return true if the inputted row of five spaces on the board results all contain the same number
    // Takes in the starting row and col, as well as direction of traversal
    // Continues until streak of five has been achieved, or until another integer has been hit
    public boolean isStreak(int row, int col, int rowIncrement, int colIncrement, int spacesVisited, int validNum) {
        // Base case
        if (spacesVisited == WIN_LENGTH) {
            return true;
        }

        // Make sure the specified spot on the board exists
        if (row < 0 || row >= BOARD_SIZE || col < 0 || col >= BOARD_SIZE) {
            return false;
        }

        // Return false if a different integer has been reached
        if (board[row][col] != validNum) {
            return false;
        }

        // Recurse to the next space
        return isStreak(row + rowIncrement, col + colIncrement, rowIncrement, colIncrement,
                spacesVisited + 1, validNum);
    }

}
