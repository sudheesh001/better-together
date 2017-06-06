package ac.robinson.bettertogether.plugin.base.cardgame.utils;

/**
 * Created by darkryder on 6/6/17.
 */

public class MathUtils {

    public static class Rectangle {
        int x1, x2, y1, y2;
        public Rectangle(int x1, int y1, int x2, int y2) {
            this.x1 = x1;
            this.x2 = x2;
            this.y1 = y1;
            this.y2 = y2;
        }

        int getX1() {
            return x1;
        }

        int getX2() {
            return x2;
        }

        int getY1() {
            return y1;
        }

        int getY2() {
            return y2;
        }
    }


    public static int rectangleIntersectionArea(Rectangle a, Rectangle b) {
        int x1 = Math.max(a.getX1(), b.getX1());
        int x2 = Math.min(a.getX2(), b.getX2());
        int y1 = Math.max(a.getY1(), b.getY1());
        int y2 = Math.min(a.getY2(), b.getY2());

        if ((x2 <= x1) || (y2 <= y1)) return 0;

        return (x2 - x1) * (y2 - y1);
    }

}
