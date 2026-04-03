package com.opensprout.lessonai.service.llm;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.opensprout.lessonai.common.exception.BusinessException;
import com.opensprout.lessonai.domain.entity.LlmConfigDO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class LlmResponseClient {

    private static final String RESPONSES_PATH = "/responses";
    private static final String EMPTY_REQUEST_ID = "N/A";
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(60);

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(REQUEST_TIMEOUT)
            .version(HttpClient.Version.HTTP_1_1)
            .build();

    public String request(LlmConfigDO llmConfig, String prompt, Long sessionId) {
        String requestUrl = buildResponsesUrl(llmConfig.getBaseUrl());
        Map<String, Object> requestBody = Map.of("model", llmConfig.getModelCode(), "input", prompt);
        String requestJson = toJson(requestBody);
        log.info("LLM config snapshot, sessionId={}, llmConfig={}", sessionId, buildConfigSnapshot(llmConfig));
        log.info("LLM HTTP request, sessionId={}, llmConfigId={}, providerType={}, url={}, body={}",
                sessionId, llmConfig.getId(), llmConfig.getProviderType(), requestUrl, requestJson);
        LlmHttpResponse httpResponse = executeRequest(llmConfig, requestUrl, requestJson);
        log.info("LLM HTTP response, sessionId={}, status={}, requestId={}, body={}",
                sessionId, httpResponse.statusCode(), httpResponse.requestId(), httpResponse.body());
        if (httpResponse.statusCode() >= 400) {
            log.error("LLM HTTP error, sessionId={}, status={}, requestId={}, body={}",
                    sessionId, httpResponse.statusCode(), httpResponse.requestId(), httpResponse.body());
            throw new BusinessException(buildHttpErrorMessage(httpResponse));
        }
        return extractContent(httpResponse.body(), sessionId);
    }

    private LlmHttpResponse executeRequest(LlmConfigDO llmConfig, String requestUrl, String requestJson) {
        try {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(requestUrl))
                    .timeout(REQUEST_TIMEOUT)
                    .version(HttpClient.Version.HTTP_1_1)
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + llmConfig.getApiKey())
                    .header(HttpHeaders.CONTENT_TYPE, "application/json")
                    .header(HttpHeaders.ACCEPT, "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestJson))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return new LlmHttpResponse(
                    response.statusCode(),
                    extractRequestId(response),
                    response.body() == null ? "" : response.body()
            );
        } catch (HttpTimeoutException exception) {
            log.error("LLM HTTP timeout, url={}, modelCode={}, bodyLength={}, errorType={}, errorMessage={}",
                    requestUrl,
                    llmConfig.getModelCode(),
                    requestJson.length(),
                    exception.getClass().getSimpleName(),
                    normalizeExceptionMessage(exception),
                    exception);
            throw new BusinessException(buildTimeoutMessage(requestUrl, llmConfig.getModelCode(), requestJson));
        } catch (BusinessException exception) {
            throw exception;
        } catch (IOException | InterruptedException exception) {
            if (exception instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            log.error("LLM HTTP transport error, url={}, modelCode={}, bodyLength={}, errorType={}, errorMessage={}",
                    requestUrl,
                    llmConfig.getModelCode(),
                    requestJson.length(),
                    exception.getClass().getSimpleName(),
                    normalizeExceptionMessage(exception),
                    exception);
            throw new BusinessException(buildTransportErrorMessage(requestUrl, llmConfig.getModelCode(), exception));
        }
    }

    private String extractRequestId(HttpResponse<String> response) {
        return response.headers().firstValue("x-request-id")
                .or(() -> response.headers().firstValue("request-id"))
                .orElse(EMPTY_REQUEST_ID);
    }

    private String buildHttpErrorMessage(LlmHttpResponse httpResponse) {
        return "大模型调用失败: HTTP "
                + httpResponse.statusCode()
                + ", requestId="
                + httpResponse.requestId()
                + ", body="
                + httpResponse.body();
    }

    private String buildTimeoutMessage(String requestUrl, String modelCode, String requestJson) {
        return "大模型调用超时: url="
                + requestUrl
                + ", modelCode="
                + modelCode
                + ", timeoutSeconds="
                + REQUEST_TIMEOUT.toSeconds()
                + ", bodyLength="
                + requestJson.length();
    }

    private String buildTransportErrorMessage(String requestUrl, String modelCode, Exception exception) {
        return "大模型调用失败: url="
                + requestUrl
                + ", modelCode="
                + modelCode
                + ", errorType="
                + exception.getClass().getSimpleName()
                + ", errorMessage="
                + normalizeExceptionMessage(exception);
    }

    private String buildResponsesUrl(String baseUrl) {
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + RESPONSES_PATH;
    }

    private Map<String, Object> buildConfigSnapshot(LlmConfigDO llmConfig) {
        return Map.of(
                "id", llmConfig.getId(),
                "name", normalizeValue(llmConfig.getName()),
                "providerType", normalizeValue(llmConfig.getProviderType()),
                "baseUrl", normalizeValue(llmConfig.getBaseUrl()),
                "modelCode", normalizeValue(llmConfig.getModelCode()),
                "enabled", llmConfig.getEnabled(),
                "isDefault", llmConfig.getIsDefault(),
                "sortOrder", llmConfig.getSortOrder(),
                "apiKeyMasked", maskApiKey(llmConfig.getApiKey())
        );
    }

    private String extractContent(String responseBody, Long sessionId) {
        try {
            JsonNode rootNode = objectMapper.readTree(responseBody);
            String content = extractResponseText(rootNode);
            if (isBlank(content)) {
                log.error("LLM response parse failed, sessionId={}, body=\n{}", sessionId, responseBody);
                throw new BusinessException("大模型返回内容为空");
            }
            return content;
        } catch (JsonProcessingException exception) {
            throw new BusinessException("大模型响应解析失败");
        }
    }

    private String extractResponseText(JsonNode rootNode) {
        String outputText = rootNode.path("output_text").asText();
        if (!isBlank(outputText)) {
            return outputText;
        }
        JsonNode outputNode = rootNode.path("output");
        if (outputNode.isArray()) {
            for (JsonNode itemNode : outputNode) {
                JsonNode contentNode = itemNode.path("content");
                if (!contentNode.isArray()) {
                    continue;
                }
                for (JsonNode partNode : contentNode) {
                    String text = partNode.path("text").asText();
                    if (!isBlank(text)) {
                        return text;
                    }
                }
            }
        }
        return rootNode.path("choices").path(0).path("message").path("content").asText();
    }

    private String toJson(Map<String, Object> payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new BusinessException("大模型请求数据转换失败");
        }
    }

    private boolean isBlank(String content) {
        return content == null || content.isBlank();
    }

    private String normalizeExceptionMessage(Exception exception) {
        return isBlank(exception.getMessage()) ? "<empty>" : exception.getMessage();
    }

    private String normalizeValue(String value) {
        return value == null ? "<null>" : value;
    }

    private String maskApiKey(String apiKey) {
        if (isBlank(apiKey)) {
            return "<empty>";
        }
        int visibleChars = 6;
        if (apiKey.length() <= visibleChars * 2) {
            return apiKey;
        }
        return apiKey.substring(0, visibleChars)
                + "..."
                + apiKey.substring(apiKey.length() - visibleChars)
                + "(len="
                + apiKey.length()
                + ")";
    }

    private static final class LlmHttpResponse {
        private final int statusCode;
        private final String requestId;
        private final String body;

        private LlmHttpResponse(int statusCode, String requestId, String body) {
            this.statusCode = statusCode;
            this.requestId = requestId;
            this.body = body;
        }

        private int statusCode() {
            return statusCode;
        }

        private String requestId() {
            return requestId;
        }

        private String body() {
            return body;
        }
    }
}
