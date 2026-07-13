package ru.practicum.main.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.main.dto.location.AdminLocationDto;
import ru.practicum.main.dto.location.UpdateLocationDto;
import ru.practicum.main.exception.LocationNotExistException;
import ru.practicum.main.mapper.LocationMapper;
import ru.practicum.main.model.Location;
import ru.practicum.main.repository.LocationRepository;
import ru.practicum.main.service.LocationService;

import java.util.List;

@Service
@RequiredArgsConstructor
public class LocationServiceImpl implements LocationService {
    private final LocationRepository locationRepository;
    private final LocationMapper locationMapper;

    @Override
    @Transactional
    public AdminLocationDto createLocation(AdminLocationDto locationDto) {
        Location location = locationMapper.toLocationModel(locationDto);
        return locationMapper.toAdminLocationDto(locationRepository.save(location));
    }

    @Override
    public List<AdminLocationDto> getLocations(Integer from, Integer size) {
        return locationRepository.findAll(PageRequest.of(from / size, size))
                .stream()
                .map(locationMapper::toAdminLocationDto)
                .toList();
    }

    @Override
    public AdminLocationDto getLocation(Long locationId) {
        return locationMapper.toAdminLocationDto(getLocationModelById(locationId));
    }

    @Override
    @Transactional
    public AdminLocationDto updateLocation(Long locationId, UpdateLocationDto updateLocationDto) {
        Location location = getLocationModelById(locationId);
        if (updateLocationDto.getLat() != null) {
            location.setLat(updateLocationDto.getLat());
        }
        if (updateLocationDto.getLon() != null) {
            location.setLon(updateLocationDto.getLon());
        }
        return locationMapper.toAdminLocationDto(locationRepository.save(location));
    }

    @Override
    @Transactional
    public void deleteLocation(Long locationId) {
        Location location = getLocationModelById(locationId);
        locationRepository.delete(location);
    }

    @Override
    public Location getLocationModelById(Long locationId) {
        return locationRepository.findById(locationId)
                .orElseThrow(() -> new LocationNotExistException(
                        String.format("Location with id=%s was not found", locationId)));
    }
}