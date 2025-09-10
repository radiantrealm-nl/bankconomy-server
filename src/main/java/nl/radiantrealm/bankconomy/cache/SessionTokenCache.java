package nl.radiantrealm.bankconomy.cache;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SessionTokenCache {
    private static final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    private static final Map<Integer, UUID> OTPTOKENMAP = new HashMap<>();
    private static final Map<Integer, Long> OTPEXPIRYMAP = new HashMap<>();

    private static final Map<UUID, UUID> SESSIONTOKENMAP = new HashMap<>();
    private static final Map<UUID, Long> SESSIONEXPIRYMAP = new HashMap<>();

    static {
        executorService.scheduleAtFixedRate(SessionTokenCache::cleanKeys, 0, 1, TimeUnit.MINUTES);
    }

    private SessionTokenCache() {}

    private static void cleanKeys() {
        long timestamp = System.currentTimeMillis();

        OTPEXPIRYMAP.forEach((key, value) -> {
            if (timestamp > value) {
                OTPTOKENMAP.remove(key);
                OTPEXPIRYMAP.remove(key);
            }
        });

        SESSIONEXPIRYMAP.forEach((key, value) -> {
            if (timestamp > value) {
                SESSIONTOKENMAP.remove(key);
                SESSIONEXPIRYMAP.remove(key);
            }
        });
    }

    public static Integer generateOTPToken(UUID playerUUID) {
        if (playerUUID == null) return null;

        Integer random = 100000 + new Random().nextInt(100000);
        long expiry = System.currentTimeMillis() + Duration.ofMinutes(5).toMillis();

        OTPTOKENMAP.put(random, playerUUID);
        OTPEXPIRYMAP.put(random, expiry);
        return random;
    }

    public static UUID verifyOTPToken(Integer token) {
        if (token == null) return null;

        UUID playerUUID = OTPTOKENMAP.remove(token);
        OTPEXPIRYMAP.remove(token);

        if (playerUUID == null) {
            return null;
        }

        UUID sessionUUID = UUID.randomUUID();
        long expiry = System.currentTimeMillis() + Duration.ofMinutes(15).toMillis();

        SESSIONTOKENMAP.put(sessionUUID, playerUUID);
        SESSIONEXPIRYMAP.put(sessionUUID, expiry);
        return playerUUID;
    }

    public static UUID verifySessionUUID(UUID sessionUUID) {
        if (sessionUUID == null) return null;
        return SESSIONTOKENMAP.get(sessionUUID);
    }
}
