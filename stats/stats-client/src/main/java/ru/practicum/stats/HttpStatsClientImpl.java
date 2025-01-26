package ru.practicum.stats;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import ru.practicum.stats.dto.StatResponseDto;

import java.util.List;
import java.util.Optional;

import static ru.practicum.stats.constants.DataTransferConvention.HIT_PATH;
import static ru.practicum.stats.constants.DataTransferConvention.STATS_PATH;

@RequiredArgsConstructor
public class HttpStatsClientImpl implements HttpStatsClient {

    private final RestTemplate restTemplate;
    private final DiscoveryClient discoveryClient;

    public List<StatResponseDto> getStats(String start, String end, List<String> uris, Boolean unique) {
        String statsServerUrl = discoveryClient.getInstances("stats-server")
            .stream()
            .findFirst()
            .map(instance -> instance.getUri().toString())
            .orElseThrow(() -> new IllegalStateException("Stats server is unavailable"));
        String url = UriComponentsBuilder
            .fromHttpUrl(statsServerUrl)
            .path(STATS_PATH)
            .queryParam("start", start)
            .queryParam("end", end)
            .queryParam("uris", String.join(",", uris))
            .queryParam("unique", unique)
            .build()
            .toUriString();

        ResponseEntity<List<StatResponseDto>> responseEntity =
            restTemplate.exchange(url, HttpMethod.GET, null,
                new ParameterizedTypeReference<>() {
                });

        return responseEntity.getBody();
    }


    @Override
    public <R> Optional<R> getStats(StatsParameters<R> param) {
        String statsServerUrl = discoveryClient.getInstances("stats-server")
            .stream()
            .findFirst()
            .map(instance -> instance.getUri().toString())
            .orElseThrow(() -> new IllegalStateException("Stats server is unavailable"));
        return Optional.ofNullable(restTemplate.getForObject(
            UriComponentsBuilder
                .fromHttpUrl(statsServerUrl)
                .path(STATS_PATH)
                .queryParam("start", param.getStart())
                .queryParam("end", param.getEnd())
                .queryParam("uris", param.getUris())
                .queryParam("unique", param.isUnique())
                .build().toUri(), param.getResponseType()));
    }

    @Override
    public <T, R> Optional<R> sendHit(T hit, Class<R> responseType) {
        String statsServerUrl = discoveryClient.getInstances("stats-server")
            .stream()
            .findFirst()
            .map(instance -> instance.getUri().toString())
            .orElseThrow(() -> new IllegalStateException("Stats server is unavailable"));
        return Optional.ofNullable(restTemplate.postForObject(
            UriComponentsBuilder
                .fromHttpUrl(statsServerUrl)
                .path(HIT_PATH)
                .build().toUri(), hit, responseType));
    }
}