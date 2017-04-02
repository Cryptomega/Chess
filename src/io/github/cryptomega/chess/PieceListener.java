/*
 * 
 */
package io.github.cryptomega.chess;

/**
 * TODO: document piece listener
 * @author Philip
 */
/** *****************************************************
     * * * * Piece Listener * * *
     *********************************************************/
public interface PieceListener
{
    /**
     * Callback on some update to piece
     * @param piece reference to the updated piece
     */
    abstract public void onUpdate(Game.ChessPiece piece);

    /**
     * Callback for move
     * @param piece reference to the moved piece
     */
    abstract public void onMove(Game.ChessPiece piece);
    
    /*
    default public void onMove(Game.ChessPiece piece)
    { onUpdate(piece); }
    */

    /**
     * Callback for capture
     * @param piece reference to the captured piece
     */
    abstract public void onCapture(Game.ChessPiece piece);

    /*
    default public void onCapture(Game.ChessPiece piece)
    { onUpdate(piece); }
    */
    
    /**
     * Callback for pawn promotion
     * @param piece reference to the pawn promoted
     * @param promoted reference to the new piece
     */
    abstract public void onPromote(Game.ChessPiece piece, Game.ChessPiece promoted);
    
    /*
    default public void onPromote(Game.ChessPiece piece, Game.ChessPiece promoted)
    { onUpdate(piece); }
    */
}