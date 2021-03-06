package de.lemke.nakbuch.domain

import android.content.Context
import android.widget.Toast
import dagger.hilt.android.qualifiers.ApplicationContext
import de.lemke.nakbuch.R
import de.lemke.nakbuch.data.UserSettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit
import javax.inject.Inject

class DiscoverEasterEggUseCase @Inject constructor(
    private val getUserSettings: GetUserSettingsUseCase,
    private val userSettingsRepository: UserSettingsRepository,
    @ApplicationContext private val context: Context,
) {
    suspend operator fun invoke(konfettiView: KonfettiView, easterEggEntryNameResourceId: Int) =
        invoke(konfettiView, context.getString(easterEggEntryNameResourceId))

    suspend operator fun invoke(konfettiView: KonfettiView, easterEggEntryName: String) = withContext(Dispatchers.Main) {
        if (getUserSettings().easterEggsEnabled) {
            if (userSettingsRepository.discoverEasterEgg(easterEggEntryName)) {
                Toast.makeText(context, context.getString(R.string.easterEggDiscovered) + easterEggEntryName, Toast.LENGTH_SHORT).show()
                showKonfetti(konfettiView)
            } else Toast.makeText(context, context.getString(R.string.easterEggAlreadyDiscovered), Toast.LENGTH_SHORT).show()
        }
    }


    private suspend fun showKonfetti(konfettiView: KonfettiView) {
        konfettiView.start(party1())
        delay(300)
        konfettiView.start(party2())
        delay(300)
        konfettiView.start(party3())
    }

    @Suppress("unused")
    private fun party(): List<Party> = listOf(party1(), party2(), party3())

    private fun party1(): Party {
        return Party(
            speed = 0f,
            maxSpeed = 70f,
            damping = 0.9f,
            angle = Angle.TOP,
            spread = 360,
            size = listOf(Size.SMALL, Size.LARGE),
            timeToLive = 3000L,
            rotation = Rotation(),
            colors = listOf(0xffcd2e, 0xff261f, 0x0fff2b, 0xaa00ff, 0x0400ff), //listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(180),
            position = Position.Relative(0.2, 0.2)
            //.shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, new Shape.DrawableShape(), true))
        )
    }

    private fun party2(): Party = party1().copy(position = Position.Relative(0.5, 0.6))

    private fun party3(): Party = party1().copy(position = Position.Relative(0.8, 0.2))

    @Suppress("unused")
    private fun festive(): List<Party> {
        val party = Party(
            speed = 30f,
            maxSpeed = 50f,
            damping = 0.9f,
            angle = Angle.TOP,
            spread = 45,
            size = listOf(Size.SMALL, Size.LARGE),
            timeToLive = 3000L,
            rotation = Rotation(),
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(30),
            position = Position.Relative(0.5, 1.0)
        )

        return listOf(
            party,
            party.copy(
                speed = 55f,
                maxSpeed = 65f,
                spread = 10,
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
            ),
            party.copy(
                speed = 50f,
                maxSpeed = 60f,
                spread = 120,
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(40),
            ),
            party.copy(
                speed = 65f,
                maxSpeed = 80f,
                spread = 10,
                emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(10),
            )
        )
    }

    @Suppress("unused")
    private fun explode(): List<Party> = listOf(
        Party(
            speed = 0f,
            maxSpeed = 30f,
            damping = 0.9f,
            spread = 360,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 100, TimeUnit.MILLISECONDS).max(100),
            position = Position.Relative(0.5, 0.3)
        )
    )

    @Suppress("unused")
    private fun parade(): List<Party> {
        val party = Party(
            speed = 10f,
            maxSpeed = 30f,
            damping = 0.9f,
            angle = Angle.RIGHT - 45,
            spread = Spread.SMALL,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(30),
            position = Position.Relative(0.0, 0.5)
        )

        return listOf(
            party,
            party.copy(
                angle = party.angle - 90, // flip angle from right to left
                position = Position.Relative(1.0, 0.5)
            ),
        )
    }

    @Suppress("unused")
    private fun rain(): List<Party> = listOf(
        Party(
            speed = 0f,
            maxSpeed = 15f,
            damping = 0.9f,
            angle = Angle.BOTTOM,
            spread = Spread.ROUND,
            colors = listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
            emitter = Emitter(duration = 5, TimeUnit.SECONDS).perSecond(100),
            position = Position.Relative(0.0, 0.0).between(Position.Relative(1.0, 0.0))
        )
    )
}