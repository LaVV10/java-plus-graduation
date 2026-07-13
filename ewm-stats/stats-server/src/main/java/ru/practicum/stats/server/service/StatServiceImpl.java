package ru.practicum.stats.server.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;
import ru.practicum.stats.server.exception.WrongTimeException;
import ru.practicum.stats.server.mapper.EndpointHitMapper;
import ru.practicum.stats.server.mapper.ViewStatsMapper;
import ru.practicum.stats.server.model.ViewStats;
import ru.practicum.stats.server.repository.StatServerRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class StatServiceImpl implements StatService {
    private final StatServerRepository statServerRepository;
    private final EndpointHitMapper endpointHitMapper;
    private final ViewStatsMapper viewStatsMapper;

    @Override
    public void saveHit(EndpointHitDto endpointHitDto) {
        log.debug("Save hit by app: {}", endpointHitDto.getApp());
        statServerRepository.save(endpointHitMapper.toEntity(endpointHitDto));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        checkDateTime(start, end);
        log.debug("Received stats.");
        List<ViewStats> viewStats;
        List<ViewStatsDto> response;
        if (unique) {
            viewStats = uris == null
                    ? statServerRepository.getUniqueViewStatsByStartAndEndTime(start, end)
                    : statServerRepository.getUniqueUrisViewStatsByStartAndEndTime(start, end, uris);
        } else {
            viewStats = uris == null ? statServerRepository.getViewStatsByStartAndEndTime(start, end)
                    : statServerRepository.getUrisViewStatsByStartAndEndTime(start, end, uris);
        }

        response = viewStatsMapper.toDtoList(viewStats);
        return response;
    }

    private void checkDateTime(LocalDateTime start, LocalDateTime end) {
        if (start.isAfter(end)) {
            throw new WrongTimeException("Wrong date and time");
        }
    }
}