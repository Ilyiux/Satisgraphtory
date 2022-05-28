package buildings.logistics;

import buildings.Building;
import main.Main;
import recipes.Material;
import main.GraphicsPanel;
import main.PointDouble;
import main.Screen;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;

public class Junction extends Building {

    private BufferedImage image;

    private boolean hasValidInput = false;

    public Junction(Point position) {
        this.position = position;
        maxInConveyors = 0;
        maxOutConveyors = 0;
        maxInPipes = 3;
        maxOutPipes = 3;
        getImages();
    }

    private void getImages() {
        image = Main.getImageFromResources("/images/buildings/pipe_junction.png");
    }

    public void update() {
        updateOutItems();
    }

    private void updateOutItems() {
        outPipeRate.clear();
        outPipeType.clear();

        double itemAmount = 0;
        Material itemType = null;
        boolean isValid = true;
        for (Pipe p : inPipes) {
            if (!p.invalidState) {
                if (itemType == null) {
                    itemType = p.type;
                } else if (itemType != p.type) {
                    isValid = false;
                }
                itemAmount += p.rate;
            }
        }
        hasValidInput = false;
        if (isValid) {
            hasValidInput = true;
            for (int i = 0; i < outPipes.size(); i++) {
                outPipeRate.add(itemAmount / outPipes.size());
                outPipeType.add(itemType);
            }
        }

        for (int c = outConveyorRate.size(); c < maxOutConveyors; c++)
            outConveyorRate.add(-1.0);
        for (int p = outPipeRate.size(); p < maxOutPipes; p++)
            outPipeRate.add(-1.0);
    }

    public void clicked(GraphicsPanel gp, int mx, int my) {
        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5));
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
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 6) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 6) {
            menuTopLeft.x -= menuBottomRight.x - (GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 6);
            menuBottomRight.x -= menuBottomRight.x - (GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 6);
        }

        if (mx < menuTopLeft.x || mx > menuBottomRight.x || my < menuTopLeft.y || my > menuBottomRight.y)
            gp.closeBuildingMenu();

        if (mx > menuTopLeft.x + 105 && mx < menuTopLeft.x + 190 && my > menuTopLeft.y + 110 && my < menuTopLeft.y + 140) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 95 && my > menuTopLeft.y + 110 && my < menuTopLeft.y + 140) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Junction(position));
        }
    }

    public void typed(GraphicsPanel gp, int keyCode) {
        if (keyCode == KeyEvent.VK_X) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (keyCode == KeyEvent.VK_D) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Junction(position));
        }
    }

    public void draw(boolean greyedOut, Graphics2D g2d) {
        Point start = Screen.convertToScreenPoint(new PointDouble(position.x, position.y));
        Point end = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 1));

        g2d.drawImage(image, start.x, start.y, end.x - start.x, end.y - start.y, null);

        if (hasValidInput)
            g2d.setColor(Color.GREEN);
        else
            g2d.setColor(Color.RED);
        if (greyedOut) g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(start.x, start.y, end.x - start.x, end.y - start.y, (int) (Screen.getZoom() / 10), (int) (Screen.getZoom() / 10));

        double size = Screen.convertLengthToScreenLength(0.08);
        for (int ic = 0; ic < maxInConveyors; ic++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x, position.y + ((double)(ic + 1) / (maxInConveyors + maxInPipes + 1))));
            g2d.setColor(Color.GRAY);
            g2d.fillRect((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int ip = 0; ip < maxInPipes; ip++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x, position.y + ((double)(ip + maxInConveyors + 1) / (maxInConveyors + maxInPipes + 1))));
            g2d.setColor(Color.ORANGE);
            g2d.fillOval((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int oc = 0; oc < maxOutConveyors; oc++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + ((double)(oc + 1) / (maxOutConveyors + maxOutPipes + 1))));
            g2d.setColor(Color.GRAY);
            g2d.fillRect((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int op = 0; op < maxOutPipes; op++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + ((double)(op + maxOutConveyors + 1) / (maxOutConveyors + maxOutPipes + 1))));
            g2d.setColor(Color.ORANGE);
            g2d.fillOval((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.15)));

        Point namePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.15));
        String nameString = "Pipe Junc.";
        g2d.drawString(nameString, namePos.x, namePos.y);
    }

    public void drawMenu(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 63));
        g2d.fillRect(0, 0, GraphicsPanel.WIDTH, GraphicsPanel.HEIGHT);

        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5));
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
        g2d.drawString("Pipeline Junction", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(new Color(232, 79, 79));
        g2d.fillRoundRect(menuTopLeft.x + 105, menuTopLeft.y + 110, 85, 30, 5, 5);
        g2d.setColor(new Color(79, 79, 232));
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 110, 85, 30, 5, 5);
        g2d.setColor(new Color(30, 32, 30));
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Delete", menuTopLeft.x + 123, menuTopLeft.y + 130);
        g2d.drawString("Duplicate", menuTopLeft.x + 18, menuTopLeft.y + 130);
    }
}
