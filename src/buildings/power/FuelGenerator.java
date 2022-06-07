package buildings.power;

import buildings.Building;
import buildings.logistics.Conveyor;
import buildings.logistics.Pipe;
import main.*;
import recipes.Material;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.HashMap;
import java.util.Map;

public class FuelGenerator extends Building {
    public FuelPossibilities fuelType = FuelPossibilities.UNSET;

    public double baseItemRate;
    public double itemRate;

    private boolean isValid = false;
    public boolean isEfficient = false;

    public double overclock = 100;
    private String overclockString = "";
    private boolean editingOverclock = false;
    private String powerString = "";
    private boolean editingPower = false;

    private final double basePower = 150;
    public double powerProduction;
    public int powerSlugs = 0;

    private BufferedImage image;
    private final Map<FuelPossibilities, BufferedImage> itemIcons = new HashMap<>();

    public FuelGenerator(Point position) {
        this.position = position;
        maxInConveyors = 0;
        maxOutConveyors = 0;
        maxInPipes = 1;
        maxOutPipes = 0;
        getImages();
        updateItemRate();
        updatePowerConsumption();
        updateShownRates();
    }

    public FuelGenerator(Point position, double overclock, FuelPossibilities fuelType) {
        this.position = position;
        this.overclock = overclock;
        this.fuelType = fuelType;
        maxInConveyors = 0;
        maxOutConveyors = 0;
        maxInPipes = 1;
        maxOutPipes = 0;
        getImages();
        updateItemRate();
        updatePowerConsumption();
        updateShownRates();
    }

    private void updateShownRates() {
        powerString = String.valueOf(basePower * Math.pow(overclock / 100, 0.76923076923));
        overclockString = String.valueOf(overclock);
    }

    private void getImages() {
        image = Main.getImageFromResources("/images/buildings/fuel_generator.png");

        for (FuelPossibilities f : FuelPossibilities.values()) {
            itemIcons.put(f, Main.getImageFromResources("/images/items/" + String.valueOf(f).toLowerCase() + ".png"));
        }
    }

    private void updateInItems() {
        inItems.clear();
        if (fuelType != FuelPossibilities.UNSET) {
            Material type = switch (fuelType) {
                case FUEL:
                    yield Material.FUEL;
                case TURBOFUEL:
                    yield Material.TURBOFUEL;
                default:
                    throw new IllegalStateException("Unexpected value: " + fuelType);
            };
            inItems.put(type, itemRate);
        }
    }

    private void updateItemRate() {
        baseItemRate = switch (fuelType) {
            case UNSET:
                yield 0;
            case FUEL:
                yield 12;
            case TURBOFUEL:
                yield 4.5;
        };

        itemRate = baseItemRate * Math.pow(overclock / 100, 0.76923076923);
        itemRate = (double)Math.round(itemRate * 10000) / 10000;
    }

    public void updatePowerConsumption() {
        powerProduction = basePower * Math.pow(overclock / 100, 0.76923076923);
        powerProduction = (double)Math.round(powerProduction * 10000) / 10000;

        if (overclock <= 100) powerSlugs = 0;
        if (overclock > 100 && overclock <= 150) powerSlugs = 1;
        if (overclock > 150 && overclock <= 200) powerSlugs = 2;
        if (overclock > 200 && overclock <= 250) powerSlugs = 3;

        power = powerProduction;
    }

    public void updateEfficiency() {
        isEfficient = false;
        if (fuelType != FuelPossibilities.UNSET) {
            double totalItems = 0;
            for (Pipe p : inPipes) {
                if (!p.invalidState) {
                    if (p.type == Material.FUEL && fuelType == FuelPossibilities.FUEL) totalItems += p.outRate;
                    if (p.type == Material.TURBOFUEL && fuelType == FuelPossibilities.TURBOFUEL) totalItems += p.outRate;
                }
            }
            if (totalItems >= itemRate) isEfficient = true;
        }
    }

    private void updateValidity() {
        isValid = fuelType != FuelPossibilities.UNSET;
    }

    public void update() {
        updateItemRate();
        updatePowerConsumption();
        updateEfficiency();
        updateInItems();
        updateValidity();
        if (!editingPower && !editingOverclock) updateShownRates();
    }

    private void exitClockInput() {
        if (editingPower) {
            editingPower = false;
            
            if (powerString.length() < 1) powerString = "0";

            double power = Double.parseDouble(powerString);
            double minPower = basePower * 0.01;
            double maxPower = basePower * 2.5;
            if (power < minPower) power = minPower;
            if (power > maxPower) power = maxPower;
            overclock = 100 * Math.pow(power / basePower, 1.3);

            overclock = (double)Math.round(overclock * 10000) / 10000;
            power = (double)Math.round(power * 10000) / 10000;

            powerString = "" + power;
            overclockString = "" + overclock;
        }
        if (editingOverclock) {
            editingOverclock = false;
            
            if (overclockString.length() < 1) overclockString = "0";

            overclock = Double.parseDouble(overclockString);
            if (overclock > 250) overclock = 250;
            if (overclock < 1) overclock = 1;

            double power = basePower * Math.pow(overclock / 100, 0.76923076923);

            power = (double)Math.round(power * 10000) / 10000;
            overclock = (double)Math.round(overclock * 10000) / 10000;

            powerString = "" + power;
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

        if (editingOverclock || editingPower) {
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
            gp.addBuilding(new FuelGenerator(position, overclock, fuelType));
        }

        // resource
        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.12);
        int spacing = buttonHeight / 4;
        int buttonWidth = (int) ((gp.getHeight() * 1.77777777) / 6 - spacing * 2);

        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing && my > spacing && my < (spacing + buttonHeight)) fuelType = FuelPossibilities.UNSET;
        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing && my > spacing * 2 + buttonHeight && my < (spacing + buttonHeight) * 2) fuelType = FuelPossibilities.FUEL;
        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing && my > spacing * 3 + buttonHeight * 2 && my < (spacing + buttonHeight) * 3) fuelType = FuelPossibilities.TURBOFUEL;

        // overclock
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 60 && my > menuTopLeft.y + 105 && my < menuTopLeft.y + 125) {
            editingOverclock = true;
            editingPower = false;
        }
        if (mx > menuTopLeft.x + 85 && mx < menuTopLeft.x + 155 && my > menuTopLeft.y + 105 && my < menuTopLeft.y + 125) {
            editingPower = true;
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
            gp.addBuilding(new FuelGenerator(position, overclock, fuelType));
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
        if (editingPower) {
            if (keyCode == KeyEvent.VK_BACK_SPACE || keyCode == KeyEvent.VK_DELETE) {
                if (powerString.length() > 0)
                    powerString = powerString.substring(0, powerString.length() - 1);
            } else if (keyCode == KeyEvent.VK_PERIOD) {
                overclockString = overclockString + ".";
            } else if (keyCode >= KeyEvent.VK_0 && keyCode <= KeyEvent.VK_9){
                powerString = powerString + (keyCode - 48);
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
        } else if (!isEfficient) {
            g2d.setColor(ColorManager.getColor("inefficient"));
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
        String nameString = "Fuel Gen.";
        g2d.drawString(nameString, namePos.x, namePos.y);

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.08)));
        Point ocPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.25), gp);
        g2d.drawString(overclock + "%", ocPoint.x, ocPoint.y);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.15)));
        Point ratePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.95), gp);
        g2d.drawString(powerProduction + "MW", ratePos.x, ratePos.y);

        Point imageStart = Screen.convertToScreenPoint(new PointDouble(position.x + 0.72, position.y + 0.72), gp);
        Point imageEnd = Screen.convertToScreenPoint(new PointDouble(position.x + 0.97, position.y + 0.97), gp);
        g2d.drawImage(itemIcons.get(fuelType), imageStart.x, imageStart.y, imageEnd.x - imageStart.x, imageEnd.y - imageStart.y, null);
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
        g2d.drawString("Fuel Generator", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Resource", menuTopLeft.x + 10, menuTopLeft.y + 70);
        g2d.drawString("Overclock", menuTopLeft.x + 10, menuTopLeft.y + 100);

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
        g2d.drawString("Item Rate: " + itemRate + "/min", menuTopLeft.x + 10, menuTopLeft.y + 245);

        Color displayBackgroundColor = new Color(20, 22, 20);

        //resource
        g2d.setFont(new Font("Bahnschrift", Font.BOLD, 16));
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 55, 110, 20, 8, 8);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.setColor(Color.WHITE);
        g2d.drawString(String.valueOf(fuelType).replace("_", " "), menuTopLeft.x + 107, menuTopLeft.y + 70);
        g2d.drawImage(itemIcons.get(fuelType), menuTopLeft.x + 87, menuTopLeft.y + 57, 16, 16, null);

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
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, textSize));
        g2d.setColor(Color.WHITE);
        g2d.drawString("UNSET", gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 5, spacing + buttonHeight - (int)(spacing * 1.2));
        g2d.drawImage(itemIcons.get(FuelPossibilities.UNSET), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 2, spacing * 2, imageSize, imageSize, null);
        g2d.drawString("FUEL", gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 5, (spacing + buttonHeight) * 2 - (int)(spacing * 1.2));
        g2d.drawImage(itemIcons.get(FuelPossibilities.FUEL), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 2, spacing * 3 + buttonHeight, imageSize, imageSize, null);
        g2d.drawString("TURBOFUEL", gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 5, (spacing + buttonHeight) * 3 - (int)(spacing * 1.2));
        g2d.drawImage(itemIcons.get(FuelPossibilities.TURBOFUEL), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 8 + spacing * 2, spacing * 4 + buttonHeight * 2, imageSize, imageSize, null);

        // overclock
        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 135, menuTopLeft.x + 190, menuTopLeft.y + 135);
        int px = menuTopLeft.x + 10 + (int)((180.0 / 249.0) * overclock);
        int py = menuTopLeft.y + 135;
        g2d.fillOval(px - 3, py - 3, 6, 7);

        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 105, 50, 20, 8, 8);
        if (editingOverclock) {
            g2d.setColor(new Color(100, 104, 100));
            g2d.drawRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 105, 50, 20, 8, 8);
        }
        g2d.setColor(Color.WHITE);
        boolean showOverclockCursor = ((int)(System.currentTimeMillis() / 500) % 2) == 0 && editingOverclock;
        g2d.drawString(overclockString + (showOverclockCursor ? "|" : ""), menuTopLeft.x + 15, menuTopLeft.y + 120);
        g2d.drawString("%", menuTopLeft.x + 65, menuTopLeft.y + 120);

        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 105, 70, 20, 8, 8);
        if (editingPower) {
            g2d.setColor(new Color(100, 104, 100));
            g2d.drawRoundRect(menuTopLeft.x + 85, menuTopLeft.y + 105, 70, 20, 8, 8);
        }
        g2d.setColor(Color.WHITE);
        boolean showItemCursor = ((int)(System.currentTimeMillis() / 500) % 2) == 0 && editingPower;
        g2d.drawString(powerString + (showItemCursor ? "|" : ""), menuTopLeft.x + 90, menuTopLeft.y + 120);
        g2d.drawString("MW", menuTopLeft.x + 160, menuTopLeft.y + 120);

        // item requirements
        if (fuelType != FuelPossibilities.UNSET) {
            int height = (int) ((gp.getHeight() * 1.777777) / 80);
            int offset = (int) (gp.getHeight() * 1.777777 / 8 + height);

            g2d.setColor(Color.WHITE);

            double itemAmount = 0;
            for (Conveyor c : inConveyors) if (c.type.toString().equals(fuelType.toString())) itemAmount += c.rate;

            g2d.setFont(new Font("Bahnschrift", itemAmount > itemRate * 0.99 && itemAmount < itemRate * 1.01 ? Font.PLAIN : Font.BOLD, (int) (height * 0.8)));
            g2d.drawImage(itemIcons.get(fuelType), offset, height, (int)(height * 0.8), (int)(height * 0.8), null);
            g2d.drawString(itemAmount + "/" + itemRate, offset + height, height + (int)(height * 0.8));
        }
    }

    public enum FuelPossibilities {
        UNSET, FUEL, TURBOFUEL
    }
}