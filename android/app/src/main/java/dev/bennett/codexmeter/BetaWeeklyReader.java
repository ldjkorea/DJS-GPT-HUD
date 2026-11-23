package dev.bennett.codexmeter;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import java.util.*;

/** Worker-only bounded SELECT; no migration, rollup, network lock or writes. */
final class BetaWeeklyReader {
    static final int MAX_ROWS=20000;
    static BetaWeeklyUsage.Model read(Context context,UsageSnapshot snapshot,long now)throws Exception {
        if(android.os.Looper.myLooper()==android.os.Looper.getMainLooper())throw new IllegalStateException("Worker required");
        BetaWeeklyUsage.Model empty=BetaWeeklyUsage.project(snapshot,Collections.emptyList(),now);
        if(empty.start==0||empty.until<empty.start)return empty;
        long generation = UsageLedgerDatabase.readableGeneration(context);
        if(!context.getDatabasePath(UsageLedgerDatabase.NAME).isFile())return empty;
        List<LedgerRecord> records=new ArrayList<>();
        try(SQLiteDatabase db=SQLiteDatabase.openDatabase(context.getDatabasePath(UsageLedgerDatabase.NAME).getAbsolutePath(),null,SQLiteDatabase.OPEN_READONLY);
            Cursor rows=db.rawQuery("SELECT meter,plan,observed_at,used,used_decimal,reset_at,window_seconds,source,manual FROM observations WHERE meter='weekly' AND observed_at>=? AND observed_at<=? ORDER BY observed_at LIMIT "+(MAX_ROWS+1),new String[]{Long.toString(empty.start),Long.toString(empty.until)})){
            while(rows.moveToNext()){
                if(records.size()==MAX_ROWS)throw new IllegalStateException("Incomplete observation set");
                records.add(new LedgerRecord(rows.getString(0),rows.getString(1),rows.getLong(2),rows.getDouble(3),rows.getString(4),rows.getLong(5),rows.getLong(6),rows.getString(7),rows.getInt(8)==1));
            }
        }
        if (generation != UsageLedgerDatabase.readableGeneration(context))
            throw new IllegalStateException("History changed while reading");
        return BetaWeeklyUsage.project(snapshot,records,now);
    }
}
