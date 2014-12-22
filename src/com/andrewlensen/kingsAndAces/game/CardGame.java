package com.andrewlensen.kingsAndAces.game;

import com.andrewlensen.kingsAndAces.game.moves.CardMove;
import com.andrewlensen.kingsAndAces.game.moves.DealMove;
import com.andrewlensen.kingsAndAces.gui.CardFrame;
import com.andrewlensen.kingsAndAces.gui.CardPanel;
import com.andrewlensen.kingsAndAces.gui.RenderMessage;

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

    //Shouldn't be used generally. Bit of a hack
    private ArrayList<Card> pack = null;
    private CountDownLatch dealLatch;

    public CardGame(CardFrame cardFrame) {
        this.cardFrame = cardFrame;
        board = new CopyOnWriteArrayList<List<Card>>();
        deck = new Stack<Card>();
        hand = new Hand(new CopyOnWriteArrayList<Card>(), -1);
        history = new Stack<CardMove>();
        kingPiles = new CopyOnWriteArrayList<List<Card>>();
        acePiles = new CopyOnWriteArrayList<List<Card>>();
    }

    public static void repaintWhileDealing(CardPanel panel) {
        panel.repaint();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public CountDownLatch getDealLatch() {
        return dealLatch;
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

        pack = new ArrayList<Card>();
        makePacks(pack);
        Collections.shuffle(pack);

        //Make 12 piles
        for (int i = 0; i < 12; i++) {
            board.add(new CopyOnWriteArrayList<Card>());
        }
        deal(panel);

    }

    public synchronized void deal(CardPanel panel) {
        hasDealt = false;
        //canAddToDeckFromBoard = false;
        //Let them initiate dealing
        panel.storeMessage(new RenderMessage("Hit Enter/click the pack to start dealing", false));
        repaintWhileDealing(panel);

        waitForNextDealConfirmation();


        //Deal to all 12 piles accounting for undos
        while (pack.size() > 13) {
            System.out.println(pack.size());
            DealMove dealMove = new DealMove();
            dealMove.makeMove(this, panel);
            history.push(dealMove);

        }
        System.out.println(pack.size());
        repaintWhileDealing(panel);
        hasDealt = true;
    }

    public synchronized void waitForNextDealConfirmation() {
        dealLatch = new CountDownLatch(1);
        KeyEventDispatcher dispatcher = new KeyEventDispatcher() {
            // Anonymous class invoked from EDT
            public boolean dispatchKeyEvent(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER)
                    dealLatch.countDown();
                return false;
            }
        };
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(dispatcher);
        try {
            dealLatch.await();  // current thread waits here until countDown() is called
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
        return board;
    }

    public Hand getHand() {
        return hand;
    }

    public void setHand(Hand hand) {
        this.hand = hand;
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

    public void undo(CardPanel panel) {
        System.out.println(history);
        if (!history.isEmpty()) {
            CardMove toUndo = history.peek();
            if (toUndo.undo(this, panel)) {
                history.pop();
            }
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

    public boolean canAddToDeckFromBoard() {
        return canAddToDeckFromBoard;
    }

    public String addToDeck(int indexFrom) {
        List<Card> cards = board.get(indexFrom);
        Card topCard = cards.get(cards.size() - 1);
        if (topCard.getRank().ordinal() == indexFrom) {
            deck.push(cards.remove(cards.size() - 1));
            return "";
        } else {
            return "Cannot add that to the deck, wrong rank";
        }
    }

    public boolean hasDealt() {
        return hasDealt;
    }

    public ArrayList<Card> getPack() {
        return pack;
    }


    public void setCanAddToDeckFromBoard(boolean canAddToDeckFromBoard) {
        this.canAddToDeckFromBoard = canAddToDeckFromBoard;
    }
}
