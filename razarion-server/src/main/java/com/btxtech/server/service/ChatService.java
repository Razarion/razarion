package com.btxtech.server.service;

import com.btxtech.server.gameengine.ClientSystemConnectionService;
import com.btxtech.server.model.ChatMessageEntity;
import com.btxtech.server.repository.ChatMessagesRepository;
import com.btxtech.server.rest.CallSign;
import com.btxtech.server.service.history.CommunityTickerService;
import com.btxtech.server.user.UserService;
import com.btxtech.shared.datatypes.ChatMessage;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayDeque;
import java.util.Date;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The one chat of the planet.
 * <p>
 * Everybody may write, registered or not. It used to take an account and a chosen name, and in all
 * the time it was like that 26 messages were written - the players come from the Facebook and
 * Instagram apps and play anonymously, and those who asked "how do I play" or "why is it so laggy"
 * were the few who had registered. A player without a name writes under the call sign the community
 * ticker gives them ("Falcon-42", {@link CallSign}), the same every time for the same player.
 * <p>
 * What keeps that from becoming a flood: a message is at most {@link #MAX_LENGTH} characters, and a
 * player gets {@link #BURST} of them per {@link #WINDOW_MS} and one per {@link #MIN_GAP_MS}.
 * <p>
 * The recorder (razarion-recorder) reads the chat and answers questions about the game through
 * {@link #answer}, under {@link #ASSISTANT_NAME}. The planet itself announces a player leaving the
 * noob island, under {@link #NEWS_NAME}: newcomers see that it can be done and that others play.
 * The social pipeline adds a line when such a crossing has become a reel on our Instagram
 * ({@link #reel}); the chat shows it behind a button and loads nothing from Instagram before that.
 */
@Service
public class ChatService {
    public static final String ASSISTANT_NAME = "Razarion AI";
    public static final String NEWS_NAME = "Razarion";
    static final int MAX_LENGTH = 300;
    static final int MAX_ANSWER_LENGTH = 500;
    static final long MIN_GAP_MS = 3_000;
    static final long WINDOW_MS = 60_000;
    static final int BURST = 6;
    static final String REEL_TEXT = "A new crossing to the mainland is on our Instagram";
    /** A post or reel of any account, with or without a query; only the shortcode is kept. */
    private static final Pattern INSTAGRAM_URL = Pattern.compile("^https://(?:www\\.)?instagram\\.com/(?:[A-Za-z0-9_.]+/)?(reel|p)/([A-Za-z0-9_-]{5,40})/?(?:\\?.*)?$");
    private final ChatMessagesRepository chatMessagesRepository;
    private final UserService userService;
    private final ClientSystemConnectionService clientSystemConnectionService;
    private final Map<String, Deque<Long>> sentPerUser = new ConcurrentHashMap<>();
    private final Logger logger = LoggerFactory.getLogger(ChatService.class);

    public ChatService(ChatMessagesRepository chatMessagesRepository, UserService userService, ClientSystemConnectionService clientSystemConnectionService, CommunityTickerService communityTickerService) {
        this.chatMessagesRepository = chatMessagesRepository;
        this.userService = userService;
        this.clientSystemConnectionService = clientSystemConnectionService;
        if (communityTickerService != null) {
            // Off the game thread that writes the history row: a database write and a broadcast
            communityTickerService.addLeftNoobIslandListener(userId -> CompletableFuture.runAsync(() -> announceLeftNoobIsland(userId)));
        }
    }

    /** What a client joining the chat is shown: the latest messages, oldest first. */
    @Transactional
    public List<ChatMessage> getAllMessages() {
        return chatMessagesRepository
                .findTop50ByOrderByIdDesc()
                .reversed()
                .stream()
                .map(ChatMessageEntity::toChatMessage)
                .toList();
    }

    @Transactional
    public void send(String message) {
        String text = clean(message, MAX_LENGTH);
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty message");
        }
        var userContext = userService.getUserContextFromContext();
        if (!allowed(userContext.getUserId(), System.currentTimeMillis())) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS, "Too many messages, please wait a moment");
        }
        String name = userContext.checkName() ? userContext.getName() : guestName(userContext.getUserId());
        var chatMessageEntity = chatMessagesRepository.save(new ChatMessageEntity()
                .init(userContext.getUserId(), name, text));

        clientSystemConnectionService.sendChatMessage(chatMessageEntity.toChatMessage());
    }

    /** An answer of the chat assistant, written by the recorder. Belongs to no account. */
    @Transactional
    public void answer(String message) {
        String text = clean(message, MAX_ANSWER_LENGTH);
        if (text.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Empty answer");
        }
        var chatMessageEntity = chatMessagesRepository.save(new ChatMessageEntity().init(null, ASSISTANT_NAME, text));
        clientSystemConnectionService.sendChatMessage(chatMessageEntity.toChatMessage());
    }

    void announceLeftNoobIsland(String userId) {
        try {
            String name = userService.getUserNames(Set.of(userId)).getOrDefault(userId, guestName(userId));
            var chatMessageEntity = chatMessagesRepository.save(new ChatMessageEntity().init(null, NEWS_NAME, leftNoobIslandText(name)));
            clientSystemConnectionService.sendChatMessage(chatMessageEntity.toChatMessage());
        } catch (Exception e) {
            logger.warn("Chat: left-noob-island announcement failed: {}", e.getMessage());
        }
    }

    /**
     * A crossing that has become a reel on our Instagram, reported by the social pipeline after it
     * posted it. Announced once per reel; a report of the same reel again is ignored, so the
     * pipeline may retry. Returns false for a repeat.
     */
    @Transactional
    public boolean reel(String url) {
        String permalink = normalizeInstagramUrl(url);
        if (permalink == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Not an Instagram post or reel: " + url);
        }
        if (chatMessagesRepository.existsByInstagramUrl(permalink)) {
            return false;
        }
        var chatMessageEntity = chatMessagesRepository.save(new ChatMessageEntity()
                .init(null, NEWS_NAME, REEL_TEXT)
                .instagramUrl(permalink));
        clientSystemConnectionService.sendChatMessage(chatMessageEntity.toChatMessage());
        return true;
    }

    /** https://www.instagram.com/reel/{shortcode}/ for any post or reel link, null for anything else. */
    static String normalizeInstagramUrl(String url) {
        if (url == null) {
            return null;
        }
        Matcher matcher = INSTAGRAM_URL.matcher(url.trim());
        if (!matcher.matches()) {
            return null;
        }
        return "https://www.instagram.com/" + matcher.group(1) + "/" + matcher.group(2) + "/";
    }

    static String leftNoobIslandText(String name) {
        return name + " just left the Noob Island and landed on the mainland!";
    }

    public record Entry(long id, Date timestamp, String userId, String userName, String message, String instagramUrl) {
    }

    /**
     * The messages after {@code after}, oldest first, for the recorder. Without {@code after} only
     * the newest one: a recorder that starts fresh goes on from now and does not answer the archive.
     */
    @Transactional
    public List<Entry> after(Long after) {
        List<ChatMessageEntity> entities = after == null
                ? chatMessagesRepository.findTopByOrderByIdDesc().map(List::of).orElse(List.of())
                : chatMessagesRepository.findTop200ByIdGreaterThanOrderByIdAsc(after);
        return entities.stream()
                .map(ChatService::entry)
                .toList();
    }

    /** For the backend: everything written from {@code from} to {@code to}, newest first, at most 1000. */
    @Transactional
    public List<Entry> between(Date from, Date to) {
        return chatMessagesRepository.findTop1000ByTimestampBetweenOrderByIdDesc(from, to).stream()
                .map(ChatService::entry)
                .toList();
    }

    private static Entry entry(ChatMessageEntity e) {
        return new Entry(e.getId(), e.getTimestamp(), e.getUserId(), e.getUserName(), e.getMessage(), e.getInstagramUrl());
    }

    /** The ticker's call sign: the same player is the same name in the ticker, the chat and on the map. */
    static String guestName(String userId) {
        return CallSign.of(userId);
    }

    static String clean(String message, int maxLength) {
        if (message == null) {
            return "";
        }
        // No line breaks or control characters: the chat shows one paragraph per message.
        String text = message.replaceAll("\\p{Cntrl}+", " ").trim();
        return text.length() > maxLength ? text.substring(0, maxLength) : text;
    }

    boolean allowed(String userId, long now) {
        Deque<Long> sent = sentPerUser.computeIfAbsent(userId, id -> new ArrayDeque<>());
        synchronized (sent) {
            while (!sent.isEmpty() && sent.peekFirst() < now - WINDOW_MS) {
                sent.pollFirst();
            }
            if (sent.size() >= BURST || (!sent.isEmpty() && now - sent.peekLast() < MIN_GAP_MS)) {
                return false;
            }
            sent.addLast(now);
            return true;
        }
    }
}
