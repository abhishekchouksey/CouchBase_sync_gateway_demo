package com.asw.couchbasegames;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.couchbase.lite.CouchbaseLiteException;
import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.QueryOptions;
import com.couchbase.lite.QueryRow;
import com.couchbase.lite.RevisionList;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.internal.RevisionInternal;
import com.couchbase.lite.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HelloWorldActivity extends AppCompatActivity {

    public static final String DB_NAME = "asmstik";
    public static final String TAG = "asmstik";
    static int id = 1;

    final ArrayList<String> documentIds = new ArrayList<String>();
    String tkey = "";
     String tvalue ="";
     String tid = "";

    ReplicationHandler replicationHandler;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Manager manager = null;
        Database database = null;

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

        Button createNewDocument = (Button)findViewById(R.id.createNewDocument);

        TextView newDocumentId = (TextView)findViewById(R.id.newDocumentId);



        Button add = (Button)findViewById(R.id.add);

        Button update = (Button)findViewById(R.id.updateContent);

        Button pull = (Button)findViewById(R.id.pull);

        Button vConflicts = (Button)findViewById(R.id.viewConflicts);

        Button purge = (Button)findViewById(R.id.purgedocument);

        Button viewDocument = (Button)findViewById(R.id.viewDocument);





        TextView key = (TextView)findViewById(R.id.addKey);
        TextView id = (TextView)findViewById(R.id.addId);
        TextView value = (TextView)findViewById(R.id.addValue);


        Database finalDatabase = database;

        Map<String, Object> map = new HashMap<String, Object>();

        add.setOnClickListener(new View.OnClickListener() {
           public void onClick(View v) {

               tkey = key.getText().toString();
               tvalue = value.getText().toString();

               map.put(tkey, tvalue);

               key.setText("");
               value.setText("");
           }

        });


        createNewDocument.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                documentIds.add(createDocument(finalDatabase, id.getText().toString(), map));

                String allIds = "";

                for (String str: documentIds) {
                    allIds = allIds + str + "   ";
                }
                newDocumentId.setText(allIds);

                replicationHandler.createPushReplication(finalDatabase);

            }
        } );


        Map<String, Object> updatedProperties = new HashMap<String, Object>();

        update.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v) {

                tkey = key.getText().toString();

                tvalue = value.getText().toString();

                updatedProperties.put(tkey, tvalue);

                updateDoc(finalDatabase, id.getText().toString(), updatedProperties);

                replicationHandler.createPushReplication(finalDatabase);
            }

            });


        pull.setOnClickListener(new View.OnClickListener() {
                                      public void onClick(View v) {

                                          replicationHandler.createPullReplication(finalDatabase);
                                      }
                                  });


        purge.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                try {
                    QueryOptions option = new QueryOptions();
                    Map<String, Object> listofdocs = finalDatabase.getAllDocs(option);

                    for (Map.Entry<String, Object> property : listofdocs.entrySet())
                    {
                        if(property.getKey() == "rows")
                        {
                            List<QueryRow> q =  (List<QueryRow>) property.getValue() ;

                            for(QueryRow r : q)
                            {
                                finalDatabase.getDocument(r.getDocumentId()).purge();
                            }
                        }
                    }

                }catch(CouchbaseLiteException e)
                {

                }

                //finalDatabase.purgeRevisions()
            }
        });

        vConflicts.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                goToConflictActivity();


            }
        });

        viewDocument.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                goToViewDocumentActivity();

            }
        });

        // Create the document





        // Update the document and add an attachment
      //  updateDoc(database, documentId[0]);

        // Get and output the contents
       // outputContents(database, documentId[0]);

        // Get and output the contents
      //  outputContents(database, documentId[0]);

        // Delete the new document
       // deleteDocument(database, documentId);

    }

    private void goToViewDocumentActivity() {
        Intent intent = new Intent(this, DocumentActivity.class);
        startActivity(intent);
    }


    private void goToConflictActivity() {

        Intent intent = new Intent(this, ConflictActivity.class);
        startActivity(intent);
    }


    /**
     * Creates the document
     *
     * @param database The CBL database
     * @return The Id of the Document that was created
     */
    private String createDocument(Database database, String id, Map<String, Object> map) {
        // Create a new document and add data

              Document document = new Document(database, id);


/*
        //String documentId = document.getId();

       // Map<String, Object> map = new HashMap<String, Object>();

        map.put("name", "John Adams");
        map.put("score", "42");
        map.put("_id", String.valueOf(id));*/


        try {
            // Save the properties to the document
            document.putProperties(map);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }

        return id;
    }

    /**
     * Outputs the contents of the document
     *
     * @param database   The CBL database
     * @param documentId The Id of the Document to output
     */
    private void outputContents(Database database, String documentId) {
        // Get the document and output all of the contents
        Document getDocument = database.getExistingDocument(documentId);

        if (getDocument != null) {
            for (Map.Entry<String, Object> property : getDocument
                    .getProperties().entrySet()) {
                Log.i(TAG, "Property Key:" + property.getKey() + " Value:"
                        + property.getValue());
            }
        } else {
            Log.i(TAG, "The Document was null");
        }
    }

    /**
     * Updates the document
     *
     * @param database   The CBL database
     * @param documentId The Id of the Document to output
     */
    private void updateDoc(Database database, String documentId, Map<String, Object> updatedProperties) {
        Document getDocument = database.getDocument(documentId);

        try {
            // Update the document with more data
            Map<String, Object> documentProperties = new HashMap<String, Object>();

            documentProperties.putAll(getDocument.getProperties());

            for (Map.Entry<String, Object> property : updatedProperties.entrySet()) {
                documentProperties.put(property.getKey(), property.getValue());
            }

            /*updatedProperties.put("score", "1337");
            updatedProperties.put("game", "Space Invaders");*/

            // Save the properties to the document

            getDocument.putProperties(documentProperties);


        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error putting", e);
        }
    }

    /**
     * Deletes the document
     *
     * @param database   The CBL database
     * @param documentId The Id of the Document to delete
     */
    private void deleteDocument(Database database, String documentId) {
        // Get the updated document and output all of the contents
        Document deleteDocument = database.getDocument(documentId);

        try {
            boolean deleted = deleteDocument.delete();

            Log.e(TAG, "Status after delete of " + documentId + " was "
                    + deleted);
        } catch (CouchbaseLiteException e) {
            Log.e(TAG, "Error deleting", e);
        }
    }
}
