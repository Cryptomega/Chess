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
            
    public static final int STATUS_WHITE_WINS_CHECKMATE =    804; // game has winner
    public static final int STATUS_BLACK_WINS_CHECKMATE =    805;
    public static final int STATUS_WHITE_WINS_RESIGNATION =  806; 
    public static final int STATUS_BLACK_WINS_RESIGNATION =  807;
    public static final int STATUS_WHITE_WINS_TIME =         808;
    public static final int STATUS_BLACK_WINS_TIME =         809;
    
    public static final int STATUS_DRAW_WHITE_CLAIMS_THREE = 810; // game is draw
    public static final int STATUS_DRAW_BLACK_CLAIMS_THREE = 811; // TODO:
    public static final int STATUS_DRAW_WHITE_CLAIMS_FIFTY = 812; // TODO:
    public static final int STATUS_DRAW_BLACK_CLAIMS_FIFTY = 813;
    public static final int STATUS_DRAW_AGREEMENT =          814; 
    public static final int STATUS_DRAW_WHITE_STALEMATE =    815;
    public static final int STATUS_DRAW_BLACK_STALEMATE =    816;

    

    /* ****************************************
     * * * Game State variables * * *
     * ****************************************/
    private boolean isGameActive;
    private int GameWhoseTurn;
    private int GameState;
    private int GameTurnCount;
    private boolean GameWhiteOffersDraw;
    private boolean GameBlackClaimsDraw;
    private double GameWhiteTimeLeft;  // time left in seconds
    private double GameBlackTimeLeft;  // time left in seconds
    
    // useful index tracker variables
    private int WhiteKingIndex = -1;
    private int BlackKingIndex = -1;
    
    // Game variables, options and preferences. Do not need to be reset between games
    private int StartingMinutes = 10;
    private int OnMoveIncrementSeconds = 5;
    private boolean isTimedGame = false;
    //private boolean mUseStandardTimer = true;
    private TimerController GameTimer = null;
    //private boolean mIsChess960 = false;
    
    
    
    /**
     * Makes end of turn game state updates, as well as check
     * for end of game conditions. 
     * @param StateCode can contains the player state code of the
     *          player who's turn is about to begin or  
     *          it can be a Game State code 
     */
    private void endTurn(int StateCode)
    {
        // DEBUG
        //System.out.println("endTurn called");

        // updates and state variables
        switch (StateCode)
        {
            case PLAYER_OK:
                GameState = (GameWhoseTurn == WHITE) ? 
                        STATUS_BLACKS_TURN : STATUS_WHITES_TURN;
                break;
            case PLAYER_IN_CHECK:
                GameState = (GameWhoseTurn == WHITE) ? 
                        STATUS_BLACK_IN_CHECK : STATUS_WHITE_IN_CHECK;
                break;
            case PLAYER_IN_CHECKMATE:
                GameState = (GameWhoseTurn == WHITE) ? 
                        STATUS_WHITE_WINS_CHECKMATE : STATUS_BLACK_WINS_CHECKMATE;
                isGameActive = false;
                break;
            case PLAYER_IN_STALEMATE:
                GameState = (GameWhoseTurn == WHITE) ? 
                        STATUS_DRAW_BLACK_STALEMATE : STATUS_DRAW_WHITE_STALEMATE;
                isGameActive = false;
                break;
            case STATUS_WHITE_WINS_TIME:
                GameState = STATUS_WHITE_WINS_TIME;
                isGameActive = false;
                break;
            case STATUS_BLACK_WINS_TIME:
                GameState = STATUS_BLACK_WINS_TIME;
                isGameActive = false;
                break;
            case STATUS_WHITE_WINS_RESIGNATION:
                GameState = STATUS_WHITE_WINS_RESIGNATION;
                isGameActive = false;
                break;
            case STATUS_BLACK_WINS_RESIGNATION:
                GameState = STATUS_BLACK_WINS_RESIGNATION;
                isGameActive = false;
                break;
            case STATUS_DRAW_AGREEMENT:
                GameState = STATUS_DRAW_AGREEMENT;
                isGameActive = false;
                break;
        }
        
        // TODO: implement more game state checks

        // transitions turn to other player
        GameWhoseTurn = (GameWhoseTurn == WHITE) ? BLACK : WHITE;
        GameTurnCount++;
        
        // switch over clock
        if( isTimedGame && GameTimer != null )
        {
            if ( isGameActive )
                GameTimer.switchTimer();
            else
                GameTimer.stopTimer();
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
        if ( GameWhoseTurn == WHITE )
            GameWhiteOffersDraw = false;
        else
            GameBlackClaimsDraw = false;
        
        // calls game state listeners
        if ( isGameActive )
            pushGameStateUpdate();
        else
            pushGameOverUpdate();
    }
    
    
    /************************************************
     * The Chess Board -  This board using an internal
     * coordinate system, [inRank][inFile]
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
     * @param originalGame original game
     * @return 
     */
    public static Game copyGame(Game originalGame)
    { return new Game(originalGame, false); }
    
    /**
     * Returns a full copy of a Game instance.
     * Steals Game and Piece listeners from original to take over display.
     * The original game can later call refreshListeners() to take back display.
     * @param originalGame Original game instance
     * @return a new game instance in same position and state as orig
     */
    public static Game copyGameAndStealListeners(Game originalGame)
    { return new Game(originalGame, true); }
    
    /**
     * Default copy constructor. Returns a complete,
     * independent copy of the Game object passed.
     * Copied game will have time disabled and no listeners
     * @param originalGame original game instance to copy
     */
    public Game(Game originalGame)
    { this(originalGame, false); }
    
    /**
     * Creates a full independent copy of a Game instance.originalGame
     * @param originalGame original Game instance to copy.
     * @param stealListeners if true game listeners belonging to
     *         origGame will be added to the copied game as well.
     *         The listeners can be refreshed from origGame to regain
     *         control.
     */
    public Game(Game originalGame, boolean stealListeners)
    {   // do some cool stuff
        // Copy game state variables
        this.isGameActive = originalGame.isGameActive;
        this.GameWhoseTurn  = originalGame.GameWhoseTurn;
        this.GameState  = originalGame.GameState;
        this.GameTurnCount  = originalGame.GameTurnCount;
        this.GameWhiteOffersDraw  = originalGame.GameWhiteOffersDraw;
        this.GameBlackClaimsDraw  = originalGame.GameBlackClaimsDraw;
        this.GameWhiteTimeLeft  = originalGame.GameWhiteTimeLeft;  // time left in seconds
        this.GameBlackTimeLeft  = originalGame.GameBlackTimeLeft;  // time left in seconds
        
        // initialize lists
        this.mChessPieces = new ArrayList<>();
        this.mChessHistory = new ArrayList<>();
        this.mGameStateListeners = new ArrayList<>();
        this.clearBoard();
        
        // create a HashMap
        HashMap<ChessPiece,ChessPiece> hashmap = new HashMap<>();
        hashmap.put(null, null); // empty squares and null references get mapped to null
        
        // copy pieces with reference hashmap
        for ( ChessPiece origPiece : originalGame.getPieces() )
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
                this.mChessBoard[r][f] = hashmap.get(originalGame.mChessBoard[r][f] );
            }
        
        // copy history with references mapped
        for ( RecordOfMove originalRecord : originalGame.mChessHistory )
        {
            //if
            this.mChessHistory.add( new RecordOfMove(originalRecord, hashmap) );
        }

        
        // disregard timer
        this.isTimedGame = false;
        this.GameTimer = null;
        
        // disregard listeners unless stealListeners = true
        if ( stealListeners )
            this.mGameStateListeners = originalGame.mGameStateListeners;
        
        //throw new UnsupportedOperationException("COY CONSTRUCTOR!");
    }
    
        
        private ChessPiece copyPiece(ChessPiece original)
        {
            ChessPiece newPiece;
            char type = original.getType();
            switch (type)
            {
                case KING:
                    newPiece = new King(original);
                    break;
                case QUEEN:
                    newPiece = new Queen(original);
                    break;
                case BISHOP:
                    newPiece = new Bishop(original);
                    break;
                case KNIGHT:
                    newPiece = new Knight(original);
                    break;
                case ROOK:
                    newPiece = new Rook(original);
                    break;
                case PAWN:
                    newPiece = new Pawn(original);
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
                    listener.onGameStateUpdate( new GameStats() );
            
            // piece listeners
            for ( ChessPiece piece : this.mChessPieces )
                if ( piece.mPieceListeners != null )
                    for ( PieceListener listener : piece.mPieceListeners )
                        listener.onUpdate( piece );
                        
        }
        
        
    /* *************************************************
     * * * * Publfic Methods * * * 
     * *************************************************/
    
    /**
     * @return true if game is active. Use startGame() to activate
     */
    public boolean isGameActive() { return isGameActive; }
    public int getWhoseTurn() { return GameWhoseTurn; }
    public int getMoveNumber() { return (2 + GameTurnCount) / 2; }
    
    public double getSecondsRemaining(int color)
    { return ( color ==  WHITE ) ? GameWhiteTimeLeft : GameBlackTimeLeft; }
    
    /**
     * @return string containing a description of the current game state.
     */
    public String getGameStatus() { return getGameStatusText(GameState); }
    
    /**
     * Gets the winner of the game, if any
     * @return White, Black, or None
     */
    public String getWinner()
    {   switch(GameState)
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
        if ( isGameActive == true )
            throw new IllegalStateException("Cannot clear game while game is active");
        
        resetGameVariables(); // reset game state variables
        initTimer();          // initialize timer
        clearBoard();         // clear game board
        clearHistory();       // clear history
        
        // clear pieces array
        if ( mChessPieces != null && !mChessPieces.isEmpty() )
            mChessPieces.clear();
        WhiteKingIndex = -1;
        BlackKingIndex = -1;
    }
    
    /**
     * Does a hard reset, ignoring GameState.
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
        if ( isGameActive == true )
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
        if ( isGameActive == true )
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
        isGameActive = true;
    }
    
    /**
     * Manually end the game
     */
    public void endGame()
    {
        isGameActive = false;
        
        if ( GameTimer != null )
            GameTimer.stopTimer();
    }
    
    /**
     * Sets the starting time. passing 0,0 disables the timer
     * @param startingMins
     * @param incrementSecs 
     */
    public void setStartTime(int startingMins, int incrementSecs)
    {
        if ( isGameActive )
            return; // cannot change if game is in progress
        
        StartingMinutes = startingMins;
        OnMoveIncrementSeconds = incrementSecs;
        GameWhiteTimeLeft = (double)StartingMinutes*60.0;
        GameBlackTimeLeft = (double)StartingMinutes*60.0;
        isTimedGame = !(startingMins == 0 && incrementSecs == 0);
        
        // initialize timer
        initTimer();
    }
    

    /**
     * Change the default timer
     * @param timer a timer object which implements Chess.TimerController
     */
    public void setGameTimer(TimerController timer)
    {   
        if ( isGameActive )
            return; // cannot change timer while game is active
        
        if ( this.GameTimer != null )   // in case a timer is already running
            this.GameTimer.stopTimer(); // for some reason
        
        this.GameTimer = timer; 
    }
    
    /**
     * Currently active player resigns
     */
    public void resign()
    { resign(GameWhoseTurn); }
    
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
    { draw(GameWhoseTurn ); }
    
    /**
     * Player offers/claims a draw
     * @param player color of offering player
     */
    public void draw(int player)
    {
        if ( player == WHITE ) {
            GameWhiteOffersDraw = true;
            if ( GameBlackClaimsDraw )
                endTurn(STATUS_DRAW_AGREEMENT);
        } else if ( player == BLACK ) {
            GameBlackClaimsDraw = true;
            if ( GameWhiteOffersDraw )
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
        if ( move.length() < 5 ) // TODO: return error code instead of throwing exception
            throw new IllegalArgumentException("Invalid input string");
        String fromString = move.substring(0, 2);
        String to = move.substring(3, 5);
        int fromRank = Game.convertInRankFromAlgebraic(fromString);
        int fromFile = Game.convertInFileFromAlgebraic(fromString);
        int toRank = Game.convertInRankFromAlgebraic(to);
        int toFile = Game.convertInFileFromAlgebraic(to);
        
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
    {   // TODO: return error instead of throw
        if ( !isValidInCoord(fromRank, fromFile) || !isValidInCoord(toRank, toFile) ) 
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
        if ( !isValidInCoord(fromRank, fromFile) || !isValidInCoord(toRank, toFile) )
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
            if ( !piece.isActive || piece.getColor() == color )
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
            GameWhiteTimeLeft = timeRemaining;  // time left in seconds
        else if ( playerColor == BLACK )
            GameBlackTimeLeft = timeRemaining;  // time left in seconds
        
        //  check if player has run out of timer
        if ( isGameActive && timeRemaining <= 0.0 )
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
            
            if ( !isValidInCoord(piece.StartInRank,piece.StartInFile) )
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
        if ( isTimedGame )
            setGameTimer(new GameTimer(this) );
        if ( GameTimer != null )
            GameTimer.initTimer(StartingMinutes, OnMoveIncrementSeconds, GameWhoseTurn);
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
        GameWhiteOffersDraw = false;
        GameBlackClaimsDraw = false;
        isGameActive = false;  
        GameTurnCount = 0;
        GameWhoseTurn = WHITE;
        GameState = STATUS_WHITES_TURN;
        GameWhiteTimeLeft = StartingMinutes*60;
        GameBlackTimeLeft = StartingMinutes*60;
    }
    // ********** END SETUP HELPERS ***************
    
    
    
    
        /**
     * pushes an update of the game state to all game state listeners
     */
    private void pushGameStateUpdate()
    { for (GameListener listener : mGameStateListeners)
            listener.onGameStateUpdate(new GameStats()); 
    }
    
    /**
     * pushes an update of the game state to all game state listeners
     */
    private void pushGameOverUpdate()
    { for (GameListener listener : mGameStateListeners)
            listener.onGameOver(new GameStats()); 
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
            if ( !piece.isActive || piece.Color == color ) // skip is inactive or same color
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
            int cInRank = checkingPiece.getRank();
            int cInFile = checkingPiece.getFile();
            ArrayList<Square> interveningSquares 
                    = Square.getInterveningSquares(
                            kingRank, kingFile, cInRank, cInFile );
            
            // get intervening squares
            interveningSquares.add( new Square(cInRank, cInFile) );
            
            // check for odd case when en passant saves king from checkmage
            if ( checkingPiece.getType() == PAWN && checkingPiece.MoveCount == 1 
                && ( ( color == WHITE && checkingPiece.getRank() == 4 ) 
                    || ( color == BLACK && checkingPiece.getRank() == 3 ) ) )
            {
                int enPassantRank = (color == WHITE) ? 5 : 2;
                if ( isValidInCoord(enPassantRank,cInFile) )
                    interveningSquares.add( new Square(enPassantRank,cInFile) );
            }
            
            // check all pieces for captures or blocks
            for( ChessPiece piece : mChessPieces )
            {
                if ( !piece.isActive || piece.Color != color ) // skip is inactive or same color
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
    private ChessPiece addPieceToGame(int color, char type, int inRank, int inFile)
    {
        if ( !isValidInCoord(inRank,inFile) )
            throw new IllegalArgumentException("Invalid coordinate argument");
        if ( mChessBoard[inRank][inFile] != null )  // position is already occupied!
            throw new IllegalStateException("Cannot add piece on occupied square");   
        ChessPiece newPiece = addPieceToGame(color, type);
        //if ( newPiece == null )
        //    return null;
        newPiece.setPosition(inRank, inFile);
        // Set startRank, startFile
        newPiece.StartInRank = inRank;
        newPiece.StartInFile = inFile;
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
        int kingIndex = (color == WHITE) ? WhiteKingIndex : BlackKingIndex;
        if ( kingIndex != -1 ) 
            return kingIndex;
        
        
        for (int i = 0; i < mChessPieces.size(); i++ )
            if ( mChessPieces.get(i).getType() == KING
                    && mChessPieces.get(i).getColor() == color )
            {
                if ( color == WHITE)
                    WhiteKingIndex = i;
                else
                    BlackKingIndex = i;
                return i;
            }
        return -1;
    }
    
    
    
    
    /* *************************************************
     * * * * Static Methods * * * 
     * *************************************************/
    // conversion methods
    private static int convertInRankFromAlgebraic(String coord)
    {   return  Character.getNumericValue(coord.toLowerCase().charAt(1)) - 1; }
    
    private static int convertInFileFromAlgebraic(String coord)
    {   return (int)coord.toLowerCase().charAt(0) - 97; }
    
    private static int convertInRankFromChessRank(int chessRank)
    {   return chessRank - 1; }
    
    private static int convertInFileFromChessFile(int chessFile)
    {   return chessFile - 1; }
    
    private static int convertInFileFromChessFile(char chessFile)
    {   return (int)Character.toUpperCase(chessFile) - (int)'A'; }
    
    
    
    private static String convertAlgebraicFromIn(int inRank, int inFile)
    {   if ( !isValidInCoord(inRank, inFile) )
            throw new IllegalArgumentException("Invalid Coordinate");
        return ((char) (inFile+97)) + String.valueOf(inRank + 1);
    }
    
    
    // get a square color
    public static int getSquareColor(String coord)
    {
        return getSquareColor(Game.convertInRankFromAlgebraic(coord),
                Game.convertInFileFromAlgebraic(coord) ); 
    }
    
    public static int getSquareColor(int rank, int file)
    {
        if ( !isValidInCoord(rank, file) )
            throw new IllegalArgumentException("Invalid Coordinate");
        return (rank+file)%2; 
    }
    
    public static boolean isMoveCodeLegal(int code)
    {   return code == MOVE_LEGAL || code == MOVE_LEGAL_EN_PASSANT
                || code == MOVE_LEGAL_CASTLE_KINGSIDE 
                || code == MOVE_LEGAL_CASTLE_QUEENSIDE ;
    }
    
    public static String getGameStatusText(int code)
    {   switch(code)
        {   case STATUS_WHITES_TURN:
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
    
    public static boolean isValidChessCoord(int chessRank, int chessFile)
    {   return isValidInCoord( 
                convertInRankFromChessRank(chessRank),
                convertInFileFromChessFile(chessFile)  );
    }
    
    private static boolean isValidInCoord(int inRank, int inFile)
    {   return ( inRank >= 0 && inRank <= 7
                && inFile >= 0 && inFile <= 7);
    }
    
    public static boolean isValidCoord(String square)
    {   return isValidInCoord(convertInRankFromAlgebraic(square),
                convertInFileFromAlgebraic(square) );
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
        protected int inRank;   // Internal rank and file index
        protected int inFile;   // 0 <= inRank, inFile <= 7 
        protected final char Type;
        protected final int Color;
        protected int Status = PIECE_NOT_PLACED;
        protected boolean isActive = false;
        protected int MoveCount = 0;
        protected int StartInRank = -1;
        protected int StartInFile = -1;
        
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
            Color = color;
            
            if ( !isValidType(type) )   // validate type
                throw new IllegalArgumentException("Invalid type.");
            Type = type;
        }
        
        /**
         * Copy constructor for ChessPiece class
         * @param orig original
         */
        protected ChessPiece(ChessPiece orig)
        {
            this.inRank = orig.inRank;
            this.inFile  = orig.inFile;
            this.Type  = orig.Type;
            this.Color  = orig.Color;
            this.Status  = orig.Status;
            this.isActive  = orig.isActive;
            this.MoveCount  = orig.MoveCount;
            this.StartInRank  = orig.StartInRank;
            this.StartInFile  = orig.StartInFile;

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
            return makeMove(Game.convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord) );
        }
        
        /**
         * Makes a move given a target square
         * @param square object representing target square
         * @return move code
         */
        public int makeMove(Square square)
        { return makeMove(square.inRank, square.inFile); }
        
        /**
         * Validates a move given a target square
         * @param square object representing target square
         * @return move code
         */
        public int validateMove(Square square)
        { return validateMove(square.inRank, square.inFile); }
        
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
            if ( !isGameActive )
                return GAME_NOT_ACTIVE;
            
            // make sure piece is active
            if ( !isActive )
                return PIECE_NOT_ACTIVE;
            
            // check mColor
            if ( Color != GameWhoseTurn )
                return MOVE_ILLEGAL_WRONG_PLAYER;
            
            // validate move
            int code = validateMove(rank, file);
            if ( code != MOVE_LEGAL ) return code;

            // capture piece, if any
            ChessPiece captured = mChessBoard[rank][file];
            if ( captured != null )
                captured.captured();
            
            // hold onto last location
            int fromRank = inRank;
            int fromFile = inFile;
            
            // set the new position and update mChessBoard
            updateChessPiece(rank, file);
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( Color == WHITE ) ? BLACK : WHITE;
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
            
            if ( !isValidInCoord(rank, file) )
                throw new IllegalArgumentException("Invalid Coordinate");
            
            // square cannot be occupied by own piece
            if ( mChessBoard[rank][file] != null
                    && mChessBoard[rank][file].getColor() == Color )
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
                captured.isActive = false;     // temporarily deactivate
            updatePosition(rank,file);   // (1)temporarily move the piece
            
            boolean isInCheck = isInCheck(Color);  // (2)
            
            // undo temporary move (3)
            updatePosition(fromRank, fromFile);
            mChessBoard[rank][file] = captured;
            if ( captured != null )
                captured.isActive = true;
            
            if ( isInCheck )
                return MOVE_ILLEGAL_KING_IN_CHECK;
            
            return MOVE_LEGAL; // returns valid for now
        }
        
        
        protected void captured()
        {
            isActive = false;
            Status = PIECE_CAPTURED;
            mChessBoard[inRank][inFile] = null;
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
        { setStartPosition(convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord) ); }
        
        /**
         * Set the starting position
         * @param rank 0-7
         * @param file 0-7
         */
        public void setStartPosition(int rank, int file)
        {
            if ( !isValidInCoord(rank, file) )
                throw new IllegalArgumentException("Illegal arguement for setStartPosition");
            
            StartInRank = rank;
            StartInFile = file;
        }
        
        protected void setPosition(String coord)
        {
            setPosition(Game.convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord) );
        }
        
        protected void setPosition(int rank, int file)
        {
            if ( !isValidInCoord(rank, file) )
                throw new IllegalArgumentException("Illegal arguement for setPosition");

            // check if square is already occupied
            if ( mChessBoard[rank][file] != null )
                throw new IllegalStateException("Cannot set piece on occupied square");
            
            inRank = rank;
            inFile = file;
            mChessBoard[rank][file] = this;
            Status = PIECE_ACTIVE;
            isActive = true;

            if ( mPieceListeners != null )
                for ( PieceListener listener : mPieceListeners )
                    listener.onUpdate(this);
        }
        
        // used by validateMove(). does not publish
        protected void updatePosition(int rank, int file)
        {
            // set current position to null
            mChessBoard[inRank][inFile] = null;
            // set reference to this piece at new square
            mChessBoard[rank][file] = this;
            // set the position in the piece
            inRank = rank;
            inFile = file;
        }
        
        /**
         * Updates the position of the piece and publishes to listeners
         * @param rank rank and
         * @param file file of new position
         */
        protected void updateChessPiece(int rank, int file)
        {
            
            // increment move counter
            MoveCount++;
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
            if ( Color != GameWhoseTurn ) 
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
            { return Game.convertAlgebraicFromIn(inRank, inFile); } 
        public int getRank() { return inRank; }
        public int getFile() { return inFile; }
        public char getType() { return Type; }
        public int getColor() { return Color; }
        public int getStatus() { return Status; }
        public int getMoveCount() { return MoveCount; }
        public String getName() { return Game.getName(Type); }
        public char getUnicode() { return Game.getUnicode(Color, Type); }

        private void reset()
        {
            if ( !isValidInCoord(StartInRank,StartInFile) )
            {
                // piece was not added to a starting square  
                // originnaly with addPieceToGame()
                // may have promoted
                isActive = false;
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
                MoveCount = 0;
                setPosition(StartInRank,StartInFile);
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
            if ( Color == WHITE && inRank != 0)
                return false;
            if ( Color == BLACK && inRank != 7 ) 
                return false;
            if ( inRank != rank )
                return false;
            if ( mChessBoard[rank][file] != null && 
                 mChessBoard[rank][file].getColor() != Color )
                return false;
            if ( abs(inFile - file) == 2 )
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
            if ( !isGameActive )
                return GAME_NOT_ACTIVE;
            
            // make sure piece is active
            if ( !isActive )
                return PIECE_NOT_ACTIVE;
            
            // check mColor
            if ( Color != GameWhoseTurn )
                return MOVE_ILLEGAL_WRONG_PLAYER;
            
            // validate move
            int code = validateCastle(rank, file);
            if ( code != MOVE_LEGAL_CASTLE_KINGSIDE &&
                    code != MOVE_LEGAL_CASTLE_QUEENSIDE ) return code;
    
            
            // get rook
            ChessPiece castlingRook = getCastlingRook(rank,file);

            int fromRank = inRank;
            int fromFile = inFile;
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
            int opponentColor = ( Color == WHITE ) ? BLACK : WHITE;
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
            if ( MoveCount != 0 ) return ILLEGAL_CASTLE_KING_HAS_MOVED;
            // get the rook
            ChessPiece castlingRook = getCastlingRook(rank,file);
            if ( !castlingRook.isActive ) return MOVE_ILLEGAL;
            // rook cannot have made a move already
            if ( castlingRook.MoveCount != 0 ) return ILLEGAL_CASTLE_ROOK_HAS_MOVED;
            
            int rookFile = castlingRook.getFile();
            int kingFile = getFile();
            boolean isCastlingKingside = rookFile > kingFile;
            int toKingFile = isCastlingKingside ? 6 : 2;
            int toRookFile = isCastlingKingside ? 5 : 3;

            // check for impeded
            int[] fileList = {rookFile,kingFile,toKingFile,toRookFile};
            int minFile = toKingFile;
            int maxFile = toKingFile;
            // find range of possibly impeded squares
            for (int i = 0; i < fileList.length; i++)
            {   minFile = fileList[i] < minFile ? fileList[i] : minFile;
                maxFile = fileList[i] > maxFile ? fileList[i] : maxFile;
            }
            // check squares in range for impeded
            for (int i = minFile; i <= maxFile; i++)
            {   ChessPiece square = mChessBoard[rank][i];
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
            {   if ( isInCheck(Color,rank,i) )
                {   throughCheck = true;
                    break;
                }                
            }
            
            // re-activate rook
            mChessBoard[rank][rookFile] = castlingRook;
            
            if ( throughCheck ) return ILLEGAL_CASTLE_THROUGH_CHECK;
            return isCastlingKingside ? MOVE_LEGAL_CASTLE_KINGSIDE : MOVE_LEGAL_CASTLE_QUEENSIDE;     
        }
        

        @Override
        public int isObserving(int rank, int file) 
        {
            if ( ( Math.abs(inRank - rank) <= 1 ) 
                    && ( Math.abs(inFile - file) <= 1 )
                    && (rank != inRank || file != inFile) )
                return PIECE_IS_OBSERVING;
            else
                return MOVE_ILLEGAL; 
        }

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = new ArrayList<>();
            for (int i = inRank-1; i <= inRank+1; i++)
                for (int j = inFile-1; j <= inFile+1; j++)
                {
                    if ( i == inRank && j == inFile )
                        continue;
                    if ( isValidInCoord(i,j) )
                        returnList.add(new Square(i,j));
                }
            // if move count is 0, add castle candidate move
            if ( MoveCount == 0)
            {
                if ( inFile == 4 )   // standard king starting position
                {
                    returnList.add(new Square(inRank,6));
                    returnList.add(new Square(inRank,2));
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
            if ( inRank == rank && inFile == file )  // already occupying square
                 return MOVE_ILLEGAL;
            else if ( inRank == rank )               // check if on same rank
            {
                // check if impeded
                int d = inFile - file;   // difference
                int s = Integer.signum(file - inFile);   // sign
                for ( int i = 1; abs(d+i*s) > 0 ; i++)
                {
                    // Look at square (mRank), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(rank, mFile+s*i)); // DEBUG
                    if ( mChessBoard[inRank][inFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( inFile == file ) {           // check if on same file
                int d = inRank - rank;
                int s = Integer.signum(rank - inRank);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+s*i, mFile)); // DEBUG
                    if( mChessBoard[inRank+s*i][inFile] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( abs((double)(rank - inRank) / (double)(file - inFile) ) == 1.0 ) {  // check diagonalS
                int d = inFile - file;   // difference
                int s = Integer.signum(file - inFile);   // sign
                int sl = (int)((double)(rank - inRank) / (double)(file - inFile)) ;  // slope
                //System.out.println("isObserving on diagonal, slope is " + sl);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    // look at square (mRank + s*i), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+sl*s*i, mFile+s*i)); // DEBUG
                    if( mChessBoard[inRank+sl*s*i][inFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING; 
            }
            return MOVE_ILLEGAL;            
        }

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = Square.getDiagonals(inRank,inFile);
            returnList.addAll( Square.getFile(inFile) );
            returnList.addAll( Square.getRank(inRank) );
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
            if ( inRank == rank && inFile == file )  // already occupying square
                 return MOVE_ILLEGAL;
            else if ( inRank == rank )               // check if on same rank
            {
                // check if impeded
                int d = inFile - file;   // difference
                int s = Integer.signum(file - inFile);   // sign
                for ( int i = 1; abs(d+i*s) > 0 ; i++)
                {
                    // Look at square (mRank), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(rank, mFile+s*i)); // DEBUG
                    if ( mChessBoard[inRank][inFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( inFile == file ) {           // check if on same file
                int d = inRank - rank;
                int s = Integer.signum(rank - inRank);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+s*i, mFile)); // DEBUG
                    if( mChessBoard[inRank+s*i][inFile] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            }
            return MOVE_ILLEGAL;
        }

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = Square.getFile(inFile);
            returnList.addAll( Square.getRank(inRank) );
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
            if ( inRank == rank && inFile == file )  // already occupying square
                 return MOVE_ILLEGAL;
            else if ( abs((double)(rank - inRank) / (double)(file - inFile) ) == 1.0 ) {  // check diagonalS
                int dif = inFile - file;   // difference
                int sign = Integer.signum(file - inFile);   // sign
                int sl = (int)((double)(rank - inRank) / (double)(file - inFile)) ;  // slope
                //System.out.println("isObserving on diagonal, slope is " + sl);
                for (int i = 1; abs(dif+i*sign) > 0; i++)
                {
                    // look at square (mRank + s*i), (mFile + s*i)
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+sl*s*i, mFile+s*i)); // DEBUG
                    if( mChessBoard[inRank+sl*sign*i][inFile+sign*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING; 
            }
            return MOVE_ILLEGAL;
        }

        @Override
        public ArrayList<Square> getCandidateMoves()
        { return Square.getDiagonals(inRank, inFile); }
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
            int absRankDif = abs(inRank - rank);
            int absFileDif = abs(inFile - file);
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
                        int candidateInRank = inRank + rankDir*rankStep;
                        int candidateInFile = inFile + fileDir*fileStep;
                        if ( isValidInCoord(candidateInRank,candidateInFile) )
                            returnList.add(new Square(candidateInRank,candidateInFile));                        
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
            return makeMove(Game.convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord), promoType );
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
            if ( !isGameActive )
                return GAME_NOT_ACTIVE;
            
            // make sure piece is active
            if ( !isActive )
                return PIECE_NOT_ACTIVE;
            
            // check mColor
            if ( Color != GameWhoseTurn )
                return MOVE_ILLEGAL_WRONG_PLAYER;
            
            // validate move
            int code = validateMove(rank, file);
            if ( code != MOVE_LEGAL && code != MOVE_LEGAL_EN_PASSANT ) return code;
            
            
            ChessPiece promotion = null;
            // check for promotion
            if ( (Color == WHITE && rank == 7 ) 
                    || (Color == BLACK && rank == 0 ) )
            {
                // get ready to promote!
                if ( !isValidPromotionType(promotionType) )
                    return AMBIGUOUS_PROMOTION; // not so fast
                promotion = addPieceToGame(Color,promotionType);
            }
            
             //  implement en passant 
            // capture piece, if any
            ChessPiece captured;
            if ( code == MOVE_LEGAL_EN_PASSANT )
                captured = mChessBoard[inRank][file];
            else
                captured = mChessBoard[rank][file];
            if ( captured != null )
                captured.captured();
            
            // hold onto last location
            int fromRank = inRank;
            int fromFile = inFile;
            
            // set the new position and update mChessBoard
            updateChessPiece(rank, file);
            
            if ( promotion != null )
            {
                // promoting
                isActive = false;
                Status = PIECE_PROMOTED;
                mChessBoard[rank][file] = null;
                promotion.setPosition(rank, file);
                
                // call onPromoted callback
                if ( mPieceListeners != null )
                    for ( PieceListener listener : mPieceListeners )
                        listener.onPromote(this,promotion);
            }
            // add promoted piece at rank,file, if needed
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( Color == WHITE ) ? BLACK : WHITE;
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
            if ( !isValidInCoord(rank, file) )
                throw new IllegalArgumentException("Invalid Coordinate");
            
            // square cannot be occupied by own piece
            if ( mChessBoard[rank][file] != null
                    && mChessBoard[rank][file].getColor() == Color )
                return MOVE_ILLEGAL_SQUARE_OCCUPIED;
            
            
            int direction = (Color == WHITE) ? 1 : -1;
            ChessPiece captured = null;
            boolean enPassant = false;
            if ( (inFile == file) && (inRank + direction == rank ) )  
            {
                // moving forward one square
                if ( mChessBoard[rank][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
            } else if ( (inFile == file) && (inRank + 2*direction == rank ) ) { 
                // moving forward two squares
                if ( Color ==  WHITE && inRank != 1 )
                    return MOVE_ILLEGAL;
                if ( Color ==  BLACK && inRank != 6 )
                    return MOVE_ILLEGAL;
                if ( mChessBoard[inRank + direction][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( mChessBoard[rank][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( MoveCount != 0 )
                    return MOVE_ILLEGAL_PAWN_HAS_MOVED;
                
            } else if ( (abs(inFile - file) == 1) && (inRank + direction == rank) ) {
                // capturing a piece
                captured = mChessBoard[rank][file];
                if ( mChessBoard[rank][file] == null )
                {
                    // nothing to capture unless en passant is possible
                    if ( mChessBoard[inRank][file] == null ) // en passant square is empty
                        return MOVE_ILLEGAL_NOTHING_TO_CAPTURE;
                    // check if we are on white's 5th rank
                    if ( Color == WHITE && inRank != 4 )
                        return MOVE_ILLEGAL;
                    // check if we are on black's 5th rank
                    if ( Color == BLACK && inRank != 3 )
                        return MOVE_ILLEGAL;
                    if (  mChessBoard[inRank][file].getColor() == Color )
                        return MOVE_ILLEGAL;
                    if ( mChessBoard[inRank][file].getType() != PAWN )
                        return MOVE_ILLEGAL;
                    ChessPiece neighborPawn = mChessBoard[inRank][file];
                    RecordOfMove lastMove = mChessHistory.get( mChessHistory.size() - 1 );
                    if ( lastMove.PieceMoved != neighborPawn )
                        return MOVE_ILLEGAL_LATE_EN_PASSANT;
                    if ( lastMove.fromRank != inRank + 2*direction )
                        return MOVE_ILLEGAL_LATE_EN_PASSANT;
                    captured = neighborPawn;
                    enPassant = true;
                        
                    
                    //if ( mChessBoard[mRank][file].getCol
                    //or && mChessBoard[mRank][file].getType )
                    // if en passant square is an enemy pawn, 
                    //and its just moved two square, E.P. is OK
                    
                } else if ( mChessBoard[rank][file].getColor() == Color ) {
                    return MOVE_ILLEGAL_SQUARE_OCCUPIED;
                }
            } else {
                return MOVE_ILLEGAL;
            }

            
            //  cange to mRank and mFile
            int fromRank = inRank;  // save current rank 
            int fromFile = inFile;  // and file
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
                captured.isActive = false;     // temporarily deactivate
                if ( enPassant )
                    mChessBoard[fromRank][file] = null;
            }
            updatePosition(rank,file);   // (1)temporarily move the piece
            
            boolean isInCheck = isInCheck(Color);  // (2)
            
            // undo temporary move (3)
            updatePosition(fromRank, fromFile);
            if ( captured != null )
            {
                captured.isActive = true;
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
            int direction = (Color == WHITE) ? 1 : -1;
            if ( (inRank + direction == rank) &&
                    abs(inFile - file) == 1 )
                return PIECE_IS_OBSERVING;
            return MOVE_ILLEGAL;
        }

        @Override
        public ArrayList<Square> getCandidateMoves()
        {
            ArrayList<Square> returnList = new ArrayList<>();
            int direction = (Color == WHITE) ? 1 : -1;
            int rank = inRank + direction;
            for (int file = inFile - 1; file <= inFile + 1; file++)
            {
                if ( isValidInCoord(rank,file) )
                    returnList.add(new Square(rank,file));
            }
            int rank2 = inRank + 2 * direction;
            if (MoveCount == 0 && isValidInCoord(rank2,inFile))
                returnList.add(new Square(rank2,inFile));
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
            
            this.whoseTurn = GameWhoseTurn;
            moveNumber = getMoveNumber();
            if ( whoseTurn == WHITE )
                movePrefix = String.valueOf(moveNumber) + ". ";
            else
                movePrefix = String.valueOf(moveNumber) + "... ";
            
            // construct move string
            StringBuilder sb = new StringBuilder();
            if ( PieceMoved.getType() == PAWN )
                sb.append(" ");
            else
                sb.append(PieceMoved.getType());
            sb.append(Game.convertAlgebraicFromIn(fromRank, fromFile))
            .append( (PieceCaptured == null) ? "-" : "x" )
            .append(Game.convertAlgebraicFromIn(toRank, toFile))
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
            
            this.whoseTurn = GameWhoseTurn;
            moveNumber = getMoveNumber();
            if ( whoseTurn == WHITE )
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
        private final int inRank;
        private final int inFile;
        
        private Square(int inRank, int inFile)
        {
            if ( !isValidInCoord(inRank,inFile))
                throw new IllegalArgumentException("new Square called with invalid coordinate");
            this.inRank = inRank;
            this.inFile = inFile;
        }
        
        // TODO: implement public getRank() and getFile() to return external coords
        
        @Override
        public String toString()
        { return Game.convertAlgebraicFromIn(inRank, inFile); }
        
        public boolean isEqual(Square square)
        { return this.inRank == square.inRank && this.inFile == square.inFile; }
        
        public static boolean isEqual(Square square1, Square square2)
        { return square1.inRank == square2.inRank && square1.inFile == square2.inFile; }
        
        // Static helper methods
        public static ArrayList<Square> getDiagonals(Square square)
        { return getDiagonals(square.inRank, square.inFile); }
        
        private static ArrayList<Square> getDiagonals(int inRank, int inFile)
        {
            ArrayList<Square> returnList = new ArrayList<>();
            
            for (int dRank = -1; dRank <= 1; dRank += 2  )
                for (int dFile = -1; dFile <= 1; dFile += 2  )
                    for (int i = 1; i < BOARD_NUMBER_RANKS; i++) // assumes board is square
                    {
                        int newInRank = inRank + i * dRank;
                        int newInFile = inFile + i * dFile;
                        if (!isValidInCoord(newInRank,newInFile))
                            break;
                        returnList.add(new Square(newInRank,newInFile));
                    }
            return returnList;
        }
        
        private static ArrayList<Square> getFile(int inFile)
        {
            ArrayList<Square> returnList = new ArrayList<>();
            if ( inFile < 0 || inFile > 7 )
                return returnList;  //return empty
            for (int i = 0; i < BOARD_NUMBER_RANKS; i++)
                returnList.add(new Square(i,inFile));
            return returnList;
        }
        
        private static ArrayList<Square> getRank(int inRank)
        {
            ArrayList<Square> returnList = new ArrayList<>();
            if ( inRank < 0 || inRank > 7 )
                return returnList;  //return empty
            for (int i = 0; i < BOARD_NUMBER_FILES; i++)
                returnList.add(new Square(inRank,i));
            return returnList;
        }
        
        public static ArrayList<Square> getInterveningSquares(
                int inRank1, int inFile1, int inRank2, int inFile2 )
        {
            ArrayList<Square> returnList = new ArrayList<>();
            if ( !isValidInCoord(inRank1,inFile1) || !isValidInCoord(inRank2,inFile2) )
                return returnList;
            int rankDif = inRank2 - inRank1;
            int fileDif = inFile2 - inFile1;
            int rankDir = rankDif > 0 ? 1 : -1;
            int fileDir = fileDif > 0 ? 1 : -1;
            
            if ( inRank1 == inRank2 ) {
                for ( int file = inFile1; file != inFile2; file += fileDir)
                    returnList.add(new Square(inRank1,file));
            } else if ( inFile1 == inFile2 ) {
                for ( int rank = inRank1; rank != inRank2; rank += rankDir)
                    returnList.add(new Square(rank,inFile1));
            } else if ( abs( (double)(rankDif) / (double)(fileDif) ) == 1.0 ) {
                int file = inFile1;
                for ( int rank = inRank1; rank != inRank2; rank += rankDir)
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
    public final class GameStats
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
        private GameStats()
        {
            // TODO: package more information to send to listener
            this.isGameActive = isGameActive();
            //this.isGameActive = Game.this.isGameActive;
            this.gameStateCode = Game.this.GameState;
            this.gameState = Game.getGameStatusText(GameState);
            this.winner = Game.this.getWinner();
            
            /*
            // TODO: add these game stats
            private int GameWhoseTurn;
            private int GameTurnCount;
            private boolean GameWhiteOffersDraw;
            private boolean GameBlackClaimsDraw;
            private double GameWhiteTimeLeft;  // time left in seconds
            private double GameBlackTimeLeft;  // time left in seconds
            */

            this.move = mChessHistory.get( mChessHistory.size() - 1 );
        }
    }

}
 