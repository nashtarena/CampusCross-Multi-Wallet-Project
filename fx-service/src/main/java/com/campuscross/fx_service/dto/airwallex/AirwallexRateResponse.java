package com.campuscross.fx_service.dto.airwallex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import java.math.BigDecimal;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AirwallexRateResponse {

    private String rate; // ← Top-level rate (sometimes large number like 7632)

    @JsonProperty("buy_currency")
    private String buyCurrency;

    @JsonProperty("sell_currency")
    private String sellCurrency;

    @JsonProperty("buy_amount")
    private String buyAmount;

    @JsonProperty("sell_amount")
    private String sellAmount;

    @JsonProperty("rate_details")
    private List<RateDetail> rateDetails; // ← ADD THIS

    // Nested class for rate_details
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class RateDetail {
        @JsonProperty("buy_amount")
        private BigDecimal buyAmount;

        @JsonProperty("sell_amount")
        private BigDecimal sellAmount;

        private BigDecimal rate; // ← USE THIS rate, not the top-level one!

        // Getters/Setters
        public BigDecimal getRate() {
            return rate;
        }

        public void setRate(BigDecimal rate) {
            this.rate = rate;
        }

        public BigDecimal getBuyAmount() {
            return buyAmount;
        }

        public void setBuyAmount(BigDecimal buyAmount) {
            this.buyAmount = buyAmount;
        }

        public BigDecimal getSellAmount() {
            return sellAmount;
        }

        public void setSellAmount(BigDecimal sellAmount) {
            this.sellAmount = sellAmount;
        }
    }

    // Getters/Setters for main fields
    public String getRate() {
        return rate;
    }

    public void setRate(String rate) {
        this.rate = rate;
    }

    public String getBuyCurrency() {
        return buyCurrency;
    }

    public void setBuyCurrency(String buyCurrency) {
        this.buyCurrency = buyCurrency;
    }

    public String getSellCurrency() {
        return sellCurrency;
    }

    public void setSellCurrency(String sellCurrency) {
        this.sellCurrency = sellCurrency;
    }

    public String getBuyAmount() {
        return buyAmount;
    }

    public void setBuyAmount(String buyAmount) {
        this.buyAmount = buyAmount;
    }

    public String getSellAmount() {
        return sellAmount;
    }

    public void setSellAmount(String sellAmount) {
        this.sellAmount = sellAmount;
    }

    public List<RateDetail> getRateDetails() {
        return rateDetails;
    }

    public void setRateDetails(List<RateDetail> rateDetails) {
        this.rateDetails = rateDetails;
    }
}
