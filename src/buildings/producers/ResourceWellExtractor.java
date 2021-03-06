package buildings.producers;

import buildings.Building;
import main.*;
import recipes.Material;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class ResourceWellExtractor extends Building {

    public Purity purity = Purity.IMPURE;
    public NodePossibilities nodeType = NodePossibilities.UNSET;

    public double baseItemRate;
    public double itemRate;

    private boolean isValid = false;

    public double overclock = 100;
    private String overclockString = "";
    private boolean editingOverclock = false;
    private String itemString = "";
    private boolean editingItems = false;

    public int powerSlugs = 0;

    private BufferedImage image;
    private final Map<NodePossibilities, BufferedImage> itemIcons = new HashMap<>();

    public ResourceWellExtractor(Point position) {
        this.position = position;
        maxInConveyors = 0;
        maxOutConveyors = 0;
        maxInPipes = 0;
        maxOutPipes = 1;
        getImages();
        updateItemRate();
        updatePowerConsumption();
        updateShownRates();
    }

    public ResourceWellExtractor(Point position, Purity purity, double overclock, NodePossibilities nodeType) {
        this.position = position;
        this.purity = purity;
        this.overclock = overclock;
        this.nodeType = nodeType;
        maxInConveyors = 0;
        maxOutConveyors = 0;
        maxInPipes = 0;
        maxOutPipes = 1;
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
        image = Main.getImageFromResources("/images/buildings/resource_well_extractor.png");

        for (NodePossibilities n : NodePossibilities.values()) {
            itemIcons.put(n, Main.getImageFromResources("/images/items/" + String.valueOf(n).toLowerCase() + ".png"));
        }
    }

    private void updateItemRate() {
        int purityMultiplier = 0;

        if (purity == Purity.PURE) purityMultiplier = 120;
        if (purity == Purity.NORMAL) purityMultiplier = 60;
        if (purity == Purity.IMPURE) purityMultiplier = 30;

        baseItemRate = purityMultiplier;
        itemRate = baseItemRate * (overclock / 100);
        // round to 5 decimal places
        itemRate = (double)Math.round(itemRate * 10000) / 10000;
    }

    private void updateValidity() {
        isValid = nodeType != NodePossibilities.UNSET;
    }

    public void updatePowerConsumption() {
        if (overclock <= 100) powerSlugs = 0;
        if (overclock > 100 && overclock <= 150) powerSlugs = 1;
        if (overclock > 150 && overclock <= 200) powerSlugs = 2;
        if (overclock > 200 && overclock <= 250) powerSlugs = 3;
    }

    public void update() {
        updateItemRate();
        updateValidity();
        updatePowerConsumption();
        if (!editingItems && !editingOverclock) updateShownRates();

        outPipeRate.clear();
        outPipeType.clear();
        if (nodeType == NodePossibilities.UNSET) {
            outPipeRate.add((double)-1);
        } else {
            outPipeRate.add(itemRate);
            if (nodeType == NodePossibilities.WATER) outPipeType.add(Material.WATER);
            if (nodeType == NodePossibilities.CRUDE_OIL) outPipeType.add(Material.CRUDE_OIL);
            if (nodeType == NodePossibilities.NITROGEN_GAS) outPipeType.add(Material.NITROGEN_GAS);
        }
    }

    private void exitClockInput() {
        if (editingItems) {
            editingItems = false;
            
            if (itemString.length() < 1) itemString = "0";

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
            
            if (overclockString.length() < 1) overclockString = "0";

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
        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5), gp);
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 150);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 150);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > gp.getHeight()) {
            menuTopLeft.y -= menuBottomRight.y - gp.getHeight();
            menuBottomRight.y -= menuBottomRight.y - gp.getHeight();
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8) {
            menuTopLeft.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8);
            menuBottomRight.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8);
        }

        if (editingOverclock || editingItems) {
            exitClockInput();
        } else {
            if ((mx < menuTopLeft.x || mx > menuBottomRight.x || my < menuTopLeft.y || my > menuBottomRight.y) && mx < gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8)
                gp.closeBuildingMenu();
        }

        if (mx > menuTopLeft.x + 105 && mx < menuTopLeft.x + 190 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 95 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.addBuilding(new ResourceWellExtractor(position, purity, overclock, nodeType));
        }

        // resource
        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.12);
        int spacing = buttonHeight / 4;
        int buttonWidth = (int) ((gp.getHeight() * 1.77777777) / 6 - spacing * 2);

        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing && my > spacing && my < (spacing + buttonHeight)) nodeType = NodePossibilities.UNSET;
        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing && my > spacing * 2 + buttonHeight && my < (spacing + buttonHeight) * 2) nodeType = NodePossibilities.CRUDE_OIL;
        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing && my > spacing * 3 + buttonHeight * 2 && my < (spacing + buttonHeight) * 3) nodeType = NodePossibilities.WATER;
        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing && my > spacing * 4 + buttonHeight * 3 && my < (spacing + buttonHeight) * 4) nodeType = NodePossibilities.NITROGEN_GAS;

        // purity
        if (mx > menuTopLeft.x + 81 && mx < menuTopLeft.x + 96 && my > menuTopLeft.y + 88 && my < menuTopLeft.y + 103) {
            if (purity != Purity.IMPURE) {
                if (purity == Purity.NORMAL) purity = Purity.IMPURE;
                if (purity == Purity.PURE) purity = Purity.NORMAL;
            }
        }
        if (mx > menuTopLeft.x + 176 && mx < menuTopLeft.x + 191 && my > menuTopLeft.y + 88 && my < menuTopLeft.y + 103) {
            if (purity != Purity.PURE) {
                if (purity == Purity.NORMAL) purity = Purity.PURE;
                if (purity == Purity.IMPURE) purity = Purity.NORMAL;
            }
        }

        // overclock
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 60 && my > menuTopLeft.y + 135 && my < menuTopLeft.y + 155) {
            editingOverclock = true;
            editingItems = false;
        }
        if (mx > menuTopLeft.x + 85 && mx < menuTopLeft.x + 155 && my > menuTopLeft.y + 135 && my < menuTopLeft.y + 155) {
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
            gp.addBuilding(new ResourceWellExtractor(position, purity, overclock, nodeType));
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

    public void draw(boolean greyedOut, Graphics2D g2d, GraphicsPanel gp) {
        Point start = Screen.convertToScreenPoint(new PointDouble(position.x, position.y), gp);
        Point end = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 1), gp);

        g2d.drawImage(image, start.x, start.y, end.x - start.x, end.y - start.y, null);

        if (!isValid) {
            g2d.setColor(ColorManager.getColor("invalid"));
        } else {
            g2d.setColor(ColorManager.getColor("valid"));
        }
        if (greyedOut) g2d.setColor(Color.GRAY);
        g2d.drawRoundRect(start.x, start.y, end.x - start.x, end.y - start.y, (int) (Screen.getZoom() / 10), (int) (Screen.getZoom() / 10));

        double size = Screen.convertLengthToScreenLength(0.08);
        for (int ic = 0; ic < maxInConveyors; ic++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x, position.y + ((double)(ic + 1) / (maxInConveyors + maxInPipes + 1))), gp);
            g2d.setColor(Color.GRAY);
            g2d.fillRect((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int ip = 0; ip < maxInPipes; ip++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x, position.y + ((double)(ip + maxInConveyors + 1) / (maxInConveyors + maxInPipes + 1))), gp);
            g2d.setColor(Color.ORANGE);
            g2d.fillOval((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int oc = 0; oc < maxOutConveyors; oc++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + ((double)(oc + 1) / (maxOutConveyors + maxOutPipes + 1))), gp);
            g2d.setColor(Color.GRAY);
            g2d.fillRect((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }
        for (int op = 0; op < maxOutPipes; op++) {
            Point pos = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + ((double)(op + maxOutConveyors + 1) / (maxOutConveyors + maxOutPipes + 1))), gp);
            g2d.setColor(Color.ORANGE);
            g2d.fillOval((int) (pos.x - size / 2), (int) (pos.y - size / 2), (int) size, (int) size);
        }

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.15)));
        Point namePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.15), gp);
        String nameString = "Well Extr.";
        g2d.drawString(nameString, namePos.x, namePos.y);

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.08)));
        Point ocPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.25), gp);
        g2d.drawString(overclock + "%", ocPoint.x, ocPoint.y);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.15)));
        Point ratePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.95), gp);
        g2d.drawString(itemRate + "/min", ratePos.x, ratePos.y);

        Point imageStart = Screen.convertToScreenPoint(new PointDouble(position.x + 0.72, position.y + 0.72), gp);
        Point imageEnd = Screen.convertToScreenPoint(new PointDouble(position.x + 0.97, position.y + 0.97), gp);
        g2d.drawImage(itemIcons.get(nodeType), imageStart.x, imageStart.y, imageEnd.x - imageStart.x, imageEnd.y - imageStart.y, null);
    }

    public void drawMenu(Graphics2D g2d, GraphicsPanel gp) {
        g2d.setColor(new Color(0, 0, 0, 63));
        g2d.fillRect(0, 0, gp.getWidth(), gp.getHeight());

        Point leftMidPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 1, position.y + 0.5), gp);
        Point menuTopLeft = new Point(leftMidPoint.x, leftMidPoint.y - 150);
        Point menuBottomRight = new Point(leftMidPoint.x + 200, leftMidPoint.y + 150);
        if (menuTopLeft.y < 0) {
            menuBottomRight.y -= menuTopLeft.y;
            menuTopLeft.y -= menuTopLeft.y;
        }
        if (menuBottomRight.y > gp.getHeight()) {
            menuTopLeft.y -= menuBottomRight.y - gp.getHeight();
            menuBottomRight.y -= menuBottomRight.y - gp.getHeight();
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8) {
            menuTopLeft.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8);
            menuBottomRight.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8);
        }

        g2d.setColor(new Color(30, 32, 30));
        g2d.fillRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);
        g2d.setColor(new Color(90, 95, 90));
        g2d.drawRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        g2d.drawString("Well Extractor", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Resource", menuTopLeft.x + 10, menuTopLeft.y + 70);
        g2d.drawString("Purity", menuTopLeft.x + 10, menuTopLeft.y + 100);
        g2d.drawString("Pressurizer Overclock", menuTopLeft.x + 10, menuTopLeft.y + 130);

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
        g2d.drawString("Initial 150MW cost for pressurizer", menuTopLeft.x + 10, menuTopLeft.y + 245);

        Color enabledColor = new Color(203, 208, 203);
        Color disabledColor = new Color(100, 104, 100);
        Color displayBackgroundColor = new Color(20, 22, 20);

        //resource
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 55, 110, 20, 8, 8);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(nodeType).replace("_", " ").replace("GAS", ""), menuTopLeft.x + 107, menuTopLeft.y + 70);
        g2d.drawImage(itemIcons.get(nodeType), menuTopLeft.x + 87, menuTopLeft.y + 57, 16, 16, null);

        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.12);
        int spacing = buttonHeight / 4;
        int buttonWidth = (int) ((gp.getHeight() * 1.77777777) / 6 - spacing * 2);
        int imageSize = spacing * 2;
        int textSize = buttonHeight / 2;

        g2d.setColor(new Color(34, 36, 34));
        g2d.fillRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8, 0, (int)(gp.getHeight() * 1.77777777) / 8, gp.getHeight());
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing, spacing, buttonWidth, buttonHeight, 10, 10);
        g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing, spacing * 2 + buttonHeight, buttonWidth, buttonHeight, 10, 10);
        g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing, spacing * 3 + buttonHeight * 2, buttonWidth, buttonHeight, 10, 10);
        g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing, spacing * 4 + buttonHeight * 3, buttonWidth, buttonHeight, 10, 10);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, textSize));
        g2d.setColor(Color.WHITE);
        g2d.drawString("UNSET", gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 5, spacing + buttonHeight - (int)(spacing * 1.2));
        g2d.drawImage(itemIcons.get(NodePossibilities.UNSET), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 2, spacing * 2, imageSize, imageSize, null);
        g2d.drawString("CRUDE OIL", gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 5, (spacing + buttonHeight) * 2 - (int)(spacing * 1.2));
        g2d.drawImage(itemIcons.get(NodePossibilities.CRUDE_OIL), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 2, spacing * 3 + buttonHeight, imageSize, imageSize, null);
        g2d.drawString("WATER", gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 5, (spacing + buttonHeight) * 3 - (int)(spacing * 1.2));
        g2d.drawImage(itemIcons.get(NodePossibilities.WATER), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 2, spacing * 4 + buttonHeight * 2, imageSize, imageSize, null);
        g2d.drawString("NITROGEN GAS", gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 5, (spacing + buttonHeight) * 4 - (int)(spacing * 1.2));
        g2d.drawImage(itemIcons.get(NodePossibilities.NITROGEN_GAS), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 2, spacing * 5 + buttonHeight * 3, imageSize, imageSize, null);

        //purity
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 102, menuTopLeft.y + 85, 73, 20, 8, 8);
        g2d.setColor(purity == Purity.IMPURE ? disabledColor : enabledColor);
        g2d.drawString("-", menuTopLeft.x + 85, menuTopLeft.y + 100);
        g2d.setColor(purity == Purity.PURE ? disabledColor : enabledColor);
        g2d.drawString("+", menuTopLeft.x + 180, menuTopLeft.y + 100);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(purity), menuTopLeft.x + 110, menuTopLeft.y + 100);

        // overclock
        g2d.setColor(Color.WHITE);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 165, menuTopLeft.x + 190, menuTopLeft.y + 165);
        int px = menuTopLeft.x + 10 + (int)((180.0 / 249.0) * overclock);
        int py = menuTopLeft.y + 165;
        g2d.fillOval(px - 3, py - 3, 6, 7);

        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 135, 50, 20, 8, 8);
        if (editingOverclock) {
            g2d.setColor(new Color(100, 104, 100));
            g2d.drawRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 135, 50, 20, 8, 8);
        }
        g2d.setColor(Color.WHITE);
        boolean showOverclockCursor = ((int)(System.currentTimeMillis() / 500) % 2) == 0 && editingOverclock;
        g2d.drawString(overclockString + (showOverclockCursor ? "|" : ""), menuTopLeft.x + 15, menuTopLeft.y + 150);
        g2d.drawString("%", menuTopLeft.x + 65, menuTopLeft.y + 150);

        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 135, 70, 20, 8, 8);
        if (editingItems) {
            g2d.setColor(new Color(100, 104, 100));
            g2d.drawRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 135, 70, 20, 8, 8);
        }
        g2d.setColor(Color.WHITE);
        boolean showItemCursor = ((int)(System.currentTimeMillis() / 500) % 2) == 0 && editingItems;
        g2d.drawString(itemString + (showItemCursor ? "|" : ""), menuTopLeft.x + 90, menuTopLeft.y + 150);
        g2d.drawString("/min", menuTopLeft.x + 160, menuTopLeft.y + 150);
    }

    public enum NodePossibilities {
        UNSET, CRUDE_OIL, WATER, NITROGEN_GAS
    }
}