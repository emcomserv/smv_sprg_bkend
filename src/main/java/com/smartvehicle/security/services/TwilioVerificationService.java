package com.smartvehicle.security.services;

import com.twilio.rest.verify.v2.service.Verification;
import com.twilio.rest.verify.v2.service.VerificationCheck;
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
        System.out.println("checkVerification "+phoneNumber +", code "+code);
        VerificationCheck verificationCheck=null;
        try {
            verificationCheck = VerificationCheck.creator(serviceSid)
                    .setTo("+91" + phoneNumber)
                    .setCode(code)
                    .create();
        }catch (Exception e){
            e.printStackTrace();
        }
        return verificationCheck.getStatus();
    }
}