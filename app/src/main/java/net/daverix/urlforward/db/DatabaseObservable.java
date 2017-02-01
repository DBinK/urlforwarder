package net.daverix.urlforward.db;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import io.reactivex.Observable;
import io.reactivex.Single;
import io.reactivex.functions.Function;

public class DatabaseObservable {
    private final SQLiteDatabase database;

    public static DatabaseObservable getWritable(SQLiteOpenHelper helper) {
        return new DatabaseObservable(helper.getWritableDatabase());
    }

    public static DatabaseObservable getReadable(SQLiteOpenHelper helper) {
        return new DatabaseObservable(helper.getReadableDatabase());
    }

    private DatabaseObservable(SQLiteDatabase database) {
        this.database = database;
    }

    public Single<Long> insert(String table, ContentValues values) {
        return databaseOperation(db -> db.insertOrThrow(table, null, values));
    }

    public Single<Integer> update(String table, ContentValues values, String where, String[] whereArgs) {
        return databaseOperation(db -> db.update(table, values, where, whereArgs));
    }

    public Single<Integer> delete(String table, String where, String[] whereArgs) {
        return databaseOperation(db -> db.delete(table, where, whereArgs));
    }

    public Observable<Cursor> query(String table, String[] columns, String selection, String[] selectionArgs) {
        return query(db -> db.query(false, table, columns, selection, selectionArgs, null, null, null, "1"));
    }

    private <T> Single<T> databaseOperation(Function<SQLiteDatabase,T> func) {
        return Single.create(s -> {
            if(s.isDisposed()) return;

            T result = func.apply(database);
            if(!s.isDisposed())
                s.onSuccess(result);
        });
    }

    private Observable<Cursor> query(Function<SQLiteDatabase,Cursor> queryFunc) {
        return Observable.create(s -> {
            if(s.isDisposed())
                return;

            Cursor cursor = null;
            try {
                cursor = queryFunc.apply(database);

                if (s.isDisposed())
                    return;

                if (cursor == null) {
                    s.onError(new IllegalStateException("cursor is null"));
                    return;
                }

                while (!s.isDisposed() && cursor.moveToNext()) {
                    s.onNext(cursor);
                }

                if (!s.isDisposed()) {
                    s.onComplete();
                }
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        });
    }
}
