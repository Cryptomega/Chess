/*
 *
 */
package io.github.cryptomega.consolechess;

//import io.github.cryptomega.chess.Game.ChessPiece;
import java.util.ArrayList;
//import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Scanner;

import io.github.cryptomega.chess.Game;
import io.github.cryptomega.chess.Game.ChessPiece;
import io.github.cryptomega.chess.GameListener;
import io.github.cryptomega.chess.PieceListener;


/**
 *
 * @author Philip
 */
public class ChessDriver  
{
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        // setup and start the chess game
        Game myGame = new Game();
        myGame.setupStandardGame();
        myGame.setStartTime(30, 10);
        myGame.startGame();
        
        // register listener
        myGame.addGameStateListener( new GameStateListener() );
        
        // add piece liseners too all the pieces. GO CRAZY
        for ( ChessPiece piece : myGame.getPieces() )
            piece.addPieceListener(new MyPieceListener() );
        
        // input string variable
        String input;
        Scanner scanner = new Scanner(System.in);
        
        // DEBUG
        // Premoves
         /*
        chess.makeMove("e2 e4");
        chess.makeMove("e7 e5");
        chess.makeMove("g1 f3");
        chess.makeMove("g8 f6");
        chess.makeMove("f1 c4");
        chess.makeMove("f8 c5");
        chess.makeMove("d2 d3");
        chess.makeMove("d7 d6");
        chess.makeMove("f3 h4");
        chess.makeMove("f6 h5");
        chess.makeMove("a2 a4");
        chess.makeMove("e8 g8");
        chess.makeMove("a4 a5");
        chess.makeMove("g8 h8");
        chess.makeMove("a5 a6");
        chess.makeMove("h8 g8");
        chess.makeMove("a6 b7");
        chess.makeMove("g8 h8");
        chess.makeMove("b7 a8=q");
        chess.makeMove("g7 g5");
        chess.makeMove("b2 b3");
        chess.makeMove("g5 g4");
        chess.makeMove("f2 f4");
        
        //*/
        
        // game loop
        while ( true )
        {
            if ( !myGame.isGameActive() )
                System.out.print("GAME OVER: ");
            
            System.out.print(myGame.getGameStatus());
            int secs = (int)myGame.getSecondsRemaining(myGame.whoseTurn() );
            System.out.println( "   " + secs + " secs remaining");
            
            ChessPiece[][] board = myGame.getBoard();
            printBoard(board);
            

            // get input            
            System.out.print("Enter move(00 to exit):");
            input = scanner.nextLine();
            
            if ( !myGame.isGameActive() )
            {
                System.out.print("exit or restart?:");
                input = scanner.nextLine();
            }
                
            if ( input.toUpperCase().equals("REFRESH") )
                myGame.refreshListeners();
                    
            if ( input.toUpperCase().equals("RESTART") )
            {   myGame.restartGame();
                continue;
            }
 
            // if user exits or game ends on time, exit loop
            if ( input.equals("00") || input.toUpperCase().equals("EXIT") )
            {   myGame.endGame();
                break;       // exit
            }

            if ( input.toUpperCase().equals("COPY") 
                    || input.toUpperCase().equals("ANALYZE") )
            {
                analyze( myGame );
                continue;
            }
            
            
            if ( input.length() == 2 )  // Get Valid moves
            {   // get moves for piece
                int rank = Game.convertAlgebraicToInternalRank(input);
                int file = Game.convertAlgebraicToInternalFile(input);
                if ( !Game.isValidCoord(rank,file) )
                {
                    System.out.println(" > > > ERROR: Invalid location < < <");
                    continue;
                }
                ChessPiece piece = board[rank][file];
                printCandidateMoves(piece);
                continue;
            }
            
            int code;    // makeMove response code
            try { code = myGame.makeMove(input); }
            catch (Exception e) 
            {
                System.out.println(" > > > ERROR:" + e.getMessage() + " < < <");
                continue;
            }
            
            // DEBUG
            // System.out.println( chess.getCompleteMoveHistory() );
            
            // DEBUG System.out.println("\n");
            System.out.println(" > > > " 
                    + Game.getMoveCodeText(code)
                    + " ("+code+") < < <");
            //break; // DEBUG
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
            ChessPiece[][] board = analysisGame.getBoard();
            printBoard(board);
            
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
            
            // DEBUG System.out.println("\n");
            System.out.println(" > > > " 
                    + Game.getMoveCodeText(code)
                    + " ("+code+") < < <");
            //break; // DEBUG

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
        ArrayList<Game.Square> list = piece.getValidMoves();
        for (Game.Square square: list)
        {
            System.out.print(square.toString() + ",");
        }
        System.out.println();
    }
    
    // Draws the text chess board to console with unicode 
    private static void printBoard(ChessPiece[][] board)
    {
        printFirstRow();
        for (int i = 7; i >= 0; i--)
        {
            System.out.print(String.valueOf(1+i) + "\u2502 ");
            for (int j = 0; j < 8; j++)
            {
                if ( board[i][j] == null )
                {
                    if ( Game.getSquareColor(i,j) == Game.WHITE )
                        System.out.print('\u2001');
                    else    // black 
                        System.out.print('\u2591');
                } else {
                    System.out.print(board[i][j].getUnicode());
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

    
    // ********************************************************
    // Game state listner callbacks
    public static class GameStateListener implements GameListener
    {
        @Override
        public void onGameStateUpdate(Game.GameStateUpdate update)
        {
            System.out.println("[GS]Code(" 
                    + update.gameStateCode + "): " + update.gameState);
        }

        @Override
        public void onGameOver( Game.GameStateUpdate update )
        {
            System.out.println();
            System.out.println("****************************************************");
            System.out.println("[GS] GAME OVER! (" 
                    + update.gameStateCode + "): " + update.gameState);
            System.out.println("****************************************************");
            System.out.println();
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
            System.out.println("[CP]"+piece.getUnicode() + " has promoted.");
        }
        
    }
    
}