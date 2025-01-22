package com.smartvehicle.security.services;

import com.twilio.exception.ApiException;
import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
import com.twilio.rest.api.v2010.account.Message;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service()
@Slf4j
public class TwilioVerificationService {

    private final com.twilio.rest.verify.v2.Service twilioService;
    @Value("${twilio.account.sid}")
    private String accountSid;

    @Value("${twilio.auth.token}")
    private String authToken;

    private String serviceSid;

    @Autowired
    public TwilioVerificationService(com.twilio.rest.verify.v2.Service twilioService) {
        this.twilioService = twilioService;
        this.serviceSid = this.twilioService.getSid();
    }

    public String startVerification(String phoneNumber) {
        Verification verification=null;
       try {
            verification = Verification.creator(
                   serviceSid,
                   "+91" + phoneNumber,
                   "sms"
           ).create();
       }catch (Exception e){
           e.printStackTrace();
       }
        log.debug(" {} ",verification);
        System.out.println(verification.getStatus());
        return verification.getStatus();
    }

    public String checkVerification(String phoneNumber, String code) {
        log.info("otp checkVerification  {} code {} ",phoneNumber,code);
        VerificationCheck verificationCheck=null;
        try {
            verificationCheck = VerificationCheck.creator(serviceSid)
                    .setTo("+91" + phoneNumber)
                    .setCode(code)
                    .create();
            String status=verificationCheck.getStatus();
            log.info(" after otp checkVerification  {} code {} status {}  ",phoneNumber,code,status);
            return status;
        }catch (ApiException e ){
            log.info(" ApiException otp checkVerification  {} ",e.getMessage());
            if(e.getMessage().contains("/VerificationCheck was not found")){
                return "approved";
            }
        }catch (Exception e){
            e.printStackTrace();
            return "Failed "+e.getMessage();
        }
        return "failed";
    }
    public String sendSmsMessage(String toPhoneNumber, String messageContent) {
        log.info("Sending SMS to {}: {}", toPhoneNumber, messageContent);
        try {
            Message message = Message.creator(
                            new com.twilio.type.PhoneNumber("+91" + toPhoneNumber),
                            messageContent,
                            serviceSid
                    )
                    .create();
            log.info("SMS sent to {} with SID: {}", toPhoneNumber, message.getSid());
            return "Sent";
        } catch (ApiException e) {
            log.error("API exception while sending SMS: {}", e.getMessage());
            return "Failed: " + e.getMessage();
        } catch (Exception e) {
            log.error("Error while sending SMS: ", e);
            return "Failed: " + e.getMessage();
        }
    }
}