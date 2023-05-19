package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.entity.RegistrationRequest;
import de.neebs.spacepeoples.integration.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class RegistrationService {
    private final PasswordEncoder passwordEncoder;

    private final TokenService tokenService;

    private final AccountService accountService;

    private final UniverseService universeService;

    private final DatabaseService databaseService;

    public Account register(RegistrationRequest registrationRequest) {
        Account account = accountService.createAccount(registrationRequest.getNickname(), passwordEncoder.encode(registrationRequest.getPassword()));
        Planet planet = universeService.assignFreePlanet(account.getAccountId());
        databaseService.createInitialResourceBuildings(planet.getPlanetId());
        databaseService.createInitialResourceEvents(planet.getPlanetId());
        universeService.createBuildings(planet.getPlanetId(), Set.of(BuildingType.BUILDING_YARD));
        return account;
    }

    public String generateToken(Authentication authentication) {
        Account account = accountService.retrieveAgentByNickname(authentication.getName());
        return tokenService.generateToken(authentication,account.getAccountId());
    }

    public String generateToken(Account account) {
        return tokenService.generateToken(account.getNickname(), new ArrayList<>(), account.getAccountId());
    }
}
