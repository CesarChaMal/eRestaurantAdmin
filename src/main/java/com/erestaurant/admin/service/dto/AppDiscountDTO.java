package com.erestaurant.admin.service.dto;

import java.io.Serializable;
import java.util.Objects;
import javax.persistence.Lob;
import javax.validation.constraints.*;

/**
 * A DTO for the {@link com.erestaurant.admin.domain.AppDiscount} entity.
 */
public class AppDiscountDTO implements Serializable {

    @NotNull(message = "must not be null")
    @Size(min = 5)
    private String id;

    @NotNull(message = "must not be null")
    @Size(min = 3)
    private String code;

    @Lob
    private String description;

    @NotNull(message = "must not be null")
    private Float percentage;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Float getPercentage() {
        return percentage;
    }

    public void setPercentage(Float percentage) {
        this.percentage = percentage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AppDiscountDTO)) {
            return false;
        }

        AppDiscountDTO appDiscountDTO = (AppDiscountDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, appDiscountDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AppDiscountDTO{" +
            "id='" + getId() + "'" +
            ", code='" + getCode() + "'" +
            ", description='" + getDescription() + "'" +
            ", percentage=" + getPercentage() +
            "}";
    }
}
