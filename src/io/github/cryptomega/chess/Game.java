/* 
 * 
 * TODO: add header info
 * @author Philip Schexnayder
 */
package io.github.cryptomega.chess;

import static java.lang.Math.abs;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
    //public static final int NONE  = -1;
    
    public static final int BOARD_NUMBER_RANKS = 8;
    public static final int BOARD_NUMBER_FILES = 8;
    
    // Piece types
    public static final char KING =   'K';
    public static final char QUEEN =  'Q';
    public static final char BISHOP = 'B';
    public static final char KNIGHT = 'N';
    public static final char ROOK =   'R';
    public static final char PAWN =   'P';
    
    // makeMove() and isValidMove() callback codes
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
    public static final int AMBIGUOUS_MOVE                    = 119;
    public static final int AMBIGUOUS_PROMOTION               = 120;
    public static final int GAME_NOT_ACTIVE                   = 121;
    public static final int PIECE_NOT_ACTIVE                  = 122;
    public static final int INVALID_COORDINATE                = 123;
    public static final int MOVE_DEBUG                        = 195;
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
    public static final int STATUS_DRAW_BLACK_CLAIMS_THREE = 811; 
    public static final int STATUS_DRAW_WHITE_CLAIMS_FIFTY = 812; 
    public static final int STATUS_DRAW_BLACK_CLAIMS_FIFTY = 813;
    public static final int STATUS_DRAW_AGREEMENT =          814; 
    public static final int STATUS_DRAW_MATERIAL =           815;
    public static final int STATUS_DRAW_WHITE_STALEMATE =    816;
    public static final int STATUS_DRAW_BLACK_STALEMATE =    817;
    
    //public static final String REGEX_CASTLE_QUEENSIDE = "O-O-O";
    public static final String REGEX_CASTLE_QUEENSIDE = "[oO0][-_][oO0][-_][oO0]";
    public static final String REGEX_CASTLE_KINGSIDE = "[oO0][-_][oO0]";
    public static final String REGEX_CASTLE = "[oO0](-[oO0])(\\1)?(.*)";
    //public static final String REGEX_MOVE_e2e4 = "([a-h][1-8]).*([a-h][1-8])";
    public static final String REGEX_MOVE_e2e4 = ".*([a-h][1-8]).*([a-h][1-8])=?([qbnrQBNR]?)(.*)";
    public static final String REGEX_MOVE_Pee4 
            = "[^PRNBQKa-h]*([PRNBQK]?)([a-h1-8]?).*([a-h][1-8])=?([qbnrQBNR]?)(.*)";
    //public static final String REGEX_PROMO = "=?([qbnrQBNR])(.*)"; // TODO: incorporate into REGEX_MOVE_Pee4 / REGEX_MOVE_e2e4
    //public static final String REGEX_DISAMBIG = "([KQBNRP]?)([a-h1-8]?)"; // TODO: incorporate into REGEX_MOVE_Pee4 
    
    private Matcher matcherCastle = null;
    private Matcher matchere2e4 = null;
    private Matcher matcherPee4 = null;
    //private Matcher matcherPromo = null;
    //private Matcher matcherDisambig = null;
    
    /*
    private Pattern ptrnCastle = null;
    private Pattern ptrne2e4 = null;
    private Pattern ptrnPee4 = null;
    private Pattern ptrnPromo = null;
    */
    /*
         * (1) try to match two coords: ([a-h][1-8]).*([a-h][1-8])
     * (2) try disamgious coord: ([PRNBQK][a-h1-7]).*([a-h][1-8])
     * (3) try normal match: ([PRNBQK])([a-h][1-8])
     * (4) pawn move match: ([a-h][1-8])
    */


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
    {   switch (StateCode) // ONLY METHOD WHICH UPDATES GAME STATE VARIABLES BETWEEN TURNS
        {   case PLAYER_OK:
                GameState = (GameWhoseTurn == WHITE) ? STATUS_BLACKS_TURN : STATUS_WHITES_TURN;
                break;
            case PLAYER_IN_CHECK:
                GameState = (GameWhoseTurn == WHITE) ? STATUS_BLACK_IN_CHECK : STATUS_WHITE_IN_CHECK;
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
            case STATUS_DRAW_WHITE_CLAIMS_FIFTY:
                GameState = STATUS_DRAW_WHITE_CLAIMS_FIFTY;
                isGameActive = false;
                break;
            case STATUS_DRAW_BLACK_CLAIMS_FIFTY:
                GameState = STATUS_DRAW_BLACK_CLAIMS_FIFTY;
                isGameActive = false;
                break;
            case STATUS_DRAW_WHITE_CLAIMS_THREE:
                GameState = STATUS_DRAW_WHITE_CLAIMS_THREE;
                isGameActive = false;
                break;
            case STATUS_DRAW_BLACK_CLAIMS_THREE:
                GameState = STATUS_DRAW_BLACK_CLAIMS_THREE;
                isGameActive = false;
                break;
        }  // TODO: implement more game state checks

        GameWhoseTurn = (GameWhoseTurn == WHITE) ? BLACK : WHITE;
        GameTurnCount++; // transitions turn to other player, increment turn count
        
        if ( this.drawByInsufficientMaterial() )
        {   GameState = STATUS_DRAW_MATERIAL;
            isGameActive = false;
        }
            
        // check draw claims
        if ( GameWhiteOffersDraw || GameBlackClaimsDraw )
            if ( drawFiftyMoves() )
            {
                isGameActive = false;
                GameState = GameWhiteOffersDraw ? 
                        STATUS_DRAW_WHITE_CLAIMS_FIFTY : STATUS_DRAW_BLACK_CLAIMS_FIFTY;
            } else if ( drawThreefoldRepetition() ) {
                isGameActive = false;
                GameState = GameWhiteOffersDraw ? 
                        STATUS_DRAW_WHITE_CLAIMS_THREE : STATUS_DRAW_BLACK_CLAIMS_THREE;
            }        
            

        if( isTimedGame && GameTimer != null ) // switch over clock
        {   if ( isGameActive )
                GameTimer.switchTimer();
            else
                GameTimer.stopTimer();
        }

        // reset draw flag as player's turn is starting, after draw claim checked
        if ( GameWhoseTurn == WHITE )
            GameWhiteOffersDraw = false;
        else
            GameBlackClaimsDraw = false;
        
        // calls game state listeners
        if ( isGameActive ) pushGameStateUpdate();
        else pushGameOverUpdate();
    }
    
    /************************************************
     * The Chess Board -  This board using an internal
     * coordinate system, [inRank][inFile]
     * a1 -> [0][0], a2 -> [0][1], b1 -> [1][0], etc.
     * Cells contain reference to the occupying 
     * chess piece, or null if the square is empty.
     ***********************************************/
    private final ChessPiece[][] GameBoard =
            new ChessPiece[BOARD_NUMBER_RANKS][BOARD_NUMBER_FILES]; 

    /* *************************************************
     * * * * ArrayList of all chess pieces * * * 
     * *************************************************/
    private final ArrayList<ChessPiece> GamePieces;  
    
    /* *************************************************
     * * * * History ArrayList * * * 
     * ************************************************/
    private final ArrayList<RecordOfMove> GameHistory;
    
    // hold future moves if for navigating through moves
    private final ArrayList<RecordOfMove> GameFuture;
    

    /* *************************************************
     * * * * Game State Listeners * * * 
     * ************************************************/
    private ArrayList<GameListener> GameStateListeners; 
    
    
    /** ************************************************
     * * * * Constructor * * * 
     * Initiates a new game. 
     *    chess.setupStandardGame();
     *    chess.setStartTime(30, 10);
     *    chess.startGame();
     * *************************************************/
    public Game()
    {
        GamePieces = new ArrayList<>();
        GameHistory = new ArrayList<>();
        this.GameFuture = new ArrayList<>();
        GameStateListeners = new ArrayList<>();
        
        clearGame(); 
        
        // initialize patterns
        initPatterns();
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
    {   
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
        this.GamePieces = new ArrayList<>();
        this.GameHistory = new ArrayList<>();
        this.GameFuture = new ArrayList<>();
        this.GameStateListeners = new ArrayList<>();
        this.clearBoard();
        
        // initialize patterns
        initPatterns();
        
        // create a HashMap
        HashMap<ChessPiece,ChessPiece> hashmap = new HashMap<>();
        hashmap.put(null, null); // empty squares and null references get mapped to null
        
        // copy pieces with reference hashmap
        for ( ChessPiece origPiece : originalGame.getPieces() )
        {
            ChessPiece newPiece = this.copyPiece( origPiece );
            this.GamePieces.add( newPiece );
            
            hashmap.put(origPiece, newPiece);  // make the hash map
            
            if ( stealListeners )
                newPiece.PieceListeners = origPiece.PieceListeners;
        }
        
        // copy board with references mapped
        for (int r = 0; r < BOARD_NUMBER_RANKS; r++)
            for (int f = 0; f < BOARD_NUMBER_FILES; f++)
               this.GameBoard[r][f] = hashmap.get(originalGame.GameBoard[r][f] );
        
        // copy history with references mapped
        for ( RecordOfMove originalRecord : originalGame.GameHistory )
            this.GameHistory.add( new RecordOfMove(originalRecord, hashmap) );

        
        // disregard timer
        this.isTimedGame = false;
        this.GameTimer = null;
        
        // disregard listeners unless stealListeners = true
        if ( stealListeners ) this.GameStateListeners = originalGame.GameStateListeners;
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
            if ( GameStateListeners != null )
                for (  GameListener listener : GameStateListeners )
                    listener.onGameStateUpdate( new GameStats() );
            
            // piece listeners
            for ( ChessPiece piece : this.GamePieces )
                if ( piece.PieceListeners != null )
                    for ( PieceListener listener : piece.PieceListeners )
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
    
    public double getSecondsRemaining()
    { return getSecondsRemaining(GameWhoseTurn); }
    public double getSecondsRemaining(int player)
    { return ( player ==  WHITE ) ? GameWhiteTimeLeft : GameBlackTimeLeft; }
    
    public String getTimeLeft()
    { return getTimeLeft(GameWhoseTurn); }
    public String getTimeLeft(int player)
    {
        double secs = ( player ==  WHITE ) ? GameWhiteTimeLeft : GameBlackTimeLeft;
        int mins = ( (int)secs / 60 ) % 60;
        int hours = ( (int)secs / (60*60) ) % 24;
        int days = ( (int)secs / (60*60*24) );
        StringBuilder sb = new StringBuilder();
        if ( days > 0 ) sb.append(days).append("D:");
        if (hours > 0 ) sb.append(String.format("%d:", hours));
        sb.append( String.format("%02d:", mins) );
        if ( secs <= 10 ) sb.append( String.format("%05.2f", secs % 60) );
        else sb.append( String.format("%02.0f", secs % 60) );
        return sb.toString();
    }
    
    /**
     * @return string containing a description of the current game state.
     */
    public String getGameStatus() { return getGameStatusText(GameState); }
    public int getGameStateCode() { return this.GameState; }
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
    public ChessPiece[][] getBoard()    // TODO: make private
    { 
        ChessPiece[][] copy = new ChessPiece[BOARD_NUMBER_RANKS][BOARD_NUMBER_FILES];
        for (int i = 0; i < BOARD_NUMBER_RANKS; i++)
            copy[i] = GameBoard[i].clone();
        return copy;
    } 
    
    /**
     * Gets all the ChessPiece references in an ArrayList
     * @return ArrayList containing references to the pieces
     */
    public ArrayList<ChessPiece> getPieces()
        { return (ArrayList<ChessPiece>) GamePieces.clone(); }
    
    
    
    /**
     * Adds a game state listener.
     * @param listener implements GameListener interface
     */
    public void addGameStateListener(GameListener listener)
    { GameStateListeners.add(listener); }
    

    
    /**
     * Gets the move history 
     * @return a String containing all the moves, 
     *         one line per turn
     */
    public String getCompleteMoveHistory()
    {
        StringBuilder history = new StringBuilder();
        boolean whitesTurn = true;
        for ( RecordOfMove item : GameHistory )
        {
            if ( whitesTurn )
                history.append(item.moveNumber).append(". ");
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
        if ( GamePieces != null && !GamePieces.isEmpty() )
            GamePieces.clear();
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
     * @param player either Game.WHITE or Game.BLACK
     */
    public void resign(int player)
    {   
        if ( !this.isGameActive ) return;
        if ( player == WHITE )
            endTurn(STATUS_BLACK_WINS_RESIGNATION);
        else if ( player == BLACK )
            endTurn(STATUS_WHITE_WINS_RESIGNATION); 
    }
    
    /**
     * The current player offers/claims a draw
     */
    public void draw()
    {   draw(GameWhoseTurn ); }
    
    /**
     * Player offers/claims a draw
     * @param player color of offering player. either Game.WHITE or Game.BLACK
     */
    public void draw(int player)    
    {   if ( !this.isGameActive ) return;
        // check for threefold repetition
        if ( player == GameWhoseTurn && drawThreefoldRepetition() )
        {
            if (GameWhoseTurn == WHITE) endTurn(STATUS_DRAW_WHITE_CLAIMS_THREE);
            else endTurn(STATUS_DRAW_BLACK_CLAIMS_THREE);
            return;
        } else if ( player == GameWhoseTurn && drawFiftyMoves() ) {
            if (GameWhoseTurn == WHITE) endTurn(STATUS_DRAW_WHITE_CLAIMS_FIFTY);
            else endTurn(STATUS_DRAW_BLACK_CLAIMS_FIFTY);
            return;
        }
                
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
     * @param move Ex: "a1-b2", "a1 a2", "e4xe5", "a7-a8=Q"
     * the third character can be any character, and extraneous
     * characters are ignored
     * @return an integer callback code MoveCode. You can use boolean Game.isMoveCodeLegal(Code)
     *         and String Game.getMoveCodeText(Code)
     * regex pattern search:  [ex ([a-h][1-8])  ]
     * (0) check for castle O-O-O or O-O
     * (1) try to match two coords: ([a-h][1-8]).*([a-h][1-8])
     * (2) try disamgious coord: ([PRNBQK][a-h1-7]).*([a-h][1-8])
     * (3) try normal match: ([PRNBQK])([a-h][1-8])
     * (4) pawn move match: ([a-h][1-8])
     */
    public int makeMove(String move) // TODO: make this work with normal move commands
    {
        if ( matcherCastle.reset(move).find() ) { // castling check
            
            System.out.println("pattern Castle matched!["
                    + matcherCastle.group(1)+"]["+matcherCastle.group(2)+"]["
                    + matcherCastle.group(3) +"]"); //DEBUG   
            // TODO: implement
            King king = ((King)getKing());//.castle
            
        } else if ( matchere2e4.reset(move).matches() ) { // full coordinate check
            
            String from = matchere2e4.group(1); // from location
            String to = matchere2e4.group(2); // to location
            System.out.println("pattern e2e4 matched!["+from+"]["+to+"]["
                    + matchere2e4.group(3) +"]" ); //DEBUG   
            //matcherPromo.reset(  matchere2e4.group(3) );
            
            if ( matchere2e4.group(3).equals("") )
                return makeMoveIn(
                    convertInRankFromAlgebraic(from),
                    convertInFileFromAlgebraic(from),
                    convertInRankFromAlgebraic(to),
                    convertInFileFromAlgebraic(to)    );
            else
                return makeMoveIn(
                    convertInRankFromAlgebraic(from),
                    convertInFileFromAlgebraic(from),
                    convertInRankFromAlgebraic(to),
                    convertInFileFromAlgebraic(to), 
                    matchere2e4.group(3).charAt(0) );
            
            
        } else if ( matcherPee4.reset(move).matches() ) {  // disambiguous check
            
            String piece = matcherPee4.group(1); // from piece
            String loc = matcherPee4.group(2); // from location
            String to = matcherPee4.group(3); // to location
            String promo = matcherPee4.group(4);
            
            System.out.println("pattern Pee4 matched!["
                    + piece + "][" + loc + "]["
                    + to + "]["
                    + promo +"]" ); //DEBUG
            
            ChessPiece p;
            try { p = this.matchPiece(piece, loc, to); }
            catch (IllegalArgumentException ex) { return AMBIGUOUS_MOVE; }
            if ( p == null ) return MOVE_ILLEGAL;
            System.out.println("Found: "+ p.getUnicode() + p.getPosition() );
            // check for promo, them p.makeMove
            //matcherPromo.reset(  matcherPee4.group(3) );
            if ( promo.equals("") )
                p.makeMoveIn(
                    convertInRankFromAlgebraic(to),
                    convertInFileFromAlgebraic(to) );
            else
                p.makeMoveIn(
                    convertInRankFromAlgebraic(to),
                    convertInFileFromAlgebraic(to),
                    promo.charAt(0) );
                
            
        }
        return MOVE_ILLEGAL;
    }
    
    
    /**
     * Checks if a move is valid using algebraic coordinates.
     * @param move Ex: "a1-b2", "a1 a2", "E4xE5", "a7-a8"
     * the third character can be any character, and extraneous
     * characters are ignored
     * @return an integer move Code. You can use boolean Game.isMoveCodeLegal(Code)
     *         and String Game.getMoveCodeText(Code)
     */
    public int validateMove(String move)
    {
        
        if ( move.length() < 5 ) 
            return INVALID_COORDINATE;
        String fromString = move.substring(0, 2);
        String toString = move.substring(3, 5);
        int fromRank = Game.convertChessRankFromAlgebraic(fromString);
        int fromFile = Game.convertChessFileFromAlgebraic(fromString);
        int toRank = Game.convertChessRankFromAlgebraic(toString);
        int toFile = Game.convertChessFileFromAlgebraic(toString);
        return this.validateMove(fromRank, fromFile, toRank, toFile);        
    }
    
    
    /**
     * Checks a move to see if its valid. Returns a move Code. You can examine the code 
     * with Game.getMoveCodeText(Code) and Game.isMoveCodeLegal(Code)
     * @param fromChessRank int 1-8
     * @param fromChessFile int 1-8 (corresponding to files a-h)
     * @param toChessRank int 1-8
     * @param toChessFile int 1-8 (corresponding to files a-h)
     * @return int Code
     */
    public int validateMove(int fromChessRank, int fromChessFile, int toChessRank, int toChessFile)
    {
        if ( !isValidChessCoord(fromChessRank, fromChessFile) 
                || !isValidChessCoord(toChessRank, toChessFile) ) 
            return INVALID_COORDINATE;
        
        if ( !this.isSquareOccupied(fromChessRank, fromChessFile) ) 
            return Game.MOVE_ILLEGAL_SQUARE_EMPTY;
        
        int r = Game.convertInRankFromChessRank(toChessRank);
        int f = Game.convertInFileFromChessFile(toChessFile);
        return this.getPieceAt(fromChessRank, fromChessFile).validateMoveIn(r, f);
    }
    
    /**
     * Makes a move using chess coordinates
     * @param fromChessRank value from 1-8
     * @param fromChessFile value from 1-8
     * @param toChessRank value from 1-8
     * @param toChessFile value from 1-8
     * @return an integer callback code MoveCode. You can use boolean Game.isMoveCodeLegal(Code)
     *         and String Game.getMoveCodeText(Code)
     */
    public int makeMove(int fromChessRank, int fromChessFile, int toChessRank, int toChessFile)
    {   return makeMoveIn(
                Game.convertInRankFromChessRank(fromChessRank),
                convertInFileFromChessFile(fromChessFile),
                Game.convertInRankFromChessRank(toChessRank),
                convertInFileFromChessFile(toChessFile)          );
    }
    protected int makeMoveIn(int fromInRank, int fromInFile, int toInRank, int toInFile)
    {   
        if ( !isValidInCoord(fromInRank, fromInFile) || !isValidInCoord(toInRank, toInFile) ) 
            return INVALID_COORDINATE;
        // check if piece exist at "from" coord
        if ( GameBoard[fromInRank][fromInFile] == null )
            return MOVE_ILLEGAL_SQUARE_EMPTY;
        else
            return GameBoard[fromInRank][fromInFile].makeMoveIn(toInRank, toInFile);
    }
    
    
    /**
     * Makes a move using chess coordinates, providing promotion type if needed
     * @param fromChessRank value from 1-8
     * @param fromChessFile value from 1-8
     * @param toChessRank value from 1-8
     * @param toChessFile value from 1-8
     * @param promotionType The piece to promote to. QUEEN, BISHOP, KNIGHT, ROOK
     * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
     */
    public int makeMove(int fromChessRank, int fromChessFile, 
            int toChessRank, int toChessFile, char promotionType )
    {   return makeMoveIn(
                Game.convertInRankFromChessRank(fromChessRank),
                convertInFileFromChessFile(fromChessFile),
                Game.convertInRankFromChessRank(toChessRank),
                convertInFileFromChessFile(toChessFile),
                promotionType );
    }
    protected int makeMoveIn(int fromRank, int fromFile, int toRank, int toFile, char promotionType)
    {
        if ( !isValidInCoord(fromRank, fromFile) || !isValidInCoord(toRank, toFile) )
            throw new IllegalArgumentException("Invalid arguements for makeMove"); // TODO: soft fail
        // call makeMove(rank,file,promotionType) on the chess piece!
        if ( GameBoard[fromRank][fromFile] == null )
            return MOVE_ILLEGAL_SQUARE_EMPTY;
        else
            return GameBoard[fromRank][fromFile].makeMoveIn(toRank, toFile, promotionType);
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
        int kingInRank = king.inRank;
        int kingInFile = king.inFile;
        return isInCheck(color, kingInRank, kingInFile);
    }
    
    /**
     * Check to see if king would be in check at a coordinate
     * @param color color of king to examine
     * @param inRank rank of square to examine
     * @param inFile file of square to examine
     * @return true if in check, false if not
     */
    private boolean isInCheck(int color, int inRank, int inFile)
    {
        
        for ( ChessPiece piece : GamePieces )
        {
            // skip if color matches kings color, or if inactive
            if ( !piece.isActive || piece.getColor() == color )
                continue;
            
            if ( piece.isObservingIn(inRank, inFile) == PIECE_IS_OBSERVING )
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
            for ( ChessPiece piece : GamePieces )
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
        
        for( ChessPiece piece : GamePieces )
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
            GamePieces.remove(piece); // remove the piece
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
        if ( GameHistory != null && !GameHistory.isEmpty() )
            GameHistory.clear();
    }
    private void clearBoard()
    {
        // clear game board
        for (int i = 0; i < BOARD_NUMBER_RANKS; i++)
            for (int j = 0; j < BOARD_NUMBER_FILES; j++)
                GameBoard[i][j] = null;
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
    { for (GameListener listener : GameStateListeners)
            listener.onGameStateUpdate(new GameStats()); 
    }
    
    /**
     * pushes an update of the game state to all game state listeners
     */
    private void pushGameOverUpdate()
    { for (GameListener listener : GameStateListeners)
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
        int kingRank = king.inRank;
        int kingFile = king.inFile;
        
        // get checking piece(s)
        ChessPiece checkingPiece = null;
        boolean doubleCheck = false;

        for( ChessPiece piece : GamePieces )
        {
            if ( !piece.isActive || piece.Color == color ) // skip is inactive or same color
                continue;
            if ( piece.isObservingIn(kingRank, kingFile) == PIECE_IS_OBSERVING )
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
            int cInRank = checkingPiece.inRank;
            int cInFile = checkingPiece.inFile;
            ArrayList<Square> interveningSquares 
                    = Square.getInterveningSquares(
                            kingRank, kingFile, cInRank, cInFile );
            
            // get intervening squares
            interveningSquares.add( new Square(cInRank, cInFile) );
            
            // check for odd case when en passant saves king from checkmage
            if ( checkingPiece.getType() == PAWN && checkingPiece.MoveCount == 1 
                && ( ( color == WHITE && checkingPiece.inRank == 4 ) 
                    || ( color == BLACK && checkingPiece.inRank == 3 ) ) )
            {
                int enPassantInRank = (color == WHITE) ? 5 : 2;
                if ( isValidInCoord(enPassantInRank,cInFile) )
                    interveningSquares.add( new Square(enPassantInRank,cInFile) );
            }
            
            // check all pieces for captures or blocks
            for( ChessPiece piece : GamePieces )
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

        GamePieces.add(newPiece);
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
        if ( GameBoard[inRank][inFile] != null )  // position is already occupied!
            throw new IllegalStateException("Cannot add piece on occupied square");   
        ChessPiece newPiece = addPieceToGame(color, type);
        //if ( newPiece == null )
        //    return null;
        newPiece.setPositionIn(inRank, inFile);
        // Set startRank, startFile
        newPiece.StartInRank = inRank;
        newPiece.StartInFile = inFile;
        return newPiece;
    }
    
    private ChessPiece getKing()
    { return getKing(GameWhoseTurn); }
    
    private ChessPiece getKing(int color)
    {
        int index = getKingIndex(color);
        if (index == -1)
            throw new IllegalStateException("King not found!");
        return GamePieces.get(index);
    }
    
    private int getKingIndex(int color)
    {
        int kingIndex = (color == WHITE) ? WhiteKingIndex : BlackKingIndex;
        if ( kingIndex != -1 ) 
            return kingIndex;
        
        
        for (int i = 0; i < GamePieces.size(); i++ )
            if ( GamePieces.get(i).getType() == KING
                    && GamePieces.get(i).getColor() == color )
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
    
    public static int convertChessRankFromAlgebraic(String coord)
    { return  Character.getNumericValue(coord.toLowerCase().charAt(1)); }
    
    public static int convertChessFileFromAlgebraic(String coord)
    { return (int)coord.toLowerCase().charAt(0) - (int)'a' + 1; }
    
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
    
    private static int convertChessRankFromInRank(int inRank)
    {   return inRank + 1; }

    private static int convertChessFileFromInFile(int inFile)
    {   return inFile + 1; }
    
    private static String convertAlgebraicFromIn(int inRank, int inFile) // TODO: return to private
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
    { return (rank+file)%2; }
    
    public static boolean isMoveCodeLegal(int code)
    {   return code == MOVE_LEGAL || code == MOVE_LEGAL_EN_PASSANT
                || code == MOVE_LEGAL_CASTLE_KINGSIDE 
                || code == MOVE_LEGAL_CASTLE_QUEENSIDE ;
    }
    
    public static boolean isGameStateCodeActive(int code)
    {   return code == STATUS_WHITES_TURN || code == STATUS_BLACKS_TURN
                || code == STATUS_WHITE_IN_CHECK || code == STATUS_BLACK_IN_CHECK;
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
                return "White claims draw by three-fold repetition.";
            case STATUS_DRAW_BLACK_CLAIMS_THREE:
                return "Black claims draw by three-fold repetition.";
            case STATUS_DRAW_WHITE_CLAIMS_FIFTY:
                return "White claims draw by fifty move rule.";    
             case STATUS_DRAW_BLACK_CLAIMS_FIFTY:
                return "Black claims draw by fifty move rule.";   
            case STATUS_DRAW_AGREEMENT:
                return "Draw by agreement.";  
            case STATUS_DRAW_MATERIAL:
                return "Draw by Insufficient Material.";
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
            case MOVE_ILLEGAL_PAWN_HAS_MOVED:
                return "Pawn has already moved.";
            case MOVE_ILLEGAL_NOTHING_TO_CAPTURE:
                return "Pawn cannot capture there.";
            case MOVE_ILLEGAL_LATE_EN_PASSANT:
                return "Cannot En Passant.";
            case AMBIGUOUS_PROMOTION:
                return "Promotion ambiguous.";
            case GAME_NOT_ACTIVE:
                return "Game is not active.";
            case PIECE_NOT_ACTIVE:
                return "Piece is not in play.";
            case ILLEGAL_CASTLE_KING_HAS_MOVED:
                return "King has moved, can no longer castle.";
            case ILLEGAL_CASTLE_ROOK_HAS_MOVED:
                return" Rook has moved, can no longer castle.";
            case ILLEGAL_CASTLE_IMPEDED:
                return "Castle impeded";
            case Game.AMBIGUOUS_MOVE:
                return "Ambiguous move.";
            case INVALID_COORDINATE:
                return "Invalid coordinate";
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

    /**
     * Test if a square is occupied
     * @param fromRank 1-8
     * @param fromFile 1-8
     * @return true or false
     */
    public boolean isSquareOccupied(int fromRank, int fromFile)
    {   if (!Game.isValidChessCoord(fromRank,fromFile)) return false;
        int r = convertInRankFromChessRank(fromRank);
        int f = convertInFileFromChessFile(fromFile);
        return GameBoard[r][f] != null;
    }

    /**
     * Returns the piece at specified square, or null if the square is empty
     * @param chessRank 1-8
     * @param chessFile 1-8
     * @throws IllegalArgumentException
     * @return ChessPiece reference or null
     */
    public ChessPiece getPieceAt(int chessRank, int chessFile)
    {
        if ( !Game.isValidChessCoord(chessRank,chessFile) )
            throw new IllegalArgumentException("No such square."); 
        
        int r = convertInRankFromChessRank(chessRank);
        int f = convertInFileFromChessFile(chessFile);
        return GameBoard[r][f];
    }

    private boolean drawByInsufficientMaterial()
    {
        int wBishopCount = 0;   // bishop counters
        int bBishopCount = 0;
        int wKnightCount = 0;   // bishop counters
        int bKnightCount = 0;
        for (ChessPiece piece : GamePieces )  // scan all pieces
        {
            if (piece.isActive == false) continue;  // skip inactive pieces
            if (piece.Type == KING) continue; // skip the kings
            if (piece.Type == QUEEN || piece.Type == ROOK || piece.Type == PAWN )
                return false;   // any of these pieces active means its not a draw
            
            // count bishops
            if (piece.Type == BISHOP)
            {
                if ( piece.getColor() == WHITE ) wBishopCount++;
                else bBishopCount++;
            }
            // count knights
            if (piece.Type == KNIGHT)
            {
                if ( piece.getColor() == WHITE ) wKnightCount++;
                else bKnightCount++;
            }
        }
        
        // minor pieces are all counted
        if ( wBishopCount >= 2 || bBishopCount >= 2 ) return false; // if 2 bishops found no draw
        if ( wKnightCount >= 1 && wBishopCount >= 1 ) return false; // w can win with B and N
        return !( bKnightCount >= 1 && bBishopCount >= 1 );  // b can win with B and N
    }

    /**
     * You know you shouldn't ever use this
     * @return true if move successfully taken back
     */
    public boolean takebackMove()
    {   if ( GameHistory.isEmpty() ) return false; // no moves to take back
                
        RecordOfMove move = GameHistory.remove( GameHistory.size() - 1 );
        // System.out.println("TAKEBACK: " + move.getFullMoveText()); // DEBUG
        
        // move the piece back
        ChessPiece moved = move.PieceMoved;  // get the moved piece
        GameBoard[move.fromInRank][move.fromInFile] = null; // for weird chess960 castles
        moved.setPositionIn(move.fromInRank, move.fromInFile );
        moved.MoveCount--; // take away its move count
        GameBoard[move.toInRank][move.toInFile] = null;

        ChessPiece promo = move.PiecePromoted;  // deactive any promoted piece
        if ( promo != null )
        {   promo.isActive = false;
            promo.Status = PIECE_NOT_ACTIVE;
            GameBoard[promo.inRank][promo.inFile] = null;
            promo.updateListeners();
        }
        
        ChessPiece captured = move.PieceCaptured; // restore any captured piece
        if ( captured != null )
        {   captured.setPositionIn(captured.inRank, captured.inFile);
        }

        ChessPiece castled = move.RookCastled; // return any castled rooks
        if ( castled != null )
        {   GameBoard[castled.inRank][castled.inFile] = null;
            castled.setPositionIn(castled.StartInRank, castled.StartInFile );
            castled.MoveCount--;
        }
        
        isGameActive = true;
        GameWhoseTurn = ( GameWhoseTurn == WHITE ) ? BLACK : WHITE;
        GameState = ( GameWhoseTurn == WHITE ) ? STATUS_WHITES_TURN : STATUS_BLACKS_TURN;
        GameTurnCount--;
        GameWhiteOffersDraw = false;
        GameBlackClaimsDraw = false; // you don't get any time back you cheater!

        // remove the record, put it in future
        GameFuture.add( move );
        return true;
    }
    
    /**
     * Redo a move after takeback()
     * @return true unless no more moves remain
     */
    public boolean redo()
    {   if ( GameFuture.isEmpty() ) return false; // no moves
        RecordOfMove move = GameFuture.remove( GameFuture.size() - 1 );
        System.out.println("redo: " + move.getFullMoveText()); // DEBUG
        
        if ( move.PieceCaptured != null ) move.PieceCaptured.captured(); // capture peice

        if ( move.RookCastled != null )
        {   // castling
            if ( move.PieceMoved.inFile < move.RookCastled.inFile )
            {          // castling kingside
                move.PieceMoved.updateChessPieceIn(move.toInRank, 6);
                move.RookCastled.updateChessPieceIn(move.toInRank, 5);
            } else {   // castling queenside
                move.PieceMoved.updateChessPieceIn(move.toInRank, 2);
                move.RookCastled.updateChessPieceIn(move.toInRank, 3);                
            }
        } else {    // normal move
            move.PieceMoved.updateChessPieceIn(move.toInRank, move.toInFile);
        }
        // remove the future move and put it in history
        GameHistory.add( move );
        
        isGameActive = !(move.Checkmate) && !(move.Stalemate);
        GameWhoseTurn = ( GameWhoseTurn == WHITE ) ? BLACK : WHITE;
        GameState = ( GameWhoseTurn == WHITE ) ? STATUS_WHITES_TURN : STATUS_BLACKS_TURN;
        GameTurnCount++;
        GameWhiteOffersDraw = false;
        GameBlackClaimsDraw = false;
        return true;
    }

    private boolean drawFiftyMoves()
    {
        if (GameHistory.size() < 100) return false; // TODO: check initial halfMoves if loaded from FEN
        for (int i = 1; i <= 100; i++)
        {
            RecordOfMove move = GameHistory.get( GameHistory.size() - i );
            if ( move.PieceMoved.getType() == PAWN || move.PieceCaptured != null )
                return false;
        }
        return true;  
    }
    
    

    private boolean drawThreefoldRepetition()
    {
        Game copy = new Game(this);
        String currentBoard = copy.getBoardPositionSignature();
        int repeats = 1;
        System.out.println("CUR: " + currentBoard); //DEBUG
        
        while ( copy.takebackMove() )  // TODO: take back 2 moves at a time
        {
            RecordOfMove move = copy.GameFuture.get( copy.GameFuture.size() - 1 ); // get last move
            // pawn moves or captures essentially resets the possible board positions
            if ( move.PieceMoved.getType() == PAWN || move.PieceCaptured != null ) return false;
            
            String compareBoard = copy.getBoardPositionSignature();
            System.out.println("SIG: " + compareBoard); //DEBUG
            if ( currentBoard.equals(compareBoard) ) repeats++;
            if ( repeats >= 3 ) return true;
        }
        return false;
    }

    /**
     * creates a unique string based on the board position,
     * according to threefold repetition rules
     * 123
     * WknXxXxXx
     * 1 = indicates player to move. W for white and B for black
     * 2 = indicates possible castling moves. n=none, k=kingside, q=queenside, b=both
     * 3 = whether capture en passant is possible. n=none, otherwise file number of ep move
     * Xx encodes a piece on a square
     * @return string signature code, unique for board position with available moves
    */  
    public String getBoardPositionSignature()
    {
        StringBuilder sb = new StringBuilder();
        //sb.append( ( GameWhoseTurn == WHITE ) ? 'W' : 'B'); // player to move
        
        sb.append( getCastleSignature( GameWhoseTurn ) ); // check for caslting
        sb.append( getEPSignature() ); // check for en passant
        
        // go through board
        int i = 0;
        int empty = 0;
        for ( int r = 0; r < 8; r++)
            for (int f =0; f < 8; f++ )
            {
                ChessPiece piece = GameBoard[r][f];
                if (piece != null)
                {
                    if ( empty > 0 ) sb.append(empty);
                    empty = 0;
                    char type = piece.getType();
                    if ( piece.getColor() == BLACK ) type = Character.toLowerCase(type);
                    sb.append(type);
                    //sb.append( (char)(i+'0') );
                } else {
                    empty++;
                }
                i++;
            }
        return sb.toString();
    }

    private String getCastleSignature(int player)
    {
        String fen = getCastleFEN(player);
        switch (fen)
        {
            case "KQ": return "B";
            case "kq": return "b";
            case "-":
                if ( player == WHITE ) return "=";
                return "-";
            default: return fen;
        }
    }
    

    private char getEPSignature()
    { return getEpFEN().charAt(0); }

    public String getFEN()
    {
        // board position
        StringBuilder sb = new StringBuilder( getBoardFEN() );
        
        if ( GameWhoseTurn == WHITE ) sb.append(" w "); // active player
        else sb.append(" b ");
        
        // castling availibility
        String white = getCastleFEN(WHITE);
        String black = getCastleFEN(BLACK);
        if ( !white.equals("-") )
        {   sb.append(white);
            if ( !black.equals("-") ) sb.append(black);
        } else {
            sb.append(black);
        }
        
        sb.append(" ").append( getEpFEN() ).append(" "); // ep
        sb.append(getHalfMoveClock()).append(" "); // half moves
        sb.append(this.getMoveNumber()); // move number
        return sb.toString();
    }
    
    private String getCastleFEN(int player)
    {
        King king = (King)this.getKing( player );
        ChessPiece kingRook = king.getCastlingRook(king.inRank, king.inFile+1);
        ChessPiece queenRook = king.getCastlingRook(king.inRank, king.inFile-1);
        if ( king.MoveCount != 0 ) return "-"; //cannot castle
        boolean kingside = kingRook != null && kingRook.MoveCount == 0;
        boolean queenside = queenRook != null && queenRook.MoveCount == 0;
        if ( !kingside && !queenside ) return "-";
        
        StringBuilder sb = new StringBuilder();
        if ( kingside )
            if ( player == WHITE ) sb.append('K');
            else sb.append('k');
        if ( queenside )
            if ( player == WHITE ) sb.append('Q');
            else sb.append('q');
        return sb.toString();
    }

    private String getBoardFEN()
    {
        StringBuilder sb = new StringBuilder();
        for ( int r = BOARD_NUMBER_RANKS; r > 0; r-- )
        {
            int emptyCount = 0;
            for ( int f = 1; f <= BOARD_NUMBER_FILES; f++ )
            {
                ChessPiece piece = this.getPieceAt(r, f);
                if ( piece == null )
                {   emptyCount++;
                    continue;
                }
                if ( emptyCount > 0 )
                {   sb.append(emptyCount);
                    emptyCount = 0;
                }
                if ( piece.getColor() == WHITE )
                    sb.append( piece.getType() );
                else
                    sb.append( Character.toLowerCase( piece.getType() ) );
            }
            if ( emptyCount > 0 ) sb.append(emptyCount);
            if ( r != 1 ) sb.append('/');
        }
        return sb.toString();
    }

    private String getEpFEN()
    {
        int dir = (GameWhoseTurn == WHITE) ? 1 : -1;
        RecordOfMove lastMove = getLastMove();
        if ( lastMove == null ) return "-";
        if ( lastMove.PieceMoved.Type == PAWN && lastMove.fromInRank == lastMove.toInRank + 2*dir )
            return Game.convertAlgebraicFromIn(lastMove.toInRank+dir, lastMove.toInFile);
        else
            return "-";
    }
    
    /**
     * @return number of moves since capture or pawn move
     */
    private int getHalfMoveClock()
    { 
        if ( GameHistory.isEmpty() ) return 0;
        int numMoves = GameHistory.size();
        for (int i = 1; i <= numMoves; i++ )
        {
            RecordOfMove move = GameHistory.get( numMoves - i );
            if ( move.PieceMoved.getType() == PAWN || move.PieceCaptured != null )
                return i - 1;
        }
        return numMoves; // TODO: add initial halfMoves if loaded from FEN
    }
    
    private RecordOfMove getLastMove()  // TODO: subsitute functions where possible
    {   if ( GameHistory.isEmpty()  ) return null;
        return GameHistory.get( GameHistory.size() - 1 );
    }

    private void initPatterns()
    {
        try
        {
            // TODO: add piece disambiguoate matcher
            
            matchere2e4 = Pattern.compile(REGEX_MOVE_e2e4).matcher("");
            matcherCastle = Pattern.compile(REGEX_CASTLE).matcher("");
            matcherPee4 = Pattern.compile(REGEX_MOVE_Pee4).matcher("");    
            //matcherPromo = Pattern.compile(REGEX_PROMO).matcher("");  
            //matcherDisambig = Pattern.compile(REGEX_DISAMBIG).matcher("");
        } catch(PatternSyntaxException pse) { // DEBUG
                System.out.println("There is a problem" +
                               " with the regular expression!");
                System.out.println("The pattern in question is: "+
                               pse.getPattern());
                System.out.println("The description is: "+
                               pse.getDescription());
                System.out.println("The message is: "+
                               pse.getMessage());
                System.out.println("The index is: "+
                               pse.getIndex());
        }
              
    }

    private ChessPiece matchPiece(String typeStr, String location, String to)
    {
         char type = 'P';
        if ( !typeStr.equals("") ) type = typeStr.charAt(0);
        int disambig = -1;
        boolean disambigRank = false;
        boolean disambigFile = false;
        if ( location.matches("[a-h]") )
        {
            disambigFile = true;
            disambig = convertInFileFromAlgebraic( location );
        } else if ( location.matches("[1-8]") ) {
        
            disambigRank = true;
            disambig = convertInRankFromAlgebraic( "x" + location );
        }
        
        // search ChessPieces
        ChessPiece match = null;
        for ( ChessPiece piece : this.GamePieces )
        {
            if ( piece.Color != GameWhoseTurn ) continue; // piece must be for current player
            if ( piece.Type != type ) continue; // must have correct type
            if ( !piece.isActive ) continue; // piece must be active
            // try to disambiguate
            if ( disambigFile && piece.inFile != disambig ) continue;
            if ( disambigRank && piece.inRank != disambig ) continue;
            // can the piece see the target
            if ( PIECE_IS_OBSERVING != piece.isObservingIn(
                    convertInRankFromAlgebraic(to), convertInFileFromAlgebraic(to)) )
                continue;  // skip if piece is not observing to square
            // at this point, we have a hit
            if ( match != null ) 
                throw new IllegalArgumentException("Can not disambiguate move.");   // already had a hit, cannot disambiguate
            match = piece;
        }
        return match;
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
        protected ArrayList<PieceListener> PieceListeners; 
        
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
            PieceListeners = new ArrayList<>();
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
        { PieceListeners.add(listener); }
        
        /**
         * Makes a move given the algebraic coordinate of target square
         * @param coord algebraic coordinate to move to, along with promotion 
         *              if needed. ex: "e4", "c1", "b8=Q"
         * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
         */
        public int makeMove(String coord)
        {
            return makeMoveIn(Game.convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord) );
        }
        
        /**
         * Makes a move given a target square
         * @param square object representing target square
         * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
         */
        public int makeMove(Square square)
        { return makeMoveIn(square.inRank, square.inFile); }
        
        /**
         * Validates a move given a target square
         * @param square object representing target square
         * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
         */
        public int validateMove(Square square)
        { return validateMoveIn(square.inRank, square.inFile); }
        
        /**
         * Validates a move given a target square
         * @param coord algebraic coordinate of target square
         * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
         */
        public int validateMove(String coord)
        {   return validateMoveIn(
                    Game.convertChessRankFromAlgebraic(coord),
                    Game.convertChessFileFromAlgebraic(coord)  );
        }
        
        /**
         * MAKES THE MOVE! after validating the move by calling validateMove
         * @param rank value from 1-8
         * @param file value from 1-8
         * @param promotionType The piece to promote to. QUEEN, BISHOP, KNIGHT, ROOK
         * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
         */
        public int makeMove(int rank, int file, char promotionType)
        {   return makeMoveIn(
                    Game.convertInRankFromChessRank(rank),
                    Game.convertInFileFromChessFile(file), promotionType );
        } 

        
        protected int makeMoveIn(int inRank, int inFile, char promotionType)
        {
            //System.out.println("Promotion type: " + promotionType); // DEBUG
            return makeMoveIn(inRank, inFile);
        }
        
        
        /**
         * MAKES THE MOVE!
         * @param rank value from 1-8
         * @param file value from 1-8
         * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
         */
        public int makeMove(int rank, int file)
        {   return makeMoveIn(
                    Game.convertInRankFromChessRank(rank),
                    Game.convertInFileFromChessFile(file)  );
        } 
        
        protected int makeMoveIn(int inRank, int inFile)
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
            int code = validateMoveIn(inRank, inFile);
            if ( code != MOVE_LEGAL ) return code;

            // hold onto last location
            int fromInRank = this.inRank;
            int fromInFile = this.inFile;
            
            // capture piece, if any
            ChessPiece captured = GameBoard[inRank][inFile];
            if ( captured != null )
                captured.captured();

            // set the new position and update mChessBoard
            updateChessPieceIn(inRank, inFile);
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( Color == WHITE ) ? BLACK : WHITE;
            int playerStateCode = checkPlayerState(opponentColor);
            boolean check = playerStateCode == PLAYER_IN_CHECK;
            boolean checkmate = playerStateCode == PLAYER_IN_CHECKMATE;
            boolean stalemate = playerStateCode == Game.PLAYER_IN_STALEMATE;
           
            // add move to mChessHistory (pass coordinates of previous square)
            GameHistory.add(new RecordOfMove(
                    this, fromInRank, fromInFile,
                    captured, null, 
                    check, checkmate, stalemate ) );
            
            //  call EndTurn()
            endTurn(playerStateCode);

            return code;
        }
        
        

        /**
         * Validates a move.
         * @param chessRank value from 1-8
         * @param chessFile value from 1-8
         * @return returns an integer move Code. You can use isMoveCodeLegal(Code)
     *         and getMoveCodeText(Code)
         */
        public int validateMove(int chessRank, int chessFile)
        {   return validateMoveIn(
                    Game.convertInRankFromChessRank(chessRank),
                    Game.convertInFileFromChessFile(chessFile)  );
        }
        
        protected int validateMoveIn(int inRank, int inFile)
        {
            // TODO: potential optimization: track validated moves
            //       so moves don't need to be re-evaluated
            
            if ( !isValidInCoord(inRank, inFile) )
                return INVALID_COORDINATE;
            
            // square cannot be occupied by own piece
            if ( GameBoard[inRank][inFile] != null
                    && GameBoard[inRank][inFile].getColor() == Color )
                return MOVE_ILLEGAL_SQUARE_OCCUPIED;
            
            // piece must be observing the square
            int isObservingCode = isObservingIn(inRank,inFile);
            if ( isObservingCode != PIECE_IS_OBSERVING )
                return isObservingCode;
                        
            // cannot move into check:
            //      (1)update moving piece position, remove catpured piece if any
            //      (2)call isInCheck(mColor)
            //      (3)undo moving piece, undo removing captured piece
            
            int fromInRank = this.inRank;  // save current rank
            int fromInFile = this.inFile;  // and file
            
            // (1) get piece to be captured, if any
            ChessPiece captured = GameBoard[inRank][inFile];
            if ( captured != null )
                captured.isActive = false;     // temporarily deactivate
            updatePositionIn(inRank,inFile);   // (1)temporarily move the piece
            
            boolean isInCheck = isInCheck(Color);  // (2)
            
            // undo temporary move (3)
            updatePositionIn(fromInRank, fromInFile);
            GameBoard[inRank][inFile] = captured;
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
            GameBoard[inRank][inFile] = null;
            // call chess piece listener function
            if ( PieceListeners != null )
                for ( PieceListener listener : PieceListeners )
                    listener.onCapture(this);
            // DEBUG:
            //System.out.println(this.getName()+ " has been captured!");
        }
        
        /**
         * Sets the starting coordinate. Game must be inactive
         * @param coord a1, g4, etc
         * @return true if start position set, false if unable to set
         */
        public boolean setStartPosition(String coord)
        {   return setStartPositionIn(convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord) ); 
        }
        
        /**
         * Sets the starting position
         * @param rank 1-8
         * @param file 1-8
         * @return true if start position set, false if unable to set
         */
        public boolean setStartPosition(int rank, int file)
        {   return setStartPositionIn(
                       Game.convertInRankFromChessRank(rank),
                       Game.convertInFileFromChessFile(file)  );
        } 
        
        private boolean setStartPositionIn(int inRank, int inFile)
        {   if ( !isActive ) return false;
            if ( !isValidInCoord(inRank, inFile) ) return false;
            StartInRank = inRank;
            StartInFile = inFile;
            return true;
        }
        
        protected void setPosition(String coord)
        {
            setPositionIn(Game.convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord) );
        }
        
        protected void setPositionIn(int inRank, int inFile)
        {
            if ( !isValidInCoord(inRank, inFile) )
                throw new IllegalArgumentException("Illegal arguement for setPosition");

            // check if square is already occupied
            if ( GameBoard[inRank][inFile] != null )
                throw new IllegalStateException("Cannot set piece on occupied square");
            
            this.inRank = inRank;
            this.inFile = inFile;
            GameBoard[inRank][inFile] = this;
            Status = PIECE_ACTIVE;
            isActive = true;

            if ( PieceListeners != null )
                for ( PieceListener listener : PieceListeners )
                    listener.onUpdate(this);
        }
        
        // used by validateMove(). does not publish
        protected void updatePositionIn(int inRank, int inFile)
        {
            // set current position to null
            GameBoard[this.inRank][this.inFile] = null;
            // set reference to this piece at new square
            GameBoard[inRank][inFile] = this;
            // set the position in the piece
            this.inRank = inRank;
            this.inFile = inFile;
        }
        
        /**
         * Updates the position of the piece and publishes to listeners
         * @param inRank rank and
         * @param inFile file of new position
         */
        protected void updateChessPieceIn(int inRank, int inFile)
        {
            
            // increment move counter
            MoveCount++;
            updatePositionIn(inRank, inFile);
            
            // call liseners
            if ( PieceListeners != null )
                for ( PieceListener listener : PieceListeners )
                    listener.onMove(this);
        }
        
        /**
         * Returns PIECE_IS_OBSERVING if true
         * @param chessRank value from 1-8
         * @param chessFile value from 1-8
         * @return Game.PIECE_IS_OBSERVING or an int move Code
         */
        public int isObserving(int chessRank, int chessFile)
        {   if (!Game.isValidChessCoord(chessRank, chessFile))
                return Game.INVALID_COORDINATE;
        
            return isObservingIn( convertInRankFromChessRank(chessRank),
                convertInFileFromChessFile(chessFile) ); }
        
        
        abstract protected int isObservingIn(int rank, int file);
        
        /**
         * returns true if the piece has a valid move
         * @return true or false
         */
        public boolean hasValidMove()
        {   if ( !isActive ) return false;
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
        public int getRank() { return Game.convertChessRankFromInRank(inRank); }
        public int getFile() { return Game.convertChessFileFromInFile(inFile); }
        public char getType() { return Type; }
        public int getColor() { return Color; }
        public int getStatus() { return Status; }
        public int getMoveCount() { return MoveCount; }
        public String getName() { return Game.getName(Type); }
        public char getUnicode() { return Game.getUnicode(Color, Type); }

        private void reset()
        {
            if ( !isValidInCoord(StartInRank,StartInFile) )
            {   // if no valid start position deactivate
                isActive = false;
            } else {
                // Resets the piece to its starting position
                MoveCount = 0;
                setPositionIn(StartInRank,StartInFile);
            }
        }
        
        public void releaseListeners()
        {
            if ( !PieceListeners.isEmpty() )
                PieceListeners.clear();
        }

        private void updateListeners()
        {
            if ( PieceListeners != null )
                for ( PieceListener listener : PieceListeners )
                    listener.onUpdate(this);
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
         * TODO: implement
         * castles kingside
         * @return callback code
         */
        public int castleKingside()
        { return -1; };
        
        /**
         * TODO: implement
         * castles queenside
         * @return callback code
         */
        public int castleQueenside()
        { return -1; }
        
        /**
         * Helper function to see if player intends to castle
         * @param rank inputed rank
         * @param file inputed file
         * @return true if player is trying to castle
         */
        protected boolean isTryingToCastle(int rank, int file)
        {
            if ( Color == WHITE && inRank != 0)
                return false;
            if ( Color == BLACK && inRank != 7 ) 
                return false;
            if ( inRank != rank )
                return false;
            if ( GameBoard[rank][file] != null && 
                 GameBoard[rank][file].getColor() != Color )
                return false;
            if ( abs(inFile - file) == 2 )
                return true;
            // in case of chess960, sometimes a player needs to initiate
            // castling by moving king onto the rook
            return ( GameBoard[rank][file] != null && 
                     GameBoard[rank][file].getType() == ROOK );
        }
                
        @Override
        protected int validateMoveIn(int inRank, int inFile)
        {
            if ( isTryingToCastle(inRank,inFile) )
                return validateCastle(inRank,inFile);
            else
                return super.validateMoveIn(inRank, inFile);
        }

        @Override
        protected int makeMoveIn(int rank, int file) 
        {
            if ( !isTryingToCastle(rank, file) )
                return super.makeMoveIn(rank, file);
            return tryToCastle(rank,file);
        }
        
        /**
         * Attempts to castle if legal
         * @param rank inputed rank
         * @param file inputed file
         * @return MOVE_LEGAL_CASTLE if successful, otherwise returns
         *         an error code
         */
        protected int tryToCastle(int rank, int file)
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
            int fromRookRank = castlingRook.inRank;
            int fromRookFile = castlingRook.inFile;

            // castling which way
            boolean isCastlingKingside = fromRookFile > fromFile;
            
            int toKingFile = isCastlingKingside ? 6 : 2;
            int toRookFile = isCastlingKingside ? 5 : 3;
            

            // update king and rook
            updateChessPieceIn(fromRank, toKingFile);
            castlingRook.updateChessPieceIn(fromRookRank, toRookFile);
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( Color == WHITE ) ? BLACK : WHITE;
            int playerStateCode = checkPlayerState(opponentColor);
            boolean check = playerStateCode == PLAYER_IN_CHECK;
            boolean checkmate = playerStateCode == PLAYER_IN_CHECKMATE;
            boolean stalemate = playerStateCode == Game.PLAYER_IN_STALEMATE;
            
             //  change this call
            GameHistory.add(new RecordOfMove(
                    this, fromRank, fromFile,
                    castlingRook,
                    check, checkmate, stalemate  ) );

            endTurn(playerStateCode);
            return code;
        }
        
        protected ChessPiece getCastlingRook(int toRank, int toFile)
        {
            // returns the rook to castle with
            int kingFile = inFile;
            int sign = Integer.signum( toFile - kingFile );
            
            for ( int i = kingFile + sign; (i >= 0 && i < BOARD_NUMBER_FILES); i += sign )
            {
                if ( GameBoard[toRank][i] != null && 
                        GameBoard[toRank][i].getType() == ROOK )
                    return GameBoard[toRank][i];
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
        protected int validateCastle(int rank, int file)
        {
            // has king already moved?
            if ( MoveCount != 0 ) return ILLEGAL_CASTLE_KING_HAS_MOVED;
            // get the rook
            ChessPiece castlingRook = getCastlingRook(rank,file);
            if ( castlingRook == null ) return ILLEGAL_CASTLE_ROOK_HAS_MOVED;
            if ( !castlingRook.isActive ) return MOVE_ILLEGAL;
            // rook cannot have made a move already
            if ( castlingRook.MoveCount != 0 ) return ILLEGAL_CASTLE_ROOK_HAS_MOVED;
            
            int rookFile = castlingRook.inFile;
            int kingFile = this.inFile;
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
            {   ChessPiece square = GameBoard[rank][i];
                if ( square == null )
                    continue;
                if ( square != this && square != castlingRook )
                    return ILLEGAL_CASTLE_IMPEDED;
            }

            // temporarily deactive rook
            if ( GameBoard[rank][rookFile] != castlingRook)
                return MOVE_ILLEGAL;
            GameBoard[rank][rookFile] = null;
            
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
            GameBoard[rank][rookFile] = castlingRook;
            
            if ( throughCheck ) return ILLEGAL_CASTLE_THROUGH_CHECK;
            return isCastlingKingside ? MOVE_LEGAL_CASTLE_KINGSIDE : MOVE_LEGAL_CASTLE_QUEENSIDE;     
        }
        

        @Override
        protected int isObservingIn(int rank, int file) 
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
        protected int isObservingIn(int rank, int file)
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
                    if ( GameBoard[inRank][inFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( inFile == file ) {           // check if on same file
                int d = inRank - rank;
                int s = Integer.signum(rank - inRank);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+s*i, mFile)); // DEBUG
                    if( GameBoard[inRank+s*i][inFile] != null )
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
                    if( GameBoard[inRank+sl*s*i][inFile+s*i] != null )
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
        protected int isObservingIn(int rank, int file)
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
                    if ( GameBoard[inRank][inFile+s*i] != null )
                        return MOVE_ILLEGAL_IMPEDED;
                }
                return PIECE_IS_OBSERVING;
            } else if ( inFile == file ) {           // check if on same file
                int d = inRank - rank;
                int s = Integer.signum(rank - inRank);
                for (int i = 1; abs(d+i*s) > 0; i++)
                {
                    //System.out.println("isObserving Checking " + Chess.convertInternalToAlgebraic(mRank+s*i, mFile)); // DEBUG
                    if( GameBoard[inRank+s*i][inFile] != null )
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
        protected int isObservingIn(int rank, int file)
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
                    if( GameBoard[inRank+sl*sign*i][inFile+sign*i] != null )
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
        protected int isObservingIn(int rank, int file)
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
            return makeMoveIn(Game.convertInRankFromAlgebraic(coord),
                    Game.convertInFileFromAlgebraic(coord), promoType );
        }
        
        @Override
        protected int makeMoveIn(int rank, int file)
        {
            return makeMoveIn(rank, file, ' ' );
        }
        
        @Override
        protected int makeMoveIn(int inRank, int inFile, char promotionType)
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
            int code = validateMoveIn(inRank, inFile);
            if ( code != MOVE_LEGAL && code != MOVE_LEGAL_EN_PASSANT ) return code;
            
            
            ChessPiece promotion = null;
            // check for promotion
            if ( (Color == WHITE && inRank == 7 ) 
                    || (Color == BLACK && inRank == 0 ) )
            {
                promotionType = Character.toUpperCase(promotionType);
                // get ready to promote!
                if ( !isValidPromotionType(promotionType) )
                    return AMBIGUOUS_PROMOTION; // not so fast
                promotion = addPieceToGame(Color,promotionType);
            }
            
            // capture piece, if any. check for En Passant
            ChessPiece captured;
            if ( code == MOVE_LEGAL_EN_PASSANT )
                captured = GameBoard[this.inRank][inFile];
            else
                captured = GameBoard[inRank][inFile];
            if ( captured != null )
                captured.captured();
            
            // hold onto last location
            int fromRank = this.inRank;
            int fromFile = this.inFile;
            
            // set the new position and update mChessBoard
            updateChessPieceIn(inRank, inFile);
            
            if ( promotion != null )
            {
                // promoting
                isActive = false;
                Status = PIECE_PROMOTED;
                GameBoard[inRank][inFile] = null;
                promotion.setPositionIn(inRank, inFile);
                promotion.StartInRank = 8;
                promotion.StartInFile = this.StartInFile;
                
                // call onPromoted callback
                if ( PieceListeners != null )
                    for ( PieceListener listener : PieceListeners )
                        listener.onPromote(this,promotion);
            }
            // add promoted piece at rank,file, if needed
            
            // check for checks, checkmate, stalemate or draw
            int opponentColor = ( Color == WHITE ) ? BLACK : WHITE;
            int playerStateCode = checkPlayerState(opponentColor);
            boolean check = playerStateCode == PLAYER_IN_CHECK;
            boolean checkmate = playerStateCode == PLAYER_IN_CHECKMATE;
            boolean stalemate = playerStateCode == Game.PLAYER_IN_STALEMATE;
            

            //  pass promotion reference
            // add move to mChessHistory (pass coordinates of previous square)
            GameHistory.add(new RecordOfMove(
                    this, fromRank, fromFile,
                    captured, promotion, 
                    check, checkmate, stalemate   ) );
            
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
        protected int validateMoveIn(int rank, int file) 
        {
            if ( !isValidInCoord(rank, file) )
                return INVALID_COORDINATE;
            
            // square cannot be occupied by own piece
            if ( GameBoard[rank][file] != null
                    && GameBoard[rank][file].getColor() == Color )
                return MOVE_ILLEGAL_SQUARE_OCCUPIED;
            
            
            int direction = (Color == WHITE) ? 1 : -1;
            ChessPiece captured = null;
            boolean enPassant = false;
            if ( (inFile == file) && (inRank + direction == rank ) )  
            {
                // moving forward one square
                if ( GameBoard[rank][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
            } else if ( (inFile == file) && (inRank + 2*direction == rank ) ) { 
                // moving forward two squares
                if ( Color ==  WHITE && inRank != 1 )
                    return MOVE_ILLEGAL;
                if ( Color ==  BLACK && inRank != 6 )
                    return MOVE_ILLEGAL;
                if ( GameBoard[inRank + direction][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( GameBoard[rank][file] != null )
                    return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( MoveCount != 0 )
                    return MOVE_ILLEGAL_PAWN_HAS_MOVED;
                
            } else if ( (abs(inFile - file) == 1) && (inRank + direction == rank) ) {
                // capturing a piece
                captured = GameBoard[rank][file];
                if ( GameBoard[rank][file] == null )
                {
                    // nothing to capture unless en passant is possible
                    if ( GameBoard[inRank][file] == null ) // en passant square is empty
                        return MOVE_ILLEGAL_NOTHING_TO_CAPTURE;
                    // check if we are on white's 5th rank
                    if ( Color == WHITE && inRank != 4 )
                        return MOVE_ILLEGAL;
                    // check if we are on black's 5th rank
                    if ( Color == BLACK && inRank != 3 )
                        return MOVE_ILLEGAL;
                    if (  GameBoard[inRank][file].getColor() == Color )
                        return MOVE_ILLEGAL;
                    if ( GameBoard[inRank][file].getType() != PAWN )
                        return MOVE_ILLEGAL;
                    // we know its a pawn on our 5th rank, possible E.P. check last move
                    ChessPiece neighborPawn = GameBoard[inRank][file];
                    RecordOfMove lastMove = GameHistory.get( GameHistory.size() - 1 );
                    if ( lastMove.PieceMoved != neighborPawn )
                        return MOVE_ILLEGAL_LATE_EN_PASSANT;
                    if ( lastMove.fromInRank != inRank + 2*direction )
                        return MOVE_ILLEGAL_LATE_EN_PASSANT;
                    captured = neighborPawn;
                    enPassant = true;
                    // if en passant square is an enemy pawn, 
                    //and its just moved two square, E.P. is OK
                } else if ( GameBoard[rank][file].getColor() == Color ) {
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
                    GameBoard[fromRank][file] = null;
            }
            updatePositionIn(rank,file);   // (1)temporarily move the piece
            
            boolean isInCheck = isInCheck(Color);  // (2)
            
            // undo temporary move (3)
            updatePositionIn(fromRank, fromFile);
            if ( captured != null )
            {
                captured.isActive = true;
                if ( enPassant )
                    GameBoard[fromRank][file] = captured;
                else
                    GameBoard[rank][file] = captured;
                    
            }
            
            if ( isInCheck )
                return MOVE_ILLEGAL_KING_IN_CHECK;
            
            if ( enPassant )
                return MOVE_LEGAL_EN_PASSANT;
            else
                return MOVE_LEGAL; 
        }
        
        @Override
        protected int isObservingIn(int rank, int file)
        {
            int direction = (Color == WHITE) ? 1 : -1;
            if ( abs(inFile - file) == 1 &&
                    (inRank + direction == rank) )
                return PIECE_IS_OBSERVING;
            
            if ( inFile == file )
            {
                if ( GameBoard[rank][file] != null ) return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( inRank + direction == rank ) return PIECE_IS_OBSERVING;
                if ( inRank + 2*direction != rank ) return MOVE_ILLEGAL;
                if ( GameBoard[inRank + direction][file] != null ) return MOVE_ILLEGAL_PAWN_BLOCKED;
                if ( MoveCount == 0 ) return PIECE_IS_OBSERVING;
                return Game.MOVE_ILLEGAL_PAWN_HAS_MOVED;
            }
            
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
        final public int moveNumber;
        final public int whoseTurn;
        final public String moveText;   // Readable move notation
        // The piece moved. required
        final public ChessPiece PieceMoved;
        final private int fromInRank, fromInFile;
        final private int toInRank, toInFile;
        // Captured piece. null if nothing captured
        final public ChessPiece PieceCaptured;
        final public boolean EnPassant;    // true if pawn has captured en passant
        //final private int capturedInRank, capturedInFile;
        // Piece promoted to. null if no promotion
        final public ChessPiece PiecePromoted;
        final public char promotionType;
        // Rook castled with. null if player didn't castle
        final public ChessPiece RookCastled;
        final public boolean Checkmate; // did this move produce checkmate
        final public boolean Stalemate; // or stalemate
        
        /**
         * Gets a notational representation of the move
         * @return example: "1... e7 e5"
         */
        public String getFullMoveText()
        {   return (whoseTurn == WHITE) ?  moveNumber + ". " + moveText
                    : moveNumber + "... " + moveText ;
        }
        
        @Override public String toString()
        { return moveText; }

        // getters
        public int getFromRank() {return convertChessRankFromInRank(fromInRank);}
        public int getFromFile() {return convertChessFileFromInFile(fromInFile);}
        public int getToRank() {return convertChessRankFromInRank(toInRank);}
        public int getToFile() {return convertChessFileFromInFile(toInFile);}
        public int getCapturedRank()
        {   if ( EnPassant ) return convertChessRankFromInRank(fromInRank);
            else return convertChessRankFromInRank(toInRank);        
        }
        public int getCapturedFile() {return convertChessFileFromInFile(toInFile);}
 
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
                boolean check, boolean checkmate, boolean stalemate)
        {
            PieceMoved = moved;
            fromInRank = movedFromRank;
            fromInFile = movedFromFile;
            toInRank = PieceMoved.inRank;
            toInFile = PieceMoved.inFile;
            
            PieceCaptured = captured;
            // record en passant moves
            EnPassant = PieceCaptured != null  && PieceCaptured.inRank != toInRank;

            
            PiecePromoted = promo;
            if ( PiecePromoted != null )
                promotionType = PiecePromoted.getType();
            else
                promotionType = 'x';
            
            RookCastled = null;
            this.Checkmate = checkmate;
            this.Stalemate = stalemate;
            this.whoseTurn = GameWhoseTurn;
            moveNumber = getMoveNumber();
            
            // construct move string
            StringBuilder sb = new StringBuilder();
            if ( PieceMoved.getType() == PAWN )
                sb.append(" ");
            else
                sb.append(PieceMoved.getType());
            sb.append(Game.convertAlgebraicFromIn(fromInRank, fromInFile))
            .append( (PieceCaptured == null) ? "-" : "x" )
            .append(Game.convertAlgebraicFromIn(toInRank, toInFile))
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
                ChessPiece castledRook, 
                boolean check, boolean checkmate, boolean stalemate)
        {
            PieceMoved = moved;
            fromInRank = movedFromRank;
            fromInFile = movedFromFile;
            toInRank = PieceMoved.inRank;
            toInFile = PieceMoved.inFile;

            RookCastled = castledRook;
            if ( RookCastled == null )
                throw new IllegalArgumentException("Called wrong RecordOfMove constructor");
            
            PieceCaptured = null;
            EnPassant = false;
            PiecePromoted = null;
            promotionType = 'x';
            this.Checkmate = checkmate;
            this.Stalemate = stalemate;
            this.whoseTurn = GameWhoseTurn;
            moveNumber = getMoveNumber();
            
            StringBuilder sb = new StringBuilder();
            
            if ( castledRook.StartInFile < movedFromFile  )
                sb.append(" 0-0-0");
            else
                sb.append("   0-0");
            if ( checkmate )
                sb.append("#");
            else if ( check )
                sb.append("+");
            moveText = sb.toString();
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
            this.moveText = orig.moveText;
            this.PieceMoved = hashmap.get( orig.PieceMoved );
            this.toInRank = orig.toInRank;
            this.toInFile = orig.toInFile;
            this.fromInRank = orig.fromInRank;
            this.fromInFile = orig.fromInFile;
            this.PieceCaptured   = hashmap.get( orig.PieceCaptured );
            this.EnPassant   = orig.EnPassant;
            this.PiecePromoted   = hashmap.get( orig.PiecePromoted ) ;
            this.promotionType   = orig.promotionType ;
            this.RookCastled   = hashmap.get( orig.RookCastled ) ;
            this.Checkmate   = orig.Checkmate;
            this.Stalemate = orig.Stalemate;
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
        
        public int getRank()
        { return Game.convertChessRankFromInRank(inRank); }
        public int getFile()
        { return Game.convertChessFileFromInFile(inFile); }
        
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

            this.move = GameHistory.get( GameHistory.size() - 1 );
        }
    }

}
 