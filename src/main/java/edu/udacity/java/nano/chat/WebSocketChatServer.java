package edu.udacity.java.nano.chat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.servlet.http.HttpSession;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;

import com.google.gson.Gson;

import org.springframework.stereotype.Component;

/**
 * WebSocket Server
 *
 * @see ServerEndpoint WebSocket Client
 * @see Session WebSocket Session
 */

@Component
@ServerEndpoint(value = "/chat", configurator = ServletAwareConfig.class)
public class WebSocketChatServer {
    Logger logger = Logger.getLogger(WebSocketChatServer.class.getName());

    /**
     * All chat sessions.
     */
    private static Map<String, Session> onlineSessions = new ConcurrentHashMap<>();

    private static void sendMessageToAll(String msg) {
        for (Session session : onlineSessions.values()) {
            session.getAsyncRemote().sendText(msg);
        }
    }

    /**
     * Open connection, 1) add session, 2) add user.
     */
    @OnOpen
    public void onOpen(Session session) {
        HttpSession HttpSession = (HttpSession) session.getUserProperties().get("httpSession");
        String username = (String) HttpSession.getAttribute("username");
        logger.log(Level.INFO, username + " joined chat");
        onlineSessions.put(username, session);
        Message message = new Message();
        message.setUsername(username);
        message.setMsg(username + " joined chat");
        message.setOnlineCount(onlineSessions.size());
        message.setType("SPEAK");
        Gson gson = new Gson();
        WebSocketChatServer.sendMessageToAll(gson.toJson(message));
    }

    /**
     * Send message, 1) get username and session, 2) send message to all.
     */
    @OnMessage
    public void onMessage(Session session, String jsonStr) {
        logger.log(Level.INFO, " got json " + jsonStr);
        Gson gson = new Gson();
        Message message = gson.fromJson(jsonStr, Message.class);
        message.setOnlineCount(onlineSessions.size());
        message.setType("SPEAK");
        WebSocketChatServer.sendMessageToAll(gson.toJson(message));
        System.out.println(jsonStr);
    }

    /**
     * Close connection, 1) remove session, 2) update user.
     */
    @OnClose
    public void onClose(Session session) {
        HttpSession HttpSession = (HttpSession) session.getUserProperties().get("httpSession");
        String username = (String) HttpSession.getAttribute("username");
        onlineSessions.remove(username);
        logger.log(Level.INFO, username + " left chat");
        Message message = new Message();
        message.setUsername(username);
        message.setMsg(username + " left chat");
        message.setOnlineCount(onlineSessions.size());
        message.setType("SPEAK");
        Gson gson = new Gson();
        WebSocketChatServer.sendMessageToAll(gson.toJson(message));
    }

    /**
     * Print exception.
     */
    @OnError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

}
