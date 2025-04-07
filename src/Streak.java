public class Streak {
    // Instance variables
    // Length of streak
    static int length;
    // Orientation of streak; 0 = vertical, 1 = (+) diagonal, 2 = horizontal, 3 = (-) diagonal
    static int orientation;
    // Coordinates for first end of string (left most, then down most end)
    static int xOne;
    static int yOne;
    // Coordinates for second end of string (right most, then up most end)
    static int xTwo;
    static int yTwo;

    //

    // Initialize instance variables
    public Streak(int theLength, int theXOne, int theYOne, int theXTwo, int theYTwo) {
        length = theLength;
        xOne = theXOne;
        yOne = theYOne;
        xTwo = theXTwo;
        yTwo = theYTwo;
    }

    // Method to find orientation of streak
    public void getOrientation() {
        // Check to see which case the coordinates match to determine orientation
        if (xOne == xTwo) {
            // Orientation is vertical
            orientation = 0;
        }
        else if (xOne < xTwo) {
            // Orientation is positive diagonal
            orientation = 1;
        }
        else if (yTwo == yTwo) {
            // Orientation is horizontal
            orientation = 2;
        }
        else {
            // Otherwise orientation must be negative diagonal
            orientation = 3;
        }
    }

}
