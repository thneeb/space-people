package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.jpa.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Service
@RequiredArgsConstructor
public class ShipService {
    private final PlanetResourceRepository planetResourceRepository;

    private final BuildingRepository buildingRepository;

    private final ResearchTypeRepository researchTypeRepository;

    private final ShipTypeRepository shipTypeRepository;

    private final ShipTypeEquipmentRepository shipTypeEquipmentRepository;

    private final ShipTypeResourceCostsRepository shipTypeResourceCostsRepository;

    private final ShipRepository shipRepository;

    public Ship createShip(String accountId, String shipTypeName, String planetId) {
        ShipType shipType = shipTypeRepository.findByAccountIdAndNickname(accountId, shipTypeName).orElseThrow(UnknownShipTypeException::new);
        if (shipType.getReady() != null) {
            throw new UnknownShipTypeException();
        }
        List<Building> buildings = buildingRepository.findByPlanetId(planetId);
        Building spaceShipFactory = buildings.stream().filter(f -> f.getBuildingType().equals(BuildingTypeEnum.SPACESHIP_FACTORY.name())).findAny().orElseThrow(() -> new BuildingNotAvailableException("We need a " + BuildingTypeEnum.SPACESHIP_FACTORY));
        if (spaceShipFactory.getLevel() == 0) {
            throw new BuildingNotAvailableException("We need a " + BuildingTypeEnum.SPACESHIP_FACTORY);
        }
        Optional<Ship> optionalShip = shipRepository.findByPlanetIdAndReadyIsNotNull(planetId);
        if (optionalShip.isPresent()) {
            if (optionalShip.get().getShipTypeId().equals(shipType.getShipTypeId())) {
                return optionalShip.get();
            } else {
                throw new FacilityBusyException(BuildingTypeEnum.SPACESHIP_FACTORY + " is busy");
            }
        }
        List<PlanetResource> planetResources = planetResourceRepository.findByPlanetId(planetId);
        List<ShipTypeResourceCosts> shipTypeResourceCosts = shipTypeResourceCostsRepository.findByShipTypeId(shipType.getShipTypeId());
        for (ShipTypeResourceCosts costs : shipTypeResourceCosts) {
            Optional<PlanetResource> optionalPlanetResource = planetResources.stream().filter(f -> f.getResourceType().equals(costs.getResourceType())).findAny();
            if (optionalPlanetResource.isEmpty()) {
                throw new NotAffordableException("No units of " + costs.getResourceType());
            }
            optionalPlanetResource.get().setUnits(optionalPlanetResource.get().getUnits() - costs.getUnits());
            if (optionalPlanetResource.get().getUnits() < 0) {
                throw new NotAffordableException("Too less units of " + costs.getResourceType());
            }
        }
        List<ResearchType> researchTypes = StreamSupport.stream(researchTypeRepository.findAll().spliterator(), false).collect(Collectors.toList());
        List<ShipTypeEquipment> shipTypeEquipments = shipTypeEquipmentRepository.findAllByShipTypeId(shipType.getShipTypeId());
        int time = 0;
        for (ShipTypeEquipment equipment : shipTypeEquipments) {
            Optional<ResearchType> optionalResearchType = researchTypes.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).findAny();
            if (optionalResearchType.isEmpty()) {
                throw new IllegalArgumentException();
            }
            time += optionalResearchType.get().getBuildingInSeconds() * Math.pow(optionalResearchType.get().getDurationLevelBase(), equipment.getLevel()) * Math.pow(optionalResearchType.get().getFacilityBase(), spaceShipFactory.getLevel());
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, time);

        Ship ship = new Ship();
        ship.setShipId(UUID.randomUUID().toString());
        ship.setShipTypeId(shipType.getShipTypeId());
        ship.setAccountId(accountId);
        ship.setFleetId(null);
        ship.setPlanetId(planetId);
        ship.setReady(calendar.getTime());
        return shipRepository.save(ship);
    }

    public List<Ship> retrieveShips(String accountId) {
        return shipRepository.findByAccountId(accountId);
    }
}
