/*
 * 
 */
package chess;

import java.util.ArrayList;
import java.util.Arrays;

/**
 *  Chess
 *  after instantiating an instance of Chess, start a game by calling
 *  setupGame() and startGame(). 
 * @author Philip Schexnayder
 */
public class Chess 
{
    /* ****************************************
     *  Declare constants
     * ****************************************/
    // Game Colors
    public static final int BLACK =  0;
    public static final int WHITE =  1;
    
    // Piece types
    public static final int KING =   2;
    public static final int QUEEN =  3;
    public static final int BISHOP = 4;
    public static final int KNIGHT = 5;
    public static final int ROOK =   6;
    public static final int PAWN =   7;
    
    // makeMove() and isValidMove() return codes
    public static final int MOVE_LEGAL                  = 100;
    public static final int MOVE_ILLEGAL                = 101;
    public static final int MOVE_ILLEGAL_KING_IN_CHECK  = 102;
    public static final int MOVE_ILLEGAL_IMPEDED        = 103;
        
    // Piece states
    public static final int PIECE_ACTIVE        = 200;
    public static final int PIECE_CAPTURED      = 201;
    public static final int PIECE_PROMOTED      = 202;
    
    
    /* ****************************************
     *  Game State variables
     * ****************************************/
    private boolean mIsGameActive;
    private boolean mIsWhitesTurn;
    private boolean mWhiteOffersDraw;
    private boolean mBlackOffersDraw;
    //private boolean mInEditMode = false;
    //private boolean mIsChess960 = false;
    
    
    /************************************************
     * The Chess Board -  This board using an internal
     * coordinate system, [rank][column]
     * a1 -> [0][0], a 2-> [0][1], b1 -> [1][0], etc.
     * Cells contain reference to the occupying 
     * chess piece, or null if square is empty.
     ***********************************************/
    private ChessPiece[][] mChessBoard = new ChessPiece[8][8];
    
    
    /** ************************************************
     * ArrayList of all chess pieces
     **************************************************/
    private ArrayList<ChessPiece> mChessPieces; 
    
    
    /**************************************************
     * History ArrayList
     *************************************************/
    private ArrayList<RecordOfMove> mChessHistory;
    
    
    /* *************************************************
     * Constructor
     **************************************************/
    public Chess()
    {
        mChessPieces = new ArrayList<>();
        mChessHistory = new ArrayList<>();
        clearGame();
    }
    
    
    /* *************************************************
     *   Public Methods
     * *************************************************/
    
    // returns true if game is currently active
    public boolean isGameActive() { return mIsGameActive; }
    public boolean isWhitesTurn() { return mIsWhitesTurn; }
    
    /**
     * Get the chess board with references to the active pieces on it
     * @return Returns a 2d array of ChessPiece
     */
    public ChessPiece[][] getBoard() 
        { 
            ChessPiece[][] copy = new ChessPiece[8][8];
            for (int i = 0; i < 8; i++)
                copy[i] = mChessBoard[i].clone();
            return copy;
            //return  mChessBoard.clone(); 
        } 
    
    /**
     * Gets all the ChessPiece references in an ArrayList
     * @return ArrayList containing references to the pieces
     */
    public ArrayList<ChessPiece> getPieces()
        { return (ArrayList<ChessPiece>) mChessPieces.clone(); }
    
    //public void restartGame() {}
    
    /**
     * Initializes the game board (mChessBoard) to all null values 
     * Clears the pieces array (mChessPieces)
     * Clears the history stack
     */
    public final void clearGame()
    {
        mIsGameActive = false;
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                mChessBoard[i][j] = null;
        
        if ( mChessPieces != null && !mChessPieces.isEmpty() )
            mChessPieces.clear();
        
        if ( mChessHistory != null && !mChessHistory.isEmpty() )
            mChessHistory.clear();
        
        mWhiteOffersDraw = false;
        mBlackOffersDraw = false;
        mIsGameActive = false;        
    }
    
    /**
     * Sets up the pieces on the board
     * for a standard game
     */
    public void setupGame()
    {
        clearGame();
        mIsWhitesTurn = true;
       
        //TODO: Create pieces and place on board

        addPieceToGame(WHITE, KING, new int[]{0,4} );
        addPieceToGame(BLACK, KING, new int[]{7,4} );
    }
    
    
    /**
     * Starts game to begin accepting moves
     */
    public void startGame()
    {
        mIsGameActive = true;
    }
    
    public int makeMove(String from, String to)
    {
        // convert string coords to internal coords
        // TODO: call makeMove(int[], int[])
        return -1; 
    }
    
    public int makeMove(int[] from, int[] to)
    {
        if ( from.length != 2 || to.length != 2 
                || from[0] < 0 || from[0] > 7
                || to[0] < 0 || to[0] > 7 
                || from[1] < 0 || from[1] > 7
                || to[1] < 0 || to[1] > 7 )
            throw new IllegalArgumentException("Invalid arguements for makeMove");
        
        // TODO: make the move!
        
        return -1; 
    }
    
    
    
    /* *************************************************
     *   Private Methods
     * *************************************************/

    /**
     * Adds a piece to the game (adds in the ArrayList mChessPieces)
     * */
    private ChessPiece addPieceToGame(int color, int type)
    {
        ChessPiece newPiece;
        // TODO: validate parameters
        if ( type == KING )
            newPiece = new King(color);
        else
            return null;

        mChessPieces.add(newPiece);
        return newPiece;
    }
    
    /**
     * Adds a piece to the game (adds in the ArrayList mChessPieces)
     * and places on board
     * */
    private ChessPiece addPieceToGame(int color, int type, int[] pos)
    {
        ChessPiece newPiece = addPieceToGame(color, type);
        if ( newPiece == null )
            return null;
        newPiece.setPosition(pos);
        return newPiece;
    }
    
    
    /* *************************************************
     *   Static Methods
     * *************************************************/
    // conversion method
    public static int[] convertAlgebraicToInternal(String coord)
    {
        int rank =  Character.getNumericValue(coord.charAt(1)) - 1;
        int col = (int)coord.charAt(0) - 97;
        return new int[]{rank,col};
    }
    
    public static String convertInternalToAlgebraic(int[] coord)
    {
        if ( !isValidCoord(coord) )
            throw new IllegalArgumentException("Invalid Coordinate");
        return ((char) (coord[1]+97)) + String.valueOf(coord[0] + 1);
    }
    
    // get a square color
    public static int getSquareColor(String coord)
    {
        // TODO: implement
        return -1; 
    }
    public static int getSquareColor(int[] pos)
    {
        if ( !isValidCoord(pos) )
            throw new IllegalArgumentException("Invalid Coordinate");
        return (pos[0]+pos[1])%2; 
    }
    
    public static char getUnicode(int color, int type)
    {
        if( color == WHITE && type == KING)
            return '\u2654'; // white king char
        else if ( color == BLACK && type == KING )
            return '\u265A'; // black king char
        else if ( color == WHITE && type == QUEEN )
            return '\u2655';
        else if ( color == BLACK && type == QUEEN )
            return '\u265B';
        else if ( color == WHITE && type == BISHOP )
            return '\u2657';
        else if ( color == BLACK && type == BISHOP )
            return '\u265D';
        else if ( color == WHITE && type == KNIGHT )
            return '\u2658';
        else if ( color == BLACK && type == KNIGHT )
            return '\u265E';
        else if ( color == WHITE && type == ROOK )
            return '\u2656';
        else if ( color == BLACK && type == ROOK )
            return '\u265C';
        else if ( color == WHITE && type == PAWN )
            return '\u2659';
        else if ( color == BLACK && type == PAWN )
            return '\u265F';
        return '?';
    }
    
    private static boolean isValidCoord(int[] coord)
    {
        if ( coord.length != 2 
                || coord[0] < 0 || coord[0] > 7
                || coord[1] < 0 || coord[1] > 7 )
            return false;
        return true;
    }
    
    
    
    /********************************************
     * Abstract Chess Piece Class
     * Contains common methods
     * *******************************************/
    public abstract class ChessPiece
    {
        protected int[] mPosition = new int[2];
        protected String mPositionString = "";
        protected int mType;
        protected int mColor;
        
        
        private void setPosition(String pos)
        {
            // TODO: set internal position
            mPositionString = pos;
        }
        
        private void setPosition(int[] pos)
        {
            if (pos.length != 2)
                throw new IllegalArgumentException("Illegal arguement for setPosition");
            // TODO: maybe some more arguement validation? private so might not be needed
            mPosition = pos;
            mChessBoard[pos[0]][pos[1]] = this;
        }
        
        // public get methods
        public String getPosition()
            { return mPositionString; } // TODO: make sure this is safe
        public int[] getPositionInternal() 
            { return Arrays.copyOf(mPosition, mPosition.length); }
        public int getType() { return mType; }
        public int getColor() { return mColor; }
        public char getUnicode()
            { return Chess.getUnicode(mColor, mType); }
    }
    
    /**
     * King class
     */
    private class King extends ChessPiece
    {
        private King(int color)
        {
            mColor = color;
            mType = KING;
        }
    }
    

    /** *****************************************************
     * ChessMove object for keeping track of game history
     *********************************************************/
    private class RecordOfMove
    {
        // TODO: imlement this class
    }
}
