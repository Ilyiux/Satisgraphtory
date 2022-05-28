package main;

import buildings.Building;
import buildings.crafters.*;
import buildings.logistics.*;
import buildings.power.CoalGenerator;
import buildings.power.FuelGenerator;
import buildings.power.NuclearPowerPlant;
import buildings.producers.*;
import buildings.WorldObject;
import recipes.Material;
import recipes.Recipe;
import recipes.Recipes;
import recipes.Materials;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class GraphicsPanel extends JPanel implements Runnable, MouseListener, KeyListener, FocusListener, MouseWheelListener {
    public final static int WIDTH = 1920;
    public final static int HEIGHT = 1080;
    private Thread thread;

    private boolean midMouseDown = false;
    private Point midMousePos;

    private boolean leftMouseDown = false;
    Building dragBuilding;

    private double totalPower = 0;

    private boolean shiftDown = false;
    private boolean ctrlDown = false;
    private boolean firstConnection = true;
    private Building tempConnectionStart;
    private ConnectionType tempConnectionType;
    private enum ConnectionType {CONVEYOR, PIPE}

    Point origin = new Point(0, 0);

    boolean inMenuObject = false;
    WorldObject menuObject;

    private int defaultConveyorTier = 1;
    private int defaultPipeTier = 1;

    ArrayList<Building> buildings = new ArrayList<>();
    ArrayList<Connector> connectors = new ArrayList<>();

    ArrayList<Long> lastButtonClickTimes = new ArrayList<>();

    MenuState menuState = MenuState.NEW_GENERATOR;

    private enum MenuState {
        NEW_GENERATOR, NEW_SMELTER, NEW_CRAFTER, NEW_LOGISTIC
    }

    GraphicsPanel() {
        this.setPreferredSize(new Dimension(WIDTH, HEIGHT));
        this.setBackground(new Color(24, 26, 24));
        this.setDoubleBuffered(false);

        this.setFocusable(true);
        init();
    }

    public void startThread() {
        thread = new Thread(this);
        thread.start();
    }

    @Override
    public void run() {
        int frameTicks = 120;
        double drawInterval = 1000000000 / (double) frameTicks;
        double delta = 0;
        long lastTime = System.nanoTime();
        long currentTime;

        while (thread != null) {
            currentTime = System.nanoTime();
            delta += (currentTime - lastTime) / drawInterval;

            lastTime = currentTime;

            if (delta > 1) {
                update();
                repaint();
                delta --;
            }
        }
    }

    // called on program start
    private void init() {
        addMouseListener(this);
        addFocusListener(this);
        addKeyListener(this);
        addMouseWheelListener(this);

        Screen.setCenter(new PointDouble(0, 0));
        Recipes.constructRecipes();
        Materials.constructSink();
        ImageManager.loadMaterialImages();

        emptyClickList();
    }

    // called before draw
    private void update() {
        if (midMouseDown) {
            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            Point screenPos = getLocationOnScreen();
            int mx = mousePos.x - screenPos.x;
            int my = mousePos.y - screenPos.y;
            Screen.translateCenter(mx - midMousePos.x, my - midMousePos.y);
            midMousePos = new Point(mx, my);
        }
        if (leftMouseDown) {
            Point mousePos = MouseInfo.getPointerInfo().getLocation();
            Point screenPos = getLocationOnScreen();
            int mx = mousePos.x - screenPos.x;
            int my = mousePos.y - screenPos.y;

            dragBuilding.position = Screen.convertToWorldPoint(new Point(mx, my));
        }

        for (Building building : buildings) building.menuOpen = false;
        if (inMenuObject) menuObject.menuOpen = true;

        for (Connector connector : connectors) connector.update();
        for (Building building : buildings) building.update();

        updateTotalPower();
    }

    private void emptyClickList() {
        lastButtonClickTimes.clear();
        for (int i = 0; i < 8; i++) lastButtonClickTimes.add((long)0);
    }

    private void setClickList(int index) {
        lastButtonClickTimes.set(index, System.currentTimeMillis());
    }

    private boolean isClickActive(int index) {
        return lastButtonClickTimes.get(index) + 150 > System.currentTimeMillis();
    }

    private void updateTotalPower() {
        totalPower = 0;
        for (Building b : buildings) {
            totalPower += b.power;
        }
    }

    private int getBuildingIndexUnderMouse(int mx, int my) {
        for (int i = 0; i < buildings.size(); i ++) {
            Building building = buildings.get(i);
            Point bs = Screen.convertToScreenPoint(new PointDouble(building.position.x, building.position.y));
            Point be = Screen.convertToScreenPoint(new PointDouble(building.position.x + 1, building.position.y + 1));
            if (bs.x < mx && be.x > mx && bs.y < my && be.y > my) return i;
        }
        return -1;
    }

    private int getConnectorIndexUnderMouse(int mx, int my) {
        for (int i = 0; i < connectors.size(); i ++) {
            Connector connector = connectors.get(i);
            Point cs = Screen.convertToScreenPoint(new PointDouble(connector.lineCenter.x - 0.25, connector.lineCenter.y - 0.25));
            Point ce = Screen.convertToScreenPoint(new PointDouble(connector.lineCenter.x + 0.25, connector.lineCenter.y + 0.25));
            if (cs.x < mx && ce.x > mx && cs.y < my && ce.y > my) return i;
        }
        return -1;
    }

    // call from building with menu open when registering a click outside the menu
    public void closeBuildingMenu() {
        inMenuObject = false;
    }

    public void deleteBuilding(Building building) {
        for (Conveyor c : building.inConveyors) {
            c.startBuilding.outConveyors.remove(c);
            connectors.remove(c);
        }
        for (Conveyor c : building.outConveyors) {
            c.endBuilding.inConveyors.remove(c);
            connectors.remove(c);
        }
        for (Pipe p : building.inPipes) {
            p.startBuilding.outPipes.remove(p);
            connectors.remove(p);
        }
        for (Pipe p : building.outPipes) {
            p.endBuilding.inPipes.remove(p);
            connectors.remove(p);
        }
        buildings.remove(building);
    }

    public void deleteConnector(Connector connector) {
        connector.startBuilding.outConveyors.remove(connector);
        connector.startBuilding.outPipes.remove(connector);
        connector.endBuilding.inConveyors.remove(connector);
        connector.endBuilding.inPipes.remove(connector);
        connectors.remove(connector);
    }

    public void addBuilding(Building building) {
        buildings.add(0, building);
    }

    public void save(String location) {
        StringBuilder saveData = new StringBuilder();

        for (Building b : buildings) {
            String data = "b ";

            if (b instanceof Assembler) {
                data += ("asm " + b.position.x + " " + b.position.y + " ");
                data += (((Assembler) b).overclock + " " + ((Assembler) b).recipeSet + " " + Recipes.recipes.indexOf(((Assembler) b).recipe));
            } else if (b instanceof Blender) {
                data += ("ble " + b.position.x + " " + b.position.y + " ");
                data += (((Blender) b).overclock + " " + ((Blender) b).recipeSet + " " + Recipes.recipes.indexOf(((Blender) b).recipe));
            } else if (b instanceof Constructor) {
                data += ("con " + b.position.x + " " + b.position.y + " ");
                data += (((Constructor) b).overclock + " " + ((Constructor) b).recipeSet + " " + Recipes.recipes.indexOf(((Constructor) b).recipe));
            } else if (b instanceof Foundry) {
                data += ("fnd " + b.position.x + " " + b.position.y + " ");
                data += (((Foundry) b).overclock + " " + ((Foundry) b).recipeSet + " " + Recipes.recipes.indexOf(((Foundry) b).recipe));
            } else if (b instanceof Manufacturer) {
                data += ("man " + b.position.x + " " + b.position.y + " ");
                data += (((Manufacturer) b).overclock + " " + ((Manufacturer) b).recipeSet + " " + Recipes.recipes.indexOf(((Manufacturer) b).recipe));
            } else if (b instanceof Packager) {
                data += ("pck " + b.position.x + " " + b.position.y + " ");
                data += (((Packager) b).overclock + " " + ((Packager) b).recipeSet + " " + Recipes.recipes.indexOf(((Packager) b).recipe));
            } else if (b instanceof ParticleAccelerator) {
                data += ("prt " + b.position.x + " " + b.position.y + " ");
                data += (((ParticleAccelerator) b).overclock + " " + ((ParticleAccelerator) b).recipeSet + " " + Recipes.recipes.indexOf(((ParticleAccelerator) b).recipe));
            } else if (b instanceof Refinery) {
                data += ("rfn " + b.position.x + " " + b.position.y + " ");
                data += (((Refinery) b).overclock + " " + ((Refinery) b).recipeSet + " " + Recipes.recipes.indexOf(((Refinery) b).recipe));
            } else if (b instanceof Smelter) {
                data += ("sme " + b.position.x + " " + b.position.y + " ");
                data += (((Smelter) b).overclock + " " + ((Smelter) b).recipeSet + " " + Recipes.recipes.indexOf(((Smelter) b).recipe));
            } else if (b instanceof Splitter) {
                data += ("spl " + b.position.x + " " + b.position.y);
            } else if (b instanceof Merger) {
                data += ("mrg " + b.position.x + " " + b.position.y);
            } else if (b instanceof Junction) {
                data += ("jnc " + b.position.x + " " + b.position.y);
            } else if (b instanceof AwesomeSink) {
                data += ("awe " + b.position.x + " " + b.position.y);
            } else if (b instanceof CoalGenerator) {
                data += ("col " + b.position.x + " " + b.position.y + " ");
                data += (((CoalGenerator) b).overclock + " " + Arrays.asList(CoalGenerator.FuelPossibilities.values()).indexOf(((CoalGenerator) b).fuelType));
            } else if (b instanceof FuelGenerator) {
                data += ("ful " + b.position.x + " " + b.position.y + " ");
                data += (((FuelGenerator) b).overclock + " " + Arrays.asList(FuelGenerator.FuelPossibilities.values()).indexOf(((FuelGenerator) b).fuelType));
            } else if (b instanceof NuclearPowerPlant) {
                data += ("nuc " + b.position.x + " " + b.position.y + " ");
                data += (((NuclearPowerPlant) b).overclock + " " + Arrays.asList(NuclearPowerPlant.FuelPossibilities.values()).indexOf(((NuclearPowerPlant) b).fuelType));
            } else if (b instanceof BottomlessBox) {
                data += ("box " + b.position.x + " " + b.position.y + " ");
                data += (((BottomlessBox) b).materialSet + " " + Arrays.asList(Material.values()).indexOf(((BottomlessBox) b).material));
            } else if (b instanceof Miner) {
                data += ("min " + b.position.x + " " + b.position.y + " ");
                data += (((Miner) b).overclock + " " + ((Miner) b).tier + " " + Arrays.asList(Purity.values()).indexOf(((Miner) b).purity) + " " + Arrays.asList(Miner.NodePossibilities.values()).indexOf(((Miner) b).nodeType));
            } else if (b instanceof OilExtractor) {
                data += ("oil " + b.position.x + " " + b.position.y + " ");
                data += (((OilExtractor) b).overclock + " " + Arrays.asList(Purity.values()).indexOf(((OilExtractor) b).purity));
            } else if (b instanceof ResourceWellExtractor) {
                data += ("res " + b.position.x + " " + b.position.y + " ");
                data += (((ResourceWellExtractor) b).overclock + " " + Arrays.asList(Purity.values()).indexOf(((ResourceWellExtractor) b).purity) + " " + Arrays.asList(ResourceWellExtractor.NodePossibilities.values()).indexOf(((ResourceWellExtractor) b).nodeType));
            } else if (b instanceof WaterExtractor) {
                data += ("wat " + b.position.x + " " + b.position.y + " ");
                data += (((WaterExtractor) b).overclock);
            } else {
                data += ("invalid");
            }
            data += ("\n");
            saveData.append(data);
        }
        for (Connector c : connectors) {
            String data = "c ";

            if (c instanceof Conveyor) {
                data += ("cov " + buildings.indexOf(c.startBuilding) + " " + buildings.indexOf(c.endBuilding) + " " + c.tier);
            } else if (c instanceof Pipe) {
                data += ("pip " + buildings.indexOf(c.startBuilding) + " " + buildings.indexOf(c.endBuilding) + " " + c.tier);
            } else {
                data += ("invalid");
            }

            data += ("\n");
            saveData.append(data);
        }

        try {
            FileWriter fWriter = new FileWriter(location, false);
            fWriter.write(saveData.toString());
            fWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load(String location) {
        ArrayList<String> buildingLines = new ArrayList<>();
        ArrayList<String> connectorLines = new ArrayList<>();

        try {
            File myObj = new File(location);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (data.startsWith("b")) {
                    buildingLines.add(data.substring(2));
                } else if (data.startsWith("c")) {
                    connectorLines.add(data.substring(2));
                } else {
                    System.out.println("Error while reading save '" + location + "''");
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        buildings.clear();
        connectors.clear();
        for (String b : buildingLines) {
            String id = b.substring(0, 3);
            String[] data = b.substring(4).split(" ");

            int posX = Integer.parseInt(data[0]);
            int posY = Integer.parseInt(data[1]);

            Building bu = null;

            if (id.equals("asm")) { // assembler
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Assembler(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("ble")) { // blender
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Blender(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("con")) { // constructor
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Constructor(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("fnd")) { // foundry
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Foundry(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("man")) { // manufacturer
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Manufacturer(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("pck")) { // packager
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Packager(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("prt")) { // particle accelerator
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new ParticleAccelerator(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("rfn")) { // refinery
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Refinery(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("sme")) { // smelter
                double overclock = Double.parseDouble(data[2]);
                boolean setRecipe = Boolean.parseBoolean(data[3]);
                Recipe recipe = Recipes.recipes.get(Integer.parseInt(data[4]));
                bu = new Smelter(new Point(posX, posY), setRecipe, recipe, overclock);
            } else if (id.equals("spl")) { // splitter
                bu = new Splitter(new Point(posX, posY));
            } else if (id.equals("mrg")) { // merger
                bu = new Merger(new Point(posX, posY));
            } else if (id.equals("jnc")) { // junction
                bu = new Junction(new Point(posX, posY));
            } else if (id.equals("awe")) { // awesome sink
                bu = new AwesomeSink(new Point(posX, posY));
            } else if (id.equals("col")) { // coal generator
                double overclock = Double.parseDouble(data[2]);
                CoalGenerator.FuelPossibilities fuelType = CoalGenerator.FuelPossibilities.values()[Integer.parseInt(data[3])];
                bu = new CoalGenerator(new Point(posX, posY), overclock, fuelType);
            } else if (id.equals("ful")) { // fuel generator
                double overclock = Double.parseDouble(data[2]);
                FuelGenerator.FuelPossibilities fuelType = FuelGenerator.FuelPossibilities.values()[Integer.parseInt(data[3])];
                bu = new FuelGenerator(new Point(posX, posY), overclock, fuelType);
            } else if (id.equals("nuc")) { // nuclear power plant
                double overclock = Double.parseDouble(data[2]);
                NuclearPowerPlant.FuelPossibilities fuelType = NuclearPowerPlant.FuelPossibilities.values()[Integer.parseInt(data[3])];
                bu = new NuclearPowerPlant(new Point(posX, posY), overclock, fuelType);
            } else if (id.equals("box")) { // bottomless box
                boolean materialSet = Boolean.parseBoolean(data[2]);
                Material material = Material.values()[Integer.parseInt(data[3])];
                bu = new BottomlessBox(new Point(posX, posY), materialSet, material);
            } else if (id.equals("min")) { // miner
                double overclock = Double.parseDouble(data[2]);
                int tier = Integer.parseInt(data[3]);
                Purity purity = Purity.values()[Integer.parseInt(data[4])];
                Miner.NodePossibilities nodeType = Miner.NodePossibilities.values()[Integer.parseInt(data[5])];
                bu = new Miner(new Point(posX, posY), tier, purity, overclock, nodeType);
            } else if (id.equals("oil")) { // oil extractor
                double overclock = Double.parseDouble(data[2]);
                Purity purity = Purity.values()[Integer.parseInt(data[3])];
                bu = new OilExtractor(new Point(posX, posY), purity, overclock);
            } else if (id.equals("res")) { // resource well extractor
                double overclock = Double.parseDouble(data[2]);
                Purity purity = Purity.values()[Integer.parseInt(data[3])];
                ResourceWellExtractor.NodePossibilities nodeType = ResourceWellExtractor.NodePossibilities.values()[Integer.parseInt(data[4])];
                bu = new ResourceWellExtractor(new Point(posX, posY), purity, overclock, nodeType);
            } else if (id.equals("wat")) { // water extractor
                double overclock = Double.parseDouble(data[2]);
                bu = new WaterExtractor(new Point(posX, posY));
            } else {
                System.out.println("Error while reading save '" + location + "'");
            }

            if (bu != null) {
                bu.update();
                buildings.add(bu);
            }
        }
        for (String c : connectorLines) {
            String id = c.substring(0, 3);
            String[] data = c.substring(4).split(" ");

            Building s = buildings.get(Integer.parseInt(data[0]));
            Building e = buildings.get(Integer.parseInt(data[1]));
            int tier = Integer.parseInt(data[2]);

            if (id.equals("cov")) { // conveyor
                Conveyor ct = new Conveyor(s, e, tier);
                s.outConveyors.add(ct);
                e.inConveyors.add(ct);
                connectors.add(ct);
            } else if (id.equals("pip")) { // pipe
                Pipe pt = new Pipe(s, e, tier);
                s.outPipes.add(pt);
                e.inPipes.add(pt);
                connectors.add(pt);
            } else {
                System.out.println("Error reading save '" + location + "' ");
            }
        }
    }

    // called on draw
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2d = (Graphics2D)g;

        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        Point screenPos = getLocationOnScreen();
        int mx = mousePos.x - screenPos.x;
        int my = mousePos.y - screenPos.y;

        g2d.setRenderingHints(new RenderingHints(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON));

        // draw origin
        g2d.setColor(new Color(28, 30, 28));
        Point originTopLeft = Screen.convertToScreenPoint(new PointDouble(origin.x, origin.y));
        Point originBottomRight = Screen.convertToScreenPoint(new PointDouble(origin.x + 1, origin.y + 1));
        g2d.drawRect(originTopLeft.x, originTopLeft.y, originBottomRight.x - originTopLeft.x, originBottomRight.y - originTopLeft.y);

        // draw all conveyors and pipes
        for (Connector connector : connectors) connector.draw(shiftDown || ctrlDown, g2d);
        // draw all buildings
        for (Building building : buildings) {
            boolean convColored = shiftDown && (firstConnection ? (building.outConveyors.size() < building.maxOutConveyors) : ((tempConnectionType == ConnectionType.CONVEYOR) && building.inConveyors.size() < building.maxInConveyors));
            boolean pipeColored = ctrlDown && (firstConnection ? (building.outPipes.size() < building.maxOutPipes) : ((tempConnectionType == ConnectionType.PIPE) && building.inPipes.size() < building.maxInPipes));
            building.draw((shiftDown || ctrlDown) && !(convColored || pipeColored), g2d); // (shiftDown || ctrlDown) && !(convColored || pipeColored)
        }

        if (inMenuObject) menuObject.drawMenu(g2d);

        // draw a connector if needed
        if ((shiftDown || ctrlDown) && !firstConnection) {
            g2d.setColor(tempConnectionType == ConnectionType.CONVEYOR ? Color.WHITE : Color.ORANGE);
            Point buildingStart = Screen.convertToScreenPoint(new PointDouble(tempConnectionStart.position.x + 0.5, tempConnectionStart.position.y + 0.5));
            int hiddenLength = Screen.convertLengthToScreenLength(0.45);
            double hiddenPercent = (double)hiddenLength / (int)Math.sqrt((mx - buildingStart.x)*(mx - buildingStart.x) + (my - buildingStart.y)*(my - buildingStart.y));
            int sx = (int)(buildingStart.x * (1 - hiddenPercent) + mx * hiddenPercent);
            int sy = (int)(buildingStart.y * (1 - hiddenPercent) + my * hiddenPercent);
            g2d.drawLine(sx, sy, mx, my);
        }

        // left menu background
        g2d.setColor(new Color(40, 44, 40));
        g2d.fillRect(0, 0, getWidth() / 8, getHeight());

        // BUTTONS
        g2d.setFont(new Font("Bahnschrift", Font.PLAIN, 20));
        int bWidth = getWidth() / 8 - 20;
        int bHeight = 40;
        Color onColor = new Color(100, 105, 100);
        Color offColor = new Color(70, 75, 70);

        g2d.setColor(menuState == MenuState.NEW_GENERATOR ? onColor : offColor);
        g2d.fillRoundRect(10, 10, bWidth, bHeight, 10, 10);
        g2d.setColor(menuState == MenuState.NEW_SMELTER ? onColor : offColor);
        g2d.fillRoundRect(10, 60, bWidth, bHeight, 10, 10);
        g2d.setColor(menuState == MenuState.NEW_CRAFTER ? onColor : offColor);
        g2d.fillRoundRect(10, 110, bWidth, bHeight, 10, 10);
        g2d.setColor(menuState == MenuState.NEW_LOGISTIC ? onColor : offColor);
        g2d.fillRoundRect(10, 160, bWidth, bHeight, 10, 10);

        g2d.setColor(new Color(150, 155, 150));
        g2d.drawString("GENERATORS", 25, 38);
        g2d.drawString("SMELTERS", 25, 88);
        g2d.drawString("CRAFTERS", 25, 138);
        g2d.drawString("LOGISTICS", 25, 188);

        g2d.setColor(new Color(24, 26, 24));
        g2d.drawLine(0, 215, getWidth() / 8, 215);

        // more menu buttons
        if (menuState == MenuState.NEW_GENERATOR) {
            g2d.setColor(isClickActive(0) ? onColor : offColor);
            g2d.fillRoundRect(10, 230, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(1) ? onColor : offColor);
            g2d.fillRoundRect(10, 280, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(2) ? onColor : offColor);
            g2d.fillRoundRect(10, 330, bWidth, bHeight + 20, 10, 10);
            g2d.setColor(isClickActive(3) ? onColor : offColor);
            g2d.fillRoundRect(10, 400, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(4) ? onColor : offColor);
            g2d.fillRoundRect(10, 450, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(5) ? onColor : offColor);
            g2d.fillRoundRect(10, 500, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(6) ? onColor : offColor);
            g2d.fillRoundRect(10, 550, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(7) ? onColor : offColor);
            g2d.fillRoundRect(10, 600, bWidth, bHeight, 10, 10);

            g2d.setColor(new Color(150, 155, 150));
            g2d.drawString("WATER EXTRACTOR", 25, 258);
            g2d.drawString("OIL EXTRACTOR", 25, 308);
            g2d.drawString("RESOURCE WELL", 25, 358);
            g2d.drawString("EXTRACTOR", 25, 378);
            g2d.drawString("MINER", 25, 428);
            g2d.drawString("COAL GENERATOR", 25, 478);
            g2d.drawString("FUEL GENERATOR", 25, 528);
            g2d.drawString("NUCLEAR POWER", 25, 578);
            g2d.drawString("BOTTOMLESS BOX", 25, 628);
        }
        if (menuState == MenuState.NEW_SMELTER) {
            g2d.setColor(new Color(70, 75, 70));

            g2d.setColor(isClickActive(0) ? onColor : offColor);
            g2d.fillRoundRect(10, 230, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(1) ? onColor : offColor);
            g2d.fillRoundRect(10, 280, bWidth, bHeight, 10, 10);

            g2d.setColor(new Color(150, 155, 150));
            g2d.drawString("SMELTER", 25, 258);
            g2d.drawString("FOUNDRY", 25, 308);
        }
        if (menuState == MenuState.NEW_CRAFTER) {
            g2d.setColor(new Color(70, 75, 70));

            g2d.setColor(isClickActive(0) ? onColor : offColor);
            g2d.fillRoundRect(10, 230, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(1) ? onColor : offColor);
            g2d.fillRoundRect(10, 280, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(2) ? onColor : offColor);
            g2d.fillRoundRect(10, 330, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(3) ? onColor : offColor);
            g2d.fillRoundRect(10, 380, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(4) ? onColor : offColor);
            g2d.fillRoundRect(10, 430, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(5) ? onColor : offColor);
            g2d.fillRoundRect(10, 480, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(6) ? onColor : offColor);
            g2d.fillRoundRect(10, 530, bWidth, bHeight + 20, 10, 10);

            g2d.setColor(new Color(150, 155, 150));
            g2d.drawString("CONSTRUCTOR", 25, 258);
            g2d.drawString("ASSEMBLER", 25, 308);
            g2d.drawString("MANUFACTURER", 25, 358);
            g2d.drawString("PACKAGER", 25, 408);
            g2d.drawString("REFINERY", 25, 458);
            g2d.drawString("BLENDER", 25, 508);
            g2d.drawString("PARTICLE", 25, 558);
            g2d.drawString("ACCELERATOR", 25, 578);
        }
        if (menuState == MenuState.NEW_LOGISTIC) {
            g2d.setColor(new Color(70, 75, 70));

            g2d.setColor(isClickActive(0) ? onColor : offColor);
            g2d.fillRoundRect(10, 230, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(1) ? onColor : offColor);
            g2d.fillRoundRect(10, 280, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(2) ? onColor : offColor);
            g2d.fillRoundRect(10, 330, bWidth, bHeight, 10, 10);
            g2d.setColor(isClickActive(3) ? onColor : offColor);
            g2d.fillRoundRect(10, 380, bWidth, bHeight, 10, 10);

            g2d.setColor(new Color(150, 155, 150));
            g2d.drawString("SPLITTER", 25, 258);
            g2d.drawString("MERGER", 25, 308);
            g2d.drawString("JUNCTION", 25, 358);
            g2d.drawString("AWESOME SINK", 25, 408);
        }

        g2d.setColor(new Color(70, 75, 70));

        g2d.drawRoundRect(10, getHeight() - bHeight - 210, bHeight, bHeight, 10, 10);
        g2d.drawRoundRect(10 + bWidth - bHeight, getHeight() - bHeight - 210, bHeight, bHeight, 10, 10);
        g2d.drawString("Conveyor Tier", 10, getHeight() - bHeight - 222);
        g2d.drawString("-", 25, getHeight() - bHeight - 182);
        g2d.drawString("+", getWidth() / 8 - 35, getHeight() - bHeight - 182);
        g2d.drawString(String.valueOf(defaultConveyorTier), bWidth / 2, getHeight() - bHeight - 182);
        g2d.drawRoundRect(10, getHeight() - bHeight - 110, bHeight, bHeight, 10, 10);
        g2d.drawRoundRect(10 + bWidth - bHeight, getHeight() - bHeight - 110, bHeight, bHeight, 10, 10);
        g2d.drawString("Pipe Tier", 10, getHeight() - bHeight - 122);
        g2d.drawString("-", 25, getHeight() - bHeight - 82);
        g2d.drawString("+", getWidth() / 8 - 35, getHeight() - bHeight - 82);
        g2d.drawString(String.valueOf(defaultPipeTier), bWidth / 2, getHeight() - bHeight - 82);

        g2d.drawRoundRect(10, getHeight() - bHeight - 60, bWidth, bHeight, 10, 10);
        g2d.drawString("Reset Origin", 25, getHeight() - bHeight - 32);
        g2d.drawRoundRect(10, getHeight() - bHeight - 10, bWidth, bHeight, 10, 10);
        g2d.drawString("Reset View", 25, getHeight() - bHeight + 18);

        g2d.drawString(Math.abs((double)(int)(totalPower * 10000) / 10000) + "mw " + (totalPower <= 0 ? "cost" : "creation"), getWidth() / 8 + 20, getHeight() - 20);

        g.dispose();
    }

    @Override
    public void focusGained(FocusEvent focusEvent) {

    }

    @Override
    public void focusLost(FocusEvent focusEvent) {

    }

    @Override
    public void keyTyped(KeyEvent keyEvent) {}

    @Override
    public void keyPressed(KeyEvent keyEvent) {
        if (inMenuObject) {
            menuObject.typed(this, keyEvent.getKeyCode());
        }
        if (!inMenuObject) {
            if (keyEvent.getKeyCode() == KeyEvent.VK_C) {
                PointDouble screenCenter = Screen.getCenter();
                origin = new Point((int) Math.floor(screenCenter.x), (int) Math.floor(screenCenter.y));
            }
            if (keyEvent.getKeyCode() == KeyEvent.VK_F) {
                Screen.setCenter(new PointDouble(origin.x, origin.y));
                Screen.setZoom(100);
            }

            if (keyEvent.getKeyCode() == KeyEvent.VK_SHIFT && !inMenuObject) {
                shiftDown = true;
                ctrlDown = false;
                leftMouseDown = false;
            }if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL && !inMenuObject) {
                ctrlDown = true;
                shiftDown = false;
                leftMouseDown = false;
            }
        }
        if (keyEvent.getKeyCode() == KeyEvent.VK_S) {
            save("C:/Users/Sam/Documents/github/Satisgraphtory/test_saves/save.stgs");
        }
        if (keyEvent.getKeyCode() == KeyEvent.VK_L) {
            load("C:/Users/Sam/Documents/github/Satisgraphtory/test_saves/save.stgs");
        }
    }

    @Override
    public void keyReleased(KeyEvent keyEvent) {
        if (keyEvent.getKeyCode() == KeyEvent.VK_SHIFT) {
            shiftDown = false;
            firstConnection = true;
        }
        if (keyEvent.getKeyCode() == KeyEvent.VK_CONTROL) {
            ctrlDown = false;
            firstConnection = true;
        }
    }

    @Override
    public void mouseClicked(MouseEvent mouseEvent) {
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        Point screenPos = getLocationOnScreen();
        int mx = mousePos.x - screenPos.x;
        int my = mousePos.y - screenPos.y;

        if (!shiftDown && !ctrlDown) {
            if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                if (mx > 10 && mx < getWidth() / 8 - 10 && my > 10 && my < 50) {
                    menuState = MenuState.NEW_GENERATOR;
                    emptyClickList();
                }
                if (mx > 10 && mx < getWidth() / 8 - 10 && my > 60 && my < 100) {
                    menuState = MenuState.NEW_SMELTER;
                    emptyClickList();
                }
                if (mx > 10 && mx < getWidth() / 8 - 10 && my > 110 && my < 150) {
                    menuState = MenuState.NEW_CRAFTER;
                    emptyClickList();
                }
                if (mx > 10 && mx < getWidth() / 8 - 10 && my > 160 && my < 200) {
                    menuState = MenuState.NEW_LOGISTIC;
                    emptyClickList();
                }

                if (menuState == MenuState.NEW_GENERATOR) {
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 230 && my < 270) {// new water extractor
                        buildings.add(0, new WaterExtractor(origin));
                        setClickList(0);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 280 && my < 320) {// new oil extractor
                        buildings.add(0, new OilExtractor(origin));
                        setClickList(1);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 330 && my < 390) {// new resource well extractor
                        buildings.add(0, new ResourceWellExtractor(origin));
                        setClickList(2);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 400 && my < 440) {// new miner
                        buildings.add(0, new Miner(origin));
                        setClickList(3);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 450 && my < 490) {// new coal generator
                        buildings.add(0, new CoalGenerator(origin));
                        setClickList(4);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 500 && my < 540) {// new fuel generator
                        buildings.add(0, new FuelGenerator(origin));
                        setClickList(5);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 550 && my < 590) {// new nuclear power plant
                        buildings.add(0, new NuclearPowerPlant(origin));
                        setClickList(6);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 600 && my < 640) {// new generator
                        buildings.add(0, new BottomlessBox(origin));
                        setClickList(7);
                    }
                }
                if (menuState == MenuState.NEW_SMELTER) {
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 230 && my < 270) {// new smelter
                        buildings.add(0, new Smelter(origin));
                        setClickList(0);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 280 && my < 320) {// new foundry
                        buildings.add(0, new Foundry(origin));
                        setClickList(1);
                    }
                }
                if (menuState == MenuState.NEW_CRAFTER) {
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 230 && my < 270) {// new constructor
                        buildings.add(0, new Constructor(origin));
                        setClickList(0);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 280 && my < 320) {// new assembler
                        buildings.add(0, new Assembler(origin));
                        setClickList(1);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 330 && my < 370) {// new manufacturer
                        buildings.add(0, new Manufacturer(origin));
                        setClickList(2);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 380 && my < 420) {// new packager
                        buildings.add(0, new Packager(origin));
                        setClickList(3);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 430 && my < 470) {// new refinery
                        buildings.add(0, new Refinery(origin));
                        setClickList(4);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 480 && my < 520) {// new blender
                        buildings.add(0, new Blender(origin));
                        setClickList(5);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 530 && my < 590) {// new particle accelerator
                        buildings.add(0, new ParticleAccelerator(origin));
                        setClickList(6);
                    }
                }
                if (menuState == MenuState.NEW_LOGISTIC) {
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 230 && my < 270) {// new splitter
                        buildings.add(0, new Splitter(origin));
                        setClickList(0);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 280 && my < 320) {// new merger
                        buildings.add(0, new Merger(origin));
                        setClickList(1);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 330 && my < 370) {// new junction
                        buildings.add(0, new Junction(origin));
                        setClickList(2);
                    }
                    if (mx > 10 && mx < getWidth() / 8 - 10 && my > 380 && my < 420) {// new awesome sink
                        buildings.add(0, new AwesomeSink(origin));
                        setClickList(3);
                    }
                }

                // increase default conveyor
                if (mx > getWidth() / 8 - 50 && mx < getWidth() / 8 - 10 && my > getHeight() - 250 && my < getHeight() - 210) {
                    defaultConveyorTier ++;
                    if (defaultConveyorTier > 5) defaultConveyorTier = 5;
                }
                // decrease default conveyor
                if (mx > 10 && mx < 50 && my > getHeight() - 250 && my < getHeight() - 210) {
                    defaultConveyorTier --;
                    if (defaultConveyorTier < 1) defaultConveyorTier = 1;
                }
                // increase default pipe
                if (mx > getWidth() / 8 - 50  && mx < getWidth() / 8 - 10 && my > getHeight() - 150 && my < getHeight() - 110) {
                    defaultPipeTier ++;
                    if (defaultPipeTier > 2) defaultPipeTier = 2;
                }
                // decrease default pipe
                if (mx > 10 && mx < 50 && my > getHeight() - 150 && my < getHeight() - 110) {
                    defaultPipeTier --;
                    if (defaultPipeTier < 1) defaultPipeTier = 1;
                }

                // set origin
                if (mx > 10 && mx < getWidth() / 8 - 10 && my > getHeight() - 100 && my < getHeight() - 60) {
                    PointDouble screenCenter = Screen.getCenter();
                    origin = new Point((int) Math.floor(screenCenter.x), (int) Math.floor(screenCenter.y));
                }

                // reset view
                if (mx > 10 && mx < getWidth() / 8 - 10 && my > getHeight() - 50 && my < getHeight() - 10) {
                    Screen.setCenter(new PointDouble(origin.x, origin.y));
                    Screen.setZoom(100);
                }
            }
        }
    }

    @Override
    public void mousePressed(MouseEvent mouseEvent) {
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        Point screenPos = getLocationOnScreen();
        int mx = mousePos.x - screenPos.x;
        int my = mousePos.y - screenPos.y;

        if (inMenuObject && !shiftDown && !ctrlDown) {
            if (mouseEvent.getButton() == MouseEvent.BUTTON1) {
                menuObject.clicked(this, mx, my);
            }
        }
        if (!inMenuObject) {
            if (mouseEvent.getButton() == MouseEvent.BUTTON2 && mx > getWidth() / 8) {
                midMouseDown = true;
                midMousePos = new Point(mx, my);
            }
            if (mouseEvent.getButton() == MouseEvent.BUTTON1 && mx > getWidth() / 8 && !shiftDown && !ctrlDown) {
                leftMouseDown = true;
                int bIndex = getBuildingIndexUnderMouse(mx, my);
                if (bIndex != -1) dragBuilding = buildings.get(bIndex);
                else leftMouseDown = false;
            }
            if (mouseEvent.getButton() == MouseEvent.BUTTON3 && mx > getWidth() / 8 && !shiftDown && !ctrlDown) {
                int cIndex = getConnectorIndexUnderMouse(mx, my);
                if (cIndex != -1) {
                    menuObject = connectors.get(cIndex);
                    inMenuObject = true;
                } else {
                    int bIndex = getBuildingIndexUnderMouse(mx, my);
                    if (bIndex != -1) {
                        menuObject = buildings.get(bIndex);
                        inMenuObject = true;
                    }
                }
            }
        }
        if (shiftDown) {
            int bIndex = getBuildingIndexUnderMouse(mx, my);
            if (bIndex != -1) {
                if (firstConnection) {
                    if (buildings.get(bIndex).outConveyors.size() < buildings.get(bIndex).maxOutConveyors) {
                        firstConnection = false;
                        tempConnectionType = ConnectionType.CONVEYOR;
                        tempConnectionStart = buildings.get(bIndex);
                    }
                } else {
                    if (buildings.get(bIndex).inConveyors.size() < buildings.get(bIndex).maxInConveyors && buildings.get(bIndex) != tempConnectionStart) {
                        firstConnection = true;
                        Conveyor newConveyor = new Conveyor(tempConnectionStart, buildings.get(bIndex), defaultConveyorTier);

                        tempConnectionStart.outConveyors.add(newConveyor);
                        buildings.get(bIndex).inConveyors.add(newConveyor);

                        connectors.add(newConveyor);
                    }
                }
            }
        }
        if (ctrlDown) {
            int bIndex = getBuildingIndexUnderMouse(mx, my);
            if (bIndex != -1) {
                if (firstConnection) {
                    if (buildings.get(bIndex).outPipes.size() < buildings.get(bIndex).maxOutPipes) {
                        firstConnection = false;
                        tempConnectionType = ConnectionType.PIPE;
                        tempConnectionStart = buildings.get(bIndex);
                    }
                } else {
                    if (buildings.get(bIndex).inPipes.size() < buildings.get(bIndex).maxInPipes && buildings.get(bIndex) != tempConnectionStart) {
                        firstConnection = true;
                        Pipe newPipe = new Pipe(tempConnectionStart, buildings.get(bIndex), defaultPipeTier);

                        tempConnectionStart.outPipes.add(newPipe);
                        buildings.get(bIndex).inPipes.add(newPipe);

                        connectors.add(newPipe);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent mouseEvent) {
        if (mouseEvent.getButton() == MouseEvent.BUTTON2) midMouseDown = false;
        if (mouseEvent.getButton() == MouseEvent.BUTTON1) leftMouseDown = false;
    }

    @Override
    public void mouseEntered(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseExited(MouseEvent mouseEvent) {

    }

    @Override
    public void mouseWheelMoved(MouseWheelEvent mouseWheelEvent) {
        Point mousePos = MouseInfo.getPointerInfo().getLocation();
        Point screenPos = getLocationOnScreen();
        int mx = mousePos.x - screenPos.x;
        int my = mousePos.y - screenPos.y;
        if (inMenuObject) {
            menuObject.scrolled(this, mouseWheelEvent.getWheelRotation());
        }
        if (!inMenuObject) {
            Screen.zoom(mouseWheelEvent.getWheelRotation(), mx, my);
        }
    }
}