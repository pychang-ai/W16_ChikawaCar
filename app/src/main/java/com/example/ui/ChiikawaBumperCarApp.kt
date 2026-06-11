package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.ScoreEntity
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

// Game entities helper
data class PhysicsItem(
    val id: Int,
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    val radius: Float,
    val isGoldApple: Boolean,
    val isStone: Boolean,
    var rotation: Float = 0f
)

data class FloatingMessage(
    val text: String,
    val color: Color,
    var x: Float,
    var y: Float,
    var age: Int,
    val maxAge: Int = 40
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChiikawaBumperCarApp(
    viewModel: GameViewModel,
    modifier: Modifier = Modifier
) {
    val screenState by viewModel.screenState.collectAsStateWithLifecycle()
    val selectedChar by viewModel.selectedCharacter.collectAsStateWithLifecycle()
    val finalScore by viewModel.currentScore.collectAsStateWithLifecycle()
    val topScores by viewModel.topScores.collectAsStateWithLifecycle()

    val pastelGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFFFFF0F3), // Immersive soft blush pink
            Color(0xFFFFD1DC)  // Soft candy pink blush
        )
    )

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 4.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(38.dp)
                                    .clip(CircleShape)
                                    .background(Color(0xFFFFB2C5))
                                    .border(2.dp, Color.White, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = when (selectedChar) {
                                        CharacterType.CHIIKAWA -> "🐰"
                                        CharacterType.HACHIWARE -> "🐱"
                                        CharacterType.USAGI -> "🐹"
                                    },
                                    fontSize = 18.sp
                                )
                            }
                            Column {
                                Text(
                                    text = "小可愛碰碰車",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color(0xFF4A2D35),
                                    lineHeight = 18.sp
                                )
                                Text(
                                    text = "Lv. 12 ${selectedChar.displayName.split(" ")[0]}",
                                    fontSize = 11.sp,
                                    color = Color(0xFF4A2D35).copy(alpha = 0.7f),
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                        
                        // Status pill representing the top registered score high mark
                        val mostHigh = topScores.firstOrNull()?.score ?: 1240
                        Row(
                            modifier = Modifier
                                .background(Color(0xFFFFD1DC), RoundedCornerShape(20.dp))
                                .border(1.5.dp, Color.White, RoundedCornerShape(20.dp))
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                                .shadow(1.dp, RoundedCornerShape(20.dp)),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = "🍎 $mostHigh",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF4A2D35)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = Color(0xFFFFF0F3)
                ),
                modifier = Modifier.shadow(2.dp)
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(pastelGradient)
        ) {
            AnimatedContent(
                targetState = screenState,
                transitionSpec = {
                    fadeIn() togetherWith fadeOut()
                },
                label = "ScreenTransition"
            ) { targetScreen ->
                when (targetScreen) {
                    ScreenState.START -> StartScreen(
                        onStartGame = { viewModel.setScreen(ScreenState.CHARACTER_SELECT) },
                        onViewLeaderboard = { viewModel.setScreen(ScreenState.LEADERBOARD) }
                    )
                    ScreenState.CHARACTER_SELECT -> CharacterSelectScreen(
                        selectedChar = selectedChar,
                        onSelect = { viewModel.selectCharacter(it) },
                        onBack = { viewModel.setScreen(ScreenState.START) },
                        onStart = {
                            SoundEffects.playStartSound()
                            viewModel.startNewGame()
                        }
                    )
                    ScreenState.PLAYING -> GameArenaScreen(
                        character = selectedChar,
                        onGameFinish = { score ->
                            viewModel.updateScore(score)
                            SoundEffects.playGameOverSound()
                            viewModel.setScreen(ScreenState.GAME_OVER)
                        }
                    )
                    ScreenState.GAME_OVER -> GameOverScreen(
                        score = finalScore,
                        character = selectedChar,
                        onSave = { name -> viewModel.saveGameScore(name) },
                        onRestart = { viewModel.setScreen(ScreenState.CHARACTER_SELECT) }
                    )
                    ScreenState.LEADERBOARD -> LeaderboardScreen(
                        scores = topScores,
                        onBack = { viewModel.setScreen(ScreenState.START) },
                        onRestart = { viewModel.setScreen(ScreenState.CHARACTER_SELECT) },
                        onClearAll = { viewModel.clearAllScores() }
                    )
                }
            }
        }
    }
}

// 1. HOME SCREEN
@Composable
fun StartScreen(
    onStartGame: () -> Unit,
    onViewLeaderboard: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo illustration drawn in real-time
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(Color.White)
                .border(4.dp, Color(0xFFFFB2C5), RoundedCornerShape(32.dp))
                .shadow(4.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Background cloud
                drawCircle(
                    color = Color(0xFFFFF0F3),
                    radius = 90f,
                    center = Offset(size.width / 2, size.height / 2 - 10f)
                )

                // Draw Chiikawa sitting in a center pink bumper car
                ChiikawaDrawers.drawCharacterWithCar(
                    drawScope = this,
                    charType = CharacterType.CHIIKAWA,
                    center = Offset(size.width / 2, size.height / 2 + 10f),
                    radius = 80f,
                    headingAngle = -10f,
                    bounceOffset = 0f,
                    isBumping = false
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "吉伊卡哇碰碰車派對",
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF4A2D35),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "收集美味蘋果得分 🍎 避開頑固大石頭 🪨",
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4A2D35).copy(alpha = 0.8f),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(36.dp))

        Button(
            onClick = onStartGame,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(60.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), clip = false)
                .testTag("start_game_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF4D6D)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("開始新遊戲 🎮", fontSize = 18.sp, fontWeight = FontWeight.Black, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onViewLeaderboard,
            modifier = Modifier
                .fillMaxWidth(0.8f)
                .height(56.dp)
                .testTag("view_leaderboard_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFFD1DC),
                contentColor = Color(0xFF4A2D35)
            ),
            shape = RoundedCornerShape(20.dp),
            elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
        ) {
            Icon(Icons.Filled.List, contentDescription = null, tint = Color(0xFF4A2D35))
            Spacer(modifier = Modifier.width(8.dp))
            Text("天梯排行榜 🏆", fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

// 2. CHARACTER SELECT SCREEN
@Composable
fun CharacterSelectScreen(
    selectedChar: CharacterType,
    onSelect: (CharacterType) -> Unit,
    onBack: () -> Unit,
    onStart: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = "選擇你的小小賽車手 🚗",
            fontSize = 22.sp,
            fontWeight = FontWeight.Black,
            color = Color(0xFF4A2D35)
        )
        Text(
            text = "每個小可愛都有不同的碰碰車屬性喔！",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF4A2D35).copy(alpha = 0.7f)
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Large display of selected character preview with beautiful bubblegum borders
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(CircleShape)
                .background(Color(selectedChar.colorHex))
                .border(4.dp, Color(0xFFFFB2C5), CircleShape)
                .shadow(4.dp, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                ChiikawaDrawers.drawCharacterWithCar(
                    drawScope = this,
                    charType = selectedChar,
                    center = Offset(size.width / 2, size.height / 2),
                    radius = 65f,
                    headingAngle = 0f,
                    isBumping = false
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = selectedChar.catchphrase,
            fontSize = 18.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFFFF4D6D)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Selection Cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            CharacterType.values().forEach { char ->
                val isSelected = selectedChar == char
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onSelect(char) }
                        .border(
                            width = if (isSelected) 3.dp else 1.dp,
                            color = if (isSelected) Color(0xFFFF4D6D) else Color(0xFFFFB2C5).copy(alpha = 0.6f),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (isSelected) Color(char.colorHex) else Color.White
                    ),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .padding(12.dp)
                            .fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = char.displayName.split(" ")[0],
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF4A2D35),
                            textAlign = TextAlign.Center
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))

                        // Stats indicators
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("速度: ", fontSize = 11.sp, color = Color(0xFF4A2D35).copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                                val speedStars = when(char) {
                                    CharacterType.CHIIKAWA -> 3
                                    CharacterType.HACHIWARE -> 4
                                    CharacterType.USAGI -> 5
                                }
                                Text("⭐".repeat(speedStars), fontSize = 10.sp)
                            }
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text("幸運: ", fontSize = 11.sp, color = Color(0xFF4A2D35).copy(alpha = 0.6f), fontWeight = FontWeight.Medium)
                                val luckStars = when(char) {
                                    CharacterType.CHIIKAWA -> 5
                                    CharacterType.HACHIWARE -> 4
                                    CharacterType.USAGI -> 2
                                }
                                Text("⭐".repeat(luckStars), fontSize = 10.sp)
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Actions
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier
                    .weight(0.4f)
                    .height(56.dp),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(2.dp, Color(0xFFFFB2C5)),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = Color(0xFFFF4D6D)
                )
            ) {
                Text("返回", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Button(
                onClick = onStart,
                modifier = Modifier
                    .weight(0.6f)
                    .height(56.dp)
                    .shadow(elevation = 6.dp, shape = RoundedCornerShape(20.dp), clip = false)
                    .testTag("enter_arena_button"),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF4D6D)
                ),
                shape = RoundedCornerShape(20.dp)
            ) {
                Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(6.dp))
                Text("進入賽場 🏁", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
    }
}

// 3. GAME ARENA SCREEN (THE HEART OF THE GAMEPLAY)
@Composable
fun GameArenaScreen(
    character: CharacterType,
    onGameFinish: (Int) -> Unit
) {
    var score by remember { mutableStateOf(0) }
    var timeLeftMs by remember { mutableStateOf(45000) } // 45 seconds game

    // Arena dimensions retrieved dynamically
    var arenaWidth by remember { mutableStateOf(1080f) }
    var arenaHeight by remember { mutableStateOf(1600f) }

    // Physical Bumper Car properties
    val carRadius = 50f
    var carX by remember { mutableStateOf(0f) }
    var carY by remember { mutableStateOf(0f) }
    var carVx by remember { mutableStateOf(0f) }
    var carVy by remember { mutableStateOf(0f) }
    var carAngle by remember { mutableStateOf(0f) }

    // Drag / Touch target point
    var targetX by remember { mutableStateOf(-1f) }
    var targetY by remember { mutableStateOf(-1f) }

    // Items list (initialized lazily when dimensions are parsed)
    var items by remember { mutableStateOf<List<PhysicsItem>>(emptyList()) }
    var initializedItems by remember { mutableStateOf(false) }

    // Score numbers floating on the screen
    var pops by remember { mutableStateOf<List<FloatingMessage>>(emptyList()) }

    // Crash animations/shaking
    var shakeOffset by remember { mutableStateOf(0f) }
    var isFlashActive by remember { mutableStateOf(false) }
    var collisionFrameCounter by remember { mutableStateOf(0) }
    var animationBounceOffset by remember { mutableStateOf(0f) }

    // Setup visual dimensions once
    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .shadow(12.dp, RoundedCornerShape(40.dp))
            .clip(RoundedCornerShape(40.dp))
            .background(Color(0xFFE2F3E5))
            .border(8.dp, Color.White, RoundedCornerShape(40.dp))
    ) {
        val boxWidth = this.constraints.maxWidth.toFloat()
        val boxHeight = this.constraints.maxHeight.toFloat()
        val widthDp = maxWidth
        val heightDp = maxHeight

        LaunchedEffect(boxWidth, boxHeight) {
            arenaWidth = boxWidth
            arenaHeight = boxHeight
            if (!initializedItems) {
                // Spawn character at center bottom
                carX = arenaWidth / 2f
                carY = arenaHeight * 0.75f
                targetX = carX
                targetY = carY

                val randomizedItems = mutableListOf<PhysicsItem>()
                // Spawn 4 normal apples, 1 golden apple, and 4 rocky stones
                var itemId = 0
                
                // Spawn apples (upper half)
                for (i in 0..3) {
                    randomizedItems.add(
                        PhysicsItem(
                            id = itemId++,
                            x = (0.15f + 0.7f * (i / 4f)) * arenaWidth,
                            y = (0.15f + 0.3f * Math.random().toFloat()) * arenaHeight,
                            vx = (-6f + 12f * Math.random().toFloat()),
                            vy = (-5f + 10f * Math.random().toFloat()),
                            radius = 24f,
                            isGoldApple = false,
                            isStone = false
                        )
                    )
                }

                // Golden Apple (Rare!)
                randomizedItems.add(
                    PhysicsItem(
                        id = itemId++,
                        x = arenaWidth / 2f,
                        y = arenaHeight * 0.25f,
                        vx = (-8f + 16f * Math.random().toFloat()),
                        vy = (-8f + 16f * Math.random().toFloat()),
                        radius = 26f,
                        isGoldApple = true,
                        isStone = false
                    )
                )

                // Spawn 4 Rocky Stones
                for (i in 0..3) {
                    randomizedItems.add(
                        PhysicsItem(
                            id = itemId++,
                            x = (0.2f + 0.6f * (i / 3f)) * arenaWidth,
                            y = (0.4f + 0.2f * Math.random().toFloat()) * arenaHeight,
                            vx = (-4f + 8f * Math.random().toFloat()),
                            vy = (-4f + 8f * Math.random().toFloat()),
                            radius = 32f,
                            isGoldApple = false,
                            isStone = true
                        )
                    )
                }
                items = randomizedItems
                initializedItems = true
            }
        }

        // Active Game Loop (runs every 16ms approx 60 FPS)
        LaunchedEffect(initializedItems) {
            if (!initializedItems) return@LaunchedEffect

            val friction = 0.94f // natural sliding drag of bumper cars
            val charSpeedStat = character.speed

            while (timeLeftMs > 0) {
                delay(16)
                timeLeftMs -= 16

                // 1. CAR PHYSICS
                if (targetX >= 0 && targetY >= 0) {
                    val dx = targetX - carX
                    val dy = targetY - carY
                    val distance = sqrt(dx * dx + dy * dy)

                    if (distance > 6f) {
                        // Apply acceleration force towards driving target
                        val forceScalar = 0.65f * charSpeedStat
                        carVx += (dx / distance) * forceScalar
                        carVy += (dy / distance) * forceScalar
                    }
                }

                // Apply sliding friction drag
                carVx *= friction
                carVy *= friction

                // Update position
                carX += carVx
                carY += carVy

                // Calculate heading rotation based on speed vector
                if (abs(carVx) > 0.4f || abs(carVy) > 0.4f) {
                    val angleRad = atan2(carVy, carVx)
                    carAngle = Math.toDegrees(angleRad.toDouble()).toFloat() + 90f
                }

                // Bounce off Arena Walls
                if (carX < carRadius) {
                    carX = carRadius
                    carVx = -carVx * 0.4f
                    shakeOffset = 6f
                } else if (carX > arenaWidth - carRadius) {
                    carX = arenaWidth - carRadius
                    carVx = -carVx * 0.4f
                    shakeOffset = 6f
                }

                if (carY < carRadius) {
                    carY = carRadius
                    carVy = -carVy * 0.4f
                    shakeOffset = 6f
                } else if (carY > arenaHeight - carRadius) {
                    carY = arenaHeight - carRadius
                    carVy = -carVy * 0.4f
                    shakeOffset = 6f
                }

                // 2. ITEMS PHYSICS (Movement & Bouncings)
                val newList = items.map { item ->
                    // Apply movement
                    var itemX = item.x + item.vx
                    var itemY = item.y + item.vy
                    var ivx = item.vx
                    var ivy = item.vy

                    // Item borders bounce
                    if (itemX < item.radius) {
                        itemX = item.radius
                        ivx = -ivx
                    } else if (itemX > arenaWidth - item.radius) {
                        itemX = arenaWidth - item.radius
                        ivx = -ivx
                    }

                    if (itemY < item.radius) {
                        itemY = item.radius
                        ivy = -ivy
                    } else if (itemY > arenaHeight - item.radius) {
                        itemY = arenaHeight - item.radius
                        ivy = -ivy
                    }

                    item.copy(x = itemX, y = itemY, vx = ivx, vy = ivy, rotation = item.rotation + 2f)
                }

                // 3. COLLISION HANDLING (Car & Items)
                val checkedList = newList.map { item ->
                    val dx = item.x - carX
                    val dy = item.y - carY
                    val dist = sqrt(dx * dx + dy * dy)
                    val collisionDist = carRadius + item.radius

                    if (dist < collisionDist) {
                        // Collision triggered!
                        if (!item.isStone) {
                            // Hit Apple!
                            SoundEffects.playAppleSound()
                            animationBounceOffset = -15f // jump bumper car visually!

                            val pointsScored = if (item.isGoldApple) {
                                // Golden reward based on luck modifiers
                                val luckBonus = (character.luck * 15f + 15f).toInt()
                                pops = pops + FloatingMessage(
                                    text = "哇哈! +$luckBonus ✨🌟",
                                    color = Color(0xFFFFD700),
                                    x = item.x,
                                    y = item.y - 10f,
                                    age = 0
                                )
                                luckBonus
                            } else {
                                val standardPoints = (10 * character.luck).toInt()
                                pops = pops + FloatingMessage(
                                    text = "+$standardPoints 🍎",
                                    color = Color(0xFFE53935),
                                    x = item.x,
                                    y = item.y - 10f,
                                    age = 0
                                )
                                standardPoints
                            }
                            score += pointsScored

                            // Respawn apple at random location safely
                            PhysicsItem(
                                id = item.id,
                                x = (0.2f + 0.6f * Math.random().toFloat()) * arenaWidth,
                                y = (0.1f + 0.4f * Math.random().toFloat()) * arenaHeight,
                                vx = (-5f + 10f * Math.random().toFloat()),
                                vy = (-5f + 10f * Math.random().toFloat()),
                                radius = item.radius,
                                isGoldApple = item.isGoldApple,
                                isStone = false
                            )
                        } else {
                            // Hit Stone! Penalty!
                            SoundEffects.playStoneSound()
                            shakeOffset = 18f
                            isFlashActive = true
                            collisionFrameCounter = 10

                            val penalty = 10
                            score = if (score - penalty < 0) 0 else score - penalty

                            pops = pops + FloatingMessage(
                                        text = "-$penalty 💥🪨",
                                color = Color(0xFFD32F2F),
                                x = item.x,
                                y = item.y - 10f,
                                age = 0
                            )

                            // Elastic rebound impulse kickback!
                            val pushDirectionX = (item.x - carX) / dist
                            val pushDirectionY = (item.y - carY) / dist

                            // Bumper car gets thrown back sharply!
                            carVx = -carVx * 0.9f - pushDirectionX * 13f
                            carVy = -carVy * 0.9f - pushDirectionY * 13f

                            // Stone rebounds in opposite direction!
                            PhysicsItem(
                                id = item.id,
                                x = item.x + pushDirectionX * 15f,
                                y = item.y + pushDirectionY * 15f,
                                vx = pushDirectionX * 14f,
                                vy = pushDirectionY * 14f,
                                radius = item.radius,
                                isGoldApple = false,
                                isStone = true
                            )
                        }
                    } else {
                        item
                    }
                }
                items = checkedList

                // 4. FLOATING POPS SIMULATION
                pops = pops.map { pop ->
                    pop.copy(y = pop.y - 2.5f, age = pop.age + 1)
                }.filter { it.age < it.maxAge }

                // 5. SHAKE AND EFFECTS DECAY
                if (shakeOffset > 0.1f) {
                    shakeOffset *= 0.82f
                } else {
                    shakeOffset = 0f
                }

                if (collisionFrameCounter > 0) {
                    collisionFrameCounter--
                    if (collisionFrameCounter == 0) {
                        isFlashActive = false
                    }
                }

                if (animationBounceOffset < 0f) {
                    animationBounceOffset += 1.5f
                } else {
                    animationBounceOffset = 0f
                }
            }

            // GAME OVER - END MATCH
            onGameFinish(score)
        }

        // Direct drag gesture steering area
        Box(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(Unit) {
                    detectDragGestures(
                        onDragStart = { offset ->
                            targetX = offset.x
                            targetY = offset.y
                        },
                        onDrag = { change, _ ->
                            change.consume()
                            targetX = change.position.x
                            targetY = change.position.y
                        },
                        onDragEnd = {
                            targetX = -1f
                            targetY = -1f
                        }
                    )
                }
                .pointerInput(Unit) {
                    detectTapGestures { offset ->
                        targetX = offset.x
                        targetY = offset.y
                    }
                }
        ) {
            // Background Canvas Grid representing a bumper car wooden floor arena
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .graphicsLayer {
                        // Apply dynamic visual shake effect on hit
                        translationX = if (shakeOffset > 0) (-shakeOffset + Math.random().toFloat() * shakeOffset * 2) else 0f
                        translationY = if (shakeOffset > 0) (-shakeOffset + Math.random().toFloat() * shakeOffset * 2) else 0f
                    }
            ) {
                // Radial dot-grid backdrop pattern (Opacity 40%)
                val dotColor = Color(0xFFC8E6C9).copy(alpha = 0.4f)
                val dotSpacing = 24.dp.toPx()
                val dotRadius = 1.5.dp.toPx()
                
                var xCoord = 0f
                while (xCoord < size.width) {
                    var yCoord = 0f
                    while (yCoord < size.height) {
                        drawCircle(
                            color = dotColor,
                            radius = dotRadius,
                            center = Offset(xCoord + dotSpacing / 2f, yCoord + dotSpacing / 2f)
                        )
                        yCoord += dotSpacing
                    }
                    xCoord += dotSpacing
                }

                // Golden background sparkles
                if (initializedItems) {
                    // Draw Apples & Stones
                    for (item in items) {
                        val pos = Offset(item.x, item.y)
                        when {
                            item.isGoldApple -> {
                                ChiikawaDrawers.drawGoldenApple(this, pos, item.radius, item.rotation)
                            }
                            !item.isStone -> {
                                ChiikawaDrawers.drawApple(this, pos, item.radius)
                            }
                            else -> {
                                ChiikawaDrawers.drawStone(this, pos, item.radius)
                            }
                        }
                    }

                    // Draw Bumper Car Player!
                    ChiikawaDrawers.drawCharacterWithCar(
                        drawScope = this,
                        charType = character,
                        center = Offset(carX, carY),
                        radius = carRadius,
                        headingAngle = carAngle,
                        bounceOffset = animationBounceOffset,
                        isBumping = isFlashActive
                    )
                }
            }

            // Draw Red Flash on hard crash!
            if (isFlashActive) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Red.copy(alpha = 0.15f))
                )
            }

            // Draw Real-time floating text scores popping up
            pops.forEach { pop ->
                Text(
                    text = pop.text,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Black,
                    color = pop.color,
                    modifier = Modifier
                        .offset(
                            x = (pop.x / arenaWidth * widthDp.value).dp,
                            y = (pop.y / arenaHeight * heightDp.value).dp
                        )
                        .shadow(1.dp, CircleShape)
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )
            }

            // Scoreboard Header Overlay HUD
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.TopCenter)
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Score card
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "$score",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Black,
                        color = Color(0xFFFF4D6D),
                        modifier = Modifier.testTag("current_score")
                    )
                    Text(
                        text = "SCORE",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF4A2D35).copy(alpha = 0.6f)
                    )
                }

                // Timer card
                Row(
                    modifier = Modifier
                        .background(Color.White.copy(alpha = 0.85f), RoundedCornerShape(16.dp))
                        .border(2.dp, Color.White, RoundedCornerShape(16.dp))
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                        .shadow(2.dp, RoundedCornerShape(16.dp)),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val secondsLeft = (timeLeftMs / 1000f).coerceAtLeast(0f)
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFF4D6D),
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = String.format("%.1fs", secondsLeft),
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (secondsLeft <= 10) Color(0xFFFF4D6D) else Color(0xFF4A2D35)
                    )
                }
            }

            // Touch Drag Guide Tip Overlay on load
            if (timeLeftMs > 40000) {
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(y = 120.dp)
                        .background(Color(0xFF455A64).copy(alpha = 0.8f), RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = "👆 在螢幕上拖曳或點擊，來操控碰碰車吧！",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.White
                    )
                }
            }
        }
    }
}

// 4. GAME OVER SCORE SAVING SCREEN
@Composable
fun GameOverScreen(
    score: Int,
    character: CharacterType,
    onSave: (String) -> Unit,
    onRestart: () -> Unit
) {
    var playerName by remember { mutableStateOf("") }
    
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .shadow(12.dp, RoundedCornerShape(32.dp))
                .border(4.dp, Color(0xFFFFB2C5), RoundedCornerShape(32.dp)),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = RoundedCornerShape(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "比賽結束！🎌",
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFF4A2D35)
                )
                
                Spacer(modifier = Modifier.height(16.dp))

                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(CircleShape)
                        .background(Color(character.colorHex))
                        .border(3.dp, Color(0xFFFFB2C5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        ChiikawaDrawers.drawCharacterWithCar(
                            drawScope = this,
                            charType = character,
                            center = Offset(size.width / 2, size.height / 2),
                            radius = 45f,
                            isBumping = false
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "最終得分",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A2D35).copy(alpha = 0.6f)
                )
                Text(
                    text = "$score",
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Black,
                    color = Color(0xFFFF4D6D),
                    modifier = Modifier.testTag("game_over_score_text")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Divider(color = Color(0xFFFFD1DC), thickness = 1.5.dp)

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "✍️ 登上天梯排行：輸入玩家暱稱",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF4A2D35),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = playerName,
                    onValueChange = { if (it.length <= 12) playerName = it },
                    placeholder = { Text("小可愛玩家", color = Color(0xFF4A2D35).copy(alpha = 0.4f)) },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_input"),
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedTextColor = Color(0xFF4A2D35),
                        unfocusedTextColor = Color(0xFF4A2D35),
                        focusedBorderColor = Color(0xFFFF4D6D),
                        unfocusedBorderColor = Color(0xFFFFB2C5)
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = { onSave(playerName) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                        .testTag("save_score_button"),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = Color(0xFFFF4D6D)
                    ),
                    border = BorderStroke(2.dp, Color(0xFFFFB2C5)),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, tint = Color(0xFFFF4D6D))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("儲存分數並查看排行 🏆", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFF4D6D))
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = onRestart,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFFFD1DC),
                        contentColor = Color(0xFF4A2D35)
                    ),
                    shape = RoundedCornerShape(20.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 2.dp)
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, tint = Color(0xFF4A2D35))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("再試一次 🔄", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A2D35))
                }
            }
        }
    }
}

// 5. LEADERBOARD SCREEN
@Composable
fun LeaderboardScreen(
    scores: List<ScoreEntity>,
    onBack: () -> Unit,
    onRestart: () -> Unit,
    onClearAll: () -> Unit
) {
    val dateFormatter = remember { SimpleDateFormat("MM-dd HH:mm", Locale.getDefault()) }
    var showConfirmDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.Filled.Home, contentDescription = "Home", tint = Color(0xFF4A2D35))
            }
            Text(
                text = "🏆 天梯排行榜 🏆",
                fontSize = 22.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF4A2D35)
            )
            IconButton(onClick = { showConfirmDialog = true }) {
                Icon(Icons.Filled.Delete, contentDescription = "Clear List", tint = Color(0xFFFF4D6D))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (scores.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.Star,
                        contentDescription = null,
                        tint = Color(0xFFFFB2C5),
                        modifier = Modifier.size(64.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("目前尚無排行紀錄", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color(0xFF4A2D35).copy(alpha = 0.6f))
                    Text("趕快啟動新遊戲刷新首登高分吧！", fontSize = 13.sp, color = Color(0xFF4A2D35).copy(alpha = 0.5f))
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .testTag("leaderboard_list"),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                itemsIndexed(scores) { index, record ->
                    val rank = index + 1
                    val cardBg = when (rank) {
                        1 -> Color(0xFFFFF3F5) // Gold pink rank background
                        2 -> Color(0xFFF9F9F9) // Silver grey
                        3 -> Color(0xFFFAF5F6) // Bronze soft pink-brown
                        else -> Color.White
                    }
                    val charType = try {
                        CharacterType.valueOf(record.characterType)
                    } catch (e: Exception) {
                        CharacterType.CHIIKAWA
                    }

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(2.dp, RoundedCornerShape(16.dp))
                            .border(
                                width = if (rank <= 3) 2.dp else 1.dp,
                                color = if (rank == 1) Color(0xFFFFB2C5) else Color(0xFFFFB2C5).copy(alpha = 0.4f),
                                shape = RoundedCornerShape(16.dp)
                            ),
                        colors = CardDefaults.cardColors(containerColor = cardBg),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .padding(12.dp)
                                .fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            // Rank number or badge
                            Box(
                                modifier = Modifier
                                    .size(36.dp)
                                    .clip(CircleShape)
                                    .background(
                                        when (rank) {
                                            1 -> Color(0xFFFFD1DC)
                                            2 -> Color(0xFFECEFF1)
                                            3 -> Color(0xFFF5E0E4)
                                            else -> Color(0xFFFFF0F3)
                                        }
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = if (rank <= 3) "👑" else "$rank",
                                    fontSize = if (rank <= 3) 14.sp else 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (rank <= 3) Color.Unspecified else Color(0xFF4A2D35).copy(alpha = 0.7f)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            // Character Icon Draw
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(Color(charType.colorHex))
                                    .border(1.5.dp, Color(0xFFFFB2C5), CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Canvas(modifier = Modifier.fillMaxSize()) {
                                    ChiikawaDrawers.drawCharacterWithCar(
                                        drawScope = this,
                                        charType = charType,
                                        center = Offset(size.width / 2, size.height / 2),
                                        radius = 21f,
                                        isBumping = false
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = record.playerName,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.ExtraBold,
                                    color = Color(0xFF4A2D35)
                                )
                                Text(
                                    text = "使用: ${charType.displayName.split(" ")[0]} • ${dateFormatter.format(Date(record.timestamp))}",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Medium,
                                    color = Color(0xFF4A2D35).copy(alpha = 0.6f)
                                )
                            }

                            Text(
                                text = "${record.score} 分",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFFF4D6D)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onRestart,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp)
                .shadow(elevation = 8.dp, shape = RoundedCornerShape(24.dp), clip = false)
                .testTag("leaderboard_restart_button"),
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFFFF4D6D)
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("再玩一局 🎮", fontSize = 16.sp, fontWeight = FontWeight.Black, color = Color.White)
        }
        Spacer(modifier = Modifier.height(8.dp))
    }

    // Leaderboard reset confirm dialog
    if (showConfirmDialog) {
        AlertDialog(
            onDismissRequest = { showConfirmDialog = false },
            title = { Text("重置天梯排行榜？", color = Color(0xFF4A2D35), fontWeight = FontWeight.Bold) },
            text = { Text("這將刪除所有存在本機的高分存檔紀錄，此動作無法回復喔！", color = Color(0xFF4A2D35).copy(alpha = 0.8f)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        onClearAll()
                        showConfirmDialog = false
                    }
                ) {
                    Text("確定重置", color = Color(0xFFFF4D6D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showConfirmDialog = false }) {
                    Text("取消", color = Color(0xFF4A2D35).copy(alpha = 0.6f))
                }
            }
        )
    }
}
