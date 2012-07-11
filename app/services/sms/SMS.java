package services.sms;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import play.Logger;
import play.Play;
import play.exceptions.MailException;

public class SMS {
	
	static ExecutorService executor = Executors.newCachedThreadPool();
	public static boolean asynchronousSend = true;
	
	/**
     * Send an email
     */
	public static Future<Boolean> send(final SMSMessage sms) {
		if (Play.configuration.getProperty("mycoolsms.mock", "false").equals("true") && Play.mode == Play.Mode.DEV) {
			Mock.send(sms);
			return new Future<Boolean>() {

				public boolean cancel(boolean mayInterruptIfRunning) {
					return false;
				}

				public boolean isCancelled() {
					return false;
				}

				public boolean isDone() {
					return true;
				}

				public Boolean get() throws InterruptedException, ExecutionException {
					return true;
				}

				public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
					return true;
				}
			};
		}

		return sendMessage(sms);

	}
    
    /**
     * Send a JavaMail message
     *
     * @param msg An Email message
     */
    public static Future<Boolean> sendMessage(final SMSMessage sms) {
        if (asynchronousSend) {
            return executor.submit(new Callable<Boolean>() {

                public Boolean call() {
                    try {
                    	MyCoolSmsService service = new MyCoolSmsService();
                    	service.sendSms(sms.number, sms.message);
                        return true;
                    } catch (Throwable e) {
                        MailException me = new MailException("Error while sending sms", e);
                        Logger.error(me, "The sms has not been sent");
                        return false;
                    }
                }
            });
        } else {
            final StringBuffer result = new StringBuffer();
            try {
            	MyCoolSmsService service = new MyCoolSmsService();
            	service.sendSms(sms.number, sms.message);
            } catch (Throwable e) {
                MailException me = new MailException("Error while sending sms", e);
                Logger.error(me, "The sms has not been sent");
                result.append("oops");
            }
            return new Future<Boolean>() {

                public boolean cancel(boolean mayInterruptIfRunning) {
                    return false;
                }

                public boolean isCancelled() {
                    return false;
                }

                public boolean isDone() {
                    return true;
                }

                public Boolean get() throws InterruptedException, ExecutionException {
                    return result.length() == 0;
                }

                public Boolean get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                    return result.length() == 0;
                }
            };
        }
    }
    
    public static class SMSMessage{
    	public final String number;
    	public final String message;
    	
		public SMSMessage(final String number, final String message) {
			super();
			this.number = number;
			this.message = message;
		}
    }
    
    public static class Mock {

        static Map<String, String> smses = new HashMap<String, String>();


        static void send(SMSMessage sms) {
        	final StringBuilder content = new StringBuilder();
                
            content.append("From Mock SMS sender\n\tNew sms received by");

            content.append("\n\tTo: " + sms.number);
            content.append("\n\t" + sms.message);
            content.append("\n");
            Logger.info(content.toString());
   
            smses.put(sms.number, sms.message);

        }

        public static String getLastMessageReceivedBy(String number) {
            return smses.get(number);
        }
        
        public static void reset(){
        	smses.clear();
        }
    }

}
