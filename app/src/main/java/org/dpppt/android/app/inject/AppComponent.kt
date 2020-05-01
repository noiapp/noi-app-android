package org.dpppt.android.app.inject

import dagger.Component
import org.dpppt.android.app.MainApplication
import org.dpppt.android.app.contacts.ContactsFragment
import org.dpppt.android.app.main.MainFragment
import org.dpppt.android.app.onboarding.OnboardingActivity
import org.dpppt.android.app.trigger.TriggerFragment
import javax.inject.Singleton

@Singleton
@Component(modules = [
    AppModule::class,
    ViewModelBuilder::class])
interface AppComponent {

    fun inject(mainApplication: MainApplication)

    fun inject(contactsFragment: ContactsFragment)

    fun inject(mainFragment: MainFragment)

    fun inject(onboardingActivity: OnboardingActivity)

    fun inject(triggerFragment: TriggerFragment)
}