package plat.proyecto.guatevivo.paginas.login.ui.onboarding

sealed interface OnboardingEvent {
    data object OnGetStartedClicked : OnboardingEvent
}
