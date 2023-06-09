package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.entity.CapacityLevel;
import de.neebs.spacepeoples.entity.CapacityType;
import de.neebs.spacepeoples.integration.jpa.*;
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

    private final PlanetCapacitySupplyRepository planetCapacitySupplyRepository;

    private final PlanetCapacityUsedRepository planetCapacityUsedRepository;

    private final PlanetRecycleResourceRepository planetRecycleResourceRepository;

    public List<Planet> retrievePlanets(String galaxyName) {
        List<Planet> list = planetRepository.findByUniverseName(galaxyName);
        list.forEach(f -> f.setGalaxyName(galaxyName));
        return list;
    }

    public String assignFreePlanet(String accountId) {
        List<Planet> list = planetRepository.findFreeSolarSystem();
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
        List<Planet> planets = planetRepository.findByAccountId(accountId);
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
        return planetResourceRepository.findByPlanetId(planetId);
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
        return buildingRepository.findByPlanetId(planetId);
    }

    public Building upgradeBuilding(String planetId, BuildingTypeEnum buildingType) {
        // only one building can be upgraded / build at one time
        List<Building> buildings = retrieveBuildings(planetId);
        Optional<Building> optionalBuilding = buildings.stream().filter(f -> f.getBuildingType().equals(buildingType.name())).findAny();
        int level = optionalBuilding.map(Building::getLevel).orElse(0);
        for (Building building : buildings) {
            if (building.getNextLevelUpdate() != null) {
                if (building.getBuildingType().equals(buildingType.name())) {
                    return building;
                } else {
                    throw new FacilityBusyException("Only one building can be build / upgraded at one time");
                }
            }
        }
        // find out level of the building yard
        int buildingYardLevel;
        Optional<Building> buildingYard = buildings.stream().filter(f -> "BUILDING_YARD".equals(f.getBuildingType())).findAny();
        if (buildingYard.isPresent()) {
            buildingYardLevel = buildingYard.get().getLevel();
        } else if (buildingType != BuildingTypeEnum.BUILDING_YARD) {
            throw new BuildingNotAvailableException("Building Yard is needed to build a building.");
        } else {
            buildingYardLevel = 0;
        }
        // does the new building / upgrade fit into the available capacities
        List<PlanetCapacitySupply> planetCapacitySupplies = planetCapacitySupplyRepository.findByPlanetId(planetId);
        List<PlanetCapacityUsed> planetCapacityUsages = planetCapacityUsedRepository.findByPlanetId(planetId);
        for (PlanetCapacityUsed used : planetCapacityUsages) {
            Optional<PlanetCapacitySupply> optional = planetCapacitySupplies.stream().filter(f -> f.getCapacityType().equals(used.getCapacityType())).findAny();
            if (optional.isPresent() && optional.get().getCapacitySupply() < used.getCapacityUsed()) {
                throw new NotAffordableException("Too less " + used.getCapacityType() + " available");
            }
        }

        // consume resources
        List<PlanetResource> resources = planetResourceRepository.findByPlanetId(planetId);
        List<BuildingResourceCosts> costs = buildingResourceCostsRepository.findByBuildingType(buildingType.name());
        for (BuildingResourceCosts cost : costs) {
            Optional<PlanetResource> optional = resources.stream().filter(f -> f.getResourceType().equals(cost.getResourceType())).findAny();
            if (optional.isEmpty()) {
                throw new NotAffordableException("No " + cost.getResourceType() + " available");
            }
            optional.get().setUnits((int)(optional.get().getUnits() - cost.getBasicValue() * Math.pow(cost.getBase(), level + cost.getExponentModifier())));
            if (optional.get().getUnits() < 0) {
                throw new NotAffordableException("Too less " + cost.getResourceType() + " available");
            }
        }
        planetResourceRepository.saveAll(resources);
        Calendar calendar = GregorianCalendar.getInstance();

        // general check: building type exists in database.
        BuildingType bt = buildingTypeRepository.findById(buildingType.name()).orElseThrow(IllegalArgumentException::new);

        int seconds = (int)(bt.getDurationInSeconds() * Math.pow(bt.getLevelBase(), level) * Math.pow(bt.getBuildingYardBase(), buildingYardLevel));
        calendar.add(Calendar.SECOND, seconds);

        Building building = new Building();
        building.setBuildingType(buildingType.name());
        building.setPlanetId(planetId);
        building.setLevel(level);
        building.setNextLevelUpdate(calendar.getTime());
        return buildingRepository.save(building);
    }

    public PlanetResource discardResources(String planetId, ResourceType resourceType, Long units) {
        if (units == null || units < 1) {
            throw new NotAffordableException("Wrong unit amount specified");
        }
        PlanetResourceId planetResourceId = new PlanetResourceId();
        planetResourceId.setPlanetId(planetId);
        planetResourceId.setResourceType(resourceType.name());
        Optional<PlanetResource> optional = planetResourceRepository.findById(planetResourceId);
        if (optional.isEmpty()) {
            throw new NotAffordableException("No resources on planet.");
        }
        if (optional.get().getUnits() < units) {
            throw new NotAffordableException("Too less resources on planet");
        }
        optional.get().setUnits(optional.get().getUnits() - units);
        return planetResourceRepository.save(optional.get());
    }

    public Building cancelBuildingRequest(String planetId, BuildingTypeEnum buildingType) {
        BuildingId buildingId = new BuildingId();
        buildingId.setBuildingType(buildingType.name());
        buildingId.setPlanetId(planetId);
        Optional<Building> optionalBuilding = buildingRepository.findById(buildingId);
        if (optionalBuilding.isEmpty()) {
            throw new BuildingNotAvailableException("Building not available");
        }
        if (optionalBuilding.get().getNextLevelUpdate() == null) {
            throw new BuildingNotAvailableException("No building request is ongoing");
        }
        PlanetCapacityId planetCapacityId = new PlanetCapacityId();
        planetCapacityId.setPlanetId(planetId);
        planetCapacityId.setCapacityType("RECYCLE");
        Optional<PlanetCapacitySupply> optionalPlanetCapacitySupply = planetCapacitySupplyRepository.findById(planetCapacityId);
        long capacitySupply;
        if (optionalPlanetCapacitySupply.isEmpty()) {
            capacitySupply = 0;
        } else {
            capacitySupply = optionalPlanetCapacitySupply.get().getCapacitySupply();
        }
        if (capacitySupply > 0) {
            List<PlanetRecycleResource> recycleResources = planetRecycleResourceRepository.findByPlanetId(planetId);
            long used = recycleResources.stream().mapToLong(PlanetRecycleResource::getUnits).sum();
            List<BuildingResourceCosts> costs = buildingResourceCostsRepository.findByBuildingType(buildingType.name());
            int recycleSum = (int)(costs.stream().mapToDouble(f -> (int) (f.getBasicValue() * Math.pow(f.getBase(), optionalBuilding.get().getLevel() + f.getExponentModifier()))).sum() * 0.75);
            double fraction = Math.min(1, (double) (capacitySupply - used) / recycleSum);
            for (BuildingResourceCosts cost : costs) {
                Optional<PlanetRecycleResource> optional = recycleResources.stream().filter(f -> f.getResourceType().equals(cost.getResourceType())).findAny();
                PlanetRecycleResource recycleResource;
                if (optional.isEmpty()) {
                    recycleResource = new PlanetRecycleResource();
                    recycleResource.setPlanetId(planetId);
                    recycleResource.setResourceType(cost.getResourceType());
                    recycleResource.setUnits(0);
                    recycleResource.setLastUpdate(new Date());
                    recycleResources.add(recycleResource);
                } else {
                    recycleResource = optional.get();
                }
                recycleResource.setUnits(recycleResource.getUnits() + (int)(cost.getBasicValue() * Math.pow(cost.getBase(), optionalBuilding.get().getLevel() + cost.getExponentModifier()) * 0.75 * fraction));
            }
            planetRecycleResourceRepository.saveAll(recycleResources);
        }
        optionalBuilding.get().setNextLevelUpdate(null);
        return buildingRepository.save(optionalBuilding.get());
    }

    public List<CapacityLevel> retrievePlanetCapacities(String planetId) {
        List<PlanetCapacitySupply> suppliedCapacities = planetCapacitySupplyRepository.findByPlanetId(planetId);
        List<PlanetCapacityUsed> usedCapacities = planetCapacityUsedRepository.findByPlanetId(planetId);
        List<CapacityLevel> capacityLevels = new ArrayList<>();
        for (PlanetCapacitySupply supply : suppliedCapacities) {
            Optional<PlanetCapacityUsed> optional = usedCapacities.stream().filter(f -> f.getCapacityType().equals(supply.getCapacityType())).findAny();
            CapacityLevel capacityLevel = new CapacityLevel();
            capacityLevel.setCapacityType(CapacityType.fromValue(supply.getCapacityType()));
            capacityLevel.setMaxUnits(supply.getCapacitySupply());
            if ("STORAGE".equals(supply.getCapacityType())) {
                capacityLevel.setActualUnits(planetResourceRepository.findByPlanetId(planetId).stream().mapToLong(PlanetResource::getUnits).sum());
            } else if ("RECYCLE".equals(supply.getCapacityType())) {
                capacityLevel.setActualUnits(planetRecycleResourceRepository.findByPlanetId(planetId).stream().mapToLong(PlanetRecycleResource::getUnits).sum());
            } else {
                if (optional.isEmpty()) {
                    capacityLevel.setActualUnits(0L);
                } else {
                    capacityLevel.setActualUnits(optional.get().getCapacityUsed());
                }
            }
            capacityLevels.add(capacityLevel);
        }
        return capacityLevels;
    }

    public List<PlanetRecycleResource> retrieveRecyclables(String planetId) {
        return planetRecycleResourceRepository.findByPlanetId(planetId);
    }

    public PlanetRecycleResource discardRecyclables(String planetId, ResourceType resourceType, Long units) {
        if (units == null || units < 1) {
            throw new NotAffordableException("Wrong unit amount specified");
        }
        PlanetResourceId planetResourceId = new PlanetResourceId();
        planetResourceId.setPlanetId(planetId);
        planetResourceId.setResourceType(resourceType.name());
        Optional<PlanetRecycleResource> optional = planetRecycleResourceRepository.findById(planetResourceId);
        if (optional.isEmpty()) {
            throw new NotAffordableException("No recyclable on planet.");
        }
        if (optional.get().getUnits() < units) {
            throw new NotAffordableException("Too less recyclables on planet");
        }
        optional.get().setUnits(optional.get().getUnits() - units);
        return planetRecycleResourceRepository.save(optional.get());
    }
}
