package com.pxfi.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pxfi.model.EndUserAgreementRequest;
import com.pxfi.model.EndUserAgreementResponse;
import com.pxfi.model.GoCardlessTokenResponse;
import com.pxfi.model.RequisitionDetailsResponse;
import com.pxfi.model.RequisitionRequest;
import com.pxfi.model.RequisitionResponse;
import com.pxfi.model.TransactionsResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

@Service
public class GoCardlessService {

    @Value("${gocardless.secret-id}")
    private String secretId;

    @Value("${gocardless.secret-key}")
    private String secretKey;

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;

    public GoCardlessService(RestTemplate restTemplate, ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.objectMapper = objectMapper;
    }

    public String fetchAccessToken() throws Exception {
        String url = "https://bankaccountdata.gocardless.com/api/v2/token/new/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        Map<String, String> body = new HashMap<>();
        body.put("secret_id", secretId);
        body.put("secret_key", secretKey);

        HttpEntity<Map<String, String>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to get access token: " + response.getBody());
        }

        GoCardlessTokenResponse tokenResponse =
                objectMapper.readValue(response.getBody(), GoCardlessTokenResponse.class);
        return tokenResponse.getAccessToken();
    }

    public List<Map<String, Object>> getInstitutions(String accessToken, String countryCode) {
        String url =
                UriComponentsBuilder.fromHttpUrl(
                                "https://bankaccountdata.gocardless.com/api/v2/institutions/")
                        .queryParam("country", countryCode)
                        .toUriString();

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<List<Map<String, Object>>> response =
                restTemplate.exchange(
                        url, HttpMethod.GET, entity, new ParameterizedTypeReference<>() {});

        return response.getBody();
    }

    public EndUserAgreementResponse createEndUserAgreement(String institutionId, String accessToken)
            throws Exception {
        String url = "https://bankaccountdata.gocardless.com/api/v2/agreements/enduser/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(accessToken);

        EndUserAgreementRequest request =
                new EndUserAgreementRequest(
                        institutionId, "90", "30", List.of("balances", "details", "transactions"));

        HttpEntity<EndUserAgreementRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    "Failed to create end-user agreement: " + response.getBody());
        }

        return objectMapper.readValue(response.getBody(), EndUserAgreementResponse.class);
    }

    public RequisitionResponse createRequisition(
            String accessToken, String institutionId, String agreementId) throws Exception {
        String url = "https://bankaccountdata.gocardless.com/api/v2/requisitions/";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        headers.setBearerAuth(accessToken);

        RequisitionRequest request =
                new RequisitionRequest(
                        "http://localhost:4200/callback", // update callback URL as needed
                        institutionId,
                        agreementId,
                        "pxfi-user-session-" + System.currentTimeMillis());

        HttpEntity<RequisitionRequest> entity = new HttpEntity<>(request, headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.POST, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException("Failed to create requisition: " + response.getBody());
        }

        return objectMapper.readValue(response.getBody(), RequisitionResponse.class);
    }

    public RequisitionDetailsResponse getRequisitionDetails(
            String accessToken, String requisitionId) throws Exception {
        String url =
                "https://bankaccountdata.gocardless.com/api/v2/requisitions/" + requisitionId + "/";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    "Failed to fetch requisition details: " + response.getBody());
        }

        return objectMapper.readValue(response.getBody(), RequisitionDetailsResponse.class);
    }

    public TransactionsResponse getAccountTransactions(String accessToken, String accountId)
            throws Exception {
        String url =
                "https://bankaccountdata.gocardless.com/api/v2/accounts/"
                        + accountId
                        + "/transactions/";

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<String> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, String.class);

        if (!response.getStatusCode().is2xxSuccessful()) {
            throw new RuntimeException(
                    "Failed to fetch account transactions: " + response.getBody());
        }

        return objectMapper.readValue(response.getBody(), TransactionsResponse.class);
    }
}
