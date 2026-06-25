package com.devindie.cmptemplate.navigation

import androidx.navigation3.runtime.NavKey
import androidx.savedstate.serialization.SavedStateConfiguration
import com.devindie.cmptemplate.core.navigation.MainRoute
import com.devindie.cmptemplate.core.navigation.MainShellRoute
import com.devindie.cmptemplate.feature.carddetail.api.CardDetailRoute
import com.devindie.cmptemplate.feature.onboarding.api.OnboardingRoute
import com.devindie.cmptemplate.feature.splash.api.SplashRoute
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import kotlinx.serialization.modules.subclass

/**
 * Polymorphic [NavKey] serialization for non-JVM targets (iOS).
 *
 * @see [androidx.navigation3.runtime.rememberNavBackStack]
 */
val navKeysSavedStateConfiguration: SavedStateConfiguration =
    SavedStateConfiguration {
        serializersModule =
            SerializersModule {
                polymorphic(NavKey::class) {
                    subclass(SplashRoute::class, SplashRoute.serializer())
                    subclass(OnboardingRoute::class, OnboardingRoute.serializer())
                    subclass(MainShellRoute::class, MainShellRoute.serializer())
                    subclass(MainRoute.Browse::class, MainRoute.Browse.serializer())
                    subclass(MainRoute.Cart::class, MainRoute.Cart.serializer())
                    subclass(MainRoute.Collection::class, MainRoute.Collection.serializer())
                    subclass(MainRoute.Profile::class, MainRoute.Profile.serializer())
                    subclass(CardDetailRoute::class, CardDetailRoute.serializer())
                }
            }
    }
