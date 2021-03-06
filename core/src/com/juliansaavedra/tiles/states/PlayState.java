package com.juliansaavedra.tiles.states;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.juliansaavedra.tiles.MainGame;
import com.juliansaavedra.tiles.ui.TextImage;
import com.juliansaavedra.tiles.ui.Tile;

import java.util.Random;

/**
 * Created by 343076 on 27/05/2015.
 */
public class PlayState extends State {

    private final int MAX_FINGERS = 10;

    private Tile[][] tiles;

    private String[] patternPacks;
    private int[] patternRow = new int [16];
    private int[] patternCol = new int [16];

    private boolean patternReady;
    private float patternTimer;
    private float patternTimerMax;
    private int patternIndex;
    private String patternLevel;

    private boolean playTime = false;
    private int playIndex = 0;

    private TextImage splashString;
    private TextImage scoreString;
    private TextImage timeString;
    private int prepareTime;
    private int splash;

    private int score = 0;
    private int level = 0;
    private int milliseconds;
    private int seconds;
    private String difficulty;

    private int maxLevel;
    private int minLevel;
    private int bonus;

    private Tile letTileFinish;
    private Tile rightTile;
    private Tile lastTile;
    private boolean hitWrongTile;
    private boolean timeUp;
    private int gameOverTicks;

    private Texture background;
    private int currentBGX = 1920;
    private float bgTimer = 1;

    public PlayState(GSM gsm,String difficulty){
        super(gsm);

        splashString = new TextImage("READY", MainGame.WIDTH/2, MainGame.HEIGHT/2,1);
        scoreString = new TextImage(score+"", MainGame.WIDTH/2, MainGame.HEIGHT/2 + 300,1);
        splashString.hide(true);

        this.difficulty = difficulty;
        setDifficulty(difficulty);

        if(MainGame.pref.getPackPref()){
            loadSoundPack("pack0");
        }
        else{
            loadSoundPack("pack1");
        }

        createTiles();

        Random rand = new Random();
        level = rand.nextInt(maxLevel - minLevel) + minLevel;
        initPattern(level);

        timeString = new TextImage(seconds+"", MainGame.WIDTH/2, MainGame.HEIGHT/2 - 300,1);
        background = MainGame.res.getTexture("playBG");

    }

    public void setDifficulty(String difficulty){
        milliseconds = 60;
        if(difficulty.equals("EASY")){
            bonus = 0;
            minLevel = 0;
            maxLevel = 47;
            patternTimerMax = 8f;
            seconds = 8;
        }
        else if(difficulty.equals("NORMAL")){
            bonus = 0;
            minLevel = 0;
            maxLevel = 47;
            patternTimerMax = 5f;
            seconds = 5;
        }
        else if(difficulty.equals("HARD")){
            bonus = 1;
            minLevel = 0;
            maxLevel = 47;
            patternTimerMax = 4f;
            seconds = 4;
        }
        else if(difficulty.equals("VERY HARD")){
            bonus = 2;
            minLevel = 48;
            maxLevel = 80;
            patternTimerMax = 4f;
            seconds = 4;
        }
        else if(difficulty.equals("INSANE")){
            bonus = 3;
            minLevel = 48;
            maxLevel = 80;
            patternTimerMax = 3f;
            seconds = 3;
        }
        else{
            patternTimerMax = 19f;
        }
    }

    public void initPattern(int previewPattern) {
        getPatterns(previewPattern);
        setPattern(previewPattern);
        patternReady = true;
        patternIndex = 0;
        patternTimer = 0;

        splashString.update(patternLevel, MainGame.WIDTH / 2, MainGame.HEIGHT / 2);
        splashString.hide(false);
        prepareTime = 250;
    }

    public void loadSoundPack(String name) {
        int numSounds = 16; // 16 maximum
        for (int i = 0; i < numSounds; i++) {
            int beatIndex = i + 1;
            String fileName = name + "/sound" + beatIndex + ".wav";
            MainGame.res.loadSound(fileName, "" + i);
        }
    }

    public void createTiles(){
        tiles = new Tile[4][4];
        int tileSize = MainGame.WIDTH / tiles[0].length;
        float boardOffset = (MainGame.HEIGHT - (tileSize * tiles.length)) / 2;

        int soundNum = 0;
        for(int row = 0 ; row < tiles.length ; row ++){
            for (int col = 0; col < tiles[0].length; col++){
                tiles[row][col] = new Tile( col * tileSize + tileSize / 2, row * tileSize + boardOffset + tileSize / 2, tileSize, tileSize, soundNum);
                Random random = new Random();
                int animation = random.nextInt(99) + 1;
                tiles[row][col].setTimer(-(animation) * 0.01f);
                //tiles[row][col].setTimer((-(tiles.length - row) - col) * 0.25f);
                soundNum++;
            }
        }
    }

    /*Parse the music.txt file to get all the patterns*/
    public void getPatterns(int pack){
        FileHandle file = Gdx.files.internal("patterns.txt");
        String text = file.readString();
        patternPacks = text.split(";");
        //System.out.println(patternPacks[pack]);
    }

    /*Parse the String obtained from getPatterns method to get individual row and col index*/
    public void setPattern(int pack) {
        boolean alternate = false;
        String[] patternSplitter = patternPacks[pack].split(",");
        patternLevel = patternSplitter[0].trim();
        for(int i = 1 ; i <= 32 ; i ++){
            int index = ((i-1)/2);
            if(!alternate){
                patternRow[index] = Integer.parseInt(patternSplitter[i]);
                //System.out.println("R: "+patternRow[index]);
                alternate = true;
            }
            else if(alternate){
                patternCol[index] = Integer.parseInt(patternSplitter[i]);
                //System.out.println("C: "+patternCol[index]);
                alternate = false;
            }
        }
    }

    public void playPattern() {
        tiles[patternRow[patternIndex]][patternCol[patternIndex]].playSound();
        if(patternTimer > patternTimerMax) {
            if (patternIndex < patternRow.length - 1) {
                patternTimer = 0;
                patternIndex++;
                if(patternIndex == patternRow.length - 1){
                    letTileFinish = tiles[patternRow[patternIndex]][patternCol[patternIndex]];
                }
            } else {
                patternReady = false;
            }
        }
    }

    public void handleInput() {

        if (Gdx.input.isTouched()) {
            mouse.set(Gdx.input.getX(), Gdx.input.getY(), 0);
            cam.unproject(mouse);
            for (int row = 0; row < tiles.length; row++) {
                for (int col = 0; col < tiles[0].length; col++) {
                    if (tiles[row][col].contains(mouse.x, mouse.y)) {
                        if (playIndex < 16 && playTime) {
                            tiles[row][col].playSound();
                            lastTile = tiles[row][col];
                            if (tiles[row][col] == tiles[patternRow[playIndex]][patternCol[playIndex]]) {
                                tiles[row][col].cdToggle(true);
                                if (!tiles[row][col].isEmpty()) {

                                    playIndex++;

                                    if (playIndex == 16) {
                                        letTileFinish = tiles[row][col];
                                        playTime = false;
                                        setDifficulty(difficulty);
                                        splashString.update("COMPLETE!", MainGame.WIDTH / 2, MainGame.HEIGHT / 2);
                                        splashString.hide(false);
                                        prepareTime = 350;
                                    }

                                }
                                score += tiles[row][col].getPoint() + bonus;
                            }
                            else {
                                if (!tiles[row][col].justSelected()) {
                                    playTime = false;
                                    hitWrongTile = true;
                                    gameOverTicks = 60;
                                    rightTile = tiles[patternRow[playIndex]][patternCol[playIndex]];
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void update(float dt) {

        if(hitWrongTile){
            lastTile.playSound();
            rightTile.showRight();
            gameOverTicks--;
            if(gameOverTicks <= 0){
                gsm.set(new GameOverState(gsm,score,difficulty));
            }
        }

        if(timeUp){
            splashString.update("TIME'S UP", MainGame.WIDTH/2, MainGame.HEIGHT/2);
            splashString.hide(false);
            gameOverTicks--;
            if(gameOverTicks <= 0){
                gsm.set(new GameOverState(gsm,score,difficulty));
            }
        }

        if(playTime) {
            if (seconds < 0) {
                playTime = false;
                timeUp = true;
                gameOverTicks = 60;
            } else {
                if (milliseconds >= 0) {
                    milliseconds--;
                } else {
                    seconds--;
                    milliseconds = 60;
                }
            }
        }

        if(prepareTime < 150){ // Pattern initialized
            if(patternReady){
                playPattern();
                patternTimer++;
            }
            else{
                if(prepareTime > 50){
                    letTileFinish.playSound();
                    splashString.update("READY", MainGame.WIDTH/2, MainGame.HEIGHT/2);
                    splashString.hide(false);
                    prepareTime--;
                }
                else if(prepareTime <= 50 && prepareTime > 0){
                    if(!hitWrongTile) {
                        prepareTime--;
                        splashString.update("GO!", MainGame.WIDTH / 2, MainGame.HEIGHT / 2);
                        splashString.hide(false);
                        playTime = true;
                    }
                    else{
                        prepareTime = 0;
                    }
                }
                else if(prepareTime <= 0){
                    if(splash > 0){
                        splash--;
                    }else if(splash == 0){
                        splash = -1;
                        splashString.hide(true);
                    }
                }
                handleInput();
            }
        }
        else if(prepareTime > 250 && prepareTime <= 350){ // A level is completed
            prepareTime--;
            if(prepareTime > 260){
                letTileFinish.playSound();
            }
            if(prepareTime == 260){
                Random rand = new Random();
                level = rand.nextInt(maxLevel - minLevel) + minLevel;
                splashString.hide(true);
                splash = 0; // Reset from -1 to 0 to avoid Splash Text showing
                playIndex = 0; // Reset the pattern index to 0
                initPattern(level);
            }
        }
        else if (prepareTime > 149 && prepareTime <= 250){ // Timer for pattern initialization/showing level
            prepareTime--;
            if(prepareTime == 160){
                splashString.hide(true);
            }
        }

        for (int row = 0; row < tiles.length; row++) {
            for (int col = 0; col < tiles[0].length; col++) {
                tiles[row][col].update(dt);
            }
        }

        scoreString.update(score + "", MainGame.WIDTH/2, MainGame.HEIGHT/2 + 300);
        if(seconds >=0) {
            timeString.update(seconds + "", MainGame.WIDTH / 2, MainGame.HEIGHT / 2 - 300);
        }

        if(bgTimer > 0){
            bgTimer--;
        }
        else if(bgTimer <= 0){
            bgTimer = 1;
            currentBGX--;
            if(currentBGX <= 0){
                currentBGX = 2132;
            }
        }
    }

    public void render(SpriteBatch sb) {

        sb.setProjectionMatrix((cam.combined));
        sb.begin();

        sb.draw(background, currentBGX - 2132, 0);
        sb.draw(background,currentBGX,0);

        for(int row = 0 ; row < tiles.length ; row ++){
            for (int col = 0; col < tiles[0].length; col++){
                tiles[row][col].render(sb);
            }
        }
        splashString.render(sb);
        scoreString.render(sb);
        timeString.render(sb);
        sb.end();

    }

}
