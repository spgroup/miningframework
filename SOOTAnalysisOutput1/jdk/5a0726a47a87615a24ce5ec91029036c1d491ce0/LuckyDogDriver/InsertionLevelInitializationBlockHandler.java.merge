package luckydogtennis;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.SQLFeatureNotSupportedException;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LuckyDogDriver implements Driver {

    static {
        registerDriver();
        System.out.println("*****in static block LuckyDogDriver");
    }

    private static void registerDriver() {
        try {
            DriverManager.registerDriver(new LuckyDogDriver());
        } catch (SQLException ex) {
            Logger.getLogger(LuckyDogDriver.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public LuckyDogDriver() {
        System.out.println("*****in LuckyDogDriver Constructor");
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
        return url.matches("^jdbc:tennis:.*");
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