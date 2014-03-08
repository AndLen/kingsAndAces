package gui;

import game.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.List;
import java.util.Stack;

/**
 * Created by Andrew on 28/12/13.
 */
public class CardPanel extends JPanel implements ComponentListener, MouseListener, MouseMotionListener {
    private static final Color BACKGROUND_GREEN = new Color(0, 150, 0);
    private static final double CARD_IMAGE_WIDTH = 224.25;
    private static final double CARD_IMAGE_HEIGHT = 312.8125;
    protected static double CARD_WIDTH;
    protected static double CARD_HEIGHT;
    private static int NUMBER_CLICKS = 0;
    private static long LAST_PRESS = System.currentTimeMillis();
    private static double CARD_X_GAP;
    private static double CARD_Y_GAP;
    private static double Y_BOARD_OFFSET;
    private static double X_BOARD_OFFSET;
    private static double DECK_Y;
    private static String ERROR;
    private final CardGame game;
    private final Object lock = new Object();
    //Moving cards
    private CardMove activeMove = null;
    private int activeX = -1;
    private int activeY = -1;
    private boolean successfulPaint = false;
    private BufferedImage lastImage;

    public CardPanel(CardGame game) {
        this.game = game;
        this.setPreferredSize(new Dimension(1000, 1000));
        this.addMouseListener(this);
        this.addMouseMotionListener(this);
        this.addComponentListener(this);
        //Set up initial constants
        componentResized(null);
    }

    public void paint(Graphics gOriginal) {
        //Double buffering and caching for dragging
        lastImage = new BufferedImage(getWidth(), getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = lastImage.createGraphics();
        g.setColor(BACKGROUND_GREEN);
        g.fillRect(0, 0, this.getWidth(), this.getHeight());

        renderHandIfAny(g);
        final List<List<Card>> gameBoard = game.getBoard();
        renderBoard(g, gameBoard);
        //Lets render this in the dragged event instead to lower load
        //renderDragCards(g, gameBoard);
        renderDeck(g);
        renderAcePile(g);
        renderKingPile(g);
        renderError(g);
        successfulPaint = true;
        gOriginal.drawImage(lastImage, 0, 0, null);
        g.dispose();
    }

    private void renderKingPile(Graphics2D g) {
        double x = X_BOARD_OFFSET + CARD_WIDTH + CARD_X_GAP;
        double y = Y_BOARD_OFFSET;
        g.setColor(Color.white);
        g.drawString("King Down", (int) x, (int) y - 1);

        for (List<Card> kingPile : game.getKingPiles()) {
            if (kingPile.size() > 0) {
                renderCard(kingPile.get(kingPile.size() - 1), g, x, y);
            }
            y += CARD_HEIGHT + CARD_Y_GAP;
        }
    }

    private void renderAcePile(Graphics2D g) {
        double x = X_BOARD_OFFSET + CARD_WIDTH * 6 + CARD_X_GAP * 6;
        double y = Y_BOARD_OFFSET;
        g.setColor(Color.white);
        g.drawString("Ace Up", (int) x, (int) y - 1);
        for (List<Card> acePile : game.getAcePiles()) {
            if (acePile.size() > 0) {
                renderCard(acePile.get(acePile.size() - 1), g, x, y);
            }
            y += CARD_HEIGHT + CARD_Y_GAP;

        }

    }

    private void renderError(Graphics2D g) {
        if (ERROR != null) {
            g.setFont(new Font("TimesRoman", Font.PLAIN, 30));
            FontMetrics fontMetrics = g.getFontMetrics();
            float xLeft = (float) (CARD_WIDTH + CARD_X_GAP);

            float x = (getWidth() - xLeft - fontMetrics.stringWidth(ERROR)) / 2;
            x += xLeft;
            float y = (float) (DECK_Y + CARD_HEIGHT / 2);
            g.setColor(Color.RED);
            g.drawString(ERROR, x, y);
        }
    }

    private void renderDeck(Graphics2D g) {
        Stack<Card> deck = game.getDeck();
        Card topCard = deck.size() == 0 ? null : deck.peek();

        double x = X_BOARD_OFFSET;
        double y = DECK_Y;

        g.setColor(Color.white);
        g.drawString(deck.size() + " cards in deck", (float) x, (float) y - 1);

        if (topCard != null && topCard.isHidden()) {
            topCard = null;
        }
        if (topCard == null) {
            renderCard(null, g, x, y);
        } else {
            renderUpsideDownCard(g, x, y, topCard.isBlueBack());
        }
    }

    private void renderDragCards(Graphics2D g, List<List<Card>> gameBoard) {
        if (activeMove != null && activeX != -1 && activeY != -1) {
            if (activeMove.getMoveType() == CardMove.MOVE_TYPE_FROM.FROM_HAND) {
                List<Card> from = game.getHand().getList();
                renderCard(from.get(activeMove.getIndexFrom()), g, activeX - (CARD_WIDTH / 2), activeY);
            } else {
                List<Card> from = null;
                switch (activeMove.getMoveType()) {
                    case FROM_ACE_PILES:
                        from = game.getAcePiles().get(activeMove.getIndexFrom());
                        break;
                    case FROM_KING_PILES:
                        from = game.getKingPiles().get(activeMove.getIndexFrom());
                        break;
                    case FROM_BOARD:
                        from = game.getBoard().get(activeMove.getIndexFrom());
                        break;
                }
                assert from != null;
                renderCard(from.get(from.size() - 1), g, activeX - (CARD_WIDTH / 2), activeY);

            }
        }
    }

    private void renderBoard(Graphics2D g, List<List<Card>> gameBoard) {
        double x = X_BOARD_OFFSET + CARD_WIDTH * 2 + CARD_X_GAP * 2;
        double y = Y_BOARD_OFFSET + CARD_HEIGHT / 2;

        if (gameBoard.size() == 0) {
            //Game not inited yet
            return;
        }

        for (int i = 0; i < gameBoard.size(); i++) {
            List<Card> cards = gameBoard.get(i);
            if (i != 0) {
                if (i % 4 == 0) {
                    x = X_BOARD_OFFSET + CARD_WIDTH * 2 + CARD_X_GAP * 2;
                    y += CARD_Y_GAP + CARD_HEIGHT;

                } else {
                    x += CARD_WIDTH + CARD_X_GAP;
                }
            }

            if (cards.size() > 0) {
                g.setColor(Color.white);
                int size = cards.size();
                g.drawString(size + (size == 1 ? " card" : " cards"), (int) x, (int) y - 1);
                renderCard(cards.get(cards.size() - 1), g, x, y);
            }

        }
    }


    private void renderHandIfAny(Graphics2D g) {
        Hand hand = game.getHand();
        if (hand.getIndex() != -1 && hand.getList().size() > 0) {

            final double handXGap = handXGap(hand.getList().size());

            double x = handXStart();
            double y = handYStart();
            for (Card card : hand.getList()) {
                renderCard(card, g, x, y);
                x += handXGap + CARD_WIDTH;
            }
        }

    }

    private double handXGap(int handSize) {
        return (3 * CARD_X_GAP + 4 * CARD_WIDTH - handSize * CARD_WIDTH) / (handSize - 1);
    }

    private double handXStart() {
        return X_BOARD_OFFSET + 2 * CARD_WIDTH + 2 * CARD_X_GAP;
    }

    private double handXEnd() {
        return handXStart() + 4 * CARD_WIDTH + 3 * CARD_X_GAP;

    }

    private double handYStart() {
        return Y_BOARD_OFFSET + 3.5 * CARD_HEIGHT + 3 * CARD_Y_GAP;
    }

    private double handYEnd() {
        return handYStart() + CARD_HEIGHT;
    }

    private void renderUpsideDownCard(Graphics2D g, double x, double y, boolean blue) {
        AffineTransform oldTransform = getAffineTransform(g, x, y);
        ImageManager.renderBlank(g, blue);
        g.setTransform(oldTransform);
    }

    private void renderCard(Card card, Graphics2D g, double x, double y) {
        AffineTransform oldTransform = getAffineTransform(g, x, y);
        ImageManager.renderSVG(card, g);
        //Revert changes.
        g.setTransform(oldTransform);
    }

    private AffineTransform getAffineTransform(Graphics2D g, double x, double y) {
        //Make necessary adjustments so SVG is drawn correctly
        AffineTransform oldTransform = g.getTransform();

        double xScale = CARD_WIDTH / CARD_IMAGE_WIDTH;
        double yScale = CARD_HEIGHT / CARD_IMAGE_HEIGHT;
        g.translate(x, y);
        g.scale(xScale, yScale);

        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        return oldTransform;
    }

    @Override
    public void componentResized(ComponentEvent e) {
        double screenWidth = Toolkit.getDefaultToolkit().getScreenSize().getWidth();
        CARD_X_GAP = getWidth() / 49;
        CARD_WIDTH = (getWidth() - CARD_X_GAP) / 8;
        CARD_WIDTH = Math.min(CARD_WIDTH, screenWidth * 0.065);

        X_BOARD_OFFSET = (getWidth() - (CARD_WIDTH * 7) - (CARD_X_GAP * 6)) / 2;

        CARD_HEIGHT = CARD_WIDTH / (CARD_IMAGE_WIDTH / CARD_IMAGE_HEIGHT);
        CARD_Y_GAP = CARD_HEIGHT / 4;
        Y_BOARD_OFFSET = CARD_Y_GAP;
        DECK_Y = getHeight() - CARD_Y_GAP - CARD_HEIGHT;
    }

    @Override
    public void componentMoved(ComponentEvent e) {

    }

    @Override
    public void componentShown(ComponentEvent e) {

    }

    @Override
    public void componentHidden(ComponentEvent e) {

    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

        try {
            if (e.getX() < X_BOARD_OFFSET || e.getX() > (getWidth() - X_BOARD_OFFSET) || e.getY() < Y_BOARD_OFFSET) {
                //Clicked off the board, nothing to do with us.
                return;
            }
            if ((System.currentTimeMillis() - LAST_PRESS) < 400) {
                NUMBER_CLICKS++;
            } else {
                NUMBER_CLICKS = 0;
            }
            LAST_PRESS = System.currentTimeMillis();
            if (clickedOnDeck(e)) {
                if (game.hasDealt()) processDeckClick();
            } else if (clickedOnHand(e)) {
                if (game.hasDealt()) processMoveFromHand(e);
            } else if (clickedOnKingPile(e)) {
                if (game.hasDealt()) processMoveFromKingPile(e);
            } else if (clickedOnAcePile(e)) {
                if (game.hasDealt()) processMoveFromAcePile(e);
            } else {
                processMoveFromBoard(e);
            }

            System.out.println(activeMove);
            activeX = e.getX();
            activeY = e.getY();
        } finally {
            this.repaint();
        }
    }

    private void processMoveFromAcePile(MouseEvent e) {
        int index = (int) (e.getY() / (CARD_Y_GAP + CARD_HEIGHT));
        if (index < 4) {
            activeMove = new CardMove(index, CardMove.MOVE_TYPE_FROM.FROM_ACE_PILES);
        }

    }

    private void processMoveFromKingPile(MouseEvent e) {
        int index = (int) (e.getY() / (CARD_Y_GAP + CARD_HEIGHT));
        if (index < 4) {
            activeMove = new CardMove(index, CardMove.MOVE_TYPE_FROM.FROM_KING_PILES);
        }
    }

    private boolean clickedOnKingPile(MouseEvent e) {
        //Beautiful
        return e.getX() >= X_BOARD_OFFSET + CARD_WIDTH + CARD_X_GAP && e.getX() <= X_BOARD_OFFSET + CARD_WIDTH * 2 + CARD_X_GAP && e.getY() >= Y_BOARD_OFFSET && e.getY() < Y_BOARD_OFFSET + CARD_HEIGHT * 4 + CARD_Y_GAP * 3;
    }

    private boolean clickedOnAcePile(MouseEvent e) {
        return e.getX() >= X_BOARD_OFFSET + CARD_WIDTH * 6 + CARD_X_GAP * 6 && e.getX() <= X_BOARD_OFFSET + CARD_WIDTH * 7 + CARD_X_GAP * 6 && e.getY() >= Y_BOARD_OFFSET && e.getY() < Y_BOARD_OFFSET + CARD_HEIGHT * 4 + CARD_Y_GAP * 3;

    }

    private void processMoveFromHand(MouseEvent e) {
        List<Card> hand = game.getHand().getList();
        if (hand.size() > 0) {

            final double handXGap = handXGap(hand.size());

            int index = (int) ((e.getX() - handXStart()) / (handXGap + CARD_WIDTH));
            activeMove = new CardMove(index, CardMove.MOVE_TYPE_FROM.FROM_HAND);

        }
    }

    private boolean clickedOnHand(MouseEvent e) {
        return e.getX() >= handXStart() && e.getX() <= handXEnd() && e.getY() >= handYStart() && e.getY() <= handYEnd();
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        //Don't want multiple threads in here really
        synchronized (lock) {
            try {
                if (e.getX() < X_BOARD_OFFSET || e.getX() > (getWidth() - X_BOARD_OFFSET)) {
                    //Clicked off the board, nothing to do with us.
                    activeMove = null;
                    return;
                }

                if (clickedOnDeck(e)) {
                    if (game.canAddToDeckFromBoard()) {
                        processAddToDeck(e);
                    }
                    //Otherwise do nothing
                } else if (game.hasDealt()) {
                    if (clickedOnHand(e)) {
                        processMoveToHand(e);
                    } else if (clickedOnKingPile(e)) {
                        processMoveToKingPile(e);
                    } else if (clickedOnAcePile(e)) {
                        processMoveToAcePile(e);
                    } else {
                        //Do nothing - can't move to board.
                    }
                }
                activeMove = null;
            } finally {
                this.repaint();
            }
            if (game.hasWon()) {
                int elapsedTime = (int) (System.currentTimeMillis() - game.getStartTime());
                StorageManager.win(elapsedTime, game.getNumMoves());
                String[] options = new String[]{"Play Again", "Quit"};
                int result = JOptionPane.showOptionDialog(CardFrame.showStats(), "Congratulations! You have won.\n Time: " + (elapsedTime / 1000) + " s\nMoves: " + game.getNumMoves(), "Win!", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
                if (result == JOptionPane.YES_OPTION) {
                    game.restart();

                } else {
                    System.exit(0);
                }
            }
        }
    }

    private void processAddToDeck(MouseEvent e) {
        String result = game.addToDeck(activeMove);
        processMoveResult(result, activeMove);
    }

    private void processMoveToHand(MouseEvent e) {
        List<Card> hand = game.getHand().getList();
        if (hand.size() > 0) {

            final double handXGap = handXGap(hand.size());

            int indexTo = (int) ((e.getX() - handXStart()) / (handXGap + CARD_WIDTH));
            String result = game.makeHandMove(activeMove.getIndexFrom(), indexTo);
            processMoveResult(result, activeMove);

        }
    }

    private void processMoveToAcePile(MouseEvent e) {
        int index = (int) (e.getY() / (CARD_Y_GAP + CARD_HEIGHT));
        if (index < 4) {
            activeMove.cardReleased(index, true);
            String result = activeMove.makeMove(game);
            processMoveResult(result, activeMove);
        }
    }

    private void processMoveToKingPile(MouseEvent e) {
        int index = (int) (e.getY() / (CARD_Y_GAP + CARD_HEIGHT));
        if (index < 4) {
            activeMove.cardReleased(index, false);
            String result = activeMove.makeMove(game);
            processMoveResult(result, activeMove);
        }
    }


    private void processDeckClick() {
        //Can assume did click on the deck.
        final Stack<Card> deck = game.getDeck();
        if (!deck.isEmpty()) {
            Card fromDeck = deck.pop();
            game.updateHand(fromDeck);
        }

    }

    private int findCol(int xPressed) {
        xPressed -= X_BOARD_OFFSET + CARD_WIDTH * 2 + CARD_X_GAP * 2;
        int col = (int) (xPressed / (CARD_WIDTH + CARD_X_GAP));
        System.out.println("Col: " + col);
        return col;
    }

    private int findRow(int yPressed) {
        yPressed -= Y_BOARD_OFFSET + CARD_HEIGHT / 2;
        int row = (int) (yPressed / (CARD_HEIGHT + CARD_Y_GAP));
        System.out.println("Row: " + row);
        return row;
    }


    private boolean clickedOnDeck(MouseEvent e) {
        return e.getX() >= X_BOARD_OFFSET && e.getX() <= X_BOARD_OFFSET + CARD_WIDTH && e.getY() >= DECK_Y && e.getY() <= DECK_Y + CARD_HEIGHT;
    }

    private void doubleClick(MouseEvent e) {
        //Do nothing atm
    }

    private void processMoveFromBoard(MouseEvent e) {
        int row = findRow(e.getY());
        int col = findCol(e.getX());
        int index = row * 4 + col;
        if (index > -1 && index < 12) {
            activeMove = new CardMove(index, CardMove.MOVE_TYPE_FROM.FROM_BOARD);
        }
    }

    private void processMoveResult(String result, CardMove move) {
        if (result.isEmpty()) {
            game.getHistory().push(move);
        } else if (!result.equals("ONTO_SELF")) {
            storeError(result);
        }
    }

    public void storeError(final String error) {
        //Render and disappear it
        new Thread() {
            public void run() {
                ERROR = error;
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if (error.equals(ERROR)) {
                    ERROR = null;
                    CardPanel.this.repaint();
                }
            }
        }.start();
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (activeMove != null) {
            activeX = e.getX();
            activeY = e.getY();
        } else {
            activeX = -1;
            activeY = -1;
        }
        BufferedImage copyImage = new BufferedImage(lastImage.getWidth(), lastImage.getHeight(), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = copyImage.createGraphics();
        g.drawImage(lastImage, 0, 0, null);
        renderDragCards(g, game.getBoard());
        Graphics panelGraphics = getGraphics();
        panelGraphics.drawImage(copyImage, 0, 0, null);
        panelGraphics.dispose();
        g.dispose();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }

    public boolean isReady() {
        return successfulPaint;
    }
}
