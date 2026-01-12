package com.talsk.amadz.ui.home.calllogs

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CountryListStickyHeaderScreen() {

    val countries = listOf(
        // A
        "Afghanistan",
        "Albania",
        "Algeria",
        "Argentina",
        "Armenia",
        "Australia",
        "Austria",
        "Azerbaijan",

        // B
        "Bangladesh",
        "Belarus",
        "Belgium",
        "Bolivia",
        "Brazil",
        "Bulgaria",

        // C
        "Cambodia",
        "Canada",
        "Chile",
        "China",
        "Colombia",
        "Croatia",
        "Czech Republic",

        // D
        "Denmark",
        "Dominican Republic",

        // E
        "Ecuador",
        "Egypt",
        "Estonia",
        "Ethiopia",

        // F
        "Finland",
        "France",

        // G
        "Georgia",
        "Germany",
        "Ghana",
        "Greece",

        // H
        "Hungary",

        // I
        "Iceland",
        "India",
        "Indonesia",
        "Iran",
        "Ireland",
        "Israel",
        "Italy",

        // J
        "Japan",
        "Jordan",

        // K
        "Kazakhstan",
        "Kenya",
        "Kuwait",

        // L
        "Latvia",
        "Lebanon",
        "Lithuania",
        "Luxembourg",

        // M
        "Malaysia",
        "Mexico",
        "Morocco",

        // N
        "Nepal",
        "Netherlands",
        "New Zealand",
        "Nigeria",
        "Norway",

        // O
        "Oman",

        // P
        "Pakistan",
        "Peru",
        "Philippines",
        "Poland",
        "Portugal",

        // Q
        "Qatar",

        // R
        "Romania",
        "Russia",

        // S
        "Saudi Arabia",
        "Singapore",
        "Slovakia",
        "South Africa",
        "South Korea",
        "Spain",
        "Sri Lanka",
        "Sweden",
        "Switzerland",

        // T
        "Thailand",
        "Turkey",

        // U
        "Ukraine",
        "United Arab Emirates",
        "United Kingdom",
        "United States",

        // V
        "Vietnam",

        // Z
        "Zimbabwe"
    ).sorted()

    val grouped = countries.groupBy { it.first().uppercaseChar() }

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        grouped.forEach { (letter, countryList) ->

            stickyHeader {
                Text(
                    text = letter.toString(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    style = MaterialTheme.typography.titleMedium
                )
            }

            items(countryList) { country ->
                Text(
                    text = country,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}
