package de.westnordost.streetcomplete.screens.about

import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import de.westnordost.streetcomplete.data.changelog.Changelog
import de.westnordost.streetcomplete.util.html.HtmlNode
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class ChangelogViewModel : ViewModel() {
    /* version name -> html */
    abstract val changelog: StateFlow<Map<String, List<HtmlNode>>?>
}

@Stable
class ChangelogViewModelImpl(private val changes: Changelog) : ChangelogViewModel() {
    override val changelog = MutableStateFlow<Map<String, List<HtmlNode>>?>(null)

    init {
        launch {
            changelog.value = changes.getChangelog()
        }
    }
}
