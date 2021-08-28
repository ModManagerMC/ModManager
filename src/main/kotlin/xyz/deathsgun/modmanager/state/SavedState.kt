package xyz.deathsgun.modmanager.state

data class SavedState(
    val fabricId: String,
    val modId: String,
    val state: ModState
)
