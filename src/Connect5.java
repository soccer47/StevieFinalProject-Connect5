public class Connect5 {
    // Instance variables
    static Board theBoard;
    // Boolean representing if the game is single player
    static boolean isSinglePlayer;
    // Boolean representing whether the game is over
    static boolean gameOver = false;
    // Boolean representing who's turn it is; true = player 1;
    static boolean isTurnP1 = true;

    public Connect5(boolean singlePlayer) {
        theBoard = new Board();
        isSinglePlayer = singlePlayer;
    }

    public static void main(String[] args){
        // creates new game object
        Connect5 game = new Connect5( );
    }


}
