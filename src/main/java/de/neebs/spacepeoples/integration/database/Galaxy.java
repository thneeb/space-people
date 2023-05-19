package de.neebs.spacepeoples.integration.database;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Getter
@Setter
@Entity
public class Galaxy {
    @Id
    @Column(name = "GALAXY_ID")
    private String galaxyId;

    @Column(name = "NICKNAME")
    private String nickname;

    public de.neebs.spacepeoples.entity.Universe toWeb() {
        return new de.neebs.spacepeoples.entity.Universe(nickname);
    }
}
