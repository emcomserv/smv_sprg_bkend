package com.smartvehicle.entity;


import com.smartvehicle.security.services.UserDetailsImpl;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;

import java.util.Map;

public class AuditEntityListener {

    @PrePersist
    public void setCreatedByAndUpdatedBy(BaseEntity entity) {
        Long userId = getCurrentUserId();
        entity.setCreatedBy(userId);
        entity.setUpdatedBy(userId);
    }

    @PreUpdate
    public void setUpdatedBy(BaseEntity entity) {
        Long userId = getCurrentUserId();
        entity.setUpdatedBy(userId);
    }

    private Long getCurrentUserId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getPrincipal())) {
            UserDetailsImpl user = (UserDetailsImpl) authentication.getPrincipal();
            System.out.println(user.getUsername() +" ID " +user.getId());
            return user.getId();
        }
        return 0L;
    }
}
