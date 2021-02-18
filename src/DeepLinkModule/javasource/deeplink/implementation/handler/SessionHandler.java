package deeplink.implementation.handler;

import com.mendix.core.Core;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.systemwideinterfaces.core.ISession;

public class SessionHandler {
	
	private static final String XAS_ID = "XASID";

	public static ISession GetFreshGuestSession(IMxRuntimeResponse response) throws Exception {
		
		ISession session = Core.initializeGuestSession();
		setCookies(response, session);
		
		return session;
	}

	private static void setCookies(IMxRuntimeResponse response, ISession session) {
		response.addCookie(Core.getConfiguration().getSessionIdCookieName(), session.getId().toString(),  "/", "", -1, true);
		response.addCookie(XAS_ID, "0."+Core.getXASId(),"/", "", -1, true);			 
	}
	
}
