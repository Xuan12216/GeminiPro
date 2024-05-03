package com.example.geminipro.Database;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(entities = {User.class}, version = 3, exportSchema = false)
@TypeConverters({Converters.class})
public abstract class AppDatabase extends RoomDatabase {
    public abstract UserDao userDao();

    public static final Migration MIGRATION_2_3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // 创建一个新的表格以临时保存数据
            database.execSQL("CREATE TABLE IF NOT EXISTS users_new (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                    "title TEXT, " +
                    "date TEXT, " +
                    "stringUris TEXT, " +
                    "userOrGemini TEXT, " +
                    "imageHashMap TEXT, " +
                    "pin INTEGER NOT NULL DEFAULT 0, " +
                    "funcType TEXT)");

            // 将旧表格中的数据插入到新表格中
            database.execSQL("INSERT INTO users_new (id, title, date, stringUris, userOrGemini, imageHashMap, pin, funcType) " +
                    "SELECT id, title, date, stringUris, userOrGemini, imageHashMap, pin, 'normal' FROM users");

            // 删除旧表格
            database.execSQL("DROP TABLE IF EXISTS users");

            // 将新表格重命名为旧表格的名称
            database.execSQL("ALTER TABLE users_new RENAME TO users");
        }
    };

}
