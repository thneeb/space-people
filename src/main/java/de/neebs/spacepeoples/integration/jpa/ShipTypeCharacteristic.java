package de.neebs.spacepeoples.integration.jpa;

import de.neebs.spacepeoples.entity.CharacteristicValue;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "SHIP_TYPE_CHARACTERISTIC")
@IdClass(ShipTypeCharacteristicId.class)
public class ShipTypeCharacteristic {
    @Id
    @Column(name = "SHIP_TYPE_ID")
    private String shipTypeId;

    @Id
    @Column(name = "CHARACTERISTIC")
    private String characteristic;

    @Column(name = "VALUE")
    private long value;

    public CharacteristicValue toWeb() {
        return new CharacteristicValue(de.neebs.spacepeoples.entity.Characteristic.valueOf(characteristic), value);
    }
}
