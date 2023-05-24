package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.database.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class UniverseService {
    private final PlanetRepository planetRepository;

    private final BuildingRepository buildingRepository;

    private final BuildingResourceCostsRepository buildingResourceCostsRepository;

    private final PlanetResourceRepository planetResourceRepository;

    private final GalaxyRepository galaxyRepository;

    private final BuildingTypeRepository buildingTypeRepository;

    public List<Planet> retrievePlanets(String galaxyName) {
        List<Planet> list = StreamSupport.stream(planetRepository.findByUniverseName(galaxyName).spliterator(), false).collect(Collectors.toList());
        list.forEach(f -> f.setGalaxyName(galaxyName));
        return list;
    }

    public String assignFreePlanet(String accountId) {
        Iterable<Planet> planets = planetRepository.findFreeSolarSystem();
        List<Planet> list = StreamSupport.stream(planets.spliterator(), false).collect(Collectors.toList());
        if (list.size() == 0) {
            throw new PlanetNotAvailableException();
        }
        Collections.shuffle(list);
        Planet planet = list.get(0);
        planet.setAccountId(accountId);
        return planetRepository.save(planet).getPlanetId();
    }

    public void createBuildings(String planetId, Set<BuildingTypeEnum> buildings) {
        List<Building> list = new ArrayList<>();
        for (BuildingTypeEnum buildingType : buildings) {
            Building building = new Building();
            building.setBuildingType(buildingType.name());
            building.setPlanetId(planetId);
            building.setLevel(1);
            list.add(building);
        }
        buildingRepository.saveAll(list);
    }

    public List<Planet> retrievePlanetsByAccountId(String accountId) {
        List<Planet> planets = StreamSupport.stream(planetRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
        List<String> galaxyIds = planets.stream().map(Planet::getGalaxyId).distinct().collect(Collectors.toList());
        List<Galaxy> galaxies = StreamSupport.stream(galaxyRepository.findAllById(galaxyIds).spliterator(), false).collect(Collectors.toList());
        planets.forEach(f -> f.setGalaxyName(Objects.requireNonNull(galaxies.stream().filter(g -> g.getGalaxyId().equals(f.getGalaxyId())).findAny().orElse(null)).getNickname()));
        return planets;
    }

    public Planet retrievePlanet(String planetId) {
        Optional<Planet> optional = planetRepository.findById(planetId);
        if (optional.isEmpty()) {
            throw new PlanetNotAvailableException();
        }
        Optional<Galaxy> optionalGalaxy = galaxyRepository.findById(optional.get().getGalaxyId());
        if (optionalGalaxy.isEmpty()) {
            throw new PlanetNotAvailableException();
        }
        optional.get().setGalaxyName(optionalGalaxy.get().getNickname());
        return optional.get();
    }

    public List<PlanetResource> retrieveResources(String planetId) {
        return StreamSupport.stream(planetResourceRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());
    }

    public IdContainer retrievePlanetIdContainer(String planetId) {
        String[] s = planetId.split("-");
        Optional<Galaxy> optionalGalaxy = galaxyRepository.findByNickname(s[0]);
        if (optionalGalaxy.isEmpty()) {
            throw new PlanetNotAvailableException();
        }
        Optional<Planet> optional = planetRepository.findByGalaxyIdAndXAndYAndZAndOrbit(optionalGalaxy.get().getGalaxyId(), Integer.parseInt(s[1]), Integer.parseInt(s[2]), Integer.parseInt(s[3]), s[4].charAt(0));
        if (optional.isEmpty()) {
            throw new PlanetNotAvailableException();
        }
        return new IdContainer(optional.get().getPlanetId(), optional.get().getAccountId(), optional.get().getGalaxyId());
    }

    public List<Building> retrieveBuildings(String planetId) {
        return StreamSupport.stream(buildingRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());
    }

    public void upgradeBuilding(String planetId, BuildingTypeEnum buildingType) {
        Optional<BuildingType> optionalBuildingType = buildingTypeRepository.findById(buildingType.name());
        if (optionalBuildingType.isEmpty()) {
            throw new IllegalArgumentException();
        }
        List<Building> buildings = retrieveBuildings(planetId);
        Optional<Building> optionalBuilding = buildings.stream().filter(f -> f.getBuildingType().equals(buildingType.name())).findAny();
        int level = optionalBuilding.map(Building::getLevel).orElse(0);
        for (Building building : buildings) {
            if (building.getNextLevelUpdate() != null) {
                throw new FacilityBusyException("Only one building can be build / upgraded at one time");
            }
        }
        int buildingYardLevel;
        Optional<Building> buildingYard = buildings.stream().filter(f -> "BUILDING_YARD".equals(f.getBuildingType())).findAny();
        if (buildingYard.isPresent()) {
            buildingYardLevel = buildingYard.get().getLevel();
        } else if (buildingType != BuildingTypeEnum.BUILDING_YARD) {
            throw new BuildingNotAvailableException("Building Yard is need to build a building.");
        } else {
            buildingYardLevel = 0;
        }
        List<PlanetResource> resources = StreamSupport.stream(planetResourceRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());
        List<BuildingResourceCosts> costs = StreamSupport.stream(buildingResourceCostsRepository.findByBuildingType(buildingType.name()).spliterator(), false).collect(Collectors.toList());
        for (BuildingResourceCosts cost : costs) {
            Optional<PlanetResource> optional = resources.stream().filter(f -> f.getResourceType().equals(cost.getResourceType())).findAny();
            if (optional.isEmpty()) {
                throw new NotAffordableException();
            }
            optional.get().setUnits((int)(optional.get().getUnits() - cost.getBasicValue() * Math.pow(cost.getBase(), level + cost.getExponentModifier())));
            if (optional.get().getUnits() < 0) {
                throw new NotAffordableException();
            }
        }
        planetResourceRepository.saveAll(resources);
        Calendar calendar = GregorianCalendar.getInstance();
        int seconds = (int)(optionalBuildingType.get().getDurationInSeconds() * Math.pow(optionalBuildingType.get().getLevelBase(), level) * Math.pow(optionalBuildingType.get().getBuildingYardBase(), buildingYardLevel));
        calendar.add(Calendar.SECOND, seconds);

        Building building = new Building();
        building.setBuildingType(buildingType.name());
        building.setPlanetId(planetId);
        building.setLevel(level);
        building.setNextLevelUpdate(calendar.getTime());
        buildingRepository.save(building);
    }
}
