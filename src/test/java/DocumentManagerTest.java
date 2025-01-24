import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ua.yutin.DocumentManager;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class DocumentManagerTest {

    private DocumentManager documentManager;

    @BeforeEach
    void setUp() {
        documentManager = new DocumentManager();
    }

    @Test
    void testSaveNewDocument() {
        DocumentManager.Author author = DocumentManager.Author.builder()
                .id("author1")
                .name("John Doe")
                .build();

        DocumentManager.Document document = DocumentManager.Document.builder()
                .title("Test Title")
                .content("Test Content")
                .author(author)
                .build();

        DocumentManager.Document savedDocument = documentManager.save(document);

        assertNotNull(savedDocument.getId(), "Document ID should be generated");
        assertNotNull(savedDocument.getCreated(), "Created timestamp should be set");
        assertEquals("Test Title", savedDocument.getTitle(), "Title should be saved correctly");
        assertEquals("Test Content", savedDocument.getContent(), "Content should be saved correctly");
    }

    @Test
    void testSaveExistingDocument() {
        DocumentManager.Author author = DocumentManager.Author.builder()
                .id("author1")
                .name("John Doe")
                .build();

        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("doc1")
                .title("Test Title")
                .content("Test Content")
                .author(author)
                .created(Instant.now())
                .build();

        documentManager.save(document);

        DocumentManager.Document updatedDocument = DocumentManager.Document.builder()
                .id("doc1")
                .title("Updated Title")
                .content("Updated Content")
                .author(author)
                .build();

        DocumentManager.Document savedDocument = documentManager.save(updatedDocument);

        assertEquals("doc1", savedDocument.getId(), "ID should remain the same");
        assertEquals(document.getCreated(), savedDocument.getCreated(), "Created timestamp should not change");
        assertEquals("Updated Title", savedDocument.getTitle(), "Title should be updated");
        assertEquals("Updated Content", savedDocument.getContent(), "Content should be updated");
    }

    @Test
    void testFindById() {
        DocumentManager.Document document = DocumentManager.Document.builder()
                .id("doc1")
                .title("Test Title")
                .content("Test Content")
                .author(DocumentManager.Author.builder().id("author1").name("John Doe").build())
                .created(Instant.now())
                .build();

        documentManager.save(document);

        Optional<DocumentManager.Document> foundDocument = documentManager.findById("doc1");

        assertTrue(foundDocument.isPresent(), "Document should be found by ID");
        assertEquals("Test Title", foundDocument.get().getTitle(), "Title should match the saved document");
    }

    @Test
    void testFindByIdNotFound() {
        Optional<DocumentManager.Document> foundDocument = documentManager.findById("nonexistent");

        assertFalse(foundDocument.isPresent(), "Document should not be found for nonexistent ID");
    }

    @Test
    void testSearch() {
        DocumentManager.Author author1 = DocumentManager.Author.builder()
                .id("author1")
                .name("John Doe")
                .build();

        DocumentManager.Author author2 = DocumentManager.Author.builder()
                .id("author2")
                .name("Jane Doe")
                .build();

        documentManager.save(DocumentManager.Document.builder()
                .title("Java Basics")
                .content("Learn Java programming")
                .author(author1)
                .created(Instant.now())
                .build());

        documentManager.save(DocumentManager.Document.builder()
                .title("Spring Framework")
                .content("Build applications with Spring")
                .author(author2)
                .created(Instant.now())
                .build());

        DocumentManager.SearchRequest searchRequest = DocumentManager.SearchRequest.builder()
                .titlePrefixes(List.of("Java"))
                .authorIds(List.of("author1"))
                .build();

        List<DocumentManager.Document> results = documentManager.search(searchRequest);

        assertEquals(1, results.size(), "Only one document should match the search criteria");
        assertEquals("Java Basics", results.get(0).getTitle(), "Title should match the search result");
    }

    @Test
    void testSearchNoCriteria() {
        documentManager.save(DocumentManager.Document.builder()
                .title("Java Basics")
                .content("Learn Java programming")
                .author(DocumentManager.Author.builder().id("author1").name("John Doe").build())
                .created(Instant.now())
                .build());

        documentManager.save(DocumentManager.Document.builder()
                .title("Spring Framework")
                .content("Build applications with Spring")
                .author(DocumentManager.Author.builder().id("author2").name("Jane Doe").build())
                .created(Instant.now())
                .build());

        List<DocumentManager.Document> results = documentManager.search(null);

        assertEquals(2, results.size(), "All documents should match when no criteria are provided");
    }
}
