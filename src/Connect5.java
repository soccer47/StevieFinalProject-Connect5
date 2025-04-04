public class Game {
    // Instance variables
    static Board theBoard;
    // Boolean representing if the game is single player
    static boolean isSinglePlayer;

    public Game(boolean singlePlayer) {
        theBoard = new Board();
        isSinglePlayer = singlePlayer;
    }

}
