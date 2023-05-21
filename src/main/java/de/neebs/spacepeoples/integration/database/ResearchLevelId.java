package de.neebs.spacepeoples.integration.database;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@EqualsAndHashCode
public class ResearchLevelId implements Serializable {
    private String accountId;
    private String shipPartType;
}
