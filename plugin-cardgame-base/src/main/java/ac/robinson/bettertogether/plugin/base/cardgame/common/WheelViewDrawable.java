package ac.robinson.bettertogether.plugin.base.cardgame.common;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;

/**
 * Created by t-apmehr on 6/18/2017.
 */

public class WheelViewDrawable extends Drawable {

    private String text;
    public int count;
    private final Paint paint;

    public WheelViewDrawable(String text) {

        this.text = text;

        this.paint = new Paint();
        paint.setColor(Color.WHITE);
        paint.setTextSize(52f);
        paint.setAntiAlias(true);
        paint.setFakeBoldText(true);
        paint.setShadowLayer(12f, 0, 0, Color.BLACK);
        paint.setStyle(Paint.Style.FILL);
        paint.setTextAlign(Paint.Align.LEFT);

        this.count = 0;
    }

    @Override
    public void draw(@NonNull Canvas canvas) {
        Rect bounds = getBounds();
        if (!text.equals("OK")) {
            canvas.drawText(text, bounds.centerX() - 13f /*just a lazy attempt to centre the text*/ * text.length(), bounds.centerY() - 20, paint);
            canvas.drawText(Integer.toString(count), bounds.centerX() - Integer.toString(count).length(), bounds.centerY() + 60f, paint);
        } else {
            canvas.drawText(text, bounds.centerX() - 13f /*just a lazy attempt to centre the text*/ * text.length(), bounds.centerY(), paint);
        }
    }

    @Override
    public void setAlpha(int alpha) {
        paint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter cf) {
        paint.setColorFilter(cf);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
