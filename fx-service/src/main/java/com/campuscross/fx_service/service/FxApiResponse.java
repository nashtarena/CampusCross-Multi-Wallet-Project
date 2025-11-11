package com.campuscross.fx_service.service; // Make sure this package name matches

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Map;

/**
 * This class maps to the JSON response from v6.exchangerate-api.com.
 * 
 * @JsonIgnoreProperties(ignoreUnknown = true) tells it to not crash
 *                                     if the JSON has extra fields we don't
 *                                     care about (like "result").
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class FxApiResponse {

    // This MUST be spelled "conversion_rates" to match the JSON
    private Map<String, BigDecimal> conversion_rates;

    // This field is also in the JSON, so we'll grab it
    private String base_code;

    // Lombok's @Data will create the getters/setters for us
}