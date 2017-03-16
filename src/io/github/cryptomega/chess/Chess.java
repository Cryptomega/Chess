/*
 * 
 */
package io.github.cryptomega.chess;

import static java.lang.Math.abs;
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
    public static final int MOVE_ILLEGAL_SQUARE_OCCUPIED      = 107;
    public static final int MOVE_ILLEGAL_PAWN_BLOCKED         = 108;
    public static final int AMBIGUOUS_PROMOTION               = 120;
    public static final int GAME_NOT_ACTIVE                   = 121;
    public static final int PIECE_NOT_ACTIVE                  = 122;
    public static final int PIECE_IS_OBSERVING                = 199;
    
    
    // Piece states
    public static final int PIECE_ACTIVE        = 200;
    public static final int PIECE_CAPTURED      = 201;
    public static final int PIECE_PROMOTED      = 202;
    public static final int PIECE_NOT_PLACED    = 203;
    
    // Player states
    public static final int PLAYER_OK           = 900;
    public static final int PLAYER_IN_CHECK     = 901;
    public static final int PLAYER_IN_CHECKMATE = 902;
    public static final int PLAYER_IN_STALEMATE = 903;
    public static final int PLAYER_CLAIMS_DRAW  = 904;
    
    
    /* ****************************************
     * * * Game State variables * * *
     * ****************************************/
    private boolean mIsGameActive;
    private int mWhoseTurn;
    
    private int mTurnCount;
    private boolean mWhiteOfferingDraw;
    private boolean mBlackOfferingDraw;
    //private boolean mInEditMode = false;
    //private boolean mInAnalysisMode = false;
    //private boolean mIsChess960 = false;
    
    private int mWhiteKingIndex = -1;
    private int mBlackKingIndex = -1;
    
    
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
    public int getMoveNumber() { return (2 + mTurnCount) / 2; }
    
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
    
    //public void restartGame() {}  // TODO: implement? might not be useful
    
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
        mTurnCount = 0;
        mWhoseTurn = WHITE;
        mWhiteKingIndex = -1;
        mBlackKingIndex = -1;
        
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
        // TODO: consider requiring mIsGameActive to be false 
        //if ( mIsGameActive == true )
        //    throw new IllegalStateException("Cannot add piece while game is active");
        clearGame();
       
        //TODO: Create pieces and place on board

        addPieceToGame(WHITE, KING, 0, 4 );
        addPieceToGame(WHITE, QUEEN, 0, 3 );
        addPieceToGame(WHITE, BISHOP, 0, 2 );
        addPieceToGame(WHITE, KNIGHT, 0, 1 );
        addPieceToGame(WHITE, ROOK, 0, 0 );
        addPieceToGame(WHITE, BISHOP, 0, 5 );
        addPieceToGame(WHITE, KNIGHT, 0, 6 );
        addPieceToGame(WHITE, ROOK, 0, 7 );
        addPieceToGame(WHITE, PAWN, 1, 4);
        
        
        addPieceToGame(BLACK, KING, 7, 4 );
        addPieceToGame(BLACK, QUEEN, 7, 3 );
        addPieceToGame(BLACK, BISHOP, 7, 2 );
        addPieceToGame(BLACK, KNIGHT, 7, 1 );
        addPieceToGame(BLACK, ROOK, 7, 0 );
        addPieceToGame(BLACK, BISHOP, 7, 5 );
        addPieceToGame(BLACK, KNIGHT, 7, 6 );
        addPieceToGame(BLACK, ROOK, 7, 7 );
        

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
    
    /**
     * Check to see if king is in check
     * @param color color of king to examine
     * @return true if in check, false if not
     */
    public boolean isInCheck(int color)
    {
        // get the king
        ChessPiece king = getKing(color);
        int kingRank = king.getPositionInternalRank();
        int kingFile = king.getPositionInternalFile();
        return isInCheck(color, kingRank, kingFile);
    }
    
    /**
     * Check to see if king would be in check at a coordinate
     * @param color color of king to examine
     * @param rank rank of square to examine
     * @param file file of square to examine
     * @return true if in check, false if not
     */
    public boolean isInCheck(int color, int rank, int file)
    {
        
        for ( ChessPiece piece : mChessPieces )
        {
            // skip if color matches kings color, or if inactive
            if ( !piece.mIsActive || piece.getColor() == color )
                continue;
            
            if ( piece.isObserving(rank, file) == PIECE_IS_OBSERVING )
                return true;
        }
        return false;
    }
    
    public int checkPlayerState(int color)
    {
        // TODO: impement
        // check for check, checkmate, stalemate, or draw
        
        /*
        return codes:
        PLAYER_OK
        PLAYER_IN_CHECK
        PLAYER_IN_CHECKMATE
        PLAYER_IN_STALEMATE
        PLAYER_CLAIMS_DRAW
        */
        return PLAYER_OK;
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
        //if ( mIsGameActive == true )
        //    throw new IllegalStateException("Cannot add piece while game is active");
        if ( color != WHITE && color != BLACK )
            throw new IllegalArgumentException("Invalid color argument");
        ChessPiece newPiece;
        switch (type) 
        {
            case KING:
                newPiece = new King(color);
                break;
            case QUEEN:
                newPiece = new Queen(color);
                break;
            case BISHOP:
                newPiece = new Bishop(color);
                break;
            case KNIGHT:
                newPiece = new Knight(color);
                break;
            case ROOK:
                newPiece = new Rook(color);
                break;
            case PAWN:
                newPiece = new Pawn(color);
                break;
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
            throw new IllegalStateException("Cannot add piece on occupied square");   
        ChessPiece newPiece = addPieceToGame(color, type);
        //if ( newPiece == null )
        //    return null;
        newPiece.setPosition(rank, file);
        return newPiece;
    }
    
    private ChessPiece getKing(int color)
    {
        int index = getKingIndex(color);
        if (index == -1)
            throw new IllegalStateException("King not found!");
        return mChessPieces.get(index);
    }
    
    private int getKingIndex(int color)
    {
        int kingIndex = (color == WHITE) ? mWhiteKingIndex : mBlackKingIndex;
        if ( kingIndex != -1 ) // TODO: check that is actually the king??? seems unnessicary
            return kingIndex;
        
        
        for (int i = 0; i < mChessPieces.size(); i++ )
            if ( mChessPieces.get(i).getType() == KING
                    && mChessPieces.get(i).getColor() == color )
            {
                if ( color == WHITE)
                    mWhiteKingIndex = i;
                else
                    mBlackKingIndex = i;
                return i;
            }
        return -1;
    }
    
    public String getCompleteMoveHistory()
    {
        String history = "";
        boolean whitesTurn = true;
        for ( RecordOfMove item : mChessHistory )
        {
            if ( whitesTurn )
                history += item.movePrefix;
            history += item.moveText;
            if ( whitesTurn )
                history += " ";
            else
                history += "\n";
            whitesTurn = !whitesTurn;
        }
        return history;
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
            case MOVE_ILLEGAL_SQUARE_OCCUPIED:
                return "Square is already occupied";
            case MOVE_ILLEGAL_PAWN_BLOCKED:
                return "Pawn is blocked";
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
        protected int mRank, mFile;
        //protected String mPositionString = "";
        protected final char mType;
        protected final int mColor;
        protected int mStatus = PIECE_NOT_PLACED;
        protected boolean mIsActive = false;
        protected int mMoveCount = 0;
        
        protected ChessPiece(int color, char type)
        {
            if ( color != WHITE && color != BLACK ) // validate color 
                throw new IllegalArgumentException("Invalid color.");
            mColor = color;
            mType = type;
        }
        
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
         * MAKES THE MOVE! after validating the move by calling validateMove
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
         * MAKES THE MOVE! after validating the move by calling validateMove
         * @param rank value from 0-7
         * @param file value from 0-7
         * @return MOVE_LEGAL (100) if its a good move, 
         *                otherwise returns error code
         */
        public int makeMove(int rank, int file)
        {
            // TODO: finish implementation

            //DEBUG 
            /*
            System.out.print(getName() + " is observing: ");
            for (int i =0; i < 8; i++ )
                for (int j =0; j < 8; j++ )
                    if ( isObserving(i,j) == MOVE_LEGAL )
                        System.out.print(
                                Chess.convertInternalToAlgebraic(i, j)
                            + ", ");
            System.out.println("");
            */
            // END DEBUG
            
            /*
            // DEBUG
            for (int i =0; i < 8; i++ )
                for (int j =0; j < 8; j++ )
                    if (isInCheck(mColor,i,j) )
                        System.out.println(Chess.convertInternalToAlgebraic(i, j) + " is in check.");
            // END DEBUG
            */
            
            // DEBUG printout
            /*
            System.out.println("Executing ("
                    + getPosition()
                    + ") " + Chess.getName(mType) 
                    + " makeMove to " +
                    Chess.convertInternalToAlgebraic(rank, file));
            */
            // END DEBUG
            
            // check if game is active
            if ( !mIsGameActive )
                return GAME_NOT_ACTIVE;
            
            // make sure piece is active
            if ( !mIsActive )
                return PIECE_NOT_ACTIVE;
            
            // check mColor
            if ( mColor != mWhoseTurn )
                return MOVE_ILLEGAL_WRONG_PLAYER;
            
            // validate move
            int code = validateMove(rank, file);
            if ( code != MOVE_LEGAL ) return code;
            
            // switch timer
            
            
             // TODO: for pawn, implement en passant differently
            // capture piece, if any
            ChessPiece captured = mChessBoard[rank][file];
            if ( captured != null )
                captured.captured();
            
            // hold onto last location
            int fromRank = mRank;
            int fromFile = mFile;
            
            // set the new position and update mChessBoard
            updatePosition(rank, file);
            
            // if we de-abstract this function, override this function
            // for king and pawn class to handle pawn promotions and castling
            // right here
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( mColor == WHITE ) ? BLACK : WHITE;
            int playerStateCode = checkPlayerState(opponentColor);
            boolean check = playerStateCode == PLAYER_IN_CHECK;
            boolean checkmate = playerStateCode == PLAYER_IN_CHECKMATE;
            
            
            // add move to mChessHistory (pass coordinates of previous square)
            mChessHistory.add(new RecordOfMove(
                    this, fromRank, fromFile,
                    captured, null, 
                    check, checkmate        ) );
            
            
            // increment mTurnCount and mMoveCount
            mTurnCount++;
            mMoveCount++;
            
            // switch turn to other player
            mWhoseTurn = (mWhoseTurn == WHITE) ? BLACK : WHITE;
 
            return code;
        }
        
        
        /**
         * Validates a move.
         * @param rank value from 0-7
         * @param file value from 0-7
         * @return MOVE_LEGAL (100) if its a good move, 
         *                otherwise returns error code
         */
        public int validateMove(int rank, int file)
        {
            // if not castling:
            
            // piece must be observing the square
            int isObservingCode = isObserving(rank,file);
            if ( isObservingCode != PIECE_IS_OBSERVING )
                return isObservingCode;
            
            // square cannot be occupied by own piece
            if ( mChessBoard[rank][file] != null
                    && mChessBoard[rank][file].getColor() == mColor )
                return MOVE_ILLEGAL_SQUARE_OCCUPIED;
            
            // cannot move into check:
            //      (1)update moving piece position, remove catpured piece if any
            //      (2)call isInCheck(mColor)
            //      (3)undo moving piece, undo removing captured piece
            
            int fromRank = getPositionInternalRank();  // save current rank
            int fromFile = getPositionInternalFile();  // and file
            
            // (1) get piece to be captured, if any
            ChessPiece captured = mChessBoard[rank][file];
            if ( captured != null )
                captured.mIsActive = false;     // temporarily deactivate
            updatePosition(rank,file);   // (1)temporarily move the piece
            
            boolean isInCheck = isInCheck(mColor);  // (2)
            
            // undo temporary move (3)
            updatePosition(fromRank, fromFile);
            mChessBoard[rank][file] = captured;
            if ( captured != null )
                captured.mIsActive = true;
            
            if ( isInCheck )
                return MOVE_ILLEGAL_KING_IN_CHECK;
            
            return MOVE_LEGAL; // returns valid for now
        }
        
        
        protected void captured()
        {
            mIsActive = false;
            mStatus = PIECE_CAPTURED;
            mChessBoard[mRank][mFile] = null;
        }
                
        
        protected void setPosition(String coord)
        {
            setPosition(Chess.convertAlgebraicToInternalRank(coord),
                    Chess.convertAlgebraicToInternalFile(coord) );
        }
        
        protected void setPosition(int rank, int file)
        {
            //if ( mIsGameActive == true )
             //   throw new IllegalStateException("Cannot set piece position while game is active");
            if ( !isValidCoord(rank, file) )
                throw new IllegalArgumentException("Illegal arguement for setPosition");

            // check if square is already occupied
            if ( mChessBoard[rank][file] != null )
                throw new IllegalStateException("Cannot set piece on occupied square");
            
            mRank = rank;
            mFile = file;
            //mPositionString = convertInternalToAlgebraic(rank, file);
            mChessBoard[rank][file] = this;
            mStatus = PIECE_ACTIVE;
            mIsActive = true;
            
        }
        
        // used by makeMove()
        protected void updatePosition(int rank, int file)
        {
            // set current position to null
            mChessBoard[mRank][mFile] = null;
            // set reference to this piece at new square
            mChessBoard[rank][file] = this;
            // set the position in the piece
            mRank = rank;
            mFile = file;
        }
        
           /**
            * Returns true if the piece is observing (attacking) a square
            * @param rank value from 0-7
            * @param file value from 0-7
            * @return true or false
            */
        abstract public int isObserving(int rank, int file);
        
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
        { super(color, KING); }

        //@Override
        //public int validateMove(int rank, int file) 
        
            // TODO: implement
            
            // TODO: override in King to implement castling
            // is player trying to castle? if yes:
            //      cannot castle out of check
            //      cannot castle through or into check
            //      king and rook cannot have moved
            //      cannot be impeded
        

        //@Override
        //public int makeMove(int rank, int file) 
        // TODO: override makeMove to implement castling

        @Override
        public int isObserving(int rank, int file) 
        {
            if ( ( Math.abs(mRank - rank) <= 1 ) 
                    && ( Math.abs(mFile - file) <= 1 )
                    && (rank != mRank || file != mFile) )
                return PIECE_IS_OBSERVING;
            else
                return MOVE_ILLEGAL; 
        }
    }
    
    
    /**
     * Queen Class
     */
    private class Queen extends ChessPiece
    {
        private Queen(int color)
        { super(color, QUEEN); }

        @Override
        public int isObserving(int rank, int file)
        {
            //System.out.println("Calling queen isObserving " + Chess.convertInternalToAlgebraic(rank, file)); // DEBUG
            if ( mRank == rank && mFile == file )  // already occupying square
                 return MOVE_ILLEGAL;
            else if ( mRank == rank )               // check if on same rank
            {
                // check if impeded
                int d = mFile - file;   // difference
                int s = Integer.signum(file - mFile);   // sign
                for ( int i = 1; abs(d+i*s) > 0 ; i++)
                {
                    // Look at square (mRank), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(rank, mFile+s*i)); // DEBUG
                    if ( mChessBoard[mRank][mFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( mFile == file ) {           // check if on same file
                int d = mRank - rank;
                int s = Integer.signum(rank - mRank);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+s*i, mFile)); // DEBUG
                    if( mChessBoard[mRank+s*i][mFile] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( abs((double)(rank - mRank) / (double)(file - mFile) ) == 1.0 ) {  // check diagonalS
                int d = mFile - file;   // difference
                int s = Integer.signum(file - mFile);   // sign
                int sl = (int)((double)(rank - mRank) / (double)(file - mFile)) ;  // slope
                //System.out.println("isObserving on diagonal, slope is " + sl);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    // look at square (mRank + s*i), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+sl*s*i, mFile+s*i)); // DEBUG
                    if( mChessBoard[mRank+sl*s*i][mFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING; 
            }
            return MOVE_ILLEGAL;            
        }
    }

    /**
     * Rook Class
     */
    private class Rook extends ChessPiece
    {
        private Rook(int color)
        { super(color, ROOK); }
        
        @Override
        public int isObserving(int rank, int file)
        {
            if ( mRank == rank && mFile == file )  // already occupying square
                 return MOVE_ILLEGAL;
            else if ( mRank == rank )               // check if on same rank
            {
                // check if impeded
                int d = mFile - file;   // difference
                int s = Integer.signum(file - mFile);   // sign
                for ( int i = 1; abs(d+i*s) > 0 ; i++)
                {
                    // Look at square (mRank), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(rank, mFile+s*i)); // DEBUG
                    if ( mChessBoard[mRank][mFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( mFile == file ) {           // check if on same file
                int d = mRank - rank;
                int s = Integer.signum(rank - mRank);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+s*i, mFile)); // DEBUG
                    if( mChessBoard[mRank+s*i][mFile] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            }
            return MOVE_ILLEGAL;
        }
    }
    
    /**
     * Bishop class
     */
    private class Bishop extends ChessPiece
    {
        private Bishop(int color)
        { super(color, BISHOP); }

        @Override
        public int isObserving(int rank, int file)
        {
            if ( mRank == rank && mFile == file )  // already occupying square
                 return MOVE_ILLEGAL;
            else if ( abs((double)(rank - mRank) / (double)(file - mFile) ) == 1.0 ) {  // check diagonalS
                int d = mFile - file;   // difference
                int s = Integer.signum(file - mFile);   // sign
                int sl = (int)((double)(rank - mRank) / (double)(file - mFile)) ;  // slope
                //System.out.println("isObserving on diagonal, slope is " + sl);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    // look at square (mRank + s*i), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+sl*s*i, mFile+s*i)); // DEBUG
                    if( mChessBoard[mRank+sl*s*i][mFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING; 
            }
            return MOVE_ILLEGAL;
        }
    }
    
    /**
     * Knight class
     */
    private class Knight extends ChessPiece
    {
        private Knight(int color)
        { super(color, KNIGHT); }

        @Override
        public int isObserving(int rank, int file)
        {
            //if ( mRank == rank && mFile == file )  // already occupying square
            //     return MOVE_ILLEGAL;
            int absRankDif = abs(mRank - rank);
            int absFileDif = abs(mFile - file);
            if ( ( absRankDif == 2 && absFileDif == 1 ) ||
                 ( absRankDif == 1 && absFileDif == 2  )  )
                return PIECE_IS_OBSERVING;
            return MOVE_ILLEGAL;
                    
        }
    }
    
    /**
     * Pawn class
     */
    private class Pawn extends ChessPiece
    {
        private Pawn(int color)
        { super(color, PAWN); }

        @Override
        public int isObserving(int rank, int file)
        {
            int direction = (mColor == WHITE) ? 1 : -1;
            if ( (mRank + direction == rank) &&
                    abs(mFile - file) == 1 )
                return PIECE_IS_OBSERVING;
            return MOVE_ILLEGAL;
        }
    }
            
    
    // TODO: Implement all the pieces!!!!
    

    /** *****************************************************
     * ChessMove object for keeping track of game history
     *********************************************************/
    private class RecordOfMove
    {
        final public int moveNumber;
        final public String movePrefix; // "1. " or "1... "
        final public String moveText;   // Readable move notation
        
        // The piece moved. required
        final public ChessPiece PieceMoved;
        final public int fromRank, fromFile;
        final public int toRank, toFile;
        
        // Captured piece. null if nothing captured
        final public ChessPiece PieceCaptured;
        final public int capturedRank, capturedFile;
                
        // Piece promoted to. null if no promotion
        final public ChessPiece PiecePromoted;
        final public char promotionType;
        
        
        // Rook castled with. null if player didn't castle
        final public ChessPiece RookCastled;
        final public int fromRookRank, fromRookFile;
        
        /**
         * Gets a notational representation of the move
         * @return example: "1... e7 e5"
         */
        public String getMoveText()
        { return movePrefix + moveText; }

        /**
         * Call after piece has moved and its position updated, before 
         * turn count has been incremented
         * @param moved reference to piece which has moved
         * @param movedFromRank rank the piece moved from
         * @param movedFromFile file the piece moved from
         * @param captured piece which was captured, or null
         * @param promo piece promoted to, or null
         * @param check true opponent is being checked
         * @param checkmate true if opponent is being mated
         */
        public RecordOfMove(ChessPiece moved, int movedFromRank, int movedFromFile, 
                ChessPiece captured, ChessPiece promo, 
                boolean check, boolean checkmate)
        {
            PieceMoved = moved;
            fromRank = movedFromRank;
            fromFile = movedFromFile;
            toRank = PieceMoved.getPositionInternalRank();
            toFile = PieceMoved.getPositionInternalFile();
            
            PieceCaptured = captured;
            if ( PieceCaptured != null )
            {
                capturedRank = moved.getPositionInternalRank();
                capturedFile = moved.getPositionInternalFile();
            } else {
                capturedRank = -1;
                capturedFile = -1;
            }
            
            PiecePromoted = promo;
            if ( PiecePromoted != null )
                promotionType = PiecePromoted.getType();
            else
                promotionType = 'x';
            
            RookCastled = null;
            fromRookRank = -1;
            fromRookFile = -1;
            
            moveNumber = getMoveNumber();
            if ( mWhoseTurn == WHITE )
                movePrefix = String.valueOf(moveNumber) + ". ";
            else
                movePrefix = String.valueOf(moveNumber) + "... ";
            
            // construct move string
            StringBuilder sb = new StringBuilder();
            if ( PieceMoved.getType() == PAWN )
                sb.append(" ");
            else
                sb.append(PieceMoved.getType());
            sb.append(Chess.convertInternalToAlgebraic(fromRank, fromFile))
            .append( (PieceCaptured == null) ? "-" : "x" )
            .append(Chess.convertInternalToAlgebraic(toRank, toFile))
            .append( (PiecePromoted == null) ? "" : "=" + promotionType )
            .append( checkmate ? "#" : check ? "+" : "");
            moveText = sb.toString();
        }
        

        /**
         * Call after piece has moved and its position updated, before 
         * turn count has been incremented. Constructor for castling
         * @param moved reference to piece which has moved
         * @param movedFromRank rank the piece moved from
         * @param movedFromFile file the piece moved from
         * @param castledRook reference to rook making the castle move
         * @param fromRookRank rank of castling rook
         * @param fromRookFile file of castling rook
         * @param check true opponent is being checked
         * @param checkmate true if opponent is being mated
         */
        public RecordOfMove(ChessPiece moved, int movedFromRank, int movedFromFile, 
                ChessPiece castledRook, int fromRookRank, int fromRookFile,
                boolean check, boolean checkmate)
        {
            PieceMoved = moved;
            fromRank = movedFromRank;
            fromFile = movedFromFile;
            toRank = PieceMoved.getPositionInternalRank();
            toFile = PieceMoved.getPositionInternalFile();

            RookCastled = castledRook;
            if ( RookCastled == null )
                throw new IllegalArgumentException("Called wrong RecordOfMove constructor");
            this.fromRookRank = fromRookRank;
            this.fromRookFile = fromRookFile;
            
            PieceCaptured = null;
            capturedRank = -1;
            capturedFile = -1;
            PiecePromoted = null;
            promotionType = 'x';
            
            moveNumber = getMoveNumber();
            if ( mWhoseTurn == WHITE )
                movePrefix = String.valueOf(moveNumber) + ". ";
            else
                movePrefix = String.valueOf(moveNumber) + "... ";
            
            if ( fromRookFile < movedFromFile  )
                moveText = "0-0-0";
            else
                moveText = "0-0";
        }
    }
}
