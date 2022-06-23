package com.erestaurant.admin.domain;

import java.io.Serializable;
import javax.validation.constraints.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * A AppDiscount.
 */
@Table("app_discount")
public class AppDiscount implements Serializable, Persistable<String> {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "must not be null")
    @Size(min = 5)
    @Id
    @Column("id")
    private String id;

    @NotNull(message = "must not be null")
    @Size(min = 3)
    @Column("code")
    private String code;

    @Column("description")
    private String description;

    @NotNull(message = "must not be null")
    @Column("percentage")
    private Float percentage;

    @Transient
    private boolean isPersisted;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public String getId() {
        return this.id;
    }

    public AppDiscount id(String id) {
        this.setId(id);
        return this;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return this.code;
    }

    public AppDiscount code(String code) {
        this.setCode(code);
        return this;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return this.description;
    }

    public AppDiscount description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getPercentage() {
        return this.percentage;
    }

    public AppDiscount percentage(Float percentage) {
        this.setPercentage(percentage);
        return this;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    @Transient
    @Override
    public boolean isNew() {
        return !this.isPersisted;
    }

    public AppDiscount setIsPersisted() {
        this.isPersisted = true;
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppDiscount)) {
            return false;
        }
        return id != null && id.equals(((AppDiscount) o).id);
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppDiscount{" +
            "id=" + getId() +
            ", code='" + getCode() + "'" +
            ", description='" + getDescription() + "'" +
            ", percentage=" + getPercentage() +
            "}";
    }
}
