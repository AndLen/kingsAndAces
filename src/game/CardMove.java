package game;

import java.util.List;

/**
 * Created by Andrew on 29/12/13.
 */
public class CardMove {

    private final int indexFrom;
    private final MOVE_TYPE_FROM moveType;
    private int indexTo = -1;
    private boolean toAce;

    public CardMove(int indexFrom, MOVE_TYPE_FROM move_typeFROM) {
        this.indexFrom = indexFrom;
        this.moveType = move_typeFROM;
    }

    public void cardReleased(int indexTo, boolean toAce) {
        this.indexTo = indexTo;
        this.toAce = toAce;
    }

    public String makeMove(CardGame game) {
        if (indexTo != -1) {

            switch (moveType) {

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
        if (toAce) {
            return game.moveCardOntoAceFromHand(indexFrom, indexTo);
        } else {
            return game.moveCardOntoKingFromHand(indexFrom, indexTo);

        }
    }

    private String makeBoardMove(CardGame game) {
        if (toAce) {
            return game.moveCardOntoAceFromBoard(indexFrom, indexTo);
        } else {
            return game.moveCardOntoKingFromBoard(indexFrom, indexTo);

        }
    }

    private String makeKingPilesMove(CardGame game) {
        if (toAce) {
            return game.moveCardOntoAceFromKing(indexFrom, indexTo);
        } else {
            return "Can only move to a pile of the same suit";

        }
    }

    private String makeAcePilesMove(CardGame game) {
        if (toAce) {
            return "Can only move to a pile of the same suit";
        } else {
            return game.moveCardOntoKingFromAce(indexFrom, indexTo);

        }

    }


    public int getIndexFrom() {
        return indexFrom;
    }


    public String toString() {
        return "FROM:" + indexFrom + (indexTo == -1 ? "" : " TO " + indexTo);
    }

    public MOVE_TYPE_FROM getMoveType() {
        return moveType;
    }

    public void undo(CardGame game) {
        if (moveType == MOVE_TYPE_FROM.FROM_HAND) {
            List<Card> hand = game.getHand().getList();
            hand.add(indexFrom, hand.remove(indexTo));
        } else {
            List<Card> to = null;
            List<Card> from = toAce ? game.getAcePiles().get(indexTo) : game.getKingPiles().get(indexTo);
            switch (moveType) {
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
            assert to != null;
            to.add(from.remove(from.size() - 1));
        }
    }

    public static enum MOVE_TYPE_FROM {FROM_ACE_PILES, FROM_KING_PILES, FROM_BOARD, FROM_HAND}

}
