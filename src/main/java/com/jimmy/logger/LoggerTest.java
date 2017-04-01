package com.jimmy.logger;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

/**
 * Created by wxp04 on 2017/4/1.
 */
public class LoggerTest {
    public static void main(String[] args) {
        ChangeLogLevelProcessUnit unit = new ChangeLogLevelProcessUnit();
        unit.getLoggerList();

        JSONObject object = new JSONObject();
        object.put("loggerName", "root");
        object.put("level", "DEBUG");

        JSONArray array = new JSONArray();
        array.add(object);

        System.out.println("Change log level result: " + unit.setLogLevel(array));

        unit.getLoggerList();
    }
}
