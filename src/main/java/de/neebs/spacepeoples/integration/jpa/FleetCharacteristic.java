package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.CharacteristicValue;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@IdClass(FleetCharacteristicId.class)
public class FleetCharacteristic {
    @Id
    private String fleetId;

    @Id
    private String characteristic;

    private Long value;

    public CharacteristicValue toWeb() {
        return new CharacteristicValue(de.neebs.spacepeoples.entity.Characteristic.valueOf(characteristic), value);
    }
}
