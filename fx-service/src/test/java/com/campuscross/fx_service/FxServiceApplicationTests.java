package com.campuscross.fx_service;

import com.campuscross.fx_service.client.SumsubClient;
import com.campuscross.fx_service.controller.KycController;
import com.campuscross.fx_service.controller.RateAlertController;
import com.campuscross.fx_service.repository.RateAlertRepository;
import com.campuscross.fx_service.repository.UserKycRepository;
import com.campuscross.fx_service.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import com.campuscross.fx_service.repository.RemittanceRepository;
import com.campuscross.fx_service.controller.RemittanceController;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
		"airwallex.enabled=false",
		"airwallex.api-url=http://localhost",
		"airwallex.client-id=test",
		"airwallex.api-key=test"
})
class FxServiceApplicationTests {

	// Mock ALL JPA repositories
	@MockBean
	private UserKycRepository userKycRepository;

	@MockBean
	private RateAlertRepository rateAlertRepository;

	// Mock ALL services that depend on JPA
	@MockBean
	private KycService kycService;

	@MockBean
	private KycAsyncProcessor kycAsyncProcessor;

	@MockBean
	private RateAlertService rateAlertService;

	@MockBean
	private SumsubWebhookHandler sumsubWebhookHandler;

	@MockBean
	private OpenSanctionsService openSanctionsService;

	// Mock ALL controllers
	@MockBean
	private KycController kycController;

	@MockBean
	private RateAlertController rateAlertController;

	// Mock ALL external clients
	@MockBean
	private SumsubClient sumsubClient;

	@MockBean
	private RemittanceStatusTracker remittanceStatusTracker;

	@Test
	void contextLoads() {
		// Test passes if application context loads successfully
	}
}