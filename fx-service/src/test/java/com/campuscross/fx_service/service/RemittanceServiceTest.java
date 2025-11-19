package com.campuscross.fx_service.service;

import com.campuscross.fx_service.client.AirwallexClient;
import com.campuscross.fx_service.dto.remittance.RemittanceRequest;
import com.campuscross.fx_service.dto.remittance.RemittanceResponse;
import com.campuscross.fx_service.model.Remittance;
import com.campuscross.fx_service.model.UserKyc;
import com.campuscross.fx_service.repository.RemittanceRepository;
import com.campuscross.fx_service.repository.UserKycRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RemittanceServiceTest {

    @Mock
    private RemittanceRepository remittanceRepository;

    @Mock
    private UserKycRepository kycRepository;

    @Mock
    private FxService fxService;

    @Mock
    private AirwallexClient airwallexClient;

    @InjectMocks
    private RemittanceService remittanceService;

    private RemittanceRequest validRequest;
    private UserKyc approvedKyc;

    @BeforeEach
    void setUp() {
        validRequest = new RemittanceRequest();
        validRequest.setUserId(1L);
        validRequest.setSourceAmount(new BigDecimal("1000.00"));
        validRequest.setSourceCurrency("USD");
        validRequest.setDestinationCurrency("EUR");
        validRequest.setBeneficiaryName("John Doe");
        validRequest.setBeneficiaryAccountNumber("DE89370400440532013000");
        validRequest.setBeneficiaryBankName("Deutsche Bank");
        validRequest.setBeneficiaryBankCode("DEUTDEFF");
        validRequest.setBeneficiaryAddress("Berlin, Germany");
        validRequest.setBeneficiaryCountry("DE");
        validRequest.setTransferPurpose("Family Support");

        approvedKyc = new UserKyc(1L);
        approvedKyc.setKycTier(UserKyc.KycTier.TIER_2);
        approvedKyc.setKycStatus(UserKyc.KycStatus.APPROVED);
    }

    @Test
    void shouldCreateRemittanceSuccessfully() {
        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(approvedKyc));
        when(fxService.getCustomerQuote("USD", "EUR"))
                .thenReturn(Optional.of(new BigDecimal("0.85")));

        // FIXED: return entity with ID assigned
        when(remittanceRepository.save(any(Remittance.class)))
                .thenAnswer(invocation -> {
                    Remittance r = invocation.getArgument(0);
                    r.setId(100L);
                    if (r.getReferenceNumber() == null) {
                        r.setReferenceNumber("REM-TEST-123");
                    }
                    return r;
                });

        RemittanceResponse response = remittanceService.createRemittance(validRequest);

        assertTrue(response.isSuccess());
        assertNotNull(response.getReferenceNumber());
        assertEquals(new BigDecimal("850.00"), response.getDestinationAmount());
        verify(remittanceRepository, times(1)).save(any());
    }

    @Test
    void shouldRejectRemittanceWithoutKyc() {
        when(kycRepository.findByUserId(1L)).thenReturn(Optional.empty());

        RemittanceResponse response = remittanceService.createRemittance(validRequest);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("KYC"));
        verify(remittanceRepository, never()).save(any());
    }

    @Test
    void shouldRejectRemittanceWithInsufficientKyc() {
        UserKyc tier1Kyc = new UserKyc(1L);
        tier1Kyc.setKycTier(UserKyc.KycTier.TIER_1);
        tier1Kyc.setKycStatus(UserKyc.KycStatus.APPROVED);

        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(tier1Kyc));

        RemittanceResponse response = remittanceService.createRemittance(validRequest);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("KYC"));
    }

    @Test
    void shouldHandleMissingFxRate() {
        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(approvedKyc));
        when(fxService.getCustomerQuote("USD", "EUR"))
                .thenReturn(Optional.empty());

        RemittanceResponse response = remittanceService.createRemittance(validRequest);

        assertFalse(response.isSuccess());
        assertTrue(response.getMessage().contains("exchange rate"));
        verify(remittanceRepository, never()).save(any());
    }

    @Test
    void shouldCalculateTransferFeeCorrectly() {
        when(kycRepository.findByUserId(1L)).thenReturn(Optional.of(approvedKyc));
        when(fxService.getCustomerQuote("USD", "EUR"))
                .thenReturn(Optional.of(new BigDecimal("0.85")));

        when(remittanceRepository.save(any(Remittance.class)))
                .thenAnswer(invocation -> {
                    Remittance r = invocation.getArgument(0);
                    r.setId(200L);
                    return r;
                });

        RemittanceResponse response = remittanceService.createRemittance(validRequest);

        assertTrue(response.isSuccess());
        assertEquals(new BigDecimal("10.00"), response.getTransferFee());
        assertEquals(new BigDecimal("1010.00"), response.getTotalCost());
    }
}
