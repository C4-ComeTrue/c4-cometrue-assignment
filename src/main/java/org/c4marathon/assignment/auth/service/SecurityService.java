package org.c4marathon.assignment.auth.service;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class SecurityService {
    public Long findMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (Long)authentication.getPrincipal();
    }
}
