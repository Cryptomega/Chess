/*
 * 
 */
package io.github.cryptomega.chess;

//import io.github.cryptomega.chess.Chess.TimerCallbackReceiver;
import io.github.cryptomega.chess.Chess.TimerController;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Chess Timer
 * @author Philip
 */
public class GameTimer implements TimerController
{
    private int mActivePlayer; // Chess.WHITE or Chess.BLACK
    
    private int mStartingMins;
    private int mIncrementSecs;
    
    private double mWhiteTimeLeftSecs;
    private double mBlackTimeLeftSecs;
    
    private final Chess mGameInstance;  // Chess game instance
    
    private Timer mTimer = null;
    private int mUpdateDelayMillisec = 100;
    
    private double mUpdateDelaySecs;

    /**
     * Creates the game timer. 
     * @param game A reference to the game object
     */
    GameTimer(Chess game)
    {   mGameInstance = game; }

    
    // TODO: add public method to manually set a player's remaining time
    // TODO: add public method to set mUpdateDelayMillisec
    
    
    
    @Override
    public void initTimer(int startingMins, int incrementSecs, int initPlayerColor)
    {
        // DEBUG
        // System.out.println("GameTimer.initTimer called.");
        
        mStartingMins = startingMins;
        mIncrementSecs = incrementSecs;
        mActivePlayer = initPlayerColor;
        
        mWhiteTimeLeftSecs = 60.0 * (double)mStartingMins;
        mBlackTimeLeftSecs = 60.0 * (double)mStartingMins;
        
        mUpdateDelaySecs = (double)mUpdateDelayMillisec / 1000.0;

    }

    @Override
    public void switchTimer()
    {
        // cancel current timer if running.
        stopTimer();
        mTimer = null;
        
        switch (mActivePlayer)
        {
            case Chess.WHITE:
                mWhiteTimeLeftSecs += (double)mIncrementSecs;
                mActivePlayer = Chess.BLACK;
                break;
            case Chess.BLACK:
                mBlackTimeLeftSecs += (double)mIncrementSecs;
                mActivePlayer = Chess.WHITE;
                break;
            default:
                return;
        }
        
        // DEBUG
        System.out.println("GameTimer.switchTimer called.");
        System.out.println("  White time left: " + mWhiteTimeLeftSecs);
        System.out.println("  Black time left: " + mBlackTimeLeftSecs);
        
        //  TODO: create a new timer and schedule it
        mTimer = new Timer();
        DecrementTask task = new DecrementTask();
        
        mTimer.schedule(task, mUpdateDelayMillisec, mUpdateDelayMillisec);
    }

    @Override
    public void stopTimer()
    {
        // DEBUG
        // System.out.println("GameTimer.stopTimer called.");
        
        if ( mTimer != null )
            mTimer.cancel();
    }

    
    private void update()
    {
        // DEBUG
        // System.out.println("GameTimer.run called.");
        
        // decrement appropriate time variable
        double remaining;
        switch (mActivePlayer)
        {
            case Chess.WHITE:
                mWhiteTimeLeftSecs -= mUpdateDelaySecs;
                remaining = mWhiteTimeLeftSecs;
                break;
            case Chess.BLACK:
                mBlackTimeLeftSecs -= mUpdateDelaySecs;
                remaining = mBlackTimeLeftSecs;
                break;
            default:
                return; 
        }
        
        // publish update
        mGameInstance.updateTimer(mActivePlayer, remaining);
        
        // updateTimer() checks if out of time. if so,
        // calls endTurn() which will call cancel()
    }
    
    private class DecrementTask extends TimerTask
    {
        @Override
        public void run()
        { update(); }
    }
    
}
