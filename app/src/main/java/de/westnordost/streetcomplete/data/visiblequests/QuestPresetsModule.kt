package de.westnordost.streetcomplete.data.visiblequests

import org.koin.dsl.module

val questPresetsModule = module {
    factory { QuestPresetsDao(get()) }
    factory { QuestTypeOrderDao(get()) }
    factory { VisibleQuestTypeDao(get()) }

    single<QuestPresetsSource> { get<QuestPresetsController>() }
    single { QuestPresetsController(get(), get()) }

    single<QuestTypeOrderSource> { get<QuestTypeOrderController>() }
    single { QuestTypeOrderController(get(), get(), get()) }

    single { TeamModeQuestFilter(get(), get()) }

    single<QuestsHiddenSource> { get<QuestsHiddenController>() }
    single { QuestsHiddenController(get(), get()) }

    single<VisibleQuestTypeSource> { get<VisibleQuestTypeController>() }
    single { VisibleQuestTypeController(get(), get(), get()) }
}
