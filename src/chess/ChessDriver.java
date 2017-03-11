/*
 *
 */
package chess;

import chess.Chess.ChessPiece;
import com.sun.xml.internal.ws.util.StringUtils;
import java.util.ArrayList;
import java.util.Arrays;


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
        
        // get the board and/or pieces 
        // use makeMove() to make moves
        
        // get game pieces
        //ArrayList<ChessPiece> pieces = chess.getPieces();
        
       //get the board
        ChessPiece[][] board = chess.getBoard();
        


        printBoard(board);
        //System.out.println("Exiting...");
    }
    
    // Text-based chess board
    // private static char[][] board = new char[8][8];
    
    /*
    private static void clearBoard() 
    {
        for (int i = 0; i < 8; i++)
            for (int j = 0; j < 8; j++)
            {
                board[i][j] = ' ';
            }
    }
    
    private static void insertDummyData() 
    {
        board[0][4] = '\u2654';
        board[7][4] = '\u265A';
    }
    */
    
    // Draws the text chess board to console
    private static void printBoard(ChessPiece[][] board)
    {
        printRowDivider();
        for (int i = 7; i >= 0; i--)
        {
            System.out.print(String.valueOf(1+i) + " | ");
            for (int j = 0; j < 8; j++)
            {
                if ( board[i][j] == null )
                    System.out.print('\u2001');
                else
                    System.out.print(board[i][j].getUnicode());
                System.out.print(" | ");
            }
            System.out.println();
            printRowDivider();
        }
        System.out.print("    a\u2001  b\u2001  c\u2001  d\u2001  ");
        System.out.println("e\u2001  f\u2001  g\u2001  h");
            
        
    }
    
    private static void printRowDivider()
    {
        char[] chars = new char[BOARD_WIDTH];
        Arrays.fill(chars, '-');
        //Arrays.fill(chars, '\u2500');
        System.out.print("  ");
        System.out.println(chars);
    }
    
    private static final int BOARD_WIDTH = 38;
}
