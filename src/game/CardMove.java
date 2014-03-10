package game;

/**
 * Created by Andrew on 9/03/14.
 */
public interface CardMove {
    public String makeMove(CardGame game);

    public void undo(CardGame game);

    void cardReleased(int indexTo, MOVE_TYPE_TO toHand);

    public static enum MOVE_TYPE_TO {TO_ACE_PILES, TO_KING_PILES, TO_HAND}

}
