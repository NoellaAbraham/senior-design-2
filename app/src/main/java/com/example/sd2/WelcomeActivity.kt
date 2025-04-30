package com.example.sd2

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout

class WelcomeActivity : AppCompatActivity() {

    data class GameCard(
        val title: String,
        val levels: String,
        val iconResId: Int,
        val backgroundColor: Int,
        val nextActivity: Class<*>
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_welcome)

        // Setup Drawer
        val drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        val navView = findViewById<NavigationView>(R.id.navigation_view)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navView.setNavigationItemSelectedListener { menuItem: MenuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> { /* Already here */ }
                R.id.nav_dashboard -> startActivity(Intent(this, DashboardTest::class.java))
                R.id.nav_chatbot -> startActivity(Intent(this, Chatbot::class.java))
                R.id.nav_appointments -> startActivity(Intent(this, DoctorsAppointment::class.java))
                R.id.nav_resources -> startActivity(Intent(this, ResourcesActivity::class.java))
                R.id.nav_feedback -> startActivity(Intent(this, FeedbackActivity::class.java))
                R.id.nav_logout -> {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                }
            }
            drawerLayout.closeDrawer(GravityCompat.START)
            true
        }

        // Setup dynamic game cards
        val gameContainer = findViewById<LinearLayout>(R.id.gameContainer)

        val gameList = listOf(
            GameCard("GAME 1", "2 Levels", R.drawable.puzzle, Color.parseColor("#A2EBF4"), Game1Lev0::class.java),
            GameCard("GAME 2", "4 Levels", R.drawable.brain, Color.parseColor("#FBC4AB"), Game2Lev0::class.java),
            GameCard("GAME 3", "2 Levels", R.drawable.match, Color.parseColor("#FFD6A5"), Game3Lev2::class.java),
            GameCard("GAME 4", "1 Level", R.drawable.ai_game, Color.parseColor("#C1FFD7"), Game4Level1::class.java)
        )

        for (game in gameList) {
            val cardView = LayoutInflater.from(this)
                .inflate(R.layout.item_card_game, gameContainer, false) as CardView

            cardView.setCardBackgroundColor(game.backgroundColor)

            val imageView = cardView.findViewById<ImageView>(R.id.cardIcon)
            val titleView = cardView.findViewById<TextView>(R.id.cardTitle)
            val levelView = cardView.findViewById<TextView>(R.id.cardLevels)

            imageView.setImageResource(game.iconResId)
            titleView.text = game.title
            levelView.text = game.levels

            cardView.setOnClickListener {
                // Show focus popup when user clicks
                val popup = FocusPopupDialogFragment.newInstance(game.nextActivity)
                popup.show(supportFragmentManager, "FocusPopup")
            }

            gameContainer.addView(cardView)
        }
    }
}
