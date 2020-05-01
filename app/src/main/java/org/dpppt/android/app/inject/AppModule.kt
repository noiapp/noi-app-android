package org.dpppt.android.app.inject

import android.app.Application
import androidx.lifecycle.ViewModel
import dagger.Module
import dagger.Provides
import dagger.multibindings.IntoMap
import org.dpppt.android.app.main.TracingViewModel
import org.dpppt.android.app.sdk.DP3T
import org.dpppt.android.app.sdk.DP3TImpl
import javax.inject.Singleton

@Module
class AppModule(private val application: Application) {

    @Provides
    fun provideApplication(): Application = application

    @Provides
    @Singleton
    fun provideDP3T(impl: DP3TImpl): DP3T = impl

    @Provides
    @IntoMap
    @ViewModelKey(TracingViewModel::class)
    fun provideTracingViewModel(impl: TracingViewModel): ViewModel = impl
}