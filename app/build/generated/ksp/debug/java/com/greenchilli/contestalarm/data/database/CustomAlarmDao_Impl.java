package com.greenchilli.contestalarm.data.database;

import android.database.Cursor;
import androidx.annotation.NonNull;
import androidx.room.CoroutinesRoom;
import androidx.room.EntityInsertionAdapter;
import androidx.room.RoomDatabase;
import androidx.room.RoomSQLiteQuery;
import androidx.room.SharedSQLiteStatement;
import androidx.room.util.CursorUtil;
import androidx.room.util.DBUtil;
import androidx.sqlite.db.SupportSQLiteStatement;
import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import javax.annotation.processing.Generated;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import kotlinx.coroutines.flow.Flow;

@Generated("androidx.room.RoomProcessor")
@SuppressWarnings({"unchecked", "deprecation"})
public final class CustomAlarmDao_Impl implements CustomAlarmDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CustomAlarmEntity> __insertionAdapterOfCustomAlarmEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAlarmStatus;

  private final SharedSQLiteStatement __preparedStmtOfDeleteCustomAlarm;

  private final SharedSQLiteStatement __preparedStmtOfDeleteAlarmsOlderThan;

  public CustomAlarmDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCustomAlarmEntity = new EntityInsertionAdapter<CustomAlarmEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `custom_alarms` (`id`,`note`,`triggerTimeMillis`,`isEnabled`) VALUES (?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CustomAlarmEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getNote());
        statement.bindLong(3, entity.getTriggerTimeMillis());
        final int _tmp = entity.isEnabled() ? 1 : 0;
        statement.bindLong(4, _tmp);
      }
    };
    this.__preparedStmtOfUpdateAlarmStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE custom_alarms SET isEnabled = ? WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteCustomAlarm = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM custom_alarms WHERE id = ?";
        return _query;
      }
    };
    this.__preparedStmtOfDeleteAlarmsOlderThan = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM custom_alarms WHERE triggerTimeMillis < ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertCustomAlarm(final CustomAlarmEntity alarm,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCustomAlarmEntity.insert(alarm);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAlarmStatus(final String id, final boolean isEnabled,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAlarmStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isEnabled ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfUpdateAlarmStatus.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteCustomAlarm(final String id, final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteCustomAlarm.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, id);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteCustomAlarm.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object deleteAlarmsOlderThan(final long thresholdMillis,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfDeleteAlarmsOlderThan.acquire();
        int _argIndex = 1;
        _stmt.bindLong(_argIndex, thresholdMillis);
        try {
          __db.beginTransaction();
          try {
            _stmt.executeUpdateDelete();
            __db.setTransactionSuccessful();
            return Unit.INSTANCE;
          } finally {
            __db.endTransaction();
          }
        } finally {
          __preparedStmtOfDeleteAlarmsOlderThan.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CustomAlarmEntity>> getAllCustomAlarms() {
    final String _sql = "SELECT * FROM custom_alarms ORDER BY triggerTimeMillis ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"custom_alarms"}, new Callable<List<CustomAlarmEntity>>() {
      @Override
      @NonNull
      public List<CustomAlarmEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
          final int _cursorIndexOfTriggerTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerTimeMillis");
          final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
          final List<CustomAlarmEntity> _result = new ArrayList<CustomAlarmEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CustomAlarmEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpNote;
            _tmpNote = _cursor.getString(_cursorIndexOfNote);
            final long _tmpTriggerTimeMillis;
            _tmpTriggerTimeMillis = _cursor.getLong(_cursorIndexOfTriggerTimeMillis);
            final boolean _tmpIsEnabled;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
            _tmpIsEnabled = _tmp != 0;
            _item = new CustomAlarmEntity(_tmpId,_tmpNote,_tmpTriggerTimeMillis,_tmpIsEnabled);
            _result.add(_item);
          }
          return _result;
        } finally {
          _cursor.close();
        }
      }

      @Override
      protected void finalize() {
        _statement.release();
      }
    });
  }

  @Override
  public List<CustomAlarmEntity> getAllCustomAlarmsSync() {
    final String _sql = "SELECT * FROM custom_alarms ORDER BY triggerTimeMillis ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfNote = CursorUtil.getColumnIndexOrThrow(_cursor, "note");
      final int _cursorIndexOfTriggerTimeMillis = CursorUtil.getColumnIndexOrThrow(_cursor, "triggerTimeMillis");
      final int _cursorIndexOfIsEnabled = CursorUtil.getColumnIndexOrThrow(_cursor, "isEnabled");
      final List<CustomAlarmEntity> _result = new ArrayList<CustomAlarmEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final CustomAlarmEntity _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpNote;
        _tmpNote = _cursor.getString(_cursorIndexOfNote);
        final long _tmpTriggerTimeMillis;
        _tmpTriggerTimeMillis = _cursor.getLong(_cursorIndexOfTriggerTimeMillis);
        final boolean _tmpIsEnabled;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsEnabled);
        _tmpIsEnabled = _tmp != 0;
        _item = new CustomAlarmEntity(_tmpId,_tmpNote,_tmpTriggerTimeMillis,_tmpIsEnabled);
        _result.add(_item);
      }
      return _result;
    } finally {
      _cursor.close();
      _statement.release();
    }
  }

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
