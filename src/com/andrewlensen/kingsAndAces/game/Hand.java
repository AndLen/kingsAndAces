package com.andrewlensen.kingsAndAces.game;

import java.util.List;

/**
 * Created by Andrew on 8/03/14.
 */
public class Hand {
    private final List<Card> hand;
    private final int index;

    public Hand(List<Card> hand, int index) {
        this.index = index;
        this.hand = hand;
    }

    public List<Card> getList() {
        return hand;
    }

    public int getIndex() {
        return index;
    }
}
