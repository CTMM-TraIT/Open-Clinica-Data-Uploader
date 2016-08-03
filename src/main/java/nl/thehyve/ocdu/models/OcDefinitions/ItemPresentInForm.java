package nl.thehyve.ocdu.models.OcDefinitions;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * Entity bean for the <code>ItemPresentInForm</code> as present in the OpenClinica study-metadata.
 * Created by jacob on 8/3/16.
 */
@Entity
public class ItemPresentInForm {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private long id;

    private String formOID;

    private int pageNumber;

    /**
     * Personal Health Information
     */
    private boolean PHI;

    private boolean required;

    private boolean showItem;

    private int orderInForm;

    public ItemPresentInForm() {
    }

    public String getFormOID() {
        return formOID;
    }

    public void setFormOID(String formOID) {
        this.formOID = formOID;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
    }

    public boolean isPHI() {
        return PHI;
    }

    public void setPHI(boolean PHI) {
        this.PHI = PHI;
    }

    public boolean isRequired() {
        return required;
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean isShowItem() {
        return showItem;
    }

    public void setShowItem(boolean showItem) {
        this.showItem = showItem;
    }

    public int getOrderInForm() {
        return orderInForm;
    }

    public void setOrderInForm(int orderInForm) {
        this.orderInForm = orderInForm;
    }
}
