package com.campuscross.fx_service.dto.airwallex;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Request DTO for Airwallex Payout API
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AirwallexPayoutRequest {

    @JsonProperty("request_id")
    private String requestId;

    @JsonProperty("source_currency")
    private String sourceCurrency;

    @JsonProperty("source_amount")
    private String sourceAmount;

    @JsonProperty("beneficiary")
    private Beneficiary beneficiary;

    @JsonProperty("reason")
    private String reason; // Transfer purpose

    @JsonProperty("reference")
    private String reference; // Your internal reference

    public static class Beneficiary {
        @JsonProperty("account_name")
        private String accountName;

        @JsonProperty("account_number")
        private String accountNumber;

        @JsonProperty("bank_name")
        private String bankName;

        @JsonProperty("swift_code")
        private String swiftCode;

        @JsonProperty("account_routing_type1")
        private String accountRoutingType1; // e.g., "swift_code"

        @JsonProperty("account_routing_value1")
        private String accountRoutingValue1;

        @JsonProperty("address")
        private Address address;

        public static class Address {
            @JsonProperty("country_code")
            private String countryCode;

            @JsonProperty("city")
            private String city;

            @JsonProperty("street_address")
            private String streetAddress;

            // Getters and Setters
            public String getCountryCode() {
                return countryCode;
            }

            public void setCountryCode(String countryCode) {
                this.countryCode = countryCode;
            }

            public String getCity() {
                return city;
            }

            public void setCity(String city) {
                this.city = city;
            }

            public String getStreetAddress() {
                return streetAddress;
            }

            public void setStreetAddress(String streetAddress) {
                this.streetAddress = streetAddress;
            }
        }

        // Getters and Setters
        public String getAccountName() {
            return accountName;
        }

        public void setAccountName(String accountName) {
            this.accountName = accountName;
        }

        public String getAccountNumber() {
            return accountNumber;
        }

        public void setAccountNumber(String accountNumber) {
            this.accountNumber = accountNumber;
        }

        public String getBankName() {
            return bankName;
        }

        public void setBankName(String bankName) {
            this.bankName = bankName;
        }

        public String getSwiftCode() {
            return swiftCode;
        }

        public void setSwiftCode(String swiftCode) {
            this.swiftCode = swiftCode;
        }

        public String getAccountRoutingType1() {
            return accountRoutingType1;
        }

        public void setAccountRoutingType1(String accountRoutingType1) {
            this.accountRoutingType1 = accountRoutingType1;
        }

        public String getAccountRoutingValue1() {
            return accountRoutingValue1;
        }

        public void setAccountRoutingValue1(String accountRoutingValue1) {
            this.accountRoutingValue1 = accountRoutingValue1;
        }

        public Address getAddress() {
            return address;
        }

        public void setAddress(Address address) {
            this.address = address;
        }
    }

    // Getters and Setters
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getSourceCurrency() {
        return sourceCurrency;
    }

    public void setSourceCurrency(String sourceCurrency) {
        this.sourceCurrency = sourceCurrency;
    }

    public String getSourceAmount() {
        return sourceAmount;
    }

    public void setSourceAmount(String sourceAmount) {
        this.sourceAmount = sourceAmount;
    }

    public Beneficiary getBeneficiary() {
        return beneficiary;
    }

    public void setBeneficiary(Beneficiary beneficiary) {
        this.beneficiary = beneficiary;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }
}
