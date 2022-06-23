package com.erestaurant.admin.repository.rowmapper;

import com.erestaurant.admin.domain.Ad;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Ad}, with proper type conversions.
 */
@Service
public class AdRowMapper implements BiFunction<Row, String, Ad> {

    private final ColumnConverter converter;

    public AdRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Ad} stored in the database.
     */
    @Override
    public Ad apply(Row row, String prefix) {
        Ad entity = new Ad();
        entity.setId(converter.fromRow(row, prefix + "_id", String.class));
        entity.setUrl(converter.fromRow(row, prefix + "_url", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        return entity;
    }
}
