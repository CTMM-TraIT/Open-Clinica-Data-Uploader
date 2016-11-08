package nl.thehyve.ocdu.models.errors;

/**
 * Defines the types of errors which the data contain. Is used to determine if the data should be disregarded from
 * uploads.
 * Created by jacob on 04/11/16.
 */
public enum ErrorClassification {

    /**
     * Errors in the structure of the input data and the reference to the data target
     * (e.g. missing cell in a CRF-record, missing required column, empty or missing clinical-axis cells. The
     * consequence of this error that the entire upload session is blocked
     */
    BLOCK_ENTIRE_UPLOAD,

    /**
     * Errors in the state of data in OpenClinica and in the data files (e.g. data supplied for a subject which is not
     * present in OpenClinica nor in the subject-input file). The upload of the entire CRF (row in the input file) is
     * blocked.
     */
    BLOCK_ENTIRE_CRF,

    /**
     * An error only concerning a single item (e.g. out of range of a number)
     */
    SINGLE_ITEM_ERROR,

    /**
     * An error affecting 2 or more items (e.g. missing mandatory item in a CRF, missing item used in a simple
     * conditional display of another item)
     */
    CROSS_ITEM_ERROR,

    /**
     * Errors in references across the 3 input sources: data-, subject and event-files. (e.g. event occurrence in data
     * file is not present in OpenClinica nor in the event file)
     */
    INPUT_CROSS_REFERENCE,

    /**
     * Only a notification for the user, no data will be disregarded
     */
    WARNING;
}


