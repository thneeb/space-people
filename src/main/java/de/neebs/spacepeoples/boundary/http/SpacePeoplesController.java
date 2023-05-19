package de.neebs.spacepeoples.boundary.http;

import de.neebs.spacepeoples.control.AccountService;
import de.neebs.spacepeoples.control.AdminService;
import de.neebs.spacepeoples.control.RegistrationService;
import de.neebs.spacepeoples.control.UniverseService;
import de.neebs.spacepeoples.controller.http.DefaultApi;
import de.neebs.spacepeoples.entity.*;
import de.neebs.spacepeoples.integration.database.Account;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SpacePeoplesController implements DefaultApi {
    private final RegistrationService registrationService;

    private final AdminService adminService;

    private final AccountService accountService;

    private final UniverseService universeService;

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
    public ResponseEntity<List<Planet>> retrievePlanets(String universeName) {
        List<Planet> planets = universeService.retrievePlanets(universeName).stream().map(de.neebs.spacepeoples.integration.database.Planet::toWeb).collect(Collectors.toList());
        return ResponseEntity.ok(planets);
    }

    @Override
    public ResponseEntity<Void> createGalaxy(Galaxy galaxy) {
        adminService.createGalaxy(galaxy.getNickname());
        URI uri = linkTo(methodOn(getClass()).retrievePlanets(galaxy.getNickname())).toUri();
        return ResponseEntity.created(uri).build();
    }
}
