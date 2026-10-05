package dev.twitchpatches.extension.emotes;

import java.net.URI;
import java.util.LinkedHashMap;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

final class EmoteProviders {
    static final String BTTV_GLOBAL = "https://api.betterttv.net/3/cached/emotes/global";
    static final String SEVEN_GLOBAL = "https://7tv.io/v3/emote-sets/global";
    static final String FFZ_GLOBAL = "https://api.frankerfacez.com/v1/set/global";

    static String channelId(String id) {
        return id != null && id.matches("[1-9][0-9]{0,19}") ? id : null;
    }

    static Map<String, Emote> bttv(String source, boolean global) throws JSONException {
        Map<String, Emote> result = new LinkedHashMap<>();
        if (global) bttvArray(new JSONArray(source), result);
        else {
            JSONObject root = new JSONObject(source);
            bttvArray(root.optJSONArray("sharedEmotes"), result);
            bttvArray(root.optJSONArray("channelEmotes"), result);
        }
        return result;
    }

    private static void bttvArray(JSONArray array, Map<String, Emote> target) {
        if (array == null) return;
        for (int i = 0; i < Math.min(array.length(), 2000); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            String code = item.optString("code");
            String id = item.optString("id");
            if (!validCode(code) || !id.matches("[A-Za-z0-9]{1,64}")) continue;
            boolean animated = item.optBoolean("animated") || "gif".equals(item.optString("imageType"));
            target.put(code, new Emote(code, "https://cdn.betterttv.net/emote/" + id + "/2x.webp", animated, false));
        }
    }

    static Map<String, Emote> sevenTv(String source, boolean global) throws JSONException {
        JSONObject root = new JSONObject(source);
        JSONObject set = global ? root : root.optJSONObject("emote_set");
        Map<String, Emote> result = new LinkedHashMap<>();
        JSONArray array = set == null ? null : set.optJSONArray("emotes");
        if (array == null) return result;
        for (int i = 0; i < Math.min(array.length(), 2000); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            String code = item.optString("name");
            JSONObject data = item.optJSONObject("data");
            JSONObject host = data == null ? null : data.optJSONObject("host");
            if (!validCode(code) || host == null) continue;
            String base = host.optString("url");
            if (base.startsWith("//")) base = "https:" + base;
            String file = webp(host.optJSONArray("files"));
            if (file == null) continue;
            String url = base + (base.endsWith("/") ? "" : "/") + file;
            if (imageUrl(url)) result.put(code, new Emote(code, url, data.optBoolean("animated"), (item.optInt("flags") & 1) != 0));
        }
        return result;
    }

    static Map<String, Emote> ffz(String source, boolean global) throws JSONException {
        Map<String, Emote> result = new LinkedHashMap<>();
        JSONObject root = new JSONObject(source);
        JSONObject sets = root.optJSONObject("sets");
        if (sets == null) return result;
        if (global) {
            JSONArray defaultSets = root.optJSONArray("default_sets");
            if (defaultSets != null) {
                for (int i = 0; i < defaultSets.length(); i++) {
                    String setId = defaultSets.optString(i);
                    if (setId.isEmpty()) setId = String.valueOf(defaultSets.optInt(i));
                    JSONObject set = sets.optJSONObject(setId);
                    if (set != null) ffzEmoticons(set.optJSONArray("emoticons"), result);
                }
            } else {
                java.util.Iterator<String> keys = sets.keys();
                while (keys.hasNext()) {
                    JSONObject set = sets.optJSONObject(keys.next());
                    if (set != null) ffzEmoticons(set.optJSONArray("emoticons"), result);
                }
            }
        } else {
            java.util.Iterator<String> keys = sets.keys();
            while (keys.hasNext()) {
                JSONObject set = sets.optJSONObject(keys.next());
                if (set != null) ffzEmoticons(set.optJSONArray("emoticons"), result);
            }
        }
        return result;
    }

    private static void ffzEmoticons(JSONArray array, Map<String, Emote> target) {
        if (array == null) return;
        for (int i = 0; i < Math.min(array.length(), 2000); i++) {
            JSONObject item = array.optJSONObject(i);
            if (item == null) continue;
            String code = item.optString("name");
            if (!validCode(code)) continue;
            JSONObject urls = item.optJSONObject("urls");
            JSONObject animated = item.optJSONObject("animated");
            boolean isAnimated = animated != null || item.optBoolean("animated");
            String rawUrl = null;
            if (animated != null) {
                rawUrl = animated.optString("2");
                if (rawUrl.isEmpty()) rawUrl = animated.optString("1");
                if (rawUrl.isEmpty()) rawUrl = animated.optString("4");
            }
            if ((rawUrl == null || rawUrl.isEmpty()) && urls != null) {
                rawUrl = urls.optString("2");
                if (rawUrl.isEmpty()) rawUrl = urls.optString("1");
                if (rawUrl.isEmpty()) rawUrl = urls.optString("4");
            }
            if (rawUrl == null || rawUrl.isEmpty()) continue;
            if (rawUrl.startsWith("//")) rawUrl = "https:" + rawUrl;
            if (!imageUrl(rawUrl)) continue;
            boolean overlay = item.optBoolean("modifier") || (item.optInt("modifier_flags") != 0);
            target.put(code, new Emote(code, rawUrl, isAnimated, overlay));
        }
    }

    private static String webp(JSONArray files) {
        if (files == null) return null;
        String fallback = null;
        for (int i = 0; i < files.length(); i++) {
            JSONObject file = files.optJSONObject(i);
            if (file == null || !"WEBP".equalsIgnoreCase(file.optString("format"))) continue;
            String name = file.optString("name");
            if (!name.matches("[1-4]x[.]webp") || file.optInt("width", 0) > 512 || file.optInt("height", 0) > 512) continue;
            if ("2x.webp".equals(name)) return name;
            if (fallback == null) fallback = name;
        }
        return fallback;
    }

    static boolean imageUrl(String url) {
        try {
            URI uri = URI.create(url);
            String host = uri.getHost();
            return "https".equals(uri.getScheme()) && uri.getUserInfo() == null && uri.getPort() == -1 &&
                    ("cdn.betterttv.net".equals(host) || "cdn.7tv.app".equals(host) || "cdn.7tv.io".equals(host) ||
                     "cdn.frankerfacez.com".equals(host) || "cdn.ffz.me".equals(host));
        } catch (IllegalArgumentException error) { return false; }
    }

    private static boolean validCode(String code) {
        if (code.isEmpty() || code.length() > 128) return false;
        for (int i = 0; i < code.length(); i++) if (EmoteTokens.separator(code.charAt(i))) return false;
        return true;
    }

    private EmoteProviders() { }
}
