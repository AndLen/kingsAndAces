package game;

import gui.CardPanel;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Andrew on 29/12/13.
 */
public class CardMove {

    private final int boardIndexFrom;
    private final int toMove;
    private final MOVE_TYPE moveType;
    private CardPanel panel;
    private int boardIndexTo = -1;
    private boolean hand;
    //private int numCardsMoved;

    public CardMove(int toMove, int boardIndexFrom, CardGame game) {
        this.toMove = toMove;
        this.boardIndexFrom = boardIndexFrom;
        moveType = MOVE_TYPE.FROM_BOARD;
        List<Card> toHide = game.getBoard().get(boardIndexFrom);
        for (int i = toMove; i < toHide.size(); i++) {
            toHide.get(i).setHidden(true);
        }
    }

    public CardMove(CardPanel panel) {
        this.panel = panel;
        //deck move
        toMove = -1;
        boardIndexFrom = -1;
        moveType = MOVE_TYPE.FROM_DECK;
    }

    public CardMove(int boardIndexFrom, CardGame game) {
        toMove = 1;
        this.boardIndexFrom = boardIndexFrom;
        moveType = MOVE_TYPE.FROM_PILE;
        List<Card> pile = game.getHand();
        pile.get(pile.size() - 1).setHidden(true);
    }

    public void cardReleased(int boardIndexTo, boolean topRow) {
        this.boardIndexTo = boardIndexTo;
        this.hand = topRow;
    }

    public String makeMove(CardGame game) {
        switch (moveType) {

            case FROM_DECK:
                return makeDeckMove(game);
            case FROM_BOARD:
                if (boardIndexTo != -1) return makeBoardMove(game);
                break;
            case FROM_PILE:
                if (boardIndexTo != -1) return makePileMove(game);
                break;
        }

        return "ERROR";
    }

    private String makePileMove(CardGame game) {
        if (hand) {
            return game.moveCardOntoTopRowFromRow(boardIndexFrom, boardIndexTo);
        } else {
            return game.moveCardOntoCardFromTopRow(boardIndexFrom, boardIndexTo);
        }
    }

    private String makeDeckMove(CardGame game) {
        return game.dealDeck(panel);
    }

    private String makeBoardMove(CardGame game) {
        if (hand) {

            return game.moveCardOntoTopRowFromBoard(toMove, boardIndexFrom, boardIndexTo);
        } else {
            //Need this in case we undo to see how many we moved easily (or at all?).
            //If size = 6 and toMove = 5, we moved 6-5 = 1 card
            //If size = 6 and toMove = 3, we moved 6-3 = 3 cards
            //numCardsMoved = game.getBoard().get(boardIndexFrom).size() - toMove;
            return game.moveCardOntoCardFromBoard(toMove, boardIndexFrom, boardIndexTo);
        }
    }

    public int getBoardIndexFrom() {
        return boardIndexFrom;
    }

    public int getToMove() {
        return toMove;
    }

    public String toString() {
        return "FROM:" + boardIndexFrom + " TOP CARD INDEX: " + toMove + (boardIndexTo == -1 ? "" : " TO " + boardIndexTo);
    }

    public MOVE_TYPE getMoveType() {
        return moveType;
    }

    public void unhideCards(CardGame game) {
        //Not efficient, but easy
        if (!game.getDeck().isEmpty()) {
            game.getDeck().peek().setHidden(false);
        }
        for (List<Card> cardList : game.getBoard()) {
            for (Card card : cardList) {
                card.setHidden(false);
            }
        }
        for (Card card : game.getHand()) {
            card.setHidden(false);
        }


    }

    public void undo(CardGame game) {

        switch (moveType) {
            case FROM_DECK:
                //This sucks a bit
                LinkedList<Card> queueList = (LinkedList<Card>) game.getDeck();
                List<List<Card>> board = game.getBoard();
                for (int i = board.size() - 1; i >= 0; i--) {
                    List<Card> cardList = board.get(i);
                    queueList.add(0, cardList.remove(cardList.size() - 1));
                }
                break;
            case FROM_BOARD:
                List<Card> boardCol = game.getBoard().get(boardIndexFrom);
                if (hand) {
                    List<Card> pile = game.getHand();
                    boardCol.add(pile.remove(pile.size() - 1));
                } else {
                    //The fun one
                    List<Card> otherCol = game.getBoard().get(boardIndexTo);
                    // for (int i = otherCol.size() - numCardsMoved; i < otherCol.size(); ) {
                    //    boardCol.add(otherCol.remove(i));
                    //  }
                    boardCol.add(otherCol.remove(otherCol.size() - 1));
                }
                break;
            case FROM_PILE:
                List<Card> pile = game.getHand();
                if (hand) {
                    List<Card> otherPile = game.getHand();
                    pile.add(otherPile.remove(otherPile.size() - 1));
                } else {
                    List<Card> col = game.getBoard().get(boardIndexTo);
                    pile.add(col.remove(col.size() - 1));
                }
                break;
        }
    }

    public static enum MOVE_TYPE {FROM_DECK, FROM_BOARD, FROM_PILE}
}
