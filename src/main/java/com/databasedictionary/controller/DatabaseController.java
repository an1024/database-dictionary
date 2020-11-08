package com.databasedictionary.controller;

import com.databasedictionary.entity.TableInfo;
import com.databasedictionary.service.MakeDownInfo;
import com.databasedictionary.service.MysqlDatabase;
import com.databasedictionary.service.OracleDatabase;
import com.databasedictionary.utils.ConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

@Controller
public class DatabaseController {
    static Logger logger = LoggerFactory.getLogger(DatabaseController.class);

    @RequestMapping("/login")
    public String login(Model model, String selector, String ip, String port, String password, String username, String database) {
        List<TableInfo> tableInfo = null;
        try {
            switch (selector) {
                case "mysql":
                    //得到生成数据
                    String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
                    Connection connection = ConnectionFactory.getConnection(url, username, password, "mysql");
                    tableInfo = MysqlDatabase.getBuildTableData(MysqlDatabase.getTables(connection, database));
                    break;
                case "oracle":
                    tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//" + ip + ":" + port + "/" + database + "", username, password);
                    break;
            }
            if (tableInfo != null) {
                if (tableInfo.size() == 0) {
                    model.addAttribute("markdown", "## 数据库无数据");
                    return "markdown";
                }
            }
            String markdown = MakeDownInfo.writeMarkdown(tableInfo);
            model.addAttribute("markdown", markdown);
            return "markdown";
        } catch (Exception e) {
            model.addAttribute("markdown", "### " + e.getMessage());
            return "markdown";
        }
    }

    @RequestMapping("/getDataBaseNameList")
    @ResponseBody
    public List<String> getDataBaseNameList(String selector, String ip, String port, String password, String username, String database) {
        List<String> list = new ArrayList<>();
        try {
            switch (selector) {
                case "mysql":
                    return MysqlDatabase.getDataBaseName(ip, database, port, username, password);
                case "oracle":
                    list.add(database);
                    return list;
            }

        } catch (Exception e) {
            logger.error("查询表名错误" + e);
        }
        return list;
    }

    @RequestMapping("/getMarkdownString")
    @ResponseBody
    public String getMarkdownString(Model model, String selector, String ip, String port, String password, String username, String database) {
        List<TableInfo> tableInfo = null;
        try {
            switch (selector) {
                case "mysql":
                    //得到生成数据
                    String url = "jdbc:mysql://" + ip + ":" + port + "/" + database + "?useSSL=false&serverTimezone=UTC";
                    Connection connection = ConnectionFactory.getConnection(url, username, password, "mysql");
                    tableInfo = MysqlDatabase.getBuildTableData(MysqlDatabase.getTables(connection, database));
                    break;
                case "oracle":
                    tableInfo = OracleDatabase.getTableInfo("jdbc:oracle:thin:@//" + ip + ":" + port + "/" + database + "", username, password);
                    break;
            }
            if (tableInfo != null) {
                if (tableInfo.size() == 0) {
                    return "## 数据库无数据";
                }
            }

            String markdown = MakeDownInfo.writeMarkdown(tableInfo);

            return markdown;
        } catch (Exception e) {
            logger.error("查询表详情错误" + e);
            return "### " + e.getMessage();
        }
    }

}