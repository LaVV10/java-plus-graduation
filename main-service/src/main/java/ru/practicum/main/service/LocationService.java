package ru.practicum.main.service;

import ru.practicum.main.dto.location.AdminLocationDto;
import ru.practicum.main.dto.location.UpdateLocationDto;
import ru.practicum.main.model.Location;

import java.util.List;

public interface LocationService {
    AdminLocationDto createLocation(AdminLocationDto locationDto);

    List<AdminLocationDto> getLocations(Integer from, Integer size);

    AdminLocationDto getLocation(Long locationId);

    AdminLocationDto updateLocation(Long locationId, UpdateLocationDto updateLocationDto);

    void deleteLocation(Long locationId);

    Location getLocationModelById(Long locationId);
}