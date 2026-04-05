package com.example.myapplication

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { App() }
    }
}

//////////////////////////////////////////////////
// MAIN NAVIGATION
//////////////////////////////////////////////////

@Composable
fun App() {
    var selectedScreen by remember { mutableStateOf("card") }

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    selected = selectedScreen == "card",
                    onClick = { selectedScreen = "card" },
                    label = { Text("Card") },
                    icon = { Text("🎴") }
                )
                NavigationBarItem(
                    selected = selectedScreen == "trivia",
                    onClick = { selectedScreen = "trivia" },
                    label = { Text("Ask") },
                    icon = { Text("🔮") }
                )
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding)) {
            if (selectedScreen == "card") CardGameScreen()
            else TriviaScreen()
        }
    }
}

//////////////////////////////////////////////////
// 🎴 CARD GAME
//////////////////////////////////////////////////

@Composable
fun CardGameScreen() {
    val scope = rememberCoroutineScope()

    var isLoading by remember { mutableStateOf(false) }
    var cardImage by remember { mutableStateOf("") }
    var correctAnswer by remember { mutableStateOf("") }
    var options by remember { mutableStateOf(listOf<String>()) }
    var result by remember { mutableStateOf("") }
    var revealed by remember { mutableStateOf(false) }

    fun loadCard() {
        isLoading = true

        scope.launch(Dispatchers.IO) {
            val res = URL("https://deckofcardsapi.com/api/deck/new/draw/?count=1").readText()
            val card = JSONObject(res).getJSONArray("cards").getJSONObject(0)

            val value = card.getString("value")
            val suit = card.getString("suit")
            val correct = "$value of $suit"

            val values = listOf("ACE","2","3","4","5","6","7","8","9","10","JACK","QUEEN","KING")
            val suits = listOf("SPADES","HEARTS","CLUBS","DIAMONDS")

            val fakeOptions = List(3) {
                "${values.random()} of ${suits.random()}"
            }

            withContext(Dispatchers.Main) {
                cardImage = card.getString("image")
                correctAnswer = correct
                options = (fakeOptions + correct).shuffled()
                result = ""
                revealed = false
                isLoading = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("🎴 Guess the Card", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        Button(onClick = { loadCard() }, enabled = !isLoading) {
            Text("Draw Card")
        }

        Spacer(Modifier.height(20.dp))

        if (isLoading) {
            CircularProgressIndicator()
        }

        // CARD DISPLAY
        Card(elevation = CardDefaults.cardElevation(10.dp)) {
            if (!revealed) {
                Box(
                    modifier = Modifier
                        .size(200.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6))
                            ),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎴", color = Color.White, style = MaterialTheme.typography.displayMedium)
                }
            } else {
                AsyncImage(
                    model = cardImage,
                    contentDescription = null,
                    modifier = Modifier.size(200.dp)
                )
            }
        }

        Spacer(Modifier.height(20.dp))

        if (!revealed) {
            options.forEach { option ->
                Button(
                    onClick = {
                        revealed = true

                        scope.launch(Dispatchers.IO) {
                            if (option == correctAnswer) {
                                val res = URL("https://api.kanye.rest").readText()
                                val quote = JSONObject(res).getString("quote")

                                withContext(Dispatchers.Main) {
                                    result = "✅ Correct!\n\n🎤 $quote"
                                }
                            } else {
                                val res = URL("https://evilinsult.com/generate_insult.php?lang=en&type=json").readText()
                                val insult = JSONObject(res).getString("insult")

                                withContext(Dispatchers.Main) {
                                    result = "❌ Wrong!\n\n🔥 $insult"
                                }
                            }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().padding(4.dp)
                ) {
                    Text(option)
                }
            }
        }

        Spacer(Modifier.height(20.dp))

        Text(result)

        Spacer(Modifier.height(20.dp))

        if (revealed) {
            Button(onClick = { loadCard() }) {
                Text("Play Again 🔄")
            }
        }
    }
}

//////////////////////////////////////////////////
// 🎲 ASK MODE (YES/NO + DOG API)
//////////////////////////////////////////////////

@Composable
fun TriviaScreen() {
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var question by remember { mutableStateOf("") }
    var answer by remember { mutableStateOf("") }
    var gif by remember { mutableStateOf("") }
    var dogImage by remember { mutableStateOf("") }
    var loading by remember { mutableStateOf(false) }

    fun askQuestion() {
        if (question.isEmpty()) return

        loading = true

        scope.launch(Dispatchers.IO) {
            try {
                val res = URL("https://yesno.wtf/api").readText()
                val obj = JSONObject(res)

                val ans = obj.getString("answer")
                val img = obj.getString("image")

                val dogRes = URL("https://dog.ceo/api/breeds/image/random").readText()
                val dogObj = JSONObject(dogRes)
                val dog = dogObj.getString("message")

                withContext(Dispatchers.Main) {
                    answer = ans
                    gif = img
                    dogImage = dog
                    loading = false
                }

            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    loading = false
                    answer = "Error 😢"
                }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text("🔮 Ask Anything", style = MaterialTheme.typography.headlineMedium)

        Spacer(Modifier.height(20.dp))

        OutlinedTextField(
            value = question,
            onValueChange = { question = it },
            label = { Text("Ask a yes/no question") }
        )

        Spacer(Modifier.height(20.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                askQuestion()
            },
            enabled = !loading
        ) {
            Text("Ask 🔮")
        }

        Spacer(Modifier.height(20.dp))

        if (loading) {
            CircularProgressIndicator()
        }

        if (gif.isNotEmpty()) {
            AsyncImage(
                model = gif,
                contentDescription = null,
                modifier = Modifier.size(250.dp)
            )
        }

        Spacer(Modifier.height(20.dp))

        if (answer.isNotEmpty()) {
            Text(
                answer.uppercase(),
                style = MaterialTheme.typography.headlineLarge
            )
        }

        Spacer(Modifier.height(20.dp))

        if (dogImage.isNotEmpty()) {
            AsyncImage(
                model = dogImage,
                contentDescription = null,
                modifier = Modifier.size(200.dp)
            )
        }
    }
}