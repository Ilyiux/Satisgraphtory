package main;

import java.awt.*;

public class Screen {
    public static PointDouble center;
    public static double zoom = 100;

    public static boolean shoulRenderPoint(PointDouble p) {
        Point s = convertToScreenPoint(p);
        return (s.x > -100 && s.x < GraphicsPanel.WIDTH + 100 && s.y > -100 && s.y < GraphicsPanel.HEIGHT + 100);
    }

    public static Point convertToScreenPoint(PointDouble p) {
        return new Point((int) (zoom * (p.x - center.x) + GraphicsPanel.WIDTH / 2), (int) (zoom * (p.y - center.y) + GraphicsPanel.HEIGHT / 2));
    }

    public static Point convertToWorldPoint(Point p) {
        return new Point((int) Math.floor((p.x - GraphicsPanel.WIDTH / 2) / zoom + center.x), (int) Math.floor((p.y - GraphicsPanel.HEIGHT / 2) / zoom + center.y));
    }

    public static int convertLengthToScreenLength(double l) {
        return (int) (l * zoom);
    }

    public static void zoom(int scrollAmount, int mx, int my) {
        zoom += -5 * scrollAmount;
        if (zoom < 5) zoom = 5;
        if (zoom > 120) zoom = 120;
        Point mp = convertToWorldPoint(new Point(mx, my));
        Point cp = convertToWorldPoint(new Point(GraphicsPanel.WIDTH / 2, GraphicsPanel.HEIGHT / 2));
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
