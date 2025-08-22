package com.example.v02.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.VpnKey
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.v02.ReelsBlockingService.BlockedKeywordLists
import com.example.v02.ReelsBlockingService.MainViewModel


@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BlockKeywordsScreen(
    navController: NavController,
    viewModel: MainViewModel = viewModel()
) {
    val savedLists by viewModel.blockedKeywordLists.collectAsState()
    val savedCustomKeywords by viewModel.customBlockedKeywords.collectAsState()

    var adult by remember { mutableStateOf(false) }
    var gambling by remember { mutableStateOf(false) }
    var violent by remember { mutableStateOf(false) }
    var hate by remember { mutableStateOf(false) }
    var drug by remember { mutableStateOf(false) }
    var scam by remember { mutableStateOf(false) }

    var customKeyword by remember { mutableStateOf("") }
    var customKeywords by remember { mutableStateOf(listOf<String>()) }

    LaunchedEffect(savedLists, savedCustomKeywords) {
        adult = savedLists.adult
        gambling = savedLists.gambling
        violent = savedLists.violent
        hate = savedLists.hate
        drug = savedLists.drug
        scam = savedLists.scam
        customKeywords = savedCustomKeywords
    }

    Scaffold(
        topBar = {
            SmallTopAppBar(
                title = { Text("Block Keywords") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text("Blocked Keyword Lists", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            }
            item { KeywordToggleRow("Adult Keywords", adult) { adult = it } }
            item { KeywordToggleRow("Gambling Keywords", gambling) { gambling = it } }
            item { KeywordToggleRow("Violent Keywords", violent) { violent = it } }
            item { KeywordToggleRow("Hate Keywords", hate) { hate = it } }
            item { KeywordToggleRow("Drug / Substance Abuse Keywords", drug) { drug = it } }
            item { KeywordToggleRow("Scam / Fraud Keywords", scam) { scam = it } }

            // ✅ Custom keyword input
            item {
                OutlinedTextField(
                    value = customKeyword,
                    onValueChange = { customKeyword = it },
                    label = { Text("Enter keyword") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
                Spacer(Modifier.height(8.dp))
                Button(onClick = {
                    if (customKeyword.isNotBlank()) {
                        customKeywords = customKeywords + customKeyword.trim()
                        customKeyword = ""
                    }
                }) { Text("+ Add Keyword") }
            }

            // ✅ Comma-separated custom keywords with cross icon
            item {
                if (customKeywords.isEmpty()) {
                    Text("No custom keywords added.")
                } else {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        customKeywords.forEachIndexed { index, keyword ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(end = 4.dp)
                            ) {
                                Text(
                                    text = keyword,
                                    fontSize = MaterialTheme.typography.bodyLarge.fontSize, // ⬆️ Bigger font
                                    modifier = Modifier.padding(end = 4.dp)
                                )
                                IconButton(
                                    onClick = { customKeywords = customKeywords - keyword },
                                    modifier = Modifier.size(20.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Cancel,
                                        contentDescription = "Remove",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                                if (index != customKeywords.lastIndex) {
                                    Text(", ")
                                }
                            }
                        }
                    }
                }
            }

            item {
                Spacer(Modifier.height(24.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") }
                    Button(onClick = {
                        viewModel.setBlockedKeywordLists(
                            BlockedKeywordLists(adult, gambling, violent, hate, drug, scam)
                        )
                        viewModel.setCustomBlockedKeywords(customKeywords)
                        navController.popBackStack()
                    }) { Text("Save") }
                }
            }
        }
    }
}

@Composable
fun KeywordToggleRow(label: String, isChecked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
    ) {
        Icon(Icons.Default.VpnKey, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
        Spacer(Modifier.width(8.dp))
        Text(label, modifier = Modifier.weight(1f))
        Switch(checked = isChecked, onCheckedChange = onCheckedChange)
    }
}

// ✅ Predefined keyword lists
object KeywordLists {
    val adult = listOf("porn", "sex", "xxx", "nude", "erotic", "hot", "boobs", "ass")
    val gambling = listOf("casino", "bet", "poker", "roulette", "gamble", "lottery", "jackpot")
    val violent = listOf("kill", "murder", "blood", "fight", "war", "gun", "knife")
    val hate = listOf("hate", "racist", "terrorist", "nazi", "homophobic", "slur")
    val drug = listOf("cocaine", "weed", "marijuana", "heroin", "meth", "ecstasy", "drug")
    val scam = listOf("scam", "fraud", "phishing", "fake", "hacked", "malware")
}
