package ru.practicum.stats.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.model.ViewStats;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ViewStatsMapper {

    public ViewStatsDto toDto(ViewStats viewStats) {
        if (viewStats == null) {
            return null;
        }

        ViewStatsDto dto = new ViewStatsDto();
        dto.setApp(viewStats.getApp());
        dto.setUri(viewStats.getUri());
        dto.setHits(viewStats.getHits());

        return dto;
    }

    public List<ViewStatsDto> toDtoList(List<ViewStats> viewStats) {
        if (viewStats == null) {
            return null;
        }

        return viewStats.stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }
}