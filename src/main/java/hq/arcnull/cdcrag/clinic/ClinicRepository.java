package hq.arcnull.cdcrag.clinic;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA access to the clinic table — plain CRUD over the source of truth.
 */
@Repository
public interface ClinicRepository extends JpaRepository<Clinic, Long> {
}
