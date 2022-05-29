package main;

import java.awt.*;

public class Screen {
    public static PointDouble center;
    public static double zoom = 100;

    public static boolean shouldRenderPoint(PointDouble p, GraphicsPanel gp) {
        Point s = convertToScreenPoint(p, gp);
        return (s.x > -100 && s.x < gp.getWidth() + 100 && s.y > -100 && s.y < gp.getHeight() + 100);
    }

    public static Point convertToScreenPoint(PointDouble p, GraphicsPanel gp) {
        return new Point((int) (zoom * (p.x - center.x) + gp.getWidth() / 2), (int) (zoom * (p.y - center.y) + gp.getHeight() / 2));
    }

    public static Point convertToWorldPoint(Point p, GraphicsPanel gp) {
        return new Point((int) Math.floor((p.x - gp.getWidth() / 2) / zoom + center.x), (int) Math.floor((p.y - gp.getHeight() / 2) / zoom + center.y));
    }

    public static int convertLengthToScreenLength(double l) {
        return (int) (l * zoom);
    }

    public static void zoom(int scrollAmount, int mx, int my, GraphicsPanel gp) {
        zoom += -5 * scrollAmount;
        if (zoom < 5) zoom = 5;
        if (zoom > 120) zoom = 120;
        Point mp = convertToWorldPoint(new Point(mx, my), gp);
        Point cp = convertToWorldPoint(new Point(gp.getWidth() / 2, gp.getHeight() / 2), gp);
        if (scrollAmount < 0 && zoom < 120) {
            center.x -= (mp.x - cp.x) * (1 - 120.0 / (double)zoom) * 0.1;
            center.y -= (mp.y - cp.y) * (1 - 120.0 / (double)zoom) * 0.1;
        }
    }

    public static void setZoom(int amount) {
        zoom = amount;
    }
    public static double getZoom() { return zoom; }

    public static void setCenter(PointDouble point) {
        center = point;
    }

    public static PointDouble getCenter() {
        return center;
    }

    public static void translateCenter(int amountX, int amountY) {
        center.x -= amountX / zoom;
        center.y -= amountY / zoom;
    }
}
