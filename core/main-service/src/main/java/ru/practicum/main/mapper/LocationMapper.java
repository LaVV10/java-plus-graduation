package ru.practicum.main.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.main.dto.location.AdminLocationDto;
import ru.practicum.main.dto.location.LocationDto;
import ru.practicum.main.model.Location;

@Component
public class LocationMapper {

    public LocationDto toLocationDto(Location location) {
        if (location == null) {
            return null;
        }

        LocationDto dto = new LocationDto();
        dto.setLat(location.getLat());
        dto.setLon(location.getLon());
        return dto;
    }

    public AdminLocationDto toAdminLocationDto(Location location) {
        if (location == null) {
            return null;
        }
        return new AdminLocationDto(location.getId(), location.getLat(), location.getLon());
    }

    public Location toLocationModel(AdminLocationDto locationDto) {
        if (locationDto == null) {
            return null;
        }
        return new Location(locationDto.getId(), locationDto.getLat(), locationDto.getLon());
    }
}