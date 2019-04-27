package bots;

import arena.BattleBotArena;
import arena.BotInfo;
import arena.Bullet;

import java.awt.*;
import java.util.ArrayList;

/**
 * The strategy is to primarily avoid bullets, by staying in one place for most of the time (like Sentry bot),
 * when bullet is heading towards Bot, then it must travel in a opposite direction where it cannot be hit. It
 * will also check the surroundings for a dead bot and to move in the other direction.
 * If there is no danger to be hit by bullet, then Bot can fire its own bullet, towards where closest
 * enemy bot will be within a double radius of the Bot(it increases chance of enemy bot will get hit - since we must conserve bullets)
 */
public class AlexBot extends Bot {

    private static final int NONE = BattleBotArena.STAY;
    private static final int UP = BattleBotArena.FIREUP;
    private static final int DOWN = BattleBotArena.FIREDOWN;
    private static final int LEFT = BattleBotArena.FIRELEFT;
    private static final int RIGHT = BattleBotArena.FIRERIGHT;

    private ArrayList<BotInfo> targets = new ArrayList<>();

    @Override
    public void newRound() {
        targets = new ArrayList<>();
    }

    private Image image = null;

    /**
     * This method is called at every time step to find out what you want your
     * Bot to do. The legal moves are defined in constants in the BattleBotArena
     * class (UP, DOWN, LEFT, RIGHT, FIREUP, FIREDOWN, FIRELEFT, FIRERIGHT, STAY,
     * SEND_MESSAGE). <br><br>
     * <p>
     * The <b>FIRE</b> moves cause a bullet to be created (if there are
     * not too many of your bullets on the screen at the moment). Each bullet
     * moves at speed set by the BULLET_SPEED constant in BattleBotArena. <br><br>
     * <p>
     * The <b>UP</b>, <b>DOWN</b>, <b>LEFT</b>, and <b>RIGHT</b> moves cause the
     * bot to move BOT_SPEED
     * pixels in the requested direction (BOT_SPEED is a constant in
     * BattleBotArena). However, if this would cause a
     * collision with any live or dead bot, or would move the Bot outside the
     * playing area defined by TOP_EDGE, BOTTOM_EDGE, LEFT_EDGE, and RIGHT_EDGE,
     * the move will not be allowed by the Arena.<br><Br>
     * <p>
     * The <b>SEND_MESSAGE</b> move (if allowed by the Arena) will cause a call-back
     * to this Bot's <i>outgoingMessage()</i> method, which should return the message
     * you want the Bot to broadcast. This will be followed with a call to
     * <i>incomingMessage(String)</i> which will be the echo of the broadcast message
     * coming back to the Bot.
     *
     * @param me       A BotInfo object with all publicly available info about this Bot
     * @param shotOK   True iff a FIRE move is currently allowed
     * @param liveBots An array of BotInfo objects for the other Bots currently in play
     * @param deadBots An array of BotInfo objects for the dead Bots littering the arena
     * @param bullets  An array of all Bullet objects currently in play
     * @return A legal move (use the constants defined in BattleBotArena)
     */
    @Override
    public int getMove(BotInfo me, boolean shotOK, BotInfo[] liveBots, BotInfo[] deadBots, Bullet[] bullets) {

        for (int i = 0; i < bullets.length; i++) {
            Bullet current = bullets[i];
            if (bulletComingForUS(me.getX(), me.getY(), current.getX(), current.getY(), current.getYSpeed())) {
                int deadBotQuadrant = closestBotQuadrant(me, deadBots);
                if (deadBotQuadrant == RIGHT) {
                    return BattleBotArena.LEFT;
                }
                return BattleBotArena.RIGHT;
            } else if (bulletComingForUS(me.getY(), me.getX(), current.getY(), current.getX(), current.getXSpeed())) {
                int deadBotQuadrant = closestBotQuadrant(me, deadBots);
                if (deadBotQuadrant == DOWN) {
                    return BattleBotArena.UP;
                }
                return BattleBotArena.DOWN;
            }
        }

        return closestBotQuadrant(me, liveBots);
    }

    /**
     * Used to identify enemy bots close to our bot
     *
     * @param me   Our BotInfo object
     * @param bots Array of other bots
     * @return the closest bot quadrant around our bot
     */
    private int closestBotQuadrant(BotInfo me, BotInfo[] bots) {
        for (int i = 0; i < bots.length; i++) {
            BotInfo current = bots[i];
            if (!targets.contains(current)) {
                int quadrant = determineQuadrant(me.getX(), me.getY(), current.getX(), current.getY());
                if (quadrant != NONE) {
                    targets.add(current);
                    return quadrant;
                }
            }
        }
        return NONE;
    }

    /**
     * Determines if bullet is headed towards the bot
     *
     * @param botX        a double of the location of the bot with x-coordinate
     * @param botY        a double of the location of the bot with y- coordinate
     * @param bulletX     a double of the location of bullet with x-coordinate
     * @param bulletY     a double of the location of bullet with y-coordinate
     * @param bulletSpeed a double of the speed of the bullet
     * @return true if bullet is heading towards us
     */
    private boolean bulletComingForUS(double botX, double botY, double bulletX, double bulletY, double bulletSpeed) {
        return botX <= bulletX && botX + Bot.RADIUS * 2 >= bulletX
                && (botY < bulletY && bulletSpeed < 0
                || botY > bulletY && bulletSpeed > 0
        );
    }

    /**
     * Used to check around our bot for other bots
     *
     * @param botX      a double of the location of the bot with x-coordinate
     * @param botY      a double of the location of the bot with y-coordinate
     * @param otherBotX a double of the location of another bot with x-coordinate
     * @param otherBotY a double of the location of another bot with y-coordinate
     * @return the quadrant which contains other bot
     */
    private int determineQuadrant(double botX, double botY, double otherBotX, double otherBotY) {
        if (botX <= otherBotX && botX + Bot.RADIUS * 2 >= otherBotX) {
            if (botY - Bot.RADIUS * 2 >= otherBotY) {
                return UP;
            }
            if (botY + Bot.RADIUS * 2 >= otherBotY) {
                return DOWN;
            }
        }
        if (botY <= otherBotY && botY + Bot.RADIUS * 2 >= otherBotY) {
            if (botX - Bot.RADIUS * 2 >= otherBotX) {
                return LEFT;
            }
            if (botX + Bot.RADIUS * 2 >= otherBotX) {
                return RIGHT;
            }
        }
        return NONE;
    }

    /**
     * Called when it is time to draw the Bot. Your Bot should be (mostly)
     * within a circle inscribed inside a square with top left coordinates
     * <i>(x,y)</i> and a size of <i>RADIUS * 2</i>. If you are using an image,
     * just put <i>null</i> for the ImageObserver - the arena has some special features
     * to make sure your images are loaded before you will use them.
     *
     * @param g The Graphics object to draw yourself on.
     * @param x The x location of the top left corner of the drawing area
     * @param y The y location of the top left corner of the drawing area
     */
    @Override
    public void draw(Graphics g, int x, int y) {
        if (image != null)
            g.drawImage(image, x, y, Bot.RADIUS * 2, Bot.RADIUS * 2, null);
        else {
            g.setColor(Color.MAGENTA);
            g.fillOval(x, y, Bot.RADIUS * 2, Bot.RADIUS * 2);
        }
    }

    /**
     * This method will only be called once, just after your Bot is created,
     * to set your name permanently for the entire match.
     *
     * @return The Bot's name
     */
    @Override
    public String getName() {
        return "Alex";
    }

    /**
     * This method is called at every time step to find out what team you are
     * currently on. Of course, there can only be one winner, but you can
     * declare and change team allegiances throughout the match if you think
     * anybody will care. Perhaps you can send coded broadcast message or
     * invitation to other Bots to set up a temporary team...
     *
     * @return The Bot's current team name
     */
    @Override
    public String getTeamName() {
        return "AlexTeam";
    }

    /**
     * This is only called after you have requested a SEND_MESSAGE move (see
     * the documentation for <i>getMove()</i>). However if you are already over
     * your messaging cap, this method will not be called. Messages longer than
     * 200 characters will be truncated by the arena before being broadcast, and
     * messages will be further truncated to fit on the message area of the screen.
     *
     * @return The message you want to broadcast
     */
    @Override
    public String outgoingMessage() {
        return null;
    }

    /**
     * This is called whenever the referee or a Bot sends a broadcast message.
     *
     * @param botNum The ID of the Bot who sent the message, or <i>BattleBotArena.SYSTEM_MSG</i> if the message is from the referee.
     * @param msg    The text of the message that was broadcast.
     */
    @Override
    public void incomingMessage(int botNum, String msg) {

    }

    /**
     * This is called by the arena at startup to find out what image names you
     * want it to load for you. All images must be stored in the <i>images</i>
     * folder of the project, but you only have to return their names (not
     * their paths).<br><br>
     * <p>
     * PLEASE resize your images in an image manipulation
     * program. They should be squares of size RADIUS * 2 so that they don't
     * take up much memory.
     *
     * @return An array of image names you want the arena to load.
     */
    @Override
    public String[] imageNames() {
        return new String[0];
    }

    /**
     * Once the arena has loaded your images (see <i>imageNames()</i>), it
     * calls this method to pass you the images it has loaded for you. Store
     * them and use them in your draw method.<br><br>
     * <p>
     * PLEASE resize your images in an
     * image manipulation program. They should be squares of size RADIUS * 2 so
     * that they don't take up much memory.<br><br>
     * <p>
     * CAREFUL: If you got the file names wrong, the image array might be null
     * or contain null elements.
     *
     * @param images The array of images (or null if there was a problem)
     */
    @Override
    public void loadedImages(Image[] images) {

    }
}
