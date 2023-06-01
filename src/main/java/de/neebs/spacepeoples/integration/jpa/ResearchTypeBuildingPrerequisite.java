package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "RESEARCH_TYPE_BUILDING_PREREQUISITE")
@IdClass(ResearchTypePrerequisiteId.class)
public class ResearchTypeBuildingPrerequisite {
    @Id
    @Column(name = "RESEARCH_TYPE")
    private String researchType;

    @Id
    @Column(name = "PREREQUISITE")
    private String prerequisite;

    @Column(name = "LEVEL")
    private int level;
}
