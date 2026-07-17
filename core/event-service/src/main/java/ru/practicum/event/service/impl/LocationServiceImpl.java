package ru.practicum.event.service.impl;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.event.dto.AdminLocationDto;
import ru.practicum.event.dto.UpdateLocationDto;
import ru.practicum.event.exception.LocationNotExistException;
import ru.practicum.event.mapper.LocationMapper;
import ru.practicum.event.model.Location;
import ru.practicum.event.repository.LocationRepository;
import ru.practicum.event.service.LocationService;

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

	public Location getLocationModelById(Long locationId) {
		return locationRepository.findById(locationId)
				.orElseThrow(() -> new LocationNotExistException(
						String.format("Location with id=%s was not found", locationId)));
	}
}
