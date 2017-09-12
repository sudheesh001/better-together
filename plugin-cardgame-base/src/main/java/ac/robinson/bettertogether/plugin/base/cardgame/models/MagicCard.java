package ac.robinson.bettertogether.plugin.base.cardgame.models;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import ac.robinson.bettertogether.plugin.base.cardgame.dealer.DealerThread;
import ac.robinson.bettertogether.plugin.base.cardgame.player.PlayerPanel;
import ac.robinson.bettertogether.plugin.base.cardgame.player.PlayerThread;

/**
 * Created by darkryder on 22/7/17.
 */

public class MagicCard extends Card {

    private boolean activatedMagic = false;
    private long timeInPlayStarted = 0;
    boolean usePlayerThreadForTime = false;
    boolean activeCompleted = true;
    boolean randomCompleted = true;
    boolean ttlCompleted = true;

    List<MagicAttributes> attributes = new ArrayList<>();
    public void addMagicAttribute(MagicAttributes magicAttribute) {
        for(MagicAttributes attribute: attributes)
            if (attribute.type == magicAttribute.type)
                return;

        attributes.add(magicAttribute);

        switch (magicAttribute.type) {
            case ACTIVATE:
                canBePlayed = false;
                activeCompleted = false;
                break;
            case RANDOM:
                randomCompleted = false;
                break;
            case TTL:
                ttlCompleted = false;
                break;
        }
    }

    public static boolean canBeSent(Renderable renderable) {
        if (renderable instanceof MagicCard) {
            return ((MagicCard) renderable).canBePlayed;
        } else if (renderable instanceof CardDeck) {
            for (Card card: ((CardDeck) renderable).getmCards()) {
                if (card instanceof MagicCard) {
                    if (!((MagicCard) card).canBePlayed) return false;
                }
            }
        }
        return true;
    }

    public static Renderable filterCanBeSent(Renderable renderable, Context mContext) {
        if (renderable instanceof MagicCard) {
            if (((MagicCard) renderable).canBePlayed) {
                return renderable;
            }
            return null;
        } else if (renderable instanceof CardDeck) {
            CardDeck filtered = new CardDeck(mContext, false);
            for (Card card: ((CardDeck) renderable).getmCards()) {
                if (card instanceof MagicCard) {
                    if (((MagicCard) card).canBePlayed) filtered.addCardToDeck(card);
                }
            }

            return filtered;
        }
        return renderable;
    }


    private void applyMagic(long currentTimeStep) {
        for (MagicAttributes attribute: attributes) {
            attribute.type.apply(attribute, currentTimeStep, this);
        }
    }

    @Override
    public void draw(Canvas canvas) {

        if (Thread.currentThread() instanceof PlayerThread) {

            if (!activeCompleted || !randomCompleted || !ttlCompleted) {
                // before: only start the magic when the card becomes visible
                if (!activatedMagic) { // && !hidden) {
                    activatedMagic = true;
                    if (Thread.currentThread() instanceof PlayerThread) {
                        timeInPlayStarted = PlayerThread.CURRENT_TIME;
                        usePlayerThreadForTime = true;
                    } else if (Thread.currentThread() instanceof DealerThread) {
                        timeInPlayStarted = DealerThread.CURRENT_TIME;
                        usePlayerThreadForTime = false;
                    } else {
                        Log.e("WTF", "draw: which thread is this??  " + Thread.currentThread().getName());
                    }
                }
            }

            long currentTimeStep = usePlayerThreadForTime ? PlayerThread.CURRENT_TIME : DealerThread.CURRENT_TIME;
            currentTimeStep -= timeInPlayStarted;

            if (activatedMagic && (!ttlCompleted || !activeCompleted || !randomCompleted)) {
                applyMagic(currentTimeStep);
            }

            if (activatedMagic) {
                int height = 0;
                for (int i = 0; i < attributes.size(); i++) {

                    Paint textPaint = new Paint(PlayerPanel.TEXT_PAINT);
                    textPaint.setTextSize(12f);

                    MagicAttributes attr = attributes.get(i);
                    String description = attr.type.toString() + " ";
                    String extra = "";
                    switch (attr.type) {
                        case ACTIVATE:
                            if (activeCompleted) continue;
                            extra = "" + (attr.startTime - currentTimeStep);
                            if (attr.startTime - currentTimeStep < 10) {
                                textPaint.setColor(Color.GREEN);
                            }
                            height++;
                            break;
                        case RANDOM:
                            if (randomCompleted) continue;
                            extra = "" + (attr.startTime - currentTimeStep);
                            if (attr.startTime - currentTimeStep < 10) {
                                textPaint.setColor(Color.RED);
                            }
                            height++;
                            break;
                        case TTL:
                            if (ttlCompleted) continue;
                            extra = "" + (attr.endTime - currentTimeStep);
                            if (attr.endTime - currentTimeStep < 5) {
                                textPaint.setColor(Color.RED);
                            }
                            height++;
                            break;
                    }
                    description += extra;

                    canvas.drawText(
                            description,
                            getX(),
                            getY() - 18 * height,
                            textPaint);
                }
            }
        }

        super.draw(canvas);
    }

    public static class MagicAttributes {
        MagicCard.MAGIC_TYPE type;
        long startTime;
        long endTime;
        List<String> newCardIds;

        public MagicAttributes(MAGIC_TYPE type, long startTime, long endTime, List<String> newCardIds) {
            this.type = type;
            this.startTime = startTime;
            this.endTime = endTime;
            this.newCardIds = newCardIds;
        }
    }


    public enum MAGIC_TYPE {
        TTL,
        ACTIVATE, // inverse of TTL
        RANDOM // change to some other card.
        ;

        void apply(MagicAttributes attributes, long timeStep, MagicCard card) {
            if (this == TTL) {
                if (timeStep > attributes.endTime) {
                    card.safeToDelete = true;
                    card.ttlCompleted = true;
                }
            } else if (this == ACTIVATE) {
                if (timeStep > attributes.startTime) {
                    card.canBePlayed = true;
                    card.activeCompleted = true;
                }
            } else if (this == RANDOM) {
                if (timeStep > attributes.startTime) {

                    card.randomCompleted = true;
                }
            }
        }
    }
}


