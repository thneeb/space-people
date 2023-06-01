package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@Entity
@Table(name = "SHIP_TYPE")
public class ShipType {
    @Id
    @Column(name = "SHIP_TYPE_ID")
    private String shipTypeId;

    @Column(name = "NICKNAME")
    private String nickname;

    @Column(name = "MANNED")
    @Convert(converter = BooleanConverter.class)
    private boolean manned;

    @Column(name = "ACCOUNT_ID")
    private String accountId;

    @Transient
    private Long researchTimeInSeconds;

    @Transient
    private Long buildingTimeInSeconds;

    @Column(name = "HYDROGEN_CONSUMPTION_PER_HOUR")
    private Long hydrogenConsumptionPerHour;

    @Column(name = "OXYGEN_CONSUMPTION_PER_HOUR")
    private Long oxygenConsumptionPerHour;

    @Column(name = "PLANET_ID")
    private String planetId;

    @Column(name = "READY")
    private Date ready;

    private static class BooleanConverter implements AttributeConverter<Boolean, Integer> {
        @Override
        public Integer convertToDatabaseColumn(Boolean aBoolean) {
            if (aBoolean == null) {
                return 0;
            } else {
                return aBoolean ? 1 : 0;
            }
        }

        @Override
        public Boolean convertToEntityAttribute(Integer integer) {
            if (integer == null) {
                return false;
            } else {
                return integer != 0;
            }
        }
    }
}
