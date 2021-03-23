package n.jdbc;

import javax.sql.DataSource;
import java.io.File;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.logging.Logger;

public class BasicDataSource implements DataSource {

    public BasicDataSource(Builder builder){
        this.dbDriver = builder.dbDriver;
        this.dbUrl = builder.dbUrl;
        this.dbName = builder.dbName;
        this.dbUsername = builder.dbUsername;
        this.dbPassword = builder.dbPassword;
    }

    String dbDriver;
    String dbUrl;
    String dbName;
    String dbUsername;
    String dbPassword;
    Integer loginTimeout;

    public static class Builder{
        String dbUrl;
        String dbName;
        String dbUsername;
        String dbPassword;
        String dbDriver;

        public Builder url(String dbUrl){
            this.dbUrl = dbUrl;
            return this;
        }
        public Builder dbName(String dbName){
            this.dbName = dbName;
            return this;
        }
        public Builder username(String dbUsername){
            this.dbUsername = dbUsername;
            return this;
        }
        public Builder password(String dbPassword){
            this.dbPassword = dbPassword;
            return this;
        }
        public Builder driver(String dbDriver){
            this.dbDriver = dbDriver;
            return this;
        }

        public BasicDataSource build(){
            return new BasicDataSource(this);
        }

    }


    @Override
    public Connection getConnection() throws SQLException {
        try {
            Class.forName(dbDriver);
            return DriverManager.getConnection(dbUrl, dbUsername, dbPassword);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new RuntimeException("Problem connecting to the database", ex);
        }
    }

    public static String getH2Path(){
        Path projectPath = Paths.get(".");
        File directory = new File(projectPath + File.separator + "exec");
        if(!directory.exists()){
            directory.mkdir();
        }
        Path execPath = Paths.get("exec");
        String h2Path = execPath.toFile().getAbsolutePath() + File.separator + "app";
        return h2Path;
    }

    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        try {
            Class.forName(dbDriver);
            return DriverManager.getConnection(dbUrl, username, password);
        } catch (SQLException | ClassNotFoundException ex) {
            throw new RuntimeException("Problem connecting to the database", ex);
        }
    }

    @Override
    public PrintWriter getLogWriter() throws SQLException {
        return null;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws SQLException {

    }

    @Override
    public void setLoginTimeout(int seconds) throws SQLException {
        this.loginTimeout = seconds;
    }

    @Override
    public int getLoginTimeout() throws SQLException {
        return this.loginTimeout;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        return null;
    }

    @Override
    public <T> T unwrap(Class<T> iface) throws SQLException {
        return null;
    }

    @Override
    public boolean isWrapperFor(Class<?> iface) throws SQLException {
        return false;
    }
}
