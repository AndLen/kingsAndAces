package com.andrewlensen.kingsAndAces.game.moves;


import com.andrewlensen.kingsAndAces.game.Card;
import com.andrewlensen.kingsAndAces.game.CardGame;
import com.andrewlensen.kingsAndAces.game.Hand;
import com.andrewlensen.kingsAndAces.gui.CardPanel;
import com.sun.istack.internal.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class DeckClickMove implements CardMove {
    private Hand prevHand;

    @Override
    public String makeMove(CardGame game, CardPanel panel) {
        Hand hand = game.getHand();
        List<List<Card>> board = game.getBoard();
        Card fromDeck = game.getDeck().pop();
        //Do this first in case we get the same index twice in a row!
        if (hand != null && hand.getIndex() != -1) {
            this.prevHand = hand;
            board.set(prevHand.getIndex(), hand.getList());
        }

        List<Card> toBeHand = board.get(fromDeck.getRank().ordinal());
        toBeHand.add(fromDeck);

        game.setHand(new Hand(toBeHand, fromDeck.getRank().ordinal()));
        //Nothing there.
        board.set(fromDeck.getRank().ordinal(), new CopyOnWriteArrayList<Card>());
        return "";
    }

    @Override
    public boolean undo(CardGame game, CardPanel panel) {
        Hand hand = game.getHand();
        List<List<Card>> board = game.getBoard();
        List<Card> fromHandList = hand.getList();
        //Must be the last one as was just done
        Card fromHand = fromHandList.remove(fromHandList.size() - 1);

        game.getDeck().push(fromHand);

        if (hand != null && hand.getIndex() != -1) {
            board.set(hand.getIndex(), hand.getList());
        }

        game.setHand(prevHand);
        //Nothing there
        if (prevHand != null) {
            board.set(prevHand.getIndex(), new CopyOnWriteArrayList<Card>());
        }
        return true;
    }

    @Override
    public void cardReleased(int indexTo, @Nullable MOVE_TYPE_TO toHand) {

    }

    public String toString() {
        return "Prev hand: " + prevHand + " TYPE: " + this.getClass().getSimpleName();
    }
}
