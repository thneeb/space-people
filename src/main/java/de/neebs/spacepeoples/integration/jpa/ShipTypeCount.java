package de.neebs.spacepeoples.integration.jpa;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(ShipTypeCountId.class)
public class ShipTypeCount {
    @Id
    private String fleetId;

    @Id
    private String nickname;

    private Long count;

    public de.neebs.spacepeoples.entity.ShipTypeCount toWeb() {
        de.neebs.spacepeoples.entity.ShipTypeCount stc = new de.neebs.spacepeoples.entity.ShipTypeCount();
        stc.setShipType(nickname);
        stc.setCount(count);
        return stc;
    }
}
