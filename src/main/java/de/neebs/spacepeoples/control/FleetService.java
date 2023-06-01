package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.jdbc.DatabaseService;
import de.neebs.spacepeoples.integration.jpa.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class FleetService {
    private final FleetRepository fleetRepository;

    private final FleetFuelRepository fleetFuelRepository;

    private final ShipTypeRepository shipTypeRepository;

    private final PlanetResourceRepository planetResourceRepository;

    private final DatabaseService databaseService;

    public Fleet createFleet(String nickname, String accountId, String planetId, Map<String, Integer> shipTypeCounts) {
        List<ShipTypeAvailability> shipAvailabilities = StreamSupport.stream(shipTypeRepository.countShipsByShipType(accountId, planetId).spliterator(), false).collect(Collectors.toList());
        for (Map.Entry<String, Integer> entry : shipTypeCounts.entrySet()) {
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
        for (Map.Entry<String, Integer> entry : shipTypeCounts.entrySet()) {
            databaseService.assignShipsToFleet(fleet.getFleetId(), planetId, entry.getKey(), entry.getValue());
        }
        return fleet;
    }

    public List<FleetFuel> refuelFleet(String accountId, String nickname) {
        Optional<Fleet> optionalFleet = fleetRepository.findByAccountIdAndNickname(accountId, nickname);
        if (optionalFleet.isEmpty()) {
            throw new FleetNotExistsException(nickname);
        }
        if (!optionalFleet.get().getStatus().equals(FleetStatusEnum.DOCKED.name())) {
            throw new FleetStatusException("Fleet must be docked to refuel from planet resources");
        }
        String planetId = optionalFleet.get().getPlanetId();
        Fuel fuelConsumption = fleetRepository.sumUpFuelConsumptionPerHour(optionalFleet.get().getFleetId());
        Long capacity = fleetRepository.sumUpFuelCapacity(optionalFleet.get().getFleetId());
        List<FleetFuel> fuelAvailable = StreamSupport.stream(fleetFuelRepository.findByFleetId(optionalFleet.get().getFleetId()).spliterator(), false).collect(Collectors.toList());

        long wantedOxygen = capacity * fuelConsumption.getOxygen() / (fuelConsumption.getOxygen() + fuelConsumption.getHydrogen());
        FleetFuel oxygen;
        Optional<FleetFuel> optionalOxygen = fuelAvailable.stream().filter(f -> f.getResourceType().equals(ResourceTypeEnum.OXYGEN.name())).findAny();
        if (optionalOxygen.isPresent()) {
            oxygen = optionalOxygen.get();
        } else {
            oxygen = new FleetFuel();
            oxygen.setFleetId(optionalFleet.get().getFleetId());
            oxygen.setResourceType(ResourceTypeEnum.OXYGEN.name());
            oxygen.setUnits(0);
            fuelAvailable.add(oxygen);
        }
        long neededOxygen = wantedOxygen - oxygen.getUnits();

        long wantedHydrogen = capacity * fuelConsumption.getHydrogen() / (fuelConsumption.getOxygen() + fuelConsumption.getHydrogen());
        FleetFuel hydrogen;
        Optional<FleetFuel> optionalHydrogen = fuelAvailable.stream().filter(f -> f.getResourceType().equals(ResourceTypeEnum.HYDROGEN.name())).findAny();
        if (optionalHydrogen.isPresent()) {
            hydrogen = optionalHydrogen.get();
        } else {
            hydrogen = new FleetFuel();
            hydrogen.setFleetId(optionalFleet.get().getFleetId());
            hydrogen.setResourceType(ResourceTypeEnum.HYDROGEN.name());
            hydrogen.setUnits(0);
            fuelAvailable.add(hydrogen);
        }
        long neededHydrogen = wantedHydrogen - hydrogen.getUnits();

        if (neededOxygen > 0 || neededHydrogen > 0) {
            List<PlanetResource> planetResources = StreamSupport.stream(planetResourceRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());
            if (neededOxygen > 0) {
                Optional<PlanetResource> optionalPlanetOxygen = planetResources.stream().filter(f -> f.getResourceType().equals(ResourceTypeEnum.OXYGEN.name())).findAny();
                if (optionalPlanetOxygen.isPresent()) {
                    long fuel = Math.min(neededOxygen, optionalPlanetOxygen.get().getUnits());
                    optionalPlanetOxygen.get().setUnits(optionalPlanetOxygen.get().getUnits() - fuel);
                    oxygen.setUnits(oxygen.getUnits() + fuel);
                }
            }
            if (neededHydrogen > 0) {
                Optional<PlanetResource> optionalPlanetHydrogen = planetResources.stream().filter(f -> f.getResourceType().equals(ResourceTypeEnum.HYDROGEN.name())).findAny();
                if (optionalPlanetHydrogen.isPresent()) {
                    long fuel = Math.min(neededHydrogen, optionalPlanetHydrogen.get().getUnits());
                    optionalPlanetHydrogen.get().setUnits(optionalPlanetHydrogen.get().getUnits() - fuel);
                    hydrogen.setUnits(hydrogen.getUnits() + fuel);
                }
            }
            planetResourceRepository.saveAll(planetResources);
            fleetFuelRepository.saveAll(fuelAvailable);
        }
        return fuelAvailable;
    }

    public List<FleetFuel> retrieveFleetFuel(String accountId, String nickname) {
        Optional<Fleet> optional = fleetRepository.findByAccountIdAndNickname(accountId, nickname);
        if (optional.isEmpty()) {
            throw new FleetNotExistsException(nickname);
        }
        return StreamSupport.stream(fleetFuelRepository.findByFleetId(optional.get().getFleetId()).spliterator(), false).collect(Collectors.toList());
    }
}
