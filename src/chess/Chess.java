/*
 * 
 */
package chess;

import java.util.ArrayList;

/**
 *  Chess
 *  after instantiating an instance of Chess, start a game by calling
 *  setupGame() and startGame(). 
 * @author Philip Schexnayder
 */
public class Chess 
{
    /* ****************************************
     * * * * Declare constants * * *
     * ****************************************/
    // Game Colors
    public static final int BLACK =  0;
    public static final int WHITE =  1;
    
    // Piece types
    public static final char KING =   'K';
    public static final char QUEEN =  'Q';
    public static final char BISHOP = 'B';
    public static final char KNIGHT = 'N';
    public static final char ROOK =   'R';
    public static final char PAWN =   'P';
    
    // makeMove() and isValidMove() return codes
    public static final int MOVE_LEGAL                        = 100;
    public static final int MOVE_ILLEGAL                      = 101;
    public static final int MOVE_ILLEGAL_KING_IN_CHECK        = 102;
    public static final int MOVE_ILLEGAL_IMPEDED              = 103;
    public static final int MOVE_ILLEGAL_SQUARE_EMPTY         = 104;
    public static final int MOVE_ILLEGAL_WRONG_PLAYER         = 105;
    public static final int MOVE_ILLEGAL_CASTLE_THROUGH_CHECK = 106;
    public static final int AMBIGUOUS_PROMOTION               = 120;
    public static final int GAME_NOT_ACTIVE                   = 121;
    public static final int PIECE_NOT_ACTIVE                  = 122;
    
        
    // Piece states
    public static final int PIECE_ACTIVE        = 200;
    public static final int PIECE_CAPTURED      = 201;
    public static final int PIECE_PROMOTED      = 202;
    public static final int PIECE_NOT_PLACED    = 203;
    
    
    /* ****************************************
     * * * Game State variables * * *
     * ****************************************/
    private boolean mIsGameActive;
    private int mWhoseTurn;
    private int mGameCounter;
    private boolean mWhiteOfferingDraw;
    private boolean mBlackOfferingDraw;
    //private boolean mInEditMode = false;
    //private boolean mInAnalysisMode = false;
    //private boolean mIsChess960 = false;
    
    
    /************************************************
     * The Chess Board -  This board using an internal
     * coordinate system, [rank][file]
     * a1 -> [0][0], a2 -> [0][1], b1 -> [1][0], etc.
     * Cells contain reference to the occupying 
     * chess piece, or null if the square is empty.
     ***********************************************/
    private final ChessPiece[][] mChessBoard = new ChessPiece[8][8];
    
    
    /** ************************************************
     * * * * ArrayList of all chess pieces * * * 
     **************************************************/
    private final ArrayList<ChessPiece> mChessPieces; 
    
    
    /**************************************************
     * * * * History ArrayList * * * 
     *************************************************/
    private final ArrayList<RecordOfMove> mChessHistory;
    
    
    /* *************************************************
     * * * * Constructor * * * 
     **************************************************/
    public Chess()
    {
        mChessPieces = new ArrayList<>();
        mChessHistory = new ArrayList<>();
        clearGame();
    }
    
    
    /* *************************************************
     * * * * Public Methods * * * 
     * *************************************************/
    
    // true if game is active. Use startGame() to activate
    public boolean isGameActive() { return mIsGameActive; }
    public int whoseTurn() { return mWhoseTurn; }
    
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
        // reset game variables
        mIsGameActive = false;
        mWhiteOfferingDraw = false;
        mBlackOfferingDraw = false;
        mIsGameActive = false;  
        mGameCounter = 0;
        mWhoseTurn = WHITE;
        
        // clear game board
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
                mChessBoard[i][j] = null;
        
        // clear pieces array
        if ( mChessPieces != null && !mChessPieces.isEmpty() )
            mChessPieces.clear();
        
        // clear history
        if ( mChessHistory != null && !mChessHistory.isEmpty() )
            mChessHistory.clear();
    }
    
    /**
     * Sets up the pieces on the board for a 
     * standard game. Call startGame() to begin!
     */
    public void setupGame()
    {
        clearGame();
       
        //TODO: Create pieces and place on board

        addPieceToGame(WHITE, KING, 0, 4 );
        addPieceToGame(BLACK, KING, 7, 4 );
    }
    
    
    /**
     * Starts game to begin accepting moves
     */
    public void startGame()
    {
        mIsGameActive = true;
    }
    
    
    
    /**
     * Makes a move on the board using algebraic coordinates.
     * @param move Ex: "a1-b2", "a1 a2", "E4xE5", "a7-a8=Q"
     * the third character can be any character, and extraneous
     * characters are ignored
     * @return returns MOVE_LEGAL (100) if the move is accepted, 
     *         otherwise returns error code
     */
    public int makeMove(String move)
    {
        if ( move.length() < 5 )
            throw new IllegalArgumentException("Invalid input string");
        String from = move.substring(0, 2);
        String to = move.substring(3, 5);
        int fromRank = Chess.convertAlgebraicToInternalRank(from);
        int fromFile = Chess.convertAlgebraicToInternalFile(from);
        int toRank = Chess.convertAlgebraicToInternalRank(to);
        int toFile = Chess.convertAlgebraicToInternalFile(to);
        
        if ( move.length() >= 7)
        {
            char promoType = move.toUpperCase().charAt(6);
            if ( promoType == QUEEN || promoType == ROOK
                    || promoType == BISHOP || promoType == KNIGHT )
                return makeMove(fromRank, fromFile, toRank, toFile, promoType);
        }
        return makeMove(fromRank, fromFile, toRank, toFile);        
    }
    
    
    /**
     * Makes a move using internal coordinates
     * @param fromRank value from 0-7
     * @param fromFile value from 0-7
     * @param toRank value from 0-7
     * @param toFile value from 0-7
     * @return returns MOVE_LEGAL (100) if the move is accepted, 
     *         otherwise returns error code
     */
    public int makeMove(int fromRank, int fromFile, int toRank, int toFile)
    {
        if ( !isValidCoord(fromRank, fromFile) || !isValidCoord(toRank, toFile) )
            throw new IllegalArgumentException("Invalid arguements for makeMove");
        // check if piece exist at "from" coord
        if ( mChessBoard[fromRank][fromFile] == null )
            return MOVE_ILLEGAL_SQUARE_EMPTY;
        else
            return mChessBoard[fromRank][fromFile].makeMove(toRank, toFile);

    }
    
    
    /**
     * Makes a move using internal coordinates, providing promotion type if needed
     * @param fromRank value from 0-7
     * @param fromFile value from 0-7
     * @param toRank value from 0-7
     * @param toFile value from 0-7
     * @param promotionType The piece to promote to. QUEEN, BISHOP, KNIGHT, ROOK
     * @return returns MOVE_LEGAL (100) if the move is accepted, 
     *         otherwise returns error code
     */
    public int makeMove(int fromRank, int fromFile, int toRank, int toFile, char promotionType)
    {
        if ( !isValidCoord(fromRank, fromFile) || !isValidCoord(toRank, toFile) )
            throw new IllegalArgumentException("Invalid arguements for makeMove");
        // call makeMove(rank,file,promotionType) on the chess piece!
        if ( mChessBoard[fromRank][fromFile] == null )
            return MOVE_ILLEGAL_SQUARE_EMPTY;
        else
            return mChessBoard[fromRank][fromFile].makeMove(toRank, toFile, promotionType);
    }
    
    
    // TODO: add validateMove(int,int,int,int) and/or (string) maybe?
    
    
    /* *************************************************
     * * * * Private Methods * * * 
     * *************************************************/

    /**
     * Adds a piece to the game (adds in the ArrayList mChessPieces)
     * */
    private ChessPiece addPieceToGame(int color, char type)
    {
        if ( color != WHITE && color != BLACK )
            throw new IllegalArgumentException("Invalid color argument");
        ChessPiece newPiece;
        switch (type) 
        {
            case KING:
                newPiece = new King(color);
                break;
            case QUEEN:
                throw new UnsupportedOperationException("Queen not yet implemented.");
            case BISHOP:
                throw new UnsupportedOperationException("Bishop not yet implemented.");
            case KNIGHT:
                throw new UnsupportedOperationException("Knight not yet implemented.");
            case ROOK:
                throw new UnsupportedOperationException("Rook not yet implemented.");
            case PAWN:
                throw new UnsupportedOperationException("Pawn not yet implemented.");
            default:
                throw new IllegalArgumentException("Invalid piece type argument");
        }

        mChessPieces.add(newPiece);
        return newPiece;
    }
    
    /**
     * Adds a piece to the game (adds in the ArrayList mChessPieces)
     * and places on board
     * */
    private ChessPiece addPieceToGame(int color, char type, int rank, int file)
    {
        if ( !isValidCoord(rank,file) )
            throw new IllegalArgumentException("Invalid coordinate argument");
        if ( mChessBoard[rank][file] != null )  // position is already occupied!
            return null;    
        ChessPiece newPiece = addPieceToGame(color, type);
        //if ( newPiece == null )
        //    return null;
        newPiece.setPosition(rank, file);
        return newPiece;
    }
    
    
    /* *************************************************
     * * * * Static Methods * * * 
     * *************************************************/
    // conversion methods
    public static int convertAlgebraicToInternalRank(String coord)
        { return  Character.getNumericValue(coord.toLowerCase().charAt(1)) - 1; }
    public static int convertAlgebraicToInternalFile(String coord)
        { return (int)coord.toLowerCase().charAt(0) - 97; }
    
    public static String convertInternalToAlgebraic(int rank, int file)
    {
        if ( !isValidCoord(rank, file) )
            throw new IllegalArgumentException("Invalid Coordinate");
        return ((char) (file+97)) + String.valueOf(rank + 1);
    }
    
    
    // get a square color
    public static int getSquareColor(String coord)
    {
        return getSquareColor(Chess.convertAlgebraicToInternalRank(coord),
                Chess.convertAlgebraicToInternalFile(coord) ); 
    }
    
    public static int getSquareColor(int rank, int file)
    {
        if ( !isValidCoord(rank, file) )
            throw new IllegalArgumentException("Invalid Coordinate");
        return (rank+file)%2; 
    }
    
    public static String getMoveCodeText(int code)
    {
        switch(code)
        {
            case MOVE_LEGAL:
                return "Move is legal.";
            case MOVE_ILLEGAL:                
                return "Not a legal move.";
            case MOVE_ILLEGAL_KING_IN_CHECK:  
                return "King is in check.";
            case MOVE_ILLEGAL_IMPEDED:     
                return "Move is impeded.";
            case MOVE_ILLEGAL_SQUARE_EMPTY:   
                return "No piece at the square.";
            case MOVE_ILLEGAL_WRONG_PLAYER:
                return "Wrong Player.";
            case MOVE_ILLEGAL_CASTLE_THROUGH_CHECK:
                return "Cannot castle through check.";
            case AMBIGUOUS_PROMOTION:
                return "Promotion ambiguous.";
            case GAME_NOT_ACTIVE:
                return "Game is not active.";
            case PIECE_NOT_ACTIVE:
                return "Piece is not in play.";
            default:
                return "Unknown Code";
        }
        
    }
    
    public static char getUnicode(int color, char type)
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
    
    public static String getName(char type)
    {
        switch (type) {
            case KING:
                return "King";
            case QUEEN:
                return "Queen";
            case BISHOP:
                return "Bishop";
            case KNIGHT:
                return "Knight";
            case ROOK:
                return "Rook";
            case PAWN:
                return "Pawn";
            default:
                return "InvalidType";
        }
    }
    
    public static String getColorString(int color)
    {
        if (color == WHITE)
            return "White";
        else if (color == BLACK)
            return "Black";
        return "InvalidColor";
    }
    
    private static boolean isValidCoord(int rank, int file)
    {
        return (rank >= 0 && rank <= 7
                && file >= 0 && file <= 7);
    }
    
    
    
    /**************************************************************************
     * ************************************************************************
     * ************************************************************************
     *  * * * * Abstract Chess Piece Class * * * * * * * * * * * * * * * * * *
     * ************************************************************************
     * ************************************************************************
     * ************************************************************************/
    public abstract class ChessPiece
    {
        protected int mRank;
        protected int mFile;
        //protected String mPositionString = "";
        protected char mType;
        protected int mColor;
        protected int mStatus;
        protected boolean mIsActive = false;
        protected int mMoveCount = 0;
        
        /**
         * Makes a move given the algebraic coordinate of target square
         * @param coord algebraic coordinate to move to, along with promotion 
         *              if needed. ex: "e4", "c1", "b8=Q"
         * @return MOVE_LEGAL (100) if its a good move, 
         *                otherwise returns error code
         */
        public int makeMove(String coord)
        {
            // TODO: Override in Pawn class to check for promotion option
            //       and call appropriate method
            return makeMove(Chess.convertAlgebraicToInternalRank(coord),
                    Chess.convertAlgebraicToInternalFile(coord) );
        }
                
        
        /**
         * MAKES THE MOVE! after validating the move by calling validateMove()
         * @param rank value from 0-7
         * @param file value from 0-7
         * @param promotionType The piece to promote to. QUEEN, BISHOP, KNIGHT, ROOK
         * @return MOVE_LEGAL (100) if its a good move, 
         *                otherwise returns error code
         */
        public int makeMove(int rank, int file, char promotionType)
        {
            // TODO: Override in Pawn class to handle promotions
            // Otherwise, just ignore promotionType
            
            //System.out.println("Promotion type: " + promotionType); // DEBUG
            return makeMove(rank, file);
        }
        
        
        /**
         * MAKES THE MOVE! after validating the move by calling validateMove()
         * @param rank value from 0-7
         * @param file value from 0-7
         * @return MOVE_LEGAL (100) if its a good move, 
         *                otherwise returns error code
         */
        abstract public int makeMove(int rank, int file);
        
        
        /**
         * Validates a move.
         * @param rank value from 0-7
         * @param file value from 0-7
         * @return MOVE_LEGAL (100) if its a good move, 
         *                otherwise returns error code
         */
        abstract public int validateMove(int rank, int file);
        
        
        private void beingCaptured()
        {
            // TODO: implement
        }
                
        
        private void setPosition(String coord)
        {
            // TODO: check that mGameActive == false
            setPosition(Chess.convertAlgebraicToInternalRank(coord),
                    Chess.convertAlgebraicToInternalFile(coord) );
        }
        
        private void setPosition(int rank, int file)
        {
            // TODO: check that mGameActive == false
            if ( !isValidCoord(rank, file) )
                throw new IllegalArgumentException("Illegal arguement for setPosition");
            mRank = rank;
            mFile = file;
            //mPositionString = convertInternalToAlgebraic(rank, file);
            mChessBoard[rank][file] = this;
            mStatus = PIECE_ACTIVE;
            mIsActive = true;
        }
        
        // public get methods
        public String getPosition()
            { return Chess.convertInternalToAlgebraic(mRank, mFile); } 
        public int getPositionInternalRank() { return mRank; }
        public int getPositionInternalFile() { return mFile; }
        public char getType() { return mType; }
        public int getColor() { return mColor; }
        public int getStatus() { return mStatus; }
        public int getMoveCount() { return mMoveCount; }
        public String getName() { return Chess.getName(mType); }
        public char getUnicode() { return Chess.getUnicode(mColor, mType); }
    }
    
    /**************************************************************************
     * ************************************************************************
     * * * * The Actual Chess Pieces * * * * * * * * * * * * * * * * * * * * * 
     * ************************************************************************
     **************************************************************************/
    
    /**
     * King class
     */
    private class King extends ChessPiece
    {
        // Constructor
        private King(int color)
        {
            mColor = color;
            mType = KING;
        }

        @Override
        public int validateMove(int rank, int file) 
        {
            // TODO: implement
            return MOVE_LEGAL; // returns valid for now
        }

        @Override
        public int makeMove(int rank, int file) 
        {
            // TODO: finish implementation
            
            // DEBUG printout
            System.out.println("Executing ("
                    + Chess.convertInternalToAlgebraic(mRank, mFile)
                    + ") " + Chess.getName(mType) 
                    + ".makeMove(" + rank + ", " + file + ")" );

            
            // check if game is active
            if ( !mIsGameActive )
                return GAME_NOT_ACTIVE;
            
            // check mColor
            if ( mColor != mWhoseTurn )
                return MOVE_ILLEGAL_WRONG_PLAYER;
            
            // validate move
            int code = validateMove(rank, file);
            if ( code != MOVE_LEGAL ) return code;
            
            // check if target square is occupied.
            // if so, capture that piece
            
            // set the new position
            // update mChessBoard
            
            // add move to mChessHistory
            
            // increment mMoveCount
            
            // call endTurn() method
            
            
            return code;            
        }
    }
    
    // TODO: Implement all the pieces!!!!
    

    /** *****************************************************
     * ChessMove object for keeping track of game history
     *********************************************************/
    private class RecordOfMove
    {
        // TODO: imlement this class
    }
}
