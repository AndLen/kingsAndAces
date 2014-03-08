package game;

import gui.CardFrame;
import gui.CardPanel;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;

/**
 * Created by Andrew on 28/12/13.
 */
public class CardGame {
    //Actually uses copy on write arraylists to prevent concurrent modification from threading
    //(swing vs logic)
    private final List<List<Card>> board;
    private final List<List<Card>> kingPiles;
    private final List<List<Card>> acePiles;
    private final Stack<Card> deck;
    private final CardFrame cardFrame;
    private final Stack<CardMove> history;
    private Hand hand;
    private long startTime;
    private boolean canAddToDeckFromBoard = false;
    private boolean hasDealt = false;

    public CardGame(CardFrame cardFrame) {
        this.cardFrame = cardFrame;
        board = new CopyOnWriteArrayList<List<Card>>();
        deck = new Stack<Card>();
        hand = new Hand(new CopyOnWriteArrayList<Card>(), -1);
        history = new Stack<CardMove>();
        kingPiles = new CopyOnWriteArrayList<List<Card>>();
        acePiles = new CopyOnWriteArrayList<List<Card>>();
    }

    private static void repaintWhileDealing(CardPanel panel) {
        panel.repaint();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void dealGame(CardPanel panel) {
        while (!panel.isReady()) {
            try {
                //Wait for graphics to render once before we deal.
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        //Ensures we get to see the deck being dealt
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        startTime = System.currentTimeMillis();
        for (int i = 0; i < 4; i++) {
            kingPiles.add(new CopyOnWriteArrayList<Card>());
        }
        kingPiles.get(0).add(new Card(Card.Suit.HEARTS, Card.Rank.KING, true));
        repaintWhileDealing(panel);
        kingPiles.get(1).add(new Card(Card.Suit.DIAMONDS, Card.Rank.KING, true));
        repaintWhileDealing(panel);
        kingPiles.get(2).add(new Card(Card.Suit.CLUBS, Card.Rank.KING, true));
        repaintWhileDealing(panel);
        kingPiles.get(3).add(new Card(Card.Suit.SPADES, Card.Rank.KING, true));
        repaintWhileDealing(panel);

        for (int i = 0; i < 4; i++) {
            acePiles.add(new CopyOnWriteArrayList<Card>());
        }
        acePiles.get(0).add(new Card(Card.Suit.HEARTS, Card.Rank.ACE, true));
        repaintWhileDealing(panel);
        acePiles.get(1).add(new Card(Card.Suit.DIAMONDS, Card.Rank.ACE, true));
        repaintWhileDealing(panel);
        acePiles.get(2).add(new Card(Card.Suit.CLUBS, Card.Rank.ACE, true));
        repaintWhileDealing(panel);
        acePiles.get(3).add(new Card(Card.Suit.SPADES, Card.Rank.ACE, true));
        repaintWhileDealing(panel);

        List<Card> pack = new ArrayList<Card>();
        makePacks(pack);
        Collections.shuffle(pack);

        //Make 12 piles
        for (int i = 0; i < 12; i++) {
            board.add(new CopyOnWriteArrayList<Card>());
        }
        //Deal to all 12 piles 7 times
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 12; j++) {
                //Want them all unrevealed
                board.get(j).add(pack.remove(0));
                repaintWhileDealing(panel);
            }
            //Get the user to check for adding to the deck
            canAddToDeckFromBoard = true;
            String msg = i == 6 ? "Hit Enter to finish dealing" : "Hit Enter to continue dealing";
            panel.storeError(msg);
            repaintWhileDealing(panel);
            waitForEnter();
            canAddToDeckFromBoard = false;

            deck.push(pack.remove(0));
            repaintWhileDealing(panel);

        }
        deck.push(pack.remove(0));
        repaintWhileDealing(panel);
        hasDealt = true;
    }

    public void waitForEnter() {
        final CountDownLatch latch = new CountDownLatch(1);
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
            // Anonymous class invoked from EDT
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    latch.countDown();
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        try {
            latch.await();  // current thread waits here until countDown() is called
        } catch (InterruptedException ignored) {

        }
        KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(dispatcher);
    }

    private void makePacks(List<Card> cardList) {
        for (Card.Suit suit : Card.Suit.values()) {
            //No kings.
            for (int i = 0; i < Card.Rank.values().length - 1; i++) {
                cardList.add(new Card(suit, Card.Rank.values()[i], true));
            }
            //No kings or aces.
            for (int i = 1; i < Card.Rank.values().length - 1; i++) {
                cardList.add(new Card(suit, Card.Rank.values()[i], true));
            }
        }
    }

    public List<List<Card>> getBoard() {
        return Collections.unmodifiableList(board);
    }

    public Hand getHand() {
        return hand;
    }


    public Stack<Card> getDeck() {
        return deck;
    }

    public boolean hasWon() {

        for (List<Card> pile : acePiles) {
            if (pile.size() != 12) {
                return false;
            }
        }
        for (List<Card> pile : kingPiles) {
            if (pile.size() != 13) {
                return false;
            }
        }
        return true;
    }

    public void restart() {
        cardFrame.restartGame();
    }

    public Stack<CardMove> getHistory() {
        return history;
    }

    public void undo() {
        System.out.println(history);
        if (!history.isEmpty()) {
            CardMove toUndo = history.pop();
            toUndo.undo(this);

        }

    }

    public long getStartTime() {
        return startTime;
    }

    public int getNumMoves() {
        //Handy
        return history.size();
    }


    public List<List<Card>> getAcePiles() {
        return acePiles;
    }

    public List<List<Card>> getKingPiles() {
        return kingPiles;
    }


    public String makeHandMove(int indexFrom, int indexTo) {
        List<Card> handList = hand.getList();
        Card card = handList.remove(indexFrom);
        if (indexTo < handList.size()) {
            handList.add(indexTo, card);
        } else {
            handList.add(card);
        }
        return "";
    }

    public String moveCardOntoAceFromBoard(int indexFrom, int indexTo) {
        List<Card> from = board.get(indexFrom);
        return moveCardOntoAcePile(from, -1, indexTo);
    }

    public String moveCardOntoKingFromBoard(int indexFrom, int indexTo) {
        List<Card> from = board.get(indexFrom);
        return moveCardOntoKingPile(from, -1, indexTo);
    }

    public String moveCardOntoAceFromKing(int indexFrom, int indexTo) {
        List<Card> from = kingPiles.get(indexFrom);
        if (from.size() == 1) {
            return wouldMakePileEmpty();
        } else {
            return moveCardOntoAcePile(from, -1, indexTo);
        }
    }


    public String moveCardOntoKingFromAce(int indexFrom, int indexTo) {
        List<Card> from = acePiles.get(indexFrom);
        if (from.size() == 1) {
            return wouldMakePileEmpty();
        } else {
            return moveCardOntoKingPile(from, -1, indexTo);
        }
    }

    private String wouldMakePileEmpty() {
        return "Cannot move the last card of this pile.";
    }

    public String moveCardOntoAceFromHand(int indexFrom, int indexTo) {
        return moveCardOntoAcePile(hand.getList(), indexFrom, indexTo);
    }

    public String moveCardOntoKingFromHand(int indexFrom, int indexTo) {
        return moveCardOntoKingPile(hand.getList(), indexFrom, indexTo);
    }

    private String moveCardOntoKingPile(List<Card> from, int indexFrom, int indexTo) {
        List<Card> toPile = kingPiles.get(indexTo);
        if (indexFrom == -1) indexFrom = from.size() - 1;

        Card toMove = from.get(indexFrom);
        //Assumes at least one card remaining.
        Card pileTop = toPile.get(toPile.size() - 1);
        if (toMove.getSuit().ordinal() == pileTop.getSuit().ordinal()) {
            if (toMove.getRank().ordinal() == pileTop.getRank().ordinal() - 1) {
                toPile.add(from.remove(indexFrom));
                return "";
            } else {
                return toMove.getRank().name() + " is not one lower than " + pileTop.getRank().name();
            }

        } else {
            return "Can only move to a pile of the same suit";
        }

    }

    private String moveCardOntoAcePile(List<Card> from, int indexFrom, int indexTo) {
        List<Card> toPile = acePiles.get(indexTo);
        if (indexFrom == -1) indexFrom = from.size() - 1;
        Card toMove = from.get(indexFrom);
        //Assumes at least one card remaining.
        Card pileTop = toPile.get(toPile.size() - 1);
        if (toMove.getSuit().ordinal() == pileTop.getSuit().ordinal()) {
            if (toMove.getRank().ordinal() == pileTop.getRank().ordinal() + 1) {
                toPile.add(from.remove(indexFrom));
                return "";
            } else {
                return toMove.getRank().name() + " is not one higher than " + pileTop.getRank().name();
            }

        } else {
            return "Can only move to a pile of the same suit";
        }

    }

    public void updateHand(Card fromDeck) {
        //Do this first in case we get the same index twice in a row!
        if (hand != null && hand.getIndex() != -1) {
            board.set(hand.getIndex(), hand.getList());
        }

        List<Card> toBeHand = board.get(fromDeck.getRank().ordinal());
        toBeHand.add(fromDeck);

        hand = new Hand(toBeHand, fromDeck.getRank().ordinal());
        //Nothing there.
        board.set(fromDeck.getRank().ordinal(), new CopyOnWriteArrayList<Card>());

    }

    public boolean canAddToDeckFromBoard() {
        return canAddToDeckFromBoard;
    }

    public String addToDeck(CardMove activeMove) {
        if (activeMove != null && activeMove.getMoveType() == CardMove.MOVE_TYPE_FROM.FROM_BOARD) {
            List<Card> cards = board.get(activeMove.getIndexFrom());
            Card topCard = cards.get(cards.size() - 1);
            if (topCard.getRank().ordinal() == activeMove.getIndexFrom()) {
                deck.push(cards.remove(cards.size() - 1));
                return "";
            } else {
                return "Cannot add that to the deck, wrong rank";
            }
        }
        return "Cannot move that to the deck.";
    }


    public boolean hasDealt() {
        return hasDealt;
    }
}
