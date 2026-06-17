package hq.arcnull.cdcrag;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the CDC RAG system.
 *
 * <p>Two stores work together here:
 * <ul>
 *   <li>PostgreSQL holds clinics as the relational source of truth.</li>
 *   <li>PgVector holds embeddings of those clinics so we can search by meaning.</li>
 * </ul>
 */
@SpringBootApplication
public class CdcRagApplication {

    public static void main(String[] args) {
        SpringApplication.run(CdcRagApplication.class, args);
    }
}
