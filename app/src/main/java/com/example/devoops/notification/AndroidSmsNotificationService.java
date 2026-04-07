package com.example.devoops.notification;

import android.telephony.SmsManager;

public class AndroidSmsNotificationService implements SmsNotificationService {

    @Override
    public void sendSms(String phoneNumber, String message) throws Exception {
        SmsManager smsManager = SmsManager.getDefault();
        if (message.length() > 160) {
            smsManager.sendMultipartTextMessage(
                    phoneNumber, null, smsManager.divideMessage(message), null, null);
        } else {
            smsManager.sendTextMessage(phoneNumber, null, message, null, null);
        }
    }
}
