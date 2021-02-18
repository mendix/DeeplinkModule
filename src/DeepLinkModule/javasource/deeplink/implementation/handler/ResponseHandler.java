package deeplink.implementation.handler;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.rmi.UnexpectedException;

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
		
		LOG.trace("Redirecting to index location: " + location );
		response.addHeader("location", location);
	}
	
	public static void serveSSOHandler(IMxRuntimeRequest request, IMxRuntimeResponse response) throws UnsupportedEncodingException, UnexpectedException {
		
		String locationSSOHandler = SSOHANDLER;
		
		String redirectLocation = null;
		
		if(locationSSOHandler != null) {
			
			redirectLocation = locationSSOHandler;
			
			if(locationSSOHandler.endsWith("=")) {
				
				String pathinfo = request.getHttpServletRequest().getPathInfo(); 
				String querystring = request.getHttpServletRequest().getQueryString();
				
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
	
		String redirectLocation = LOGINPAGE;
		String requestURL = request.getHttpServletRequest().getRequestURL().toString();
		
		requestURL = requestURL.substring(0, requestURL.indexOf(ensureStartingSlash(deeplink.proxies.constants.Constants.getRequestHandlerName())));
		
		if(Constants.getLoginLocation()!=null && Constants.getLoginLocation().length()>0)
			redirectLocation = Constants.getLoginLocation();
			
			if(redirectLocation.endsWith("=")) {
				if(SSOHANDLER!=null && SSOHANDLER.length()>0) {
					redirectLocation += URLEncoder.encode(requestURL +SSOHANDLER, java.nio.charset.StandardCharsets.UTF_8.toString()); 
				}
				
				redirectLocation += URLEncoder.encode(request.getHttpServletRequest().getRequestURL().toString(), java.nio.charset.StandardCharsets.UTF_8.toString());
			}
		
		if(!redirectLocation.startsWith("http")) {
			redirectLocation = ensureStartingSlash(redirectLocation);
		}
			
		response.setStatus(IMxRuntimeResponse.SEE_OTHER);
		response.addHeader("location", redirectLocation);

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
	
	
}
