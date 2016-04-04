package com.vradmin.wearcontroller;

import android.app.Activity;
import android.os.Bundle;
import android.support.wearable.view.WatchViewStub;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;

import java.util.List;

public class MainActivity extends Activity {

    public static final String TAG = MainActivity.class.getSimpleName();

    private Button mButton;
    private TextView mTextView;

    private GoogleApiClient mGoogleApiClient;
    private String mPath = "ListenerService";
    private String mMessage = "Are you receiving?";

    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Build client for the wearable API
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Wearable.API)
                .build();

        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mButton = (Button) stub.findViewById(R.id.button);

                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Check connection then send message
                        if (mGoogleApiClient == null) {
                            mTextView.setText("Clicked, but no connection");
                        } else {
                            mTextView.setText("Clicked, and connected!");
                            new Thread(new SendMessageThread(mPath, mMessage)).start();
                        }
                    }
                });
            }
        });
    }

    //----------------------------------------------------------------------------------------------
    @Override
    protected void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    //----------------------------------------------------------------------------------------------
    @Override
    protected void onStop() {
        if (mGoogleApiClient != null && mGoogleApiClient.isConnected())
            mGoogleApiClient.disconnect();
        super.onStop();
    }

    //----------------------------------------------------------------------------------------------
    /** Sending message could block main UI thread, so executes on a new thread. */
    class SendMessageThread implements Runnable {

        private String path;
        private String message;

        public SendMessageThread(String p, String msg) {
            path = p;
            message = msg;
        }

        @Override
        public void run() {
            final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {
                @Override
                public void onResult(NodeApi.GetConnectedNodesResult nodeResult) {
                    final List<Node> nodes = nodeResult.getNodes();

                    // Send to all connected devices, or log no nodes
                    if (nodes != null) {
                        Log.v(TAG, "Nodes found!");
                        for (int i = 0; i < nodes.size(); i++) {
                            final Node node = nodes.get(i);
                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes());
                        }
                    } else {
                        Log.e(TAG, "no nodes found");
                    }
                }
            });
        }
    }
}
