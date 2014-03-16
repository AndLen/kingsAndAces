package com.andrewlensen.kingsAndAces.game.moves;

import com.andrewlensen.kingsAndAces.game.Card;
import com.andrewlensen.kingsAndAces.game.CardGame;
import com.andrewlensen.kingsAndAces.gui.CardPanel;

import java.util.List;

public class CardMoveImpl implements CardMove {

    private final int indexFrom;
    private final MOVE_TYPE_FROM moveTypeFrom;
    private int indexTo = -1;
    private MOVE_TYPE_TO moveTypeTo;

    public CardMoveImpl(int indexFrom, MOVE_TYPE_FROM move_typeFROM) {
        this.indexFrom = indexFrom;
        this.moveTypeFrom = move_typeFROM;
    }

    public void cardReleased(int indexTo, MOVE_TYPE_TO move_type_to) {
        this.indexTo = indexTo;
        this.moveTypeTo = move_type_to;
    }

    public String makeMove(CardGame game, CardPanel panel) {
        if (indexTo != -1) {

            switch (moveTypeFrom) {

                case FROM_ACE_PILES:
                    return makeAcePilesMove(game);

                case FROM_KING_PILES:
                    return makeKingPilesMove(game);
                case FROM_BOARD:
                    return makeBoardMove(game);
                case FROM_HAND:
                    return makeHandMove(game);
            }
        }
        return "ERROR";
    }

    private String makeHandMove(CardGame game) {
        switch (moveTypeTo) {

            case TO_ACE_PILES:
                return game.moveCardOntoAceFromHand(indexFrom, indexTo);

            case TO_KING_PILES:
                return game.moveCardOntoKingFromHand(indexFrom, indexTo);

            case TO_HAND:
                return game.makeHandMove(indexFrom, indexTo);


        }
        return "ERROR";

    }

    private String makeBoardMove(CardGame game) {
        switch (moveTypeTo) {
            case TO_ACE_PILES:
                return game.moveCardOntoAceFromBoard(indexFrom, indexTo);

            case TO_KING_PILES:
                return game.moveCardOntoKingFromBoard(indexFrom, indexTo);

            case TO_HAND:
                return "Cannot move to hand.";

            case TO_DECK:
                return game.addToDeck(indexFrom);
        }
        return "ERROR";
    }

    private String makeKingPilesMove(CardGame game) {
        switch (moveTypeTo) {

            case TO_ACE_PILES:
                return game.moveCardOntoAceFromKing(indexFrom, indexTo);

            case TO_KING_PILES:
                return "Can only move to a pile of the same suit";

            case TO_HAND:
                return "Cannot move to hand.";

        }
        return "ERROR";

    }

    private String makeAcePilesMove(CardGame game) {
        switch (moveTypeTo) {

            case TO_ACE_PILES:
                return "Can only move to a pile of the same suit";
            case TO_KING_PILES:
                return game.moveCardOntoKingFromAce(indexFrom, indexTo);
            case TO_HAND:
                return "Cannot move to hand.";
        }
        return "ERROR";

    }


    public int getIndexFrom() {
        return indexFrom;
    }


    public String toString() {
        return "FROM:" + indexFrom + (indexTo == -1 ? "" : " TO " + indexTo) + " TYPE FROM: " + moveTypeFrom + " TYPE TO: " + moveTypeTo;
    }

    public MOVE_TYPE_FROM getMoveTypeFrom() {
        return moveTypeFrom;
    }

    public boolean undo(CardGame game, CardPanel panel) {
        if (moveTypeFrom == MOVE_TYPE_FROM.FROM_HAND) {

            switch (moveTypeTo) {

                case TO_ACE_PILES:
                    List<Card> fromAce = game.getAcePiles().get(indexTo);
                    game.getHand().getList().add(indexFrom, fromAce.remove(fromAce.size() - 1));
                    break;
                case TO_KING_PILES:
                    List<Card> fromKing = game.getKingPiles().get(indexTo);
                    game.getHand().getList().add(indexFrom, fromKing.remove(fromKing.size() - 1));
                    break;
                case TO_HAND:
                    List<Card> hand = game.getHand().getList();
                    hand.add(indexFrom, hand.remove(indexTo));
                    return true;
            }

        } else {
            List<Card> to = null;
            List<Card> from = null;
            switch (moveTypeFrom) {
                case FROM_ACE_PILES:
                    to = game.getAcePiles().get(indexFrom);
                    break;
                case FROM_KING_PILES:
                    to = game.getKingPiles().get(indexFrom);
                    break;
                case FROM_BOARD:
                    to = game.getBoard().get(indexFrom);
                    break;
            }
            switch (moveTypeTo) {

                case TO_ACE_PILES:
                    from = game.getAcePiles().get(indexTo);
                    break;
                case TO_KING_PILES:
                    from = game.getKingPiles().get(indexTo);
                    break;

                case TO_DECK:
                    from = game.getDeck();
                    break;
            }
            assert to != null;
            assert from != null;
            to.add(from.remove(from.size() - 1));
        }
        return true;
    }


    public static enum MOVE_TYPE_FROM {FROM_ACE_PILES, FROM_KING_PILES, FROM_BOARD, FROM_HAND}


}
