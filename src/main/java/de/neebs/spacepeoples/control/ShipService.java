package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.database.*;
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
    private final ResearchLevelRepository researchLevelRepository;

    private final PlanetRepository planetRepository;

    private final BuildingRepository buildingRepository;

    private final ShipPartTypeRepository shipPartTypeRepository;

    private final ShipTypeRepository shipTypeRepository;

    private final ShipTypeEquipmentRepository shipTypeEquipmentRepository;

    public List<ResearchLevel> retrieveResearchLevels(String accountId) {
        return StreamSupport.stream(researchLevelRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
    }

    public void upgradeShipPart(String shipPart, String planetId) {
        Optional<ShipPartType> optionalShipPartType = shipPartTypeRepository.findById(shipPart);
        if (optionalShipPartType.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Optional<Planet> optionalPlanet = planetRepository.findById(planetId);
        if (optionalPlanet.isEmpty()) {
            throw new PlanetNotAvailableException();
        }
        BuildingId buildingId = new BuildingId();
        buildingId.setBuildingType("SPACESHIP_RESEARCH_FACILITY");
        buildingId.setPlanetId(planetId);
        Optional<Building> optionalResearchFacility = buildingRepository.findById(buildingId);
        if (optionalResearchFacility.isEmpty() || optionalResearchFacility.get().getLevel() == 0) {
            throw new BuildingNotAvailableException("Cannot do research without a research facility");
        }
        List<ResearchLevel> researchLevels = StreamSupport.stream(researchLevelRepository.findByAccountId(optionalPlanet.get().getAccountId()).spliterator(), false).collect(Collectors.toList());
        for (ResearchLevel rl : researchLevels) {
            if (planetId.equals(rl.getPlanetId())) {
                throw new FacilityBusyException("Research Facility on planet is already busy");
            }
        }
        Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> f.getShipPartType().equals(shipPart)).findAny();
        ResearchLevel researchLevel;
        if (optionalResearchLevel.isEmpty()) {
            researchLevel = new ResearchLevel();
            researchLevel.setLevel(0);
            researchLevel.setShipPartType(shipPart);
            researchLevel.setAccountId(optionalPlanet.get().getAccountId());
        } else {
            researchLevel = optionalResearchLevel.get();
        }
        researchLevel.setPlanetId(planetId);
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, (int)(optionalShipPartType.get().getDurationInSeconds() * Math.pow(optionalShipPartType.get().getDurationBasis(), Math.max(researchLevel.getLevel() - optionalResearchFacility.get().getLevel(), 1))));
        researchLevel.setNextLevelUpdate(calendar.getTime());
        researchLevelRepository.save(researchLevel);
    }

    public FullShipType createShipType(String accountId, String nickname, List<ShipTypeEquipment> equipments) {
        // ships must have a fuel tank
        Optional<ShipTypeEquipment> optionalTank = equipments.stream().filter(f -> "FUEL_TANK".equals(f.getShipPartType())).findAny();
        if (optionalTank.isEmpty() || optionalTank.get().getLevel() < 1) {
            throw new ShipPartMissingException("No spaceship without FUEL_TANK");
        }
        // ships must have an engine
        Optional<ShipTypeEquipment> optionalEngine = equipments.stream().filter(f -> "ENGINE".equals(f.getShipPartType())).findAny();
        if (optionalEngine.isEmpty() || optionalEngine.get().getLevel() < 1) {
            throw new ShipPartMissingException("No spaceship without ENGINE");
        }
        String shipTypeId = UUID.randomUUID().toString();
        int basicUnit = 0;
        List<ResearchLevel> researchLevels = StreamSupport.stream(researchLevelRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
        List<ShipPartType> shipPartTypes = StreamSupport.stream(shipPartTypeRepository.findAll().spliterator(), false).collect(Collectors.toList());
        for (ShipTypeEquipment equipment : equipments) {
            // BASIC_UNIT is not relevant for the calculation here
            if ("BASIC_UNIT".equals(equipment.getShipPartType())) {
                continue;
            }
            // extract the shipPartType
            Optional<ShipPartType> optionalShipPartType = shipPartTypes.stream().filter(f -> f.getShipPartType().equals(equipment.getShipPartType())).findAny();
            if (optionalShipPartType.isEmpty()) {
                throw new IllegalStateException();
            }
            // if the used component has no per level wight we use 1 - these components are automatically upgraded and only count with a fix value
            int level;
            if (optionalShipPartType.get().getSpacePerLevel() == 0) {
                level = 1;
                equipment.setLevel(null);
            } else {
                level = equipment.getLevel();
            }
            // no negative levels are allowed
            if (level < 0) {
                throw new ShipPartMissingException("No negative levels of ship parts can be used");
            }
            // the researched level must at least hit the used level
            Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> f.getShipPartType().equals(equipment.getShipPartType())).findAny();
            if (optionalResearchLevel.isEmpty() || optionalResearchLevel.get().getLevel() < level) {
                throw new ShipPartMissingException("The installed ship part must be researched at least to the used level");
            }
            // set missing attributes
            equipment.setShipTypeId(shipTypeId);
            // calculate the needed basic unit for the additional parts
            basicUnit += optionalShipPartType.get().getSpaceFix() + optionalShipPartType.get().getSpacePerLevel() * equipment.getLevel();
        }
        Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> "BASIC_UNIT".equals(f.getShipPartType())).findAny();
        if (optionalResearchLevel.isEmpty() || optionalResearchLevel.get().getLevel() == 0) {
            throw new ShipPartMissingException("BASIC_UNIT must be researched");
        }
        // calculate the needed basic unit and match it to the researched value
        basicUnit = ((basicUnit + (basicUnit + 9) / 10 + 9) / 10) * 10;
        if (optionalResearchLevel.get().getLevel() * 10 < basicUnit) {
            throw new ShipPartMissingException("BASIC_UNIT must be researched to an higher level");
        }
        ShipType shipType = new ShipType();
        shipType.setShipTypeId(shipTypeId);
        shipType.setNickname(nickname);
        shipType.setAccountId(accountId);
        shipType.setStability(basicUnit);
        shipType.setAcceleration(0);
        shipType.setAttack(0);
        shipType.setCargoUnits(0);
        shipType.setHydrogenConsumptionPerHour(0);
        shipType.setArmour(0);
        shipType = shipTypeRepository.save(shipType);
        shipTypeEquipmentRepository.saveAll(equipments);
        return new FullShipType(shipType, equipments, new ArrayList<>());
    }

    public List<FullShipType> retrieveShipTypes(String accountId) {
        List<ShipType> shipTypes = StreamSupport.stream(shipTypeRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
        List<String> shipTypeIds = shipTypes.stream().map(ShipType::getShipTypeId).collect(Collectors.toList());
        List<ShipTypeEquipment> shipTypeEquipment = StreamSupport.stream(shipTypeEquipmentRepository.findAllByShipTypeIdIn(shipTypeIds).spliterator(), false).collect(Collectors.toList());
        List<FullShipType> list = new ArrayList<>();
        for (ShipType shipType : shipTypes) {
            list.add(new FullShipType(shipType, shipTypeEquipment.stream().filter(f -> f.getShipTypeId().equals(shipType.getShipTypeId())).collect(Collectors.toList()), new ArrayList<>()));
        }
        return list;
    }
}
