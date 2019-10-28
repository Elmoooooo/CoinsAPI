package de.elmo.coinsapi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

import java.sql.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

public class MySQL {
    private static MySQL instance;

    private String host, database, user, password;
    private int port;
    private Connection connection;

    private ExecutorService executor;
    private Plugin plugin;

    public MySQL(Plugin plugin, String host, String database, String user, String password, int port) {
        this.host = host;
        this.database = database;
        this.user = user;
        this.password = password;
        this.port = port;
        this.plugin = plugin;

        this.executor = Executors.newCachedThreadPool();

        MySQL.instance = this;
    }

    public void update(String stat) {
        if (this.isConnected()) {
            this.executor.execute(() -> this.queryUpdate(stat));
        }
    }

    public void query(String statment, Consumer<ResultSet> consumer) {
        if (this.isConnected()) {
            this.executor.execute(() -> {
                ResultSet resultSet = this.query(statment);
                Bukkit.getScheduler().runTask(this.plugin, () -> consumer.accept(resultSet));
            });
        }
    }

    private void queryUpdate(String query) {
        if (this.isConnected()) {
            try (PreparedStatement preparedStatement = this.connection.prepareStatement(query)) {
                this.queryUpdate(preparedStatement);
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    public ResultSet query(String query) {
        if (this.isConnected()) {
            try {
                return this.query(this.connection.prepareStatement(query));
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    private void queryUpdate(PreparedStatement stmt) {
        if (this.isConnected()) {
            try {
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }


    private ResultSet query(PreparedStatement stat) {
        if (this.isConnected()) {
            try {
                return stat.executeQuery();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public boolean isConnected() {
        try {
            if (this.connection == null || !this.connection.isValid(10) || this.connection.isClosed()) {
                return false;
            }
        } catch (SQLException e) {

            e.printStackTrace();
            return false;
        }
        return true;
    }

    public void connect() {
        try {
            this.connection = DriverManager.getConnection("jdbc:mysql://" + this.host + ":" + this.port + "/" + this.database + "?autoReconnect=true", this.user, this.password);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void closeConnection() {
        if (this.isConnected()) {
            try {
                this.connection.close();
            } catch (SQLException e) {
            } finally {
                this.connection = null;
            }
        }
    }

    public Connection getConnection() {
        return connection;
    }


    public static MySQL getInstance() {
        return instance;
    }
}

