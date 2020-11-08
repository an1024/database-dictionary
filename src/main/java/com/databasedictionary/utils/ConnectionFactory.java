package com.databasedictionary.utils;

import com.databasedictionary.service.DataDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.*;

public class ConnectionFactory {
    public static String mysql = "mysql";
    public static String oracle = "oracle";
    public static Logger logger = LoggerFactory.getLogger(ConnectionFactory.class);

    /**
     * 连接数据库
     */
    public static Connection getConnection(String url, String username, String password, String driverName) throws Exception {
        Connection connection = null;
        //进行数据库驱动的加载
        if (mysql.equals(driverName)) {
            Class.forName(DataDriver.MySQLDriverClassName);
        } else if (oracle.equals(driverName)) {
            Class.forName(DataDriver.OracleDriverClassName);
        }
        logger.info("加载驱动成功！");

        //创建连接
        connection = DriverManager.getConnection(url, username, password);
        if (connection.isClosed()) {
            logger.info("建立连接失败！");
        }

        return connection;
    }

    /**
     * 关闭连接
     *
     * @param connection
     * @param preparedStatement
     * @param resultSet
     * @param statement
     */
    public static void closeConnection(Connection connection, PreparedStatement preparedStatement, ResultSet resultSet, Statement statement) {
        if (null != resultSet) {
            try {
                resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (null != statement) {
            try {
                statement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (null != preparedStatement) {
            try {
                preparedStatement.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        if (null != connection) {
            try {
                connection.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
