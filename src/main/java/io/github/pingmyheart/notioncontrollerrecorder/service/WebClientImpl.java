package io.github.pingmyheart.notioncontrollerrecorder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.github.pingmyheart.notioncontrollerrecorder.dto.external.response.NotionBaseResponse;
import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.http.HttpMethod;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;

@RequiredArgsConstructor
@Builder
public class WebClientImpl {

    private final ObjectMapper objectMapper;
    private final WebClient notionWebClient;
    private final String notionToken;

    public <T extends NotionBaseResponse> T exchange(String url,
                                                     HttpMethod httpMethod,
                                                     Class<T> responseClass,
                                                     Object requestBody) {
//        Objects.requireNonNull(notionToken, "notionToken is null");
        WebClient.RequestBodySpec request = notionWebClient.method(httpMethod)
                .uri(url)
                .header("Authorization", "Bearer " + notionToken);
        if (requestBody != null) {
            request.bodyValue(requestBody);
        }
        return request.exchangeToMono(clientResponse -> clientResponse.statusCode().is2xxSuccessful() ?
                        clientResponse.bodyToMono(responseClass) :
                        clientResponse.createException()
                                .map(e -> exceptionToBaseResponse(e, responseClass)))
                .block();
    }

    @SneakyThrows
    private <T extends NotionBaseResponse> T exceptionToBaseResponse(WebClientResponseException exception, Class<T> clazz) {
        try {
            return objectMapper.readValue(exception.getResponseBodyAsString(), clazz);
        } catch (JsonProcessingException e) {
            throw new MojoFailureException("Cannot parse notion webclient error response");
        }
    }


}
