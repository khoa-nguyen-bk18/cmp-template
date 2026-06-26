package com.devindie.cmptemplate.feature.onboarding.impl

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Collections
import androidx.compose.material.icons.outlined.Explore
import androidx.compose.material.icons.outlined.RocketLaunch

internal object OnboardingPages {
    val default =
        listOf(
            OnboardingPage(
                title = "Welcome",
                body = "Discover and collect your favorite cards.",
                icon = Icons.Outlined.Explore,
            ),
            OnboardingPage(
                title = "Browse",
                body = "Swipe through curated collections anytime.",
                icon = Icons.Outlined.Collections,
            ),
            OnboardingPage(
                title = "Get started",
                body = "Your collection is ready — dive in.",
                icon = Icons.Outlined.RocketLaunch,
            ),
        )
}
