package buildings.crafters;

import buildings.Building;
import buildings.BuildingType;
import buildings.logistics.Conveyor;
import buildings.logistics.Pipe;
import main.*;
import recipes.Recipe;
import recipes.Recipes;
import recipes.Material;

import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.util.ArrayList;

public class Constructor extends Building {
    public boolean recipeSet = false;
    public Recipe recipe;
    private ArrayList<Recipe> possibleRecipes;
    public double rate;
    private int recipeScroll = 0;

    private boolean isValid = false;
    private boolean isEfficient = false;

    private final double basePower = 4;
    public double powerConsumption;
    public int powerSlugs = 0;

    public double overclock = 100;
    private String overclockString = "";
    private boolean editingOverclock = false;

    private BufferedImage image;

    public Constructor(Point position) {
        this.position = position;
        maxInConveyors = 1;
        maxOutConveyors = 1;
        maxInPipes = 0;
        maxOutPipes = 0;
        getImages();
        initializeRecipes();
    }

    public Constructor(Point position, boolean recipeSet, Recipe recipe, double overclock) {
        this.position = position;
        this.recipeSet = recipeSet;
        this.recipe = recipe;
        this.overclock = overclock;
        maxInConveyors = 1;
        maxOutConveyors = 1;
        maxInPipes = 0;
        maxOutPipes = 0;
        getImages();
        initializeRecipes();
    }

    private void initializeRecipes() {
        possibleRecipes = Recipes.getRecipeByBuilding(BuildingType.CONSTRUCTOR);
    }

    private void getImages() {
        image = Main.getImageFromResources("/images/buildings/constructor.png");
    }

    public void update() {
        if (recipeSet && !editingOverclock) updateShownRates();
        if (recipeSet) {
            rate = 60 / recipe.craftTime * (overclock / 100);
            rate = (double)Math.round(rate * 10000) / 10000;
        }

        updateEfficiency();
        updateInItems();
        updateValidity();
        updateOutItems();
        updatePowerConsumption();
    }

    private void updateInItems() {
        inItems.clear();
        if (recipeSet) {
            for (Material m : recipe.input.keySet()) {
                inItems.put(m, recipe.input.get(m) * (overclock / 100) * (60 / recipe.craftTime));
            }
        }
    }

    private void updateOutItems() {
        outConveyorRate.clear();
        outConveyorType.clear();
        outPipeRate.clear();
        outPipeType.clear();
        if (recipeSet) {
            ArrayList<Material> outMats = new ArrayList<>(recipe.output.keySet());
            for (Material m : outMats) {
                if (Recipes.isConveyorMaterial(m)) {
                    outConveyorRate.add(60 / recipe.craftTime * (double)recipe.output.get(m) * (overclock / 100));
                    outConveyorType.add(m);
                } else {
                    outPipeRate.add(60 / recipe.craftTime * (double)recipe.output.get(m) * (overclock / 100));
                    outPipeType.add(m);
                }
            }
        }
        for (int c = outConveyorRate.size(); c < maxOutConveyors; c++)
            outConveyorRate.add(-1.0);
        for (int p = outPipeRate.size(); p < maxOutPipes; p++)
            outPipeRate.add(-1.0);
    }

    private void updateEfficiency() {
        if (recipeSet) {
            isEfficient = true;
            for (Material m : recipe.input.keySet()) {
                double amount = (60 / recipe.craftTime) * recipe.input.get(m) * (overclock / 100);
                double hasAmount = 0;
                for (Conveyor c : inConveyors) {
                    if (!c.invalidState) {
                        if (c.type == m) hasAmount += c.outRate;
                    }
                }
                for (Pipe p : inPipes) {
                    if (!p.invalidState) {
                        if (p.type == m) hasAmount += p.outRate;
                    }
                }
                if (hasAmount > amount * 1.01 || hasAmount < amount * 0.99) {
                    isEfficient = false;
                    break;
                }
            }
        }
    }

    private void updateValidity() {
        isValid = recipeSet;
    }

    private void updateShownRates() {
        overclockString = String.valueOf(overclock);
    }

    public void updatePowerConsumption() {
        powerConsumption = basePower * Math.pow((overclock / 100), 1.6);
        // round to 5 decimal places
        powerConsumption = (double)Math.round(powerConsumption * 10000) / 10000;

        if (overclock <= 100) powerSlugs = 0;
        if (overclock > 100 && overclock <= 150) powerSlugs = 1;
        if (overclock > 150 && overclock <= 200) powerSlugs = 2;
        if (overclock > 200 && overclock <= 250) powerSlugs = 3;

        power = -powerConsumption;
    }

    private void exitClockInput() {
        editingOverclock = false;

        if (overclockString.length() == 0) overclockString = "1";
        overclock = Double.parseDouble(overclockString);
        if (overclock > 250) overclock = 250;
        if (overclock < 1) overclock = 1;

        overclock = (double)Math.round(overclock * 100) / 100;

        overclockString = "" + overclock;
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
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
            menuBottomRight.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
        }

        if (editingOverclock) {
            exitClockInput();
        } else {
            if ((mx < menuTopLeft.x || mx > menuBottomRight.x || my < menuTopLeft.y || my > menuBottomRight.y) && mx < gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6)
                gp.closeBuildingMenu();
        }

        if (mx > menuTopLeft.x + 105 && mx < menuTopLeft.x + 190 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 95 && my > menuTopLeft.y + 260 && my < menuTopLeft.y + 290) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Constructor(position, recipeSet, recipe, overclock));
        }

        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.25);
        int spacing = buttonHeight / 10;
        int buttonWidth = (int) ((gp.getHeight() * 1.77777777) / 6 - spacing * 2);

        if (mx > gp.getWidth() - buttonWidth - spacing && mx < gp.getWidth() - spacing) {
            ArrayList<Recipe> showRecipes = new ArrayList<>(possibleRecipes.subList(recipeScroll, Math.min(possibleRecipes.size(), recipeScroll + (gp.getHeight() - spacing) / (buttonHeight + spacing))));
            for (int i = 0; i < showRecipes.size(); i++) {
                if (my > spacing + i * (buttonHeight + spacing) && my < (buttonHeight + spacing) + i * (buttonHeight + spacing)) {
                    recipeSet = true;
                    recipe = showRecipes.get(i);
                }
            }
        }

        if (mx > menuTopLeft.x + 10 && mx < menuTopLeft.x + 60 && my > menuTopLeft.y + 135 && my < menuTopLeft.y + 155) {
            editingOverclock = true;
        }
    }

    public void typed(GraphicsPanel gp, int keyCode) {
        if (keyCode == KeyEvent.VK_X) {
            gp.closeBuildingMenu();
            gp.deleteBuilding(this);
        }
        if (keyCode == KeyEvent.VK_D) {
            gp.closeBuildingMenu();
            gp.addBuilding(new Constructor(position, recipeSet, recipe, overclock));
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
    }

    public void scrolled(GraphicsPanel gp, int scrollAmount) {
        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.25);
        int spacing = buttonHeight / 10;

        if (possibleRecipes.size() >= recipeScroll + (gp.getHeight() - spacing) / (spacing + buttonHeight)) {
            recipeScroll += scrollAmount;
            if (recipeScroll < 0) recipeScroll = 0;
            if (recipeScroll + (gp.getHeight() - spacing) / (spacing + buttonHeight) > possibleRecipes.size()) recipeScroll = possibleRecipes.size() - (gp.getHeight() - spacing) / (spacing + buttonHeight);
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
        String nameString = "Constructor";
        g2d.drawString(nameString, namePos.x, namePos.y);

        g2d.setColor(Color.GRAY);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.08)));
        Point ocPoint = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.25), gp);
        g2d.drawString(overclock + "%", ocPoint.x, ocPoint.y);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, Screen.convertLengthToScreenLength(0.15)));
        Point ratePos = Screen.convertToScreenPoint(new PointDouble(position.x + 0.05, position.y + 0.95), gp);
        if (recipeSet) {
            g2d.drawString(rate + "/min", ratePos.x, ratePos.y);
        } else {
            g2d.drawString("-/min", ratePos.x, ratePos.y);
        }

        if (recipeSet) {
            ArrayList<Material> outMats = new ArrayList<>(recipe.output.keySet());
            for (Material m : outMats) {
                int index = outMats.indexOf(m);

                Point imageStart = Screen.convertToScreenPoint(new PointDouble(position.x + 0.72, position.y + 0.72 - 0.2 * index), gp);
                Point imageEnd = Screen.convertToScreenPoint(new PointDouble(position.x + 0.97, position.y + 0.97 - 0.2 * index), gp);
                g2d.drawImage(ImageManager.getMaterialImage(m), imageStart.x, imageStart.y, imageEnd.x - imageStart.x, imageEnd.y - imageStart.y, null);
            }
        }

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
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= 200 + Screen.convertLengthToScreenLength(1);
            menuBottomRight.x -= 200 + Screen.convertLengthToScreenLength(1);
        }
        if (menuBottomRight.x > gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6) {
            menuTopLeft.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
            menuBottomRight.x -= menuBottomRight.x - (gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6);
        }

        g2d.setColor(new Color(30, 32, 30));
        g2d.fillRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);
        g2d.setColor(new Color(90, 95, 90));
        g2d.drawRoundRect(menuTopLeft.x, menuTopLeft.y, menuBottomRight.x - menuTopLeft.x, menuBottomRight.y - menuTopLeft.y, 10, 10);

        g2d.setColor(Color.WHITE);
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        g2d.drawString("Constructor", menuTopLeft.x + 10, menuTopLeft.y + 25);
        g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 35, menuTopLeft.x + 190, menuTopLeft.y + 35);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
        g2d.drawString("Recipe", menuTopLeft.x + 10, menuTopLeft.y + 70);
        g2d.drawString("Overclock", menuTopLeft.x + 10, menuTopLeft.y + 130);

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
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

        Color displayBackgroundColor = new Color(20, 22, 20);

        // recipe
        g2d.setColor(displayBackgroundColor);
        g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 75, 180, 20, 8, 8);
        if (recipeSet) {
            g2d.setColor(Color.WHITE);
            g2d.drawString(recipe.name, menuTopLeft.x + 15, menuTopLeft.y + 89);
        }

        int buttonHeight = (int)((gp.getHeight() * 1.77777777) / 6 * 0.25);
        int spacing = buttonHeight / 10;
        int buttonWidth = (int) ((gp.getHeight() * 1.77777777) / 6 - spacing * 2);
        int textSize = (int) (buttonHeight / 5);
        int imageSize = spacing * 4;

        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, textSize));
        g2d.setColor(new Color(34, 36, 34));
        g2d.fillRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6, 0, (int)(gp.getHeight() * 1.77777777) / 6, gp.getHeight());
        ArrayList<Recipe> showRecipes = new ArrayList<>(possibleRecipes.subList(recipeScroll, Math.min(possibleRecipes.size(), recipeScroll + (gp.getHeight() - spacing) / (spacing + buttonHeight))));
        for (Recipe recipe : showRecipes) {
            int index = showRecipes.indexOf(recipe);

            g2d.setColor(!recipe.name.startsWith("Alt:") ? new Color(20, 22, 20) : new Color(32, 15, 15));
            g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing, spacing + index * (spacing + buttonHeight), buttonWidth, buttonHeight, 10, 10);
            if (recipeSet && recipe == this.recipe) {
                g2d.setColor(new Color(100, 104, 100));
                g2d.drawRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing, spacing + index * (spacing + buttonHeight), buttonWidth, buttonHeight, 10, 10);
            }
            g2d.setColor(Color.WHITE);
            g2d.drawString(recipe.name, gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing * 2, spacing * 4 + index * (buttonHeight + spacing));

            ArrayList<Material> inMats = new ArrayList<>(recipe.input.keySet());
            for (Material inMat : inMats) {
                int matIndex = inMats.indexOf(inMat);
                g2d.setColor(new Color(39, 40, 39));
                g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing * 2 + (imageSize + spacing / 2) * matIndex, (int) (spacing * 5.5 + index * (spacing + buttonHeight)), imageSize, imageSize, 4, 4);
                g2d.drawImage(ImageManager.getMaterialImage(inMat), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6  + spacing * 2 + (imageSize + spacing / 2) * matIndex, (int) (spacing * 5.5 + index * (spacing + buttonHeight)), imageSize, imageSize, null);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(recipe.input.get(inMat) * (60 / recipe.craftTime)), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing * 2 + (imageSize + spacing / 2) * matIndex, buttonHeight - spacing + index * (spacing + buttonHeight));
            }

            int matOffset = recipe.input.keySet().size();
            g2d.fillPolygon(new Polygon(new int[]{gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + (int)(spacing * 3.5) + (imageSize + spacing / 2) * matOffset, gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + (int)(spacing * 3.5) + (imageSize + spacing / 2) * matOffset, gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + (int)(spacing * 4.5) + (imageSize + spacing / 2) * matOffset}, new int[]{(spacing * 6) + index * (spacing + buttonHeight), (spacing * 7) + index * (spacing + buttonHeight), (int)(spacing * 6.5) + index * (spacing + buttonHeight)}, 3));

            ArrayList<Material> outMats = new ArrayList<>(recipe.output.keySet());
            for (Material outMat : outMats) {
                int matIndex = outMats.indexOf(outMat) + matOffset + 1;
                g2d.setColor(new Color(39, 40, 39));
                g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing * 2 + (imageSize + spacing / 2) * matIndex, (int) (spacing * 5.5 + index * (spacing + buttonHeight)), imageSize, imageSize, 4, 4);
                g2d.drawImage(ImageManager.getMaterialImage(outMat), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6  + spacing * 2 + (imageSize + spacing / 2) * matIndex, (int) (spacing * 5.5 + index * (spacing + buttonHeight)), imageSize, imageSize, null);
                g2d.setColor(Color.WHITE);
                g2d.drawString(String.valueOf(recipe.output.get(outMat) * (60 / recipe.craftTime)), gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing * 2 + (imageSize + spacing / 2) * matIndex, buttonHeight - spacing + index * (spacing + buttonHeight));
            }
        }
        if (possibleRecipes.size() > (gp.getHeight() - spacing) / (spacing + buttonHeight)) {
            int rHeight = (gp.getHeight() - spacing * 2) / possibleRecipes.size();
            g2d.setColor(new Color(25, 27, 25));
            g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing / 4, spacing, spacing / 2, possibleRecipes.size() * rHeight, 5, 5);
            g2d.setColor(new Color(60, 64, 60));
            g2d.fillRoundRect(gp.getWidth() - (int)(gp.getHeight() * 1.77777777) / 6 + spacing / 4, spacing + rHeight * recipeScroll, spacing / 2, rHeight * ((gp.getHeight() - spacing) / (buttonHeight + spacing)), 5, 5);
        }

        // overclock
        if (recipeSet) {
            g2d.setColor(Color.WHITE);
            g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 16));
            g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 165, menuTopLeft.x + 190, menuTopLeft.y + 165);
            int px = menuTopLeft.x + 10 + (int) ((180.0 / 249.0) * overclock);
            int py = menuTopLeft.y + 165;
            g2d.fillOval(px - 3, py - 3, 6, 7);

            g2d.setColor(displayBackgroundColor);
            g2d.fillRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 135, 50, 20, 8, 8);
            if (editingOverclock) {
                g2d.setColor(new Color(100, 104, 100));
                g2d.drawRoundRect(menuTopLeft.x + 10, menuTopLeft.y + 135, 50, 20, 8, 8);
            }
            g2d.setColor(Color.WHITE);
            boolean showOverclockCursor = ((int) (System.currentTimeMillis() / 500) % 2) == 0 && editingOverclock;
            g2d.drawString(overclockString + (showOverclockCursor ? "|" : ""), menuTopLeft.x + 15, menuTopLeft.y + 150);
            g2d.drawString("%", menuTopLeft.x + 65, menuTopLeft.y + 150);
        } else {
            g2d.setColor(Color.WHITE);
            g2d.drawLine(menuTopLeft.x + 10, menuTopLeft.y + 145, menuTopLeft.x + 100, menuTopLeft.y + 145);
        }

        // item requirements
        if (recipeSet) {
            int height = (int) ((gp.getHeight() * 1.777777) / 80);
            int offset = (int) (gp.getHeight() * 1.777777 / 8 + height);

            g2d.setColor(Color.WHITE);

            ArrayList<Material> inMats = new ArrayList<>(recipe.input.keySet());
            for (Material m : inMats) {
                int index = inMats.indexOf(m);

                double total = recipe.input.get(m) * (60 / recipe.craftTime);
                double amount = 0;
                for (Conveyor c : inConveyors) if (c.type == m) amount += c.rate;
                for (Pipe p : inPipes) if (p.type == m) amount += p.rate;

                g2d.setFont(new Font("Bahnschrift", amount > total * 0.99 && amount < total * 1.01 ? Font.PLAIN : Font.BOLD, (int) (height * 0.8)));

                g2d.drawImage(ImageManager.getMaterialImage(m), offset, height * (index + 1), (int)(height * 0.8), (int)(height * 0.8), null);
                g2d.drawString(amount + "/" + total, offset + height, height * (index + 1) + (int)(height * 0.8));
            }
        }
    }
}
