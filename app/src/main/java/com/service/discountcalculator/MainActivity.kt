package com.service.discountcalculator

import PreferencesHelper
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.LocalIndication
import androidx.compose.foundation.background
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.service.discountcalculator.ui.theme.DiscountCalculatorTheme
import kotlinx.coroutines.launch
import java.math.BigDecimal

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            DiscountCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    DiscountCalculatorApp()
                }
            }
        }
    }
}

@Composable
fun DiscountCalculatorApp(modifier: Modifier = Modifier) {

    val context = LocalContext.current
    val preferencesHelper = remember { PreferencesHelper(context) }

    var actualPrice = rememberSaveable { mutableStateOf("") }
    var quantity = rememberSaveable { mutableStateOf("") }
    var discountPercentage = rememberSaveable { mutableStateOf("") }
    var taxPercentage = rememberSaveable { mutableStateOf("") }
    var discountedPrice = rememberSaveable { mutableStateOf("0.00") }
    var amountSaved = rememberSaveable { mutableStateOf("0.00") }
    var finalPrice = rememberSaveable { mutableStateOf("0.00") }

    var amountSavedValue = BigDecimal(0)
    var discountedPriceValue = BigDecimal(0)
    var finalPriceValue = BigDecimal(0)

    val selectedColorScheme =
        remember { mutableStateOf(preferencesHelper.getSetting("colorScheme", "Blue")) }
    val colorScheme = remember { mutableStateOf(getColorScheme(selectedColorScheme.value)) }

    val isCurrencySymbolOn =
        remember { mutableStateOf(preferencesHelper.getSetting("isCurrencySymbolOn", false)) }
    val isCommaSeparatorOn =
        remember { mutableStateOf(preferencesHelper.getSetting("isCommaSeparatorOn", false)) }
    val selectedCurrency =
        remember { mutableStateOf(preferencesHelper.getSetting("currency", "Auto Detect Locale")) }

    fun applyColorScheme() {
        colorScheme.value = getColorScheme(selectedColorScheme.value)
        preferencesHelper.saveSetting("colorScheme", selectedColorScheme.value)
    }

    fun applyCurrencyFormat() {
        var currencyCode = "Auto Detect Locale"
        if (!selectedCurrency.value.equals("Auto Detect Locale")) {
            currencyCode = selectedCurrency.value.split("-")[1].trim()
        }

        amountSaved.value = CurrencyFormat().formatAccordingToCurrency(
            amountSavedValue.setScale(2, BigDecimal.ROUND_HALF_EVEN),
            currencyCode,
            isCurrencySymbolOn.value,
            isCommaSeparatorOn.value
        )

        discountedPrice.value = CurrencyFormat().formatAccordingToCurrency(
            discountedPriceValue.setScale(2, BigDecimal.ROUND_HALF_EVEN),
            currencyCode,
            isCurrencySymbolOn.value,
            isCommaSeparatorOn.value
        )

        finalPrice.value = CurrencyFormat().formatAccordingToCurrency(
            finalPriceValue.setScale(2, BigDecimal.ROUND_HALF_EVEN),
            currencyCode,
            isCurrencySymbolOn.value,
            isCommaSeparatorOn.value
        )
        preferencesHelper.saveSetting("currency", selectedCurrency.value)
        preferencesHelper.saveSetting("isCurrencySymbolOn", isCurrencySymbolOn.value)
        preferencesHelper.saveSetting("isCommaSeparatorOn", isCommaSeparatorOn.value)
    }

    fun calculateDiscount() {
        val price = actualPrice.value.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val qty = quantity.value.toBigDecimalOrNull() ?: BigDecimal.ONE
        val discount = discountPercentage.value.toBigDecimalOrNull() ?: BigDecimal.ZERO
        val tax = taxPercentage.value.toBigDecimalOrNull() ?: BigDecimal.ZERO

        val totalActualPrice = price.multiply(qty)
        amountSavedValue = totalActualPrice.multiply(discount.divide(BigDecimal(100)))
        discountedPriceValue = totalActualPrice.subtract(amountSavedValue)
        finalPriceValue =
            discountedPriceValue.multiply(BigDecimal.ONE.add(tax.divide(BigDecimal(100))))

        if (isCurrencySymbolOn.value) {
            applyCurrencyFormat()
        } else {
            amountSaved.value = amountSavedValue.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()
            discountedPrice.value =
                discountedPriceValue.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()
            finalPrice.value = finalPriceValue.setScale(2, BigDecimal.ROUND_HALF_EVEN).toString()
        }
    }

    UIHandler(
        actualPrice = actualPrice,
        quantity = quantity,
        discountPercentage = discountPercentage,
        taxPercentage = taxPercentage,
        amountSaved = amountSaved,
        discountedPrice = discountedPrice,
        finalPrice = finalPrice,
        selectedColorScheme = selectedColorScheme,
        selectedCurrency = selectedCurrency,
        appearance = colorScheme,
        isCurrencySymbolOn = isCurrencySymbolOn,
        isCommaSeparatorOn = isCommaSeparatorOn,
        onInputChange = ::calculateDiscount,
        onColorSchemeChange = ::applyColorScheme,
        onCurrencyChange = ::applyCurrencyFormat,
        modifier = Modifier
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun UIHandler(
    actualPrice: MutableState<String>,
    quantity: MutableState<String>,
    discountPercentage: MutableState<String>,
    taxPercentage: MutableState<String>,
    amountSaved: MutableState<String>,
    discountedPrice: MutableState<String>,
    finalPrice: MutableState<String>,
    selectedColorScheme: MutableState<String>,
    selectedCurrency: MutableState<String>,
    appearance: MutableState<Appearance>,
    isCurrencySymbolOn: MutableState<Boolean>,
    isCommaSeparatorOn: MutableState<Boolean>,
    onInputChange: () -> Unit,
    onColorSchemeChange: () -> Unit,
    onCurrencyChange: () -> Unit,
    modifier: Modifier
) {

    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    var colorSchemeDropdownExpanded by remember { mutableStateOf(false) }
    var currencyDropdownExpanded by remember { mutableStateOf(false) }

    val colorSchemeOptions = listOf("Red", "Blue", "Green", "Pink", "Dark")
    val currencyOptions = listOf(
        "Auto Detect Locale",
        "Rupee (₹) - INR",
        "Dollar ($) - USD",
        "Pound (£) - GBP",
        "Ruble (₽) - RUB",
        "Euro (€) - EUR",
        "Yen (¥) - JPY"
    )

    val interactionSource = remember { MutableInteractionSource() }
    val indication = LocalIndication.current

    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    LaunchedEffect(drawerState.isOpen) {
        if (drawerState.isOpen) {
            focusManager.clearFocus()
        }
    }

    ModalNavigationDrawer(
        modifier = Modifier.background(appearance.value.backgroundColor),
        drawerState = drawerState,
        gesturesEnabled = false,
        drawerContent = {
            ModalDrawerSheet(modifier = Modifier.fillMaxHeight()
                .verticalScroll(rememberScrollState()))
            {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Preferences", style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.padding(vertical = 16.dp)
                    )
                    IconButton(onClick = { scope.launch { drawerState.close() } }) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close Drawer")
                    }
                }
                Text(
                    text = "Appearance",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.value.inputFieldsFontColor,
                    modifier = Modifier.padding(8.dp)
                )
                ExposedDropdownMenuBox(
                    expanded = colorSchemeDropdownExpanded,
                    onExpandedChange = {
                        colorSchemeDropdownExpanded = !colorSchemeDropdownExpanded
                    }
                ) {
                    TextField(
                        value = selectedColorScheme.value,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Select Colour Scheme", fontSize = 14.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = colorSchemeDropdownExpanded
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .onFocusChanged { keyboardController?.hide() }
                    )
                    ExposedDropdownMenu(
                        expanded = colorSchemeDropdownExpanded,
                        onDismissRequest = { colorSchemeDropdownExpanded = false }
                    ) {
                        colorSchemeOptions.forEach { colorOption ->
                            DropdownMenuItem(
                                text = { Text(text = colorOption) },
                                onClick = {
                                    selectedColorScheme.value = colorOption
                                    onColorSchemeChange()
                                    colorSchemeDropdownExpanded = false
                                },
                                modifier = Modifier
                                    .padding(5.dp)
                                    .height(30.dp)
                                    .indication(interactionSource, indication)
                            )
                        }
                    }
                }

                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "Number Format",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.value.inputFieldsFontColor,
                    modifier = Modifier.padding(8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = currencyDropdownExpanded,
                    onExpandedChange = { currencyDropdownExpanded = !currencyDropdownExpanded }
                ) {
                    TextField(
                        value = selectedCurrency.value,
                        onValueChange = { onCurrencyChange() },
                        readOnly = true,
                        label = { Text("Select Currency", fontSize = 14.sp) },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                expanded = currencyDropdownExpanded
                            )
                        },
                        colors = TextFieldDefaults.textFieldColors(
                            containerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent,
                            disabledIndicatorColor = Color.Transparent
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                            .onFocusChanged { keyboardController?.hide() }
                    )
                    ExposedDropdownMenu(
                        expanded = currencyDropdownExpanded,
                        onDismissRequest = { currencyDropdownExpanded = false }
                    ) {
                        currencyOptions.forEach { currency ->
                            DropdownMenuItem(
                                text = { Text(text = currency) },
                                onClick = {
                                    selectedCurrency.value = currency
                                    onCurrencyChange()
                                    currencyDropdownExpanded = false
                                },
                                modifier = Modifier
                                    .padding(5.dp)
                                    .height(30.dp)
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(text = "Show Currency Symbol:")
                    Switch(
                        checked = isCurrencySymbolOn.value,
                        onCheckedChange = {
                            isCurrencySymbolOn.value = it
                            onCurrencyChange()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                )
                {
                    Text(text = "Show Comma Separator:")
                    Switch(
                        checked = isCommaSeparatorOn.value,
                        onCheckedChange = {
                            isCommaSeparatorOn.value = it
                            onCurrencyChange()
                        },
                        colors = SwitchDefaults.colors(
                            checkedThumbColor = MaterialTheme.colorScheme.primary,
                            checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                            uncheckedThumbColor = MaterialTheme.colorScheme.secondary,
                            uncheckedTrackColor = MaterialTheme.colorScheme.secondaryContainer,
                        )
                    )
                }
            /* *** TO DO ***
                Divider(modifier = Modifier.padding(vertical = 8.dp))

                Text(
                    text = "App Experience",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = appearance.value.inputFieldsFontColor,
                    modifier = Modifier.padding(8.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(modifier = Modifier.fillMaxWidth(),
                    onClick = {
                        val uri = Uri.parse("market://details?id=${context.packageName}")
                        val goToMarketIntent = Intent(Intent.ACTION_VIEW, uri)
                        goToMarketIntent.addFlags(
                            Intent.FLAG_ACTIVITY_NO_HISTORY or
                                    Intent.FLAG_ACTIVITY_NEW_DOCUMENT or
                                    Intent.FLAG_ACTIVITY_MULTIPLE_TASK
                        )
                        try {
                            context.startActivity(goToMarketIntent)
                        } catch (e: ActivityNotFoundException) {
                            context.startActivity(
                                Intent(
                                    Intent.ACTION_VIEW,
                                    Uri.parse("http://xxxx.xxxx.com/store/apps/details?id=${context.packageName}")
                                )
                            )
                        }
                    }) {
                    Row (horizontalArrangement = Arrangement.Start) {
                        Text("Send Feedback")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(modifier = Modifier.fillMaxWidth(),
                    shape = RectangleShape,
                    onClick = {}) {
                    Text("About")
                }
                */
            }
        },
        content = {
            Column()
            {
                CenterAlignedTopAppBar(
                    title = {
                        Text(
                            "Discount Calculator Pro",
                            fontSize = 20.sp,
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Filled.Menu,
                                contentDescription = "Navigate to other features",
                                tint = Color.White
                            )
                        }
                    },
                    actions = {
                        IconButton(onClick = {
                            actualPrice.value = ""
                            discountPercentage.value = ""
                            quantity.value = ""
                            taxPercentage.value = ""
                            onInputChange()
                        }) {
                            Icon(
                                imageVector = Icons.Filled.Refresh,
                                tint = Color.White,
                                contentDescription = "Reset all the text input fields"
                            )
                        }
                    },
                    modifier = modifier
                        .fillMaxWidth()
                        .shadow(8.dp),
                    colors = TopAppBarDefaults.smallTopAppBarColors(
                        containerColor = appearance.value.topAppBarContainerColor
                    )
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(8.dp)
                        .verticalScroll(scrollState)
                ) {
                    ActualPriceAndDiscountInputField(
                        appearance = appearance,
                        actualPriceLabel = "Actual Price",
                        actualPriceState = actualPrice,
                        disPerLabel = "Discount (%)",
                        disPerState = discountPercentage,
                        onInputChangeOfActualPrice = {
                            actualPrice.value = it
                            onInputChange()
                        },
                        onInputChangeOfDisPer = {
                            discountPercentage.value = it
                            onInputChange()
                        })

                    QuantityAndTaxInputField(
                        appearance = appearance,
                        quantityLabel = "Quantity",
                        taxPerLabel = "Tax (%)",
                        quantityState = quantity,
                        taxPerState = taxPercentage,
                        onInputChangeOfQuantity = {
                            quantity.value = it
                            onInputChange()
                        },
                        onInputChangeOfTaxPer = {
                            taxPercentage.value = it
                            onInputChange()
                        })

                    ResultBars(
                        appearance,
                        amountSaved.value,
                        discountedPrice.value,
                        finalPrice.value
                    )
                }
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ActualPriceAndDiscountInputField(
    appearance: MutableState<Appearance>,
    actualPriceLabel: String,
    actualPriceState: MutableState<String>,
    disPerLabel: String,
    disPerState: MutableState<String>,
    onInputChangeOfActualPrice: (String) -> Unit,
    onInputChangeOfDisPer: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        TextField(
            label = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = actualPriceLabel,
                        fontSize = appearance.value.inputFieldLabelsFontSize,
                        color = appearance.value.inputFieldsLabelColor,
                    )
                }
            },
            value = actualPriceState.value.trim(),
            onValueChange = {
                try {
                    if (it.isNotBlank()) {
                        val decimalIndex = it.indexOf('.')
                        if (decimalIndex != -1 && it.substring(decimalIndex).length > 3) {
                            return@TextField
                        }
                        if (it.length >= 1 && it.length <= 13) {
                            actualPriceState.value = it.toDouble().toString()
                            onInputChangeOfActualPrice(it)
                        }
                    } else {
                        actualPriceState.value = ""
                        onInputChangeOfActualPrice(it)
                    }
                } catch (e: NumberFormatException) {
                    println("Error: ${e.message}")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = appearance.value.inputFieldsFontSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = appearance.value.inputFieldsFontColor,
                lineHeight = 20.sp
            ),
            singleLine = true,
            trailingIcon = {
                if (actualPriceState.value.isNotBlank()) {
                    IconButton(onClick = { onInputChangeOfActualPrice("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            tint = Color.DarkGray,
                            contentDescription = "Clear text"
                        )
                    }
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .weight(2f)
                .height(65.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        TextField(
            value = disPerState.value.trim(),
            label = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = disPerLabel,
                        fontSize = appearance.value.inputFieldLabelsFontSize,
                        color = appearance.value.inputFieldsLabelColor,
                    )
                }
            },
            onValueChange = {
                try {
                    if (it.isNotBlank()) {
                        val decimalIndex = it.indexOf('.')
                        if (decimalIndex != -1 && it.substring(decimalIndex).length > 3) {
                            return@TextField
                        }
                        if (it.toFloat() >= 0 && it.toFloat() <= 100) {
                            disPerState.value = it.toFloat().toString()
                            onInputChangeOfDisPer(it)
                        }
                    } else {
                        disPerState.value = ""
                        onInputChangeOfDisPer(it)
                    }
                } catch (e: NumberFormatException) {
                    println("Error: ${e.message}")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = appearance.value.inputFieldsFontSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = appearance.value.inputFieldsFontColor
            ),
            singleLine = true,
            trailingIcon = {
                if (disPerState.value.isNotBlank()) {
                    IconButton(onClick = { onInputChangeOfDisPer("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            tint = Color.DarkGray,
                            contentDescription = "Clear text"
                        )
                    }
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .weight(1.6f)
                .height(65.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuantityAndTaxInputField(
    appearance: MutableState<Appearance>,
    quantityLabel: String,
    taxPerLabel: String,
    quantityState: MutableState<String>,
    taxPerState: MutableState<String>,
    onInputChangeOfQuantity: (String) -> Unit,
    onInputChangeOfTaxPer: (String) -> Unit
) {
    Spacer(modifier = Modifier.height(10.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        TextField(
            value = quantityState.value.trim(),
            label = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = quantityLabel,
                        fontSize = appearance.value.inputFieldLabelsFontSize,
                        color = appearance.value.inputFieldsLabelColor,
                    )
                }
            },
            onValueChange = {
                try {
                    if (it.isNotBlank()) {
                        val decimalIndex = it.indexOf('.')
                        if (decimalIndex != -1 && it.substring(decimalIndex).length > 3) {
                            return@TextField
                        }
                        if (it.toFloat() >= 0 && it.toFloat() <= 1000) {
                            quantityState.value = it.toDouble().toString()
                            onInputChangeOfQuantity(it)
                        }
                    } else {
                        quantityState.value = ""
                        onInputChangeOfQuantity(it)
                    }
                } catch (e: NumberFormatException) {
                    println("Error: ${e.message}")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = appearance.value.inputFieldsFontSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = appearance.value.inputFieldsFontColor
            ),
            singleLine = true,
            trailingIcon = {
                if (quantityState.value.isNotBlank()) {
                    IconButton(onClick = { onInputChangeOfQuantity("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            tint = Color.DarkGray,
                            contentDescription = "Clear text"
                        )
                    }
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .weight(1f)
                .height(65.dp)
        )

        Spacer(modifier = Modifier.width(16.dp))

        TextField(
            value = taxPerState.value.trim(),
            label = {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = taxPerLabel,
                        fontSize = appearance.value.inputFieldLabelsFontSize,
                        color = appearance.value.inputFieldsLabelColor,
                    )
                }
            },
            onValueChange = {
                try {
                    if (it.isNotBlank()) {
                        val decimalIndex = it.indexOf('.')
                        if (decimalIndex != -1 && it.substring(decimalIndex).length > 3) {
                            return@TextField
                        }
                        if (it.toFloat() >= 0 && it.toFloat() <= 100) {
                            taxPerState.value = it.toFloat().toString()
                            onInputChangeOfTaxPer(it)
                        }
                    } else {
                        taxPerState.value = ""
                        onInputChangeOfTaxPer(it)
                    }
                } catch (e: NumberFormatException) {
                    println("Error: ${e.message}")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            textStyle = TextStyle(
                fontSize = appearance.value.inputFieldsFontSize,
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                color = appearance.value.inputFieldsFontColor
            ),
            singleLine = true,
            trailingIcon = {
                if (taxPerState.value.isNotBlank()) {
                    IconButton(onClick = { onInputChangeOfTaxPer("") }) {
                        Icon(
                            imageVector = Icons.Filled.Clear,
                            tint = Color.DarkGray,
                            contentDescription = "Clear text"
                        )
                    }
                }
            },
            colors = TextFieldDefaults.textFieldColors(
                containerColor = Color.Transparent
            ),
            modifier = Modifier
                .weight(1f)
                .height(65.dp)
        )
    }
}

@Composable
fun ResultBars(
    appearance: MutableState<Appearance>,
    amountSaved: String,
    discountedPrice: String,
    finalPrice: String
) {
    Spacer(modifier = Modifier.height(10.dp))
    Column() {
        AmountSavedResultBar(appearance = appearance, label = "Amount Saved", value = amountSaved)
        Spacer(modifier = Modifier.height(3.dp))
        DiscountedPriceResultBar(
            appearance = appearance,
            label = "Discounted Price",
            value = discountedPrice
        )
        Spacer(modifier = Modifier.height(3.dp))
        FinalPriceResultBar(
            appearance = appearance,
            label = "Final Price (Incl. Tax)",
            value = finalPrice
        )
    }
}

@Composable
fun AmountSavedResultBar(appearance: MutableState<Appearance>, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color = appearance.value.resultBarsContainerColor[0]
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = appearance.value.inputFieldLabelsFontSize,
                color = Color.White
            )
            Text(
                text = value,
                fontSize = appearance.value.inputFieldsFontSize,
                modifier = Modifier.padding(vertical = 3.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun DiscountedPriceResultBar(appearance: MutableState<Appearance>, label: String, value: String) {
    Surface(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RectangleShape,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color = appearance.value.resultBarsContainerColor[1]
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = appearance.value.inputFieldLabelsFontSize,
                modifier = Modifier.padding(vertical = 2.dp),
                color = Color.White
            )
            Text(
                text = value,
                fontSize = appearance.value.inputFieldsFontSize,
                modifier = Modifier.padding(vertical = 3.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Composable
fun FinalPriceResultBar(appearance: MutableState<Appearance>, label: String, value: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RectangleShape,
        tonalElevation = 8.dp,
        shadowElevation = 16.dp,
        color = appearance.value.resultBarsContainerColor[2]
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = label,
                fontSize = appearance.value.inputFieldLabelsFontSize,
                modifier = Modifier.padding(vertical = 2.dp),
                color = Color.White
            )

            Text(
                text = value,
                fontSize = appearance.value.inputFieldsFontSize,
                modifier = Modifier.padding(vertical = 3.dp),
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DiscountCalculatorAppPreview() {
    DiscountCalculatorTheme {
        DiscountCalculatorApp()
    }
}