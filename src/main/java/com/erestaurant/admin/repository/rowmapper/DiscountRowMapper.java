package com.erestaurant.admin.repository.rowmapper;

import com.erestaurant.admin.domain.Discount;
import io.r2dbc.spi.Row;
import java.util.function.BiFunction;
import org.springframework.stereotype.Service;

/**
 * Converter between {@link Row} to {@link Discount}, with proper type conversions.
 */
@Service
public class DiscountRowMapper implements BiFunction<Row, String, Discount> {

    private final ColumnConverter converter;

    public DiscountRowMapper(ColumnConverter converter) {
        this.converter = converter;
    }

    /**
     * Take a {@link Row} and a column prefix, and extract all the fields.
     * @return the {@link Discount} stored in the database.
     */
    @Override
    public Discount apply(Row row, String prefix) {
        Discount entity = new Discount();
        entity.setId(converter.fromRow(row, prefix + "_id", String.class));
        entity.setCode(converter.fromRow(row, prefix + "_code", String.class));
        entity.setDescription(converter.fromRow(row, prefix + "_description", String.class));
        entity.setPercentage(converter.fromRow(row, prefix + "_percentage", Float.class));
        return entity;
    }
}
