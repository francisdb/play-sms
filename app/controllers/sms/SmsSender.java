package controllers.sms;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import play.Logger;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesNamesTracer;
import play.classloading.enhancers.LocalvariablesNamesEnhancer.LocalVariablesSupport;
import play.exceptions.UnexpectedException;
import play.templates.Template;
import play.templates.TemplateLoader;
import services.sms.SMS;
import services.sms.SMS.SMSMessage;

/**
 * Application sms support
 */
public class SmsSender implements LocalVariablesSupport {

	protected static ThreadLocal<HashMap<String, Object>> infos = new ThreadLocal<HashMap<String, Object>>();

	public static void setNumber(final String number) {
		HashMap<String, Object> map = getContext();
		map.put("number", number);
		infos.set(map);
	}
	
    @SuppressWarnings("unchecked")
	public static Future<Boolean> send(Object... args) {
		final HashMap<String, Object> map = getContext();

		// Body character set
		final String number = (String) map.get("number");

		String templateName = (String) map.get("method");
		if (templateName.startsWith("notifiers.")) {
			templateName = templateName.substring("notifiers.".length());
		}
		if (templateName.startsWith("controllers.")) {
			templateName = templateName.substring("controllers.".length());
		}
		templateName = templateName.substring(0, templateName.indexOf("("));
		templateName = templateName.replace(".", "/");

		// overrides Template name
		if (args.length > 0 && args[0] instanceof String && LocalVariablesNamesTracer.getAllLocalVariableNames(args[0]).isEmpty()) {
			templateName = args[0].toString();
		}

		final Map<String, Object> templateTextBinding = new HashMap<String, Object>();
		for (Object o : args) {
			List<String> names = LocalVariablesNamesTracer.getAllLocalVariableNames(o);
			for (String name : names) {
				Logger.info(name + " = " + o);
				templateTextBinding.put(name, o);
			}
		}

		Template templateText = TemplateLoader.load(templateName + ".txt");
		String smsText = templateText.render(templateTextBinding);

		return SMS.send(new SMSMessage(number, smsText));
	}

    public static boolean sendAndWait(Object... args) {
        try {
            Future<Boolean> result = send(args);
            return result.get();
        } catch (InterruptedException e) {
            Logger.error(e, "Error while waiting SmsSender.send result");
        } catch (ExecutionException e) {
            Logger.error(e, "Error while waiting SmsSender.send result");
        }
        return false;
    }
    
	private static HashMap<String, Object> getContext() {
		HashMap<String, Object> map = infos.get();
		if (map == null) {
			throw new UnexpectedException("SmsSender not instrumented ?");
		}
		return map;
	}

}
