package deeplink.implementation.handler.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mendix.m2ee.api.IMxRuntimeRequest;

import deeplink.proxies.constants.Constants;

public class DeeplinkRequest {
	
	private String _deeplinkName = null;

	private String _path = null;
	private String _pathArgument = null;
	private String _queryString = null;
	
	public DeeplinkRequest(IMxRuntimeRequest request) {
		
		String path = request.getResourcePath().replaceFirst("/" + Constants.getRequestHandlerName() +"/", "");
		String querystring = request.getHttpServletRequest().getQueryString();
		
		if(querystring != null && querystring.contains("sso_callback=true")) {
			querystring = querystring.replaceAll("&sso_callback=true", "");
			querystring = querystring.replaceAll("sso_callback=true", "");
		}
		
		List<String> splitted_path = new ArrayList<String>(Arrays.asList(path.split("/")));		
		
		this._deeplinkName = splitted_path.get(0);
		splitted_path.remove(0);
		
		if(splitted_path.size()>=1) {
			this._pathArgument = splitted_path.get(0);
		}

		this._path = String.join("/", splitted_path);  
		
		if(querystring!=null && querystring.length()>0) {
			this._path += "?" + querystring;
		}
		
		this._queryString = querystring != null ? querystring : "";
		
	}
	
	public String getDeeplinkName() {
		return this._deeplinkName;
	}
	
	public String getQueryString() {
		return this._queryString;
	}
	
	public String getPath() {
		return this._path;
	}
	
	public String getPathArgument() {
		return this._pathArgument;
	}
	
}