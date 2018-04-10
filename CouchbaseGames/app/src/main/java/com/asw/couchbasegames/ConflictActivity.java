package com.asw.couchbasegames;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.TransactionalTask;
import com.couchbase.lite.UnsavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ConflictActivity extends AppCompatActivity {

    public static final String DB_NAME = "couchbasegames";
    public static final String TAG = "couchbasegames";
    Database database = null;
    Manager manager = null;
    TextView trevsid = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conflict);


        try {
            manager = new Manager(
                    new AndroidContext(getApplicationContext()),
                    Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(DB_NAME);
        } catch (Exception e) {
            Log.e(TAG, "Error getting database", e);
            return;
        }

        Database finalDatabase = database;


        Button vConflicts = (Button) findViewById(R.id.getConflits);
        EditText tid = (EditText) findViewById(R.id.confId);

        trevsid = (TextView) findViewById(R.id.revids);


        vConflicts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                //doOperation(tid.getText().toString());


            }
        });
    }

    private void doOperation(String id) {

        Document document = database.getDocument(id);
        String revIds = "";

        List<SavedRevision> savedRevisions;
        try {
            savedRevisions = document.getConflictingRevisions();
            if (savedRevisions.size() > 1) {
                // There is more than one current revision, thus a conflict!
                database.runInTransaction(new TransactionalTask() {
                    @Override
                    public boolean run() {
                        try {
                            // Come up with a merged/resolved document in some way that's
                            // appropriate for the app. You could even just pick the body of
                            // one of the revisions.
                            Map<String, Object> mergedProps = mergeRevisions(savedRevisions);
                            // Delete the conflicting revisions to get rid of the conflict:
                            SavedRevision current = document.getCurrentRevision();
                            for (SavedRevision rev : savedRevisions) {
                                UnsavedRevision newRev = rev.createRevision();
                                if (rev.getId().equals(current.getId())) {
                                    newRev.setProperties(mergedProps);
                                } else {
                                    newRev.setIsDeletion(true);
                                }
                                // saveAllowingConflict allows 'rev' to be updated even if it
                                // is not the document's current revision.
                                newRev.save(true);
                            }
                        } catch (CouchbaseLiteException e) {
                            return false;
                        }
                        return true;
                    }
                });
            }

        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error getting database", e);
        }

    }


    //merging revision based on time it is saved.
    //one more strategy
//    https://github.com/dankito/Sync/blob/d208aac8dbbb152b1393200a8dd1c6d94fbbb0c1/libs/CouchbaseLiteSyncManager/src/main/java/net/dankito/sync/synchronization/ConflictHandler.java

    private Map<String, Object> mergeRevisions(List<SavedRevision> savedRevisions) {
        Object time_saved = null;
        Object time_saved2 = null;
        SavedRevision Rev = null;

        for (SavedRevision savedRevision : savedRevisions) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");

            if (time_saved == null) {
                time_saved = savedRevision.getProperty("time_saved");
                Rev = savedRevision;
                continue;
            } else {
                time_saved2 = savedRevision.getProperty("time_saved");
                try {
                    if (sdf.parse(time_saved.toString()).after(sdf.parse(time_saved2.toString()))) {
                        time_saved = time_saved2;
                        Rev = savedRevision;
                    }
                } catch (ParseException ex) {

                }
            }
        }

        return Rev.getProperties();


    }

}

