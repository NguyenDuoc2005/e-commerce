package com.ecommerce.auth.security;

import com.ecommerce.auth.client.UserClient;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserClient userClient;

    public CustomUserDetailsService(UserClient userClient) {
        this.userClient = userClient;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        String role = LoginRoleContext.get();
        if ("ADMIN".equals(role)) {
            Map<String, Object> staff = userClient.getStaffByEmail(email, true);
            if (!staff.isEmpty()) {
                return UserPrincipal.create(staff, "ADMIN");
            }
        } else if ("USER".equals(role)) {
            Map<String, Object> customer = userClient.getCustomerByEmail(email, true);
            if (!customer.isEmpty()) {
                return UserPrincipal.create(customer, "USERS");
            }
        }

        throw new UsernameNotFoundException("User not found with email: " + email);
    }
}
