/*
 *
 */
package io.github.cryptomega.chess;

import io.github.cryptomega.chess.Chess.ChessPiece;
//import java.util.ArrayList;
//import java.util.Arrays;
import java.util.Scanner;


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
        Chess chess = new Chess();
        chess.setupGame();
        chess.startGame();
        
        // input string variable
        String input;
        Scanner scanner = new Scanner(System.in);
        
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
        
        
        // game loop
        while ( true )
        {
            ChessPiece[][] board = chess.getBoard();
            printBoard(board);
            
            // DEBUG isObserving() function
            //ChessPiece piece = chess.getPieces().get(2);
            //System.out.println("Move Code: " +
            //        Chess.getMoveCodeText( piece.isObserving(0,0) ));
            //break;
            
            System.out.print("Enter move(00 to exit):");
            input = scanner.nextLine();
            
            
            if ( input.equals("00") )
                break;
            
            int code;
            try { code = chess.makeMove(input); }
            catch (Exception e) 
            {
                System.out.println(" > > > ERROR:" + e.getMessage() + " < < <");
                continue;
            }
            
            // DEBUG
            System.out.println( chess.getCompleteMoveHistory() );
            
            // DEBUG System.out.println("\n");
            if ( code != Chess.MOVE_LEGAL )
                System.out.println(" > > > " 
                        + Chess.getMoveCodeText(code)
                        + " ("+code+") < < <");
            else
                System.out.println(Chess.getMoveCodeText(code));
            //break; // DEBUG
        }


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
                    if ( Chess.getSquareColor(i,j) == Chess.WHITE )
                        System.out.print('\u2001');
                    else    // black 
                        System.out.print('\u2591');
                } else {
                    System.out.print(board[i][j].getUnicode());
                }
                if ( Chess.getSquareColor(i,j) == Chess.WHITE )
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
}