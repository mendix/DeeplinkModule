package deeplink.utils;

import java.util.InputMismatchException;
import java.util.List;

import com.mendix.core.Core;
import com.mendix.logging.ILogNode;
import com.mendix.systemwideinterfaces.core.IContext;
import com.mendix.systemwideinterfaces.core.IMendixObject;


import deeplink.proxies.DeepLink;
import deeplink.proxies.microflows.Microflows;

public class DeeplinkUtils {
	private static ILogNode LOG = Core.getLogger(deeplink.implementation.Commons.logNodeName);

	public static void updateOrCreateDeeplink(IContext context, String deeplinkName, boolean forceLogin, String description, 
			String microflowName, boolean keepAsHomeDeeplink,String entity, String attribute) throws Exception {

		List<IMendixObject> list = Core.createXPathQuery(String.format("//%s[%s=$value]", 
				DeepLink.getType(),
				DeepLink.MemberNames.Name.toString()))
				.setVariable("value",deeplinkName)
				.execute(context);
		
		DeepLink deeplink = null;
		
		if((entity == null) != (attribute==null))
		{
			throw new IllegalArgumentException("The value of parameters 'entity' and 'attribute' should both be either null or not null.");
		}
		
		if(list.size()>=1) {
			deeplink = DeepLink.initialize(context, list.get(0));
			if(list.size()>1) {
				LOG.warn(String.format("Deeplink configuration is corrupt. Multiple deep links with name '%s' found.", deeplinkName));
			}
		}
		else if(list.size()==0) {
			deeplink = new DeepLink(context);
		}
		
		deeplink.proxies.Microflow microflowFromDeepLink = null;
		
		try {
			
			deeplink.setAllowGuests(!forceLogin);
			deeplink.setDescription(description);
			deeplink.setMicroflow(microflowName);
			deeplink.setName(deeplinkName);
			deeplink.setUseAsHome(keepAsHomeDeeplink);
			
			deeplink.setObjectType(entity);
			deeplink.setObjectAttribute(attribute);

			microflowFromDeepLink = Microflows.dS_MicroflowByDeeplink(context, deeplink);
			
			deeplink.setUseObjectArgument(microflowFromDeepLink.getUseObjectArgument());
			deeplink.setUseStringArgument(microflowFromDeepLink.getUseStringArg());
			if(microflowFromDeepLink.getUseStringArg()) {
				deeplink.setSeparateGetParameters(microflowFromDeepLink.getNrOfParameters()>0);
			}
			
			Core.commit(context, deeplink.getMendixObject());
		}
		catch(Exception e) {
			LOG.error(String.format("Deeplink '%s' could not be created",deeplinkName),e);
		}
	}
}
