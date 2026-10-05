package dev.twitchpatches.extension.emotes;

import java.util.Collections;
import java.util.Map;
import org.junit.Test;
import static org.junit.Assert.*;

public class EmoteProvidersTest {
    @Test public void channelLocalBttvOverridesSharedCodeAndRecognizesGif() throws Exception {
        Map<String, Emote> emotes = EmoteProviders.bttv("{\"sharedEmotes\":[{\"id\":\"shared\",\"code\":\"Wave\"}],\"channelEmotes\":[{\"id\":\"local\",\"code\":\"Wave\",\"imageType\":\"gif\"}]}", false);
        assertEquals(1, emotes.size());
        assertTrue(emotes.get("Wave").url.contains("/local/"));
        assertTrue(emotes.get("Wave").animated);
    }

    @Test public void sevenTvUsesAliasAndPreferredWebpAndPreservesOverlayFlag() throws Exception {
        Map<String, Emote> emotes = EmoteProviders.sevenTv("{\"emote_set\":{\"emotes\":[{\"name\":\"Alias\",\"flags\":1,\"data\":{\"animated\":true,\"host\":{\"url\":\"//cdn.7tv.app/emote/example\",\"files\":[{\"name\":\"1x.webp\",\"format\":\"WEBP\"},{\"name\":\"2x.webp\",\"format\":\"WEBP\"}]}}}]}}", false);
        Emote emote = emotes.get("Alias");
        assertEquals("https://cdn.7tv.app/emote/example/2x.webp", emote.url);
        assertTrue(emote.animated);
        assertTrue(emote.overlay);
    }

    @Test public void ffzUsesEmoticonsAndRecognizesAnimatedAndModifier() throws Exception {
        String json = "{\"sets\":{\"1\":{\"emoticons\":[{\"id\":101,\"name\":\"FFZStatic\",\"urls\":{\"1\":\"//cdn.frankerfacez.com/emoticon/101/1\",\"2\":\"//cdn.frankerfacez.com/emoticon/101/2\"}}," +
                "{\"id\":102,\"name\":\"FFZAnim\",\"modifier\":true,\"urls\":{\"1\":\"//cdn.frankerfacez.com/emoticon/102/1\"}," +
                "\"animated\":{\"2\":\"//cdn.frankerfacez.com/emoticon/102/animated/2\"}}]}}}";
        Map<String, Emote> emotes = EmoteProviders.ffz(json, false);
        assertEquals(2, emotes.size());
        Emote st = emotes.get("FFZStatic");
        assertEquals("https://cdn.frankerfacez.com/emoticon/101/2", st.url);
        assertFalse(st.animated);
        assertFalse(st.overlay);

        Emote anim = emotes.get("FFZAnim");
        assertEquals("https://cdn.frankerfacez.com/emoticon/102/animated/2", anim.url);
        assertTrue(anim.animated);
        assertTrue(anim.overlay);
    }

    @Test public void malformedEntriesAndUntrustedImageHostsAreIgnored() throws Exception {
        assertTrue(EmoteProviders.bttv("[{\"code\":\"two words\",\"id\":\"ok\"},{\"code\":\"ok\",\"id\":\"../other\"}]", true).isEmpty());
        assertFalse(EmoteProviders.imageUrl("http://cdn.7tv.app/a.webp"));
        assertFalse(EmoteProviders.imageUrl("https://cdn.7tv.app.example/a.webp"));
        assertFalse(EmoteProviders.imageUrl("https://user@cdn.7tv.app/a.webp"));
        assertFalse(EmoteProviders.imageUrl("https://cdn.7tv.app:8080/a.webp"));
        assertTrue(EmoteProviders.imageUrl("https://cdn.frankerfacez.com/emoticon/1/1"));
        assertTrue(EmoteProviders.imageUrl("https://cdn.ffz.me/emoticon/1/1"));
    }

    @Test public void channelAndProviderPrecedenceIsDeterministic() {
        Emote global = new Emote("Code", "global", false, false);
        Emote bttv = new Emote("Code", "bttv", false, false);
        Emote seven = new Emote("Code", "seven", false, false);
        Emote ffz = new Emote("Code", "ffz", false, false);
        Map<String, Emote> globals = Collections.singletonMap("Code", global);
        assertSame(bttv, EmoteCatalog.combine(globals, Collections.singletonMap("Code", bttv), Collections.emptyMap(), Collections.emptyMap()).get("Code"));
        assertSame(seven, EmoteCatalog.combine(globals, Collections.singletonMap("Code", bttv), Collections.singletonMap("Code", seven), Collections.emptyMap()).get("Code"));
        assertSame(ffz, EmoteCatalog.combine(globals, Collections.singletonMap("Code", bttv), Collections.singletonMap("Code", seven), Collections.singletonMap("Code", ffz)).get("Code"));
        assertSame(global, globals.get("Code"));
    }

    @Test public void channelIdsCannotInjectRequestPaths() {
        assertEquals("123", EmoteProviders.channelId("123"));
        assertNull(EmoteProviders.channelId("123/../../other"));
        assertNull(EmoteProviders.channelId("channel-name"));
        assertNull(EmoteProviders.channelId("0"));
    }

    @Test public void absentProviderChannelIsAnEmptyCatalog() throws Exception {
        assertTrue(EmoteProviders.bttv("{}", false).isEmpty());
        assertTrue(EmoteProviders.sevenTv("{}", false).isEmpty());
        assertTrue(EmoteProviders.ffz("{}", false).isEmpty());
    }
}
