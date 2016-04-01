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

    //----------------------------------------------------------------------------------------------
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Connect to wear devices
        mGoogleApiClient = new GoogleApiClient.Builder(this).addApi(Wearable.API).build();
        mGoogleApiClient.connect();

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
                            final PendingResult<NodeApi.GetConnectedNodesResult> nodes = Wearable.NodeApi.getConnectedNodes(mGoogleApiClient);
                            nodes.setResultCallback(new ResultCallback<NodeApi.GetConnectedNodesResult>() {

                                @Override
                                public void onResult(NodeApi.GetConnectedNodesResult result) {
                                    String path = "/message_path";
                                    String message = "Are you receiving?";
                                    final List<Node> nodes = result.getNodes();

                                    // Send to all connected devices, or log no nodes
                                    if (nodes != null) {
                                        Log.d(TAG, "Nodes found!");
                                        for (int i=0; i<nodes.size(); i++) {
                                            final Node node = nodes.get(i);

                                            Wearable.MessageApi.sendMessage(mGoogleApiClient, node.getId(), path, message.getBytes());
                                        }
                                    } else {
                                        Log.d(TAG, "no nodes found");
                                    }
                                }
                            });
                        }
                    }
                });
            }
        });
    }
}
