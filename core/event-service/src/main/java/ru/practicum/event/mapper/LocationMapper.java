package ru.practicum.event.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.common.dto.LocationDto;
import ru.practicum.event.dto.AdminLocationDto;
import ru.practicum.event.model.Location;

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

	public Location toLocationModel(LocationDto locationDto) {
		if (locationDto == null) {
			return null;
		}
		Location location = new Location();
		location.setLat(locationDto.getLat());
		location.setLon(locationDto.getLon());
		return location;
	}
}
