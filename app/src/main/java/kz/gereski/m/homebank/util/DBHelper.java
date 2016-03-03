package kz.gereski.m.homebank.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteStatement;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.util.Log;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import kz.gereski.m.homebank.domain.Product;

import static kz.gereski.m.homebank.util.DBHelper.ACTION.DELETE;
import static kz.gereski.m.homebank.util.DBHelper.ACTION.INSERT;
import static kz.gereski.m.homebank.util.Formatter.YYYYMMDDdashed;
import static kz.gereski.m.homebank.util.Formatter.gap;
import static kz.gereski.m.homebank.util.Formatter.parseDate;

public class DBHelper extends SQLiteOpenHelper {
    public static final String SHOPPING_TAB = "shopping";
    public static final String GROUPS_TAB = "groups";
    private final String LOG_TAG = "myHomeBank";
    int newVersion = 2;

    public DBHelper(Context context) {
        super(context, "myBankDB", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        if (needUpgrade(db, newVersion)) setVersion(db, newVersion);
        createDatabase(db);
        addDefaultGroups(db);
    }

    private void createDatabase(SQLiteDatabase db) {
        Log.d(LOG_TAG, "--- onCreate database ---");
        String tab1 = "create table if not exists shopping (" +
                "id integer primary key," +
                "date integer," +
                "group_name integer," +
                "name text," +
                "shop text," +
                "price real," +
                "amount real," +
                "total real," +
                "action integer," +
                "action_time integer" +
                ");";

        db.execSQL(tab1);

        String tab2 = "create table if not exists groups (" +
                "id integer primary key AUTOINCREMENT," +
                "key integer," +
                "value text," +
                "color text," +
                "action integer" +
                "action_time integer" +
                ");";

        db.execSQL(tab2);

        upgradeGroupTable(db);
    }

    private void upgradeGroupTable(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from " + GROUPS_TAB + ";", null);
        List<String> names = Arrays.asList(c.getColumnNames());
        c.close();
        String sql = "";
        if (!names.contains("action")) {
            sql += "ALTER TABLE " + GROUPS_TAB + " ADD COLUMN action integer;";
            sql += "UPDATE " + GROUPS_TAB + "SET action=0;";
        }
        if (!names.contains("action_time")) {
            sql += "ALTER TABLE " + GROUPS_TAB + " ADD COLUMN action_time integer;";
            sql += "UPDATE " + GROUPS_TAB + "SET action_time=" + new Date().getTime() + ";";
        }
        if (!sql.equals("")) db.execSQL(sql);
    }

    private void addDefaultGroups(SQLiteDatabase db) {
        int s = getGroups(db).keySet().size();
        if (s == 0) {
            String sql =
                    "insert into groups (key, value, color, action, action_time) values (0, 'Нет группы', '', 0, 1);" +
                            "insert into groups (key, value, color, action, action_time) values (1, 'Продукты', '', 0, 2);" +
//                    "insert into groups (key, value, color, action, action_time) values (2, 'Общепит', '', 0, 3);" +
                            "insert into groups (key, value, color, action, action_time) values (3, 'Одежда', '', 0, 4);" +
                            "insert into groups (key, value, color, action, action_time) values (4, 'Проезд', '', 0, 5);" +
//                    "insert into groups (key, value, color, action, action_time) values (5, 'Электроника', '', 0, 6);" +
                            "insert into groups (key, value, color, action, action_time) values (6, 'Квартплата', '', 0, 7);" +
                            "insert into groups (key, value, color, action, action_time) values (7, 'Связь', '', 0, 8);" +
                            "insert into groups (key, value, color, action, action_time) values (8, 'Образование', '', 0, 9);" +
//                    "insert into groups (key, value, color, action, action_time) values (9, 'Подарки', '', 0, 10);" +
//                    "insert into groups (key, value, color, action, action_time) values (11, 'Медицина', '', 0, 12);" +
                            "insert into groups (key, value, color, action, action_time) values (10, 'Другое', '', 0, 11)";

            SQLiteStatement stmt;
            List<String> comms = Arrays.asList(sql.split(";"));
            for (String com : comms) {
                if (com.equals("")) {
                    return;
                }
                stmt = db.compileStatement(com + ";");
                stmt.execute();
            }
        }
    }

    public void sqlWriteGroupToDb(long groupKey, String groupName, String color, ACTION action) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("action", action.ordinal());
        values.put("action_time", new Date().getTime());

        if (action != DELETE) {
            values.put("value", groupName);
            values.put("color", color);
        }

        if (action == INSERT) {
            values.put("key", new Date().getTime());
            values.put("value", groupName);
            values.put("color", color);
            db.insert(GROUPS_TAB, null, values);

        } else db.update(GROUPS_TAB, values, "key = ?",
                new String[]{String.valueOf(groupKey)});

        db.close();
    }

    public void sqlWriteProductToDb(Product product, ACTION action) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("action", action.ordinal());
        values.put("action_time", new Date().getTime());

        if (action != DELETE) {
            values.put("name", product.name);
            values.put("group_name", product.group.id);
            values.put("shop", product.shop);
            values.put("price", product.price);
            values.put("amount", product.amount);
            values.put("total", product.price * product.amount);
        }

        if (action == INSERT) {
            values.put("id", product.id);
            values.put("date", product.date.getTime());
            db.insert(SHOPPING_TAB, null, values);
        } else db.update(SHOPPING_TAB, values, "id = " + product.id, null);

        db.close();
    }

    public Double getTotalExpense(int month, int year) {
        double total = 0.0;
        Date d1 = parseDate(year + "-" + gap(month, 2) + "-01", YYYYMMDDdashed);
        Date d2 = parseDate(year + "-" + gap(month, 2) + "-31", YYYYMMDDdashed);
        String start = String.valueOf(d1 != null ? d1.getTime() : new Date());
        String end = String.valueOf(d2 != null ? d2.getTime() : new Date());
        String delete = String.valueOf(ACTION.DELETE.ordinal());

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select sum(total) as 'Total' from " + SHOPPING_TAB
                        + " where date >= ? and date <= ? and action != ?",
                new String[]{start, end, delete});
        if (c.moveToFirst()) {
            int totalIdx = c.getColumnIndex("Total");
            do {
                total += c.getDouble(totalIdx);
            } while (c.moveToNext());

        } else c.close();
        db.close();
        return total;
    }

    public Map<Long, Double> getExpensesByGroup(int month, int year) {
        Map<Long, Double> res = new HashMap<>();

        Date d1 = parseDate(year + "-" + gap(month, 2) + "-01", YYYYMMDDdashed);
        Date d2 = parseDate(year + "-" + gap(month, 2) + "-31", YYYYMMDDdashed);
        String start = String.valueOf(d1 != null ? d1.getTime() : new Date());
        String end = String.valueOf(d2 != null ? d2.getTime() : new Date());
        String delete = String.valueOf(ACTION.DELETE.ordinal());

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select sum(total) as 'Total', group_name from " + SHOPPING_TAB
                        + " where date >= ? and date <= ? and action != ? group by group_name",
                new String[]{start, end, delete});
        if (c.moveToFirst()) {
            int totalIdx = c.getColumnIndex("Total");
            int groupIdx = c.getColumnIndex("group_name");
            do {
                res.put(c.getLong(groupIdx), c.getDouble(totalIdx));
            } while (c.moveToNext());

        } else c.close();
        db.close();
        return res;
    }

    public Map<Long, Double> getDailyExpense(Date startDate, int numberOfDays) {
        Map<Long, Double> days = new HashMap<>();

        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);
        for (int d = 0; d < numberOfDays; d++) {
            days.put(cal.getTime().getTime(), 0.0);
            cal.add(Calendar.DATE, 1);
        }

        String start = String.valueOf(startDate.getTime());
        String end = String.valueOf(cal.getTime().getTime());
        String delete = String.valueOf(ACTION.DELETE.ordinal());

        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select date, price, amount from " + SHOPPING_TAB
                        + " where date >= ? and date <= ? and action != ?",
                new String[]{start, end, delete});
        if (c.moveToFirst()) {
            int dateIdx = c.getColumnIndex("date");
            int priceIdx = c.getColumnIndex("price");
            int amountIdx = c.getColumnIndex("amount");
            long dateVal;
            double priceVal;
            double amountVal;
            do {
                dateVal = c.getLong(dateIdx);
                priceVal = c.getDouble(priceIdx);
                amountVal = c.getDouble(amountIdx);

                if (days.get(dateVal) != null)
                    days.put(dateVal, days.get(dateVal) + priceVal * amountVal);
            } while (c.moveToNext());

        } else c.close();
        db.close();
        return days;
    }

    public int getGroupColorByKey(long id, SQLiteDatabase db) {
//        int transperent = Color.TRANSPARENT;
        int transperent = Color.argb(50, 160, 160, 160);
        if (id < 0) return transperent;
        Cursor cursor = db.rawQuery("select color from groups where key = ? and action is not ?",
                new String[]{String.valueOf(id), String.valueOf(DELETE.ordinal())});
        if (cursor.moveToFirst()) {
            int colorIdx = cursor.getColumnIndex("color");
            String col = cursor.getString(colorIdx);
            if (col == null || col.equals("")) {
                return transperent;
            } else {
                return Integer.parseInt(col);
            }
        } else {
            cursor.close();
            return transperent;
        }
    }

    public Map<Long, String> getGroups(SQLiteDatabase db) {
        Map<Long, String> groups = new HashMap<>();

        Cursor c = db.rawQuery("select key, value from groups where action is not ?",
                new String[]{String.valueOf(DELETE.ordinal())});
        if (c.moveToFirst()) {
            int keyIdx = c.getColumnIndex("key");
            int valueIdx = c.getColumnIndex("value");

            do {
                groups.put(c.getLong(keyIdx), c.getString(valueIdx));
            } while (c.moveToNext());
        } else {
            c.close();
        }

        return new TreeMap<>(groups);
    }

    public List<Product> getListOfTheDay(Date date) {
        List<Product> products = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Map<Long, String> groups = getGroups(db);

        Cursor c = db.rawQuery("select * from shopping where date = ? and action is not ?",
                new String[]{String.valueOf(date.getTime()),
                        String.valueOf(ACTION.DELETE.ordinal())});

        if (c.moveToFirst()) {
            int idIdx = c.getColumnIndex("id");
            int nameIdx = c.getColumnIndex("name");
            int groupIdx = c.getColumnIndex("group_name");
            int shopIdx = c.getColumnIndex("shop");
            int priceIdx = c.getColumnIndex("price");
            int amountIdx = c.getColumnIndex("amount");

            do {
                Product product = new Product();
                product.id = Long.parseLong(c.getString(idIdx));
                product.name = c.getString(nameIdx);
                product.shop = c.getString(shopIdx);
                product.price = c.getDouble(priceIdx);
                product.amount = c.getDouble(amountIdx);
                Long group = c.getLong(groupIdx);
                product.group.id = group;
                product.group.name = groups.get(group != null ? group : 0L);

                products.add(product);

            } while (c.moveToNext());
        } else {
            c.close();
        }
        db.close();

        return products;
    }

    public List<Product> getListByGroup(Calendar monthOfTheYear, long groupId) {
        int min = monthOfTheYear.getActualMinimum(Calendar.DAY_OF_MONTH);
        int max = monthOfTheYear.getActualMaximum(Calendar.DAY_OF_MONTH);
        int month = monthOfTheYear.get(Calendar.MONTH);
        int year = monthOfTheYear.get(Calendar.YEAR);
        Date start = Formatter.parseDate(year, month, min, "-", Formatter.YYYYMMDDdashed);
        Date end = Formatter.parseDate(year, month, max, "-", Formatter.YYYYMMDDdashed);
        return getListByGroup(start.getTime(), end.getTime(), groupId);
    }

    public List<Product> getListByGroup(long begin, long end, long groupId) {
        List<Product> products = new ArrayList<>();

        SQLiteDatabase db = getWritableDatabase();
        Map<Long, String> groups = getGroups(db);

        Cursor c = db.rawQuery(
                "select * from shopping where date >= ? and date <= ? and group_name = ? and action is not ?",
//                "select * from shopping where action is not ? and group_name = ?",
                new String[]{
                        String.valueOf(begin),
                        String.valueOf(end),
                        String.valueOf(groupId),
                        String.valueOf(ACTION.DELETE.ordinal())
                });

        if (c.moveToFirst()) {
            int idIdx = c.getColumnIndex("id");
            int nameIdx = c.getColumnIndex("name");
            int groupIdx = c.getColumnIndex("group_name");
            int shopIdx = c.getColumnIndex("shop");
            int priceIdx = c.getColumnIndex("price");
            int amountIdx = c.getColumnIndex("amount");

            do {
                Product product = new Product();
                product.id = Long.parseLong(c.getString(idIdx));
                product.name = c.getString(nameIdx);
                product.shop = c.getString(shopIdx);
                product.price = c.getDouble(priceIdx);
                product.amount = c.getDouble(amountIdx);
                Long group = c.getLong(groupIdx);
                product.group.id = group;
                product.group.name = groups.get(group != null ? group : 0L);

                products.add(product);

            } while (c.moveToNext());
        } else {
            c.close();
        }
        db.close();

        return products;
    }

    public String getExportingData(int numberOfMonths) throws ParseException {
        return getDataFromShoppingTable(numberOfMonths) + getDataFromGroupsTable();
    }

    @NonNull
    private String getDataFromShoppingTable(int numberOfMonths) throws ParseException {
        Calendar cal = Calendar.getInstance();
        int lastMonth = cal.get(Calendar.MONTH) + 1;
        int lastYear = cal.get(Calendar.YEAR);
        int lastDay = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

        cal.add(Calendar.MONTH, -numberOfMonths);
        int firstMonth = cal.get(Calendar.MONTH) + 1;
        int firstYear = cal.get(Calendar.YEAR);

        Long start = YYYYMMDDdashed.parse(firstYear + "-" + gap(firstMonth, 2) + "-01").getTime();
        Long end = YYYYMMDDdashed.parse(lastYear + "-" + gap(lastMonth, 2) + "-" + gap(lastDay, 2)).getTime();

        StringBuilder sql = new StringBuilder();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + SHOPPING_TAB + " where date >= ? and date <= ? order by id",
                new String[]{start.toString(), end.toString()});
        if (c.moveToFirst()) {
            int idIdx = c.getColumnIndex("id");
            int groupsIdx = c.getColumnIndex("group_name");
            int dateIdx = c.getColumnIndex("date");
            int nameIdx = c.getColumnIndex("name");
            int shopIdx = c.getColumnIndex("shop");
            int priceIdx = c.getColumnIndex("price");
            int amountIdx = c.getColumnIndex("amount");
            int actionIdx = c.getColumnIndex("action");
            int totalIdx = c.getColumnIndex("total");
            int action_timeIdx = c.getColumnIndex("action_time");

            do {
                long id = c.getLong(idIdx);
                long groups = c.getLong(groupsIdx);
                long date = c.getLong(dateIdx);
                String name = c.getString(nameIdx);
                String shop = c.getString(shopIdx);
                double price = c.getDouble(priceIdx);
                double amount = c.getDouble(amountIdx);
                double total = c.getDouble(totalIdx);
                int action = c.getInt(actionIdx);
                long action_time = c.getLong(action_timeIdx);

                sql.append("insert or replace into shopping (id, group_name, date, name, shop, price, amount, total, action, action_time) "
                        + "values (")
                        .append(id).append(",")
                        .append(groups).append(",")
                        .append(date).append(",'")
                        .append(name).append("','")
                        .append(shop).append("',")
                        .append(price).append(",")
                        .append(amount).append(",")
                        .append(total).append(",")
                        .append(action).append(",")
                        .append(action_time).append(");");

            } while (c.moveToNext());
        } else c.close();
        db.close();

        return sql.toString();
    }

    @NonNull
    private String getDataFromGroupsTable() throws ParseException {

        StringBuilder sql = new StringBuilder();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("select * from " + GROUPS_TAB, null);
        if (c.moveToFirst()) {
            int keyIdx = c.getColumnIndex("key");
            int valueIdx = c.getColumnIndex("value");
            int colorIdx = c.getColumnIndex("color");
            int actionIdx = c.getColumnIndex("action");
            int action_timeIdx = c.getColumnIndex("action_time");

            do {
                long key = c.getLong(keyIdx);
                String value = c.getString(valueIdx);
                String color = c.getString(colorIdx);
                int action = c.getInt(actionIdx);
                long action_time = c.getLong(action_timeIdx);

                sql.append("insert or replace into " + GROUPS_TAB + " (key, value, color, action, action_time) "
                        + "values (")
                        .append(key).append(",'")
                        .append(value).append("','")
                        .append(color).append("',")
                        .append(action).append(",")
                        .append(action_time).append(");");

            } while (c.moveToNext());
        } else c.close();
        db.close();

        return sql.toString();
    }

    public boolean setImportedData(String sql) {
        SQLiteDatabase db = getWritableDatabase();
        SQLiteStatement stmt;
        List<String> comms = Arrays.asList(sql.split(";"));
        for (String com : comms) {
            if (com.equals("")) {
                return false;
            }
            stmt = db.compileStatement(com + ";");
            stmt.execute();
        }
        db.close();
        return true;
    }

    public boolean isDatabaseExists() {
        SQLiteDatabase checkDB = null;
        try {
            checkDB = SQLiteDatabase
                    .openDatabase("/data/data/kz.gereski.m.homebank/databases/myBankDB", null,
                            SQLiteDatabase.OPEN_READONLY);
            checkDB.close();
        } catch (SQLiteException e) {
            // database doesn't exist yet.
        }
        return checkDB != null;
    }

    public void setVersion(SQLiteDatabase db, int version) {
        db.execSQL("PRAGMA user_version = " + version);
    }

    public int getVersion(SQLiteDatabase db) {
        return ((Long) DatabaseUtils.longForQuery(db, "PRAGMA user_version;", null)).intValue();
    }

    public boolean needUpgrade(SQLiteDatabase db, int newVersion) {
        return newVersion > getVersion(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public enum ACTION {INSERT, UPDATE, DELETE}
}
