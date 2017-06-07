/*
 *
 * by Philip Schexnayder
 *
 * This code makes use of the pgn-parser library available at
 * https://github.com/supareno/pgn-parser
 */
package io.github.cryptomega.chess;

import static io.github.cryptomega.chess.Game.*;
import com.supareno.pgnparser.PGNParser;
import com.supareno.pgnparser.Parser;
//import com.supareno.pgnparser.jaxb.Games;
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
    //LOGGER.debug ( getNumberOfGames() + " for pgn parsing" );
    }

    public final int getNumberOfGames()
    { return games.size(); }
    
    public Game getNextGame()
    {
        Game myGame = new Game();
        
        if ( !iter.hasNext() ) return myGame;
        com.supareno.pgnparser.jaxb.Game aGame = iter.next();
        
        // get the match details // TODO: 
        myGame.whitePlayer = aGame.getWhite();
        myGame.blackPlayer = aGame.getBlack();
        myGame.GameResult = aGame.getResult();
        myGame.startingFEN = aGame.getFEN();

        // TODO: check FEN
        
        myGame.setupStandardGame();
        myGame.startGame();
        
                
        // go through all the moves
        int i = 0;
        for ( Hit hit : aGame.getHits().getHit() )
        {
            //System.out.println( hit.getNumber() +"["+ hit.getContent() +"]"); // DEBUG
            for ( String move : hit.getHitSeparated() )
            {
                
                //System.out.print("."+ ++i + "[" + move +"]"); // DEBUG
                int code = myGame.makeMove(move);
                if ( !isMoveCodeLegal( code ) )
                {
                    System.out.print("DEBUG:ERROR LOADING MOVE: "); // DEBUG
                    System.out.println( getMoveCodeText(code) );
                }
            }
        }
        // TODO: get game title

        
        return myGame;
    }
}
