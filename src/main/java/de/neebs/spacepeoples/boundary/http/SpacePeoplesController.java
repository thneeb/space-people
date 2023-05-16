package de.neebs.spacepeoples.boundary.http;

import de.neebs.spacepeoples.control.AccountService;
import de.neebs.spacepeoples.control.RegistrationService;
import de.neebs.spacepeoples.controller.http.DefaultApi;
import de.neebs.spacepeoples.entity.Agent;
import de.neebs.spacepeoples.entity.RegistrationRequest;
import de.neebs.spacepeoples.entity.TokenBody;
import de.neebs.spacepeoples.integration.database.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SpacePeoplesController implements DefaultApi {
    private final RegistrationService registrationService;

    private final AccountService accountService;

    @Override
    public ResponseEntity<TokenBody> token() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(new TokenBody(registrationService.generateToken(authentication)));
    }

    @Override
    public ResponseEntity<TokenBody> createAccount(RegistrationRequest request) {
        Account account = registrationService.register(request);
        String accessToken = registrationService.generateToken(account);
        return ResponseEntity.ok(new TokenBody(accessToken));
    }

    @Override
    public ResponseEntity<Agent> retrieveAccount() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Jwt jwt = (Jwt)authentication.getPrincipal();
        log.debug(jwt.getClaims().toString());
        String accountId = jwt.getClaimAsString("account-id");
        log.debug(authentication.getAuthorities().toString());
        return ResponseEntity.ok(accountService.retrieveAgent(accountId));
    }

    @Override
    public ResponseEntity<Void> generatePlanets() {
        log.debug("Accessed secured method");
        return ResponseEntity.ok().build();
    }
}
