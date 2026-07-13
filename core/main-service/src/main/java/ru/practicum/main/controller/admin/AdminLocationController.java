package ru.practicum.main.controller.admin;

import jakarta.validation.Valid;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.main.dto.location.AdminLocationDto;
import ru.practicum.main.dto.location.UpdateLocationDto;
import ru.practicum.main.service.LocationService;

import java.util.List;

@RestController
@RequestMapping("/admin/locations")
@RequiredArgsConstructor
@Validated
public class AdminLocationController {
    private final LocationService locationService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public AdminLocationDto createLocation(@RequestBody @Valid AdminLocationDto locationDto) {
        return locationService.createLocation(locationDto);
    }

    @GetMapping
    public List<AdminLocationDto> getLocations(@RequestParam(name = "from", defaultValue = "0") @PositiveOrZero Integer from,
                                               @RequestParam(name = "size", defaultValue = "10") @PositiveOrZero Integer size) {
        return locationService.getLocations(from, size);
    }

    @GetMapping("/{locationId}")
    public AdminLocationDto getLocation(@PathVariable Long locationId) {
        return locationService.getLocation(locationId);
    }

    @PatchMapping("/{locationId}")
    public AdminLocationDto updateLocation(@PathVariable Long locationId,
                                           @RequestBody @Valid UpdateLocationDto updateLocationDto) {
        return locationService.updateLocation(locationId, updateLocationDto);
    }

    @DeleteMapping("/{locationId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLocation(@PathVariable Long locationId) {
        locationService.deleteLocation(locationId);
    }
}