package com.andrewlensen.kingsAndAces.game;

/**
 * Created by Andrew on 28/12/13.
 */
public class Card {
    private final Suit suit;
    private final Rank rank;
    //e.g. hiding while being dragged
    private boolean isHidden = false;
    //i.e. below other cards on the board.
    private final boolean blueBack;

    public Card(Suit suit, Rank rank, boolean blueBack) {

        this.suit = suit;
        this.rank = rank;
        this.blueBack = blueBack;
    }

    public Suit getSuit() {
        return suit;
    }

    public Rank getRank() {
        return rank;
    }

    public String toString() {
        return rank.name() + suit.name();
    }

    public String shortToString() {
        return "" + rank.name().charAt(0) + suit.name().charAt(0);
    }

    public boolean matchesAndOneAbove(Card other) {
        return (other.getSuit() == this.suit && this.rank.ordinal() == other.getRank().ordinal() + 1);
    }

    public boolean isHidden() {
        return isHidden;
    }

    public void setHidden(boolean isHidden) {
        this.isHidden = isHidden;
    }


    public boolean isBlueBack() {
        return blueBack;
    }

    public enum Suit {
        SPADES, CLUBS, DIAMONDS, HEARTS;
    }

    public enum Rank {
        ACE, TWO, THREE, FOUR, FIVE, SIX, SEVEN, EIGHT, NINE, TEN, JACK, QUEEN, KING;
    }
}
