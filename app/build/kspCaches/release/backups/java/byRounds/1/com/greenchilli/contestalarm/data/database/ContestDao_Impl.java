package com.greenchilli.contestalarm.data.database;

import android.database.Cursor;
import android.os.CancellationSignal;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import java.lang.Integer;
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
public final class ContestDao_Impl implements ContestDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<ContestEntity> __insertionAdapterOfContestEntity;

  private final SharedSQLiteStatement __preparedStmtOfUpdateAlarmStatus;

  public ContestDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfContestEntity = new EntityInsertionAdapter<ContestEntity>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `contests` (`id`,`name`,`startTimeSeconds`,`durationSeconds`,`items`,`platform`,`url`,`status`,`isAlarmSet`,`alarmOffsetSeconds`) VALUES (?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final ContestEntity entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getName());
        statement.bindLong(3, entity.getStartTimeSeconds());
        statement.bindLong(4, entity.getDurationSeconds());
        if (entity.getItems() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getItems());
        }
        statement.bindString(6, entity.getPlatform());
        statement.bindString(7, entity.getUrl());
        statement.bindString(8, entity.getStatus());
        final int _tmp = entity.isAlarmSet() ? 1 : 0;
        statement.bindLong(9, _tmp);
        statement.bindLong(10, entity.getAlarmOffsetSeconds());
      }
    };
    this.__preparedStmtOfUpdateAlarmStatus = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "UPDATE contests SET isAlarmSet = ?, alarmOffsetSeconds = ? WHERE id = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertContests(final List<ContestEntity> contests,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfContestEntity.insert(contests);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object updateAlarmStatus(final String contestId, final boolean isSet, final long offset,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfUpdateAlarmStatus.acquire();
        int _argIndex = 1;
        final int _tmp = isSet ? 1 : 0;
        _stmt.bindLong(_argIndex, _tmp);
        _argIndex = 2;
        _stmt.bindLong(_argIndex, offset);
        _argIndex = 3;
        _stmt.bindString(_argIndex, contestId);
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
  public Flow<List<ContestEntity>> getAllContests() {
    final String _sql = "SELECT * FROM contests ORDER BY startTimeSeconds ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"contests"}, new Callable<List<ContestEntity>>() {
      @Override
      @NonNull
      public List<ContestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeSeconds");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfItems = CursorUtil.getColumnIndexOrThrow(_cursor, "items");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsAlarmSet = CursorUtil.getColumnIndexOrThrow(_cursor, "isAlarmSet");
          final int _cursorIndexOfAlarmOffsetSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmOffsetSeconds");
          final List<ContestEntity> _result = new ArrayList<ContestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ContestEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpStartTimeSeconds;
            _tmpStartTimeSeconds = _cursor.getLong(_cursorIndexOfStartTimeSeconds);
            final int _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getInt(_cursorIndexOfDurationSeconds);
            final String _tmpItems;
            if (_cursor.isNull(_cursorIndexOfItems)) {
              _tmpItems = null;
            } else {
              _tmpItems = _cursor.getString(_cursorIndexOfItems);
            }
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsAlarmSet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAlarmSet);
            _tmpIsAlarmSet = _tmp != 0;
            final long _tmpAlarmOffsetSeconds;
            _tmpAlarmOffsetSeconds = _cursor.getLong(_cursorIndexOfAlarmOffsetSeconds);
            _item = new ContestEntity(_tmpId,_tmpName,_tmpStartTimeSeconds,_tmpDurationSeconds,_tmpItems,_tmpPlatform,_tmpUrl,_tmpStatus,_tmpIsAlarmSet,_tmpAlarmOffsetSeconds);
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
  public Flow<List<ContestEntity>> getUpcomingContests(final long currentTimeSeconds) {
    final String _sql = "SELECT * FROM contests WHERE startTimeSeconds > ? ORDER BY startTimeSeconds ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, currentTimeSeconds);
    return CoroutinesRoom.createFlow(__db, false, new String[] {"contests"}, new Callable<List<ContestEntity>>() {
      @Override
      @NonNull
      public List<ContestEntity> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeSeconds");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfItems = CursorUtil.getColumnIndexOrThrow(_cursor, "items");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsAlarmSet = CursorUtil.getColumnIndexOrThrow(_cursor, "isAlarmSet");
          final int _cursorIndexOfAlarmOffsetSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmOffsetSeconds");
          final List<ContestEntity> _result = new ArrayList<ContestEntity>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final ContestEntity _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpStartTimeSeconds;
            _tmpStartTimeSeconds = _cursor.getLong(_cursorIndexOfStartTimeSeconds);
            final int _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getInt(_cursorIndexOfDurationSeconds);
            final String _tmpItems;
            if (_cursor.isNull(_cursorIndexOfItems)) {
              _tmpItems = null;
            } else {
              _tmpItems = _cursor.getString(_cursorIndexOfItems);
            }
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsAlarmSet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAlarmSet);
            _tmpIsAlarmSet = _tmp != 0;
            final long _tmpAlarmOffsetSeconds;
            _tmpAlarmOffsetSeconds = _cursor.getLong(_cursorIndexOfAlarmOffsetSeconds);
            _item = new ContestEntity(_tmpId,_tmpName,_tmpStartTimeSeconds,_tmpDurationSeconds,_tmpItems,_tmpPlatform,_tmpUrl,_tmpStatus,_tmpIsAlarmSet,_tmpAlarmOffsetSeconds);
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
  public Object getContestById(final String contestId,
      final Continuation<? super ContestEntity> $completion) {
    final String _sql = "SELECT * FROM contests WHERE id = ?";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindString(_argIndex, contestId);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<ContestEntity>() {
      @Override
      @Nullable
      public ContestEntity call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
          final int _cursorIndexOfStartTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeSeconds");
          final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
          final int _cursorIndexOfItems = CursorUtil.getColumnIndexOrThrow(_cursor, "items");
          final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
          final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
          final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
          final int _cursorIndexOfIsAlarmSet = CursorUtil.getColumnIndexOrThrow(_cursor, "isAlarmSet");
          final int _cursorIndexOfAlarmOffsetSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmOffsetSeconds");
          final ContestEntity _result;
          if (_cursor.moveToFirst()) {
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpName;
            _tmpName = _cursor.getString(_cursorIndexOfName);
            final long _tmpStartTimeSeconds;
            _tmpStartTimeSeconds = _cursor.getLong(_cursorIndexOfStartTimeSeconds);
            final int _tmpDurationSeconds;
            _tmpDurationSeconds = _cursor.getInt(_cursorIndexOfDurationSeconds);
            final String _tmpItems;
            if (_cursor.isNull(_cursorIndexOfItems)) {
              _tmpItems = null;
            } else {
              _tmpItems = _cursor.getString(_cursorIndexOfItems);
            }
            final String _tmpPlatform;
            _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
            final String _tmpUrl;
            _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
            final String _tmpStatus;
            _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
            final boolean _tmpIsAlarmSet;
            final int _tmp;
            _tmp = _cursor.getInt(_cursorIndexOfIsAlarmSet);
            _tmpIsAlarmSet = _tmp != 0;
            final long _tmpAlarmOffsetSeconds;
            _tmpAlarmOffsetSeconds = _cursor.getLong(_cursorIndexOfAlarmOffsetSeconds);
            _result = new ContestEntity(_tmpId,_tmpName,_tmpStartTimeSeconds,_tmpDurationSeconds,_tmpItems,_tmpPlatform,_tmpUrl,_tmpStatus,_tmpIsAlarmSet,_tmpAlarmOffsetSeconds);
          } else {
            _result = null;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public Object getContestCount(final Continuation<? super Integer> $completion) {
    final String _sql = "SELECT COUNT(*) FROM contests";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 0);
    final CancellationSignal _cancellationSignal = DBUtil.createCancellationSignal();
    return CoroutinesRoom.execute(__db, false, _cancellationSignal, new Callable<Integer>() {
      @Override
      @NonNull
      public Integer call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final Integer _result;
          if (_cursor.moveToFirst()) {
            final int _tmp;
            _tmp = _cursor.getInt(0);
            _result = _tmp;
          } else {
            _result = 0;
          }
          return _result;
        } finally {
          _cursor.close();
          _statement.release();
        }
      }
    }, $completion);
  }

  @Override
  public List<ContestEntity> getUpcomingContestsSync(final long currentTimeSeconds) {
    final String _sql = "SELECT * FROM contests WHERE startTimeSeconds > ? ORDER BY startTimeSeconds ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    _statement.bindLong(_argIndex, currentTimeSeconds);
    __db.assertNotSuspendingTransaction();
    final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
    try {
      final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
      final int _cursorIndexOfName = CursorUtil.getColumnIndexOrThrow(_cursor, "name");
      final int _cursorIndexOfStartTimeSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "startTimeSeconds");
      final int _cursorIndexOfDurationSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "durationSeconds");
      final int _cursorIndexOfItems = CursorUtil.getColumnIndexOrThrow(_cursor, "items");
      final int _cursorIndexOfPlatform = CursorUtil.getColumnIndexOrThrow(_cursor, "platform");
      final int _cursorIndexOfUrl = CursorUtil.getColumnIndexOrThrow(_cursor, "url");
      final int _cursorIndexOfStatus = CursorUtil.getColumnIndexOrThrow(_cursor, "status");
      final int _cursorIndexOfIsAlarmSet = CursorUtil.getColumnIndexOrThrow(_cursor, "isAlarmSet");
      final int _cursorIndexOfAlarmOffsetSeconds = CursorUtil.getColumnIndexOrThrow(_cursor, "alarmOffsetSeconds");
      final List<ContestEntity> _result = new ArrayList<ContestEntity>(_cursor.getCount());
      while (_cursor.moveToNext()) {
        final ContestEntity _item;
        final String _tmpId;
        _tmpId = _cursor.getString(_cursorIndexOfId);
        final String _tmpName;
        _tmpName = _cursor.getString(_cursorIndexOfName);
        final long _tmpStartTimeSeconds;
        _tmpStartTimeSeconds = _cursor.getLong(_cursorIndexOfStartTimeSeconds);
        final int _tmpDurationSeconds;
        _tmpDurationSeconds = _cursor.getInt(_cursorIndexOfDurationSeconds);
        final String _tmpItems;
        if (_cursor.isNull(_cursorIndexOfItems)) {
          _tmpItems = null;
        } else {
          _tmpItems = _cursor.getString(_cursorIndexOfItems);
        }
        final String _tmpPlatform;
        _tmpPlatform = _cursor.getString(_cursorIndexOfPlatform);
        final String _tmpUrl;
        _tmpUrl = _cursor.getString(_cursorIndexOfUrl);
        final String _tmpStatus;
        _tmpStatus = _cursor.getString(_cursorIndexOfStatus);
        final boolean _tmpIsAlarmSet;
        final int _tmp;
        _tmp = _cursor.getInt(_cursorIndexOfIsAlarmSet);
        _tmpIsAlarmSet = _tmp != 0;
        final long _tmpAlarmOffsetSeconds;
        _tmpAlarmOffsetSeconds = _cursor.getLong(_cursorIndexOfAlarmOffsetSeconds);
        _item = new ContestEntity(_tmpId,_tmpName,_tmpStartTimeSeconds,_tmpDurationSeconds,_tmpItems,_tmpPlatform,_tmpUrl,_tmpStatus,_tmpIsAlarmSet,_tmpAlarmOffsetSeconds);
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
