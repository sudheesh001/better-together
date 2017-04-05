package ac.robinson.bettertogether.plugin.base.cardgame.player;

import android.graphics.Canvas;
import android.util.Log;
import android.view.SurfaceHolder;

/**
 * Created by t-sus on 4/5/2017.
 */

public class PlayerThread extends Thread {
    private static final String TAG = PlayerThread.class.getSimpleName();

    private SurfaceHolder surfaceHolder;
    private PlayerPanel gamePanel;
    private boolean running;

    public void setRunning(boolean running) {
        this.running = running;
    }

    public PlayerThread(SurfaceHolder surfaceHolder, PlayerPanel gamePanel) {
        super();
        this.surfaceHolder = surfaceHolder;
        this.gamePanel = gamePanel;
    }

    @Override
    public void run() {
        Canvas canvas;
        Log.d(TAG, "Starting game loop for player");
        while (running) {
            canvas = null;
            // try locking the canvas for exclusive pixel editing
            // in the surface
            try {
                canvas = this.surfaceHolder.lockCanvas();
                synchronized (surfaceHolder) {
                    // render state to the screen
                    // draws the canvas on the panel
                    this.gamePanel.render(canvas);
                }
            } finally {
                // in case of an exception the surface is not left in
                // an inconsistent state
                if (canvas != null) {
                    surfaceHolder.unlockCanvasAndPost(canvas);
                }
            }	// end finally
        }
    }
}
