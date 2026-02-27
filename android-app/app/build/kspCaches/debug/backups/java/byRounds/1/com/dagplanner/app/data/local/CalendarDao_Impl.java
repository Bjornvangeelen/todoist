package com.dagplanner.app.data.local;

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
import com.dagplanner.app.data.model.CalendarEvent;
import java.lang.Class;
import java.lang.Exception;
import java.lang.IllegalStateException;
import java.lang.Object;
import java.lang.Override;
import java.lang.String;
import java.lang.SuppressWarnings;
import java.time.LocalDate;
import java.time.LocalTime;
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
public final class CalendarDao_Impl implements CalendarDao {
  private final RoomDatabase __db;

  private final EntityInsertionAdapter<CalendarEvent> __insertionAdapterOfCalendarEvent;

  private final Converters __converters = new Converters();

  private final SharedSQLiteStatement __preparedStmtOfClearAll;

  private final SharedSQLiteStatement __preparedStmtOfClearByCalendar;

  public CalendarDao_Impl(@NonNull final RoomDatabase __db) {
    this.__db = __db;
    this.__insertionAdapterOfCalendarEvent = new EntityInsertionAdapter<CalendarEvent>(__db) {
      @Override
      @NonNull
      protected String createQuery() {
        return "INSERT OR REPLACE INTO `calendar_events` (`id`,`calendarId`,`title`,`description`,`location`,`startDate`,`startTime`,`endDate`,`endTime`,`isAllDay`,`colorHex`,`calendarName`,`isRecurring`,`htmlLink`) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
      }

      @Override
      protected void bind(@NonNull final SupportSQLiteStatement statement,
          @NonNull final CalendarEvent entity) {
        statement.bindString(1, entity.getId());
        statement.bindString(2, entity.getCalendarId());
        statement.bindString(3, entity.getTitle());
        if (entity.getDescription() == null) {
          statement.bindNull(4);
        } else {
          statement.bindString(4, entity.getDescription());
        }
        if (entity.getLocation() == null) {
          statement.bindNull(5);
        } else {
          statement.bindString(5, entity.getLocation());
        }
        final String _tmp = __converters.fromLocalDate(entity.getStartDate());
        if (_tmp == null) {
          statement.bindNull(6);
        } else {
          statement.bindString(6, _tmp);
        }
        final String _tmp_1 = __converters.fromLocalTime(entity.getStartTime());
        if (_tmp_1 == null) {
          statement.bindNull(7);
        } else {
          statement.bindString(7, _tmp_1);
        }
        final String _tmp_2 = __converters.fromLocalDate(entity.getEndDate());
        if (_tmp_2 == null) {
          statement.bindNull(8);
        } else {
          statement.bindString(8, _tmp_2);
        }
        final String _tmp_3 = __converters.fromLocalTime(entity.getEndTime());
        if (_tmp_3 == null) {
          statement.bindNull(9);
        } else {
          statement.bindString(9, _tmp_3);
        }
        final int _tmp_4 = entity.isAllDay() ? 1 : 0;
        statement.bindLong(10, _tmp_4);
        if (entity.getColorHex() == null) {
          statement.bindNull(11);
        } else {
          statement.bindString(11, entity.getColorHex());
        }
        statement.bindString(12, entity.getCalendarName());
        final int _tmp_5 = entity.isRecurring() ? 1 : 0;
        statement.bindLong(13, _tmp_5);
        if (entity.getHtmlLink() == null) {
          statement.bindNull(14);
        } else {
          statement.bindString(14, entity.getHtmlLink());
        }
      }
    };
    this.__preparedStmtOfClearAll = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM calendar_events";
        return _query;
      }
    };
    this.__preparedStmtOfClearByCalendar = new SharedSQLiteStatement(__db) {
      @Override
      @NonNull
      public String createQuery() {
        final String _query = "DELETE FROM calendar_events WHERE calendarId = ?";
        return _query;
      }
    };
  }

  @Override
  public Object insertEvents(final List<CalendarEvent> events,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        __db.beginTransaction();
        try {
          __insertionAdapterOfCalendarEvent.insert(events);
          __db.setTransactionSuccessful();
          return Unit.INSTANCE;
        } finally {
          __db.endTransaction();
        }
      }
    }, $completion);
  }

  @Override
  public Object clearAll(final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearAll.acquire();
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
          __preparedStmtOfClearAll.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Object clearByCalendar(final String calendarId,
      final Continuation<? super Unit> $completion) {
    return CoroutinesRoom.execute(__db, true, new Callable<Unit>() {
      @Override
      @NonNull
      public Unit call() throws Exception {
        final SupportSQLiteStatement _stmt = __preparedStmtOfClearByCalendar.acquire();
        int _argIndex = 1;
        _stmt.bindString(_argIndex, calendarId);
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
          __preparedStmtOfClearByCalendar.release(_stmt);
        }
      }
    }, $completion);
  }

  @Override
  public Flow<List<CalendarEvent>> getEventsInRange(final LocalDate from, final LocalDate to) {
    final String _sql = "SELECT * FROM calendar_events WHERE startDate >= ? AND startDate <= ? ORDER BY startDate ASC, startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 2);
    int _argIndex = 1;
    final String _tmp = __converters.fromLocalDate(from);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    _argIndex = 2;
    final String _tmp_1 = __converters.fromLocalDate(to);
    if (_tmp_1 == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp_1);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"calendar_events"}, new Callable<List<CalendarEvent>>() {
      @Override
      @NonNull
      public List<CalendarEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCalendarId = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfIsAllDay = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllDay");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfCalendarName = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarName");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfHtmlLink = CursorUtil.getColumnIndexOrThrow(_cursor, "htmlLink");
          final List<CalendarEvent> _result = new ArrayList<CalendarEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CalendarEvent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCalendarId;
            _tmpCalendarId = _cursor.getString(_cursorIndexOfCalendarId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final LocalDate _tmpStartDate;
            final String _tmp_2;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_2 = null;
            } else {
              _tmp_2 = _cursor.getString(_cursorIndexOfStartDate);
            }
            final LocalDate _tmp_3 = __converters.toLocalDate(_tmp_2);
            if (_tmp_3 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDate', but it was NULL.");
            } else {
              _tmpStartDate = _tmp_3;
            }
            final LocalTime _tmpStartTime;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfStartTime);
            }
            _tmpStartTime = __converters.toLocalTime(_tmp_4);
            final LocalDate _tmpEndDate;
            final String _tmp_5;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_5 = null;
            } else {
              _tmp_5 = _cursor.getString(_cursorIndexOfEndDate);
            }
            final LocalDate _tmp_6 = __converters.toLocalDate(_tmp_5);
            if (_tmp_6 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDate', but it was NULL.");
            } else {
              _tmpEndDate = _tmp_6;
            }
            final LocalTime _tmpEndTime;
            final String _tmp_7;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_7 = null;
            } else {
              _tmp_7 = _cursor.getString(_cursorIndexOfEndTime);
            }
            _tmpEndTime = __converters.toLocalTime(_tmp_7);
            final boolean _tmpIsAllDay;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsAllDay);
            _tmpIsAllDay = _tmp_8 != 0;
            final String _tmpColorHex;
            if (_cursor.isNull(_cursorIndexOfColorHex)) {
              _tmpColorHex = null;
            } else {
              _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            }
            final String _tmpCalendarName;
            _tmpCalendarName = _cursor.getString(_cursorIndexOfCalendarName);
            final boolean _tmpIsRecurring;
            final int _tmp_9;
            _tmp_9 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_9 != 0;
            final String _tmpHtmlLink;
            if (_cursor.isNull(_cursorIndexOfHtmlLink)) {
              _tmpHtmlLink = null;
            } else {
              _tmpHtmlLink = _cursor.getString(_cursorIndexOfHtmlLink);
            }
            _item = new CalendarEvent(_tmpId,_tmpCalendarId,_tmpTitle,_tmpDescription,_tmpLocation,_tmpStartDate,_tmpStartTime,_tmpEndDate,_tmpEndTime,_tmpIsAllDay,_tmpColorHex,_tmpCalendarName,_tmpIsRecurring,_tmpHtmlLink);
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
  public Flow<List<CalendarEvent>> getEventsForDate(final LocalDate date) {
    final String _sql = "SELECT * FROM calendar_events WHERE startDate = ? ORDER BY isAllDay DESC, startTime ASC";
    final RoomSQLiteQuery _statement = RoomSQLiteQuery.acquire(_sql, 1);
    int _argIndex = 1;
    final String _tmp = __converters.fromLocalDate(date);
    if (_tmp == null) {
      _statement.bindNull(_argIndex);
    } else {
      _statement.bindString(_argIndex, _tmp);
    }
    return CoroutinesRoom.createFlow(__db, false, new String[] {"calendar_events"}, new Callable<List<CalendarEvent>>() {
      @Override
      @NonNull
      public List<CalendarEvent> call() throws Exception {
        final Cursor _cursor = DBUtil.query(__db, _statement, false, null);
        try {
          final int _cursorIndexOfId = CursorUtil.getColumnIndexOrThrow(_cursor, "id");
          final int _cursorIndexOfCalendarId = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarId");
          final int _cursorIndexOfTitle = CursorUtil.getColumnIndexOrThrow(_cursor, "title");
          final int _cursorIndexOfDescription = CursorUtil.getColumnIndexOrThrow(_cursor, "description");
          final int _cursorIndexOfLocation = CursorUtil.getColumnIndexOrThrow(_cursor, "location");
          final int _cursorIndexOfStartDate = CursorUtil.getColumnIndexOrThrow(_cursor, "startDate");
          final int _cursorIndexOfStartTime = CursorUtil.getColumnIndexOrThrow(_cursor, "startTime");
          final int _cursorIndexOfEndDate = CursorUtil.getColumnIndexOrThrow(_cursor, "endDate");
          final int _cursorIndexOfEndTime = CursorUtil.getColumnIndexOrThrow(_cursor, "endTime");
          final int _cursorIndexOfIsAllDay = CursorUtil.getColumnIndexOrThrow(_cursor, "isAllDay");
          final int _cursorIndexOfColorHex = CursorUtil.getColumnIndexOrThrow(_cursor, "colorHex");
          final int _cursorIndexOfCalendarName = CursorUtil.getColumnIndexOrThrow(_cursor, "calendarName");
          final int _cursorIndexOfIsRecurring = CursorUtil.getColumnIndexOrThrow(_cursor, "isRecurring");
          final int _cursorIndexOfHtmlLink = CursorUtil.getColumnIndexOrThrow(_cursor, "htmlLink");
          final List<CalendarEvent> _result = new ArrayList<CalendarEvent>(_cursor.getCount());
          while (_cursor.moveToNext()) {
            final CalendarEvent _item;
            final String _tmpId;
            _tmpId = _cursor.getString(_cursorIndexOfId);
            final String _tmpCalendarId;
            _tmpCalendarId = _cursor.getString(_cursorIndexOfCalendarId);
            final String _tmpTitle;
            _tmpTitle = _cursor.getString(_cursorIndexOfTitle);
            final String _tmpDescription;
            if (_cursor.isNull(_cursorIndexOfDescription)) {
              _tmpDescription = null;
            } else {
              _tmpDescription = _cursor.getString(_cursorIndexOfDescription);
            }
            final String _tmpLocation;
            if (_cursor.isNull(_cursorIndexOfLocation)) {
              _tmpLocation = null;
            } else {
              _tmpLocation = _cursor.getString(_cursorIndexOfLocation);
            }
            final LocalDate _tmpStartDate;
            final String _tmp_1;
            if (_cursor.isNull(_cursorIndexOfStartDate)) {
              _tmp_1 = null;
            } else {
              _tmp_1 = _cursor.getString(_cursorIndexOfStartDate);
            }
            final LocalDate _tmp_2 = __converters.toLocalDate(_tmp_1);
            if (_tmp_2 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDate', but it was NULL.");
            } else {
              _tmpStartDate = _tmp_2;
            }
            final LocalTime _tmpStartTime;
            final String _tmp_3;
            if (_cursor.isNull(_cursorIndexOfStartTime)) {
              _tmp_3 = null;
            } else {
              _tmp_3 = _cursor.getString(_cursorIndexOfStartTime);
            }
            _tmpStartTime = __converters.toLocalTime(_tmp_3);
            final LocalDate _tmpEndDate;
            final String _tmp_4;
            if (_cursor.isNull(_cursorIndexOfEndDate)) {
              _tmp_4 = null;
            } else {
              _tmp_4 = _cursor.getString(_cursorIndexOfEndDate);
            }
            final LocalDate _tmp_5 = __converters.toLocalDate(_tmp_4);
            if (_tmp_5 == null) {
              throw new IllegalStateException("Expected NON-NULL 'java.time.LocalDate', but it was NULL.");
            } else {
              _tmpEndDate = _tmp_5;
            }
            final LocalTime _tmpEndTime;
            final String _tmp_6;
            if (_cursor.isNull(_cursorIndexOfEndTime)) {
              _tmp_6 = null;
            } else {
              _tmp_6 = _cursor.getString(_cursorIndexOfEndTime);
            }
            _tmpEndTime = __converters.toLocalTime(_tmp_6);
            final boolean _tmpIsAllDay;
            final int _tmp_7;
            _tmp_7 = _cursor.getInt(_cursorIndexOfIsAllDay);
            _tmpIsAllDay = _tmp_7 != 0;
            final String _tmpColorHex;
            if (_cursor.isNull(_cursorIndexOfColorHex)) {
              _tmpColorHex = null;
            } else {
              _tmpColorHex = _cursor.getString(_cursorIndexOfColorHex);
            }
            final String _tmpCalendarName;
            _tmpCalendarName = _cursor.getString(_cursorIndexOfCalendarName);
            final boolean _tmpIsRecurring;
            final int _tmp_8;
            _tmp_8 = _cursor.getInt(_cursorIndexOfIsRecurring);
            _tmpIsRecurring = _tmp_8 != 0;
            final String _tmpHtmlLink;
            if (_cursor.isNull(_cursorIndexOfHtmlLink)) {
              _tmpHtmlLink = null;
            } else {
              _tmpHtmlLink = _cursor.getString(_cursorIndexOfHtmlLink);
            }
            _item = new CalendarEvent(_tmpId,_tmpCalendarId,_tmpTitle,_tmpDescription,_tmpLocation,_tmpStartDate,_tmpStartTime,_tmpEndDate,_tmpEndTime,_tmpIsAllDay,_tmpColorHex,_tmpCalendarName,_tmpIsRecurring,_tmpHtmlLink);
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

  @NonNull
  public static List<Class<?>> getRequiredConverters() {
    return Collections.emptyList();
  }
}
