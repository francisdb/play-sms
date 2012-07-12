package services.sms;

import services.sms.MyCoolSmsService.SmsException;

/**
 * Play sms plugin sms service
 *
 */
public interface SmsService {

	/**
	 * @param number The cell phone number in international format. For example: 49123456789 or +49123456789 or 0049123456789
	 * @param message The message body.
	 * @param senderId The sender id to use or <code>null</code> for default
	 * @throws SmsException 
	 */
	void sendSms(final String number, final String message, final String senderId) throws SmsException;

}