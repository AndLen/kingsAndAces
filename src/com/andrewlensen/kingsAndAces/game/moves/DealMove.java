package com.andrewlensen.kingsAndAces.game.moves;

import com.andrewlensen.kingsAndAces.game.Card;
import com.andrewlensen.kingsAndAces.game.CardGame;
import com.andrewlensen.kingsAndAces.gui.CardPanel;
import com.andrewlensen.kingsAndAces.gui.RenderMessage;

import java.util.List;
import java.util.Stack;

/**
 * Created by Andrew on 11/03/14.
 */
public class DealMove implements CardMove {
    @Override
    public String makeMove(CardGame game, CardPanel panel) {
        List<List<Card>> board = game.getBoard();
        List<Card> pack = game.getPack();
        Stack<Card> deck = game.getDeck();

        game.setCanAddToDeckFromBoard(false);

        for (int j = 0; j < 12; j++) {
            //Want them all unrevealed
            board.get(j).add(pack.remove(0));
            CardGame.repaintWhileDealing(panel);
        }
        //Get the user to check for adding to the deck
        game.setCanAddToDeckFromBoard(true);

        String msg = pack.size() == 2 ? "Hit Enter/click the pack to finish dealing" : "Hit Enter/click the pack to continue dealing";
        panel.storeMessage(new RenderMessage(msg, false));

        CardGame.repaintWhileDealing(panel);
        game.waitForNextDealConfirmation();

        //  if (deck.size() == board.get(0).size() - 1) {
        //Only add to the deck if they didn't undo.
        deck.push(pack.remove(0));
        if (pack.size() == 1) {
            deck.push(pack.remove(0));
        }
        //  }
        System.out.println("DECK ADDED: " + deck);
        CardGame.repaintWhileDealing(panel);
        System.out.println("PACK:" + pack);
        System.out.println("---------------------------");
        return "";

    }

    @Override
    public boolean undo(final CardGame game, final CardPanel panel) {
        List<Card> pack = game.getPack();
        if (game.canAddToDeckFromBoard()) {
            //We don't want to do this halfway through a deal
            if (pack != null) {
                Stack<Card> deck = game.getDeck();
                List<List<Card>> board = game.getBoard();
                System.out.println("Pack size:" + pack.size());
                //        if (deck.size() == 8 && board.get(0).size() == 7) {
                if (pack.size() == 0) {
                    //Special case where we deal 2 on the last one
                    pack.add(0, deck.pop());
                    pack.add(0, deck.pop());
                    System.out.println("Special undo");
                }// else if (deck.size() != 0 && deck.size() == board.get(0).size()) {
                else {
                    pack.add(0, deck.pop());
                }
                for (int i = 11; i >= 0; i--) {
                    List<Card> pile = board.get(i);
                    if (pile.size() > 0) {
                        pack.add(0, pile.remove(pile.size() - 1));
                    }
                }

                CardGame.repaintWhileDealing(panel);
                if (game.hasDealt()) {
                    new Thread() {
                        public void run() {
                            game.deal(panel);

                        }
                    }.start();

                }
                System.out.println("DECK UNDID: " + deck);
                System.out.println("PACK:" + pack);

            }
            System.out.println("===========================");
            return true;
        } else {
            return false;
        }
    }

    public String toString() {
        return "DealMove";
    }

    @Override
    public void cardReleased(int indexTo, MOVE_TYPE_TO toHand) {

    }
}
