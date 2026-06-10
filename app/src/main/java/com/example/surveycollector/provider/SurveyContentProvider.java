package com.example.surveycollector.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.sqlite.db.SupportSQLiteDatabase;
import com.example.surveycollector.model.AppDatabase;

public class SurveyContentProvider extends ContentProvider {

    public static final String AUTHORITY = "com.example.surveycollector";
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/surveys");

    private static final int SURVEYS = 1;
    private static final int SURVEY_ID = 2;

    private static final UriMatcher uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    static {
        uriMatcher.addURI(AUTHORITY, "surveys", SURVEYS);
        uriMatcher.addURI(AUTHORITY, "surveys/#", SURVEY_ID);
    }

    private AppDatabase database;

    @Override
    public boolean onCreate() {
        database = AppDatabase.getInstance(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection,
                        @Nullable String selection, @Nullable String[] selectionArgs,
                        @Nullable String sortOrder) {

        SupportSQLiteDatabase db = database.getOpenHelper().getReadableDatabase();
        Cursor cursor;

        switch (uriMatcher.match(uri)) {
            case SURVEYS:
                cursor = db.query(
                        "SELECT * FROM surveys" +
                                (sortOrder != null ? " ORDER BY " + sortOrder : " ORDER BY id DESC")
                );
                break;

            case SURVEY_ID:
                String id = uri.getLastPathSegment();
                cursor = db.query(
                        "SELECT * FROM surveys WHERE id = ?",
                        new Object[]{id}
                );
                break;

            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }

        if (getContext() != null) {
            cursor.setNotificationUri(getContext().getContentResolver(), uri);
        }

        return cursor;
    }

    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        switch (uriMatcher.match(uri)) {
            case SURVEYS:
                return "vnd.android.cursor.dir/vnd." + AUTHORITY + ".surveys";
            case SURVEY_ID:
                return "vnd.android.cursor.item/vnd." + AUTHORITY + ".surveys";
            default:
                throw new IllegalArgumentException("Unknown URI: " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues values) {
        throw new UnsupportedOperationException("Insert not supported — use Room DAO directly.");
    }

    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection,
                      @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Delete not supported — use Room DAO directly.");
    }

    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues values,
                      @Nullable String selection, @Nullable String[] selectionArgs) {
        throw new UnsupportedOperationException("Update not supported — use Room DAO directly.");
    }
}
