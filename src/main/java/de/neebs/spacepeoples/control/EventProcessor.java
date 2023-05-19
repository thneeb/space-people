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
    public void productResources() {
        int resources = databaseService.executeResourceProduction();
        log.debug("Updated resources: " + resources);
    }
}
