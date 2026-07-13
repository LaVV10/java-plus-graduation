package ru.practicum.stats.client;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.stats.dto.EndpointHitDto;
import ru.practicum.stats.dto.ViewStatsDto;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP-клиент сервиса статистики.
 * <p>
 * Адрес stats-server определяется через службу обнаружения (Eureka):
 * вместо фиксированного хоста используется имя зарегистрированного сервиса
 * {@code stats-server}, которое резолвится балансировщиком благодаря
 * {@code @LoadBalanced}-аннотации на бине {@link RestTemplate} в main-service.
 */
@Service
public class StatsClient {
    /**
     * Имя сервиса статистики в реестре Eureka.
     */
    private static final String STATS_SERVER_SERVICE_ID = "stats-server";

    private final String serverUrl;
    private final RestTemplate restTemplate;

    public StatsClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        this.serverUrl = "http://" + STATS_SERVER_SERVICE_ID;
    }

    public void addStats(EndpointHitDto endpointHitDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<EndpointHitDto> requestEntity = new HttpEntity<>(endpointHitDto, headers);
        restTemplate.exchange(serverUrl + "/hit", HttpMethod.POST, requestEntity, EndpointHitDto.class);
    }

    public List<ViewStatsDto> getStats(String start, String end, List<String> uris, Boolean unique) {

        Map<String, Object> parameters = new HashMap<>();

        parameters.put("start", start);
        parameters.put("end", end);
        parameters.put("uris", uris);
        parameters.put("unique", unique);
        ResponseEntity<String> response = restTemplate.getForEntity(
                serverUrl + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                String.class, parameters);

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return Arrays.asList(objectMapper.readValue(response.getBody(), ViewStatsDto[].class));
        } catch (JsonProcessingException exception) {
            throw new RuntimeException(String.format("Json processing error: %s", exception.getMessage()));
        }
    }
}
