package de.neebs.spacepeoples.control;

import de.neebs.spacepeoples.entity.ShipTypeCount;
import de.neebs.spacepeoples.integration.jpa.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class TypeConverter {
    private final ShipTypeRepository shipTypeRepository;

    private final PlanetRepository planetRepository;

    private final GalaxyRepository galaxyRepository;

    public de.neebs.spacepeoples.entity.Ship convert(Ship ship) {
        return convert(List.of(ship)).get(0);
        /*
        Optional<ShipType> optionalShipType = shipTypeRepository.findById(ship.getShipTypeId());
        if (optionalShipType.isEmpty()) {
            throw new IllegalStateException();
        }
        Optional<Planet> optionalPlanet = planetRepository.findById(ship.getPlanetId());
        if (optionalPlanet.isEmpty()) {
            throw new IllegalStateException();
        }
        Optional<Galaxy> optionalGalaxy = galaxyRepository.findById(optionalPlanet.get().getGalaxyId());
        if (optionalGalaxy.isEmpty()) {
            throw new IllegalStateException();
        }
        optionalPlanet.get().setGalaxyName(optionalGalaxy.get().getNickname());
        de.neebs.spacepeoples.entity.Ship s = new de.neebs.spacepeoples.entity.Ship();
        s.setShipType(optionalShipType.get().getNickname());
        s.setPlanetId(optionalPlanet.get().toWeb().getId());
        s.setFleetName(null);
        s.setReady(s.getReady());
        return s;

         */
    }

    public List<de.neebs.spacepeoples.entity.Ship> convert(List<Ship> ships) {
        List<String> shipTypeIds = ships.stream().map(Ship::getShipTypeId).distinct().collect(Collectors.toList());
        List<ShipType> shipTypes = StreamSupport.stream(shipTypeRepository.findAllById(shipTypeIds).spliterator(), false).collect(Collectors.toList());
        List<String> planetIds = ships.stream().map(Ship::getPlanetId).filter(Objects::nonNull).distinct().collect(Collectors.toList());
        List<Planet> planets = StreamSupport.stream(planetRepository.findAllById(planetIds).spliterator(), false).collect(Collectors.toList());
        List<String> galaxyIds = planets.stream().map(Planet::getGalaxyId).distinct().collect(Collectors.toList());
        List<Galaxy> galaxies = StreamSupport.stream(galaxyRepository.findAllById(galaxyIds).spliterator(), false).collect(Collectors.toList());
        for (Planet p : planets) {
            Optional<Galaxy> optionalGalaxy = galaxies.stream().filter(f -> f.getGalaxyId().equals(p.getGalaxyId())).findAny();
            if (optionalGalaxy.isEmpty()) {
                throw new IllegalStateException();
            }
            p.setGalaxyName(optionalGalaxy.get().getNickname());
        }

        List<de.neebs.spacepeoples.entity.Ship> list = new ArrayList<>();
        for (Ship ship : ships) {
            de.neebs.spacepeoples.entity.Ship s = new de.neebs.spacepeoples.entity.Ship();
            Optional<ShipType> optionalShipType = shipTypes.stream().filter(f -> f.getShipTypeId().equals(ship.getShipTypeId())).findAny();
            if (optionalShipType.isEmpty()) {
                throw new IllegalStateException();
            }
            s.setShipType(optionalShipType.get().getNickname());
            if (ship.getPlanetId() != null) {
                Optional<Planet> optionalPlanet = planets.stream().filter(f -> f.getPlanetId().equals(ship.getPlanetId())).findAny();
                if (optionalPlanet.isEmpty()) {
                    throw new IllegalStateException();
                }
                s.setPlanetId(optionalPlanet.get().toWeb().getId());
            }
            s.setFleetName(null);
            s.setReady(ship.getReady());
            list.add(s);
        }
        return list;
    }
}
