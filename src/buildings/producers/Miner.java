package buildings.producers;

import buildings.Building;
import main.Main;
import recipes.Material;
import main.GraphicsPanel;
import main.PointDouble;
import main.Screen;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Miner extends Building {

    public Purity purity = Purity.IMPURE;
    public NodePossibilities nodeType = NodePossibilities.UNSET;

    public int tier;

    public double baseItemRate;
    public double itemRate;

    public double overclock = 100;
    private String overclockString = "";
    private boolean editingOverclock = false;
    private String itemString = "";
    private boolean editingItems = false;

    private double basePower = 5;
    public double powerConsumption;
    public int powerSlugs = 0;

    private BufferedImage image_mk1;
    private BufferedImage image_mk2;
    private BufferedImage image_mk3;
    private final Map<NodePossibilities, BufferedImage> itemIcons = new HashMap<>();

    public Miner(Point position) {
        this.position = position;
        tier = 1;
        maxInConveyors = 0;
        maxOutConveyors = 1;
        maxInPipes = 0;
        maxOutPipes = 0;
        getImages();
        updateItemRate();
        updatePowerConsumption();
        updateShownRates();
    }

    public Miner(Point position, int tier, Purity purity, double overclock, NodePossibilities nodeType) {
        this.position = position;
        this.tier = tier;
        this.purity = purity;
        this.overclock = overclock;
        this.nodeType = nodeType;
        maxInConveyors = 0;
        maxOutConveyors = 1;
        maxInPipes = 0;
        maxOutPipes = 0;
        getImages();
        updateItemRate();
        updatePowerConsumption();
        updateShownRates();
    }

    private void updateShownRates() {
        itemString = String.valueOf(baseItemRate * overclock / 100);
        overclockString = String.valueOf(overclock);
    }

    private void getImages() {
        image_mk1 = Main.getImageFromResources("/images/buildings/miner_mk1.png");
        image_mk2 = Main.getImageFromResources("/images/buildings/miner_mk2.png");
        image_mk3 = Main.getImageFromResources("/images/buildings/miner_mk3.png");

        for (NodePossibilities n : NodePossibilities.values()) {
            itemIcons.put(n, Main.getImageFromResources("/images/items/" + String.valueOf(n).toLowerCase() + ".png"));
        }
    }

    private void updateItemRate() {
        int purityMultiplier = 0;
        int tierMultiplier = 0;

        if (tier == 3) tierMultiplier = 4;
        if (tier == 2) tierMultiplier = 2;
        if (tier == 1) tierMultiplier = 1;
        if (purity == Purity.PURE) purityMultiplier = 120;
        if (purity == Purity.NORMAL) purityMultiplier = 60;
        if (purity == Purity.IMPURE) purityMultiplier = 30;

        baseItemRate = purityMultiplier * tierMultiplier;
        itemRate = baseItemRate * (overclock / 100);
        // round to 5 decimal places
        itemRate = (double)Math.round(itemRate * 10000) / 10000;
    }

    public void updatePowerConsumption() {
        if (tier == 1) basePower = 5;
        if (tier == 2) basePower = 12;
        if (tier == 3) basePower = 30;

        powerConsumption = basePower * Math.pow((overclock / 100), 1.6);
        // round to 5 decimal places
        powerConsumption = (double)Math.round(powerConsumption * 10000) / 10000;

        if (overclock <= 100) powerSlugs = 0;
        if (overclock > 100 && overclock <= 150) powerSlugs = 1;
        if (overclock > 150 && overclock <= 200) powerSlugs = 2;
        if (overclock > 200 && overclock <= 250) powerSlugs = 3;

        power = -powerConsumption;
    }

    public void update() {
        updateItemRate();
        updatePowerConsumption();
        if (!editingItems && !editingOverclock) updateShownRates();

        outConveyorRate.clear();
        outConveyorType.clear();
        if (nodeType == NodePossibilities.UNSET) {
            outConveyorRate.add((double)-1);
        } else {
            outConveyorRate.add(itemRate);
            if (nodeType == NodePossibilities.IRON_ORE) outConveyorType.add(Material.IRON_ORE);
            if (nodeType == NodePossibilities.COPPER_ORE) outConveyorType.add(Material.COPPER_ORE);
            if (nodeType == NodePossibilities.COAL) outConveyorType.add(Material.COAL);
            if (nodeType == NodePossibilities.LIMESTONE) outConveyorType.add(Material.LIMESTONE);
            if (nodeType == NodePossibilities.CATERIUM_ORE) outConveyorType.add(Material.CATERIUM_ORE);
            if (nodeType == NodePossibilities.SULFUR) outConveyorType.add(Material.SULFUR);
            if (nodeType == NodePossibilities.RAW_QUARTZ) outConveyorType.add(Material.RAW_QUARTZ);
            if (nodeType == NodePossibilities.BAUXITE) outConveyorType.add(Material.BAUXITE);
            if (nodeType == NodePossibilities.URANIUM) outConveyorType.add(Material.URANIUM);
            if (nodeType == NodePossibilities.SAM_ORE) outConveyorType.add(Material.SAM_ORE);
        }
    }

    private void exitClockInput() {
        if (editingItems) {
            editingItems = false;

            double items = Double.parseDouble(itemString);
            double minItems = baseItemRate * 0.01;
            double maxItems = baseItemRate * 2.5;
            if (items < minItems) items = minItems;
            if (items > maxItems) items = maxItems;
            overclock = items / baseItemRate * 100;

            overclock = (double)Math.round(overclock * 100) / 100;
            items = (double)Math.round(items * 100) / 100;

            itemString = "" + items;
            overclockString = "" + overclock;
        }
        if (editingOverclock) {
            editingOverclock = false;

            overclock = Double.parseDouble(overclockString);
            if (overclock > 250) overclock = 250;
            if (overclock < 1) overclock = 1;

            double items = (baseItemRate * overclock / 100);

            items = (double)Math.round(items * 100) / 100;
            overclock = (double)Math.round(overclock * 100) / 100;

            itemString = "" + items;
            overclockString = "" + overclock;
        }
    }

    public void clicked(GraphicsPanel gp, int mx, int my) {
        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5));
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 150);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 150);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > GraphicsPanel.HEIGHT) {
            menuTopLeft.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
            menuBottomRight.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
        }
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8) {
            menuTopLeft.x -= menuBottomRight.x - (GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8);
            menuBottomRight.x -= menuBottomRight.x - (GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8);
        }

        if (editingOverclock || editingItems) {
            exitClockInput();
        } else {
            if ((mx < menuTopLeft.x || mx > menuBottomRight.x || my < menuTopLeft.y || my > menuBottomRight.y) && mx < GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8)
                gp.closeBuildingMenu();
        }

        if (mx > menuTopLeft.x + 105 && mx < menuTopLeft.x + 190 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 95 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Miner(position, tier, purity, overclock, nodeType));
        }

        // tier
        if (mx > menuTopLeft.x + 96 && mx < menuTopLeft.x + 111 && my > menuTopLeft.y + 58 && my < menuTopLeft.y + 73) {
            if (tier != 1) tier --;
        }
        if (mx > menuTopLeft.x + 176 && mx < menuTopLeft.x + 191 && my > menuTopLeft.y + 58 && my < menuTopLeft.y + 73) {
            if (tier != 3) tier ++;
        }

        // resource
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 10 && my < 50) nodeType = NodePossibilities.UNSET;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 60 && my < 100) nodeType = NodePossibilities.IRON_ORE;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 110 && my < 150) nodeType = NodePossibilities.COPPER_ORE;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 160 && my < 200) nodeType = NodePossibilities.COAL;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 210 && my < 250) nodeType = NodePossibilities.LIMESTONE;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 260 && my < 300) nodeType = NodePossibilities.SULFUR;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 310 && my < 350) nodeType = NodePossibilities.CATERIUM_ORE;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 360 && my < 400) nodeType = NodePossibilities.RAW_QUARTZ;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 410 && my < 450) nodeType = NodePossibilities.BAUXITE;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 460 && my < 500) nodeType = NodePossibilities.URANIUM;
        if (mx > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10 && mx < GraphicsPanel.WIDTH - 10 && my > 510 && my < 550) nodeType = NodePossibilities.SAM_ORE;

        // purity
        if (mx > menuTopLeft.x + 81 && mx < menuTopLeft.x + 96 && my > menuTopLeft.y + 118 && my < menuTopLeft.y + 133) {
            if (purity != Purity.IMPURE) {
                if (purity == Purity.NORMAL) purity = Purity.IMPURE;
                if (purity == Purity.PURE) purity = Purity.NORMAL;
            }
        }
        if (mx > menuTopLeft.x + 176 && mx < menuTopLeft.x + 191 && my > menuTopLeft.y + 118 && my < menuTopLeft.y + 133) {
            if (purity != Purity.PURE) {
                if (purity == Purity.NORMAL) purity = Purity.PURE;
                if (purity == Purity.IMPURE) purity = Purity.NORMAL;
            }
        }

        // overclock
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 60 && my > menuTopLeft.y + 165 && my < menuTopLeft.y + 185) {
            editingOverclock = true;
            editingItems = false;
        }
        if (mx > menuTopLeft.x + 85 && mx < menuTopLeft.x + 155 && my > menuTopLeft.y + 165 && my < menuTopLeft.y + 185) {
            editingItems = true;
            editingOverclock = false;
        }
    }

    public void typed(GraphicsPanel gp, int keyCode) {
        if (keyCode == KeyEvent.VK_X) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (keyCode == KeyEvent.VK_D) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Miner(position, tier, purity, overclock, nodeType));
        }
        if (editingOverclock) {
            if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                if (overclockString.length() > 0)
                    overclockString = overclockString.substring(0, overclockString.length() - 1);
            } else if (keyCode == KeyEvent.VK_PERIOD) {
                overclockString = overclockString + ".";
            } else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9){
                overclockString = overclockString + (keyCode - 48);
            }
            if (keyCode == KeyEvent.VK_ENTER) exitClockInput();
        }
        if (editingItems) {
            if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                if (itemString.length() > 0)
                    itemString = itemString.substring(0, itemString.length() - 1);
            } else if (keyCode == KeyEvent.VK_PERIOD) {
                overclockString = overclockString + ".";
            } else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9){
                itemString = itemString + (keyCode - 48);
            }
            if (keyCode == KeyEvent.VK_ENTER) exitClockInput();
        }
    }

    public void draw(boolean greyedOut, Graphics2D g2d) {
        Point start = Screen.convertToScreenPoint(new PointDouble(position.x, position.y));
        Point end = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 1));

        BufferedImage image = null;
        if (tier == 1) image = image_mk1;
        if (tier == 2) image = image_mk2;
        if (tier == 3) image = image_mk3;
        g2d.drawImage(image, start.x, start.y, end.x - start.x, end.y - start.y, null);

        if (nodeType != NodePossibilities.UNSET) {
            g2d.setColor(Color.GREEN);
        } else {
            g2d.setColor(Color.RED);
        }
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
        String nameString = "Miner Mk." + tier;
        g2d.drawString(nameString, namePos.x, namePos.y);

        Point ratePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.95));
        g2d.drawString(itemRate + "/min", ratePos.x, ratePos.y);

        Point imageStart = Screen.convertToScreenPoint(new PointDouble(position.x + 0.72, position.y + 0.72));
        Point imageEnd = Screen.convertToScreenPoint(new PointDouble(position.x + 0.97, position.y + 0.97));
        g2d.drawImage(itemIcons.get(nodeType), imageStart.x, imageStart.y, imageEnd.x - imageStart.x, imageEnd.y - imageStart.y, null);
    }

    public void drawMenu(Graphics2D g2d) {
        g2d.setColor(new Color(0, 0, 0, 63));
        g2d.fillRect(0, 0, GraphicsPanel.WIDTH, GraphicsPanel.HEIGHT);

        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5));
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 150);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 150);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > GraphicsPanel.HEIGHT) {
            menuTopLeft.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
            menuBottomRight.y -= menuBottomRight.y - GraphicsPanel.HEIGHT;
        }
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8) {
            menuTopLeft.x -= menuBottomRight.x - (GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8);
            menuBottomRight.x -= menuBottomRight.x - (GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8);
        }

        g2d.setColor(new Color(30, 32, 30));
        g2d.fillRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);
        g2d.setColor(new Color(90, 95, 90));
        g2d.drawRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        g2d.drawString("Miner", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Tier", menuTopLeft.x + 10, menuTopLeft.y + 70);
        g2d.drawString("Resource", menuTopLeft.x + 10, menuTopLeft.y + 100);
        g2d.drawString("Purity", menuTopLeft.x + 10, menuTopLeft.y + 130);
        g2d.drawString("Overclock", menuTopLeft.x + 10, menuTopLeft.y + 160);

        g2d.setColor(new Color(232, 79, 79));
        g2d.fillRoundRect(menuTopLeft.x + 105, menuTopLeft.y + 260, 85, 30, 5, 5);
        g2d.setColor(new Color(79, 79, 232));
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 260, 85, 30, 5, 5);
        g2d.setColor(new Color(30, 32, 30));
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Delete", menuTopLeft.x + 123, menuTopLeft.y + 280);
        g2d.drawString("Duplicate", menuTopLeft.x + 18, menuTopLeft.y + 280);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 12));
        g2d.setColor(new Color(90, 95, 90));
        g2d.drawString("Power Slugs: " + powerSlugs, menuTopLeft.x + 10, menuTopLeft.y + 230);
        g2d.drawString("Power Consumption: " + powerConsumption + "MW", menuTopLeft.x + 10, menuTopLeft.y + 245);

        Color enabledColor = new Color(203, 208, 203);
        Color disabledColor = new Color(100, 104, 100);
        Color displayBackgroundColor = new Color(20, 22, 20);

        //tier
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 130, menuTopLeft.y + 55, 20, 20, 8, 8);
        g2d.setColor(tier == 1 ? disabledColor : enabledColor);
        g2d.drawString("-", menuTopLeft.x + 100, menuTopLeft.y + 70);
        g2d.setColor(tier == 3 ? disabledColor : enabledColor);
        g2d.drawString("+", menuTopLeft.x + 180, menuTopLeft.y + 70);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(tier), menuTopLeft.x + 137, menuTopLeft.y + 70);

        //resource
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 85, 110, 20, 8, 8);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(nodeType).replace("_", " "), menuTopLeft.x + 107, menuTopLeft.y + 100);
        g2d.drawImage(itemIcons.get(nodeType), menuTopLeft.x + 87, menuTopLeft.y + 87, 16, 16, null);

        g2d.setColor(new Color(34, 36, 34));
        g2d.fillRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8, 0, GraphicsPanel.WIDTH / 8, GraphicsPanel.HEIGHT);
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 10, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 60, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 110, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 160, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 210, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 260, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 310, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 360, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 410, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 460, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.fillRoundRect(GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 10, 510, GraphicsPanel.WIDTH / 8 - 20, 40, 10, 10);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        g2d.setColor(Color.WHITE);
        g2d.drawString("UNSET", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 38);
        g2d.drawImage(itemIcons.get(NodePossibilities.UNSET), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 20, 20, 20, null);
        g2d.drawString("IRON", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 88);
        g2d.drawImage(itemIcons.get(NodePossibilities.IRON_ORE), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 70, 20, 20, null);
        g2d.drawString("COPPER", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 138);
        g2d.drawImage(itemIcons.get(NodePossibilities.COPPER_ORE), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 120, 20, 20, null);
        g2d.drawString("COAL", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 188);
        g2d.drawImage(itemIcons.get(NodePossibilities.COAL), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 170, 20, 20, null);
        g2d.drawString("LIMESTONE", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 238);
        g2d.drawImage(itemIcons.get(NodePossibilities.LIMESTONE), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 220, 20, 20, null);
        g2d.drawString("SULFUR", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 288);
        g2d.drawImage(itemIcons.get(NodePossibilities.SULFUR), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 270, 20, 20, null);
        g2d.drawString("CATERIUM", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 338);
        g2d.drawImage(itemIcons.get(NodePossibilities.CATERIUM_ORE), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 320, 20, 20, null);
        g2d.drawString("QUARTZ", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 388);
        g2d.drawImage(itemIcons.get(NodePossibilities.RAW_QUARTZ), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 370, 20, 20, null);
        g2d.drawString("BAUXITE", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 438);
        g2d.drawImage(itemIcons.get(NodePossibilities.BAUXITE), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 420, 20, 20, null);
        g2d.drawString("URANIUM", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 488);
        g2d.drawImage(itemIcons.get(NodePossibilities.URANIUM), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 470, 20, 20, null);
        g2d.drawString("SAM", GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 50, 538);
        g2d.drawImage(itemIcons.get(NodePossibilities.SAM_ORE), GraphicsPanel.WIDTH - GraphicsPanel.WIDTH / 8 + 20, 520, 20, 20, null);

        //purity
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 102, menuTopLeft.y + 115, 73, 20, 8, 8);
        g2d.setColor(purity == Purity.IMPURE ? disabledColor : enabledColor);
        g2d.drawString("-", menuTopLeft.x + 85, menuTopLeft.y + 130);
        g2d.setColor(purity == Purity.PURE ? disabledColor : enabledColor);
        g2d.drawString("+", menuTopLeft.x + 180, menuTopLeft.y + 130);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(purity), menuTopLeft.x + 110, menuTopLeft.y + 130);

        // overclock
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 195, menuTopLeft.x + 190, menuTopLeft.y + 195);
        int px = menuTopLeft.x + 10 + (int)((180.0 / 249.0) * overclock);
        int py = menuTopLeft.y + 195;
        g2d.fillOval(px - 3, py - 3, 6, 7);

        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 165, 50, 20, 8, 8);
        if (editingOverclock) {
            g2d.setColor(new Color(100, 104, 100));
            g2d.drawRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 165, 50, 20, 8, 8);
        }
        g2d.setColor(Color.WHITE);
        boolean showOverclockCursor = ((int)(System.currentTimeMillis() / 500) % 2) == 0 && editingOverclock;
        g2d.drawString(overclockString + (showOverclockCursor ? "|" : ""), menuTopLeft.x + 15, menuTopLeft.y + 180);
        g2d.drawString("%", menuTopLeft.x + 65, menuTopLeft.y + 180);

        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 165, 70, 20, 8, 8);
        if (editingItems) {
            g2d.setColor(new Color(100, 104, 100));
            g2d.drawRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 165, 70, 20, 8, 8);
        }
        g2d.setColor(Color.WHITE);
        boolean showItemCursor = ((int)(System.currentTimeMillis() / 500) % 2) == 0 && editingItems;
        g2d.drawString(itemString + (showItemCursor ? "|" : ""), menuTopLeft.x + 90, menuTopLeft.y + 180);
        g2d.drawString("/min", menuTopLeft.x + 160, menuTopLeft.y + 180);
    }

    public enum NodePossibilities {
        UNSET, COPPER_ORE, IRON_ORE, CATERIUM_ORE, BAUXITE, URANIUM, LIMESTONE, SAM_ORE, COAL, SULFUR, RAW_QUARTZ
    }
}