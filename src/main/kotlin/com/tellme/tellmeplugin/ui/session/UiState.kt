package com.tellme.tellmeplugin.ui.session

/**
 * Represents the current state of a file analysis session.
 */
enum class UiState {
    /** Initial state, no analysis started */
    IDLE,
    /** Waiting in queue for analysis to start */
    WAITING,
    /** Analysis in progress, streaming tokens */
    LOADING,
    /** Analysis completed successfully */
    DONE,
    /** Analysis failed with an error */
    ERROR
}
