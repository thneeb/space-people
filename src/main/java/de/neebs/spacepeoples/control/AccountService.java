package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.entity.Agent;
import de.neebs.spacepeoples.entity.Planet;
import de.neebs.spacepeoples.integration.database.Account;
import de.neebs.spacepeoples.integration.database.AccountRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {
    private final AccountRepository accountRepository;

    public Agent retrieveAgent(String accountId) {
        Optional<Account> optional = accountRepository.findById(accountId);
        if (!optional.isPresent()) {
            throw new IllegalStateException();
        }
        return new Agent(optional.get().getNickname());
    }

    public Account createAccount(String nickname, String password) {
        Account account = new Account();
        account.setAccountId(UUID.randomUUID().toString());
        account.setPassword(password);
        account.setNickname(nickname);
        return accountRepository.save(account);
    }

    public Account retrieveAgentByNickname(String name) {
        Optional<Account> optional = accountRepository.findByNickname(name);
        if (!optional.isPresent()) {
            throw new IllegalStateException();
        }
        return optional.get();
    }

    public Planet assignFreePlanet(String accountId) {
        return null;
    }
}
