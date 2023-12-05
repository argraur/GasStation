package dev.argraur.gasstation.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyHorizontalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ShapeDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.argraur.gasstation.domain.model.CarStatus
import dev.argraur.gasstation.ui.elements.ServerCard
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject

@OptIn(ExperimentalResourceApi::class, ExperimentalMaterial3Api::class)
@Composable
fun App(viewModel: AppViewModel = koinInject()) {
    val serverState by viewModel.serverState.collectAsState()
    val carFlows by viewModel.cars.collectAsState()
    val stationState by viewModel.stationState.collectAsState()

    var pricePerLiter by remember { mutableStateOf(stationState.pricePerLiter.toString()) }

    var carsByStatus by remember { mutableStateOf<Map<CarStatus, List<Int>>>(emptyMap()) }

    val observingJobs = mutableListOf<Job>()

    LaunchedEffect(carFlows) {
        observingJobs.forEach { it.cancel() }
        carFlows.forEach { carFlow ->
            println(carFlow.value.id)
            val job = Job()
            observingJobs.add(job)
            CoroutineScope(job + Dispatchers.Default).launch {
                carFlow.collect { car ->
                    val currentMap = carsByStatus.toMutableMap()
                    val carsInStatus = currentMap.getOrDefault(car.status, emptyList()) + car.id
                    if (!(currentMap[car.status] ?: emptyList()).contains(car.id)) {
                        currentMap.forEach {
                            if (it.value.contains(car.id)){
                                currentMap[it.key] = it.value.filter { it != car.id }
                            }
                        }
                        currentMap[car.status] = carsInStatus
                        carsByStatus = currentMap
                    }
                }
            }
        }
    }

    MaterialTheme(
        colorScheme = darkColorScheme()
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(if (serverState.running) "Server is running at localhost:${serverState.port}" else "Server is not running", fontFamily = FontFamily.Monospace)
                    },
                    actions = {
                        IconButton(
                            onClick = {
                                viewModel.resetStation()
                            },
                        ) {
                            Icon(Icons.Filled.Delete, "Reset station")
                        }
                        IconButton(
                            onClick = {
                                viewModel.startServer()
                            },
                            enabled = !serverState.running
                        ) {
                            Icon(Icons.Filled.PlayArrow, "Start server")
                        }
                        IconButton(
                            onClick = {
                                viewModel.stopServer()
                            },
                            enabled = serverState.running
                        ) {
                            Icon(Icons.Filled.Close, "Stop server")
                        }
                    }
                )
            }
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(it).padding(horizontal = 12.dp).padding(bottom = 12.dp), verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically)) {
                Row(modifier = Modifier.weight(0.7f).padding(bottom = 12.dp), horizontalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterHorizontally)) {
                    ServerCard {
                        Column {
                            Text("Queue", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(carsByStatus[CarStatus.IN_QUEUE] ?: listOf()) {
                                    Text("Car ${it}")
                                }
                            }
                        }
                    }
                    ServerCard {
                        Column {
                            Text("Fueling", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(carsByStatus[CarStatus.FUELING] ?: listOf()) {
                                    Text("Car ${it}")
                                }
                            }
                        }
                    }
                    ServerCard {
                        Column {
                            Text("Buying snacks", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(carsByStatus[CarStatus.STOCKING_UP] ?: listOf()) {
                                    Text("Car ${it}")
                                }
                            }
                        }
                    }
                    ServerCard {
                        Column {
                            Text("In toilet", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(carsByStatus[CarStatus.DRIVER_ON_TOILET_BREAK] ?: listOf()) {
                                    Text("Car ${it}")
                                }
                            }
                        }
                    }
                    ServerCard {
                        Column {
                            Text("Paying", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(carsByStatus[CarStatus.PAYING] ?: listOf()) {
                                    Text("Car ${it}")
                                }
                            }
                        }
                    }
                }
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Text("Total income: $${stationState.income}", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text("Price per liter", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                        OutlinedTextField(
                            modifier = Modifier.width(120.dp).padding(start = 8.dp, end = 24.dp),
                            value = pricePerLiter,
                            onValueChange = {
                                pricePerLiter = it
                                val double = it.toDoubleOrNull()
                                if (double != null) {
                                    viewModel.changePricePerLiter(double)
                                }
                            },
                            maxLines = 1,
                            label = { Text("$") }
                        )
                        Text("# of Pumps", style = MaterialTheme.typography.titleMedium, fontSize = 18.sp)
                        IconButton(onClick = { viewModel.changeMaxPumps(stationState.pumps - 1) }) {
                            Icon(painterResource("remove.xml"), "Remove pump")
                        }
                        Text(stationState.pumps.toString(), fontSize = 18.sp)
                        IconButton(onClick = { viewModel.changeMaxPumps(stationState.pumps + 1) }) {
                            Icon(Icons.Filled.Add, "Add pump")
                        }
                    }
                }
            }

            /*
            AnimatedVisibility(serverState.running) {
                Image(
                    ,
                    null
                )
            }*/
        }
    }
}