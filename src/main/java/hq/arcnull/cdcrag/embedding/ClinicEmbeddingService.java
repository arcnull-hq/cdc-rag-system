package hq.arcnull.cdcrag.embedding;

import hq.arcnull.cdcrag.clinic.Clinic;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

/**
 * Bridges clinics to the vector store: it indexes clinics by meaning and
 * answers natural-language searches over them.
 *
 * <p>Each clinic becomes one {@link Document}. We stash the clinic id (and a
 * few display fields) in the document metadata so a semantic hit can be traced
 * back to the authoritative SQL row.
 */
@Service
public class ClinicEmbeddingService {

    /** Metadata key linking a vector document back to its clinic row. */
    public static final String META_CLINIC_ID = "clinicId";

    private final VectorStore vectorStore;

    public ClinicEmbeddingService(VectorStore vectorStore) {
        this.vectorStore = vectorStore;
    }

    /**
     * Embed a clinic and upsert it into the vector store. The document id is the
     * clinic id as a string, so re-indexing the same clinic overwrites rather
     * than duplicates.
     */
    public void index(Clinic clinic) {
        Document document = new Document(
                String.valueOf(clinic.getId()),
                clinic.toEmbeddableText(),
                Map.of(
                        META_CLINIC_ID, clinic.getId(),
                        "name", clinic.getName(),
                        "location", clinic.getLocation() == null ? "" : clinic.getLocation()
                )
        );
        vectorStore.add(List.of(document));
    }

    /**
     * Find the clinics whose meaning is closest to the query.
     *
     * @param query natural-language search, e.g. "where can I get a child vaccinated?"
     * @param topK  maximum number of matches to return
     */
    public List<Document> search(String query, int topK) {
        SearchRequest request = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .build();
        return vectorStore.similaritySearch(request);
    }
}
