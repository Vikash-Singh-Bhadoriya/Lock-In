package com.vikashsinghapp.lockin.presentation.navigation

sealed class Screen(val route: String, val screenName: String) {
    data object OnboardingScreen : Screen("onboarding_screen", "OnBoarding")
    data object OnboardingReviewScreen : Screen("onboarding_review_screen", "OnBoarding Review")
    data object JournalScreen : Screen("journal_screen", "Journal")
    data object TomorrowFocusScreen : Screen("tomorrow_focus_screen", "Plan")
    data object TaskScreen : Screen("task_screen", "Task")

    data object SettingsScreen : Screen("settings_screen", "Settings")
    data object FeedbackScreen : Screen("feedback_screen", "Feedback")
    data object FAQScreen : Screen("faq_screen", "FAQ")
    data object AttributionScreen : Screen("attribution_screen", "Attributions")

    data object TaskDetailScreen : Screen("task_detail_screen", "Detail")
    data object TemplateEditorScreen : Screen("template_editor_screen", "Template")

    data object AnalyticsScreen : Screen("analytics_screen", "Analytics")
    data object ActiveFocusScreen : Screen("active_focus_screen", "Active Focus")
}