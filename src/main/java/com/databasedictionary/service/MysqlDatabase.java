package com.databasedictionary.service;

import com.databasedictionary.entity.ColumnInfo;
import com.databasedictionary.entity.IndexInfo;
import com.databasedictionary.entity.TableInfo;
import com.databasedictionary.utils.ColumnBasicEnum;
import com.databasedictionary.utils.ConnectionFactory;
import com.databasedictionary.utils.SignEnum;
import com.databasedictionary.utils.TableBasicEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MysqlDatabase {
    static Logger logger = LoggerFactory.getLogger(MysqlDatabase.class);

    /**
     * 获取数据库所有库名
     */
    public static List<String> getDataBaseName(String ip, String dbName, String port, String userName, String passWord) {
        //得到生成数据
        String url = "jdbc:mysql://" + ip + ":" + port + "/" + dbName + "?useSSL=false&serverTimezone=UTC";
        try {
            Connection connection = ConnectionFactory.getConnection(url, userName, passWord, "mysql");
            Statement statement = connection.createStatement();
            List<String> dbList = new ArrayList<>(8);
            ResultSet resultSet = null;
            String sql = " show databases ";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                String dbNames = resultSet.getString(1);
                dbList.add(dbNames);
            }
            return dbList;
        } catch (Exception e) {
            logger.error("查询数据库名字集合异常", e);
            return new ArrayList<>(1);
        }
    }

    /**
     * 表信息的集合
     */
    public static List<TableInfo> getBuildTableData(List<Map<String, Object>> tables) {
        //循环处理表
        List<TableInfo> resultList = new ArrayList<TableInfo>();
        for (Map<String, Object> table : tables) {
            TableInfo tableInfo = (TableInfo) table.get("tableInfo");
            String tableName = tableInfo.getTableName();
            String createTable = (String) table.get("createTable");
            tableInfo.setTableName(tableName);
            //处理表信息字符
            tableInfo = takeTableInfo(tableInfo, createTable);
            resultList.add(tableInfo);
        }
        return resultList;
    }


    /**
     * 表的基本数据
     */
    public static TableInfo takeTableInfo(TableInfo tableInfo, String tableInfos) {
        //去掉回车
        tableInfos = MakeDownInfo.dest(tableInfos, SignEnum.back_quote.getDesc());
        tableInfos = MakeDownInfo.dest(tableInfos, SignEnum.single_quotation_marks.getDesc());
        String[] test = tableInfos.split("\n");
        //处理字符串
        String str = test[test.length - 1];
        str = MakeDownInfo.dest(str, SignEnum.right_brackets.getDesc()).trim();
        String[] table = str.split(" ");
        List<IndexInfo> indexInfoList = new ArrayList<IndexInfo>();
        int indexInfoSize = test.length;
        for (int i = 0; i < indexInfoSize - 1; i++) {
            String temp = test[i];
            //主键索引
            if (temp.contains(TableBasicEnum.PRIMARY_KEY.getDesc())) {
                temp = MakeDownInfo.dest(temp, SignEnum.left_brackets.getDesc());
                temp = MakeDownInfo.dest(temp, SignEnum.right_brackets.getDesc());
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = "";
                if (tempForIndex.length > 3) {
                    containKey = tempForIndex[2];
                } else {
                    containKey = tempForIndex[tempForIndex.length - 1];
                }
                IndexInfo indexInfo1 = new IndexInfo(TableBasicEnum.WORD_PRIMARY.getDesc(), TableBasicEnum.WORD_PRIMARY.getDesc(), MakeDownInfo.drop(containKey));
                indexInfo1.setIsIndex(1);
                indexInfoList.add(indexInfo1);
            }
            //唯一索引
            if (temp.contains(TableBasicEnum.UNIQUE_KEY.getDesc())) {
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = "";
                if (tempForIndex.length > 3) {
                    containKey = tempForIndex[3];
                } else {
                    containKey = tempForIndex[tempForIndex.length - 1];
                }
                String type = tempForIndex[0] + tempForIndex[1];
                String name = tempForIndex[2];
                containKey = MakeDownInfo.dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = MakeDownInfo.dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = MakeDownInfo.dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, MakeDownInfo.drop(containKey));
                indexInfoList.add(indexInfo1);
            }
            //普通索引
            if (temp.contains(TableBasicEnum.KEY.getDesc())) {
                String[] tempForIndex = temp.trim().split(" ");
                if (!tempForIndex[0].equalsIgnoreCase(TableBasicEnum.WORD_key.getDesc())) {
                    continue;
                }
                String containKey = "";
                if (tempForIndex.length > 3) {
                    containKey = tempForIndex[2];
                } else {
                    containKey = tempForIndex[tempForIndex.length - 1];
                }
                String type = tempForIndex[0];
                String name = tempForIndex[1];
                containKey = MakeDownInfo.dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = MakeDownInfo.dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = MakeDownInfo.dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, MakeDownInfo.drop(containKey));
                indexInfoList.add(indexInfo1);
            }
            //全文索引
            if (temp.contains("FULLTEXT KEY")) {
                String[] tempForIndex = temp.trim().split(" ");
                String containKey = "";
                if (tempForIndex.length > 3) {
                    containKey = tempForIndex[3];
                } else {
                    containKey = tempForIndex[tempForIndex.length - 1];
                }
                String type = tempForIndex[0];
                String name = tempForIndex[2];
                containKey = MakeDownInfo.dest(containKey, SignEnum.left_brackets.getDesc());
                containKey = MakeDownInfo.dest(containKey, SignEnum.right_brackets.getDesc());
                containKey = MakeDownInfo.dest(containKey, SignEnum.single_quotation_marks.getDesc());
                IndexInfo indexInfo1 = new IndexInfo(name, type, MakeDownInfo.drop(containKey));
                indexInfoList.add(indexInfo1);
            }
        }
        tableInfo.setIndexInfoList(indexInfoList);
        //得到表字符集和ENGINE、表注释
        for (int i = 0; i < table.length; i++) {
            String oneTemp = table[i];
            //引擎
            if (oneTemp.contains(TableBasicEnum.ENGINE.getDesc())) {
                tableInfo.setStorageEngine(MakeDownInfo.dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getStorageEngine() == null) {
                    tableInfo.setStorageEngine("");
                }
            }
            //字符集
            if (oneTemp.contains(TableBasicEnum.CHARSET.getDesc())) {
                tableInfo.setOrderType(MakeDownInfo.dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getOrderType() == null) {
                    tableInfo.setOrderType("");
                }
            }

            //描述
            if (oneTemp.contains(TableBasicEnum.COMMENT.getDesc())) {
                tableInfo.setDescription(MakeDownInfo.dropSign(table[i]));
                continue;
            } else {
                if (tableInfo.getDescription() == null) {
                    tableInfo.setDescription("");
                }
            }
        }

        return tableInfo;
    }


    /**
     * 获取数据库所有表信息
     */
    public static List<Map<String, Object>> getTables(Connection connection, String dbName) {
        Statement statement = null;
        ResultSet resultSet = null;
        List<Map<String, Object>> tables = new ArrayList<Map<String, Object>>();
        try {
            //获取表名
            statement = connection.createStatement();
            String sql = "show full tables FROM `" + dbName + "`" + "where Table_type = 'BASE TABLE'";
            resultSet = statement.executeQuery(sql);
            while (resultSet.next()) {
                Map<String, Object> resMap = new HashMap<String, Object>(2);
                TableInfo tableInfo = new TableInfo();
                //表名
                String tableName = resultSet.getString(1);
                //获取表信息
                String sqlTableInfo = "SHOW CREATE TABLE `" + tableName + "`";
                String createTable = getTableInfo(connection, sqlTableInfo);
                tableInfo.setTableName(tableName);
                resMap.put("createTable", createTable);
                String sql1 = "show full columns from `" + tableName + "`";
                List<ColumnInfo> columnInfos = getTableBaseInfo(connection, sql1);
                tableInfo.setColumnList(columnInfos);
                resMap.put("tableInfo", tableInfo);
                tables.add(resMap);
            }
            return tables;
        } catch (Exception e) {
            e.printStackTrace();
            return tables;
        } finally {
            try {
                ConnectionFactory.closeConnection(connection, null
                        , resultSet, statement);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 获取单个表全部信息
     */
    public static String getTableInfo(Connection connection, String sqlTableInfo) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sqlTableInfo);
        String table = "";
        while (resultSet.next()) {
            table = resultSet.getString("Create Table");
        }
        return table;
    }

    /**
     * 设置表的基本信息
     */
    public static List<ColumnInfo> getTableBaseInfo(Connection connection, String sql) throws Exception {
        Statement statement = connection.createStatement();
        ResultSet resultSet = statement.executeQuery(sql);
        List<ColumnInfo> columnInfos = new ArrayList<ColumnInfo>();
        int order = 1;
        while (resultSet.next()) {
            ColumnInfo columnInfo = new ColumnInfo();
            columnInfo.setName(resultSet.getString(ColumnBasicEnum.Field.getDesc()) + " " + resultSet.getString(ColumnBasicEnum.Extra.getDesc()));
            columnInfo.setType(resultSet.getString(ColumnBasicEnum.Type.getDesc()));
            columnInfo.setDescription(resolveLineFeed(resultSet.getString(ColumnBasicEnum.Comment.getDesc())));
            columnInfo.setIsNull(resultSet.getString(ColumnBasicEnum.Null.getDesc()));
            columnInfo.setOrder(order++);
            columnInfo.setDefaultValue(resultSet.getString(ColumnBasicEnum.Default.getDesc()));
            columnInfos.add(columnInfo);
            if (null == columnInfo.getDefaultValue()) {
                columnInfo.setDescription("");
            }
        }
        statement.close();
        resultSet.close();
        return columnInfos;
    }

    /**
     * 处理注释换行问题
     */
    public static String resolveLineFeed(String commentDesc) {
        if (!StringUtils.isEmpty(commentDesc)) {
            //去换行符
            return StringUtils.delete(commentDesc, "\\r|\\n");
        }
        return "";

    }


}
