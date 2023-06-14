package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.ResearchType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "RESEARCH_LEVEL")
@IdClass(ResearchLevelId.class)
public class ResearchLevel {
    @Id
    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @Id
    @Column(name = "RESEARCH_TYPE")
    private String researchType;

    @Column(name = "LEVEL")
    private int level;

    @Column(name = "NEXT_LEVEL_UPDATE")
    private Date nextLevelUpdate;

    @Column(name = "PLANET_ID")
    private String planetId;

    public de.neebs.spacepeoples.entity.ResearchLevel toWeb() {
        de.neebs.spacepeoples.entity.ResearchLevel researchLevel = new de.neebs.spacepeoples.entity.ResearchLevel();
        researchLevel.setLevel(level);
        researchLevel.setResearchType(ResearchType.valueOf(researchType));
        researchLevel.setPlanetId(planetId);
        researchLevel.setNextLevelUpdate(nextLevelUpdate);
        return researchLevel;
    }
}
