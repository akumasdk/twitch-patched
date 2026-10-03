package dev.twitchpatches.patches.twitch.shared

import app.morphe.patcher.patch.bytecodePatch

internal val twitchExtensionPatch = bytecodePatch {
    extendWith("extensions/twitch.mpe")
}
