package com.greenchilli.contestalarm.data.database;

import androidx.annotation.NonNull;
import androidx.room.DatabaseConfiguration;
import androidx.room.InvalidationTracker;
import androidx.room.RoomDatabase;
import androidx.room.RoomOpenHelper;
import androidx.room.migration.AutoMigrationSpec;
import androidx.room.migration.Migration;
import androidx.room.util.DBUtil;
import androidx.room.util.TableInfo;
import androidx.sqlite.db.SupportSQLiteDatabase;
import androidx.sqlite.db.SupportSQLiteOpenHelper;
import java.lang.Class;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.annotation.processing.Generated;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class AppDatabase_Impl extends AppDatabase {
  private volatile ContestDao _contestDao;

  private volatile CustomAlarmDao _customAlarmDao;

  @Override
  @NonNull
  protected SupportSQLiteOpenHelper createOpenHelper(@NonNull final DatabaseConfiguration config) {
    final SupportSQLiteOpenHelper.Callback _openCallback = new RoomOpenHelper(config, new RoomOpenHelper.Delegate(3) {
      @Override
      public void createAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS `contests` (`id` TEXT NOT NULL, `name` TEXT NOT NULL, `startTimeSeconds` INTEGER NOT NULL, `durationSeconds` INTEGER NOT NULL, `items` TEXT, `platform` TEXT NOT NULL, `url` TEXT NOT NULL, `status` TEXT NOT NULL, `isAlarmSet` INTEGER NOT NULL, `alarmOffsetSeconds` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS `custom_alarms` (`id` TEXT NOT NULL, `note` TEXT NOT NULL, `triggerTimeMillis` INTEGER NOT NULL, `isEnabled` INTEGER NOT NULL, PRIMARY KEY(`id`))");
        db.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)");
        db.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'ba1b3018e712a846e6c8cadfef6b1002')");
      }

      @Override
      public void dropAllTables(@NonNull final SupportSQLiteDatabase db) {
        db.execSQL("DROP TABLE IF EXISTS `contests`");
        db.execSQL("DROP TABLE IF EXISTS `custom_alarms`");
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onDestructiveMigration(db);
          }
        }
      }

      @Override
      public void onCreate(@NonNull final SupportSQLiteDatabase db) {
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onCreate(db);
          }
        }
      }

      @Override
      public void onOpen(@NonNull final SupportSQLiteDatabase db) {
        mDatabase = db;
        internalInitInvalidationTracker(db);
        final List<? extends RoomDatabase.Callback> _callbacks = mCallbacks;
        if (_callbacks != null) {
          for (RoomDatabase.Callback _callback : _callbacks) {
            _callback.onOpen(db);
          }
        }
      }

      @Override
      public void onPreMigrate(@NonNull final SupportSQLiteDatabase db) {
        DBUtil.dropFtsSyncTriggers(db);
      }

      @Override
      public void onPostMigrate(@NonNull final SupportSQLiteDatabase db) {
      }

      @Override
      @NonNull
      public RoomOpenHelper.ValidationResult onValidateSchema(
          @NonNull final SupportSQLiteDatabase db) {
        final HashMap<String, TableInfo.Column> _columnsContests = new HashMap<String, TableInfo.Column>(10);
        _columnsContests.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("name", new TableInfo.Column("name", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("startTimeSeconds", new TableInfo.Column("startTimeSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("durationSeconds", new TableInfo.Column("durationSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("items", new TableInfo.Column("items", "TEXT", false, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("platform", new TableInfo.Column("platform", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("url", new TableInfo.Column("url", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("status", new TableInfo.Column("status", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("isAlarmSet", new TableInfo.Column("isAlarmSet", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsContests.put("alarmOffsetSeconds", new TableInfo.Column("alarmOffsetSeconds", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysContests = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesContests = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoContests = new TableInfo("contests", _columnsContests, _foreignKeysContests, _indicesContests);
        final TableInfo _existingContests = TableInfo.read(db, "contests");
        if (!_infoContests.equals(_existingContests)) {
          return new RoomOpenHelper.ValidationResult(false, "contests(com.greenchilli.contestalarm.data.database.ContestEntity).\n"
                  + " Expected:\n" + _infoContests + "\n"
                  + " Found:\n" + _existingContests);
        }
        final HashMap<String, TableInfo.Column> _columnsCustomAlarms = new HashMap<String, TableInfo.Column>(4);
        _columnsCustomAlarms.put("id", new TableInfo.Column("id", "TEXT", true, 1, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomAlarms.put("note", new TableInfo.Column("note", "TEXT", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomAlarms.put("triggerTimeMillis", new TableInfo.Column("triggerTimeMillis", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        _columnsCustomAlarms.put("isEnabled", new TableInfo.Column("isEnabled", "INTEGER", true, 0, null, TableInfo.CREATED_FROM_ENTITY));
        final HashSet<TableInfo.ForeignKey> _foreignKeysCustomAlarms = new HashSet<TableInfo.ForeignKey>(0);
        final HashSet<TableInfo.Index> _indicesCustomAlarms = new HashSet<TableInfo.Index>(0);
        final TableInfo _infoCustomAlarms = new TableInfo("custom_alarms", _columnsCustomAlarms, _foreignKeysCustomAlarms, _indicesCustomAlarms);
        final TableInfo _existingCustomAlarms = TableInfo.read(db, "custom_alarms");
        if (!_infoCustomAlarms.equals(_existingCustomAlarms)) {
          return new RoomOpenHelper.ValidationResult(false, "custom_alarms(com.greenchilli.contestalarm.data.database.CustomAlarmEntity).\n"
                  + " Expected:\n" + _infoCustomAlarms + "\n"
                  + " Found:\n" + _existingCustomAlarms);
        }
        return new RoomOpenHelper.ValidationResult(true, null);
      }
    }, "ba1b3018e712a846e6c8cadfef6b1002", "95049209f1dde0fb17519bb78a952a42");
    final SupportSQLiteOpenHelper.Configuration _sqliteConfig = SupportSQLiteOpenHelper.Configuration.builder(config.context).name(config.name).callback(_openCallback).build();
    final SupportSQLiteOpenHelper _helper = config.sqliteOpenHelperFactory.create(_sqliteConfig);
    return _helper;
  }

  @Override
  @NonNull
  protected InvalidationTracker createInvalidationTracker() {
    final HashMap<String, String> _shadowTablesMap = new HashMap<String, String>(0);
    final HashMap<String, Set<String>> _viewTables = new HashMap<String, Set<String>>(0);
    return new InvalidationTracker(this, _shadowTablesMap, _viewTables, "contests","custom_alarms");
  }

  @Override
  public void clearAllTables() {
    super.assertNotMainThread();
    final SupportSQLiteDatabase _db = super.getOpenHelper().getWritableDatabase();
    try {
      super.beginTransaction();
      _db.execSQL("DELETE FROM `contests`");
      _db.execSQL("DELETE FROM `custom_alarms`");
      super.setTransactionSuccessful();
    } finally {
      super.endTransaction();
      _db.query("PRAGMA wal_checkpoint(FULL)").close();
      if (!_db.inTransaction()) {
        _db.execSQL("VACUUM");
      }
    }
  }

  @Override
  @NonNull
  protected Map<Class<?>, List<Class<?>>> getRequiredTypeConverters() {
    final HashMap<Class<?>, List<Class<?>>> _typeConvertersMap = new HashMap<Class<?>, List<Class<?>>>();
    _typeConvertersMap.put(ContestDao.class, ContestDao_Impl.getRequiredConverters());
    _typeConvertersMap.put(CustomAlarmDao.class, CustomAlarmDao_Impl.getRequiredConverters());
    return _typeConvertersMap;
  }

  @Override
  @NonNull
  public Set<Class<? extends AutoMigrationSpec>> getRequiredAutoMigrationSpecs() {
    final HashSet<Class<? extends AutoMigrationSpec>> _autoMigrationSpecsSet = new HashSet<Class<? extends AutoMigrationSpec>>();
    return _autoMigrationSpecsSet;
  }

  @Override
  @NonNull
  public List<Migration> getAutoMigrations(
      @NonNull final Map<Class<? extends AutoMigrationSpec>, AutoMigrationSpec> autoMigrationSpecs) {
    final List<Migration> _autoMigrations = new ArrayList<Migration>();
    return _autoMigrations;
  }

  @Override
  public ContestDao contestDao() {
    if (_contestDao != null) {
      return _contestDao;
    } else {
      synchronized(this) {
        if(_contestDao == null) {
          _contestDao = new ContestDao_Impl(this);
        }
        return _contestDao;
      }
    }
  }

  @Override
  public CustomAlarmDao customAlarmDao() {
    if (_customAlarmDao != null) {
      return _customAlarmDao;
    } else {
      synchronized(this) {
        if(_customAlarmDao == null) {
          _customAlarmDao = new CustomAlarmDao_Impl(this);
        }
        return _customAlarmDao;
      }
    }
  }
}
