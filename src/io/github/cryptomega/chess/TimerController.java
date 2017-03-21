/*
 * 
 */
package io.github.cryptomega.chess;

/**
 *
 * @author Philip
 */
/** *****************************************************
     * Chess Timer Interface
     *********************************************************/
    public interface TimerController
    {
        /**
         * Called when game is being set up to initialize the timer.
         * Call Chess.updateTimer(int playerColor, double timeRemaining) 
         *  to publish updates the player's remaining time
         * @param startingMins starting time in minutes
         * @param incrementSecs per turn increment time in seconds
         * @param initPlayerColor player which moves first, starting the game timer
         */
        public void initTimer(int startingMins, int incrementSecs, int initPlayerColor);
        
        /**
         * Called after each move to switch the active player
         */
        public void switchTimer();
        
        /**
         * Called on Game Over to stop the active timer
         */
        public void stopTimer();
        
    }