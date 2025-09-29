package com.smartvehicle.service;

import com.smartvehicle.entity.ContactInfo;
import com.smartvehicle.mapper.ContactInfoMapper;
import com.smartvehicle.payload.request.ContactInfoCreateReq;
import com.smartvehicle.payload.response.ContactInfoResponse;
import com.smartvehicle.repository.ContactInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ContactInfoService {

    @Autowired
    private ContactInfoRepository contactInfoRepository;

    @Autowired
    private ContactInfoMapper contactInfoMapper;

    @Autowired
    private EmailService emailService;

    @Transactional
    public ContactInfoResponse create(ContactInfoCreateReq request) {
        ContactInfo entity = contactInfoMapper.toEntity(request);
        ContactInfo saved = contactInfoRepository.save(entity);
        // After saving, send emails: 1) to recipient with details 2) acknowledgment to user
        try {
            String recipientBody = buildRecipientHtml(saved);
            emailService.sendEmail(emailService.getActiveSettings().getRecipient(),
                    "New Contact Request from " + saved.getFullName(), recipientBody);

            String userAckBody = buildUserAckHtml(saved);
            emailService.sendEmail(saved.getEmail(),
                    "We received your request", userAckBody);
        } catch (MessagingException ex) {
            // Log and continue; do not fail the request if email fails
            ex.printStackTrace();
        }
        return contactInfoMapper.toResponse(saved);
    }

    private String buildRecipientHtml(ContactInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<h3>New Contact Submission</h3>");
        sb.append("<ul>");
        sb.append("<li><b>Full Name:</b> ").append(info.getFullName()).append("</li>");
        sb.append("<li><b>Contact Number:</b> ").append(info.getContactNumber()).append("</li>");
        sb.append("<li><b>Email:</b> ").append(info.getEmail()).append("</li>");
        sb.append("<li><b>School Name:</b> ").append(info.getSchoolName()).append("</li>");
        sb.append("<li><b>Message:</b> ").append(info.getMessage()).append("</li>");
        sb.append("</ul>");
        return sb.toString();
    }

    private String buildUserAckHtml(ContactInfo info) {
        StringBuilder sb = new StringBuilder();
        sb.append("<p>Hi ").append(info.getFullName()).append(",</p>");
        sb.append("<p>Your message has been received successfully. Our team will contact you shortly.</p>");
        sb.append("<p>Summary you submitted:</p>");
        sb.append("<ul>");
        sb.append("<li><b>School Name:</b> ").append(info.getSchoolName()).append("</li>");
        sb.append("<li><b>Message:</b> ").append(info.getMessage()).append("</li>");
        sb.append("</ul>");
        sb.append("<p>Thank you.</p>");
        return sb.toString();
    }
}


