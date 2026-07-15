package ru.practicum.stats.server.mapper;

import org.springframework.stereotype.Component;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.server.config.ConfigUtil;
import ru.practicum.stats.server.model.EndpointHit;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class EndpointHitMapper {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(ConfigUtil.DATE);

    public EndpointHit toEntity(EndpointHitDto endpointHitDto) {
        if (endpointHitDto == null) {
            return null;
        }

        EndpointHit endpointHit = new EndpointHit();
        endpointHit.setApp(endpointHitDto.getApp());
        endpointHit.setUri(endpointHitDto.getUri());
        endpointHit.setIp(endpointHitDto.getIp());

        if (endpointHitDto.getTimestamp() != null) {
            endpointHit.setTimestamp(LocalDateTime.parse(endpointHitDto.getTimestamp(), FORMATTER));
        }

        return endpointHit;
    }
}