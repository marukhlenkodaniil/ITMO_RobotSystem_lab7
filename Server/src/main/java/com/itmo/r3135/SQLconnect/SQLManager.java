package com.itmo.r3135.SQLconnect;

import com.itmo.r3135.World.Color;
import com.itmo.r3135.World.UnitOfMeasure;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;
import java.util.Random;

public class SQLManager {
    static final Logger logger = LogManager.getLogger("SQLManager");
    private Connection connection;

    public boolean initDatabaseConnection(String host, int port, String dataBaseName, String user, String password) {
        logger.info("Database connect...");
//        try {
//            Class.forName(driver.getJdbcDriver());
//        } catch (ClassNotFoundException e) {
//            logger.fatal("Чтобы подключиться к базе данных, нужен драйвер: " + config.getJdbcDriver());
//        }
        String databaseUrl = "jdbc:postgresql://" + host + ":" + port + "/" + dataBaseName;
        try {
            logger.info("Database URL: " + databaseUrl);
            connection = DriverManager.getConnection(databaseUrl, user, password);
            logger.info("Database '" + connection.getCatalog() + "' is connected! ");
            return true;
        } catch (SQLException e) {
            logger.fatal("Error SQL connection: " + e.toString());
            return false;
        }

    }

    public boolean initTables() {
        try {
            Statement statement = connection.createStatement();
            //Таблица данных пользователей
            statement.execute("create table if not exists users (" +
                    "id serial primary key not null, username text unique , email text unique, password_hash bytea)"
            );
            //таблица со статусами пользователя
            statement.execute("CREATE TABLE if not exists statuses " +
                    "(Id serial primary key not null ,name varchar(20) NOT NULL UNIQUE )");
            String[] statusList = {"reg", "npass"};

            try {
                for (String status : statusList)
                    statement.execute("insert into statuses(name) values('" + status + "') ");
            } catch (SQLException ignore) {
            }
            statement.execute("create table if not exists userstatus (" +
                    "id serial primary key not null, statusid int, code text," +
                    "foreign key (id) references users(id) on delete cascade," +
                    "foreign key (statusid) references statuses(id) on delete cascade)"
            );

            //таблица с color
            statement.execute("CREATE TABLE if not exists colors " +
                    "(Id serial primary key not null ,name varchar(20) NOT NULL UNIQUE )");
            Color[] colors = {Color.BLACK, Color.BLUE, Color.GREEN, Color.RED, Color.YELLOW};

            try {
                for (Color color : colors)
                    statement.execute("insert into colors(name) values('" + color + "') ");
            } catch (SQLException ignore) {//пока не знаю, как избежать ошибок дубликата, пожтому так.
            }

            //таблица с unitOfMeasure
            statement.execute("CREATE TABLE if not exists unitOfMeasures " +
                    "(Id serial primary key not null ,unitname varchar(20) NOT NULL UNIQUE )");
            UnitOfMeasure[] unitOfMeasures =
                    {UnitOfMeasure.GRAMS, UnitOfMeasure.LITERS, UnitOfMeasure.MILLIGRAMS, UnitOfMeasure.PCS};
            try {
                for (UnitOfMeasure unitOfMeasure : unitOfMeasures)
                    statement.execute("insert into unitOfMeasures(unitname) values('" + unitOfMeasure + "') ");
            } catch (SQLException ignore) {//пока не знаю, как избежать ошибок дубликата, пожтому так.
            }
            //кривая таблица Product
            statement.execute("create table if not exists products " +
                    "(id serial primary key not null , name text, x float,y double precision," +
                    "creationDate timestamp,price double precision, partNumber text," +
                    "manufactureCost float, unitOfMeasure_id  int,user_id integer," +
                    "foreign key (unitOfMeasure_id) references unitofmeasures(id)," +
                    "foreign key (user_id) references users(id))"
            );
            //кривая таблица Person(owner)
            statement.execute("create table if not exists owners " +
                    "(id serial primary key not null, ownerName text, ownerBirthday timestamp," +
                    "ownerEyeColor_id int,ownerHairColor_id int," +
                    "foreign key (ownerEyeColor_id) references colors(id)," +
                    "foreign key (ownerHairColor_id) references colors(id)," +
                    "foreign key (id) references products(id) on delete cascade)"
            );

            return true;
        } catch (SQLException e) {
            logger.fatal("Error in tables initialisation.");
            e.printStackTrace();
            return false;
        }
    }

    public int getUserId(String loginName) {
        int userId = -1;
        try {
            PreparedStatement s = connection
                    .prepareStatement("select id from users where (email = ? or username =?)");
            s.setString(1, loginName);
            s.setString(2, loginName);
            ResultSet resultSet = s.executeQuery();
            if (resultSet.next()) userId = resultSet.getInt("id");
        } catch (SQLException ignore) {
        }
        return userId;
    }

    public String userStatusReg(int userId) {
        return setStatus(userId, "reg");
    }

    public String userStatusNewPass(int userId) {
        return setStatus(userId, "npass");
    }

    private String setStatus(int userId, String status) {
        try {
            clearStatus(userId);
            PreparedStatement statement2 = connection.prepareStatement("insert into userstatus(id,statusid,code) " +
                    "values (?,(select id from statuses where name = ?),? )");
            statement2.setInt(1, userId);
            statement2.setString(2, status);
            statement2.setString(3, randomString());
            ResultSet resultSet = statement2.executeQuery();
            if (resultSet.next()) return resultSet.getString("name");
        } catch (SQLException e) {
            return null;
        }
        return null;
    }

    public String getUserStatus(int id) {
        try {
            PreparedStatement s = connection
                    .prepareStatement("select name from statuses where id = " +
                            "( select statusid from userstatus where id = " + id + " )");
            ResultSet resultSet = s.executeQuery();
            if (resultSet.next()) return resultSet.getString("name");
        } catch (SQLException ignore) {
        }
        return null;
    }

    public String getUserCode(int id) {
        try {
            PreparedStatement s = connection
                    .prepareStatement("select code from userstatus where id =" + id);
            ResultSet resultSet = s.executeQuery();
            if (resultSet.next()) return resultSet.getString("code");
        } catch (SQLException ignore) {
        }
        return null;
    }

    private static String randomString() {
        char[] chs = "ZXCVBNMASDFGHJKLQWERTYUIOP1234567890zxcvbnmasdfghjklqwertyuiop".toCharArray();
        String number = new String();
        Random random = new Random();
        for (int i = 0; i < 10; i++) {
            number = number + (chs[random.nextInt(chs.length)]);
        }
        return number;
    }

    public Connection getConnection() {
        return connection;
    }

    public boolean isReg(int uderId) {
        return "reg".equals(getUserStatus(uderId));
    }

    public boolean clearStatus(int userId) {
        try {
            Statement statement = connection.createStatement();
            statement.execute("delete from userstatus where id = " + userId);
        } catch (SQLException e) {
            return false;
        }
        return true;
    }

    public boolean isNewPass(int uderId) {
        return "npass".equals(getUserStatus(uderId));
    }
}
