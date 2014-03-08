package game;

import gui.CardFrame;
import gui.CardPanel;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Created by Andrew on 28/12/13.
 */
public class CardGame {
    //Actually uses copy on write arraylists to prevent concurrent modification from threading
    //(swing vs logic)
    private final List<List<Card>> board;
    private final List<List<Card>> kingPiles;
    private final List<List<Card>> acePiles;
    private final Queue<Card> deck;
    private final List<Card> hand;
    private final CardFrame cardFrame;
    private final Stack<CardMove> history;
    private long startTime;

    public CardGame(CardFrame cardFrame) {
        this.cardFrame = cardFrame;
        board = new CopyOnWriteArrayList<List<Card>>();
        deck = new LinkedList<Card>();
        hand = new CopyOnWriteArrayList<Card>();
        history = new Stack<CardMove>();
        kingPiles = new CopyOnWriteArrayList<List<Card>>();
        acePiles = new CopyOnWriteArrayList<List<Card>>();
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
            deck.add(pack.remove(0));
            repaintWhileDealing(panel);

        }
        deck.add(pack.remove(0));
        repaintWhileDealing(panel);

    }

    private static void repaintWhileDealing(CardPanel panel) {
        panel.repaint();
        try {
            Thread.sleep(50);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

    public String moveCardOntoCardFromTopRow(int boardIndexFrom, int boardIndexTo) {
        String result = moveCardOntoCard(hand, boardIndexTo, hand.size() - 1);
        if (result.isEmpty()) {
            hand.remove(hand.size() - 1);
        }
        return result;
    }

    public String moveCardOntoCardFromBoard(int toMoveTop, int boardIndexFrom, int boardIndexTo) {
        List<Card> from = board.get(boardIndexFrom);
        String result = moveCardOntoCard(from, boardIndexTo, toMoveTop);
        if (result.isEmpty()) {
            while (from.size() > toMoveTop) {
                from.remove(toMoveTop);
            }
        }
        return result;
    }

    private String moveCardOntoCard(List<Card> from, int boardIndexTo, int listIndexFrom) {
        List<Card> to = board.get(boardIndexTo);
        Card firstToMove = from.get(listIndexFrom);
        System.out.println("TO: " + to.toString());
        if (to.isEmpty()) {
            //Need to move a king there.
            if (from.get(listIndexFrom).getRank() == Card.Rank.KING) {
                for (int i = listIndexFrom; i < from.size(); i++) {
                    to.add(from.get(i));
                }
                return "";
            } else {
                return "New pile must start with a King";
            }
        }
        Card lastInRow = to.get(to.size() - 1);

        if (firstToMove == lastInRow) {
            //Don't want to move onto self, nor display warning
            return "ONTO_SELF";
        }
        if (firstToMove.getRank().ordinal() == lastInRow.getRank().ordinal() - 1) {
            if (listIndexFrom < from.size() - 1) {
                if (suitMoveIsValid(firstToMove.getSuit(), lastInRow.getSuit())) {
                    for (int i = listIndexFrom; i < from.size(); i++) {
                        to.add(from.get(i));
                    }
                    return "";
                } else {
                    return "Can only move onto a different coloured suit.";
                }

            } else {
                if (suitMoveIsValid(firstToMove.getSuit(), lastInRow.getSuit())) {
                    //Valid move
                    to.add(from.get(listIndexFrom));
                    return "";

                } else {
                    return "You can only move onto a card of different colour.";
                }
            }
        } else {
            return firstToMove.getRank().name() + " is not one lower than " + lastInRow.getRank().name();
        }


    }

    private boolean suitMoveIsValid(Card.Suit from, Card.Suit to) {
        if (from == to) {
            return false;
        }
        switch (to) {
            case HEARTS:
                return from != Card.Suit.DIAMONDS;
            case DIAMONDS:
                return from != Card.Suit.HEARTS;
            case CLUBS:
                return from != Card.Suit.SPADES;
            case SPADES:
                return from != Card.Suit.CLUBS;
            default:
                return false;
        }
    }

    public boolean isValidNewMove(int col, int index) {
        List<Card> cardList = board.get(col);
        for (int i = index + 1; i < cardList.size(); i++) {
            Card prev = cardList.get(i - 1);
            Card toCheck = cardList.get(i);
            if (!suitMoveIsValid(toCheck.getSuit(), prev.getSuit()) || toCheck.getRank().ordinal() != prev.getRank().ordinal() - 1) {
                return false;
            }
        }
        return true;
    }

    public List<Card> getHand() {
        return Collections.unmodifiableList(hand);
    }

    public String moveCardOntoTopRowFromRow(int boardIndexFrom, int boardIndexTo) {
        String result = moveCardOntoTopRow(hand, boardIndexTo, hand.size() - 1);
        if (result.isEmpty()) {
            hand.remove(hand.size() - 1);
        }
        return result;

    }

    public String moveCardOntoTopRowFromBoard(int toMoveTop, int boardIndexFrom, int boardIndexTo) {
        List<Card> from = board.get(boardIndexFrom);
        String result = moveCardOntoTopRow(from, boardIndexTo, toMoveTop);
        if (result.isEmpty()) {
            from.remove(from.size() - 1);
        }
        return result;
    }

    private String moveCardOntoTopRow(List<Card> from, int boardIndexTo, int listIndexFrom) {

        Card fromTop = from.get(listIndexFrom);
        if (hand.size() == 0) {
            if (fromTop.getRank() == Card.Rank.ACE) {
                hand.add(from.get(listIndexFrom));
                return "";
            } else {
                return "Can only move an Ace to an empty pile.";
            }
        } else {
            Card pileTop = hand.get(hand.size() - 1);
            //Want reference equality
            if (pileTop == fromTop) {
                //Don't want to move onto self, nor display warning
                return "ONTO_SELF";
            }
            if (listIndexFrom != from.size() - 1) {
                return "Can only move the bottom card to the top row.";
            }
            if (fromTop.getSuit().ordinal() == pileTop.getSuit().ordinal()) {
                if (fromTop.getRank().ordinal() == pileTop.getRank().ordinal() + 1) {
                    hand.add(from.get(listIndexFrom));
                    return "";
                } else {
                    return fromTop.getRank().name() + " is not one higher than " + pileTop.getRank().name();
                }

            } else {
                return "Can only move to a pile of the same suit";
            }
        }
    }

    public Queue<Card> getDeck() {
        return deck;
    }

    public boolean hasWon() {

        for (List<Card> pile : acePiles) {
            if (pile.size() != 13) {
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

    public String dealDeck(final CardPanel panel) {
        //Have to thread it as we can't hope to repaint on the EDT

        Thread t = new Thread() {
            public void run() {
                synchronized (CardGame.this) {
                    for (int i = 0; i < 10 && !deck.isEmpty(); i++) {
                        board.get(i).add(deck.poll());
                        repaintWhileDealing(panel);
                    }
                }
            }
        };
        t.start();
        return "";
    }


    public List<List<Card>> getAcePiles() {
        return acePiles;
    }

    public List<List<Card>> getKingPiles() {
        return kingPiles;
    }
}
