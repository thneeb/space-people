package de.neebs.spacepeoples.boundary.http;

import de.neebs.spacepeoples.control.*;
import de.neebs.spacepeoples.controller.http.DefaultApi;
import de.neebs.spacepeoples.entity.*;
import de.neebs.spacepeoples.entity.Building;
import de.neebs.spacepeoples.entity.BuildingType;
import de.neebs.spacepeoples.entity.Galaxy;
import de.neebs.spacepeoples.entity.Planet;
import de.neebs.spacepeoples.entity.ResearchLevel;
import de.neebs.spacepeoples.entity.ResourceType;
import de.neebs.spacepeoples.entity.ShipPartType;
import de.neebs.spacepeoples.entity.ShipType;
import de.neebs.spacepeoples.integration.database.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.linkTo;
import static org.springframework.hateoas.server.mvc.WebMvcLinkBuilder.methodOn;

@Slf4j
@RestController
@RequiredArgsConstructor
public class SpacePeoplesController implements DefaultApi {
    private final RegistrationService registrationService;

    private final AdminService adminService;

    private final AccountService accountService;

    private final UniverseService universeService;

    private final ShipService shipService;

    @Override
    public ResponseEntity<TokenBody> token() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ResponseEntity.ok(new TokenBody(registrationService.generateToken(authentication)));
    }

    @Override
    public ResponseEntity<TokenBody> createAccount(RegistrationRequest request) {
        Account account = registrationService.register(request);
        String accessToken = registrationService.generateToken(account);
        return ResponseEntity.ok(new TokenBody(accessToken));
    }

    @Override
    public ResponseEntity<Agent> retrieveAccount() {
        String accountId = getAccountId();
        return ResponseEntity.ok(accountService.retrieveAgent(accountId));
    }

    @Override
    public ResponseEntity<List<Planet>> retrieveMyPlanets() {
        String accountId = getAccountId();
        List<Planet> list = universeService.retrievePlanetsByAccountId(accountId).stream().map(de.neebs.spacepeoples.integration.database.Planet::toWeb).collect(Collectors.toList());
        return ResponseEntity.ok(list);
    }

    @Override
    public ResponseEntity<PlanetDetails> retrieveMyPlanetDetails(String planetId) {
        IdContainer idContainer = getIdContainer(planetId);
        Planet planet = universeService.retrievePlanet(idContainer.getPlanetId()).toWeb();
        List<Building> buildings = universeService.retrieveBuildings(idContainer.getPlanetId()).stream().map(de.neebs.spacepeoples.integration.database.Building::toWeb).collect(Collectors.toList());
        List<ResourceLevel> resources = universeService.retrieveResources(idContainer.getPlanetId()).stream().map(PlanetResource::toWeb).collect(Collectors.toList());
        List<CapacityLevel> capacities = universeService.retrievePlanetCapacities(idContainer.getPlanetId());
        PlanetDetails details = new PlanetDetails(planet, resources, buildings, capacities);
        return ResponseEntity.ok(details);
    }

    @Override
    public ResponseEntity<List<Planet>> retrievePlanets(String universeName) {
        List<Planet> planets = universeService.retrievePlanets(universeName).stream().map(de.neebs.spacepeoples.integration.database.Planet::toWeb).collect(Collectors.toList());
        return ResponseEntity.ok(planets);
    }

    @Override
    public ResponseEntity<List<Building>> retrievePlanetBuildings(String planetId) {
        IdContainer idContainer = getIdContainer(planetId);
        List<Building> buildings = universeService.retrieveBuildings(idContainer.getPlanetId()).stream().map(de.neebs.spacepeoples.integration.database.Building::toWeb).collect(Collectors.toList());
        return ResponseEntity.ok(buildings);
    }

    @Override
    public ResponseEntity<Building> retrieveBuilding(String planetId, BuildingType buildingType) {
        IdContainer idContainer = getIdContainer(planetId);
        Optional<de.neebs.spacepeoples.integration.database.Building> optional = universeService.retrieveBuildings(idContainer.getPlanetId()).stream().filter(f -> f.getBuildingType().equals(buildingType.name())).findAny();
        if (optional.isEmpty()) {
            throw new BuildingNotAvailableException("Requested building is not available at this planet.");
        }
        return ResponseEntity.ok(optional.get().toWeb());
    }

    @Override
    public ResponseEntity<Building> createBuilding(String planetId, CreateBuildingRequest request) {
        IdContainer idContainer = getIdContainer(planetId);
        Building building = universeService.upgradeBuilding(idContainer.getPlanetId(), BuildingTypeEnum.valueOf(request.getBuildingType().name())).toWeb();
        URI uri = linkTo(methodOn(getClass()).retrieveBuilding(planetId, request.getBuildingType())).withSelfRel().toUri();
        return ResponseEntity.created(uri).body(building);
    }

    @Override
    public ResponseEntity<Building> levelUpBuilding(String planetId, BuildingType buildingType) {
        IdContainer idContainer = getIdContainer(planetId);
        return ResponseEntity.ok(universeService.upgradeBuilding(idContainer.getPlanetId(), BuildingTypeEnum.valueOf(buildingType.name())).toWeb());
    }

    @Override
    public ResponseEntity<Building> cancelBuildingRequest(String planetId, BuildingType buildingType) {
        IdContainer idContainer = getIdContainer(planetId);
        return ResponseEntity.ok(universeService.cancelBuildingRequest(idContainer.getPlanetId(), BuildingTypeEnum.valueOf(buildingType.name())).toWeb());
    }

    @Override
    public ResponseEntity<List<ResourceLevel>> retrieveResources(String planetId) {
        IdContainer idContainer = getIdContainer(planetId);
        return ResponseEntity.ok(universeService.retrieveResources(idContainer.getPlanetId()).stream().map(PlanetResource::toWeb).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<ResourceLevel> discardResources(String planetId, ResourceType resourceType, Integer units) {
        IdContainer idContainer = getIdContainer(planetId);
        return ResponseEntity.ok(universeService.discardResources(idContainer.getPlanetId(), de.neebs.spacepeoples.integration.database.ResourceType.valueOf(resourceType.name()), units).toWeb());
    }

    @Override
    public ResponseEntity<List<ResourceLevel>> retrieveRecyclables(String planetId) {
        IdContainer idContainer = getIdContainer(planetId);
        return ResponseEntity.ok(universeService.retrieveRecyclables(idContainer.getPlanetId()).stream().map(PlanetRecycleResource::toWeb).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<ResourceLevel> discardRecyclables(String planetId, ResourceType resourceType, Integer units) {
        IdContainer idContainer = getIdContainer(planetId);
        return ResponseEntity.ok(universeService.discardRecyclables(idContainer.getPlanetId(), de.neebs.spacepeoples.integration.database.ResourceType.valueOf(resourceType.name()), units).toWeb());
    }

    @Override
    public ResponseEntity<List<CapacityLevel>> retrievePlanetCapacities(String planetId) {
        IdContainer idContainer = getIdContainer(planetId);
        return ResponseEntity.ok(universeService.retrievePlanetCapacities(idContainer.getPlanetId()));
    }

    @Override
    public ResponseEntity<List<ResearchLevel>> retrieveResearchLevels() {
        String accountId = getAccountId();
        return ResponseEntity.ok(shipService.retrieveResearchLevels(accountId).stream().map(de.neebs.spacepeoples.integration.database.ResearchLevel::toWeb).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<Void> startShipPartResearch(StartShipPartResearchRequest startShipPartResearchRequest) {
        IdContainer idContainer = getIdContainer(startShipPartResearchRequest.getPlanetId());
        shipService.upgradeShipPart(startShipPartResearchRequest.getShipPart().name(), idContainer.getPlanetId());
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<Void> levelUpShipPartResearch(ShipPartType shipPartType, LevelUpShipPartResearchRequest request) {
        IdContainer idContainer = getIdContainer(request.getPlanetId());
        shipService.upgradeShipPart(shipPartType.name(), idContainer.getPlanetId());
        return ResponseEntity.accepted().build();
    }

    @Override
    public ResponseEntity<List<ShipType>> retrieveShipTypes() {
        String accountId = getAccountId();
        return ResponseEntity.ok(shipService.retrieveShipTypes(accountId).stream().map(FullShipType::toWeb).collect(Collectors.toList()));
    }

    @Override
    public ResponseEntity<ShipType> createShipType(ShipType shipType) {
        String accountId = getAccountId();
        return ResponseEntity.ok(shipService.createShipType(accountId, shipType.getNickname(), shipType.getEquipments().stream().map(f -> new ShipTypeEquipment(f.getShipPart().name(), f.getLevel())).collect(Collectors.toList())).toWeb());
    }

    private IdContainer getIdContainer(String planetId) {
        String accountId = getAccountId();
        IdContainer idContainer = universeService.retrievePlanetIdContainer(planetId);
        if (!idContainer.getAccountId().equals(accountId)) {
            throw new PlanetNotAvailableException();
        }
        return idContainer;
    }

    @Override
    public ResponseEntity<Void> createGalaxy(Galaxy galaxy) {
        adminService.createGalaxy(galaxy.getNickname());
        URI uri = linkTo(methodOn(getClass()).retrievePlanets(galaxy.getNickname())).toUri();
        return ResponseEntity.created(uri).build();
    }

    @Override
    public ResponseEntity<List<BuildingType>> retrieveBuildingTypes() {
        return ResponseEntity.ok(Arrays.asList(BuildingType.values()));
    }

    private String getAccountId() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug(authentication.getAuthorities().toString());
        Jwt jwt = (Jwt)authentication.getPrincipal();
        log.debug(jwt.getClaims().toString());
        return jwt.getClaimAsString("account-id");
    }
}
