/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.github.cryptomega.chess;

import com.supareno.pgnparser.PGNParser;
import com.supareno.pgnparser.Parser;
import com.supareno.pgnparser.jaxb.Games;
import com.supareno.pgnparser.jaxb.Hit;
import java.util.Iterator;
import java.util.List;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;

/**
 *
 * @author Philip
 */
public class PGNLoader
{
    protected final String filename;
    protected List<com.supareno.pgnparser.jaxb.Game> games;
    protected Iterator<com.supareno.pgnparser.jaxb.Game> iter;
    protected Logger LOGGER;
    protected int index;
    
    public PGNLoader( String filename )
    {
        this.filename = filename;
        // to parse pgn files
    BasicConfigurator.configure ();
    Parser parser = new PGNParser ( Level.DEBUG ); // Level.WARN
    
    games = parser.parseFile ( filename ).getGame();
    iter = games.iterator();
    index = 0;
    
    LOGGER = Logger.getLogger ( PGNLoader.class );
    LOGGER.debug ( getNumberOfGames() + " for pgn parsing" );
    }

    public final int getNumberOfGames()
    { return games.size(); }
    
    public Game getNextGame()
    {
        Game myGame = new Game();
        if ( !iter.hasNext() ) return myGame;
        myGame.setupStandardGame();
        myGame.startGame();
        
        com.supareno.pgnparser.jaxb.Game aGame = iter.next();
        for ( Hit hit : aGame.getHits().getHit() )
        {
            System.out.println( hit.getContent() );
        }
        // TODO: get game title
        
        // TODO: implement
        
        LOGGER.debug( "end" );
        
        return myGame;
    }
}
