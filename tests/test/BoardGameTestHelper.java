package test;

import Model.*;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

public class BoardGameTestHelper { // for accessing private fields

    public static Object getPrivateFieldValue(Object obj, String fieldName) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(obj);
        } catch (Exception e) {
            throw new RuntimeException("Could not access private field for testing: " + fieldName, e);
        }
    }

    public static void setPrivateFieldValue(Object obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            field.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException("Could not set private field for testing: " + fieldName, e);
        }
    }

    @SuppressWarnings("unchecked") // Suppress warning for casting to generic Deck types
    public static <T> Deck<T> getDeck(BoardGame bg, String deckFieldName) {
        return (Deck<T>) getPrivateFieldValue(bg, deckFieldName);
    }

    public static FloodDeck getFloodDeck(BoardGame bg) {
        return (FloodDeck) getPrivateFieldValue(bg, "floodDeck");
    }

    public static TreasureDeck getTreasureDeck(BoardGame bg) {
        return (TreasureDeck) getPrivateFieldValue(bg, "treasureDeck");
    }

    public static WaterMeter getWaterMeter(BoardGame bg) {
        return (WaterMeter) getPrivateFieldValue(bg, "waterMeter");
    }

    public static GameState getGameState(BoardGame bg) {
        return (GameState) getPrivateFieldValue(bg, "gameState");
    }

    public static void setGameState(BoardGame bg, GameState state) {
        setPrivateFieldValue(bg, "gameState", state);
    }

    public static Player[] getPlayers(BoardGame bg) {
        return (Player[]) getPrivateFieldValue(bg, "players");
    }

    public static void setPlayers(BoardGame bg, Player... players) {
        setPrivateFieldValue(bg, "players", players);
        setPrivateFieldValue(bg, "playerCount", players.length);
    }
    public static void setPlayerCount(BoardGame bg, int playerCount) {
        setPrivateFieldValue(bg, "playerCount", playerCount);
    }


    public static int getPlayerCount(BoardGame bg) {
        return (int) getPrivateFieldValue(bg, "playerCount");
    }

    public static int getPlayerTurnId(BoardGame bg) {
        return (int) getPrivateFieldValue(bg, "playerTurnId");
    }

    public static void setPlayerTurnId(BoardGame bg, int id) {
        setPrivateFieldValue(bg, "playerTurnId", id);
    }

    public static int getCurrentPlayerActionsNum(BoardGame bg) {
        return (int) getPrivateFieldValue(bg, "currentPlayerActionsNum");
    }

    public static void setCurrentPlayerActionsNum(BoardGame bg, int num) {
        setPrivateFieldValue(bg, "currentPlayerActionsNum", num);
    }

    public static Zone[][] getBoard(BoardGame bg) {
        return (Zone[][]) getPrivateFieldValue(bg, "board");
    }

    public static void setZone(BoardGame bg, int x, int y, Zone zone) {
        Zone[][] board = getBoard(bg);
        board[x][y] = zone;
    }

    @SuppressWarnings("unchecked")
    public static EnumSet<Artefact> getClaimedArtefacts(BoardGame bg) {
        return (EnumSet<Artefact>) getPrivateFieldValue(bg, "claimedArtefacts");
    }

    public static void setClaimedArtefacts(BoardGame bg, EnumSet<Artefact> artefacts) {
        setPrivateFieldValue(bg, "claimedArtefacts", artefacts);
    }

    @SuppressWarnings("unchecked")
    public static List<Player> getPlayersOnInaccessibleZones(BoardGame bg) {
        return (List<Player>) getPrivateFieldValue(bg, "playersOnInaccessibleZones");
    }

    public static void setPlayersOnInaccessibleZones(BoardGame bg, List<Player> players) {
        setPrivateFieldValue(bg, "playersOnInaccessibleZones", players);
    }

    public static Player getCurrentPlayerRunningFromInaccessibleZone(BoardGame bg) {
        return (Player) getPrivateFieldValue(bg, "currentPlayerRunningFromInaccessibleZone");
    }

    public static void setCurrentPlayerRunningFromInaccessibleZone(BoardGame bg, Player player) {
        setPrivateFieldValue(bg, "currentPlayerRunningFromInaccessibleZone", player);
    }

    public static void setTreasureDrawnThisTurn(BoardGame bg, boolean value) {
        setPrivateFieldValue(bg, "treasureDrawnThisTurn", value);
    }

    public static boolean getTreasureDrawnThisTurn(BoardGame bg) {
        return (boolean) getPrivateFieldValue(bg, "treasureDrawnThisTurn");
    }

    public static void setChosenPlayerByNavigator(BoardGame bg, Player player) {
        setPrivateFieldValue(bg, "chosenPlayerByNavigator", player);
    }

    public static Player getChosenPlayerByNavigator(BoardGame bg) {
        return (Player) getPrivateFieldValue(bg, "chosenPlayerByNavigator");
    }

    public static void setShoreUpsLeft(BoardGame bg, int count) {
        setPrivateFieldValue(bg, "shoreUpsLeft", count);
    }

    public static int getShoreUpsLeft(BoardGame bg) {
        return (int) getPrivateFieldValue(bg, "shoreUpsLeft");
    }
}
