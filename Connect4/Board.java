//It turns out, we need a lot of classes/functions/methods for this to work
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.MouseInfo;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.event.KeyEvent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.Timer;
import javax.swing.event.MouseInputListener;
import java.io.*;
import java.util.Scanner;
import java.io.File;
import java.awt.event.KeyListener;
import java.util.Random;

public class Board extends JPanel implements ActionListener {
    //All global integers
    int[][] board = new int[6][7];
    private final int DELAY = 25;
    private int screenHeight;
    private int spaceBetween;
    private int screenWidth;
    private int circleSize;
    int currentColumn = 0;
    int currentItem = -1;
    int currentPosX = 0;
    int currentPosY = 0;
    int setKeyCode = -1;
    int currentRow = 0;
    int escapeInt = 27;
    int rightKey = 39;
    int spaceKey = 32;
    int leftKey = 37;
    int losses = 0;
    int wins = 0;
    int key = -1;
    int escapeKeyCode;

    //All global colors
    final Color player2 = Color.yellow;
    final Color blank = Color.white;
    final Color player1 = Color.red;

    //All global booleans
    boolean displayingStatistics = false;
    boolean loadingOrSavingGame = false;
    boolean changingGamemode = false;
    boolean settingsOpen = false;
    boolean singlePlayer = false;
    boolean managedWin = false;
    boolean escapeMenu = false;
    boolean mouseDown = false;
    boolean player_1 = false;
    boolean gameWon = false;

    //All other global variables
    GButton[] buttons = { new GButton(), new GButton(), new GButton(), new GButton(), new GButton(), new GButton() };
    GInputPrompt[] prompts = { new GInputPrompt(), new GInputPrompt(), new GInputPrompt(), new GInputPrompt() };
    final String saveLocation = System.getProperty("user.home") + "\\Connect 4\\";
    GButton loadingOrSavingButton = new GButton();
    Font font = new Font("Serif", Font.BOLD, 36);
    GInputPrompt prompt = new GInputPrompt();
    GButton changeButton = new GButton();
    long last_time = System.nanoTime();
    String loadingOrSavingString = "";
    Dimension screenSize;
    double deltaTime = 0;
    private Timer timer;
    JFrame frame;

    //Removes all chips from the game
    void resetBoard(){
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board[i].length; j++){
                board[i][j] = 0;
            }
        }
    }

    //Stops the game if any player won
    void testForWinner(){
        if(isWinner(1)){
            gameWon = true;
        }else if(isWinner(2)){
            gameWon = true;
        }else if(boardFull()){
            gameWon = true;
        }
    }

    //Gets the lowest point in the column and places a chip there (if you can)
    void placeChip(){
        int i = 6;
        boolean found = false;
        while(i > 0){
            i--;
            if(board[i][currentColumn] == 0){
                found = true;
                break;
            }
        }
        if(found){
            player_1 = !player_1;
            board[i][currentColumn] = 1;
            if(!player_1)
                board[i][currentColumn] = 2;
            testForWinner();
        }
    }

    //Gets the best position (for a beatable AI) to place a chip for the computer
    void getBestSpot(){
        for(int i = 0; i < 7; i++){
            if(tryPlaceChip(i, false)){
                currentColumn = i;
                return;
            }
        }
        for(int i = 0; i < 7; i++){
            if(tryPlaceChip(i, true)){
                currentColumn = i;
                return;
            }
        }
        currentColumn = new Random().nextInt(7);
    }

    //Test places a chip to see how it affects the game
    boolean tryPlaceChip(int c, boolean defend){
        int i = 6;
        boolean found = false;
        while(i > 0){
            i--;
            if(board[i][c] == 0){
                found = true;
                break;
            }
        }
        if(found){
            board[i][c] = 1;
            if(!player_1)
                board[i][c] = 2;
            if(isWinner(1) && !defend){
                board[i][c] = 0;
                return true;
            }else if(isWinner(2) && defend){
                board[i][c] = 0;
                return true;
            }
            board[i][c] = 0;
            return false;
        }else{
            return false;
        }
    }

    //Sets key binds
    String convertToName(int key){
        switch(key){
            case 13:
                return "Enter";
            case 27:
                return "Escape";
            case 37:
                return "Left";
            case 38:
                return "Up";
            case 39:
                return "Right";
            case 40:
                return "Down";
            case 32:
                return "Space";
            default:
                return Character.toString((char)key).toLowerCase();
        }
    }

    //Gets delta time (Used to make button animations smooth)
    void calculateFps(){
        long time = System.nanoTime();
        deltaTime = (int) ((time - last_time) / 1000000);
        last_time = time;
        deltaTime /= 1000;
    }

    //Draws a button
    void drawButton(Graphics g, int width, int height, int centerX, int centerY, String text, FontMetrics metrics){
        g.setColor(new Color(0, 0, 0, 255));
        g.fillRect(centerX - width / 2 + 4, centerY - height / 2 + 4, width, height);
        g.setColor(new Color(34,40,49, 255));
        g.fillRect(centerX - width / 2, centerY - height / 2, width, height);
        g.setColor(Color.black);
        int drawX, drawY;
        drawX = centerX - metrics.stringWidth(text) / 2;
        drawY = centerY + metrics.getHeight() / 4;
        g.drawString(text, drawX + 3, drawY + 3);
        g.setColor(Color.white);
        g.drawString(text, drawX, drawY);
    }

    //Draws an input prompt
    void drawInput(Graphics g, GInputPrompt prompt, int width, int height, int centerX, int centerY, FontMetrics metrics){
        g.setColor(new Color(0, 0, 0, 255));
        g.fillRect(centerX - width / 2 + 4, centerY - height / 2 + 4, width, height);
        g.setColor(new Color(255, 255, 255, 255 / 2));
        g.fillRect(centerX - width / 2 - 4, centerY - height / 2 - 4, width + 8, height + 8);
        g.setColor(new Color(34,40,49, 255));
        g.fillRect(centerX - width / 2, centerY - height / 2, width, height);
        g.setColor(Color.white);
        int drawY = centerY + metrics.getHeight() / 4;
        g.drawString(prompt.getValue(), centerX - width / 2 + 4, drawY);
    }

    //Gets the height and width of the window
    void calculateDismensions(){
        screenSize = frame.getBounds().getSize();
    }
    
    //Gets the settings file, and applies them
    void loadSettings(){
        File file = new File(saveLocation + "settings\\settings.txt");
        if(file.exists()){
            try{
                Scanner in = new Scanner(file);
                while(in.hasNextLine()){
                    String line = in.nextLine();
                    if(line.split(":").length > 1){
                        String key = line.split(":")[0];
                        int keyCode = Integer.parseInt(line.split(":")[1]);
                        changeKey(key, keyCode);
                    }
                }
                in.close();
            }catch(FileNotFoundException e){}
        }
    }

    //Edits the settings file (when we change a key bind)
    void setSettings(){
        File file = new File(saveLocation + "settings\\settings.txt");
        try{
            FileWriter fWriter = new FileWriter(file);
            fWriter.write("Settings:" + escapeInt + "\n");
            fWriter.write("Left:" + leftKey + "\n");
            fWriter.write("Right:" + rightKey + "\n");
            fWriter.write("Space:" + spaceKey);
            fWriter.close();
        }catch(IOException e){}
    }

    //Loads a save by its name
    boolean load(String name){
        try{
            File file = new File(saveLocation + "saves\\" + name + ".txt");
            Scanner in = new Scanner(file);
            if(file.exists()){
                if(in.hasNextLine()){
                    String data = in.nextLine();
                    String[] newBoard = data.split("BOARD::")[1].split(";;")[0].split(",");
                    player_1 = data.split("TURN::")[1].equals("true");
                    for(int i = 0; i < newBoard.length; i++){
                        int x, y;
                        x = (int)(i / 7);
                        y = i - (x * 7);
                        board[x][y] = Integer.parseInt(newBoard[i]);
                    }
                }
                in.close();
                testForWinner();
                return true;
            }
            in.close();
            testForWinner();
        }catch(FileNotFoundException err){
            System.out.print("\nFile not found");
        }
        return false;
    }

    //Creates a save by its name
    boolean save(String name){
        name = name.replace(" ","_");
        String s = (saveLocation + "saves\\" + name + ".txt");
        File file = new File(s);
        try{
            FileWriter fileW = new FileWriter(file);

            String str = "BOARD::";
            for(int i = 0; i < board.length; i++){
                for(int j = 0; j < board[i].length; j++){
                    str += board[i][j];
                    if(i != board.length - 1 || j != board[i].length - 1){
                        str += ",";
                    }
                }
            }

            str += ";;TURN::" + player_1;

            fileW.write(str);
            fileW.close();
        }catch(IOException err){
            err.printStackTrace();
            return false;
        }
        return true;
    }

    //Creates the directories we need for this all to function (  "Your home directory/Connect 4/"  )
    boolean createDefaultDirectories(String dir){
        File targetFile = new File(dir);
        if(targetFile.exists()) return false;
        File parent = targetFile.getParentFile();
        if (!parent.exists() && !parent.mkdirs()) {
            System.out.println("Error creating directory!");
            return false;
        }else{
            targetFile.mkdirs();
            createDefaultDirectories(saveLocation + "saves\\");
            return true;
        }
    }

    //Gets the current statistics
    void getStatistics(){
        File f = new File(saveLocation + "statistics\\statistics.txt");
        if(f.exists()){
            try{
                Scanner in = new Scanner(f);
                while(in.hasNextLine()){
                    String line = in.nextLine();
                    if(line.split(":").length > 1){
                        String type = line.split(":")[0];
                        int value = Integer.parseInt(line.split(":")[1]);
                        if(type.equals("Wins")){
                            wins = value;
                        }else{
                            losses = value;
                        }
                    }
                }
                in.close();
            }catch(FileNotFoundException e){}
        }
    }

    //Saves the current game mode so it will be the same when you load up again
    void saveGameMode(){
        File f = new File(saveLocation + "settings\\gamemode.txt");
        try{
            FileWriter writer = new FileWriter(f);
            writer.write(Boolean.toString(singlePlayer));
            writer.close();
        }catch(IOException e){}
    }

    //Sets the game mode to the previous one
    void loadGameMode(){
        File f = new File(saveLocation + "settings\\gamemode.txt");
        if(f.exists()){
            try{
                Scanner in = new Scanner(f);
                if(in.hasNextLine()){
                    singlePlayer = Boolean.parseBoolean(in.nextLine());
                }
                in.close();
            }catch(FileNotFoundException e){}
        }
    }

    //Edits the statistics file to the current wins and losses
    void setStatistics(){
        File f = new File(saveLocation + "statistics\\statistics.txt");
        try{
            FileWriter fWriter = new FileWriter(f);
            fWriter.write("Wins:" + wins + "\n" + "Losses:" + losses);
            fWriter.close();
        }catch(IOException e){
            System.out.println(e);
        }
    }

    //Tests to see if the board is filled
    boolean boardFull(){
        for(int i = 0; i < board.length; i++){
            for(int j = 0; j < board.length; j++){
                if(board[i][j] == 0){
                    return false;
                }
            }
        }
        return true;
    }

    //Draws our circles and text to the screen
    void drawBoard(Graphics g){
        //Get deltaTime and window size
        calculateFps();
        calculateDismensions();
        //Determine where our chips are drawn
        int currentX = spaceBetween;
        int currentY = spaceBetween;
        g.setColor(Color.black);
        String toWrite;
        FontMetrics metrics = g.getFontMetrics(font);
        prompts[0].setValue(convertToName(spaceKey));
        prompts[1].setValue(convertToName(escapeInt));
        prompts[2].setValue(convertToName(leftKey));
        prompts[3].setValue(convertToName(rightKey));
        if(escapeMenu){
            int height = (int)screenSize.getHeight() / 12;
            if(settingsOpen){
                String[] changeableKeys = { "Select", "Menu", "Left", "Right" };
                int centerY = (int)screenSize.getHeight() / 2;
                int[] yPoses = { centerY - height * 3, centerY - height, centerY + height, centerY + height * 3 };
                String string = "Click an Input Box and type a key";
                int drawX = metrics.stringWidth(string);
                g.setColor(Color.black);
                g.drawString(string, (int)screenSize.getWidth() / 2 - drawX / 2, 60);
                for(int i = 0; i < changeableKeys.length; i++){
                    if(key != -1 && currentItem == i){
                        if(i == 0) spaceKey = key;
                        if(i == 1) escapeInt = key;
                        if(i == 2) leftKey = key;
                        if(i == 3) rightKey = key;
                        setSettings();

                        currentItem = -1;
                        key = -1;
                    }
                    drawButton(g, (int)(screenSize.getWidth() / 4), height, (int)screenSize.getWidth() / 3, yPoses[i], changeableKeys[i], metrics);
                    drawInput(g, prompts[i], (int)(screenSize.getWidth() / 4), height, 2 * ((int)screenSize.getWidth() / 3), yPoses[i], metrics);
                    if(mouseOver(2 * ((int)screenSize.getWidth() / 3) - ((int)(screenSize.getWidth() / 4) / 2), yPoses[i], (int)(screenSize.getWidth() / 4), height) && mouseDown){
                        currentItem = i;
                        mouseDown = false;
                    }
                    currentY += spaceBetween + height;
                }
                return;
            }
            else if(loadingOrSavingGame){
                prompt.setActive();
                int drawY = (int)(screenSize.getHeight() / 2 + height * 0.8);
                if(mouseOver((int)screenSize.getWidth() / 2 - (int)(screenSize.getWidth() / 1.75) / 2, drawY - height / 4, (int)(screenSize.getWidth() / 1.75), height)){
                    loadingOrSavingButton.grow(deltaTime);
                    boolean worked = false;
                    if(mouseDown && loadingOrSavingString == "Save"){
                        worked = save(prompt.getValue());
                    }else if(mouseDown){
                        worked = load(prompt.getValue());
                    }
                    if(worked){
                        loadingOrSavingGame = false;  
                        escapeMenu = false;
                    }
                    mouseDown = false;
                }else{
                    loadingOrSavingButton.shrink(deltaTime);
                }
                drawButton(g, (int)(screenSize.getWidth() / loadingOrSavingButton.getSize()), height, (int)screenSize.getWidth() / 2, drawY, loadingOrSavingString, metrics);
                drawInput(g, prompt, (int)(screenSize.getWidth() / 2), height, (int)screenSize.getWidth() / 2, (int)(screenSize.getHeight() / 2 - height * 0.8), metrics);
                mouseDown = false;
                return;
            }else if(displayingStatistics){
                int centerY = (int)screenSize.getHeight() / 2;
                String string = "Statistics:";
                int drawX = metrics.stringWidth(string);
                g.setColor(Color.black);
                g.drawString(string, (int)screenSize.getWidth() / 2 - drawX / 2, 60);
                drawButton(g, (int)(screenSize.getWidth() / 4), height, (int)screenSize.getWidth() / 3, centerY - height, "Player 1 Wins", metrics);
                drawButton(g, (int)(screenSize.getWidth() / 4), height, 2 * ((int)screenSize.getWidth() / 3), centerY - height, Integer.toString(wins), metrics);

                drawButton(g, (int)(screenSize.getWidth() / 4), height, (int)screenSize.getWidth() / 3, centerY + height, "Player 2 Wins", metrics);
                drawButton(g, (int)(screenSize.getWidth() / 4), height, 2 * ((int)screenSize.getWidth() / 3), centerY + height, Integer.toString(losses), metrics);
                return;
            }else if(changingGamemode){
                int centerY = (int)screenSize.getHeight() / 2;
                String string = "Current Mode:  Multiplayer";
                if(singlePlayer) string = "Current Mode:  Singleplayer";
                g.drawString(string, (int)screenSize.getWidth() / 2 - metrics.stringWidth(string) / 2, centerY - height - metrics.getHeight() / 2);
                drawButton(g, (int)(screenSize.getWidth() / changeButton.getSize()), height, (int)screenSize.getWidth() / 2, centerY + height, "Change", metrics);
                if(mouseOver((int)screenSize.getWidth() / 2 - (int)(screenSize.getWidth() / changeButton.getSize()) / 2, centerY + height, (int)(screenSize.getWidth() / changeButton.getSize()), height)){
                    changeButton.grow(deltaTime);
                    if(mouseDown){
                        singlePlayer = !singlePlayer;
                        saveGameMode();
                    }
                }else{
                    changeButton.shrink(deltaTime);
                }
                mouseDown = false;
                return;
            }
            String[] labels = { "Save Game", "Load Game", "Game Mode", "Settings", "Statistics", "Quit" };
            int spaceBetween = ((int)screenSize.getHeight() - height * labels.length) / 5;
            int drawY = spaceBetween + spaceBetween / 2;
            for(int i = 0; i < labels.length; i++){
                if(mouseOver((int)screenSize.getWidth() / 2 - (int)(screenSize.getWidth() / 1.75) / 2, drawY - height / labels.length, (int)(screenSize.getWidth() / 1.75), height)){
                    if(mouseDown){
                        mouseDown = false;
                        if(labels[i] == "Save Game"){
                            loadingOrSavingString = "Save";
                            loadingOrSavingGame = true;
                        }else if(labels[i] == "Load Game"){
                            loadingOrSavingString = "Load";
                            loadingOrSavingGame = true;
                        }else if(labels[i] == "Settings"){
                            settingsOpen = true;
                        }else if(labels[i] == "Quit"){
                            System.exit(0);
                        }else if(labels[i] == "Statistics"){
                            displayingStatistics = true;
                        }else if(labels[i] == "Game Mode"){
                            changingGamemode = true;
                        }
                        break;
                    }
                    buttons[i].grow(deltaTime);
                    drawButton(g, (int)(screenSize.getWidth() / buttons[i].getSize()), height, (int)screenSize.getWidth() / 2, drawY, labels[i], metrics);
                }else{
                    buttons[i].shrink(deltaTime);
                    drawButton(g, (int)(screenSize.getWidth() / buttons[i].getSize()), height, (int)screenSize.getWidth() / 2, drawY, labels[i], metrics);
                }
                drawY += height + spaceBetween / 2;
            }
            prompt.disable();
            mouseDown = false;
            return;
        }
        prompt.disable();
        if(player_1 && !gameWon){
            toWrite = "Player 2's turn";
        }else if(!gameWon){
            toWrite = "Player 1's turn";
        }else{
            if(isWinner(1)){
                toWrite = "Player 1 won the game!";
                if(!managedWin){
                    wins++;
                    managedWin = true;
                    setStatistics();
                }
            }else if(isWinner(2)){
                toWrite = "Player 2 won the game!";
                if(!managedWin){
                    losses++;
                    managedWin = true;
                    setStatistics();
                }
            }else if(boardFull()){
                toWrite = "No one won the game! TIE!";
                if(!managedWin){
                    managedWin = true;
                }
            }else{
                toWrite = "Error!";
            }
        }
        int drawX = (screenWidth - metrics.stringWidth(toWrite)) / 2;
        g.fillRect(10, 750, screenWidth - 40, 130);
        g.setColor(Color.white);
        g.drawString(toWrite, drawX, 825);
        if(!gameWon){
            int posX = currentX;
            for(int i = 0; i < board.length; i++){
                currentX = spaceBetween;
                for(int j = 0; j < board[i].length; j++){
                    if(currentColumn == j)
                        posX = currentX;
                        if(mouseOver(currentX, 0, circleSize, (int)screenSize.getHeight())){
                            currentPosX = currentX;
                            currentPosY = currentY;
                            currentRow = i;
                            currentColumn = j;
                            posX = currentX;
                            break;
                        }
                    currentX += spaceBetween + circleSize;
                }
                currentY += spaceBetween + circleSize;
            }
            g.setColor(new Color(0, 0, 0, 120));
            g.fillRect(posX, 0, circleSize, screenHeight);
            if(mouseDown){
                if(singlePlayer && player_1){
                    
                }else{
                    placeChip();
                    mouseDown = false;
                }
            }
            if(singlePlayer && player_1){
                getBestSpot();
                placeChip();
            }
        }
        currentX = spaceBetween;
        currentY = spaceBetween;
        for(int i = 0; i < board.length; i++){
            currentX = spaceBetween;
            for(int j = 0; j < board[i].length; j++){
                Color c;
                if(board[i][j] == 0)
                    c = blank;
                else if(board[i][j] == 1)
                    c = player1;
                else
                    c = player2;
                if(j == currentColumn && c == blank){
                    if(!gameWon){
                        if(i < 5){
                            if(board[i+1][j] != 0){
                                c = Color.green;
                            }
                        }else if(c == blank){
                            c = Color.green;
                        }
                    }
                }
                drawCircle(g, c, currentX, currentY);
                currentX += spaceBetween + circleSize;
            }
            currentY += spaceBetween + circleSize;
        }
        if(gameWon){
            g.setColor(Color.black);
            g.drawString("Click Anywhere To Play Again", 
                (screenWidth - metrics.stringWidth("Click Anywhere To Play Again")) / 2 + 1, 
                ((screenHeight - metrics.getHeight()) / 2) + 1);
            g.drawString("Click Anywhere To Play Again", 
                (screenWidth - metrics.stringWidth("Click Anywhere To Play Again")) / 2, 
                ((screenHeight - metrics.getHeight()) / 2));
        }
    }

    //Tests if a particular player is the winner
    public boolean isWinner(int player){
		for(int row = 0; row<board.length; row++){
			for (int col = 0;col < board[0].length - 3;col++){
				if (board[row][col] == player && board[row][col+1] == player &&
                    board[row][col+2] == player && board[row][col+3] == player){
					return true;
				}
			}			
		}
		for(int row = 0; row < board.length - 3; row++){
			for(int col = 0; col < board[0].length; col++){
                if (board[row][col] == player && board[row+1][col] == player &&
                    board[row+2][col] == player && board[row+3][col] == player){
					return true;
				}
			}
		}
		for(int row = 3; row < board.length; row++){
			for(int col = 0; col < board[0].length - 3; col++){
				if (board[row][col] == player && board[row-1][col+1] == player &&
					board[row-2][col+2] == player && board[row-3][col+3] == player){
					return true;
				}
			}
		}
		for(int row = 0; row < board.length - 3; row++){
			for(int col = 0; col < board[0].length - 3; col++){
				if (board[row][col] == player && board[row+1][col+1] == player &&
					board[row+2][col+2] == player && board[row+3][col+3] == player){
					return true;
				}
			}
		}
		return false;
    }
    
    //Utilized by the boxCollides function
    boolean collides(int x, int y, int r, int b, int x2, int y2, int r2, int b2) {//Algorithm to tell if two objects collide
        return !(r <= x2 || x > r2 || b <= y2 || y > b2);
    }

    //Takes in x & y positions of 2 objects, and their width & height 
    boolean boxCollides(int pos0, int pos1, int size0, int size1, int pos2, int pos3, int size2, int size3) {
        return collides(pos0, pos1,
            pos0 + size0, pos1 + size1,
            pos2, pos3,
            pos2 + size2, pos3 + size3);
    }

    //Test if mouse is over object
    boolean mouseOver(int x, int y, int width, int height){
        //Get mouse position
        int mX =  MouseInfo.getPointerInfo().getLocation().x - frame.getLocationOnScreen().x;
        int mY = MouseInfo.getPointerInfo().getLocation().y - frame.getLocationOnScreen().y;
        //If the mouse intersects with the object
        if(boxCollides(mX - 1, mY - 1, 3, 3, x, y, width, height)){
            return true;
        }
        return false;
    }

    //Default Constructor (Don't use)
    public Board() {
        initBoard();
    }

    //Constructor setting dismensions and adding mouse listeners
    public Board(int sW, int sH, JFrame f){
        getStatistics();
        loadGameMode();
        createDefaultDirectories(saveLocation + "settings\\");
        createDefaultDirectories(saveLocation + "statistics\\");
        getStatistics();
        screenWidth = sW;
        screenHeight = sH;
        frame = f;
        frame.addMouseListener(new MouseInputListener(){
            @Override
            public void mouseMoved(MouseEvent e) {}
            @Override
            public void mouseDragged(MouseEvent e) {}
            @Override
            public void mouseReleased(MouseEvent e) {}
            @Override
            public void mousePressed(MouseEvent e) {}
            @Override
            public void mouseExited(MouseEvent e) {}
            @Override
            public void mouseEntered(MouseEvent e) {}
            @Override
            public void mouseClicked(MouseEvent e) {
                mouseDown = true;
                if(gameWon){
                    mouseDown = false;
                    gameWon = false;
                    managedWin = false;
                    resetBoard();
                }
            }
        });
        initBoard();
    }

    //Changes a key binding
    void changeKey(String keyName, int key){
        if(keyName.equals("Settings")){
            escapeInt = key;
        }else if(keyName.equals("Left")){
            leftKey = key;
        }else if(keyName.equals("Right")){
            rightKey = key;
        }else if(keyName.equals("Space")){
            spaceKey = key;
        }
    }

    //Called when the game starts
    private void initBoard() {
        loadSettings();
        resetBoard();
        frame.addKeyListener(new KeyListener(){
            @Override
            public void keyTyped(KeyEvent e) {}
            
            @Override
            public void keyPressed(KeyEvent e) {
                if(currentItem != -1){
                    key = e.getKeyCode();
                    return;
                }
                int code = e.getKeyCode();
                if(code == 16) return;
                if(code == escapeInt){
                    escapeMenu = !escapeMenu;
                    if(!escapeMenu){
                        changingGamemode = false;
                        settingsOpen = false;
                        loadingOrSavingGame = false;
                        displayingStatistics = false;
                        prompt.setValue("");
                        prompt.disable();
                    }
                    return;
                }
                if(code == 8 || code == 46){
                    prompt.delete();
                }else{
                    prompt.addChar(e.getKeyChar());
                }
                if(!escapeMenu){
                    if(e.getKeyCode() == leftKey){
                        currentColumn = Math.max(0, currentColumn - 1);
                    }else if(e.getKeyCode() == spaceKey){
                        mouseDown = true;
                        if(gameWon){
                            mouseDown = false;
                            gameWon = false;
                            managedWin = false;
                            resetBoard();
                        }
                    }else if(e.getKeyCode() == rightKey){
                        currentColumn = Math.min(currentColumn + 1, 6);
                    }
                }else if(!loadingOrSavingGame){
                    if(e.getKeyCode() == spaceKey){
                        mouseDown = true;
                        if(gameWon){
                            mouseDown = false;
                            gameWon = false;
                            managedWin = false;
                            resetBoard();
                        }
                    }
                }
            }
            @Override
            public void keyReleased(KeyEvent e) {}
        });
        circleSize = screenWidth / 9;
        spaceBetween = screenWidth - (circleSize * 7);
        spaceBetween /= 9;
        setBackground(Color.blue);
        setPreferredSize(new Dimension(screenWidth, screenHeight));

        timer = new Timer(DELAY, this);
        timer.start();
    }

    //Draws a circle with the provided color and position (How this entire project started)
    private void drawCircle(Graphics g, Color c, int x, int y){
        g.setColor(c);
        g.fillOval(x, y, circleSize, circleSize);
    }

    //All Overrides are required from the parent class
    @Override
    public void paintComponent(Graphics g) {
        g.setFont(font);
        super.paintComponent(g);
        drawBoard(g);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        repaint();
    }
    private static final long serialVersionUID = 1L;
}
//Used to manage our input prompts
class GInputPrompt{
    String text;
    boolean active = false;
    public GInputPrompt(){ text = ""; }
    public void setActive(){ active = true; }
    public void disable(){ active = true; }
    public void addChar(Character c){        
        if(active)
            text += c;
    }
    public String getValue(){
        return text;
    }
    public void delete(){
        if(active)
            text = text.substring(0, text.length() - 1);
    }
    public void setValue(String value){
        text = value;
    }
}
//Used to manage our buttons (Smooth Stretching)
class GButton{
    float size = 2;
    public GButton(){}
    public void grow(double deltaTime){
        size -= deltaTime;
        if(size < 1.75f)
            size = 1.75f;
    }
    public void shrink(double deltaTime){
        size += deltaTime;
        if(size > 2)
            size = 2;
    }
    public float getSize(){
        return size;
    }
}