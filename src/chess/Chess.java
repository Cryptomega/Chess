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
     * coordinate system, [row][col]
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
        // call makeMove(int[], int[])
        return -1; 
    }
    
    public int makeMove(int[] from, int[] to)
    {
        if ( from.length != 2 || to.length != 2 
                || from[0] < 0 || from[0] > 7
                || to[0] < 0 || to[0] > 7 
                || from[1] < 0 || from[1] > 7
                || to[1] < 0 || to[1] > 7 )
            throw new IllegalArgumentException("Invalid arguements for makeMove(int[], int[])");
        
        // make the move!
        
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
        // TODO: implement this method
        return new int[]{-1,-1};
    }
    
     public static String convertInternalToAlgebraic(int[] pos)
    {
        // TODO: implement this method
        return "";
    }
    
    // get a square color
    public static int getSquareColor(String coord)
    {
        // TODO: implement
        return -1; 
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
        
        public String getPosition() { return mPositionString; } // TODO: make sure this is safe
        public int[] getPositionInternal() 
            { return Arrays.copyOf(mPosition, mPosition.length); }
        
        public int getType() { return mType; }
        public int getColor() { return mColor; }
        
        public abstract char getUnicode();
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

        @Override
        public char getUnicode()
        {
            if( mColor == WHITE )
                return '\u2654'; // white king char
            else
                return '\u265A'; // black king char
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
