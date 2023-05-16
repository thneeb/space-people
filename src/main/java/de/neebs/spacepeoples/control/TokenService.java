package de.neebs.spacepeoples.control;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.stream.Collectors;

import static org.springframework.security.oauth2.jose.jws.MacAlgorithm.HS256;

@Service
@RequiredArgsConstructor
public class TokenService {
    private final JwtEncoder encoder;

    public String generateToken(Authentication authentication, String accountId) {
        return generateToken(authentication.getName(), authentication.getAuthorities(), accountId);
    }

    public String generateToken(String name, Collection<? extends GrantedAuthority> authorities, String accountId) {
        Instant now = Instant.now();
        String scope = authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(" "));
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(1, ChronoUnit.HOURS))
                .subject(name)
                .claim("scope", scope)
                .claim("account-id", accountId)
                .build();
        JwsHeader header = JwsHeader.with(HS256).build();
        return this.encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue();
    }
}
