package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.jpa.Account;
import de.neebs.spacepeoples.integration.jpa.AccountRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {
    private final AccountRepository accountRepository;

    @Override
    public UserDetails loadUserByUsername(String nickname) throws UsernameNotFoundException {
        Optional<Account> optional = accountRepository.findByNickname(nickname);
        if (optional.isEmpty()) {
            throw new UsernameNotFoundException(nickname + " is unknown");
        }
        List<GrantedAuthority> list = new ArrayList<>();
        if ("Admin".equalsIgnoreCase(nickname)) {
            list.add(new SimpleGrantedAuthority("ADMIN"));
        }
        return new User(nickname, optional.get().getPassword(), list);
    }
}
