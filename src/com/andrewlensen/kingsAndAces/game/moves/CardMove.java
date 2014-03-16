package com.andrewlensen.kingsAndAces.game.moves;

import com.andrewlensen.kingsAndAces.game.CardGame;
import com.andrewlensen.kingsAndAces.gui.CardPanel;

/**
 * Created by Andrew on 9/03/14.
 */
public interface CardMove {
    public String makeMove(CardGame game, CardPanel panel);

    public boolean undo(CardGame game, CardPanel panel);

    void cardReleased(int indexTo, MOVE_TYPE_TO toHand);

    public static enum MOVE_TYPE_TO {TO_ACE_PILES, TO_KING_PILES, TO_HAND,TO_DECK}

}
