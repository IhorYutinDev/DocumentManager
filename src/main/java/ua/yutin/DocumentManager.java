package ua.yutin;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class DocumentManager {

    private final Map<String, Document> storage = new HashMap<>();

    /**
     * Implementation of this method should upsert the document to your storage
     * And generate unique id if it does not exist, don't change [created] field
     *
     * @param document - document content and author data
     * @return saved document
     */
    public Document save(Document document) {
        if (document.getId() == null || document.getId().isEmpty()) {
            document.setId(generateId());
        }
        Document existing = storage.get(document.getId());
        if (existing != null) {
            document.setCreated(existing.getCreated());
        } else {
            document.setCreated(Instant.now());
        }

        storage.put(document.getId(), document);

        return document;
    }

    private String generateId() {
        return Stream.generate(() -> UUID.randomUUID().toString())
                .filter(id -> !storage.containsKey(id))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Unable to generate unique ID"));
    }

    /**
     * Implementation this method should find documents which match with request
     *
     * @param request - search request, each field could be null
     * @return list matched documents
     */
    public List<Document> search(SearchRequest request) {
        return storage.values().stream()
                .filter(doc -> matchesRequest(doc, request))
                .collect(Collectors.toList());
    }

    private boolean matchesRequest(Document document, SearchRequest request) {
        if (request == null) return true;

        if (request.getTitlePrefixes() != null &&
                request.getTitlePrefixes().stream().noneMatch(prefix -> document.getTitle().startsWith(prefix))) {
            return false;
        }

        if (request.getContainsContents() != null &&
                request.getContainsContents().stream().noneMatch(content -> document.getContent().contains(content))) {
            return false;
        }

        if (request.getAuthorIds() != null &&
                !request.getAuthorIds().contains(document.getAuthor().getId())) {
            return false;
        }

        if (request.getCreatedFrom() != null &&
                document.getCreated().isBefore(request.getCreatedFrom())) {
            return false;
        }

        if (request.getCreatedTo() != null &&
                document.getCreated().isAfter(request.getCreatedTo())) {
            return false;
        }

        return true;
    }

    /**
     * Implementation this method should find document by id
     *
     * @param id - document id
     * @return optional document
     */
    public Optional<Document> findById(String id) {
        return Optional.ofNullable(storage.get(id));
    }

    @Data
    @Builder
    public static class SearchRequest {
        private List<String> titlePrefixes;
        private List<String> containsContents;
        private List<String> authorIds;
        private Instant createdFrom;
        private Instant createdTo;
    }

    @Data
    @Builder
    public static class Document {
        private String id;
        private String title;
        private String content;
        private Author author;
        private Instant created;
    }

    @Data
    @Builder
    public static class Author {
        private String id;
        private String name;
    }
}
