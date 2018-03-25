
/* The following class, Game, is responsible for uniting all classes for the unified,
 * coordinated functionality of what is expected from the game, UNO. */

import code.UNOClasses.Card.UNOCard;

import java.util.*;     // added for Vector type

// TODO: Add Player class

public class Game {
    //private vector<Player> Players; /* TODO: need to decide how player order is to be decided and kept track of */
        //TODO: uncomment when player class declared
    private Vector<UNOCard> drawPile;
    private Vector<UNOCard> discardPile;
    //private Player currentPlayer; //TODO: uncomment when Player class declared

    public void initialize(){
        /* the caller function from main() which will be responsible for initializing the game instance, by calling:
        *   -   Prompting the user for the number of AI players they would like to play against
        *   -   Randomizing player order for the total number of players
        *   -   Initialize card deck
        *   -   Deal cards
        *   etc.
        *   */

        initializeDrawPile();
        initializeDiscardPile();

        // TODO: implement the initialize() class LAST (once all classes have been structured)
    }

    private void initializeDrawPile(){
        /* initializes the Draw Pile -- should be called from the initialize() function  */
        // TODO: implement initializeDrawPile() in Game AFTER Deck & Card Classes are defined
    }

    private void initializeDiscardPile(){
        /* initializes the Discard Pile -- should be called from the initialize() function  */
        // TODO: implement initializeDiscardPile() in Game AFTER Deck & Card Classes are defined
    }

    public UNOCard drawCard(){
        /* returns an UNOCard from the top of the drawPile &
        * removes that card from the drawPile*/

        UNOCard returnCard = drawPile.get(drawPile.size()-1);
        drawPile.remove(drawPile.size()-1);
        return returnCard;
    }

    private boolean validateMove(UNOCard card){
        /* function verifies if the played card is a valid move against the game rules
         */
        boolean result = false;

        // TODO: implement logic for verifying if the move is valid

        return result;
    }

    private void addCardToDiscardPile(UNOCard card){
        discardPile.add(card);
    }

    public boolean playCard(UNOCard card){
        /* the player-specified card is attempted to add to the discard pile.
        Function does the following:
            [1] Verifies if card is a valid play, if yes then [2], if not then returns false
            [2] Adds the card to the discardPile & returns true
         */
        if (validateMove(card)){
            addCardToDiscardPile(card);
            return true;
        }
        else { return false; }
    }


}
