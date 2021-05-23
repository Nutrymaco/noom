package com.nutrymaco.orm;

import com.nutrymaco.orm.tests.util.DBUtil;

public class ClearDB {
    public static void main(String[] args) {
        DBUtil.dropAllTables();
        DBUtil.deleteTypes();
    }
}
