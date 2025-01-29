package com.quests.OPIslandsQuestSystem;

import com.google.gson.Gson;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.inventory.FurnaceExtractEvent;
import org.bukkit.event.inventory.FurnaceSmeltEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.player.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import java.sql.*;
import java.util.*;

public final class Quest extends JavaPlugin implements Listener {
    private static final String DATABASE_URL = "jdbc:sqlite:quests.db";

    @Override
    public void onEnable() {
        connect();
        setupDatabase();
        getServer().getPluginManager().registerEvents(this, this);
        addQuests();
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }

    public static void connect() {
        var url = "jdbc:sqlite:quests.db";

        try (var conn = DriverManager.getConnection(url)) {
            if (conn != null) {
                System.out.println("Connection to SQLite has been established.");
                try (Statement stmt = conn.createStatement()) {
                    stmt.execute("PRAGMA busy_timeout = 30000;"); // Timeout setzen
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private Connection getConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(DATABASE_URL);
        connection.createStatement().execute("PRAGMA busy_timeout = 30000;"); // Timeout setzen
        return connection;
    }

    private void setupDatabase() {
        try (Connection conn = getConnection(); Statement stmt = conn.createStatement()) {
            // Tabelle für Quests
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
                    + " task TEXT,"
                    + " required_value TEXT,"
                    + " displayitem TEXT NOT NULL"
                    + ");";


            // Tabelle für Spielerfortschritte
            String progressTable = "CREATE TABLE IF NOT EXISTS playerprogress ("
                    + " player_id TEXT NOT NULL,"
                    + " quest_id INTEGER NOT NULL,"
                    + " isQuestforPlayerAvailable BOOLEAN NOT NULL,"
                    + " completed BOOLEAN NOT NULL,"
                    + " progress INTEGER DEFAULT 0,"
                    + " PRIMARY KEY (player_id, quest_id),"
                    + " FOREIGN KEY (quest_id) REFERENCES quests(id)"
                    + ");";

            // Tabelle für Spieler
            String playersTable = "CREATE TABLE IF NOT EXISTS players ("
                    + " uuid TEXT PRIMARY KEY,"
                    + " name TEXT NOT NULL,"
                    + " join_date TEXT NOT NULL"
                    + ");";

            stmt.execute(questsTable);
            stmt.execute(progressTable);
            stmt.execute(playersTable);

            getLogger().info("Datenbanktabellen wurden erfolgreich erstellt oder existieren bereits.");
        } catch (SQLException e) {
            getLogger().severe("Fehler beim Erstellen der Datenbank: " + e.getMessage());
        }
    }


    private void addQuests() {
        String sql = "INSERT OR IGNORE INTO quests(id, name, description, reward, displayreward, layer, dimension, difficulty, nextquests, task, required_value, displayitem) "
                + "VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";

        Gson gson = new Gson();

        Integer[] ids = {1, 2, 3, 4, 5, 6, 7, 8};
        String[] names = {
                "Miner",
                "Miner +",
                "Miner ++",
                "Level Grinder",
                "Cod Man",
                "Eisen Kämpfer",
                "Spawn",
                "Block Breaker"
        };
        String[] descriptions = {
                "Baue 100 Stein ab",
                "Baue 500 Deepslate ab",
                "Baue 1000 Tuff ab",
                "Erreich Level 15",
                "Fische 50 Cod",
                "Sammle 1 Eisenschwert auf",
                "Erreiche 0 0 0",
                "Baue 1000 Stein und 1000 Deepslate ab"
        };
        String[] rewards = {
                "DIAMOND, 10",
                "DIAMOND, 100",
                "DIAMOND, 1000",
                "STONE_SWORD, 1",
                "STONE, 10",
                "GOLD_INGOT, 64",
                "GOLD_INGOT, 100",
                "DIAMOND, 1000"
        };
        String[] displayRewards = {
                "10 Diamanten",
                "100 Diamanten",
                "1000 Diamanten",
                "Stone Sword",
                "10 Stein",
                "64 Gold",
                "100 Gold",
                "1000 Diamanten"
        };
        String[] layers = {
                "Kohle", "Kohle", "Kohle", "Kohle", "Kohle", "Kohle", "Kohle", "Kohle"
        };
        String[] dimensions = {
                "Oberwelt", "Oberwelt", "Oberwelt", "Oberwelt", "Oberwelt", "Oberwelt", "Oberwelt", "Oberwelt"
        };
        String[] difficulties = {
                "Einfach", "Einfach", "Einfach", "Einfach", "Einfach", "Einfach", "Einfach", "Einfach"
        };
        String[] nextQuests = {
                "2", "3, 4", "5", "6", "7", "8", "1", "1"
        };

        // Aufgaben als strukturierte Liste
        List<List<Map<String, Object>>> allTasks = Arrays.asList(
                List.of(Map.of("type", "mineBlock", "amount", 100, "target", "STONE")),
                List.of(Map.of("type", "mineBlock", "amount", 500, "target", "DEEPSLATE")),
                List.of(Map.of("type", "mineBlock", "amount", 1000, "target", "TUFF")),
                List.of(Map.of("type", "reachLevel", "amount", 15)),
                List.of(Map.of("type", "fishItem", "amount", 50, "target", "COD")),
                List.of(Map.of("type", "getItem", "amount", 1, "target", "IRON_SWORD")),
                List.of(Map.of("type", "reachCoordinates", "x", 0, "y", 0, "z", 0)),
                List.of(
                        Map.of("type", "mineBlock", "amount", 1000, "target", "STONE"),
                        Map.of("type", "mineBlock", "amount", 1000, "target", "DEEPSLATE")
                )
        );

        Integer[] requiredValues = {
                100,
                500,
                1000,
                15,
                50,
                1,
                1,
                2000,
        };

        String[] displayItems = {
                "STONE", "DEEPSLATE", "TUFF", "STONE_SWORD", "COD", "IRON_SWORD", "STONE", "DEEPSLATE"
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
                pstmt.setString(10, gson.toJson(allTasks.get(i))); // Aufgaben als JSON speichern
                pstmt.setInt(11, requiredValues[i]);
                pstmt.setString(12, displayItems[i]);
                pstmt.executeUpdate();
            }
            getLogger().info("Standardquests wurden erfolgreich hinzugefügt.");
        } catch (SQLException e) {
            getLogger().severe("Fehler beim Hinzufügen der Standardquests: " + e.getMessage());
        }
    }


    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        String playerName = player.getName();
        String joinDate = java.time.LocalDate.now().toString();

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
                } else {
                    player.sendMessage(ChatColor.YELLOW + "Willkommen zurück, " + playerName + "!");
                }
            }

        } catch (SQLException e) {
            getLogger().severe("Fehler bei der Spielerregistrierung: " + e.getMessage());
        }
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
            getLogger().info("Fortschritt für Spieler " + playerUUID + " wurde initialisiert.");
        }

        // Stelle sicher, dass nur beim ersten Join die erste Quest auf 1 gesetzt wird
        String updateQuest1 = "UPDATE playerprogress SET isQuestforPlayerAvailable = 1 WHERE player_id = ? AND quest_id = 1";
        try (PreparedStatement updateStmt = conn.prepareStatement(updateQuest1)) {
            updateStmt.setString(1, playerUUID);
            updateStmt.executeUpdate();
        }
    }


    // GUI

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            Player player = (Player) sender;
            openQuestScreen(player);
            return true;
        } else {
            return false;
        }
    }

    private final Map<UUID, Inventory> Quests = new HashMap<>();

    public void openQuestScreen(Player player) {
        checkAndCompleteQuests(player);
        Inventory inv = getServer().createInventory(null, 54, "Quests");

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

        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, Border);
        }

        inv.setItem(45, Close);
        inv.setItem(53, Help);

        int[] questSlots = {
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };

        String url = "jdbc:sqlite:quests.db";
        String sql = "SELECT quests.id, quests.name, quests.description, quests.reward, quests.displayreward, " +
                "quests.layer, quests.dimension, quests.difficulty, quests.displayitem, pp.isQuestforPlayerAvailable, " +
                "pp.progress, pp.completed, quests.nextquests, quests.required_value " +
                "FROM quests " +
                "LEFT JOIN playerprogress pp ON quests.id = pp.quest_id AND pp.player_id = ?";

        try (Connection conn = DriverManager.getConnection(url);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, player.getUniqueId().toString());
            try (ResultSet rs = pstmt.executeQuery()) {
                int slotIndex = 0;

                while (rs.next() && slotIndex < questSlots.length) {
                    int questId = rs.getInt("id");
                    String questName = rs.getString("name");
                    String description = rs.getString("description");
                    String reward = rs.getString("reward");
                    String displayReward = rs.getString("displayreward");
                    String layer = rs.getString("layer");
                    String dimension = rs.getString("dimension");
                    String difficulty = rs.getString("difficulty");
                    String displayItemName = rs.getString("displayitem");
                    int progress = rs.getInt("progress");
                    int requiredAmount = rs.getInt("required_value");
                    boolean isAvailable = rs.getBoolean("isQuestforPlayerAvailable");
                    boolean isCompleted = rs.getBoolean("completed");
                    String nextQuests = rs.getString("nextquests");

                    // Prüfen, ob die Quest abgeschlossen ist
                    if (progress >= requiredAmount && !isCompleted) {
                        String updateSql = "UPDATE playerprogress SET completed = 1 WHERE player_id = ? AND quest_id = ?";
                        try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                            updateStmt.setString(1, player.getUniqueId().toString());
                            updateStmt.setInt(2, questId);
                            updateStmt.executeUpdate();
                        }

                        // Belohnung vergeben
                        player.getInventory().addItem(parseReward(reward));
                        player.sendMessage(ChatColor.GOLD + "Du hast die Quest '" + questName + "' abgeschlossen!");

                        // Nächste Quests freischalten
                        if (nextQuests != null && !nextQuests.trim().isEmpty()) {
                            for (String questIdStr : nextQuests.split(",")) {
                                unlockNextQuest(player.getUniqueId().toString(), questIdStr.trim());
                            }
                        }

                        // Nochmal prüfen, ob neue Quests abgeschlossen werden können
                        checkAndCompleteQuests(player);
                    }

                    // Item-Icon bestimmen
                    Material material = Material.BARRIER;
                    if (isCompleted) {
                        material = Material.LIME_DYE;
                        description = "Diese Quest hast du bereits abgeschlossen.";
                    } else if (isAvailable) {
                        try {
                            material = Material.valueOf(displayItemName.toUpperCase());
                        } catch (IllegalArgumentException e) {
                            player.sendMessage("Ungültiges Display-Item: " + displayItemName);
                        }
                    }

                    // ItemStack für die Quest erstellen
                    ItemStack questItem = new ItemStack(material, 1);
                    var meta = questItem.getItemMeta();
                    if (meta != null) {
                        meta.setDisplayName(ChatColor.GREEN + questName);

                        List<String> lore = new ArrayList<>();
                        if (isCompleted) {
                            lore.add(ChatColor.GRAY + description);
                        } else if (isAvailable) {
                            lore.add(ChatColor.GRAY + description);
                            lore.add("");
                            lore.add(ChatColor.DARK_AQUA + "Ebene: " + ChatColor.WHITE + layer);
                            lore.add(ChatColor.DARK_AQUA + "Dimension: " + ChatColor.WHITE + dimension);
                            lore.add(ChatColor.DARK_AQUA + "Schwierigkeit: " + ChatColor.WHITE + difficulty);
                            lore.add("");
                            lore.add(ChatColor.GOLD + "Fortschritt: " + ChatColor.YELLOW + progress + "/" + requiredAmount);
                            lore.add("");
                            lore.add(ChatColor.GOLD + "Belohnung: ");
                            lore.add(ChatColor.WHITE + "- " + ChatColor.GREEN + displayReward);
                        } else {
                            meta.setDisplayName(ChatColor.RED + "Diese Quest ist noch nicht freigeschaltet.");
                        }

                        meta.setLore(lore);
                        questItem.setItemMeta(meta);
                    }

                    inv.setItem(questSlots[slotIndex], questItem);
                    slotIndex++;
                }
            }
        } catch (SQLException e) {
            player.sendMessage("Fehler beim Laden der Quests: " + e.getMessage());
        }

        Quests.put(player.getUniqueId(), inv);
        player.openInventory(inv);
    }



    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getView().getTitle().equals("Quests")) {
            if (event.getCurrentItem() == null || event.getCurrentItem().getType() == Material.AIR) {
                return;
            }

            event.setCancelled(true);

            if (event.getSlot() == 53 && event.getCurrentItem().getType() == Material.WRITTEN_BOOK) {
                event.getWhoClicked().closeInventory();
                openBook((Player) event.getWhoClicked());
            } else if (event.getSlot() == 45 && event.getCurrentItem().getType() == Material.HOPPER) {
                event.getWhoClicked().closeInventory();
            }

            Player player = (Player) event.getWhoClicked();
            String playerUUID = player.getUniqueId().toString();
            int slotIndex = event.getSlot();
            String url = "jdbc:sqlite:quests.db";

            // Asynchronous task to handle the database query
            new BukkitRunnable() {
                @Override
                public void run() {
                    String sql = "SELECT quests.id, pp.progress, pp.completed, quests.reward, quests.nextquests, quests.required_value " +
                            "FROM quests " +
                            "LEFT JOIN playerprogress pp ON quests.id = pp.quest_id AND pp.player_id = ? " +
                            "WHERE quests.id = ?";

                    try (Connection conn = DriverManager.getConnection(url);
                         PreparedStatement pstmt = conn.prepareStatement(sql)) {

                        pstmt.setString(1, playerUUID);
                        pstmt.setInt(2, slotIndex);
                        try (ResultSet rs = pstmt.executeQuery()) {
                            if (rs.next()) {
                                int progress = rs.getInt("progress");
                                boolean completed = rs.getBoolean("completed");
                                int requiredValue = rs.getInt("required_value"); // Hier holen wir den required_value

                                if (!completed && progress >= requiredValue) {  // Hier vergleichen wir progress mit requiredValue
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
            }.runTaskAsynchronously(this);  // Task wird asynchron ausgeführt
        }
    }


    private void checkAndCompleteQuests(Player player) {
        new BukkitRunnable() {
            @Override
            public void run() {
                String url = "jdbc:sqlite:quests.db";
                String sql = "SELECT quest_id, reward, nextquests, progress, required_value FROM playerprogress " +
                        "JOIN quests ON playerprogress.quest_id = quests.id " +
                        "WHERE player_id = ? AND completed = 0";

                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement pstmt = conn.prepareStatement(sql)) {

                    pstmt.setString(1, player.getUniqueId().toString());
                    try (ResultSet rs = pstmt.executeQuery()) {
                        List<Integer> completedQuests = new ArrayList<>();
                        List<ItemStack> rewards = new ArrayList<>();
                        List<String> nextQuestIds = new ArrayList<>();

                        while (rs.next()) {
                            int questId = rs.getInt("quest_id");
                            String reward = rs.getString("reward");
                            String nextQuests = rs.getString("nextquests");
                            int progress = rs.getInt("progress");
                            int requiredValue = rs.getInt("required_value");

                            // Überprüfen, ob der Fortschritt dem required_value entspricht
                            if (progress >= requiredValue) {
                                // Markiere die Quest als abgeschlossen (läuft im Async-Thread)
                                completeQuest(player.getUniqueId().toString(), questId);

                                // Belohnung vorbereiten (muss später synchron erfolgen)
                                rewards.add(parseReward(reward));
                                completedQuests.add(questId);

                                // Folgequests speichern
                                if (nextQuests != null && !nextQuests.isEmpty()) {
                                    nextQuestIds.addAll(Arrays.asList(nextQuests.split(",")));
                                }
                            }
                        }

                        // Synchronen Task starten, um Inventar und Nachrichten zu aktualisieren
                        if (!completedQuests.isEmpty()) {
                            new BukkitRunnable() {
                                @Override
                                public void run() {
                                    for (int questId : completedQuests) {
                                        player.sendMessage(ChatColor.GREEN + "Du hast die Quest " + questId + " abgeschlossen!");
                                    }

                                    for (ItemStack reward : rewards) {
                                        player.getInventory().addItem(reward);
                                    }

                                    for (String questIdStr : nextQuestIds) {
                                        unlockNextQuest(player.getUniqueId().toString(), questIdStr);
                                    }
                                }
                            }.runTask(Quest.this); // Quest ist deine Hauptklasse, die von JavaPlugin erbt
                        }
                    }
                } catch (SQLException e) {
                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            player.sendMessage("Fehler beim Überprüfen der abgeschlossenen Quests: " + e.getMessage());
                        }
                    }.runTask(Quest.this);
                }
            }
        }.runTaskAsynchronously(Quest.this); // Task läuft asynchron
    }

    private void completeQuest(String playerUUID, int questId) {
        String url = "jdbc:sqlite:quests.db";
        String updateSql = "UPDATE playerprogress SET completed = 1 WHERE player_id = ? AND quest_id = ?";

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {

                    updateStmt.setString(1, playerUUID);
                    updateStmt.setInt(2, questId);
                    updateStmt.executeUpdate();
                } catch (SQLException e) {
                    System.err.println("Fehler beim Abschließen der Quest: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(Quest.this); // Wird asynchron ausgeführt
    }

    private ItemStack parseReward(String reward) {
        String[] parts = reward.split(", ");

        // Versuche, den Materialnamen zu parsen
        Material material = null;
        try {
            material = Material.valueOf(parts[0]);
        } catch (IllegalArgumentException e) {
            // Falls das Material nicht gefunden wurde, gib eine Fehlermeldung aus
            System.err.println("Ungültiges Material: " + parts[0]);
            return null;  // Rückgabe von null bei ungültigem Material
        }

        // Versuche, die Menge zu parsen
        int amount = 0;
        try {
            amount = Integer.parseInt(parts[1]);
        } catch (NumberFormatException e) {
            // Fehlerbehandlung bei ungültiger Zahl
            System.err.println("Ungültige Menge: " + parts[1]);
            return null;  // Rückgabe von null bei ungültiger Menge
        }

        // Rückgabe des ItemStacks
        return new ItemStack(material, amount);
    }

    public void unlockNextQuest(String playerUUID, String nextQuestIds) {
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    // IDs als Liste splitten
                    String[] questIdsArray = nextQuestIds.split(",");
                    String sql = "UPDATE playerprogress SET isQuestforPlayerAvailable = 1 WHERE player_id = ? AND quest_id = ?";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        for (String questIdStr : questIdsArray) {
                            int questId = Integer.parseInt(questIdStr.trim());
                            stmt.setString(1, playerUUID);
                            stmt.setInt(2, questId);
                            stmt.addBatch();
                        }

                        int[] rowsUpdated = stmt.executeBatch();
                        getLogger().info(rowsUpdated.length + " Quests wurden erfolgreich freigeschaltet!");
                    }
                } catch (SQLException e) {
                    getLogger().severe("Fehler beim Freischalten der Quests: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }




    public void openBook(Player player) {
        ItemStack book = new ItemStack(Material.WRITTEN_BOOK);


        BookMeta bookMeta = (BookMeta) book.getItemMeta();
        if (bookMeta != null) {
            bookMeta.setTitle("Mein Buch");
            bookMeta.setAuthor("Plugin");


            bookMeta.addPage(
                    "Jake, Schickst du mir jetzt gefälligst diesen Text oder ich werde das nie mehr ändern!!!"

            );

            book.setItemMeta(bookMeta);
        }

        player.openBook(book);
    }
    //Tasks

    @EventHandler
    public void onPlayerPickupItem(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        ItemStack item = event.getItem().getItemStack();
        String itemName = item.getType().toString();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id, task, required_value FROM quests WHERE task LIKE 'getItem%'")) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String[] taskDetails = rs.getString("task").split(",");
                            String targetItem = taskDetails[2].trim();
                            int requiredAmount = Integer.parseInt(rs.getString("required_value"));

                            if (itemName.equals(targetItem)) {
                                handleQuestProgress(player, questId, item.getAmount(), requiredAmount);
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
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id, task, required_value FROM quests WHERE task LIKE 'mineBlock%'")) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String taskJson = rs.getString("task"); // Wir nehmen an, dass die Aufgaben als JSON gespeichert sind
                            String[] taskDetails = taskJson.split(",");
                            for (String task : taskDetails) {
                                Map<String, Object> taskMap = new Gson().fromJson(task, Map.class);
                                String taskType = (String) taskMap.get("type");

                                if (taskType.equals("mineBlock")) {
                                    String targetBlock = (String) taskMap.get("target");
                                    int requiredAmount = ((Double) taskMap.get("amount")).intValue();

                                    if (blockName.equalsIgnoreCase(targetBlock)) {
                                        handleQuestProgress(player, questId, 1, requiredAmount);
                                    }
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
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        String advancementKey = event.getAdvancement().getKey().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id, task FROM quests WHERE task LIKE 'getAdvancement%'")) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String[] taskDetails = rs.getString("task").split(",");
                            String requiredAdvancement = taskDetails[1].trim();

                            if (advancementKey.equalsIgnoreCase(requiredAdvancement)) {
                                handleQuestProgress(player, questId, 1, 1);
                            }
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void handleQuestProgress(Player player, int questId, int progressIncrease, int requiredAmount) {
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection()) {
                    String selectQuery = "SELECT progress FROM playerprogress WHERE player_id = ? AND quest_id = ?";
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                        selectStmt.setString(1, playerUUID);
                        selectStmt.setInt(2, questId);

                        try (ResultSet rs = selectStmt.executeQuery()) {
                            if (rs.next()) {
                                int currentProgress = rs.getInt("progress");
                                int newProgress = currentProgress + progressIncrease;

                                // Verhindern, dass der Fortschritt über den requiredAmount hinausgeht
                                if (newProgress > requiredAmount) {
                                    newProgress = requiredAmount;
                                }

                                if (currentProgress != newProgress) {
                                    String updateQuery = "UPDATE playerprogress SET progress = ? WHERE player_id = ? AND quest_id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                        updateStmt.setInt(1, newProgress);
                                        updateStmt.setString(2, playerUUID);
                                        updateStmt.setInt(3, questId);
                                        updateStmt.executeUpdate();

                                        if (newProgress >= requiredAmount) {
                                            player.sendMessage("Quest " + questId + " abgeschlossen!");
                                        } else {
                                            player.sendMessage("Quest Fortschritt: " + newProgress + "/" + requiredAmount);
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
        }.runTaskAsynchronously(this);
    }


    @EventHandler
    public void onPlayerreachesCoords(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        double playerX = player.getLocation().getX();
        double playerY = player.getLocation().getY();
        double playerZ = player.getLocation().getZ();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id, task FROM quests WHERE task LIKE 'reachCoordinates%'");
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int questId = rs.getInt("id");
                        String taskDetails = rs.getString("task").replace("reachCoordinates", "").replace("{", "").replace("}", "").trim();
                        String[] coordinates = taskDetails.split(",");

                        if (coordinates.length == 3) {
                            try {
                                double requiredX = Double.parseDouble(coordinates[0].trim());
                                double requiredY = Double.parseDouble(coordinates[1].trim());
                                double requiredZ = Double.parseDouble(coordinates[2].trim());
                                double tolerance = 3.0;

                                double distance = Math.sqrt(Math.pow(playerX - requiredX, 2) + Math.pow(playerY - requiredY, 2) + Math.pow(playerZ - requiredZ, 2));
                                if (distance <= tolerance) {
                                    handleQuestProgress(player, questId, 1, 1);
                                }
                            } catch (NumberFormatException ignored) {}
                        }
                    }
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onPlayerLevelChange(PlayerLevelChangeEvent event) {
        Player player = event.getPlayer();
        int newLevel = player.getLevel();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id, task FROM quests WHERE task LIKE 'reachLevel%'");
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int questId = rs.getInt("id");
                        String[] taskDetails = rs.getString("task").replace("reachLevel", "").replace("{", "").replace("}", "").trim().split(",");

                        if (taskDetails.length == 1) {
                            try {
                                int requiredLevel = Integer.parseInt(taskDetails[0].trim());
                                if (newLevel >= requiredLevel) {
                                    handleQuestProgress(player, questId, 1, 1);
                                }
                            } catch (NumberFormatException ignored) {}
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
        Player player = (Player) event.getPlayer();
        Block block = player.getTargetBlockExact(5);
        if (block == null) return;
        String blockName = block.getType().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement("SELECT id, task FROM quests WHERE task LIKE 'openInterface%'");
                     ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        int questId = rs.getInt("id");
                        String[] taskDetails = rs.getString("task").replace("openInterface", "").replace("{", "").replace("}", "").trim().split(",");

                        if (taskDetails.length == 2) {
                            String requiredBlock = taskDetails[0].trim();
                            int requiredAmount = Integer.parseInt(taskDetails[1].trim());
                            if (blockName.equalsIgnoreCase(requiredBlock)) {
                                handleQuestProgress(player, questId, 1, requiredAmount);
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
    public void onKillEntity(EntityDeathEvent event) {
        Player player = event.getEntity().getKiller();
        if (player == null) return;

        String playerUUID = player.getUniqueId().toString();
        Entity entity = event.getEntity();
        String entityType = entity.getType().name();

        new BukkitRunnable() {
            @Override
            public void run() {
                String query = "SELECT id, task, required_value FROM quests WHERE task LIKE 'killEntity%'";

                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String[] taskDetails = rs.getString("task").split(",");
                            String targetEntity = taskDetails[2].trim();
                            int requiredAmount = Integer.parseInt(rs.getString("required_value"));

                            if (entityType.equalsIgnoreCase(targetEntity)) {
                                handleQuestProgress(player, questId, 1, requiredAmount);
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
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        String blockName = block.getType().name();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                String query = "SELECT id, task, required_value FROM quests WHERE task LIKE 'placeBlock%'";

                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String[] taskDetails = rs.getString("task").split(",");
                            String targetBlock = taskDetails[2].trim();
                            int requiredAmount = Integer.parseInt(rs.getString("required_value"));

                            if (blockName.equalsIgnoreCase(targetBlock)) {
                                handleQuestProgress(player, questId, 1, requiredAmount);
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
    public void onPlayerFish(PlayerFishEvent event) {
        Player player = event.getPlayer();
        Entity caughtEntity = event.getCaught();

        if (caughtEntity == null || !(caughtEntity instanceof Item)) {
            return;
        }

        Item caughtItem = (Item) caughtEntity;
        Material fishMaterial = caughtItem.getItemStack().getType();
        String fishName = fishMaterial.name();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                String query = "SELECT id, task, required_value FROM quests WHERE task LIKE 'fishItem%'";

                try (Connection conn = getConnection();
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("id");
                            String[] taskDetails = rs.getString("task").split(",");
                            String targetFish = taskDetails[2].trim();
                            int requiredAmount = Integer.parseInt(rs.getString("required_value"));

                            if (fishName.equalsIgnoreCase(targetFish) || fishName.equalsIgnoreCase("RAW_" + targetFish)) {
                                handleQuestProgress(player, questId, 1, requiredAmount);
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
    public void onPlayerCraft(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        ItemStack clickedItem = event.getCurrentItem();

        if (clickedItem == null || clickedItem.getType() == Material.AIR) {
            return;
        }

        String itemName = clickedItem.getType().name();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                String url = "jdbc:sqlite:quests.db";
                String query = "SELECT quest_id, task FROM quests "
                        + "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id "
                        + "WHERE playerprogress.isQuestforPlayerAvailable = 1 "
                        + "AND playerprogress.player_id = ? AND quests.task LIKE 'craftItem%'";

                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("quest_id");
                            String[] taskDetails = rs.getString("task").replace("{", "").replace("}", "").split(",");

                            if (taskDetails.length != 3) {
                                System.err.println("Ungültiges Task-Format: " + rs.getString("task"));
                                continue;
                            }

                            String taskType = taskDetails[0].trim();
                            int requiredAmount = Integer.parseInt(taskDetails[1].trim());
                            String targetItem = taskDetails[2].trim();

                            if ("craftItem".equalsIgnoreCase(taskType) && itemName.equalsIgnoreCase(targetItem)) {
                                handleCraftItemQuest(player, requiredAmount, questId);
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Fehler beim Verarbeiten der craftItem-Quest: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void handleCraftItemQuest(Player player, int requiredAmount, int questId) {
        String playerUUID = player.getUniqueId().toString();
        String url = "jdbc:sqlite:quests.db";

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = DriverManager.getConnection(url)) {
                    String selectQuery = "SELECT progress FROM playerprogress WHERE player_id = ? AND quest_id = ?";
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                        selectStmt.setString(1, playerUUID);
                        selectStmt.setInt(2, questId);

                        try (ResultSet rs = selectStmt.executeQuery()) {
                            if (rs.next()) {
                                int progress = rs.getInt("progress");

                                if (progress < requiredAmount) {
                                    String updateQuery = "UPDATE playerprogress SET progress = progress + 1 WHERE player_id = ? AND quest_id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                        updateStmt.setString(1, playerUUID);
                                        updateStmt.setInt(2, questId);
                                        updateStmt.executeUpdate();
                                    }

                                    int finalProgress = progress + 1;
                                    player.sendMessage("Quest Fortschritt: " + finalProgress + "/" + requiredAmount);
                                } else {
                                    player.sendMessage("Quest bereits abgeschlossen!");
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Fehler beim Aktualisieren des Fortschritts: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onFurnaceSmelt(FurnaceSmeltEvent event) {
        ItemStack result = event.getResult();
        String smeltedItem = result.getType().name();

        Block furnaceBlock = event.getBlock();
        InventoryHolder holder = (InventoryHolder) furnaceBlock.getState();

        if (holder.getInventory().getViewers().isEmpty()) {
            return;
        }

        HumanEntity entity = holder.getInventory().getViewers().get(0);
        if (!(entity instanceof Player)) {
            return;
        }

        Player player = (Player) entity;
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                String url = "jdbc:sqlite:quests.db";
                String query = "SELECT quest_id, task FROM quests "
                        + "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id "
                        + "WHERE playerprogress.isQuestforPlayerAvailable = 1 "
                        + "AND playerprogress.player_id = ? AND quests.task LIKE 'smeltItem%'";

                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("quest_id");
                            String[] taskDetails = rs.getString("task").replace("{", "").replace("}", "").split(",");

                            if (taskDetails.length != 3) {
                                System.err.println("Ungültiges Task-Format: " + rs.getString("task"));
                                continue;
                            }

                            String taskType = taskDetails[0].trim();
                            int requiredAmount = Integer.parseInt(taskDetails[1].trim());
                            String targetItem = taskDetails[2].trim();

                            if ("smeltItem".equalsIgnoreCase(taskType) && smeltedItem.equalsIgnoreCase(targetItem)) {
                                handleSmeltItemQuest(player, requiredAmount, questId);
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Fehler beim Verarbeiten der smeltItem-Quest: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void handleSmeltItemQuest(Player player, int requiredAmount, int questId) {
        String playerUUID = player.getUniqueId().toString();
        String url = "jdbc:sqlite:quests.db";

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = DriverManager.getConnection(url)) {
                    String selectQuery = "SELECT progress FROM playerprogress WHERE player_id = ? AND quest_id = ?";
                    try (PreparedStatement selectStmt = conn.prepareStatement(selectQuery)) {
                        selectStmt.setString(1, playerUUID);
                        selectStmt.setInt(2, questId);

                        try (ResultSet rs = selectStmt.executeQuery()) {
                            if (rs.next()) {
                                int progress = rs.getInt("progress");

                                if (progress < requiredAmount) {
                                    String updateQuery = "UPDATE playerprogress SET progress = progress + 1 WHERE player_id = ? AND quest_id = ?";
                                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                        updateStmt.setString(1, playerUUID);
                                        updateStmt.setInt(2, questId);
                                        updateStmt.executeUpdate();
                                    }

                                    int finalProgress = progress + 1;
                                    player.sendMessage("Quest Fortschritt: " + finalProgress + "/" + requiredAmount);
                                } else {
                                    player.sendMessage("Quest bereits abgeschlossen!");
                                }
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Fehler beim Aktualisieren des Fortschritts: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        int currentHeight = player.getLocation().getBlockY();
        String playerUUID = player.getUniqueId().toString();

        new BukkitRunnable() {
            @Override
            public void run() {
                String url = "jdbc:sqlite:quests.db";
                String query = "SELECT quest_id, task FROM quests "
                        + "INNER JOIN playerprogress ON quests.id = playerprogress.quest_id "
                        + "WHERE playerprogress.isQuestforPlayerAvailable = 1 "
                        + "AND playerprogress.player_id = ? AND quests.task LIKE 'reachHeight%'";

                try (Connection conn = DriverManager.getConnection(url);
                     PreparedStatement pstmt = conn.prepareStatement(query)) {
                    pstmt.setString(1, playerUUID);
                    try (ResultSet rs = pstmt.executeQuery()) {
                        while (rs.next()) {
                            int questId = rs.getInt("quest_id");
                            String[] taskDetails = rs.getString("task").replace("{", "").replace("}", "").split(",");

                            if (taskDetails.length != 2) {
                                System.err.println("Ungültiges Task-Format: " + rs.getString("task"));
                                continue;
                            }

                            String taskType = taskDetails[0].trim();
                            int requiredHeight = Integer.parseInt(taskDetails[1].trim());

                            if ("reachHeight".equalsIgnoreCase(taskType) && currentHeight >= requiredHeight) {
                                handleHeightQuest(player, requiredHeight, questId);
                            }
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Fehler beim Verarbeiten der reachHeight-Quest: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }

    private void handleHeightQuest(Player player, int requiredHeight, int questId) {
        String playerUUID = player.getUniqueId().toString();
        String url = "jdbc:sqlite:quests.db";

        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = DriverManager.getConnection(url)) {
                    String updateQuery = "UPDATE playerprogress SET progress = ? WHERE player_id = ? AND quest_id = ?";
                    try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                        updateStmt.setInt(1, requiredHeight);
                        updateStmt.setString(2, playerUUID);
                        updateStmt.setInt(3, questId);
                        updateStmt.executeUpdate();
                    }
                    player.sendMessage("Quest abgeschlossen!");
                } catch (SQLException e) {
                    System.err.println("Fehler beim Aktualisieren der Höhe: " + e.getMessage());
                }
            }
        }.runTaskAsynchronously(this);
    }


}

//Tom?