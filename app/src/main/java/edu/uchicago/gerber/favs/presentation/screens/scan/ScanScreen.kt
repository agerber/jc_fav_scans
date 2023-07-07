package edu.uchicago.gerber.favs.presentation.screens.scan

import android.graphics.Bitmap
import android.view.View
import android.widget.Toast
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Menu
import androidx.compose.material3.Button
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.widget.ContentLoadingProgressBar
import androidx.navigation.NavController
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.rememberPermissionState
import edu.uchicago.gerber.favs.R
import edu.uchicago.gerber.favs.common.Constants
import edu.uchicago.gerber.favs.presentation.viewmodels.PokemonViewModel
import edu.uchicago.gerber.favs.presentation.widgets.BottomNavigationBar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.lifecycle.viewmodel.compose.viewModel
import edu.uchicago.gerber.favs.presentation.navigation.Screen

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun ScanScreen(
    navController: NavController,
    pokemonViewModel: PokemonViewModel = viewModel()
) {

    val cameraPermissionState = rememberPermissionState(
        android.Manifest.permission.CAMERA
    )
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var bitmapBuffer: Bitmap? = remember { null }
    var imageAnalyzer: ImageAnalysis?
    var classifier: ImageClassificationHelper? by remember { mutableStateOf(null) }
    val cameraProviderFuture = remember(context) { ProcessCameraProvider.getInstance(context) }
    val cameraProvider = remember(cameraProviderFuture) { cameraProviderFuture.get() }
    val executor = remember(context) { ContextCompat.getMainExecutor(context) }
    var cameraSelector: CameraSelector? by remember { mutableStateOf(null) }
    var preview by remember { mutableStateOf<Preview?>(null) }
    var infTime by remember { mutableStateOf(0L) }
    var delegate by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)

    lateinit var overlay: OverlayView


    Scaffold(
        modifier = Constants.modifier,
        bottomBar = { BottomNavigationBar(navController) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(paddingValues = paddingValues),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            if (cameraPermissionState.hasPermission) {

                Scaffold(
                    topBar = {
                        TopAppBar(
                            title = {},
                            navigationIcon = {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        if (drawerState.isOpen) {
                                            drawerState.close()
                                        } else {
                                            drawerState.open()
                                        }
                                    }
                                }) {
                                    Icon(
                                        Icons.Rounded.Menu,
                                        contentDescription = "MenuButton"
                                    )
                                }
                            }
                        )
                    }
                ) { paddingValues ->
                    ModalNavigationDrawer(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(
                                top = paddingValues.calculateTopPadding()
                            ),
                        drawerState = drawerState,
                        drawerContent = {
                            ModalDrawerSheet(
                                drawerShape = RectangleShape
                            ) {
                                NavigationDrawer(
                                    // will make it dynamic in the other steps
                                    confidence = pokemonViewModel.confidence.value,
                                    inferenceTime = infTime,
                                    delegate = delegate,
                                    onAddCon = {
                                        if (pokemonViewModel.confidence.value < 95) {
                                            pokemonViewModel.setConfidence(pokemonViewModel.confidence.value + 5)
                                        }
                                    },
                                    onSubtractCon = {
                                        if (pokemonViewModel.confidence.value > 0) {
                                            pokemonViewModel.setConfidence(pokemonViewModel.confidence.value - 5)
                                        }
                                    },
                                    onDelegateChange = {
                                        classifier?.currentDelegate = it
                                        delegate = it
                                        classifier?.clear()
                                    }
                                )
                            }
                        }) {
                        //in Jetpack compose, we can inflate legacy xml layouts
                        AndroidView(factory = {
                            View.inflate(it, R.layout.camera_scan, null)
                        }, modifier = Modifier
                            .fillMaxSize()
                            .background(androidx.compose.ui.graphics.Color.Yellow),
                            //we can use the update method to get references to Views within the layouts
                            update = {
                                val viewFinder =
                                    it.findViewById<PreviewView>(R.id.view_finder)
                                val progress =
                                    it.findViewById<ContentLoadingProgressBar>(R.id.progress_circular)
                                overlay =
                                    it.findViewById(R.id.overlay)

                                overlay.setConfidenceThreshold(pokemonViewModel.confidence)
                                overlay.setOnButtonClickListener(object :
                                    OverlayView.OnLabelClickListener {
                                    override fun onLabelClicked(name: String) {
                                        //set the name to the viewModel
                                        pokemonViewModel.setName(name)
                                        //navigate to the Detail screen
                                        navController.navigate(Screen.Detail.route)
                                    }
                                })
                                var imageRotationDegrees = 0

                                classifier = ImageClassificationHelper(
                                    context,
                                    object : ImageClassificationHelper.DetectorListener {
                                        override fun onInitialized() {
                                            viewFinder.post {
                                                cameraProviderFuture.addListener(
                                                    {
                                                        // CameraProvider
                                                        val cProvider =
                                                            cameraProvider
                                                                ?: throw IllegalStateException(
                                                                    "Camera initialization failed."
                                                                )

                                                        // CameraSelector - makes assumption that we're only using the back camera
                                                        cameraSelector =
                                                            CameraSelector.Builder()
                                                                .requireLensFacing(
                                                                    CameraSelector.LENS_FACING_BACK
                                                                ).build()

                                                        // Preview. Only using the 4:3 ratio because this is the closest to the models
                                                        preview =
                                                            Preview.Builder()
                                                                .setTargetAspectRatio(
                                                                    AspectRatio.RATIO_4_3
                                                                )
                                                                .setTargetRotation(
                                                                    viewFinder.display.rotation
                                                                )
                                                                .build()

                                                        // ImageAnalysis. Using RGBA 8888 to match how our models work
                                                        imageAnalyzer =
                                                            ImageAnalysis.Builder()
                                                                .setTargetAspectRatio(
                                                                    AspectRatio.RATIO_4_3
                                                                )
                                                                .setTargetRotation(
                                                                    viewFinder.display.rotation
                                                                )
                                                                .setBackpressureStrategy(
                                                                    ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST
                                                                )
                                                                .setOutputImageFormat(
                                                                    ImageAnalysis.OUTPUT_IMAGE_FORMAT_RGBA_8888
                                                                )
                                                                .build()
                                                                // The analyzer can then be assigned to the instance
                                                                .also { analyzer ->
                                                                    analyzer.setAnalyzer(
                                                                        executor
                                                                    ) { image ->
                                                                        if (bitmapBuffer == null) {
                                                                            // The image rotation and RGB image buffer are initialized only once
                                                                            // the analyzer has started running
                                                                            imageRotationDegrees =
                                                                                image.imageInfo.rotationDegrees
                                                                            bitmapBuffer =
                                                                                Bitmap.createBitmap(
                                                                                    image.width,
                                                                                    image.height,
                                                                                    Bitmap.Config.ARGB_8888
                                                                                )
                                                                        }


                                                                        // Copy out RGB bits to our shared buffer
                                                                        image.use {
                                                                            bitmapBuffer?.copyPixelsFromBuffer(
                                                                                image.planes[0].buffer
                                                                            )
                                                                        }

                                                                        // Perform the image classification for the current frame
                                                                        classifier?.classify(
                                                                            bitmapBuffer!!,
                                                                            imageRotationDegrees
                                                                        )

                                                                    }
                                                                }

                                                        // Must unbind the use-cases before rebinding them
                                                        cProvider.unbindAll()

                                                        try {
                                                            // A variable number of use-cases can be passed here -
                                                            // camera provides access to CameraControl & CameraInfo
                                                            cProvider.bindToLifecycle(
                                                                lifecycleOwner,
                                                                cameraSelector!!,
                                                                preview,
                                                                imageAnalyzer
                                                            )

                                                            // Attach the viewfinder's surface provider to preview use case
                                                            preview?.setSurfaceProvider(
                                                                viewFinder.surfaceProvider
                                                            )
                                                        } catch (exc: Exception) {
                                                            exc.printStackTrace()
                                                        }
                                                    },
                                                    executor
                                                )
                                            }
                                            progress.visibility = View.GONE
                                        }

                                        override fun onError(error: String) {
                                            // can't interact with ui outside the main thread
                                            CoroutineScope(Dispatchers.Main).launch {
                                                progress.visibility = View.GONE
                                                Toast.makeText(
                                                    context,
                                                    error,
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                if (error == "GPU is not supported on this device.")
                                                    delegate = 0
                                            }
                                        }

                                        override fun onResults(
                                            results: List<ImageClassificationHelper.Recognition>?,
                                            inferenceTime: Long
                                        ) {
                                            overlay.setResults(results)
                                            infTime = inferenceTime
                                        }

                                    }
                                )
                            }
                        )
                    }
                }
            } else {
                Column(
                    Modifier
                        .fillMaxSize()
                        .padding(12.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    val textToShow = if (cameraPermissionState.shouldShowRationale) {
                        // If the user has denied the permission but the rationale can be shown,
                        // then gently explain why the app requires this permission
                        "The camera is important for this feature. Please grant the permission."
                    } else {
                        // If it's the first time the user lands on this feature, or the user
                        // doesn't want to be asked again for this permission, explain that the
                        // permission is required
                        "Camera permission required for this feature to be available. " +
                                "Please grant the permission"
                    }
                    Text(textToShow)
                    Button(onClick = { cameraPermissionState.launchPermissionRequest() }) {
                        Text("Request permission")
                    }
                }
            }
        }
    }
}