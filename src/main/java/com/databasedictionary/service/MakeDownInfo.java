package com.databasedictionary.service;


import com.databasedictionary.entity.ColumnInfo;
import com.databasedictionary.entity.IndexInfo;
import com.databasedictionary.entity.TableInfo;
import com.databasedictionary.utils.SignEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MakeDownInfo {
    static Logger logger = LoggerFactory.getLogger(MakeDownInfo.class);

    /**
     * 去掉字符串中的符号
     */
    public static String dest(String param, String reg) {
        String temp = "";
        if (param != null) {
            Pattern pattern = Pattern.compile(reg);
            Matcher m = pattern.matcher(param);
            temp = m.replaceAll("");
        }
        return temp;
    }

    /**
     * 得到一个包含一个等号的字符串，获取等号后的值
     */
    public static String dropSign(String param) {
        param = param.trim();
        int index = param.indexOf(SignEnum.equal_sign.getDesc());
        String res = param.substring(index + 1);
        if (res.contains(SignEnum.single_quotation_marks.getDesc())) {
            try {
                res = dest(res, SignEnum.single_quotation_marks.getDesc());
            } catch (Exception e) {
                e.printStackTrace();
                logger.error("获取等号后的值异常");
                return param;
            }
        }
        return res;
    }

    /**
     * 去掉索引逗号
     */
    public static String drop(String param) {
        try {
            param = param.trim();
            char[] tempArry = param.toCharArray();
            char res = tempArry[param.length() - 1];
            if (param.contains(SignEnum.comma.getDesc()) && res == ',') {
                return param.substring(0, param.length() - 1);
            } else {
                return param;
            }
        } catch (Exception e) {
            e.getStackTrace();
            return param;
        }
    }

    /**
     * 转成markdown语法
     */
    public static String writeMarkdown(List<TableInfo> list) {
        StringBuffer markdown = new StringBuffer();
        String res1 = "|:------:|:------:|:------:|:------:|:------:|:------:|" + "\n";
        int i = 1;
        for (TableInfo info : list) {
            StringBuffer oneTble = new StringBuffer();
            oneTble.append("##" + i + "." + info.getTableName() + " " + info.getDescription() + "\n" + "基本信息:" + info.getDescription() + " " + info.getStorageEngine() + " " + info.getOrderType() + "\n\n" + "|序列|列名|类型|可空|默认值|注释|" + "\n");
            oneTble.append(res1);
            List<ColumnInfo> columnInfos = info.getColumnList();
            //拼接列
            for (int k = 0; k < columnInfos.size(); k++) {
                ColumnInfo Column = columnInfos.get(k);
                oneTble.append("|").append(Column.getOrder()).append("|").
                        append(Column.getName()).append("|").
                        append(Column.getType()).append("|").
                        append(Column.getIsNull()).append("|").
                        append(Column.getDefaultValue()).append("|");
                if (null == Column.getDescription()) {
                    oneTble.append(" ").append("|").append("\n");
                } else {
                    String str = Column.getDescription();
                    str = str.replaceAll("\n", "");
                    if (str.length() == 0) {
                        oneTble.append("...").append("|").append("\n");
                    } else {
                        oneTble.append(str).append("|").append("\n");
                    }

                }

            }
            //拼接索引
            oneTble.append("\n");
            oneTble.append("|序列|索引名|类型|包含字段|" + "\n");
            oneTble.append("|:------:|:------:|:------:|:------:|" + "\n");
            List<IndexInfo> indexInfolist = info.getIndexInfoList();
            int j = 1;
            for (IndexInfo indexInfo : indexInfolist) {
                oneTble.append("|").append(j).append("|").
                        append(indexInfo.getName()).append("|").
                        append(indexInfo.getType()).append("|").
                        append(indexInfo.getContainKey()).append("|").
                        append("\n");
                j++;
            }
            i++;
            oneTble.append("\n");
            markdown.append(oneTble);
        }
        //目录
        markdown.insert(0, "[TOC]\n");
        return markdown.toString();
    }

}
