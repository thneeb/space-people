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

    public Ship createShip(String accountId, String shipType, String planetId) {
        Optional<ShipType> optionalShipType = shipTypeRepository.findByAccountIdAndNickname(accountId, shipType);
        if (optionalShipType.isEmpty()) {
            throw new UnknownShipTypeException();
        }
        if (optionalShipType.get().getReady() != null) {
            throw new UnknownShipTypeException();
        }
        List<Building> buildings = StreamSupport.stream(buildingRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());
        Optional<Building> spaceShipFactory = buildings.stream().filter(f -> f.getBuildingType().equals(BuildingTypeEnum.SPACESHIP_FACTORY.name())).findAny();
        if (spaceShipFactory.isEmpty() || spaceShipFactory.get().getLevel() == 0) {
            throw new BuildingNotAvailableException("We need a " + BuildingTypeEnum.SPACESHIP_FACTORY);
        }
        if (shipRepository.findByPlanetIdAndReadyIsNotNull(planetId).isPresent()) {
            throw new FacilityBusyException(BuildingTypeEnum.SPACESHIP_FACTORY + " is busy");
        }
        List<PlanetResource> planetResources = StreamSupport.stream(planetResourceRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());
        List<ShipTypeResourceCosts> shipTypeResourceCosts = StreamSupport.stream(shipTypeResourceCostsRepository.findByShipTypeId(optionalShipType.get().getShipTypeId()).spliterator(), false).collect(Collectors.toList());
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
        List<ShipTypeEquipment> shipTypeEquipments = StreamSupport.stream(shipTypeEquipmentRepository.findAllByShipTypeId(optionalShipType.get().getShipTypeId()).spliterator(), false).collect(Collectors.toList());
        int time = 0;
        for (ShipTypeEquipment equipment : shipTypeEquipments) {
            Optional<ResearchType> optionalResearchType = researchTypes.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).findAny();
            if (optionalResearchType.isEmpty()) {
                throw new IllegalArgumentException();
            }
            time += optionalResearchType.get().getBuildingInSeconds() * Math.pow(optionalResearchType.get().getDurationLevelBase(), equipment.getLevel()) * Math.pow(optionalResearchType.get().getFacilityBase(), spaceShipFactory.get().getLevel());
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, time);

        Ship ship = new Ship();
        ship.setShipId(UUID.randomUUID().toString());
        ship.setShipTypeId(optionalShipType.get().getShipTypeId());
        ship.setAccountId(accountId);
        ship.setFleetId(null);
        ship.setPlanetId(planetId);
        ship.setReady(calendar.getTime());
        return shipRepository.save(ship);
    }

    public List<Ship> retrieveShips(String accountId) {
        return StreamSupport.stream(shipRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
    }
}
