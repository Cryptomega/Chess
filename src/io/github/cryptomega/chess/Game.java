/*
 * 
 */
package io.github.cryptomega.chess;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
//import org.springframework.util.StopWatch;


/**
 *  Chess
 *  after instantiating an instance of Chess, start a game by calling
 *  setupGame() and startGame(). 
 * @author Philip Schexnayder
 */
public class Game
{
    /* ****************************************
     * * * * Declare constants * * *
     * ****************************************/
    // Game Colors
    public static final int BLACK =  0;
    public static final int WHITE =  1;
    public static final int NONE  = -1;
    
    public static final int BOARD_NUMBER_RANKS = 8;
    public static final int BOARD_NUMBER_FILES = 8;
    
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
    
    // * * * Game states * * *
    public static final int STATUS_WHITES_TURN =             800; // Game in Progress
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
    public static final int STATUS_DRAW_BLACK_CLAIMS_THREE = 811; // TODO:
    public static final int STATUS_DRAW_WHITE_CLAIMS_FIFTY = 812; // TODO:
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
    private boolean mWhiteOffersOrClaimsDraw;
    private boolean mBlackOffersOrClaimsDraw;
    private double mWhiteTimeLeft;  // time left in seconds
    private double mBlackTimeLeft;  // time left in seconds
    
    // useful index tracker variables
    private int mWhiteKingIndex = -1;
    private int mBlackKingIndex = -1;
    
    // Game variables, options and preferences. Do not need to be reset between games
    private int mStartingMinutes = 10;
    private int mOnMoveIncrementSeconds = 5;
    private boolean mIsTimedGame = false;
    //private boolean mUseStandardTimer = true;
    private TimerController mTimer = null;
    //private boolean mIsChess960 = false;
    
    
    
    /**
     * Makes end of turn game state updates, as well as check
     * for end of game conditions. 
     * @param nextPlayerState contains the player state code of the
     *          player who's turn is about to begin. Alternatively 
     *          it can take a Game State code 
     */
    private void endTurn(int nextPlayerState)
    {
        // DEBUG
        //System.out.println("endTurn called");

        // updates and state variables
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
            case STATUS_WHITE_WINS_TIME:
                mGameState = STATUS_WHITE_WINS_TIME;
                mIsGameActive = false;
                break;
            case STATUS_BLACK_WINS_TIME:
                mGameState = STATUS_BLACK_WINS_TIME;
                mIsGameActive = false;
                break;
            case STATUS_WHITE_WINS_RESIGNATION:
                mGameState = STATUS_WHITE_WINS_RESIGNATION;
                mIsGameActive = false;
                break;
            case STATUS_BLACK_WINS_RESIGNATION:
                mGameState = STATUS_BLACK_WINS_RESIGNATION;
                mIsGameActive = false;
                break;
            case STATUS_DRAW_AGREEMENT:
                mGameState = STATUS_DRAW_AGREEMENT;
                mIsGameActive = false;
                break;
        }
        
        // TODO: implement more game state checks

        // transitions turn to other player
        mWhoseTurn = (mWhoseTurn == WHITE) ? BLACK : WHITE;
        mTurnCount++;
        
        // switch over clock
        if( mIsTimedGame && mTimer != null )
        {
            if ( mIsGameActive )
                mTimer.switchTimer();
            else
                mTimer.stopTimer();
        }
         
        // TODO: implement draws claim check
        // checkForDraw();
        //      if player wants to claim draw,
        //      check three fold repetition
        //      check last fifty moves

        // checks for draw conditions
        // updates game state for wins, draws

        // ONLY METHOD WHICH UPDATES GAME STATE VARIABLES
        
        
        // reset draw flag as player's turn is starting, after draw claim checked
        if ( mWhoseTurn == WHITE )
            mWhiteOffersOrClaimsDraw = false;
        else
            mBlackOffersOrClaimsDraw = false;
        
        // calls game state listeners
        if ( mIsGameActive )
            pushGameStateUpdate();
        else
            pushGameOverUpdate();
    }
    
    
    /************************************************
     * The Chess Board -  This board using an internal
     * coordinate system, [rank][file]
     * a1 -> [0][0], a2 -> [0][1], b1 -> [1][0], etc.
     * Cells contain reference to the occupying 
     * chess piece, or null if the square is empty.
     ***********************************************/
    private final ChessPiece[][] mChessBoard =
            new ChessPiece[BOARD_NUMBER_RANKS][BOARD_NUMBER_FILES];
    
    
    /* *************************************************
     * * * * ArrayList of all chess pieces * * * 
     * *************************************************/
    private final ArrayList<ChessPiece> mChessPieces; 
    
    
    /* *************************************************
     * * * * History ArrayList * * * 
     * ************************************************/
    private final ArrayList<RecordOfMove> mChessHistory;
    
    
    // Game State Listeners
    private ArrayList<GameListener> mGameStateListeners;
    
    /** ************************************************
     * * * * Constructor * * * 
     * Initiates a new game. 
     *    chess.setupStandardGame();
     *    chess.setStartTime(30, 10);
     *    chess.startGame();
     * *************************************************/
    public Game()
    {
        mChessPieces = new ArrayList<>();
        mChessHistory = new ArrayList<>();
        mGameStateListeners = new ArrayList<>();
        
        clearGame(); 
    }
    
    /**
     * Returns a full independent copy of a Game instance.
     * Timer and listeners are not copied.
     * @param orig original game
     * @return 
     */
    public static Game copyGame(Game orig)
    { return new Game(orig, false); }
    
    /**
     * Returns a full copy of a Game instance.
     * Steals Game and Piece listeners from original to take over display.
     * The original game can later call refreshListeners() to take back display.
     * @param orig Original game instance
     * @return a new game instance in same position and state as orig
     */
    public static Game copyGameAndStealListeners(Game orig)
    { return new Game(orig, true); }
    
    /**
     * Default copy constructor. Returns a complete,
     * independent copy of the Game object passed.
     * Copied game will have time disabled and no listeners
     * @param orig original game instance to copy
     */
    public Game(Game orig)
    { this(orig, false); }
    
    /**
     * Creates a full independent copy of a Game instance. 
     * @param origGame original Game instance to copy.
     * @param stealListeners if true game listeners belonging to
     *         origGame will be added to the copied game as well.
     *         The listeners can be refreshed from origGame to regain
     *         control.
     */
    public Game(Game origGame, boolean stealListeners)
    {   // do some cool stuff
        // Copy game state variables
        this.mIsGameActive = origGame.mIsGameActive;
        this.mWhoseTurn  = origGame.mWhoseTurn;
        this.mGameState  = origGame.mGameState;
        this.mTurnCount  = origGame.mTurnCount;
        this.mWhiteOffersOrClaimsDraw  = origGame.mWhiteOffersOrClaimsDraw;
        this.mBlackOffersOrClaimsDraw  = origGame.mBlackOffersOrClaimsDraw;
        this.mWhiteTimeLeft  = origGame.mWhiteTimeLeft;  // time left in seconds
        this.mBlackTimeLeft  = origGame.mBlackTimeLeft;  // time left in seconds
        
        // initialize lists
        this.mChessPieces = new ArrayList<>();
        this.mChessHistory = new ArrayList<>();
        this.mGameStateListeners = new ArrayList<>();
        this.clearBoard();
        
        // create a HashMap
        HashMap<ChessPiece,ChessPiece> hashmap = new HashMap<>();
        hashmap.put(null, null); // empty squares and null references get mapped to null
        
        // copy pieces with reference hashmap
        for ( ChessPiece origPiece : origGame.getPieces() )
        {
            ChessPiece newPiece = this.copyPiece( origPiece );
            this.mChessPieces.add( newPiece );
            
            hashmap.put(origPiece, newPiece);  // make the hash map
            
            if ( stealListeners )
                newPiece.mPieceListeners = origPiece.mPieceListeners;
        }
        
        // copy board with references mapped
        for (int r = 0; r < BOARD_NUMBER_RANKS; r++)
            for (int f = 0; f < BOARD_NUMBER_FILES; f++)
            {
                // TODO: add (null, null) to hashmap and eliminate this if branch
                this.mChessBoard[r][f] = hashmap.get(origGame.mChessBoard[r][f] );
            }
        
        // copy history with references mapped
        for ( RecordOfMove origRecord : origGame.mChessHistory )
        {
            //if
            this.mChessHistory.add( new RecordOfMove(origRecord, hashmap) );
        }

        
        // disregard timer
        this.mIsTimedGame = false;
        this.mTimer = null;
        
        // disregard listeners unless stealListeners = true
        if ( stealListeners )
            this.mGameStateListeners = origGame.mGameStateListeners;
        
        //throw new UnsupportedOperationException("COY CONSTRUCTOR!");
    }
    
        
        private ChessPiece copyPiece(ChessPiece orig)
        {
            ChessPiece newPiece;
            char type = orig.getType();
            switch (type)
            {
                case KING:
                    newPiece = new King(orig);
                    break;
                case QUEEN:
                    newPiece = new Queen(orig);
                    break;
                case BISHOP:
                    newPiece = new Bishop(orig);
                    break;
                case KNIGHT:
                    newPiece = new Knight(orig);
                    break;
                case ROOK:
                    newPiece = new Rook(orig);
                    break;
                case PAWN:
                    newPiece = new Pawn(orig);
                    break;
                default:
                    throw new IllegalArgumentException("Invalid piece type argument"); 
            }
            return newPiece;
        }
        
        /**
         * Pushes an update on all game state listeners
         * and all piece listeners
         */
        public void refreshListeners()
        {
            // game state listeners
            if ( mGameStateListeners != null )
                for (  GameListener listener : mGameStateListeners )
                    listener.onGameStateUpdate( new GameStateUpdate() );
            
            // piece listeners
            for ( ChessPiece piece : this.mChessPieces )
                if ( piece.mPieceListeners != null )
                    for ( PieceListener listener : piece.mPieceListeners )
                        listener.onUpdate( piece );
                        
        }
        
        
    /* *************************************************
     * * * * Public Methods * * * 
     * *************************************************/
    
    /**
     * @return true if game is active. Use startGame() to activate
     */
    public boolean isGameActive() { return mIsGameActive; }
    public int whoseTurn() { return mWhoseTurn; }
    public int getMoveNumber() { return (2 + mTurnCount) / 2; }
    
    public double getSecondsRemaining(int color)
    { return ( color ==  WHITE ) ? mWhiteTimeLeft : mBlackTimeLeft; }
    
    public String getGameStatus() { return getGameStatusText(mGameState); }
    
    /**
     * Gets the winner of the game, if any
     * @return White, Black, or None
     */
    public String getWinner()
    {   switch(mGameState)
        {
            case STATUS_WHITE_WINS_CHECKMATE:
            case STATUS_WHITE_WINS_RESIGNATION:
            case STATUS_WHITE_WINS_TIME:
                return "White";
            case STATUS_BLACK_WINS_CHECKMATE:
            case STATUS_BLACK_WINS_RESIGNATION:
            case STATUS_BLACK_WINS_TIME:
                return "Black";
            default:
                return "None";
        }
    }
    
    /**
     * Get the chess board with references to the active pieces on it
     * @return Returns a 2d array of ChessPiece
     */
    public ChessPiece[][] getBoard()
        { 
            ChessPiece[][] copy = new ChessPiece[BOARD_NUMBER_RANKS][BOARD_NUMBER_FILES];
            for (int i = 0; i < BOARD_NUMBER_RANKS; i++)
                copy[i] = mChessBoard[i].clone();
            return copy;
        } 
    
    /**
     * Gets all the ChessPiece references in an ArrayList
     * @return ArrayList containing references to the pieces
     */
    public ArrayList<ChessPiece> getPieces()
        { return (ArrayList<ChessPiece>) mChessPieces.clone(); }
    
    
    
    /**
     * Adds a game state listener.
     * @param listener implements GameListener interface
     */
    public void addGameStateListener(GameListener listener)
    { mGameStateListeners.add(listener); }
    

    
    /**
     * Gets the move history 
     * @return a String containing all the moves, 
     *         one line per turn
     */
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
        if ( mIsGameActive == true )
            throw new IllegalStateException("Cannot clear game while game is active");
        
        resetGameVariables(); // reset game state variables
        initTimer();          // initialize timer
        clearBoard();         // clear game board
        clearHistory();       // clear history
        
        // clear pieces array
        if ( mChessPieces != null && !mChessPieces.isEmpty() )
            mChessPieces.clear();
        mWhiteKingIndex = -1;
        mBlackKingIndex = -1;
    }
    
    /**
     * Does a hard reset, ignoring mGameState.
     * GUI might want to confirm action before 
     * calling this.
     */
    public void restartGame() 
    {
        endGame();
        resetGame();
        startGame();
    }
    
    /**
     * Resets the game, so it can be restarted with startGame()
     */
    public void resetGame()
    {
        if ( mIsGameActive == true )
            throw new IllegalStateException("Cannot clear game while game is active");
        
        resetGameVariables(); // reset game state variables
        initTimer();          // initialize timer
        clearBoard();         // clear game board
        clearHistory();       // clear history
        resetPieces();  // reset the pieces
    }
    

    /**
     * Sets up the pieces on the board for a 
     * standard game. Call startGame() to begin!
     */
    public void setupStandardGame()
    {
        // require mIsGameActive to be false 
        if ( mIsGameActive == true )
            throw new IllegalStateException("Cannot setup game while game is active");
        
        

        addPieceToGame(WHITE, KING, 0, 4 );
        addPieceToGame(WHITE, QUEEN, 0, 3 );
        addPieceToGame(WHITE, BISHOP, 0, 2 );
        addPieceToGame(WHITE, KNIGHT, 0, 1 );
        addPieceToGame(WHITE, ROOK, 0, 0 );
        addPieceToGame(WHITE, BISHOP, 0, 5 );
        addPieceToGame(WHITE, KNIGHT, 0, 6 );
        addPieceToGame(WHITE, ROOK, 0, 7 );
        for ( int i =0; i<BOARD_NUMBER_FILES; i++)
            addPieceToGame(WHITE, PAWN, 1, i);
        
        
        addPieceToGame(BLACK, KING, 7, 4 );
        addPieceToGame(BLACK, QUEEN, 7, 3 );
        addPieceToGame(BLACK, BISHOP, 7, 2 );
        addPieceToGame(BLACK, KNIGHT, 7, 1 );
        addPieceToGame(BLACK, ROOK, 7, 0 );
        addPieceToGame(BLACK, BISHOP, 7, 5 );
        addPieceToGame(BLACK, KNIGHT, 7, 6 );
        addPieceToGame(BLACK, ROOK, 7, 7 );
        for ( int i =0; i<BOARD_NUMBER_FILES; i++)
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
     * Manually end the game
     */
    public void endGame()
    {
        mIsGameActive = false;
        
        if ( mTimer != null )
            mTimer.stopTimer();
    }
    
    /**
     * Sets the starting time. passing 0,0 disables the timer
     * @param startingMins
     * @param incrementSecs 
     */
    public void setStartTime(int startingMins, int incrementSecs)
    {
        if ( mIsGameActive )
            return; // cannot change if game is in progress
        
        mStartingMinutes = startingMins;
        mOnMoveIncrementSeconds = incrementSecs;
        mWhiteTimeLeft = (double)mStartingMinutes*60.0;
        mBlackTimeLeft = (double)mStartingMinutes*60.0;
        mIsTimedGame = !(startingMins == 0 && incrementSecs == 0);
        
        // initialize timer
        initTimer();
    }
    

    /**
     * Change the default timer
     * @param timer a timer object which implements Chess.TimerController
     */
    public void setGameTimer(TimerController timer)
    {   
        if ( mIsGameActive )
            return; // cannot change timer while game is active
        
        if ( this.mTimer != null )   // in case a timer is already running
            this.mTimer.stopTimer(); // for some reason
        
        this.mTimer = timer; 
    }
    
    /**
     * Currently active player resigns
     */
    public void resign()
    { resign(mWhoseTurn); }
    
    /**
     * Player resigns
     * @param player color of resigning player
     */
    public void resign(int player)
    {  
        if ( player == WHITE )
            endTurn(STATUS_BLACK_WINS_RESIGNATION);
        else if ( player == BLACK )
            endTurn(STATUS_WHITE_WINS_RESIGNATION); 
    }
    
    /**
     * The current player offers/claims a draw
     */
    public void draw()
    { draw( mWhoseTurn ); }
    
    /**
     * Player offers/claims a draw
     * @param player color of offering player
     */
    public void draw(int player)
    {
        if ( player == WHITE ) {
            mWhiteOffersOrClaimsDraw = true;
            if ( mBlackOffersOrClaimsDraw )
                endTurn(STATUS_DRAW_AGREEMENT);
        } else if ( player == BLACK ) {
            mBlackOffersOrClaimsDraw = true;
            if ( mWhiteOffersOrClaimsDraw )
                endTurn(STATUS_DRAW_AGREEMENT);
        }
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
        int fromRank = Game.convertAlgebraicToInternalRank(from);
        int fromFile = Game.convertAlgebraicToInternalFile(from);
        int toRank = Game.convertAlgebraicToInternalRank(to);
        int toFile = Game.convertAlgebraicToInternalFile(to);
        
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
        // check for check, checkmate, stalemate, or draw
        boolean playerInCheck = isInCheck(color);
        if ( playerInCheck )
        {
            // check for checkmate
            if ( cannotEscapeCheck(color) )
                return PLAYER_IN_CHECKMATE;        
        } else {
        
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
        if ( playerInCheck )
            return PLAYER_IN_CHECK;
        return PLAYER_OK;
    }
    
    
    /**
     * Method called by game timer to update the remaining timer
     * @param playerColor color of player to upgrade time
     * @param timeRemaining remaining time in seconds
     */
    public void updateTimer(int playerColor, double timeRemaining)
    {
        if ( playerColor == WHITE )
            mWhiteTimeLeft = timeRemaining;  // time left in seconds
        else if ( playerColor == BLACK )
            mBlackTimeLeft = timeRemaining;  // time left in seconds
        
        //  check if player has run out of timer
        if ( mIsGameActive && timeRemaining <= 0.0 )
        {
            // call endTurn with proper out of time code
            if ( playerColor == WHITE )
                endTurn(STATUS_BLACK_WINS_TIME);
            else if ( playerColor == BLACK )
                endTurn(STATUS_WHITE_WINS_TIME);
        } 
    }
    
    
    /* *************************************************
     * * * * Private Methods * * * 
     * *************************************************/
    
    // ********** START SETUP HELPERS ***************
    private void resetPieces()
    {
        ArrayList<ChessPiece> cleanUpList = new ArrayList<>();
        
        for( ChessPiece piece : mChessPieces )
        {
            piece.reset();
            
            if ( !isValidCoord(piece.mStartRank,piece.mStartFile) )
                cleanUpList.add(piece); // save to clean up
        }
        
        for ( ChessPiece piece : cleanUpList)
        {
            // piece was not added to a starting square  
            // originnaly with addPieceToGame()
            // may have promoted
            // get rid of it
            piece.releaseListeners(); // release listeners
            mChessPieces.remove(piece); // remove the piece
        }         
    }
    private void initTimer()    // initialize timer
    {   
        if ( mIsTimedGame )
            setGameTimer(new GameTimer(this) );
        if ( mTimer != null )
            mTimer.initTimer(mStartingMinutes, mOnMoveIncrementSeconds, mWhoseTurn);
    }
    private void clearHistory()
    {
        // clear history
        if ( mChessHistory != null && !mChessHistory.isEmpty() )
            mChessHistory.clear();
    }
    private void clearBoard()
    {
        // clear game board
        for (int i = 0; i < BOARD_NUMBER_RANKS; i++)
            for (int j = 0; j < BOARD_NUMBER_FILES; j++)
                mChessBoard[i][j] = null;
    }
    private void resetGameVariables()
    {
        // reset game variables
        mWhiteOffersOrClaimsDraw = false;
        mBlackOffersOrClaimsDraw = false;
        mIsGameActive = false;  
        mTurnCount = 0;
        mWhoseTurn = WHITE;
        mGameState = STATUS_WHITES_TURN;
        mWhiteTimeLeft = mStartingMinutes*60;
        mBlackTimeLeft = mStartingMinutes*60;
    }
    // ********** END SETUP HELPERS ***************
    
    
    
    
        /**
     * pushes an update of the game state to all game state listeners
     */
    private void pushGameStateUpdate()
    { for (GameListener listener : mGameStateListeners)
            listener.onGameStateUpdate(new GameStateUpdate()); 
    }
    
    /**
     * pushes an update of the game state to all game state listeners
     */
    private void pushGameOverUpdate()
    { for (GameListener listener : mGameStateListeners)
            listener.onGameOver(new GameStateUpdate()); 
    }

     // *****************************************************************

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
        // Set startRank, startFile
        newPiece.mStartRank = rank;
        newPiece.mStartFile = file;
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
        if ( kingIndex != -1 ) 
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
        return getSquareColor(Game.convertAlgebraicToInternalRank(coord),
                Game.convertAlgebraicToInternalFile(coord) ); 
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
    
    public static boolean isValidType(char type)
    { return type == KING || type == QUEEN || type == BISHOP 
                || type == KNIGHT || type == ROOK || type == PAWN;
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
        // 
        protected int mRank, mFile;
        protected final char mType;
        protected final int mColor;
        protected int mStatus = PIECE_NOT_PLACED;
        protected boolean mIsActive = false;
        protected int mMoveCount = 0;
        protected int mStartRank = -1, mStartFile = -1;  
        
        // Listeners
        protected ArrayList<PieceListener> mPieceListeners;
        
        //********************************************************
        // TODO: implement editing mode
        // TODO: imlement edit functions: editPosition(int,int) editActiveState()
        
        
        /**
         * Constructor
         * @param color WHITE or BLACK
         * @param type KING,QUEEN,BISHOP,KNIGHT,ROOK, or PAWN
         */
        protected ChessPiece(int color, char type)
        {
            mPieceListeners = new ArrayList<>();
            if ( color != WHITE && color != BLACK ) // validate color 
                throw new IllegalArgumentException("Invalid color.");
            mColor = color;
            
            if ( !isValidType(type) )   // validate type
                throw new IllegalArgumentException("Invalid type.");
            mType = type;
        }
        
        /**
         * Copy constructor for ChessPiece class
         * @param orig original
         */
        protected ChessPiece(ChessPiece orig)
        {
            this.mRank = orig.mRank;
            this.mFile  = orig.mFile;
            this.mType  = orig.mType;
            this.mColor  = orig.mColor;
            this.mStatus  = orig.mStatus;
            this.mIsActive  = orig.mIsActive;
            this.mMoveCount  = orig.mMoveCount;
            this.mStartRank  = orig.mStartRank;
            this.mStartFile  = orig.mStartFile;

        }
        
        /**
         * Adds a listener to receive update callback from the piece
         * @param listener 
         */
        public void addPieceListener(PieceListener listener)
        { mPieceListeners.add(listener); }
        
        /**
         * Makes a move given the algebraic coordinate of target square
         * @param coord algebraic coordinate to move to, along with promotion 
         *              if needed. ex: "e4", "c1", "b8=Q"
         * @return MOVE_LEGAL (100) if its a good move, 
         *                otherwise returns error code
         */
        public int makeMove(String coord)
        {
            return makeMove(Game.convertAlgebraicToInternalRank(coord),
                    Game.convertAlgebraicToInternalFile(coord) );
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

            // capture piece, if any
            ChessPiece captured = mChessBoard[rank][file];
            if ( captured != null )
                captured.captured();
            
            // hold onto last location
            int fromRank = mRank;
            int fromFile = mFile;
            
            // set the new position and update mChessBoard
            updateChessPiece(rank, file);
            
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
            
            //  call EndTurn()
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
            // call chess piece listener function
            if ( mPieceListeners != null )
                for ( PieceListener listener : mPieceListeners )
                    listener.onCapture(this);
            // DEBUG:
            //System.out.println(this.getName()+ " has been captured!");
        }
        
        /**
         * Sets the starting coordinate
         * @param coord a1, g4, etc
         */
        public void setStartPosition(String coord)
        { setStartPosition(convertAlgebraicToInternalRank(coord),
                    Game.convertAlgebraicToInternalFile(coord) ); }
        
        /**
         * Set the starting position
         * @param rank 0-7
         * @param file 0-7
         */
        public void setStartPosition(int rank, int file)
        {
            if ( !isValidCoord(rank, file) )
                throw new IllegalArgumentException("Illegal arguement for setStartPosition");
            
            mStartRank = rank;
            mStartFile = file;
        }
        
        protected void setPosition(String coord)
        {
            setPosition(Game.convertAlgebraicToInternalRank(coord),
                    Game.convertAlgebraicToInternalFile(coord) );
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
            mChessBoard[rank][file] = this;
            mStatus = PIECE_ACTIVE;
            mIsActive = true;

            if ( mPieceListeners != null )
                for ( PieceListener listener : mPieceListeners )
                    listener.onUpdate(this);
        }
        
        // used by validateMove(). does not publish
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
         * Updates the position of the piece and publishes to listeners
         * @param rank rank and
         * @param file file of new position
         */
        protected void updateChessPiece(int rank, int file)
        {
            
            // increment move counter
            mMoveCount++;
            updatePosition(rank, file);
            
            // call liseners
            if ( mPieceListeners != null )
                for ( PieceListener listener : mPieceListeners )
                    listener.onMove(this);
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
            { return Game.convertInternalToAlgebraic(mRank, mFile); } 
        public int getRank() { return mRank; }
        public int getFile() { return mFile; }
        public char getType() { return mType; }
        public int getColor() { return mColor; }
        public int getStatus() { return mStatus; }
        public int getMoveCount() { return mMoveCount; }
        public String getName() { return Game.getName(mType); }
        public char getUnicode() { return Game.getUnicode(mColor, mType); }

        private void reset()
        {
            if ( !isValidCoord(mStartRank,mStartFile) )
            {
                // piece was not added to a starting square  
                // originnaly with addPieceToGame()
                // may have promoted
                mIsActive = false;
                /*
                // get rid of it
                // DEBUG:
                System.out.println("Piece.reset is removing junk piece "+mChessPieces.size());
                releaseListeners(); // release listeners
                mChessPieces.remove(this); // DEBUG TEST
                // DEBUG:
                System.out.println("Piece.reset size after removing: "+ mChessPieces.size());
                */
            } else {
                // Resets the piece to its starting position
                mMoveCount = 0;
                setPosition(mStartRank,mStartFile);
            }
        }
        
        public void releaseListeners()
        {
            if ( !mPieceListeners.isEmpty() )
                mPieceListeners.clear();
        }


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
        
        private King(ChessPiece orig)
        { super(orig); }

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
            
            for ( int i = kingFile + sign; (i >= 0 && i < BOARD_NUMBER_FILES); i += sign )
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
                    // TODO: make castling discoverable as candidate move in Chess960 
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
        
        private Queen(ChessPiece orig)
        { super(orig); }

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
        
        private Rook(ChessPiece orig)
        { super(orig); }
        
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
        
        private Bishop(ChessPiece orig)
        { super(orig); }

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
        
        private Knight(ChessPiece orig)
        { super(orig); }

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
        
        private Pawn(ChessPiece orig)
        { super(orig); }

        @Override
        public int makeMove(String coord)
        {
            char promoType = ' ';
            if ( coord.length() >= 4 )
                promoType = coord.charAt(3);
            return makeMove(Game.convertAlgebraicToInternalRank(coord),
                    Game.convertAlgebraicToInternalFile(coord), promoType );
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
                // promoting
                mIsActive = false;
                mStatus = PIECE_PROMOTED;
                mChessBoard[rank][file] = null;
                promotion.setPosition(rank, file);
                
                // call onPromoted callback
                if ( mPieceListeners != null )
                    for ( PieceListener listener : mPieceListeners )
                        listener.onPromote(this,promotion);
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
    public class RecordOfMove
    {
        // TODO: prepare to be serializable
        // TODO: have method to convert piece reference to string with starting square
        // TODO: consider moving RecordOfMove outside of Chess class
        final public int moveNumber;
        final public int whoseTurn;
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
        private RecordOfMove(ChessPiece moved, int movedFromRank, int movedFromFile, 
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
            
            whoseTurn = mWhoseTurn;
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
            sb.append(Game.convertInternalToAlgebraic(fromRank, fromFile))
            .append( (PieceCaptured == null) ? "-" : "x" )
            .append(Game.convertInternalToAlgebraic(toRank, toFile))
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
        private RecordOfMove(ChessPiece moved, int movedFromRank, int movedFromFile, 
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
            
            whoseTurn = mWhoseTurn;
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
        
        /**
         * Copy constructor with HashMap
         * @param orig original record
         * @param hashmap HashMap mapping piece old piece references to new
         */
        private RecordOfMove(RecordOfMove orig, HashMap<ChessPiece,ChessPiece> hashmap)
        {
            this.moveNumber = orig.moveNumber;
            this.whoseTurn = orig.whoseTurn;
            this.movePrefix = orig.movePrefix;
            this.moveText = orig.moveText;
            this.PieceMoved = hashmap.get( orig.PieceMoved );
            this.toRank = orig.toRank;
            this.toFile = orig.toFile;
            this.fromRank = orig.fromRank;
            this.fromFile = orig.fromFile;
            this.PieceCaptured   = hashmap.get( orig.PieceCaptured );
            this.capturedRank   = orig.capturedRank ;
            this.capturedFile   = orig.capturedFile ;
            this.PiecePromoted   = hashmap.get( orig.PiecePromoted ) ;
            this.promotionType   = orig.promotionType ;
            this.RookCastled   = hashmap.get( orig.RookCastled ) ;
            this.fromRookRank   = orig.fromRookRank ;
            this.fromRookFile   = orig.fromRookFile ;
            this.checkmate   = orig.checkmate ;
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
        { return Game.convertInternalToAlgebraic(rank, file); }
        
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
                    for (int i = 1; i < BOARD_NUMBER_RANKS; i++) // assumes board is square
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
            for (int i = 0; i < BOARD_NUMBER_RANKS; i++)
                returnList.add(new Square(i,file));
            return returnList;
        }
        
        public static ArrayList<Square> getRank(int rank)
        {
            ArrayList<Square> returnList = new ArrayList<>();
            if ( rank < 0 || rank > 7 )
                return returnList;  //return empty
            for (int i = 0; i < BOARD_NUMBER_FILES; i++)
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
    
    
    
    
    /** *****************************************************
     * Helper class for Game State Listener
     * TODO: pack some more information into this class
     *********************************************************/
    public final class GameStateUpdate
    {
        /**
         *
         */
        public RecordOfMove move;
        public boolean isGameActive;
        public int gameStateCode;
        public String gameState;
        public String winner;
        
        /**
         * Packages information about the current game state
         * to be sent to game state listeners
         */
        private GameStateUpdate()
        {
            // TODO: package more information to send to listener
            gameStateCode = mGameState;
            gameState = Game.getGameStatusText(mGameState);
            isGameActive = mIsGameActive;
            move = mChessHistory.get( mChessHistory.size() - 1 );
        }
    }

}
 