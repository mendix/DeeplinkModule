package deeplink.implementation.handler;

import java.util.List;

import com.mendix.core.Core;
import com.mendix.core.CoreException;
import com.mendix.externalinterface.connector.RequestHandler;
import com.mendix.logging.ILogNode;
import com.mendix.m2ee.api.IMxRuntimeRequest;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;
import com.mendix.systemwideinterfaces.core.ISession;

import deeplink.proxies.DeepLink;
import deeplink.proxies.PendingLink;
import deeplink.implementation.handler.helpers.DeeplinkRequest;

public class DeeplinkHandler extends RequestHandler {

	private static final ILogNode LOG = Core.getLogger(deeplink.implementation.Commons.logNodeName);
	private static final String SSOHandler = deeplink.proxies.constants.Constants.getSSOHandlerLocation().length()==0 ? null : deeplink.proxies.constants.Constants.getSSOHandlerLocation();

	@Override
	protected void processRequest(IMxRuntimeRequest request, IMxRuntimeResponse response, String path) throws Exception {
		
		final DeeplinkRequest deepLinkRequest = new DeeplinkRequest(request);

		ISession session = null;
		IContext sessionContext = null;
		IContext systemContext = Core.createSystemContext();
		
		ISession sessionFromRequest = this.getSessionFromRequest(request);

		if(sessionFromRequest==null && !Core.getConfiguration().getEnableGuestLogin())
		{
			//Directly serve login page because no request session and no anonymous users allowed
			ResponseHandler.serveLogin(request,response);
		}
		else {

			if(sessionFromRequest ==null) {
				session = SessionHandler.GetFreshGuestSession(response);				
			}
			else {
				session = sessionFromRequest;
			}

			sessionContext = session.createContext();
			
			if(deepLinkRequest.getDeeplinkName().length()==0) {
				ResponseHandler.serve404(response);
			}
			else {
			
				DeepLink deepLinkConfigurationObject = getDeepLinkConfigurationObject(systemContext, deepLinkRequest.getDeeplinkName());
				
				if(deepLinkConfigurationObject != null) {
					
					if(LOG.isTraceEnabled()) {
						LOG.trace(String.format("Handling deeplink with existing session(%s)", session.getId()));
					}
					
					/* anonymous users are not immediately forwarded to index when
						- deeplink does not allow anonymous users
						- deeplink configuration has a SSO handler configured
					*/
					if(session.getUser(sessionContext).isAnonymous() && 
							((DeeplinkHandler.SSOHandler != null && request.getParameter("sso_callback")== null ) || !deepLinkConfigurationObject.getAllowGuests())) {
						
						if(!deepLinkConfigurationObject.getAllowGuests()) {
							ResponseHandler.serveLogin(request,response);
						}
						else if(DeeplinkHandler.SSOHandler != null) {
							ResponseHandler.serveSSOHandler(request, response);
						} 
					}
					else {
		
						PendingLink preparedPendingLink = preparePendingLink(sessionContext, session, deepLinkConfigurationObject, deepLinkRequest);
		
						if(preparedPendingLink == null) {
							ResponseHandler.serve404(response);
						}
						else {
							ResponseHandler.serveIndex(response, deepLinkConfigurationObject.getIndexPage());
						}
					}
				}
				else {
					ResponseHandler.serve404(response);
				}
			}
		}
	}
	
	private PendingLink preparePendingLink(IContext context, ISession session, DeepLink deepLinkConfigurationObject, DeeplinkRequest deepLinkRequest) {
		
		clearExistingPendingLinks(deepLinkConfigurationObject.getMendixObject(), session.getUserName());
		
		PendingLink newPendingLink = new PendingLink(context);
		
		newPendingLink.setPendingLink_DeepLink(deepLinkConfigurationObject);
		newPendingLink.setUser(session.getUserName());
		
		if (deepLinkConfigurationObject.getUseStringArgument()) {
			newPendingLink.setStringArgument(deepLinkRequest.getPath());
		}
		
		if(deepLinkConfigurationObject.getObjectType() != null && deepLinkConfigurationObject.getObjectType().length() > 0 ) {
			
			IMendixObject objectFromRequestParameters = getObjectByRequestParameters(context, deepLinkConfigurationObject, deepLinkRequest);
			
			if(objectFromRequestParameters != null) {
				
				newPendingLink.setArgument(objectFromRequestParameters.getId().toLong());
				newPendingLink.setSessionId(session.getId().toString());
				
			}
			else {
				if(LOG.isTraceEnabled()) {
					LOG.trace(String.format("The deeplink '%s' accepts the object '%s' as an argument, "
							+ "but an object with value '%s' for attribute '%s' wasn't found "
							+ "in the database",
							deepLinkConfigurationObject.getName(),
							deepLinkConfigurationObject.getObjectType(),
							deepLinkRequest.getPath(),
							deepLinkConfigurationObject.getObjectAttribute()
							));
				}
				return null;
			}
		}
		
		try {
			if(LOG.isTraceEnabled()) {
				LOG.trace(String.format("Created new pending link for session(%s) and user(%s)", session.getId(), session.getUserName()));
			}
			Core.commit(context, newPendingLink.getMendixObject());
		} catch (CoreException e) {
			LOG.error(e);
			return null;
		}
		
		return newPendingLink;
		
	}
	
	protected IMendixObject getObjectByRequestParameters(IContext context, DeepLink deepLinkConfigurationObject, DeeplinkRequest deeplinkRequest) {
		List<IMendixObject> parameterObjectList = Core
				.createXPathQuery(String.format("//%s[%s=$value]", 
						deepLinkConfigurationObject.getObjectType(),
						deepLinkConfigurationObject.getObjectAttribute()))
				.setVariable("value", deeplinkRequest.getPathArgument())
				.execute(context);
		
		if(parameterObjectList.size() == 1) {
			return parameterObjectList.get(0);
		}
		else {
			return null;
		}
		
	}

	private void clearExistingPendingLinks(IMendixObject mxObject, String username) {
		
		IContext systemContext = Core.createSystemContext();
		
		List<IMendixObject> pendinglinks = Core
				.createXPathQuery(String.format("//%s[%s=$mxId and %s=$username]", 
						PendingLink.getType(),
						PendingLink.MemberNames.PendingLink_DeepLink.toString(),
						PendingLink.MemberNames.User.toString()))
				.setVariable("mxId", mxObject.getId())
				.setVariable("username", username)
				.execute(systemContext);
		
		Core.delete(systemContext, pendinglinks);
	}

	protected DeepLink getDeepLinkConfigurationObject(IContext context, String deeplinkName) {
		
		List<IMendixObject> mendixObjList = Core.createXPathQuery(String.format("//%s[%s=$value]",
					DeepLink.getType(),
					DeepLink.MemberNames.Name.toString()))
				.setVariable("value", deeplinkName)
				.execute(context);
		
		if (mendixObjList.size() != 1) {
			if(LOG.isDebugEnabled()) {
				LOG.debug(String.format("Input parameter '%s' is configured %d %s in the deeplink configuration.",
						deeplinkName,
						mendixObjList.size(),
						mendixObjList.size() == 0 ? "time": "times"));
			}

			return null;
		}
		else {
			return DeepLink.initialize(context, mendixObjList.get(0));
		}
	}
}
