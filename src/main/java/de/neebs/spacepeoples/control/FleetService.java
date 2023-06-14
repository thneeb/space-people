package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.entity.CapacityType;
import de.neebs.spacepeoples.integration.jdbc.DatabaseService;
import de.neebs.spacepeoples.integration.jpa.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class FleetService {
    private final FleetRepository fleetRepository;

    private final FleetFuelRepository fleetFuelRepository;

    private final FleetResourceRepository fleetResourceRepository;

    private final ShipTypeRepository shipTypeRepository;

    private final ShipTypeFuelConsumptionRepository shipTypeFuelConsumptionRepository;

    private final PlanetResourceRepository planetResourceRepository;

    private final PlanetCapacitySupplyRepository planetCapacitySupplyRepository;

    private final BuildingRepository buildingRepository;

    private final DatabaseService databaseService;

    public Fleet createFleet(String nickname, String accountId, String planetId, Map<String, Long> shipTypeCounts) {
        List<ShipTypeAvailability> shipAvailabilities = shipTypeRepository.countShipsByShipType(accountId, planetId);
        for (Map.Entry<String, Long> entry : shipTypeCounts.entrySet()) {
            Optional<ShipTypeAvailability> optional = shipAvailabilities.stream().filter(f -> f.getShipType().equals(entry.getKey())).findAny();
            if (optional.isEmpty()) {
                throw new NotEnoughShipsAvailableException("Not enough ships of " + entry.getKey() + ": " + entry.getValue() + "/0");
            }
            if (optional.get().getCount() < entry.getValue()) {
                throw new NotEnoughShipsAvailableException("Not enough ships of " + entry.getKey() + ": " + entry.getValue() + "/" + optional.get().getCount());
            }
        }
        Fleet fleet = new Fleet();
        fleet.setFleetId(UUID.randomUUID().toString());
        fleet.setNickname(nickname);
        fleet.setStatus(FleetStatusEnum.DOCKED.name());
        fleet.setAccountId(accountId);
        fleet.setPlanetId(planetId);
        fleet = fleetRepository.save(fleet);
        for (Map.Entry<String, Long> entry : shipTypeCounts.entrySet()) {
            databaseService.assignShipsToFleet(fleet.getFleetId(), planetId, entry.getKey(), entry.getValue());
        }
        return fleet;
    }

    public List<FleetFuel> refuelFleet(String accountId, String nickname) {
        Fleet fleet = fleetRepository.findByAccountIdAndNickname(accountId, nickname).orElseThrow(() -> new FleetNotExistsException(nickname));
        if (!fleet.getStatus().equals(FleetStatusEnum.DOCKED.name())) {
            throw new FleetStatusException("Fleet must be docked to refuel from planet resources");
        }
        String planetId = fleet.getPlanetId();
        List<ShipTypeFuelConsumption> fuelConsumptions = shipTypeFuelConsumptionRepository.sumUpFuelConsumption(fleet.getFleetId());
        long total = fuelConsumptions.stream().mapToLong(ShipTypeFuelConsumption::getUnits).sum();
        Long capacity = fleetRepository.sumUpFuelCapacity(fleet.getFleetId());
        List<FleetFuel> fuelAvailable = fleetFuelRepository.findByFleetId(fleet.getFleetId());

        List<PlanetResource> planetResources = planetResourceRepository.findByPlanetId(planetId);
        for (ShipTypeFuelConsumption stfc : fuelConsumptions) {
            long wanted = capacity * stfc.getUnits() / total;
            Optional<FleetFuel> optionalFleetFuel = fuelAvailable.stream().filter(f -> f.getResourceType().equals(stfc.getResourceType())).findAny();
            FleetFuel fleetFuel;
            if (optionalFleetFuel.isEmpty()) {
                fleetFuel = new FleetFuel(fleet.getFleetId(), stfc.getResourceType(), 0);
                fuelAvailable.add(fleetFuel);
            } else {
                fleetFuel = optionalFleetFuel.get();
            }
            long needed = wanted - fleetFuel.getUnits();

            Optional<PlanetResource> optionalPlanetFuel = planetResources.stream().filter(f -> f.getResourceType().equals(stfc.getResourceType())).findAny();
            if (optionalPlanetFuel.isPresent() && needed > 0) {
                long fuel = Math.min(needed, optionalPlanetFuel.get().getUnits());
                optionalPlanetFuel.get().setUnits(optionalPlanetFuel.get().getUnits() - fuel);
                fleetFuel.setUnits(fleetFuel.getUnits() + fuel);
            }
        }
        planetResourceRepository.saveAll(planetResources);
        fleetFuelRepository.saveAll(fuelAvailable);
        return fuelAvailable;
    }

    public List<FleetFuel> retrieveFleetFuel(String accountId, String nickname) {
        Optional<Fleet> optional = fleetRepository.findByAccountIdAndNickname(accountId, nickname);
        if (optional.isEmpty()) {
            throw new FleetNotExistsException(nickname);
        }
        return fleetFuelRepository.findByFleetId(optional.get().getFleetId());
    }

    public Fleet fleetToOrbit(String accountId, String nickname) {
        Fleet fleet = fleetRepository.findByAccountIdAndNickname(accountId, nickname).orElseThrow(() -> new FleetNotExistsException(nickname));
        if (fleet.getStatus().equals(FleetStatusEnum.ORBIT.name())) {
            return fleet;
        }
        if (!fleet.getStatus().equals(FleetStatusEnum.DOCKED.name())) {
            throw new FleetStatusException("Only fleets in port can be send to orbit");
        }
        long time = launchOrLandFleet(fleet);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, (int)time);
        fleet.setStatus(FleetStatusEnum.ORBIT.name());
        fleet.setNextStatusUpdate(calendar.getTime());
        return fleetRepository.save(fleet);
    }

    public Fleet fleetToPort(String accountId, String nickname) {
        Fleet fleet = fleetRepository.findByAccountIdAndNickname(accountId, nickname).orElseThrow(() -> new FleetNotExistsException(nickname));
        if (fleet.getStatus().equals(FleetStatusEnum.DOCKED.name())) {
            return fleet;
        }
        if (!fleet.getStatus().equals(FleetStatusEnum.ORBIT.name())) {
            throw new FleetStatusException("Only fleets in port can be send to orbit");
        }
        long time = launchOrLandFleet(fleet);

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, (int)time);
        fleet.setStatus(FleetStatusEnum.DOCKED.name());
        fleet.setNextStatusUpdate(calendar.getTime());
        return fleetRepository.save(fleet);
    }

    private long launchOrLandFleet(Fleet fleet) {
        if (fleet.getNextStatusUpdate() != null) {
            throw new FleetBusyException();
        }

        int maxStability = fleetRepository.maxStability(fleet.getFleetId());
        BuildingId buildingId = new BuildingId();
        buildingId.setPlanetId(fleet.getPlanetId());
        buildingId.setBuildingType(BuildingTypeEnum.SPACEPORT.name());
        Building port = buildingRepository.findById(buildingId).orElseThrow(() -> new BuildingNotAvailableException(buildingId.getBuildingType() + " not found"));

        // calculate time for takeoff
        long time = Math.round(maxStability * Math.pow(0.91, port.getLevel()) * 30);

        // calculate used fuel
        List<FleetFuel> fleetFuels = fleetFuelRepository.findByFleetId(fleet.getFleetId());
        List<ShipTypeFuelConsumption> consumptions = shipTypeFuelConsumptionRepository.sumUpFuelConsumption(fleet.getFleetId());
        for (ShipTypeFuelConsumption consumption : consumptions) {
            FleetFuel fleetFuel = fleetFuels.stream().filter(f -> f.getResourceType().equals(consumption.getResourceType())).findAny().orElse(new FleetFuel(fleet.getFleetId(), consumption.getResourceType(), 0));
            fleetFuel.setUnits(fleetFuel.getUnits() - consumption.getUnits() * time / 3600);
            if (fleetFuel.getUnits() < 0) {
                throw new NotAffordableException("To less fuel to execute action");
            }
        }
        fleetFuelRepository.saveAll(fleetFuels);
        return time;
    }

    public List<FullFleet> retrieveFleets(String accountId) {
        List<Fleet> fleets = fleetRepository.findByAccountId(accountId);
        List<String> fleetIds = fleets.stream().map(Fleet::getFleetId).collect(Collectors.toList());
        List<FleetFuel> fleetFuels = fleetFuelRepository.findByFleetIdIn(fleetIds);
        List<ShipTypeCount> fleetShipTypes = fleetRepository.sumUpShipTypesByFleetIdIn(fleetIds);
        List<FleetCharacteristic> fleetCharacteristics = fleetRepository.sumUpCharacteristicsByFleetIdIn(fleetIds);
        List<FullFleet> list = new ArrayList<>();
        for (Fleet fleet : fleets) {
            list.add(new FullFleet(fleet,
                    fleetFuels.stream().filter(f -> f.getFleetId().equals(fleet.getFleetId())).collect(Collectors.toList()),
                    fleetShipTypes.stream().filter(f -> f.getFleetId().equals(fleet.getFleetId())).collect(Collectors.toList()),
                    fleetCharacteristics.stream().filter(f -> f.getFleetId().equals(fleet.getFleetId())).collect(Collectors.toList())));
        }
        return list;
    }

    public List<FleetResource> retrieveResources(String accountId, String nickname) {
        Fleet fleet = fleetRepository.findByAccountIdAndNickname(accountId, nickname).orElseThrow(() -> new FleetNotExistsException(nickname));
        return fleetResourceRepository.findByFleetId(fleet.getFleetId());
    }

    public List<FleetResource> setResourcesInFleet(String accountId, String nickname, Map<String, Long> resources) {
        Fleet fleet = fleetRepository.findByAccountIdAndNickname(accountId, nickname).orElseThrow(() -> new FleetNotExistsException(nickname));
        if (!fleet.getStatus().equals(FleetStatusEnum.DOCKED) || fleet.getNextStatusUpdate() != null) {
            throw new FleetStatusException("Fleet must be finally docked in a port");
        }
        List<FleetResource> fleetResources = fleetResourceRepository.findByFleetId(fleet.getFleetId());
        List<PlanetResource> planetResources = planetResourceRepository.findByPlanetId(fleet.getPlanetId());
        PlanetCapacityId planetCapacityId = new PlanetCapacityId();
        planetCapacityId.setPlanetId(fleet.getPlanetId());
        planetCapacityId.setCapacityType(CapacityType.STORAGE.getValue());
        PlanetCapacitySupply storage = planetCapacitySupplyRepository.findById(planetCapacityId).orElseThrow(IllegalArgumentException::new);
        retrieveResources(accountId, nickname);
        for (Map.Entry<String, Long> entry : resources.entrySet()) {
            Optional<FleetResource> optionalFleetResource = fleetResources.stream().filter(f -> f.getResourceType().equals(entry.getKey())).findAny();
            FleetResource fleetResource;
            if (optionalFleetResource.isEmpty()) {
                fleetResource = new FleetResource();
                fleetResource.setFleetId(fleetResource.getFleetId());
                fleetResource.setResourceType(entry.getKey());
                fleetResource.setUnits(0);
                fleetResources.add(fleetResource);
            } else {
                fleetResource = optionalFleetResource.get();
            }
            PlanetResource planetResource;
            Optional<PlanetResource> optionalPlanetResource = planetResources.stream().filter(f -> f.getResourceType().equals(entry.getKey())).findAny();
            if (optionalPlanetResource.isEmpty()) {
                planetResource = new PlanetResource();
                planetResource.setPlanetId(fleet.getPlanetId());
                planetResource.setResourceType(entry.getKey());
                planetResource.setUnits(0);
                planetResources.add(planetResource);
            } else {
                planetResource = optionalPlanetResource.get();
            }
            if (entry.getValue() > fleetResource.getUnits()) {
                long toLoad = Math.min(planetResource.getUnits(), entry.getValue() - fleetResource.getUnits());
                planetResource.setUnits(planetResource.getUnits() - toLoad);
                fleetResource.setUnits(fleetResource.getUnits() + toLoad);
            } else {
                long toUnload = fleetResource.getUnits() - entry.getValue();
                planetResource.setUnits(planetResource.getUnits() + toUnload);
                fleetResource.setUnits(fleetResource.getUnits() - toUnload);
            }
        }
        if (fleetResources.stream().anyMatch(f -> f.getUnits() < 0)) {
            throw new NotAffordableException("Cannot have negative resources in the fleet");
        }
        if (planetResources.stream().mapToLong(PlanetResource::getUnits).sum() > storage.getCapacitySupply()) {
            throw new NotAffordableException("Not enough space in planets warehouse");
        }
        long fleetStorage = fleetRepository.sumUpStorage(fleet.getFleetId());
        if (fleetStorage < fleetResources.stream().mapToLong(FleetResource::getUnits).sum()) {
            throw new NotAffordableException("Cannot store all the requested resources in the fleet");
        }
        fleetResourceRepository.saveAll(fleetResources);
        planetResourceRepository.saveAll(planetResources);
        return fleetResources;
    }

    public Fleet renameFleet(String accountId, String oldNickname, String newNickname) {
        Fleet fleet = fleetRepository.findByAccountIdAndNickname(accountId, oldNickname).orElseThrow(() -> new FleetNotExistsException(oldNickname));
        fleet.setNickname(newNickname);
        return fleetRepository.save(fleet);
    }
}
