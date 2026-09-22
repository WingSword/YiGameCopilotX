package org.walks.gamecopilot.distribution

// Replace only the platform adapter; compile the actual shared policy and clients.
internal fun platformDistributionChannel(): DistributionChannel =
    DistributionChannel.fromId(System.getenv("YIGAME_TEST_CHANNEL") ?: "direct")
