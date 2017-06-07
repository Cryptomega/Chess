/*
 * 
 */
package io.github.cryptomega.chess;

/**
 * TODO: document game listener
 * @author Philip
 */
/** *****************************************************
     * Game State Listener
     *********************************************************/
    public interface GameListener
    {
        /**
         * Call back listener. This function is called every time a move 
         * is made.
         * @param update is a GameStateUpdate object 
         */
        abstract public void onGameStateUpdate( Game.State update );
        
        /**
         * This function is called when the game ends;
         * @param update is a GameStateUpdate object 
         */
        abstract public void onGameOver( Game.State update );
        
        
        abstract public void onGameStart( Game.State update );
        
        /*
        default public void onGameOver( Game.GameStats update )
        { onGameStateUpdate( update ); }
        */
    }
