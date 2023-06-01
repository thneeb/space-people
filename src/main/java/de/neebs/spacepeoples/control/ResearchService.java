package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.jdbc.DatabaseService;
import de.neebs.spacepeoples.integration.jdbc.ResearchPrerequisite;
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
public class ResearchService {
    private final ResearchLevelRepository researchLevelRepository;

    private final PlanetRepository planetRepository;

    private final PlanetResourceRepository planetResourceRepository;

    private final BuildingRepository buildingRepository;

    private final ResearchTypeRepository researchTypeRepository;

    private final ResearchResourceResearchCostsRepository researchResourceResearchCostsRepository;

    private final ResearchResourceBuildingCostsRepository researchResourceBuildingCostsRepository;

    private final CharacteristicRepository characteristicRepository;

    private final ShipTypeRepository shipTypeRepository;

    private final ShipTypeEquipmentRepository shipTypeEquipmentRepository;

    private final ShipTypeCharacteristicRepository shipTypeCharacteristicRepository;

    private final ShipTypeResourceCostsRepository shipTypeResourceCostsRepository;

    private final DatabaseService databaseService;

    public List<ResearchLevel> retrieveResearchLevels(String accountId) {
        return StreamSupport.stream(researchLevelRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
    }

    public ResearchLevel upgradeResearch(String researchType, String accountId, String planetId) {
        Optional<ResearchType> optionalResearchType = researchTypeRepository.findById(researchType);
        if (optionalResearchType.isEmpty()) {
            throw new IllegalArgumentException();
        }
        Optional<Planet> optionalPlanet = planetRepository.findById(planetId);
        if (optionalPlanet.isEmpty()) {
            throw new PlanetNotAvailableException();
        }
        List<ResearchPrerequisite> researchPrerequisites = databaseService.findResearchPrerequisites(accountId, researchType);
        if (!researchPrerequisites.isEmpty()) {
            throw new ResearchPrerequisiteException(researchPrerequisites.toString());
        }
        researchPrerequisites = databaseService.findBuildingPrerequisites(accountId, researchType);
        if (!researchPrerequisites.isEmpty()) {
            throw new ResearchPrerequisiteException(researchPrerequisites.toString());
        }
        BuildingId buildingId = new BuildingId();
        buildingId.setBuildingType(BuildingTypeEnum.RESEARCH_FACILITY.name());
        buildingId.setPlanetId(planetId);
        Optional<Building> optionalResearchFacility = buildingRepository.findById(buildingId);
        if (optionalResearchFacility.isEmpty() || optionalResearchFacility.get().getLevel() == 0) {
            throw new BuildingNotAvailableException("Cannot do research without a research facility");
        }
        List<ResearchLevel> researchLevels = StreamSupport.stream(researchLevelRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
        for (ResearchLevel rl : researchLevels) {
            if (planetId.equals(rl.getPlanetId())) {
                throw new FacilityBusyException("Research Facility on planet is already busy");
            }
        }

        Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> f.getResourceType().equals(researchType)).findAny();
        ResearchLevel researchLevel;
        if (optionalResearchLevel.isEmpty()) {
            researchLevel = new ResearchLevel();
            researchLevel.setLevel(0);
            researchLevel.setResourceType(researchType);
            researchLevel.setAccountId(accountId);
        } else {
            researchLevel = optionalResearchLevel.get();
        }
        researchLevel.setPlanetId(planetId);

        List<PlanetResource> planetResources = StreamSupport.stream(planetResourceRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());
        List<ResearchResourceResearchCosts> researchResourceResearchCosts = StreamSupport.stream(researchResourceResearchCostsRepository.findByResearchType(researchType).spliterator(), false).collect(Collectors.toList());
        for (ResearchResourceResearchCosts costs : researchResourceResearchCosts) {
            Optional<PlanetResource> optionalPlanetResource = planetResources.stream().filter(f -> f.getResourceType().equals(costs.getResourceType())).findAny();
            if (optionalPlanetResource.isEmpty()) {
                throw new NotAffordableException("No " + costs.getResourceType() + " available on planet");
            }
            long units = Math.round(optionalPlanetResource.get().getUnits() - costs.getBasicValue() * Math.pow(costs.getBase(), researchLevel.getLevel() + costs.getExponentModifier()));
            if (units < 0) {
                throw new NotAffordableException("Not enough available of " + costs.getResourceType() + ". Would lead to " + units);
            }
            optionalPlanetResource.get().setUnits(units);
        }
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, (int)(optionalResearchType.get().getResearchInSeconds() * Math.pow(optionalResearchType.get().getDurationLevelBase(), researchLevel.getLevel()) * Math.pow(optionalResearchType.get().getFacilityBase(), optionalResearchFacility.get().getLevel())));
        researchLevel.setNextLevelUpdate(calendar.getTime());
        return researchLevelRepository.save(researchLevel);
    }

    public FullShipType evaluateShipType(String accountId, String planetId, boolean manned, List<ShipTypeEquipment> equipments) {
        List<ResearchLevel> researchLevels = StreamSupport.stream(researchLevelRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());

        return internalShipTypeCalculation(accountId, planetId, manned, equipments, researchLevels);
    }

    private FullShipType internalShipTypeCalculation(String accountId, String planetId, boolean manned, List<ShipTypeEquipment> equipments, List<ResearchLevel> researchLevels) {
        List<ResearchType> researchTypes = StreamSupport.stream(researchTypeRepository.findAll().spliterator(), false).collect(Collectors.toList());

        // check the mandatory parts of the spaceship type
        List<Characterstic> characteristics = StreamSupport.stream(characteristicRepository.findAll().spliterator(), false).collect(Collectors.toList());
        for (Characterstic characterstic : characteristics.stream().filter(Characterstic::isMandatory).collect(Collectors.toList())) {
            List<ResearchType> neededResearchTypes = researchTypes.stream().filter(f -> f.getCharacteristic().equals(characterstic.getCharacteristic())).collect(Collectors.toList());
            if (equipments.stream().noneMatch(f -> neededResearchTypes.stream().map(ResearchType::getResearchType).collect(Collectors.toList()).contains(f.getResearchType()))) {
                throw new ShipPartMissingException("No spaceship without " + characterstic.getCharacteristic());
            }
        }

        // research facility must not be occupied
        BuildingId buildingId = new BuildingId();
        buildingId.setBuildingType(BuildingTypeEnum.RESEARCH_FACILITY.name());
        buildingId.setPlanetId(planetId);
        Optional<Building> optionalResearchFacility = buildingRepository.findById(buildingId);
        if (optionalResearchFacility.isEmpty()) {
            throw new BuildingNotAvailableException("No ShipType research without a RESEARCH_FACILITY");
        }
        int researchFacilityLevel = optionalResearchFacility.get().getLevel();

        String shipTypeId = UUID.randomUUID().toString();
        int basicUnit = 0;
        for (ShipTypeEquipment equipment : equipments) {
            // BASIC_UNIT is not relevant for the calculation here
            if ("BASIC_UNIT".equals(equipment.getResearchType())) {
                continue;
            }
            // extract the shipPartType
            Optional<ResearchType> optionalShipPartType = researchTypes.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).findAny();
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
            Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> f.getResourceType().equals(equipment.getResearchType())).findAny();
            if (optionalResearchLevel.isEmpty() || optionalResearchLevel.get().getLevel() < level) {
                throw new ShipPartMissingException("The installed ship part must be researched at least to the used level");
            }
            // set missing attributes
            equipment.setShipTypeId(shipTypeId);
            // calculate the needed basic unit for the additional parts
            basicUnit += optionalShipPartType.get().getSpaceFix() + optionalShipPartType.get().getSpacePerLevel() * equipment.getLevel();
        }

        // Is BASIC_UNIT enough researched?
        Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> "BASIC_UNIT".equals(f.getResourceType())).findAny();
        if (optionalResearchLevel.isEmpty() || optionalResearchLevel.get().getLevel() == 0) {
            throw new ShipPartMissingException("BASIC_UNIT must be researched");
        }
        // calculate the needed basic unit and match it to the researched value
        basicUnit = (basicUnit + 8) / 9;
        if (optionalResearchLevel.get().getLevel() < basicUnit) {
            throw new ShipPartMissingException("BASIC_UNIT must be researched to an higher level");
        }
        // Add BASIC_UNIT to equipment or adjust level
        Optional<ShipTypeEquipment> optionalBasicUnit = equipments.stream().filter(f -> f.getResearchType().equals("BASIC_UNIT")).findAny();
        if (optionalBasicUnit.isPresent()) {
            optionalBasicUnit.get().setLevel(basicUnit);
        } else {
            ShipTypeEquipment equipment = new ShipTypeEquipment();
            equipment.setShipTypeId(shipTypeId);
            equipment.setResearchType("BASIC_UNIT");
            equipment.setLevel(basicUnit);
            equipments.add(equipment);
        }

        List<ShipTypeResourceCosts> shipTypeBuildingCosts = new ArrayList<>();
        List<ShipTypeResourceCosts> shipTypeResearchCosts = new ArrayList<>();
        List<ResearchResourceResearchCosts> researchCosts = StreamSupport.stream(researchResourceResearchCostsRepository.findAll().spliterator(), false).collect(Collectors.toList());
        List<ResearchResourceBuildingCosts> buildingCosts = StreamSupport.stream(researchResourceBuildingCostsRepository.findAll().spliterator(), false).collect(Collectors.toList());
        long researchTime = 0;
        long buildingTime = 0;
        for (ShipTypeEquipment equipment : equipments) {
            Optional<ResearchType> optionalResearchType = researchTypes.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).findAny();
            if (optionalResearchType.isEmpty()) {
                throw new IllegalStateException();
            }
            researchTime += Math.round(optionalResearchType.get().getResearchInSeconds() * Math.pow(optionalResearchType.get().getDurationLevelBase(), equipment.getLevel() * Math.pow(optionalResearchType.get().getFacilityBase(), researchFacilityLevel)));
            for (ResearchResourceResearchCosts c : researchCosts.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).collect(Collectors.toList())) {
                long units = Math.round(c.getBasicValue() * Math.pow(c.getBase(), equipment.getLevel() - c.getExponentModifier()));
                Optional<ShipTypeResourceCosts> optionalPlanetResource = shipTypeResearchCosts.stream().filter(f -> f.getResourceType().equals(c.getResourceType())).findAny();
                ShipTypeResourceCosts researchResourceCosts;
                if (optionalPlanetResource.isEmpty()) {
                    researchResourceCosts = new ShipTypeResourceCosts();
                    researchResourceCosts.setResourceType(c.getResourceType());
                    researchResourceCosts.setUnits(0);
                    shipTypeResearchCosts.add(researchResourceCosts);
                } else {
                    researchResourceCosts = optionalPlanetResource.get();
                }
                researchResourceCosts.setUnits(researchResourceCosts.getUnits() + units);
            }
            for (ResearchResourceBuildingCosts c : buildingCosts.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).collect(Collectors.toList())) {
                Optional<ShipTypeResourceCosts> optional = shipTypeBuildingCosts.stream().filter(f -> f.getResourceType().equals(c.getResourceType())).findAny();
                ShipTypeResourceCosts costs;
                if (optional.isPresent()) {
                    costs = optional.get();
                } else {
                    costs = new ShipTypeResourceCosts();
                    costs.setShipTypeId(shipTypeId);
                    costs.setResourceType(c.getResourceType());
                    costs.setUnits(0);
                    shipTypeBuildingCosts.add(costs);
                }
                costs.setUnits(costs.getUnits() + Math.round(c.getBasicValue() * Math.pow(c.getBase(), equipment.getLevel() + c.getExponentModifier())));
            }
        }

        long hydrogenConsumption = 0;
        long oxygenConsumption = 0;
        List<ShipTypeCharacteristic> shipTypeCharacteristics = new ArrayList<>();
        for (Characterstic characteristic : characteristics) {
            if (characteristic.getCharacteristic().equals("SPECIAL_UNIT")) {
                continue;
            }
            long value = 0;
            List<ResearchType> relevantResearchTypes = researchTypes.stream().filter(f -> f.getCharacteristic().equals(characteristic.getCharacteristic())).collect(Collectors.toList());
            for (ResearchType researchType : relevantResearchTypes) {
                Optional<ShipTypeEquipment> optionalEquipment = equipments.stream().filter(f -> f.getResearchType().equals(researchType.getResearchType())).findAny();
                if (optionalEquipment.isPresent()) {
                    long basicValue = researchType.getBenefitBasicValue();
                    if (characteristic.getCharacteristic().equals("BASIC_UNIT") && manned) {
                        basicValue *= 2;
                    }
                    value += basicValue * Math.pow(researchType.getBenefitBase(), optionalEquipment.get().getLevel() + researchType.getBenefitExponentModifier());
                    switch (researchType.getResearchType()) {
                        case "COMBUSTION_DRIVE":
                            oxygenConsumption += Math.round(1 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555));
                            hydrogenConsumption += Math.round(2 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555));
                            break;
                        case "FUSION_DRIVE":
                            hydrogenConsumption += Math.round(3 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555));
                            break;
                        case "TIME_WARP_DRIVE":
                            hydrogenConsumption += Math.round(2 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555));
                            break;
                    }
                }
            }
            ShipTypeCharacteristic stc = new ShipTypeCharacteristic();
            stc.setCharacteristic(characteristic.getCharacteristic());
            stc.setShipTypeId(shipTypeId);
            stc.setValue(value);
            shipTypeCharacteristics.add(stc);
        }

        Calendar calendar = GregorianCalendar.getInstance();
        calendar.add(Calendar.SECOND, (int)researchTime);
        Date ready = calendar.getTime();

        ShipType shipType = new ShipType();
        shipType.setShipTypeId(shipTypeId);
        shipType.setManned(manned);
        shipType.setAccountId(accountId);
        shipType.setResearchTimeInSeconds(researchTime);
        shipType.setBuildingTimeInSeconds(buildingTime);
        shipType.setOxygenConsumptionPerHour(oxygenConsumption);
        shipType.setHydrogenConsumptionPerHour(hydrogenConsumption);
        shipType.setPlanetId(planetId);
        shipType.setReady(ready);
        return new FullShipType(shipType, equipments, shipTypeCharacteristics, shipTypeBuildingCosts, shipTypeResearchCosts);
    }

    public FullShipType createShipType(String accountId, String planetId, String nickname, boolean manned, List<ShipTypeEquipment> equipments) {
        List<ResearchLevel> researchLevels = StreamSupport.stream(researchLevelRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());

        FullShipType fullShipType = internalShipTypeCalculation(accountId, planetId, manned, equipments, researchLevels);

        if (researchLevels.stream().anyMatch(f -> planetId.equals(f.getPlanetId()))) {
            throw new FacilityBusyException("RESEARCH_FACILITY is busy researching a new building block");
        }
        if (shipTypeRepository.findByPlanetId(planetId).isPresent()) {
            throw new FacilityBusyException("RESEARCH_FACILITY is busy researching another ship type");
        }

        List<PlanetResource> resources = StreamSupport.stream(planetResourceRepository.findByPlanetId(planetId).spliterator(), false).collect(Collectors.toList());

        for (ShipTypeResourceCosts c : fullShipType.getResearchResources()) {
            Optional<PlanetResource> optional = resources.stream().filter(f -> f.getResourceType().equals(c.getResourceType())).findAny();
            if (optional.isEmpty()) {
                throw new NotAffordableException("Too less " + c.getResourceType() + " resources: " + c.getUnits());
            }
            optional.get().setUnits(optional.get().getUnits() - c.getUnits());
            if (optional.get().getUnits() < 0) {
                throw new NotAffordableException("Too less " + c.getResourceType() + " resources: " + c.getUnits());
            }
        }

        fullShipType.getShipType().setNickname(nickname);
        shipTypeRepository.save(fullShipType.getShipType());
        shipTypeEquipmentRepository.saveAll(fullShipType.getEquipments());
        shipTypeCharacteristicRepository.saveAll(fullShipType.getCharacteristics());
        shipTypeResourceCostsRepository.saveAll(fullShipType.getBuildingResources());
        return fullShipType;
    }

    public List<FullShipType> retrieveShipTypes(String accountId) {
        List<ShipType> shipTypes = StreamSupport.stream(shipTypeRepository.findByAccountId(accountId).spliterator(), false).collect(Collectors.toList());
        List<String> shipTypeIds = shipTypes.stream().map(ShipType::getShipTypeId).collect(Collectors.toList());
        List<ShipTypeEquipment> shipTypeEquipment = StreamSupport.stream(shipTypeEquipmentRepository.findAllByShipTypeIdIn(shipTypeIds).spliterator(), false).collect(Collectors.toList());
        List<ShipTypeCharacteristic> shipTypeCharacteristics = StreamSupport.stream(shipTypeCharacteristicRepository.findByShipTypeIdIn(shipTypeIds).spliterator(), false).collect(Collectors.toList());
        List<ShipTypeResourceCosts> shipTypeResourceCosts = StreamSupport.stream(shipTypeResourceCostsRepository.findByShipTypeIdIn(shipTypeIds).spliterator(), false).collect(Collectors.toList());
        List<FullShipType> list = new ArrayList<>();
        for (ShipType shipType : shipTypes) {
            list.add(new FullShipType(shipType, shipTypeEquipment.stream().filter(f -> f.getShipTypeId().equals(shipType.getShipTypeId())).collect(Collectors.toList()), shipTypeCharacteristics, shipTypeResourceCosts, null));
        }
        return list;
    }
}
