package com.yechan.fishing.fishing_api.domain.search.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostItem;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsRequest;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchPostsResponse;
import com.yechan.fishing.fishing_api.domain.search.dto.SearchRegionCountItem;
import com.yechan.fishing.fishing_api.global.external.opensearch.OpenSearchProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class SearchService {

    private final WebClient openSearchWebClient;
    private final OpenSearchProperties openSearchProperties;
    private final ObjectMapper objectMapper;

    public SearchService(
            @Qualifier("openSearchWebClient") WebClient openSearchWebClient,
            OpenSearchProperties openSearchProperties,
            ObjectMapper objectMapper
    ) {
        this.openSearchWebClient = openSearchWebClient;
        this.openSearchProperties = openSearchProperties;
        this.objectMapper = objectMapper;
    }

    public SearchPostsResponse searchPosts(SearchPostsRequest request) {
        int size = request.safeSize();

        Map<String, Object> body = buildQuery(request, size);

        JsonNode response = openSearchWebClient.post()
                .uri("/{index}/_search", openSearchProperties.getIndexName())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            return new SearchPostsResponse(List.of(), 0, size, null);
        }

        long total = response.path("hits").path("total").path("value").asLong(0L);
        JsonNode hits = response.path("hits").path("hits");
        List<SearchPostItem> items = new ArrayList<>();
        String nextCursor = null;

        if (hits.isArray()) {
            for (JsonNode hit : hits) {
                items.add(toItem(hit.path("_source")));
            }
            if (hits.size() == size) {
                JsonNode lastSort = hits.get(hits.size() - 1).path("sort");
                nextCursor = encodeCursor(lastSort);
            }
        }

        return new SearchPostsResponse(items, total, size, nextCursor);
    }

    public List<SearchRegionCountItem> getRegionCounts(LocalDate fromDate, LocalDate untilDate) {
        List<Object> filters = new ArrayList<>();
        filters.add(Map.of("term", Map.of("access_status", "ok")));
        filters.add(Map.of("exists", Map.of("field", "region")));
        addPublishedAtRangeFilter(filters, fromDate, untilDate);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", 0);
        body.put("query", Map.of(
                "bool",
                Map.of("filter", filters)
        ));
        body.put("aggs", Map.of(
                "regions",
                Map.of(
                        "terms",
                        Map.of(
                                "field", "region",
                                "size", 20,
                                "order", Map.of("_count", "desc")
                        )
                )
        ));

        JsonNode response = openSearchWebClient.post()
                .uri("/{index}/_search", openSearchProperties.getIndexName())
                .bodyValue(body)
                .retrieve()
                .bodyToMono(JsonNode.class)
                .block();

        if (response == null) {
            return List.of();
        }

        JsonNode buckets = response.path("aggregations").path("regions").path("buckets");
        List<SearchRegionCountItem> items = new ArrayList<>();

        if (buckets.isArray()) {
            for (JsonNode bucket : buckets) {
                String region = bucket.path("key").asText(null);
                long count = bucket.path("doc_count").asLong(0L);

                if (StringUtils.hasText(region) && count > 0) {
                    items.add(new SearchRegionCountItem(region, count));
                }
            }
        }

        return items;
    }

    private Map<String, Object> buildQuery(SearchPostsRequest request, int size) {
        List<Object> must = new ArrayList<>();
        List<Object> filter = new ArrayList<>();

        filter.add(Map.of("term", Map.of("access_status", "ok")));

        if (StringUtils.hasText(request.q())) {
            must.add(Map.of(
                    "multi_match",
                    Map.of(
                            "query", request.q().trim(),
                            "fields", List.of("title^3", "body_text", "place^2", "region", "species", "board_name"),
                            "type", "best_fields"
                    )
            ));
        }

        addTermFilter(filter, "board_key", request.boardKey());
        addTermFilter(filter, "region", request.region());
        addPublishedAtRangeFilter(filter, request.fromDate(), request.untilDate());

        Map<String, Object> bool = new LinkedHashMap<>();
        if (!must.isEmpty()) {
            bool.put("must", must);
        }
        bool.put("filter", filter);

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("size", size);
        body.put("sort", List.of(
                Map.of("published_at", Map.of("order", "desc", "missing", "_last")),
                Map.of("article_id", Map.of("order", "desc"))
        ));
        body.put("_source", List.of(
                "article_id", "title", "url", "author_name", "published_at", "date_text",
                "board_key", "board_name", "species", "region", "place", "access_status"
        ));
        body.put("query", Map.of("bool", bool));
        List<Object> searchAfter = decodeCursor(request.cursor());
        if (searchAfter != null) {
            body.put("search_after", searchAfter);
        }
        return body;
    }

    private void addTermFilter(List<Object> filters, String field, String value) {
        if (!StringUtils.hasText(value)) {
            return;
        }
        filters.add(Map.of("term", Map.of(field, value.trim())));
    }

    private void addPublishedAtRangeFilter(List<Object> filters, LocalDate fromDate, LocalDate untilDate) {
        if (fromDate == null && untilDate == null) {
            return;
        }

        Map<String, Object> range = new LinkedHashMap<>();
        if (fromDate != null) {
            range.put("gte", fromDate.toString());
        }
        if (untilDate != null) {
            range.put("lte", untilDate.toString());
        }
        filters.add(Map.of("range", Map.of("published_at", range)));
    }

    private SearchPostItem toItem(JsonNode source) {
        String species = textOrNull(source, "species");
        String region = textOrNull(source, "region");
        String place = textOrNull(source, "place");

        List<String> tags = new ArrayList<>();
        if (species != null) {
            tags.add(species);
        }
        if (region != null) {
            tags.add(region);
        }
        if (place != null) {
            tags.add(place);
        }

        return new SearchPostItem(
                textOrNull(source, "article_id"),
                textOrNull(source, "title"),
                textOrNull(source, "url"),
                textOrNull(source, "author_name"),
                firstNonBlank(textOrNull(source, "published_at"), textOrNull(source, "date_text")),
                textOrNull(source, "board_key"),
                textOrNull(source, "board_name"),
                species,
                region,
                place,
                textOrNull(source, "access_status"),
                tags
        );
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (value.isMissingNode() || value.isNull()) {
            return null;
        }

        String text = value.asText();
        return StringUtils.hasText(text) ? text : null;
    }

    private String firstNonBlank(String first, String second) {
        if (StringUtils.hasText(first)) {
            return first;
        }
        return StringUtils.hasText(second) ? second : null;
    }

    private String encodeCursor(JsonNode sortValues) {
        if (!sortValues.isArray() || sortValues.isEmpty()) {
            return null;
        }

        try {
            List<Object> values = new ArrayList<>();
            for (JsonNode node : sortValues) {
                if (node.isNull()) {
                    values.add(null);
                } else if (node.isNumber()) {
                    values.add(node.numberValue());
                } else if (node.isBoolean()) {
                    values.add(node.booleanValue());
                } else {
                    values.add(node.asText());
                }
            }
            String json = objectMapper.writeValueAsString(values);
            return Base64.getUrlEncoder().encodeToString(json.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            return null;
        }
    }

    private List<Object> decodeCursor(String cursor) {
        if (!StringUtils.hasText(cursor)) {
            return null;
        }

        try {
            byte[] decoded = Base64.getUrlDecoder().decode(cursor);
            List<?> values = objectMapper.readValue(decoded, List.class);
            return new ArrayList<>(values);
        } catch (Exception exception) {
            return null;
        }
    }
}
