package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.integration.database.DatabaseService;
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
    public void finishBuildings() {
        int finishedBuildings = databaseService.finishBuildings();
        if (finishedBuildings > 0) {
            log.debug("Finished buildings: " + finishedBuildings);
        }
    }

    @Scheduled(fixedRate = 1000)
    public void finishResearch() {
        int finishedResearches = databaseService.finishResearches();
        if (finishedResearches > 0) {
            log.debug("Finished researches: " + finishedResearches);
        }
    }
}
