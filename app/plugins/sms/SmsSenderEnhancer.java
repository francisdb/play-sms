package plugins.sms;

import java.rmi.UnexpectedException;

import javassist.CtClass;
import javassist.CtMethod;
import javassist.Modifier;
import play.Logger;
import play.classloading.ApplicationClasses.ApplicationClass;
import play.classloading.enhancers.Enhancer;

/**
 * Enhance smssender classes. 
 */
public class SmsSenderEnhancer extends Enhancer {

    private static final String SMS_SENDER_CLASS_NAME = "controllers.sms.SmsSender";

	@Override
    public void enhanceThisClass(ApplicationClass applicationClass) throws Exception {
//        if (isScala(applicationClass)) {
//            return;
//        }
    	
    	if(SMS_SENDER_CLASS_NAME.equals(applicationClass.name)){
    		return;
    	}

        CtClass ctClass = makeClass(applicationClass);

        if (!ctClass.subtypeOf(classPool.get(SMS_SENDER_CLASS_NAME))) {
            return;
        }
        
    	Logger.info("Enhancing " + applicationClass.name);

        for (final CtMethod ctMethod : ctClass.getDeclaredMethods()) {

            if (Modifier.isPublic(ctMethod.getModifiers()) && Modifier.isStatic(ctMethod.getModifiers()) && ctMethod.getReturnType().isPrimitive()) {
                try {
                    ctMethod.insertBefore("if(infos.get() != null) {play.Logger.warn(\"You call " + ctMethod.getLongName() + " from \" + ((java.util.Map)infos.get()).get(\"method\") + \". It's forbidden in a SmsSender. It will propably fail...\", new Object[0]);}; infos.set(new java.util.HashMap());((java.util.Map)infos.get()).put(\"method\", \"" + ctMethod.getLongName() + "\");");
                    ctMethod.insertAfter("infos.set(null);", true);
                } catch (Exception e) {
                    Logger.error(e, "Error in SmsSenderEnhancer");
                    throw new UnexpectedException("Error in SmsSenderEnhancer", e);
                }
            }

        }

        // Done.
        applicationClass.enhancedByteCode = ctClass.toBytecode();
        ctClass.defrost();

    }
}
