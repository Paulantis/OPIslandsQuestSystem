




















/*Was hast du denn hier verloren?





Bist du dir sicher, dass du nichts Besseres zutun hast, als mich zu nerven?






Hat man dir nicht gesagt, dass man nicht in andern Leutens Code herumschnüffeln soll?

 */























package com.quests.OPIslandsQuestSystem;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.advancement.Advancement;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.*;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.*;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.*;
import org.bukkit.inventory.*;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public final class Quest extends JavaPlugin implements Listener {
    private static final String DATABASE_URL = "jdbc:sqlite:quests.db";

    @Override
    public void onEnable() {
        DatabaseManager.initializeDatabase();
        connect();
        setupDatabase();
        getServer().getPluginManager().registerEvents(this, this);
        new BukkitRunnable() {
            @Override
            public void run() {
                addQuests();
            }
        }.runTaskAsynchronously(this);
        setupRestrictedZones();
        setupSellOffers();
        setupBuyOffers();
    }

    @Override
    public void onDisable() {
        DatabaseManager.close();
        getLogger().info("Database connection closed.");
    }

    public void connect() {
        var url = "jdbc:sqlite:quests.db";

        try (var conn = DatabaseManager.getConnection()) {
            if (conn != null) {
                getLogger().info("Connection to SQLite has been established.");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA busy_timeout = 30000;");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DatabaseManager.getConnection();
        connection.createStatement().execute("PRAGMA busy_timeout = 30000;");
        return connection;
    }

    private void setupDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            String questsTable = "CREATE TABLE IF NOT EXISTS quests ("
                    + " id INTEGER PRIMARY KEY,"
                    + " name TEXT,"
                    + " description TEXT,"
                    + " reward TEXT,"
                    + " displayreward TEXT,"
                    + " layer TEXT NOT NULL,"
                    + " dimension TEXT NOT NULL,"
                    + " difficulty TEXT NOT NULL,"
                    + " nextquests TEXT,"
                    + " task_description TEXT,"
                    + " task TEXT,"
                    + " required_quests TEXT DEFAULT NULL,"
                    + " required_value TEXT,"
                    + " displayitem TEXT NOT NULL,"
                    + " taskmode TEXT NOT NULL"
                    + ");";


            String progressTable = "CREATE TABLE IF NOT EXISTS playerprogress ("
                    + " player_id TEXT NOT NULL,"
                    + " quest_id INTEGER NOT NULL,"
                    + " isQuestforPlayerAvailable BOOLEAN NOT NULL DEFAULT 0,"
                    + " completed BOOLEAN NOT NULL,"
                    + " progress INTEGER DEFAULT 0,"
                    + " tasks TEXT,"
                    + " PRIMARY KEY (player_id, quest_id),"
                    + " FOREIGN KEY (quest_id) REFERENCES quests(id)"
                    + ");";

            String playersTable = "CREATE TABLE IF NOT EXISTS players ("
                    + " uuid TEXT PRIMARY KEY,"
                    + " name TEXT NOT NULL,"
                    + " join_date TEXT NOT NULL"
                    + ");";

            String shopsTable = "CREATE TABLE IF NOT EXISTS shops ("
                    + " id INTEGER PRIMARY KEY,"
                    + " uuid TEXT NOT NULL,"
                    + " item TEXT NOT NULL,"
                    + " available BOOLEAN NOT NULL,"
                    + " UNIQUE(uuid, item)"
                    + ");";

            stmt.execute(questsTable);
            stmt.execute(progressTable);
            stmt.execute(playersTable);
            stmt.execute(shopsTable);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void addQuests() {
        String sql = "INSERT OR IGNORE INTO quests(id, name, description, reward, displayreward, layer, dimension, difficulty, nextquests, task_description, task, required_quests, required_value, displayitem, taskmode) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        Gson gson = new Gson();

        Integer[] ids = {1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55};
        String[] names = {
                "Gestrandet",
                "Starter Equipment",
                "Der Wald",
                "Die Mine",
                "Zombies!",
                "Anfänger Gärtner",
                "Die geheime Basis",
                "Auf in den Kampf",
                "Apfelkuchen?",
                "Bedrock?",
                "Anfänger Farmer",
                "Noch mehr Zombies!",
                "Angler???",
                "Angler!!!",
                "Professioneller Koch",
                "Minecart Fan",
                "Tiefere Ebenen",
                "Zeit zum kochen",
                "Das volle Sortiment",
                "Die zweite Mine?",
                "Unfair!",
                "Fortgeschrittener Gärtner",
                "Das Lager",
                "Fortschrittlich!",
                "Aua, das tut ja weh",
                "Bedrock again?",
                "Fortgeschrittener Farmer",
                "Besseres Aim?",
                "Uff, schwer",
                "Magier I",
                "Professioneller Koch",
                "Skelett Bezwinger",
                "Magier II",
                "Uii, wie das glänzt",
                "Was ist da denn?",
                "Kupfer, juhu",
                "Wie tief ist diese Mine eigentlich?",
                "Zeit zum bauen",
                "Lieblingsholz",
                "Die dritte Mine?",
                "Monster, Hilfe!",
                "Garten Experte",
                "Der Bunker",
                "Diamanten!",
                "Whiter?",
                "Ganz unten?",
                "Profi Farmer",
                "Hexen?",
                "Reich!",
                "Magier III",
                "Experten Koch",
                "Monster Bezwinger",
                "Becon I",
                "Becon II 18",
                "Nether"
        };
        String[] descriptions = {
                "Finde das erste Tagebuch",
                "Besorge dir alle Stein Tools",
                "Fälle 50 Eichen und 50 Birken Stämme",
                "Baue 30x Kohle Erz ab",
                "Töte 50 Zombies",
                "Platziere 5x Eichen Setzling und 5x Birken Setzling",
                "Finde das Kohle Versteck",
                "Crafte 25 Stein Schwerter und 25 Holz Schwerter",
                "Habe 25 Äpfel im Inventar",
                "Stehe auf Bedrock",
                "Baue 100x Rote Beete an",
                "Töte 100 Zombies",
                "Angle 10 Kabeljau",
                "Angle 50 Lachs",
                "Brate 32 Kabeljau",
                "Habe alle Minecart Arten im Inventar",
                "Erledige alle Kohle Quests",
                "Besorge dir einen Ofen",
                "Fälle von jeder Baum Art 64 Stämme",
                "Baue 50x Eisen Erz ab",
                "Töte 100 Skelette",
                "Habe 128 Zuckerrohr im Inventar",
                "Finde die Wegkreuzung",
                "Crafte Full Iron, ein Schild und alle Eisen Tools",
                "Habe 64 Kaktuse im Inventar",
                "Stehe auf Höhe 5",
                "Baue 300 Weizen ab",
                "Töte 5 Skelette mit einem Bogen",
                "Crafte einen Amboss",
                "Verzaubere deine Eisen Rüstung und 5 Bücher",
                "Mache 20 Rote Beete Suppen",
                "Töte 200 Skelette",
                "Habe ein Effizienz 5, Sharpness 5 oder Smite 5 Buch",
                "Kaufe eine Diamantbeinschutz",
                "Crafte ein Fernglas",
                "Crafte 32 Kupfer Blöcke",
                "Erledige alle Eisen Quests",
                "Crafte einen Schleifstein, Schmiedetisch, Bücherregal, Laterne, Lagerfeuer, Kreissäge",
                "Fälle von einer Baum Art 400 Stämme",
                "Baue 50x Gold Erz ab",
                "Sterbe an einem Gegner",
                "Farme 25 Melonen, 25 Kürbisse und 500 Rote Beete",
                "Finde das verlassene Gold Lager",
                "Crafte ein volle Diamant Rüstung!",
                "Töte einen Whiter",
                "Stehe auf Höhe -32",
                "Crafte 1000 Brote",
                "Töte 25 Hexen",
                "Crafte einen Diamant Block",
                "Verzaubere deine Dia Rüstung und deine Dia Tools",
                "Brate 200 Lachs",
                "Töte 50 Zombies, Skelette, Hexen, Slimes, Boggeds, Spinnen, Höhlenspinnen",
                "Power einen Becon",
                "Powere einen Full Becon",
                "Betrete den Nether, indem du auf den Reincorced Deepslate am Portal klickst"
        };
        String[] rewards = {
                "HEART_OF_THE_SEA,1,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,3,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,3,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,3,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "OAK_LEAVES,64",
                "HEART_OF_THE_SEA,1,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "IRON_SWORD,1",
                "HEART_OF_THE_SEA,3,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,1,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,3,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,5,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,5,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,3,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,1,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "openArea,-139,44,13,-143,44,15",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,10,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,6,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,8,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,5,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "DIAMOND_SWORD,1",
                "SUSPICIOUS_STEW,1,{\"name\":\"Kaktussalat\",\"lore\":[\"Nom, nom tut das weh.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,10,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,5,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,7,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,12,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,3,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,1,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,2,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,6,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "openArea,-149,5,1,-146,5,3",
                "HEART_OF_THE_SEA,8,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,18,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,10,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,5,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,30,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,6,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "NETHERITE_UPGRADE_SMITHING_TEMPLATE,1",
                "HEART_OF_THE_SEA,30,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,6,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,40,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,15,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "ANCIENT_DEBRIS,2",
                "HEART_OF_THE_SEA,10,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,30,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,25,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,15,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "HEART_OF_THE_SEA,100,{\"name\":\"Perle\",\"lore\":[\"Kann zum traden benutzt werden.\",\"Belohnungen von Quests.\"]}",
                "GOLDEN_CARROT,64"
        };
        String[] displayRewards = {
                "1x Perle",
                "2x Perlen",
                "3x Perlen",
                "3x Perlen",
                "3x Perlen",
                "64x Eichen Blätter",
                "1x Perle",
                "1x Eisenschwert",
                "3x Perlen",
                "1x Perlen",
                "3x Perlen",
                "5x Perlen",
                "2x Perlen",
                "5x Perlen",
                "3x Perlen",
                "1x Perle",
                "Eisen Mine freischalten",
                "2x Perlen",
                "10x Perlen",
                "6x Perlen",
                "8x Perlen",
                "5x Perlen",
                "2x Perlen",
                "1x Diamant Schwert",
                "1x Kaktus Salat",
                "2x Perlen",
                "10x Perlen",
                "2x Perlen",
                "2x Perlen",
                "5x Perlen",
                "7x Perlen",
                "12x Perlen",
                "3x Perlen",
                "1x Perle",
                "2x Perlen",
                "6x Perlen",
                "Eisen Mine freuschalten",
                "8x Perlen",
                "18x Perlen",
                "10x Perlen",
                "5x Perlen",
                "30x Perlen",
                "6x Perle",
                "1x Upgrade Template",
                "30x Perlen",
                "6x Perlen",
                "40x Perlen",
                "15x Perlen",
                "2x Antiker Schrott",
                "10x Perlen",
                "30x Perlen",
                "25x Perlen",
                "15x Perlen",
                "100x Perlen",
                "64x Goldene Karotten"
        };
        String[] layers = {
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Kohle",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Eisen",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
                "Gold",
        };
        String[] dimensions = {
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Oberwelt",
                "Nether",
        };
        String[] difficulties = {
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Normal",
                "Einfach",
                "Einfach",
                "Einfach",
                "Normal",
                "Einfach",
                "Einfach",
                "Einfach",
                "Normal",
                "Einfach",
                "Normal",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Normal",
                "Normal",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Einfach",
                "Schwer",
                "Einfach",
                "Einfach",
                "Normal",
                "Einfach",
                "Normal",
                "Schwer",
                "Einfach",
                "Schwer",
                "Schwer",
                "Normal",
                "Normal",
                "Normal",
                "Schwer",
                "Schwer",
                "???",
                "Normal",
        };
        String[] nextQuests = {
                "2, 3, 13",
                "4",
                "6",
                "5, 16",
                "7, 8, 10",
                "9, 11",
                "12",
                "",
                "",
                "",
                "",
                "",
                "14",
                "15",
                "",
                "",
                "18, 19, 20, 21",
                "",
                "22",
                "",
                "23, 24, 28",
                "25",
                "35, 36",
                "26, 29, 30",
                "27",
                "",
                "31",
                "32",
                "",
                "33, 34",
                "",
                "",
                "",
                "",
                "",
                "",
                "38, 39, 40, 42, 44",
                "",
                "",
                "41, 43, 48, 52",
                "",
                "47",
                "45, 46",
                "49, 50",
                "53",
                "",
                "51",
                "",
                "",
                "",
                "",
                "",
                "54",
                "",
                "",
        };
        List<List<Map<String, Object>>> allTaskDescriptions = Arrays.asList(
                List.of(
                        Map.of("text", "Tagebuch: ")
                ),
                List.of(
                        Map.of( "text", "Steinschwert: "),
                        Map.of( "text", "Steinspitzhacke: "),
                        Map.of( "text", "Steinaxt: "),
                        Map.of( "text", "Steinschaufel: "),
                        Map.of( "text", "Steinharke: ")
                ),
                List.of(
                        Map.of("text", "Eichenstamm: "),
                        Map.of("text", "Birkenstamm: ")
                ),
                List.of(
                        Map.of("text", "Steinkohle: ")
                ),
                List.of(
                        Map.of("text", "Zombies: ")
                ),
                List.of(
                        Map.of("text", "Eichensätzlinge: "),
                        Map.of("text", "Birkensätzlinge: ")
                ),
                List.of(
                        Map.of("text", "Erreiche das Kohle Versteck: ")
                ),
                List.of(
                        Map.of("text", "Holzschwerter: "),
                        Map.of("text", "Steinschwerter: ")
                ),
                List.of(
                        Map.of("text", "Äpfel: ")
                ),
                List.of(
                        Map.of("text", "Stehe auf Bedrock: ")
                ),
                List.of(
                        Map.of("text", "Rote Bete: ")
                ),
                List.of(
                        Map.of("text", "Zombies: ")
                ),
                List.of(
                        Map.of("text", "Kabeljau: ")
                ),
                List.of(
                        Map.of("text", "Lachs: ")
                ),
                List.of(
                        Map.of( "text", "Gebratener Kabeljau: ")
                ),
                List.of(
                        Map.of("text", "Lore: "),
                        Map.of("text", "TNT-Lore: "),
                        Map.of("text", "Trichterlore: "),
                        Map.of("text", "Güterlore: "),
                        Map.of("text", "Ofenlore: ")
                ),
                List.of(
                        Map.of("text", "Erledige alle Kohle Quests: ")
                ),
                List.of(
                        Map.of("text", "Ofen: ")
                ),
                List.of(
                        Map.of("text", "Eiche: "),
                        Map.of("text", "Birke: "),
                        Map.of("text", "Schwarzeiche: "),
                        Map.of("text", "Dschungel: "),
                        Map.of("text", "Akazie: "),
                        Map.of("text", "Fichte: "),
                        Map.of("text", "Mangrove: "),
                        Map.of("text", "Kirschblüte: ")
                ),
                List.of(
                        Map.of("text", "Eisenerz: ")
                ),
                List.of(
                        Map.of("text", "Skelete: ")
                ),
                List.of(
                        Map.of("text", "Zuckerrohr: ")
                ),
                List.of(
                        Map.of("text", "Finde das Eisenlage")
                ),
                List.of(
                        Map.of("text", "Eisenhelm: "),
                        Map.of("text", "Eisenharnisch: "),
                        Map.of("text", "Eisenbeinschutz: "),
                        Map.of("text", "Eisenstiefel: "),
                        Map.of("text", "Eisenschwert: "),
                        Map.of("text", "Eisenspitzhacke: "),
                        Map.of("text", "Eisenaxt: "),
                        Map.of("text", "Eisenschaufel: "),
                        Map.of("text", "Eisenharke: "),
                        Map.of("text", "Schild: ")
                ),
                List.of(
                        Map.of("text", "Kaktenen: ")
                ),
                List.of(
                        Map.of("text", "Stehe auf dem 2. Bedrock: ")
                ),
                List.of(
                        Map.of("text", "Weizen: ")
                ),
                List.of(
                        Map.of("text", "Skelette: ")
                ),
                List.of(
                        Map.of("text", "Amboss: ")
                ),
                List.of(
                        Map.of("text", "Bücher: "),
                        Map.of("text", "Eisenhelm: "),
                        Map.of("text", "Eisenharnisch: "),
                        Map.of("text", "Eisenbeinschutz: "),
                        Map.of("text", "Eisenstiefel: ")
                ),
                List.of(
                        Map.of("text", "Rote Bete Suppen: ")
                ),
                List.of(
                        Map.of("text", "Skelette")
                ),
                List.of(
                        Map.of("text", "Habe ein Efi, Shp oder Smt 5 Buch im Inventar")
                ),
                List.of(
                        Map.of("text", "Diamandbeinschutz: ")
                ),
                List.of(
                        Map.of("text", "Fernglas: ")
                ),
                List.of(
                        Map.of("text", "Kupferblöcke: ")
                ),
                List.of(
                        Map.of("text", "Erledige alle Eisen Quests: ")
                ),
                List.of(
                        Map.of("text", "Schleifstein: "),
                        Map.of("text", "Schmiedetisch: "),
                        Map.of("text", "Bücherregal: "),
                        Map.of("text", "Laterne: "),
                        Map.of("text", "Lagerfeuer: "),
                        Map.of("text", "Steinsäge: ")
                ),
                List.of(
                        Map.of("text", "Eiche: "),
                        Map.of("text", "Birke: "),
                        Map.of("text", "Schwarzeiche: "),
                        Map.of("text", "Dschungel: "),
                        Map.of("text", "Akazie: "),
                        Map.of("text", "Fichte: "),
                        Map.of("text", "Mangrove: "),
                        Map.of("text", "Kirschblüte: ")
                ),
                List.of(
                        Map.of("text", "Golderz: ")
                ),
                List.of(
                        Map.of("text", "Sterbe an Spinne: ")
                ),
                List.of(
                        Map.of("text", "Melonen: "),
                        Map.of("text", "Kürbisse: "),
                        Map.of("text", "Rote Bete: ")
                ),
                List.of(
                        Map.of("text", "Finde den Goldbunker:")
                ),
                List.of(
                        Map.of("text", "Diamanthelm: "),
                        Map.of("text", "Diamantharnisch: "),
                        Map.of("text", "Diamantbeinschutz: "),
                        Map.of("text", "Diamantstiefel: ")
                ),
                List.of(
                        Map.of("text", "Wither: ")
                ),
                List.of(
                        Map.of("text", "Stehe auf dem 3. Bedrock: ")
                ),
                List.of(
                        Map.of("text", "Brote: ")
                ),
                List.of(
                        Map.of("text", "Hexen: ")
                ),
                List.of(
                        Map.of("text", "Diamantblock: ")
                ),
                List.of(
                        Map.of("text", "Diamanthelm: "),
                        Map.of("text", "Diamantharnisch: "),
                        Map.of("text", "Diamantbeinschutz: "),
                        Map.of("text", "Diamantstiefel: "),
                        Map.of("text", "Diamantschwert: "),
                        Map.of("text", "Diamantspitzhacke: "),
                        Map.of("text", "Diamantaxt: "),
                        Map.of("text", "Diamantschaufel: "),
                        Map.of("text", "Diamantharke: ")
                ),
                List.of(
                        Map.of("text", "Gebratener Lachs: ")
                ),
                List.of(
                        Map.of("text", "Zombies: "),
                        Map.of("text", "Skelette: "),
                        Map.of("text", "Hexen: "),
                        Map.of("text", "Schleime: "),
                        Map.of("text", "Sumpfskelette: "),
                        Map.of("text", "Spinne: "),
                        Map.of("text", "Höhlenspinne: ")
                ),
                List.of(
                        Map.of("text", "Power einen Beacon: ")
                ),
                List.of(
                        Map.of("text", "Power einen Full Beacon: ")
                ),
                List.of(
                        Map.of("text", "Nether: ")
                )
        );

        List<List<Map<String, Object>>> allTasks = Arrays.asList(
                List.of(Map.of("type", "openInterface", "x", -127, "y", 72, "z", -15, "amount", 1)),
                List.of(
                        Map.of("type", "getItem", "amount", 1, "target", "STONE_SWORD"),
                        Map.of("type", "getItem", "amount", 1, "target", "STONE_PICKAXE"),
                        Map.of("type", "getItem", "amount", 1, "target", "STONE_AXE"),
                        Map.of("type", "getItem", "amount", 1, "target", "STONE_SHOVEL"),
                        Map.of("type", "getItem", "amount", 1, "target", "STONE_HOE")
                ),
                List.of(
                        Map.of("type", "mineBlock", "amount", 50, "target", "OAK_LOG"),
                        Map.of("type", "mineBlock", "amount", 50, "target", "BIRCH_LOG")
                ),
                List.of(Map.of("type", "mineBlock", "amount", 30, "target", "COAL_ORE")),
                List.of(Map.of("type", "killEntity", "amount", 50, "target", "ZOMBIE")),
                List.of(
                        Map.of("type", "placeBlock", "amount", 5, "target", "OAK_SAPLING"),
                        Map.of("type", "placeBlock", "amount", 5, "target", "BIRCH_SAPLING")
                ),
                List.of(Map.of("type", "reachCoordinates", "x", -125, "y", 55, "z", 29, "amount", 1)),
                List.of(
                        Map.of("type", "craftItem", "amount", 25, "target", "WOODEN_SWORD"),
                        Map.of("type", "craftItem", "amount", 25, "target", "STONE_SWORD")
                ),
                List.of(Map.of("type", "getItem", "amount", 25, "target", "APPLE")),
                List.of(Map.of("type", "standOnBlock", "height", 45, "amount", 1, "block", "BEDROCK")),
                List.of(Map.of("type", "placeBlock", "amount", 100, "target", "BEETROOTS")),
                List.of(Map.of("type", "killEntity", "amount", 100, "target", "ZOMBIE")),
                List.of(
                        Map.of("type", "fishItem", "amount", 10, "target", "COD")
                ),
                List.of(
                        Map.of("type", "fishItem", "amount", 50, "target", "SALMON")
                ),
                List.of(Map.of("type", "smeltItem", "amount", 32, "target", "COOKED_COD")),
                List.of(
                        Map.of("type", "getItem", "amount", 1, "target", "MINECART"),
                        Map.of("type", "getItem", "amount", 1, "target", "TNT_MINECART"),
                        Map.of("type", "getItem", "amount", 1, "target", "HOPPER_MINECART"),
                        Map.of("type", "getItem", "amount", 1, "target", "CHEST_MINECART"),
                        Map.of("type", "getItem", "amount", 1, "target", "FURNACE_MINECART")
                ),
                List.of(Map.of()),
                List.of(Map.of("type", "getItem", "amount", 1, "target", "FURNACE")),
                List.of(
                        Map.of("type", "mineBlock", "amount", 64, "target", "OAK_LOG"),
                        Map.of("type", "mineBlock", "amount", 64, "target", "BIRCH_LOG"),
                        Map.of("type", "mineBlock", "amount", 64, "target", "DARK_OAK_LOG"),
                        Map.of("type", "mineBlock", "amount", 64, "target", "JUNGLE_LOG"),
                        Map.of("type", "mineBlock", "amount", 64, "target", "ACACIA_LOG"),
                        Map.of("type", "mineBlock", "amount", 64, "target", "SPRUCE_LOG"),
                        Map.of("type", "mineBlock", "amount", 64, "target", "MANGROVE_LOG"),
                        Map.of("type", "mineBlock", "amount", 64, "target", "CHERRY_LOG")
                ),
                List.of(Map.of("type", "mineBlock", "amount", 50, "target", "IRON_ORE")),
                List.of(Map.of("type", "killEntity", "amount", 100, "target", "SKELETON")),
                List.of(Map.of("type", "getItem", "amount", 128, "target", "SUGAR_CANE")),
                List.of(Map.of("type", "reachCoordinates", "x", -165, "y", 20, "z", 25, "amount", 1)),
                List.of(
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_HELMET"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_CHESTPLATE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_LEGGINGS"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_BOOTS"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_SWORD"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_PICKAXE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_AXE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_SHOVEL"),
                        Map.of("type", "craftItem", "amount", 1, "target", "IRON_HOE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "SHIELD")
                ),
                List.of(Map.of("type", "getItem", "amount", 64, "target", "CACTUS")),
                List.of(Map.of("type", "standOnBlock", "height", 6, "amount", 1, "block", "BEDROCK")),
                List.of(Map.of("type", "mineBlock", "amount", 300, "target", "WHEAT")),
                List.of(Map.of("type", "killEntity", "amount", 5, "target", "SKELETON", "weapon", "BOW")),
                List.of(Map.of("type", "craftItem", "amount", 1, "target", "ANVIL")),
                List.of(
                        Map.of("type", "onEnchant", "amount", 5, "item", "BOOK", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "IRON_HELMET", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "IRON_CHESTPLATE", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "IRON_LEGGINGS", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "IRON_BOOTS", "target", "DOESNTMATTER")
                ),
                List.of(Map.of("type", "craftItem", "amount", 20, "target", "BEETROOT_SOUP")),
                List.of(Map.of("type", "killEntity", "amount", 200, "target", "SKELETON")),
                List.of(
                        Map.of(
                                "type", "getItem",
                                "target", "ENCHANTED_BOOK",
                                "amount", 1,
                                "meta", Map.of(
                                        "enchantment", "SMITE",
                                        "level", 5
                                )
                        ),
                        Map.of(
                                "type", "getItem",
                                "target", "ENCHANTED_BOOK",
                                "amount", 1,
                                "meta", Map.of(
                                        "enchantment", "SHARPNESS",
                                        "level", 5
                                )
                        ),
                        Map.of(
                                "type", "getItem",
                                "target", "ENCHANTED_BOOK",
                                "amount", 1,
                                "meta", Map.of(
                                        "enchantment", "EFFICIENCY",
                                        "level", 5
                                )
                        )
                ),
                List.of(Map.of("type", "getItem", "amount", 1, "target", "DIAMOND_LEGGINGS")),
                List.of(Map.of("type", "craftItem", "amount", 1, "target", "SPYGLASS")),
                List.of(Map.of("type", "craftItem", "amount", 32, "target", "COPPER_BLOCK")),
                List.of(Map.of()),
                List.of(
                        Map.of("type", "craftItem", "amount", 1, "target", "GRINDSTONE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "SMITHING_TABLE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "BOOKSHELF"),
                        Map.of("type", "craftItem", "amount", 1, "target", "LANTERN"),
                        Map.of("type", "craftItem", "amount", 1, "target", "CAMPFIRE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "STONECUTTER")
                ),
                List.of(
                        Map.of("type", "mineBlock", "amount", 400, "target", "OAK_LOG"),
                        Map.of("type", "mineBlock", "amount", 400, "target", "BIRCH_LOG"),
                        Map.of("type", "mineBlock", "amount", 400, "target", "DARK_OAK_LOG"),
                        Map.of("type", "mineBlock", "amount", 400, "target", "JUNGLE_LOG"),
                        Map.of("type", "mineBlock", "amount", 400, "target", "ACACIA_LOG"),
                        Map.of("type", "mineBlock", "amount", 400, "target", "SPRUCE_LOG"),
                        Map.of("type", "mineBlock", "amount", 400, "target", "MANGROVE_LOG"),
                        Map.of("type", "mineBlock", "amount", 400, "target", "CHERRY_LOG")
                ),
                List.of(Map.of("type", "mineBlock", "amount", 50, "target", "DEEPSLATE_GOLD_ORE")),
                List.of(
                        Map.of(
                                "type", "death", "target", "ENTITY_ATTACK", "amount", 1
                        )
                ),
                List.of(
                        Map.of("type", "mineBlock", "amount", 25, "target", "MELON"),
                        Map.of("type", "mineBlock", "amount", 25, "target", "PUMPKIN"),
                        Map.of("type", "mineBlock", "amount", 500, "target", "BEETROOTS")
                ),
                List.of(Map.of("type", "reachCoordinates", "x", -126, "y", -25, "z", 15, "amount", 1)),
                List.of(
                        Map.of("type", "craftItem", "amount", 1, "target", "DIAMOND_HELMET"),
                        Map.of("type", "craftItem", "amount", 1, "target", "DIAMOND_CHESTPLATE"),
                        Map.of("type", "craftItem", "amount", 1, "target", "DIAMOND_LEGGINGS"),
                        Map.of("type", "craftItem", "amount", 1, "target", "DIAMOND_BOOTS")
                ),
                List.of(Map.of("type", "killEntity", "amount", 1, "target", "WITHER")),
                List.of(Map.of("type", "standOnBlock", "height", -32, "amount", 1, "block", "BEDROCK")),
                List.of(Map.of("type", "craftItem", "amount", 1000, "target", "BREAD")),
                List.of(Map.of("type", "killEntity", "amount", 25, "target", "WITCH")),
                List.of(Map.of("type", "craftItem", "amount", 1, "target", "DIAMOND_BLOCK")),
                List.of(
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_HELMET", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_CHESTPLATE", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_LEGGINGS", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_BOOTS", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_SWORD", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_PICKAXE", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_AXE", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_SHOVEL", "target", "DOESNTMATTER"),
                        Map.of("type", "onEnchant", "amount", 1, "item", "DIAMOND_HOE", "target", "DOESNTMATTER")
                ),
                List.of(Map.of("type", "smeltItem", "amount", 200, "target", "COOKED_SALMON")),
                List.of(
                        Map.of("type", "killEntity", "amount", 50, "target", "ZOMBIE"),
                        Map.of("type", "killEntity", "amount", 50, "target", "SKELETON"),
                        Map.of("type", "killEntity", "amount", 50, "target", "WITCH"),
                        Map.of("type", "killEntity", "amount", 50, "target", "SLIME"),
                        Map.of("type", "killEntity", "amount", 50, "target", "BOGGED"),
                        Map.of("type", "killEntity", "amount", 50, "target", "SPIDER"),
                        Map.of("type", "killEntity", "amount", 50, "target", "CAVE_SPIDER")
                ),
                List.of(Map.of("type", "achievemant", "target", "minecraft:nether/create_beacon", "amount", 1)),
                List.of(Map.of("type", "achievemant", "target", "minecraft:nether/create_full_beacon", "amount", 1)),
                List.of(Map.of("type", "reachCoordinates", "x", -152, "y", -50, "z", 6, "amount", 1))
        );

        String[] required_quests = {
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "",
                "37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54",
        };

        Integer[] requiredValues = {
                1,
                5,
                100,
                30,
                50,
                10,
                1,
                50,
                25,
                1,
                100,
                100,
                10,
                50,
                32,
                5,
                0,
                1,
                512,
                50,
                100,
                128,
                1,
                10,
                64,
                1,
                300,
                5,
                1,
                9,
                20,
                200,
                1,
                1,
                1,
                32,
                0,
                6,
                3200,
                50,
                1,
                550,
                1,
                4,
                1,
                1,
                1000,
                25,
                1,
                9,
                200,
                350,
                1,
                1,
                1
        };

        String[] displayItems = {
                "BOOK",
                "STONE_PICKAXE",
                "OAK_LOG",
                "COAL_ORE",
                "ZOMBIE_HEAD",
                "OAK_SAPLING",
                "COAL_BLOCK",
                "WOODEN_SWORD",
                "APPLE",
                "BEDROCK",
                "BEETROOT",
                "IRON_SWORD",
                "COD",
                "SALMON",
                "COOKED_COD",
                "MINECART",
                "RAW_IRON_BLOCK",
                "FURNACE",
                "IRON_AXE",
                "RAW_IRON",
                "SKELETON_SKULL",
                "SUGAR_CANE",
                "IRON_BLOCK",
                "IRON_CHESTPLATE",
                "CACTUS",
                "BEDROCK",
                "HAY_BLOCK",
                "BOW",
                "ANVIL",
                "BOOK",
                "BEETROOT_SOUP",
                "BONE",
                "ENCHANTED_BOOK",
                "DIAMOND_LEGGINGS",
                "SPYGLASS",
                "COPPER_BLOCK",
                "RAW_GOLD_BLOCK",
                "GRINDSTONE",
                "DIAMOND_AXE",
                "DEEPSLATE_GOLD_ORE",
                "TOTEM_OF_UNDYING",
                "MELON",
                "GOLD_BLOCK",
                "DIAMOND_HELMET",
                "WITHER_SKELETON_SKULL",
                "BEDROCK",
                "BREAD",
                "REDSTONE",
                "DIAMOND_BLOCK",
                "ENCHANTER",
                "COOKED_SALMON",
                "DIAMOND_SWORD",
                "BEACON",
                "EMERALD_BLOCK",
                "NETHERRACK"
        };
        String[] taskmode = {
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ANY",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
                "ALL",
        };

        try (Connection conn = getConnection(); PreparedStatement pstmt = conn.prepareStatement(sql)) {
            for (int i = 0; i < ids.length; i++) {
                pstmt.setInt(1, ids[i]);
                pstmt.setString(2, names[i]);
                pstmt.setString(3, descriptions[i]);
                pstmt.setString(4, rewards[i]);
                pstmt.setString(5, displayRewards[i]);
                pstmt.setString(6, layers[i]);
                pstmt.setString(7, dimensions[i]);
                pstmt.setString(8, difficulties[i]);
                pstmt.setString(9, nextQuests[i]);

                String taskdescriptionJson = gson.toJson(allTaskDescriptions.get(i));
                pstmt.setString(10, taskdescriptionJson);

                String taskJson = gson.toJson(allTasks.get(i));
                pstmt.setString(11, taskJson);

                String requiredQuestsJson = required_quests[i].isEmpty() ? "[]" : gson.toJson(Arrays.stream(required_quests[i].split(","))
                        .map(String::trim)
                        .map(Integer::parseInt)
                        .collect(Collectors.toList()));

                pstmt.setString(12, requiredQuestsJson);

                pstmt.setInt(13, requiredValues[i]);
                pstmt.setString(14, displayItems[i]);
                pstmt.setString(15, taskmode[i]);
                pstmt.executeUpdate();
            }
            getLogger().info("Standard quests successfully loaded.");
        } catch (SQLException e) {
            throw new RuntimeException("Error while adding the standard quests: " + e);
        }
    }

    public void canUnlockQuest(String playerUUID, int questId, Consumer<Boolean> callback) {
        new BukkitRunnable() {
            @Override
            public void run() {
                boolean canUnlock = false;
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT required_quests FROM quests WHERE id = ?");
                     PreparedStatement checkStmt = conn.prepareStatement(
                             "SELECT COUNT(*) FROM playerprogress WHERE player_id = ? AND quest_id = ? AND completed = 1")) {

                    pstmt.setInt(1, questId);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        if (rs.next()) {
                            String requiredQuestsJson = rs.getString("required_quests");

                            if (requiredQuestsJson == null || requiredQuestsJson.trim().isEmpty() || requiredQuestsJson.equals("[]")) {
                                callback.accept(false);
                                return;
                            }

                            Gson gson = new Gson();
                            List<Integer> requiredQuests = gson.fromJson(requiredQuestsJson, new TypeToken<List<Integer>>() {}.getType());

                            canUnlock = true;
                            for (int requiredQuestId : requiredQuests) {
                                checkStmt.setString(1, playerUUID);
                                checkStmt.setInt(2, requiredQuestId);
                                try (ResultSet checkRs = checkStmt.executeQuery()) {
                                    if (checkRs.next() && checkRs.getInt(1) == 0) {
                                        canUnlock = false;
                                        break;
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }

                final boolean finalResult = canUnlock;
                new BukkitRunnable() {
                    @Override
                    public void run() {
                        callback.accept(finalResult);
                    }
                }.runTask(Quest.this);
            }
        }.runTaskAsynchronously(Quest.this);
    }

    public void checkForNewUnlockedQuests(Player player) {
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id FROM quests WHERE required_quests IS NOT NULL")) {

                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<Integer> potentialQuests = new ArrayList<>();

                        while (rs.next()) {
                            potentialQuests.add(rs.getInt("id"));
                        }

                        for (int questId : potentialQuests) {
                            canUnlockQuest(playerUUID, questId, result -> {
                                if (result) {
                                    new BukkitRunnable() {
                                        @Override
                                        public void run() {
                                            try (Connection conn2 = getConnection();
                                                 PreparedStatement unlockStmt = conn2.prepareStatement(
                                                         "UPDATE playerprogress SET isQuestforPlayerAvailable = 1 WHERE player_id = ? AND quest_id = ?")) {
                                                unlockStmt.setString(1, playerUUID);
                                                unlockStmt.setInt(2, questId);
                                                unlockStmt.executeUpdate();
                                            } catch (SQLException e) {
                                                e.printStackTrace();
                                            }
                                        }
                                    }.runTask(Quest.this);
                                }
                            });
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Quest.this);
    }

    @EventHandler
    public void onPlayerInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        checkPlayerInventoryForQuestItems(player);
    }

    @EventHandler
    public void onInventoryInteract(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player player) {
            checkPlayerInventoryForQuestItems(player);
        }
    }

    @EventHandler
    public void onHotbarChange(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();
        checkPlayerInventoryForQuestItems(player);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        checkPlayerInventoryForQuestItems(player);
    }
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        String playerName = player.getName();
        String joinDate = LocalDate.now().toString();

        try (Connection conn = getConnection()) {
            String checkPlayerExists = "SELECT 1 FROM players WHERE uuid = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkPlayerExists)) {
                checkStmt.setString(1, playerUUID);
                ResultSet rs = checkStmt.executeQuery();

                if (!rs.next()) {
                    String sql = "INSERT INTO players(uuid, name, join_date) VALUES(?, ?, ?)";
                    try (PreparedStatement pstmt = conn.prepareStatement(sql)) {
                        pstmt.setString(1, playerUUID);
                        pstmt.setString(2, playerName);
                        pstmt.setString(3, joinDate);
                        pstmt.executeUpdate();
                    }
                    initializePlayerProgress(conn, playerUUID);
                    player.sendMessage(ChatColor.GREEN + "Willkommen auf dem Server, " + playerName + "!");
                    addAvailableItemsToPlayerShopsTable(player);
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Willkommen zurück, " + playerName + "!");
                }
            }

        } catch (SQLException e) {
            getLogger().severe("Fehler bei der Spielerregistrierung: " + e.getMessage());
        }
        checkPlayerInventoryForQuestItems(event.getPlayer());
    }

    private void initializePlayerProgress(Connection conn, String playerUUID) throws SQLException {
        String selectQuests = "SELECT id FROM quests";
        String insertProgress = "INSERT OR IGNORE INTO playerprogress(player_id, quest_id, isQuestforPlayerAvailable, completed, progress) "
                + "VALUES(?, ?, 0, 0, 0)";

        try (PreparedStatement selectStmt = conn.prepareStatement(selectQuests);
             PreparedStatement insertStmt = conn.prepareStatement(insertProgress)) {
            ResultSet quests = selectStmt.executeQuery();
            while (quests.next()) {
                int questId = quests.getInt("id");
                insertStmt.setString(1, playerUUID);
                insertStmt.setInt(2, questId);
                insertStmt.executeUpdate();
            }
            getLogger().info("Progress for players " + playerUUID + " was initialized.");
        }

        String updateQuest1 = "UPDATE playerprogress SET isQuestforPlayerAvailable = 1 WHERE player_id = ? AND quest_id = 1";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuest1)) {
            updateStmt.setString(1, playerUUID);
            updateStmt.executeUpdate();
        }
    }
    private void addAvailableItemsToPlayerShopsTable(Player player) {
        try (Connection conn = getConnection()) {
            String checkSql = "SELECT COUNT(*) FROM shops WHERE uuid = ? AND item = ?";
            String insertSql = "INSERT INTO shops (uuid, item, available) VALUES (?, ?, ?)";

            try (PreparedStatement checkStmt = conn.prepareStatement(checkSql);
                 PreparedStatement insertStmt = conn.prepareStatement(insertSql)) {

                int batchSize = 0;

                for (BuyOffer offer : buyOffers.values()) {
                    if (offer.isOnetimebought() != 3) {
                        String item = offer.getItem().getType().name();

                        checkStmt.setString(1, player.getUniqueId().toString());
                        checkStmt.setString(2, item);

                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (!rs.next() || rs.getInt(1) == 0) {
                                insertStmt.setString(1, player.getUniqueId().toString());
                                insertStmt.setString(2, item);
                                insertStmt.setBoolean(3, true);
                                insertStmt.addBatch();
                                batchSize++;
                            }
                        }

                        if (batchSize >= 50) {
                            insertStmt.executeBatch();
                            batchSize = 0;
                        }
                    }
                }

                if (batchSize > 0) {
                    insertStmt.executeBatch();
                }

                player.sendMessage(ChatColor.GREEN + "Alle verfügbaren Items wurden für dich freigeschaltet!");

            } catch (SQLException e) {
                e.printStackTrace();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }



    // GUI
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            openQuestScreen(player, 0);
            return true;
        } else {
            sender.sendMessage(MiniMessage.miniMessage().deserialize(
                    "<red>Dieser Befehl kann nur von einem Spieler ausgeführt werden!"
            ));
            return true;
        }
    }

    private final Map<UUID, Inventory> Quests = new HashMap<>();
    private final Map<UUID, Integer> playerPages = new HashMap<>();

    public void openQuestScreen(Player player, int pageIndex) {
        setCurrentPage(player, pageIndex);
        checkAndCompleteQuests(player);
        Inventory inv = getServer().createInventory(null, 54, "Quests - Seite " + (pageIndex + 1));

        ItemStack Border = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        var borderMeta = Border.getItemMeta();
        if (borderMeta != null) {
            borderMeta.setDisplayName(" ");
            Border.setItemMeta(borderMeta);
        }

        ItemStack Close = new ItemStack(Material.HOPPER, 1);
        var closeMeta = Close.getItemMeta();
        if (closeMeta != null) {
            closeMeta.setDisplayName(ChatColor.RED + "EXIT");
            Close.setItemMeta(closeMeta);
        }

        ItemStack Help = new ItemStack(Material.WRITTEN_BOOK, 1);
        var helpMeta = Help.getItemMeta();
        if (helpMeta != null) {
            helpMeta.setDisplayName(ChatColor.GREEN + "HELP");
            Help.setItemMeta(helpMeta);
        }

        ItemStack NextPage = new ItemStack(Material.ARROW, 1);
        var nextMeta = NextPage.getItemMeta();
        if (nextMeta != null) {
            nextMeta.setDisplayName(ChatColor.YELLOW + "Nächste Seite");
            NextPage.setItemMeta(nextMeta);
        }

        ItemStack PrevPage = new ItemStack(Material.ARROW, 1);
        var prevMeta = PrevPage.getItemMeta();
        if (prevMeta != null) {
            prevMeta.setDisplayName(ChatColor.YELLOW + "Vorherige Seite");
            PrevPage.setItemMeta(prevMeta);
        }

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, Border);
        }

        inv.setItem(45, Close);
        inv.setItem(53, Help);

        String sql = "SELECT q.id, q.name, q.description, q.reward, q.displayreward, q.layer, q.dimension, q.difficulty, " +
                "q.displayitem, pp.isQuestforPlayerAvailable, pp.completed, pp.tasks, q.task, q.task_description " +
                "FROM quests q " +
                "LEFT JOIN playerprogress pp ON q.id = pp.quest_id AND pp.player_id = ? " +
                "ORDER BY q.id ASC";

        try (Connection conn = DatabaseManager.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, player.getUniqueId().toString());

            try (ResultSet rs = pstmt.executeQuery()) {
                List<ItemStack> questItems = new ArrayList<>();
                Gson gson = new Gson();
                Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                while (rs.next()) {
                    int questId = rs.getInt("id");
                    String questName = rs.getString("name");
                    String description = rs.getString("description");
                    String reward = rs.getString("reward");
                    String displayReward = rs.getString("displayreward");
                    String displayItemName = rs.getString("displayitem");
                    boolean isAvailable = rs.getBoolean("isQuestforPlayerAvailable");
                    boolean isCompleted = rs.getBoolean("completed");
                    String taskJson = rs.getString("task");
                    String progressJson = rs.getString("tasks");
                    String taskDescriptionJson = rs.getString("task_description");

                    Material material = Material.BARRIER;
                    if (isCompleted) {
                        material = Material.LIME_DYE;
                    } else if (isAvailable) {
                        try {
                            material = Material.valueOf(displayItemName.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            player.sendMessage("Ungültiges Display-Item: " + displayItemName);
                        }
                    }


                    ItemStack questItem = new ItemStack(material, 1);
                    var meta = questItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.GREEN + questName);
                        List<String> lore = new ArrayList<>();

                        if (isCompleted) {
                            lore.add(ChatColor.DARK_GREEN + "✅ Quest abgeschlossen!");
                        } else if (isAvailable) {
                            Map<String, Integer> progressMap = progressJson != null ? gson.fromJson(progressJson, progressType) : new HashMap<>();
                            List<Map<String, Object>> taskList = taskJson != null ? gson.fromJson(taskJson, taskListType) : new ArrayList<>();
                            List<Map<String, Object>> taskDescriptions = taskDescriptionJson != null ? gson.fromJson(taskDescriptionJson, taskListType) : new ArrayList<>();
                            String layer = rs.getString("layer");
                            String dimension = rs.getString("dimension");
                            String difficulty = rs.getString("difficulty");
                            lore.add(ChatColor.GRAY + description);
                            lore.add("");
                            lore.add(ChatColor.BLUE + "Ebene: " + ChatColor.WHITE + layer);
                            lore.add(ChatColor.BLUE + "Dimension: " + ChatColor.WHITE + dimension);
                            lore.add(ChatColor.BLUE + "Difficulty: " + ChatColor.WHITE + difficulty);
                            lore.add("");
                            lore.add(ChatColor.BLUE + "Aufgaben:");
                            for (int i = 0; i < taskList.size(); i++) {
                                Map<String, Object> task = taskList.get(i);
                                String taskType = (String) task.get("type");
                                String target = task.containsKey("target") ? (String) task.get("target") : "Unbekannt";
                                int requiredAmount = task.containsKey("amount") && task.get("amount") instanceof Number
                                        ? ((Number) task.get("amount")).intValue()
                                        : 1;

                                String progressKey = taskType + ":" + target;
                                int taskProgress = progressMap.getOrDefault(progressKey, 0);
                                String taskDescription = (i < taskDescriptions.size()) ? (String) taskDescriptions.get(i).get("text") : "";

                                String progressText = ChatColor.GRAY + " - " + ChatColor.GOLD + taskDescription + " " + taskProgress + "/" + requiredAmount;
                                lore.add(progressText);
                            }

                            lore.add("");
                            lore.add(ChatColor.BLUE + "Belohnung: ");
                            lore.add(ChatColor.GRAY + " - " + ChatColor.GREEN + displayReward);
                        } else {
                            meta.setDisplayName(ChatColor.RED + "Diese Quest ist noch nicht freigeschaltet.");
                        }

                        meta.setLore(lore);
                        questItem.setItemMeta(meta);
                    }

                    questItems.add(questItem);
                }

                int[] questSlots = {
                        10, 11, 12, 13, 14, 15, 16,
                        19, 20, 21, 22, 23, 24, 25,
                        28, 29, 30, 31, 32, 33, 34,
                        37, 38, 39, 40, 41, 42, 43
                };

                int totalPages = (int) Math.ceil((double) questItems.size() / questSlots.length);
                int startIndex = pageIndex * questSlots.length;
                int endIndex = Math.min(startIndex + questSlots.length, questItems.size());

                for (int i = startIndex; i < endIndex; i++) {
                    inv.setItem(questSlots[i - startIndex], questItems.get(i));
                }

                if (pageIndex > 0) {
                    inv.setItem(48, PrevPage);
                }
                if (endIndex < questItems.size()) {
                    inv.setItem(50, NextPage);
                }
            }
        } catch (SQLException e) {
            player.sendMessage("Fehler beim Laden der Quests: " + e.getMessage());
        }

        Quests.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }

    private int getCurrentPage(Player player) {
        return playerPages.getOrDefault(player.getUniqueId(), 0);
    }

    private void setCurrentPage(Player player, int page) {
        playerPages.put(player.getUniqueId(), page);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Quests")) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);

            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            Player player = (Player) event.getWhoClicked();
            int currentPage = getCurrentPage(player);

            if (event.getSlot() == 53 && event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
                event.getWhoClicked().closeInventory();
                openBook(player);
            } else if (event.getSlot() == 45 && event.getCurrentItem().getType() == Material.HOPPER) {
                event.getWhoClicked().closeInventory();
            } else if (event.getSlot() == 48) {
                if (currentPage > 0) {
                    openQuestScreen(player, currentPage - 1);
                }
            } else if (event.getSlot() == 50) {
                openQuestScreen(player, currentPage + 1);
            }
            String playerUUID = player.getUniqueId().toString();
            int slotIndex = event.getSlot();
            String url = "jdbc:sqlite:quests.db";

            new BukkitRunnable() {
                @Override
                public void run() {
                    String sql = "SELECT quests.id, pp.progress, pp.completed, quests.reward, quests.nextquests, quests.required_value " +
                            "FROM quests " +
                            "LEFT JOIN playerprogress pp ON quests.id = pp.quest_id AND pp.player_id = ? " +
                            "WHERE quests.id = ?";

                    try (Connection conn = DatabaseManager.getConnection();
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setString(1, playerUUID);
                        pstmt.setInt(2, slotIndex);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                int progress = rs.getInt("progress");
                                boolean completed = rs.getBoolean("completed");
                                int requiredValue = rs.getInt("required_value");

                                if (!completed && progress >= requiredValue) {
                                    String updateSql = "UPDATE playerprogress SET completed = 1 WHERE player_id = ? AND quest_id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                                        updateStmt.setString(1, playerUUID);
                                        updateStmt.setInt(2, slotIndex);
                                        updateStmt.executeUpdate();
                                    }

                                    String reward = rs.getString("reward");
                                    player.getInventory().addItem(parseReward(reward));

                                    String nextQuests = rs.getString("nextquests");
                                    if (!nextQuests.isEmpty()) {
                                        String[] questIds = nextQuests.split(",");
                                        for (String questId : questIds) {
                                            unlockNextQuest(player.getUniqueId().toString(), questId);
                                        }
                                    }
                                }
                            }
                        } catch (SQLException e) {
                            player.sendMessage("Fehler beim Verarbeiten der Quest: " + e.getMessage());
                        }
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }.runTaskAsynchronously(this);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryShiftClick(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Quests") && event.isShiftClick()) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryDrag(InventoryDragEvent event) {
        if (event.getView().getTitle().startsWith("Quests")) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onHotbarSwap(InventoryClickEvent event) {
        if (event.getView().getTitle().startsWith("Quests") && event.getClick().isKeyboardClick()) {
            event.setCancelled(true);
            event.setResult(Event.Result.DENY);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onItemDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getOpenInventory().getTitle().startsWith("Quests")) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onInventoryMove(InventoryMoveItemEvent event) {
        if (event.getDestination().getHolder() instanceof Player) {
            Player player = (Player) event.getDestination().getHolder();
            if (player.getOpenInventory().getTitle().startsWith("Quests")) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSwapHandItems(PlayerSwapHandItemsEvent event) {
        if (event.getPlayer().getOpenInventory().getTitle().startsWith("Quests")) {
            event.setCancelled(true);
        }
    }

    private void checkAndCompleteQuests(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String url = "jdbc:sqlite:quests.db";
                String sql = "SELECT playerprogress.quest_id, quests.required_value, playerprogress.progress " +
                        "FROM playerprogress " +
                        "JOIN quests ON playerprogress.quest_id = quests.id " +
                        "WHERE playerprogress.player_id = ? AND playerprogress.completed = 0";

                try (Connection conn = DatabaseManager.getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, player.getUniqueId().toString());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<Integer> completedQuests = new ArrayList<>();

                        while (rs.next()) {
                            int questId = rs.getInt("quest_id");
                            int progress = rs.getInt("progress");
                            int requiredAmount = rs.getInt("required_value");

                            if (progress >= requiredAmount) {
                                completedQuests.add(questId);
                            }
                        }

                        if (!completedQuests.isEmpty()) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    for (int questId : completedQuests) {
                                        completeQuest(player, questId);
                                    }
                                }
                            }.runTask(Quest.this);
                        }
                    }
                } catch (SQLException e) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage(ChatColor.RED + "Fehler beim Überprüfen der Quests: " + e.getMessage());
                        }
                    }.runTask(Quest.this);
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void completeQuest(Player player, int questId) {
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    conn.setAutoCommit(false);

                    String checkQuery = "SELECT completed, isQuestforPlayerAvailable, nextquests FROM playerprogress " +
                            "JOIN quests ON playerprogress.quest_id = quests.id " +
                            "WHERE playerprogress.player_id = ? AND playerprogress.quest_id = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                        checkStmt.setString(1, playerUUID);
                        checkStmt.setInt(2, questId);
                        try (ResultSet rs = checkStmt.executeQuery()) {
                            if (!rs.next() || rs.getBoolean("completed") || rs.getInt("isQuestforPlayerAvailable") == 0) {
                                conn.rollback();
                                return;
                            }

                            String nextQuests = rs.getString("nextquests");
                            boolean hasNextQuests = (nextQuests != null && !nextQuests.isEmpty());

                            String updateQuery = "UPDATE playerprogress SET completed = 1 WHERE player_id = ? AND quest_id = ?";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setString(1, playerUUID);
                                updateStmt.setInt(2, questId);
                                updateStmt.executeUpdate();
                            }

                            String rewardQuery = "SELECT reward, displayreward FROM quests WHERE id = ?";
                            try (PreparedStatement rewardStmt = conn.prepareStatement(rewardQuery)) {
                                rewardStmt.setInt(1, questId);
                                try (ResultSet rewardRs = rewardStmt.executeQuery()) {
                                    if (rewardRs.next()) {
                                        String reward = rewardRs.getString("reward");
                                        String displayReward = rewardRs.getString("displayreward");

                                        if (reward != null && reward.startsWith("openArea")) {
                                            String[] parts = reward.split(",");
                                            if (parts.length > 0 && parts[parts.length - 1].isEmpty()) {
                                                parts = Arrays.copyOf(parts, parts.length - 1);
                                            }
                                            if (parts.length == 7) {
                                                try {
                                                    int x1 = Integer.parseInt(parts[1]);
                                                    int y1 = Integer.parseInt(parts[2]);
                                                    int z1 = Integer.parseInt(parts[3]);
                                                    int x2 = Integer.parseInt(parts[4]);
                                                    int y2 = Integer.parseInt(parts[5]);
                                                    int z2 = Integer.parseInt(parts[6]);

                                                    new BukkitRunnable() {
                                                        @Override
                                                        public void run() {
                                                            clearArea(player.getWorld(), x1, y1, z1, x2, y2, z2);
                                                            player.sendMessage(ChatColor.GREEN + "Der Bereich wurde freigeschaltet!");
                                                        }
                                                    }.runTask(Quest.this);
                                                } catch (NumberFormatException e) {
                                                    getLogger().warning("Ungültige Koordinaten für openArea in Quest " + questId);
                                                }
                                            } else {
                                                getLogger().warning("Reward openArea hat nicht die erwartete Anzahl an Parametern. Erwartet: 7, gefunden: " + parts.length);
                                            }
                                        } else {
                                            ItemStack rewardItem = parseReward(reward);
                                            if (rewardItem != null) {
                                                new BukkitRunnable() {
                                                    @Override
                                                    public void run() {
                                                        Map<Integer, ItemStack> leftover = player.getInventory().addItem(rewardItem);
                                                        if (!leftover.isEmpty()) {

                                                            Location loc = player.getLocation();
                                                            for (ItemStack item : leftover.values()) {
                                                                player.getWorld().dropItemNaturally(loc, item);
                                                            }
                                                            player.sendMessage(ChatColor.GOLD + "Du hast die Quest " + questId + " abgeschlossen und hast " + displayReward + " erhalten");
                                                        } else {
                                                            player.sendMessage(ChatColor.GOLD + "Du hast die Quest " + questId + " abgeschlossen und hast " + displayReward + " erhalten");
                                                        }
                                                    }
                                                }.runTask(Quest.this);
                                            }
                                        }
                                    }

                                }
                            }

                            conn.commit();

                            checkForNewUnlockedQuests(player);

                            if (hasNextQuests) {
                                new BukkitRunnable() {
                                    @Override
                                    public void run() {
                                        for (String nextQuestId : nextQuests.split(",")) {
                                            unlockNextQuest(playerUUID, nextQuestId.trim());
                                        }
                                    }
                                }.runTaskAsynchronously(Quest.this);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void clearArea(World world, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (world == null) {
            getLogger().warning("Welt nicht gefunden!");
            return;
        }

        int minX = Math.min(x1, x2);
        int maxX = Math.max(x1, x2);
        int minY = Math.min(y1, y2);
        int maxY = Math.max(y1, y2);
        int minZ = Math.min(z1, z2);
        int maxZ = Math.max(z1, z2);

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Block block = world.getBlockAt(x, y, z);
                    block.setType(Material.AIR);
                }
            }
        }

        getLogger().info("Bereich von (" + minX + ", " + minY + ", " + minZ + ") bis (" + maxX + ", " + maxY + ", " + maxZ + ") wurde geleert.");
    }


    public ItemStack parseReward(String rewardString) {
        try {
            String[] parts = rewardString.split(",", 3);
            if (parts.length < 2) return null;

            Material material = Material.matchMaterial(parts[0].toUpperCase());
            if (material == null) return null;

            int amount = Integer.parseInt(parts[1]);
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();

            if (meta != null && parts.length == 3) {
                Gson gson = new Gson();
                Map<String, Object> attributes = gson.fromJson(parts[2], new TypeToken<Map<String, Object>>() {}.getType());

                if (attributes.containsKey("name")) {
                    String itemName = (String) attributes.get("name");
                    meta.setDisplayName(ChatColor.GREEN + itemName);
                }

                if (attributes.containsKey("lore")) {
                    List<String> lore = (List<String>) attributes.get("lore");
                    List<String> formattedLore = new ArrayList<>();
                    for (String line : lore) {
                        formattedLore.add(ChatColor.DARK_GRAY + line);
                    }
                    meta.setLore(formattedLore);
                }

                item.setItemMeta(meta);
            }

            return item;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public void unlockNextQuest(String playerUUID, String nextQuestId) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    int questId = Integer.parseInt(nextQuestId.trim());

                    String sql = "UPDATE playerprogress SET isQuestforPlayerAvailable = 1 WHERE player_id = ? AND quest_id = ?";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, playerUUID);
                        stmt.setInt(2, questId);
                        int rowsUpdated = stmt.executeUpdate();

                        if (rowsUpdated > 0) {
                        } else {
                        }
                    }
                } catch (SQLException | NumberFormatException e) {
                    throw new RuntimeException("Fehler beim Freischalten der Quest " + nextQuestId + ": " + e);
                }
            }
        }.runTaskAsynchronously(this);
    }

    public void openBook(Player player) {
        Component page1 = MiniMessage.miniMessage().deserialize("<gray>|-------<dark_gray><bold>FAQ</bold></dark_gray>-------|</gray>\n" +
                "Mehr Infos auf unserem Discord");
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);


        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setTitle("FAQ");
            bookMeta.setAuthor("Plugin");


            bookMeta.pages(page1);
            book.setItemMeta(bookMeta);
        }

        player.openBook(book);
    }

    //Tasks
    public void checkPlayerInventoryForQuestItems(Player player) {
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id, task FROM quests WHERE task LIKE '%getItem%'");
                     PreparedStatement progressStmt = conn.prepareStatement("SELECT tasks, isQuestforPlayerAvailable FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {

                    Gson gson = new Gson();
                    Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                    Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");

                            if (taskJson == null || taskJson.isEmpty()) continue;
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();

                            if (progressRs.next()) {
                                boolean questAvailable = progressRs.getBoolean("isQuestforPlayerAvailable");
                                if (!questAvailable) {
                                    continue;
                                }
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            } else {
                                continue;
                            }

                            boolean progressUpdated = false;

                            for (Map<String, Object> task : taskDetails) {
                                if ("getItem".equals(task.get("type"))) {
                                    String targetItem = ((String) task.get("target")).toUpperCase();
                                    Object amountObj = task.get("amount");
                                    int requiredAmount = 0;

                                    if (amountObj instanceof Number) {
                                        requiredAmount = ((Number) amountObj).intValue();
                                    } else if (amountObj instanceof String) {
                                        try {
                                            requiredAmount = Integer.parseInt((String) amountObj);
                                        } catch (NumberFormatException e) {
                                            Bukkit.getLogger().warning("[OPIslandsQuestSystem] Fehler: 'amount' konnte nicht in eine Zahl umgewandelt werden: " + amountObj);
                                            continue;
                                        }
                                    }

                                    int itemCount = 0;
                                    for (ItemStack stack : player.getInventory().getContents()) {
                                        if (stack != null && stack.getType().toString().equalsIgnoreCase(targetItem)) {
                                            if (stack.getType() == Material.ENCHANTED_BOOK) {
                                                ItemMeta meta = stack.getItemMeta();
                                                if (meta instanceof EnchantmentStorageMeta enchantMeta) {
                                                    Map<String, Object> metaMap = (Map<String, Object>) task.get("meta");
                                                    if (metaMap != null) {
                                                        Object enchantObj = metaMap.get("enchantment");
                                                        Object levelObj = metaMap.get("level");
                                                        if (enchantObj instanceof String requiredEnchant && levelObj instanceof Number) {
                                                            requiredEnchant = requiredEnchant.toUpperCase();
                                                            int requiredLevel = ((Number) levelObj).intValue();
                                                            for (Map.Entry<Enchantment, Integer> entry : enchantMeta.getStoredEnchants().entrySet()) {
                                                                String enchantName = entry.getKey().getKey().getKey().toUpperCase();
                                                                int enchantLevel = entry.getValue().intValue();
                                                                if (enchantName.equals(requiredEnchant) && enchantLevel >= requiredLevel) {
                                                                    itemCount += stack.getAmount();
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            } else {
                                                itemCount += stack.getAmount();
                                            }
                                        }
                                    }

                                    String progressKey = "getItem:" + targetItem;
                                    int newProgress = Math.min(itemCount, requiredAmount);
                                    if (progressMap.getOrDefault(progressKey, 0) != newProgress) {
                                        progressMap.put(progressKey, newProgress);
                                        progressUpdated = true;
                                    }

                                    if (itemCount >= requiredAmount) {
                                        if (progressMap.getOrDefault(progressKey, 0) < requiredAmount) {
                                            progressMap.put(progressKey, requiredAmount);
                                            progressUpdated = true;
                                        }
                                    }


                                }
                            }

                            if (progressUpdated) {
                                updateTaskProgress(player, questId, progressMap);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Quest.this);
    }


    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (event.getEntity() instanceof Player player) {
            checkPlayerInventoryForQuestItems(player);
        }
    }

    private final Map<UUID, Map<Location, Long>> blockActionCooldowns = new HashMap<>();

    private final long COOLDOWN_MILLIS = 30000;

    private boolean isCrop(Material type) {
        return type == Material.WHEAT ||
                type == Material.CARROTS ||
                type == Material.POTATOES ||
                type == Material.BEETROOTS;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%mineBlock%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            boolean progressUpdated = false;
                            int totalProgress = 0;
                            int requiredTotal = 0;

                            for (Map<String, Object> task : taskDetails) {
                                if ("mineBlock".equals(task.get("type"))) {
                                    String targetBlock = (String) task.get("target");
                                    int requiredAmount = ((Number) task.get("amount")).intValue();
                                    requiredTotal += requiredAmount;

                                    if (blockName.equalsIgnoreCase(targetBlock)) {
                                        String progressKey = "mineBlock:" + targetBlock;
                                        int currentProgress = progressMap.getOrDefault(progressKey, 0);

                                        if (currentProgress < requiredAmount) {
                                            progressMap.put(progressKey, currentProgress + 1);
                                            progressUpdated = true;
                                        }
                                    }
                                }
                            }

                            if (progressUpdated) {
                                totalProgress = progressMap.values().stream().mapToInt(Integer::intValue).sum();
                                updateTaskProgress(player, questId, progressMap);

                                if (totalProgress >= requiredTotal) {
                                    completeQuest(player, questId);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }


    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        ItemStack enchantedItem = event.getItem();
        String playerUUID = player.getUniqueId().toString();
        String itemType = enchantedItem.getType().name();

        Map<Enchantment, Integer> enchantments = event.getEnchantsToAdd();
        if (enchantments.isEmpty()) return;

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%onEnchant%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?");
                     PreparedStatement updateStmt = conn.prepareStatement(
                             "UPDATE playerprogress SET tasks = ?, progress = ? WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            boolean progressUpdated = false;
                            int totalProgress = 0;
                            int requiredTotal = 0;

                            for (Map<String, Object> task : taskDetails) {
                                if ("onEnchant".equals(task.get("type"))) {
                                    String requiredEnchant = ((String) task.getOrDefault("target", "DOESNTMATTER")).toUpperCase();
                                    String requiredItem = ((String) task.getOrDefault("item", "ANY")).toUpperCase();
                                    int requiredAmount = ((Number) task.getOrDefault("amount", 1)).intValue();

                                    requiredTotal += requiredAmount;

                                    if (requiredItem.equals("ANY") || itemType.equalsIgnoreCase(requiredItem)) {
                                        for (Enchantment enchant : enchantments.keySet()) {
                                            String enchantmentName = enchant.getKey().getKey().toUpperCase();
                                            if (requiredEnchant.equals("DOESNTMATTER") || enchantmentName.equals(requiredEnchant)) {
                                                String progressKey = "onEnchant:" + requiredItem + ":" + requiredEnchant;
                                                int currentProgress = progressMap.getOrDefault(progressKey, 0);
                                                int newProgress = Math.min(currentProgress + 1, requiredAmount);
                                                progressMap.put(progressKey, newProgress);
                                                progressUpdated = true;
                                            }
                                        }
                                    }
                                }
                            }

                            if (progressUpdated) {
                                totalProgress = progressMap.values().stream().mapToInt(Integer::intValue).sum();

                                updateStmt.setString(1, gson.toJson(progressMap));
                                updateStmt.setInt(2, totalProgress);
                                updateStmt.setString(3, playerUUID);
                                updateStmt.setInt(4, questId);
                                updateStmt.executeUpdate();

                                if (totalProgress >= requiredTotal) {
                                    completeQuest(player, questId);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void updateTaskProgress(Player player, int questId, Map<String, Integer> progressMap) {
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    Gson gson = new Gson();
                    String progressJson = gson.toJson(progressMap);

                    int totalProgress = 0;
                    int requiredTotal = 0;

                    String query = "SELECT task FROM quests WHERE id = ?";
                    try (PreparedStatement questStmt = conn.prepareStatement(query)) {
                        questStmt.setInt(1, questId);
                        try (ResultSet rs = questStmt.executeQuery()) {
                            if (rs.next()) {
                                String taskJson = rs.getString("task");
                                List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, new TypeToken<List<Map<String, Object>>>() {}.getType());

                                for (Map<String, Object> task : taskDetails) {
                                    String taskType = (String) task.get("type");
                                    String target = task.get("target") != null ? (String) task.get("target") : "UNKNOWN";
                                    int requiredAmount = ((Number) task.get("amount")).intValue();

                                    String progressKey = taskType + ":" + target;
                                    int currentProgress = progressMap.getOrDefault(progressKey, 0);

                                    totalProgress += Math.min(currentProgress, requiredAmount);
                                    requiredTotal += requiredAmount;
                                }
                            }
                        }
                    }

                    String updateQuery = "UPDATE playerprogress SET tasks = ?, progress = ? WHERE player_id = ? AND quest_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setString(1, progressJson);
                        updateStmt.setInt(2, totalProgress);
                        updateStmt.setString(3, playerUUID);
                        updateStmt.setInt(4, questId);
                        updateStmt.executeUpdate();
                    }

                    if (totalProgress >= requiredTotal) {
                        completeQuest(player, questId);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void handleQuestProgress(Player player, int questId, String taskType, String target, int amount) {
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement checkStmt = conn.prepareStatement(
                             "SELECT isQuestforPlayerAvailable, completed, tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {

                    checkStmt.setString(1, playerUUID);
                    checkStmt.setInt(2, questId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (!rs.next() || rs.getInt("isQuestforPlayerAvailable") == 0 || rs.getBoolean("completed")) {
                        return;
                    }

                    Gson gson = new Gson();
                    Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();
                    Map<String, Integer> progressMap = gson.fromJson(rs.getString("tasks"), progressType);
                    if (progressMap == null) progressMap = new HashMap<>();

                    String progressKey = taskType + ":" + target;
                    int currentProgress = progressMap.getOrDefault(progressKey, 0);
                    int requiredAmount = getRequiredAmount(questId, taskType, target, conn);

                    if (currentProgress < requiredAmount) {
                        int newProgress = Math.min(currentProgress + amount, requiredAmount);
                        progressMap.put(progressKey, newProgress);

                        String updateQuery = "UPDATE playerprogress SET tasks = ?, progress = ? WHERE player_id = ? AND quest_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                            updateStmt.setString(1, gson.toJson(progressMap));
                            updateStmt.setInt(2, newProgress);
                            updateStmt.setString(3, playerUUID);
                            updateStmt.setInt(4, questId);
                            updateStmt.executeUpdate();
                        }

                        if (newProgress >= requiredAmount) {
                            completeQuest(player, questId);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private int getRequiredAmount(int questId, String taskType, String target, Connection conn) throws SQLException {
        Gson gson = new Gson();
        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();

        String query = "SELECT task FROM quests WHERE id = ?";
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, questId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    List<Map<String, Object>> taskList = gson.fromJson(rs.getString("task"), taskListType);
                    for (Map<String, Object> task : taskList) {
                        if (taskType.equals(task.get("type")) && target.equals(task.get("target"))) {
                            return ((Number) task.get("amount")).intValue();
                        }
                    }
                }
            }
        }
        return Integer.MAX_VALUE;
    }

    @EventHandler
    public void onPlayerReachesCoords(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;

        double playerX = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double playerZ = player.getLocation().getZ();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%reachCoordinates%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?");
                     PreparedStatement updateStmt = conn.prepareStatement(
                             "UPDATE playerprogress SET tasks = ?, progress = ? WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            boolean progressUpdated = false;
                            int totalProgress = 0;
                            int requiredTotal = 0;

                            for (Map<String, Object> task : taskDetails) {
                                if ("reachCoordinates".equals(task.get("type"))) {
                                    double requiredX = ((Number) task.getOrDefault("x", 0)).doubleValue();
                                    double requiredY = ((Number) task.getOrDefault("y", 0)).doubleValue();
                                    double requiredZ = ((Number) task.getOrDefault("z", 0)).doubleValue();
                                    int requiredAmount = ((Number) task.getOrDefault("amount", 1)).intValue();
                                    double tolerance = 3;

                                    requiredTotal += requiredAmount;
                                    String progressKey = "reachCoordinates:" + requiredX + "," + requiredY + "," + requiredZ;
                                    int currentProgress = progressMap.getOrDefault(progressKey, 0);

                                    double distanceXZ = Math.sqrt(Math.pow(playerX - requiredX, 2) + Math.pow(playerZ - requiredZ, 2));
                                    boolean yMatch = Math.abs(playerY - requiredY) <= tolerance;

                                    if (distanceXZ <= tolerance && yMatch) {
                                        int newProgress = Math.min(currentProgress + 1, requiredAmount);
                                        progressMap.put(progressKey, newProgress);
                                        progressUpdated = true;
                                    }
                                }
                            }

                            if (progressUpdated) {
                                totalProgress = progressMap.values().stream().mapToInt(Integer::intValue).sum();

                                updateStmt.setString(1, gson.toJson(progressMap));
                                updateStmt.setInt(2, totalProgress);
                                updateStmt.setString(3, playerUUID);
                                updateStmt.setInt(4, questId);
                                updateStmt.executeUpdate();

                                if (totalProgress >= requiredTotal) {
                                    completeQuest(player, questId);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof BlockState blockState)) return;

        Location inventoryLocation = blockState.getLocation();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%openInterface%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?");
                     PreparedStatement updateStmt = conn.prepareStatement(
                             "UPDATE playerprogress SET tasks = ?, progress = ? WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            boolean progressUpdated = false;
                            int totalProgress = 0;
                            int requiredTotal = 0;

                            for (Map<String, Object> task : taskDetails) {
                                if ("openInterface".equals(task.get("type"))) {
                                    double requiredX = ((Number) task.getOrDefault("x", 0)).doubleValue();
                                    double requiredY = ((Number) task.getOrDefault("y", 0)).doubleValue();
                                    double requiredZ = ((Number) task.getOrDefault("z", 0)).doubleValue();
                                    int requiredAmount = ((Number) task.getOrDefault("amount", 1)).intValue();

                                    requiredTotal += requiredAmount;
                                    String progressKey = "openInterface:" + requiredX + "," + requiredY + "," + requiredZ;
                                    int currentProgress = progressMap.getOrDefault(progressKey, 0);

                                    if (inventoryLocation.getBlockX() == requiredX &&
                                            inventoryLocation.getBlockY() == requiredY &&
                                            inventoryLocation.getBlockZ() == requiredZ) {
                                        int newProgress = Math.min(currentProgress + 1, requiredAmount);
                                        progressMap.put(progressKey, newProgress);
                                        progressUpdated = true;
                                    }
                                }
                            }

                            if (progressUpdated) {
                                totalProgress = progressMap.values().stream().mapToInt(Integer::intValue).sum();

                                updateStmt.setString(1, gson.toJson(progressMap));
                                updateStmt.setInt(2, totalProgress);
                                updateStmt.setString(3, playerUUID);
                                updateStmt.setInt(4, questId);
                                updateStmt.executeUpdate();

                                if (totalProgress >= requiredTotal) {
                                    completeQuest(player, questId);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Entity killedEntity = event.getEntity();

        ItemStack usedWeapon = null;
        EntityDamageEvent lastDamage = killedEntity.getLastDamageCause();
        if (lastDamage instanceof EntityDamageByEntityEvent) {
            EntityDamageByEntityEvent edbee = (EntityDamageByEntityEvent) lastDamage;
            Entity damager = edbee.getDamager();
            if (damager instanceof Projectile) {
                Projectile projectile = (Projectile) damager;
                if (projectile.getShooter() instanceof Player) {
                    if (projectile instanceof Arrow) {
                        usedWeapon = new ItemStack(Material.BOW);
                    }
                }
            } else if (damager instanceof Player) {
                usedWeapon = killer.getInventory().getItemInMainHand();
            }
        }
        if (usedWeapon == null) {
            usedWeapon = killer.getInventory().getItemInMainHand();
        }

        String weaponName = usedWeapon != null ? usedWeapon.getType().toString() : "none";
        getLogger().info("onEntityDeath: Spieler " + killer.getName() + " hat " +
                killedEntity.getType().toString() + " getötet mit " + weaponName);

        ItemStack finalUsedWeapon = usedWeapon;
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    String query = "SELECT quest_id, tasks FROM playerprogress " +
                            "WHERE player_id = ? AND completed = 0 AND isQuestforPlayerAvailable = 1";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, killer.getUniqueId().toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            Gson gson = new Gson();
                            Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();
                            while (rs.next()) {
                                int questId = rs.getInt("quest_id");
                                String tasksJson = rs.getString("tasks");
                                Map<String, Integer> progressMap;
                                if (tasksJson == null || tasksJson.trim().isEmpty()) {
                                    progressMap = new HashMap<>();
                                } else {
                                    progressMap = gson.fromJson(tasksJson, progressType);
                                    if (progressMap == null) progressMap = new HashMap<>();
                                }

                                String questQuery = "SELECT task FROM quests WHERE id = ?";
                                try (PreparedStatement questStmt = conn.prepareStatement(questQuery)) {
                                    questStmt.setInt(1, questId);
                                    try (ResultSet questRs = questStmt.executeQuery()) {
                                        boolean hasKillEntityTask = false;
                                        String requiredWeapon = null;
                                        if (questRs.next()) {
                                            String taskJson = questRs.getString("task");
                                            List<Map<String, Object>> taskList = gson.fromJson(
                                                    taskJson,
                                                    new TypeToken<List<Map<String, Object>>>() {}.getType()
                                            );
                                            if (taskList != null) {
                                                for (Map<String, Object> task : taskList) {
                                                    String taskType = (String) task.get("type");
                                                    if ("killEntity".equalsIgnoreCase(taskType)) {
                                                        String target = task.get("target") != null ? (String) task.get("target") : "UNKNOWN";
                                                        if (target.equalsIgnoreCase(killedEntity.getType().toString())) {
                                                            if (task.containsKey("weapon")) {
                                                                requiredWeapon = (String) task.get("weapon");
                                                                if (finalUsedWeapon != null &&
                                                                        finalUsedWeapon.getType().toString().equalsIgnoreCase(requiredWeapon)) {
                                                                    hasKillEntityTask = true;
                                                                }
                                                            } else {
                                                                hasKillEntityTask = true;
                                                            }
                                                            if (hasKillEntityTask) {
                                                                break;
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (hasKillEntityTask) {
                                            String progressKey = "killEntity:" + killedEntity.getType().toString();
                                            if (requiredWeapon != null) {
                                                progressKey += ":" + requiredWeapon;
                                            }
                                            int current = progressMap.getOrDefault(progressKey, 0);
                                            progressMap.put(progressKey, current + 1);
                                            updateTaskProgress(killer, questId, progressMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    getLogger().severe("Fehler in onEntityDeath: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Quest.this);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%placeBlock%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            for (Map<String, Object> task : taskDetails) {
                                if ("placeBlock".equals(task.get("type"))) {
                                    String targetBlock = (String) task.get("target");
                                    int requiredAmount = ((Number) task.get("amount")).intValue();

                                    if (blockName.equalsIgnoreCase(targetBlock)) {
                                        String progressKey = "placeBlock:" + targetBlock;
                                        int currentProgress = progressMap.getOrDefault(progressKey, 0);
                                        int newProgress = Math.min(currentProgress + 1, requiredAmount);
                                        progressMap.put(progressKey, newProgress);
                                    }
                                }
                            }

                            Location loc = event.getBlock().getLocation();
                            Material type = event.getBlock().getType();

                            if (!isCrop(type)) {
                                UUID playerId = player.getUniqueId();
                                long now = System.currentTimeMillis();
                                Map<Location, Long> playerCooldowns = blockActionCooldowns.computeIfAbsent(playerId, k -> new HashMap<>());
                                Long lastAction = playerCooldowns.get(loc);
                                if (lastAction != null && now - lastAction < COOLDOWN_MILLIS) {
                                    return;
                                }
                                playerCooldowns.put(loc, now);
                            }

                            updateTaskProgress(player, questId, progressMap);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        Entity caughtEntity = event.getCaught();

        if (!(caughtEntity instanceof Item caughtItem)) return;

        Material fishMaterial = caughtItem.getItemStack().getType();
        String fishName = fishMaterial.name();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%fishItem%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            for (Map<String, Object> task : taskDetails) {
                                if ("fishItem".equals(task.get("type"))) {
                                    String targetFish = (String) task.get("target");
                                    int requiredAmount = ((Number) task.get("amount")).intValue();

                                    if (fishName.equalsIgnoreCase(targetFish)) {
                                        String progressKey = "fishItem:" + targetFish;
                                        int currentProgress = progressMap.getOrDefault(progressKey, 0);
                                        int newProgress = Math.min(currentProgress + 1, requiredAmount);
                                        progressMap.put(progressKey, newProgress);
                                    }
                                }
                            }

                            updateTaskProgress(player, questId, progressMap);
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onCraftItem(CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String playerUUID = player.getUniqueId().toString();
        ItemStack result = event.getRecipe().getResult();
        String craftedItem = result.getType().toString();
        int amountCrafted = result.getAmount();

        if (event.isShiftClick()) {
            int maxCraftable = getMaxCraftable(event.getInventory());
            amountCrafted *= maxCraftable;
        }

        final int finalAmount = amountCrafted;

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%craftItem%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            boolean progressUpdated = false;

                            for (Map<String, Object> task : taskDetails) {
                                if ("craftItem".equals(task.get("type"))) {
                                    String targetItem = ((String) task.get("target")).toUpperCase();
                                    int requiredAmount = ((Number) task.get("amount")).intValue();

                                    if (craftedItem.equalsIgnoreCase(targetItem)) {
                                        String progressKey = "craftItem:" + targetItem;
                                        int currentProgress = progressMap.getOrDefault(progressKey, 0);
                                        int newProgress = Math.min(currentProgress + finalAmount, requiredAmount);
                                        progressMap.put(progressKey, newProgress);
                                        progressUpdated = true;
                                    }
                                }
                            }

                            if (progressUpdated) {
                                updateTaskProgress(player, questId, progressMap);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private int getMaxCraftable(CraftingInventory inv) {
        int minAmount = Integer.MAX_VALUE;
        for (ItemStack item : inv.getMatrix()) {
            if (item != null && item.getType() != Material.AIR) {
                minAmount = Math.min(minAmount, item.getAmount());
            }
        }
        return minAmount == Integer.MAX_VALUE ? 1 : minAmount;
    }

    @EventHandler
    public void onFurnaceTake(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getType() != InventoryType.FURNACE) return;

        if (event.getSlot() != 2) return;

        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        String smeltedItem = clickedItem.getType().name();
        int amountTaken = clickedItem.getAmount();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%smeltItem%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            boolean progressUpdated = false;
                            int totalProgress = 0;
                            int requiredTotal = 0;

                            for (Map<String, Object> task : taskDetails) {
                                if ("smeltItem".equals(task.get("type"))) {
                                    String targetItem = (String) task.get("target");
                                    int requiredAmount = ((Number) task.get("amount")).intValue();
                                    requiredTotal += requiredAmount;

                                    if (smeltedItem.equalsIgnoreCase(targetItem)) {
                                        String progressKey = "smeltItem:" + targetItem;
                                        int currentProgress = progressMap.getOrDefault(progressKey, 0);

                                        int newProgress = Math.min(currentProgress + amountTaken, requiredAmount);
                                        progressMap.put(progressKey, newProgress);
                                        progressUpdated = true;
                                    }
                                }
                            }

                            if (progressUpdated) {
                                totalProgress = progressMap.values().stream().mapToInt(Integer::intValue).sum();
                                updateTaskProgress(player, questId, progressMap);

                                if (totalProgress >= requiredTotal) {
                                    completeQuest(player, questId);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }


    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
                event.getFrom().getBlockY() == event.getTo().getBlockY() &&
                event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        enforceZoneRestrictions(player);


        Location playerLocation = player.getLocation();
        Block blockBelow = playerLocation.clone().subtract(0, 1, 0).getBlock();
        Material blockType = blockBelow.getType();
        double currentHeight = playerLocation.getY();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(
                             "SELECT id, task FROM quests " +
                                     "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id " +
                                     "WHERE playerprogress.player_id = ? " +
                                     "AND playerprogress.isQuestforPlayerAvailable = 1 " +
                                     "AND quests.task LIKE '%standOnBlock%'");
                     PreparedStatement progressStmt = conn.prepareStatement(
                             "SELECT tasks FROM playerprogress WHERE player_id = ? AND quest_id = ?");
                     PreparedStatement updateStmt = conn.prepareStatement(
                             "UPDATE playerprogress SET tasks = ?, progress = ? WHERE player_id = ? AND quest_id = ?")) {

                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        Gson gson = new Gson();
                        Type taskListType = new TypeToken<List<Map<String, Object>>>() {}.getType();
                        Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task");
                            List<Map<String, Object>> taskDetails = gson.fromJson(taskJson, taskListType);

                            progressStmt.setString(1, playerUUID);
                            progressStmt.setInt(2, questId);
                            ResultSet progressRs = progressStmt.executeQuery();
                            Map<String, Integer> progressMap = new HashMap<>();
                            if (progressRs.next()) {
                                String progressJson = progressRs.getString("tasks");
                                if (progressJson != null && !progressJson.isEmpty()) {
                                    progressMap = gson.fromJson(progressJson, progressType);
                                }
                            }

                            boolean progressUpdated = false;
                            int totalProgress = 0;
                            int requiredTotal = 0;

                            for (Map<String, Object> task : taskDetails) {
                                if ("standOnBlock".equals(task.get("type"))) {
                                    String requiredBlock = (String) task.getOrDefault("block", "STONE");
                                    int requiredHeight = ((Number) task.getOrDefault("height", 0)).intValue();
                                    int requiredAmount = ((Number) task.getOrDefault("amount", 1)).intValue();

                                    requiredTotal += requiredAmount;
                                    String progressKey = "standOnBlock:" + requiredBlock + ":" + requiredHeight;
                                    int currentProgress = progressMap.getOrDefault(progressKey, 0);

                                    if (blockType.name().equalsIgnoreCase(requiredBlock) && currentHeight >= requiredHeight) {
                                        if (currentProgress == 0) {
                                            progressMap.put(progressKey, 1);
                                            progressUpdated = true;
                                        }
                                    }
                                }
                            }

                            if (progressUpdated) {
                                totalProgress = progressMap.values().stream().mapToInt(Integer::intValue).sum();

                                updateStmt.setString(1, gson.toJson(progressMap));
                                updateStmt.setInt(2, totalProgress);
                                updateStmt.setString(3, playerUUID);
                                updateStmt.setInt(4, questId);
                                updateStmt.executeUpdate();

                                if (totalProgress >= requiredTotal) {
                                    completeQuest(player, questId);
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        if (player == null) return;

        EntityDamageEvent lastDamage = player.getLastDamageCause();
        if (lastDamage == null) return;

        String deathCause = lastDamage.getCause().name();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    String query = "SELECT quest_id, tasks FROM playerprogress " +
                            "WHERE player_id = ? AND completed = 0 AND isQuestforPlayerAvailable = 1";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, player.getUniqueId().toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            Gson gson = new Gson();
                            Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                            while (rs.next()) {
                                int questId = rs.getInt("quest_id");
                                String tasksJson = rs.getString("tasks");
                                Map<String, Integer> progressMap;
                                if (tasksJson == null || tasksJson.trim().isEmpty()) {
                                    progressMap = new HashMap<>();
                                } else {
                                    progressMap = gson.fromJson(tasksJson, progressType);
                                    if (progressMap == null) progressMap = new HashMap<>();
                                }

                                String questQuery = "SELECT task FROM quests WHERE id = ?";
                                try (PreparedStatement questStmt = conn.prepareStatement(questQuery)) {
                                    questStmt.setInt(1, questId);
                                    try (ResultSet questRs = questStmt.executeQuery()) {
                                        boolean hasDeathTask = false;
                                        if (questRs.next()) {
                                            String taskJson = questRs.getString("task");
                                            List<Map<String, Object>> taskList = gson.fromJson(taskJson,
                                                    new TypeToken<List<Map<String, Object>>>() {}.getType());
                                            if (taskList != null) {
                                                for (Map<String, Object> task : taskList) {
                                                    String taskType = (String) task.get("type");
                                                    if ("death".equalsIgnoreCase(taskType)) {
                                                        String target = task.get("target") != null ? (String) task.get("target") : "UNKNOWN";
                                                        if (target.equalsIgnoreCase(deathCause)) {
                                                            hasDeathTask = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (hasDeathTask) {
                                            String progressKey = "death:" + deathCause;
                                            int current = progressMap.getOrDefault(progressKey, 0);
                                            progressMap.put(progressKey, current + 1);
                                            updateTaskProgress(player, questId, progressMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Quest.this);
    }

    @EventHandler
    public void onPlayerAdvancementDone(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        if (player == null) return;

        Advancement advancement = event.getAdvancement();
        if (advancement == null) return;

        String advancementKey = advancement.getKey().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    String query = "SELECT quest_id, tasks FROM playerprogress " +
                            "WHERE player_id = ? AND completed = 0 AND isQuestforPlayerAvailable = 1";
                    try (PreparedStatement ps = conn.prepareStatement(query)) {
                        ps.setString(1, player.getUniqueId().toString());
                        try (ResultSet rs = ps.executeQuery()) {
                            Gson gson = new Gson();
                            Type progressType = new TypeToken<Map<String, Integer>>() {}.getType();

                            while (rs.next()) {
                                int questId = rs.getInt("quest_id");
                                String tasksJson = rs.getString("tasks");
                                Map<String, Integer> progressMap;
                                if (tasksJson == null || tasksJson.trim().isEmpty()) {
                                    progressMap = new HashMap<>();
                                } else {
                                    progressMap = gson.fromJson(tasksJson, progressType);
                                    if (progressMap == null) progressMap = new HashMap<>();
                                }

                                String questQuery = "SELECT task FROM quests WHERE id = ?";
                                try (PreparedStatement questStmt = conn.prepareStatement(questQuery)) {
                                    questStmt.setInt(1, questId);
                                    try (ResultSet questRs = questStmt.executeQuery()) {
                                        boolean hasAchievementTask = false;
                                        if (questRs.next()) {
                                            String taskJson = questRs.getString("task");
                                            List<Map<String, Object>> taskList = gson.fromJson(taskJson,
                                                    new TypeToken<List<Map<String, Object>>>() {}.getType());
                                            if (taskList != null) {
                                                for (Map<String, Object> task : taskList) {
                                                    String taskType = (String) task.get("type");
                                                    if ("achievement".equalsIgnoreCase(taskType)) {
                                                        String target = task.get("target") != null ? (String) task.get("target") : "UNKNOWN";
                                                        if (target.equalsIgnoreCase(advancementKey)) {
                                                            hasAchievementTask = true;
                                                            break;
                                                        }
                                                    }
                                                }
                                            }
                                        }

                                        if (hasAchievementTask) {
                                            String progressKey = "achievement:" + advancementKey;
                                            int current = progressMap.getOrDefault(progressKey, 0);
                                            progressMap.put(progressKey, current + 1);

                                            updateTaskProgress(player, questId, progressMap);
                                        }
                                    }
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(Quest.this);
    }


    private boolean hasCompletedQuests(Player player, List<Integer> requiredQuestIds) {
        String playerUUID = player.getUniqueId().toString();
        for (int questId : requiredQuestIds) {
            try (Connection conn = getConnection();
                 PreparedStatement stmt = conn.prepareStatement(
                         "SELECT completed FROM playerprogress WHERE player_id = ? AND quest_id = ?")) {
                stmt.setString(1, playerUUID);
                stmt.setInt(2, questId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (!rs.next() || !rs.getBoolean("completed")) {
                        return false;
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    private void enforceZoneRestrictions(Player player) {
        double playerY = player.getLocation().getY();

        for (Map<String, Object> zone : restrictedZones) {
            double minY = (double) zone.get("minY");
            List<Integer> requiredQuests = (List<Integer>) zone.get("requiredQuests");
            Location safeLocation = (Location) zone.get("safeLocation");

            if (playerY < minY) {
                if (!hasCompletedQuests(player, requiredQuests)) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.teleport(safeLocation);
                            player.sendMessage(ChatColor.RED + "Du hast noch nicht alle notwendigen Quests abgeschlossen! Du wurdest an einen sicheren Ort teleportiert.");
                        }
                    }.runTask(Quest.this);
                    break;
                }
            }
        }
    }


    private final List<Map<String, Object>> restrictedZones = new ArrayList<>();

    public void setupRestrictedZones() {
        Map<String, Object> zone1 = new HashMap<>();
        zone1.put("minY", 43.0);
        zone1.put("requiredQuests", Arrays.asList(1));
        zone1.put("safeLocation", new Location(Bukkit.getWorld("world"), -130, 68, -2));

        Map<String, Object> zone2 = new HashMap<>();
        zone2.put("minY", 3.0);
        zone2.put("requiredQuests", Arrays.asList(2));
        zone2.put("safeLocation", new Location(Bukkit.getWorld("world"), -130, 68, -2));

        Map<String, Object> zone3 = new HashMap<>();
        zone3.put("minY", -33.0);
        zone3.put("requiredQuests", Arrays.asList(2));
        zone3.put("safeLocation", new Location(Bukkit.getWorld("world"), -130, 68, -2));

        restrictedZones.add(zone1);
        restrictedZones.add(zone2);
        restrictedZones.add(zone3);
    }

    //Villager Shop
    @EventHandler
    public void onPlayerInteractShopKeeper(PlayerInteractEntityEvent event) {
        if (!(event.getRightClicked() instanceof Villager)) return;

        Villager villager = (Villager) event.getRightClicked();
        Player player = event.getPlayer();

        Location shopLocation = new Location(Bukkit.getWorld("world"), -128, 72, -14);

        double tolerance = 2.0;
        if (!villager.getLocation().getWorld().equals(shopLocation.getWorld()) ||
                villager.getLocation().distance(shopLocation) > tolerance) {
            return;
        }

        event.setCancelled(true);
        shopInv(player);
    }

    @EventHandler
    public void onShopClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        if (event.getView().getTitle().equals("Shop")) {
            event.setCancelled(true);
            if (event.getSlot() == 14 && event.getCurrentItem().getType() == Material.DIAMOND) {
                event.getWhoClicked().closeInventory();
                sellInv(player, 0);
            } else if (event.getSlot() == 12 && event.getCurrentItem().getType() == Material.EMERALD) {
                event.getWhoClicked().closeInventory();
                buyInv(player, 0);
            }
        }

    }

    public void shopInv(Player player) {
        Inventory shopInventory = Bukkit.createInventory(null, 27, "Shop");

        ItemStack diamond = new ItemStack(Material.DIAMOND, 1);
        ItemMeta sellMeta = diamond.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName(ChatColor.AQUA + "Verkaufen");
            diamond.setItemMeta(sellMeta);
        }

        ItemStack emerald = new ItemStack(Material.EMERALD, 1);
        ItemMeta buyMeta = emerald.getItemMeta();
        if (buyMeta != null) {
            buyMeta.setDisplayName(ChatColor.GREEN + "Kaufen");
            emerald.setItemMeta(buyMeta);
        }

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }

        for (int i = 0; i < shopInventory.getSize(); i++) {
            shopInventory.setItem(i, glassPane);
        }

        shopInventory.setItem(14, diamond);
        shopInventory.setItem(12, emerald);

        player.openInventory(shopInventory);
    }


    @EventHandler
    public void onSellInventoryClose(InventoryCloseEvent event) {
        if (!event.getView().getTitle().equals("Verkaufen")) return;

        Player player = (Player) event.getPlayer();
        Inventory inv = event.getInventory();

        for (int slot = 0; slot < 27; slot++) {
            ItemStack item = inv.getItem(slot);
            if (item == null) continue;

            Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
            if (!leftover.isEmpty()) {
                Location loc = player.getLocation();
                for (ItemStack it : leftover.values()) {
                    player.getWorld().dropItemNaturally(loc, it);
                }
            }
            inv.setItem(slot, null);
        }
    }

    public class BuyOffer {
        private final ItemStack item;
        private final int price;
        private final int amount;
        private int onetimebought;
        private final List<Integer> requiredQuests;


        public BuyOffer(ItemStack item, int price, int amount, int onetimebought,  List<Integer> requiredQuests) {
            this.item = item;
            this.price = price;
            this.amount = amount;
            this.onetimebought = onetimebought;
            this.requiredQuests = requiredQuests;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getPrice() {
            return price;
        }

        public int getAmount() {
            return amount;
        }
        public int isOnetimebought() {
            return onetimebought;
        }

        public List<Integer> getRequiredQuests() {
            return requiredQuests;
        }
    }

    private final Map<Integer, BuyOffer> buyOffers = new HashMap<>();

    private void setupBuyOffers() {
        addBuyOffer(Material.COAL, 2, 64, 1, Arrays.asList());
        addBuyOffer(createUnbreakableIronChestplate(), 10, 1, 1, Arrays.asList());
        addBuyOffer(Material.BREAD, 3, 64, 1, Arrays.asList());
        addBuyOffer(Material.CHICKEN_SPAWN_EGG, 6, 2, 1, Arrays.asList());
        addBuyOffer(Material.COBBLESTONE, 2, 64, 1, Arrays.asList());
        addBuyOffer(Material.OAK_SAPLING, 1, 1, 1, Arrays.asList());
        addBuyOffer(Material.BIRCH_SAPLING, 1, 1, 1, Arrays.asList());
        addBuyOffer(Material.SPRUCE_SAPLING, 4, 1, 1, Arrays.asList());
        addBuyOffer(Material.DARK_OAK_SAPLING, 4, 1, 1, Arrays.asList());
        addBuyOffer(Material.JUNGLE_SAPLING, 8, 1, 1, Arrays.asList(17));
        addBuyOffer(Material.ACACIA_SAPLING, 8, 1, 1, Arrays.asList(17));
        addBuyOffer(Material.CHERRY_SAPLING, 16, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.MANGROVE_PROPAGULE, 16, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.DIRT, 2, 16, 1, Arrays.asList());
        addBuyOffer(Material.BONE, 1, 5, 1, Arrays.asList());
        addBuyOffer(Material.TRIDENT, 25, 1, 1, Arrays.asList());
        addBuyOffer(Material.COW_SPAWN_EGG, 4, 2, 1, Arrays.asList());
        addBuyOffer(Material.WHEAT_SEEDS, 8, 1, 1, Arrays.asList());
        addBuyOffer(Material.CACTUS, 8, 1, 1, Arrays.asList());
        addBuyOffer(Material.DIAMOND_LEGGINGS, 20, 1, 1, Arrays.asList(17));
        addBuyOffer(Material.COPPER_BLOCK, 1, 5, 1, Arrays.asList(17));
        addBuyOffer(Material.ENCHANTING_TABLE, 50, 1, 1, Arrays.asList(17));
        addBuyOffer(Material.RAW_IRON, 3, 16, 1, Arrays.asList(17));
        addBuyOffer(Material.REDSTONE, 2, 64, 1, Arrays.asList(17));
        addBuyOffer(Material.WOLF_SPAWN_EGG, 10, 1, 2, Arrays.asList(17));
        addBuyOffer(Material.WOLF_ARMOR, 10, 1, 1, Arrays.asList(17));
        addBuyOffer(Material.WIND_CHARGE, 10, 24, 1, Arrays.asList(17));
        addBuyOffer(Material.LAVA_BUCKET, 5, 1, 1, Arrays.asList(17));
        addBuyOffer(Material.LAPIS_LAZULI, 20, 6, 1, Arrays.asList(17));
        addBuyOffer(Material.DIAMOND, 8, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.WITHER_SKELETON_SKULL, 64, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.SOUL_SAND, 2, 4, 1, Arrays.asList(37));
        addBuyOffer(Material.ANCIENT_DEBRIS, 50, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.NETHERITE_UPGRADE_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.SENTRY_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.VEX_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.WILD_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.COAST_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.DUNE_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.WARD_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.SILENCE_ARMOR_TRIM_SMITHING_TEMPLATE, 64, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.SNOUT_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.RIB_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.EYE_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.SPIRE_ARMOR_TRIM_SMITHING_TEMPLATE, 10, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.WAYFINDER_ARMOR_TRIM_SMITHING_TEMPLATE, 20, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.RAISER_ARMOR_TRIM_SMITHING_TEMPLATE, 20, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.SHAPER_ARMOR_TRIM_SMITHING_TEMPLATE, 20, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.HOST_ARMOR_TRIM_SMITHING_TEMPLATE, 20, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.BEE_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.LLAMA_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.PANDA_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.ARMADILLO_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.CAMEL_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.CAT_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.CHICKEN_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.COW_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.DONKEY_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.FOX_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.FROG_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.HORSE_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.PIG_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
        addBuyOffer(Material.SHEEP_SPAWN_EGG, 40, 1, 1, Arrays.asList(37));
    }

    private void addBuyOffer(Material material, int price, int amount, int onetimebought, List<Integer> requiredQuests) {
        ItemStack item = new ItemStack(material, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            item.setItemMeta(meta);
        }
        buyOffers.put(buyOffers.size(), new BuyOffer(item, price, amount, onetimebought, requiredQuests));
    }
    private void addBuyOffer(ItemStack item, int price, int amount, int onetimebought, List<Integer> requiredQuests) {
        buyOffers.put(buyOffers.size(), new BuyOffer(item, price, amount, onetimebought, requiredQuests));
    }

    private ItemStack createUnbreakableIronChestplate() {
        ItemStack chestplate = new ItemStack(Material.IRON_CHESTPLATE, 1);
        ItemMeta meta = chestplate.getItemMeta();

        meta.setUnbreakable(true);
        chestplate.setItemMeta(meta);
        return chestplate;
    }


    private final Map<Player, Integer> buyPageMap = new HashMap<>();
    private final int ITEMS_PER_PAGE = 28;

    private boolean hasCompletedRequiredQuests(Player player, List<Integer> requiredQuests) {
        if (requiredQuests.isEmpty()) return true;

        String playerUUID = player.getUniqueId().toString();
        Set<Integer> completedQuests = new HashSet<>();

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT quest_id FROM playerprogress WHERE player_id = ? AND completed = true")) {
            stmt.setString(1, playerUUID);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    completedQuests.add(rs.getInt("quest_id"));
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }

        return completedQuests.containsAll(requiredQuests);
    }


    public void buyInv(Player player, int pageIndex) {
        buyPageMap.put(player, pageIndex);

        Inventory buyInv = Bukkit.createInventory(null, 54, "Kaufen - Seite " + (pageIndex + 1));

        int[] allowedSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        ItemStack Exit = new ItemStack(Material.BARRIER, 1);
        ItemMeta exitMeta = Exit.getItemMeta();
        if (exitMeta != null) {
            exitMeta.setDisplayName(ChatColor.RED + "EXIT");
            Exit.setItemMeta(exitMeta);
        }
        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < buyInv.getSize(); i++) {
            buyInv.setItem(i, glassPane);
        }

        List<BuyOffer> offerList = new ArrayList<>();
        for (BuyOffer offer : buyOffers.values()) {
            if (hasCompletedRequiredQuests(player, offer.getRequiredQuests())) {
                offerList.add(offer);
            }
        }

        int totalPages = (int) Math.ceil((double) offerList.size() / allowedSlots.length);
        if (pageIndex >= totalPages) pageIndex = totalPages - 1;


        List<BuyOffer> offerList1 = new ArrayList<>(buyOffers.values());
        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, offerList.size());

        for (int i = startIndex, slotIndex = 0; i < endIndex; i++, slotIndex++) {
            BuyOffer offer = offerList.get(i);
            ItemStack displayItem = offer.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                meta.setLore(Arrays.asList(
                        ChatColor.YELLOW + "Preis: " + ChatColor.GOLD + offer.getPrice() + " Perlen",
                        ChatColor.GREEN + "Klicken, um zu kaufen"
                ));
                displayItem.setAmount(offer.getAmount());
                displayItem.setItemMeta(meta);
            }
            boolean isItemAvailable = isItemAvailableForPlayer(player, offer);
            if (slotIndex < allowedSlots.length) {
                if (isItemAvailable) {
                    if (offer.isOnetimebought() == 1) {
                        buyInv.setItem(allowedSlots[slotIndex], displayItem);
                    } else if (offer.isOnetimebought() == 2) {
                        buyInv.setItem(allowedSlots[slotIndex], displayItem);
                    }
                } else {
                    ItemStack unavailableItem = new ItemStack(Material.BARRIER);
                    ItemMeta unavailableMeta = unavailableItem.getItemMeta();
                    if (unavailableMeta != null) {
                        unavailableMeta.setDisplayName(ChatColor.RED + "Nicht verfügbar");
                        unavailableItem.setItemMeta(unavailableMeta);
                    }
                    buyInv.setItem(allowedSlots[slotIndex], unavailableItem);
                }
            }
            buyInv.setItem(45, Exit);
        }

        if (pageIndex > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.YELLOW + "⬅ Vorherige Seite");
                prevPage.setItemMeta(prevMeta);
            }
            buyInv.setItem(48, prevPage);
        }

        if (endIndex < offerList.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.YELLOW + "Nächste Seite ➡");
                nextPage.setItemMeta(nextMeta);
            }
            buyInv.setItem(50, nextPage);
        }

        player.openInventory(buyInv);
    }

    private boolean isItemAvailableForPlayer(Player player, BuyOffer offer) {
        try (Connection conn = getConnection()) {
            String sql = "SELECT available FROM shops WHERE uuid = ? AND item = ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, offer.getItem().getType().name());
                ResultSet rs = stmt.executeQuery();


                while (rs.next()) {
                    if (rs.getBoolean("available")) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }



    public void confirmBuyInv(Player player, BuyOffer offer) {
        Inventory confirmInv = Bukkit.createInventory(null, 27, "Kauf bestätigen");

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < confirmInv.getSize(); i++) {
            confirmInv.setItem(i, glassPane);
        }

        confirmInv.setItem(13, offer.getItem().clone());

        ItemStack buyButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemMeta buyMeta = buyButton.getItemMeta();
        if (buyMeta != null) {
            buyMeta.setDisplayName(ChatColor.GREEN + "Kaufen");
            buyButton.setItemMeta(buyMeta);
        }
        confirmInv.setItem(15, buyButton);

        ItemStack cancelButton = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ChatColor.RED + "Abbrechen");
            cancelButton.setItemMeta(cancelMeta);
        }
        confirmInv.setItem(11, cancelButton);

        player.openInventory(confirmInv);
    }

    public void getLevel(Player player) {

    }

    @EventHandler
    public void onBuyInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("Kaufen - Seite")) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }

        if (clickedItem.getType() == Material.BARRIER) {
            shopInv(player);
            event.setCancelled(true);
        }

        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        for (BuyOffer offer : buyOffers.values()) {
            if (clickedItem.getType() == offer.getItem().getType()) {
                confirmBuyInv(player, offer);
                return;
            }
        }
        int currentPage = buyPageMap.getOrDefault(player, 0);

        if (clickedItem.getType() == Material.ARROW) {
            if (clickedItem.getItemMeta().getDisplayName().contains("⬅")) {
                buyInv(player, currentPage - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("➡")) {
                buyInv(player, currentPage + 1);
            }
            return;
        }

    }


    @EventHandler
    public void onConfirmInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Kauf bestätigen")) return;

        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        Inventory inv = event.getInventory();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
            ItemStack itemToBuy = inv.getItem(13);
            if (itemToBuy == null) return;

            for (BuyOffer offer : buyOffers.values()) {
                if (itemToBuy.isSimilar(offer.getItem())) {
                    int playerPearls = getPearlCount(player);
                    if (playerPearls >= offer.getPrice()) {
                        removePearls(player, offer.getPrice());
                        ItemStack item = offer.getItem().clone();
                        int amount = offer.getAmount();
                        item.setAmount(amount);
                        player.getInventory().addItem(item);
                        player.sendMessage(ChatColor.GREEN + "Du hast " + offer.getItem().getItemMeta().getDisplayName() + " für " + offer.getPrice() + " Perlen gekauft!");
                        if (offer.onetimebought == 2) {
                            updatePlayerShopData(player, offer.getItem().getType().name(), false);
                        }
                    } else {
                        player.sendMessage(ChatColor.RED + "Nicht genügend Perlen!");
                    }
                    player.closeInventory();
                    return;
                }
            }
        } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
            player.closeInventory();
            buyInv(player, 0);
        }
    }

    private void updatePlayerShopData(Player player, String item, boolean available) {
        String playerUUID = player.getUniqueId().toString();

        try (Connection conn = getConnection()) {
            String checkQuery = "SELECT COUNT(*) FROM shops WHERE uuid = ? AND item = ?";
            try (PreparedStatement checkStmt = conn.prepareStatement(checkQuery)) {
                checkStmt.setString(1, playerUUID);
                checkStmt.setString(2, item);
                ResultSet rs = checkStmt.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) {
                    String updateQuery = "UPDATE shops SET available = ? WHERE uuid = ? AND item = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setBoolean(1, available);
                        updateStmt.setString(2, playerUUID);
                        updateStmt.setString(3, item);
                        updateStmt.executeUpdate();
                    }
                } else {
                    String insertQuery = "INSERT INTO shops (uuid, item, available) VALUES (?, ?, ?)";
                    try (PreparedStatement insertStmt = conn.prepareStatement(insertQuery)) {
                        insertStmt.setString(1, playerUUID);
                        insertStmt.setString(2, item);
                        insertStmt.setBoolean(3, available);
                        insertStmt.executeUpdate();
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    private void saveItemToDatabase(Player player, BuyOffer offer) {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO shops (uuid, item, available) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, player.getUniqueId().toString());
                stmt.setString(2, offer.getItem().getType().toString());
                stmt.setBoolean(3, false);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private int getPearlCount(Player player) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.HEART_OF_THE_SEA) {
                count += item.getAmount();
            }
        }
        return count;
    }

    private void removePearls(Player player, int amount) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == Material.HEART_OF_THE_SEA) {
                int stackAmount = item.getAmount();
                if (stackAmount > amount) {
                    item.setAmount(stackAmount - amount);
                    return;
                } else {
                    amount -= stackAmount;
                    item.setAmount(0);
                }
                if (amount <= 0) return;
            }
        }
    }






    public class SellOffer {
        private final ItemStack item;
        private final int price;
        private final int amount;
        private final List<Integer> requiredQuests;

        public SellOffer(ItemStack item, int price, int amount, List<Integer> requiredQuests) {
            this.item = item;
            this.price = price;
            this.amount = amount;
            this.requiredQuests = requiredQuests;
        }

        public ItemStack getItem() {
            return item;
        }

        public int getPrice() {
            return price;
        }

        public int getAmount() {
            return amount;
        }

        public List<Integer> getRequiredQuests() {
            return requiredQuests;
        }
    }


    private final Map<Integer, SellOffer> sellOffers = new HashMap<>();

    private void setupSellOffers() {
        addSellOffer(Material.BEETROOT, 1, 32, Arrays.asList());
        addSellOffer(Material.ROTTEN_FLESH, 1, 40, Arrays.asList());
        addSellOffer(Material.OAK_LOG, 1, 32, Arrays.asList());
        addSellOffer(Material.BIRCH_LOG, 1, 32, Arrays.asList());
        addSellOffer(Material.SPRUCE_LOG, 1, 48, Arrays.asList());
        addSellOffer(Material.DARK_OAK_LOG, 1, 64, Arrays.asList());
        addSellOffer(Material.JUNGLE_LOG, 1, 64, Arrays.asList(17));
        addSellOffer(Material.ACACIA_LOG, 1, 32, Arrays.asList(17));
        addSellOffer(Material.CHERRY_LOG, 1, 32, Arrays.asList(37));
        addSellOffer(Material.MANGROVE_LOG, 1, 32, Arrays.asList(37));
        addSellOffer(Material.COD, 1, 24, Arrays.asList());
        addSellOffer(Material.SALMON, 1, 24, Arrays.asList());
        addSellOffer(Material.TROPICAL_FISH, 1, 24, Arrays.asList());
        addSellOffer(Material.EGG, 1, 24, Arrays.asList());
        addSellOffer(Material.LEATHER, 1, 24, Arrays.asList());
        addSellOffer(Material.BONE, 4, 32, Arrays.asList(17));
        addSellOffer(Material.PUFFERFISH, 2, 10, Arrays.asList(17));
        addSellOffer(Material.CACTUS, 1, 128, Arrays.asList(17));
        addSellOffer(Material.SPIDER_EYE, 2, 24, Arrays.asList(37));
        addSellOffer(Material.STRING, 2, 32, Arrays.asList(37));
        addSellOffer(Material.SUGAR_CANE, 1, 64, Arrays.asList(17));
        addSellOffer(Material.SUGAR, 1, 48, Arrays.asList(37));
    }

    private void addSellOffer(Material material, int price, int amount, List<Integer> requiredQuests) {
        ItemStack item = new ItemStack(material, amount);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            item.setItemMeta(meta);
        }
        sellOffers.put(sellOffers.size(), new SellOffer(item, price, amount, requiredQuests));
    }

    private final Map<Player, Integer> sellPageMap = new HashMap<>();

    public void sellInv(Player player, int pageIndex) {
        sellPageMap.put(player, pageIndex);
        Inventory sellInv = Bukkit.createInventory(null, 54, "Verkaufen - Seite " + (pageIndex + 1));

        int[] allowedSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        ItemStack exitButton = new ItemStack(Material.BARRIER, 1);
        ItemMeta exitMeta = exitButton.getItemMeta();
        if (exitMeta != null) {
            exitMeta.setDisplayName(ChatColor.RED + "EXIT");
            exitButton.setItemMeta(exitMeta);
        }

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < sellInv.getSize(); i++) {
            sellInv.setItem(i, glassPane);
        }

        List<SellOffer> availableOffers = new ArrayList<>();
        for (SellOffer offer : sellOffers.values()) {
            if (hasCompletedRequiredQuests(player, offer.getRequiredQuests())) {
                availableOffers.add(offer);
            }
        }

        int startIndex = pageIndex * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, availableOffers.size());

        for (int i = startIndex, slotIndex = 0; i < endIndex; i++, slotIndex++) {
            SellOffer offer = availableOffers.get(i);
            ItemStack displayItem = offer.getItem().clone();
            ItemMeta meta = displayItem.getItemMeta();
            if (meta != null) {
                meta.setLore(Arrays.asList(
                        ChatColor.YELLOW + "Preis: " + ChatColor.GOLD + offer.getPrice() + " Perlen",
                        ChatColor.GREEN + "Klicken, um zu verkaufen"
                ));
                displayItem.setItemMeta(meta);
            }
            sellInv.setItem(allowedSlots[slotIndex], displayItem);
        }

        sellInv.setItem(45, exitButton);

        if (pageIndex > 0) {
            ItemStack prevPage = new ItemStack(Material.ARROW);
            ItemMeta prevMeta = prevPage.getItemMeta();
            if (prevMeta != null) {
                prevMeta.setDisplayName(ChatColor.YELLOW + "⬅ Vorherige Seite");
                prevPage.setItemMeta(prevMeta);
            }
            sellInv.setItem(48, prevPage);
        }

        if (endIndex < availableOffers.size()) {
            ItemStack nextPage = new ItemStack(Material.ARROW);
            ItemMeta nextMeta = nextPage.getItemMeta();
            if (nextMeta != null) {
                nextMeta.setDisplayName(ChatColor.YELLOW + "Nächste Seite ➡");
                nextPage.setItemMeta(nextMeta);
            }
            sellInv.setItem(50, nextPage);
        }

        player.openInventory(sellInv);
    }


    public void confirmSellInv(Player player, SellOffer offer) {
        Inventory confirmInv = Bukkit.createInventory(null, 27, "Verkauf bestätigen");

        ItemStack glassPane = new ItemStack(Material.GRAY_STAINED_GLASS_PANE, 1);
        ItemMeta glassMeta = glassPane.getItemMeta();
        if (glassMeta != null) {
            glassMeta.setDisplayName(" ");
            glassPane.setItemMeta(glassMeta);
        }
        for (int i = 0; i < confirmInv.getSize(); i++) {
            confirmInv.setItem(i, glassPane);
        }

        confirmInv.setItem(13, offer.getItem().clone());

        ItemStack sellButton = new ItemStack(Material.LIME_STAINED_GLASS_PANE, 1);
        ItemMeta sellMeta = sellButton.getItemMeta();
        if (sellMeta != null) {
            sellMeta.setDisplayName(ChatColor.GREEN + "Verkaufen");
            sellButton.setItemMeta(sellMeta);
        }
        confirmInv.setItem(15, sellButton);

        ItemStack cancelButton = new ItemStack(Material.RED_STAINED_GLASS_PANE, 1);
        ItemMeta cancelMeta = cancelButton.getItemMeta();
        if (cancelMeta != null) {
            cancelMeta.setDisplayName(ChatColor.RED + "Abbrechen");
            cancelButton.setItemMeta(cancelMeta);
        }
        confirmInv.setItem(11, cancelButton);

        player.openInventory(confirmInv);
    }


    @EventHandler
    public void onSellInventoryClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().startsWith("Verkaufen - Seite")) return;

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            event.setCancelled(true);
            return;
        }

        if (clickedItem.getType() == Material.BARRIER) {
            shopInv(player);
            event.setCancelled(true);
            return;
        }

        if (clickedItem.getType() == Material.GRAY_STAINED_GLASS_PANE) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        for (SellOffer offer : sellOffers.values()) {
            if (clickedItem.getType() == offer.getItem().getType()) {
                confirmSellInv(player, offer);
                return;
            }
        }

        int currentPage = sellPageMap.getOrDefault(player, 0);
        if (clickedItem.getType() == Material.ARROW) {
            if (clickedItem.getItemMeta().getDisplayName().contains("⬅")) {
                sellInv(player, currentPage - 1);
            } else if (clickedItem.getItemMeta().getDisplayName().contains("➡")) {
                sellInv(player, currentPage + 1);
            }
        }
    }

    @EventHandler
    public void onConfirmSellClick(InventoryClickEvent event) {
        if (!event.getView().getTitle().equals("Verkauf bestätigen")) return;
        event.setCancelled(true);

        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();
        if (clickedItem == null || clickedItem.getType() == Material.AIR) return;

        if (clickedItem.getType() == Material.LIME_STAINED_GLASS_PANE) {
            ItemStack itemToSell = event.getInventory().getItem(13);
            if (itemToSell == null) return;

            for (SellOffer offer : sellOffers.values()) {
                if (itemToSell.getType() == offer.getItem().getType()) {
                    int amountToSell = offer.getAmount();
                    int price = offer.getPrice();

                    if (getItemCount(player, itemToSell.getType()) < amountToSell) {
                        player.sendMessage(ChatColor.RED + "Du hast nicht genug Items zum Verkaufen!");
                        return;
                    }

                    removeItems(player, itemToSell.getType(), amountToSell);
                    addPearls(player, price);

                    player.sendMessage(ChatColor.GREEN + "Du hast " + amountToSell + "x " + itemToSell.getType() + " für " + price + " Perlen verkauft!");
                    return;
                }
            }
        } else if (clickedItem.getType() == Material.RED_STAINED_GLASS_PANE) {
            player.closeInventory();
            sellInv(player, 0);
        }
    }

    private void removeItems(Player player, Material material, int amountToRemove) {
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                int stackAmount = item.getAmount();
                if (stackAmount > amountToRemove) {
                    item.setAmount(stackAmount - amountToRemove);
                    return;
                } else {
                    amountToRemove -= stackAmount;
                    item.setAmount(0);
                }
                if (amountToRemove <= 0) return;
            }
        }
    }

    private void addPearls(Player player, int amount) {
        ItemStack pearlItem = new ItemStack(Material.HEART_OF_THE_SEA, amount);
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(pearlItem);
        if (!leftover.isEmpty()) {
            Location loc = player.getLocation();
            for (ItemStack it : leftover.values()) {
                player.getWorld().dropItemNaturally(loc, it);
            }
            player.sendMessage(ChatColor.GOLD + "Dein Inventar war voll. Einige Perlen wurden auf den Boden gelegt!");
        }
    }

    private int getItemCount(Player player, Material material) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents()) {
            if (item != null && item.getType() == material) {
                count += item.getAmount();
            }
        }
        return count;
    }

}


//Hast du dir jetzt ernsthaft die Mühe gemacht dich durch diesen 3000 Zeilen langen Code zu lesen❓
//Wie unnötig von dir, jetzt ernsthaft❗️
//Man sollte dich einsperren, dafür das du eventuell in der Zeit, 3 kleinen Kindern das Leben hättest retten können❗️
//Aber so sind leider diese Menschen, aber warum❓
//Nun denn, lies weiter, mal sehen, was du noch so für Schwachsinn ausbuddeln kannst 👀
