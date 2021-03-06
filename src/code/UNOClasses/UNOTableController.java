package code.UNOClasses;

import code.UNOClasses.Card.UNOCard;
import code.UNOClasses.Card.UNOColor;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.*;

public class UNOTableController implements Initializable {

    @FXML
    private Spinner numberOfPlayersSpinner;
    @FXML
    private Button setNumberOfPlayersButton;
    @FXML
    private Label numberOfPlayersLabel;
    @FXML
    private Button startNewGame;
    @FXML
    private Label showHandLabel;
    @FXML
    private Label showDiscardPile;
    @FXML
    private Label showCompPlayerCardNumberLabel;
    @FXML
    private ChoiceBox chooseCardFromHandChoiceBox;
    @FXML
    private ImageView discardPileImage;
    @FXML
    private ImageView drawPileImage;
    @FXML
    private ImageView[] showPlayerHandImageView;
    @FXML
    private HBox playerHandHBox;
    @FXML
    private FlowPane playerHandFlowPane;
    @FXML
    private GridPane playerHandGridPane;
    @FXML
    private Button drawCardButton;
    @FXML
    private Label showCurrentPlayerLabel;
    @FXML
    private ChoiceBox chooseWildColorChoiceBox;
    @FXML
    private Button chooseWildColorButton;

    private int numberOfCompPlayers;
    private Vector<Player> players;
    private Stack<UNOCard> discardPile;
    private PlayerTurnState pts = PlayerTurnState.getInstance();
    Boolean GameOver;
    public int totalNumberOfPlayers = 0;
    public Deck deck;
    int aIPlayerCount = 0;

    public void setNumberOfCompPlayers() {
        this.numberOfCompPlayers = this.numberOfPlayersSpinner.getValue().hashCode();
        numberOfPlayersLabel.setText("You have chosen " + numberOfCompPlayers + " computer players.");
    }

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        //Configure the spinner with values of 1-9
        SpinnerValueFactory<Integer> playerNumberFactory = new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 9, 1);
        this.numberOfPlayersSpinner.setValueFactory(playerNumberFactory);
        //numberOfPlayersSpinner.setEditable(true);

        numberOfPlayersLabel.setText("");
        showHandLabel.setText("");
        showDiscardPile.setText("");
        showCompPlayerCardNumberLabel.setText("");
        showCurrentPlayerLabel.setText("");
        chooseCardFromHandChoiceBox.setValue("");
        setChooseWildColorChoiceBox();
        discardPileImage.setImage(null);
    }

    public void startNewGame() {
        setTotalNumberOfPlayers(numberOfCompPlayers + 1);
        initiatePlayersVector(totalNumberOfPlayers);
        shufflePlayerOrder(players);
        pts.initialize(totalNumberOfPlayers, 0);
        deck = new Deck();
        deck.shuffleDeck();
        dealHand(deck, players);
        initializeDiscardPile(deck);
        refreshUI();
        GameOver = false;
        play();
    }

    public void newDeckFromDiscard() {
        /** When deck is empty (size == 0), then the top card of the discard pile
         * is the only card in the discard pile, and the rest of the cards in the
         * discard pile get shuffled and become the deck.
         */

        if (deck.isEmpty()) {
            UNOCard tempCard = discardPile.pop();
            deck.shuffleDiscardPile(discardPile);
            discardPile.removeAllElements();
            discardPile.push(tempCard);
        }
        // else do nothing
    }


    public void refreshUI() {
        //main view block, updates view at the end of each turn
        viewHumanPlayerHand();
        viewDiscardPile();
        showCompPlayerCardNumberLabel.setText(viewCompPlayerCardNumber());
        cardsInHand();
        setDiscardPileImage();
        setShowPlayerHandImageView();
        setShowCurrentPlayerLabel();
    }

    public void pauseGame(int milliseconds) {
        try {
            Thread.sleep(milliseconds); //2000 milliseconds = 2 seconds.
        } catch (InterruptedException ex) {
            //just continue if it doesn't sleep.
        }
    }

    public void AIMove(Player player) {
        /** Should be called with input of the AI player object
         * who needs to make a move. Will keep drawing cards as
         * long as the AI player's function makeMove() returns null.
         * As soon as the function returns an actual card to play,
         * then check if card is playable or not by calling the checkMove()
         * function. If checkMove() returns false, then keep trying
         * checkMove() until returns true.
         * Once checkMove() returns true, then we're removing the card from
         * the computer player's hand and pushing the card to the discard pile.
         *
         * @author Darya Kiktenko
         */
        // initializing values utilized later on
        UNOCard returnedCard;

        returnedCard = null; // in the case that playCard() returns false, we can try again
        while (returnedCard == null) {
            returnedCard = player.makeMove(discardPile.peek(),
                    players.get(pts.peekLastTurn()).myHand().handTotal(),
                    players.get(pts.peekNextTurn()).myHand().handTotal(),
                    players.get(pts.peekTwoPlayers()).myHand().handTotal());
            // if makeMove returns null, that means they need to draw a card

            if (returnedCard == null) {
                player.addCardtoHand(drawCard());
            }
        }
        // to get to this point, player's makeMove() has returned a card to play -- now we check this card
        playCard(returnedCard, player);
    }


    public void play() {

        refreshUI();
        while ((getHumanPlayerInt() != pts.getCurrentTurn()) && GameOver == false) {
            //check for empty deck before players start to draw cards from the deck.
            if (deck.isEmpty()) {
                newDeckFromDiscard();
            }

            AIMove(players.get(pts.getCurrentTurn()));

            refreshUI();
        }

        //Announce game winner. Restart game if the user wants to
    }

    public void viewHumanPlayerHand() {
        this.showHandLabel.setText("Your hand:");
    }

    public void viewDiscardPile() {
        this.showDiscardPile.setText("Discard Pile: " + viewLastDiscardPileCard().toString());
    }

    public void setShowCurrentPlayerLabel() {
        int currentPlayerIndex = pts.getCurrentTurn();
        this.showCurrentPlayerLabel.setText("Current Player: " + players.get(currentPlayerIndex).getName());
    }

    public String viewCompPlayerCardNumber() {
        Vector<Player> players = getPlayers();
        String remainingCards = "Player status\n";
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isHuman() == false) {
                remainingCards += (i + 1) + ". " + players.get(i).getName() + ": " + players.get(i).myHand().handTotal() + " cards\n";
            } else if (players.get(i).isHuman() == true) {
                players.get(i).setName("You");
                remainingCards += (i + 1) + ". " + players.get(i).getName() + ": " + players.get(i).myHand().handTotal() + " cards\n";
            }
        }
        return remainingCards;
    }

    public void cardsInHand() {
        this.chooseCardFromHandChoiceBox.setItems(FXCollections.observableArrayList(getHumanPlayer().myHand().getUnoCardsList()));
        System.out.println(chooseCardFromHandChoiceBox.getItems());
    }

    public void removeImageFromHand(UNOCard unoCard, Player player)
    {
        for (int i = 0; i < player.myHand().handTotal(); i++)
        {
            if (player.myHand().getUnoCardsList().contains(unoCard))
            {
                player.myHand().getUnoCardsList().remove(unoCard);
            }
        }

        setShowPlayerHandImageView();
    }

    public UNOCard getCardFromChoiceBox()
    {
        UNOCard unoCard = (UNOCard) this.chooseCardFromHandChoiceBox.getValue();
        return  unoCard;
    }

    public UNOColor getWildColorFromChoiceBox()
    {
        UNOColor Unoocolor = (UNOColor) this.chooseWildColorChoiceBox.getValue();
        return  Unoocolor;
    }

    public void playCardButtonPushed() {

        try
        {
            if (playCard(getCardFromChoiceBox(), players.get(pts.getCurrentTurn())))
            {
                if (getCardFromChoiceBox().isWildDraw4() || getCardFromChoiceBox().isWild()) {
                    if (this.chooseWildColorChoiceBox == null) {
                        throw new IllegalStateException();
                    }
                    else
                        getLastDiscardPileCard().set_color(getWildColorFromChoiceBox());
                }
                System.out.println("DISCARD PILE" + discardPile.peek());
                setDiscardPileImage();
                removeImageFromHand(getCardFromChoiceBox(), getHumanPlayer());
                play();
            }
            else
            {
                throw new IllegalStateException();
            }
        }
        catch (IllegalStateException exception)
        {
            System.out.println("The card you attempted to play is invalid " +
                    "please try again or pass");
        }

    }

    public void setDiscardPileImage() {
        String cardType = viewLastDiscardPileCard().get_type().toString();
        String cardColor = viewLastDiscardPileCard().get_color().toString();
        String path = "resources/images/" + cardColor + "_" + cardType + ".png";
        Image card = new Image(path);
        this.discardPileImage.setImage(card);
    }

    public List createHandImageArray() {
        getHumanPlayer().myHand().sort();
        Vector<UNOCard> hand = getHumanPlayer().myHand().getUnoCardsList();
        List<Image> cardImages = new LinkedList();
        for (int i = 0; i < hand.size(); i++) {
            String cardType = hand.get(i).get_type().toString();
            String cardColor = hand.get(i).get_color().toString();
            String path = "resources/images/" + cardColor + "_" + cardType + ".png";
            Image card = new Image(path);
            cardImages.add(card);
        }
        return cardImages;
    }

    public void setShowPlayerHandImageView() {
        this.playerHandFlowPane.getChildren().clear();
        int handTotal = getHumanPlayer().myHand().handTotal();
        showPlayerHandImageView = new ImageView[handTotal];
        List<Image> allCardImages = createHandImageArray();
        for (int i = 0; i < handTotal; i++) {
            showPlayerHandImageView[i] = new ImageView(allCardImages.get(i));
            showPlayerHandImageView[i].setFitHeight(100);
            showPlayerHandImageView[i].setFitWidth(75);
            showPlayerHandImageView[i].setSmooth(true);
            showPlayerHandImageView[i].setPreserveRatio(true);
            this.playerHandFlowPane.getChildren().add(showPlayerHandImageView[i]);
        }
    }

    public void drawCardButtonPushed() {
        getHumanPlayer().myHand().addUNOCard(drawCard());
        setShowPlayerHandImageView();
    }

    //Methods from Game

    public Vector<Player> shufflePlayerOrder(Vector<Player> playerVector) {
        /** Shuffles the players vector
         * Adapted from original dealHand(), separated for OOP & unit testing purposes
         * @author Darya Kiktenko
         * @author Pranjali Mishra
         */

        Collections.shuffle(playerVector);
        return playerVector;
    }

    public int getTotalNumberOfPlayers() {
        /** Obtains the total number of players in the game in addition to the human player
         * @author Darya Kiktenko
         * @author Pranjali Mishra
         */
        int result = 1; //default for the human player

        // TODO: To be changed with incorporation of the UI

        result += 3; // 3 AI

        return result;
    }

    public void setTotalNumberOfPlayers(int numberOfPlayers) {
        /** Sets the total number of players which will be playing the game (including the human user)
         * @author Darya Kiktenko
         */
        totalNumberOfPlayers = numberOfPlayers;
    }

    public Vector getPlayers() {
        return this.players;
    }

    public Player getHumanPlayer() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isHuman() == true) {
                return players.get(i);
            }
        }
        return null;
    }

    public int getHumanPlayerInt() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).isHuman() == true) {
                return i;
            }
        }
        return -1; //should never have human player as -1 and will break the code.
    }

    public String toString() {
        String gameState = null;
        for (int i = 0; i < players.size(); i++) {
            gameState += players.get(i).toString();
        }
        return gameState;
    }

    public Vector<Player> initiatePlayersVector(int numberOfPlayers) {
        /** Initializes the players vector class member
         * by creating a new player object and adding that to the vector
         * @author Darya Kiktenko
         */
        players = new Vector<Player>();
        for (int i = 0; i < (numberOfPlayers - 1); i++) { // minus 1, because human player will be separately initialized
            Player tempPlayer = new Player(false); //false, because all except human are AI players
            tempPlayer.setName(tempPlayer.chooseRandomName());
            players.add(tempPlayer);
        }

        Player human = new Player(true);
        players.add(human);

        return players;
    }

    public Vector<Player> dealHand(Deck deckToDealFrom, Vector<Player> gamePlayers) { // SRS - FR1.2 & FR1.3 implementation
        /** deals 7 cards from the deck to each of the players in the gamePlayers vector,
         * including the AI and the human player.
         * Returns the players vector that now have been dealt cards.
         * Adapted from the original dealHand() function written by Pranjali Mishra
         * @author Pranjali Mishra
         */

        for (int j = 0; j < 7; j++) {
            for (Player player : gamePlayers) {
                player.addCardtoHand(deckToDealFrom.deal());
            }
        }
        return gamePlayers;
    }

    public Stack<UNOCard> initializeDiscardPile(Deck deckToDealFrom) { // SRS - FR1.4 & FR1.5 & FR1.6 implementation
        /** initializes the Discard Pile AFTER players have been dealt their hand
         * Obtains the top card from the deck & places it on top of the discardPile.
         * Unless the drawn card is wild draw 4 card, then the card is returned to the bottom of the draw
         * deck and another card is drawn from the top of the draw deck.
         * Returns the new initialized stack of UNOCards that is the discardPile
         * Adapted from the original initializeDiscardPile() function written by Pranjali Mishra
         * @author Pranjali Mishra
         * @author Darya Kiktenko
         */
        discardPile = new Stack<UNOCard>();
        UNOCard topCard = null;
        boolean validCard = false;
        while (!validCard) {
            topCard = deckToDealFrom.deal();
            if (topCard.isWildDraw4()) {
                System.out.println("First card drawn is a \"Wild Draw Four\"- adding back to the Deck & drawing another card.");
                deck.addCard(topCard);
            } else {
                discardPile.add(topCard);
                validCard = true;
            }
        }
        return discardPile;
    }

    public UNOCard drawCard() {
        /** returns the UNOCard from the top of the deck & removes that card from the deck
         * @author Darya Kiktenko
         * */
        return deck.deal();
    }


    public boolean validateCardColorsMatch(UNOCard playedCard, UNOCard discardPileTopCard) {
        /** compares two cards based on the color
         * @author Darya Kiktenko*/

        boolean result = false;
        if (discardPileTopCard.get_color() == playedCard.get_color()) {
            result = true;
        }
        return result;
    }

    public boolean validateCardTypesMatch(UNOCard playedCard, UNOCard discardPileTopCard) {
        /** compares two cards based on their type.
         * @author Darya Kiktenko */

        boolean result = false;
        if (discardPileTopCard.get_type() == playedCard.get_type()) {
            result = true;
        }
        return result;
    }

    public boolean challengeWildDraw4Card(Player challenger, Player challenged) {
        /* NOTE: distinction between challenger and challenged.
            Challenger is the PERSON AFTER the challenged (turn-wise) who suspects
            that the challenged that they have a card in their hand which matched the discardPile's  top card
            before the challenged played the Wild 4 Draw card.
            Challenged is the player who played the Wild 4 Draw card.
         */

        /* function check's the challenged hand against the discardPile's top card BEFORE the challenged played
         * the Wild 4 Draw card. If the hand contains any color of the same COLOR as the discardPile's top card,
         * function returns true (& challenged will have to draw 4 cards).
         * If the challenged does NOT have any card with the same COLOR as the discardPile's top card,
         * the function will return false (& the challenger will have to draw 6 cards).
         * */

        // TODO: implement the challenge function
        return false;
    }

    private boolean validateMove(UNOCard playedCard, Player currentPlayer) {
        /* function verifies if the played card is a valid move against the game rules
         */

        UNOCard discardPileCard = discardPile.peek();
        if (playedCard.isWild() || playedCard.isWildDraw4()) {
            /* if the played card is a wild card, then there's nothing to check except
            to prompt the player to what color they would like to change the game play to */

            // SRS - FR2.3 complete
            UNOCard declareColorCard = discardPile.pop();
            if (!currentPlayer.isHuman()) {
                Random r = new Random();
                declareColorCard.set_color(UNOColor.values()[r.nextInt(4)]); //cast random int
            } else {
                //TODO: prompt the human player for what color they'd like to discard pile to be & set it to declareColorCard
            }
            discardPile.push(declareColorCard); // taking top card & changing it's color
            return true;
        } else {
            // otherwise, it's not a special card that is played and we just need to check if either the type or color match
            if (validateCardColorsMatch(playedCard, discardPileCard) || validateCardTypesMatch(playedCard, discardPileCard)) {
                return true;
            }
        }
        return false;
    }

    private void addCardToDiscardPile(UNOCard card) {
        discardPile.push(card);
    }

    public boolean playCard(UNOCard card, Player currentPlayer) {
        /* the player-specified card is attempted to add to the discard pile.
        Function does the following:
            [1] Verifies if card is a valid play, if yes then [2], if not then returns false
            [2] Adds the card to the discardPile & returns true
         */
        if (validateMove(card, currentPlayer)) {
            addCardToDiscardPile(card);
            performCardAction(card); //if the card isn't a skip or reverse the next player will get their turn.
            currentPlayer.discardCard(card);
            addCardToDiscardPile(card);
            return true;
        } else {
            //if the player is not human then we want to skip their turn if they try to play an invalid card.
            if (!players.get(pts.getCurrentTurn()).isHuman()) {
                pts.moveNextPlayer(); //if the card is not valid and the player is computer they will pass their turn. If they are human they will get another try.
            }
            return false;
        }
    }

    public UNOCard viewLastDiscardPileCard() {
        /* returns the UNOCard object of the last card placed in the discard pile
        which can be seen by everyone playing BUT does NOT remove the card from the pile
         */
        return discardPile.peek();
    }

    public UNOCard getLastDiscardPileCard() {
        /* removes and returns the UNOCard object of the last card placed in the discard pile
         */
        return discardPile.pop();
    }

    //
    private void performCardAction(UNOCard c) {
        switch (c.get_type()) {
            case REVERSE:
                pts.reverseTurnOrder();
                break;
            case SKIP:
                pts.skipNextPlayer();
                break;
            case DRAWTWO:
                pts.moveNextPlayer();
                for (int i = 0; i < 2; i++) {
                    players.get(pts.getCurrentTurn()).addCardtoHand(deck.deal());
                }
                break;
            default:
                pts.moveNextPlayer();
                break;
        }
    }

    public void callUNO() {
        for (Player player : players) {
            if (player.isHuman() && player.myHand().handTotal() == 1) {
                player.myHand().callUNO();
            } else {
                System.out.println("You can only call UNO! when there " +
                        "is one card left in your hand");
            }
        }
    }

    public boolean isGameOver(){
        /** For every player, checks the number of cards remaining.
         * If any player has 0 cards remaining, the game is over, returning
         * true.
         * Else, returns false.
         *
         * @author Darya Kiktenko
         */

        for (Player player : players){
            if (player.myHand().handTotal() == 0){
                GameOver = true;
                return true;
            }
        }
        return false;
    }

    public void setChooseWildColorChoiceBox() {
        ObservableList<UNOColor> unoColors = FXCollections.observableArrayList(UNOColor.BLUE, UNOColor.GREEN, UNOColor.YELLOW,
        UNOColor.RED);
        this.chooseWildColorChoiceBox.setItems(unoColors);
    }
    }
