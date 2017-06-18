package ac.robinson.bettertogether.plugin.base.cardgame.utils.wheelview.transformer;

import android.graphics.drawable.Drawable;

import ac.robinson.bettertogether.plugin.base.cardgame.utils.wheelview.WheelView;

public interface WheelSelectionTransformer {
    void transform(Drawable drawable, WheelView.ItemState itemState);
}
