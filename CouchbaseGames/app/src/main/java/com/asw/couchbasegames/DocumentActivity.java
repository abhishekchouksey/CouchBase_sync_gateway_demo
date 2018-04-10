package com.asw.couchbasegames;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.util.Log;

import java.util.Map;

public class DocumentActivity extends AppCompatActivity {

    public static final String DB_NAME = "couchbasegames";
    public static final String TAG = "couchbasegames";
    Database database = null;
    Manager manager = null;
    EditText documentdetails;
    ReplicationHandler replicationHandler;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_document);

        replicationHandler =  ReplicationHandler.createReplicationHandler(getApplicationContext(), this);


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


        Button getDocument = (Button) findViewById(R.id.getDocument);
        EditText tid = (EditText) findViewById(R.id.documentid);
        documentdetails = (EditText) findViewById(R.id.documentdetails);


        getDocument.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                doOperation(tid.getText().toString());


            }
        });
    }

    private void doOperation(String documentId) {

        documentdetails.setText("");

        Document getDocument = database.getExistingDocument(documentId);

        if (getDocument != null) {
            for (Map.Entry<String, Object> property : getDocument
            .getProperties().entrySet()) {
                documentdetails.setText( documentdetails.getText() + property.getKey() + " :"
                        + property.getValue() +"/n");
            }
        } else {
            //replicationHandler.createPushReplication(database);

//            replicationHandler.createPullReplication(database);

            documentdetails.setText("The Document is not in mobile");
        }

    }

}

