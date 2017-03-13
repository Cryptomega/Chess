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
        
        ChessPiece[][] board = chess.getBoard();
        
        ArrayList<ChessPiece> pieces = chess.getPieces();
        
        
        printBoard(board);
        
        //chess.makeMove(0,4,1,4);
        
        System.out.print( chess.makeMove("e8 e7") );
        System.out.println("(Move code)");
        
        //ChessPiece a = pieces.get(0);
        //System.out.println( a.makeMove(0,0, 'x') );
        
        //System.out.print(a.getName() + "(" + a.getType() +") ");
        //System.out.print( Chess.getColorString(a.getColor()) );
        //System.out.println(" " + a.getPosition() + " moves:" + a.getMoveCount() );
        

    }


    // Draws the text chess board to console
    private static void printBoard(ChessPiece[][] board)
    {
        printFirstRow();
        for (int i = 7; i >= 0; i--)
        {
            System.out.print(String.valueOf(1+i) + "\u2502 ");
            for (int j = 0; j < 8; j++)
            {
                if ( board[i][j] == null )
                    System.out.print('\u2001');
                else
                    System.out.print(board[i][j].getUnicode());
                System.out.print(" \u2502 ");
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
