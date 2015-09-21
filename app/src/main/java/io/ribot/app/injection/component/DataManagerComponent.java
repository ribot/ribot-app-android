package io.ribot.app.injection.component;

import dagger.Component;
import io.ribot.app.data.DataManager;
import io.ribot.app.injection.module.DataManagerModule;
import io.ribot.app.injection.scope.PerDataManager;

@PerDataManager
@Component(dependencies = ApplicationComponent.class, modules = DataManagerModule.class)
public interface DataManagerComponent {

    void inject(DataManager dataManager);
}
