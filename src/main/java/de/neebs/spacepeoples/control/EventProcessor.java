package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.jdbc.DatabaseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventProcessor {
    private final DatabaseService databaseService;

    @Scheduled(fixedRate = 1000)
    public void produceResources() {
        int resources = databaseService.executeResourceProduction();
        if (resources > 0) {
            log.debug("Updated resources: " + resources);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void recycleResources() {
        int resources = databaseService.executeResourceRecycling();
        if (resources > 0) {
            log.debug("Recycled resources: " + resources);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void finishBuildings() {
        int finishedBuildings = databaseService.finishBuildings();
        if (finishedBuildings > 0) {
            log.debug("Finished buildings: " + finishedBuildings);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void finishResearches() {
        int finishedResearches = databaseService.finishResearches();
        if (finishedResearches > 0) {
            log.debug("Finished researches: " + finishedResearches);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void finishShipTypes() {
        int finished = databaseService.finishShipTypes();
        if (finished > 0) {
            log.debug("Finished shipTypes: " + finished);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void finishShips() {
        int finished = databaseService.finishShips();
        if (finished > 0) {
            log.debug("Finished ships: " + finished);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void finishFleetActions() {
        int finished = databaseService.finishFleetActions();
        if (finished > 0) {
            log.debug("Finished fleet actions: " + finished);
        }
    }
}
