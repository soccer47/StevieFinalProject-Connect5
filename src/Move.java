public class Move {
    // Instance variables
    // Row and column of move
    public int row;
    public int col;
    // Score associated with move (for minimax algorithm)
    public int score;

    // Initialize instance variables
    public Move(int row, int col, int score) {
        this.row = row;
        this.col = col;
        this.score = score;
    }
}