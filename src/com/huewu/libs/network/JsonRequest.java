package com.huewu.libs.network;

import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map.Entry;

public abstract class JsonRequest<T> {

	public final static int PORT = 80;	
	public final static int RETRY_COUNT = 5;

	protected URL url = null;
	protected Method method = Method.GET;
	protected HashMap<String, String> headers = new HashMap<String, String>();

	protected ArrayList<T> response = new ArrayList<T>(5);
	protected Exception exception = null;
	protected boolean useSecure = false;
	protected int retryCount = 0;
	protected int maxRetryCount = RETRY_COUNT;
	protected int status = 0;

	private ResponseDecoder<T> mDecoder;
	private ResponseListener<?> mRespListener;
	private LinkedHashMap<String, String> mFormData = new LinkedHashMap<String, String>();
	private LinkedHashMap<String, Collection<?>> mFormCollectionData = new LinkedHashMap<String, Collection<?>>(); 

	private boolean mForceUseCache = false;

	public enum Method
	{
		GET, POST, PUT, DELETE
	}

	public JsonRequest( Method method, URL url ){
		this.method = method;
		this.url = url;
	}

	public JsonRequest(Method method, String urlStr) {
		this.method = method;
		try {
			this.url = new URL(urlStr);
		} catch (MalformedURLException e) {
			this.url = null;
		}
	}

	public void putFormData( String key, String value ){
		if( key != null && value != null)
			mFormData.put(key, value);
	}

	public void putFormDataArray( String key, Collection<?> values ){
		if( key != null && values != null)
			mFormCollectionData.put(key, values);
	}


	public URL getURL() {
		return url;
	}

	public Method getMethod() {
		return method;
	}

	public String getFormDataString() {
		//convert form data to byte array.
		if(mFormData.size() == 0)
			return "";

		StringBuilder builder = new StringBuilder();

		//handle key value pair.
		for( Entry<String, String> pair : mFormData.entrySet() ){
			if(pair.getKey() == null || pair.getValue() == null)
				continue;

			if(builder.length() > 0)
				builder.append("&");

			builder.append(pair.getKey());
			builder.append("=");
			try {
				builder.append(URLEncoder.encode(pair.getValue(), "utf-8"));
			} catch (UnsupportedEncodingException e) {
				builder.append(pair.getValue());
			}
		}

		//handle array values.
		for( Entry<String, Collection<?>> pair : mFormCollectionData.entrySet() ){
			if(pair.getKey() == null || pair.getValue() == null)
				continue;

			Collection<?> c = pair.getValue();
			for( Object obj : c ){
				if(builder.length() > 0)
					builder.append("&");

				try {
					builder.append(pair.getKey() + "[]");
					builder.append("=");
					builder.append(URLEncoder.encode(obj.toString(), "utf-8"));
				} catch (UnsupportedEncodingException e) {
					builder.append(pair.getValue());
				}
			}
		}



		return builder.toString();		
	}

	public byte[] getFormData() {
		String data = getFormDataString();
		//convert form data to byte array.
		if(data == null || data.length()== 0)
			return null;

		return data.getBytes();
	}

	public void setDecoder( ResponseDecoder<T> decoder ){
		mDecoder = decoder;
	}

	public void setResponseListener( ResponseListener<?> listener ){
		mRespListener = listener;
	}

	public ResponseDecoder<T> getDecoder(){
		return mDecoder;
	}

	public ResponseListener<?> getResponseListener(){
		return mRespListener;
	}

	/**
	 * return read-only unmodifiable map.
	 * 
	 * @return
	 */
	public HashMap<String, String> getHeaders() {
		return (HashMap<String, String>) Collections.unmodifiableMap(headers);
	}

	public boolean useForceCache() {
		return mForceUseCache;
	}

	public void setForceCache(boolean flag) {
		mForceUseCache = flag;
	}

	public int getResponseCode(){
		return status;
	}

	public void setResponseCode( int code ){
		status = code;
	}

	@SuppressWarnings("unchecked")
	public void addResponse( Object obj ){
		response.add((T) obj);
	}

	public ArrayList<T> getResponse() {
		return response;
	}

	public boolean isCanceled() {
		return false;
	}

	public int getTimeout() {
		return 5000;	//default timeout value is 5sec.
	}
}// end of class
