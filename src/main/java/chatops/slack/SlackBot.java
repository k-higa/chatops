package chatops.slack;

import me.ramswaroop.jbot.core.slack.Bot;
import me.ramswaroop.jbot.core.slack.Controller;
import me.ramswaroop.jbot.core.slack.EventType;
import me.ramswaroop.jbot.core.slack.models.Event;
import me.ramswaroop.jbot.core.slack.models.Message;
import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

@Component
public class SlackBot extends Bot {

    private static final Logger logger = LoggerFactory.getLogger(SlackBot.class);

    @Value("${slackBotToken}")
    private String slackToken;

    @Override
    public String getSlackToken() {
        return slackToken;
    }

    @Override

    public Bot getSlackBot() {
        return this;
    }

    @Controller(events = {EventType.DIRECT_MENTION, EventType.DIRECT_MESSAGE})
    public void onReceiveDM(WebSocketSession session, Event event) throws URISyntaxException, IOException {
        logger.info(event.getText());
        if (event.getText().contains("deploy")) {
            logger.info("deploy process execute");
            final URI uri = new URI("http://localhost:8080/job/build/build?token=dev_ops"); //JenkinsのURL
            ResponseEntity<String> responseEntity = executeJenkinsJob(uri);
            reply(session, event, new Message("deploy start : " + responseEntity.getStatusCode()));

        } else if("con".equals(event.getText())){
            logger.info("Connecting to WebSocket at " + slackService.getWebSocketUrl());
            logger.info(String.valueOf(session.isOpen()));
            if (!session.isOpen()) {
                slackService.startRTM(getSlackToken());
            }

        } else {
            reply(session, event, new Message("Hi, I am " + slackService.getCurrentUser().getName()));
        }
    }


    private ResponseEntity<String> executeJenkinsJob(URI uri) {
        String plainCreds = "jenkins_id" + ":" + "jenkins_password";
        byte[] plainCredsBytes = plainCreds.getBytes();
        byte[] base64CredsBytes = Base64.encodeBase64(plainCredsBytes);
        String base64Creds = new String(base64CredsBytes);

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Basic " + base64Creds);

        HttpEntity<String> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();
        ResponseEntity<String> model = restTemplate.exchange(uri, HttpMethod.GET, request, String.class);
        return model;
    }

}
