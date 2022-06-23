package com.erestaurant.admin.repository.rowmapper;

import com.erestaurant.admin.domain.CompositePermission;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link CompositePermission}, with proper type conversions.
 */
@Service
public class CompositePermissionRowMapper implements BiFunction<Row, String, CompositePermission> {

    private final ColumnConverter converter;

    public CompositePermissionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link CompositePermission} stored in the database.
     */
    @Override
    public CompositePermission apply(Row row, String prefix) {
        CompositePermission entity = new CompositePermission();
        entity.setId(converter.fromRow(row, prefix + "_id", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        return entity;
    }
}
