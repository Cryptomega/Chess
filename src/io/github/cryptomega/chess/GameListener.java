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
         * is made, and also when the game ends if onGameOver is not implemented.
         * @param update is a GameStateUpdate object 
         */
        abstract public void onGameStateUpdate( Game.GameStats update );
        
        /**
         * This function is called when the game ends;
         * @param update is a GameStateUpdate object 
         */
        abstract public void onGameOver( Game.GameStats update );
        
        
        /*
        default public void onGameOver( Game.GameStats update )
        { onGameStateUpdate( update ); }
        */
    }
