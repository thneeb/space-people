package de.neebs.spacepeoples.integration.jpa;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@Entity
@Table(name = "CHARACTERISTIC")
public class Characterstic {
    @Id
    @Column(name = "CHARACTERISTIC")
    private String characteristic;

    @Column(name = "MANDATORY")
    @Convert(converter = Characterstic.BooleanConverter.class)
    private boolean mandatory;

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
            return integer != 0;
        }
    }
}
