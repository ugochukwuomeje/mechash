package com.ugo.mecash_multicurrency_wallet.config;

import com.ugo.mecash_multicurrency_wallet.entity.User;
import com.ugo.mecash_multicurrency_wallet.repository.UserRepository;
import com.ugo.mecash_multicurrency_wallet.service.UserDetailsImp;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    UserRepository repo;
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        Optional<User> optionalUser = repo.findByEmail(email);
        if (!optionalUser.isPresent()) {
            throw new UsernameNotFoundException("user not found");
        }
        log.info("########### User found ##########" + optionalUser.get().getFirstName());
        return new UserDetailsImp(optionalUser.get());
    }
}
