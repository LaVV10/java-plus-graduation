package ru.practicum.event.service;

import ru.practicum.event.dto.AdminLocationDto;
import ru.practicum.event.dto.UpdateLocationDto;

import java.util.List;

public interface LocationService {
	AdminLocationDto createLocation(AdminLocationDto locationDto);

	List<AdminLocationDto> getLocations(Integer from, Integer size);

	AdminLocationDto getLocation(Long locationId);

	AdminLocationDto updateLocation(Long locationId, UpdateLocationDto updateLocationDto);

	void deleteLocation(Long locationId);
}
