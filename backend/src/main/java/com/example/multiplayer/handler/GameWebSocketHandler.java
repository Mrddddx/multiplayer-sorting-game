package com.example.multiplayer.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

public class GameWebSocketHandler extends TextWebSocketHandler {

    private final List<Player> queue = new CopyOnWriteArrayList<>();
    private final Map<String, WebSocketSession> sessions = new HashMap<>();
    private final ObjectMapper mapper = new ObjectMapper();
    private boolean gameStarted = false;

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        Map<String, Object> data = mapper.readValue(message.getPayload(), new TypeReference<>() {});
        String type = (String) data.get("type");
        String id = session.getId();

        switch (type) {
            case "join": {
                String name = (String) data.get("name");
                int height = new Random().nextInt(50) + 150;
                Player player = new Player(id, name, height);
                queue.add(player);
                broadcastState();
                break;
            }
            case "ready": {
                Player p = getPlayerById(id);
                if (p != null) {
                    p.setReady(true);
                    broadcastState();
                    checkAndStartGame();
                }
                break;
            }
            case "swapLeft":
            case "swapRight": {
                if (!gameStarted) return;
                int index = findIndexById(id);
                if (index == -1) return;
                if ("swapLeft".equals(type) && index > 0) {
                    Collections.swap(queue, index, index - 1);
                } else if ("swapRight".equals(type) && index < queue.size() - 1) {
                    Collections.swap(queue, index, index + 1);
                }
                broadcastState();
                if (isSorted()) {
                    broadcastMessage("gameOver", "🎉 排序完成！");
                }
                break;
            }
            case "restart": {
                gameStarted = false;
                for (Player p : queue) {
                    p.setReady(false);
                    p.setHeight(new Random().nextInt(50) + 150);
                }
                broadcastState();
                break;
            }
        }
    }

    private void checkAndStartGame() throws Exception {
        if (queue.stream().allMatch(Player::isReady) && queue.size() > 1) {
            gameStarted = true;
            broadcastMessage("start", "所有人已准备，游戏开始！");
        }
    }

    private void broadcastState() throws Exception {
        List<Map<String, Object>> data = new ArrayList<>();
        for (Player p : queue) {
            data.add(Map.of(
                    "id", p.getId(),
                    "name", p.getName(),
                    "height", p.getHeight(),
                    "ready", p.isReady()
            ));
        }
        for (Map.Entry<String, WebSocketSession> entry : sessions.entrySet()) {
            WebSocketSession s = entry.getValue();
            if (s.isOpen()) {
                String sid = s.getId();
                String payload = mapper.writeValueAsString(Map.of(
                        "type", "update",
                        "queue", data,
                        "started", gameStarted,
                        "yourId", sid
                ));
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    private void broadcastMessage(String type, String message) throws Exception {
        String payload = mapper.writeValueAsString(Map.of("type", type, "message", message));
        for (WebSocketSession s : sessions.values()) {
            if (s.isOpen()) {
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    private int findIndexById(String id) {
        for (int i = 0; i < queue.size(); i++) {
            if (queue.get(i).getId().equals(id)) return i;
        }
        return -1;
    }

    private Player getPlayerById(String id) {
        return queue.stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null);
    }

    private boolean isSorted() {
        for (int i = 1; i < queue.size(); i++) {
            if (queue.get(i - 1).getHeight() > queue.get(i).getHeight()) return false;
        }
        return true;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        queue.removeIf(p -> p.getId().equals(session.getId()));
        sessions.remove(session.getId());
    }

    static class Player {
        private String id;
        private String name;
        private int height;
        private boolean ready;

        public Player(String id, String name, int height) {
            this.id = id;
            this.name = name;
            this.height = height;
            this.ready = false;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public int getHeight() { return height; }
        public boolean isReady() { return ready; }

        public void setReady(boolean ready) { this.ready = ready; }
        public void setHeight(int height) { this.height = height; }
    }
}