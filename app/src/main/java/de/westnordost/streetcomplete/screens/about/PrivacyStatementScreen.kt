package de.westnordost.streetcomplete.screens.about

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AppBarDefaults
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.ui.common.BackIcon
import de.westnordost.streetcomplete.ui.common.HtmlText
import de.westnordost.streetcomplete.util.html.tryParseHtml

/** Shows the privacy statement */
@Composable
fun PrivacyStatementScreen(
    onClickBack: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        TopAppBar(
            title = { Text(stringResource(R.string.about_title_privacy_statement)) },
            windowInsets = AppBarDefaults.topAppBarWindowInsets,
            navigationIcon = { IconButton(onClick = onClickBack) { BackIcon() } },
        )
        SelectionContainer {
            HtmlText(
                html =
                    tryParseHtml(stringResource(R.string.privacy_html)) +
                    tryParseHtml(stringResource(R.string.privacy_html_tileserver2, "JawgMaps", "https://www.jawg.io/en/confidentiality/")) +
                    tryParseHtml(stringResource(R.string.privacy_html_statistics)) +
                    tryParseHtml(stringResource(R.string.privacy_html_image_upload2)),
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .windowInsetsPadding(WindowInsets.safeDrawing.only(
                        WindowInsetsSides.Horizontal + WindowInsetsSides.Bottom
                    ))
                    .padding(16.dp),
                style = MaterialTheme.typography.body2,
            )
        }
    }
}
