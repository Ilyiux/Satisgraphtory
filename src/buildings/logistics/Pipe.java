package buildings.logistics;

import buildings.Building;
import main.GraphicsPanel;
import main.ImageManager;
import main.PointDouble;
import main.Screen;

import java.awt.*;
import java.awt.event.KeyEvent;

public class Pipe  extends Connector{
    public Pipe(Building start, Building end, int tier) {
        startBuilding = start;
        endBuilding = end;
        this.tier = tier;
        updateCenter();
    }

    public void update() {
        invalidState = false;
        updateRate();
        updateCenter();
    }

    public void updateCenter() {
        lineCenter = new PointDouble((startBuilding.position.x + 0.5) * (1 - 0.5) + (endBuilding.position.x + 0.5) * 0.5, (startBuilding.position.y + 0.5) * (1 - 0.5) + (endBuilding.position.y + 0.5) * 0.5);
    }

    private void updateRate() {
        double getRate = startBuilding.outPipeRate.get(startBuilding.outPipes.indexOf(this));
        if (getRate == -1) {
            rate = 0;
            invalidState = true;
        } else {
            rate = getRate;
        }

        if (tier == 1) maxRate = 300;
        if (tier == 2) maxRate = 600;

        if (startBuilding.outPipeRate.get(startBuilding.outPipes.indexOf(this)) != -1)
            type = startBuilding.outPipeType.get(startBuilding.outPipes.indexOf(this));

        if (rate > maxRate) invalidState = true;
    }

    public void typed(GraphicsPanel gp, int keyCode) {
        if (keyCode == KeyEvent.VK_X) {
            gp.closeBuildingMenu();
            gp.deleteConnector(this);
        }
        if (keyCode == KeyEvent.VK_1) {
            tier = 1;
        }
        if (keyCode == KeyEvent.VK_2) {
            tier = 2;
        }
    }

    public void clicked(GraphicsPanel gp, int mx, int my) {
        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(lineCenter.x + 0.25, lineCenter.y));
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 75);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 75);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > GraphicsPanel.HEIGHT) {
            menuTopLeft.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
            menuBottomRight.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
        }
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(0.5);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(0.5);
        }

        if (mx < menuTopLeft.x || mx > menuBottomRight.x || my < menuTopLeft.y || my > menuBottomRight.y)
            gp.closeBuildingMenu();

        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 190 && my > menuTopLeft.y + 110 && my < menuTopLeft.y + 140) {
            gp.closeBuildingMenu();
            gp.deleteConnector(this);
        }

        // tier
        if (mx > menuTopLeft.x + 96 && mx < menuTopLeft.x + 111 && my > menuTopLeft.y + 58 && my < menuTopLeft.y + 73)
            if (tier != 1) tier --;
        if (mx > menuTopLeft.x + 176 && mx < menuTopLeft.x + 191 && my > menuTopLeft.y + 58 && my < menuTopLeft.y + 73)
            if (tier != 2) tier ++;
    }

    public void draw(boolean greyedOut, Graphics2D g2d) {
        Point startCenter = Screen.convertToScreenPoint(new PointDouble(startBuilding.position.x + 0.5, startBuilding.position.y + 0.5));
        Point endCenter = Screen.convertToScreenPoint(new PointDouble(endBuilding.position.x + 0.5, endBuilding.position.y + 0.5));

        Point center = Screen.convertToScreenPoint(lineCenter);
        int boxSize = (int)(0.25 * Screen.getZoom());
        g2d.setColor(new Color(40, 43, 40));
        g2d.drawRoundRect(center.x - boxSize, center.y - boxSize, boxSize * 2, boxSize * 2, 6, 6);

        int distance = (int)Math.sqrt((endCenter.x - startCenter.x)*(endCenter.x - startCenter.x) + (endCenter.y - startCenter.y)*(endCenter.y - startCenter.y));
        if (distance == 0) return;
        int missingDistance = Screen.convertLengthToScreenLength(0.45);

        double startP = (double)missingDistance / (double)distance;
        double endP = 1 - startP;

        int sx = (int)(startCenter.x * (1 - startP) + endCenter.x * startP);
        int sy = (int)(startCenter.y * (1 - startP) + endCenter.y * startP);
        int ex = (int)(startCenter.x * (1 - endP) + endCenter.x * endP);
        int ey = (int)(startCenter.y * (1 - endP) + endCenter.y * endP);

        if (invalidState) {
            g2d.setStroke(new BasicStroke(3));
            g2d.setColor(Color.RED);
            g2d.drawLine(sx, sy, ex, ey);
        }

        g2d.setStroke(new BasicStroke(1));
        g2d.setColor(Color.ORANGE);
        g2d.drawLine(sx, sy, ex, ey);

        double angle = Math.atan2(ey - sy, ex - sx);

        double arrowSize = 0.075 * Screen.getZoom();

        if (invalidState) g2d.setColor(Color.RED);
        double cosplus = arrowSize * Math.cos(angle + Math.PI / 2);
        double cosminus = arrowSize * Math.cos(angle - Math.PI / 2);
        double sinplus = arrowSize * Math.sin(angle + Math.PI / 2);
        double sinminus = arrowSize * Math.sin(angle - Math.PI / 2);
        // first arrow
        double sbaseLerp = 0.25 - (arrowSize / distance);
        Point spb = new Point((int)(sx * (1 - sbaseLerp) + ex * sbaseLerp), (int)(sy * (1 - sbaseLerp) + ey * sbaseLerp));
        Point spb1 = new Point((int) (spb.x + cosplus), (int) (spb.y + sinplus));
        Point spb2 = new Point((int) (spb.x + cosminus), (int) (spb.y + sinminus));
        double sheadLerp = 0.25;
        Point sph = new Point((int)(sx * (1 - sheadLerp) + ex * sheadLerp), (int)(sy * (1 - sheadLerp) + ey * sheadLerp));
        g2d.drawLine(spb1.x, spb1.y, sph.x, sph.y);
        g2d.drawLine(spb2.x, spb2.y, sph.x, sph.y);
        // second arrow
        double ebaseLerp = 0.75 - (arrowSize / distance);
        Point epb = new Point((int)(sx * (1 - ebaseLerp) + ex * ebaseLerp), (int)(sy * (1 - ebaseLerp) + ey * ebaseLerp));
        Point epb1 = new Point((int) (epb.x + cosplus), (int) (epb.y + sinplus));
        Point epb2 = new Point((int) (epb.x + cosminus), (int) (epb.y + sinminus));
        double eheadLerp = 0.75;
        Point eph = new Point((int)(sx * (1 - eheadLerp) + ex * eheadLerp), (int)(sy * (1 - eheadLerp) + ey * eheadLerp));
        g2d.drawLine(epb1.x, epb1.y, eph.x, eph.y);
        g2d.drawLine(epb2.x, epb2.y, eph.x, eph.y);

        g2d.setColor(Color.WHITE);
        int textZoomSize = (int)(0.16 * Screen.getZoom());
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, textZoomSize));
        String tierText = "";
        if (tier == 1) tierText = "I";
        if (tier == 2) tierText = "II";
        String rateText = rate + "/" + maxRate;
        int tierTextWidth = g2d.getFontMetrics().stringWidth(tierText);
        int rateTextWidth = g2d.getFontMetrics().stringWidth(rateText);
        Point textCenter = new Point((int)(sx * (1 - 0.5) + ex * 0.5), (int)(sy * (1 - 0.5) + ey * 0.5));
        g2d.drawString(tierText, textCenter.x - tierTextWidth / 2, textCenter.y - (int)(0.06 * Screen.getZoom()));
        if (rate == 0) {
            g2d.drawString(rateText, textCenter.x - rateTextWidth / 2, textCenter.y + (int)(0.14 * Screen.getZoom()));
        } else {
            g2d.drawString(rateText, textCenter.x - rateTextWidth / 2 + textZoomSize / 2, textCenter.y + (int)(0.14 * Screen.getZoom()));
            g2d.drawImage(ImageManager.getMaterialImage(type), textCenter.x - rateTextWidth / 2 - (int)(textZoomSize * 0.1) - textZoomSize / 2, textCenter.y + (int)(0.14 * Screen.getZoom()) - (int)(textZoomSize * 0.9), textZoomSize, textZoomSize, null);
        }
    }

    public void drawMenu(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 63));
        g2d.fillRect(0, 0, GraphicsPanel.WIDTH, GraphicsPanel.HEIGHT);

        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(lineCenter.x + 0.25, lineCenter.y));
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 75);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 75);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > GraphicsPanel.HEIGHT) {
            menuTopLeft.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
            menuBottomRight.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
        }
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(0.5);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(0.5);
        }

        g2d.setColor(new Color(30, 32, 30));
        g2d.fillRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);
        g2d.setColor(new Color(90, 95, 90));
        g2d.drawRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        g2d.drawString("Conveyor", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Tier", menuTopLeft.x + 10, menuTopLeft.y + 70);

        g2d.setColor(new Color(232, 79, 79));
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 110, 180, 30, 5, 5);
        g2d.setColor(new Color(30, 32, 30));
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Delete", menuTopLeft.x + 78, menuTopLeft.y + 130);

        Color enabledColor = new Color(203, 208, 203);
        Color disabledColor = new Color(100, 104, 100);
        Color displayBackgroundColor = new Color(20, 22, 20);

        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 130, menuTopLeft.y + 55, 20, 20, 8, 8);
        g2d.setColor(tier == 1 ? disabledColor : enabledColor);
        g2d.drawString("-", menuTopLeft.x + 100, menuTopLeft.y + 70);
        g2d.setColor(tier == 2 ? disabledColor : enabledColor);
        g2d.drawString("+", menuTopLeft.x + 180, menuTopLeft.y + 70);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(tier), menuTopLeft.x + 137, menuTopLeft.y + 70);
    }
}
