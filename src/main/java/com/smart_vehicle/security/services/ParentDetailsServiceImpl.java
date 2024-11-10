package com.smart_vehicle.security.services;

import com.smart_vehicle.models.Parent;
import com.smart_vehicle.repository.ParentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ParentDetailsServiceImpl implements UserDetailsService {

    @Autowired
    ParentRepository parentRepository;

    @Override
    @Transactional
    public ParentDetailsImpl loadUserByUsername(String username) throws UsernameNotFoundException {
        Parent parent = parentRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Parent Not Found with username: " + username));

        return ParentDetailsImpl.build(parent);
    }

    @Transactional
    public Parent findParentByUsername(String username) throws UsernameNotFoundException {
        Parent parent = parentRepository.findByUserName(username)
                .orElseThrow(() -> new UsernameNotFoundException("Parent Not Found with username: " + username));

        return parent;
    }
}
