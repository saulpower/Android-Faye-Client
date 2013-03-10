/* The MIT License
 * 
 * Copyright (c) 2011 Paul Crawford
 * Copyright (c) 2013 Saul Howard
 *
 * Ported from Objective-C to Java by Saul Howard <saulpower1@gmail.com>
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * 
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package com.saulpower.fayeclient;

import java.net.URI;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.Handler;
import android.util.Log;

import com.saulpower.fayeclient.WebSocketClient.Listener;

public class FayeClient implements Listener {
	
    public final String TAG = this.getClass().getSimpleName();

	private final String HANDSHAKE_CHANNEL 		= "/meta/handshake";
	private final String CONNECT_CHANNEL 		= "/meta/connect";
	private final String DISCONNECT_CHANNEL 	= "/meta/disconnect";
	private final String SUBSCRIBE_CHANNEL 		= "/meta/subscribe";
	private final String UNSUBSCRIBE_CHANNEL 	= "/meta/unsubscribe";
	
	private final String KEY_CHANNEL			= "channel";
	private final String KEY_SUCCESS			= "successful";
	private final String KEY_CLIENT_ID			= "clientId";
	private final String KEY_VERSION			= "version";
	private final String KEY_MIN_VERSION		= "minimumVersion";
	private final String KEY_SUBSCRIPTION		= "subscription";
	private final String KEY_SUP_CONN_TYPES		= "supportedConnectionTypes";
	private final String KEY_CONN_TYPE			= "connectionType";
	private final String KEY_DATA				= "data";
	private final String KEY_ID					= "id";
	private final String KEY_EXT				= "ext";
	private final String KEY_ERROR				= "error";
	
	private final String VALUE_VERSION			= "1.0";
	private final String VALUE_MIN_VERSION		= "1.0beta";
	private final String VALUE_CONN_TYPE		= "websocket";
	
	private final long RECONNECT_WAIT			= 10000;
	private final int MAX_CONNECTION_ATTEMPTS   = 3;

    private WebSocketClient mClient;
    private boolean mConnected = false;
    private int mConnectionAttempts = 0;
    
    private URI mFayeUrl;
    private String mFayeClientId;
	private String mActiveSubChannel;
    
    private JSONObject mConnectionExtension;
    
    private boolean mRunning = false;
    private Handler mHandler;
    private Runnable mConnectionMonitor = new Runnable() {
		
		@Override
		public void run() {
			
			if (!mConnected) {
				
				openWebSocketConnection();
				
				if (mConnectionAttempts < MAX_CONNECTION_ATTEMPTS) {
					mConnectionAttempts++;
					getHandler().postDelayed(this, RECONNECT_WAIT);
				}
				
			} else {
				
				getHandler().removeCallbacks(this);
				mRunning = false;
				mConnectionAttempts = 0;
			}
		}
	};
	
	private FayeListener mFayeListener;

	/**
	 * Register a callback to be invoked for specific Faye client
	 * events
	 * 
	 * @param mFayeListener The callback that will run
	 */
	public void setFayeListener(FayeListener mFayeListener) {
		this.mFayeListener = mFayeListener;
	}

	private Handler getHandler() {
		
		return mHandler;
	}
    
	/**
	 * Creates a new Faye Client for communicating with a Faye server at the
	 * provided URL and the specified channel.
	 * 
	 * @param fayeUrl The URL of the FayeServer
	 * @param channel The channel to subscribe to
	 */
    public FayeClient(Handler handler, URI fayeUrl, String channel) {
    	
    	mHandler = handler;
    	mFayeUrl = fayeUrl;
    	mActiveSubChannel = channel;
    }
    
	/**
	 * Connect to a server using the extension authentication object
	 * 
	 * @param extension
	 *            Bayeux extension authentication that exchanges authentication
	 *            credentials and tokens within Bayeux messages ext fields
	 */
    public void connectToServer(JSONObject extension) {
    	mConnectionExtension = extension;
    	openWebSocketConnection();
    }
    
    public void disconnectFromServer() {
    	disconnect();
    }
    
	/**
	 * Sends events on a channel by sending an event message
	 * 
	 * @param json
	 *            JSON object containing message to be sent to server
	 */
    public void sendMessage(JSONObject json) {
    	publish(json, mConnectionExtension);
    }
    
    private void openWebSocketConnection() {
    	
    	if (mClient != null) {
    		mClient.disconnect();
    		mClient = null;
    	}
    	
    	mClient = new WebSocketClient(getHandler(), mFayeUrl, this, null);
    	mClient.connect();
    }
    
    private void closeWebSocketConnection() {
    	mClient.disconnect();
    }
    
    private void resetWebSocketConnection() {
    	
    	if (!mConnected) {
    		
    		if (!mRunning) {
    			getHandler().post(mConnectionMonitor);
    		}
    	}
    }
    
    /**
     * Initiates a connection negotiation by sending a message to the
     * "/meta/handshake" channel.
     * 
     * Example JSON
	 * {
	 * 		KEY_CHANNEL: "/meta/handshake",
     *  	KEY_VERSION: "1.0",
     *  	KEY_MIN_VERSION: "1.0beta",
     *  	KEY_SUP_CONN_TYPES:
     *  		["long-polling", "callback-polling", "iframe", "websocket]
     * }
     */
    private void handshake() {
    	
    	try {
    		
	    	JSONArray connTypes = new JSONArray();
	    	connTypes.put("long-polling");
	    	connTypes.put("callback-polling");
	    	connTypes.put("iframe");
	    	connTypes.put("websocket");
	    	
	    	JSONObject json = new JSONObject();
	    	json.put(KEY_CHANNEL, HANDSHAKE_CHANNEL);
	    	json.put(KEY_VERSION, VALUE_VERSION);
	    	json.put(KEY_MIN_VERSION, VALUE_MIN_VERSION);
	    	json.put(KEY_SUP_CONN_TYPES, connTypes);
	    	
	    	mClient.send(json.toString());
    	
    	} catch (JSONException ex) {
    		Log.e(TAG, "Handshake Failed", ex);
    	}
    }

	/**
	 * After a Bayeux client has discovered the server's capabilities
	 * with a handshake exchange, a connection is established by
	 * sending a message to the "/meta/connect" channel.
	 * 
	 * Example JSON
	 * {
	 * 		KEY_CHANNEL: "/meta/connect",
	 *  	KEY_CLIENT_ID: "Un1q31d3nt1f13r",
	 *  	KEY_CONN_TYPES: "long-polling"
	 * }
	 */
	public void connect() {

		try {
	    	
	    	JSONObject json = new JSONObject();
	    	json.put(KEY_CHANNEL, CONNECT_CHANNEL);
	    	json.put(KEY_CLIENT_ID, mFayeClientId);
	    	json.put(KEY_CONN_TYPE, VALUE_CONN_TYPE);
	    	
	    	mClient.send(json.toString());
    	
    	} catch (JSONException ex) {
    		Log.e(TAG, "Handshake Failed", ex);
    	}
	}
	
	/**
	 * Cease operation by sending a request to the "/meta/disconnect"
	 * channel for the server to remove any client-related state.
	 * 
	 * Example JSON
	 * {
	 * 		KEY_CHANNEL: "/meta/disconnect",
	 *  	KEY_CLIENT_ID: "Un1q31d3nt1f13r"
	 * }
	 */
	public void disconnect() {

		try {
	    	
	    	JSONObject json = new JSONObject();
	    	json.put(KEY_CHANNEL, DISCONNECT_CHANNEL);
	    	json.put(KEY_CLIENT_ID, mFayeClientId);
	    	
	    	mClient.send(json.toString());
    	
    	} catch (JSONException ex) {
    		Log.e(TAG, "Handshake Failed", ex);
    	}
	}
	
	
	/**
	 * Register interest in a channel and request that messages published to
	 * that channel are delivered.
	 * 
	 * Example JSON
	 * {
	 * 		KEY_CHANNEL: "/meta/subscribe",
	 *  	KEY_CLIENT_ID: "Un1q31d3nt1f13r",
	 *  	KEY_SUBSCRIPTION: "/foo/ **"
	 * }
	 */
	public void subscribe() {

		try {

	    	JSONObject json = new JSONObject();
	    	json.put(KEY_CHANNEL, SUBSCRIBE_CHANNEL);
	    	json.put(KEY_CLIENT_ID, mFayeClientId);
	    	json.put(KEY_SUBSCRIPTION, mActiveSubChannel);
	    	
			if (null != mConnectionExtension) {
				
		    	json.put(KEY_EXT, mConnectionExtension);
			}
	    	
	    	mClient.send(json.toString());
    	
    	} catch (JSONException ex) {
    		Log.e(TAG, "Handshake Failed", ex);
    	}
	}
	
	
	/**
	 * Send unsubscribe messages to cancel interest in channel and to request
	 * that messages published to that channel are not delivered.
	 * 
	 * Example JSON
	 * {
	 * 		KEY_CHANNEL: "/meta/unsubscribe",
	 *  	KEY_CLIENT_ID: "Un1q31d3nt1f13r",
	 *  	KEY_SUBSCRIPTION: "/foo/**"
	 * }
	 */
	public void unsubscribe() {

		try {
	    	
	    	JSONObject json = new JSONObject();
	    	json.put(KEY_CHANNEL, UNSUBSCRIBE_CHANNEL);
	    	json.put(KEY_CLIENT_ID, mFayeClientId);
	    	json.put(KEY_SUBSCRIPTION, mActiveSubChannel);
	    	
	    	mClient.send(json.toString());
    	
    	} catch (JSONException ex) {
    		Log.e(TAG, "Handshake Failed", ex);
    	}
	}

	/**
	 * Publish events on a channel by sending an event message
	 * 
	 * Example JSON
	 * {
	 * 		KEY_CHANNEL:	"/some/channel",
	 * 		KEY_CLIENT_ID:	"Un1q31d3nt1f13r",
	 * 		KEY_DATA:		"some application string or JSON encoded object",
	 * 		KEY_ID:		"some unique message id"
	 * }
	 * 
	 * @param message
	 *            JSON object containing message to be sent to server
	 * 
	 * @param extension
	 *            Bayeux extension authentication that exchanges authentication
	 *            credentials and tokens within Bayeux messages ext fields
	 */
	public void publish(JSONObject message, JSONObject extension) {
	    
	    String channel		= mActiveSubChannel;
	    long number			= (new Date()).getTime();
	    String messageId	= String.format("msg_%d_%d", number, 1);

	    try {
	    	
	    	JSONObject json = new JSONObject();
	    	json.put(KEY_CHANNEL, channel);
	    	json.put(KEY_CLIENT_ID, mFayeClientId);
	    	json.put(KEY_DATA, message);
	    	json.put(KEY_ID, messageId);
	    	
		    if (null != extension) {
		    	json.put(KEY_EXT, extension);
		    }
	    	
	    	mClient.send(json.toString());
    	
    	} catch (JSONException ex) {
    		Log.e(TAG, "Handshake Failed", ex);
    	}
	}

	/*
	 * (non-Javadoc)
	 * @see com.saulpower.fayeclient.WebSocketClient.Listener#onConnect()
	 */
	@Override
	public void onConnect() {
		
		mConnected = true;
		handshake();
	}

	/*
	 * (non-Javadoc)
	 * @see com.saulpower.fayeclient.WebSocketClient.Listener#onMessage(java.lang.String)
	 */
	@Override
	public void onMessage(String message) {
		
		parseFayeMessage(message);
	}

	/*
	 * (non-Javadoc)
	 * @see com.saulpower.fayeclient.WebSocketClient.Listener#onMessage(byte[])
	 */
	@Override
	public void onMessage(byte[] data) {
		Log.i(TAG, "Data message");
	}

	/*
	 * (non-Javadoc)
	 * @see com.saulpower.fayeclient.WebSocketClient.Listener#onDisconnect(int, java.lang.String)
	 */
	@Override
	public void onDisconnect(int code, String reason) {
        
		mConnected = false;
        
        if (mFayeListener != null) {
        	mFayeListener.disconnectedFromServer();
        }
	}

	/*
	 * (non-Javadoc)
	 * @see com.saulpower.fayeclient.WebSocketClient.Listener#onError(java.lang.Exception)
	 */
	@Override
	public void onError(Exception error) {
        
		mConnected = false;
		resetWebSocketConnection();
	}
	
	/**
	 * Parse the Faye message and call the appropriate
	 * listener method.
	 * 
	 * @param message A json string from the Faye server
	 */
	private void parseFayeMessage(String message) {
		
		try {

			JSONArray messageArray = new JSONArray(message);
			
			for (int i = 0; i < messageArray.length(); i++) {
				
				JSONObject fayeMessage = messageArray.optJSONObject(i);
				
				if (fayeMessage == null) continue;
				
				String channel = fayeMessage.optString(KEY_CHANNEL);
				boolean success = fayeMessage.optBoolean(KEY_SUCCESS);
				
				if (channel.equals(HANDSHAKE_CHANNEL)) {
					
					if (success) {
						
						mFayeClientId = fayeMessage.optString(KEY_CLIENT_ID);
						
						if (mFayeListener != null) {
							mFayeListener.connectedToServer();
						}
						
						connect();
						subscribe();
						
					} else if (BuildConfig.DEBUG) Log.d(TAG, "Error with Handshake");
					
					return;
				}
				
				if (channel.equals(CONNECT_CHANNEL)) {
					
					if (success) {

						mConnected = true;
						connect();
						
					} else if (BuildConfig.DEBUG) Log.d(TAG, "Error Connecting to Faye");
					
					return;
				}
				
				if (channel.equals(DISCONNECT_CHANNEL)) {
					
					if (success) {

						mConnected = false;
						closeWebSocketConnection();
						
						if (mFayeListener != null) {
							mFayeListener.disconnectedFromServer();
						}
						
					} else if (BuildConfig.DEBUG) Log.d(TAG, "Error Disconnecting to Faye");
					
					return;
				}
				
				if (channel.equals(SUBSCRIBE_CHANNEL)) {
					
					if (success) {

						if (mFayeListener != null) {
							mFayeListener.subscribedToChannel(fayeMessage.optString(KEY_SUBSCRIPTION));
						}
						
					} else if (BuildConfig.DEBUG) {
						
						Log.d(TAG, String.format("Error subscribing to %s with error %s", fayeMessage.optString(KEY_SUBSCRIPTION), fayeMessage.optString(KEY_ERROR)));
						
						if (mFayeListener != null) {
							mFayeListener.subscriptionFailedWithError(fayeMessage.optString(KEY_ERROR));
						}
					}
					
					return;
				}
				
				if (channel.equals(UNSUBSCRIBE_CHANNEL)) {
					
					if (success) {

						if (BuildConfig.DEBUG) Log.d(TAG, String.format("Unsubscribed from channel %s on Faye", fayeMessage.optString(KEY_SUBSCRIPTION)));
						
					} else if (BuildConfig.DEBUG) Log.d(TAG, "Error Connecting to Faye");
					
					return;
				}
				
				if (isSubscribedToChannel(channel)) {
					
					JSONObject data = null;
					
					if ((data = fayeMessage.optJSONObject(KEY_DATA)) != null && mFayeListener != null) {
						mFayeListener.messageReceived(data);
					}
					
					return;
				}
				
				if (BuildConfig.DEBUG) Log.d(TAG, String.format("No match for channel %s", channel));
			}
			
		} catch (JSONException ex) {
			Log.e(TAG, "Could not parse faye message", ex);
		}
	}
	
	/**
	 * Checks to see if we are subscribed to the passed in channel
	 * 
	 * @param channel
	 *            Name of channel to check
	 * @return True if we are connected to the passed in channel, false
	 *         otherwise
	 */
	private boolean isSubscribedToChannel(String channel) {
		
		boolean isSubscribed = false;
		
		if (mActiveSubChannel != null && mActiveSubChannel.length() > 0 && channel != null && channel.length() > 0) {
			
			String[] subscribedChannelSegments = mActiveSubChannel.split("/");
			String[] channelSegments = channel.split("/");
			
			int i = 0;
			isSubscribed = true;
			
			do {
				
				String s1 = subscribedChannelSegments[i];
				String s2 = (i < channelSegments.length ? channelSegments[i] : null);
				
				if (s2 == null) break;
				
				if (!s2.equals(s1)) {
					
					if (s1.equals("**")) {
						break;
					} else {
						isSubscribed = false;
					}
				}
				
				i++;
				
			} while (isSubscribed && i < subscribedChannelSegments.length);
		}
		
		return isSubscribed;
	}
	
	public interface FayeListener {
		public void connectedToServer();
		public void disconnectedFromServer();
		public void subscribedToChannel(String subscription);
		public void subscriptionFailedWithError(String error);
		public void messageReceived(JSONObject json);
	}
}
