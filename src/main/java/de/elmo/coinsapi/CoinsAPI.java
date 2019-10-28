package de.elmo.coinsapi;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.ResultSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class CoinsAPI
        extends JavaPlugin {
    private static CoinsAPI instance;
    private ExecutorService executorService = Executors.newCachedThreadPool();

    private LoadingCache<UUID, Integer> cache;

    static CoinsAPI getInstance() {
        return instance;
    }


    public void onEnable() {
        instance = this;
        MySQL mySQL = new MySQL(this, "localhost", "CoinsAPI", "root", "test", 3306);
        mySQL.connect();
        mySQL.update("CREATE TABLE IF NOT EXISTS coinsTable (uuid TEXT,coins INTEGER);");

        getServer().getScheduler().scheduleAsyncRepeatingTask(this, () -> {
            if (!MySQL.getInstance().isConnected()) {
                MySQL.getInstance().connect();
            }
        }, 1200L, 1200L);

        initCache();

        getCommand("coins").setExecutor(new CommandCoins());
    }


    private void initCache() {
        this.cache = CacheBuilder.newBuilder().expireAfterAccess(30L, TimeUnit.MINUTES).maximumSize(1000L).build(new CacheLoader<UUID, Integer>() {
            public Integer load(UUID uuid) {
                Future<Integer> future = CoinsAPI.this.executorService.submit(() -> {
                    try {
                        ResultSet resultSet = MySQL.getInstance().query("SELECT * FROM coinsTable WHERE uuid='" + uuid.toString() + "'");

                        if (resultSet.next()) {
                            return resultSet.getInt("coins");
                        }
                        MySQL.getInstance().update("INSERT INTO coinsTable (uuid,coins) VALUES ('" + uuid.toString() + "',0)");
                        System.out.println("INSERT " + uuid);
                        return 0;

                    } catch (Exception ex) {
                        return 0;
                    }
                });

                try {
                    return future.get();
                } catch (Exception ex) {
                    ex.printStackTrace();

                    return 0;
                }
            }
        });
    }


    public void onDisable() {
        MySQL.getInstance().closeConnection();
        System.out.println("Disabled");
    }

    public int getCoins(Player player) {
        try {
            return this.cache.get(player.getUniqueId());
        } catch (Exception ex) {
            return 0;
        }
    }

    public void setCoins(Player player, int coins) {
        MySQL.getInstance().update("UPDATE coinsTable SET coins = " + coins + " WHERE uuid = '" + player.getUniqueId().toString() + "';");
        this.cache.invalidate(player.getUniqueId());
    }

    public void addCoins(Player player, int coins) {
        MySQL.getInstance().update("UPDATE coinsTable SET coins = coins + " + coins + " WHERE uuid = '" + player.getUniqueId().toString() + "';");
        this.cache.invalidate(player.getUniqueId());
    }

    public void removeCoins(Player player, int coins) {
        MySQL.getInstance().update("UPDATE coinsTable SET coins = coins - " + coins + " WHERE uuid = '" + player.getUniqueId().toString() + "';");
        this.cache.invalidate(player.getUniqueId());
    }
}
