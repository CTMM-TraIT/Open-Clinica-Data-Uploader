package nl.thehyve.ocdu.repositories;

import nl.thehyve.ocdu.models.ClinicalData;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

/**
 * Created by piotrzakrzewski on 16/04/16.
 */
public interface ClinicalDataRepository extends CrudRepository<ClinicalData,Long> {

    List<ClinicalData> findByOwner(String owner);

    List<ClinicalData> findBySubmission(String submission);
}