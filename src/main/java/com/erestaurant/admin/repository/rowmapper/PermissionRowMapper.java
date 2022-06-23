package com.erestaurant.admin.repository.rowmapper;

import com.erestaurant.admin.domain.Permission;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Permission}, with proper type conversions.
 */
@Service
public class PermissionRowMapper implements BiFunction<Row, String, Permission> {

    private final ColumnConverter converter;

    public PermissionRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Permission} stored in the database.
     */
    @Override
    public Permission apply(Row row, String prefix) {
        Permission entity = new Permission();
        entity.setId(converter.fromRow(row, prefix + "_id", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        return entity;
    }
}
