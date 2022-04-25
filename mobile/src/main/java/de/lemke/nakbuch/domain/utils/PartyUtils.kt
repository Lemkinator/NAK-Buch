package de.lemke.nakbuch.domain.utils

import App
import android.content.Context
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import de.lemke.nakbuch.R
import nl.dionsegijn.konfetti.core.*
import nl.dionsegijn.konfetti.core.emitter.Emitter
import nl.dionsegijn.konfetti.core.models.Size
import nl.dionsegijn.konfetti.xml.KonfettiView
import java.util.concurrent.TimeUnit

class PartyUtils {
    companion object{
        fun discoverEasterEgg(
            mContext: Context,
            konfettiView: KonfettiView,
            easterEggEntryNameResourceId: Int
        ) = discoverEasterEgg(
            mContext,
            konfettiView,
            mContext.getString(easterEggEntryNameResourceId)
        )

        fun discoverEasterEgg(
            mContext: Context,
            konfettiView: KonfettiView,
            easterEggEntryName: String
        ) {
            val sp = App.myRepository.getDefaultSharedPreferences()
            if (sp.getBoolean("easterEggs", true)) {
                val s: MutableSet<String> =
                    HashSet(sp.getStringSet("discoveredEasterEggs", HashSet())!!)
                if (!s.contains(easterEggEntryName)) {
                    s.add(easterEggEntryName)
                    sp.edit().putStringSet("discoveredEasterEggs", s).apply()
                    showKonfetti(konfettiView)
                    Toast.makeText(
                        mContext,
                        mContext.getString(R.string.easterEggDiscovered) + easterEggEntryName,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        const val partyDelay2 = 400L
        const val partyDelay3 = 800L
        fun party(): List<Party> {
            return listOf(
                party1(),
                party2(),
                party3(),
            )
        }

        fun showKonfetti(konfettiView: KonfettiView) {
            konfettiView.start(party1())
            Handler(Looper.getMainLooper()).postDelayed(
                { konfettiView.start(party2()) },
                partyDelay2
            )
            Handler(Looper.getMainLooper()).postDelayed(
                { konfettiView.start(party3()) },
                partyDelay3
            )
        }

        fun party1(): Party {
            return Party(
                speed = 0f,
                maxSpeed = 50f,
                damping = 0.9f,
                angle = Angle.TOP,
                spread = 360,
                size = listOf(Size.SMALL, Size.LARGE),
                timeToLive = 3000L,
                rotation = Rotation(),
                colors = listOf(
                    0xffcd2e,
                    0xff261f,
                    0x0fff2b,
                    0xaa00ff,
                    0x0400ff
                ), //listOf(0xfce18a, 0xff726d, 0xf4306d, 0xb48def),
                emitter = Emitter(duration = 200, TimeUnit.MILLISECONDS).max(100),
                position = Position.Relative(0.2, 0.2)
                //.shapes(Arrays.asList(Shape.Square.INSTANCE, Shape.Circle.INSTANCE, new Shape.DrawableShape(), true))
            )
        }

        fun party2(): Party {
            return party1().copy(
                position = Position.Relative(0.5, 0.6)
            )
        }

        fun party3(): Party {
            return party1().copy(
                position = Position.Relative(0.8, 0.2)
            )
        }

        fun festive(): List<Party> {
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

        fun explode(): List<Party> {
            return listOf(
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
        }

        fun parade(): List<Party> {
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

        fun rain(): List<Party> {
            return listOf(
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
    }
}