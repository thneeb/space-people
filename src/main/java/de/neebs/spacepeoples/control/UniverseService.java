package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class UniverseService {
    private final PlanetRepository planetRepository;

    private final BuildingRepository buildingRepository;

    public List<Planet> retrievePlanets(String universeName) {
        return StreamSupport.stream(planetRepository.findByUniverseName(universeName).spliterator(), false).collect(Collectors.toList());
    }

    public Planet assignFreePlanet(String accountId) {
        Iterable<Planet> planets = planetRepository.findFreeSolarSystem();
        List<Planet> list = StreamSupport.stream(planets.spliterator(), false).collect(Collectors.toList());
        if (list.size() == 0) {
            throw new PlanetNotAvailableException();
        }
        Collections.shuffle(list);
        Planet planet = list.get(0);
        planet.setAccountId(accountId);
        return planetRepository.save(planet);
    }

    public void createBuildings(String planetId, Set<BuildingType> buildings) {
        List<Building> list = new ArrayList<>();
        for (BuildingType buildingType : buildings) {
            Building building = new Building();
            building.setBuildingType(buildingType.name());
            building.setPlanetId(planetId);
            building.setLevel(1);
            building.setStatus(BuildingStatus.READY.name());
            list.add(building);
        }
        buildingRepository.saveAll(list);
    }

    public void createInitialResources(String planetId) {


    }
}
