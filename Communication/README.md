# Communication Library

A library used to create and send HTTP requests to a remote server and return the string response

## Usage

```java
    HashMap<String, String> headers = new HashMap<String, String>();
    headers.put("Content-Type", "application/json");
    headers.put("Accept", "application/json");

    String url = String.format("%s://%s/%s", protocol, baseUrl, endpoint);
    String response = "";

    try {

        response = HttpRequest.sendGet(url, headers, null);

        JSONObject json = new JSONObject(response);

        return json;

    } catch (Exception e) {

        Log.e(TAG, "Error downloading sync", e);
        Log.e(TAG, "Server Response: " + response);

        return null;
    }
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