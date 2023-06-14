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

    private final ShipTypeFuelConsumptionRepository shipTypeFuelConsumptionRepository;

    private final DatabaseService databaseService;

    public List<ResearchLevel> retrieveResearchLevels(String accountId) {
        return researchLevelRepository.findByAccountId(accountId);
    }

    public ResearchLevel upgradeResearch(String researchType, String accountId, String planetId) {
        List<ResearchLevel> researchLevels = researchLevelRepository.findByAccountId(accountId);
        for (ResearchLevel rl : researchLevels) {
            if (rl.getResearchType().equals(researchType) && planetId.equals(rl.getPlanetId())) {
                return rl;
            }
            if (planetId.equals(rl.getPlanetId())) {
                throw new FacilityBusyException("Research Facility on planet is already busy");
            }
        }

        ResearchType researchType1 = researchTypeRepository.findById(researchType).orElseThrow(IllegalArgumentException::new);
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
        Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> f.getResearchType().equals(researchType)).findAny();
        ResearchLevel researchLevel;
        if (optionalResearchLevel.isEmpty()) {
            researchLevel = new ResearchLevel();
            researchLevel.setLevel(0);
            researchLevel.setResearchType(researchType);
            researchLevel.setAccountId(accountId);
        } else {
            researchLevel = optionalResearchLevel.get();
        }
        researchLevel.setPlanetId(planetId);

        List<PlanetResource> planetResources = planetResourceRepository.findByPlanetId(planetId);
        List<ResearchResourceResearchCosts> researchResourceResearchCosts = researchResourceResearchCostsRepository.findByResearchType(researchType);
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
        calendar.add(Calendar.SECOND, (int)(researchType1.getResearchInSeconds() * Math.pow(researchType1.getDurationLevelBase(), researchLevel.getLevel()) * Math.pow(researchType1.getFacilityBase(), optionalResearchFacility.get().getLevel())));
        researchLevel.setNextLevelUpdate(calendar.getTime());
        return researchLevelRepository.save(researchLevel);
    }

    public FullShipType evaluateShipType(String accountId, String planetId, boolean manned, List<ShipTypeEquipment> equipments) {
        List<ResearchLevel> researchLevels = researchLevelRepository.findByAccountId(accountId);

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
        Building researchFacility = buildingRepository.findById(buildingId).orElseThrow(() -> new BuildingNotAvailableException("No ShipType research without a RESEARCH_FACILITY"));
        int researchFacilityLevel = researchFacility.getLevel();

        String shipTypeId = UUID.randomUUID().toString();
        int basicUnit = 0;
        for (ShipTypeEquipment equipment : equipments) {
            // BASIC_UNIT is not relevant for the calculation here
            if ("BASIC_UNIT".equals(equipment.getResearchType())) {
                continue;
            }
            // extract the shipPartType
            ResearchType researchType = researchTypes.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).findAny().orElseThrow(IllegalArgumentException::new);
            // if the used component has no per level wight we use 1 - these components are automatically upgraded and only count with a fix value
            int level;
            if (researchType.getSpacePerLevel() == 0) {
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
            ResearchLevel researchLevel = researchLevels.stream().filter(f -> f.getResearchType().equals(equipment.getResearchType())).findAny().orElseThrow(() -> new ShipPartMissingException(equipment.getResearchType() + " is not researched at all"));
            if (researchLevel.getLevel() < level) {
                throw new ShipPartMissingException(researchLevel.getResearchType() + " must be further researched " + level + "/" + researchLevel.getLevel());
            }
            // set missing attributes
            equipment.setShipTypeId(shipTypeId);
            // calculate the needed basic unit for the additional parts
            basicUnit += researchType.getSpaceFix() + researchType.getSpacePerLevel() * equipment.getLevel();
        }

        // Is BASIC_UNIT enough researched?
        Optional<ResearchLevel> optionalResearchLevel = researchLevels.stream().filter(f -> "BASIC_UNIT".equals(f.getResearchType())).findAny();
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

        ShipTypeFuelConsumption oxygen = new ShipTypeFuelConsumption(shipTypeId, ResourceTypeEnum.OXYGEN.name(), 0);
        ShipTypeFuelConsumption hydrogen = new ShipTypeFuelConsumption(shipTypeId, ResourceTypeEnum.HYDROGEN.name(), 0);
        List<ShipTypeFuelConsumption> consumptions = List.of(oxygen, hydrogen);
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
                            oxygen.setUnits(oxygen.getUnits() + Math.round(1 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555)));
                            hydrogen.setUnits(hydrogen.getUnits() + Math.round(2 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555)));
                            break;
                        case "FUSION_DRIVE":
                            hydrogen.setUnits(hydrogen.getUnits() + Math.round(3 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555)));
                            break;
                        case "TIME_WARP_DRIVE":
                            hydrogen.setUnits(hydrogen.getUnits() + Math.round(2 * Math.pow(1.11, optionalEquipment.get().getLevel() - 0.555)));
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
        shipType.setPlanetId(planetId);
        shipType.setReady(ready);
        return new FullShipType(shipType, equipments, shipTypeCharacteristics, shipTypeBuildingCosts, shipTypeResearchCosts, consumptions);
    }

    public FullShipType createShipType(String accountId, String planetId, String nickname, boolean manned, List<ShipTypeEquipment> equipments) {
        Optional<ShipType> optionalShipType = shipTypeRepository.findByAccountIdAndNickname(accountId, nickname);
        if (optionalShipType.isPresent()) {
            return internalRetrieveShipTypes(List.of(optionalShipType.get())).get(0);
        }
        List<ResearchLevel> researchLevels = researchLevelRepository.findByAccountId(accountId);

        FullShipType fullShipType = internalShipTypeCalculation(accountId, planetId, manned, equipments, researchLevels);

        if (researchLevels.stream().anyMatch(f -> planetId.equals(f.getPlanetId()))) {
            throw new FacilityBusyException("RESEARCH_FACILITY is busy researching a new building block");
        }
        if (shipTypeRepository.findByPlanetId(planetId).isPresent()) {
            throw new FacilityBusyException("RESEARCH_FACILITY is busy researching another ship type");
        }
        if (shipTypeRepository.findByAccountIdAndNickname(accountId, nickname).isPresent()) {
            throw new ShipTypeAlreadyExistsException();
        }

        List<PlanetResource> resources = planetResourceRepository.findByPlanetId(planetId);

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
        shipTypeFuelConsumptionRepository.saveAll(fullShipType.getFuelConsumptions());
        return fullShipType;
    }

    public List<FullShipType> retrieveShipTypes(String accountId) {
        List<ShipType> shipTypes = shipTypeRepository.findByAccountId(accountId);
        return internalRetrieveShipTypes(shipTypes);
    }

    private List<FullShipType> internalRetrieveShipTypes(List<ShipType> shipTypes) {
        List<String> shipTypeIds = shipTypes.stream().map(ShipType::getShipTypeId).collect(Collectors.toList());
        List<ShipTypeEquipment> shipTypeEquipment = shipTypeEquipmentRepository.findAllByShipTypeIdIn(shipTypeIds);
        List<ShipTypeCharacteristic> shipTypeCharacteristics = shipTypeCharacteristicRepository.findByShipTypeIdIn(shipTypeIds);
        List<ShipTypeResourceCosts> shipTypeResourceCosts = shipTypeResourceCostsRepository.findByShipTypeIdIn(shipTypeIds);
        List<ShipTypeFuelConsumption> shipTypeFuelConsumptions = shipTypeFuelConsumptionRepository.findByShipTypeIdIn(shipTypeIds);
        List<FullShipType> list = new ArrayList<>();
        for (ShipType shipType : shipTypes) {
            list.add(new FullShipType(shipType, shipTypeEquipment.stream().filter(f -> f.getShipTypeId().equals(shipType.getShipTypeId())).collect(Collectors.toList()),
                    shipTypeCharacteristics.stream().filter(f -> f.getShipTypeId().equals(shipType.getShipTypeId())).collect(Collectors.toList()),
                    shipTypeResourceCosts.stream().filter(f -> f.getShipTypeId().equals(shipType.getShipTypeId())).collect(Collectors.toList()),
                    null,
                    shipTypeFuelConsumptions.stream().filter(f -> f.getShipTypeId().equals(shipType.getShipTypeId())).collect(Collectors.toList())));
        }
        return list;
    }
}
