/*
 *  This is a console based driver for the chess library
 *  This code if for developing/debugging purposes
 *  It runs in netbeans console. It will likely not working properly in other console environments
 */
package io.github.cryptomega.consolechess;

//import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Scanner;


import io.github.cryptomega.chess.Game;
import io.github.cryptomega.chess.Game.ChessPiece;
import io.github.cryptomega.chess.GameListener;
import io.github.cryptomega.chess.PieceListener;

import io.github.cryptomega.chess.PGNLoader;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Philip
 */
public class ChessDriver  
{

    // PGN loader
    private static PGNLoader loader = null;
    final private static String PGN_GAMES = "pgn\\Fischer.pgn";
    final private static int BOARD_HALF_WIDTH = (int)(45 / 2);
    final private static int REPLAY_DELAY = 650;
    
/**
 * @param args the command line arguments
 */
public static void main(String[] args)
{
    // setup and start the chess game
    Game myGame = new Game().setupStandardGame().setStartTime(1, 0).startGame();
    myGame.registerGameStateListener( new GameStateListener() ); // register listener

    // add piece liseners too all the pieces. GO CRAZY
    for ( ChessPiece piece : myGame.getPieces() )
        piece.registerPieceListener(new MyPieceListener() );

    // input string variable
    Scanner scanner = new Scanner(System.in);
    String message = "";

    OUTER:
    while (true)
    {
        printHeader(myGame);
        printBoard(myGame);
        printPrompt(message);
        message = "";
        String input = scanner.nextLine();
        
        switch ( input.toUpperCase() ) {
            case "REFRESH":
                myGame.refreshListeners();
                continue;
            case "RESTART":
                myGame.restartGame();
                continue;
            case "DRAW":
                myGame.draw();
                continue;
            case "RESIGN":
                myGame.resign();
                continue;
            case "00":
            case "EXIT":
                myGame.endGame();
                break OUTER; // exit
            case "COPY":
            case "ANALYZE":
                analyze( myGame );
                continue;
            case "TAKEBACK":
                //while( myGame.takebackMove() ) {}
                myGame.takebackMove();
                continue;
            case "TAKEBACKALL":
                while( myGame.takebackMove() ) {}
                continue;
            case "REDO":
                //while ( myGame.redo() ) {}
                myGame.redo();
                continue;
            case "":
                myGame.redo();
                continue;
            case "DEBUG":       // DEBUG
                debug(myGame);
                continue;
            case "SIG":
                message = myGame.getBoardPositionSignature();
                continue;
            case "FEN":
                message = myGame.getFEN();
                continue;
            case "LOAD":
                myGame.endGame().releaseAllListeners();
                myGame = loadPGN(PGN_GAMES);
                continue;
            case "REPLAY":
                replay(myGame);
                continue;
            case "HISTORY":
                message =  myGame.getHistory();
                continue;
            default:
                // make sure game is active
                if ( !myGame.isGameActive() ) message = "> > > Game is inactive < < <";
                
                // validate move
                else if ( input.matches("(?i)VALIDATE.*") ) message = validate(myGame, input);
                
                // additional commands with parameters can be received here
                
                else // try to make the move
                {
                    int code = myGame.makeMove(input);
                    if ( !Game.isMoveCodeLegal(code) )
                        message = "> > > " + Game.getMoveCodeText(code) + " ("+code+") < < <";
                }
        }
    }
}

public static void analyze(Game myGame)
{
    //Game analysisGame = new Game(myGame, true);
    Game analysisGame = Game.copyGameAndStealListeners(myGame);

    String input;
    Scanner scanner = new Scanner(System.in);

    while ( true )
    {
        //ChessPiece[][] board = analysisGame.getBoard();
        
        printBoard(analysisGame);

        System.out.print("Analyzing (00 to exit):");
        // TODO: analysis loop
        input = scanner.nextLine();

        if ( input.equals("00") )
            break;

        int code;    // makeMove response code
        try { code = analysisGame.makeMove(input); }
        catch (Exception e) 
        {
            System.out.println(" > > > ERROR:" + e.getMessage() + " < < <");
            continue;
        }

        System.out.println(" > > > " 
                + Game.getMoveCodeText(code)
                + " ("+code+") < < <");
    }
}


public static void printCandidateMoves(ChessPiece piece)
{
    if ( piece == null )
    {
        System.out.println("No piece there.");
        return;
    }
    System.out.print("Printing valid moves: ");
    List<Game.Square> list = piece.getValidMoves();
    for (Game.Square square: list)
    {
        System.out.print(square.toString() + ",");
    }
    System.out.println();
}

// Draws the text chess board to console with unicode 
private static void printBoard(Game game)
{
    printFirstRow();
    for (int i = 7; i >= 0; i--)
    {
        System.out.print(String.valueOf(1+i) + "\u2502 ");
        for (int j = 0; j < 8; j++)
        {
            if ( game.getPieceAt(i+1, j+1) == null )
            {
                if ( Game.getSquareColor(i,j) == Game.WHITE )
                    System.out.print('\u2001');
                else    // black 
                    System.out.print('\u2591');
            } else {
                System.out.print(game.getPieceAt(i+1, j+1).getUnicode());
            }
            if ( Game.getSquareColor(i,j) == Game.WHITE )
            {   //white
                System.out.print(" \u2502 ");
            } else { // black
                System.out.print(" \u2502 ");
            }
        }
        System.out.println();
        if ( i == 0)
        {
            printLastRow();
            break;
        }
        printRowDivider();
    }

    // column labels
    System.out.print("    ");
    for (char i = 'a'; i <= 'h'; i++)
        System.out.print(i + "\u2001\u2001 ");
    System.out.println();
}
private static void printFirstRow()
{
    System.out.print(" ");
    System.out.print("\u250C");
    for (int i = 0; i < 7; i++)
        //System.out.print("\u2500\u2500\u252C");
        System.out.print("-\u2500-\u252C");
    System.out.println("-\u2500-\u2510");
}
private static void printRowDivider()
{
    System.out.print(" \u251C");
    for (int i = 0; i < 7; i++)
        System.out.print("-\u2500-\u253C");
   System.out.println("-\u2500-\u2524");
}
private static void printLastRow()
{
    System.out.print(" ");
    System.out.print("\u2514");
    for (int i = 0; i < 7; i++)
        System.out.print("-\u2500-\u2534");
    System.out.println("-\u2500-\u2518");
}


    private static void printHeader(Game myGame)
    {
        int n = myGame.getMatchTitle().length();  // count number of characters
        n = n / 2 + BOARD_HALF_WIDTH;
        System.out.println( String.format("%1$"+n+"s", myGame.getMatchTitle() ) ); // title
        System.out.println( myGame.getLastMove() ); // last move
        
        if ( !myGame.isGameActive() ) { // if game is over
            System.out.println("GAME OVER: " + myGame.getGameStatus());
        } else {
            String status = "(" + myGame.getMoveNumber() + ") " +  myGame.getGameStatus();
            System.out.print(status);
            if ( myGame.isTimed() ) 
            {
                n = 2*BOARD_HALF_WIDTH - status.length();
                System.out.print(String.format("%1$"+n+"s", myGame.getTimeLeft() )  );
            }
            System.out.println();
        }
    }
    
    private static void printPrompt(String message)
    {
        System.out.println("  <RESTART|DRAW|RESIGN|TAKEBACK|REDO|EXIT>");
        System.out.println("     <HISTORY|FEN|ANALYZE|LOAD|REPLAY>");
        System.out.println("  <DEBUG|REFRESH|SIG|TAKEBACKALL|VALIDATE>");
        if ( !message.equals("") ) 
        {
            int n = message.contains("\n") ? 1 :
                message.length() / 2 + BOARD_HALF_WIDTH;
            System.out.println(String.format("%1$"+n+"s", message));
        }
        
        System.out.print("Enter move:");
    }
    





    private static void replay(Game myGame)
    {
        while( myGame.takebackMove() ) {}
                while ( myGame.redo() ) 
                { 
                    printHeader(myGame);
                    printBoard(myGame);
                    try { Thread.sleep(REPLAY_DELAY); }
                    catch (InterruptedException ex)
                    { Logger.getLogger(ChessDriver.class.getName()).log(Level.SEVERE, null, ex); }
                }
    }

    private static Game loadPGN(String pgn)
    {
        if ( loader == null ) loader = new PGNLoader(pgn);
        return loader.getNextGame(); 
    }

    private static String validate(Game myGame, String input)
    {
        //System.out.println("DEBUG:VALIDATE ");
        if ( input.length() <= 9 ) return "Command usage: 'validate MOVE'";
    
        String move = input.substring(8).trim();
        int code = myGame.validateMove( move );
        return "Move[" +  move + "]: " 
                + Game.getMoveCodeText(code)
                + " ("+code+")";
    }





// ********************************************************
// Game state listner callbacks
public static class GameStateListener implements GameListener
{
    @Override
    public void onGameStateUpdate(Game.State update)
    {
        System.out.println("[GS]Code(" + update.gameStateCode + "): " + update.status);
        System.out.println("[GS]winner:" + update.winner );
        System.out.println("[GS]winner:" + update.winner );
        System.out.println("[GS]move:" + update.move.toString() );
        System.out.println("[GS]isGameActive:" + update.isGameActive );
        
    }

    @Override
    public void onGameOver( Game.State update )
    {
        System.out.println();
        System.out.println("****************************************************");
        System.out.println("[GS] GAME OVER! " + update.status );
        System.out.println("****************************************************");
        System.out.println();
    }

        @Override
        public void onGameStart(Game.State update)
        {
            System.out.println("Starting new game!");
        }
    }

    /*******************************************************************
 * ************     Quick Game PieceListener     *******************
 ***************************************************************** */
public static class MyPieceListener implements PieceListener
{

    @Override
    public void onUpdate(ChessPiece piece)
    {
        System.out.println("[CP]"+piece.getUnicode() + " has updated.");
    }

    @Override
    public void onMove(ChessPiece piece)
    {
        System.out.println("[CP]"+piece.getUnicode() + " has moved.");
    }

    @Override
    public void onCapture(ChessPiece piece)
    {
        System.out.println("[CP]"+piece.getUnicode() + " has been captured.");
    }

    @Override
    public void onPromote(ChessPiece piece, ChessPiece promoted)
    {
        promoted.registerPieceListener(new MyPieceListener() );
        System.out.println("[CP]"+piece.getUnicode() + " has promoted.");
    }
}

/*******************************************************************
 * Test method for debuging
 * Just runs through a list of moves
 * @param myGame 
 ********************************************************************/
private static void debug(Game myGame)
{

     // DEBUG Premoves
     ///*
     String[] movelist = {

    "e2 e4", "e7 e5",
    "g1 f3", "g8 f6",
    "f1 c4", "f8 c5",
    "d2 d3", "d7 d6",
    "f3 h4", "f6 h5",
    "a2 a4", "e8 g8",
    "a4 a5", "g8 h8",
    "a5 a6", "h8 g8",
    "a6 b7", "g8 h8",
    "b7 a8=q", "g7 g5",
    "b2 b3", "g5 g4",
    "f2 f4", "g4 f3",

    "d3 d4", "c5 b6",
    "e1 g1", "d8 h4",
    "b1 c3", "f8 g8",
    "a8 b8", "g8 g2",
    "g1 h1",  // MATE IN ONE
             "h4 d8",
    "d4 e5", "d6 e5",
    "c4 f7", "g2 c2",
    "f7 h5", "c2 c3",
    "b8 b6", "d8 d1",
    "b6 a7", "c3 c1",
    "a1 c1", "d1 b3",
    "a7 c7", "c8 e6",
    "c7 e5", "h8 g8",
    "f1 f3", "b3 f3",
    "h1 g1", "f3 e4",
    "h2 h3", "e6 h3",
    "h5 e2", "e4 e5",
    "c1 e1", "h7 h5",
    "e2 h5", "e5 h5",
    "e1 e2", "h5 e2",

    // repeat moves to test draw
    "g1 h1", "g8 h8",
    "h1 g1", "h8 g8",
    "g1 h1", "g8 h8",
    "h1 g1", "h8 g8",
    "g1 h1", "g8 h8",
    "h1 g1", "h8 g8",
    "g1 h1", "g8 h8",
    "h1 g1", "h8 g8",
    "g1 h1", "g8 h8",
    "h1 g1", "h8 g8",
    
     };

    for (String move : movelist)
    {
        //System.out.println(move);
        //int fromRank = 1 + (int)move.charAt(1) - (int)'1';
        //int fromFile = 1 + (int)move.charAt(0) - (int)'a';
        //char fromFile = move.charAt(0);
        //int toRank = 1 + (int)move.charAt(4) - (int)'1';
        //int toFile = 1 + (int)move.charAt(3) - (int)'a';
        //char toFile = move.charAt(3);

        //System.out.println("Move: " + fromRank + fromFile + toRank + toFile);
        //myGame.makeMove(fromRank, fromFile, toRank, toFile);
        //myGame.makeMove(move);

        // extra DEBUG print line
        //Game.ChessPiece movingPiece = myGame.getPieceAt(fromRank,fromFile);
        //int code = myGame.validateMove(fromRank, fromFile, toRank, toFile);
        int code;
        //char promo = ' '; 
        /*
        if ( move.length() >= 7) promo = move.charAt(6);
        if ( movingPiece == null ) {
            code = Game.MOVE_ILLEGAL_SQUARE_EMPTY;

        } else if ( move.length() >= 7) {
            code = movingPiece.makeMove(toRank, toFile, promo);
        } else {
            code = movingPiece.makeMove(toRank, toFile);
        }
        */
        code = myGame.makeMove(move);
        //System.out.println("DEBUG:" + Game.getMoveCodeText(code));
    }
    //System.out.println( myGame.getCompleteMoveHistory() );
    //*/ // END DEBUG
}

}