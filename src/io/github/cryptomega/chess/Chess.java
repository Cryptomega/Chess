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
    public static final int MOVE_LEGAL_EN_PASSANT             = 101;
    public static final int MOVE_LEGAL_CASTLE_KINGSIDE        = 102;
    public static final int MOVE_LEGAL_CASTLE_QUEENSIDE       = 103;
    public static final int MOVE_ILLEGAL                      = 104;
    public static final int MOVE_ILLEGAL_IMPEDED              = 105;
    public static final int ILLEGAL_CASTLE_THROUGH_CHECK      = 106;
    public static final int MOVE_ILLEGAL_SQUARE_OCCUPIED      = 107;
    public static final int MOVE_ILLEGAL_PAWN_BLOCKED         = 108;
    public static final int MOVE_ILLEGAL_PAWN_HAS_MOVED       = 109;
    public static final int MOVE_ILLEGAL_NOTHING_TO_CAPTURE   = 110;
    public static final int MOVE_ILLEGAL_LATE_EN_PASSANT      = 111;
    public static final int MOVE_ILLEGAL_KING_IN_CHECK        = 112;
    public static final int MOVE_ILLEGAL_SQUARE_EMPTY         = 113;
    public static final int ILLEGAL_CASTLE_KING_HAS_MOVED     = 114;
    public static final int ILLEGAL_CASTLE_ROOK_HAS_MOVED     = 115;
    public static final int ILLEGAL_CASTLE_IMPEDED            = 116;
    public static final int MOVE_ILLEGAL_WRONG_PLAYER         = 117;
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
    
    // Game states
    public static final int STATUS_WHITES_TURN =             800; // in progress
    public static final int STATUS_BLACKS_TURN =             801;
    public static final int STATUS_WHITE_IN_CHECK =          802;
    public static final int STATUS_BLACK_IN_CHECK =          803;
            
    public static final int STATUS_WHITE_WINS_CHECKMATE =    804; // wins
    public static final int STATUS_BLACK_WINS_CHECKMATE =    805;
    public static final int STATUS_WHITE_WINS_RESIGNATION =  806;
    public static final int STATUS_BLACK_WINS_RESIGNATION =  807;
    public static final int STATUS_WHITE_WINS_TIME =         808;
    public static final int STATUS_BLACK_WINS_TIME =         809;
    
    public static final int STATUS_DRAW_WHITE_CLAIMS_THREE = 810; // draws
    public static final int STATUS_DRAW_BLACK_CLAIMS_THREE = 811;
    public static final int STATUS_DRAW_WHITE_CLAIMS_FIFTY = 812;
    public static final int STATUS_DRAW_BLACK_CLAIMS_FIFTY = 813;
    public static final int STATUS_DRAW_AGREEMENT =          814;
    public static final int STATUS_DRAW_WHITE_STALEMATE =    815;
    public static final int STATUS_DRAW_BLACK_STALEMATE =    816;
    
    
    /* ****************************************
     * * * Game State variables * * *
     * ****************************************/
    private boolean mIsGameActive;
    private int mWhoseTurn;
    private int mGameState;
    
    private int mTurnCount;
    private boolean mWhiteOfferingDraw;
    private boolean mBlackOfferingDraw;
    //private boolean mInEditMode = false;
    //private boolean mInAnalysisMode = false;
    //private boolean mIsChess960 = false;
    
    private int mWhiteKingIndex = -1;
    private int mBlackKingIndex = -1;
    
    /**
     * Makes end of turn game state updates, as well as check
     * for end of game conditions. 
     * @param nextPlayerState contains the player state code of the
     *          player who's turn is about to begin
     */
    private void endTurn(int nextPlayerState)
    {
        // DEBUG
        System.out.println("endTurn called");
        
        switch (nextPlayerState)
        {
            case PLAYER_OK:
                mGameState = (mWhoseTurn == WHITE) ? 
                        STATUS_BLACKS_TURN : STATUS_WHITES_TURN;
                break;
            case PLAYER_IN_CHECK:
                mGameState = (mWhoseTurn == WHITE) ? 
                        STATUS_BLACK_IN_CHECK : STATUS_WHITE_IN_CHECK;
                break;
            case PLAYER_IN_CHECKMATE:
                mGameState = (mWhoseTurn == WHITE) ? 
                        STATUS_WHITE_WINS_CHECKMATE : STATUS_BLACK_WINS_CHECKMATE;
                mIsGameActive = false;
                break;
            case PLAYER_IN_STALEMATE:
                mGameState = (mWhoseTurn == WHITE) ? 
                        STATUS_DRAW_BLACK_STALEMATE : STATUS_DRAW_WHITE_STALEMATE;
                mIsGameActive = false;
                break;
        }
        
        // TODO: implement
        
        // mIsGameActive
        

        // switch turn to other player
        mWhoseTurn = (mWhoseTurn == WHITE) ? BLACK : WHITE;
        mTurnCount++;
        
        // switch over clock
        
        // TODO: implement drawing
        
        // mWhiteOfferingDraw
        // mBlackOfferingDraw
        
        // checks for draw conditions
        // updates game state for wins, draws
        // transistions between turns
        // updates and state variables
        // ONLY METHOD WHICH UPDATES GAME STATE VARIABLES
        // TODO: calls game state listeners
    }
    
    
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
    
    public String getGameStatus() { return getGameStatusText(mGameState); }
    
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
    
    public String getCompleteMoveHistory()
    {
        StringBuilder history = new StringBuilder();
        boolean whitesTurn = true;
        for ( RecordOfMove item : mChessHistory )
        {
            if ( whitesTurn )
                history.append( item.movePrefix );
            history.append( item.moveText );
            if ( whitesTurn )
                history.append( " ");
            else
                history.append(  "\n" );
            whitesTurn = !whitesTurn;
        }
        return history.toString();
    }
    
    /**
     * Initializes the game board (mChessBoard) to all null values 
     * Clears the pieces array (mChessPieces)
     * Clears the history stack
     */
    public final void clearGame()
    {
        // reset game variables
        mWhiteOfferingDraw = false;
        mBlackOfferingDraw = false;
        mIsGameActive = false;  
        mTurnCount = 0;
        mWhoseTurn = WHITE;
        mGameState = STATUS_WHITES_TURN;
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
       
        

        addPieceToGame(WHITE, KING, 0, 4 );
        addPieceToGame(WHITE, QUEEN, 0, 3 );
        addPieceToGame(WHITE, BISHOP, 0, 2 );
        addPieceToGame(WHITE, KNIGHT, 0, 1 );
        addPieceToGame(WHITE, ROOK, 0, 0 );
        addPieceToGame(WHITE, BISHOP, 0, 5 );
        addPieceToGame(WHITE, KNIGHT, 0, 6 );
        addPieceToGame(WHITE, ROOK, 0, 7 );
        for ( int i =0; i<8; i++)
            addPieceToGame(WHITE, PAWN, 1, i);
        
        
        addPieceToGame(BLACK, KING, 7, 4 );
        addPieceToGame(BLACK, QUEEN, 7, 3 );
        addPieceToGame(BLACK, BISHOP, 7, 2 );
        addPieceToGame(BLACK, KNIGHT, 7, 1 );
        addPieceToGame(BLACK, ROOK, 7, 0 );
        addPieceToGame(BLACK, BISHOP, 7, 5 );
        addPieceToGame(BLACK, KNIGHT, 7, 6 );
        addPieceToGame(BLACK, ROOK, 7, 7 );
        for ( int i =0; i<8; i++)
            addPieceToGame(BLACK, PAWN, 6, i);

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
        int kingRank = king.getRank();
        int kingFile = king.getFile();
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
        ChessPiece king = getKing(color);
        boolean playerInCheck = isInCheck(color);
        boolean playerInMate = false;
        if ( playerInCheck )
        {
            // check for checkmate
            if ( cannotEscapeCheck(color) )
                return PLAYER_IN_CHECKMATE;        
        } else {
        
            // TODO:
            // check for stalemate
            boolean playerHasValidMove = false;
            for ( ChessPiece piece : mChessPieces )
            {
                if ( piece.getColor() != color )
                    continue;
                if ( piece.hasValidMove() )
                {
                    playerHasValidMove = true;
                    break;
                }
            }
            
            if ( !playerHasValidMove )
                return PLAYER_IN_STALEMATE;
            
        }

        // if player wants to claim draw,
        //      check three fold repetition
        //      check last fifty moves

        /*
        return codes:
        PLAYER_OK
        PLAYER_IN_CHECK
        PLAYER_IN_CHECKMATE
        PLAYER_IN_STALEMATE
        PLAYER_CLAIMS_DRAW
        */
        
        if ( playerInCheck )
            return PLAYER_IN_CHECK;
        return PLAYER_OK;
    }
    
    
    
    // TODO: add validateMove(int,int,int,int) and/or (string) maybe?
    
    
    /* *************************************************
     * * * * Private Methods * * * 
     * *************************************************/

    /**
     * Assumes king is in check, and checks for checkmate
     * @param color color of player 
     * @return true if player cannot escape check
     */
    private boolean cannotEscapeCheck(int color)
    {
        // get the king
        ChessPiece king = getKing(color);
        int kingRank = king.getRank();
        int kingFile = king.getFile();
        
        // get checking piece(s)
        ChessPiece checkingPiece = null;
        boolean doubleCheck = false;

        for( ChessPiece piece : mChessPieces )
        {
            if ( !piece.mIsActive || piece.mColor == color ) // skip is inactive or same color
                continue;
            if ( piece.isObserving(kingRank, kingFile) == PIECE_IS_OBSERVING )
            {
                if ( checkingPiece == null )
                {
                    checkingPiece = piece;  // grab checking piece
                } else {
                    // it is double check
                    doubleCheck = true;
                    break;
                }
            }
        }
        
        if ( checkingPiece == null )
            return false;
        
        if ( !doubleCheck )
        {
            // if not double check, check for blocks or captures
            int cRank = checkingPiece.getRank();
            int cFile = checkingPiece.getFile();
            ArrayList<Square> interveningSquares 
                    = Square.getInterveningSquares(
                            kingRank, kingFile, cRank, cFile );
            
            // get intervening squares
            interveningSquares.add( new Square(cRank, cFile) );
            
            // check for odd case when en passant saves king from checkmage
            if ( checkingPiece.getType() == PAWN && checkingPiece.mMoveCount == 1 
                && ( ( color == WHITE && checkingPiece.getRank() == 4 ) 
                    || ( color == BLACK && checkingPiece.getRank() == 3 ) ) )
            {
                int enPassantRank = (color == WHITE) ? 5 : 2;
                if ( isValidCoord(enPassantRank,cFile) )
                    interveningSquares.add( new Square(enPassantRank,cFile) );
            }
            
            // check all pieces for captures or blocks
            for( ChessPiece piece : mChessPieces )
            {
                if ( !piece.mIsActive || piece.mColor != color ) // skip is inactive or same color
                    continue;
                for (Square square : interveningSquares )
                    if ( isMoveCodeLegal(piece.validateMove(square)) )
                        return false;
            }
        }
        //check for king moves
        return !king.hasValidMove();
    }
    
    /**
     * Adds a piece to the game (adds in the ArrayList mChessPieces)
     * */
    private ChessPiece addPieceToGame(int color, char type)
    {
        type = Character.toUpperCase(type);
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
    
    public static boolean isMoveCodeLegal(int code)
    {
        return code == MOVE_LEGAL
                || code == MOVE_LEGAL_EN_PASSANT
                || code == MOVE_LEGAL_CASTLE_KINGSIDE
                || code == MOVE_LEGAL_CASTLE_QUEENSIDE ;
    }
    
    public static String getGameStatusText(int code)
    {
        switch(code)
        {
            case STATUS_WHITES_TURN:
                return "White's turn.";
            case STATUS_BLACKS_TURN:
                return "Black's turn.";
            case STATUS_WHITE_IN_CHECK:
                return "White is in check!";
            case STATUS_BLACK_IN_CHECK:
                return "Black is in check!";
            case STATUS_WHITE_WINS_CHECKMATE:
                return "White wins by checkmate!";
            case STATUS_BLACK_WINS_CHECKMATE:
                return "Black wins by checkmate!";
            case STATUS_WHITE_WINS_TIME:
                return "White wins on time!";
            case STATUS_BLACK_WINS_TIME:
                return "Black wins on time!";
            case STATUS_WHITE_WINS_RESIGNATION:
                return "White wins by resignation.";
            case STATUS_BLACK_WINS_RESIGNATION:
                return "Black wins by resignation.";
            case STATUS_DRAW_WHITE_STALEMATE:
                return "White is stalemated!";
            case STATUS_DRAW_BLACK_STALEMATE:
                return "Black is stalemated!";
            case STATUS_DRAW_WHITE_CLAIMS_THREE:
                return "White claims drawy by three-fold repetition.";
            case STATUS_DRAW_BLACK_CLAIMS_THREE:
                return "Black claims drawy by three-fold repetition.";
            case STATUS_DRAW_WHITE_CLAIMS_FIFTY:
                return "White claims drawy by fifty move rule.";    
             case STATUS_DRAW_BLACK_CLAIMS_FIFTY:
                return "Black claims drawy by fifty move rule.";   
            case STATUS_DRAW_AGREEMENT:
                return "Draw by agreement.";    
            default:
                return "Unknown Game State.";   
        }
    }
    
    public static String getMoveCodeText(int code)
    {
        switch(code)
        {
            case MOVE_LEGAL:
                return "Move is legal.";
            case MOVE_LEGAL_EN_PASSANT:
                return "En Passant.";
            case MOVE_LEGAL_CASTLE_KINGSIDE:
                return "Move is legal. Castling Kingside.";
                case MOVE_LEGAL_CASTLE_QUEENSIDE:
                return "Move is legal. Castling Queenside.";
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
            case ILLEGAL_CASTLE_THROUGH_CHECK:
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
    
    public static boolean isValidCoord(int rank, int file)
    {
        return (rank >= 0 && rank <= 7
                && file >= 0 && file <= 7);
    }
    
    public static boolean isValidCoord(String square)
    {
        return isValidCoord(convertAlgebraicToInternalRank(square),
                convertAlgebraicToInternalFile(square) );
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
            return makeMove(Chess.convertAlgebraicToInternalRank(coord),
                    Chess.convertAlgebraicToInternalFile(coord) );
        }
        
        /**
         * Makes a move given a target square
         * @param square object representing target square
         * @return move code
         */
        public int makeMove(Square square)
        { return makeMove(square.rank, square.file); }
        
        /**
         * Validates a move given a target square
         * @param square object representing target square
         * @return move code
         */
        public int validateMove(Square square)
        { return validateMove(square.rank, square.file); }
        
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
            
           
            // DEBUG printout
            /*
            System.out.println("Executing ("
                    + getPosition()
                    + ") " + Chess.getName(mType) 
                    + " makeMove (ChessPiece) to " +
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
            
            
            // capture piece, if any
            ChessPiece captured = mChessBoard[rank][file];
            if ( captured != null )
                captured.captured();
            
            // hold onto last location
            int fromRank = mRank;
            int fromFile = mFile;
            
            // set the new position and update mChessBoard
            updateChessPiece(rank, file);
            
            // if we de-abstract this function, override this function
            // for king and pawn class to handle pawn promotions and castling
            // right here
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( mColor == WHITE ) ? BLACK : WHITE;
            int playerStateCode = checkPlayerState(opponentColor);
            boolean check = playerStateCode == PLAYER_IN_CHECK;
            boolean checkmate = playerStateCode == PLAYER_IN_CHECKMATE;
            
            // DEBUG
            /*
                System.out.println("playerStateCode: " + playerStateCode);
            if ( check )
                System.out.println(getColorString(opponentColor) +" is in Check!");
            if ( checkmate )
                System.out.println(getColorString(opponentColor) +" is in Checkmate!");
            // */ // END DEBUG
            
            // add move to mChessHistory (pass coordinates of previous square)
            mChessHistory.add(new RecordOfMove(
                    this, fromRank, fromFile,
                    captured, null, 
                    check, checkmate        ) );
            
            // TODO: call EndTurn()
            endTurn(playerStateCode);

            
            
            
            
 
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
            // TODO: potential optimization: track validated moves
            //       so moves don't need to be re-evaluated
            
            if ( !isValidCoord(rank, file) )
                throw new IllegalArgumentException("Invalid Coordinate");
            
            // square cannot be occupied by own piece
            if ( mChessBoard[rank][file] != null
                    && mChessBoard[rank][file].getColor() == mColor )
                return MOVE_ILLEGAL_SQUARE_OCCUPIED;
            
            // piece must be observing the square
            int isObservingCode = isObserving(rank,file);
            if ( isObservingCode != PIECE_IS_OBSERVING )
                return isObservingCode;
                        
            // cannot move into check:
            //      (1)update moving piece position, remove catpured piece if any
            //      (2)call isInCheck(mColor)
            //      (3)undo moving piece, undo removing captured piece
            
            int fromRank = getRank();  // save current rank
            int fromFile = getFile();  // and file
            
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
            // TODO: call chess piece listener function
            // DEBUG:
            System.out.println(this.getName()+ " has been captured!");
        }
                
        
        protected void setPosition(String coord)
        {
            setPosition(Chess.convertAlgebraicToInternalRank(coord),
                    Chess.convertAlgebraicToInternalFile(coord) );
        }
        
        protected void setPosition(int rank, int file)
        {
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
        
        protected void updateChessPiece(int rank, int file)
        {
            // TODO: call piece lisener update function
            
            // DEBUG:
            System.out.println("Calling updateChessPiece");
            
            // increment move counter
            mMoveCount++;
            updatePosition(rank, file);
        }
        
           /**
            * Returns true if the piece is observing (attacking) a square
            * @param rank value from 0-7
            * @param file value from 0-7
            * @return true or false
            */
        abstract public int isObserving(int rank, int file);
        
        /**
         * returns true if the piece has a valid move
         * @return true or false
         */
        public boolean hasValidMove()
        {
            for ( Square square : getCandidateMoves() )
                if ( isMoveCodeLegal( validateMove(square) ) )
                    return true;
            return false;
        }
        
        /**
         * Gets all valid moves for this piece
         * @return ArrayList of Square objects containing valid moves
         */
        public ArrayList<Square> getValidMoves()
        {
            ArrayList<Square> validMoves = new ArrayList<>();
            if ( mColor != mWhoseTurn ) 
                return validMoves;  //return empty if wrong turn
            ArrayList<Square> candidateMoves = getCandidateMoves();
            
            for (Square square : candidateMoves)
            {
                if ( isMoveCodeLegal( validateMove(square) ) )
                    validMoves.add(square);
            }
            return validMoves;
        }
        
        /**
         * Gets a list of squares the piece might be able to move to
         * @return ArrayList of Square objects containing candidate squares
         */
        abstract public ArrayList<Square> getCandidateMoves();
        
        // public get methods
        /**
         * Gets the position in algebraic coordinates
         * @return string
         */
        public String getPosition()
            { return Chess.convertInternalToAlgebraic(mRank, mFile); } 
        public int getRank() { return mRank; }
        public int getFile() { return mFile; }
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

        /**
         * Helper function to see if player intends to castle
         * @param rank inputed rank
         * @param file inputed file
         * @return true if player is trying to castle
         */
        private boolean isTryingToCastle(int rank, int file)
        {
            if ( mColor == WHITE && mRank != 0)
                return false;
            if ( mColor == BLACK && mRank != 7 ) 
                return false;
            if ( mRank != rank )
                return false;
            if ( mChessBoard[rank][file] != null && 
                 mChessBoard[rank][file].getColor() != mColor )
                return false;
            if ( abs(mFile - file) == 2 )
                return true;
            return ( mChessBoard[rank][file] != null && 
                     mChessBoard[rank][file].getType() == ROOK );
        }
                
        @Override
        public int validateMove(int rank, int file)
        {
            if ( isTryingToCastle(rank,file) )
                return validateCastle(rank,file);
            else
                return super.validateMove(rank, file);
        }

        @Override
        public int makeMove(int rank, int file) 
        {
            if ( !isTryingToCastle(rank, file) )
                return super.makeMove(rank, file);
            return tryToCastle(rank,file);
        }
        
        /**
         * Attempts to castle if legal
         * @param rank inputed rank
         * @param file inputed file
         * @return MOVE_LEGAL_CASTLE if successful, otherwise returns
         *         an error code
         */
        private int tryToCastle(int rank, int file)
        {
            // DEBUG printout
            //System.out.println("Executing Castle to " +

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
            int code = validateCastle(rank, file);
            if ( code != MOVE_LEGAL_CASTLE_KINGSIDE &&
                    code != MOVE_LEGAL_CASTLE_QUEENSIDE ) return code;
     
            // TODO: switch timer
            
            // get rook
            ChessPiece castlingRook = getCastlingRook(rank,file);

            int fromRank = mRank;
            int fromFile = mFile;
            int fromRookRank = castlingRook.getRank();
            int fromRookFile = castlingRook.getFile();

            // castling which way
            boolean isCastlingKingside = fromRookFile > fromFile;
            
            int toKingFile = isCastlingKingside ? 6 : 2;
            int toRookFile = isCastlingKingside ? 5 : 3;
            

            // update king and rook
            updateChessPiece(fromRank, toKingFile);
            castlingRook.updateChessPiece(fromRookRank, toRookFile);
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( mColor == WHITE ) ? BLACK : WHITE;
            int playerStateCode = checkPlayerState(opponentColor);
            boolean check = playerStateCode == PLAYER_IN_CHECK;
            boolean checkmate = playerStateCode == PLAYER_IN_CHECKMATE;
            
             //  change this call
            mChessHistory.add(new RecordOfMove(
                    this, fromRank, fromFile,
                    castlingRook, fromRookRank, fromRookFile,
                    check, checkmate        ) );

            
            endTurn(playerStateCode);
            
            return code;
        }
        
        private ChessPiece getCastlingRook(int toRank, int toFile)
        {
            // returns the rook to castle with
            int kingFile = getFile();
            int sign = Integer.signum( toFile - kingFile );
            
            for ( int i = kingFile + sign; (i >= 0 && i < 8); i += sign )
            {
                if ( mChessBoard[toRank][i] != null && 
                        mChessBoard[toRank][i].getType() == ROOK )
                    return mChessBoard[toRank][i];
            }
            return null;
        }
        
        
        /**
         * Checks if king can castle, 
         * assuming isTryingToCastle(rank,file) returned true
         * @param rank
         * @param file
         * @return MOVE_LEGAL_CASTLE_KINGSIDE or MOVE_LEGAL_CASTLE_QUEENSIDE
         *         if castling is allowed, otherwise returns an error code
         */
        private int validateCastle(int rank, int file)
        {
            // has king already moved?
            if ( mMoveCount != 0 )
                return ILLEGAL_CASTLE_KING_HAS_MOVED;
            
            // get the rook
            ChessPiece castlingRook = getCastlingRook(rank,file);
            if ( !castlingRook.mIsActive )
                return MOVE_ILLEGAL;
            
            // rook cannot have made a move already
            if ( castlingRook.mMoveCount != 0 )
                return ILLEGAL_CASTLE_ROOK_HAS_MOVED;
            
            // check if impeded
            int rookFile = castlingRook.getFile();
            int kingFile = getFile();
            
            boolean isCastlingKingside = rookFile > kingFile;
            
            int toKingFile = isCastlingKingside ? 6 : 2;
            int toRookFile = isCastlingKingside ? 5 : 3;
            
            
            // check for impeded
            int[] fileList = {rookFile,kingFile,toKingFile,toRookFile};
            int minFile = toKingFile;
            int maxFile = toKingFile;
            for (int i = 0; i < fileList.length; i++)
            {
                minFile = fileList[i] < minFile ? fileList[i] : minFile;
                maxFile = fileList[i] > maxFile ? fileList[i] : maxFile;
            }
            
            for (int i = minFile; i <= maxFile; i++)
            {
                // DEBUG
                //System.out.println("checking impeded, file: " + i);
                ChessPiece square = mChessBoard[rank][i];
                if ( square == null )
                    continue;
                if ( square != this && square != castlingRook )
                    return ILLEGAL_CASTLE_IMPEDED;
            }
                        
            
            
            // temporarily deactive rook
            if ( mChessBoard[rank][rookFile] != castlingRook)
                return MOVE_ILLEGAL;
            mChessBoard[rank][rookFile] = null;
            
            // check for checks
            minFile = isCastlingKingside ? kingFile : toKingFile;
            maxFile = isCastlingKingside ? toKingFile : kingFile;
            boolean throughCheck = false;
            for (int i = minFile; i <= maxFile; i++)
            {
                //System.out.println("checking for check, file: " + i);
                if ( isInCheck(mColor,rank,i) )
                {
                    throughCheck = true;
                    break;
                }                
            }
            
            // re-activate rook
            mChessBoard[rank][rookFile] = castlingRook;
            
            if ( throughCheck )
                return ILLEGAL_CASTLE_THROUGH_CHECK;
            
            return isCastlingKingside ? 
                    MOVE_LEGAL_CASTLE_KINGSIDE : MOVE_LEGAL_CASTLE_QUEENSIDE;
            
            
            //      cannot castle out of check
            //      cannot castle through or into check        
        };
        

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

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = new ArrayList<>();
            for (int i = mRank-1; i <= mRank+1; i++)
                for (int j = mFile-1; j <= mFile+1; j++)
                {
                    if ( i == mRank && j == mFile )
                        continue;
                    if ( isValidCoord(i,j) )
                        returnList.add(new Square(i,j));
                }
            // if move count is 0, add castle candidate move
            if ( mMoveCount == 0)
            {
                if ( mFile == 4 )   // standard king starting position
                {
                    returnList.add(new Square(mRank,6));
                    returnList.add(new Square(mRank,2));
                } else {
                    // TODO: make castling discoverable in Chess960 
                }
            }
            
            return returnList;
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

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = Square.getDiagonals(mRank,mFile);
            returnList.addAll( Square.getFile(mFile) );
            returnList.addAll( Square.getRank(mRank) );
            return returnList;            
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

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = Square.getFile(mFile);
            returnList.addAll( Square.getRank(mRank) );
            return returnList;  
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

        @Override
        public ArrayList<Square> getCandidateMoves()
        { return Square.getDiagonals(mRank, mFile); }
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

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = new ArrayList<>();
            for (int rankStep = 1; rankStep <= 2; rankStep++)
                for (int rankDir = -1; rankDir <= 1; rankDir += 2)
                    for (int fileDir = -1; fileDir <= 1; fileDir += 2)
                    {
                        int fileStep = ( rankStep == 1 ) ? 2 : 1;
                        int rank = mRank + rankDir*rankStep;
                        int file = mFile + fileDir*fileStep;
                        if ( isValidCoord(rank,file) )
                            returnList.add(new Square(rank,file));                        
                    }
            return returnList;
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
        public int makeMove(String coord)
        {
            char promoType = ' ';
            if ( coord.length() >= 4 )
                promoType = coord.charAt(3);
            return makeMove(Chess.convertAlgebraicToInternalRank(coord),
                    Chess.convertAlgebraicToInternalFile(coord), promoType );
        }
        
        @Override
        public int makeMove(int rank, int file)
        {
            return makeMove(rank, file, ' ' );
        }
        
        @Override
        public int makeMove(int rank, int file, char promotionType)
        {
            // the pawn business
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
            if ( code != MOVE_LEGAL && code != MOVE_LEGAL_EN_PASSANT ) return code;
            
            // switch timer
            
            ChessPiece promotion = null;
            // check for promotion
            if ( (mColor == WHITE && rank == 7 ) 
                    || (mColor == BLACK && rank == 0 ) )
            {
                // get ready to promote!
                if ( !isValidPromotionType(promotionType) )
                    return AMBIGUOUS_PROMOTION; // not so fast
                promotion = addPieceToGame(mColor,promotionType);
            }
            
             //  implement en passant 
            // capture piece, if any
            ChessPiece captured;
            if ( code == MOVE_LEGAL_EN_PASSANT )
                captured = mChessBoard[mRank][file];
            else
                captured = mChessBoard[rank][file];
            if ( captured != null )
                captured.captured();
            
            // hold onto last location
            int fromRank = mRank;
            int fromFile = mFile;
            
            // set the new position and update mChessBoard
            updateChessPiece(rank, file);
            
            if ( promotion != null )
            {
                mIsActive = false;
                mStatus = PIECE_PROMOTED;
                mChessBoard[rank][file] = null;
                promotion.setPosition(rank, file);
            }
            // add promoted piece at rank,file, if needed
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( mColor == WHITE ) ? BLACK : WHITE;
            int playerStateCode = checkPlayerState(opponentColor);
            boolean check = playerStateCode == PLAYER_IN_CHECK;
            boolean checkmate = playerStateCode == PLAYER_IN_CHECKMATE;
            
            
            
            
            //  pass promotion reference
            // add move to mChessHistory (pass coordinates of previous square)
            mChessHistory.add(new RecordOfMove(
                    this, fromRank, fromFile,
                    captured, promotion, 
                    check, checkmate        ) );
            
            
            endTurn(playerStateCode);
            
            return code;
        }
        
        // validation helper function
        public boolean isValidPromotionType(char promotionType)
        {
            promotionType = Character.toUpperCase(promotionType);
            return promotionType == QUEEN
                    || promotionType == BISHOP
                    || promotionType == KNIGHT
                    || promotionType == ROOK;
                    
        }
        
        
        @Override
        public int validateMove(int rank, int file) 
        {
            if ( !isValidCoord(rank, file) )
                throw new IllegalArgumentException("Invalid Coordinate");
            
            // square cannot be occupied by own piece
            if ( mChessBoard[rank][file] != null
                    && mChessBoard[rank][file].getColor() == mColor )
                return MOVE_ILLEGAL_SQUARE_OCCUPIED;
            
            
            int direction = (mColor == WHITE) ? 1 : -1;
            ChessPiece captured = null;
            boolean enPassant = false;
            if ( (mFile == file) && (mRank + direction == rank ) )  
            {
                // moving forward one square
                if ( mChessBoard[rank][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
            } else if ( (mFile == file) && (mRank + 2*direction == rank ) ) { 
                // moving forward two squares
                if ( mColor ==  WHITE && mRank != 1 )
                    return MOVE_ILLEGAL;
                if ( mColor ==  BLACK && mRank != 6 )
                    return MOVE_ILLEGAL;
                if ( mChessBoard[mRank + direction][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( mChessBoard[rank][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( mMoveCount != 0 )
                    return MOVE_ILLEGAL_PAWN_HAS_MOVED;
                
            } else if ( (abs(mFile - file) == 1) && (mRank + direction == rank) ) {
                // capturing a piece
                captured = mChessBoard[rank][file];
                if ( mChessBoard[rank][file] == null )
                {
                    // nothing to capture unless en passant is possible
                    if ( mChessBoard[mRank][file] == null ) // en passant square is empty
                        return MOVE_ILLEGAL_NOTHING_TO_CAPTURE;
                    // check if we are on white's 5th rank
                    if ( mColor == WHITE && mRank != 4 )
                        return MOVE_ILLEGAL;
                    // check if we are on black's 5th rank
                    if ( mColor == BLACK && mRank != 3 )
                        return MOVE_ILLEGAL;
                    if (  mChessBoard[mRank][file].getColor() == mColor )
                        return MOVE_ILLEGAL;
                    if ( mChessBoard[mRank][file].getType() != PAWN )
                        return MOVE_ILLEGAL;
                    ChessPiece neighborPawn = mChessBoard[mRank][file];
                    RecordOfMove lastMove = mChessHistory.get( mChessHistory.size() - 1 );
                    if ( lastMove.PieceMoved != neighborPawn )
                        return MOVE_ILLEGAL_LATE_EN_PASSANT;
                    if ( lastMove.fromRank != mRank + 2*direction )
                        return MOVE_ILLEGAL_LATE_EN_PASSANT;
                    captured = neighborPawn;
                    enPassant = true;
                        
                    
                    //if ( mChessBoard[mRank][file].getCol
                    //or && mChessBoard[mRank][file].getType )
                    // if en passant square is an enemy pawn, 
                    //and its just moved two square, E.P. is OK
                    
                } else if ( mChessBoard[rank][file].getColor() == mColor ) {
                    return MOVE_ILLEGAL_SQUARE_OCCUPIED;
                }
            } else {
                return MOVE_ILLEGAL;
            }

            
            //  cange to mRank and mFile
            int fromRank = mRank;  // save current rank 
            int fromFile = mFile;  // and file
            //int fromRank = getPositionInternalRank();  // save current rank 
            //int fromFile = getPositionInternalFile();  // and file
            
            // cannot move into check:
            //      (1)update moving piece position, remove catpured piece if any
            //      (2)call isInCheck(mColor)
            //      (3)undo moving piece, undo removing captured piece
            // (1) get piece to be captured, if any
            //ChessPiece captured = mChessBoard[rank][file];
            if ( captured != null )
            {
                captured.mIsActive = false;     // temporarily deactivate
                if ( enPassant )
                    mChessBoard[fromRank][file] = null;
            }
            updatePosition(rank,file);   // (1)temporarily move the piece
            
            boolean isInCheck = isInCheck(mColor);  // (2)
            
            // undo temporary move (3)
            updatePosition(fromRank, fromFile);
            if ( captured != null )
            {
                captured.mIsActive = true;
                if ( enPassant )
                    mChessBoard[fromRank][file] = captured;
                else
                    mChessBoard[rank][file] = captured;
                    
            }
            
            if ( isInCheck )
                return MOVE_ILLEGAL_KING_IN_CHECK;
            
            if ( enPassant )
                return MOVE_LEGAL_EN_PASSANT;
            else
                return MOVE_LEGAL; 
        }
        
        @Override
        public int isObserving(int rank, int file)
        {
            int direction = (mColor == WHITE) ? 1 : -1;
            if ( (mRank + direction == rank) &&
                    abs(mFile - file) == 1 )
                return PIECE_IS_OBSERVING;
            return MOVE_ILLEGAL;
        }

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = new ArrayList<>();
            int direction = (mColor == WHITE) ? 1 : -1;
            int rank = mRank + direction;
            for (int file = mFile - 1; file <= mFile + 1; file++)
            {
                if ( isValidCoord(rank,file) )
                    returnList.add(new Square(rank,file));
            }
            int rank2 = mRank + 2 * direction;
            if (mMoveCount == 0 && isValidCoord(rank2,mFile))
                returnList.add(new Square(rank2,mFile));
            return returnList;
        }
    }
            
    

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
        
        final public boolean checkmate;
        
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
            toRank = PieceMoved.getRank();
            toFile = PieceMoved.getFile();
            
            PieceCaptured = captured;
            if ( PieceCaptured != null )
            {
                capturedRank = moved.getRank();
                capturedFile = moved.getFile();
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
            
            this.checkmate = checkmate;
            
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
            toRank = PieceMoved.getRank();
            toFile = PieceMoved.getFile();

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
            
            this.checkmate = checkmate;
            
            moveNumber = getMoveNumber();
            if ( mWhoseTurn == WHITE )
                movePrefix = String.valueOf(moveNumber) + ". ";
            else
                movePrefix = String.valueOf(moveNumber) + "... ";
            
            if ( fromRookFile < movedFromFile  )
                moveText = " 0-0-0";
            else
                moveText = "   0-0";
        }
    }
    
    /********************************************************
     ******************************************************** 
     *********************************************************/
    /**
     * Square class for methods that return list of squares,
     *         ArrayList of Square objects
     */
    public static class Square
    {
        public final int rank;
        public final int file;
        
        public Square(int rank, int file)
        {
            if ( !isValidCoord(rank,file))
                throw new IllegalArgumentException("new Square called with invalid coordinate");
            this.rank = rank;
            this.file = file;
        }
        
        @Override
        public String toString()
        { return Chess.convertInternalToAlgebraic(rank, file); }
        
        public boolean isEqual(Square square)
        { return this.rank == square.rank && this.file == square.file; }
        
        public static boolean isEqual(Square square1, Square square2)
        { return square1.rank == square2.rank && square1.file == square2.file; }
        
        // Static helper methods
        public static ArrayList<Square> getDiagonals(Square square)
        { return getDiagonals(square.rank, square.file); }
        
        public static ArrayList<Square> getDiagonals(int rank, int file)
        {
            ArrayList<Square> returnList = new ArrayList<>();
            
            for (int dRank = -1; dRank <= 1; dRank += 2  )
                for (int dFile = -1; dFile <= 1; dFile += 2  )
                    for (int i = 1; i < 8; i++)
                    {
                        int newRank = rank + i * dRank;
                        int newFile = file + i * dFile;
                        if (!isValidCoord(newRank,newFile))
                            break;
                        returnList.add(new Square(newRank,newFile));
                    }
            return returnList;
        }
        
        public static ArrayList<Square> getFile(int file)
        {
            ArrayList<Square> returnList = new ArrayList<>();
            if ( file < 0 || file > 7 )
                return returnList;  //return empty
            for (int i = 0; i < 8; i++)
                returnList.add(new Square(i,file));
            return returnList;
        }
        
        public static ArrayList<Square> getRank(int rank)
        {
            ArrayList<Square> returnList = new ArrayList<>();
            if ( rank < 0 || rank > 7 )
                return returnList;  //return empty
            for (int i = 0; i < 8; i++)
                returnList.add(new Square(rank,i));
            return returnList;
        }
        
        public static ArrayList<Square> getInterveningSquares(
                int rank1, int file1, int rank2, int file2 )
        {
            ArrayList<Square> returnList = new ArrayList<>();
            if ( !isValidCoord(rank1,file1) || !isValidCoord(rank2,file2) )
                return returnList;
            int rankDif = rank2 - rank1;
            int fileDif = file2 - file1;
            int rankDir = rankDif > 0 ? 1 : -1;
            int fileDir = fileDif > 0 ? 1 : -1;
            
            if ( rank1 == rank2 ) {
                for ( int file = file1; file != file2; file += fileDir)
                    returnList.add(new Square(rank1,file));
            } else if ( file1 == file2 ) {
                for ( int rank = rank1; rank != rank2; rank += rankDir)
                    returnList.add(new Square(rank,file1));
            } else if ( abs( (double)(rankDif) / (double)(fileDif) ) == 1.0 ) {
                int file = file1;
                for ( int rank = rank1; rank != rank2; rank += rankDir)
                {
                    //add
                    returnList.add(new Square(rank,file));
                    file += fileDir;
                }
            }
            return returnList;            
        }
    }
    
    public static interface GameListener
    {
        abstract public void onGameStateUpdate( int GameStateCode );
    }
}
