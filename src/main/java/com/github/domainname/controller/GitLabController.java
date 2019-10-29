package com.github.domainname.controller;

import com.github.domainname.WebhookClient;
import com.github.domainname.vendor.gitlab.GitLabWebhookListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.gitlab4j.api.GitLabApiException;
import org.gitlab4j.api.webhook.WebHookManager;
import org.slf4j.MDC;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static com.github.domainname.util.ExceptionUtils.logException;

/**
 * @author jeff
 * @date 2019/10/25
 */
@ConditionalOnProperty("gitlab.enabled")
@RestController
@RequiredArgsConstructor
@Slf4j
public class GitLabController {

    private static final String SUCCESS_JSON = "{}";

    private final WebHookManager webHookManager = new WebHookManager();
    private final WebhookClient webhookClient;

    @PostConstruct
    public void init() {
        webHookManager.addListener(new GitLabWebhookListener(webhookClient));
    }

    @PostMapping("/gitlab/webhook/{key}")
    public String webhook(@PathVariable String key, HttpServletRequest request) throws GitLabApiException {
        MDC.put("key", key);

        webHookManager.handleEvent(request);

        return SUCCESS_JSON;
    }

    @ExceptionHandler(GitLabApiException.class)
    public void handleError(GitLabApiException e, HttpServletResponse response) throws IOException {
        logException(log, e);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }

    @ExceptionHandler(RuntimeException.class)
    public void handleError(RuntimeException e, HttpServletResponse response) throws IOException {
        logException(log, e);
        response.sendError(HttpStatus.INTERNAL_SERVER_ERROR.value());
    }
}
