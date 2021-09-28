package deeplink.implementation.handler;

import java.util.HashMap;

import com.mendix.core.Core;
import com.mendix.systemwideinterfaces.core.IContext;

public class DeeplinkExecutionHandler {
	
	public static void execute(IContext context, String mf, HashMap<String,Object> parameterMap) {

		if(parameterMap.size() > 0) {
			Core.microflowCall(mf).withParams(parameterMap).execute(context);
	    } else { //no argument
			Core.microflowCall(mf).execute(context);
		}
		return;
	}
}
