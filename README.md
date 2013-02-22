# Faye Client for Android

A simple Faye client for Android.

## Credits

### Faye
Faye is a simple JSON based Pub-Sub server which has support for node.js and Ruby (using Rack).

Check out the Faye project [here](http://faye.jcoglan.com)

### Android Websockets
The hybi parser is based on code from the [faye project](https://github.com/faye/faye-websocket-node).

Websockets ported from JavaScript to Java by [Eric Butler](https://twitter.com/codebutler) <eric@codebutler.com>.

### FayeObjC
Faye client ported from Paul Crawford's [FayeObjC](https://github.com/pcrawfor/FayeObjC) to Java by [Saul Howard](saulpower1@gmail.com).

## Usage

Here is a some sample code to create the client as a service in Android:

```java
	public class WebSocketService extends IntentService implements FayeListener {
	
		public final String TAG = this.getClass().getSimpleName();
		
		FayeClient mClient;
		
		public WebSocketService() {
			super("WebSocketService");
		}
	
		@Override
		protected void onHandleIntent(Intent intent) {
	
			// SSL bug in pre-Gingerbread devices makes websockets currently unusable
			if (android.os.Build.VERSION.SDK_INT <= 8) return;
			
			Log.i(TAG, "Starting Web Socket");
			
			try {
				
				String baseUrl = Preferences.getString(Preferences.KEY_FAYE_HOST, DebugActivity.PROD_FAYE_HOST);
				
				URI uri = URI.create(String.format("wss://%s:443/events", baseUrl));
				String channel = String.format("/%s/**", User.getCurrentUser().getUserId());
				
				JSONObject ext = new JSONObject();
				ext.put("authToken", User.getCurrentUser().getAuthorizationToken());
				
				mClient = new FayeClient(uri, channel);
				mClient.setFayeListener(this);
				mClient.connectToServer(ext);
				
			} catch (JSONException ex) {}
		}
	
		@Override
		public void onCreate() {
			super.onCreate();
		}
	
		@Override
		public void onStart(Intent intent, int startId) {
			super.onStart(intent, startId);
		}
	
		@Override
		public int onStartCommand(Intent intent, int flags, int startId) {
			return super.onStartCommand(intent, flags, startId);
		}
	
		@Override
		public void connectedToServer() {
			Log.i(TAG, "Connected to Server");
		}
	
		@Override
		public void disconnectedFromServer() {
			Log.i(TAG, "Disonnected to Server");
		}
	
		@Override
		public void subscribedToChannel(String subscription) {
			Log.i(TAG, String.format("Subscribed to channel %s on Faye", subscription));
		}
	
		@Override
		public void subscriptionFailedWithError(String error) {
			Log.i(TAG, String.format("Subscription failed with error: %s", error));
		}
	
		@Override
		public void messageReceived(JSONObject json) {
			Log.i(TAG, String.format("Received message %s", json.toString()));
		}
	}
```

Add the following service to your AndroidManifest.xml file:

```xml
	<service android:name=".shared.Services.WebSocketService" />
```

Finally, to start the service simply call:

```java
	Intent intent = new Intent(this, WebSocketService.class);
	startService(intent);
```

## License

(The MIT License)
	
	Copyright (c) 2009-2012 James Coglan
	Copyright (c) 2011 Paul Crawford 
	Copyright (c) 2012 Eric Butler
	Copyright (c) 2013 Saul Howard
	
	Permission is hereby granted, free of charge, to any person obtaining a copy of
	this software and associated documentation files (the 'Software'), to deal in
	the Software without restriction, including without limitation the rights to use,
	copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the
	Software, and to permit persons to whom the Software is furnished to do so,
	subject to the following conditions:
	
	The above copyright notice and this permission notice shall be included in all
	copies or substantial portions of the Software.
	
	THE SOFTWARE IS PROVIDED 'AS IS', WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
	IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
	FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
	COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
	IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
	CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
	 
