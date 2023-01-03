package deeplink.implementation.handler;

import com.mendix.core.Core;
import com.mendix.m2ee.api.IMxRuntimeResponse;
import com.mendix.core.conf.RuntimeVersion;
import com.mendix.systemwideinterfaces.core.ISession;
import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

public class SessionHandler {
	
	private static final String XAS_ID = "XASID";

	public static ISession GetFreshGuestSession(IMxRuntimeResponse response) throws Exception {
		ISession session = Core.initializeGuestSession();
		setCookies(response, session);
		
		return session;
	}

	private static void setCookies(IMxRuntimeResponse response, ISession session) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
		String[] mxVersion = RuntimeVersion.getInstance().toString().split("\\.");
		if (Integer.parseInt(mxVersion[0]) >=9 && Integer.parseInt(mxVersion[1]) >= 20) {
			// use reflection to call the addCookie method with the 7th parameter for 'isHostOnly', which was added in 9.20
			@SuppressWarnings("rawtypes")
			Class[] methodSignature = {String.class, String.class, String.class, String.class, int.class, boolean.class, boolean.class};
			Method addCookie = response.getClass().getMethod("addCookie", methodSignature);
			addCookie.invoke(response, Core.getConfiguration().getSessionIdCookieName(), session.getId().toString(), "/", "", -1, true, true);
			addCookie.invoke(response, XAS_ID, "0." + Core.getXASId(),"/", "", -1, true, true);

		} else {
			response.addCookie(Core.getConfiguration().getSessionIdCookieName(), session.getId().toString(), "/", "", -1, true);
			response.addCookie(XAS_ID, "0." + Core.getXASId(),"/", "", -1, true);
		}
	}

}
