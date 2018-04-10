package com.asw.couchbasegames;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class ReplicationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_replication);

        ReplicationHandler replicationHandler = ReplicationHandler.createReplicationHandler(getApplicationContext(), this);

        replicationHandler.createPullReplication(replicationHandler.database);

        replicationHandler.createPushReplication(replicationHandler.database);
    }
}
