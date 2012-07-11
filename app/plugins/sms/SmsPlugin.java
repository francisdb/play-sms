package plugins.sms;

import play.Logger;
import play.PlayPlugin;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;
import play.exceptions.UnexpectedException;

public class SmsPlugin extends PlayPlugin {
	
	@Override
	public void enhance(ApplicationClass applicationClass) throws Exception {

		if ("plugins.sms.SmsSenderEnhancer".equals(applicationClass.name)) {
			return;
		}

		Class<?>[] enhancers = new Class[] { SmsSenderEnhancer.class };
		for (Class<?> enhancer : enhancers) {
			try {
				long start = System.currentTimeMillis();
				((Enhancer) enhancer.newInstance()).enhanceThisClass(applicationClass);
				if (Logger.isTraceEnabled()) {
					Logger.trace("%sms to apply %s to %s", System.currentTimeMillis() - start, enhancer.getSimpleName(), applicationClass.name);
				}
			} catch (Exception e) {
				throw new UnexpectedException("While applying " + enhancer + " on " + applicationClass.name, e);
			}
		}
	}
}
