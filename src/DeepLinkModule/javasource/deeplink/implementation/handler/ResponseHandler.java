package deeplink.implementation.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.UnexpectedException;

import javax.servlet.http.HttpServletRequest;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;

import deeplink.proxies.constants.Constants;

public class ResponseHandler {
	
	private static final ILogNode LOG = Core.getLogger(deeplink.implementation.Commons.logNodeName);
	private static final String SSOHANDLER = ensureStartingSlash(Constants.getSSOHandlerLocation());
	private static final String INDEXPAGE = "index.html";
	private static final String LOGINPAGE = "login.html";
	
	public static void serve404(IMxRuntimeResponse response) throws IOException {

		response.setStatus(IMxRuntimeResponse.NOT_FOUND);
		
	}
	
	public static void serveIndex(IMxRuntimeResponse response, String indexpage) {
		
		response.setStatus(IMxRuntimeResponse.SEE_OTHER);
		
		String location = null;
		if(indexpage!= null && indexpage.length()>0) {
			location = indexpage;
		}
		else if (deeplink.proxies.constants.Constants.getIndexPage()!=null && 
				deeplink.proxies.constants.Constants.getIndexPage().length()>0) {
			location = deeplink.proxies.constants.Constants.getIndexPage();
		}
		else {
			location = INDEXPAGE;
		}
		
		if(!location.startsWith("/")) {
			location = "/" + location;
		}
		
		if (LOG.isTraceEnabled()) {
			LOG.trace("Redirecting to index location: " + location );
		}
		response.addHeader("location", location);
	}
	
	public static void serveSSOHandler(IMxRuntimeRequest request, IMxRuntimeResponse response) throws UnsupportedEncodingException, UnexpectedException {
		
		String locationSSOHandler = SSOHANDLER;
		
		String redirectLocation = null;
		
		if(locationSSOHandler != null) {
			
			redirectLocation = locationSSOHandler;
			
			if(locationSSOHandler.endsWith("=")) {
				
				String pathinfo = ensureStartingSlash(request.getHttpServletRequest().getPathInfo()); 
				String querystring = request.getHttpServletRequest().getQueryString();
				
				if(!deeplink.proxies.constants.Constants.getEnableLeadingSlash()) {
					pathinfo = ensureNoStartingSlash(pathinfo);
				}
				
				if(SSOHANDLER!=null && SSOHANDLER.length()>0) {
					if(querystring!=null && querystring.length() > 0) { 
						querystring = "?" + querystring;
						querystring += "&sso_callback=true";
					}
					else {
						querystring = "?sso_callback=true";
					}
				}
				
				redirectLocation += URLEncoder.encode(pathinfo+querystring,
						java.nio.charset.StandardCharsets.UTF_8.toString());
			}
			
			response.setStatus(IMxRuntimeResponse.SEE_OTHER);
			response.addHeader("location", redirectLocation);
		}
		else {
			LOG.error("SSOHandler location constant is not configured");
			response.setStatus(IMxRuntimeResponse.INTERNAL_SERVER_ERROR);
		}
	}
	
	public static void serveLogin(IMxRuntimeRequest request,IMxRuntimeResponse response) throws UnsupportedEncodingException {
	
		String loginLocation = LOGINPAGE;
		HttpServletRequest servletRequest = request.getHttpServletRequest();
		
		String requestURL = servletRequest.getRequestURL().toString();

		String scheme = servletRequest.getScheme();
		String serverName = servletRequest.getServerName();
		int port = servletRequest.getServerPort();
		
		String requestPathInfo= request.getHttpServletRequest().getPathInfo();
		StringBuilder host = new StringBuilder(scheme)
				.append("://")
				.append(serverName)
				.append(":")
				.append(port);
		
		
		String queryString = request.getHttpServletRequest().getQueryString();
		
		String continuationURL = "";
		
		if(Constants.getLoginLocation()!=null && Constants.getLoginLocation().length()>0)
			loginLocation = Constants.getLoginLocation();

			if(loginLocation.endsWith("=")) {
				
				if(loginLocation.startsWith("http") && !requestURL.startsWith(loginLocation)) {
					
					continuationURL += host.toString(); 
					
					//external location, make sure continuation routes via SSOHandler.
					if(SSOHANDLER!=null && SSOHANDLER.length()>0 && !loginLocation.startsWith(SSOHANDLER)) {
						continuationURL += SSOHANDLER;
					}
					
				}
				if(requestPathInfo!=null) {
					
					if(!deeplink.proxies.constants.Constants.getEnableLeadingSlash()) {
						continuationURL += ensureNoStartingSlash(requestPathInfo);
					}
					else {
						continuationURL += requestPathInfo;
					}
				}
				if(queryString != null) {
					continuationURL += "?" + queryString;
				}
				
				if(continuationURL != null && continuationURL.length()>0 ) {
					loginLocation += URLEncoder.encode(continuationURL, java.nio.charset.StandardCharsets.UTF_8.toString());
				}
			}
		
		if(!loginLocation.startsWith("http")) {
			loginLocation = ensureStartingSlash(loginLocation);
		}
			
		response.setStatus(IMxRuntimeResponse.SEE_OTHER);
		response.addHeader("location", loginLocation);

	}
	
	private static String ensureStartingSlash(String s) {
		
		if(s == null) {
			return null;
		}
		
		if(!s.startsWith("/")) {
			s = "/" + s;
		}
		return s;
	}
	
	private static String ensureNoStartingSlash(String s) {
		
		if(s == null) {
			return null;
		}
		
		if(s.startsWith("/")) {
			s = s.substring(1);
		}
		return s;
	}
	
	
}
