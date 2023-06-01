package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.jdbc.DatabaseService;
import de.neebs.spacepeoples.integration.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {
    private final GalaxyRepository galaxyRepository;

    private final PlanetRepository planetRepository;

    private final DatabaseService databaseService;

    public Galaxy createGalaxy(String nickname) {
        Galaxy galaxy = new Galaxy();
        galaxy.setGalaxyId(UUID.randomUUID().toString());
        galaxy.setNickname(nickname);
        galaxy = galaxyRepository.save(galaxy);
        generatePlanets(galaxy.getGalaxyId());
        return galaxy;
    }

    @Async
    public void generatePlanets(String galaxyId) {
        List<Planet> planets = new ArrayList<>();
        for (int x = 1; x < 10; x++) {
            for (int y = 1; y < 10; y++) {
                for (int z = 1; z < 10; z++) {
                    planets.addAll(generateSolarSystem(galaxyId, x, y, z));
                }
            }
        }
        planetRepository.saveAll(planets);
        databaseService.generateResources(galaxyId);
    }

    private List<Planet> generateSolarSystem(String universeId, int x, int y, int z) {
        List<Planet> planets = new ArrayList<>();
        int size = new Random().nextInt(7);
        char startLetter = 'A';
        for (char letter = startLetter; letter < (startLetter + size + 3); letter++) {
            Planet planet = new Planet();
            planet.setPlanetId(UUID.randomUUID().toString());
            planet.setGalaxyId(universeId);
            planet.setX(x);
            planet.setY(y);
            planet.setZ(z);
            planet.setOrbit(letter);
            planet.setName(x + " - " + y + " - " + z + " - "+ letter);
            planets.add(planet);
        }
        return planets;
    }
}
