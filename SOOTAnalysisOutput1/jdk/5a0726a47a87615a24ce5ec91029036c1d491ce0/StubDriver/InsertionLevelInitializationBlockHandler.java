package com.luckydogtennis;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class StubDriver implements Driver {

    static {
        System.out.println("*****in static block StubDriver");
        registerDriver();
    }

    private static void registerDriver() {
        try {
            DriverManager.registerDriver(new StubDriver());
        } catch (SQLException ex) {
            Logger.getLogger(StubDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public StubDriver() {
        System.out.println("*****in StubDriver Constructor*************");
    }

    @Override
    public Connection connect(String url, Properties info) throws SQLException {
        if (acceptsURL(url)) {
            return new StubConnection();
        }
        return null;
    }

    @Override
    public boolean acceptsURL(String url) throws SQLException {
        return url.matches("^jdbc:stub:.*");
    }

    @Override
    public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public int getMajorVersion() {
        return 1;
    }

    @Override
    public int getMinorVersion() {
        return 0;
    }

    @Override
    public boolean jdbcCompliant() {
        return true;
    }

    @Override
    public Logger getParentLogger() throws SQLFeatureNotSupportedException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}