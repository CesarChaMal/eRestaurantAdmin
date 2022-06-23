package com.erestaurant.admin.repository.rowmapper;

import com.erestaurant.admin.domain.SimplePermission;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link SimplePermission}, with proper type conversions.
 */
@Service
public class SimplePermissionRowMapper implements BiFunction<Row, String, SimplePermission> {

    private final ColumnConverter converter;

    public SimplePermissionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link SimplePermission} stored in the database.
     */
    @Override
    public SimplePermission apply(Row row, String prefix) {
        SimplePermission entity = new SimplePermission();
        entity.setId(converter.fromRow(row, prefix + "_id", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        return entity;
    }
}
