package com.erestaurant.admin.repository.rowmapper;

import com.erestaurant.admin.domain.AppDiscount;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link AppDiscount}, with proper type conversions.
 */
@Service
public class AppDiscountRowMapper implements BiFunction<Row, String, AppDiscount> {

    private final ColumnConverter converter;

    public AppDiscountRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link AppDiscount} stored in the database.
     */
    @Override
    public AppDiscount apply(Row row, String prefix) {
        AppDiscount entity = new AppDiscount();
        entity.setId(converter.fromRow(row, prefix + "_id", String.class));
        entity.setCode(converter.fromRow(row, prefix + "_code", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setPercentage(converter.fromRow(row, prefix + "_percentage", Float.class));
        return entity;
    }
}
