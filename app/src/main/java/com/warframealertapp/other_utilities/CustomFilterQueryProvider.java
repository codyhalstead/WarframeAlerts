package com.warframealertapp.other_utilities;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.widget.FilterQueryProvider;

/**
 * Created by Cody on 11/3/2017.
 */

public class CustomFilterQueryProvider implements FilterQueryProvider {
    private String tableName;
    private String column;
    private SQLiteDatabase db;

    public CustomFilterQueryProvider(String tableName, String column, SQLiteDatabase db){
        this.tableName = tableName;
        this.column = column;
        this.db = db;
    }

    //Set up the search query
    @Override
    public Cursor runQuery(CharSequence constraint) {
        String query = "SELECT * FROM " + tableName +  " WHERE "  +  column + " LIKE '%" + constraint + "%' ORDER BY " + column + " ASC";
        return db.rawQuery(query, null);
    }
}
