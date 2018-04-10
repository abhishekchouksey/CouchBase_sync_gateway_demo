package com.asw.couchbasegames;

import android.app.Activity;
import android.content.Context;

import com.couchbase.lite.Database;
import com.couchbase.lite.Document;
import com.couchbase.lite.Manager;
import com.couchbase.lite.ReplicationFilter;
import com.couchbase.lite.SavedRevision;
import com.couchbase.lite.android.AndroidContext;
import com.couchbase.lite.auth.Authenticator;
import com.couchbase.lite.auth.AuthenticatorFactory;
import com.couchbase.lite.auth.OIDCLoginCallback;
import com.couchbase.lite.auth.OIDCLoginContinuation;
import com.couchbase.lite.auth.TokenStore;
import com.couchbase.lite.auth.TokenStoreFactory;
import com.couchbase.lite.replicator.Replication;
import com.couchbase.lite.util.Log;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Abhishek.Chouksey on 2/1/2018.
 */

public class ReplicationHandler {
    public static final String DB_NAME = "db";
    public static final String TAG = "db";
    public Activity activity;

    public Manager manager = null;
    public Database database = null;

    private static ReplicationHandler replicationHandler = null;

    /**
     * The URL for the Sync Gateway.<br>
     * Note: 10.0.2.2 == Android Simulator equivalent of 127.0.0.1
     */

    public static final String GATEWAY_SYNC_URL = "http://10.0.2.2:4984/"
            + DB_NAME;

    URL syncGatewayURI;

    public static ReplicationHandler createReplicationHandler(Context wrappedContext, Activity activity)
    {
        if (replicationHandler == null)
            replicationHandler = new ReplicationHandler (wrappedContext, activity);

        return replicationHandler;
    }

    private ReplicationHandler(Context wrappedContext, Activity activity)
    {
        this.activity = activity;

        try {
            manager = new Manager(
                    new AndroidContext(wrappedContext),
                    Manager.DEFAULT_OPTIONS);
            database = manager.getDatabase(DB_NAME);

            syncGatewayURI = new URL(GATEWAY_SYNC_URL);
        }catch (Exception e) {
            Log.e(TAG, "Error getting database", e);
            return;
        }
    }


    /**
     * Creates a pull replication from the Sync Gateway
     *
     * @param database The database object to use
     */
    public void createPullReplication(Database database) {
        Replication pull = database.createPullReplication(syncGatewayURI);


      //  pull.setCookie("cookie", "2c65f0c448b0e5a0c78067a5400b7893d429344f", "/", 10, false, true);
        //pull.setAuthenticator(authenticator);
        Map<String, String> session = new HashMap<String, String>();
        session.put("session_id", "2c65f0c448b0e5a0c78067a5400b7893d429344f");
        session.put("expires", "2018-03-08T14:44:10.3654461-05:00");
        session.put("cookie_name", "SyncGatewaySession");


        pull.setCookie(session.get("cookie_name"), session.get("session_id"), "/", 10, false, false);

        List<String> channels = new ArrayList<String>();

        channels.add("superEntity-1");
        channels.add("superEntity-2");

        pull.setChannels(channels);




        /**/

      /* Authenticator authenticator = AuthenticatorFactory.createOpenIDConnectAuthenticator(new OIDCLoginCallback() {
            @Override
            public void callback(URL loginURL, URL redirectURL, OIDCLoginContinuation loginContinuation) {

                //loginURL  = loginURL.toString().replace("localhost:4984/", "10.0.2.2:4984/"));

                final CouchbaseOAuthWebViewDialog dialog = CouchbaseOAuthWebViewDialog.newInstance(loginURL, redirectURL);
                dialog.setReceiver(new OAuthReceiver() {

                    @Override
                    public void receiveLoginAttempted(String url) {
                        Log.d("app", String.format("login redirect url %s original redirect url %s", url, redirectURL));
                        URL redirectUrl = null;

                        try {
                            redirectUrl = new URL(url);
                        } catch (MalformedURLException e) {
                            loginContinuation.callback(null, e);
                            return;
                        }
                        dialog.removeReceive();
                        dialog.dismiss();
                        Log.d("app", String.format("loginContinuation redirectUrl=%s", redirectUrl));
                        loginContinuation.callback(redirectUrl, null);
                    }

                    @Override
                    public void receiveOAuthCode(String code) {
                        dialog.removeReceive();
                        dialog.dismiss();
                        Log.d("app", String.format("unexpected receiveOAuthError %s", code));
                    }

                    @Override
                    public void receiveOAuthError(String error) {
                        dialog.removeReceive();
                        dialog.dismiss();
                        Log.d("app", String.format("unexpected receiveOAuthCode %s", error));
                    }
                });
                dialog.setStyle(android.R.style.Theme_Light_NoTitleBar, 0);
                dialog.show(activity.getFragmentManager(), "TAG");
                activity.setContentView(R.layout.couchbase_oauth_web_view);
            }

        }, new TokenStore() {
            @Override
            public Map<String, String> loadTokens(URL remoteURL, String localUUID) throws Exception {
                return null;
            }

            @Override
            public boolean saveTokens(URL remoteURL, String localUUID, Map<String, String> tokens) {
                return false;
            }

            @Override
            public boolean deleteTokens(URL remoteURL, String localUUID) {
                return false;
            }
        });


       // Authenticator authenticator = AuthenticatorFactory.createBasicAuthenticator("abhishek2","");

        pull.setAuthenticator(authenticator);*/

        pull.addChangeListener(new Replication.ChangeListener() {

            @Override
            public void changed(Replication.ChangeEvent changeEvent) {
                Replication pull = changeEvent.getSource();

                // The number of changes made so far
                int completedChangesCount = pull.getCompletedChangesCount();

                // The number of changes still to be processed
                int changesCount = pull.getChangesCount();

        Log.i("couchbaseevents", "Pull replication: " + completedChangesCount +
                " " + changesCount);
    }
});

        // Start the replication that will run once
        pull.start();

    }

    /**
     * Creates a push replication to the Sync Gateway
     *
     * @param database The database object to use
     */
    public void createPushReplication(Database database) {
        Replication push = database.createPushReplication(syncGatewayURI);

        push.addChangeListener(new Replication.ChangeListener() {

            @Override
            public void changed(Replication.ChangeEvent changeEvent) {
                Replication push = changeEvent.getSource();

                // The number of changes made so far
                int completedChangesCount = push.getCompletedChangesCount();

                // The number of changes still to be processed
                int changesCount = push.getChangesCount();

                Log.i("couchbaseevents", "Push replication: " + completedChangesCount +
                        " " + changesCount);
            }
        });

        // Start the replication that will run once
        push.start();
    }
}
