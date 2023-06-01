package de.neebs.spacepeoples.integration.jdbc;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ResearchPrerequisite {
    private String prerequisite;
    private int neededLevel;
    private int actualLevel;
}
