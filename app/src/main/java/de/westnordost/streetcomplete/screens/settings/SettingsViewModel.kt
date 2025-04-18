package de.westnordost.streetcomplete.screens.settings

import android.content.res.Resources
import androidx.compose.runtime.Stable
import androidx.lifecycle.ViewModel
import com.russhwolf.settings.SettingsListener
import de.westnordost.streetcomplete.R
import de.westnordost.streetcomplete.data.Cleaner
import de.westnordost.streetcomplete.data.preferences.Autosync
import de.westnordost.streetcomplete.data.preferences.Preferences
import de.westnordost.streetcomplete.data.preferences.ResurveyIntervals
import de.westnordost.streetcomplete.data.preferences.Theme
import de.westnordost.streetcomplete.data.quest.QuestKey
import de.westnordost.streetcomplete.data.quest.QuestType
import de.westnordost.streetcomplete.data.quest.QuestTypeRegistry
import de.westnordost.streetcomplete.data.visiblequests.QuestPreset
import de.westnordost.streetcomplete.data.visiblequests.QuestPresetsSource
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenController
import de.westnordost.streetcomplete.data.visiblequests.QuestsHiddenSource
import de.westnordost.streetcomplete.data.visiblequests.VisibleQuestTypeSource
import de.westnordost.streetcomplete.util.ktx.getYamlObject
import de.westnordost.streetcomplete.util.ktx.launch
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@Stable
abstract class SettingsViewModel : ViewModel() {
    abstract val selectableLanguageCodes: StateFlow<List<String>?>
    abstract val selectedQuestPresetName: StateFlow<String?>
    abstract val hiddenQuestCount: StateFlow<Int>
    abstract val questTypeCount: StateFlow<QuestTypeCount?>

    abstract val resurveyIntervals: StateFlow<ResurveyIntervals>
    abstract val showAllNotes: StateFlow<Boolean>
    abstract val autosync: StateFlow<Autosync>
    abstract val theme: StateFlow<Theme>
    abstract val keepScreenOn: StateFlow<Boolean>
    abstract val selectedLanguage: StateFlow<String?>

    abstract fun unhideQuests()

    abstract fun deleteCache()

    abstract fun setResurveyIntervals(value: ResurveyIntervals)
    abstract fun setShowAllNotes(value: Boolean)
    abstract fun setAutosync(value: Autosync)
    abstract fun setTheme(value: Theme)
    abstract fun setKeepScreenOn(value: Boolean)
    abstract fun setSelectedLanguage(value: String?)
}

data class QuestTypeCount(val total: Int, val enabled: Int)

@Stable
class SettingsViewModelImpl(
    private val prefs: Preferences,
    private val resources: Resources,
    private val cleaner: Cleaner,
    private val hiddenQuestsController: QuestsHiddenController,
    private val questTypeRegistry: QuestTypeRegistry,
    private val visibleQuestTypeSource: VisibleQuestTypeSource,
    private val questPresetsSource: QuestPresetsSource,
) : SettingsViewModel() {

    private val visibleQuestTypeListener = object : VisibleQuestTypeSource.Listener {
        override fun onQuestTypeVisibilityChanged(questType: QuestType, visible: Boolean) { updateQuestTypeCount() }
        override fun onQuestTypeVisibilitiesChanged() { updateQuestTypeCount() }
    }

    private val questPresetsListener = object : QuestPresetsSource.Listener {
        override fun onSelectedQuestPresetChanged() { updateSelectedQuestPreset() }
        override fun onAddedQuestPreset(preset: QuestPreset) { updateSelectedQuestPreset() }
        override fun onRenamedQuestPreset(preset: QuestPreset) { updateSelectedQuestPreset() }
        override fun onDeletedQuestPreset(presetId: Long) { updateSelectedQuestPreset() }
    }

    private val hiddenQuestsListener = object : QuestsHiddenSource.Listener {
        override fun onHid(key: QuestKey, timestamp: Long) { updateHiddenQuests() }
        override fun onUnhid(key: QuestKey, timestamp: Long) { updateHiddenQuests() }
        override fun onUnhidAll() { updateHiddenQuests() }
    }

    override val hiddenQuestCount = MutableStateFlow(0)
    override val questTypeCount = MutableStateFlow<QuestTypeCount?>(null)
    override val selectedQuestPresetName = MutableStateFlow<String?>(null)
    override val selectableLanguageCodes = MutableStateFlow<List<String>?>(null)

    override val resurveyIntervals = MutableStateFlow(prefs.resurveyIntervals)
    override val autosync = MutableStateFlow(prefs.autosync)
    override val theme = MutableStateFlow(prefs.theme)
    override val showAllNotes = MutableStateFlow(prefs.showAllNotes)
    override val keepScreenOn = MutableStateFlow(prefs.keepScreenOn)
    override val selectedLanguage = MutableStateFlow(prefs.language)

    private val listeners = mutableListOf<SettingsListener>()

    init {
        visibleQuestTypeSource.addListener(visibleQuestTypeListener)
        questPresetsSource.addListener(questPresetsListener)
        hiddenQuestsController.addListener(hiddenQuestsListener)

        listeners += prefs.onResurveyIntervalsChanged { resurveyIntervals.value = it }
        listeners += prefs.onAutosyncChanged { autosync.value = it }
        listeners += prefs.onThemeChanged { theme.value = it }
        listeners += prefs.onAllShowNotesChanged { showAllNotes.value = it }
        listeners += prefs.onKeepScreenOnChanged { keepScreenOn.value = it }
        listeners += prefs.onLanguageChanged { selectedLanguage.value = it }

        updateQuestTypeCount()
        updateSelectableLanguageCodes()
        updateHiddenQuests()
        updateSelectedQuestPreset()
    }

    override fun onCleared() {
        visibleQuestTypeSource.removeListener(visibleQuestTypeListener)
        questPresetsSource.removeListener(questPresetsListener)
        hiddenQuestsController.removeListener(hiddenQuestsListener)

        listeners.forEach { it.deactivate() }
        listeners.clear()
    }

    override fun deleteCache() {
        cleaner.cleanAll()
    }

    override fun setResurveyIntervals(value: ResurveyIntervals) { prefs.resurveyIntervals = value }
    override fun setShowAllNotes(value: Boolean) { prefs.showAllNotes = value }
    override fun setAutosync(value: Autosync) { prefs.autosync = value }
    override fun setTheme(value: Theme) { prefs.theme = value }
    override fun setKeepScreenOn(value: Boolean) { prefs.keepScreenOn = value }
    override fun setSelectedLanguage(value: String?) { prefs.language = value }

    override fun unhideQuests() {
        launch(IO) {
            hiddenQuestsController.unhideAll()
        }
    }

    private fun updateSelectedQuestPreset() {
        launch(IO) {
            selectedQuestPresetName.value = questPresetsSource.selectedQuestPresetName
        }
    }

    private fun updateSelectableLanguageCodes() {
        launch(IO) {
            selectableLanguageCodes.value = resources.getYamlObject<List<String>>(R.raw.languages)
        }
    }

    private fun updateHiddenQuests() {
        launch(IO) {
            hiddenQuestCount.value = hiddenQuestsController.countAll()
        }
    }

    private fun updateQuestTypeCount() {
        launch(IO) {
            questTypeCount.value = QuestTypeCount(
                total = questTypeRegistry.size,
                enabled = questTypeRegistry.count { visibleQuestTypeSource.isVisible(it) }
            )
        }
    }
}
