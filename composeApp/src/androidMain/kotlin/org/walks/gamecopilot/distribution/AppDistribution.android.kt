package org.walks.gamecopilot.distribution

import org.walks.gamecopilot.BuildConfig

internal actual fun platformDistributionChannel(): DistributionChannel =
    DistributionChannel.fromId(BuildConfig.DISTRIBUTION_CHANNEL)
