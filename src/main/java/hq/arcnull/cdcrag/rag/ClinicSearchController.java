package hq.arcnull.cdcrag.rag;

import hq.arcnull.cdcrag.clinic.Clinic;
import hq.arcnull.cdcrag.clinic.ClinicRepository;
import hq.arcnull.cdcrag.embedding.ClinicEmbeddingService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.ai.document.Document;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * The RAG-facing API.
 *
 * <ul>
 *   <li>{@code POST /clinics} — persist a clinic and index it for semantic search.</li>
 *   <li>{@code GET  /clinics/search?query=...} — retrieve clinics by meaning.</li>
 * </ul>
 */
@RestController
@RequestMapping("/clinics")
public class ClinicSearchController {

    private final ClinicRepository clinicRepository;
    private final ClinicEmbeddingService embeddingService;

    public ClinicSearchController(ClinicRepository clinicRepository,
                                  ClinicEmbeddingService embeddingService) {
        this.clinicRepository = clinicRepository;
        this.embeddingService = embeddingService;
    }

    /** Create a clinic in SQL, then embed it into the vector store. */
    @PostMapping
    public ResponseEntity<Clinic> create(@Valid @RequestBody CreateClinicRequest request) {
        Clinic clinic = new Clinic(
                request.name(),
                request.location(),
                request.specialties(),
                request.description()
        );
        clinic = clinicRepository.save(clinic);
        embeddingService.index(clinic);
        return ResponseEntity.status(HttpStatus.CREATED).body(clinic);
    }

    /** Search clinics by natural language. */
    @GetMapping("/search")
    public List<ClinicMatch> search(@RequestParam @NotBlank String query,
                                    @RequestParam(defaultValue = "5") int topK) {
        return embeddingService.search(query, topK).stream()
                .map(ClinicMatch::from)
                .toList();
    }

    /** Incoming payload for creating a clinic. */
    public record CreateClinicRequest(
            @NotBlank String name,
            String location,
            String specialties,
            @NotBlank String description
    ) {
    }

    /** A single semantic match, flattened for the API response. */
    public record ClinicMatch(Object clinicId, String name, String location, String text) {
        static ClinicMatch from(Document document) {
            return new ClinicMatch(
                    document.getMetadata().get(ClinicEmbeddingService.META_CLINIC_ID),
                    String.valueOf(document.getMetadata().getOrDefault("name", "")),
                    String.valueOf(document.getMetadata().getOrDefault("location", "")),
                    document.getFormattedContent()
            );
        }
    }
}
