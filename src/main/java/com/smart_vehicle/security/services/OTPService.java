package com.smart_vehicle.security.services;


import com.smart_vehicle.models.EmailDTO;
import com.smart_vehicle.models.Parent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class OTPService {

    private final Logger LOGGER = LoggerFactory.getLogger(OTPService.class);

    private OTPGenerator oTPGenerator;
    private EmailService emailService;
    private ParentDetailsServiceImpl parentService;

    public OTPService(OTPGenerator oTPGenerator, EmailService emailService, ParentDetailsServiceImpl parentService)
    {
        this.oTPGenerator = oTPGenerator;
        this.emailService = emailService;
        this.parentService = parentService;
    }

    public Boolean sendOTPToEmail(String userName)
    {
    	 Parent parent = parentService.findParentByUsername(userName);
         List<String> recipients = new ArrayList<>();
         recipients.add(parent.getEmail());

        // generate otp
        Integer otpValue = oTPGenerator.generateOTP(userName+"_"+parent.getEmail());
        if (otpValue == -1)
        {
            LOGGER.error("OTP generator is not working...");
            return  false;
        }

        LOGGER.info("Generated OTP: {}", otpValue);


        // generate emailDTO object
        EmailDTO emailDTO = new EmailDTO();
        emailDTO.setSubject("Smart Home OTP");
        emailDTO.setBody("OTP is : " + otpValue);
        emailDTO.setRecipients(recipients);

        // send generated e-mail
        return emailService.sendSimpleMessage(emailDTO);
    }

    public Boolean sendOTPToPhone(String userName)
    {
    	 // fetch user e-mail from database
        Parent parent = parentService.findParentByUsername(userName);
        List<String> recipients = new ArrayList<>();
        recipients.add(parent.getEmail());
        // generate otp
        Integer otpValue = oTPGenerator.generateOTP(userName+"_"+parent.getContactNum());
        if (otpValue == -1)
        {
            LOGGER.error("OTP generator is not working...");
            return  false;
        }

        LOGGER.info("Generated OTP: {}", otpValue);

        //TODO:Add logic to send SMS to phone number.
       return true;

    }


    /**
     * Method for validating provided OTP
     *
     * @param key - provided key
     * @param otpNumber - provided OTP number
     * @return boolean value (true|false)
     */
    public Boolean validateOTP(String key, Integer otpNumber)
    {
        // get OTP from cache
        Integer cacheOTP = oTPGenerator.getOTPByKey(key);
        if (cacheOTP!=null && cacheOTP.equals(otpNumber))
        {
            oTPGenerator.clearOTPFromCache(key);
            return true;
        }
        return false;
    }
}