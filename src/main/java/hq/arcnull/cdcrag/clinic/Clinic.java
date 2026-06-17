package hq.arcnull.cdcrag.clinic;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;

/**
 * A clinic — the SQL source of truth.
 *
 * <p>Rows here are authoritative. The vector store holds a derived, embedded
 * copy used only for semantic retrieval; if the two ever disagree, this wins.
 */
@Entity
@Table(name = "clinic")
public class Clinic {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Column(nullable = false)
    private String name;

    /** City / region, e.g. "Nairobi, Kenya". */
    @Column
    private String location;

    /** Comma-separated specialties, e.g. "pediatrics, maternity, dental". */
    @Column(length = 1000)
    private String specialties;

    /** Free-text description — the richest signal for semantic search. */
    @NotBlank
    @Column(length = 4000, nullable = false)
    private String description;

    protected Clinic() {
        // for JPA
    }

    public Clinic(String name, String location, String specialties, String description) {
        this.name = name;
        this.location = location;
        this.specialties = specialties;
        this.description = description;
    }

    /**
     * The text we embed. Concatenating the human-meaningful fields gives the
     * embedding model the fullest possible picture of what this clinic is.
     */
    public String toEmbeddableText() {
        StringBuilder sb = new StringBuilder();
        sb.append(name);
        if (location != null && !location.isBlank()) {
            sb.append(" — ").append(location);
        }
        if (specialties != null && !specialties.isBlank()) {
            sb.append("\nSpecialties: ").append(specialties);
        }
        sb.append("\n").append(description);
        return sb.toString();
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getSpecialties() {
        return specialties;
    }

    public void setSpecialties(String specialties) {
        this.specialties = specialties;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
